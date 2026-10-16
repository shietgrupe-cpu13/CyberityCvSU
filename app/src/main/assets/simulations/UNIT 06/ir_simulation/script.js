/* Cyberity — Incident Response Simulation: Stipend Day (level 605).
 * Local only. Fictional students, accounts, amounts and addresses
 * (documentation IP ranges); nothing is blocked, paid, sent or touched on the
 * device this runs on.
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

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function showBanner(id, kind, html) {
  var banner = document.getElementById(id);
  if (!banner) return;
  banner.className = 'banner show ' + kind;
  banner.innerHTML = html;
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/* ---------------------------------------------------------------------------
 * Incident state
 *
 * One record for every page: the clock, what has been found and done, and
 * whether the payout has gone out. sessionStorage first, window.name as the
 * fallback that survives a page load inside the WebView.
 * ------------------------------------------------------------------------ */
var STATE_KEY = 'ir_state';
var PAYOUT_AT = 240;          // minutes after 1:00 PM
var AT_RISK = 92000;

function freshState() {
  return {
    min: 0, dangers: {},
    verdicts: {}, alertsDone: false,
    rangeDone: false, payoutsDone: false, view: '',
    done: {}, released: false, heldInTime: false,
    picks: {}, notified: false,
    causeSeen: false, fixPicks: {}, fixed: false,
    fields: {}, reported: false
  };
}

function loadState() {
  var raw = null;
  try { raw = window.sessionStorage.getItem(STATE_KEY); } catch (e) { raw = null; }
  if (raw === null) {
    var tag = STATE_KEY + '=';
    raw = window.name.indexOf(tag) === 0 ? window.name.substring(tag.length) : null;
  }
  try { return raw ? JSON.parse(raw) : freshState(); } catch (e) { return freshState(); }
}

var S = loadState();

function saveState() {
  var raw = JSON.stringify(S);
  try { window.sessionStorage.setItem(STATE_KEY, raw); } catch (e) { /* ignored */ }
  window.name = STATE_KEY + '=' + raw;
}

function clockText(min) {
  var total = 13 * 60 + min;
  var h = Math.floor(total / 60);
  var m = total % 60;
  var h12 = h > 12 ? h - 12 : h;
  return h12 + ':' + (m < 10 ? '0' : '') + m + (h >= 12 ? ' PM' : ' AM');
}

/** Every action spends time; at 5:00 PM the payout goes out regardless. */
function spend(minutes) {
  S.min += minutes;
  if (!S.released && S.min >= PAYOUT_AT) {
    S.released = true;
    S.heldInTime = !!S.done.hold;
  }
  saveState();
  renderHud();
}

/** A dangerous move: costs a heart in the app and 20 wasted minutes here. */
function danger(clue) {
  S.dangers[clue] = true;
  spend(20);
  Cyberity.clueFound(clue);
}

function riskText() {
  if (!S.payoutsDone) return '<b>unknown</b>';
  if (S.done.hold && (S.heldInTime || !S.released)) return '<b class="good-text">₱0 · held</b>';
  if (S.released && !S.heldInTime) return '<b class="bad-text">₱92,000 LOST</b>';
  return '<b class="bad-text">₱92,000</b>';
}

function renderHud() {
  var hud = document.getElementById('hud');
  if (!hud) return;
  var left = PAYOUT_AT - S.min;
  var payout = S.released
    ? 'PAYOUT RELEASED'
    : 'PAYOUT IN ' + Math.floor(left / 60) + 'h ' + (left % 60) + 'm';
  var calm = S.done.hold || (S.released && S.heldInTime);
  hud.innerHTML =
    '<div class="hud' + (calm ? ' calm' : '') + '">' +
      '<span class="hud-clock mono">THU ' + clockText(S.min) + '</span>' +
      '<span class="hud-mid">' + payout + '</span>' +
      '<span class="hud-risk">AT RISK ' + riskText() + '</span>' +
    '</div>';
}

/* ---------------------------------------------------------------------------
 * HQ
 * ------------------------------------------------------------------------ */
function containedAll() {
  return !!(S.done.freeze && S.done.hold && S.done.block && S.done.reset);
}

function renderHq(mountId) {
  var phases = [
    ['alerts.html', '1 · DETECT', 'Alert queue', S.alertsDone],
    ['logs.html', '2 · SCOPE', 'Logs & search', S.rangeDone && S.payoutsDone],
    ['contain.html', '3 · CONTAIN', 'Before the 5:00 PM payout', containedAll()],
    ['notify.html', '4 · NOTIFY', 'Tell the right people', S.notified],
    ['cause.html', '5 · ROOT CAUSE', 'Why did it work?', S.fixed],
    ['report.html', '6 · WRITE-UP', 'Report and scorecard', S.reported]
  ];
  document.getElementById(mountId).innerHTML =
    '<div class="hq-head">' +
      '<h1>Incident HQ</h1>' +
      '<p>Student portal &middot; scholarship stipend day &middot; incident lead: <b>you</b></p>' +
    '</div>' +
    '<div class="explain">' +
      '<div class="explain-row"><b>5 PM</b><span>₱4,000 to each of 1,850 scholars, sent to the ' +
        'GCash number in their portal profile.</span></div>' +
      '<div class="explain-row"><b>Clock</b><span>Every action spends time. The payout won\'t wait.</span></div>' +
      '<div class="explain-row"><b>Hearts</b><span>Out of proportion, public, or phishing-like ' +
        'moves cost a heart and 20 minutes.</span></div>' +
    '</div>' +
    '<div class="section-label flush">Phases</div>' +
    phases.map(function (p) {
      return '<a class="choice tile ' + (p[3] ? 'read' : 'unread') + '" href="' + p[0] + '">' +
          '<span class="tile-body">' +
            '<span class="alert-top"><span class="tile-name">' + p[1] + '</span>' +
              '<span class="state ' + (p[3] ? 'ok">DONE' : 'todo">OPEN') + '</span></span>' +
            '<span class="tile-sub">' + escapeHtml(p[2]) + '</span>' +
          '</span>' +
        '</a>';
    }).join('');
}

/* ---------------------------------------------------------------------------
 * 1 · Detect
 * ------------------------------------------------------------------------ */
var ALERTS = [
  { id: 'a1', sev: 'HIGH', when: 'Tue 9:20 PM', real: true,
    title: 'Failed portal sign-ins: 41,163 in 40 hours',
    rows: ['normal: about 900 a day', 'accounts tried: 3,912',
      'source: mostly 192.0.2.0/24', 'pattern: 1 to 3 tries per account, then next'] },
  { id: 'a2', sev: 'LOW', when: 'Tue 11:41 PM', real: true,
    title: 'Payout number changed on 24 accounts this week',
    rows: ['normal: about 3 a week', 'first change: Tue 23:41', 'field: GCash payout number'] },
  { id: 'a3', sev: 'LOW', when: 'Tue 9:31 PM', real: true,
    title: 'Successful sign-ins from a hosting provider range',
    rows: ['range: 192.0.2.0/24 · CloudHost (servers)', 'accounts signed in: 57',
      'students in the last 180 days who', 'signed in from a hosting provider: 0'] },
  { id: 'a4', sev: 'HIGH', when: 'Mon 8:01 AM', real: false,
    title: 'Student portal CPU at 95%',
    rows: ['Mon 08:00 enrollment opened (announced)', 'back to normal by Mon 11:10',
      'before any of the failed sign-ins began'] },
  { id: 'a5', sev: 'MED', when: 'Wed 9:15 AM', real: false,
    title: 'Admin sign-in from a new device: registrar.admin',
    rows: ['TKT-0199, Wed 09:00: new laptop issued', 'to the registrar admin by ITSO',
      'on campus, MFA approved'] },
  { id: 'a6', sev: 'LOW', when: 'Thu 6:00 AM', real: false,
    title: 'Certificate for portal.cvsu.edu.ph expires in 30 days',
    rows: ['renewal ticket already open', 'nothing has expired'] },
  { id: 'a7', sev: 'MED', when: 'Thu 10:30 AM', real: false,
    title: 'Scholarship payout file exported',
    rows: ['by scholarship.staff, on campus', 'same export every stipend day,',
      'last 18 months'] }
];

function renderAlerts(mountId) {
  document.getElementById(mountId).innerHTML =
    '<div class="note top">Seven alerts since Tuesday night. Nobody has looked at them yet.</div>' +
    ALERTS.map(function (a) {
      var v = S.verdicts[a.id];
      return '<div class="choice card ' + (v ? 'read' : 'unread') + '">' +
          '<div class="alert-top"><span class="sev sev-' + a.sev.toLowerCase() + '">' + a.sev + '</span>' +
            '<span class="sms-when">' + escapeHtml(a.when) + '</span></div>' +
          '<div class="card-title">' + escapeHtml(a.title) + '</div>' +
          '<div class="rows mono">' + a.rows.map(escapeHtml).join('\n') + '</div>' +
          '<div class="verdict-row">' +
            '<button class="verdict-btn' + (v === 'real' ? ' picked-a' : '') +
              '" onclick="setVerdict(\'' + a.id + '\',\'real\')">REAL THREAT</button>' +
            '<button class="verdict-btn' + (v === 'noise' ? ' picked-b' : '') +
              '" onclick="setVerdict(\'' + a.id + '\',\'noise\')">NOISE</button>' +
          '</div>' +
        '</div>';
    }).join('') +
    (S.alertsDone ? '' : '<button class="cta" onclick="submitVerdicts()">SUBMIT VERDICTS</button>') +
    '<div class="banner' + (S.alertsDone ? ' show good' : '') + '" id="alerts-banner">' +
      (S.alertsDone ? 'Queue sorted. Three real alerts, one story.' : '') + '</div>';
}

function setVerdict(id, v) {
  if (S.alertsDone) return;
  S.verdicts[id] = v;
  saveState();
  renderAlerts('detail');
}

function submitVerdicts() {
  var missing = ALERTS.filter(function (a) { return !S.verdicts[a.id]; }).length;
  if (missing > 0) {
    showBanner('alerts-banner', 'warn', missing + ' alert' + (missing === 1 ? '' : 's') +
      ' still need a verdict.');
    return;
  }
  spend(5);
  var right = ALERTS.filter(function (a) { return (S.verdicts[a.id] === 'real') === a.real; }).length;
  if (right < ALERTS.length) {
    showBanner('alerts-banner', 'warn', right + ' of 7 correct. Does a schedule, a ticket or a ' +
      'routine explain it? Did it start before the attack did?');
    return;
  }
  S.alertsDone = true;
  saveState();
  renderAlerts('detail');
  Cyberity.clueFound('alerts_done');
}

/* ---------------------------------------------------------------------------
 * 2 · Scope
 *
 * 24 payout changes. 23 came from the attacker's range; one is a real
 * student who changed her own number, from her own phone, with a ticket.
 * ------------------------------------------------------------------------ */
var VICTIMS = [
  '202211304', '202310877', '202110452', '202312019', '202210661', '202010293',
  '202311740', '202111508', '202212935', '202310126', '202011847', '202213370',
  '202112664', '202310588', '202211912', '202010731', '202311263', '202112049',
  '202212487', '202310914', '202111336', '202011625', '202212208'
];

function payoutRows() {
  var rows = ['when       student    changed from', ''];
  VICTIMS.forEach(function (sn, i) {
    // One change roughly every 95 minutes from Tue 23:41, in time order.
    var at = 23 * 60 + 41 + i * 95;
    var day = ['Tue', 'Wed', 'Thu'][Math.floor(at / 1440)];
    var hh = Math.floor((at % 1440) / 60);
    var mm = at % 60;
    var t = day + ' ' + (hh < 10 ? '0' : '') + hh + ':' + (mm < 10 ? '0' : '') + mm;
    rows.push('<span class="hot">' + t + '  ' + sn + '  192.0.2.' + (14 + i * 9) + ' · CloudHost</span>');
    // The genuine change (Wed 10:02) falls between Wed 09:11 and Wed 10:46.
    if (i === 6) {
      rows.push('<span class="ok">Wed 10:02  202312045  198.51.100.140 · Indang, PH</span>');
      rows.push('<span class="ok">           her usual phone · TKT-0201:</span>');
      rows.push('<span class="ok">           "lost my old SIM, new GCash no."</span>');
    }
  });
  rows.push('', 'changes this week: 24', 'stipend per scholar: ₱4,000');
  return rows;
}

function renderLogs(mountId) {
  var result = '';
  if (S.view === 'range') {
    result =
      '<div class="artifact a-sys"><span class="artifact-tag">SIGN-INS · from 192.0.2.0/24 · Tue to Thu</span>' +
        '<div class="rows mono">' + [
          'first attempt:   Tue 21:14',
          'last attempt:    Thu 12:58 (still going)',
          '',
          '<span class="hot">failed:          41,106</span>',
          'accounts tried:  3,912 student accounts',
          '<span class="hot">successful:      57 accounts</span>',
          '',
          'after signing in, the attacker:',
          '  opened the profile page      57 accounts',
          '<span class="hot">  changed the payout number    23 accounts</span>',
          '  changed nothing else'
        ].join('\n') + '</div>' +
        '<div class="note">Location: CloudHost data centre. No student signs in from there.</div></div>';
  } else if (S.view === 'payouts') {
    result =
      '<div class="artifact a-sys"><span class="artifact-tag">PAYOUT NUMBER CHANGES · this week</span>' +
        '<div class="rows mono">' + payoutRows().join('\n') + '</div>' +
        '<div class="note">Check where each change came from. Not every change is the attacker.</div></div>';
  }

  document.getElementById(mountId).innerHTML =
    '<div class="note top">Each search spends 5 minutes.</div>' +
    '<button class="appui tool-btn" onclick="searchRange()"><b>SEARCH 192.0.2.0/24</b>' +
      'Every sign-in from the range in the alerts</button>' +
    '<button class="appui tool-btn" onclick="searchPayouts()"><b>PAYOUT NUMBER CHANGES</b>' +
      'Every GCash number change this week, and where it came from</button>' +
    result;
}

function searchRange() {
  if (S.view !== 'range') spend(5);
  S.view = 'range';
  S.rangeDone = true;
  saveState();
  renderLogs('detail');
  Cyberity.clueFound('range_searched');
}

function searchPayouts() {
  if (S.view !== 'payouts') spend(5);
  S.view = 'payouts';
  S.payoutsDone = true;
  saveState();
  renderHud();
  renderLogs('detail');
  Cyberity.clueFound('payouts_checked');
}

/* ---------------------------------------------------------------------------
 * 3 · Contain
 * ------------------------------------------------------------------------ */
var ACTIONS = [
  { id: 'freeze', cost: 10, name: 'Freeze payout number changes in the portal',
    sub: 'Nobody can change a GCash number until it\'s lifted. 10 min.' },
  { id: 'hold', cost: 15, name: 'Hold the 24 payouts whose number changed this week',
    sub: 'Everyone else is paid at 5 PM as normal. 15 min.' },
  { id: 'block', cost: 5, name: 'Block 192.0.2.0/24 at the portal firewall',
    sub: 'The range stops reaching the portal. 5 min.' },
  { id: 'reset', cost: 10, name: 'Sign out and force a password reset on the 57 accounts',
    sub: 'Ends the attacker\'s sessions. Owners set a new password. 10 min.' }
];

var DANGERS = [
  { id: 'cancel_all', name: 'Cancel today\'s stipend for all 1,850 scholars',
    why: '1,826 scholars who did nothing wrong lose the money they were promised today, ' +
      'to protect 24 payouts you could hold on their own.' },
  { id: 'portal_offline', name: 'Take the student portal offline',
    why: 'Enrollment is running. Thousands of students lose access to stop one attacker you can ' +
      'block by IP range and by freezing one field.' },
  { id: 'fb_post', name: 'Post the details on the CvSU Facebook page',
    why: 'A public post tells the attacker exactly what you\'ve found, and gives every scammer a ' +
      'story to use on worried scholars tonight.' }
];

function renderContain(mountId) {
  var released = S.released
    ? '<div class="banner show ' + (S.heldInTime ? 'good">5:00 PM payout released. The 24 held ' +
        'payouts stayed held.' : 'bad">5:00 PM payout released before the changed payouts were ' +
        'held. ₱92,000 went to the attacker\'s numbers.') + '</div>'
    : '';
  document.getElementById(mountId).innerHTML =
    released +
    '<div class="section-label flush">Containment actions</div>' +
    ACTIONS.map(function (a) {
      var done = S.done[a.id];
      return '<div class="device' + (done ? '' : ' odd') + '">' +
          '<div class="device-name">' + escapeHtml(a.name) + '</div>' +
          '<div class="device-sub">' + escapeHtml(a.sub) + '</div>' +
          (done ? '<span class="state ok">DONE</span>'
            : '<button class="small-btn" onclick="act(\'' + a.id + '\')">DO IT</button>') +
        '</div>';
    }).join('') +
    '<div class="banner" id="contain-banner"></div>' +
    '<div class="section-label flush">Other options</div>' +
    DANGERS.map(function (d) {
      return '<button class="cta danger" onclick="dangerAction(\'' + d.id + '\')">' +
        escapeHtml(d.name).toUpperCase() + '</button>';
    }).join('') +
    '<div class="banner" id="danger-banner"></div>';
}

function act(id) {
  if (S.done[id]) return;
  if (id === 'hold' && !S.done.freeze) {
    spend(5);
    showBanner('contain-banner', 'warn', 'Not yet. Payout numbers can still be changed, so any hold ' +
      'list is out of date the moment you finish it. Freeze changes first.');
    return;
  }
  var a = ACTIONS.filter(function (x) { return x.id === id; })[0];
  S.done[id] = true;
  spend(a.cost);
  renderContain('detail');
  if (id === 'hold') {
    showBanner('contain-banner', S.released && !S.heldInTime ? 'bad' : 'good', S.released && !S.heldInTime
      ? 'Held, but too late for today: the 5 PM payout already went out.'
      : '24 payouts held. ₱92,000 can no longer reach the attacker.');
  }
  if (containedAll()) {
    showBanner('contain-banner', 'good', 'Contained at ' + clockText(S.min) + '. The attacker is ' +
      'out of the accounts, off the portal, and can\'t change where the money goes.');
    Cyberity.clueFound('contained_all');
  }
}

function dangerAction(id) {
  var d = DANGERS.filter(function (x) { return x.id === id; })[0];
  danger(id);
  renderContain('detail');
  showBanner('danger-banner', 'bad', 'Blocked by the simulation, and 20 minutes lost arguing ' +
    'about it. ' + escapeHtml(d.why));
}

/* ---------------------------------------------------------------------------
 * 4 · Notify
 * ------------------------------------------------------------------------ */
var NOTIFY = [
  { id: 'n1', right: true, name: 'Data Protection Officer',
    sub: '57 students\' profiles (birthdays, addresses) were opened by the attacker.' },
  { id: 'n2', right: true, name: 'Scholarship Office',
    sub: 'They run the payout, and 24 scholars will ask why theirs is late.' },
  { id: 'n3', right: true, name: 'The 57 affected students, by portal notice and CvSU email',
    sub: 'Set a new password, never reuse it, and how to get a held payout released.' },
  { id: 'n4', right: false, danger: true, name: 'The affected students, by Messenger',
    sub: '"Please reply with your GCash number so we can confirm your stipend."' },
  { id: 'n5', right: false, name: 'All 1,850 scholars, by SMS blast',
    sub: '"The student portal has been hacked. Change your password now."' },
  { id: 'n6', right: true, name: 'The e-wallet provider\'s fraud team',
    sub: 'Report the 23 numbers the attacker added, so they can be flagged.' }
];

function renderNotify(mountId) {
  document.getElementById(mountId).innerHTML =
    '<div class="note top">Select everyone who needs to act. Nobody else.</div>' +
    NOTIFY.map(function (n) {
      var on = S.picks[n.id];
      return '<button class="cond' + (on ? ' on' : '') + '" onclick="pickNotify(\'' + n.id + '\')">' +
          '<span class="cond-box">' + (on ? '&#10003;' : '') + '</span>' +
          '<span class="cond-text"><b>' + escapeHtml(n.name) + '</b>' +
            '<span class="fix-line">' + escapeHtml(n.sub) + '</span></span>' +
        '</button>';
    }).join('') +
    (S.notified ? '' : '<button class="cta" onclick="sendNotify()">SEND NOTIFICATIONS</button>') +
    '<div class="banner' + (S.notified ? ' show good' : '') + '" id="notify-banner">' +
      (S.notified ? 'Sent through official channels.' : '') + '</div>';
}

function pickNotify(id) {
  if (S.notified) return;
  S.picks[id] = !S.picks[id];
  saveState();
  renderNotify('detail');
}

function sendNotify() {
  if (S.picks.n4) {
    danger('messenger_ask');
    showBanner('notify-banner', 'bad', 'Blocked by the simulation. "Reply with your GCash number" ' +
      'is exactly what the next scammer will send. If ITSO does it, students learn to answer it. ' +
      'Untick it.');
    return;
  }
  var picked = NOTIFY.filter(function (n) { return S.picks[n.id]; }).length;
  if (picked === 0) {
    showBanner('notify-banner', 'warn', 'Select at least one recipient.');
    return;
  }
  spend(10);
  var right = NOTIFY.filter(function (n) { return !!S.picks[n.id] === n.right; }).length;
  if (right < NOTIFY.length) {
    showBanner('notify-banner', 'warn', right + ' of 6 choices right. Who must act on this, and ' +
      'who would only panic?');
    return;
  }
  S.notified = true;
  saveState();
  renderNotify('detail');
  Cyberity.clueFound('notified');
}

/* ---------------------------------------------------------------------------
 * 5 · Root cause
 * ------------------------------------------------------------------------ */
var FIXES = [
  { id: 'f1', right: true, name: 'Limit sign-in attempts per IP and per account, with a CAPTCHA after 5 fails',
    sub: 'Affects: students who mistype a lot see a CAPTCHA.' },
  { id: 'f2', right: true, name: 'Refuse passwords that appear in known breach lists',
    sub: 'Affects: students with a leaked password must pick a new one at next sign-in.' },
  { id: 'f3', right: true, name: 'Changing a payout number needs a one-time code sent to the student\'s CvSU email',
    sub: 'Affects: 30 seconds more for about 3 genuine changes a week.' },
  { id: 'f4', right: false, name: 'Force every student to change their password every month',
    sub: 'Affects: 18,000 students, every month.' },
  { id: 'f5', right: false, name: 'Stop paying stipends to GCash; bank accounts only',
    sub: 'Affects: scholars without a bank account.' },
  { id: 'f6', right: false, name: 'Close the student portal outside office hours',
    sub: 'Affects: every student who works or studies at night.' }
];

function renderCause(mountId) {
  var settings = S.causeSeen
    ? '<div class="artifact a-sys"><span class="artifact-tag">PORTAL SECURITY · settings and checks</span>' +
        '<div class="rows mono">' + [
          '<span class="hot">sign-in attempt limit:     none</span>',
          '<span class="hot">CAPTCHA:                   none</span>',
          '<span class="hot">MFA for students:          not available</span>',
          '<span class="hot">payout number change:      password only</span>',
          '',
          'breach check on the 57 stolen passwords:',
          '<span class="warn">  49 appear in the "ShopZone 2025" leak</span>',
          '<span class="warn">   8 appear in other public leaks</span>',
          '',
          'CvSU portal database: <span class="ok">no sign of breach</span>'
        ].join('\n') + '</div></div>'
    : '<button class="appui tool-btn" onclick="openSettings()"><b>PORTAL SECURITY</b>' +
        'Sign-in limits, MFA, and where the stolen passwords came from</button>';

  document.getElementById(mountId).innerHTML =
    settings +
    '<div class="section-label flush">Proposed fixes</div>' +
    FIXES.map(function (f) {
      var on = S.fixPicks[f.id];
      return '<button class="cond' + (on ? ' on' : '') + '" onclick="pickFix(\'' + f.id + '\')">' +
          '<span class="cond-box">' + (on ? '&#10003;' : '') + '</span>' +
          '<span class="cond-text"><b>' + escapeHtml(f.name) + '</b>' +
            '<span class="fix-line">' + escapeHtml(f.sub) + '</span></span>' +
        '</button>';
    }).join('') +
    (S.fixed ? '' : '<button class="cta" onclick="applyFixes()">APPLY FIXES</button>') +
    '<div class="banner' + (S.fixed ? ' show good' : '') + '" id="fix-banner">' +
      (S.fixed ? 'Applied. Stuffing is throttled, leaked passwords are refused, and money can\'t ' +
        'be redirected with a password alone.' : '') + '</div>';
}

function openSettings() {
  S.causeSeen = true;
  spend(5);
  renderCause('detail');
  Cyberity.clueFound('cause_seen');
}

function pickFix(id) {
  if (S.fixed) return;
  S.fixPicks[id] = !S.fixPicks[id];
  saveState();
  renderCause('detail');
}

function applyFixes() {
  var picked = FIXES.filter(function (f) { return S.fixPicks[f.id]; }).length;
  if (picked === 0) {
    showBanner('fix-banner', 'warn', 'Select at least one fix.');
    return;
  }
  spend(10);
  var right = FIXES.filter(function (f) { return !!S.fixPicks[f.id] === f.right; }).length;
  if (right < FIXES.length) {
    showBanner('fix-banner', 'warn', right + ' of 6 choices right. Which fixes would have stopped ' +
      'this attacker, and which only make life harder for everyone?');
    return;
  }
  S.fixed = true;
  saveState();
  renderCause('detail');
  Cyberity.clueFound('fixes_done');
}

/* ---------------------------------------------------------------------------
 * 6 · Write-up and scorecard
 *
 * The money field has to match what actually happened in this run: held in
 * time, or paid out to the attacker.
 * ------------------------------------------------------------------------ */
var FIELDS = [
  { label: 'What happened', options: [
      'Phishing email sent to scholars',
      'Credential stuffing on the student portal, used to redirect stipend payouts',
      'Ransomware on a Scholarship Office PC',
      'An insider changed payout numbers'], correct: function () { return 1; } },
  { label: 'When it started', options: [
      'Tue 9:14 PM · first failed sign-ins from 192.0.2.0/24',
      'Wed 10:02 AM · first payout number change of the week',
      'Thu 1:00 PM · alerts reviewed by the incident lead'], correct: function () { return 0; } },
  { label: 'When it was discovered', options: [
      'Tue 9:14 PM · first failed sign-ins from 192.0.2.0/24',
      'Wed 10:02 AM · first payout number change of the week',
      'Thu 1:00 PM · alerts reviewed by the incident lead'], correct: function () { return 2; } },
  { label: 'Accounts taken over', options: ['24', '57', '1,850', '3,912'],
    correct: function () { return 1; } },
  { label: 'Payout number changes', options: [
      '24, all by the attacker',
      '23 by the attacker, 1 genuine (202312045, verified at the desk)',
      '57, all by the attacker'], correct: function () { return 1; } },
  { label: 'Money', options: [
      '₱92,000 at risk, held before the 5 PM payout',
      '₱92,000 at risk, paid out to the attacker\'s numbers',
      '₱96,000 at risk, held',
      '₱7,400,000 at risk, all stipends cancelled'],
    correct: function () { return S.heldInTime || (S.done.hold && !S.released) ? 0 : 1; } },
  { label: 'Actions taken', options: [
      'Froze payout changes, held 24 payouts, blocked the range, reset 57 accounts; told the DPO, ' +
        'Scholarship Office, students and the e-wallet provider',
      'Cancelled all stipends and took the portal offline',
      'Posted a warning on Facebook and messaged students on Messenger',
      'No action yet'], correct: function () { return 0; } }
];

function renderReport(mountId) {
  document.getElementById(mountId).innerHTML =
    '<div class="artifact a-sys"><span class="artifact-tag">ITSO INCIDENT REPORT · STIPEND DAY</span>' +
      FIELDS.map(function (f, i) {
        var v = S.fields[i];
        return '<label class="form-row"><span class="form-label">' + escapeHtml(f.label) + '</span>' +
          '<select class="form-select" id="field-' + i + '" onchange="setField(' + i + ', this.value)"' +
            (S.reported ? ' disabled' : '') + '>' +
            '<option value="">Choose…</option>' +
            f.options.map(function (o, j) {
              return '<option value="' + j + '"' + (String(v) === String(j) ? ' selected' : '') + '>' +
                escapeHtml(o) + '</option>';
            }).join('') +
          '</select></label>';
      }).join('') +
    '</div>' +
    (S.reported ? '' : '<button class="cta" onclick="submitReport()">SUBMIT REPORT</button>') +
    '<div class="banner" id="report-banner"></div>' +
    '<div id="scorecard"></div>';
  if (S.reported) showScore();
}

function setField(i, value) {
  S.fields[i] = value;
  saveState();
}

function submitReport() {
  var missing = FIELDS.filter(function (f, i) {
    return S.fields[i] === undefined || S.fields[i] === '';
  }).length;
  if (missing > 0) {
    showBanner('report-banner', 'warn', missing + ' field' + (missing === 1 ? ' is' : 's are') +
      ' still empty.');
    return;
  }
  if (!containedAll() || !S.notified) {
    showBanner('report-banner', 'warn', 'The report can\'t be closed while containment or ' +
      'notification is unfinished. Finish phases 3 and 4.');
    return;
  }
  spend(5);
  var right = FIELDS.filter(function (f, i) { return Number(S.fields[i]) === f.correct(); }).length;
  if (right < FIELDS.length) {
    showBanner('report-banner', 'warn', right + ' of 7 correct. Watch the difference between when ' +
      'it started and when it was found, and the one payout change that wasn\'t the attacker.');
    return;
  }
  S.reported = true;
  saveState();
  renderReport('detail');
  Cyberity.clueFound('report_done');
  Cyberity.flagDiscovered('report_done');
}

function showScore() {
  var hearts = Object.keys(S.dangers).length;
  var saved = S.heldInTime || (S.done.hold && !S.released);
  var grade = !saved ? 'ROOKIE' : (hearts === 0 ? 'INCIDENT LEAD' : 'ANALYST');
  var note = !saved
    ? 'The money left before it was held. Next run: freeze and hold first, investigate after.'
    : (hearts === 0
      ? 'Every peso protected, nothing out of proportion. That\'s the job.'
      : 'Money protected, but ' + hearts + ' move' + (hearts === 1 ? '' : 's') +
        ' cost a heart. Replay for a clean run.');
  document.getElementById('scorecard').innerHTML =
    '<div class="banner show good">Report accepted. Case code: ' +
      '<b class="mono">CYBERITY{1nc1d3nt_l34d}</b></div>' +
    '<div class="scorecard">' +
      '<div class="grade">' + grade + '</div>' +
      '<div class="score-grid">' +
        '<div><b class="mono">' + (saved ? '₱92,000' : '₱0') + '</b><span>protected</span></div>' +
        '<div><b class="mono">' + clockText(S.min) + '</b><span>finished</span></div>' +
        '<div><b class="mono">' + hearts + '</b><span>hearts lost</span></div>' +
        '<div><b class="mono">1,826</b><span>paid on time</span></div>' +
      '</div>' +
      '<div class="note">' + escapeHtml(note) + '</div>' +
    '</div>';
}
