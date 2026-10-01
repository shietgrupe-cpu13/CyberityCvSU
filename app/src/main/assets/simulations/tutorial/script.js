/* Cyberity — Tutorial practice simulation.
 * Everything here is local. No network calls, no real credentials, no storage
 * beyond which messages have been opened.
 */

/* Bridge to Android. AndroidLab is injected by LabScreen; if it's missing
 * (e.g. previewing these files in a desktop browser) every call is a no-op. */
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
    emailOpened: function (id) { safe(function () { AndroidLab.notifyEmailOpened(id); }); }
  };
})();

/* Which messages have been opened. Every page here is a real navigation, so it
 * has to survive a page load: sessionStorage first, window.name as a fallback. */
var ReadState = (function () {
  var KEY = 'cyberity.tutorial.read';
  var TAG = KEY + '=';

  function load() {
    var raw = null;
    try {
      raw = window.sessionStorage.getItem(KEY);
    } catch (e) {
      raw = null;
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
      /* window.name below is the fallback */
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

/* Mailbox contents: one ordinary message and one that is not. */
var EMAILS = {
  library: {
    id: 'library',
    sender: 'Library Services',
    address: 'library@cvsu.edu.ph',
    subject: 'Library hours during finals week',
    time: '9:20 AM',
    preview: 'The library stays open until 10 PM from Monday to Friday.',
    senderNote: 'Sending domain matches the organisation named in the message.',
    senderBad: false,
    body: [
      'Hi,',
      'During finals week the main library will stay open until 10 PM, Monday to Friday. Study rooms can be booked at the front desk.',
      'There is nothing to click and nothing to reply to. See you there.',
      'Library Services, Cavite State University'
    ]
  },

  portal: {
    id: 'portal',
    sender: 'CvSU Student Portal',
    address: 'support@cvsu-verify.example',
    subject: 'URGENT: Password expires in 2 hours',
    time: '9:05 AM',
    preview: 'Sign in now or your enrollment will be locked.',
    senderNote: 'The display name says CvSU. The domain is not cvsu.edu.ph — it belongs to someone else.',
    senderBad: true,
    body: [
      'Dear Student,',
      'Your student portal password expires in 2 hours. If you do not sign in and confirm it now, your enrollment will be locked and you will lose access to your grades.',
      'CvSU Student Portal Team'
    ],
    cta: { label: 'SIGN IN NOW', target: 'portal.html' }
  }
};

var MAIL_ORDER = ['library', 'portal'];

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function renderMailbox(mountId) {
  var html = '';
  MAIL_ORDER.forEach(function (key) {
    var m = EMAILS[key];
    var isRead = ReadState.has(m.id);

    html +=
      '<a class="choice mail-item ' + (isRead ? 'read' : 'unread') + '" href="message.html#' + m.id + '">' +
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
  document.getElementById(mountId).innerHTML = html;

  var unread = ReadState.unreadCount(MAIL_ORDER);
  document.getElementById('unread-count').textContent = unread === 0
    ? 'student.account@cvsu.edu.ph · all read'
    : 'student.account@cvsu.edu.ph · ' + unread + ' unread';
}

function renderMessage(mountId) {
  var id = (window.location.hash || '#library').substring(1);
  var m = EMAILS[id] || EMAILS.library;

  ReadState.mark(m.id);
  Cyberity.emailOpened(m.id);

  var html =
    '<div class="artifact a-msg">' +
      '<span class="artifact-tag">EMAIL</span>' +
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
      '</div>' +
      '<h2>' + escapeHtml(m.subject) + '</h2>';

  m.body.forEach(function (p) { html += '<p>' + escapeHtml(p) + '</p>'; });

  if (m.cta) {
    html += '<button class="cta" onclick="onCtaClick(\'' + m.cta.target + '\')">' +
      escapeHtml(m.cta.label) + '</button>';
    html += '<div class="field-hint">Opens in the sandboxed browser.</div>';
  }

  html += '</div>';

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('msg-title').textContent = m.sender;
}

function toggleReveal(id, onFirstOpen) {
  var el = document.getElementById(id);
  var first = !el.classList.contains('open');
  el.classList.toggle('open');
  if (first && typeof onFirstOpen === 'function') onFirstOpen();
}

function onSenderInspect(id) {
  toggleReveal('rev-sender', function () {
    if (id === 'portal') Cyberity.clueFound('sender_inspected');
  });
}

function onCtaClick(target) {
  Cyberity.clueFound('link_followed');
  window.location.href = target;
}
