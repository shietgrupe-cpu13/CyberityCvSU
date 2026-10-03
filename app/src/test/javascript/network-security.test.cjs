const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const base = path.resolve(__dirname,'../../main/assets/simulations/UNIT 05');
const folders = {501:'network_basics',502:'wifi_security',503:'safe_browsing',504:'network_threats',550:'rogue_ap',505:'firewall_ops'};
const modules = Object.fromEntries(Object.entries(folders).map(([id,folder])=>[id,require(path.join(base,folder,'script.js'))]));
const cases = Object.assign({},...Object.values(modules).map(lab=>lab.cases));
const {evaluateNetworkCase} = modules[505];
const {evaluateRuleOrder} = modules[505];
const correct = {
  repair:{policy:'portal'}, wifiUpgrade:{encryption:'transition',secret:'unique',wps:'off'},
  guests:{isolation:'segment'}, certificate:{decision:'stop'}, identity:{decision:'bookmark'},
  arp:{binding:'inspect'}, dns:{resolver:'approved',tls:'on'}, rogueSurvey:{radio:'99'},
  rogueReport:{report:'evidence'}, reconnect:{profile:'forget',network:'official'},
  firewall:{dns:'approved',web:'portal',default:'deny'}, order:{order:'narrow'},
  stateful:{state:'on',inbound:'deny'}
};
function combinations(controls, index = 0, value = {}) {
  if (index === controls.length) return [value];
  const [key,,options] = controls[index];
  return options.flatMap(([option]) => combinations(controls,index+1,{...value,[key]:option}));
}
for (const [level,stages] of Object.entries(cases)) stages.forEach((scenario,index) => {
  if (!scenario.controls) return;
  test(`${level}/${index}: only the policy satisfying all service and security criteria verifies`,() => {
    for (const config of combinations(scenario.controls)) {
      const result = modules[level].evaluateNetworkCase(scenario.mode,config);
      assert.equal(result.ok,JSON.stringify(config) === JSON.stringify(correct[scenario.mode]),JSON.stringify(config));
      assert.ok(result.lines.length);
    }
  });
});
test('bypassing browser identity protections reports a dangerous action',() => {
  assert.equal(evaluateNetworkCase('certificate',{decision:'bypass'}).unsafe,true);
  assert.equal(evaluateNetworkCase('identity',{decision:'login'}).unsafe,true);
  assert.ok(!evaluateNetworkCase('certificate',{decision:'stop'}).unsafe);
});
test('firewall broad exceptions show the exact packet exposure and rule-order failure',() => {
  const broad = evaluateNetworkCase('firewall',{dns:'any',web:'any',default:'deny'});
  assert.equal(broad.ok,false);
  assert.match(broad.lines[2],/ALLOW.*FAIL/);
  assert.match(broad.lines[3],/ALLOW.*FAIL/);
  const shadowed = evaluateNetworkCase('order',{order:'original'});
  assert.match(shadowed.lines[0],/ALLOW.*rule 1.*FAIL/);
  const missingState = evaluateNetworkCase('stateful',{state:'off',inbound:'deny'});
  assert.match(missingState.lines[1],/DENY.*FAIL/);
});

// Minimal DOM harness tests bridge behavior without adding runtime dependencies.
class Element {
  constructor(tag) {this.tag = tag; this.children = []; this.listeners = {}; this.attributes = {}; this.style = {}; this.textContent = ''; this.value = '';}
  set textContent(value) {this._text = value; this.children = [];}
  get textContent() {return this._text + this.children.map(child=>child.textContent).join('');}
  appendChild(child) {this.children.push(child); if (this.tag === 'select' && this.children.length === 1) this.value = child.value;}
  replaceChildren() {this.children = []; this._text = '';}
  setAttribute(key,value) {this.attributes[key] = value;}
  addEventListener(key,fn) {this.listeners[key] = fn;}
  dispatchEvent(event) {this.trigger(event.type);}
  scrollIntoView() {}
  trigger(key) {this.listeners[key]?.();}
}
function harness(level,index,options = {}) {
  const root = new Element('main'), events = [];
  const storage = options.storage || new Map();
  const window = options.window || {name:'',location:{search:''},sessionStorage:{getItem:key=>storage.get(key) ?? null,setItem:(key,value)=>storage.set(key,value)}};
  const context = vm.createContext({document:{getElementById:()=>root,createElement:tag=>new Element(tag)},
    window,URLSearchParams,Event,AndroidLab:{notifyClueFound:id=>events.push(id)}});
  vm.runInContext(fs.readFileSync(path.join(base,folders[level],'script.js'),'utf8'),context);
  vm.runInContext(index === -1 ? 'renderNetworkDashboard()' : `mountNetworkCase(${level},${index})`,context);
  function all(node = root) {return [node,...node.children.flatMap(child=>all(child))];}
  return {root,events,all,storage,window};
}
for (const [level,stages] of Object.entries(cases)) stages.forEach((scenario,index) => {
  test(`${level}/${index}: evidence gates replay; verification reaches the Android bridge`,() => {
    const previous = level === '505' && index === 2 ? [0,1] : level === '550' && index > 0 ? [index-1] : [];
    const storage = new Map();
    storage.set(`cyberity_net_${level}`,JSON.stringify({runs:previous.length,stages:Object.fromEntries(previous.map(i=>[i,{inspected:[],values:{},verified:true,lastResult:null}]))}));
    const h = harness(level,index,{storage});
    const run = h.all().find(n=>n.id === 'run-replay');
    run.trigger('click'); assert.deepEqual(h.events,[]);
    for (const detail of h.all().filter(n=>n.tag === 'details')) {detail.open=true; detail.trigger('toggle');}
    assert.deepEqual(h.events,[`net_${level}_${index}_inspected`]);
    for (const [key,value] of Object.entries(correct[scenario.mode] || {})) h.all().find(n=>n.id === `control-${key}`).value = value;
    run.trigger('click');
    assert.deepEqual(h.events,[`net_${level}_${index}_inspected`,`net_${level}_${index}_verified`]);
    if (level === '505' && index === 2) assert.match(h.all().find(n=>n.attributes.role === 'status').textContent,/CYBERITY\{stateful_campus_verified}/);
    assert.deepEqual(harness(level,index).events,[], 'A fresh attempt cannot inherit bridge evidence.');
  });
});
test('each console owns local CSS, JS and named tool pages',()=>{
  for (const [level,folder] of Object.entries(folders)) {
    const meta = modules[level].meta;
    for (const filename of ['desk.html',...meta.pages]) {
      const source = fs.readFileSync(path.join(base,folder,filename),'utf8');
      assert.match(source,/href="styles.css"/);
      assert.match(source,/src="script.js"/);
      assert.ok(!source.includes('../network_security/'));
    }
    const h = harness(level,-1);
    assert.equal(h.all().filter(n=>n.className === 'choice tile').length,3);
    const links = h.all().filter(n=>n.tag === 'a');
    for (const link of links) assert.ok(fs.existsSync(path.join(base,folder,link.href)));
  }
});
test('verified work survives tool navigation; a fresh console entry clears the attempt',()=>{
  const h = harness(505,0);
  for (const detail of h.all().filter(n=>n.tag === 'details')) {detail.open=true; detail.trigger('toggle');}
  for (const [key,value] of Object.entries(correct.firewall)) h.all().find(n=>n.id === `control-${key}`).value=value;
  h.all().find(n=>n.id === 'run-replay').trigger('click');
  const restored = harness(505,0,{storage:h.storage});
  assert.deepEqual(restored.events,['net_505_0_inspected','net_505_0_verified']);
  assert.equal(restored.all().find(n=>n.id === 'control-dns').value,'approved');
  const desk = harness(505,-1,{storage:h.storage});
  assert.match(desk.all().find(n=>n.className === 'stats').textContent,/1\/3/);
  desk.window.location.search='?fresh=1';
  harness(505,-1,{storage:h.storage,window:desk.window});
  assert.deepEqual(harness(505,0,{storage:h.storage}).events,[]);
});
test('window.name fallback retains evidence when file-origin storage is unavailable',()=>{
  const window = {name:'',location:{search:''},sessionStorage:{getItem:()=>{throw Error('unavailable');},setItem:()=>{throw Error('unavailable');}}};
  const h = harness(550,0,{window});
  for (const detail of h.all().filter(n=>n.tag === 'details')) {detail.open=true; detail.trigger('toggle');}
  assert.deepEqual(harness(550,0,{window}).events,['net_550_0_inspected']);
});
test('packet walk and firewall rule repair operate their dedicated tools',()=>{
  const trace = harness(501,0);
  const advance = trace.all().find(n=>n.textContent === 'ADVANCE ONE HOP');
  for (let i=0;i<4;i++) advance.trigger('click');
  assert.ok(trace.events.includes('net_501_0_inspected'));
  const firewall = harness(505,1);
  firewall.all().find(n=>n.textContent === 'REMOVE BROAD ALLOW RULE').trigger('click');
  assert.equal(firewall.all().find(n=>n.id === 'control-order').value,'narrow');
  assert.match(firewall.all().find(n=>n.className === 'rule-list').textContent,/exact|portal TCP 443/);
});
test('final operations report and rogue reconnect require verified preceding work',()=>{
  for (const [level,index,config] of [[505,2,correct.stateful],[550,1,correct.rogueReport],[550,2,correct.reconnect]]) {
    const h = harness(level,index);
    for (const detail of h.all().filter(n=>n.tag === 'details')) {detail.open=true; detail.trigger('toggle');}
    for (const [key,value] of Object.entries(config)) h.all().find(n=>n.id === `control-${key}`).value=value;
    h.all().find(n=>n.id === 'run-replay').trigger('click');
    assert.ok(!h.events.includes(`net_${level}_${index}_verified`));
    assert.match(h.all().find(n=>n.attributes.role === 'status').textContent,/CASE INCOMPLETE/);
  }
});
test('all three firewall tools complete as one connected case and issue the report',()=>{
  const storage = new Map();
  for (let index=0;index<3;index++) {
    const h = harness(505,index,{storage});
    for (const detail of h.all().filter(n=>n.tag === 'details')) {detail.open=true; detail.trigger('toggle');}
    for (const [key,value] of Object.entries(correct[cases[505][index].mode])) h.all().find(n=>n.id === `control-${key}`).value=value;
    h.all().find(n=>n.id === 'run-replay').trigger('click');
    assert.ok(h.events.includes(`net_505_${index}_verified`));
    if (index === 2) assert.match(h.all().find(n=>n.attributes.role === 'status').textContent,/CYBERITY\{stateful_campus_verified}/);
  }
});
test('dangerous buttons report only the penalty, never verification evidence',() => {
  for (const [level,index] of [[503,1],[503,2],[550,1]]) {
    const h = harness(level,index);
    h.all().find(n=>n.className === 'danger').trigger('click');
    assert.deepEqual(h.events,[`net_${level}_unsafe`]);
  }
});

function timerWindow(storage = new Map()) {
  const pending = new Map(), listeners = {}; let serial=0;
  const window = {name:'',location:{search:''},sessionStorage:{getItem:key=>storage.get(key) ?? null,setItem:(key,value)=>storage.set(key,value)},
    matchMedia:()=>({matches:false}),setTimeout:fn=>{const id=++serial; pending.set(id,fn); return id;},
    clearTimeout:id=>pending.delete(id),addEventListener:(name,fn)=>{listeners[name]=fn;}};
  return {window,storage,pending,listeners,step() {const [id,fn]=pending.entries().next().value; pending.delete(id); fn();},
    flush() {let guard=30; while(pending.size && guard-->0) this.step(); assert.ok(guard>0,'Replay timer must settle.');}};
}
function inspectAll(h) {
  for (const detail of h.all().filter(n=>n.className === 'evidence-card')) {detail.open=true; detail.trigger('toggle');}
}
function setConfig(h,config,dispatch = false) {
  for (const [key,value] of Object.entries(config)) {const select=h.all().find(n=>n.id === `control-${key}`); select.value=value; if (dispatch) select.trigger('change');}
}
test('paced replay unlocks proof only after the last check; show-all completes once',()=>{
  const clock=timerWindow(); const h=harness(505,0,clock); inspectAll(h); setConfig(h,correct.firewall);
  h.all().find(n=>n.id === 'run-replay').trigger('click');
  assert.ok(!h.events.includes('net_505_0_verified'));
  assert.equal(h.all().find(n=>n.id === 'run-replay').disabled,true);
  clock.step(); assert.ok(!h.events.includes('net_505_0_verified'));
  const skip=h.all().find(n=>n.className === 'tool-btn skip-replay');
  skip.trigger('click'); skip.trigger('click');
  assert.equal(clock.pending.size,0);
  assert.equal(h.events.filter(id=>id === 'net_505_0_verified').length,1);
  assert.equal(JSON.parse(clock.storage.get('cyberity_net_505')).journal.length,1);
  assert.match(h.all().find(n=>n.className === 'case-journal').textContent,/1 saved replay/);
});
test('changing a setting cancels an in-flight replay and cannot grant stale proof',()=>{
  const clock=timerWindow(); const h=harness(505,0,clock); inspectAll(h); setConfig(h,correct.firewall);
  h.all().find(n=>n.id === 'run-replay').trigger('click'); clock.step();
  setConfig(h,{web:'any'},true);
  assert.equal(clock.pending.size,0); assert.ok(!h.events.includes('net_505_0_verified'));
  h.all().find(n=>n.id === 'run-replay').trigger('click'); clock.flush();
  assert.ok(!h.events.includes('net_505_0_verified'));
  assert.match(h.all().find(n=>n.attributes.role === 'status').textContent,/Staff SMB.*ALLOW/s);
});
test('leaving a tool cancels its unfinished timers and keeps it unverified',()=>{
  const clock=timerWindow(); const h=harness(501,0,clock); inspectAll(h);
  h.all().find(n=>n.id === 'run-replay').trigger('click'); clock.listeners.pagehide();
  assert.equal(clock.pending.size,0);
  assert.ok(!h.events.includes('net_501_0_verified'));
  assert.equal(JSON.parse(clock.storage.get('cyberity_net_501')).stages[0].verified,false);
});
test('dangerous action cancels pending browser verification',()=>{
  const clock=timerWindow(); const h=harness(503,1,clock); inspectAll(h); setConfig(h,correct.certificate);
  h.all().find(n=>n.id === 'run-replay').trigger('click');
  h.all().find(n=>n.className === 'danger').trigger('click');
  assert.equal(clock.pending.size,0); assert.ok(!h.events.includes('net_503_1_verified')); assert.ok(h.events.includes('net_503_unsafe'));
});
test('team conversations survive navigation and react to actual service outcomes',()=>{
  const h=harness(501,2);
  h.all().find(n=>n.textContent === 'What happened?').trigger('click');
  inspectAll(h); setConfig(h,{policy:'all'}); h.all().find(n=>n.id === 'run-replay').trigger('click');
  assert.match(h.all().find(n=>n.className === 'message-thread').textContent,/Either enrollment is still broken or the share is exposed/);
  setConfig(h,correct.repair,true); h.all().find(n=>n.id === 'run-replay').trigger('click');
  const restored=harness(501,2,{storage:h.storage});
  assert.match(restored.all().find(n=>n.className === 'message-thread').textContent,/Enrollment loads, and the unsolicited share connection is still blocked/);
  assert.equal(restored.all().find(n=>n.textContent === 'What happened?').disabled,true);
  assert.equal(JSON.parse(h.storage.get('cyberity_net_501')).journal.length,2);
});
test('direct rule editing evaluates actual first matches and keeps final deny pinned',()=>{
  assert.equal(evaluateRuleOrder(['staff','portal','final']).ok,true);
  assert.equal(evaluateRuleOrder(['portal','staff','final']).ok,true);
  assert.equal(evaluateRuleOrder(['broad','staff','portal','final']).ok,false);
  assert.equal(evaluateRuleOrder(['final','staff','portal']).ok,false);
  const h=harness(505,1); inspectAll(h);
  h.all().find(n=>n.textContent === 'REMOVE').trigger('click');
  assert.equal(h.all().find(n=>n.id === 'control-order').value,'custom');
  h.all().find(n=>n.id === 'run-replay').trigger('click');
  assert.ok(h.events.includes('net_505_1_verified'));
  assert.equal(JSON.parse(h.storage.get('cyberity_net_505')).stages[1].customRules.at(-1),'final');
});
test('unread-evidence navigation and resume links reflect actual case progress',()=>{
  const h=harness(502,0), next=h.all().find(n=>n.className === 'tool-btn evidence-next');
  for(let i=0;i<cases[502][0].records.length;i++) next.trigger('click');
  assert.ok(h.events.includes('net_502_0_inspected')); assert.equal(next.disabled,true);
  h.all().find(n=>n.id === 'run-replay').trigger('click');
  const desk=harness(502,-1,{storage:h.storage});
  assert.equal(desk.all().find(n=>n.className === 'continue-card').href,'upgrade.html');
});
test('editing a previously approved policy invalidates the downstream report',()=>{
  const storage=new Map();
  for (let index=0;index<3;index++) {
    const h=harness(505,index,{storage}); inspectAll(h); setConfig(h,correct[cases[505][index].mode]);
    h.all().find(n=>n.id === 'run-replay').trigger('click');
  }
  const policy=harness(505,0,{storage}); setConfig(policy,{web:'any'},true);
  const report=harness(505,2,{storage});
  assert.ok(!report.events.includes('net_505_2_verified'));
  assert.ok(!report.all().find(n=>n.attributes.role === 'status').textContent.includes('CYBERITY{'));
  assert.equal(JSON.parse(storage.get('cyberity_net_505')).stages[2].verified,false);
});
test('reduced-motion mode completes verification immediately without scheduling animation',()=>{
  const clock=timerWindow(); clock.window.matchMedia=()=>({matches:true});
  const h=harness(501,0,clock); inspectAll(h); h.all().find(n=>n.id === 'run-replay').trigger('click');
  assert.equal(clock.pending.size,0); assert.ok(h.events.includes('net_501_0_verified'));
});
