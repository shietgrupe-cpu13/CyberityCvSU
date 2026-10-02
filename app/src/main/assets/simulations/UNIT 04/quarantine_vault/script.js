/* Cyberity — Quarantine Vault (level 401).
 * Local only. Fictional files, hosts and addresses; nothing installs, runs,
 * connects or touches the device this runs on.
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

/* Which files have been inspected. sessionStorage first, window.name as the
   fallback that survives a page load inside the WebView. */
var Store = {
  KEY: 'qv_seen',
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
 * The five quarantined files
 *
 * Every file is listed with the same weight and the same scary antivirus
 * label: nothing in the list says which are hostile, or which family they
 * belong to. Working that out from behaviour is the exercise.
 * ------------------------------------------------------------------------ */
var SPECS = {
  q1: {
    id: 'q1', name: 'FreeFlix_HD.apk', size: '14.2 MB',
    found: 'student phone · brought to the ITSO counter',
    flagged: 'Android/Suspicious.Gen',
    summary: 'A free movie streaming app installed from a link on a Facebook page. The ' +
      'owner says it "works fine" but the phone has become unusable.',
    clue: 'opened_q1',
    tools: [
      {
        label: 'SANDBOX REPORT', sub: 'What it did during 30 minutes in the sandbox',
        clue: 'report_q1', tag: 'SANDBOX REPORT · FreeFlix_HD.apk',
        rows: [
          '00:00 streams a sample video <span class="ok">(works)</span>',
          '00:02 <span class="hot">homepage -> searchfast.example</span>',
          '00:02 <span class="hot">search   -> searchfast.example</span>',
          '00:03 <span class="hot">hides its own launcher icon</span>',
          '01:30 full-screen ad over home screen',
          '03:00 full-screen ad over home screen',
          '...   one every 90 s, app open or not',
          '12:40 <span class="warn">"clicks" 6 ads in the background</span>',
          '',
          'files read:          none',
          'contacts read:       none',
          'keystrokes captured: none'
        ],
        note: 'Its whole business is advertising: it earns money for every ad you see and ' +
          'every ad it "clicks" for you. It does not touch your files or passwords. Hiding ' +
          'its icon is how it avoids being uninstalled.'
      },
      {
        label: 'PERMISSIONS', sub: 'What it asked the phone for',
        tag: 'PERMISSIONS · FreeFlix_HD.apk',
        rows: [
          'internet                 granted',
          '<span class="warn">display over other apps  granted</span>',
          '<span class="warn">run at startup           granted</span>',
          'read contacts            -',
          'accessibility service    -'
        ],
        note: '"Display over other apps" is what lets it draw ads on top of everything else ' +
          'you open.'
      }
    ]
  },

  q2: {
    id: 'q2', name: 'KeyboardThemes_Pro.apk', size: '6.8 MB',
    found: 'student phone · flagged by the campus Wi-Fi gateway',
    flagged: 'Android/Suspicious.Gen',
    summary: 'A keyboard app with anime and neon themes, shared in a block group chat as a ' +
      '"free Pro version". The owner uses it every day.',
    clue: 'opened_q2',
    tools: [
      {
        label: 'PERMISSIONS', sub: 'What it asked the phone for',
        clue: 'perms_q2', tag: 'PERMISSIONS · KeyboardThemes_Pro.apk',
        rows: [
          'internet                 granted',
          '<span class="hot">accessibility service    granted</span>',
          '  "observe your actions, retrieve',
          '   window content, and see text',
          '   that you type"',
          'run at startup           granted',
          'display over other apps  -'
        ],
        note: 'Accessibility access exists for screen readers. An app that holds it can read ' +
          'everything shown on the screen and everything typed into any app.'
      },
      {
        label: 'SANDBOX REPORT', sub: 'What it captured while ITSO typed test logins',
        clue: 'keylog_q2', tag: 'SANDBOX REPORT · KeyboardThemes_Pro.apk',
        rows: [
          'keyboard themes work as advertised <span class="ok">(works)</span>',
          '',
          'captured from other apps:',
          ' Chrome    portal.cvsu.edu.ph',
          ' Chrome    <span class="hot">student no.  CAPTURED</span>',
          ' Chrome    <span class="hot">password     CAPTURED</span>',
          ' GCash     <span class="hot">MPIN         CAPTURED</span>',
          ' Messenger chat text  CAPTURED',
          '(every value captured in full)',
          '',
          '<span class="hot">every 10 min: upload keys.log</span>',
          '<span class="hot">  -> 45.61.87.200:443</span>',
          'ads shown:       none',
          'files encrypted: none'
        ],
        note: 'Nothing visible ever happens. No ads, no slowdown, no warning. It quietly ' +
          'records what you type and ships it off. The address it uploads to is the same one ' +
          'behind the phishing in Unit 3.'
      }
    ]
  },

  q3: {
    id: 'q3', name: 'LabMonitor_Agent.exe', size: '3.1 MB',
    found: 'all 30 PCs in Computer Laboratory 2',
    flagged: 'RemoteAdmin.Generic · "can view and control screens"',
    summary: 'A student ran a free antivirus scan on a lab PC and reported this file. It is ' +
      'running on every machine in the room.',
    clue: 'opened_q3',
    tools: [
      {
        label: 'SANDBOX REPORT', sub: 'What it did during a 30-minute class session',
        clue: 'report_q3', tag: 'SANDBOX REPORT · LabMonitor_Agent.exe',
        rows: [
          '<span class="warn">screen thumbnail every 5 s</span>',
          '<span class="warn">  -> instructor console</span>',
          '<span class="warn">can lock keyboard and mouse</span>',
          '<span class="warn">can open a website on all PCs</span>',
          'tray icon: "This PC can be viewed',
          '           by your instructor"',
          '',
          'connects to: lab2-console.cvsu.edu.ph',
          '             <span class="ok">10.14.0.5, on campus only</span>',
          'keystrokes captured: none',
          'files read:          none',
          'runs only during class hours'
        ],
        note: 'On capability alone this looks alarming: it watches screens and can take ' +
          'control. Capability is not what decides the verdict.'
      },
      {
        label: 'PUBLISHER & POLICY', sub: 'Who installed it, and who was told',
        clue: 'publisher_q3', tag: 'PUBLISHER & POLICY',
        rows: [
          'signed by : <span class="ok">CvSU ITSO (valid)</span>',
          'installed : <span class="ok">by ITSO, official lab image</span>',
          'ticket    : CHG-2026-0412',
          '            approved by lab coordinator',
          '',
          'Lab Acceptable Use Policy, sec. 4:',
          '<span class="ok">"Lab PCs may be monitored by the</span>',
          '<span class="ok"> instructor during class sessions.</span>',
          '<span class="ok"> Monitoring is shown by a tray icon."</span>',
          '',
          'login banner on lab PCs: <span class="ok">yes</span>'
        ],
        note: 'Installed by the people who own the machines, for a stated purpose, with the ' +
          'users told in writing and on screen. Antivirus flags remote-admin tools by what ' +
          'they can do, so false alarms like this one are common.'
      }
    ]
  },

  q4: {
    id: 'q4', name: 'BatteryBoost_Cleaner.apk', size: '9.4 MB',
    found: 'student phone · brought to the ITSO counter',
    flagged: 'Android/Suspicious.Gen',
    summary: 'A "battery saver and phone cleaner" app. Since installing it, the owner says ' +
      'the phone is hot in her pocket and dies before lunch.',
    clue: 'opened_q4',
    tools: [
      {
        label: 'RESOURCE MONITOR', sub: 'CPU, battery and heat, screen off',
        clue: 'monitor_q4', tag: 'RESOURCE MONITOR · screen OFF, phone idle',
        rows: [
          'process           cpu   battery/hr',
          '<span class="hot">batteryboost      97%   -18%</span>',
          'system             2%    -1%',
          '',
          '<span class="hot">battery temp: 46 °C</span>',
          '  (normal when idle: 28–32 °C)',
          '<span class="warn">runs only with the screen off</span>',
          '<span class="warn">or while charging</span>',
          '',
          'files read: none',
          'ads shown:  none'
        ],
        note: 'It works hardest exactly when you are not looking at the phone, so you never ' +
          'see it slow down. You only notice the heat and the battery.'
      },
      {
        label: 'NETWORK LOG', sub: 'What it talks to',
        clue: 'network_q4', tag: 'NETWORK LOG · com.batteryboost.clean',
        rows: [
          '<span class="hot">pool.coinpool.example:3333</span>',
          '<span class="hot">  connected 6h 12m</span>',
          '-> login  wallet 4Bx9...q7Kd',
          '<- job    new block template',
          '-> submit share accepted',
          '-> submit share accepted',
          '   ...    1,940 shares today',
          '',
          '<span class="warn">port 3333: a common mining pool port</span>'
        ],
        note: 'A mining pool pays out cryptocurrency for computing work. The wallet is the ' +
          'attacker’s. The electricity, the heat and the worn-out battery are the owner’s.'
      }
    ]
  },

  q5: {
    id: 'q5', name: 'PrintHelper.exe', size: '412 KB',
    found: 'LAB07 · Computer Laboratory 2',
    flagged: 'Win32/Suspicious.Gen',
    summary: 'Installed as a background service called "Print Spooler Helper". The PC seems ' +
      'normal to students using it.',
    clue: 'opened_q5',
    tools: [
      {
        label: 'SANDBOX REPORT', sub: 'What it did after it was started',
        clue: 'report_q5', tag: 'SANDBOX REPORT · PrintHelper.exe',
        rows: [
          'installs as service:',
          '  "Print Spooler Helper"',
          'starts with Windows',
          'no window, no tray icon',
          '<span class="hot">checks in every 60 s</span>',
          '<span class="hot">  -> 45.61.87.200:8080/gate</span>',
          'then waits for orders',
          '',
          'files read:          none',
          'keystrokes captured: none',
          'ads shown:           none'
        ],
        note: 'On its own it does almost nothing. It sits and waits for orders from a ' +
          'machine the attacker controls, known as a command-and-control (C2) server.'
      },
      {
        label: 'COMMAND LOG', sub: 'Orders it received from the C2 server',
        clue: 'commands_q5', tag: 'COMMAND LOG · from 45.61.87.200',
        rows: [
          'Aug 04 21:00 <- idle',
          'Aug 11 02:13 <- spam 5,000 emails',
          'Aug 11 02:40 -> done',
          '<span class="hot">Aug 18 08:00 <- flood</span>',
          '<span class="hot">   portal.cvsu.edu.ph:443 for 600 s</span>',
          '<span class="hot">Aug 18 08:10 -> done, 2.1M requests</span>',
          'Aug 18 08:10 <- idle',
          '',
          '<span class="warn">Aug 18 08:00 was the first morning</span>',
          '<span class="warn">of enrollment. The same order went</span>',
          '<span class="warn">to 1,300 other bots that day.</span>'
        ],
        note: 'One bot is harmless. Thirteen hundred of them, all ordered to hit the same ' +
          'site at the same minute, is a distributed denial-of-service (DDoS) attack: the ' +
          'target is buried in traffic until real users cannot get in.'
      }
    ]
  }
};

var ORDER = ['q1', 'q2', 'q3', 'q4', 'q5'];

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
  KEY: 'quarantine_vault_last',
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
 * Vault list
 * ------------------------------------------------------------------------ */
function renderVault(mountId) {
  var html = ORDER.map(function (id) {
    var s = SPECS[id];
    var seen = Store.has(id);
    return '' +
      '<a class="choice proc-row ' + (seen ? 'read' : 'unread') +
        '" id="card-' + id + '" href="specimen.html#' + id + '">' +
        '<span class="proc-body">' +
          '<span class="sms-top">' +
            '<span class="proc-name mono">' + escapeHtml(s.name) + '</span>' +
            '<span class="sms-when">' + escapeHtml(s.size) + '</span>' +
          '</span>' +
          '<span class="spec-meta">' + escapeHtml(s.found) + '</span>' +
          '<span class="proc-sum">' + escapeHtml(s.summary) + '</span>' +
          '<span class="spec-why mono">antivirus: ' + escapeHtml(s.flagged) + '</span>' +
          '<span class="mail-badge">' + (seen ? 'INSPECTED' : 'NOT INSPECTED') + '</span>' +
        '</span>' +
      '</a>';
  }).join('');

  document.getElementById(mountId).innerHTML = html;

  var left = ORDER.filter(function (k) { return !Store.has(k); }).length;
  var counter = document.getElementById('seen-count');
  if (counter) {
    counter.textContent = left === 0
      ? 'All five inspected'
      : left + ' of 5 not inspected yet';
  }

  ReturnTo.restore();
}

/* ---------------------------------------------------------------------------
 * One file
 * ------------------------------------------------------------------------ */
function renderSpecimen(mountId) {
  ReturnTo.remember((window.location.hash || '').substring(1));

  var id = (window.location.hash || '#q1').substring(1);
  var s = SPECS[id] || SPECS.q1;
  window.CURRENT = s;

  Store.mark(s.id);
  if (s.clue) Cyberity.clueFound(s.clue);

  var tools = s.tools.map(function (t, i) {
    return '<button class="appui tool-btn" onclick="openTool(' + i + ')">' +
        '<b>' + escapeHtml(t.label) + '</b>' + escapeHtml(t.sub) +
      '</button>' +
      '<div class="reveal-slot" id="tool-' + i + '"></div>';
  }).join('');

  document.getElementById(mountId).innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">QUARANTINED FILE</span>' +
      '<h2 class="mono">' + escapeHtml(s.name) + '</h2>' +
      '<div class="detail-sub">' + escapeHtml(s.found) + ' · ' + escapeHtml(s.size) + '</div>' +
      '<div class="detail-sub mono">antivirus: ' + escapeHtml(s.flagged) + '</div>' +
      '<p style="font-size:14px;margin:10px 0 0">' + escapeHtml(s.summary) + '</p>' +
    '</div>' +
    tools +
    '<div class="banner" id="spec-banner"></div>' +
    '<button class="cta danger" onclick="restoreFile()">RESTORE FROM QUARANTINE AND TRY IT</button>' +
    '<a class="cta" href="vault.html">BACK TO THE VAULT</a>';

  var title = document.getElementById('spec-title');
  if (title) title.textContent = s.name;
  window.scrollTo(0, 0);
}

function openTool(index) {
  var s = window.CURRENT;
  var t = s.tools[index];
  var box = document.getElementById('tool-' + index);
  if (!t || box.classList.contains('open')) return;

  box.className = 'reveal-slot open';
  box.innerHTML =
    '<div class="artifact a-sys">' +
      '<span class="artifact-tag">' + escapeHtml(t.tag) + '</span>' +
      '<div class="rows mono">' + t.rows.join('\n') + '</div>' +
      '<div class="note">' + escapeHtml(t.note) + '</div>' +
    '</div>';
  box.scrollIntoView({ behavior: 'smooth', block: 'start' });

  if (t.clue) Cyberity.clueFound(t.clue);
}

/** Dangerous: letting a quarantined file loose to "see what it does". */
function restoreFile() {
  var banner = document.getElementById('spec-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation. Quarantine exists so a file cannot run. ' +
    'Restoring it to find out what it does turns an investigation into a second infection. ' +
    'Everything you need is already in the sandbox reports.';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('file_restored');
}

/* ---------------------------------------------------------------------------
 * Classification board
 *
 * One label per file. Submitting only says how many are right, never which,
 * so the student has to reason from the reports rather than cycle the chips.
 * ------------------------------------------------------------------------ */
var LABELS = ['Adware', 'Spyware', 'Cryptominer', 'Bot', 'Not malware'];
var KEY = { q1: 'Adware', q2: 'Spyware', q3: 'Not malware', q4: 'Cryptominer', q5: 'Bot' };
var picks = {};

function renderBoard(mountId) {
  document.getElementById(mountId).innerHTML = ORDER.map(function (id) {
    var s = SPECS[id];
    var chips = LABELS.map(function (label, i) {
      var on = picks[id] === label ? ' on' : '';
      return '<button class="chip' + on + '" onclick="pick(\'' + id + '\',' + i + ')">' +
        escapeHtml(label) + '</button>';
    }).join('');
    return '<div class="board-row">' +
        '<div class="board-name mono">' + escapeHtml(s.name) + '</div>' +
        '<div class="board-hint">' + escapeHtml(s.found) + '</div>' +
        '<div class="chips">' + chips + '</div>' +
      '</div>';
  }).join('');
}

function pick(id, index) {
  picks[id] = LABELS[index];
  renderBoard('board');
  var banner = document.getElementById('board-banner');
  if (banner.className.indexOf('good') === -1) banner.className = 'banner';
}

function submitBoard() {
  var banner = document.getElementById('board-banner');
  var missing = ORDER.filter(function (id) { return !picks[id]; }).length;
  if (missing > 0) {
    banner.className = 'banner show warn';
    banner.textContent = missing === 1
      ? 'One file still has no label.'
      : missing + ' files still have no label.';
    return;
  }
  var right = ORDER.filter(function (id) { return picks[id] === KEY[id]; }).length;
  if (right < ORDER.length) {
    banner.className = 'banner show warn';
    banner.textContent = right + ' of 5 correct. Re-read the reports for the ones you are ' +
      'least sure of: what does each file actually DO?';
    return;
  }
  banner.className = 'banner show good';
  banner.innerHTML = 'All five classified correctly. Report code: ' +
    '<b class="mono">CYBERITY{b3hav10ur_n0t_n4m3s}</b>';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('board_complete');
  Cyberity.flagDiscovered('board_complete');
}
