/* Cyberity — Threat Console simulation (level 102).
 * Local only. No network, no real hosts, no real credentials.
 */

/* ---------------------------------------------------------------------------
 * Bridge to Android. Same contract as the Inbox Triage lab: AndroidLab is
 * injected by LabScreen, and every call is a no-op without it.
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
    alertOpened: function (id) { safe(function () { AndroidLab.notifyClueFound('alert_opened_' + id); }); },
    urlInspected: function (url) { safe(function () { AndroidLab.notifyUrlInspected(url); }); },
    flagDiscovered: function (token) { safe(function () { AndroidLab.notifyFlagDiscovered(token); }); }
  };
})();

/* ---------------------------------------------------------------------------
 * Alert queue
 * ------------------------------------------------------------------------ */
var ALERTS = {
  a1: {
    id: 'a1',
    code: 'A1',
    severity: 'medium',
    time: '01:12',
    title: 'Outbound traffic spike to unknown host',
    host: 'REG-SRV-02 · Registrar file server',
    summary: '4.2 GB transferred to an address not seen in the last 30 days.',
    evidence: [
      {
        clue: 'network_opened',
        name: 'Network connections',
        sub: 'Destination and volume',
        rows: [
          '01:08:44  REG-SRV-02 -> 172.19.4.90:8443  est   0.4 GB',
          '01:22:10  REG-SRV-02 -> 172.19.4.90:8443  est   1.9 GB',
          '01:47:55  REG-SRV-02 -> 172.19.4.90:8443  est   4.2 GB',
          '02:02:03  REG-SRV-02 -> 172.19.4.90:8443  closed'
        ],
        note: 'One destination, one port, a clean close. Nothing fanned out.'
      },
      {
        clue: 'asset_notes_opened',
        name: 'Asset notes',
        sub: 'Owner, role, scheduled jobs',
        rows: [
          'owner        : Registrar IT',
          'role         : records file server',
          'backup job   : nightly 01:00-02:15',
          'backup target: 172.19.4.90 (offsite vault, added 3 days ago)'
        ],
        note: 'The destination was added to the backup config three days ago, which is why it looks new to the sensor.'
      },
      {
        clue: 'process_tree_opened',
        name: 'Process tree',
        sub: 'What initiated the transfer',
        rows: [
          'systemd',
          '  └─ backup-agent.service',
          '       └─ vaultsync --target 172.19.4.90 --window nightly'
        ],
        note: 'Started by the scheduler, not by a user session.'
      }
    ]
  },

  a2: {
    id: 'a2',
    code: 'A2',
    severity: 'high',
    time: '00:39',
    title: 'Mass file rename with unknown extension',
    host: 'LAB-PC-07 · Computer lab workstation',
    summary: '1,847 files renamed to *.lokd in under two minutes.',
    evidence: [
      {
        clue: 'process_tree_opened',
        name: 'Process tree',
        sub: 'What touched the files',
        rows: [
          'explorer.exe',
          '  └─ setup_labtools.exe   (downloaded 00:31)',
          '       └─ svhost32.exe    (writes to Documents, Pictures, Desktop)'
        ],
        note: 'A downloaded installer spawned a second binary with a name imitating a system process.'
      },
      {
        clue: 'file_activity_opened',
        name: 'File activity',
        sub: 'Rename and write pattern',
        rows: [
          '00:39:02  Documents\\thesis.docx    -> thesis.docx.lokd',
          '00:39:02  Documents\\data.xlsx      -> data.xlsx.lokd',
          '00:40:51  Desktop\\READ_ME_NOW.txt   created',
          '00:40:52  vssadmin delete shadows /all'
        ],
        note: 'Encrypt, drop a ransom note, destroy the local restore points. Textbook ransomware sequence.'
      }
    ]
  },

  a3: {
    id: 'a3',
    code: 'A3',
    severity: 'high',
    time: '02:35',
    title: 'Repeated sign-in failures on one account',
    host: 'LIB-PC-14 · Library shared workstation',
    summary: '312 failed sign-ins against a single account in six minutes.',
    evidence: [
      {
        clue: 'auth_log_opened',
        name: 'Authentication log',
        sub: 'Failed and successful sign-ins',
        rows: [
          '02:35:41  FAIL  user=student.aquino  src=45.61.87.200  reason=bad_password',
          '02:35:42  FAIL  user=student.aquino  src=45.61.87.200  reason=bad_password',
          '02:35:43  FAIL  user=student.aquino  src=45.61.87.200  reason=bad_password',
          '   ... 308 further failures, same user, same src ...',
          '02:41:06  FAIL  user=student.aquino  src=45.61.87.200  reason=bad_password',
          '02:41:07  OK    user=student.aquino  src=45.61.87.200  session=8841'
        ],
        hot: [0, 1, 2, 3, 4],
        ok: [5],
        note: 'Roughly one attempt per second, from one address, against one account — and the last line is not a failure.'
      },
      {
        clue: 'network_opened',
        name: 'Network connections',
        sub: 'Where the attempts arrived from',
        rows: [
          '02:35:40  45.61.87.200 -> sso-gw:443   est',
          '02:41:07  45.61.87.200 -> sso-gw:443   est',
          '02:41:31  45.61.87.200 -> mail-gw:443  est'
        ],
        note: 'After the successful sign-in the same address moved on to another service.'
      },
      {
        clue: 'asset_notes_opened',
        name: 'Asset notes',
        sub: 'Owner, role, exposure',
        rows: [
          'owner   : Library services',
          'role    : shared public workstation',
          'account : student.aquino (2nd year, CS)',
          'mfa     : not enrolled'
        ],
        note: 'No second factor on the account, so a correct password was all that was needed.'
      }
    ]
  },

  a4: {
    id: 'a4',
    code: 'A4',
    severity: 'low',
    time: '23:48',
    title: 'USB mass storage connected outside hours',
    host: 'STAFF-LAPTOP-9 · Faculty laptop',
    summary: 'Removable device mounted at 23:48, 6.1 GB copied to it.',
    evidence: [
      {
        clue: 'file_activity_opened',
        name: 'File activity',
        sub: 'What was copied',
        rows: [
          '23:49:11  copy  D:\\ <- \\Shared\\Grades\\sem1_finals.xlsx',
          '23:51:40  copy  D:\\ <- \\Shared\\Grades\\sem1_raw_scores.csv',
          '23:57:02  device removed'
        ],
        note: 'Authorised user, sensitive data, no encryption on the device. A policy problem rather than an intrusion.'
      },
      {
        clue: 'asset_notes_opened',
        name: 'Asset notes',
        sub: 'Owner and policy',
        rows: [
          'owner  : Faculty (Dept. of Computer Studies)',
          'policy : removable media must be encrypted',
          'device : unencrypted, not registered'
        ],
        note: 'Nothing here was forced. The risk is the data walking out on an unencrypted stick.'
      }
    ]
  },

  a5: {
    id: 'a5',
    code: 'A5',
    severity: 'medium',
    time: '03:10',
    title: 'Connection flood from many addresses',
    host: 'WEB-01 · Public enrolment portal',
    summary: '18,400 half-open connections in 90 seconds; portal unresponsive.',
    evidence: [
      {
        clue: 'network_opened',
        name: 'Network connections',
        sub: 'Connection state summary',
        rows: [
          '03:10:02  SYN_RECV  18,400   sources: 2,131 distinct',
          '03:10:44  ESTAB         12',
          '03:11:30  portal health check: TIMEOUT'
        ],
        note: 'Many sources, almost no completed handshakes. Availability is the target, not data.'
      }
    ]
  }
};

var ALERT_ORDER = ['a1', 'a2', 'a3', 'a4', 'a5'];

/* ---------------------------------------------------------------------------
 * Raw log archive — only ever shown in response to a query.
 * ------------------------------------------------------------------------ */
var LOG_ARCHIVE = [
  '00:31:18  lab-pc-07   DOWNLOAD  file=setup_labtools.exe src=203.0.113.44',
  '00:39:02  lab-pc-07   FILE      rename count=1847 ext=.lokd',
  '00:40:52  lab-pc-07   PROC      vssadmin delete shadows /all',
  '01:08:44  reg-srv-02  NET       dst=172.19.4.90:8443 job=vaultsync',
  '01:47:55  reg-srv-02  NET       dst=172.19.4.90:8443 bytes=4.2G job=vaultsync',
  '02:35:41  sso-gw      AUTH      FAIL user=student.aquino src=45.61.87.200',
  '02:36:15  sso-gw      AUTH      FAIL user=student.aquino src=45.61.87.200 attempts=34',
  '02:38:02  sso-gw      AUTH      FAIL user=student.aquino src=45.61.87.200 attempts=147',
  '02:40:58  sso-gw      AUTH      FAIL user=student.aquino src=45.61.87.200 attempts=311',
  '02:41:07  sso-gw      AUTH      OK   user=student.aquino src=45.61.87.200 session=8841',
  '02:41:07  sso-gw      AUDIT     session=8841 note=CYBERITY{brute_force_confirmed} src=45.61.87.200',
  '02:41:31  mail-gw     SESSION   user=student.aquino src=45.61.87.200 action=mailbox_open',
  '02:44:09  mail-gw     RULE      user=student.aquino created forward-all -> external',
  '02:52:20  sso-gw      AUTH      OK   user=faculty.reyes src=10.14.2.31',
  '23:48:55  staff-lap-9 USB       mount device=unregistered',
  '23:49:11  staff-lap-9 FILE      copy dst=D:\\ src=sem1_finals.xlsx',
  '03:10:02  web-01      NET       SYN_RECV=18400 sources=2131',
  '03:11:30  web-01      HEALTH    status=TIMEOUT',
  '04:02:44  sso-gw      AUTH      OK   user=lib.desk src=10.14.9.7',
  '04:19:03  reg-srv-02  JOB       vaultsync completed rc=0'
];

/* ---------------------------------------------------------------------------
 * Rendering
 * ------------------------------------------------------------------------ */

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function renderConsole(mountId) {
  var html = '';
  ALERT_ORDER.forEach(function (key) {
    var a = ALERTS[key];
    html +=
      '<a class="alert-row" href="alert.html#' + a.id + '">' +
        '<span class="sev-bar ' + a.severity + '"></span>' +
        '<span class="alert-body">' +
          '<span class="alert-meta">' +
            '<span class="alert-id">' + escapeHtml(a.code) + '</span>' +
            '<span class="sev-chip ' + a.severity + '">' + a.severity + '</span>' +
            '<span>' + escapeHtml(a.time) + '</span>' +
          '</span>' +
          '<span class="alert-title">' + escapeHtml(a.title) + '</span>' +
          '<span class="alert-host">' + escapeHtml(a.host) + '</span>' +
        '</span>' +
      '</a>';
  });
  document.getElementById(mountId).innerHTML = html;
}

function renderAlert(mountId) {
  var id = (window.location.hash || '#a1').substring(1);
  var a = ALERTS[id] || ALERTS.a1;

  Cyberity.alertOpened(a.id);

  var html =
    '<h2>' + escapeHtml(a.title) + '</h2>' +
    '<div class="detail-sub">' + escapeHtml(a.code) + ' · ' + escapeHtml(a.time) +
    ' · ' + escapeHtml(a.host) + '</div>' +
    '<p style="font-size:14px;margin:0 0 14px">' + escapeHtml(a.summary) + '</p>';

  a.evidence.forEach(function (ev, index) {
    var rows = ev.rows.map(function (line, i) {
      var cls = '';
      if (ev.hot && ev.hot.indexOf(i) !== -1) cls = 'hot';
      if (ev.ok && ev.ok.indexOf(i) !== -1) cls = 'ok';
      var safe = escapeHtml(line);
      return cls ? '<span class="' + cls + '">' + safe + '</span>' : safe;
    }).join('\n');

    html +=
      '<div class="evidence" id="ev-' + index + '">' +
        '<div class="evidence-head" onclick="toggleEvidence(' + index + ", '" + ev.clue + "')\">" +
          '<span>' +
            '<span class="evidence-name">' + escapeHtml(ev.name) + '</span>' +
            '<span class="evidence-sub">' + escapeHtml(ev.sub) + '</span>' +
          '</span>' +
          '<span class="evidence-caret">expand</span>' +
        '</div>' +
        '<div class="evidence-body">' +
          '<div class="rows">' + rows + '</div>' +
          '<div class="note">' + escapeHtml(ev.note) + '</div>' +
        '</div>' +
      '</div>';
  });

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('alert-code').textContent = a.code + ' · ' + a.host.split(' ·')[0];
}

function toggleEvidence(index, clue) {
  var box = document.getElementById('ev-' + index);
  var first = !box.classList.contains('open');
  box.classList.toggle('open');
  box.querySelector('.evidence-caret').textContent = box.classList.contains('open') ? 'collapse' : 'expand';
  if (first && clue) Cyberity.clueFound(clue);
}

/* ---------------------------------------------------------------------------
 * Log archive search — nothing is shown until the analyst queries for something.
 * ------------------------------------------------------------------------ */

function runLogQuery() {
  var query = document.getElementById('q').value.trim();
  var out = document.getElementById('log-out');
  var count = document.getElementById('result-count');

  if (query === '') {
    out.innerHTML = '<span class="empty">Enter an indicator to query the archive — an address, a host, or an account name.</span>';
    count.textContent = 'Showing 0 of ' + LOG_ARCHIVE.length + ' entries';
    return;
  }

  var needle = query.toLowerCase();
  var hits = LOG_ARCHIVE.filter(function (line) {
    return line.toLowerCase().indexOf(needle) !== -1;
  });

  count.textContent = 'Showing ' + hits.length + ' of ' + LOG_ARCHIVE.length + ' entries · query "' + query + '"';

  if (hits.length === 0) {
    out.innerHTML = '<span class="empty">No entries match that query. Check the indicator and try again.</span>';
    return;
  }

  out.innerHTML = hits.map(function (line) {
    return '<span class="hit">' + escapeHtml(line) + '</span>';
  }).join('\n');

  Cyberity.clueFound('log_search_used');

  var revealedFlag = hits.some(function (line) {
    return line.indexOf('CYBERITY{') !== -1;
  });
  if (revealedFlag) Cyberity.flagDiscovered('log_archive');
}
