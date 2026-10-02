/* Cyberity — Bonus XP Cache (level 450).
 * A reward, not a test: four single-digit clues from 401-404 open a
 * combination lock, and the chest deals out a field guide of every malware
 * family in Unit 4. Wrong codes only shake the lock; nothing costs a heart.
 */

var Cyberity = (function () {
  function safe(fn) {
    try { if (typeof AndroidLab !== 'undefined') fn(); } catch (e) { /* preview */ }
  }
  return {
    clueFound: function (id) { safe(function () { AndroidLab.notifyClueFound(id); }); }
  };
})();

function $(id) { return document.getElementById(id); }

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* ---------------------------------------------------------------------------
 * The four clues — one per level, each answer a single digit
 * ------------------------------------------------------------------------ */
var CLUES = [
  { q: 'In the quarantine vault, how many of the five flagged files were NOT malware?',
    from: '401 · What is Malware?', a: 1 },
  { q: 'In the fake StatWise setup wizard, how many red flags did you report?',
    from: '402 · Viruses & Trojans', a: 3 },
  { q: 'How many of CEIT’s five "backups" survived the ransomware?',
    from: '403 · Ransomware', a: 2 },
  { q: 'In the defense stack, how many attacks did you have to stop?',
    from: '404 · Malware Prevention', a: 4 }
];

var digits = [0, 0, 0, 0];
var known = [false, false, false, false];
var opened = false;

function renderCache() {
  Cyberity.clueFound('cache_viewed');
  $('wheels').innerHTML = digits.map(function (d, i) {
    return '<div class="wheel">' +
        '<span class="wheel-n">' + (i + 1) + '</span>' +
        '<button onclick="spin(' + i + ',1)" aria-label="up">▲</button>' +
        '<div class="digit' + (known[i] ? ' right' : '') + '" id="d' + i + '">' + d + '</div>' +
        '<button onclick="spin(' + i + ',-1)" aria-label="down">▼</button>' +
      '</div>';
  }).join('');
  $('clues').innerHTML = CLUES.map(function (c, i) {
    return '<div class="clue' + (known[i] ? ' right' : '') + '">' +
        '<span class="clue-n">' + (known[i] ? '✓' : i + 1) + '</span>' +
        '<div><div class="clue-q">' + escapeHtml(c.q) + '</div>' +
        '<div class="clue-from">' + escapeHtml(c.from) + '</div></div>' +
      '</div>';
  }).join('');
}

function spin(i, dir) {
  if (opened) return;
  digits[i] = (digits[i] + dir + 10) % 10;
  known[i] = false;
  $('cache-banner').className = 'banner';
  renderCache();
}

function tryLock() {
  if (opened) return;
  var right = 0;
  CLUES.forEach(function (c, i) {
    known[i] = digits[i] === c.a;
    if (known[i]) right++;
  });
  renderCache();
  if (right < CLUES.length) {
    var chest = $('chest');
    chest.classList.remove('shake'); void chest.getBoundingClientRect(); chest.classList.add('shake');
    var b = $('cache-banner');
    b.className = 'banner show warn';
    b.innerHTML = right === 0
      ? 'The lock doesn’t budge. Read the clues again; each answer is a single digit.'
      : '<b>' + right + ' of 4</b> digits are right; they’ve turned green. Fix the others and try again.';
    return;
  }
  openChest();
}

function openChest() {
  opened = true;
  $('chest').classList.add('open');
  $('xp-pop').classList.add('show');
  $('lock-box').style.display = 'none';
  $('cache-banner').className = 'banner show good';
  $('cache-banner').innerHTML = 'Unlocked! The cache holds your XP and a field guide to every ' +
    'kind of malware in Unit 4. <b>Tap a card</b> to flip it.';
  Cyberity.clueFound('cache_opened');
  setTimeout(dealCards, 900);
}

/* ---------------------------------------------------------------------------
 * The field guide — one card per family met in this unit
 * ------------------------------------------------------------------------ */
var CARDS = [
  { sym: 'AD', name: 'Adware', lv: '401', does: 'Floods you with ads and hijacks your browser.', stop: 'Install apps only from the official store.' },
  { sym: 'SP', name: 'Spyware', lv: '401', does: 'Secretly records what you type and sends it out.', stop: 'Check permissions: a keyboard never needs accessibility.' },
  { sym: 'CM', name: 'Cryptominer', lv: '401', does: 'Uses your processor to mine coins for someone else.', stop: 'Watch for heat and battery drain; remove unknown apps.' },
  { sym: 'BT', name: 'Bot', lv: '401', does: 'Waits for remote orders: spam, floods, attacks.', stop: 'Allowlisting and patching keep it from running.' },
  { sym: 'VR', name: 'Virus', lv: '402', does: 'Copies itself into files and spreads when they’re opened.', stop: 'Never click Enable Content on a document you didn’t expect.' },
  { sym: 'TJ', name: 'Trojan', lv: '402', does: 'Hides inside a program you choose to install.', stop: 'Official site, valid signature, matching hash.' },
  { sym: 'RW', name: 'Ransomware', lv: '403', does: 'Encrypts files, steals a copy, demands payment.', stop: 'Offline or immutable backups; isolate fast; never pay.' },
  { sym: 'WM', name: 'Worm', lv: '405', does: 'Spreads over the network with nobody clicking.', stop: 'Patch on time and segment the network.' }
];
var flipped = {};

function dealCards() {
  $('cards-label').style.display = 'block';
  $('cards').innerHTML = CARDS.map(function (c, i) {
    return '<div class="card" id="card' + i + '" onclick="flip(' + i + ')"><div class="card-in">' +
        '<div class="face front"><span class="sym">' + c.sym + '</span><span class="nm">' + escapeHtml(c.name) + '</span>' +
          '<span class="lv">Level ' + c.lv + ' · tap to flip</span></div>' +
        '<div class="face back"><b>' + escapeHtml(c.name) + '</b>' +
          '<span class="lbl">WHAT IT DOES</span>' + escapeHtml(c.does) +
          '<span class="lbl stop">HOW TO STOP IT</span>' + escapeHtml(c.stop) + '</div>' +
      '</div></div>';
  }).join('');
  CARDS.forEach(function (c, i) {
    setTimeout(function () { $('card' + i).classList.add('dealt'); }, 110 * i);
  });
  paintFlips();
  setTimeout(function () { $('cards-label').scrollIntoView({ behavior: 'smooth', block: 'start' }); }, 300);
}

function flip(i) {
  $('card' + i).classList.toggle('flipped');
  flipped[i] = true;
  paintFlips();
}

function paintFlips() {
  var n = Object.keys(flipped).length;
  $('flip-count').textContent = n === CARDS.length
    ? 'All 8 read. The worm card is the one you’ll meet next, in Lab PC 14.'
    : n + ' of 8 cards flipped';
  if (n === CARDS.length) Cyberity.clueFound('guide_read');
}
