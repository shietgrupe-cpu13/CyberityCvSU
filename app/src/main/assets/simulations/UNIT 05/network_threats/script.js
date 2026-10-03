/* CYBERITY — Campus Network Watch (504). Local only.
 * Evidence, policy evaluation, console rendering and per-attempt state.
 * No real packets, credentials, firewall rules or wireless settings are sent or changed. */
'use strict';
const NETWORK_META = {
  "level": 504,
  "folder": "network_threats",
  "title": "Campus Network Watch",
  "badge": "BASELINE REVIEW",
  "context": "CvSU ITSO · Gateway and DNS investigation NET-0504",
  "metric": "3",
  "metricLabel": "Network findings",
  "pages": [
    "capture.html",
    "gateway.html",
    "resolver.html"
  ],
  "mission": "Library traffic is being observed and redirected. Work from the packet captures and independently approved settings. Stop the forged local mapping, then repair the unauthorized resolver.",
  "tools": [
    "Distinguish passive observation from TLS decryption.",
    "Compare gateway MAC mappings and test binding inspection.",
    "Restore approved DNS while retaining certificate checking."
  ]
};
const NETWORK_CASES = {
  "504": [
    {
      "title": "Capture · Passive observer",
      "records": [
        [
          "Plaintext observation",
          "HTTP notice body: classroom=Library-2\nObserver read a copy; no packet modification recorded."
        ],
        [
          "TLS observation",
          "HTTPS application content: encrypted\nOnly destination IP, timing and sizes visible.\nNo evidence of TLS decryption."
        ]
      ],
      "action": "REPLAY OBSERVER",
      "mode": "sniff",
      "result": "The observer passively captured traffic.\nHTTP message read: yes.\nValid HTTPS application content read: no.\nTechnique shown: packet sniffing."
    },
    {
      "title": "Gateway · Changed ARP mapping",
      "records": [
        [
          "Approved baseline",
          "Gateway IPv4: 192.0.2.1\nGateway MAC: 02:00:00:00:01:01\nTrusted switch binding supplied by ITSO."
        ],
        [
          "New advertisement",
          "ARP reply: 192.0.2.1 is at 02:00:00:00:09:99\nOrigin: unapproved student port."
        ],
        [
          "Observed consequence",
          "Victim sends gateway traffic toward :09:99.\nAn interception position is possible; valid TLS content is not automatically readable."
        ]
      ],
      "action": "REPLAY ARP ADVERTISEMENT",
      "mode": "arp",
      "controls": [
        [
          "binding",
          "Switch policy",
          [
            [
              "accept",
              "Accept all advertisements"
            ],
            [
              "inspect",
              "Dynamic ARP inspection with ITSO trusted bindings"
            ]
          ]
        ]
      ]
    },
    {
      "title": "Resolver · Different portal answer",
      "records": [
        [
          "Approved baseline",
          "Resolver: 192.0.2.53\nportal.campus.example → 192.0.2.20\nCertificate validates the intended name."
        ],
        [
          "Unauthorized change",
          "Resolver changed to 198.51.100.53\nportal.campus.example → 198.51.100.99\nPresented certificate does not cover portal.campus.example."
        ]
      ],
      "action": "TEST NAME AND TLS",
      "mode": "dns",
      "controls": [
        [
          "resolver",
          "Resolver setting",
          [
            [
              "rogue",
              "198.51.100.53 · unauthorized"
            ],
            [
              "approved",
              "192.0.2.53 · approved"
            ]
          ]
        ],
        [
          "tls",
          "Certificate checking",
          [
            [
              "off",
              "Ignore errors"
            ],
            [
              "on",
              "Keep validation enabled"
            ]
          ]
        ]
      ]
    }
  ]
};
'use strict';
/* Pure packet-policy functions are shared by the UI and offline behavior tests. */
function evaluateNetworkCase(mode, values) {
  const v = values;
  switch (mode) {
    case 'repair': return {ok:v.policy === 'portal', lines:[`Portal TCP 443: ${v.policy === 'none' ? 'DENY · enrollment broken' : 'ALLOW'}`, `Unsolicited SMB: ${v.policy === 'all' ? 'ALLOW · exposed' : 'DENY'}`]};
    case 'wifiUpgrade': return {ok:v.encryption === 'transition' && v.secret === 'unique' && v.wps === 'off', lines:[`New tablets: ${['transition','wpa3'].includes(v.encryption) ? 'protected with WPA3' : 'approved protection missing'}`, `Old readers: ${v.encryption === 'transition' ? 'protected with WPA2-AES; migration pending' : v.encryption === 'wpa3' ? 'cannot connect' : 'approved protection missing'}`, `Passphrase: ${v.secret === 'unique' ? 'unique policy PASS' : 'factory default FAIL'}`, `WPS: ${v.wps === 'off' ? 'disabled PASS' : 'enabled FAIL'}`]};
    case 'guests': return {ok:v.isolation === 'segment', lines:[`Guest internet: ${v.isolation === 'off' ? 'DENY · service broken' : 'ALLOW'}`, `Guest → staff / other guest: ${v.isolation === 'segment' || v.isolation === 'off' ? 'DENY' : 'ALLOW · SSID name did not isolate traffic'}`]};
    case 'certificate': return {ok:v.decision === 'stop', unsafe:v.decision === 'bypass', lines:[v.decision === 'stop' ? 'STOPPED · No login sent. Verify the intended portal through ITSO on a trusted connection.' : 'FAIL · The requested name is not authenticated. Do not bypass the warning.']};
    case 'identity': return {ok:v.decision === 'bookmark', unsafe:v.decision === 'login', lines:[v.decision === 'bookmark' ? 'PASS · Known portal bookmark selected; no credential sent to the lookalike.' : 'FAIL · Valid TLS to campus-login.example does not establish university ownership.']};
    case 'arp': return {ok:v.binding === 'inspect', lines:[v.binding === 'inspect' ? 'Forged ARP reply: REJECTED · trusted binding retained: 192.0.2.1 → 02:00:00:00:01:01.' : 'Forged ARP reply: ACCEPTED · gateway traffic redirected toward :09:99.']};
    case 'dns': return {ok:v.resolver === 'approved' && v.tls === 'on', lines:[`DNS answer: ${v.resolver === 'approved' ? '192.0.2.20 · approved portal' : '198.51.100.99 · unapproved target'}`, `Identity check: ${v.tls !== 'on' ? 'DISABLED · unsafe' : v.resolver === 'approved' ? 'PASS · intended portal name validates' : 'FAIL · hostname mismatch; browser stops'}`]};
    case 'rogueSurvey': return {ok:v.radio === '99', lines:[v.radio === '99' ? 'CORROBORATED · :99 absent from inventory; LOBBY-17 is an unapproved AP uplink.' : 'Approved radio and managed uplink match inventory. Investigate the other radio, regardless of signal strength.']};
    case 'rogueReport': return {ok:v.report === 'evidence', lines:[v.report === 'evidence' ? 'REPORT ACCEPTED · :99, LOBBY-17 and credential demand recorded. Student disconnected; no credentials sent.' : 'REPORT INCOMPLETE · Correlate the radio identifier, physical uplink and observed demand.']};
    case 'reconnect': return {ok:v.profile === 'forget' && v.network === 'official', lines:[`Unapproved profile: ${v.profile === 'forget' ? 'removed; auto-join disabled' : 'still saved · FAIL'}`, `Connection identity: ${v.network === 'official' ? 'approved radio; authentication server validated; portal reachable' : 'strongest signal alone is not identity · FAIL'}`]};
    case 'firewall': {
      const packets = [
        {name:'A · Approved DNS',protocol:'udp',destination:'192.0.2.53',port:53,expected:true},
        {name:'B · Portal HTTPS',protocol:'tcp',destination:'192.0.2.20',port:443,expected:true},
        {name:'C · Staff SMB',protocol:'tcp',destination:'192.0.2.30',port:445,expected:false},
        {name:'D · Unapproved DNS',protocol:'udp',destination:'198.51.100.53',port:53,expected:false}
      ];
      const results = packets.map(p => {
        let rule = 'Final rule', allow = v.default === 'allow';
        if (p.port === 53 && (v.dns === 'any' || (v.dns === 'approved' && p.destination === '192.0.2.53'))) {allow = true; rule = 'DNS exception';}
        else if (p.protocol === 'tcp' && (v.web === 'any' || (v.web === 'portal' && p.destination === '192.0.2.20' && p.port === 443))) {allow = true; rule = 'Web exception';}
        return {pass:allow === p.expected, line:`${p.name}: ${allow ? 'ALLOW' : 'DENY'} · ${rule} · ${allow === p.expected ? 'PASS' : 'FAIL'}`};
      });
      return {ok:results.every(r => r.pass),lines:results.map(r => r.line)};
    }
    case 'order': return {ok:v.order === 'narrow', lines:v.order === 'original' ? ['Staff SMB: ALLOW · rule 1 broad TCP allow · FAIL','Portal HTTPS: ALLOW · rule 1 broad TCP allow','Later staff deny: never reached'] : v.order === 'denyFirst' ? ['Staff SMB: DENY · rule 1 · PASS','Portal HTTPS: ALLOW · broad TCP rule','Other TCP destinations remain exposed · FAIL'] : ['Staff SMB: DENY · exact staff deny · PASS','Portal HTTPS: ALLOW · exact portal exception · PASS','Other unmatched traffic: DENY · final rule · PASS']};
    case 'stateful': return {ok:v.state === 'on' && v.inbound === 'deny', lines:['A · NEW outbound HTTPS: ALLOW · portal exception',`B · ESTABLISHED reply: ${v.state === 'on' ? 'ALLOW · tracked connection · PASS' : 'DENY · return path broken · FAIL'}`,`C · NEW unsolicited inbound: ${v.inbound === 'deny' ? 'DENY · PASS' : 'ALLOW · exposed · FAIL'}`]};
    default: return {ok:false,lines:['Unknown replay.']};
  }
}

'use strict';

/* This workspace keeps the Unit 06 console/tool/state pattern. Every lab ships
 * its own copy alongside its scenario-specific evidence and styles. */
const NETWORK_STORY = {
  "person": "Ana Mendoza",
  "role": "ITSO network analyst",
  "initial": [
    "We have a capture from the library link. Tell me what it actually proves before calling it a full compromise.",
    "The gateway address did not change, but its local mapping did. Compare it with the trusted binding.",
    "A workstation now asks a different DNS resolver. The portal answer changed too."
  ],
  "context": [
    "The HTTP message was readable. The TLS capture contains encrypted application data and visible metadata.",
    "The approved gateway MAC came from the switch baseline. The new advertisement arrived on an unapproved port.",
    "Use the independently approved resolver record; do not accept whichever answer comes back first."
  ],
  "constraints": [
    "Do not claim TLS was decrypted without evidence.",
    "Use authorized binding inspection, not a blanket shutdown of the library network.",
    "Repair the resolver and retain certificate validation. DNS alone cannot establish website trust."
  ],
  "pass": [
    "The finding is precise: passive observation, with plaintext exposed but no evidence of TLS decryption.",
    "The forged advertisement was rejected and the approved gateway mapping retained.",
    "The intended name resolves to the approved portal again and its certificate validates."
  ],
  "fail": [
    "The evidence does not support that conclusion yet.",
    "The unapproved gateway advertisement still needs to be rejected.",
    "The resolver or identity check is still wrong. Keep both protections in place."
  ]
};
const RULE_LABELS = {broad:'ALLOW guest → ANY TCP',staff:'DENY guest → staff TCP 445',portal:'ALLOW guest → portal TCP 443',final:'DENY unmatched'};
function rulesForPreset(preset) {
  return preset === 'original' ? ['broad','staff','portal','final'] : preset === 'denyFirst' ? ['staff','broad','final'] : ['portal','staff','final'];
}
function evaluateRuleOrder(ids) {
  const packets = [{name:'Staff SMB',target:'staff',expected:false},{name:'Portal HTTPS',target:'portal',expected:true},{name:'Other TCP destination',target:'other',expected:false}];
  const checks = packets.map(packet=>{
    const match = ids.find(id=>id === 'broad' || id === 'final' || id === packet.target);
    const allow = match === 'broad' || match === 'portal';
    return {pass:allow === packet.expected && !!match,line:`${packet.name}: ${allow ? 'ALLOW' : 'DENY'} · rule ${ids.indexOf(match)+1} · ${RULE_LABELS[match] || 'NO MATCH'} · ${allow === packet.expected && match ? 'PASS' : 'FAIL'}`};
  });
  const structure = !ids.includes('broad') && ids[ids.length-1] === 'final' && ['staff','portal','final'].every(id=>ids.includes(id)) && new Set(ids).size === ids.length;
  if (!structure) checks.push({pass:false,line:'Policy structure: FAIL · remove the broad allow, retain exact service rules and keep the final deny last.'});
  return {ok:checks.every(c=>c.pass),lines:checks.map(c=>c.line)};
}
function shiftTime() {
  const minutes=8*60+30+NETWORK_STATE.runs*2;
  return `${String(Math.floor(minutes/60)%24).padStart(2,'0')}:${String(minutes%60).padStart(2,'0')}`;
}
function appendCaseJournal(index,result) {
  NETWORK_STATE.journal=NETWORK_STATE.journal || [];
  NETWORK_STATE.journal.push({time:shiftTime(),tool:NETWORK_CASES[NETWORK_META.level][index].title,ok:result.ok,checks:result.lines});
  NETWORK_STATE.journal=NETWORK_STATE.journal.slice(-12); saveNetworkState();
}
function renderCaseJournal(root,existing) {
  const log=existing || netElement('details',null,root,'case-journal');
  log.replaceChildren();
  const entries=NETWORK_STATE.journal || [];
  netElement('summary',`SHIFT LOG · ${entries.length} saved replay${entries.length === 1 ? '' : 's'}`,log);
  if (!entries.length) {netElement('p','Your replay results will appear here. The shift clock is simulated.',log); return log;}
  [...entries].reverse().forEach(entry=>{
    const row=netElement('div',null,log,'journal-entry');
    netElement('span',entry.time + ' · ' + (entry.ok ? 'VERIFIED' : 'NEEDS WORK'),row,entry.ok ? 'good-text mono' : 'bad mono');
    netElement('strong',entry.tool,row); netElement('pre',entry.checks.join('\n'),row,'mono');
  });
  return log;
}
function renderTeamChannel(root,index,stage) {
  const story=NETWORK_STORY;
  const panel=netElement('details',null,root,'team-channel'); panel.open=true;
  const summary=netElement('summary',null,panel);
  netElement('span',story.person.split(' ').map(p=>p[0]).slice(0,2).join(''),summary,'person-avatar');
  const identity=netElement('span',null,summary,'person-identity');
  netElement('strong',story.person,identity); netElement('span',story.role+' · simulated teammate',identity);
  const thread=netElement('div',null,panel,'message-thread');
  const memo=stage || {};
  memo.chat=memo.chat || [];
  function bubble(who,text,self) {
    const row=netElement('div',null,thread,self ? 'message self' : 'message');
    netElement('span',who,row,'message-who'); netElement('p',text,row);
  }
  bubble(story.person,story.initial[index],false);
  memo.chat.forEach(item=>bubble(item.who,item.text,item.self));
  const replies=netElement('div',null,panel,'reply-options');
  [['context','What happened?','I am checking the reported symptoms. What changed?',story.context[index]],['constraints','What must keep working?','Before I change anything, what must keep working?',story.constraints[index]]].forEach(([key,label,question,answer])=>{
    const button=netButton(label,replies,()=>{
      if (memo.chat.some(item=>item.key === key)) return;
      const sent={key,who:'You · ITSO trainee',text:question,self:true};
      const received={key:key+'-reply',who:story.person,text:answer,self:false};
      memo.chat.push(sent,received);
      if (stage) saveNetworkState();
      bubble(sent.who,sent.text,true); bubble(received.who,received.text,false);
      button.disabled=true;
    },'reply-chip');
    button.disabled=memo.chat.some(item=>item.key === key);
  });
  return {result(result) {
    const text=result.ok ? story.pass[index] : story.fail[index];
    const key='result-'+(result.ok ? 'pass' : 'fail')+'-'+text;
    if (memo.chat[memo.chat.length-1]?.key === key) return;
    const item={key,who:story.person,text,self:false}; memo.chat.push(item);
    memo.chat=memo.chat.slice(-10); if (stage) saveNetworkState();
    bubble(item.who,item.text,false);
  }};
}

const STORE_KEY = `cyberity_net_${NETWORK_META.level}`;
function freshNetworkState() {return {stages:{},runs:0,journal:[]};}
function loadNetworkState() {
  let raw = null;
  try {raw = window.sessionStorage.getItem(STORE_KEY);} catch (_) {}
  if (!raw) try {if (window.name.startsWith(STORE_KEY + '=')) raw = window.name.slice(STORE_KEY.length + 1);} catch (_) {}
  try {
    const state = JSON.parse(raw);
    return state && state.stages && Number.isFinite(state.runs) ? state : freshNetworkState();
  } catch (_) {return freshNetworkState();}
}
let NETWORK_STATE = loadNetworkState();
function saveNetworkState() {
  const raw = JSON.stringify(NETWORK_STATE);
  try {window.sessionStorage.setItem(STORE_KEY,raw);} catch (_) {}
  try {window.name = STORE_KEY + '=' + raw;} catch (_) {}
}
function networkClue(id) {try {AndroidLab.notifyClueFound(id);} catch (_) {}}
function netElement(tag, text, parent, className) {
  const node = document.createElement(tag);
  if (text !== undefined && text !== null) node.textContent = text;
  if (className) node.className = className;
  if (parent) parent.appendChild(node);
  return node;
}
function netButton(text,parent,fn,className = 'tool-btn') {
  const button = netElement('button',text,parent,className);
  button.type = 'button'; button.addEventListener('click',fn); return button;
}
function renderNetworkHeader(root,title,current) {
  const head = netElement('header',null,root,'desk-head');
  const row = netElement('div',null,head,'clock-row');
  netElement('h1',title,row);
  netElement('span',NETWORK_META.badge,row,'clock mono');
  netElement('p',NETWORK_META.context,head);
  const nav = netElement('nav',null,root,'tabs'); nav.setAttribute('aria-label','Lab tools');
  [['Console','desk.html',-1],...NETWORK_CASES[NETWORK_META.level].map((s,i)=>[s.title.split(' · ')[0],NETWORK_META.pages[i],i])].forEach(([label,file,i]) => {
    const link = netElement('a',label,nav,'tab'); link.href = file;
    if (current === i) link.setAttribute('aria-current','page');
  });
}
function renderNetworkDashboard() {
  // Only a new lab entry carries fresh=1. Returning to Console keeps the case.
  if (new URLSearchParams(window.location.search).get('fresh') === '1') {
    NETWORK_STATE = freshNetworkState(); saveNetworkState();
  }
  const root = document.getElementById('workspace');
  renderNetworkHeader(root,NETWORK_META.title,-1);
  const stats = netElement('div',null,root,'stats');
  [[`${Object.values(NETWORK_STATE.stages).filter(s=>s.verified).length}/3`,'Tools verified'],[String(NETWORK_STATE.runs),'Replays run'],[NETWORK_META.metric,NETWORK_META.metricLabel]].forEach(([value,label]) => {
    const stat = netElement('div',null,stats,'stat');
    netElement('span',value,stat,'stat-num mono'); netElement('span',label,stat,'stat-label');
  });
  const suggested = NETWORK_CASES[NETWORK_META.level].findIndex((s,i)=>!NETWORK_STATE.stages[i]?.verified);
  const handoff = netElement('a',suggested < 0 ? 'CASE VERIFIED · Review your work' : 'CONTINUE INVESTIGATION → ' + NETWORK_CASES[NETWORK_META.level][suggested].title,root,'continue-card');
  handoff.href = NETWORK_META.pages[Math.max(0,suggested)];
  renderTeamChannel(root,Math.max(0,suggested),NETWORK_STATE.stages[Math.max(0,suggested)]);
  netElement('div','SHIFT BRIEFING',root,'section-label');
  const briefing = netElement('section',null,root,'briefing');
  netElement('p',NETWORK_META.mission,briefing);
  netElement('p','Inspect the evidence, operate the tool, and replay the consequences. Your work remains available when you return to this console.',briefing);
  netElement('div','INVESTIGATION TOOLS',root,'section-label');
  NETWORK_CASES[NETWORK_META.level].forEach((s,i) => {
    const tile = netElement('a',null,root,'choice tile'); tile.href = NETWORK_META.pages[i];
    const row = netElement('div',null,tile,'tile-row');
    netElement('span',String(i+1).padStart(2,'0'),row,'tool-number mono');
    const body = netElement('div',null,row,'tile-body');
    netElement('strong',s.title,body,'tile-name');
    netElement('span',NETWORK_META.tools[i],body,'tile-sub');
    const stage = NETWORK_STATE.stages[i];
    netElement('span',stage?.verified ? 'VERIFIED' : stage?.inspected.length ? 'IN PROGRESS' : 'WAITING',body,stage?.verified ? 'badge good' : 'badge');
  });
  renderCaseJournal(root);
  netElement('p','CvSU ITSO training environment · all hosts, packets and actions are fictional.',root,'foot');
}

function mountNetworkCase(levelId,index) {
  const scenario = NETWORK_CASES[levelId][index];
  const root = document.getElementById('workspace');
  const state = NETWORK_STATE.stages[index] || {inspected:[],values:{},verified:false,lastResult:null};
  NETWORK_STATE.stages[index] = state;
  const inspected = new Set(state.inspected);
  const controls = {}, details = [], visual = {};
  function clue(suffix) {networkClue(`net_${levelId}_${index}_${suffix}`);}
  function unsafe() {networkClue(`net_${levelId}_unsafe`);}
  function add(tag,text,parent = root,className) {return netElement(tag,text,parent,className);}
  renderNetworkHeader(root,scenario.title,index);
  let replayVersion = 0, replayTimer = null, runButton = null, showAllButton = null, finishReplay = null, journalView = null, nextEvidenceButton = null;
  const hud = add('div',null,root,'case-hud');
  hud.setAttribute('aria-live','polite');
  const hudEvidence = add('span','',hud,'hud-evidence');
  const hudState = add('span','',hud,'hud-state');
  const hudHelp = add('span','',hud,'hud-help');
  const team = renderTeamChannel(root,index,state);
  function updateHud() {
    hudEvidence.textContent = 'EVIDENCE ' + inspected.size + '/' + scenario.records.length;
    hudState.textContent = state.verified ? 'VERIFIED' : replayTimer !== null ? 'REPLAYING' : 'INVESTIGATING';
    hudHelp.textContent = state.verified ? 'Swipe the top handle down to answer' : inspected.size < scenario.records.length ? 'Open the remaining evidence' : 'Run the acceptance checks';
    hud.className = 'case-hud' + (state.verified ? ' calm' : '');
    if (runButton) runButton.disabled = inspected.size !== scenario.records.length || replayTimer !== null;
    if (nextEvidenceButton) {nextEvidenceButton.disabled=inspected.size === scenario.records.length; nextEvidenceButton.textContent=inspected.size === scenario.records.length ? 'ALL EVIDENCE INSPECTED' : 'OPEN NEXT UNREAD EVIDENCE';}
  }
  function cancelReplay() {
    replayVersion++;
    if (replayTimer !== null) window.clearTimeout(replayTimer);
    replayTimer = null; finishReplay = null;
    if (showAllButton) showAllButton.hidden = true;
    if (runButton) runButton.textContent = scenario.action;
  }
  const panel = add('section',null,root,'instrument');
  const evidence = add('section',null,root,'evidence');
  add('div','EVIDENCE WORKBENCH',evidence,'section-label');
  const count = add('p',`Evidence inspected: ${inspected.size} / ${scenario.records.length}`,evidence,'status');
  function mark(i) {
    inspected.add(i); state.inspected = [...inspected]; saveNetworkState();
    count.textContent = `Evidence inspected: ${inspected.size} / ${scenario.records.length}`;
    if (inspected.size === scenario.records.length) clue('inspected');
    updateHud();
  }
  function inspect(i) {details[i].open = true; mark(i); details[i].scrollIntoView({behavior:'smooth',block:'nearest'});}
  scenario.records.forEach(([title,text],i) => {
    const detail = add('details',null,evidence,'evidence-card'); details.push(detail);
    add('summary',`${String(i+1).padStart(2,'0')} · ${title}`,detail);
    add('pre',text,detail,'rows mono');
    detail.open = inspected.has(i);
    detail.addEventListener('toggle',() => {if (detail.open) mark(i);});
  });
  function instrumentTitle(label,sub) {add('h2',label,panel); add('p',sub,panel,'instrument-sub');}
  function rowTable(headers,rows,actions) {
    const scroll = add('div',null,panel,'table-scroll');
    const table = add('table',null,scroll,'data-table');
    const tr = add('tr',null,add('thead',null,table)); headers.forEach(h=>add('th',h,tr));
    const body = add('tbody',null,table);
    rows.forEach((row,i)=>{
      const tr = add('tr',null,body);
      row.forEach(value=>add('td',value,tr));
      if (actions) {const td = add('td',null,tr); netButton('INSPECT',td,()=>inspect(actions[i]),'tool-btn small');}
    });
    return body;
  }
  if (levelId === 501) {
    instrumentTitle('Packet route map','Tap the devices to inspect the route. The packet walk advances one hop at a time.');
    const map = add('div',null,panel,'route-map'); visual.hops = [];
    [['PC','192.0.2.10',1],['DNS','192.0.2.53',0],['Router','192.0.2.1',1],['Portal','192.0.2.20',index === 0 ? 2 : 1]].forEach(([name,ip,record])=>{
      const node = netButton('',map,()=>inspect(Math.min(record,scenario.records.length-1)),'route-node');
      add('strong',name,node); add('span',ip,node,'mono'); visual.hops.push(node);
    });
    if (index === 0) {
      let hop = 0;
      visual.hopText = add('p','Packet waiting at the library PC.',panel,'status');
      netButton('ADVANCE ONE HOP',panel,()=>{
        visual.hops.forEach((node,i)=>node.className = `route-node${i === hop ? ' active' : ''}`);
        visual.hopText.textContent = ['PC asks its resolver for the portal address.','DNS returns 192.0.2.20; name lookup is complete.','Router forwards the request into the portal network.','Portal receives TCP 443; TLS verifies the intended name.'][hop];
        inspect([0,0,1,2][hop]); hop = (hop+1)%4;
      });
    } else if (index === 1) rowTable(['Protocol','Client port','Destination','Service'],[['UDP','53143','192.0.2.53:53','DNS'],['TCP','53144','192.0.2.20:443','HTTPS']],[0,1]);
  } else if (levelId === 502) {
    instrumentTitle('Wireless controller','Link protection, client compatibility and guest routes are separate checks.');
    const clients = add('div',null,panel,'client-grid');
    const cards = index === 0 ? [['LEGACY','WEP reader','Obsolete protection'],['OPEN','Visitor hotspot','No radio encryption'],['AES','Library AP','WPA2 protected link']] : index === 1 ? [['20','New tablets','WPA3 capable'],['4','Archive readers','WPA2-AES only'],['1','Library AP','Migration window']] : [['WEB','Internet','Visitors need access'],['SMB','Staff share','Visitors must not reach'],['PEER','Other guests','Client isolation required']];
    visual.clients = cards.map(([number,label,status],i)=>{
      const card = netButton('',clients,()=>inspect(Math.min(i,scenario.records.length-1)),'client-card');
      add('span',number,card,'client-icon mono'); add('strong',label,card); return add('span',status,card,'client-status');
    });
  } else if (levelId === 503) {
    instrumentTitle('Sandbox browser','Inspect the connection and the destination before making a sign-in decision.');
    const chrome = add('div',null,panel,'browser-window');
    const bar = add('div',null,chrome,'address-bar');
    netButton(index === 1 ? '⚠ IDENTITY' : 'CONNECTION',bar,()=>inspect(Math.min(1,scenario.records.length-1)),'security-chip');
    add('span',index === 2 ? 'https://campus-login.example/portal.campus.example/login' : index === 1 ? 'https://portal.campus.example/enrollment' : 'http://portal.campus.example/notice',bar,'address mono');
    const page = add('div',null,chrome,'browser-page');
    add('span',index === 1 ? 'CERTIFICATE WARNING' : index === 2 ? 'CAMPUS SIGN-IN' : 'NETWORK OBSERVER',page,'eyebrow');
    add('h3',index === 1 ? 'This connection cannot verify the portal.' : index === 2 ? 'The logo looks familiar. Check the hostname.' : 'What crossed the network?',page);
    add('p',index === 1 ? 'Requested: portal.campus.example\nPresented: hotspot-login.example' : index === 2 ? 'Valid certificate: campus-login.example\nKnown portal: portal.campus.example' : 'HTTP content is readable. Valid TLS content is encrypted; connection metadata remains visible.',page,'mono');
    scenario.records.forEach(([label],i)=>netButton(label,page,()=>inspect(i),'browser-tool'));
    visual.browser = add('span','No action taken',page,'badge');
  } else if (levelId === 504) {
    instrumentTitle('Network watch · baseline comparison','Read the capture alongside the trusted configuration. A change is evidence to investigate.');
    rowTable(['Observation','Approved / protected','Observed'], index === 0 ? [['HTTP body','No confidentiality','classroom=Library-2 readable'],['TLS body','Encrypted application data','Metadata only']] : index === 1 ? [['Gateway IP','192.0.2.1','192.0.2.1'],['Gateway MAC','02:00:00:00:01:01','02:00:00:00:09:99'],['Ingress','Trusted gateway uplink','Unapproved student port']] : [['Resolver','192.0.2.53','198.51.100.53'],['Portal answer','192.0.2.20','198.51.100.99'],['TLS identity','portal.campus.example','Does not cover intended name']]);
    const lane = add('div',null,panel,'threat-path');
    add('span','VICTIM',lane,'endpoint'); add('span','→',lane,'path-arrow');
    visual.threat = add('span',index === 0 ? 'PASSIVE OBSERVER' : 'UNAPPROVED PATH',lane,'threat-node');
    add('span','→',lane,'path-arrow'); add('span','PORTAL',lane,'endpoint');
    scenario.records.forEach(([label],i)=>netButton(`OPEN ${label.toUpperCase()}`,panel,()=>inspect(i)));
  } else if (levelId === 550) {
    instrumentTitle('Radio survey · Library lobby','SSID: CvSU-Campus · signal strength does not establish ownership.');
    const radios = add('div',null,panel,'radio-survey');
    [['11','-62 dBm','LIB-AP-1','62%'],['12','-70 dBm','LIB-AP-2','45%'],['99','-35 dBm','LOBBY-17','95%']].forEach(([suffix,signal,uplink,width],i)=>{
      const row = netButton('',radios,()=>inspect(index === 0 ? i : Math.min(i,scenario.records.length-1)),'radio-row');
      add('strong',`02:00:00:00:05:${suffix}`,row,'mono');
      const track = add('span',null,row,'signal-track'); const fill = add('span',null,track,'signal-fill'); fill.style.width = width;
      add('span',`${signal} · ${uplink}`,row,'radio-meta');
    });
    if (index === 0) netButton('OPEN INDEPENDENT ITSO INVENTORY',panel,()=>inspect(3));
    visual.radio = add('p','Inventory correlation pending.',panel,'status');
  } else if (levelId === 505) {
    instrumentTitle('Firewall operations · first-match policy','Evaluate required services and forbidden paths. Every verdict records the matching rule.');
    visual.rules = add('ol',null,panel,'rule-list');
    const log = add('section',null,panel,'packet-monitor');
    add('span','PACKET MONITOR',log,'eyebrow');
    visual.packets = add('div','Waiting for packet replay.',log,'packet-results mono');
    scenario.records.forEach(([label],i)=>netButton(`OPEN ${label.toUpperCase()}`,panel,()=>inspect(i)));
  }
  const tools = add('section',null,root,'control-panel');
  add('div',scenario.controls ? 'CONFIGURATION' : 'REPLAY',tools,'section-label');
  const output = add('div','Replay has not run yet.',root,'output');
  output.setAttribute('role','status'); output.setAttribute('aria-live','polite');
  function values() {return Object.fromEntries(Object.entries(controls).map(([key,select])=>[key,select.value]));}
  function ruleLines(v) {
    if (index === 0) return [`DNS: ${v.dns === 'approved' ? 'ALLOW UDP/TCP 53 → 192.0.2.53' : v.dns === 'any' ? 'ALLOW UDP/TCP 53 → ANY' : 'No exception'}`,`WEB: ${v.web === 'portal' ? 'ALLOW TCP 443 → 192.0.2.20' : v.web === 'any' ? 'ALLOW TCP → ANY' : 'No exception'}`,`${v.default === 'deny' ? 'DENY' : 'ALLOW'} unmatched traffic`];
    if (index === 1) return v.order === 'original' ? ['ALLOW guest → ANY TCP','DENY guest → staff TCP 445','ALLOW guest → portal TCP 443','DENY unmatched'] : v.order === 'denyFirst' ? ['DENY guest → staff TCP 445','ALLOW guest → ANY TCP','DENY unmatched'] : ['ALLOW guest → portal TCP 443','DENY guest → staff TCP 445','DENY unmatched'];
    return ['ALLOW guest → portal TCP 443',v.state === 'on' ? 'ALLOW ESTABLISHED replies for allowed connections' : 'DROP return packets · state tracking off',v.inbound === 'deny' ? 'DENY unsolicited inbound NEW' : 'ALLOW unsolicited inbound NEW'];
  }
  function updateRules() {
    if (!visual.rules) return;
    visual.rules.replaceChildren();
    if (index !== 1) {ruleLines(values()).forEach(line=>add('li',line,visual.rules,'rule mono')); return;}
    const ids = state.customRules || rulesForPreset(controls.order.value);
    ids.forEach((id,i)=>{
      const rule = add('li',null,visual.rules,'rule mono');
      add('span',RULE_LABELS[id],rule,'rule-label');
      const buttons = add('div',null,rule,'rule-actions');
      if (id !== 'final') {
        const up = netButton('↑',buttons,()=>moveRule(i,-1),'rule-move'); up.setAttribute('aria-label','Move ' + RULE_LABELS[id] + ' up'); up.disabled = i === 0;
        const down = netButton('↓',buttons,()=>moveRule(i,1),'rule-move'); down.setAttribute('aria-label','Move ' + RULE_LABELS[id] + ' down'); down.disabled = i >= ids.length-2;
        if (id === 'broad') netButton('REMOVE',buttons,()=>editRules(ids.filter(item=>item!=='broad')),'rule-remove');
      } else add('span','FINAL',buttons,'badge');
    });
  }
  function editRules(ids) {
    controls.order.value = 'custom'; controls.order.dispatchEvent(new Event('change'));
    state.customRules = ids; saveNetworkState(); updateRules();
  }
  function moveRule(index,delta) {
    const ids = [...(state.customRules || rulesForPreset(controls.order.value))];
    const target = index+delta;
    if (target < 0 || target >= ids.length-1 || ids[index] === 'final') return;
    [ids[index],ids[target]] = [ids[target],ids[index]]; editRules(ids);
  }
  (scenario.controls || []).forEach(([key,label,options])=>{
    const l = add('label',label,tools);
    const select = add('select',null,l); select.id = `control-${key}`;
    options.forEach(([value,text])=>{const opt=add('option',text,select); opt.value=value;});
    if (levelId === 505 && index === 1 && key === 'order') {
      const custom = add('option','Custom order · edited in the rule list',select); custom.value='custom';
    }
    select.value = state.values[key] || options[0][0]; controls[key] = select;
    select.addEventListener('change',()=>{
      cancelReplay();
      if (key === 'order') {
        if (select.value !== 'custom') delete state.customRules;
        else if (!state.customRules) state.customRules=rulesForPreset(state.values.order || 'original');
      }
      state.values = values(); state.verified = false; state.lastResult = null;
      const affected = levelId === 505 && index < 2 ? [2] : levelId === 550 ? [1,2].filter(i=>i>index) : [];
      affected.forEach(i=>{const later=NETWORK_STATE.stages[i]; if (later) {later.verified=false; later.lastResult=null;}});
      saveNetworkState();
      output.textContent = 'Unsaved verification · configuration changed. Replay the packets again.'; output.className = 'output'; updateRules();
      if (visual.packets) visual.packets.textContent = 'Policy changed · awaiting replay.';
      if (visual.clients) visual.clients.forEach(node=>{node.textContent='Settings changed · replay to verify'; node.className='client-status';});
      if (visual.radio) visual.radio.textContent='Selection changed · correlation pending.';
      if (visual.browser) {visual.browser.textContent='Action changed · verify again'; visual.browser.className='badge';}
      if (visual.threat) {visual.threat.textContent='Configuration changed · verify again'; visual.threat.className='threat-node';}
      updateHud();
    });
  });
  updateRules();
  if (levelId === 505 && index === 1) {
    netButton('REMOVE BROAD ALLOW RULE',tools,()=>{controls.order.value='narrow'; controls.order.dispatchEvent(new Event('change'));});
  }
  function showResult(result) {
    output.replaceChildren();
    add('strong',result.ok ? 'VERIFIED · All acceptance checks passed' : 'NOT VERIFIED · Review the failed checks',output,'result-title');
    result.lines.forEach((line,i)=>{
      const row = add('div',null,output,`result-row ${/FAIL|EXPOSED|DISABLED|ACCEPTED.*redirected|not authenticated/i.test(line) ? 'bad' : ''}`);
      add('span',String(i+1).padStart(2,'0'),row,'result-number mono'); add('span',line,row,'result-text');
    });
    add('p',result.ok ? 'Return to the task panel to submit your finding.' : 'Adjust the configuration and run the replay again.',output);
    output.className = `output ${result.ok ? 'pass' : 'fail'}`;
    const handoff = add('div',null,output,'handoff');
    add('strong',result.ok ? 'Finding ready' : 'Keep investigating',handoff);
    add('span',result.ok ? 'Swipe the simulation handle down to submit your answer. Your work is saved here.' : 'Change the failed setting and replay. The evidence stays available.',handoff);
    if (result.ok && index < 2) {const next = add('a','REVIEW NEXT TOOL → ' + NETWORK_CASES[levelId][index+1].title.split(' · ')[0],handoff,'next-tool'); next.href=NETWORK_META.pages[index+1];}
    team.result(result);
    const reaction=add('div',null,output,'outcome-person');
    add('span',NETWORK_STORY.person.split(' ').map(part=>part[0]).slice(0,2).join(''),reaction,'person-avatar');
    const quote=add('div',null,reaction,'outcome-quote');
    add('strong',NETWORK_STORY.person + ' · ' + NETWORK_STORY.role,quote);
    add('p',result.ok ? NETWORK_STORY.pass[index] : NETWORK_STORY.fail[index],quote);
    if (visual.packets) visual.packets.textContent = result.lines.join('\n\n');
    if (visual.browser) {visual.browser.textContent = result.ok ? index === 0 ? 'OBSERVER REPLAY VERIFIED' : 'SAFE ACTION VERIFIED' : 'IDENTITY NOT VERIFIED'; visual.browser.className = result.ok ? 'badge good' : 'badge bad';}
    if (visual.threat) {visual.threat.textContent = result.ok && index > 0 ? 'UNAPPROVED PATH BLOCKED' : index === 0 ? 'OBSERVATION REPLAYED' : 'UNAPPROVED PATH'; visual.threat.className = result.ok ? 'threat-node contained' : 'threat-node';}
    if (visual.radio) visual.radio.textContent = result.lines[0];
    if (visual.clients) visual.clients.forEach((node,i)=>{
      node.textContent = result.lines[Math.min(i,result.lines.length-1)];
      node.className = `client-status${result.ok ? ' good-text' : ''}`;
    });
    if (levelId === 505 && index === 2 && result.ok) {
      add('div','VERIFICATION REPORT',output,'eyebrow');
      add('pre','CYBERITY{stateful_campus_verified}',output,'report-code mono');
      add('p','Required HTTPS: PASS · established replies: PASS · unsolicited inbound: DENY',output);
    }
  }
  const run = netButton(scenario.action,tools,()=>{
    cancelReplay();
    if (inspected.size !== scenario.records.length) {
      output.textContent = 'Inspect every evidence record first. The replay needs the complete case.'; output.className='output fail'; return;
    }
    state.values = values();
    const result = scenario.result ? {ok:true,lines:scenario.result.split('\n')} : levelId === 505 && index === 1 && state.customRules ? evaluateRuleOrder(state.customRules) : evaluateNetworkCase(scenario.mode,state.values);
    // A final operations report verifies the entire case, not an isolated last page.
    const prerequisites = levelId === 505 && index === 2 ? [0,1] : levelId === 550 && index > 0 ? [index-1] : [];
    const missing = prerequisites.filter(i=>!NETWORK_STATE.stages[i]?.verified);
    if (missing.length) {
      result.ok = false;
      result.lines.unshift(`CASE INCOMPLETE · First verify ${missing.map(i=>NETWORK_CASES[levelId][i].title.split(' · ')[0]).join(' and ')} using the console tools.`);
    }
    NETWORK_STATE.runs++; state.verified = false; state.lastResult = null; saveNetworkState();
    if (result.unsafe) unsafe();
    const revision = replayVersion; let settled = false;
    function complete() {
      if (revision !== replayVersion || settled) return;
      settled=true; finishReplay=null;
      if (replayTimer !== null) window.clearTimeout(replayTimer);
      replayTimer=null; showAllButton.hidden=true;
      state.verified=result.ok; state.lastResult=result; saveNetworkState();
      appendCaseJournal(index,result); journalView=renderCaseJournal(root,journalView);
      showResult(result); if (result.ok) clue('verified');
      run.textContent=scenario.action; updateHud();
    }
    let reduced = true;
    try {reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;} catch (_) {}
    if (reduced || !window.setTimeout) {complete(); return;}
    output.replaceChildren(); output.className='output replaying';
    const replayLabel = add('strong','REPLAY IN PROGRESS',output,'result-title');
    const replayRows = add('div',null,output,'replay-stream');
    let packet = 0;
    function tick() {
      if (revision !== replayVersion) return;
      add('div',result.lines[packet],replayRows,'live-packet mono'); packet++;
      replayLabel.textContent='CHECK ' + packet + '/' + result.lines.length;
      if (visual.packets) visual.packets.textContent=result.lines.slice(0,packet).join('\n\n');
      if (packet === result.lines.length) {complete(); return;}
      replayTimer=window.setTimeout(tick,220);
    }
    showAllButton.hidden=false; finishReplay=complete;
    run.textContent='REPLAYING…'; replayTimer=window.setTimeout(tick,100); updateHud();
  },'cta');
  run.id = 'run-replay'; runButton=run;
  showAllButton=netButton('SHOW ALL RESULTS',tools,()=>{if (finishReplay) finishReplay();},'tool-btn skip-replay');
  showAllButton.hidden=true;
  nextEvidenceButton=netButton('OPEN NEXT UNREAD EVIDENCE',evidence,()=>{const next=scenario.records.findIndex((record,i)=>!inspected.has(i)); if (next>=0) inspect(next);},'tool-btn evidence-next');
  if (scenario.unsafe) {
    netButton(scenario.unsafe,tools,()=>{cancelReplay(); state.verified=false; state.lastResult=null; saveNetworkState(); updateHud(); unsafe(); output.replaceChildren(); output.textContent='Unsafe identity bypass recorded. No real credential was sent. Stop and verify through a trusted channel.'; output.className='output fail';},'danger');
    add('p','Dangerous action · costs one heart per attempt. All credentials here are fictional.',tools,'warning');
  }
  if (inspected.size === scenario.records.length) clue('inspected');
  if (state.lastResult) showResult(state.lastResult);
  if (state.verified && inspected.size === scenario.records.length) clue('verified');
  updateHud();
  journalView=renderCaseJournal(root);
  try {window.addEventListener('pagehide',cancelReplay);} catch (_) {}
  add('p','Local ITSO training sandbox · simulated people and traffic · nothing on your device or a real network changes.',root,'foot');
}


if (typeof module !== "undefined") module.exports={cases:NETWORK_CASES,meta:NETWORK_META,evaluateNetworkCase,evaluateRuleOrder};
