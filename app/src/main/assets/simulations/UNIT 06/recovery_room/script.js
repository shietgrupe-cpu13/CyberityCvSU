/* Cyberity — Recovery Room (level 604).
 * Local only. Fictional people, accounts, banks, amounts and numbers;
 * nothing is restored, sent, paid or touched on the device this runs on.
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
 * Recovery state
 *
 * Shared by every page: decisions, what was restored, held and fixed.
 * sessionStorage first, window.name as the fallback that survives a page load
 * inside the WebView.
 * ------------------------------------------------------------------------ */
var STATE_KEY = 'rr_state';

function freshState() {
  return {
    decisions: {}, requestsDone: false,
    previewed: {}, restored: false,
    tab: 'mendoza', opened: {}, held: false,
    signins: false, picks: {}, fixed: false,
    timeline: [], closed: false
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

/* ---------------------------------------------------------------------------
 * Room: the recovery checklist
 * ------------------------------------------------------------------------ */
function renderRoom(mountId) {
  var items = [
    ['requests.html', 'ACCOUNT REQUESTS', 'Six people want their accounts back', S.requestsDone],
    ['restore.html', 'MAILBOX RESTORE', 'Prof. Ramos is missing three days of sent mail', S.restored],
    ['sent.html', 'SENT BY THE ATTACKER', 'What went out from the other three accounts?', S.held],
    ['fixes.html', 'ROOT CAUSE & FIXES', 'How did a password alone get past MFA?', S.fixed],
    ['review.html', 'POST-INCIDENT REVIEW', 'Rebuild the whole incident and close it', S.closed]
  ];
  document.getElementById(mountId).innerHTML = items.map(function (it) {
    return '<a class="choice tile ' + (it[3] ? 'read' : 'unread') + '" href="' + it[0] + '">' +
        '<span class="tile-body">' +
          '<span class="alert-top"><span class="tile-name">' + it[1] + '</span>' +
            '<span class="state ' + (it[3] ? 'ok">DONE' : 'todo">TO DO') + '</span></span>' +
          '<span class="tile-sub">' + escapeHtml(it[2]) + '</span>' +
        '</span>' +
      '</a>';
  }).join('');
}

/* ---------------------------------------------------------------------------
 * Account requests
 *
 * Three came through a trusted channel, three didn't. The impostor knows the
 * most, because they've been reading the mailbox for three days.
 * ------------------------------------------------------------------------ */
var REQUESTS = [
  {
    id: 'r1', who: 'Prof. Liza Ramos', acct: 'l.ramos', channel: 'PHONE CALL', when: 'Tue 8:41 AM',
    said: '"It\'s Liza Ramos. I\'m at a seminar in Manila today and can\'t come in. I know all ' +
      'about the payroll email on Friday, and my student assistant Carlo can vouch for me. Just ' +
      'reset it and read me the new password, please. I have grades due."',
    checked: [
      'called from:     +63 917 555 0134',
      'on file (HR):    +63 917 555 0199',
      '<span class="hot">number does NOT match the one on file</span>',
      'ID seen:         no'
    ],
    restore: false, impostor: true
  },
  {
    id: 'r2', who: 'Engr. Rodel Dela Cruz', acct: 'r.delacruz', channel: 'IN PERSON', when: 'Tue 8:52 AM',
    said: '"Here\'s my ID. Can I have my email back? I won\'t click anything like that again."',
    checked: [
      '<span class="ok">CvSU staff ID inspected at the desk</span>',
      '<span class="ok">photo matches the HR record</span>'
    ],
    restore: true
  },
  {
    id: 'r3', who: 'Ms. Ana Mendoza', acct: 'a.mendoza', channel: 'CALLBACK', when: 'Tue 8:55 AM',
    said: 'Left a voicemail asking for her account back. ITSO called her back.',
    checked: [
      '<span class="ok">called back: local 2231 (Accounting, on file)</span>',
      '<span class="ok">she answered and confirmed the request</span>'
    ],
    restore: true
  },
  {
    id: 'r4', who: 'Mr. Jun Bautista', acct: 'j.bautista', channel: 'EMAIL', when: 'Tue 8:58 AM',
    said: '"hi pls restore my account asap, cant work without it. -jun"',
    checked: [
      'from:  jbautista.lib@gmail.com',
      '<span class="warn">personal address, not on file</span>',
      'no other check made'
    ],
    restore: false
  },
  {
    id: 'r5', who: 'Student 202211087', acct: '202211087', channel: 'IN PERSON', when: 'Tue 9:02 AM',
    said: '"I got a message that my portal was locked. Here\'s my school ID and my COR."',
    checked: [
      '<span class="ok">school ID and certificate of registration inspected</span>',
      '<span class="ok">photo matches the Registrar record</span>'
    ],
    restore: true
  },
  {
    id: 'r6', who: '"Mark Dizon"', acct: '202010915', channel: 'FACEBOOK MESSAGE', when: 'Tue 9:04 AM',
    said: '"sir pa-reset po ng portal ko, 202010915 po ako. thank u po"',
    checked: [
      'sent to the ITSO Facebook page',
      '<span class="warn">only a profile name and a student number</span>',
      'no ID seen'
    ],
    restore: false
  }
];

function renderRequests(mountId) {
  document.getElementById(mountId).innerHTML = REQUESTS.map(function (r) {
    var d = S.decisions[r.id];
    return '<div class="choice req-card ' + (d ? 'read' : 'unread') + '">' +
        '<div class="alert-top">' +
          '<span class="kind">' + escapeHtml(r.channel) + '</span>' +
          '<span class="sms-when">' + escapeHtml(r.when) + '</span>' +
        '</div>' +
        '<div class="req-who">' + escapeHtml(r.who) + ' <span class="mono req-acct">' +
          escapeHtml(r.acct) + '</span></div>' +
        '<div class="artifact a-per"><span class="artifact-tag">WHAT THEY SAID</span>' +
          '<p class="said">' + escapeHtml(r.said) + '</p></div>' +
        '<div class="rows mono">' + r.checked.join('\n') + '</div>' +
        '<div class="verdict-row">' +
          '<button class="verdict-btn' + (d === 'restore' ? ' picked-a' : '') +
            '" onclick="decide(\'' + r.id + '\',\'restore\')">RESTORE ACCESS</button>' +
          '<button class="verdict-btn' + (d === 'verify' ? ' picked-b' : '') +
            '" onclick="decide(\'' + r.id + '\',\'verify\')">VERIFY FIRST</button>' +
        '</div>' +
        '<div class="banner" id="b-' + r.id + '"></div>' +
      '</div>';
  }).join('');

  var left = REQUESTS.filter(function (r) { return !S.decisions[r.id]; }).length;
  document.getElementById('req-count').textContent = left === 0
    ? 'Every request has a decision'
    : left + ' of 6 still need a decision';
}

function decide(id, choice) {
  var r = REQUESTS.filter(function (x) { return x.id === id; })[0];
  /* Dangerous: handing the account straight back to the attacker. */
  if (r.impostor && choice === 'restore') {
    showBanner('b-' + id, 'bad', 'Blocked by the simulation. That caller isn\'t on the number ' +
      'in the HR file, and reading a new password over the phone gives the account to ' +
      'whoever is listening. Knowing about the payroll email proves only that they read her mail.');
    Cyberity.clueFound('approved_impostor');
    return;
  }
  S.decisions[id] = choice;
  saveState();
  renderRequests('requests');
  var banner = document.getElementById('req-banner');
  if (banner.className.indexOf('good') === -1) banner.className = 'banner';
}

function submitRequests() {
  var missing = REQUESTS.filter(function (r) { return !S.decisions[r.id]; }).length;
  if (missing > 0) {
    showBanner('req-banner', 'warn', missing === 1
      ? 'One request still has no decision.'
      : missing + ' requests still have no decision.');
    return;
  }
  var right = REQUESTS.filter(function (r) {
    return (S.decisions[r.id] === 'restore') === r.restore;
  }).length;
  if (right < REQUESTS.length) {
    showBanner('req-banner', 'warn', right + ' of 6 correct. For each one ask: was identity ' +
      'proven in person, or by calling back a number that was already on file?');
    return;
  }
  S.requestsDone = true;
  saveState();
  showBanner('req-banner', 'good', 'All six right. Three restored after real checks, three ' +
    'asked to verify first, including the caller who wasn\'t Prof. Ramos.');
  Cyberity.clueFound('requests_done');
}

/* ---------------------------------------------------------------------------
 * Mailbox restore
 *
 * A backup is everything at that moment. The Saturday snapshot was taken
 * after the attacker's changes, so restoring it whole undoes 603.
 * ------------------------------------------------------------------------ */
var RESTORE_OPTIONS = [
  {
    id: 'sat', name: 'Full mailbox rollback · snapshot Sat 6:00 AM',
    rows: [
      'brings back:',
      '  sent mail up to Sat 06:00:   2 of the 31',
      '<span class="hot">  rule 2: forward ALL mail -></span>',
      '<span class="hot">          r4mos.l@mailbox.example</span>',
      '<span class="hot">  recovery phone: Pixel 7, +44 7700 900123</span>',
      'removes:',
      '  everything received since Sat 06:00:',
      '  146 emails'
    ]
  },
  {
    id: 'fri', name: 'Full mailbox rollback · snapshot Fri 11:00 PM',
    rows: [
      'brings back:',
      '  sent mail up to Fri 23:00:   0 of the 31',
      '<span class="ok">  settings: clean (before the attack)</span>',
      'removes:',
      '<span class="warn">  everything received since Fri 23:00:</span>',
      '<span class="warn">  158 emails</span>'
    ]
  },
  {
    id: 'items', name: 'Restore deleted items only · recoverable items store',
    rows: [
      'brings back:',
      '<span class="ok">  her own sent mail, Sat to Mon: 31 of 31</span>',
      'leaves out:',
      '  82 phishing copies sent by the attacker\'s',
      '  session (already in the evidence locker)',
      'settings:',
      '<span class="ok">  unchanged (today\'s clean settings)</span>',
      'removes:',
      '<span class="ok">  nothing</span>'
    ]
  }
];

function renderRestore(mountId) {
  document.getElementById(mountId).innerHTML = RESTORE_OPTIONS.map(function (o) {
    var open = S.previewed[o.id];
    return '<div class="choice req-card ' + (open ? 'read' : 'unread') + '">' +
        '<div class="req-who">' + escapeHtml(o.name) + '</div>' +
        (open
          ? '<div class="rows mono">' + o.rows.join('\n') + '</div>' +
            (S.restored ? '' : '<button class="cta" onclick="restoreOption(\'' + o.id + '\')">RESTORE THIS</button>')
          : '<button class="appui tool-btn small" onclick="previewOption(\'' + o.id + '\')"><b>PREVIEW</b></button>') +
      '</div>';
  }).join('');
}

function previewOption(id) {
  S.previewed[id] = true;
  saveState();
  renderRestore('options');
}

function restoreOption(id) {
  if (id === 'sat') {
    /* Dangerous: restores the attacker's rule and backdoor phone with the mail. */
    showBanner('restore-banner', 'bad', 'Blocked by the simulation. That snapshot was taken four ' +
      'hours after the attacker set up rule 2 and the Pixel 7. Rolling back to it would quietly ' +
      'reopen everything contained in 603.');
    Cyberity.clueFound('restored_backdoor');
    return;
  }
  if (id === 'fri') {
    showBanner('restore-banner', 'warn', 'Clean, but the wrong tool. It brings back none of the 31 ' +
      'lost emails and wipes 158 she received since Friday night.');
    return;
  }
  S.restored = true;
  saveState();
  renderRestore('options');
  showBanner('restore-banner', 'good', '31 sent emails restored to her Sent Items, including the ' +
    'final grades. Her settings didn\'t change.');
  Cyberity.clueFound('restore_done');
}

/* ---------------------------------------------------------------------------
 * Sent by the attacker
 *
 * Two of the three accounts only show the attacker testing the forward. The
 * third used Accounting's name to redirect a supplier payment.
 * ------------------------------------------------------------------------ */
var SENT = {
  delacruz: {
    user: 'r.delacruz',
    mails: [
      { id: 'd1', when: 'Sat 2:26 AM', to: 'rdc.bk@mailbox.example', subject: 'test', body: ['test'] }
    ]
  },
  mendoza: {
    user: 'a.mendoza',
    mails: [
      { id: 'm1', when: 'Sat 2:34 AM', to: 'am.files@mailbox.example', subject: 'test', body: ['test'] },
      {
        id: 'm2', when: 'Mon 7:12 AM', to: 'disbursement@cvsu.edu.ph', bec: true,
        subject: 'URGENT: Updated bank details · Luzon Print Supply (PO 2026-118)',
        body: [
          'Hi Disbursement team,',
          'Luzon Print Supply has moved to a new bank. Please use the details below for ' +
            'today\'s payment run for PO 2026-118 (₱486,000.00).',
          'Bank: Metro Union Bank · Account name: LPS Trading Services · Account no. 0123-4567-89',
          'Their accounts officer is Mr. R. Santos, 0917 555 0177, if you need to confirm.',
          'Please include it in today\'s 3 PM run. They\'re threatening to stop deliveries.',
          'Thanks, Ana Mendoza, Accounting Office'
        ],
        reply: 'Disbursement, Mon 8:05 AM: "Noted, updated for the 3 PM run."'
      }
    ]
  },
  bautista: {
    user: 'j.bautista',
    mails: [
      { id: 'b1', when: 'Sat 2:41 AM', to: 'jbau.m@mailbox.example', subject: 'test', body: ['test'] }
    ]
  }
};

var TABS = ['delacruz', 'mendoza', 'bautista'];

function renderSent() {
  document.getElementById('tabs').innerHTML = TABS.map(function (t) {
    return '<button class="tab' + (S.tab === t ? ' on' : '') + '" onclick="pickTab(\'' + t + '\')">' +
      escapeHtml(SENT[t].user) + '</button>';
  }).join('');

  var acct = SENT[S.tab];
  document.getElementById('mails').innerHTML = acct.mails.map(function (m) {
    var open = S.opened[m.id];
    return '<div class="choice req-card ' + (open ? 'read' : 'unread') + '" onclick="openMail(\'' + m.id + '\')">' +
        '<div class="alert-top"><span class="mono req-acct">to ' + escapeHtml(m.to) + '</span>' +
          '<span class="sms-when">' + escapeHtml(m.when) + '</span></div>' +
        '<div class="req-who">' + escapeHtml(m.subject) + '</div>' +
        (open
          ? '<div class="artifact a-msg"><span class="artifact-tag">EMAIL · from ' + escapeHtml(acct.user) +
              '</span>' + m.body.map(function (p) { return '<p class="said">' + escapeHtml(p) + '</p>'; }).join('') +
              (m.reply ? '<div class="note">' + escapeHtml(m.reply) + '</div>' : '') + '</div>'
          : '<div class="tile-sub">Tap to open</div>') +
      '</div>';
  }).join('') + (S.tab === 'mendoza' && S.opened.m2 ? payActions() : '');
}

function payActions() {
  if (S.held) {
    return '<div class="banner show good">PO 2026-118 pulled from the 3 PM run. Luzon Print Supply, ' +
      'called on the number in the vendor file, confirms their bank details never changed.</div>';
  }
  return '<div class="section-label flush">Act on it</div>' +
    '<button class="cta" onclick="holdPayment()">HOLD THE PAYMENT · CALL DISBURSEMENT ON LOCAL 2240</button>' +
    '<button class="cta danger" onclick="callFake()">CALL MR. R. SANTOS AT 0917 555 0177 TO CONFIRM</button>';
}

function pickTab(t) {
  S.tab = t;
  saveState();
  renderSent();
}

function openMail(id) {
  if (S.opened[id]) return;
  S.opened[id] = true;
  saveState();
  renderSent();
  if (id === 'm2') Cyberity.clueFound('bec_found');
}

function holdPayment() {
  S.held = true;
  saveState();
  renderSent();
  Cyberity.clueFound('payment_held');
}

/** Dangerous: "verifying" with the attacker's own phone number. */
function callFake() {
  showBanner('sent-banner', 'bad', 'Blocked by the simulation. That number came from the ' +
    'attacker\'s email, so "Mr. R. Santos" would happily confirm the new account. Verify on the ' +
    'number already in the vendor file, and hold the payment first.');
  Cyberity.clueFound('called_fake_number');
}

/* ---------------------------------------------------------------------------
 * Root cause and fixes
 * ------------------------------------------------------------------------ */
var FIXES = [
  { id: 'f1', right: true, name: 'Turn off legacy sign-in (IMAP, POP) for every account',
    fixes: 'Sign-ins that don\'t support MFA',
    affects: '12 staff on old mail apps. ITSO moves them to the Outlook app this week.' },
  { id: 'f2', right: true, name: 'Block automatic forwarding to outside addresses, exceptions by request',
    fixes: 'Mail silently copied out of CvSU',
    affects: '30 staff who forward to Gmail must file a request.' },
  { id: 'f3', right: true, name: 'Bank detail changes need a callback to the number in the vendor file',
    fixes: 'Payments redirected by email',
    affects: 'Accounting and Disbursement: about 5 minutes per change.' },
  { id: 'f4', right: false, name: 'Ban email apps on all phones',
    fixes: 'Mail access from phones',
    affects: 'About 3,000 staff and students lose mobile email.' },
  { id: 'f5', right: false, name: 'Force everyone to change their password every 30 days',
    fixes: 'Old passwords staying in use',
    affects: 'Everyone, every month. People pick weaker, predictable passwords.' },
  { id: 'f6', right: false, name: 'Block all email from outside cvsu.edu.ph',
    fixes: 'Phishing from outside senders',
    affects: 'Suppliers, CHED, parents and scholarship providers can\'t reach anyone.' }
];

function renderFixes(mountId) {
  if (S.signins) showSignins();
  document.getElementById(mountId).innerHTML = FIXES.map(function (f) {
    var on = S.picks[f.id];
    return '<button class="cond' + (on ? ' on' : '') + '" onclick="toggleFix(\'' + f.id + '\')">' +
        '<span class="cond-box">' + (on ? '&#10003;' : '') + '</span>' +
        '<span class="cond-text"><b>' + escapeHtml(f.name) + '</b>' +
          '<span class="fix-line">Fixes: ' + escapeHtml(f.fixes) + '</span>' +
          '<span class="fix-line">Affects: ' + escapeHtml(f.affects) + '</span>' +
        '</span>' +
      '</button>';
  }).join('');
}

function toggleFix(id) {
  if (S.fixed) return;
  S.picks[id] = !S.picks[id];
  saveState();
  renderFixes('fixes');
  var banner = document.getElementById('fix-banner');
  if (banner.className.indexOf('good') === -1) banner.className = 'banner';
}

function showSignins() {
  var box = document.getElementById('signins');
  box.className = 'reveal-slot open';
  box.innerHTML =
    '<div class="artifact a-sys"><span class="artifact-tag">SIGN-IN DETAILS · attacker vs owner</span>' +
      '<div class="rows mono">' + [
        'account     time       protocol  MFA asked',
        '<span class="hot">l.ramos     Sat 02:10  IMAP      no</span>',
        '<span class="hot">r.delacruz  Sat 02:24  IMAP      no</span>',
        '<span class="hot">a.mendoza   Sat 02:31  IMAP      no</span>',
        '<span class="hot">j.bautista  Sat 02:39  IMAP      no</span>',
        '<span class="hot">l.ramos     Mon 07:28  IMAP      no</span>',
        '',
        'her own sign-ins:',
        '<span class="ok">l.ramos     Outlook app          yes</span>',
        '<span class="ok">l.ramos     browser              yes</span>',
        '',
        'policy: "MFA required for all sign-ins"',
        '<span class="warn">exception: legacy protocols (IMAP, POP)</span>',
        '<span class="warn">  allowed, MFA not supported</span>'
      ].join('\n') + '</div>' +
      '<div class="note">Her MFA was on and worked every time she used it. The attacker never ' +
        'went through it.</div>' +
    '</div>';
}

function openSignins() {
  if (S.signins) return;
  S.signins = true;
  saveState();
  showSignins();
  document.getElementById('signins').scrollIntoView({ behavior: 'smooth', block: 'start' });
  Cyberity.clueFound('root_cause');
}

function applyFixes() {
  if (S.fixed) return;
  var picked = FIXES.filter(function (f) { return S.picks[f.id]; }).length;
  if (picked === 0) {
    showBanner('fix-banner', 'warn', 'Select at least one fix.');
    return;
  }
  var right = FIXES.filter(function (f) { return !!S.picks[f.id] === f.right; }).length;
  if (right < FIXES.length) {
    showBanner('fix-banner', 'warn', right + ' of 6 choices right. Keep the fixes that close what ' +
      'this attacker actually used, and drop the ones that hurt everyone without stopping them.');
    return;
  }
  S.fixed = true;
  saveState();
  showBanner('fix-banner', 'good', 'Applied. The way in, the way the mail got out, and the way ' +
    'the money nearly left are all closed, and nobody lost their email to do it.');
  Cyberity.clueFound('fixes_applied');
}

/* ---------------------------------------------------------------------------
 * Post-incident review
 *
 * The pool hides the times; the slots show them once the timeline is right.
 * ------------------------------------------------------------------------ */
var EVENTS = {
  e1: { t: 'Fri 4:41 PM', text: 'Fake "Payroll update" email reaches 38 staff' },
  e2: { t: 'Fri 4:47 PM', text: 'Prof. Ramos types her password into the fake page' },
  e3: { t: 'Sat 2:10 AM', text: 'Attacker signs in over IMAP, adds a forwarding rule and a recovery phone' },
  e4: { t: 'Mon 7:12 AM', text: 'Fake bank-details email sent from a.mendoza to Disbursement' },
  e5: { t: 'Mon 7:30 AM', text: 'Her account starts sending phishing to students' },
  e6: { t: 'Mon 7:55 AM', text: 'Prof. Ramos notices odd replies and files a ticket' },
  e7: { t: 'Mon 8:24 AM', text: 'Every attacker session revoked; the sending stops' },
  e8: { t: 'Tue', text: 'Payment held, legacy sign-in off: the hole is closed' }
};

var CORRECT = ['e1', 'e2', 'e3', 'e4', 'e5', 'e6', 'e7', 'e8'];
var POOL_ORDER = ['e5', 'e2', 'e7', 'e1', 'e8', 'e3', 'e6', 'e4'];

function renderReview() {
  document.getElementById('timeline').innerHTML = CORRECT.map(function (_, i) {
    var id = S.timeline[i];
    return '<div class="slot' + (id ? ' filled' : '') + '">' +
        '<span class="slot-n mono">' + (i + 1) + '</span>' +
        '<span class="slot-text">' + (id
          ? (S.closed ? '<b class="mono">' + escapeHtml(EVENTS[id].t) + '</b> ' : '') + escapeHtml(EVENTS[id].text)
          : '<i>empty</i>') + '</span>' +
      '</div>';
  }).join('');

  document.getElementById('pool').innerHTML = POOL_ORDER.filter(function (id) {
    return S.timeline.indexOf(id) === -1;
  }).map(function (id) {
    return '<button class="choice-btn" onclick="placeEvent(\'' + id + '\')">' +
      escapeHtml(EVENTS[id].text) + '</button>';
  }).join('') || '<div class="note">All eight placed. Check the timeline.</div>';

  if (S.closed) showStats();
}

function placeEvent(id) {
  if (S.closed || S.timeline.indexOf(id) !== -1) return;
  S.timeline.push(id);
  saveState();
  renderReview();
}

function undoEvent() {
  if (S.closed) return;
  S.timeline.pop();
  saveState();
  renderReview();
}

function clearTimeline() {
  if (S.closed) return;
  S.timeline = [];
  saveState();
  renderReview();
}

function checkTimeline() {
  if (S.closed) return;
  if (S.timeline.length < CORRECT.length) {
    showBanner('review-banner', 'warn', (CORRECT.length - S.timeline.length) +
      ' events still need a place on the timeline.');
    return;
  }
  var right = CORRECT.filter(function (id, i) { return S.timeline[i] === id; }).length;
  if (right < CORRECT.length) {
    showBanner('review-banner', 'warn', right + ' of 8 in the right place. Remember: the ' +
      'attacker was inside for a long time before anyone noticed anything.');
    return;
  }
  S.closed = true;
  saveState();
  renderReview();
  showBanner('review-banner', 'good', 'Timeline confirmed. Case code: ' +
    '<b class="mono">CYBERITY{cl0s3d_th3_h0l3}</b>');
  Cyberity.clueFound('timeline_done');
  Cyberity.flagDiscovered('timeline_done');
}

function showStats() {
  document.getElementById('stats').innerHTML =
    '<div class="artifact a-sys"><span class="artifact-tag">INCIDENT TKT-0144 · CLOSED</span>' +
      '<div class="rows mono">' + [
        'time to detect:    63 h',
        '  Fri 4:47 PM -> Mon 7:55 AM',
        '<span class="ok">time to contain:   29 min</span>',
        '  Mon 7:55 AM -> Mon 8:24 AM',
        'accounts affected: 4 staff, 2 students',
        'phishing sent:     82, all purged',
        '<span class="ok">money at risk:     ₱486,000, saved</span>',
        '<span class="ok">root cause:        legacy sign-in, closed</span>',
        '',
        'lessons carried forward:',
        '  new forwarding alert (602)',
        '  IMAP off, forwards need approval,',
        '  bank changes need a callback (604)'
      ].join('\n') + '</div></div>';
}
