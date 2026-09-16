/* Cyberity — Inbox Triage simulation.
 * Everything here is local. No network calls, no real credentials, no storage.
 */

/* ---------------------------------------------------------------------------
 * Bridge to Android. AndroidLab is injected by LabScreen; if it's missing
 * (e.g. previewing these files in a desktop browser) every call is a no-op,
 * so the simulation still runs.
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
    emailOpened: function (id) { safe(function () { AndroidLab.notifyEmailOpened(id); }); },
    urlInspected: function (url) { safe(function () { AndroidLab.notifyUrlInspected(url); }); },
    flagDiscovered: function (token) { safe(function () { AndroidLab.notifyFlagDiscovered(token); }); }
  };
})();

/* ---------------------------------------------------------------------------
 * Mailbox contents
 * ------------------------------------------------------------------------ */
var EMAILS = {
  it: {
    id: 'it',
    sender: 'IT Support',
    address: 'ithelpdesk@cvsu.edu.ph',
    subject: 'Scheduled password reset — 30 September',
    time: '9:42 AM',
    preview: 'Campus accounts rotate passwords at the end of the term.',
    senderNote: 'Sending domain matches the organisation named in the message.',
    senderBad: false,
    headers: [
      ['Return-Path', 'ithelpdesk@cvsu.edu.ph'],
      ['Reply-To', 'ithelpdesk@cvsu.edu.ph'],
      ['Received from', 'mail.cvsu.edu.ph'],
      ['SPF', 'pass']
    ],
    body: [
      'Hi,',
      'As part of the end-of-term maintenance window, all campus accounts will be asked to set a new password on 30 September. No action is needed before then.',
      'You will change your password from the campus portal you normally use. We will never send you a link to do it, and we will never ask you for your current password.',
      'IT Helpdesk, Cavite State University'
    ]
  },

  hr: {
    id: 'hr',
    sender: 'HR Department',
    address: 'hr.office@cvsu.edu.ph',
    subject: 'Updated student employment policy',
    time: '9:31 AM',
    preview: 'The revised handbook takes effect next semester.',
    senderNote: 'Sending domain matches the organisation named in the message.',
    senderBad: false,
    headers: [
      ['Return-Path', 'hr.office@cvsu.edu.ph'],
      ['Reply-To', 'hr.office@cvsu.edu.ph'],
      ['Received from', 'mail.cvsu.edu.ph'],
      ['SPF', 'pass']
    ],
    body: [
      'Good morning,',
      'The student employment handbook has been revised for the coming semester. Section 4 (working hours during exam weeks) is the part most student assistants will care about.',
      'Printed copies are available at the HR office. There is nothing to sign and nothing to return by email.',
      'Human Resources'
    ]
  },

  m365: {
    id: 'm365',
    sender: 'Microsoft 365 Security',
    address: 'security@micr0soft-support.example',
    subject: 'URGENT: Your account will be suspended',
    time: '9:15 AM',
    preview: 'Verify within 24 hours or access will be permanently removed.',
    senderNote: 'The display name says Microsoft. The domain does not belong to Microsoft — read it character by character.',
    senderBad: true,
    headers: [
      ['Return-Path', 'bounce@mail-relay-77.example'],
      ['Reply-To', 'no-reply@micr0soft-support.example'],
      ['Received from', 'vps-77-214.hosting.example'],
      ['SPF', 'fail']
    ],
    body: [
      'Dear User,',
      'Unusual sign-in activity was detected on your Microsoft 365 account. For your protection, access has been limited.',
      'Your account will be permanently suspended within 24 hours unless you verify your identity now. All mail and files will be lost.',
      'Microsoft 365 Security Team'
    ],
    cta: { label: 'VERIFY ACCOUNT', target: 'browser.html' }
  },

  fin: {
    id: 'fin',
    sender: 'Finance Department',
    address: 'finance@cvsu.edu.ph',
    subject: 'Statement of account attached',
    time: '8:54 AM',
    preview: 'Your statement for the first semester is attached.',
    senderNote: 'Sending domain matches the organisation named in the message.',
    senderBad: false,
    headers: [
      ['Return-Path', 'finance@cvsu.edu.ph'],
      ['Reply-To', 'finance@cvsu.edu.ph'],
      ['Received from', 'mail.cvsu.edu.ph'],
      ['SPF', 'pass']
    ],
    body: [
      'Good day,',
      'Your statement of account for the first semester is attached as a PDF. Settle any balance at the cashier before the enrolment deadline.',
      'Finance Office'
    ],
    attachment: {
      name: 'statement_1st_sem.pdf',
      size: '184 KB',
      type: 'PDF document',
      note: 'Single extension, ordinary document type, and it came from the domain that matches the sender. Nothing here executes.',
      bad: false
    }
  }
};

var MAIL_ORDER = ['it', 'hr', 'm365', 'fin'];

/* ---------------------------------------------------------------------------
 * Rendering
 * ------------------------------------------------------------------------ */

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function renderInbox(mountId) {
  var mount = document.getElementById(mountId);
  var html = '';
  MAIL_ORDER.forEach(function (key) {
    var m = EMAILS[key];
    html +=
      '<a class="mail-item" href="email.html#' + m.id + '">' +
        '<span class="mail-dot"></span>' +
        '<span class="mail-body">' +
          '<span class="mail-row">' +
            '<span class="mail-sender">' + escapeHtml(m.sender) + '</span>' +
            '<span class="mail-time">' + escapeHtml(m.time) + '</span>' +
          '</span>' +
          '<span class="mail-subject">' + escapeHtml(m.subject) + '</span>' +
          '<span class="mail-preview">' + escapeHtml(m.preview) + '</span>' +
        '</span>' +
      '</a>';
  });
  mount.innerHTML = html;
}

function toggleReveal(id, onFirstOpen) {
  var el = document.getElementById(id);
  var first = !el.classList.contains('open');
  el.classList.toggle('open');
  if (first && typeof onFirstOpen === 'function') onFirstOpen();
}

function renderMessage(mountId) {
  var id = (window.location.hash || '#it').substring(1);
  var m = EMAILS[id] || EMAILS.it;

  Cyberity.emailOpened(m.id);

  var html = '';

  html +=
    '<div class="field" onclick="onSenderInspect(\'' + m.id + '\')">' +
      '<div class="field-label">From</div>' +
      '<div class="field-value">' + escapeHtml(m.sender) + '</div>' +
      '<div class="field-hint">Tap to expand the real address</div>' +
      '<div class="reveal" id="rev-sender">' +
        '<div class="reveal-box ' + (m.senderBad ? '' : 'ok') + '">' +
          '<div class="mono">' + escapeHtml(m.address) + '</div>' +
          '<div style="margin-top:6px">' + escapeHtml(m.senderNote) + '</div>' +
          '<span class="tag ' + (m.senderBad ? 'bad' : 'good') + '">' +
            (m.senderBad ? 'Domain mismatch' : 'Domain matches') +
          '</span>' +
        '</div>' +
      '</div>' +
    '</div>';

  html += '<h2>' + escapeHtml(m.subject) + '</h2>';

  m.body.forEach(function (p) { html += '<p>' + escapeHtml(p) + '</p>'; });

  if (m.cta) {
    html += '<button class="cta" onclick="onCtaClick(\'' + m.cta.target + '\')">' +
      escapeHtml(m.cta.label) + '</button>';
    html += '<div class="field-hint">Opens in the sandboxed browser.</div>';
  }

  if (m.attachment) {
    html +=
      '<div class="field" style="margin-top:14px" onclick="onAttachmentInspect()">' +
        '<div class="field-label">Attachment</div>' +
        '<div class="field-value mono">' + escapeHtml(m.attachment.name) + '</div>' +
        '<div class="field-hint">Tap to inspect the file</div>' +
        '<div class="reveal" id="rev-attach">' +
          '<div class="reveal-box ' + (m.attachment.bad ? '' : 'ok') + '">' +
            '<div>' + escapeHtml(m.attachment.type) + ' · ' + escapeHtml(m.attachment.size) + '</div>' +
            '<div style="margin-top:6px">' + escapeHtml(m.attachment.note) + '</div>' +
          '</div>' +
        '</div>' +
      '</div>';
  }

  var rows = '';
  m.headers.forEach(function (h) {
    rows += '<tr><td>' + escapeHtml(h[0]) + '</td><td class="mono">' + escapeHtml(h[1]) + '</td></tr>';
  });

  html +=
    '<button class="link-btn" onclick="onHeadersToggle()">Show message headers</button>' +
    '<div class="reveal" id="rev-headers">' +
      '<table class="headers-table">' + rows + '</table>' +
    '</div>';

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('msg-title').textContent = m.sender;
  window.CURRENT_EMAIL = m;
}

/* Handlers referenced by the generated markup. */

function onSenderInspect(id) {
  toggleReveal('rev-sender', function () {
    if (id === 'm365') Cyberity.clueFound('sender_inspected');
  });
}

function onHeadersToggle() {
  toggleReveal('rev-headers', function () {
    Cyberity.clueFound('headers_expanded');
  });
}

function onAttachmentInspect() {
  toggleReveal('rev-attach', function () {
    Cyberity.clueFound('attachment_inspected');
  });
}

function onCtaClick(target) {
  Cyberity.clueFound('link_followed');
  window.location.href = target;
}
