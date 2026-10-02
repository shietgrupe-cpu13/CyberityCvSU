/* Cyberity — Lab 3 hardening sprint (level 404).
 * Local only. Fictional lab, patches, programs and settings; nothing is
 * installed, changed or run on the device this runs on.
 *
 * Five tools, one per page:
 *   patch.html      fit the right patches into a 75-minute maintenance window
 *   allow.html      build an application allowlist and replay a test day
 *   accounts.html   replay one trojan as an admin and as a standard user
 *   harden.html     harden settings: risk gauge vs. help-desk complaints
 *   stack.html      stack defense layers and launch four attacks through them
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

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function $(id) { return document.getElementById(id); }

function showBanner(id, kind, html, quiet) {
  var b = $(id);
  b.className = 'banner show ' + kind;
  b.innerHTML = html;
  if (!quiet) b.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function hideBanner(id) { $(id).className = 'banner'; }

/* ===========================================================================
 * 1 · PATCH WINDOW
 *
 * Time for two updates before the 08:30 class. The PDF hole looks scarier
 * (Critical), but nobody is attacking it yet; the two holes attackers are
 * using right now go first. The Start menu update fixes nothing.
 * ========================================================================= */
var SLOTS = 2;

var PATCHES = {
  p1: { name: 'Windows file-sharing hole', level: 'CRITICAL', cls: 'crit', attacked: true,
        what: 'Lets a worm jump from PC to PC on its own, with nobody clicking anything. ' +
          'One infected PC could reach all forty.' },
  p3: { name: 'PDF reader hole', level: 'CRITICAL', cls: 'crit', attacked: false,
        what: 'Only works if someone opens a specially made PDF. No attacks have been ' +
          'seen yet.' },
  p2: { name: 'Chrome browser hole', level: 'HIGH', cls: 'high', attacked: true,
        what: 'Visiting a bad website can let it take over the PC. Every student uses ' +
          'Chrome.' },
  p4: { name: 'New Start menu design', level: 'NOT A SECURITY FIX', cls: '', attacked: false,
        what: 'Changes how the Start menu looks. It doesn’t fix any hole.' }
};
var PATCH_ORDER = ['p1', 'p3', 'p2', 'p4'];
var chosen = {};

function chosenList() {
  return PATCH_ORDER.filter(function (k) { return chosen[k]; });
}

function renderPatches() {
  Cyberity.clueFound('patch_opened');
  $('patches').innerHTML = PATCH_ORDER.map(function (k) {
    var p = PATCHES[k];
    return '<div class="patch' + (chosen[k] ? ' on' : '') + '" onclick="togglePatch(\'' + k + '\')">' +
        '<span class="box">✓</span>' +
        '<span class="patch-body">' +
          '<span class="patch-name">' + escapeHtml(p.name) + '</span>' +
          '<span class="patch-meta">' +
            '<span class="pill ' + p.cls + '">' + escapeHtml(p.level) + '</span>' +
            (p.attacked ? '<span class="pill exp">ATTACKERS ARE USING THIS NOW</span>' : '') +
          '</span>' +
          '<span class="muted" style="display:block;margin-top:5px">' + escapeHtml(p.what) + '</span>' +
        '</span>' +
      '</div>';
  }).join('');
  paintSlots();
}

function paintSlots() {
  var n = chosenList().length;
  $('slots').innerHTML = [0, 1].map(function (i) {
    var k = chosenList()[i];
    return '<div class="slot' + (k ? ' full' : '') + '">' +
      (k ? escapeHtml(PATCHES[k].name) : 'Update ' + (i + 1) + ': empty') + '</div>';
  }).join('');
  $('slot-note').textContent = n + ' of ' + SLOTS + ' chosen';
}

function togglePatch(k) {
  if (!chosen[k] && chosenList().length >= SLOTS) {
    showBanner('patch-banner', 'warn', 'There’s only time for <b>two</b> updates before ' +
      'class. Tap one you’ve chosen to remove it first.');
    return;
  }
  chosen[k] = !chosen[k];
  renderPatches();
  hideBanner('patch-banner');
}

function submitPatches() {
  var list = chosenList();
  if (list.length < SLOTS) {
    showBanner('patch-banner', 'warn', 'Choose <b>two</b> updates for this morning.');
    return;
  }
  Cyberity.clueFound('patch_tried');
  if (chosen.p4) {
    showBanner('patch-banner', 'warn', 'The new Start menu doesn’t fix any security hole. ' +
      'With time for only two, spend both on holes.');
    return;
  }
  if (chosen.p3) {
    var left = chosen.p1 ? 'the Chrome hole' : 'the Windows file-sharing hole';
    showBanner('patch-banner', 'warn', 'The PDF hole <b>looks</b> scarier because it’s ' +
      'Critical, but nobody is attacking it yet. Meanwhile <b>' + left + '</b> is being ' +
      'attacked right now, and you left it open. Swap them.');
    return;
  }
  showBanner('patch-banner', 'good', 'Approved. Both holes attackers are using <b>right now</b> ' +
    'are closed before the first student logs in. The PDF hole is first in line next week, and ' +
    'the Start menu can wait for the holidays.');
  Cyberity.clueFound('patch_plan_ok');
}

/* ===========================================================================
 * 2 · APPLICATION ALLOWLIST
 *
 * Antivirus blocks what it knows is bad. An allowlist runs only what is known
 * to be good, and blocks everything else, including malware nobody has seen
 * yet. The trap is allowing a folder that students can write to.
 * ========================================================================= */
var RULES = {
  r1: { text: 'Allow programs signed by Microsoft', note: 'Windows, Word, Excel' },
  r2: { text: 'Allow programs in C:\\Program Files', note: 'only admins can install there' },
  r3: { text: 'Allow programs signed by Google and StatWise Inc.', note: 'Chrome, the thesis statistics tool' },
  r4: { text: 'Allow anything in the user\u2019s Downloads folder', note: '"so students can run what they need"' },
  r5: { text: 'Allow anything in C:\\Users\\*\\AppData', note: '"some apps update themselves from there"' },
  r6: { text: 'Block everything else', note: 'default deny: always on', locked: true }
};
var RULE_ORDER = ['r1', 'r2', 'r3', 'r4', 'r5', 'r6'];
var rules = { r1: true, r4: true, r6: true };   // last semester's policy

var PROGRAMS = [
  { name: 'WINWORD.EXE', where: 'Program Files · signed Microsoft', good: true, by: ['r1', 'r2'] },
  { name: 'chrome.exe', where: 'Program Files · signed Google', good: true, by: ['r2', 'r3'] },
  { name: 'StatWise.exe', where: 'AppData\\Local · signed StatWise Inc.', good: true, by: ['r3', 'r5'] },
  { name: 'netbeans64.exe', where: 'Program Files · installed by ITSO', good: true, by: ['r2'] },
  { name: 'Grade_Calculator_2026.exe', where: 'Downloads · unsigned', good: false, by: ['r4'] },
  { name: 'svch0st.exe', where: 'AppData\\Roaming · unsigned', good: false, by: ['r5'] },
  { name: 'activator.exe', where: 'Downloads\\StatWise_FULL · unsigned', good: false, by: ['r4'] },
  { name: 'Grade_Calc_v2.exe', where: 'Desktop · unsigned · brand-new variant', good: false, by: [], fresh: true }
];

function renderRules() {
  Cyberity.clueFound('allow_opened');
  $('rules').innerHTML = RULE_ORDER.map(function (k) {
    var r = RULES[k];
    return '<div class="rule-row"><div class="rule-text">' + escapeHtml(r.text) +
      '<small>' + escapeHtml(r.note) + '</small></div>' +
      '<button class="switch' + (rules[k] ? ' on' : '') + '"' + (r.locked ? ' disabled' : '') +
        ' aria-label="toggle" onclick="toggleRule(\'' + k + '\')"></button></div>';
  }).join('');
}

function toggleRule(k) {
  if (RULES[k].locked) return;
  rules[k] = !rules[k];
  renderRules();
  $('results').innerHTML = '';
  $('results').style.display = 'none';
  hideBanner('allow-banner');
}

function runTestDay() {
  var wrong = 0, legitBlocked = 0, badRan = 0;
  var rows = PROGRAMS.map(function (p) {
    var ran = p.by.some(function (r) { return rules[r]; });
    var right = ran === p.good;
    if (!right) { wrong++; if (p.good) legitBlocked++; else badRan++; }
    return '<div class="res-row"><div class="res-name"><b>' + escapeHtml(p.name) + '</b>' +
        '<small>' + escapeHtml(p.where) + (p.good ? ' · needed for class' : ' · malware') + '</small></div>' +
        '<span class="res-tag ' + (ran ? 'ran' : 'blocked') + '">' + (ran ? 'RAN' : 'BLOCKED') + '</span>' +
        '<span class="res-ok ' + (right ? 'good' : 'bad') + '">' + (right ? '\u2713' : '\u2717') + '</span>' +
      '</div>';
  }).join('');
  $('results').innerHTML = rows;
  $('results').style.display = 'block';
  Cyberity.clueFound('test_day_run');
  if (wrong === 0) {
    showBanner('allow-banner', 'good', 'Perfect test day: every class program ran, all four malware ' +
      'samples were blocked. Including <b>Grade_Calc_v2.exe</b>, a variant no antivirus had seen yet. ' +
      'An allowlist doesn\u2019t need to recognise malware. It only needs to recognise what\u2019s allowed.');
    Cyberity.clueFound('allowlist_ok');
    return;
  }
  var parts = [];
  if (badRan) parts.push(badRan + ' malware sample' + (badRan > 1 ? 's' : '') + ' ran');
  if (legitBlocked) parts.push(legitBlocked + ' class program' + (legitBlocked > 1 ? 's were' : ' was') + ' blocked');
  showBanner('allow-banner', 'warn', 'Not yet: ' + parts.join(', and ') + '. Look at <b>where</b> each ' +
    'one runs from, and which rule let it through.');
}

/** Dangerous: switching application control off for convenience. */
function disableAllowlist() {
  Cyberity.clueFound('allowlist_disabled');
  showBanner('allow-banner', 'bad', 'Blocked by the simulation. With application control off, Lab 3 ' +
    'is back to trusting antivirus alone, which only stops malware it already knows. The next ' +
    'Grade_Calculator variant would run on all 40 PCs. Fix the rules instead of removing them.');
}

/* ===========================================================================
 * 3 · SAME CLICK, TWO ACCOUNTS
 *
 * One trojan, replayed in a sandbox under two kinds of account. Least
 * privilege doesn't stop it running; it shrinks what it can do once it does.
 * ========================================================================= */
var STEPS = [
  { what: 'Run Grade_Calculator_2026.exe', admin: true, std: true },
  { what: 'Open a connection to 45.61.87.200', admin: true, std: true },
  { what: 'Read this student\u2019s own Documents', admin: true, std: true },
  { what: 'Install "WindowsUpdateSvc" for all users', admin: true, std: false },
  { what: 'Write into C:\\Windows\\System32', admin: true, std: false },
  { what: 'Turn off antivirus', admin: true, std: false },
  { what: 'Read other users\u2019 files on this PC', admin: true, std: false },
  { what: 'Add its own admin account', admin: true, std: false }
];
var account = 'admin';
var replayed = {};
var replayTimer = null;

function renderAccounts() {
  Cyberity.clueFound('accounts_opened');
  paintAccount();
}

function setAccount(a) {
  account = a;
  paintAccount();
  $('replay').innerHTML = '<span class="muted">Press REPLAY to run the same click as this account.</span>';
}

function paintAccount() {
  $('seg-admin').className = account === 'admin' ? 'on' : '';
  $('seg-std').className = account === 'std' ? 'on' : '';
  function score(k) {
    return replayed[k] ? STEPS.filter(function (s) { return s[k]; }).length + ' / ' + STEPS.length : '?';
  }
  $('cmp').innerHTML =
    '<div class="cmp' + (replayed.admin ? ' bad' : '') + '"><b>' + score('admin') + '</b><span>actions that worked as<br>Administrator</span></div>' +
    '<div class="cmp' + (replayed.std ? ' good' : '') + '"><b>' + score('std') + '</b><span>actions that worked as<br>Standard user</span></div>';
}

function replay() {
  if (replayTimer) return;
  var box = $('replay');
  box.innerHTML = '';
  var i = 0;
  replayTimer = setInterval(function () {
    var s = STEPS[i];
    var ok = s[account];
    var d = document.createElement('div');
    d.innerHTML = (ok ? '<span class="hot">\u2713 worked </span>' : '<span class="ok">\u2717 denied </span>') +
      escapeHtml(s.what);
    box.appendChild(d);
    i++;
    if (i === STEPS.length) {
      clearInterval(replayTimer);
      replayTimer = null;
      replayed[account] = true;
      paintAccount();
      Cyberity.clueFound('replay_' + account);
      if (replayed.admin && replayed.std) {
        showBanner('acct-banner', 'good', 'Same click, same trojan. As an admin it took over the whole ' +
          'PC; as a standard user it was stuck inside one student\u2019s profile. It still ran and still ' +
          'phoned home, which is why least privilege is one layer, not the whole answer.', true);
        Cyberity.clueFound('replay_both');
      }
    }
  }, 260);
}

/** Dangerous: giving every student admin rights to save help-desk tickets. */
function everyoneAdmin() {
  Cyberity.clueFound('everyone_admin');
  showBanner('acct-banner', 'bad', 'Blocked by the simulation. Fewer tickets this week, but every trojan ' +
    'a student runs now gets the whole PC: system files, other users, the antivirus switch. Install ' +
    'what classes need centrally instead, and keep daily accounts standard.');
}

/* ===========================================================================
 * 4 · HARDENING PANEL
 *
 * Each setting moves two meters: attack risk and help-desk complaints.
 * Turning everything to maximum breaks classes, and broken security gets
 * worked around. The target is low risk with zero complaints.
 * ========================================================================= */
var SETTINGS = {
  upd: { name: 'Windows Update', hint: 'how patches arrive',
         opts: [['Off', 20, 0], ['Notify only', 8, 0], ['Automatic', 0, 0]] },
  mac: { name: 'Office macros', hint: 'small programs inside documents',
         opts: [['Allow all', 15, 0], ['Block macros from the internet', 0, 0],
                ['Block every macro', 0, 1, 'Accounting class: our grade templates use macros and stopped working.']] },
  usb: { name: 'USB storage', hint: 'flash drives',
         opts: [['Allowed, autorun on', 15, 0], ['Allowed, autorun off, scanned', 3, 0],
                ['Blocked completely', 0, 1, 'Students can\u2019t submit projects on flash drives any more.']] },
  ext: { name: 'File name extensions', hint: 'the .exe at the end of a name',
         opts: [['Hidden', 10, 0], ['Shown', 0, 0]] },
  rdp: { name: 'Remote Desktop', hint: 'controlling a PC over the network',
         opts: [['Open to the internet', 15, 0], ['Campus VPN only', 4, 0],
                ['Off', 0, 1, 'ITSO can\u2019t fix lab PCs remotely; every ticket now needs a walk across campus.']] },
  pwd: { name: 'Local admin password', hint: 'the built-in admin account',
         opts: [['Same on every PC', 15, 0], ['Unique per PC, managed', 0, 0]] },
  fw:  { name: 'Firewall', hint: 'filters network connections',
         opts: [['Off', 10, 0], ['On', 0, 0]] }
};
var SET_ORDER = ['upd', 'mac', 'usb', 'ext', 'rdp', 'pwd', 'fw'];
var picks = { upd: 1, mac: 0, usb: 0, ext: 0, rdp: 0, pwd: 0, fw: 1 };   // how Lab 2 was set up
var RISK_TARGET = 10;

function renderHarden() {
  Cyberity.clueFound('harden_opened');
  $('settings').innerHTML = SET_ORDER.map(function (k) {
    var s = SETTINGS[k];
    return '<div class="set"><div class="set-name">' + escapeHtml(s.name) + '<small>' + escapeHtml(s.hint) + '</small></div>' +
      '<div class="chips">' + s.opts.map(function (o, i) {
        return '<button class="chip' + (picks[k] === i ? ' on' : '') + '" onclick="pickSetting(\'' + k + '\',' + i + ')">' +
          escapeHtml(o[0]) + '</button>';
      }).join('') + '</div></div>';
  }).join('');
  paintMeters();
}

function pickSetting(k, i) {
  picks[k] = i;
  renderHarden();
}

function paintMeters() {
  var risk = 0, tickets = [];
  SET_ORDER.forEach(function (k) {
    var o = SETTINGS[k].opts[picks[k]];
    risk += o[1];
    if (o[2]) tickets.push(o[3]);
  });
  $('risk-num').textContent = risk;
  var bar = $('risk-bar');
  bar.style.width = risk + '%';
  bar.className = risk <= RISK_TARGET ? 'ok' : risk <= 40 ? 'warn' : 'over';
  $('cmp-num').textContent = tickets.length;
  $('cmp-num').className = 'complaints ' + (tickets.length ? 'bad' : 'good');
  $('tickets').innerHTML = tickets.map(function (t) { return '<div class="ticket">\u2709 ' + escapeHtml(t) + '</div>'; }).join('');
  if (risk <= RISK_TARGET && tickets.length === 0) {
    showBanner('harden-banner', 'good', 'Lab 3 signed off: risk ' + risk + ', zero complaints. Every ' +
      'setting is as tight as it can be <b>without breaking a class</b>. That\u2019s the job.', true);
    Cyberity.clueFound('hardened_ok');
  } else if (risk <= RISK_TARGET) {
    showBanner('harden-banner', 'warn', 'Risk is low, but people can\u2019t do their work. Security ' +
      'that breaks classes gets switched off or worked around. Find a setting that protects ' +
      '<b>and</b> keeps the class running.', true);
  } else {
    hideBanner('harden-banner');
  }
}

/* ===========================================================================
 * 5 · DEFENSE IN DEPTH
 *
 * Four attacks from this unit, six layers, room for four. Each attack is
 * stopped by a different layer, so no single control is enough.
 * ========================================================================= */
var ATTACKS = [
  { id: 'a1', name: 'Trojan', sub: 'chat link' },
  { id: 'a2', name: 'Macro virus', sub: 'flash drive' },
  { id: 'a3', name: 'Worm', sub: 'unpatched SMB' },
  { id: 'a4', name: 'Ransomware', sub: 'stolen admin pw' }
];

var LAYERS = {
  av:    { name: 'Antivirus', sub: 'blocks known malware', blocks: ['a2'] },
  patch: { name: 'Patching on schedule', sub: 'closes known holes', blocks: ['a3'] },
  allow: { name: 'Application allowlist', sub: 'only approved programs run', blocks: ['a1'] },
  macro: { name: 'Untrusted macros blocked', sub: 'documents can\u2019t run code', blocks: ['a2'] },
  mfa:   { name: 'MFA + unique admin passwords', sub: 'a stolen password isn\u2019t enough', blocks: ['a4'] },
  fw:    { name: 'Perimeter firewall', sub: 'blocks the internet from reaching in', blocks: [] }
};
var LAYER_ORDER = ['av', 'patch', 'allow', 'macro', 'mfa', 'fw'];
var MAX_LAYERS = 4;
var stack = [];
var launching = false;

function renderStack() {
  Cyberity.clueFound('stack_opened');
  $('layers').innerHTML = LAYER_ORDER.map(function (k) {
    var l = LAYERS[k];
    var pos = stack.indexOf(k);
    return '<button class="layer-btn' + (pos >= 0 ? ' on' : '') + '" onclick="toggleLayer(\'' + k + '\')">' +
      '<span>' + escapeHtml(l.name) + '<small>' + escapeHtml(l.sub) + '</small></span>' +
      '<span class="n">' + (pos >= 0 ? 'LAYER ' + (pos + 1) : '') + '</span></button>';
  }).join('');
  $('slot-count').innerHTML = '<b>' + stack.length + ' / ' + MAX_LAYERS + '</b> layers in the stack';
  drawStack(null);
}

function toggleLayer(k) {
  if (launching) return;
  var i = stack.indexOf(k);
  if (i >= 0) stack.splice(i, 1);
  else if (stack.length < MAX_LAYERS) stack.push(k);
  else { showBanner('stack-banner', 'warn', 'The budget covers four layers. Remove one to add another.'); return; }
  hideBanner('stack-banner');
  renderStack();
}

var LANE_X = [52, 136, 220, 304];

function drawStack(shots) {
  var top = 50, slabH = 26, gap = 20;
  var h = top + Math.max(1, stack.length) * (slabH + gap) + 46;
  var svg = '<svg class="stack-svg" viewBox="0 0 356 ' + h + '" role="img" aria-label="Defense layers">';
  ATTACKS.forEach(function (a, i) {
    svg += '<text class="lane-label" x="' + LANE_X[i] + '" y="12" text-anchor="middle">' + a.name + '</text>' +
      '<text class="node-sub" x="' + LANE_X[i] + '" y="24" text-anchor="middle" style="fill:var(--gray);font-size:8.5px">' + a.sub + '</text>' +
      '<line class="lane-line" x1="' + LANE_X[i] + '" y1="28" x2="' + LANE_X[i] + '" y2="' + (h - 34) + '"/>';
  });
  stack.forEach(function (k, j) {
    var y = top + j * (slabH + gap);
    svg += '<rect class="slab" x="6" y="' + y + '" width="344" height="' + slabH + '" rx="6"/>';
    ATTACKS.forEach(function (a, i) {
      if (LAYERS[k].blocks.indexOf(a.id) === -1) {
        svg += '<rect class="hole" x="' + (LANE_X[i] - 13) + '" y="' + (y + 4) + '" width="26" height="' + (slabH - 8) + '" rx="9"/>';
      }
    });
    svg += '<text class="slab-name" x="10" y="' + (y - 5) + '">' + (j + 1) + '. ' + escapeHtml(LAYERS[k].name) + '</text>';
  });
  if (stack.length === 0) {
    svg += '<text class="goal-name" x="178" y="' + (top + 18) + '" text-anchor="middle">Add layers from the list below</text>';
  }
  svg += '<rect class="goal" x="6" y="' + (h - 30) + '" width="344" height="26" rx="6"/>' +
    '<text class="goal-name" x="178" y="' + (h - 13) + '" text-anchor="middle">LAB 3 PCs</text>';
  if (shots) {
    shots.forEach(function (s, i) {
      svg += '<circle class="shot' + (s.stopAt >= 0 ? ' stopped' : '') + '" id="shot-' + i + '" cx="' + LANE_X[i] + '" cy="30" r="7"/>';
    });
  }
  svg += '</svg>';
  $('stack').innerHTML = svg;
  if (shots) {
    setTimeout(function () {
      shots.forEach(function (s, i) {
        var y = s.stopAt >= 0 ? top + s.stopAt * (slabH + gap) + slabH / 2 : h - 17;
        var c = $('shot-' + i);
        if (c) c.setAttribute('cy', y);
        if (c) c.style.cy = y + 'px';
      });
    }, 60);
  }
}

function launch() {
  if (launching) return;
  if (stack.length === 0) { showBanner('stack-banner', 'warn', 'Add at least one layer first.'); return; }
  launching = true;
  var shots = ATTACKS.map(function (a) {
    var stopAt = -1;
    for (var j = 0; j < stack.length; j++) {
      if (LAYERS[stack[j]].blocks.indexOf(a.id) >= 0) { stopAt = j; break; }
    }
    return { stopAt: stopAt };
  });
  drawStack(shots);
  Cyberity.clueFound('stack_launched');
  setTimeout(function () {
    launching = false;
    var through = ATTACKS.filter(function (a, i) { return shots[i].stopAt < 0; });
    if (through.length === 0) {
      showBanner('stack-banner', 'good', 'All four attacks stopped, and each by a <b>different</b> layer. ' +
        'Take any one layer away and something gets through. Report code: ' +
        '<b class="mono">CYBERITY{l4y3rs_n0t_s1lv3r_bull3ts}</b>');
      Cyberity.clueFound('stack_ok');
      Cyberity.flagDiscovered('stack_code');
      return;
    }
    showBanner('stack-banner', 'bad', through.map(function (a) { return '<b>' + a.name + '</b>'; }).join(', ') +
      (through.length === 1 ? ' got' : ' got') + ' through every layer to the lab PCs. Which layer stops ' +
      (through.length === 1 ? 'it' : 'them') + '? Swap one in and launch again.');
  }, 1100);
}
