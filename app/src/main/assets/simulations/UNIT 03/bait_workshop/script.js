/* Cyberity — ITSO Bait Workshop simulation (level 302).
 *
 * The student joins the ITSO awareness team for drill season and builds the
 * pieces of a training phishing email, one trick per station, then flips
 * sides and catches a classmate's. Local only: every domain is fictional
 * (.example, or a real name shown only to explain why it can't be used),
 * links never load and nothing is ever sent.
 */

var Cyberity = (function () {
  function safe(fn) {
    try { if (typeof AndroidLab !== 'undefined') fn(); } catch (e) { /* browser preview */ }
  }
  return {
    clueFound: function (id) { safe(function () { AndroidLab.notifyClueFound(id); }); },
    flagDiscovered: function (token) { safe(function () { AndroidLab.notifyFlagDiscovered(token); }); }
  };
})();

var Store = {
  get: function (key) { try { return window.sessionStorage.getItem('bw_' + key); } catch (e) { return null; } },
  set: function (key, value) { try { window.sessionStorage.setItem('bw_' + key, value); } catch (e) {} }
};

/** Report a clue once per session and remember it for the studio checklist. */
function clue(id) {
  Cyberity.clueFound(id);
  Store.set('clue_' + id, '1');
}

function hasClue(id) { return Store.get('clue_' + id) === '1'; }

function esc(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function $(id) { return document.getElementById(id); }

var toastTimer = null;
function toast(html, kind) {
  var t = $('toast');
  if (!t) return;
  t.className = 'toast show ' + (kind || '');
  t.innerHTML = html;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(function () { t.className = 'toast'; }, 3600);
}

function shake(el) {
  if (!el) return;
  el.classList.remove('shake');
  void el.offsetWidth;
  el.classList.add('shake');
}

function banner(id, kind, html) {
  var b = $(id);
  if (!b) return;
  b.className = 'banner show ' + kind;
  b.innerHTML = html;
}

/* ===========================================================================
 * STUDIO · the hub
 * ======================================================================== */

var STATIONS = [
  { no: 1, page: 'url.html', name: 'URL builder',
    sub: 'Make an address that reads as CHED but belongs to the drill.',
    done: ['url_built', 'owner_test_passed'] },
  { no: 2, page: 'letters.html', name: 'Letter swap',
    sub: 'Try lookalikes of microsoft.com under the lens, then x-ray them.',
    done: ['lens_used', 'xray_run'] },
  { no: 3, page: 'mask.html', name: 'Mask the link',
    sub: 'Give the link one label and another destination. See what gives it away.',
    done: ['mask_built', 'reveal_longpress', 'reveal_hover', 'reveal_qr'] },
  { no: 4, page: 'spoof.html', name: 'Spoof attempt',
    sub: 'Send as the Registrar and watch the mail gateway decide.',
    done: ['spoof_forged', 'spoof_owndomain'] },
  { no: 5, page: 'flip.html', name: 'Flip sides',
    sub: 'Kim built bait for you. Catch every trick before the clock runs out.',
    done: ['flip_done'] }
];

function stationDone(s) {
  return s.done.every(hasClue);
}

function renderStudio(mountId) {
  var cards = STATIONS.map(function (s) {
    var done = stationDone(s);
    return '<a class="station' + (done ? ' done' : '') + '" href="' + s.page + '">' +
      '<span class="station-no">' + (done ? '✓' : s.no) + '</span>' +
      '<span class="station-main"><span class="station-name">' + esc(s.name) + '</span>' +
      '<span class="station-sub" style="display:block">' + esc(s.sub) + '</span></span>' +
      '<span class="chip' + (done ? ' done' : '') + '">' + (done ? 'Done' : 'Open') + '</span></a>';
  }).join('');

  $(mountId).innerHTML =
    '<div class="desk-head"><h1>Bait workshop</h1>' +
    '<p>CvSU IT Services Office · awareness team · drill season</p></div>' +
    '<div class="mentor"><span class="avatar">SN</span><span>' +
    '<b>Sir Navarro, awareness lead:</b> Next month we send a practice phishing email to the ' +
    'volunteers who signed up for drills. You build it, one trick per station. Every trick ' +
    'you make work here is one you will never fall for again.</span></div>' +
    '<div class="section-label">Stations</div>' + cards +
    '<div class="section-label">Workshop rules</div>' +
    '<div class="explain">' +
    '<div class="explain-row"><b>Domains</b><span>ITSO registered the .example drill domains. Nothing else is yours to use.</span></div>' +
    '<div class="explain-row"><b>Recipients</b><span>Drills go only to the volunteers who agreed to them.</span></div>' +
    '<div class="explain-row"><b>Goal</b><span>Know each trick well enough to spot it in a real inbox.</span></div>' +
    '</div>';
}

/* ===========================================================================
 * STATION 1 · URL builder
 * ======================================================================== */

var URL_TILES = ['ched', 'gov', 'ph', 'scholarship', 'cvsu', 'edu', 'secure', 'claim'];

var URL_DOMAINS = [
  { key: 'drill', name: 'stipend-claim.example', owned: true,
    note: 'Registered by ITSO for this drill. Yours to use.' },
  { key: 'ched', name: 'ched.gov.ph', owned: false,
    note: 'Refused. CHED owns ched.gov.ph, and .gov.ph names are only issued to government agencies.' },
  { key: 'cvsu', name: 'cvsu.edu.ph', owned: false,
    note: 'Refused. CvSU already owns it, and nobody can register a name that is taken.' }
];

var URL_PATHS = ['/claim', '/verify', '/ched.gov.ph/claim'];

var urlState = { labels: [], reg: 'drill', path: '/claim' };

function renderUrlStation(mountId) {
  try {
    var saved = JSON.parse(Store.get('url_state') || 'null');
    if (saved) urlState = saved;
  } catch (e) { /* fresh start */ }

  $(mountId).innerHTML =
    '<div class="wrap">' +
    '<h2>Make the address say CHED</h2>' +
    '<p class="lede">The drill email claims to come from the CHED scholarship office. You can ' +
    'only register names nobody owns yet, so the real CHED domain is off the table. ' +
    'Build an address a student would read as CHED at a glance.</p>' +

    '<div class="card"><div class="card-label">Your address</div>' +
    '<div class="addr" id="addr"></div>' +
    '<dl class="readout">' +
    '<dt>A student reads</dt><dd id="glance"></dd>' +
    '<dt>Real owner</dt><dd class="owner" id="owner"></dd>' +
    '</dl></div>' +

    '<div class="card"><div class="card-label">1 · Labels in front (tap to add, tap again to remove)</div>' +
    '<div class="sub-slots" id="slots"></div>' +
    '<div class="row-label">Label tiles</div><div class="tiles" id="label-tiles"></div>' +
    '<div class="row-label">2 · Registered domain (the part someone paid for)</div>' +
    '<div class="tiles" id="reg-tiles"></div>' +
    '<div class="row-label">3 · Path (after the first single slash)</div>' +
    '<div class="tiles" id="path-tiles"></div>' +
    '<div id="url-banner" class="banner"></div></div>' +

    '<div class="card" id="owner-test"><div class="card-label">Pressure test · tap the owner</div>' +
    '<p class="small" style="margin:0 0 6px">Three addresses from last year\'s drills. For each one, tap ' +
    'the piece that decides who owns the page.</p>' +
    '<div id="pieces"></div><div id="test-banner" class="banner"></div></div>' +
    '</div>';

  drawUrl();
  drawOwnerTest();
}

function urlHost() {
  var reg = URL_DOMAINS.filter(function (d) { return d.key === urlState.reg; })[0];
  return (urlState.labels.length ? urlState.labels.join('.') + '.' : '') + reg.name;
}

function glanceRead() {
  var host = urlHost();
  if (host.indexOf('ched.gov.ph') === 0) return 'CHED';
  if (host.indexOf('cvsu.edu.ph') === 0) return 'CvSU';
  if (host.indexOf('ched') === 0) return 'probably CHED…';
  if (host.indexOf('cvsu') === 0) return 'probably CvSU…';
  return host.split('.')[0];
}

function drawUrl() {
  var reg = URL_DOMAINS.filter(function (d) { return d.key === urlState.reg; })[0];
  var labels = urlState.labels.length ? urlState.labels.join('.') + '.' : '';
  $('addr').innerHTML = '<span class="scheme">https://</span>' +
    '<span class="labels">' + esc(labels) + '</span>' +
    '<span class="reg">' + esc(reg.name) + '</span>' +
    '<span class="path">' + esc(urlState.path) + '</span>';
  $('glance').textContent = glanceRead();
  $('owner').textContent = reg.name;

  $('slots').innerHTML = urlState.labels.length
    ? urlState.labels.map(function (l, i) {
        return '<button class="slot-tile" onclick="removeLabel(' + i + ')">' + esc(l) + ' ✕</button>';
      }).join('')
    : '<span class="placeholder">No labels yet. The address is just the registered domain.</span>';

  $('label-tiles').innerHTML = URL_TILES.map(function (t) {
    return '<button class="tile" onclick="addLabel(\'' + t + '\')">' + t + '</button>';
  }).join('');

  $('reg-tiles').innerHTML = URL_DOMAINS.map(function (d) {
    return '<button class="tile' + (d.key === urlState.reg ? ' on' : '') + '" id="reg-' + d.key +
      '" onclick="pickReg(\'' + d.key + '\')">' + esc(d.name) + '</button>';
  }).join('');

  $('path-tiles').innerHTML = URL_PATHS.map(function (p, i) {
    return '<button class="tile' + (p === urlState.path ? ' on' : '') + '" onclick="pickPath(' + i + ')">' +
      esc(p) + '</button>';
  }).join('');

  Store.set('url_state', JSON.stringify(urlState));
  checkUrlGoal();
}

function addLabel(t) {
  if (urlState.labels.length >= 5) { toast('Five labels is plenty. Remove one first.', 'bad'); return; }
  urlState.labels.push(t);
  drawUrl();
}

function removeLabel(i) {
  urlState.labels.splice(i, 1);
  drawUrl();
}

function pickReg(key) {
  var d = URL_DOMAINS.filter(function (x) { return x.key === key; })[0];
  if (!d.owned) {
    shake($('reg-' + key));
    banner('url-banner', 'bad', '<b>Registrar:</b> ' + esc(d.note) + ' This is the one part of an ' +
      'address you cannot fake, which is exactly why it is the part to read.');
    return;
  }
  urlState.reg = key;
  drawUrl();
}

function pickPath(i) {
  urlState.path = URL_PATHS[i];
  drawUrl();
  if (urlState.path.indexOf('ched.gov.ph') !== -1) {
    toast('Words after the first single slash are just folder names on <b>your</b> server. ' +
      'They never change the owner.', 'good');
  }
}

function checkUrlGoal() {
  var l = urlState.labels;
  var startsChed = l[0] === 'ched' && l[1] === 'gov' && l[2] === 'ph';
  if (startsChed && urlState.reg === 'drill') {
    banner('url-banner', 'good', '<b>Glance test passed.</b> Anyone who reads left to right and ' +
      'stops at the first familiar name sees CHED. The owner is still ' +
      '<b>stipend-claim.example</b>, the drill domain, sitting just before the first single slash.');
    if (!hasClue('url_built')) clue('url_built');
  } else if (urlState.labels.length && urlState.reg === 'drill') {
    banner('url-banner', 'warn', 'Close. A student skimming this would not think "CHED" yet. ' +
      'Put the agency\'s name at the very front, in the order people expect to see it.');
  } else {
    var b = $('url-banner');
    if (b && b.className.indexOf('bad') === -1) b.className = 'banner';
  }
}

/* --- pressure test --- */

var OWNER_TESTS = [
  { pieces: ['https://login', '.microsoftonline.com', '.secure-signin.example', '/auth'], owner: 2,
    why: 'secure-signin.example owns it. "login.microsoftonline.com" is three labels its owner typed in front.' },
  { pieces: ['https://portal', '.cvsu.edu.ph', '/grades'], owner: 1,
    why: 'Genuine: cvsu.edu.ph owns it. Not every address is a trap, and a checker who flags everything gets ignored.' },
  { pieces: ['http://gcash', '.com-verify.example', '/claim/gcash.com'], owner: 1,
    why: 'com-verify.example owns it. The hyphen glues ".com" onto someone else\'s name, and gcash.com after the slash is just a folder.' }
];

function drawOwnerTest() {
  var solved = JSON.parse(Store.get('owner_solved') || '[]');
  $('pieces').innerHTML = OWNER_TESTS.map(function (t, ti) {
    var done = solved.indexOf(ti) !== -1;
    return '<div class="piece-line" id="test-' + ti + '">' + t.pieces.map(function (p, pi) {
      var cls = done && pi === t.owner ? ' right' : '';
      return '<button class="piece' + cls + '" onclick="tapPiece(' + ti + ',' + pi + ', this)">' +
        esc(p) + '</button>';
    }).join('') + '</div>' +
    (done ? '<div class="small" style="margin-bottom:8px">' + esc(t.why) + '</div>' : '');
  }).join('');
  if (solved.length === OWNER_TESTS.length) {
    banner('test-banner', 'good', '<b>All three read correctly.</b> The rule never changes: find the ' +
      'first single slash, then read the registered name just before it, from the right.');
  }
}

function tapPiece(ti, pi, el) {
  var solved = JSON.parse(Store.get('owner_solved') || '[]');
  if (solved.indexOf(ti) !== -1) return;
  var t = OWNER_TESTS[ti];
  if (pi === t.owner) {
    solved.push(ti);
    Store.set('owner_solved', JSON.stringify(solved));
    drawOwnerTest();
    if (solved.length === OWNER_TESTS.length && !hasClue('owner_test_passed')) clue('owner_test_passed');
  } else {
    el.classList.add('wrong');
    shake(el);
    var p = t.pieces[pi];
    var msg = p.charAt(0) === '/'
      ? 'That is after the first single slash: a folder on the server, not the owner.'
      : (pi < t.owner ? 'That is a label someone typed in front of their own domain.'
                      : 'Not that piece. Look just before the first single slash.');
    toast(msg, 'bad');
  }
}

/* ===========================================================================
 * STATION 2 · Letter swap
 * ======================================================================== */

// "fooled" is how many of the 40 volunteers in last semester's eye test read
// the lookalike as microsoft.com on a phone-sized inbox line.
var SWAPS = [
  { id: 'rn', domain: 'rnicrosoft.com', hot: [0, 1], swap: 'r + n stand in for m', fooled: 31 },
  { id: 'zero', domain: 'micr0soft.com', hot: [4], swap: 'a zero stands in for the letter o', fooled: 17 },
  { id: 'ell', domain: 'mlcrosoft.com', hot: [1], swap: 'a lowercase L stands in for i', fooled: 24 },
  { id: 'cyr', domain: 'mіcrosoft.com', hot: [1], swap: 'a Cyrillic і (U+0456) stands in for the Latin i', fooled: 39,
    browser: 'xn--mcrosoft-thh.com' },
  { id: 'drop', domain: 'microsft.com', hot: [], swap: 'one o is simply missing', fooled: 22 }
];

var letterPick = null;
var letterZoom = 1;

function renderLetterStation(mountId) {
  $(mountId).innerHTML =
    '<div class="wrap">' +
    '<h2>One letter off</h2>' +
    '<p class="lede">The drill email imitates a Microsoft 365 notice. Below are five lookalike ' +
    'domains the team could register. Pick each one, see it the way a student sees it in a ' +
    'phone inbox, then drag the lens until the swap shows.</p>' +
    '<div class="card"><div class="card-label">Candidates</div><div id="cands"></div></div>' +
    '<div class="card"><div class="card-label">How it looks in an inbox</div>' +
    '<div class="phone-line" id="phone"></div>' +
    '<div class="lens" id="lens"></div>' +
    '<div class="zoom-row"><span>Lens</span><input type="range" id="zoom" min="1" max="4" step="0.1" value="1">' +
    '<span id="zoom-x">1.0×</span></div>' +
    '<div class="small" id="swap-info" style="margin-top:10px"></div>' +
    '<div id="inspect-count" class="small" style="margin-top:6px"></div></div>' +
    '<div class="card"><div class="card-label">Browser x-ray</div>' +
    '<p class="small" style="margin:0 0 8px">An inbox shows a domain however the sender\'s font draws it. ' +
    'A browser address bar is stricter. Run every candidate through it.</p>' +
    '<button class="cta" id="xray-btn" onclick="runXray()">RUN BROWSER X-RAY</button>' +
    '<div id="xray"></div><div id="xray-banner" class="banner"></div></div>' +
    '</div>';

  $('zoom').addEventListener('input', function () {
    letterZoom = parseFloat(this.value);
    $('zoom-x').textContent = letterZoom.toFixed(1) + '×';
    drawLens();
  });
  drawCands();
  drawLens();
  if (hasClue('xray_run')) runXray();
}

function inspected() { return JSON.parse(Store.get('swaps_seen') || '[]'); }

function drawCands() {
  var seen = inspected();
  $('cands').innerHTML = SWAPS.map(function (s) {
    var on = letterPick === s.id ? ' on' : '';
    return '<div class="cand' + on + '" onclick="pickSwap(\'' + s.id + '\')">' +
      '<span class="mono">' + esc(s.domain) + '</span>' +
      '<span class="chip' + (seen.indexOf(s.id) !== -1 ? ' done' : '') + '">' +
      (seen.indexOf(s.id) !== -1 ? 'Inspected' : 'Pick') + '</span></div>';
  }).join('');
  $('inspect-count').innerHTML = '<b>' + seen.length + ' / 5</b> inspected under the lens' +
    (seen.length < 3 ? ' · inspect at least three before the x-ray' : '');
}

function pickSwap(id) {
  letterPick = id;
  letterZoom = 1;
  $('zoom').value = 1;
  $('zoom-x').textContent = '1.0×';
  drawCands();
  drawLens();
}

function drawLens() {
  var s = SWAPS.filter(function (x) { return x.id === letterPick; })[0];
  if (!s) {
    $('phone').innerHTML = '<span class="dim">Pick a candidate above.</span>';
    $('lens').innerHTML = '<span class="dim" style="font-size:13px">The lens is empty.</span>';
    $('swap-info').innerHTML = '';
    return;
  }
  $('phone').innerHTML = '<div class="from-name">Microsoft 365</div>' +
    '<div>no-reply@' + esc(s.domain) + '</div>' +
    '<div class="dim">Action required: your password expires today</div>';

  var showSwap = letterZoom >= 2.6;
  $('lens').innerHTML = s.domain.split('').map(function (c, i) {
    var hot = showSwap && s.hot.indexOf(i) !== -1 ? ' hot' : '';
    return '<span class="ch' + hot + '" style="font-size:' + Math.round(13 * letterZoom) + 'px">' +
      esc(c) + '</span>';
  }).join('');

  var pct = Math.round(s.fooled / 40 * 100);
  $('swap-info').innerHTML = showSwap
    ? '<b>The swap:</b> ' + esc(s.swap) + '.<br>Last drill\'s eye test: fooled <b>' + s.fooled +
      ' of 40</b> volunteers.<div class="meter"><i style="width:' + pct + '%"></i></div>'
    : 'Drag the lens up until the swapped character stands out.';

  if (showSwap) {
    var seen = inspected();
    if (seen.indexOf(s.id) === -1) {
      seen.push(s.id);
      Store.set('swaps_seen', JSON.stringify(seen));
      drawCands();
      if (seen.length >= 3 && !hasClue('lens_used')) clue('lens_used');
    }
  }
}

function runXray() {
  if (inspected().length < 3) {
    shake($('xray-btn'));
    toast('Inspect at least three candidates under the lens first.', 'bad');
    return;
  }
  $('xray').innerHTML = '<table class="xray-table">' +
    '<tr><td class="small">Domain</td><td class="small">Address bar shows</td><td class="small">Fooled</td></tr>' +
    SWAPS.map(function (s) {
      var shown = s.browser
        ? '<span class="mono bad">' + esc(s.browser) + '</span>'
        : '<span class="mono">' + esc(s.domain) + '</span>';
      return '<tr><td class="mono">' + esc(s.domain) + '</td><td>' + shown + '</td><td>' + s.fooled +
        '/40</td></tr>';
    }).join('') + '</table>';
  banner('xray-banner', 'warn', '<b>The best eye-test score got caught.</b> The Cyrillic ' +
    'і fooled 39 of 40 people in an inbox, but browsers display any name that mixes ' +
    'alphabets in its raw <b>xn--</b> form, so the disguise falls apart in the address bar. ' +
    'Same-alphabet swaps like rn for m survive, which is why attackers keep using them.');
  if (!hasClue('xray_run')) clue('xray_run');
}

/* ===========================================================================
 * STATION 3 · Mask the link
 * ======================================================================== */

var MASK_TEXTS = ['View my grades', 'portal.cvsu.edu.ph', 'https://portal.cvsu.edu.ph/grades'];

var MASK_DESTS = [
  { key: 'real', url: 'https://portal.cvsu.edu.ph/grades',
    note: 'That is the real portal. A volunteer who taps it learns nothing, and the drill records no result.' },
  { key: 'drill', url: 'https://grades.cvsu-portal.example/d/CYBERITY{l4b3l_1s_n0t_l1nk}',
    note: 'The drill landing page. Anyone who reaches it gets a two-minute lesson instead of a login form.' }
];

var MASK_FORMATS = ['Text link', 'Button', 'QR code'];

var mask = { text: 0, dest: 'real', format: 0, tab: '' };

var REVEALS = [
  { key: 'longpress', label: 'Phone · hold' },
  { key: 'hover', label: 'Laptop · hover' },
  { key: 'qr', label: 'QR scanner' }
];

function maskBuilt() {
  return mask.dest === 'drill' && mask.text > 0;
}

function renderMaskStation(mountId) {
  try {
    var saved = JSON.parse(Store.get('mask_state') || 'null');
    if (saved) mask = saved;
  } catch (e) { /* fresh */ }

  $(mountId).innerHTML =
    '<div class="wrap">' +
    '<h2>The label and the link</h2>' +
    '<p class="lede">A link is two things the sender types separately: the <b>text</b> people ' +
    'see and the <b>destination</b> it opens. Make the drill link show the real portal while it ' +
    'opens the drill page. Then check it the three ways a careful student would.</p>' +
    '<div class="card"><div class="card-label">Link text people see</div><div class="tiles" id="mt"></div>' +
    '<div class="row-label">Destination it really opens</div><div class="tiles" id="md"></div>' +
    '<div class="row-label">How it appears</div><div class="tiles" id="mf"></div>' +
    '<div id="mask-banner" class="banner"></div></div>' +
    '<div class="card-label" style="padding:0 2px">Preview · what the volunteer sees</div>' +
    '<div class="mail"><div class="mail-head"><div><b>CvSU Student Portal</b> · 08:05</div>' +
    '<div class="subj">Your 1st semester grades are posted</div></div>' +
    '<div class="mail-body"><p>Hi! Your grades for the 1st semester are now available.</p>' +
    '<div id="mlink"></div><p class="dim" style="margin-top:8px">CvSU Student Portal</p></div></div>' +
    '<div class="card"><div class="card-label">Check it like a student would</div>' +
    '<div class="tabs" id="tabs"></div><div id="reveal"></div></div>' +
    '</div>';
  drawMask();
}

function drawMask() {
  $('mt').innerHTML = MASK_TEXTS.map(function (t, i) {
    return '<button class="tile' + (mask.text === i ? ' on' : '') + '" onclick="setMask(\'text\',' + i + ')">' +
      esc(t) + '</button>';
  }).join('');
  $('md').innerHTML = MASK_DESTS.map(function (d) {
    var label = d.key === 'real' ? 'Real portal' : 'Drill landing page';
    return '<button class="tile' + (mask.dest === d.key ? ' on' : '') + '" onclick="setMask(\'dest\',\'' +
      d.key + '\')">' + label + '</button>';
  }).join('');
  $('mf').innerHTML = MASK_FORMATS.map(function (f, i) {
    return '<button class="tile' + (mask.format === i ? ' on' : '') + '" onclick="setMask(\'format\',' + i + ')">' +
      f + '</button>';
  }).join('');

  var text = MASK_TEXTS[mask.text];
  var link;
  if (mask.format === 0) link = '<span class="fake-link" onclick="maskTap()">' + esc(text) + '</span>';
  else if (mask.format === 1) link = '<span class="fake-btn" onclick="maskTap()">' + esc(text) + '</span>';
  else link = qrSvg() + '<div class="small">Scan to view your grades</div>';
  $('mlink').innerHTML = link;

  var dest = MASK_DESTS.filter(function (d) { return d.key === mask.dest; })[0];
  if (maskBuilt()) {
    banner('mask-banner', 'good', '<b>Masked.</b> The text says ' + esc(text) + '. The link opens the ' +
      'drill page. Nothing in the email itself tells them apart.');
    if (!hasClue('mask_built')) clue('mask_built');
  } else if (mask.dest === 'real') {
    banner('mask-banner', 'warn', esc(dest.note));
  } else {
    banner('mask-banner', 'warn', '"View my grades" hides the destination, but it does not ' +
      'borrow the portal\'s address. Make the text <b>look like</b> the real address.');
  }

  $('tabs').innerHTML = REVEALS.map(function (r) {
    var cls = (mask.tab === r.key ? ' on' : '') + (hasClue('reveal_' + r.key) ? ' seen' : '');
    return '<button class="tab' + cls + '" onclick="showReveal(\'' + r.key + '\')">' + r.label + '</button>';
  }).join('');
  drawReveal();
  Store.set('mask_state', JSON.stringify(mask));
}

function setMask(field, value) {
  mask[field] = value;
  drawMask();
}

function maskTap() {
  toast('Links never load in the workshop. Use the checks below to see where it goes.', '');
}

function showReveal(key) {
  mask.tab = key;
  drawMask();
}

function drawReveal() {
  var dest = MASK_DESTS.filter(function (d) { return d.key === mask.dest; })[0];
  var text = MASK_TEXTS[mask.text];
  var out = '';
  if (!mask.tab) {
    out = '<p class="small" style="margin:0">Pick a check above.</p>';
  } else if (mask.tab === 'longpress') {
    out = '<div class="sheet"><div class="grab"></div>' +
      '<div class="small">Holding the link on a phone opens this preview:</div>' +
      '<div class="mono" style="margin-top:6px;font-size:13px">' + esc(dest.url) + '</div>' +
      '<div class="sheet-actions"><span>Open</span><span>Copy link</span><span>Share</span></div></div>' +
      '<p class="small">The preview shows the <b>destination</b>, not the text. ' +
      (maskBuilt() ? 'The drill landing\'s address carries this drill\'s code in its path.' : '') + '</p>';
  } else if (mask.tab === 'hover') {
    out = '<div class="small">On a laptop, resting the pointer on the link shows the destination in ' +
      'the bottom-left corner of the browser:</div>' +
      '<div class="statusbar">' + esc(dest.url) + '</div>' +
      '<p class="small">Text: <span class="mono">' + esc(text) + '</span><br>' +
      'Corner: <span class="mono">' + esc(dest.url) + '</span><br>' +
      (maskBuilt() ? '<b class="decor">They don\'t match. That mismatch is the whole trick.</b>' : '') + '</p>';
  } else {
    var host = dest.url.replace(/^https?:\/\//, '').split('/')[0];
    out = '<div class="viewfinder">' + qrSvg() + '<div class="scan-pill">' + esc(host) + '</div></div>' +
      '<p class="small">A QR code has <b>no text to compare at all</b>. The scanner flashes the ' +
      'host for a moment, and many mail filters can\'t read a link that is inside an image. ' +
      'That is why QR phishing ("quishing") is growing.</p>';
  }
  $('reveal').innerHTML = out;
  if (mask.tab && maskBuilt() && !hasClue('reveal_' + mask.tab)) {
    clue('reveal_' + mask.tab);
    $('tabs').querySelector('.tab.on').classList.add('seen');
  }
  if (mask.tab && !maskBuilt()) {
    $('reveal').innerHTML += '<p class="small decor">Finish the mask first: portal-looking text, drill destination.</p>';
  }
  if (mask.tab === 'longpress' && maskBuilt()) Cyberity.flagDiscovered('mask');
}

/** A decorative, fixed QR-style pattern. It encodes nothing. */
function qrSvg(handler) {
  var cells = '';
  var seed = 7;
  for (var y = 0; y < 21; y++) {
    for (var x = 0; x < 21; x++) {
      var finder = (x < 7 && y < 7) || (x > 13 && y < 7) || (x < 7 && y > 13);
      var on;
      if (finder) {
        var fx = x > 13 ? x - 14 : x, fy = y > 13 ? y - 14 : y;
        on = fx === 0 || fx === 6 || fy === 0 || fy === 6 || (fx > 1 && fx < 5 && fy > 1 && fy < 5);
      } else {
        seed = (seed * 9301 + 49297) % 233280;
        on = seed / 233280 > 0.52;
      }
      if (on) cells += '<rect x="' + x + '" y="' + y + '" width="1" height="1"/>';
    }
  }
  return '<svg class="qr" viewBox="0 0 21 21" fill="#111" onclick="' + (handler || 'maskTap()') + '">' + cells + '</svg>';
}

/* ===========================================================================
 * STATION 4 · Spoof attempt
 * ======================================================================== */

var SPOOF_FROMS = [
  { key: 'forged', addr: 'registrar@cvsu.edu.ph', domain: 'cvsu.edu.ph',
    label: 'registrar@cvsu.edu.ph', sub: 'The real Registrar\'s address, typed into From' },
  { key: 'own', addr: 'registrar@cvsu-registrar.example', domain: 'cvsu-registrar.example',
    label: 'registrar@cvsu-registrar.example', sub: 'ITSO\'s drill domain, with SPF and DKIM set up' }
];

var SPOOF_TO = [
  { key: 'volunteers', label: 'Drill volunteers (12)' },
  { key: 'all', label: 'All CvSU students (14,206)' }
];

var spoof = { from: 'forged', to: 'volunteers', busy: false };

function renderSpoofStation(mountId) {
  $(mountId).innerHTML =
    '<div class="wrap">' +
    '<h2>Send as the Registrar</h2>' +
    '<p class="lede">The From line is typed by whoever sends the mail. The receiving gateway ' +
    'checks it with <b>SPF</b> (is this server allowed to send for that domain?), <b>DKIM</b> (is ' +
    'there a valid signature from that domain?) and <b>DMARC</b> (do they line up with the From ' +
    'domain, and what to do if not). Try both From addresses and watch the gateway decide.</p>' +
    '<div class="card"><div class="card-label">Compose</div>' +
    '<div class="small">Display name</div><div style="font-weight:700;margin-bottom:8px">Office of the Registrar</div>' +
    '<div class="small">Sending server</div><div class="mono" style="font-size:13px;margin-bottom:8px">' +
    'drill-mailer.example · 203.0.113.25</div>' +
    '<div class="row-label">From address</div><div id="sf"></div>' +
    '<div class="row-label">Send to</div><div class="tiles" id="st"></div>' +
    '<button class="cta" id="send-btn" onclick="sendSpoof()">SEND THROUGH THE GATEWAY</button>' +
    '<div id="spoof-banner" class="banner"></div></div>' +
    '<div class="card"><div class="card-label">Receiving mail gateway · cvsu.edu.ph</div>' +
    '<div class="gate" id="gate"><span class="dim">Waiting for a message…</span></div>' +
    '<div id="inbox-view"></div></div>' +
    '<div id="spoof-lesson" class="banner"></div>' +
    '</div>';
  drawSpoof();
  if (hasClue('spoof_forged') && hasClue('spoof_owndomain')) spoofLesson();
}

function drawSpoof() {
  $('sf').innerHTML = SPOOF_FROMS.map(function (f) {
    var on = spoof.from === f.key;
    return '<div class="cand' + (on ? ' on' : '') + '" onclick="setSpoof(\'from\',\'' + f.key + '\')">' +
      '<span><span class="mono" style="font-size:13px">' + esc(f.label) + '</span>' +
      '<span class="small" style="display:block">' + esc(f.sub) + '</span></span>' +
      '<span class="chip' + (hasClue(f.key === 'forged' ? 'spoof_forged' : 'spoof_owndomain') ? ' done' : '') +
      '">' + (hasClue(f.key === 'forged' ? 'spoof_forged' : 'spoof_owndomain') ? 'Tried' : '') + '</span></div>';
  }).join('');
  $('st').innerHTML = SPOOF_TO.map(function (t) {
    return '<button class="tile' + (spoof.to === t.key ? ' on' : '') + '" onclick="setSpoof(\'to\',\'' +
      t.key + '\')">' + esc(t.label) + '</button>';
  }).join('');
}

function setSpoof(field, value) {
  if (spoof.busy) return;
  spoof[field] = value;
  drawSpoof();
}

function sendSpoof() {
  if (spoof.busy) return;
  if (spoof.to === 'all') {
    Cyberity.clueFound('sent_to_all');
    shake($('send-btn'));
    banner('spoof-banner', 'bad', '<b>Stopped by Sir Navarro.</b> 14,206 students never agreed to a ' +
      'drill. A fake Registrar email sent to all of them is not training, it is an incident: ' +
      'panic about balances, calls to the real Registrar, and a lesson that ITSO itself lies. ' +
      'Drills go only to the volunteers.');
    return;
  }
  $('spoof-banner').className = 'banner';
  var f = SPOOF_FROMS.filter(function (x) { return x.key === spoof.from; })[0];
  var forged = f.key === 'forged';
  var steps = forged ? [
    'MAIL FROM ' + f.addr + ' via 203.0.113.25',
    'SPF   cvsu.edu.ph allows 203.0.113.25?  <span class="fail">FAIL</span>',
    'DKIM  signature from cvsu.edu.ph?         <span class="none">NONE</span>',
    'DMARC cvsu.edu.ph policy p=reject         <span class="fail">FAIL</span>',
    'Authentication-Results: spf=fail dkim=none dmarc=fail header.from=cvsu.edu.ph'
  ] : [
    'MAIL FROM ' + f.addr + ' via 203.0.113.25',
    'SPF   cvsu-registrar.example allows 203.0.113.25?  <span class="pass">PASS</span>',
    'DKIM  signature d=cvsu-registrar.example           <span class="pass">PASS</span>',
    'DMARC cvsu-registrar.example aligned               <span class="pass">PASS</span>',
    'Authentication-Results: spf=pass dkim=pass dmarc=pass header.from=cvsu-registrar.example'
  ];
  spoof.busy = true;
  $('inbox-view').innerHTML = '';
  $('gate').innerHTML = steps.map(function (s) { return '<div class="step">' + s + '</div>'; }).join('') +
    '<div class="verdict step ' + (forged ? 'fail' : 'pass') + '">' +
    (forged ? '✕ REJECTED · never reached an inbox' : '✓ DELIVERED · inbox') + '</div>';
  var items = $('gate').querySelectorAll('.step');
  items.forEach(function (el, i) {
    setTimeout(function () { el.classList.add('in'); }, 120 + i * 420);
  });
  setTimeout(function () {
    spoof.busy = false;
    clue(forged ? 'spoof_forged' : 'spoof_owndomain');
    drawSpoof();
    $('inbox-view').innerHTML = forged ? '' :
      '<div class="row-label">How it lands in a volunteer\'s inbox</div>' +
      '<div class="phone-line"><div class="from-name">Office of the Registrar</div>' +
      '<div>Unpaid balance: settle before Friday</div>' +
      '<div class="dim">Same name the real Registrar uses. The address is one tap away, and ' +
      'most people never tap.</div></div>';
    if (hasClue('spoof_forged') && hasClue('spoof_owndomain')) spoofLesson();
  }, 120 + items.length * 420);
}

function spoofLesson() {
  banner('spoof-lesson', 'warn', '<b>Same display name, opposite results.</b> Forging ' +
    'cvsu.edu.ph failed every check, because only CvSU\'s servers may send for CvSU. Your own ' +
    'lookalike domain passed all three, because DMARC only proves the mail really came from ' +
    '<b>the domain in the From address</b>. It never asks whether that is the domain you trust. ' +
    '"dmarc=pass" on a lookalike is not good news.');
}

/* ===========================================================================
 * STATION 5 · Flip sides
 * ======================================================================== */

var FLIP_SECONDS = 90;
var FLIP_PENALTY = 10;

// Tells: the tricks from stations 1–4 plus the pressure they ride on.
var FLIP_TELLS = {
  lookalike: 'Lookalike sender: rn standing in for m',
  passwrong: 'DMARC passed, but for the lookalike\'s own domain',
  urgency: 'Pressure: locked in 2 hours',
  masked: 'Link text shows the portal, the destination doesn\'t',
  qr: 'A QR code: a link with no text to check'
};

// Things that look suspicious-ish but prove nothing. Tagging one costs time.
var FLIP_DECOYS = {
  greeting: 'Your name is on the class list. A greeting with your name proves nothing either way.',
  time: 'Mail arrives at every hour. The time sent is not evidence.',
  footer: 'Copying a company address into a footer is free. It proves nothing either way.',
  logo: 'A logo is just an image anyone can paste.'
};

var flip = { running: false, left: FLIP_SECONDS, found: {}, timer: null, preview: false };

function renderFlipStation(mountId) {
  $(mountId).innerHTML =
    '<div class="wrap">' +
    '<h2>Kim built one for you</h2>' +
    '<p class="lede">Kim from the awareness team built a drill email aimed at you, using every ' +
    'trick from the workshop. Start the clock and tap every <b>tell</b> before it runs out. ' +
    'Tagging something that proves nothing costs ' + FLIP_PENALTY + ' seconds.</p>' +
    '<div class="timer"><span id="found-n">0 / 5 tells</span>' +
    '<span class="clock" id="clock">1:30</span>' +
    '<button class="tile on" id="start-btn" onclick="startFlip()">START</button></div>' +
    '<div class="mail" id="flip-mail"></div>' +
    '<div class="found-list" id="found-list"></div>' +
    '<div id="flip-banner" class="banner"></div>' +
    '</div>';
  drawFlipMail();
  if (hasClue('flip_done')) flipWin(true);
}

function tagSpan(id, html, kind, extra) {
  return '<span class="tagable' + (extra ? ' ' + extra : '') + '" data-' + kind + '="' + id + '" onclick="flipTap(this)">' + html + '</span>';
}

function drawFlipMail() {
  $('flip-mail').innerHTML =
    '<div class="mail-head">' +
    '<div>' + tagSpan('logo', '<b>▦ Microsoft 365</b>', 'decoy') + ' · ' +
    tagSpan('time', '08:02', 'decoy') + '</div>' +
    '<div>From: Microsoft 365 &lt;' + tagSpan('lookalike', 'no-reply@rnicrosoft-365.example', 'tell') + '&gt;</div>' +
    '<div class="subj">Mailbox storage full: action required</div></div>' +
    '<div class="hdr-lines">' +
    tagSpan('passwrong', 'Authentication-Results: spf=pass dkim=pass dmarc=pass header.from=rnicrosoft-365.example', 'tell') +
    '</div>' +
    '<div class="mail-body">' +
    '<p>' + tagSpan('greeting', 'Hi Juan,', 'decoy') + '</p>' +
    '<p>Your CvSU mailbox is 99% full. ' +
    tagSpan('urgency', 'It will be locked in 2 hours and new mail will bounce.', 'tell') + '</p>' +
    '<p>Keep your mailbox: ' + tagSpan('masked', '<span class="fake-link">portal.cvsu.edu.ph/mailbox</span>', 'tell') + '</p>' +
    '<p>Or scan from your phone:</p>' + tagSpan('qr', qrSvg('void 0'), 'tell', 'qr-wrap') +
    '<p class="dim" style="margin-top:8px">' +
    tagSpan('footer', 'Microsoft Corporation · One Microsoft Way, Redmond', 'decoy') + '</p>' +
    '</div>';
}

function startFlip() {
  if (hasClue('flip_done')) return;
  clearInterval(flip.timer);
  flip = { running: true, left: FLIP_SECONDS, found: {}, timer: null, preview: false };
  drawFlipMail();
  document.body.classList.add('playing');
  $('flip-banner').className = 'banner';
  $('start-btn').textContent = 'RESTART';
  updateFlip();
  flip.timer = setInterval(function () {
    flip.left -= 1;
    if (flip.left <= 0) { flip.left = 0; flipLose(); }
    updateFlip();
  }, 1000);
}

function updateFlip() {
  var n = Object.keys(flip.found).length;
  $('found-n').textContent = n + ' / 5 tells';
  var m = Math.floor(flip.left / 60), s = flip.left % 60;
  var clock = $('clock');
  clock.textContent = m + ':' + (s < 10 ? '0' : '') + s;
  clock.className = 'clock' + (flip.left <= 15 ? ' low' : '');
  $('found-list').innerHTML = Object.keys(flip.found).map(function (k) {
    return '<b>✓</b> ' + esc(FLIP_TELLS[k]);
  }).join('<br>');
}

function flipTap(el) {
  if (!flip.running) {
    toast('Press START first. Nothing in this email opens.', '');
    return;
  }
  var tell = el.getAttribute('data-tell');
  var decoy = el.getAttribute('data-decoy');
  if (tell) {
    if (tell === 'masked' && !flip.preview) {
      flip.preview = true;
      toast('Hold preview: <span class="mono">https://cvsu.edu.ph.mailbox-keep.example/m</span>' +
        '<br>Tap the link again to tag it.', '');
      return;
    }
    if (flip.found[tell]) return;
    flip.found[tell] = true;
    el.classList.add('tagged');
    updateFlip();
    if (Object.keys(flip.found).length === 5) flipWin(false);
  } else if (decoy) {
    el.classList.add('miss');
    shake(el);
    flip.left = Math.max(0, flip.left - FLIP_PENALTY);
    toast('<b>−' + FLIP_PENALTY + 's · not a tell.</b> ' + esc(FLIP_DECOYS[decoy]), 'bad');
    setTimeout(function () { el.classList.remove('miss'); }, 900);
    updateFlip();
    if (flip.left <= 0) flipLose();
  }
}

function flipLose() {
  clearInterval(flip.timer);
  flip.running = false;
  document.body.classList.remove('playing');
  banner('flip-banner', 'bad', '<b>Time\'s up.</b> Kim\'s email would have landed. You found ' +
    Object.keys(flip.found).length + ' of 5. Press RESTART for a fresh clock.');
}

function flipWin(restored) {
  clearInterval(flip.timer);
  flip.running = false;
  document.body.classList.remove('playing');
  if (restored) {
    Object.keys(FLIP_TELLS).forEach(function (k) {
      flip.found[k] = true;
      var el = document.querySelector('[data-tell="' + k + '"]');
      if (el) el.classList.add('tagged');
    });
    updateFlip();
  }
  $('start-btn').style.display = 'none';
  banner('flip-banner', 'good', '<b>All five caught' + (restored ? '' : ' with ' + flip.left + 's left') +
    '.</b> A lookalike sender, a DMARC pass for the wrong domain, a 2-hour threat, a masked link ' +
    'and a QR code. Drill report code: <b class="mono">CYBERITY{b41t_fl1pp3d}</b>');
  if (!restored) {
    clue('flip_done');
    Cyberity.flagDiscovered('flip');
  }
}
