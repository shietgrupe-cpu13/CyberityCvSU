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
 * Read / unread state.
 *
 * Every page here is a real navigation, so this has to survive a page load.
 * sessionStorage is the primary store; window.name follows the frame across
 * navigations and covers the case where DOM storage is unavailable. Both are
 * tied to this WebView, so closing the lab and starting it again correctly
 * gives a fresh mailbox with everything unread.
 * ------------------------------------------------------------------------ */
var ReadState = (function () {
  var KEY = 'cyberity.read';
  var TAG = KEY + '=';

  function load() {
    var raw = null;
    try {
      raw = window.sessionStorage.getItem(KEY);
    } catch (e) {
      raw = null; /* DOM storage unavailable — fall through */
    }
    if (raw === null) {
      raw = window.name.indexOf(TAG) === 0 ? window.name.substring(TAG.length) : '';
    }
    return raw.split(',').filter(function (s) { return s.length > 0; });
  }

  function store(ids) {
    var raw = ids.join(',');
    try {
      window.sessionStorage.setItem(KEY, raw);
    } catch (e) {
      /* ignored — window.name below is the fallback */
    }
    window.name = TAG + raw;
  }

  return {
    has: function (id) { return load().indexOf(id) !== -1; },

    mark: function (id) {
      var ids = load();
      if (ids.indexOf(id) === -1) {
        ids.push(id);
        store(ids);
      }
    },

    unreadCount: function (allIds) {
      var ids = load();
      return allIds.filter(function (id) { return ids.indexOf(id) === -1; }).length;
    }
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
  KEY: 'inbox_triage_last',
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

function renderInbox(mountId) {
  var mount = document.getElementById(mountId);
  var html = '';
  MAIL_ORDER.forEach(function (key) {
    var m = EMAILS[key];
    var isRead = ReadState.has(m.id);

    html +=
      '<a class="choice mail-item ' + (isRead ? 'read' : 'unread') + '" href="email.html#' + m.id + '">' +
        '<span class="mail-dot"></span>' +
        '<span class="mail-body">' +
          '<span class="mail-row">' +
            '<span class="mail-sender">' + escapeHtml(m.sender) + '</span>' +
            '<span class="mail-time">' + escapeHtml(m.time) + '</span>' +
          '</span>' +
          '<span class="mail-subject">' + escapeHtml(m.subject) + '</span>' +
          '<span class="mail-preview">' + escapeHtml(m.preview) + '</span>' +
          '<span class="mail-badge">' + (isRead ? 'READ' : 'UNREAD') + '</span>' +
        '</span>' +
      '</a>';
  });
  mount.innerHTML = html;

  updateUnreadCount('unread-count');

  ReturnTo.restore();
}

/* Keeps the "N unread" line in the header honest. */
function updateUnreadCount(countId) {
  var el = document.getElementById(countId);
  if (!el) return;

  var unread = ReadState.unreadCount(MAIL_ORDER);
  el.textContent = unread === 0
    ? 'student.account@cvsu.edu.ph · all read'
    : 'student.account@cvsu.edu.ph · ' + unread + ' unread';
}

function toggleReveal(id, onFirstOpen) {
  var el = document.getElementById(id);
  var first = !el.classList.contains('open');
  el.classList.toggle('open');
  if (first && typeof onFirstOpen === 'function') onFirstOpen();
}

function renderMessage(mountId) {
  ReturnTo.remember((window.location.hash || '').substring(1));
  var id = (window.location.hash || '#it').substring(1);
  var m = EMAILS[id] || EMAILS.it;

  ReadState.mark(m.id);
  Cyberity.emailOpened(m.id);

  // The message itself is evidence: everything the sender controls goes
  // inside one EMAIL frame. Tools that inspect it stay outside.
  var html =
    '<div class="artifact a-msg">' +
      '<span class="artifact-tag">EMAIL</span>';

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

  html += '</div>';   // end of the EMAIL frame

  var rows = '';
  m.headers.forEach(function (h) {
    rows += '<tr><td>' + escapeHtml(h[0]) + '</td><td class="mono">' + escapeHtml(h[1]) + '</td></tr>';
  });

  html +=
    '<button class="link-btn" onclick="onHeadersToggle()">Show message headers</button>' +
    '<div class="reveal" id="rev-headers">' +
      '<div class="artifact a-sys">' +
        '<span class="artifact-tag">MESSAGE HEADERS</span>' +
        '<table class="headers-table">' + rows + '</table>' +
      '</div>' +
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
