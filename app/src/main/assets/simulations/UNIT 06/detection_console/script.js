/* Cyberity — Detection Console (level 602).
 * Local only. Fictional accounts, hosts and addresses (documentation IP
 * ranges); nothing is searched, sent or touched on the device this runs on.
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

/* Small per-page memory: verdicts, the last search, whether the rule was
   saved. A convenience only, so a refused sessionStorage just starts fresh. */
var Memo = {
  get: function (key, fallback) {
    try {
      var raw = window.sessionStorage.getItem('dc_' + key);
      return raw === null ? fallback : JSON.parse(raw);
    } catch (e) { return fallback; }
  },
  set: function (key, value) {
    try { window.sessionStorage.setItem('dc_' + key, JSON.stringify(value)); } catch (e) { /* ignored */ }
  }
};

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function showBanner(id, kind, text) {
  var banner = document.getElementById(id);
  banner.className = 'banner show ' + kind;
  banner.textContent = text;
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/* ---------------------------------------------------------------------------
 * Console home
 * ------------------------------------------------------------------------ */
function renderConsole() {
  if (Memo.get('saved', false)) {
    var ttd = document.getElementById('ttd');
    if (ttd) { ttd.textContent = '~1 min'; ttd.className = 'stat-num mono good-text'; }
  }
}

/* ---------------------------------------------------------------------------
 * Alert queue
 *
 * Ten alerts, three real. Both HIGH alerts are routine and the attack itself
 * arrives as a LOW, so severity alone sorts them the wrong way round.
 * ------------------------------------------------------------------------ */
var ALERTS = [
  {
    id: 'a1', sev: 'MED', when: 'Fri 4:52 PM', real: true,
    title: 'Newly registered domain visited from 38 staff PCs',
    rows: [
      'domain:      cvsu-payroll.example',
      'registered:  Thu 23:40 (two days old)',
      'visited by:  38 staff PCs, Fri 16:41 to 17:30',
      '',
      'every visit followed an email titled',
      '"Payroll update: confirm your account"'
    ]
  },
  {
    id: 'a2', sev: 'LOW', when: 'Sat 2:10 AM', real: true,
    title: 'Sign-in from a new country: l.ramos',
    rows: [
      'account:   l.ramos',
      'from:      203.0.113.47 · Frankfurt, DE',
      'device:    unknown (mail app)',
      'countries in last 90 days: PH only',
      '',
      'similar alerts suppressed: 3',
      '  (same IP, other accounts, within 30 min)'
    ]
  },
  {
    id: 'a3', sev: 'MED', when: 'Sat 3:00 AM', real: false,
    title: 'Brute force: 40 failed sign-ins on svc-printer',
    rows: [
      'account:  svc-printer (service account)',
      'from:     10.14.2.20 · print server, on campus',
      'pattern:  1 attempt every 3 minutes, since',
      '          Fri 23:00',
      '',
      'Fri 22:55  ITSO rotated service passwords',
      '           (CHG-0918). Print server still has',
      '           the old one saved.'
    ]
  },
  {
    id: 'a4', sev: 'LOW', when: 'Sat 10:15 AM', real: false,
    title: 'New device sign-in: m.villanueva',
    rows: [
      'account:  m.villanueva',
      'from:     198.51.100.61 · Cavite, PH',
      'device:   new Android phone',
      '',
      'help desk TKT-0138, Sat 10:02:',
      '  "Got a new phone, setting up my email,',
      '   the app asks for MFA, is this right?"',
      '  MFA approved on her old phone'
    ]
  },
  {
    id: 'a5', sev: 'HIGH', when: 'Sat 2:00 PM', real: false,
    title: 'Antivirus disabled on LAB12',
    rows: [
      'host:      LAB12 · Computer Laboratory 2',
      'by:        itso.tech2 (lab technician)',
      'disabled:  Sat 14:00',
      're-enabled: Sat 14:22',
      '',
      'CHG-0921: reimage LAB12 with the new',
      'semester image, Sat 13:30 to 15:00'
    ]
  },
  {
    id: 'a6', sev: 'HIGH', when: 'Sun 1:00 AM', real: false,
    title: 'Large transfer: 40 GB to an external address',
    rows: [
      'host:   backup01 · ITSO server room',
      'to:     offsite backup provider (contracted)',
      'size:   40.2 GB',
      '',
      'same transfer, every night at 01:00,',
      'last 180 nights: 38 to 44 GB'
    ]
  },
  {
    id: 'a7', sev: 'LOW', when: 'Sun 9:40 AM', real: false,
    title: '6 failed sign-ins: k.reyes',
    rows: [
      'account:  k.reyes',
      'from:     10.14.5.12 · Registrar office PC',
      '09:34 to 09:39  failed x6',
      '09:41           success, same PC',
      '',
      'password changed Fri 17:05 (expiry)'
    ]
  },
  {
    id: 'a8', sev: 'LOW', when: 'Sun 9:30 PM', real: false,
    title: 'Student portal sign-in from Japan: 202310577',
    rows: [
      'account:  202310577 (student)',
      'from:     192.0.2.88 · Osaka, JP',
      'device:   same laptop as last 60 days',
      'signs in from Osaka almost daily',
      '',
      'Registrar: exchange student, Osaka,',
      'Aug to Dec, approved'
    ]
  },
  {
    id: 'a9', sev: 'MED', when: 'Mon 6:00 AM', real: false,
    title: 'Admin tool run on 30 PCs at once',
    rows: [
      'tool:   PsExec (remote admin)',
      'by:     itso.admin',
      'on:     LAB2-01 to LAB2-30',
      'ran:    office suite update installer',
      '',
      'CHG-0923: scheduled software update,',
      'Mon 06:00, announced Thu'
    ]
  },
  {
    id: 'a10', sev: 'MED', when: 'Mon 7:54 AM', real: true,
    title: 'Mass outgoing email: l.ramos',
    rows: [
      'account:  l.ramos',
      'sent:     50 emails in 24 minutes',
      'subject:  "Scholarship release form"',
      'to:       students only',
      'link:     cvsu-payroll.example/scholar',
      '',
      'her normal: about 12 emails a day'
    ]
  }
];

function renderAlerts(mountId) {
  var verdicts = Memo.get('verdicts', {});
  document.getElementById(mountId).innerHTML = ALERTS.map(function (a) {
    var v = verdicts[a.id];
    return '<div class="choice alert-card ' + (v ? 'read' : 'unread') + '">' +
        '<div class="alert-top">' +
          '<span class="sev sev-' + a.sev.toLowerCase() + '">' + a.sev + '</span>' +
          '<span class="sms-when">' + escapeHtml(a.when) + '</span>' +
        '</div>' +
        '<div class="alert-title">' + escapeHtml(a.title) + '</div>' +
        '<button class="appui tool-btn small" onclick="toggleDetails(\'' + a.id + '\')">' +
          '<b>DETAILS</b></button>' +
        '<div class="reveal-slot" id="det-' + a.id + '">' +
          '<div class="rows mono">' + a.rows.map(escapeHtml).join('\n') + '</div>' +
        '</div>' +
        '<div class="verdict-row">' +
          '<button class="verdict-btn' + (v === 'real' ? ' picked-real' : '') +
            '" onclick="setVerdict(\'' + a.id + '\',\'real\')">REAL THREAT</button>' +
          '<button class="verdict-btn' + (v === 'noise' ? ' picked-noise' : '') +
            '" onclick="setVerdict(\'' + a.id + '\',\'noise\')">FALSE ALARM</button>' +
        '</div>' +
      '</div>';
  }).join('');

  var left = ALERTS.filter(function (a) { return !verdicts[a.id]; }).length;
  var counter = document.getElementById('verdict-count');
  if (counter) {
    counter.textContent = left === 0
      ? 'Every alert has a verdict'
      : left + ' of 10 still need a verdict';
  }
}

function toggleDetails(id) {
  var box = document.getElementById('det-' + id);
  box.className = box.classList.contains('open') ? 'reveal-slot' : 'reveal-slot open';
}

function setVerdict(id, verdict) {
  var verdicts = Memo.get('verdicts', {});
  verdicts[id] = verdict;
  Memo.set('verdicts', verdicts);
  var open = ALERTS.filter(function (a) {
    var box = document.getElementById('det-' + a.id);
    return box && box.classList.contains('open');
  }).map(function (a) { return a.id; });
  renderAlerts('alerts');
  open.forEach(function (o) { document.getElementById('det-' + o).className = 'reveal-slot open'; });
  var banner = document.getElementById('alerts-banner');
  if (banner.className.indexOf('good') === -1) banner.className = 'banner';
}

/** Dangerous: clearing the queue by severity, the habit that hid the attack. */
function dismissLow() {
  showBanner('dismiss-banner', 'bad', 'Blocked by the simulation. Bulk-dismissing by severity is ' +
    'alert fatigue in one tap: one of those four LOW alerts is the attacker signing in. ' +
    'Severity is the tool\'s guess, not a verdict.');
  Cyberity.clueFound('dismissed_low');
}

function submitVerdicts() {
  var verdicts = Memo.get('verdicts', {});
  var missing = ALERTS.filter(function (a) { return !verdicts[a.id]; }).length;
  if (missing > 0) {
    showBanner('alerts-banner', 'warn', missing === 1
      ? 'One alert still has no verdict.'
      : missing + ' alerts still have no verdict.');
    return;
  }
  var right = ALERTS.filter(function (a) {
    return (verdicts[a.id] === 'real') === a.real;
  }).length;
  if (right < ALERTS.length) {
    showBanner('alerts-banner', 'warn', right + ' of 10 correct. For each one ask: does a ' +
      'schedule, a ticket or a known habit explain it? If nothing does, it\'s real.');
    return;
  }
  showBanner('alerts-banner', 'good', 'All ten right. Three real threats, and every one of ' +
    'them was in this queue before Monday 8 AM. Seven false alarms, including both HIGHs.');
  Cyberity.clueFound('alerts_complete');
}

/* ---------------------------------------------------------------------------
 * Sign-in logs
 *
 * Two searches matter: the account, and then the IP that doesn't fit it. Any
 * IP in a result is tappable, so pivoting is one tap once it's been spotted.
 * ------------------------------------------------------------------------ */
var ATTACKER_IP = '203.0.113.47';

var RAMOS_LOG = [
  ['Fri 07:48', 'SUCCESS', '10.14.3.21', 'Indang campus · LR-PC'],
  ['Fri 12:31', 'SUCCESS', '10.14.3.21', 'Indang campus · LR-PC'],
  ['Fri 16:30', 'SUCCESS', '10.14.3.21', 'Indang campus · LR-PC'],
  ['Fri 19:12', 'SUCCESS', '198.51.100.23', 'Cavite, PH · her Android phone'],
  ['Sat 01:55', 'SUCCESS', '198.51.100.23', 'Cavite, PH · her Android phone (mail sync)'],
  ['Sat 02:10', 'SUCCESS', ATTACKER_IP, 'Frankfurt, DE · NEW unknown device (mail app)'],
  ['Sat 09:20', 'SUCCESS', '198.51.100.23', 'Cavite, PH · her Android phone'],
  ['Sun 18:05', 'SUCCESS', '198.51.100.23', 'Cavite, PH · her Android phone'],
  ['Mon 07:28', 'SUCCESS', ATTACKER_IP, 'Frankfurt, DE · unknown device (mail app)'],
  ['Mon 07:52', 'SUCCESS', '10.14.3.21', 'Indang campus · LR-PC']
];

var IP_LOG = [
  ['Sat 02:03', 'FAILED', 'm.garcia'],
  ['Sat 02:05', 'FAILED', 'e.torres'],
  ['Sat 02:07', 'FAILED', 'p.lim'],
  ['Sat 02:10', 'SUCCESS', 'l.ramos'],
  ['Sat 02:14', 'FAILED', 'c.navarro'],
  ['Sat 02:18', 'FAILED', 'd.aquino'],
  ['Sat 02:24', 'SUCCESS', 'r.delacruz'],
  ['Sat 02:27', 'FAILED', 'g.santos'],
  ['Sat 02:31', 'SUCCESS', 'a.mendoza'],
  ['Sat 02:35', 'FAILED', 'h.villareal'],
  ['Sat 02:39', 'SUCCESS', 'j.bautista'],
  ['Sat 02:44', 'FAILED', 'n.ocampo'],
  ['Mon 07:28', 'SUCCESS', 'l.ramos']
];

function ipLink(ip) {
  return '<a class="ip-link" onclick="runSearch(\'' + ip + '\')">' + ip + '</a>';
}

function resultLine(cols, ipCol) {
  var status = cols[1] === 'SUCCESS'
    ? '<span class="ok">SUCCESS</span>'
    : '<span class="hot">FAILED </span>';
  var rest = cols.slice(2).map(function (c, i) {
    return i + 2 === ipCol ? ipLink(c) : escapeHtml(c);
  }).join('  ');
  return escapeHtml(cols[0]) + '  ' + status + '  ' + rest;
}

function runSearch(query) {
  var q = String(query || '').trim().toLowerCase();
  var input = document.getElementById('q');
  if (input) input.value = q;
  Memo.set('query', q);
  var out = document.getElementById('results');

  if (q.indexOf('ramos') !== -1) {
    out.innerHTML =
      '<div class="artifact a-sys">' +
        '<span class="artifact-tag">SIGN-INS · l.ramos · Fri to Mon</span>' +
        '<div class="rows mono">' + RAMOS_LOG.map(function (r) { return resultLine(r, 2); })
          .join('\n') + '</div>' +
        '<div class="note">10 results. Tap any IP address to search for it across every account.</div>' +
      '</div>' +
      '<button class="appui tool-btn" onclick="openBaseline()">' +
        '<b>BASELINE</b>What is normal for l.ramos, from the last 90 days</button>' +
      '<div class="reveal-slot" id="baseline"></div>';
    Cyberity.clueFound('searched_user');
    return;
  }

  if (q.indexOf(ATTACKER_IP) !== -1) {
    out.innerHTML =
      '<div class="artifact a-sys">' +
        '<span class="artifact-tag">SIGN-INS · from ' + ATTACKER_IP + ' · all accounts</span>' +
        '<div class="rows mono">' + IP_LOG.map(function (r) { return resultLine(r, -1); })
          .join('\n') +
          '\n...        <span class="hot">FAILED </span>  26 more accounts' +
          '\n\naccounts tried:  38' +
          '\n<span class="ok">signed in:        4</span>' +
          '\n<span class="hot">failed:          34</span>' +
          '\n\nevery account tried received the' +
          '\n"Payroll update" email on Friday</div>' +
        '<div class="note">Location for every line: Frankfurt, DE · unknown device (mail app).</div>' +
      '</div>';
    Cyberity.clueFound('searched_ip');
    return;
  }

  out.innerHTML = q === ''
    ? ''
    : '<div class="banner show warn">No results for "' + escapeHtml(q) + '". Try an account ' +
      'name like l.ramos, or an IP address from a result.</div>';
}

function openBaseline() {
  var box = document.getElementById('baseline');
  if (box.classList.contains('open')) return;
  box.className = 'reveal-slot open';
  box.innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">BASELINE · l.ramos · last 90 days</span>' +
      '<div class="rows mono">' + [
        'countries:  <span class="ok">PH only</span>',
        'places:     Indang campus (10.14.x.x)',
        '            Cavite, mobile (198.51.100.23)',
        'devices:    LR-PC (office desktop)',
        '            Android phone',
        'hours:      07:00 to 20:00 on campus',
        '            phone syncs mail day and night',
        'mail apps:  Outlook on LR-PC, phone app'
      ].join('\n') + '</div>' +
      '<div class="note">A sign-in that breaks one habit is worth a look. One that breaks three at ' +
        'once is a red flag.</div>' +
    '</div>';
  box.scrollIntoView({ behavior: 'smooth', block: 'start' });
  Cyberity.clueFound('baseline_ramos');
}

function restoreSearch() {
  var q = Memo.get('query', '');
  if (q) runSearch(q);
}

/* ---------------------------------------------------------------------------
 * Detection rules
 * ------------------------------------------------------------------------ */
var RULES = [
  {
    id: 'r1', name: 'Sign-in from a new country', status: 'ON', sev: 'LOW', mutable: true,
    rows: [
      'fires when:  an account signs in from a',
      '             country not seen in 90 days',
      'suppresses:  repeats from the same IP',
      '             within 30 minutes',
      'last month:  212 alerts',
      '',
      'this weekend:',
      '  Sat 02:10  l.ramos   (LOW, not reviewed)',
      '  <span class="warn">3 more suppressed as repeats</span>'
    ]
  },
  {
    id: 'r2', name: 'Mass outgoing email', status: 'ON', sev: 'MED',
    rows: [
      'fires when:  one account sends 40+ emails',
      '             in 30 minutes',
      '',
      'this weekend:',
      '  Mon 07:54  l.ramos   (MED, not reviewed)'
    ]
  },
  {
    id: 'r3', name: 'Newly registered domain', status: 'ON', sev: 'MED',
    rows: [
      'fires when:  a campus PC visits a domain',
      '             registered in the last 7 days',
      '',
      'this weekend:',
      '  Fri 16:52  cvsu-payroll.example  (MED,',
      '             38 PCs, not reviewed)'
    ]
  },
  {
    id: 'r4', name: 'Mailbox rule forwards outside CvSU', status: 'MUTED', sev: '-', muted: true,
    rows: [
      'fires when:  a mailbox rule forwards mail',
      '             to an address outside',
      '             cvsu.edu.ph',
      '',
      'Jul 02  created by itso.admin, MED',
      'Jul-Aug fired 30 to 40 times a week',
      '<span class="hot">Aug 14  MUTED by itso.admin:</span>',
      '<span class="hot">        "too noisy. 30+ a week, all staff</span>',
      '<span class="hot">         forwarding to their own Gmail"</span>',
      '',
      'since Aug 14: 0 alerts shown, 214 hidden',
      '',
      'this weekend, it would have fired on:',
      '<span class="hot">  Sat 02:10  l.ramos    -> r4mos.l@mailbox.example</span>',
      '<span class="hot">  Sat 02:25  r.delacruz -> rdc.bk@mailbox.example</span>',
      '<span class="hot">  Sat 02:32  a.mendoza  -> am.files@mailbox.example</span>',
      '<span class="hot">  Sat 02:40  j.bautista -> jbau.m@mailbox.example</span>',
      '<span class="warn">  each rule also deletes the forwarded</span>',
      '<span class="warn">  mail from Sent Items</span>'
    ]
  },
  {
    id: 'r5', name: 'Brute force: 20+ failed sign-ins', status: 'ON', sev: 'MED',
    rows: [
      'fires when:  one account fails 20+ times',
      '             in an hour',
      '',
      'this weekend:',
      '  Sat 03:00  svc-printer  (MED, not reviewed)'
    ]
  }
];

function renderRules(mountId) {
  document.getElementById(mountId).innerHTML = RULES.map(function (r, i) {
    return '<div class="choice rule-card' + (r.muted ? ' muted' : '') + '">' +
        '<div class="alert-top">' +
          '<span class="rule-status ' + (r.muted ? 'off' : 'on') + '">' + r.status + '</span>' +
          (r.sev !== '-' ? '<span class="sev sev-' + r.sev.toLowerCase() + '">' + r.sev + '</span>' : '') +
        '</div>' +
        '<div class="alert-title">' + escapeHtml(r.name) + '</div>' +
        '<button class="appui tool-btn small" onclick="openRule(' + i + ')">' +
          '<b>HISTORY</b></button>' +
        '<div class="reveal-slot" id="rule-' + i + '"></div>' +
        (r.mutable ? '<button class="cta danger" onclick="muteCountry()">MUTE THIS RULE ' +
          '(212 alerts last month)</button><div class="banner" id="mute-banner"></div>' : '') +
      '</div>';
  }).join('');
}

function openRule(index) {
  var r = RULES[index];
  var box = document.getElementById('rule-' + index);
  if (box.classList.contains('open')) return;
  box.className = 'reveal-slot open';
  box.innerHTML = '<div class="rows mono">' + r.rows.join('\n') + '</div>';
  if (r.muted) Cyberity.clueFound('muted_rule');
}

/** Dangerous: the same mistake that hid the forwarding rule, made again. */
function muteCountry() {
  showBanner('mute-banner', 'bad', 'Blocked by the simulation. This is the rule that caught the ' +
    'attacker signing in. Muting it because it\'s noisy is exactly how the forwarding alert ' +
    'went blind in August. Tune noisy rules; don\'t silence them.');
  Cyberity.clueFound('muted_country');
}

/* ---------------------------------------------------------------------------
 * Rule builder
 *
 * Last week's 46 mailbox-rule events, grouped by what they look like. The
 * counter re-tests on every toggle. Several combinations reach 4 of 4 with no
 * false alarms; what they share is that none of them relies on external
 * forwarding alone.
 * ------------------------------------------------------------------------ */
var CONDITIONS = [
  { key: 'ext', label: 'Forwards to an address outside cvsu.edu.ph' },
  { key: 'del', label: 'Also deletes or hides the forwarded mail' },
  { key: 'nc', label: 'Created right after a sign-in from a new country' },
  { key: 'off', label: 'Created between 10 PM and 5 AM' },
  { key: 'admin', label: 'Created by an admin account' }
];

var EVENTS = [
  { n: 4, attack: true, ext: 1, del: 1, nc: 1, off: 1, admin: 0, who: 'the attacker' },
  { n: 24, ext: 1, del: 0, nc: 0, off: 0, admin: 0, who: 'staff forwarding to their own Gmail' },
  { n: 6, ext: 1, del: 0, nc: 0, off: 1, admin: 0, who: 'staff setting up Gmail forwards at night' },
  { n: 5, ext: 0, del: 0, nc: 0, off: 0, admin: 0, who: 'inbox sorting rules' },
  { n: 2, ext: 0, del: 1, nc: 0, off: 0, admin: 0, who: 'newsletter auto-delete rules' },
  { n: 1, ext: 0, del: 1, nc: 0, off: 1, admin: 0, who: 'a late-night newsletter auto-delete rule' },
  { n: 2, ext: 1, del: 0, nc: 1, off: 1, admin: 0, who: 'staff at a conference in Singapore' },
  { n: 1, ext: 0, del: 1, nc: 1, off: 0, admin: 0, who: 'a staff member abroad deleting spam' },
  { n: 1, ext: 1, del: 1, nc: 0, off: 0, admin: 1, who: 'ITSO\'s vendor help desk forward' }
];

var picked = {};

function testRule() {
  var keys = CONDITIONS.map(function (c) { return c.key; }).filter(function (k) { return picked[k]; });
  var caught = 0, noise = 0, who = [];
  EVENTS.forEach(function (e) {
    var hit = keys.every(function (k) { return e[k] === 1; });
    if (!hit) return;
    if (e.attack) caught += e.n;
    else { noise += e.n; who.push(e.n + ' × ' + e.who); }
  });
  return { caught: caught, noise: noise, who: who, any: keys.length > 0 };
}

function renderBuilder(mountId) {
  document.getElementById(mountId).innerHTML = CONDITIONS.map(function (c) {
    return '<button class="cond' + (picked[c.key] ? ' on' : '') +
      '" onclick="toggleCond(\'' + c.key + '\')">' +
        '<span class="cond-box">' + (picked[c.key] ? '&#10003;' : '') + '</span>' +
        '<span>' + escapeHtml(c.label) + '</span>' +
      '</button>';
  }).join('');

  var r = testRule();
  document.getElementById('caught').textContent = r.caught + ' of 4';
  document.getElementById('noise').textContent = String(r.noise);
  document.getElementById('caught-bar').style.width = (r.caught / 4 * 100) + '%';
  document.getElementById('noise-bar').style.width = (r.noise / 42 * 100) + '%';
  document.getElementById('caught').className = 'mono ' + (r.caught === 4 ? 'good-text' : 'bad-text');
  document.getElementById('noise').className = 'mono ' + (r.noise === 0 ? 'good-text' : 'bad-text');
  document.getElementById('noise-who').textContent = r.noise === 0
    ? (r.caught === 4 ? 'Every attack, no false alarms.' : 'Quiet, but it\'s missing attacks: a false negative.')
    : 'Would also fire on: ' + r.who.join(', ') + '.';
}

function toggleCond(key) {
  picked[key] = !picked[key];
  renderBuilder('conditions');
  var banner = document.getElementById('builder-banner');
  if (banner.className.indexOf('good') === -1) banner.className = 'banner';
}

function saveRule() {
  var r = testRule();
  if (!r.any) {
    showBanner('builder-banner', 'warn', 'Pick at least one condition. A rule with none fires on ' +
      'every mailbox rule anyone makes.');
    return;
  }
  if (r.caught < 4) {
    showBanner('builder-banner', 'warn', 'This rule misses ' + (4 - r.caught) + ' of the 4 attacks. ' +
      'A quiet rule that misses the attack is a false negative. Remove a condition the attacker ' +
      'didn\'t meet.');
    return;
  }
  if (r.noise > 0) {
    showBanner('builder-banner', 'warn', 'Every attack caught, but ' + r.noise + ' false alarms a ' +
      'week. That\'s how the old rule ended up muted. Add a condition that normal staff rules ' +
      'don\'t meet.');
    return;
  }
  var banner = document.getElementById('builder-banner');
  banner.className = 'banner show good';
  banner.innerHTML = 'Rule saved and switched on. Time to detect: 63 h &rarr; ~1 min. ' +
    'Rule code: <b class="mono">CYBERITY{c4ught_1n_m1nut3s}</b>';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Memo.set('saved', true);
  Cyberity.clueFound('rule_saved');
  Cyberity.flagDiscovered('rule_saved');
}
