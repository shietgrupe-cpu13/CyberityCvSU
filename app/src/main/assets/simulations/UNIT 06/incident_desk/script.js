/* Cyberity — Incident Desk (level 601).
 * Local only. Fictional people, tickets, hosts and addresses; nothing is sent,
 * no account is touched, and nothing on the device this runs on changes.
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

/* Which tickets have been opened. sessionStorage first, window.name as the
   fallback that survives a page load inside the WebView. */
var Store = {
  KEY: 'id_seen',
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
  }
};

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* ---------------------------------------------------------------------------
 * The seven weekend tickets
 *
 * Every ticket gets the same card and the same weight on the desk: nothing in
 * the list says which are incidents. The loud ones (antivirus, firewall) are
 * events; the apologetic one (TKT-0144) is the worst thing in the queue.
 * ------------------------------------------------------------------------ */
var TICKETS = {
  t1: {
    id: 't1', code: 'TKT-0139', when: 'Sat 11:20 PM', channel: 'WEB FORM',
    title: 'Someone tried to log in to my portal?',
    from: 'Ana C. · BSCS 2',
    report: 'I got an email saying there was a failed login on my student portal account at ' +
      '11:14 PM. Then I realised it was probably me, I typed my password wrong the first ' +
      'time. Just checking it\'s fine?',
    tools: [
      {
        label: 'LOGIN HISTORY', sub: 'Her portal account, last 30 days',
        tag: 'LOGIN HISTORY · student portal',
        rows: [
          'Sat 23:14  <span class="warn">failed</span>   her phone · Indang, Cavite',
          'Sat 23:15  <span class="ok">success</span>  same phone, same Wi-Fi',
          '',
          'other failed attempts, 30 days:  0',
          'new devices, 30 days:            0'
        ],
        note: 'One typo, then a normal login a minute later from the same phone. The alert did ' +
          'its job. Nothing was exposed, changed or lost.'
      }
    ]
  },

  t2: {
    id: 't2', code: 'TKT-0140', when: 'Sun 2:35 PM', channel: 'CALL',
    title: 'Antivirus warning on LAB04',
    from: 'Lab technician · Computer Laboratory 1',
    report: 'Antivirus popped up on LAB04 during the weekend open lab. A student tried to ' +
      'download a "free Minecraft" file. Big red warning. Logging it like you asked us to.',
    tools: [
      {
        label: 'ANTIVIRUS LOG', sub: 'LAB04, Sunday',
        tag: 'ANTIVIRUS LOG · LAB04',
        rows: [
          'Sun 14:22  download: free_minecraft_2026.exe',
          '<span class="hot">           trojan detected</span>',
          '<span class="ok">           BLOCKED before it ran</span>',
          '<span class="ok">           moved to quarantine</span>',
          'Sun 14:30  full scan: <span class="ok">0 threats</span>',
          '',
          'session: guest account, no admin rights'
        ],
        note: 'The scariest-looking ticket in the queue, and a defence that worked: the file never ' +
          'ran. That is an event worth logging, not an incident.'
      }
    ]
  },

  t3: {
    id: 't3', code: 'TKT-0141', when: 'Sat 9:40 PM', channel: 'WEB FORM',
    title: 'Our grades are on the Freedom Wall',
    from: 'Student · BSIT 3',
    report: 'Someone posted a link on the CvSU Freedom Wall page. It opens a spreadsheet with ' +
      'ALL the BSIT students\' grades, birthdays and home addresses. Mine is in there. Please ' +
      'do something.',
    tools: [
      {
        label: 'WHAT\'S IN THE FILE', sub: 'The sheet behind the link',
        clue: 'file_t3', tag: 'FILE DETAILS · BSIT_grades_2nd_sem_FINAL.xlsx',
        rows: [
          'owner:    registrar.staff3@cvsu.edu.ph',
          '<span class="hot">sharing:  anyone with the link can view</span>',
          'shared:   Sat 19:20',
          '',
          'rows:     1,214 students',
          'columns:',
          '<span class="hot">  student number</span>',
          '  full name',
          '<span class="hot">  birthday</span>',
          '<span class="hot">  home address</span>',
          '<span class="hot">  final grades, every subject</span>'
        ],
        note: 'A staff member meant to share it with one office and picked "anyone with the link" ' +
          'instead. No hacker needed: personal data is exposed all the same.'
      },
      {
        label: 'INCIDENT POLICY', sub: 'CvSU ITSO reporting procedure',
        clue: 'policy_t3', tag: 'POLICY · ITSO incident reporting (excerpt)',
        rows: [
          '1. Report every suspected incident to',
          '   the ITSO incident line: local 2110',
          '   or itso-incident@cvsu.edu.ph',
          '2. Do not investigate, fix or delete',
          '   anything yourself.',
          '3. Do not post or talk about it publicly,',
          '   and do not contact the person you',
          '   think is responsible.',
          '',
          '<span class="warn">4. PERSONAL DATA: ITSO also informs the</span>',
          '<span class="warn">   Data Protection Officer at once.</span>',
          '<span class="warn">   The DPO notifies the National Privacy</span>',
          '<span class="warn">   Commission within 72 hours of the</span>',
          '<span class="warn">   University learning of the breach,</span>',
          '<span class="warn">   and notifies the affected people.</span>',
          '',
          '<span class="hot">this ticket received:  Sat 21:40</span>',
          '<span class="hot">now:                   Mon 08:02</span>',
          '<span class="hot">72-hour deadline:      Tue 21:40</span>'
        ],
        note: 'The clock started when the student told ITSO, not when someone opened the ticket. ' +
          'Over 58 of the 72 hours are already gone.'
      },
      {
        label: 'FOLLOW-UP', sub: 'What changed since the ticket came in',
        clue: 'update_t3', tag: 'FOLLOW-UP · Registrar',
        rows: [
          'Sun 09:05  Registrar turned link sharing OFF',
          '           (staff saw the post themselves)',
          '<span class="ok">link now: access denied</span>',
          '',
          '<span class="hot">open for:          13 h 45 min</span>',
          '<span class="hot">times opened:      640</span>',
          '<span class="hot">copies downloaded: unknown</span>'
        ],
        note: 'The leak has stopped, but anyone who opened it in those 14 hours could have kept a ' +
          'copy. Closing a link doesn\'t un-expose the data.'
      }
    ],
    actions: [
      {
        label: 'POST A WARNING ON THE FREEDOM WALL', clue: 'posted_wall',
        result: 'Blocked by the simulation. A public post sends even more people looking for the ' +
          'sheet, and announces the breach before the DPO has assessed it or the affected ' +
          'students have been told properly. Report it; don\'t broadcast it.'
      }
    ]
  },

  t4: {
    id: 't4', code: 'TKT-0144', when: 'Mon 7:57 AM', channel: 'EMAIL',
    title: 'Strange replies from my students',
    from: 'Prof. Liza Ramos · Department of IT',
    report: 'Good morning. Since earlier, some of my students have been replying to me asking ' +
      'about a "scholarship release form". I never sent anything like that. Probably spam? ' +
      'Sorry to bother you on a Monday.',
    tools: [
      {
        label: 'THE EMAIL SHE CLICKED', sub: 'Still in her inbox, from Friday',
        clue: 'email_t4', tag: 'EMAIL · from her inbox', kind: 'a-msg',
        rows: [
          'From:    CvSU Payroll <payroll@cvsu-payroll.example>',
          'To:      l.ramos@cvsu.edu.ph',
          'Date:    Fri 16:41',
          'Subject: Payroll update: confirm your account',
          '',
          '"Your October salary is on hold until',
          ' you confirm your CvSU account."',
          '[ CONFIRM ACCOUNT ]',
          '<span class="hot">  -> https://cvsu-payroll.example/login</span>',
          '',
          '<span class="warn">Fri 16:47  link opened on her PC</span>',
          '<span class="hot">Fri 16:47  password typed into that page</span>',
          '',
          'same email also sent to: 37 other staff'
        ],
        note: 'The sender isn\'t cvsu.edu.ph. This email is evidence: it holds the attacker\'s ' +
          'domain and the list of 37 other staff who got it, who may also have clicked.'
      },
      {
        label: 'MAILBOX RULES', sub: 'Automatic rules on her account',
        clue: 'rules_t4', tag: 'MAILBOX RULES · l.ramos',
        rows: [
          'rule 1  "Move newsletters"',
          '        created Aug 12 by l.ramos, on campus',
          '',
          '<span class="hot">rule 2  (no name)</span>',
          '<span class="hot">        created Sat 02:10</span>',
          '<span class="hot">        from 203.0.113.47, outside PH</span>',
          '<span class="hot">        forward ALL mail -> r4mos.l@mailbox.example</span>',
          '<span class="hot">        then delete it from Sent Items</span>',
          '',
          '<span class="warn">Sat 02:10  login as l.ramos</span>',
          '<span class="warn">           new device, new country</span>'
        ],
        note: 'She didn\'t make rule 2. Someone signed in with her password at 2 AM Saturday and ' +
          'set it up to copy her mail and hide what they send from her own account.'
      },
      {
        label: 'SENT LOG', sub: 'From the mail server, not her Sent folder',
        clue: 'outbox_t4', tag: 'MAIL SERVER · outgoing from l.ramos',
        rows: [
          'Subject: "Scholarship release form"',
          '  link -> cvsu-payroll.example/scholar',
          '',
          'Mon 07:30  to 10 students',
          'Mon 07:36  to 10 students',
          'Mon 07:42  to 10 students',
          'Mon 07:48  to 10 students',
          'Mon 07:54  to 10 students',
          '<span class="hot">Mon 08:00  to 2 students, batch running</span>',
          '',
          '<span class="hot">sent so far:  52</span>',
          '<span class="hot">still queued: 348 students</span>',
          '<span class="hot">● STILL SENDING</span>'
        ],
        note: 'Her Sent folder looks empty because of rule 2. The server saw everything: a phishing ' +
          'link going out to her students from a real professor\'s address, ten at a time, right ' +
          'now.'
      }
    ],
    actions: [
      {
        label: 'DELETE THE PHISHING EMAIL', clue: 'deleted_email',
        result: 'Blocked by the simulation. Deleting it destroys the sender, the link and the ' +
          'headers, and the list of 37 other staff who received it. The investigators need it ' +
          'exactly as it is.'
      },
      {
        label: 'FORWARD IT TO ALL STAFF AS A WARNING', clue: 'forwarded_email',
        result: 'Blocked by the simulation. That puts a live phishing link in front of every staff ' +
          'member on campus. Warnings come from ITSO, written fresh, without the link.'
      },
      {
        label: 'RUN A CLEANER AND RESTART HER PC', clue: 'restarted_pc',
        result: 'Blocked by the simulation. A restart wipes what was running and a cleaner rewrites ' +
          'files and timestamps. And it fixes nothing: the attacker is in her email account, not ' +
          'on her PC.'
      }
    ]
  },

  t5: {
    id: 't5', code: 'TKT-0142', when: 'Sun 12:10 AM', channel: 'WEB FORM',
    title: 'Portal won\'t load!!',
    from: 'Student · BSHM 1',
    report: 'I\'ve been trying to submit my enlistment since 10 PM and the portal won\'t load. ' +
      'Deadline is Monday!! Are we hacked??',
    tools: [
      {
        label: 'ITSO ANNOUNCEMENTS', sub: 'Posted on the ITSO page this week',
        tag: 'ANNOUNCEMENT · CvSU ITSO page',
        rows: [
          'Wed 10:00  "Student portal maintenance:',
          '            Sat 10:00 PM to Sun 2:00 AM.',
          '            Please submit enlistment early."',
          '',
          '<span class="ok">Sun 01:41  portal back online</span>',
          'data lost:          none',
          'unexpected access:  none'
        ],
        note: 'The portal was down, but on purpose, on schedule, and announced. Planned work is not ' +
          'an incident, even when it\'s badly timed.'
      }
    ]
  },

  t6: {
    id: 't6', code: 'TKT-0143', when: 'Sat 4:20 PM', channel: 'EMAIL',
    title: 'Work laptop stolen',
    from: 'Mr. Ben Aquino · Guidance Office',
    report: 'My car window was smashed in a mall parking lot this afternoon and my bag was taken. ' +
      'My CvSU laptop was in it. I\'ve filed a police report. What do I do about the laptop?',
    tools: [
      {
        label: 'DEVICE RECORD', sub: 'ITSO asset inventory',
        clue: 'device_t6', tag: 'DEVICE RECORD · CVSU-GO-0117',
        rows: [
          'assigned:    B. Aquino, Guidance Office',
          '<span class="warn">contains:    counseling notes,</span>',
          '<span class="warn">             about 85 students</span>',
          '',
          '<span class="ok">disk encryption:  BitLocker, ON</span>',
          '<span class="ok">sign-in:          password + PIN</span>',
          '<span class="ok">remote lock:      available</span>',
          'last online: Sat 15:02',
          '',
          'police report: filed Sat 17:10'
        ],
        note: 'Counseling notes are among the most sensitive records a school keeps, so this is an ' +
          'incident. But with full-disk encryption, a thief without the password sees only ' +
          'scrambled data.'
      }
    ]
  },

  t7: {
    id: 't7', code: 'TKT-0145', when: 'Mon 12:00 AM', channel: 'AUTO',
    title: 'Daily summary: 1,912 blocked connections',
    from: 'Campus firewall · automatic',
    report: 'ALERT: 1,912 inbound connection attempts blocked in the last 24 hours. Top ports: 22, ' +
      '3389.',
    tools: [
      {
        label: 'FIREWALL SUMMARY', sub: 'Sunday, all day',
        tag: 'FIREWALL · daily summary',
        rows: [
          'blocked inbound:              1,912',
          '  port 22   (remote login)    1,104',
          '  port 3389 (remote desktop)    691',
          '  other                         117',
          '<span class="ok">allowed through:                  0</span>',
          '',
          'normal daily range:   1,500 to 2,500'
        ],
        note: 'Automated scanners knock on every address on the internet, every day. Blocked knocks ' +
          'are the firewall working. They\'re logged so a change in the pattern stands out (that\'s ' +
          'Level 602).'
      }
    ]
  }
};

/* Shown in ticket-number order, the way a real queue lists them. */
var ORDER = ['t1', 't2', 't3', 't5', 't6', 't4', 't7'];

/* ---------------------------------------------------------------------------
 * Returning to the list
 *
 * Shared block: identical in every simulation apart from KEY.
 *
 * Opening an item is a full page load, so coming back drops the student at the
 * top of the list and they have to hunt for their place again. Remember which
 * item was opened and put it back under their eyes instead.
 *
 * sessionStorage only, deliberately: this is a convenience, not state the
 * level depends on, so if the WebView refuses it the list simply behaves as
 * it did before.
 * ------------------------------------------------------------------------ */
var ReturnTo = {
  KEY: 'incident_desk_last',
  remember: function (id) {
    if (!id) return;
    try { window.sessionStorage.setItem(ReturnTo.KEY, id); } catch (e) { /* ignored */ }
  },
  restore: function () {
    var id = null;
    try { id = window.sessionStorage.getItem(ReturnTo.KEY); } catch (e) { id = null; }
    if (!id) return;
    var row = document.getElementById('card-' + id) ||
      document.querySelector('[href$="#' + id + '"]');
    // Jumped, not smoothed: an animated scroll on arrival reads as the page
    // sliding away by itself.
    if (row) row.scrollIntoView({ block: 'center' });
  }
};

/* ---------------------------------------------------------------------------
 * The queue
 * ------------------------------------------------------------------------ */
function renderDesk(mountId) {
  var html = ORDER.map(function (id) {
    var t = TICKETS[id];
    var seen = Store.has(id);
    return '' +
      '<a class="choice proc-row ' + (seen ? 'read' : 'unread') +
        '" id="card-' + id + '" href="ticket.html#' + id + '">' +
        '<span class="proc-body">' +
          '<span class="sms-top">' +
            '<span class="ticket-id mono">' + escapeHtml(t.code) + '</span>' +
            '<span class="sms-when">' + escapeHtml(t.when) + '</span>' +
          '</span>' +
          '<span class="proc-name">' + escapeHtml(t.title) + '</span>' +
          '<span class="spec-meta">' + escapeHtml(t.from) +
            ' &nbsp;<span class="kind">' + escapeHtml(t.channel) + '</span></span>' +
          '<span class="mail-badge">' + (seen ? 'OPENED' : 'NEW') + '</span>' +
        '</span>' +
      '</a>';
  }).join('');

  document.getElementById(mountId).innerHTML = html;

  var left = ORDER.filter(function (k) { return !Store.has(k); }).length;
  var counter = document.getElementById('seen-count');
  if (counter) {
    counter.textContent = left === 0
      ? 'All seven opened'
      : left + ' of 7 not opened yet';
  }

  ReturnTo.restore();
}

/* ---------------------------------------------------------------------------
 * One ticket
 * ------------------------------------------------------------------------ */
function renderTicket(mountId) {
  ReturnTo.remember((window.location.hash || '').substring(1));

  var id = (window.location.hash || '#t1').substring(1);
  var t = TICKETS[id] || TICKETS.t1;
  window.CURRENT = t;

  Store.mark(t.id);
  Cyberity.clueFound('opened_' + t.id);

  var tools = t.tools.map(function (tool, i) {
    return '<button class="appui tool-btn" onclick="openTool(' + i + ')">' +
        '<b>' + escapeHtml(tool.label) + '</b>' + escapeHtml(tool.sub) +
      '</button>' +
      '<div class="reveal-slot" id="tool-' + i + '"></div>';
  }).join('');

  var actions = (t.actions || []).map(function (a, i) {
    return '<button class="cta danger" onclick="takeAction(' + i + ')">' +
      escapeHtml(a.label) + '</button>';
  }).join('');

  document.getElementById(mountId).innerHTML =
    '<div class="artifact a-per">' +
      '<span class="artifact-tag">' + escapeHtml(t.channel) + ' · ' + escapeHtml(t.when) + '</span>' +
      '<h2>' + escapeHtml(t.title) + '</h2>' +
      '<div class="detail-sub">' + escapeHtml(t.from) + '</div>' +
      '<p class="report-text">' + escapeHtml(t.report) + '</p>' +
    '</div>' +
    '<div class="section-label flush">Evidence</div>' +
    tools +
    (actions ? '<div class="section-label flush">Actions</div>' + actions : '') +
    '<div class="banner" id="ticket-banner"></div>' +
    '<a class="cta" href="desk.html">BACK TO THE DESK</a>';

  var title = document.getElementById('ticket-title');
  if (title) title.textContent = t.code;
  window.scrollTo(0, 0);
}

function openTool(index) {
  var tool = window.CURRENT.tools[index];
  var box = document.getElementById('tool-' + index);
  if (!tool || box.classList.contains('open')) return;

  box.className = 'reveal-slot open';
  box.innerHTML =
    '<div class="artifact ' + (tool.kind || 'a-sys') + '">' +
      '<span class="artifact-tag">' + escapeHtml(tool.tag) + '</span>' +
      '<div class="rows mono">' + tool.rows.join('\n') + '</div>' +
      '<div class="note">' + escapeHtml(tool.note) + '</div>' +
    '</div>';
  box.scrollIntoView({ behavior: 'smooth', block: 'start' });

  if (tool.clue) Cyberity.clueFound(tool.clue);
}

/** Dangerous: well-meaning "fixes" that destroy evidence or spread the harm. */
function takeAction(index) {
  var a = window.CURRENT.actions[index];
  var banner = document.getElementById('ticket-banner');
  banner.className = 'banner show bad';
  banner.textContent = a.result;
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound(a.clue);
}

/* ---------------------------------------------------------------------------
 * Triage board
 *
 * One tag per ticket. Submitting only says how many are right, never which,
 * so the student has to reason from the evidence rather than cycle the chips.
 * ------------------------------------------------------------------------ */
var TAGS = ['Event', 'Incident'];
var TRIAGE_KEY = {
  t1: 'Event', t2: 'Event', t3: 'Incident', t4: 'Incident',
  t5: 'Event', t6: 'Incident', t7: 'Event'
};
var tags = {};

function renderTriage(mountId) {
  document.getElementById(mountId).innerHTML = ORDER.map(function (id) {
    var t = TICKETS[id];
    var chips = TAGS.map(function (label, i) {
      var on = tags[id] === label ? ' on' : '';
      return '<button class="chip' + on + '" onclick="tag(\'' + id + '\',' + i + ')">' +
        escapeHtml(label) + '</button>';
    }).join('');
    return '<div class="board-row">' +
        '<div class="board-name"><span class="ticket-id mono">' + escapeHtml(t.code) +
          '</span> ' + escapeHtml(t.title) + '</div>' +
        '<div class="board-hint">' + escapeHtml(t.from) + '</div>' +
        '<div class="chips">' + chips + '</div>' +
      '</div>';
  }).join('');
}

function tag(id, index) {
  tags[id] = TAGS[index];
  renderTriage('board');
  var banner = document.getElementById('board-banner');
  if (banner.className.indexOf('good') === -1) banner.className = 'banner';
}

function submitTriage() {
  var banner = document.getElementById('board-banner');
  var missing = ORDER.filter(function (id) { return !tags[id]; }).length;
  if (missing > 0) {
    banner.className = 'banner show warn';
    banner.textContent = missing === 1
      ? 'One ticket still has no tag.'
      : missing + ' tickets still have no tag.';
    return;
  }
  var right = ORDER.filter(function (id) { return tags[id] === TRIAGE_KEY[id]; }).length;
  if (right < ORDER.length) {
    banner.className = 'banner show warn';
    banner.textContent = right + ' of 7 correct. For the ones you are least sure of, ask: was ' +
      'anything actually exposed, taken over or lost, or did a defence simply do its job?';
    return;
  }
  banner.className = 'banner show good';
  banner.textContent = 'All seven sorted correctly. Four events to log, three incidents that ' +
    'need action this morning.';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('triage_complete');
}

/* ---------------------------------------------------------------------------
 * Incident report form
 *
 * Every field is a pick list, so the student is judging facts, not typing on
 * a phone keyboard. Wrong options are the mistakes real first reports make:
 * the discovery time in place of the start time, a guess in place of a fact.
 * ------------------------------------------------------------------------ */
var CHAT = [
  { us: true, t: '8:10', text: 'Good morning, Prof. Ramos. This is the ITSO service desk about ' +
    'your ticket. Please don\'t delete anything or restart your PC for now.' },
  { us: false, t: '8:10', text: 'Oh, okay. I haven\'t touched anything. Is it serious?' },
  { us: true, t: '8:11', text: 'Did you open any email recently that asked you to log in?' },
  { us: false, t: '8:12', text: 'The "Payroll update" one on Friday. I clicked it at 4:47, I ' +
    'remember because I was about to go home. It asked for my CvSU login so I typed it in. ' +
    'The page just reloaded.' },
  { us: true, t: '8:12', text: 'When did you first notice something was off?' },
  { us: false, t: '8:13', text: 'This morning at 7:55, when I opened my mail. Three students had ' +
    'replied asking about a "scholarship release form". I never sent that.' },
  { us: true, t: '8:15', text: 'Understood. I\'ve just reported it to the ITSO incident line and ' +
    'they\'re taking it from here. Please leave your PC and your mailbox exactly as they are.' },
  { us: false, t: '8:15', text: 'Thank you. I\'m so sorry, I thought it was real.' },
  { us: true, t: '8:16', text: 'You did the right thing by reporting it.' }
];

var FIELDS = [
  {
    label: 'What happened',
    options: [
      'Spam received by a faculty member',
      'Faculty email account taken over through a fake login page',
      'Ransomware on a faculty PC',
      'Faculty member forgot her password'
    ],
    correct: 1
  },
  {
    label: 'When it started (earliest known)',
    options: [
      'Fri 4:47 PM · password typed into the fake page',
      'Sat 2:10 AM · forwarding rule created',
      'Mon 7:55 AM · she noticed the replies',
      'Mon 8:02 AM · ticket opened by the desk'
    ],
    correct: 0
  },
  {
    label: 'When it was discovered',
    options: [
      'Fri 4:47 PM · password typed into the fake page',
      'Sat 2:10 AM · forwarding rule created',
      'Mon 7:55 AM · she noticed the replies',
      'Mon 8:02 AM · ticket opened by the desk'
    ],
    correct: 2
  },
  {
    label: 'Reported by',
    options: [
      'A student who received the email',
      'Prof. Liza Ramos, Department of IT',
      'The campus firewall',
      'The service desk'
    ],
    correct: 1
  },
  {
    label: 'What is affected',
    options: [
      'Her PC only',
      'The student portal',
      'Her CvSU email account, and the students it is emailing',
      'The whole campus network'
    ],
    correct: 2
  },
  {
    label: 'Still happening?',
    options: [
      'No, it stopped on Saturday',
      'Yes, the account is still sending phishing emails',
      'Unknown'
    ],
    correct: 1
  },
  {
    label: 'Actions already taken',
    options: [
      'Phishing email deleted and PC restarted',
      'Warning forwarded to all staff',
      'Her password changed by the service desk',
      'Nothing changed on her PC or mailbox; reported to the ITSO incident line at 8:15 AM'
    ],
    correct: 3
  }
];

function renderReport(mountId) {
  document.getElementById(mountId).innerHTML = FIELDS.map(function (f, i) {
    var opts = '<option value="">Choose…</option>' + f.options.map(function (o, j) {
      return '<option value="' + j + '">' + escapeHtml(o) + '</option>';
    }).join('');
    return '<label class="form-row">' +
        '<span class="form-label">' + escapeHtml(f.label) + '</span>' +
        '<select class="form-select" id="field-' + i + '" onchange="clearReportBanner()">' +
          opts + '</select>' +
      '</label>';
  }).join('');
}

function openChat() {
  var box = document.getElementById('chat-slot');
  if (box.classList.contains('open')) return;
  box.className = 'reveal-slot open';
  box.innerHTML =
    '<div class="artifact a-per">' +
      '<span class="artifact-tag">CALL TRANSCRIPT · MON</span>' +
      '<div class="chat">' + CHAT.map(function (m) {
        return '<div class="bubble-row' + (m.us ? ' us' : '') + '">' +
            '<div class="bubble">' +
              '<div class="bubble-name">' + (m.us ? 'You' : 'Prof. Ramos') + ' · ' + m.t + '</div>' +
              escapeHtml(m.text) +
            '</div>' +
          '</div>';
      }).join('') + '</div>' +
    '</div>';
  box.scrollIntoView({ behavior: 'smooth', block: 'start' });
  Cyberity.clueFound('chat_t4');
}

function clearReportBanner() {
  var banner = document.getElementById('report-banner');
  if (banner.className.indexOf('good') === -1) banner.className = 'banner';
}

function submitReport() {
  var banner = document.getElementById('report-banner');
  var values = FIELDS.map(function (f, i) { return document.getElementById('field-' + i).value; });
  var missing = values.filter(function (v) { return v === ''; }).length;
  if (missing > 0) {
    banner.className = 'banner show warn';
    banner.textContent = missing === 1
      ? 'One field is still empty.'
      : missing + ' fields are still empty.';
    return;
  }
  var right = FIELDS.filter(function (f, i) { return Number(values[i]) === f.correct; }).length;
  if (right < FIELDS.length) {
    banner.className = 'banner show warn';
    banner.textContent = right + ' of 7 correct. Check the chat again: when did it START, and ' +
      'when was it only NOTICED?';
    return;
  }
  banner.className = 'banner show good';
  banner.innerHTML = 'Report accepted by the incident team. Report code: ' +
    '<b class="mono">CYBERITY{r3p0rt_1t_f4st}</b>';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('report_complete');
  Cyberity.flagDiscovered('report_complete');
}
