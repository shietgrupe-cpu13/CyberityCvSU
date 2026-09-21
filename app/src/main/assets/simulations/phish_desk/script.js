/* Cyberity — ITSO Phish Report Desk simulation (level 301).
 * Local only. No network, no real accounts, no real people.
 * Every address and link here is fictional; links never load.
 */

/* ---------------------------------------------------------------------------
 * Bridge to Android. Same contract as the other labs: AndroidLab is injected
 * by LabScreen, and every call is a no-op without it.
 * ------------------------------------------------------------------------ */
var Cyberity = (function () {
  function safe(fn) {
    try {
      if (typeof AndroidLab !== 'undefined') fn();
    } catch (e) {
      /* preview mode */
    }
  }
  return {
    clueFound: function (id) { safe(function () { AndroidLab.notifyClueFound(id); }); },
    flagDiscovered: function (token) { safe(function () { AndroidLab.notifyFlagDiscovered(token); }); }
  };
})();

/* Read / unread state.
 *
 * Every page here is a real navigation, so this has to survive a page load.
 * sessionStorage is the primary store; window.name follows the frame across
 * navigations and covers the case where DOM storage is unavailable. Both are
 * tied to this WebView, so re-opening the lab correctly starts fresh.
 */
var Store = {
  KEY: 'phish_read',

  raw: function () {
    var raw = null;
    try { raw = window.sessionStorage.getItem(Store.KEY); } catch (e) { raw = null; }
    if (raw === null) {
      var tag = Store.KEY + '=';
      raw = window.name.indexOf(tag) === 0 ? window.name.substring(tag.length) : '';
    }
    return raw || '';
  },

  has: function (id) {
    return Store.raw().split(',').indexOf(id) !== -1;
  },

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
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/* ---------------------------------------------------------------------------
 * Reported messages
 * rows use the same hot / ok / warn highlighting as the other labs.
 * ------------------------------------------------------------------------ */
var REPORTS = {
  r1: {
    id: 'r1',
    code: 'R1',
    reportedBy: '37 students',
    time: 'Mon 08:02',
    fromName: 'GCash Advisory',
    fromAddr: 'advisory@gcash-ph-alerts.example',
    to: 'undisclosed-recipients (4,212)',
    subject: 'Your GCash wallet will be suspended in 24 hours',
    body: [
      'Dear Customer,',
      'We detected unusual activity on your account. To avoid permanent suspension, ' +
        'verify your identity within 24 hours.',
      'LINK:Verify my account|hxxps://gcash-ph-alerts.example/verify|r1',
      'Failure to verify will result in loss of your remaining balance.',
      'GCash Security Team'
    ],
    headers: {
      rows: [
        'From       : GCash Advisory <advisory@gcash-ph-alerts.example>',
        'Reply-To   : advisory@gcash-ph-alerts.example',
        'To         : undisclosed-recipients (4,212 addresses)',
        'Received   : from bulk-mta-12.sender-pool.example'
      ],
      hot: [0, 2]
    },
    gateway: {
      rows: [
        'SPF        : pass  (gcash-ph-alerts.example)',
        'DKIM       : none',
        'Links      : 1 · domain registered 2 days ago',
        'Attachment : none',
        'Verdict    : SUSPICIOUS · delivered with warning banner'
      ],
      warn: [2, 4],
      note: 'SPF only proves the mail came from the domain it claims. It doesn\'t prove ' +
        'that domain belongs to GCash.'
    }
  },

  r2: {
    id: 'r2',
    code: 'R2',
    reportedBy: 'J. Villanueva (BSIT 4)',
    time: 'Mon 21:47',
    fromName: 'Ma. Santos',
    fromAddr: 'm.santos@cvsu.edu.ph',
    to: 'j.villanueva@cvsu.edu.ph',
    subject: 'Re: Chapter 3 revisions',
    body: [
      'Hi Jasmine,',
      'I finished reviewing Chapter 3 of "Smart Irrigation Monitoring for Indang Farms". ' +
        'The panel wants the revisions before your defense on the 26th, so please check my ' +
        'comments tonight.',
      'LINK:Open revision comments|hxxps://docs-review.cvsu-share.example/ch3|r2',
      'You\'ll need to sign in with your CvSU account to see the comments.',
      'Ma\'am Santos'
    ],
    headers: {
      rows: [
        'From       : Ma. Santos <m.santos@cvsu.edu.ph>',
        'Reply-To   : m.santos@cvsu.edu.ph',
        'To         : j.villanueva@cvsu.edu.ph',
        'Received   : from mail.cvsu.edu.ph · sign-in from 102.89.34.7 (outside PH)'
      ],
      warn: [3]
    },
    gatewayClue: 'gateway_opened_r2',
    gateway: {
      rows: [
        'SPF        : pass  (sent by m.santos@cvsu.edu.ph)',
        'DKIM       : pass  (signed by cvsu.edu.ph)',
        'Links      : 1 · docs-review.cvsu-share.example · not on any blocklist',
        'Attachment : none',
        'Verdict    : CLEAN · delivered'
      ],
      ok: [0, 1, 4],
      warn: [2],
      note: 'Every check passed because the adviser\'s real account sent it. Her password was ' +
        'phished last week, and the attacker is now using her mailbox.'
    }
  },

  r3: {
    id: 'r3',
    code: 'R3',
    reportedBy: 'Campus Administrator',
    time: 'Tue 10:15',
    fromName: 'Office of the University President',
    fromAddr: 'president.office@cvsu-edu.ph.example',
    to: 'campus.admin@cvsu.edu.ph',
    subject: 'Confidential payment request',
    body: [
      'Good morning,',
      'I am in the accreditation meeting and cannot take calls. We need to settle the ' +
        'accreditation consultant\'s fee of ₱385,000 before 3:00 PM today or we lose our slot.',
      'Please process the transfer to the account below and reply to confirm once done. ' +
        'Keep this confidential until the announcement.',
      'BOX:Account name: Quantum Accreditation Partners · Account no: 0031-7782-9910',
      'Thank you for acting quickly.'
    ],
    headersClue: 'headers_opened_r3',
    headers: {
      rows: [
        'From       : Office of the University President',
        '             <president.office@cvsu-edu.ph.example>',
        'Reply-To   : pres.office.cvsu@gmail.example',
        'To         : campus.admin@cvsu.edu.ph',
        'Received   : from smtp.freemail.example'
      ],
      hot: [1, 2],
      note: 'The domain is cvsu-edu.ph.example, not cvsu.edu.ph. Any reply goes to a free ' +
        'webmail address.'
    },
    gateway: {
      rows: [
        'SPF        : pass  (cvsu-edu.ph.example)',
        'DKIM       : pass  (cvsu-edu.ph.example)',
        'Links      : none',
        'Attachment : none',
        'Verdict    : CLEAN · delivered'
      ],
      ok: [4],
      note: 'No link and no attachment means nothing for the scanners to analyse.'
    }
  },

  r4: {
    id: 'r4',
    code: 'R4',
    reportedBy: 'K. Mendoza (BSCS 2)',
    time: 'Wed 07:30',
    fromName: 'Office of the Registrar',
    fromAddr: 'registrar@cvsu.edu-ph.example',
    to: 'students-2s@cvsu.edu.ph',
    subject: 'RE-SENT: Enrollment schedule, 2nd semester (corrected attachment)',
    body: [
      'Dear Students,',
      'The attachment in yesterday\'s announcement had an error. Please use the corrected ' +
        'schedule below. The previous file is no longer valid.',
      'Enrollment runs from October 6 to 10 at the Registrar and online through the portal.',
      'Office of the Registrar'
    ],
    attachment: { name: 'enrollment_schedule_2S.pdf.html', size: '14 KB', preview: 'r4' },
    headers: {
      rows: [
        'From       : Office of the Registrar <registrar@cvsu.edu-ph.example>',
        'Reply-To   : registrar@cvsu.edu-ph.example',
        'To         : students-2s@cvsu.edu.ph',
        'Received   : from vps-203-88.hosting.example'
      ],
      hot: [0]
    },
    gateway: {
      rows: [
        'SPF        : pass  (cvsu.edu-ph.example)',
        'DKIM       : none',
        'Links      : none in the body',
        'Attachment : enrollment_schedule_2S.pdf.html · no malware found',
        'Verdict    : CLEAN · delivered'
      ],
      warn: [3],
      note: 'An .html file isn\'t a program, so antivirus has nothing to flag. It just opens ' +
        'in a browser.'
    }
  },

  r5: {
    id: 'r5',
    code: 'R5',
    reportedBy: 'P. Garcia (BSED 1)',
    time: 'Tue 16:05',
    fromName: 'Office of the Registrar',
    fromAddr: 'registrar@cvsu.edu.ph',
    to: 'students-2s@cvsu.edu.ph',
    subject: 'Enrollment schedule, 2nd semester',
    body: [
      'Dear Students,',
      'Attached is the enrollment schedule for the 2nd semester, AY 2026-2027. Enrollment ' +
        'runs from October 6 to 10 at the Registrar and online through the portal.',
      'Sign in through the portal you normally use. The Registrar will never email you a ' +
        'sign-in link.',
      'Office of the Registrar'
    ],
    attachment: { name: 'enrollment_schedule_2S.pdf', size: '212 KB', preview: 'r5' },
    headers: {
      rows: [
        'From       : Office of the Registrar <registrar@cvsu.edu.ph>',
        'Reply-To   : registrar@cvsu.edu.ph',
        'To         : students-2s@cvsu.edu.ph',
        'Received   : from mail.cvsu.edu.ph'
      ],
      ok: [0, 3]
    },
    gateway: {
      rows: [
        'SPF        : pass  (cvsu.edu.ph)',
        'DKIM       : pass  (signed by cvsu.edu.ph)',
        'Links      : none',
        'Attachment : enrollment_schedule_2S.pdf · no malware found',
        'Verdict    : CLEAN · delivered'
      ],
      ok: [0, 1, 3, 4]
    }
  }
};

var REPORT_ORDER = ['r1', 'r2', 'r3', 'r4', 'r5'];

/* ---------------------------------------------------------------------------
 * Queue
 * ------------------------------------------------------------------------ */
function renderQueue(mountId) {
  var html = REPORT_ORDER.map(function (key) {
    var r = REPORTS[key];
    var read = Store.has(key);
    return '' +
      '<a class="ticket-row ' + (read ? 'read' : 'unread') + '" href="report.html#' + r.id + '">' +
        '<span class="sev-bar ' + (read ? 'seen' : 'new') + '"></span>' +
        '<span class="ticket-body">' +
          '<span class="ticket-meta">' +
            '<span class="ticket-id">' + escapeHtml(r.code) + '</span>' +
            '<span class="kind">EMAIL</span>' +
            '<span style="margin-left:auto">' + escapeHtml(r.time) + '</span>' +
          '</span>' +
          '<span class="ticket-title">' + escapeHtml(r.subject) + '</span>' +
          '<span class="ticket-from">From ' + escapeHtml(r.fromName) +
            ' · reported by ' + escapeHtml(r.reportedBy) + '</span>' +
          '<span class="mail-badge">' + (read ? 'READ' : 'NOT READ YET') + '</span>' +
        '</span>' +
      '</a>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;

  var left = REPORT_ORDER.filter(function (k) { return !Store.has(k); }).length;
  var counter = document.getElementById('unread-count');
  if (counter) {
    counter.textContent = left === 0
      ? 'All five read — compare them before you decide'
      : left + ' of 5 still unread';
  }
}

/* ---------------------------------------------------------------------------
 * Report (message view)
 * ------------------------------------------------------------------------ */
function renderRows(ev) {
  return ev.rows.map(function (line, i) {
    var cls = '';
    if (ev.hot && ev.hot.indexOf(i) !== -1) cls = 'hot';
    if (ev.ok && ev.ok.indexOf(i) !== -1) cls = 'ok';
    if (ev.warn && ev.warn.indexOf(i) !== -1) cls = 'warn';
    var safe = escapeHtml(line);
    return cls ? '<span class="' + cls + '">' + safe + '</span>' : safe;
  }).join('\n');
}

function renderBodyLine(line) {
  if (line.indexOf('LINK:') === 0) {
    var parts = line.substring(5).split('|');
    var label = parts[0], shown = parts[1], site = parts[2];
    return '<a class="mail-link" href="browser.html#' + site + '">' + escapeHtml(label) + '</a>' +
      '<div class="link-target">Opens a safe copy of the page in the sandbox browser.<br>' +
      'Address: <span class="mono">' + escapeHtml(shown) + '</span></div>';
  }
  if (line.indexOf('BOX:') === 0) {
    return '<div class="mail-box">' + escapeHtml(line.substring(4)) + '</div>';
  }
  return '<p>' + escapeHtml(line) + '</p>';
}

function currentReport() {
  var key = (window.location.hash || '#r1').substring(1);
  return REPORTS[key] || REPORTS.r1;
}

function evidenceBox(boxId, name, sub, ev, clue) {
  return '' +
    '<div class="evidence" id="' + boxId + '">' +
      '<div class="evidence-head" onclick="toggleEvidence(\'' + boxId + '\', \'' + (clue || '') + '\')">' +
        '<span>' +
          '<span class="evidence-name">' + escapeHtml(name) + '</span>' +
          '<span class="evidence-sub">' + escapeHtml(sub) + '</span>' +
        '</span>' +
        '<span class="evidence-caret">expand</span>' +
      '</div>' +
      '<div class="evidence-body">' +
        '<div class="rows">' + renderRows(ev) + '</div>' +
        (ev.note ? '<div class="note">' + escapeHtml(ev.note) + '</div>' : '') +
      '</div>' +
    '</div>';
}

function renderReport(mountId) {
  var r = currentReport();

  Store.mark(r.id);
  Cyberity.clueFound('report_opened_' + r.id);

  var html =
    '<div class="mail-card">' +
      '<h2>' + escapeHtml(r.subject) + '</h2>' +
      '<div class="mail-field"><span class="mail-label">From</span>' +
        '<span><b>' + escapeHtml(r.fromName) + '</b><br>' +
        '<span class="mono small">' + escapeHtml(r.fromAddr) + '</span></span></div>' +
      '<div class="mail-field"><span class="mail-label">To</span>' +
        '<span class="mono small">' + escapeHtml(r.to) + '</span></div>' +
      '<div class="mail-field"><span class="mail-label">Sent</span><span>' + escapeHtml(r.time) + '</span></div>' +
      '<div class="mail-body">' + r.body.map(renderBodyLine).join('') + '</div>';

  if (r.attachment) {
    html +=
      '<a class="attachment" href="preview.html#' + r.attachment.preview + '">' +
        '<span class="att-icon">📎</span>' +
        '<span><span class="att-name">' + escapeHtml(r.attachment.name) + '</span><br>' +
        '<span class="att-meta">' + escapeHtml(r.attachment.size) + ' · tap to preview in sandbox</span></span>' +
      '</a>';
  }
  html += '</div>';

  html += '<div class="section-label">Investigation</div>';
  html += evidenceBox('ev-headers', 'Full headers', 'Where the message really came from, and where replies go',
    r.headers, r.headersClue);
  html += evidenceBox('ev-gateway', 'Mail gateway scan', 'What the filters checked before delivering it',
    r.gateway, r.gatewayClue);

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('report-code').textContent = r.code + ' · reported by ' + r.reportedBy;
  window.scrollTo(0, 0);
}

/** Expands/collapses an evidence box; reports [clue] the first time it opens. */
function toggleEvidence(boxId, clue) {
  var box = document.getElementById(boxId);
  var first = !box.classList.contains('open') && box.getAttribute('data-seen') !== '1';
  box.classList.toggle('open');
  box.querySelector('.evidence-caret').textContent =
    box.classList.contains('open') ? 'collapse' : 'expand';
  if (first) {
    box.setAttribute('data-seen', '1');
    if (clue) Cyberity.clueFound(clue);
  }
}

/* ---------------------------------------------------------------------------
 * Sandbox preview of attachments
 * ------------------------------------------------------------------------ */
function renderPreview() {
  var key = (window.location.hash || '#r4').substring(1);
  var fake = document.getElementById('preview-fake');
  var real = document.getElementById('preview-real');
  var back = document.getElementById('back-link');

  if (key === 'r5') {
    fake.style.display = 'none';
    real.style.display = 'block';
    back.setAttribute('onclick', "location.href='report.html#r5'");
    document.getElementById('preview-file').textContent = 'enrollment_schedule_2S.pdf';
  } else {
    fake.style.display = 'block';
    real.style.display = 'none';
    back.setAttribute('onclick', "location.href='report.html#r4'");
    document.getElementById('preview-file').textContent = 'enrollment_schedule_2S.pdf.html';
    Cyberity.clueFound('attachment_previewed');
  }
}

/** Shows the page code behind the fake sign-in form. */
function inspectPage() {
  var panel = document.getElementById('source');
  if (panel.classList.contains('open')) return;
  panel.classList.add('open');
  panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
  Cyberity.clueFound('page_inspected');
  Cyberity.flagDiscovered('form_hidden_field');
}

/** The dangerous action: handing credentials to any of the fake pages. */
function fakeSignIn(which) {
  var banner = document.getElementById('signin-banner');
  banner.className = 'banner show bad';
  banner.textContent = (SITES[which] && SITES[which].warning) ||
    'Blocked by the simulation. On a real device your CvSU email and password would already ' +
    'be on the attacker\'s server, and the page would quietly send you on to the real ' +
    'schedule so you never notice.';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('credentials_submitted');
}

/* ---------------------------------------------------------------------------
 * Sandbox browser — the pages R1 and R2 actually open
 *
 * Students are meant to look: reading a URL teaches far less than seeing what
 * it serves. Nothing here reaches the network, and a page only calls itself
 * out as hostile once the student tries to sign in, so opening a link never
 * gives away which report is which before they decide.
 * ------------------------------------------------------------------------ */
var SITES = {
  r1: {
    report: 'r1',
    url: 'hxxps://gcash-ph-alerts.example/verify',
    realDomain: 'gcash-ph-alerts.example',
    clue: 'site_visited_r1',
    brand: 'GCash',
    accent: '#0b6b2e',
    title: 'Verify your account to avoid suspension',
    note: 'Wallet verification · expires in 23:41',
    fields: [
      { label: 'Mobile number', type: 'tel', placeholder: '09XX XXX XXXX' },
      { label: 'MPIN', type: 'password', placeholder: '\u2022\u2022\u2022\u2022' }
    ],
    button: 'Verify now',
    warning: 'Blocked by the simulation. On a real phone your mobile number and MPIN would ' +
      'now be on the attacker\'s server, and the wallet could be emptied within minutes. ' +
      'No wallet provider asks for your MPIN on a page you reached from an email.'
  },
  r2: {
    report: 'r2',
    url: 'hxxps://docs-review.cvsu-share.example/ch3',
    realDomain: 'cvsu-share.example',
    clue: 'site_visited_r2',
    brand: 'CvSU Document Review',
    accent: '#005CEB',
    title: 'Sign in to view "Chapter 3 \u2014 revision comments"',
    note: 'Shared with you by m.santos@cvsu.edu.ph',
    fields: [
      { label: 'CvSU email', type: 'email', placeholder: 'name@cvsu.edu.ph' },
      { label: 'Password', type: 'password', placeholder: '\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022' }
    ],
    button: 'Sign in',
    warning: 'Blocked by the simulation. Look at the address again — this page is not part ' +
      'of cvsu.edu.ph. On a real phone your CvSU password would now belong to the attacker, ' +
      'and your account would send the next round of these emails.'
  }
};

function renderBrowser(mountId) {
  var key = (window.location.hash || '#r1').substring(1);
  var site = SITES[key] || SITES.r1;

  Cyberity.clueFound(site.clue);

  var fields = site.fields.map(function (f) {
    return '<label class="fake-label">' + escapeHtml(f.label) + '</label>' +
      '<input class="fake-input" type="' + f.type + '" placeholder="' + escapeHtml(f.placeholder) +
      '" autocomplete="off">';
  }).join('');

  document.getElementById(mountId).innerHTML =
    '<div class="url-bar">' +
      '<div class="url-label">Address of the page you are on</div>' +
      '<div class="mono url-text">' + escapeHtml(site.url) + '</div>' +
      '<div class="url-note">Site owner: <b>' + escapeHtml(site.realDomain) + '</b></div>' +
    '</div>' +
    '<div class="fake-page">' +
      '<div class="fake-bar" style="background:' + site.accent + ';color:#ffffff">' +
        escapeHtml(site.brand) + '</div>' +
      '<div class="fake-inner">' +
        '<div class="fake-head">' + escapeHtml(site.title) + '</div>' +
        '<div class="fake-title">' + escapeHtml(site.note) + '</div>' +
        fields +
        '<button class="fake-btn" style="background:' + site.accent + '" ' +
          'onclick="fakeSignIn(\'' + key + '\')">' + escapeHtml(site.button) + '</button>' +
      '</div>' +
    '</div>' +
    '<div class="banner" id="signin-banner"></div>' +
    '<div class="note">A safe copy — nothing you type is sent anywhere. Look at what the page ' +
      'asks for, and at who is really asking.</div>';

  var back = document.getElementById('back-link');
  if (back) back.setAttribute('onclick', "location.href='report.html#" + site.report + "'");
  var title = document.getElementById('browser-url');
  if (title) title.textContent = site.realDomain;
}
