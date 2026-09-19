/* Cyberity — ITSO Mail Forensics Bench simulation (level 302).
 * Local only. No network, no real accounts. Every address and link is
 * fictional and links never load.
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

var Store = {
  get: function (key) { try { return window.sessionStorage.getItem('mf_' + key); } catch (e) { return null; } },
  set: function (key, value) { try { window.sessionStorage.setItem('mf_' + key, value); } catch (e) {} }
};

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* ---------------------------------------------------------------------------
 * The four emails
 * ------------------------------------------------------------------------ */
var MAILS = {
  e1: {
    id: 'e1', code: 'E1', time: 'Mon 09:20',
    fromName: 'CHED Scholarship', fromAddr: 'scholarship@ched.gov.ph.stipend-verify.com',
    to: 'you@cvsu.edu.ph',
    subject: 'Your scholarship stipend is on hold',
    body: [
      'Good day,',
      'Your ₱10,000 stipend release is on hold pending identity verification. Verify now ' +
        'to receive it this cycle.',
      { link: 'https://ched.gov.ph.verify-portal/claim', real: 'https://ched.gov.ph.stipend-verify.com/claim', clue: 'link_inspected_e1' },
      'CHED Scholarship Office'
    ]
  },
  e2: {
    id: 'e2', code: 'E2', time: 'Mon 12:44',
    fromName: 'BPI Online', fromAddr: 'alerts@Iogin-bpi.com',
    to: 'you@cvsu.edu.ph',
    subject: 'New sign-in to your account',
    body: [
      'Dear Client,',
      'A new device signed in to your BPI account. If this wasn\'t you, secure your account ' +
        'immediately.',
      { link: 'https://Iogin-bpi.com/secure', real: 'https://Iogin-bpi.com/secure', clue: null },
      'BPI Online Security'
    ]
  },
  e3: {
    id: 'e3', code: 'E3', time: 'Tue 08:05',
    fromName: 'CvSU Portal', fromAddr: 'no-reply@cvsu.edu.ph',
    to: 'you@cvsu.edu.ph',
    subject: 'Your 2nd semester grades are posted',
    body: [
      'Dear Student,',
      'Your grades for the 2nd semester are now available. View them at the link below.',
      { link: 'https://portal.cvsu.edu.ph/grades', real: 'http://45.61.87.200/grades', clue: 'link_inspected_e3' },
      'CvSU Student Portal'
    ]
  },
  e4: {
    id: 'e4', code: 'E4', time: 'Tue 15:30',
    fromName: 'Office of the Registrar', fromAddr: 'registrar@cvsu.edu.ph',
    to: 'you@cvsu.edu.ph',
    subject: 'FINAL NOTICE: unpaid balance — enrollment will be cancelled',
    body: [
      { text: 'Dear Student,',
        tell: { id: 'greeting', label: '"Dear Student" — a real balance notice names you.' } },
      { text: 'Our records show an unpaid balance of ₱4,500. If it is not settled TODAY, ' +
          'your enrollment will be cancelled and you will be dropped from all classes.',
        tell: { id: 'threat', label: 'Threat and false urgency: "settle TODAY or be dropped".' } },
      { text: 'Pay via GCash to 0917-555-2210 and reply to this email with a screenshot of ' +
          'the receipt.',
        tell: { id: 'channel', label: 'Payment demanded by GCash reply, not through the cashier.' } },
      { text: 'See the attached statement of account.', tell: null },
      { attachment: 'statement_of_account.pdf.exe',
        tell: { id: 'attach', label: 'Attachment is .pdf.exe — a program, not a document.' } },
      { text: 'Office of the Registrar', tell: null }
    ],
    greetingTell: { id: 'greeting', label: '"Dear Student" — a real balance notice names you.' },
    headers: {
      rows: [
        'From        : Office of the Registrar <registrar@cvsu.edu.ph>',
        'Reply-To    : cvsu.billing.dept@gmail.example',
        'Return-Path : bounce@mailerblast.example',
        'Authentication-Results:',
        '   spf=fail   (cvsu.edu.ph does not allow mailerblast.example)',
        '   dkim=none',
        '   dmarc=fail (p=reject)'
      ],
      hot: [1, 4, 6],
      tells: {
        1: { id: 'replyto', label: 'Reply-To is a free Gmail-style address, not cvsu.edu.ph.' },
        4: { id: 'spf', label: 'SPF/DMARC failed: an outside mailer sent this, not CvSU.' }
      }
    }
  }
};

var MAIL_ORDER = ['e1', 'e2', 'e3', 'e4'];

/* ---------------------------------------------------------------------------
 * Inbox
 * ------------------------------------------------------------------------ */
function renderInbox(mountId) {
  var html = MAIL_ORDER.map(function (key) {
    var m = MAILS[key];
    var seen = Store.get('seen_' + key) === '1';
    return '' +
      '<a class="ticket-row" href="mail.html#' + m.id + '">' +
        '<span class="sev-bar ' + (seen ? 'seen' : 'new') + '"></span>' +
        '<span class="ticket-body">' +
          '<span class="ticket-meta">' +
            '<span class="ticket-id">' + escapeHtml(m.code) + '</span>' +
            '<span class="seen-chip' + (seen ? '' : ' new') + '">' + (seen ? 'read' : 'new') + '</span>' +
            '<span>' + escapeHtml(m.time) + '</span>' +
          '</span>' +
          '<span class="ticket-title">' + escapeHtml(m.subject) + '</span>' +
          '<span class="ticket-from">' + escapeHtml(m.fromName) + ' &lt;' + escapeHtml(m.fromAddr) + '&gt;</span>' +
        '</span>' +
      '</a>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
}

/* ---------------------------------------------------------------------------
 * Message view
 * ------------------------------------------------------------------------ */
var tagMode = false;
var foundTells = {};

function currentMail() {
  var key = (window.location.hash || '#e1').substring(1);
  return MAILS[key] || MAILS.e1;
}

function renderMail(mountId) {
  var m = currentMail();
  Store.set('seen_' + m.id, '1');
  Cyberity.clueFound('mail_opened_' + m.id);
  tagMode = false;
  foundTells = {};

  var html =
    '<div class="mail-card">' +
      '<h2 id="subject-line">' + escapeHtml(m.subject) + '</h2>' +
      '<div class="mail-field"><span class="mail-label">From</span>' +
        '<span><b>' + escapeHtml(m.fromName) + '</b><br>' +
        '<span class="mono small">' + escapeHtml(m.fromAddr) + '</span></span></div>' +
      '<div class="mail-field"><span class="mail-label">To</span>' +
        '<span class="mono small">' + escapeHtml(m.to) + '</span></div>' +
      '<div class="mail-field"><span class="mail-label">Sent</span><span>' + escapeHtml(m.time) + '</span></div>' +
      '<div class="mail-body" id="mail-body">' + renderBody(m) + '</div>' +
    '</div>';

  // Per-message bench tool
  if (m.id === 'e1' || m.id === 'e3') {
    html += '<div class="bench-note">Tap the blue link to reveal where it actually goes. It will not open.</div>';
  } else if (m.id === 'e2') {
    html += '<a class="cta" href="lookup.html">OPEN DOMAIN LOOKUP</a>';
  } else if (m.id === 'e4') {
    html += '<button class="cta" id="analyze-btn" onclick="analyzeHeaders()">RUN HEADER ANALYZER</button>';
    html += '<div id="headers-wrap"></div>';
    html += '<button class="cta" id="tag-btn" onclick="toggleTagMode()" style="display:none">TURN ON TAG MODE</button>';
    html += '<div class="tag-status" id="tag-status" style="display:none"></div>';
    html += '<div class="banner" id="report-banner"></div>';
  }

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('mail-code').textContent = m.code;
  window.scrollTo(0, 0);
}

function renderBody(m) {
  return m.body.map(function (part, i) {
    if (typeof part === 'string') return '<p>' + escapeHtml(part) + '</p>';
    if (part.link) {
      return '<span class="mail-link" onclick="revealLink(this)" ' +
        'data-real="' + escapeHtml(part.real) + '" data-clue="' + (part.clue || '') + '">' +
        escapeHtml(part.link) + '</span>' +
        '<div class="link-target">Really goes to: <span class="mono">' + escapeHtml(part.real) +
        '</span><br>Links are disabled in this simulation.</div>';
    }
    if (part.attachment) {
      return '<div class="attachment" onclick="runAttachment()">' +
        '<span class="att-icon">📎</span>' +
        '<span data-tell="attach"><span class="att-name">' + escapeHtml(part.attachment) + '</span><br>' +
        '<span class="att-meta">tap to open — be careful</span></span></div>';
    }
    if (part.text) {
      var tid = part.tell ? part.tell.id : '';
      return '<p data-tell="' + tid + '">' + escapeHtml(part.text) + '</p>';
    }
    return '';
  }).join('');
}

/** Reveals a link's real destination; never opens it. */
function revealLink(el) {
  var target = el.nextElementSibling;
  if (target) target.classList.add('show');
  var clue = el.getAttribute('data-clue');
  if (clue) Cyberity.clueFound(clue);
}

/** The dangerous action: running the .pdf.exe. */
function runAttachment() {
  if (tagMode) { tagTell('attach'); return; }
  var body = document.getElementById('mail-body');
  var warn = document.createElement('div');
  warn.className = 'banner show bad';
  warn.textContent = 'Blocked by the simulation. A file ending in .exe is a program. Opening ' +
    'it on a real phone or PC would run the attacker\'s code, not show a statement.';
  body.appendChild(warn);
  Cyberity.clueFound('attachment_run');
}

/* ---------------------------------------------------------------------------
 * E4 · header analyzer + tag mode
 * ------------------------------------------------------------------------ */
function analyzeHeaders() {
  var m = MAILS.e4;
  var wrap = document.getElementById('headers-wrap');
  wrap.innerHTML =
    '<div class="rows" id="header-rows">' +
      m.headers.rows.map(function (line, i) {
        var cls = m.headers.hot.indexOf(i) !== -1 ? 'hot' : '';
        var tell = m.headers.tells[i] ? ' data-tell="' + m.headers.tells[i].id + '"' : '';
        return '<span class="' + cls + '"' + tell + '>' + escapeHtml(line) + '</span>';
      }).join('\n') +
    '</div>' +
    '<div class="note">The From line is typed by the sender. The authentication results are ' +
      'added by the receiving mail server and can\'t be faked by the sender.</div>';
  document.getElementById('analyze-btn').textContent = 'HEADERS ANALYZED';
  document.getElementById('tag-btn').style.display = 'block';
  Cyberity.clueFound('headers_analyzed_e4');
}

function toggleTagMode() {
  tagMode = !tagMode;
  document.getElementById('tag-btn').textContent = tagMode ? 'TAG MODE ON — TAP THE TELLS' : 'TURN ON TAG MODE';
  document.body.classList.toggle('tagging', tagMode);
  var status = document.getElementById('tag-status');
  status.style.display = tagMode ? 'block' : 'none';
  updateTagStatus();
}

/** Total tells across body, greeting and headers. */
var TOTAL_TELLS = 6;
var TELL_LABELS = {
  threat: 'Threat and false urgency',
  channel: 'Pay by GCash reply',
  attach: '.pdf.exe attachment',
  greeting: 'Generic "Dear Student"',
  replyto: 'Reply-To is free webmail',
  spf: 'SPF / DMARC failed'
};

// Delegated tap handling for anything carrying a data-tell.
document.addEventListener('click', function (e) {
  if (!tagMode) return;
  var el = e.target.closest('[data-tell]');
  if (!el) return;
  var id = el.getAttribute('data-tell');
  if (!id) return;
  e.preventDefault();
  e.stopPropagation();
  tagTell(id, el);
}, true);

function tagTell(id, el) {
  if (!id || foundTells[id]) return;
  foundTells[id] = true;
  if (el) el.classList.add('tagged');
  // The generic greeting lives on the first body paragraph.
  updateTagStatus();

  if (Object.keys(foundTells).length >= TOTAL_TELLS) {
    var banner = document.getElementById('report-banner');
    banner.className = 'banner show good';
    banner.innerHTML = 'All six tells tagged. Report code: <b class="mono">CYBERITY{s1x_t3lls_sp0tt3d}</b>';
    Cyberity.clueFound('all_tells_e4');
    Cyberity.flagDiscovered('all_tells');
  }
}

function updateTagStatus() {
  var status = document.getElementById('tag-status');
  if (!status) return;
  var n = Object.keys(foundTells).length;
  var names = Object.keys(foundTells).map(function (k) { return TELL_LABELS[k] || k; });
  status.innerHTML = '<b>' + n + ' / ' + TOTAL_TELLS + ' tells found</b>' +
    (names.length ? '<br><span class="small">' + escapeHtml(names.join(' · ')) + '</span>' : '');
}

/* ---------------------------------------------------------------------------
 * E2 · domain lookup
 * ------------------------------------------------------------------------ */
function renderLookup(mountId) {
  Cyberity.clueFound('domain_lookup_e2');
  var fake = 'Iogin-bpi.com';
  var real = 'login-bpi.com';
  document.getElementById(mountId).innerHTML =
    '<div class="lookup-card">' +
      '<div class="lookup-h">Sender domain (from E2)</div>' +
      '<div class="mono lookup-dom">' + escapeHtml(fake) + '</div>' +
      charBreakdown(fake, 0) +
      '<div class="lookup-meta warn">Registered: 3 days ago · Registrant: hidden</div>' +
    '</div>' +
    '<div class="lookup-card">' +
      '<div class="lookup-h">The bank\'s real domain</div>' +
      '<div class="mono lookup-dom">' + escapeHtml(real) + '</div>' +
      charBreakdown(real, 0) +
      '<div class="lookup-meta ok">Registered: 21 years ago · Registrant: Bank of the Philippine Islands</div>' +
    '</div>' +
    '<div class="note">Same width on screen, different first character. A capital i (I) stands ' +
      'in for a lowercase L (l). The brand-new registration date is the second giveaway.</div>';
}

function charBreakdown(domain, hotIndex) {
  var chars = domain.split('').map(function (c, i) {
    var name = charName(c);
    var cls = i === hotIndex ? 'char hot' : 'char';
    return '<span class="' + cls + '"><span class="ch">' + escapeHtml(c) + '</span>' +
      '<span class="cn">' + name + '</span></span>';
  }).join('');
  return '<div class="charmap">' + chars + '</div>';
}

function charName(c) {
  if (c === 'I') return 'cap I';
  if (c === 'l') return 'low l';
  if (c === '.') return 'dot';
  if (c === '-') return 'dash';
  return c;
}
