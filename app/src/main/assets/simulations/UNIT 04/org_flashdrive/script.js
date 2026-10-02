/* Cyberity — Org Flash Drive (level 402).
 * Local only. Fictional files, machines and vendors; no macro runs, no
 * installer runs, nothing touches the device this runs on. The "macro" shown
 * is readable pseudocode for teaching, not working code.
 *
 * Four tools, one per page:
 *   map.html     outbreak map + timeline, and the cleanup list
 *   macro.html   label each line of the virus
 *   wizard.html  a trojan's setup wizard: report the red flags, then cancel
 *   hash.html    compare a download's SHA-256 with the published one
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

/* Small persistent set. sessionStorage first, window.name as the fallback
   that survives a page load inside the WebView. */
var Store = {
  KEY: 'fd_state',
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

function $(id) { return document.getElementById(id); }

function showBanner(id, kind, html) {
  var b = $(id);
  b.className = 'banner show ' + kind;
  b.innerHTML = html;
  b.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/* ===========================================================================
 * 1 · OUTBREAK MAP
 *
 * The drive's week, one event per step. Scrubbing the slider moves the drive
 * between buildings and colours each computer by what happened there:
 * green = clean, amber = drive plugged in but nothing infected opened,
 * red = an infected Word document was opened, so Word's template is infected.
 * ========================================================================= */
var PCS = {
  org:   { name: 'ORGPC-01',        bldg: 'Student Center · org room', x: 10,  y: 10 },
  lib:   { name: 'LIB-PC-07',       bldg: 'University Library',        x: 186, y: 10 },
  kiosk: { name: 'PRINT-KIOSK',     bldg: 'Admin Building',            x: 10,  y: 102 },
  fac:   { name: 'ADVISER-LAPTOP',  bldg: 'Faculty Room',              x: 186, y: 102 },
  lab1:  { name: 'LAB1-PC-03',      bldg: 'Computer Lab 1',            x: 10,  y: 194 },
  home:  { name: 'TREASURER-LAPTOP', bldg: 'Off campus · treasurer\u2019s home', x: 186, y: 194, off: true }
};
var PC_ORDER = ['org', 'lib', 'kiosk', 'fac', 'lab1', 'home'];

/* state after each step: pc -> clean | visited | infected */
var EVENTS = [
  {
    when: 'Fri Aug 22 · 14:10', at: 'org', kind: 'ok',
    text: 'Officers\u2019 meeting. Minutes_Aug19.doc opened from the drive. <b>No macros ' +
      'in it.</b> Every file on the drive is still its normal size.',
    state: { org: 'clean' }
  },
  {
    when: 'Mon Aug 25 · 21:40', at: 'home', kind: 'warn',
    text: 'The treasurer downloads a <b>free budget template</b> from templates-free.example, ' +
      'saves it as <b>Org_Budget_2026.docm</b> and copies it onto the drive. She doesn\u2019t ' +
      'open it in Word yet.',
    state: { org: 'clean', home: 'visited' }
  },
  {
    when: 'Tue Aug 26 · 08:02', at: 'org', kind: 'bad',
    text: 'Org_Budget_2026.docm opened on ORGPC-01. Word shows a yellow security bar. ' +
      '<b>Someone clicks Enable Content.</b> Seconds later, Word\u2019s template ' +
      '<b>Normal.dotm changes</b>.',
    state: { org: 'infected', home: 'visited' }, zero: true
  },
  {
    when: 'Tue Aug 26 · 08:15', at: 'org', kind: 'bad',
    text: 'Minutes_Aug26.doc saved on ORGPC-01. Not a word was pasted in, yet it grows from ' +
      '<b>41 KB to 79 KB (+38 KB)</b> and now contains a macro called BudgetHelper.',
    state: { org: 'infected', home: 'visited' }
  },
  {
    when: 'Wed Aug 27 · 13:22', at: 'lib', kind: 'bad',
    text: 'At the library, the events officer opens Event_Proposal.doc to fix one line. ' +
      'It saves at <b>+38 KB</b> with the same macro, and LIB-PC-07\u2019s template changes.',
    state: { org: 'infected', home: 'visited', lib: 'infected' }
  },
  {
    when: 'Thu Aug 28 · 09:14', at: 'lab1', kind: 'bad',
    text: 'In Computer Lab 1, Sponsorship_Letter.doc is opened and printed. It saves at ' +
      '<b>+38 KB</b>. LAB1-PC-03\u2019s template changes.',
    state: { org: 'infected', home: 'visited', lib: 'infected', lab1: 'infected' }
  },
  {
    when: 'Thu Aug 28 · 15:30', at: 'kiosk', kind: 'warn',
    text: 'At the admin building\u2019s print kiosk, only <b>CSS_Logo.png</b> is printed for ' +
      'a poster. <b>No Word document is opened.</b> The kiosk\u2019s template is untouched.',
    state: { org: 'infected', home: 'visited', lib: 'infected', lab1: 'infected', kiosk: 'visited' }
  },
  {
    when: 'Fri Aug 29 · 10:05', at: 'fac', kind: 'bad',
    text: 'The adviser opens Minutes_Aug26.doc on her laptop to sign off on it. ' +
      '<b>ADVISER-LAPTOP\u2019s template changes.</b> ITSO pulls the drive at 11:00.',
    state: { org: 'infected', home: 'visited', lib: 'infected', lab1: 'infected', kiosk: 'visited', fac: 'infected' }
  }
];

var mapStep = 0;
var cleanupMode = false;
var cleanupPicks = {};
var CLEANUP_KEY = { org: true, lib: true, lab1: true, fac: true };

function renderMap() {
  var W = 164, H = 80;
  var svg = '<svg class="map-svg" viewBox="0 0 360 284" role="img" aria-label="Campus map">';
  PC_ORDER.forEach(function (k) {
    var p = PCS[k];
    svg += '<g data-pc="' + k + '" onclick="tapPc(\'' + k + '\')" style="cursor:pointer">' +
      '<rect class="bldg' + (p.off ? ' off' : '') + '" x="' + p.x + '" y="' + p.y +
        '" width="' + W + '" height="' + H + '" rx="10"/>' +
      '<text class="bldg-name" x="' + (p.x + 10) + '" y="' + (p.y + 17) + '">' + escapeHtml(p.bldg.split(' · ')[0]) + '</text>' +
      '<circle class="pc-ring" id="ring-' + k + '" cx="' + (p.x + 22) + '" cy="' + (p.y + 48) + '" r="12"/>' +
      '<circle class="pc-dot" id="dot-' + k + '" cx="' + (p.x + 22) + '" cy="' + (p.y + 48) + '" r="8"/>' +
      '<text class="pc-name" x="' + (p.x + 38) + '" y="' + (p.y + 46) + '">' + escapeHtml(p.name.replace('TREASURER-', 'TREAS-')) + '</text>' +
      '<text class="pc-state" id="st-' + k + '" x="' + (p.x + 38) + '" y="' + (p.y + 61) + '"></text>' +
    '</g>';
  });
  svg += '<g class="drive-icon" id="drive">' +
    '<rect class="drive-cap" x="0" y="3" width="6" height="8" rx="1"/>' +
    '<rect class="drive-body" x="5" y="0" width="26" height="14" rx="3"/>' +
    '<text class="drive-label" x="18" y="10" text-anchor="middle">USB</text></g>';
  svg += '</svg>';
  $('map').innerHTML = svg;
  $('tl-range').max = EVENTS.length - 1;
  setStep(0);
}

function setStep(i) {
  mapStep = Math.max(0, Math.min(EVENTS.length - 1, i));
  var ev = EVENTS[mapStep];
  $('tl-range').value = mapStep;
  $('tl-when').textContent = ev.when;
  $('tl-count').textContent = 'Event ' + (mapStep + 1) + ' of ' + EVENTS.length;
  $('tl-prev').disabled = mapStep === 0;
  $('tl-next').disabled = mapStep === EVENTS.length - 1;

  var card = $('event');
  card.className = 'event-card ' + ev.kind;
  card.innerHTML = '<div class="event-where">' + escapeHtml(PCS[ev.at].name) + ' \u00b7 ' +
    escapeHtml(PCS[ev.at].bldg) + '</div><div class="event-text">' + ev.text + '</div>';

  paintMap();
  var p = PCS[ev.at];
  $('drive').setAttribute('transform', 'translate(' + (p.x + 124) + ',' + (p.y + 8) + ')');
  $('drive').style.transform = 'translate(' + (p.x + 124) + 'px,' + (p.y + 8) + 'px)';

  Store.mark('step' + mapStep);
  if (ev.zero) Cyberity.clueFound('patient_zero_seen');
  var all = true;
  for (var s = 0; s < EVENTS.length; s++) if (!Store.has('step' + s)) all = false;
  if (all) Cyberity.clueFound('timeline_complete');
}

var STATE_TEXT = { clean: 'clean', visited: 'drive was here', infected: 'template infected' };

function paintMap() {
  var state = EVENTS[mapStep].state;
  PC_ORDER.forEach(function (k) {
    var s = cleanupMode ? '' : (state[k] || '');
    $('dot-' + k).setAttribute('class', 'pc-dot ' + s);
    $('st-' + k).textContent = cleanupMode ? (cleanupPicks[k] ? 'on the list' : '') : (STATE_TEXT[s] || 'not visited yet');
    $('ring-' + k).setAttribute('class', 'pc-ring' + (cleanupMode && cleanupPicks[k] ? ' on' : ''));
  });
}

function tapPc(k) {
  if (!cleanupMode) {
    // Outside cleanup mode, tapping a building jumps to its latest event.
    for (var i = EVENTS.length - 1; i >= 0; i--) {
      if (EVENTS[i].at === k && Store.has('step' + i)) { setStep(i); return; }
    }
    return;
  }
  cleanupPicks[k] = !cleanupPicks[k];
  renderPickList();
  paintMap();
}

function startCleanup() {
  cleanupMode = true;
  Cyberity.clueFound('cleanup_opened');
  $('timeline-box').style.display = 'none';
  $('cleanup-box').style.display = 'block';
  renderPickList();
  paintMap();
  $('cleanup-box').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function stopCleanup() {
  cleanupMode = false;
  $('timeline-box').style.display = 'block';
  $('cleanup-box').style.display = 'none';
  setStep(mapStep);
}

function renderPickList() {
  $('pick-list').innerHTML = PC_ORDER.map(function (k) {
    return '<button class="chip' + (cleanupPicks[k] ? ' on' : '') + '" onclick="tapPc(\'' + k + '\')">' +
      escapeHtml(PCS[k].name) + '</button>';
  }).join('');
}

function submitCleanup() {
  var picked = PC_ORDER.filter(function (k) { return cleanupPicks[k]; });
  var extra = picked.filter(function (k) { return !CLEANUP_KEY[k]; }).length;
  var missing = Object.keys(CLEANUP_KEY).filter(function (k) { return !cleanupPicks[k]; }).length;
  if (picked.length === 0) {
    showBanner('cleanup-banner', 'warn', 'Tap the computers that need cleaning first.');
    return;
  }
  if (extra > 0 && missing > 0) {
    showBanner('cleanup-banner', 'warn', 'Not yet. Your list includes a computer that never ' +
      'opened an infected document, and misses one that did.');
    return;
  }
  if (extra > 0) {
    showBanner('cleanup-banner', 'warn', 'Too many. At least one computer on your list had the ' +
      'drive plugged in but never opened an infected Word document.');
    return;
  }
  if (missing > 0) {
    showBanner('cleanup-banner', 'warn', 'Not everything. At least one computer where an infected ' +
      'document was opened is missing. Its template would re-infect every new document.');
    return;
  }
  showBanner('cleanup-banner', 'good', 'Cleanup list accepted: four templates to clean, every ' +
    'infected document to replace, four users to warn. Ticket code: ' +
    '<b class="mono">CYBERITY{cl34n_3v3ry_h0st}</b>');
  Cyberity.clueFound('cleanup_correct');
  Cyberity.flagDiscovered('cleanup_ticket');
}

/* ===========================================================================
 * 2 · MACRO DISSECTOR
 *
 * The BudgetHelper macro as code. Six lines do the work; the student gives
 * each one a job. Every line in a virus is doing one of four things.
 * ========================================================================= */
var MACRO = [
  { t: "' runs when the document is opened" },
  { t: 'Sub AutoOpen()' },
  { t: '  DisplayAlerts = False', id: 'a', key: 'cam' },
  { t: '  CopySelfTo NormalTemplate', id: 'b', key: 'rep' },
  { t: 'End Sub' },
  { t: '' },
  { t: "' runs every time ANY document is saved" },
  { t: 'Sub FileSave()' },
  { t: '  CopySelfTo ActiveDocument', id: 'c', key: 'rep' },
  { t: '  ActiveDocument.Save', id: 'd', key: 'cam' },
  { t: '  If Day(Today) = 15 Then', id: 'e', key: 'trg' },
  { t: '    DeleteRecentDocuments', id: 'f', key: 'pay' },
  { t: '  End If' },
  { t: 'End Sub' }
];

var BUCKETS = {
  rep: 'REPLICATION',
  trg: 'TRIGGER',
  pay: 'PAYLOAD',
  cam: 'CAMOUFLAGE'
};

var labels = {};
var selectedLine = null;

function renderMacro() {
  Cyberity.clueFound('macro_viewed');
  $('code').innerHTML = MACRO.map(function (l, i) {
    var tag = l.id && labels[l.id]
      ? '<span class="tag ' + labels[l.id] + '">' + BUCKETS[labels[l.id]] + '</span>' : '';
    var cls = l.id ? ' pickable' + (selectedLine === l.id ? ' sel' : '') : '';
    var click = l.id ? ' onclick="pickLine(\'' + l.id + '\')"' : '';
    return '<div class="code-line' + cls + '"' + click + '>' +
      '<span class="ln">' + (i + 1) + '</span><span class="txt">' + escapeHtml(l.t || ' ') + '</span>' + tag +
    '</div>';
  }).join('');
  var done = MACRO.filter(function (l) { return l.id && labels[l.id]; }).length;
  $('label-count').textContent = done + ' of 6 lines labelled';
  var off = selectedLine === null;
  ['rep', 'trg', 'pay', 'cam'].forEach(function (k) { $('b-' + k).disabled = off; });
}

function pickLine(id) {
  selectedLine = selectedLine === id ? null : id;
  renderMacro();
}

function labelAs(bucket) {
  if (selectedLine === null) return;
  labels[selectedLine] = bucket;
  selectedLine = null;
  renderMacro();
  $('macro-banner').className = 'banner';
}

function checkLabels() {
  var lines = MACRO.filter(function (l) { return l.id; });
  var unlabelled = lines.filter(function (l) { return !labels[l.id]; }).length;
  if (unlabelled > 0) {
    showBanner('macro-banner', 'warn', unlabelled + ' highlighted line' +
      (unlabelled === 1 ? ' still needs' : 's still need') + ' a label.');
    return;
  }
  var right = lines.filter(function (l) { return labels[l.id] === l.key; }).length;
  if (right < lines.length) {
    showBanner('macro-banner', 'warn', right + ' of 6 correct. Ask of each line: does it make ' +
      'a copy, decide WHEN, do the damage, or keep you from noticing?');
    return;
  }
  showBanner('macro-banner', 'good', 'Dissected. Two lines copy it, one decides when, one does ' +
    'the damage, and two keep Word looking normal. Build tag: ' +
    '<b class="mono">CYBERITY{m4cr0_tr1gg3r_p4yl04d}</b>');
  Cyberity.clueFound('macro_dissected');
  Cyberity.flagDiscovered('macro_build_tag');
}

/** Dangerous: letting the macro run. */
function enableContent() {
  showBanner('macro-banner', 'bad', 'Blocked by the simulation. "Enable Content" is the one ' +
    'click this virus needs: it runs AutoOpen, which copies the macro into Word\u2019s template. ' +
    'From then on every document saved on that computer is infected. Read macros as text; ' +
    'never enable them to find out what they do.');
  Cyberity.clueFound('content_enabled');
}

/* ===========================================================================
 * 3 · SETUP WIZARD — StatWise_2026_FULL_Activated
 *
 * A trojan's installer, screen by screen. Three screens carry a red flag; the
 * student reports each one, then cancels. Agreeing to the last screen is the
 * dangerous action.
 * ========================================================================= */
var WIZ = [
  {
    title: 'User Account Control',
    flag: 'publisher',
    flagNote: 'Unknown publisher. Real software companies sign their installers; Windows shows ' +
      'their name here.',
    body: '<div class="win-uac">Do you want to allow this app to make changes to your device?</div>' +
      '<b>StatWise_FULL_setup.exe</b>' +
      '<div class="win-row"><span>Verified publisher</span><b class="bad">Unknown</b></div>' +
      '<div class="win-row"><span>File origin</span><span>Downloaded from the internet</span></div>' +
      '<div class="win-row"><span>Came from</span><span>activator.zip, "free software" group</span></div>',
    next: 'Yes', back: null, cancel: 'No'
  },
  {
    title: 'StatWise 2026 FULL Setup',
    flag: null,
    flagNote: 'Nothing unusual here. Every installer asks where to install.',
    body: '<h3>Welcome to StatWise 2026 FULL</h3>' +
      '<div class="muted">All features unlocked. No licence needed.</div>' +
      '<div style="margin-top:10px">Install to:</div>' +
      '<div class="win-field">C:\\Program Files\\StatWise 2026</div>' +
      '<div class="muted">Space required: 412 MB</div>',
    next: 'Next >', back: '< Back', cancel: 'Cancel'
  },
  {
    title: 'StatWise 2026 FULL Setup',
    flag: 'bundle',
    flagNote: 'Extra programs ticked for you, including a "helper" that starts with Windows. ' +
      'That startup service is the part the uploader really wanted on your PC.',
    body: '<h3>Select components</h3>' +
      '<label class="win-check"><input type="checkbox" checked disabled> StatWise 2026 (required)</label>' +
      '<label class="win-check"><input type="checkbox" checked> QuickSearch toolbar<small>Sets your browser homepage</small></label>' +
      '<label class="win-check"><input type="checkbox" checked> PC Booster Pro<small>Recommended</small></label>' +
      '<label class="win-check"><input type="checkbox" checked> Remote Helper Service<small>Keeps your activation alive. Runs at startup.</small></label>',
    next: 'Next >', back: '< Back', cancel: 'Cancel'
  },
  {
    title: 'StatWise 2026 FULL Setup',
    flag: 'antivirus',
    flagNote: 'No genuine installer needs your antivirus turned off. "False positive" is what ' +
      'the trojan says so you\u2019ll ignore the one tool that recognised it.',
    body: '<h3>Security software detected</h3>' +
      '<div class="win-warn">Windows Security is blocking <b>activator.exe</b>.<br>' +
      'This is a <b>FALSE POSITIVE</b> caused by the activation patch.</div>' +
      '<div style="margin-top:8px">To finish installing, Setup will turn off real-time ' +
      'protection. You can turn it back on later.</div>',
    next: 'Disable & Continue', nextDanger: true, back: '< Back', cancel: 'Cancel'
  }
];

var wizStep = 0;
var FLAGS_TOTAL = 3;

function renderWizard() {
  Cyberity.clueFound('wizard_opened');
  var s = WIZ[wizStep];
  $('wiz').innerHTML =
    '<div class="win">' +
      '<div class="win-bar"><span>' + escapeHtml(s.title) + '</span><span class="win-x">\u2715</span></div>' +
      '<div class="win-body">' +
        (wizStep === 0 ? '' : '<div class="win-side">STATWISE FULL</div>') +
        '<div class="win-main">' + s.body + '</div>' +
      '</div>' +
      '<div class="win-btns">' +
        (s.back ? '<button class="win-btn" onclick="wizBack()">' + s.back + '</button>' : '') +
        '<button class="win-btn' + (s.nextDanger ? ' danger' : ' primary') + '" onclick="wizNext()">' + s.next + '</button>' +
        '<button class="win-btn" onclick="wizCancel()">' + s.cancel + '</button>' +
      '</div>' +
    '</div>';
  var found = ['publisher', 'bundle', 'antivirus'].filter(function (f) { return Store.has('flag_' + f); }).length;
  $('flag-count').innerHTML = '<b>' + found + ' / ' + FLAGS_TOTAL + '</b> red flags reported';
}

function reportFlag() {
  var s = WIZ[wizStep];
  if (!s.flag) {
    showBanner('wiz-banner', 'warn', s.flagNote);
    return;
  }
  var already = Store.has('flag_' + s.flag);
  Store.mark('flag_' + s.flag);
  renderWizard();
  showBanner('wiz-banner', 'good', (already ? 'Already reported. ' : 'Red flag reported. ') + s.flagNote);
  var found = ['publisher', 'bundle', 'antivirus'].filter(function (f) { return Store.has('flag_' + f); }).length;
  if (found === FLAGS_TOTAL) Cyberity.clueFound('wizard_flags_all');
}

function wizNext() {
  if (wizStep === WIZ.length - 1) {
    showBanner('wiz-banner', 'bad', 'Blocked by the simulation. You just switched off ' +
      'antivirus and installed a startup "Remote Helper Service" with administrator rights: ' +
      'a backdoor that runs every time the PC starts. The statistics program works fine, ' +
      'which is exactly why nobody would suspect it. The wizard has been reset.');
    Cyberity.clueFound('installer_run');
    wizStep = 0;
    renderWizard();
    return;
  }
  wizStep++;
  $('wiz-banner').className = 'banner';
  renderWizard();
}

function wizBack() {
  if (wizStep > 0) wizStep--;
  $('wiz-banner').className = 'banner';
  renderWizard();
}

function wizCancel() {
  var found = ['publisher', 'bundle', 'antivirus'].filter(function (f) { return Store.has('flag_' + f); }).length;
  if (found < FLAGS_TOTAL) {
    showBanner('wiz-banner', 'warn', 'Cancelled, which is the right instinct. But you\u2019ve ' +
      'reported ' + found + ' of 3 red flags. The wizard has restarted: find the rest so ' +
      'you can explain why it\u2019s a trojan, then cancel again.');
    wizStep = 0;
    renderWizard();
    return;
  }
  showBanner('wiz-banner', 'good', 'Installation cancelled, nothing installed. Unknown ' +
    'publisher, a hidden startup service bundled in, and a request to turn off your ' +
    'antivirus: three reasons this "free full version" is a trojan.');
  Cyberity.clueFound('wizard_cancelled');
  wizStep = 0;
  renderWizard();
}

/* ===========================================================================
 * 4 · HASH COMPARER
 *
 * The mirror's copy was built so its hash shares the first six and last six
 * characters with the vendor's. Matching a few characters at each end is
 * cheap for an attacker to arrange; matching all 64 is not.
 * ========================================================================= */
var PUBLISHED = 'c4591874f5ed17cceec76e6db75da8dfe33d7a0b06ead2e0155544abd515073c';

var DLS = {
  d1: {
    name: 'StatWise_Setup_2026.exe', from: 'statwise.example (the vendor\u2019s own site)',
    signer: 'StatWise Inc. (valid)', signerOk: true,
    hash: 'c4591874f5ed17cceec76e6db75da8dfe33d7a0b06ead2e0155544abd515073c'
  },
  d2: {
    name: 'StatWise_Setup_2026 (1).exe', from: 'fastdl-mirror.example (a download mirror)',
    signer: 'none (unsigned)', signerOk: false,
    hash: 'c45918ec783c880472dacf322ac59c0b77322223f8bf11549a66678f1415073c'
  }
};

var currentDl = 'd1';
var revealed = {};

function renderHash() {
  Cyberity.clueFound('hash_viewed');
  var d = DLS[currentDl];
  $('sw-d1').className = 'chip' + (currentDl === 'd1' ? ' on' : '');
  $('sw-d2').className = 'chip' + (currentDl === 'd2' ? ' on' : '');
  $('dl-info').innerHTML =
    '<div class="board-name mono">' + escapeHtml(d.name) + '</div>' +
    '<div class="board-hint">from ' + escapeHtml(d.from) + '</div>' +
    '<div class="board-hint">signed by: <b style="color:var(--' + (d.signerOk ? 'green' : 'red') + ')">' +
      escapeHtml(d.signer) + '</b></div>';

  var html = '';
  for (var r = 0; r < 4; r++) {
    var pub = '', dl = '';
    for (var c = 0; c < 16; c++) {
      var i = r * 16 + c;
      pub += '<span class="hg-cell">' + PUBLISHED.charAt(i) + '</span>';
      var cls = '';
      if (revealed[currentDl]) cls = PUBLISHED.charAt(i) === d.hash.charAt(i) ? ' same' : ' diff';
      dl += '<span class="hg-cell' + cls + '" id="c' + i + '" onclick="tapChar(' + i + ')">' + d.hash.charAt(i) + '</span>';
    }
    html += '<div class="hg-pair"><div class="hg-row pub">' + pub + '</div><div class="hg-row dl">' + dl + '</div></div>';
  }
  $('grid').innerHTML = html;
  $('hash-banner').className = 'banner';
}

function switchDl(k) { currentDl = k; renderHash(); }

function nope(i) {
  var el = $('c' + i);
  el.classList.remove('nope'); void el.offsetWidth; el.classList.add('nope');
}

function firstDiff(k) {
  var h = DLS[k].hash;
  for (var i = 0; i < 64; i++) if (h.charAt(i) !== PUBLISHED.charAt(i)) return i;
  return -1;
}

function tapChar(i) {
  var d = DLS[currentDl];
  if (revealed[currentDl]) return;
  if (d.hash.charAt(i) === PUBLISHED.charAt(i)) {
    nope(i);
    showBanner('hash-banner', 'warn', 'Character ' + (i + 1) + ' is the same in both. Keep comparing.');
    return;
  }
  if (i !== firstDiff(currentDl)) {
    nope(i);
    showBanner('hash-banner', 'warn', 'That one differs, but there\u2019s an earlier difference. ' +
      'Find the FIRST character that doesn\u2019t match.');
    return;
  }
  revealed[currentDl] = true;
  renderHash();
  var n = 0;
  for (var j = 0; j < 64; j++) if (d.hash.charAt(j) !== PUBLISHED.charAt(j)) n++;
  showBanner('hash-banner', 'bad', 'Found it: character ' + (i + 1) + '. In total <b>' + n +
    ' of 64</b> characters differ. The first six and last six match, and that is no ' +
    'accident: an attacker can cheaply tweak a file until the ends of its hash look right, ' +
    'betting you only glance at them. <b>This file is not the vendor\u2019s.</b>');
  Cyberity.clueFound('hash_' + currentDl + '_mismatch');
}

function sayIdentical() {
  if (currentDl === 'd2') {
    showBanner('hash-banner', 'warn', 'Look again, past the first and last few characters. ' +
      'Compare every one.');
    return;
  }
  revealed.d1 = true;
  renderHash();
  showBanner('hash-banner', 'good', 'Identical, all 64 characters. Together with the valid ' +
    'StatWise Inc. signature and the vendor\u2019s own site, this is the copy to install.');
  Cyberity.clueFound('hash_d1_match');
}
