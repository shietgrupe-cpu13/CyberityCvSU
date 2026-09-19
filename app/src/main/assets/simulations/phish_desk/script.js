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

var Store = {
  get: function (key) {
    try { return window.sessionStorage.getItem('phish_' + key); } catch (e) { return null; }
  },
  set: function (key, value) {
    try { window.sessionStorage.setItem('phish_' + key, value); } catch (e) { /* ignore */ }
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
      'LINK:Verify my account|hxxps://gcash-ph-alerts.example/verify',
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
      'LINK:Open revision comments|hxxps://docs-review.cvsu-share.example/ch3',
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
    var seen = Store.get('seen_' + key) === '1';
    return '' +
      '<a class="ticket-row" href="report.html#' + r.id + '">' +
        '<span class="sev-bar ' + (seen ? 'seen' : 'new') + '"></span>' +
        '<span class="ticket-body">' +
          '<span class="ticket-meta">' +
            '<span class="ticket-id">' + escapeHtml(r.code) + '</span>' +
            '<span class="seen-chip' + (seen ? '' : ' new') + '">' + (seen ? 'read' : 'new') + '</span>' +
            '<span>' + escapeHtml(r.time) + '</span>' +
          '</span>' +
          '<span class="ticket-title">' + escapeHtml(r.subject) + '</span>' +
          '<span class="ticket-from">From ' + escapeHtml(r.fromName) +
            ' · reported by ' + escapeHtml(r.reportedBy) + '</span>' +
        '</span>' +
      '</a>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
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
    return '<div class="mail-link" onclick="hoverLink(this)" data-url="' + escapeHtml(parts[1]) + '">' +
      escapeHtml(parts[0]) + '</div>' +
      '<div class="link-target">Link goes to: <span class="mono">' + escapeHtml(parts[1]) +
      '</span><br>Links are disabled in this simulation.</div>';
  }
  if (line.indexOf('BOX:') === 0) {
    return '<div class="mail-box">' + escapeHtml(line.substring(4)) + '</div>';
  }
  return '<p>' + escapeHtml(line) + '</p>';
}

/** Tapping a link never opens it. It only reveals where it points. */
function hoverLink(el) {
  var target = el.nextElementSibling;
  if (target) target.classList.add('show');
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

  Store.set('seen_' + r.id, '1');
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

/** The dangerous action: typing a password into the fake portal. */
function fakeSignIn() {
  var banner = document.getElementById('signin-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation. On a real device, your CvSU email and ' +
    'password would already be on the attacker\'s server, and the page would quietly send ' +
    'you to the real schedule so you never notice.';
  Cyberity.clueFound('credentials_submitted');
}
