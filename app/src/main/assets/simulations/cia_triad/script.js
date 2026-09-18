/* Cyberity — Registrar Incident Desk simulation (level 103).
 * Local only. No network, no real files, no real student data.
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
    ticketOpened: function (id) { safe(function () { AndroidLab.notifyClueFound('ticket_opened_' + id); }); },
    flagDiscovered: function (token) { safe(function () { AndroidLab.notifyFlagDiscovered(token); }); }
  };
})();

/* Small persistence so the desk remembers what the student did while they move
 * between pages. DOM storage is enabled in LabScreen; if it ever isn't, the
 * simulation still works, it just forgets on navigation. */
var Store = {
  get: function (key) {
    try { return window.sessionStorage.getItem('cia_' + key); } catch (e) { return null; }
  },
  set: function (key, value) {
    try { window.sessionStorage.setItem('cia_' + key, value); } catch (e) { /* ignore */ }
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
 * Tickets
 * ------------------------------------------------------------------------ */
var TICKETS = {
  inc301: {
    id: 'inc301',
    code: 'INC-301',
    severity: 'high',
    time: 'Mon 09:12',
    title: 'Enrollment list is circulating online',
    from: 'Guidance Office',
    report: 'A student showed me a link to the full BSIT enrollment masterlist — names, ' +
      'student numbers, and home addresses. She found it posted in a public Facebook group. ' +
      'Nobody outside the Registrar should have that file.',
    evidence: [
      {
        name: 'File details',
        sub: 'What the link points to',
        rows: [
          'file     : enrollment_masterlist_2026.xlsx',
          'owner    : registrar.staff@cvsu.edu.ph',
          'contains : 1,284 students · name, student no., address',
          'sharing  : Anyone with the link · can view'
        ],
        hot: [3],
        note: 'The file itself is intact and online. The problem is who can open it.'
      }
    ],
    tool: { href: 'share.html', label: 'Open file sharing settings' }
  },
  inc302: {
    id: 'inc302',
    code: 'INC-302',
    severity: 'high',
    time: 'Mon 10:40',
    title: 'Submitted grade is different in the portal',
    from: 'Prof. A. Reyes · ITEC 106',
    report: 'I submitted a 2.75 for student 2023-10457 in ITEC 106 last week. The portal ' +
      'now shows 1.25. I never edited it after submitting, and the grade sheet was locked.',
    evidence: [
      {
        name: 'Grade submission receipt',
        sub: 'What the professor submitted',
        rows: [
          'submitted : Wed 09-10 16:22 by a.reyes@cvsu.edu.ph',
          'student   : 2023-10457 · ITEC 106',
          'grade     : 2.75',
          'status    : locked after submission'
        ],
        ok: [2]
      },
      {
        name: 'Portal record',
        sub: 'What the portal shows now',
        rows: [
          'student   : 2023-10457 · ITEC 106',
          'grade     : 1.25'
        ],
        hot: [1],
        note: 'Everyone can still read the record and the portal is up. The value itself is wrong.'
      }
    ],
    tool: { href: 'verify.html', label: 'Open integrity checker' }
  },
  inc303: {
    id: 'inc303',
    code: 'INC-303',
    severity: 'medium',
    time: 'Mon 13:05',
    title: 'Portal will not load — enrollment closes Friday',
    from: 'Registrar Front Desk',
    report: 'Students at the counter say the enrollment portal just spins, then shows an ' +
      'error. Nothing has been leaked or changed as far as we know — nobody can get in at all.',
    evidence: [
      {
        name: 'Student-facing error',
        sub: 'What students see',
        rows: [
          'GET /enroll  ->  502 Bad Gateway',
          'retry after 30s ->  502 Bad Gateway'
        ],
        hot: [0, 1],
        note: 'The web page answers, but whatever sits behind it does not.'
      }
    ],
    tool: { href: 'status.html', label: 'Open service status' }
  }
};

var TICKET_ORDER = ['inc301', 'inc302', 'inc303'];

function renderDesk(mountId) {
  var html = TICKET_ORDER.map(function (key) {
    var t = TICKETS[key];
    var seen = Store.get('seen_' + key) === '1';
    return '' +
      '<a class="ticket-row" href="ticket.html#' + t.id + '">' +
        '<span class="sev-bar ' + t.severity + '"></span>' +
        '<span class="ticket-body">' +
          '<span class="ticket-meta">' +
            '<span class="ticket-id">' + escapeHtml(t.code) + '</span>' +
            '<span class="seen-chip' + (seen ? '' : ' new') + '">' + (seen ? 'read' : 'new') + '</span>' +
            '<span>' + escapeHtml(t.time) + '</span>' +
          '</span>' +
          '<span class="ticket-title">' + escapeHtml(t.title) + '</span>' +
          '<span class="ticket-from">' + escapeHtml(t.from) + '</span>' +
        '</span>' +
      '</a>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
}

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

function renderTicket(mountId) {
  var key = (window.location.hash || '#inc301').substring(1);
  var t = TICKETS[key] || TICKETS.inc301;

  Store.set('seen_' + t.id, '1');
  Cyberity.ticketOpened(t.id);

  var html =
    '<h2>' + escapeHtml(t.title) + '</h2>' +
    '<div class="detail-sub">' + escapeHtml(t.code) + ' · ' + escapeHtml(t.time) +
    ' · ' + escapeHtml(t.from) + '</div>' +
    '<div class="report">' + escapeHtml(t.report) + '</div>';

  t.evidence.forEach(function (ev, index) {
    html +=
      '<div class="evidence" id="ev-' + index + '">' +
        '<div class="evidence-head" onclick="toggleEvidence(\'ev-' + index + '\', \'\')">' +
          '<span>' +
            '<span class="evidence-name">' + escapeHtml(ev.name) + '</span>' +
            '<span class="evidence-sub">' + escapeHtml(ev.sub) + '</span>' +
          '</span>' +
          '<span class="evidence-caret">expand</span>' +
        '</div>' +
        '<div class="evidence-body">' +
          '<div class="rows">' + renderRows(ev) + '</div>' +
          (ev.note ? '<div class="note">' + escapeHtml(ev.note) + '</div>' : '') +
        '</div>' +
      '</div>';
  });

  html += '<a class="cta" href="' + t.tool.href + '">' + escapeHtml(t.tool.label) + '</a>';

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('ticket-code').textContent = t.code;
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
 * INC-301 · File sharing (confidentiality)
 * ------------------------------------------------------------------------ */
var SHARE_MODES = {
  anyone: 'Anyone with the link',
  campus: 'CvSU only',
  restricted: 'Restricted'
};

var pendingShareMode = null;

function initShare() {
  var applied = Store.get('share_mode') || 'anyone';
  pendingShareMode = applied;
  paintShare(applied);
}

function pickShare(mode) {
  pendingShareMode = mode;
  var opts = document.querySelectorAll('.option');
  for (var i = 0; i < opts.length; i++) {
    opts[i].classList.toggle('selected', opts[i].getAttribute('data-mode') === mode);
  }
}

function applyShare() {
  var mode = pendingShareMode || 'anyone';
  Store.set('share_mode', mode);
  paintShare(mode);

  var banner = document.getElementById('share-banner');
  banner.className = 'banner show';

  if (mode === 'restricted') {
    banner.classList.add('good');
    banner.textContent = 'Saved. Only the Registrar staff added to this file can open it. ' +
      'The public link now returns "access denied".';
    Cyberity.clueFound('sharing_restricted');
  } else if (mode === 'campus') {
    banner.classList.add('warn');
    banner.textContent = 'Saved — but every CvSU account, including every student, can still ' +
      'open 1,284 home addresses. That is narrower, not closed.';
  } else {
    banner.classList.add('bad');
    banner.textContent = 'Nothing changed. The link is still public.';
  }
}

function paintShare(mode) {
  pickShare(mode);
  var now = document.getElementById('access-now');
  var locked = mode === 'restricted';
  now.innerHTML = 'General access: <b class="' + (locked ? 'locked' : '') + '">' +
    escapeHtml(SHARE_MODES[mode]) + '</b>';
}

/* ---------------------------------------------------------------------------
 * INC-302 · Integrity checker (integrity)
 * ------------------------------------------------------------------------ */
var RECORD_FILES = [
  {
    name: 'enrollment_masterlist_2026.xlsx',
    baseline: '9f2c6a1e0b7d4c33a8e5f16b2d90c7aa41d0',
    current:  '9f2c6a1e0b7d4c33a8e5f16b2d90c7aa41d0'
  },
  {
    name: 'grades_bsit3a_2026.csv',
    baseline: '5b7e03d9c1a84f62be0d7a915f3ce248c912',
    current:  'a03d88f1e7264b0c95d1f3a6c20e8b4d77f1'
  },
  {
    name: 'grades_bsit3b_2026.csv',
    baseline: 'c41f9e27ab3056d8e19c74b0f6a2d5e3108b',
    current:  'c41f9e27ab3056d8e19c74b0f6a2d5e3108b'
  },
  {
    name: 'scholarship_list_2026.csv',
    baseline: '2e8d71c05f9ab4e6d3c0817f4b2a96e5d0c3',
    current:  '2e8d71c05f9ab4e6d3c0817f4b2a96e5d0c3'
  }
];

function renderVerify(mountId) {
  var html = RECORD_FILES.map(function (f, i) {
    return '' +
      '<div class="hash-row" id="hash-' + i + '">' +
        '<div class="hash-top">' +
          '<span class="mono" style="font-size:12px">' + escapeHtml(f.name) + '</span>' +
          '<button class="small-btn" id="hash-btn-' + i + '" onclick="computeHash(' + i + ')">COMPUTE</button>' +
        '</div>' +
        '<div class="hash-lines">baseline ' + escapeHtml(f.baseline) + '</div>' +
        '<div class="hash-lines" id="hash-out-' + i + '"></div>' +
      '</div>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
}

function computeHash(i) {
  var f = RECORD_FILES[i];
  var btn = document.getElementById('hash-btn-' + i);
  var out = document.getElementById('hash-out-' + i);
  var row = document.getElementById('hash-' + i);

  btn.disabled = true;
  out.textContent = 'hashing…';

  setTimeout(function () {
    var match = f.current === f.baseline;
    row.classList.add(match ? 'match' : 'mismatch');
    out.innerHTML =
      'current  <span class="' + (match ? 'ok' : 'hot') + '">' + escapeHtml(f.current) + '</span>' +
      '<div class="verdict ' + (match ? 'ok' : 'hot') + '">' +
        (match ? 'MATCH · unchanged since baseline' : 'MISMATCH · file changed since baseline') +
      '</div>' +
      (match ? '' : '<button class="small-btn" style="margin-top:8px" onclick="showDiff()">VIEW CHANGES</button>');
    btn.textContent = 'DONE';
    Cyberity.clueFound('integrity_check_run');
  }, 650);
}

function showDiff() {
  var panel = document.getElementById('diff');
  if (panel.classList.contains('open')) return;
  panel.classList.add('open');
  panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
  Cyberity.clueFound('diff_viewed');
  Cyberity.flagDiscovered('audit_trail');
}

/** The dangerous action: trusting whatever is on disk now. */
function rebaseline() {
  var banner = document.getElementById('rebase-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation — but in a real system you just signed the ' +
    'tampered grade as "trusted". The only proof it was changed would be gone.';
  Cyberity.clueFound('baseline_overwritten');
}

/* ---------------------------------------------------------------------------
 * INC-303 · Service status (availability)
 * ------------------------------------------------------------------------ */
var SERVICES = [
  {
    name: 'portal-web',
    role: 'Enrollment website',
    state: 'degraded',
    rows: [
      'requests/min : 410   (normal for enrollment week: 350-500)',
      'upstream     : records-db  ->  connection refused',
      'responses    : 502 Bad Gateway to 100% of /enroll'
    ],
    ok: [0],
    hot: [1, 2],
    note: 'Traffic is ordinary. The website is up; it just can\'t reach its database.'
  },
  {
    name: 'auth',
    role: 'CvSU sign-in',
    state: 'up',
    rows: ['sign-ins/min : 96', 'errors       : 0'],
    ok: [0, 1]
  },
  {
    name: 'records-db',
    role: 'Student records database',
    state: 'down',
    clue: 'disk_usage_opened',
    rows: [
      'status   : stopped · "could not write to file: No space left on device"',
      'disk /   : 50.0 GB of 50.0 GB used (100%)',
      '',
      'largest paths',
      '  /var/log/portal/debug.log   47.9 GB   growing since 09-12',
      '  /var/lib/records/           1.6 GB',
      '  /usr                        0.5 GB',
      '',
      'change log',
      '  09-12 22:00  maintenance: log_level=DEBUG (to be reverted)',
      '  09-12 23:40  maintenance closed'
    ],
    hot: [0, 1, 4],
    warn: [9],
    note: 'The records themselves are only 1.6 GB and intact. Something else ate the disk.'
  },
  {
    name: 'file-share',
    role: 'Registrar documents',
    state: 'up',
    rows: ['storage : 38%', 'errors  : 0'],
    ok: [0, 1]
  }
];

function renderStatus(mountId) {
  var html = SERVICES.map(function (s, i) {
    var label = s.state === 'up' ? 'UP' : (s.state === 'down' ? 'DOWN' : 'DEGRADED');
    return '' +
      '<div class="evidence" id="svc-' + i + '">' +
        '<div class="evidence-head" onclick="toggleEvidence(\'svc-' + i + '\', \'' + (s.clue || '') + '\')">' +
          '<span>' +
            '<span style="font-size:14px;font-weight:700"><span class="svc-dot ' + s.state + '"></span>' +
              escapeHtml(s.name) + '</span>' +
            '<span class="evidence-sub">' + escapeHtml(s.role) + ' · ' +
              '<span class="svc-state ' + s.state + '">' + label + '</span></span>' +
          '</span>' +
          '<span class="evidence-caret">expand</span>' +
        '</div>' +
        '<div class="evidence-body">' +
          '<div class="rows">' + renderRows(s) + '</div>' +
          (s.note ? '<div class="note">' + escapeHtml(s.note) + '</div>' : '') +
        '</div>' +
      '</div>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
}
