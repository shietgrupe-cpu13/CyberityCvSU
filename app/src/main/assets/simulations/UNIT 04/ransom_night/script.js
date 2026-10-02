/* Cyberity — Ransomware at 2 AM (level 403).
 * Local only. Fictional department, hosts, gang and backups; nothing is
 * encrypted, sent or paid. The "chat" is scripted and offline.
 *
 * Five tools, one per page:
 *   monitor.html  live share monitor + network diagram: find and isolate the source
 *   chat.html     the gang's "support" portal: what they really want
 *   backups.html  test-restore five backups and see which survived
 *   restore.html  pick a restore point on the attack timeline
 *   plan.html     build a 3-2-1 backup plan that would have saved the night
 */

var Cyberity = (function () {
  function safe(fn) {
    try { if (typeof AndroidLab !== 'undefined') fn(); } catch (e) { /* preview */ }
  }
  return {
    clueFound: function (id) { safe(function () { AndroidLab.notifyClueFound(id); }); },
    flagDiscovered: function (token) { safe(function () { AndroidLab.notifyFlagDiscovered(token); }); }
  };
})();

/* Small persistent set. sessionStorage first, window.name as the fallback
   that survives a page load inside the WebView. */
var Store = {
  KEY: 'rn_state',
  raw: function () {
    var raw = null;
    try { raw = window.sessionStorage.getItem(Store.KEY); } catch (e) { raw = null; }
    if (raw === null) {
      var tag = Store.KEY + '=';
      raw = window.name.indexOf(tag) === 0 ? window.name.substring(tag.length) : '';
    }
    return raw || '';
  },
  has: function (id) { return Store.raw().split(',').indexOf(id) !== -1; },
  mark: function (id) {
    if (Store.has(id)) return;
    var ids = Store.raw().split(',').filter(function (s) { return s.length > 0; });
    ids.push(id);
    var raw = ids.join(',');
    try { window.sessionStorage.setItem(Store.KEY, raw); } catch (e) { /* ignored */ }
    window.name = Store.KEY + '=' + raw;
  },
  /* the frozen counter value, kept as "count:1234" */
  count: function () {
    var hit = Store.raw().split(',').filter(function (s) { return s.indexOf('count:') === 0; })[0];
    return hit ? parseInt(hit.substring(6), 10) : null;
  }
};

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function $(id) { return document.getElementById(id); }

function showBanner(id, kind, html) {
  var b = $(id);
  b.className = 'banner show ' + kind;
  b.innerHTML = html;
  b.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function fmt(n) { return String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ','); }

/* ===========================================================================
 * 1 · LIVE SHARE MONITOR
 *
 * The department share is being encrypted right now. The feed mixes the
 * attack with ordinary late-night traffic; the student finds the machine
 * writing .lockd files and pulls its cable on the network diagram. Cutting
 * the internet, the share, or the wrong PC teaches something but doesn't
 * stop it.
 * ========================================================================= */
var TOTAL_FILES = 9412;
var encrypted = 1184;
var contained = false;
var timers = [];

var NODES = {
  inet:  { name: 'INTERNET', sub: 'campus uplink', x: 130, y: 6,   w: 100 },
  nas:   { name: 'CEIT-NAS', sub: 'dept. share',   x: 262, y: 74,  w: 92 },
  sec:   { name: 'CEIT-SEC-02', sub: 'secretary',  x: 2,   y: 170, w: 86 },
  fac:   { name: 'CEIT-FAC-05', sub: 'faculty',    x: 92,  y: 170, w: 86 },
  dean:  { name: 'CEIT-DEAN-01', sub: "dean's office", x: 182, y: 170, w: 86 },
  lab:   { name: 'CEIT-LAB-11', sub: 'comp. lab',  x: 272, y: 170, w: 86 }
};
var NODE_ORDER = ['inet', 'nas', 'sec', 'fac', 'dean', 'lab'];
var cut = {};

var FILES = [
  'Grades\\BSIT-3A_Midterm.xlsx', 'Grades\\BSCS-2B_Prelim.xlsx', 'Thesis\\2025\\Group07_Final.docx',
  'Records\\Enrollment_1stSem.xlsx', 'Admin\\Faculty_Loading.xlsx', 'Thesis\\2026\\Proposal_G12.docx',
  'Grades\\BSCpE-4A_Final.xlsx', 'Admin\\Memo_0912.pdf', 'Records\\Shiftees_2026.xlsx',
  'Thesis\\2024\\Group03_Final.pdf', 'Admin\\Budget_Q3.xlsx', 'Grades\\BSIT-1C_Prelim.xlsx'
];

var NOISE = [
  '<span class="host">CEIT-FAC-05 </span> OPEN  Syllabus_IT101.docx',
  '<span class="host">CEIT-LAB-11 </span> READ  Software\\update.msi <span class="ok">(scheduled)</span>',
  '<span class="host">CEIT-FAC-05 </span> SAVE  Syllabus_IT101.docx',
  '<span class="host">CEIT-DEAN-01</span> idle  screen locked'
];

function clock(i) {
  var s = 14 * 60 + 7 + i;
  return '02:' + String(Math.floor(s / 60)).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');
}

var tick = 0;
function feedLine() {
  tick++;
  var line;
  if (!contained && tick % 3 !== 0) {
    var f = FILES[tick % FILES.length];
    line = '<span class="host">CEIT-SEC-02 </span> <span class="hot">WRITE ' + escapeHtml(f) + '.lockd</span>';
  } else if (contained && tick % 2 === 0) {
    line = '<span class="host">CEIT-SEC-02 </span> <span class="ok">-- no connection --</span>';
  } else {
    line = NOISE[tick % NOISE.length];
  }
  var feed = $('feed');
  var div = document.createElement('div');
  div.className = 'ln';
  div.innerHTML = clock(tick) + ' ' + line;
  feed.insertBefore(div, feed.firstChild);
  while (feed.children.length > 9) feed.removeChild(feed.lastChild);
}

function paintCounter() {
  $('count').textContent = fmt(encrypted);
  $('counter').className = 'counter' + (contained ? ' stopped' : '');
  $('counter-label').innerHTML = '<span class="pulse"></span>' +
    (contained ? 'Encryption stopped' : 'Files encrypted on \\\\CEIT-NAS\\Share');
  $('counter-sub').textContent = contained
    ? 'Contained. ' + fmt(TOTAL_FILES - encrypted) + ' files were saved by pulling one cable.'
    : 'of ' + fmt(TOTAL_FILES) + ' files · still climbing';
}

function renderMonitor() {
  Cyberity.clueFound('monitor_viewed');
  var saved = Store.count();
  if (Store.has('contained') && saved !== null) { contained = true; encrypted = saved; cut.sec = true; }
  drawNet();
  paintCounter();
  for (var i = 0; i < 6; i++) feedLine();
  timers.push(setInterval(function () {
    if (contained) return;
    encrypted = Math.min(TOTAL_FILES, encrypted + 4 + Math.floor(Math.random() * 7));
    paintCounter();
  }, 400));
  timers.push(setInterval(feedLine, 900));
}

function drawNet() {
  var sw = { x: 140, y: 82, w: 80, h: 30 };
  var svg = '<svg class="net-svg" viewBox="0 0 360 210" role="img" aria-label="Network diagram">';
  NODE_ORDER.forEach(function (k) {
    var n = NODES[k];
    var nx = n.x + n.w / 2, ny = n.y + (k === 'inet' ? 30 : (k === 'nas' ? 15 : 0));
    var sx = k === 'nas' ? sw.x + sw.w : sw.x + sw.w / 2, sy = k === 'inet' ? sw.y : (k === 'nas' ? sw.y + 15 : sw.y + sw.h);
    if (k === 'nas') { nx = n.x; }
    svg += '<line class="cable' + (cut[k] ? ' cut' : '') + '" x1="' + sx + '" y1="' + sy + '" x2="' + nx + '" y2="' + ny + '"/>';
  });
  svg += '<rect class="switch-box" x="' + sw.x + '" y="' + sw.y + '" width="' + sw.w + '" height="' + sw.h + '" rx="6"/>' +
    '<text class="switch-name" x="180" y="101" text-anchor="middle">CEIT SWITCH</text>';
  NODE_ORDER.forEach(function (k) {
    var n = NODES[k];
    var cls = 'node-box' + (cut[k] ? (k === 'sec' ? ' safe' : ' cut') : '');
    svg += '<g onclick="unplug(\'' + k + '\')" style="cursor:pointer">' +
      '<rect class="' + cls + '" x="' + n.x + '" y="' + n.y + '" width="' + n.w + '" height="30" rx="7"/>' +
      '<text class="node-name" x="' + (n.x + n.w / 2) + '" y="' + (n.y + 13) + '" text-anchor="middle">' + n.name + '</text>' +
      '<text class="node-sub" x="' + (n.x + n.w / 2) + '" y="' + (n.y + 24) + '" text-anchor="middle">' +
        (cut[k] ? 'UNPLUGGED' : n.sub) + '</text>' +
    '</g>';
  });
  svg += '</svg>';
  $('net').innerHTML = svg;
}

function unplug(k) {
  if (contained && k === 'sec') return;
  if (k === 'sec') {
    cut.sec = true;
    contained = true;
    drawNet();
    paintCounter();
    Store.mark('contained');
    Store.mark('count:' + encrypted);
    showBanner('net-banner', 'good', '<b>Isolated.</b> The writes stopped the moment CEIT-SEC-02 ' +
      'lost its connection. It stays <b>powered on</b>: what is in its memory is evidence, and ' +
      'turning it off can destroy it. ' + fmt(encrypted) + ' files are encrypted; ' +
      fmt(TOTAL_FILES - encrypted) + ' are safe.');
    Cyberity.clueFound('contained');
    return;
  }
  if (cut[k]) return;
  if (k === 'nas') {
    showBanner('net-banner', 'warn', 'You unplug the NAS, and every faculty member loses the ' +
      'share. CEIT-SEC-02 is still running and still reaching every other machine on the ' +
      'switch. ITSO plugs the NAS back in: <b>isolate the source</b>, not the victim.');
    Cyberity.clueFound('tried_nas');
    return;
  }
  cut[k] = true;
  drawNet();
  if (k === 'inet') {
    showBanner('net-banner', 'warn', 'The campus uplink is cut, and the counter keeps climbing. ' +
      'The ransomware already has the key and the files it needs <b>inside</b> the network. ' +
      'It doesn’t need the internet to encrypt.');
    Cyberity.clueFound('tried_internet');
    return;
  }
  var who = { fac: 'CEIT-FAC-05, where a faculty member was working late on a syllabus,',
    dean: 'CEIT-DEAN-01, the dean’s locked and idle PC,',
    lab: 'CEIT-LAB-11, which was only running its scheduled update,' }[k];
  showBanner('net-banner', 'warn', who + ' is now offline, and the counter keeps climbing. ' +
    'Read the feed again: <b>which machine is writing the .lockd files?</b>');
  Cyberity.clueFound('tried_wrong_pc');
}

/* ===========================================================================
 * 2 · THE GANG'S "SUPPORT" CHAT
 *
 * Ransomware groups run help desks. Asking the right questions shows the two
 * things that matter: there is no guarantee, and they took a copy of the data.
 * ========================================================================= */
var CHAT = {
  price: {
    q: 'How much do you want?',
    a: '3,500,000 pesos in Bitcoin. Pay within 48 hours and we give 50% discount. After 72 ' +
      'hours the price doubles.'
  },
  proof: {
    q: 'How do we know you can even decrypt our files?',
    a: 'Send us 1 small file. We decrypt free, as proof. We are professionals.'
  },
  backup: {
    q: 'We have backups. Why would we pay?',
    a: 'Good for you :) Restore your files. But before we encrypted, we downloaded 18 GB from ' +
      'your share. Student records, grades, addresses. If you don’t pay, we publish ' +
      'everything on our blog. Sample below.',
    attach: 'enrollment_1stsem_2026.csv  (sample, 3 of 2,140 rows)\n' +
      'student_no, name, program, address, mobile\n' +
      '2023•••••, D•• C•••, BSIT, Indang Cavite, 0917•••••\n' +
      '2024•••••, M•• R•••, BSCS, Tanza Cavite, 0928•••••',
    clue: 'leak_threat'
  },
  delete: {
    q: 'If we pay, will you delete the copy you took?',
    a: 'Yes of course. You have our word.',
    needs: 'backup'
  },
  who: {
    q: 'Who are you?',
    a: 'LockD. We are a business, nothing personal. Your security was weak, we only show you. ' +
      'Many universities already paid us.'
  }
};
var CHAT_ORDER = ['price', 'proof', 'backup', 'delete', 'who'];
var asked = {};
var busy = false;

function renderChat() {
  Cyberity.clueFound('chat_opened');
  addMsg('them', 'Hello CEIT. All your files are encrypted with military grade algorithm. ' +
    'Ask your questions here. Be fast, the timer is running.');
  renderReplies();
}

function addMsg(who, text, attach) {
  var d = document.createElement('div');
  d.className = 'msg ' + who;
  d.innerHTML = '<div class="msg-who">' + (who === 'them' ? 'LOCKD SUPPORT' : 'YOU · ITSO') + '</div>' +
    escapeHtml(text) + (attach ? '<div class="attach">' + escapeHtml(attach) + '</div>' : '');
  $('chat').appendChild(d);
  d.scrollIntoView({ behavior: 'smooth', block: 'end' });
}

function renderReplies() {
  var html = CHAT_ORDER.filter(function (k) { return !CHAT[k].needs || asked[CHAT[k].needs]; })
    .map(function (k) {
      return '<button class="reply' + (asked[k] ? ' used' : '') + '" onclick="ask(\'' + k + '\')">' +
        escapeHtml(CHAT[k].q) + '</button>';
    }).join('');
  html += '<button class="reply danger" onclick="agreeToPay()">OK. We will pay. Send the wallet address.</button>';
  $('replies').innerHTML = html;
}

function ask(k) {
  if (busy || asked[k]) return;
  busy = true;
  asked[k] = true;
  var c = CHAT[k];
  addMsg('us', c.q);
  renderReplies();
  var t = document.createElement('div');
  t.className = 'typing';
  t.textContent = 'LockD Support is typing…';
  $('chat').appendChild(t);
  setTimeout(function () {
    t.parentNode.removeChild(t);
    addMsg('them', c.a, c.attach);
    if (c.clue) Cyberity.clueFound(c.clue);
    busy = false;
    var all = CHAT_ORDER.every(function (q) { return asked[q]; });
    if (all) Cyberity.clueFound('chat_complete');
  }, 900);
}

/** Dangerous: agreeing to pay. */
function agreeToPay() {
  Cyberity.clueFound('ransom_paid');
  showBanner('chat-banner', 'bad', 'Blocked by the simulation. Paying funds the next attack and ' +
    'buys only a promise from the people who just robbed you: no key is guaranteed, and the ' +
    'stolen copy of the student records can be sold or leaked anyway. Paying also marks CvSU as ' +
    'a target that pays. Decisions like this go to university leadership, legal counsel and the ' +
    'authorities, never to a chat window at 2 AM.');
}

/* ===========================================================================
 * 3 · BACKUP TEST BENCH
 *
 * Five places the department thought were backups. Each one is restored onto
 * a clean, isolated test PC. Whatever the infected machine could reach, the
 * ransomware reached too.
 * ========================================================================= */
var BACKUPS = {
  b1: {
    name: 'USB external drive', sub: 'always plugged into CEIT-SEC-02 · copies every night',
    ok: false,
    result: '<b class="hot">ENCRYPTED.</b> Every file ends in .lockd. The drive was connected ' +
      'to the infected PC, so to the ransomware it was just another folder.'
  },
  b2: {
    name: 'NAS snapshot folder', sub: '\\\\CEIT-NAS\\backup · mapped as drive Z: on every PC',
    ok: false,
    result: '<b class="hot">GONE.</b> The snapshots were deleted Wednesday night using the ' +
      'admin password the attacker stole. Anything an admin can delete, an attacker with the ' +
      'admin password can delete.'
  },
  b3: {
    name: 'Cloud sync folder', sub: 'mirrors the share to a cloud drive in real time',
    ok: false,
    result: '<b class="hot">SYNCED THE DAMAGE.</b> Sync copies every change, and encryption is a ' +
      'change. The cloud now holds 6,431 .lockd files. <b>Sync is not a backup.</b>'
  },
  b4: {
    name: 'Offline backup disk', sub: 'Sunday 23:00 · unplugged after backup · locked in the dept. cabinet',
    ok: true, offline: true,
    result: '<b class="ok">RESTORED.</b> All files open normally. The disk was unplugged and in a ' +
      'cabinet when the encryption ran, so there was nothing to reach.'
  },
  b5: {
    name: 'Cloud backup service', sub: 'nightly · immutable versions kept for 30 days',
    ok: true,
    result: '<b class="ok">RESTORED.</b> Versions from Sunday to Wednesday are intact. ' +
      '"Immutable" means they cannot be changed or deleted for 30 days, not even with the admin ' +
      'password the attacker had.'
  }
};
var BK_ORDER = ['b1', 'b2', 'b3', 'b4', 'b5'];

function renderBackups() {
  Cyberity.clueFound('backups_opened');
  $('bench').innerHTML = BK_ORDER.map(function (k) {
    var b = BACKUPS[k];
    var done = Store.has('tested_' + k);
    return '<div class="bk' + (done ? (b.ok ? ' good' : ' bad') : '') + '" id="bk-' + k + '">' +
        '<div class="bk-name">' + escapeHtml(b.name) + '</div>' +
        '<div class="bk-sub">' + escapeHtml(b.sub) + '</div>' +
        '<div class="bk-actions">' +
          '<button class="small-btn" onclick="testRestore(\'' + k + '\')">TEST RESTORE ON A CLEAN PC</button>' +
          (b.offline ? '<button class="small-btn danger" onclick="plugIntoInfected()">PLUG INTO CEIT-SEC-02</button>' : '') +
        '</div>' +
        '<div class="bar" id="bar-' + k + '"><i></i></div>' +
        '<div class="bk-result' + (done ? ' show' : '') + '" id="res-' + k + '">' + b.result + '</div>' +
      '</div>';
  }).join('');
  updateTested();
}

function testRestore(k) {
  var bar = $('bar-' + k);
  if (Store.has('tested_' + k) || bar.classList.contains('show')) return;
  bar.className = 'bar show';
  setTimeout(function () { bar.firstChild.style.width = '100%'; }, 30);
  setTimeout(function () {
    bar.className = 'bar';
    Store.mark('tested_' + k);
    $('bk-' + k).className = 'bk ' + (BACKUPS[k].ok ? 'good' : 'bad');
    $('res-' + k).className = 'bk-result show';
    updateTested();
  }, 1200);
}

function updateTested() {
  var n = BK_ORDER.filter(function (k) { return Store.has('tested_' + k); }).length;
  $('tested-count').innerHTML = '<b>' + n + ' / 5</b> backups tested';
  if (n === BK_ORDER.length) Cyberity.clueFound('tested_all');
}

/** Dangerous: connecting the last clean backup to the infected machine. */
function plugIntoInfected() {
  Cyberity.clueFound('backup_exposed');
  showBanner('bench-banner', 'bad', 'Blocked by the simulation. CEIT-SEC-02 is still infected. ' +
    'Plugging the department’s last clean offline copy into it would hand the ransomware ' +
    'one more drive to encrypt. Restore onto a clean, isolated machine, never onto the patient.');
}

/* ===========================================================================
 * 4 · RESTORE POINT PICKER
 *
 * The encryption ran Thursday, but the break-in was Monday. Every snapshot
 * taken after Monday 09:12 carries the attacker's remote-access tool.
 * ========================================================================= */
var TIMELINE = [
  { when: 'Sun 23:00', what: 'Nightly backup snapshot', kind: 'snap' },
  { when: 'Mon 09:12', what: 'The secretary opens an "unpaid invoice" email attachment. It ' +
    'quietly installs a remote-access tool, RemoteSupport_svc.exe.', kind: 'attack' },
  { when: 'Mon 23:00', what: 'Nightly backup snapshot', kind: 'snap' },
  { when: 'Tue 21:40', what: 'Attacker logs in with a stolen admin password and copies 18 GB ' +
    'of student records out.', kind: 'attack' },
  { when: 'Tue 23:00', what: 'Nightly backup snapshot', kind: 'snap' },
  { when: 'Wed 22:10', what: 'Attacker deletes the NAS snapshots.', kind: 'attack' },
  { when: 'Wed 23:00', what: 'Nightly backup snapshot', kind: 'snap' },
  { when: 'Thu 02:00', what: 'Encryption starts on CEIT-SEC-02. ITSO is paged at 02:14.', kind: 'attack' }
];

var SNAPS = {
  wed: { label: 'Wed 23:00', clean: false, lost: 'about 3 hours of work' },
  tue: { label: 'Tue 23:00', clean: false, lost: 'one day of work' },
  mon: { label: 'Mon 23:00', clean: false, lost: 'two days of work' },
  sun: { label: 'Sun 23:00', clean: true, lost: 'Monday to Wednesday’s edits' }
};
var SNAP_ORDER = ['wed', 'tue', 'mon', 'sun'];
var pickedSnap = null;

function renderRestore() {
  Cyberity.clueFound('restore_opened');
  $('timeline').innerHTML = TIMELINE.map(function (e) {
    return '<div class="tl-row ' + e.kind + '"><div class="tl-when">' + e.when + '</div>' +
      '<div class="tl-what">' + escapeHtml(e.what) + '</div></div>';
  }).join('');
  renderSnapPick();
}

function renderSnapPick() {
  $('snaps').innerHTML = SNAP_ORDER.map(function (k) {
    return '<button class="chip' + (pickedSnap === k ? ' on' : '') + '" onclick="pickSnap(\'' + k + '\')">' +
      SNAPS[k].label + '</button>';
  }).join('');
}

function pickSnap(k) {
  pickedSnap = k;
  renderSnapPick();
  $('restore-banner').className = 'banner';
}

function runRestore() {
  if (!pickedSnap) {
    showBanner('restore-banner', 'warn', 'Pick a snapshot first.');
    return;
  }
  var s = SNAPS[pickedSnap];
  Cyberity.clueFound('restore_tried');
  if (!s.clean) {
    showBanner('restore-banner', 'bad', 'Restored ' + s.label + ' to an isolated test PC. Files ' +
      'open fine, and you’d only lose ' + s.lost + '. But the scan finds ' +
      '<b>RemoteSupport_svc.exe</b>, installed Monday 09:12. Put this back into service and you ' +
      'hand the attacker their door back. <b>Restore from before the break-in, not just before ' +
      'the encryption.</b>');
    return;
  }
  showBanner('restore-banner', 'good', 'Restored Sun 23:00 to an isolated test PC. Scan: ' +
    '<b>clean</b>. You lose ' + s.lost + ', which staff can rebuild from email and paper. ' +
    'Recovery code: <b class="mono">CYBERITY{r3st0r3_b3f0r3_th3_br34ch}</b>');
  Cyberity.clueFound('clean_restore');
  Cyberity.flagDiscovered('restore_code');
}

/* ===========================================================================
 * 5 · 3-2-1 PLAN BUILDER
 *
 * Copy 1 is the live share. The student configures two more copies and the
 * checklist lights up as the plan meets 3-2-1, plus the extra "1" that
 * ransomware made necessary: one copy offline or immutable.
 * ========================================================================= */
var MEDIA = { nas: 'Same NAS', disk: 'External disk', cloud: 'Cloud backup' };
var PLACES = { on: 'On campus', off: 'Off campus' };
var GUARDS = { live: 'Always connected', offline: 'Unplugged after backup', immutable: 'Immutable versions' };
var plan = { c2: {}, c3: {} };

function renderPlan() {
  Cyberity.clueFound('plan_opened');
  ['c2', 'c3'].forEach(function (c) {
    var p = plan[c];
    function group(kind, opts, val) {
      return Object.keys(opts).map(function (k) {
        var disabled = kind === 'place' && p.medium === 'cloud' && k === 'on';
        disabled = disabled || (kind === 'guard' && p.medium === 'nas' && k === 'offline');
        return '<button class="chip' + (val === k ? ' on' : '') + '"' + (disabled ? ' disabled' : '') +
          ' onclick="setPlan(\'' + c + '\',\'' + kind + '\',\'' + k + '\')">' + opts[k] + '</button>';
      }).join('');
    }
    $(c).innerHTML =
      '<div class="copy-h">Copy ' + (c === 'c2' ? 2 : 3) + ' <span>a backup</span></div>' +
      '<div class="opt-label">Medium</div><div class="chips">' + group('medium', MEDIA, p.medium) + '</div>' +
      '<div class="opt-label">Where</div><div class="chips">' + group('place', PLACES, p.place) + '</div>' +
      '<div class="opt-label">Protection</div><div class="chips">' + group('guard', GUARDS, p.guard) + '</div>';
  });
  checkPlan();
}

function setPlan(c, kind, k) {
  plan[c][kind] = k;
  if (kind === 'medium' && k === 'cloud') plan[c].place = 'off';
  if (kind === 'medium' && k === 'nas') { plan[c].place = 'on'; if (plan[c].guard === 'offline') plan[c].guard = null; }
  renderPlan();
}

function complete(p) { return p.medium && p.place && p.guard; }

function checkPlan() {
  var c2 = plan.c2, c3 = plan.c3;
  var both = complete(c2) && complete(c3);
  var three = both && c2.medium !== 'nas' && c3.medium !== 'nas';
  var media = {}; media.nas = true;
  [c2, c3].forEach(function (p) { if (p.medium) media[p.medium] = true; });
  var two = both && Object.keys(media).length >= 2;
  var one = both && (c2.place === 'off' || c3.place === 'off');
  var safe = both && [c2, c3].some(function (p) { return p.medium !== 'nas' && (p.guard === 'offline' || p.guard === 'immutable'); });
  var rules = [
    [three, '<b>3</b> copies of the data, on separate devices (a copy on the same NAS dies with it)'],
    [two, '<b>2</b> different kinds of storage'],
    [one, '<b>1</b> copy off campus'],
    [safe, '<b>1</b> copy offline or immutable, out of the attacker’s reach']
  ];
  $('rules').innerHTML = rules.map(function (r) {
    return '<div class="rule' + (r[0] ? ' on' : '') + '"><i>✓</i><span>' + r[1] + '</span></div>';
  }).join('');
  var ok = three && two && one && safe;
  var b = $('plan-banner');
  if (ok) {
    b.className = 'banner show good';
    b.innerHTML = 'Plan approved: 3-2-1, with one copy the ransomware could never have touched. ' +
      'Last step for the real thing: <b>test a restore every month</b>. A backup you’ve never ' +
      'restored is only a hope.';
    Cyberity.clueFound('plan_valid');
  } else {
    b.className = 'banner';
  }
}
