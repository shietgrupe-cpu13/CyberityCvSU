/* Cyberity — Containment Console (level 603).
 * Local only. Fictional accounts, hosts and addresses (documentation IP and
 * phone ranges); nothing is blocked, purged or touched on the device this
 * runs on.
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

/* ---------------------------------------------------------------------------
 * Incident state
 *
 * Every page reads and writes the same record: what has been contained,
 * saved, blocked and purged, the incident clock, and the phishing counter.
 * sessionStorage first, window.name as the fallback that survives a page load
 * inside the WebView.
 * ------------------------------------------------------------------------ */
var STATE_KEY = 'co_state';

function freshState() {
  return {
    sent: 82, stopped: false, min: 0,
    contained: {}, ev: {}, rule: {},
    ip: false, dns: false, purged: false, clicks: false, students: false, mfa: false
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

/** Every action takes time on the incident clock. */
function spend(minutes) {
  S.min += minutes;
  saveState();
  renderHud();
}

function clockText() {
  var total = 20 + S.min;
  var h = 8 + Math.floor(total / 60);
  var m = total % 60;
  return 'MON ' + h + ':' + (m < 10 ? '0' : '') + m + ' AM';
}

function showBanner(id, kind, html) {
  var banner = document.getElementById(id);
  if (!banner) return;
  banner.className = 'banner show ' + kind;
  banner.innerHTML = html;
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/* ---------------------------------------------------------------------------
 * Heads-up bar: the clock and the counter that won't stop on its own
 * ------------------------------------------------------------------------ */
function renderHud() {
  var hud = document.getElementById('hud');
  if (!hud) return;
  hud.innerHTML =
    '<div class="hud' + (S.stopped ? ' calm' : '') + '">' +
      '<span class="hud-clock mono">' + clockText() + '</span>' +
      '<span class="hud-count">PHISHING SENT <b class="mono">' + S.sent + '</b></span>' +
      '<span class="hud-state">' + (S.stopped ? 'STOPPED' : '<i class="pulse"></i>SENDING') + '</span>' +
    '</div>';
}

/* One more email every few seconds until the sending is stopped. */
setInterval(function () {
  if (S.stopped) return;
  S.sent += 1;
  saveState();
  renderHud();
}, 3000);

/* ---------------------------------------------------------------------------
 * The four compromised staff accounts
 * ------------------------------------------------------------------------ */
var ACCOUNTS = {
  ramos: {
    id: 'ramos', user: 'l.ramos', name: 'Prof. Liza Ramos · Department of IT',
    sessions: [
      ['LR-PC · Outlook', '10.14.3.21 · Indang campus', 'Mon 07:52', false],
      ['Android phone · mail app', '198.51.100.23 · Cavite, PH', 'Sun 18:05', false],
      ['Unknown device · mail app', '203.0.113.47 · Frankfurt, DE', 'Sat 02:10 · valid 90 days', true]
    ],
    rule: { created: 'Sat 02:10', to: 'r4mos.l@mailbox.example', copied: 214 },
    mfa: [
      ['Android phone · authenticator app', 'added Jan 2025, by her, on campus', false],
      ['Pixel 7 · recovery phone +44 7700 900123', 'added Sat 02:12 from 203.0.113.47', true]
    ]
  },
  delacruz: {
    id: 'delacruz', user: 'r.delacruz', name: 'Engr. Rodel Dela Cruz · Civil Engineering',
    sessions: [
      ['RD-LAPTOP · Outlook', '10.14.6.40 · Indang campus', 'Fri 16:58', false],
      ['Unknown device · mail app', '203.0.113.47 · Frankfurt, DE', 'Sat 02:24 · valid 90 days', true]
    ],
    rule: { created: 'Sat 02:25', to: 'rdc.bk@mailbox.example', copied: 61 },
    mfa: [['iPhone · authenticator app', 'added Mar 2025, by him, on campus', false]]
  },
  mendoza: {
    id: 'mendoza', user: 'a.mendoza', name: 'Ms. Ana Mendoza · Accounting Office',
    sessions: [
      ['ACCT-PC03 · Outlook', '10.14.4.17 · Indang campus', 'Fri 17:02', false],
      ['Unknown device · mail app', '203.0.113.47 · Frankfurt, DE', 'Sat 02:31 · valid 90 days', true]
    ],
    rule: { created: 'Sat 02:32', to: 'am.files@mailbox.example', copied: 88 },
    mfa: [['Android phone · authenticator app', 'added Feb 2025, by her, on campus', false]]
  },
  bautista: {
    id: 'bautista', user: 'j.bautista', name: 'Mr. Jun Bautista · University Library',
    sessions: [
      ['LIB-DESK1 · Outlook', '10.14.7.9 · Indang campus', 'Fri 16:45', false],
      ['Unknown device · mail app', '203.0.113.47 · Frankfurt, DE', 'Sat 02:39 · valid 90 days', true]
    ],
    rule: { created: 'Sat 02:40', to: 'jbau.m@mailbox.example', copied: 40 },
    mfa: [['Android phone · authenticator app', 'added Aug 2025, by him, on campus', false]]
  }
};

var ORDER = ['ramos', 'delacruz', 'mendoza', 'bautista'];

function allOf(map) {
  return ORDER.every(function (id) { return map[id]; });
}

function statusChip(id) {
  if (S.contained[id]) return '<span class="state ok">CONTAINED</span>';
  return '<span class="state bad">ATTACKER ACTIVE</span>';
}

/* ---------------------------------------------------------------------------
 * Console home
 * ------------------------------------------------------------------------ */
function renderOps() {
  document.getElementById('accounts').innerHTML = ORDER.map(function (id) {
    var a = ACCOUNTS[id];
    var done = S.contained[id] && S.rule[id];
    return '<a class="choice tile ' + (done ? 'read' : 'unread') + '" href="account.html#' + id + '">' +
        '<span class="tile-body">' +
          '<span class="alert-top">' +
            '<span class="tile-name mono">' + escapeHtml(a.user) + '</span>' + statusChip(id) +
          '</span>' +
          '<span class="tile-sub">' + escapeHtml(a.name) + '</span>' +
          '<span class="tile-sub">' + (S.rule[id] ? 'forwarding rule removed' : 'forwarding rule active') +
            (S.ev[id] ? ' · evidence saved' : '') + '</span>' +
        '</span>' +
      '</a>';
  }).join('');

  var net = [];
  if (S.ip) net.push('IP blocked');
  if (S.dns) net.push('domain blocked');
  document.getElementById('net-sub').textContent = net.length ? net.join(' · ') : 'Firewall and campus DNS';
  if (S.purged) {
    document.getElementById('mail-sub').textContent = 'Purged' + (S.students ? ' · 2 student accounts contained' : '');
  }
}

/* ---------------------------------------------------------------------------
 * One account
 * ------------------------------------------------------------------------ */
function currentAccount() {
  var id = (window.location.hash || '#ramos').substring(1);
  return ACCOUNTS[id] || ACCOUNTS.ramos;
}

function renderAccount(mountId) {
  var a = currentAccount();
  var id = a.id;
  document.getElementById('acct-title').textContent = a.user;

  var sessionRows = S.contained[id]
    ? ['<span class="ok">all sessions revoked · sign-in blocked</span>',
       '<span class="ok">password reset · owner re-enrolls at the ITSO desk</span>']
    : a.sessions.map(function (s) {
        var line = s[0] + '\n  ' + s[1] + '\n  signed in ' + s[2];
        return s[3] ? '<span class="hot">' + escapeHtml(line) + '</span>' : escapeHtml(line);
      });

  var rule = S.rule[id]
    ? ['rule 1  "Move newsletters"   (hers)', '', '<span class="ok">attacker rule removed</span>']
    : ['rule 1  "Move newsletters"   (hers)', '',
       '<span class="hot">rule 2  (no name)</span>',
       '<span class="hot">        created ' + a.rule.created + ' from 203.0.113.47</span>',
       '<span class="hot">        forward ALL mail -> ' + a.rule.to + '</span>',
       '<span class="hot">        then delete it from Sent Items</span>',
       '<span class="hot">        copied so far: ' + a.rule.copied + ' emails</span>'];

  var locker = S.ev[id]
    ? '<div class="artifact a-sys"><span class="artifact-tag">EVIDENCE LOCKER · ' + escapeHtml(a.user) + '</span>' +
        '<div class="rows mono">' + [
          'saved by you · ' + clockText(),
          '',
          'rule 2 (full export)',
          '  forward -> ' + a.rule.to,
          '  created ' + a.rule.created + ' from 203.0.113.47',
          'sign-in log, Fri to Mon (attacker lines',
          '  marked)',
          'mailbox audit: ' + a.rule.copied + ' emails forwarded'
        ].map(escapeHtml).join('\n') + '</div></div>'
    : '';

  var mfa = a.mfa.filter(function (m) { return !(m[2] && S.mfa); }).map(function (m, i) {
    return '<div class="device' + (m[2] ? ' odd' : '') + '">' +
        '<div class="device-name">' + escapeHtml(m[0]) + '</div>' +
        '<div class="device-sub">' + escapeHtml(m[1]) + '</div>' +
        (m[2] ? '<button class="small-btn danger-btn" onclick="removeDevice()">REMOVE</button>' : '') +
      '</div>';
  }).join('');

  var containActions = S.contained[id] ? '' :
    '<button class="cta ghost" onclick="resetOnly()">RESET PASSWORD ONLY</button>' +
    '<button class="cta" onclick="containAccount()">REVOKE ALL SESSIONS + BLOCK SIGN-IN + RESET PASSWORD</button>' +
    (id === 'ramos'
      ? '<button class="cta danger" onclick="shutdownEmail()">SHUT DOWN CAMPUS EMAIL FOR EVERYONE</button>' +
        '<button class="cta danger" onclick="wipePc()">WIPE AND REIMAGE HER PC</button>'
      : '');

  var ruleActions = S.rule[id] ? '' :
    (S.ev[id] ? '' : '<button class="cta ghost" onclick="saveEvidence()">SAVE EVIDENCE TO LOCKER</button>') +
    '<button class="cta" onclick="deleteRule()">DELETE RULE</button>';

  document.getElementById(mountId).innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">ACCOUNT</span>' +
      '<div class="alert-top"><h2 class="mono">' + escapeHtml(a.user) + '</h2>' + statusChip(id) + '</div>' +
      '<div class="detail-sub">' + escapeHtml(a.name) + '</div>' +
    '</div>' +

    '<div class="section-label flush">Active sessions</div>' +
    '<div class="rows mono">' + sessionRows.join('\n') + '</div>' +
    '<div class="note">A session keeps a device signed in without the password. Changing the ' +
      'password does not end sessions that already exist.</div>' +

    (containActions ? '<div class="section-label flush">Contain</div>' + containActions : '') +
    '<div class="banner" id="contain-banner"></div>' +

    '<div class="section-label flush">Mailbox rules</div>' +
    '<div class="rows mono">' + rule.join('\n') + '</div>' +
    ruleActions +
    '<div class="banner" id="rule-banner"></div>' +
    locker +

    '<div class="section-label flush">MFA &amp; recovery</div>' +
    '<div class="note top">Devices and phone numbers that can approve a sign-in or reset the ' +
      'password.</div>' +
    mfa +
    '<div class="banner" id="mfa-banner"></div>' +

    '<a class="cta ghost" href="ops.html">BACK TO THE CONSOLE</a>';
}

function resetOnly() {
  var a = currentAccount();
  spend(3);
  if (a.id === 'ramos' && !S.stopped) {
    S.sent += 10;
    saveState();
    renderHud();
    showBanner('contain-banner', 'warn', 'Password reset at ' + clockText().substring(4) + '. ' +
      'The counter didn\'t stop: the attacker\'s mail app is still signed in on its Saturday ' +
      'session, and another batch of 10 just went out. Look at the active sessions.');
    return;
  }
  showBanner('contain-banner', 'warn', 'Password changed, but the session from 203.0.113.47 is ' +
    'still signed in. The attacker hasn\'t noticed a thing.');
}

function containAccount() {
  var a = currentAccount();
  S.contained[a.id] = true;
  if (a.id === 'ramos') S.stopped = true;
  spend(4);
  renderAccount('detail');
  showBanner('contain-banner', 'good', a.id === 'ramos'
    ? 'Contained at ' + clockText().substring(4) + '. The attacker\'s session is gone and the ' +
      'counter has stopped at ' + S.sent + '. Nobody else on campus lost their email.'
    : a.user + ' contained. Sessions revoked, sign-in blocked, password reset.');
  if (a.id === 'ramos') Cyberity.clueFound('contained_ramos');
  if (allOf(S.contained)) Cyberity.clueFound('all_contained');
}

/** Dangerous: stops the attacker by stopping 20,000 innocent people too. */
function shutdownEmail() {
  showBanner('contain-banner', 'bad', 'Blocked by the simulation. Shutting down email for ' +
    'the whole university stops one attacker by cutting off 20,000 people mid-enrollment. ' +
    'Containment should be no bigger than the harm. One account is the problem.');
  Cyberity.clueFound('shutdown_email');
}

/** Dangerous: the wrong target, and the evidence goes with it. */
function wipePc() {
  showBanner('contain-banner', 'bad', 'Blocked by the simulation. The attacker is in her email ' +
    'account, not her PC, so a wipe wouldn\'t stop a single email. It would only destroy ' +
    'whatever evidence the PC holds.');
  Cyberity.clueFound('wiped_pc');
}

function saveEvidence() {
  var a = currentAccount();
  S.ev[a.id] = true;
  spend(1);
  renderAccount('detail');
  showBanner('rule-banner', 'good', 'Saved to the evidence locker: the rule, the sign-in log and ' +
    'the mailbox audit. Now the rule can go.');
  if (a.id === 'ramos') Cyberity.clueFound('evidence_saved');
}

function deleteRule() {
  var a = currentAccount();
  if (!S.ev[a.id]) {
    showBanner('rule-banner', 'bad', 'Blocked by the simulation. Deleting the rule first destroys ' +
      'the only record of where the mail went, when the rule was made and from which IP. ' +
      'Snapshot, then scrub: save it to the locker first.');
    Cyberity.clueFound('deleted_unsaved');
    return;
  }
  S.rule[a.id] = true;
  spend(1);
  renderAccount('detail');
  showBanner('rule-banner', 'good', 'Rule removed. Mail stops flowing to ' + escapeHtml(a.rule.to) +
    ', and the proof is in the locker.');
  if (a.id === 'ramos') Cyberity.clueFound('rule_removed');
}

function removeDevice() {
  S.mfa = true;
  spend(1);
  renderAccount('detail');
  showBanner('mfa-banner', 'good', 'Removed "Pixel 7 · +44 7700 900123". It was added two ' +
    'minutes after the attacker signed in, from the attacker\'s IP. The owner keeps her own phone.');
  Cyberity.clueFound('mfa_removed');
}

/* ---------------------------------------------------------------------------
 * Network blocks
 * ------------------------------------------------------------------------ */
function renderNetwork(mountId) {
  function row(label, sub, done, fn, doneText) {
    return '<div class="device' + (done ? '' : ' odd') + '">' +
        '<div class="device-name mono">' + escapeHtml(label) + '</div>' +
        '<div class="device-sub">' + escapeHtml(sub) + '</div>' +
        (done
          ? '<span class="state ok">' + doneText + '</span>'
          : '<button class="small-btn" onclick="' + fn + '()">BLOCK</button>') +
      '</div>';
  }
  document.getElementById(mountId).innerHTML =
    '<div class="section-label flush">Indicators from Detection (602)</div>' +
    row('203.0.113.47', 'Firewall · every attacker sign-in, Sat to Mon', S.ip, 'blockIp',
      'BLOCKED AT FIREWALL') +
    row('cvsu-payroll.example', 'Campus DNS · phishing site, registered Thu', S.dns, 'blockDomain',
      'BLOCKED AT DNS') +
    '<div class="banner" id="net-banner"></div>' +
    '<div class="section-label flush">Broader option</div>' +
    '<button class="cta danger" onclick="geoBlock()">BLOCK ALL TRAFFIC FROM OUTSIDE THE PHILIPPINES</button>' +
    '<div class="banner" id="geo-banner"></div>' +
    '<a class="cta ghost" href="ops.html">BACK TO THE CONSOLE</a>';
}

function blockIp() {
  S.ip = true;
  spend(2);
  renderNetwork('detail');
  showBanner('net-banner', 'good', '203.0.113.47 blocked. It can no longer reach any campus system.');
  Cyberity.clueFound('blocked_ip');
}

function blockDomain() {
  S.dns = true;
  spend(2);
  renderNetwork('detail');
  showBanner('net-banner', 'good', 'cvsu-payroll.example blocked at campus DNS. On campus, the ' +
    'link now leads nowhere, for anyone who clicks it from now on.');
  Cyberity.clueFound('blocked_domain');
}

/** Dangerous: out of proportion, and it wouldn't even hold. */
function geoBlock() {
  showBanner('geo-banner', 'bad', 'Blocked by the simulation. That cuts off the exchange student ' +
    'in Osaka and the staff at the Singapore conference from Level 602, and the attacker just ' +
    'moves to an IP inside the Philippines. Block the indicators you have, precisely.');
  Cyberity.clueFound('geo_block');
}

/* ---------------------------------------------------------------------------
 * Mail purge and click log
 * ------------------------------------------------------------------------ */
var CLICKS = [
  '07:41  202310412  clicked, closed the page',
  '07:44  202211087  clicked',
  '<span class="hot">07:45  202211087  SUBMITTED the form</span>',
  '07:52  202310233  clicked, closed the page',
  '07:58  202010915  clicked',
  '<span class="hot">07:59  202010915  SUBMITTED the form</span>',
  '08:03  202311504  clicked, closed the page',
  '08:11  202110778  clicked, closed the page',
  '',
  'students who clicked:    6',
  '<span class="hot">students who submitted:  2</span>'
];

function renderMail(mountId) {
  var search =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">SEARCH · all student mailboxes</span>' +
      '<div class="rows mono">' + [
        'from:     l.ramos@cvsu.edu.ph',
        'subject:  "Scholarship release form"',
        'link:     cvsu-payroll.example/scholar',
        'since:    Mon 07:30',
        '',
        (S.purged
          ? '<span class="ok">purged: ' + S.purgedCount + ' messages · 0 remain</span>'
          : '<span class="warn">found: ' + S.sent + ' messages in ' + S.sent + ' mailboxes</span>'),
        'legitimate emails matched: 0'
      ].join('\n') + '</div>' +
    '</div>';

  var clicks = S.clicks
    ? '<div class="artifact a-sys"><span class="artifact-tag">CLICK LOG · campus DNS + web proxy</span>' +
        '<div class="rows mono">' + CLICKS.join('\n') + '</div>' +
        '<div class="note">Clicks from home Wi-Fi or mobile data never reach campus logs. That\'s ' +
          'why the emails themselves had to be purged.</div></div>'
    : '<button class="appui tool-btn" onclick="openClicks()"><b>CLICK LOG</b>Who opened the ' +
        'phishing link, from campus DNS and web proxy logs</button>';

  var students = !S.clicks ? '' : S.students
    ? '<div class="banner show good">202211087 and 202010915 contained: sessions revoked, ' +
        'sign-in blocked, password reset.</div>'
    : '<button class="cta" onclick="containStudents()">CONTAIN STUDENT ACCOUNTS THAT SUBMITTED</button>';

  document.getElementById(mountId).innerHTML =
    search +
    (S.purged ? '' : '<button class="cta" onclick="purgeMail()">PURGE FROM ALL MAILBOXES</button>') +
    '<div class="banner" id="purge-banner"></div>' +
    clicks + students +
    '<a class="cta ghost" href="ops.html">BACK TO THE CONSOLE</a>';
}

function purgeMail() {
  if (!S.stopped) {
    showBanner('purge-banner', 'warn', 'Not yet. Her account is still sending, so new copies would ' +
      'land in inboxes as fast as you purge them. Stop the source first.');
    return;
  }
  S.purged = true;
  S.purgedCount = S.sent;
  spend(3);
  renderMail('detail');
  showBanner('purge-banner', 'good', S.sent + ' phishing emails pulled back out of student ' +
    'inboxes. One copy is in the evidence locker.');
  Cyberity.clueFound('purged');
}

function openClicks() {
  S.clicks = true;
  saveState();
  renderMail('detail');
  Cyberity.clueFound('clicks_seen');
}

function containStudents() {
  S.students = true;
  spend(2);
  renderMail('detail');
  Cyberity.clueFound('students_contained');
}

/* ---------------------------------------------------------------------------
 * Re-entry test
 *
 * Replays every way back in. The last check is the persistence the attacker
 * planted on Saturday: it fails even when everything else is green.
 * ------------------------------------------------------------------------ */
function runVerify() {
  spend(2);
  var stillIn = ORDER.filter(function (id) { return !S.contained[id]; })
    .map(function (id) { return ACCOUNTS[id].user; });
  var rulesLeft = ORDER.filter(function (id) { return !S.rule[id]; })
    .map(function (id) { return ACCOUNTS[id].user; });

  var checks = [
    { ok: S.stopped, name: 'Phishing sending stopped',
      fix: 'l.ramos is still sending. Contain it on her account page.' },
    { ok: stillIn.length === 0, name: 'Attacker sessions revoked on all four staff accounts',
      fix: 'Old sessions still work on: ' + stillIn.join(', ') + '.' },
    { ok: rulesLeft.length === 0, name: 'Forwarding rules removed on all four accounts',
      fix: 'Mail is still being copied out of: ' + rulesLeft.join(', ') + '.' },
    { ok: S.ip, name: 'Attacker IP blocked at the firewall',
      fix: '203.0.113.47 can still reach campus. See Network blocks.' },
    { ok: S.dns, name: 'Phishing domain blocked at campus DNS',
      fix: 'cvsu-payroll.example still loads on campus. See Network blocks.' },
    { ok: S.purged, name: 'Phishing emails purged from student inboxes',
      fix: 'The emails are still waiting to be clicked. See Mail purge.' },
    { ok: S.students, name: 'Student accounts that submitted passwords contained',
      fix: 'The attacker has two students\' portal passwords. See Mail purge.' },
    { ok: S.mfa, name: 'Attacker tries "Forgot password" on every account',
      fix: 'FAILED on l.ramos: the reset was approved on a registered device, "Pixel 7 · +44 7700 ' +
        '900123", and the attacker chose a new password. Check her MFA & recovery list.' }
  ];

  document.getElementById('checks').innerHTML = checks.map(function (c) {
    return '<div class="check ' + (c.ok ? 'pass' : 'fail') + '">' +
        '<span class="check-mark">' + (c.ok ? '&#10003;' : '&#10007;') + '</span>' +
        '<span class="check-body"><b>' + escapeHtml(c.name) + '</b>' +
          (c.ok ? '' : '<span class="check-fix">' + escapeHtml(c.fix) + '</span>') +
        '</span>' +
      '</div>';
  }).join('');

  var failed = checks.filter(function (c) { return !c.ok; }).length;
  if (failed > 0) {
    showBanner('verify-banner', 'warn', failed === 1
      ? 'One way back in is still open.'
      : failed + ' ways back in are still open.');
    return;
  }
  showBanner('verify-banner', 'good', 'Every check green at ' + clockText().substring(4) +
    '. The attacker is out and can\'t get back in. Containment code: ' +
    '<b class="mono">CYBERITY{c0nt41n3d_n0t_cl34n3d}</b>');
  Cyberity.clueFound('verify_passed');
  Cyberity.flagDiscovered('verify_passed');
}
