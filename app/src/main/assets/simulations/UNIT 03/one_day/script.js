/* Cyberity — One Day, One Phone (level 305, the Unit 3 capstone).
 *
 * The student's own phone, from 07:40 to 21:00 in fast time. Eight things
 * arrive across email, Messenger, a canteen poster and a live call; three
 * are genuine. Each part of the day is one lab task (phone.html#b1 to
 * #b5), and #replay ends the day by sorting it into campaigns.
 *
 * Dangerous choices cost a heart AND change the rest of the day: a password
 * typed into a fake page brings a sign-in alert, the poster form hands the
 * caller your details, and the "remote help" app empties your wallet.
 * Local only: every person, number and domain is fictional.
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

/* ---------------------------------------------------------------------------
 * State, kept for the whole lab run so earlier parts of the day stay put
 * ------------------------------------------------------------------------ */

var S = {
  block: 0,          // furthest part of the day reached (1-5, 6 = replay)
  arrived: [],       // event ids that have arrived
  verdicts: {},      // id -> 'safe' | 'report'
  clues: {},         // clue id -> true
  danger: {},        // dangerous action id -> time it happened
  used: {},          // "eventId:tool" -> true
  groups: {},        // replay: id -> 'a' | 'b' | 'g'
  replayDone: false,
  ringing: false
};

function load() {
  try {
    var raw = window.sessionStorage.getItem('od_state');
    if (raw) {
      var saved = JSON.parse(raw);
      for (var k in saved) S[k] = saved[k];
    }
  } catch (e) { /* fresh day */ }
}

function save() {
  try { window.sessionStorage.setItem('od_state', JSON.stringify(S)); } catch (e) {}
}

function clue(id) {
  if (S.clues[id]) return;
  S.clues[id] = true;
  save();
  Cyberity.clueFound(id);
}

/** Dangerous action: costs a heart in the app (once) and is remembered for the rest of the day. */
function danger(id) {
  if (!S.danger[id]) {
    S.danger[id] = nowTime();
    save();
  }
  Cyberity.clueFound(id);
}

function used(eventId, tool) { return !!S.used[eventId + ':' + tool]; }
function markUsed(eventId, tool) { S.used[eventId + ':' + tool] = true; save(); }

function esc(text) {
  return String(text)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function $(id) { return document.getElementById(id); }

var toastTimer = null;
function toast(html, kind) {
  var t = $('toast');
  t.className = 'toast show ' + (kind || '');
  t.innerHTML = html;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(function () { t.className = 'toast'; }, 4200);
}

function shake(el) {
  if (!el) return;
  el.classList.remove('shake');
  void el.offsetWidth;
  el.classList.add('shake');
}

/* ---------------------------------------------------------------------------
 * The day
 * ------------------------------------------------------------------------ */

var BLOCKS = {
  1: { start: '07:40', label: 'Morning' },
  2: { start: '11:30', label: 'Midday' },
  3: { start: '13:05', label: 'Lunch' },
  4: { start: '15:20', label: 'Afternoon' },
  5: { start: '18:00', label: 'Evening' }
};

var ATTACKER_IP = '198.51.100.73';
var REGISTRANT_A = 'regbox88@mail.example';

// What a domain lookup (WHOIS) returns for each link's owner. Campaign 1's
// three domains share one registrant, which is how T5 ties them together.
var WHOIS = {
  'records-update.example': { created: '5 October 2026', registrar: 'QuickNames Ltd.', contact: REGISTRANT_A },
  'cvsu-scholarship-portal.example': { created: '4 October 2026', registrar: 'QuickNames Ltd.', contact: REGISTRANT_A },
  'drive-cvsu.example': { created: '5 October 2026', registrar: 'QuickNames Ltd.', contact: REGISTRANT_A },
  'cvsu-freeload.example': { created: '30 September 2026', registrar: 'CheapDomainz', contact: 'free.load.promo@mail.example' },
  'cvsu.edu.ph': { created: '18 March 1998', registrar: 'PH Domain Registry (.edu.ph)', contact: 'Cavite State University' },
  'google.com': { created: '15 September 1997', registrar: 'MarkMonitor Inc.', contact: 'Google LLC' }
};

var EVENTS = [
  { id: 'e1', block: 1, time: '07:40', app: 'mail', truth: 'genuine', group: 'g',
    fromName: 'Google Classroom', fromAddr: 'no-reply@classroom.google.com',
    subject: 'Prof. Dimaano posted a new announcement in CS 311',
    body: ['CS 311 · Data Communications and Networking', 'Prof. Liza Dimaano:',
      '"Good morning, class! Today\'s 10:00 session moves to Room 204, CEIT Building, because of the ' +
        'aircon repair. Bring your lab notebooks."'],
    link: { text: 'Open', url: 'https://classroom.google.com/c/NjA4MzE2/p/announce', owner: 'google.com',
      ip: '', page: 'classroom', button: true },
    sign: 'Google Classroom · you received this because you are enrolled in CS 311',
    auth: 'spf=pass dkim=pass dmarc=pass header.from=classroom.google.com',
    appCheck: true,
    asked: 'nothing', ind: 'Email · link → classroom.google.com · asked for nothing · same post inside the Classroom app',
    wrong: 'This one is real. Open the Classroom app: Prof. Dimaano posted the same announcement there, and the email asks you for nothing.' },

  { id: 'e2', block: 1, time: '09:15', app: 'mail', truth: 'phish', group: 'a',
    fromName: 'Office of the Registrar', fromAddr: 'registrar@cvsu-records.example',
    subject: 'Update your student record before Friday',
    body: ['Good morning,',
      'All students must confirm their student record before enrollment opens on Friday. ' +
        'Records not confirmed will be placed on hold.'],
    link: { text: 'portal.cvsu.edu.ph/records', url: 'https://portal.cvsu.edu.ph.records-update.example/login',
      owner: 'records-update.example', ip: ATTACKER_IP, page: 'login' },
    sign: 'Office of the Registrar',
    auth: 'spf=pass dkim=pass dmarc=pass header.from=cvsu-records.example',
    asked: 'your CvSU login', ind: 'Email · link → portal.cvsu.edu.ph.records-update.example · registered 5 Oct by ' + REGISTRANT_A + ' · wanted your CvSU login',
    wrong: 'Hold the link and read who owns the destination. Then look at which domain the checks passed for.' },

  { id: 'e3', block: 2, time: '11:30', app: 'msgr', truth: 'phish', group: 'a',
    from: 'CvSU Scholarship Office', preview: 'Congratulations! You are shortlisted for the ₱5,000 allowance',
    text: 'Congratulations, Juan! 🎓 You are shortlisted for the ₱5,000 CvSU Digital Learning Allowance. ' +
      'Confirm with your CvSU account before 5:00 PM today or your slot goes to the next student.',
    link: { text: 'cvsu-scholarship-portal.example/confirm', url: 'https://cvsu-scholarship-portal.example/confirm',
      owner: 'cvsu-scholarship-portal.example', ip: ATTACKER_IP, page: 'login' },
    asked: 'your CvSU login', ind: 'Messenger, from a Page renamed "CvSU Scholarship Office" 3 days ago · link → cvsu-scholarship-portal.example · registered 4 Oct by ' + REGISTRANT_A + ' · wanted your CvSU login',
    wrong: 'The name and logo say CvSU. Check who is behind the Page, and find the official one yourself.' },

  { id: 'e4', block: 3, time: '13:05', app: 'camera', truth: 'phish', group: 'b',
    preview: 'Poster at the canteen: FREE 50GB for CvSU students',
    qr: { host: 'cvsu-freeload.example', url: 'https://cvsu-freeload.example/register' },
    asked: 'your name, number and course', ind: 'Canteen poster · QR → cvsu-freeload.example · form wanted name, mobile number and course',
    wrong: 'Scan it and read the address. Then ask: who owns that, and what does it want from you?' },

  { id: 'e5', block: 3, time: '13:40', app: 'mail', truth: 'genuine', group: 'g',
    fromName: 'CvSU University Library', fromAddr: 'library@cvsu.edu.ph',
    subject: 'Reminder: 1 book due tomorrow',
    body: ['Hi Juan,', '"Network Security Essentials" is due tomorrow, 8 October. You can renew it once ' +
      'from your library account.'],
    link: { text: 'library.cvsu.edu.ph/account', url: 'https://library.cvsu.edu.ph/account',
      owner: 'cvsu.edu.ph', ip: '', page: 'library' },
    sign: 'CvSU University Library',
    auth: 'spf=pass dkim=pass dmarc=pass header.from=cvsu.edu.ph',
    asked: 'nothing new', ind: 'Email · link → library.cvsu.edu.ph · asked for nothing new',
    wrong: 'Read where its link goes and what it wants. This one goes to CvSU\'s own library and asks for nothing.' },

  { id: 'e6', block: 4, time: '15:20', app: 'call', truth: 'phish', group: 'b',
    from: '+63 917 555 0144', preview: 'Incoming call',
    asked: 'to install an app', ind: 'Call from 0917 555 0144 · "LinkPH promo team" · knew your name, course and number · wanted you to install an app',
    wrong: 'What did LinkPH say when you called the hotline from their own app?' },

  { id: 'e7', block: 5, time: '18:00', app: 'mail', truth: 'phish', group: 'a',
    fromName: 'Bea Lacson', fromAddr: 'b.lacson@cvsu.edu.ph',
    subject: 'Thesis_Chapter3_final — shared with you',
    body: ['Hi! Pakicheck naman ng Chapter 3 bago ko i-submit kay Sir 🙏', 'Thanks!!'],
    link: { text: 'Open in Drive', url: 'https://drive-cvsu.example/s/ch3', owner: 'drive-cvsu.example',
      ip: ATTACKER_IP, page: 'login', button: true },
    sign: 'Bea',
    auth: 'spf=pass dkim=pass dmarc=pass header.from=cvsu.edu.ph',
    asked: 'your CvSU login', ind: 'Email from Bea\'s real account · link → drive-cvsu.example · registered 5 Oct by ' + REGISTRANT_A + ' · wanted your CvSU login',
    wrong: 'The checks passed because it really came from Bea\'s account. Hold the link, look up its domain, and ask: who registered it?' },

  { id: 'e8', block: 5, time: '19:30', app: 'mail', truth: 'genuine', group: 'g',
    fromName: 'CvSU IT Services Office', fromAddr: 'itso@cvsu.edu.ph',
    subject: 'Portal maintenance tonight, 11 PM to 1 AM',
    body: ['The student portal will be unavailable tonight from 11:00 PM to 1:00 AM for scheduled ' +
      'maintenance.', 'No action is needed from you.'],
    sign: 'CvSU IT Services Office',
    auth: 'spf=pass dkim=pass dmarc=pass header.from=cvsu.edu.ph',
    asked: 'nothing', ind: 'Email · no link · asked for nothing',
    wrong: 'It has no link, asks for nothing, and passed the checks for cvsu.edu.ph. Nothing about it needs reporting.' }
];

function ev(id) { return EVENTS.filter(function (e) { return e.id === id; })[0]; }
function arrived(id) { return S.arrived.indexOf(id) !== -1; }
function eventsIn(block) { return EVENTS.filter(function (e) { return e.block === block; }); }

function nowTime() {
  if (S.block >= 6) return '21:00';
  var last = null;
  EVENTS.forEach(function (e) { if (arrived(e.id)) last = e.time; });
  return last || (BLOCKS[S.block] ? BLOCKS[S.block].start : '07:40');
}

function minutes(t) { var p = t.split(':'); return parseInt(p[0], 10) * 60 + parseInt(p[1], 10); }

/* ---------------------------------------------------------------------------
 * Boot and arrivals
 * ------------------------------------------------------------------------ */

var view = { name: 'home', id: null, app: null };
var arrivalQueue = [];
var arrivalRun = 0;   // bumped on every route, so a stale arrival timer from an earlier part of the day stops

function boot() {
  load();
  route();
  window.addEventListener('hashchange', route);
}

function route() {
  arrivalRun++;
  arrivalQueue = [];
  var n = $('notif');
  if (n) n.className = 'notif';
  var h = (window.location.hash || '#b1').substring(1);
  var target = h === 'replay' ? 6 : (parseInt(h.replace('b', ''), 10) || 1);
  if (target > S.block) S.block = target;
  save();

  // Everything from earlier parts of the day is already there.
  EVENTS.forEach(function (e) {
    if (e.block < target && !arrived(e.id)) S.arrived.push(e.id);
  });
  save();

  if (target === 6) {
    view = { name: 'replay' };
    render();
    return;
  }

  view = { name: 'home' };
  render();

  // This part's messages arrive one by one, as notifications.
  arrivalQueue = eventsIn(target).filter(function (e) { return !arrived(e.id); });
  var run = arrivalRun;
  setTimeout(function () { nextArrival(run); }, 900);
}

function nextArrival(run) {
  if (run !== arrivalRun) return;
  var e = arrivalQueue.shift();
  if (!e) return;
  S.arrived.push(e.id);
  save();
  if (e.app === 'call') {
    S.ringing = true;
    save();
    renderClock();
    openIncomingCall();
  } else {
    notify(e);
    render();
  }
  if (arrivalQueue.length) setTimeout(function () { nextArrival(run); }, 2600);
}

var APP_META = {
  sms: { name: 'Messages', icon: '💬', cls: 'ic-sms' },
  mail: { name: 'Mail', icon: '✉', cls: 'ic-mail' },
  msgr: { name: 'Messenger', icon: '⚡', cls: 'ic-msgr' },
  call: { name: 'Phone', icon: '✆', cls: 'ic-phone' },
  camera: { name: 'Camera', icon: '◉', cls: 'ic-cam' },
  gcash: { name: 'GCash', icon: 'G', cls: 'ic-gcash' },
  classroom: { name: 'Classroom', icon: '▣', cls: 'ic-class' }
};

function notify(e) {
  var m = APP_META[e.app];
  var title = e.app === 'mail' ? e.fromName : (e.app === 'camera' ? 'Camera · canteen' : e.from);
  var line = e.app === 'mail' ? e.subject : e.preview;
  var n = $('notif');
  n.innerHTML = '<span class="app-icon ' + m.cls + '">' + m.icon + '</span>' +
    '<span class="notif-text"><b>' + esc(title) + ' · ' + e.time + '</b><span>' + esc(line) + '</span></span>';
  n.onclick = function () { n.className = 'notif'; openEvent(e.id); };
  n.className = 'notif show';
  setTimeout(function () { n.className = 'notif'; }, 3200);
}

/* ---------------------------------------------------------------------------
 * Rendering
 * ------------------------------------------------------------------------ */

function renderClock() { $('clock').textContent = nowTime(); }

function render() {
  renderClock();
  var s = $('screen');
  if (view.name === 'home') s.innerHTML = homeHtml();
  else if (view.name === 'list') s.innerHTML = listHtml(view.app);
  else if (view.name === 'event') s.innerHTML = eventHtml(ev(view.id));
  else if (view.name === 'gcash') s.innerHTML = gcashHtml();
  else if (view.name === 'classroom') s.innerHTML = classroomHtml();
  else if (view.name === 'phone') s.innerHTML = phoneHtml();
  else if (view.name === 'replay') s.innerHTML = replayHtml();
  window.scrollTo(0, 0);
}

function go(name, extra) {
  view = { name: name };
  for (var k in (extra || {})) view[k] = extra[k];
  render();
}

function pending(app) {
  return EVENTS.filter(function (e) {
    return e.app === app && arrived(e.id) && !S.verdicts[e.id];
  }).length;
}

function appButton(key) {
  var m = APP_META[key];
  var count = (key === 'gcash' || key === 'classroom') ? 0 : pending(key);
  return '<button class="app" onclick="openApp(\'' + key + '\')">' +
    '<span class="app-icon ' + m.cls + '">' + m.icon + '</span>' + m.name +
    (count ? '<span class="badge">' + count + '</span>' : '') + '</button>';
}

function homeHtml() {
  var block = S.block;
  var mine = eventsIn(block);
  var left = mine.filter(function (e) { return !S.verdicts[e.id]; }).length;
  var waiting = arrivalQueue.length;
  var todo;
  if (left || waiting) {
    todo = '<b>' + BLOCKS[block].label + '.</b> Deal with everything that arrives: decide whether ' +
      'each one is <b>genuine</b> or should be <b>reported to ITSO</b>. ' + left + ' still to decide here.';
  } else {
    var next = BLOCKS[block + 1];
    todo = '<b>All clear for now.</b> ' + (next ? 'Nothing else arrives until ' + next.start +
      '. Swipe the bar down and answer the task.' : 'That was the last message of the day. ' +
      'Swipe the bar down and answer the task.');
  }
  return dayStrip() +
    '<div class="home-greet"><h1>' + nowTime() + '</h1><p>Tuesday, 7 October · CvSU Indang</p></div>' +
    '<div class="apps">' + ['mail', 'msgr', 'classroom', 'call', 'camera', 'gcash'].map(appButton).join('') + '</div>' +
    '<div class="todo">' + todo + '</div>';
}

function dayStrip() {
  var start = minutes('07:00'), end = minutes('21:00');
  var pct = function (t) { return ((minutes(t) - start) / (end - start) * 100).toFixed(1); };
  var dots = EVENTS.map(function (e) {
    var cls = S.verdicts[e.id] ? ' done' : (arrived(e.id) ? ' arrived' : '');
    return '<span class="daystrip-dot' + cls + '" style="left:' + pct(e.time) + '%"></span>';
  }).join('');
  return '<div class="daystrip"><div class="daystrip-track">' +
    '<div class="daystrip-fill" style="width:' + pct(nowTime()) + '%"></div>' + dots + '</div>' +
    '<div class="daystrip-labels"><span>07:00</span><span>12:00</span><span>17:00</span><span>21:00</span></div></div>';
}

function appBar(title, back) {
  return '<div class="appbar"><button onclick="' + (back || "go('home')") + '">&#8592;</button>' +
    '<span class="title">' + esc(title) + '</span></div>';
}

function openApp(key) {
  if (key === 'gcash') return go('gcash');
  if (key === 'classroom') return go('classroom');
  if (key === 'call') return go('phone');
  if (key === 'camera') {
    if (arrived('e4')) return openEvent('e4');
    $('screen').innerHTML = appBar('Camera') + '<div class="empty">Nothing worth photographing yet.</div>';
    return;
  }
  go('list', { app: key });
}

function chip(id) {
  var v = S.verdicts[id];
  if (!v) return '';
  return '<span class="verdict-chip ' + v + '">' + (v === 'safe' ? 'Genuine' : 'Reported') + '</span>';
}

function listHtml(app) {
  var rows = EVENTS.filter(function (e) { return e.app === app && arrived(e.id); }).reverse();
  var html = appBar(APP_META[app].name);
  if (app === 'sms') rows = rows.concat([{ id: 'old1', static: true }]);
  if (!rows.length) return html + '<div class="empty">Nothing here yet.</div>';
  return html + rows.map(function (e) {
    if (e.static) {
      return '<div class="list-row"><span class="avatar">M</span><span class="list-main">' +
        '<span class="list-top">Mama<span class="t">Yesterday</span></span>' +
        '<span class="list-sub">Ingat sa biyahe anak. Kumain ka bago umalis.</span></span></div>';
    }
    var name = e.fromName || e.from;
    var sub = e.app === 'mail' ? e.subject : e.preview;
    return '<div class="list-row' + (S.verdicts[e.id] ? '' : ' unread') + '" onclick="openEvent(\'' + e.id + '\')">' +
      '<span class="avatar">' + esc(name.charAt(0)) + '</span><span class="list-main">' +
      '<span class="list-top"><span>' + esc(name) + chip(e.id) + '</span><span class="t">' + e.time + '</span></span>' +
      '<span class="list-sub">' + esc(sub) + '</span></span></div>';
  }).join('');
}

function openEvent(id) {
  var e = ev(id);
  if (e.app === 'call') return go('phone');
  go('event', { id: id });
}

/* ---------------------------------------------------------------------------
 * One message, with its tools and the verdict bar
 * ------------------------------------------------------------------------ */

function toolBtn(e, key, label, risky) {
  var cls = 'tool' + (used(e.id, key) ? ' used' : '') + (risky ? ' risky' : '');
  return '<button class="' + cls + '" onclick="useTool(\'' + e.id + '\',\'' + key + '\')">' + label + '</button>';
}

function linkHtml(e) {
  if (!e.link) return '';
  var l = e.link;
  var inner = l.button
    ? '<span class="fake-btn" onclick="tapLink(\'' + e.id + '\')">' + esc(l.text) + '</span>'
    : '<span class="fake-link" onclick="tapLink(\'' + e.id + '\')">' + esc(l.text) + '</span>';
  return '<p>' + inner + '</p>';
}

function verdictBar(e) {
  var v = S.verdicts[e.id];
  return '<div class="verdict-bar">' +
    '<button class="v-safe' + (v === 'safe' ? ' on' : '') + '" onclick="decide(\'' + e.id + '\',\'safe\')">✓ Genuine</button>' +
    '<button class="v-report' + (v === 'report' ? ' on' : '') + '" onclick="decide(\'' + e.id + '\',\'report\')">⚑ Report to ITSO</button>' +
    '</div>';
}

function eventHtml(e) {
  var head = appBar(APP_META[e.app].name, e.app === 'camera' ? "go('home')" : "go('list',{app:'" + e.app + "'})");
  var body = '';

  if (e.app === 'msgr') {
    body = '<div class="view"><div class="sender"><span class="avatar" style="background:#1D4ED8;color:#fff">🎓</span><span>' +
      '<span class="sender-name">CvSU Scholarship Office</span><span class="sender-addr" style="display:block">' +
      'Page · Education · 212 followers</span></span></div>' +
      '<div class="bubble">' + esc(e.text) + '</div>' +
      '<div class="bubble"><span class="fake-link" onclick="tapLink(\'e3\')">' + esc(e.link.text) + '</span></div>' +
      '<div class="bubble-time">' + e.time + '</div>' +
      '<div class="tools">' + toolBtn(e, 'hold', 'Hold the link') + toolBtn(e, 'pageinfo', 'Page transparency') +
        toolBtn(e, 'official', 'Find the official page') + '</div>' +
      holdInfo(e) +
      (used(e.id, 'pageinfo') ? '<div class="info"><b>Page transparency</b><br>' +
        '<span class="k">Created:</span> 4 October 2026 (3 days ago)<br>' +
        '<span class="k">Name changes:</span> "Pinoy Memes Hub" → "Scholarship Updates PH" → "CvSU Scholarship Office"<br>' +
        '<span class="k">Ads running:</span> 1 · "Claim your ₱5,000 allowance"<br>' +
        '<span class="k">Followers:</span> 212</div>' : '') +
      (used(e.id, 'official') ? '<div class="info"><b>You typed cvsu.edu.ph yourself</b> and followed its Facebook link ' +
        'from the footer. The official page is <b>Cavite State University – Office of Student Affairs</b>: ' +
        'created 2011, 48K followers. Scholarships are posted there and applied for on portal.cvsu.edu.ph. There is ' +
        'no "Digital Learning Allowance" anywhere on it.</div>' : '') +
      (S.verdicts.e3 === 'report' ? '<div class="info"><b>ITSO replied:</b> Thanks. We\'ve reported the Page to ' +
        'Facebook and posted a warning on the official page. Your ticket: ' +
        '<b class="mono">CYBERITY{p4g3_n0t_0ff1c14l}</b></div>' : '') +
      '</div>';
  }

  if (e.app === 'mail') {
    var alert = (S.danger.gave_password && minutes(e.time) > minutes(S.danger.gave_password))
      ? '<div class="alert-strip">Security alert: new sign-in to your CvSU account from ' + ATTACKER_IP +
        ' at ' + S.danger.gave_password + '. It wasn\'t you.</div>' : '';
    body = alert + '<div class="view"><div class="mail-subject">' + esc(e.subject) + '</div>' +
      '<div class="sender" onclick="useTool(\'' + e.id + '\',\'sender\')"><span class="avatar">' + esc(e.fromName.charAt(0)) +
      '</span><span><span class="sender-name">' + esc(e.fromName) + '</span>' +
      '<span class="sender-addr" style="display:block">' +
      (used(e.id, 'sender') ? esc(e.fromAddr) : 'to me · ' + e.time + ' · tap for address') + '</span></span></div>' +
      '<div class="mail-body">' + e.body.map(function (p) { return '<p>' + esc(p) + '</p>'; }).join('') +
      linkHtml(e) + '<p>' + esc(e.sign) + '</p></div>' +
      '<div class="tools">' + toolBtn(e, 'headers', 'Show details') + (e.link ? toolBtn(e, 'hold', 'Hold the link') : '') +
        (e.appCheck ? toolBtn(e, 'classroom', 'Open the Classroom app') : '') + '</div>' +
      (used(e.id, 'headers') ? '<div class="info"><span class="k">From:</span> <span class="mono">' + esc(e.fromAddr) +
        '</span><br><span class="k">Authentication-Results:</span><br><span class="mono">' + esc(e.auth) + '</span></div>' : '') +
      holdInfo(e) + '</div>';
  }

  if (e.app === 'camera') {
    body = '<div class="view" style="padding-left:0;padding-right:0">' +
      '<div class="small" style="padding:0 14px">Taped to a pillar in the canteen, next to the water dispenser.</div>' +
      '<div class="poster"><div class="tag">CvSU students only · today</div><h2>FREE 50GB<br>DATA</h2>' +
      '<p>Scan and register before 5 PM. Limited slots!</p>' +
      '<div class="poster-row">' + qrSvg() + '<p style="font-size:11px">Student Council × LinkPH<br>' +
      'Load arrives in 24 hours</p></div></div>' +
      '<div class="tools" style="padding:0 14px">' + toolBtn(e, 'scan', 'Scan the QR code') + '</div>' +
      (used(e.id, 'scan') ? '<div class="scan-pill" onclick="openPage(\'e4\')">🔗 ' + esc(e.qr.host) + ' · tap to open</div>' +
        '<div class="info" style="margin:8px 14px">The scanner shows the host for a moment. Read it before you tap: ' +
        'who owns <span class="mono">' + esc(e.qr.host) + '</span>, and is that who the poster says is giving away the load?</div>' +
        '<div style="margin:0 14px">' + whoisHtml('e4', e.qr.host) + '</div>' : '') +
      '</div>';
  }

  return head + body + verdictBar(e);
}

function holdInfo(e) {
  if (!e.link || !used(e.id, 'hold')) return '';
  return '<div class="info"><span class="k">Opens:</span> <span class="mono">' + esc(e.link.url) + '</span><br>' +
    '<span class="k">Owner:</span> <b>' + esc(e.link.owner) + '</b>' +
    (e.link.ip ? '<br><span class="k">Server:</span> <span class="mono">' + e.link.ip + '</span>' : '') + '</div>' +
    whoisHtml(e.id, e.link.owner);
}

function whoisHtml(id, domain) {
  var w = WHOIS[domain];
  if (!w) return '';
  if (!used(id, 'whois')) {
    return '<div class="tools"><button class="tool" onclick="useTool(\'' + id + '\',\'whois\')">Look up ' +
      esc(domain) + '</button></div>';
  }
  return '<div class="info"><b>Domain lookup (WHOIS)</b><br>' +
    '<span class="k">Domain:</span> <span class="mono">' + esc(domain) + '</span><br>' +
    '<span class="k">Registered:</span> ' + esc(w.created) + '<br>' +
    '<span class="k">Registrar:</span> ' + esc(w.registrar) + '<br>' +
    '<span class="k">Registrant contact:</span> <span class="mono">' + esc(w.contact) + '</span></div>';
}

function useTool(id, tool) {
  var e = ev(id);
  markUsed(id, tool);
  if (tool === 'classroom') return go('classroom');
  if (tool === 'hold') {
    if (id === 'e2') clue('e2_link_previewed');
    if (id === 'e3') clue('e3_link_previewed');
    if (id === 'e7') clue('e7_link_previewed');
  }
  if (tool === 'headers' && id === 'e7') clue('e7_details_checked');
  if (tool === 'scan') clue('e4_scanned');
  if (tool === 'whois') clue(id + '_whois');
  if (tool === 'pageinfo') clue('e3_page_info');
  if (tool === 'official') clue('e3_official_checked');
  render();
}

/** Tapping a link (not holding it): preview first, with the choice to open it anyway. */
function tapLink(id) {
  var e = ev(id);
  markUsed(id, 'hold');
  if (id === 'e2') clue('e2_link_previewed');
  if (id === 'e3') clue('e3_link_previewed');
  if (id === 'e7') clue('e7_link_previewed');
  openSheet('<h3>Open this link?</h3><div class="info"><span class="mono">' + esc(e.link.url) + '</span><br>' +
    '<span class="k">Owner:</span> <b>' + esc(e.link.owner) + '</b></div>' +
    '<button class="sheet-btn' + (e.truth === 'phish' ? ' danger' : '') + '" onclick="closeSheet();openPage(\'' + id +
    '\')">Open in browser</button><button class="sheet-btn primary" onclick="closeSheet();render()">Don\'t open</button>');
}

function openSheet(html) {
  $('sheet').innerHTML = '<div class="grab"></div>' + html;
  $('scrim').className = 'scrim show';
}

function closeSheet() { $('scrim').className = 'scrim'; }

/* ---------------------------------------------------------------------------
 * Pages the links open
 * ------------------------------------------------------------------------ */

function openPage(id) {
  var e = ev(id);
  var url = e.qr ? e.qr.url : e.link.url;
  var host = url.replace(/^https?:\/\//, '').split('/')[0];
  var page;
  if (id === 'e4') {
    page = '<h4>FREE 50GB for CvSU students</h4><p>Register to receive your load within 24 hours.</p>' +
      '<label>Full name</label><input id="f1" value="Juan dela Cruz">' +
      '<label>Mobile number</label><input id="f2" value="0917 482 3301">' +
      '<label>Course &amp; year</label><input id="f3" value="BSCS 3-B">' +
      '<button class="go" onclick="submitPage(\'e4\')">Claim my 50GB</button>';
  } else if (e.link.page === 'classroom') {
    page = '<h4>CS 311 · Stream</h4><p>Prof. Liza Dimaano · 07:39</p>' +
      '<p>Today\'s 10:00 session moves to Room 204, CEIT Building. Bring your lab notebooks.</p>';
  } else if (e.link.page === 'library') {
    page = '<h4>CvSU Library · My account</h4><p>Signed in as Juan dela Cruz.</p>' +
      '<p><b>Network Security Essentials</b><br>Due 8 October · 1 renewal left</p>' +
      '<button class="go" onclick="toast(\'Renewed. Due 15 October.\', \'good\')">Renew</button>';
  } else {
    var title = id === 'e3' ? 'CvSU Digital Learning Allowance' : (id === 'e7' ? 'Drive · Thesis_Chapter3_final' : 'Student Portal');
    page = '<h4>' + title + '</h4><p>Sign in with your CvSU account to continue.</p>' +
      '<label>CvSU email</label><input value="juan.delacruz@cvsu.edu.ph">' +
      '<label>Password</label><input type="password" value="">' +
      '<button class="go" onclick="submitPage(\'' + id + '\')">Sign in</button>';
  }
  openSheet('<div class="browser-bar">🔒 <span class="mono">' + esc(host) + '</span></div>' +
    '<div class="browser-page">' + page + '</div>' +
    '<button class="sheet-btn" onclick="closeSheet()">Close the page</button>');
}

function submitPage(id) {
  closeSheet();
  if (id === 'e4') {
    danger('gave_number');
    toast('<b>Sent.</b> Your name, number and course are now on a server that isn\'t CvSU\'s or a telco\'s. ' +
      'No load is coming.', 'bad');
  } else {
    danger('gave_password');
    toast('<b>Your CvSU password just went to ' + esc(ev(id).link.owner) + '.</b> The page "failed" and sent you ' +
      'to the real portal so nothing would feel wrong.', 'bad');
  }
  render();
}

/* ---------------------------------------------------------------------------
 * Verdicts
 * ------------------------------------------------------------------------ */

function decide(id, verdict) {
  var e = ev(id);
  var correct = e.truth === 'genuine' ? 'safe' : 'report';
  if (verdict !== correct) {
    shake(document.querySelector(verdict === 'safe' ? '.v-safe' : '.v-report'));
    toast((verdict === 'report' ? '<b>False alarm.</b> ' : '<b>Not so fast.</b> ') + esc(e.wrong), 'bad');
    return;
  }
  if (id === 'e3' && !(S.clues.e3_page_info && S.clues.e3_official_checked)) {
    toast('<b>ITSO asks:</b> what makes you sure it isn\'t ours? Check who runs the Page, and find the official ' +
      'CvSU page yourself, starting from cvsu.edu.ph.', 'bad');
    return;
  }
  if (id === 'e6' && !S.clues.e6_verified) {
    toast('<b>ITSO asks:</b> before we log this, check with LinkPH itself. Call the hotline listed in their ' +
      'own app, not the number that called you.', 'bad');
    return;
  }
  S.verdicts[id] = verdict;
  save();
  if (id === 'e3') { clue('e3_reported'); Cyberity.flagDiscovered('page_ticket'); }
  var block = e.block;
  if (eventsIn(block).every(function (x) { return S.verdicts[x.id]; })) clue('b' + block + '_done');
  toast(verdict === 'safe' ? '<b>Marked genuine.</b>' : '<b>Reported to ITSO.</b>', 'good');
  render();
}

/* ---------------------------------------------------------------------------
 * Classroom: the app the student already uses, so it is the honest check
 * ------------------------------------------------------------------------ */

function classroomHtml() {
  var posts = [
    { who: 'Prof. Liza Dimaano', t: 'Oct 3', text: 'Lab 4 rubric is in Classwork. Due Friday 11:59 PM.' }
  ];
  if (arrived('e1')) {
    posts.unshift({ who: 'Prof. Liza Dimaano', t: '07:39',
      text: 'Good morning, class! Today\'s 10:00 session moves to Room 204, CEIT Building, because of the ' +
        'aircon repair. Bring your lab notebooks.' });
    clue('e1_checked_app');
  }
  return appBar('Classroom') +
    '<div class="wallet" style="background:linear-gradient(135deg,#15803D,#166534)">' +
    '<div class="small" style="color:#DCFCE7">BSCS 3-B</div><div class="bal" style="font-size:20px">' +
    'CS 311 · Data Communications and Networking</div></div>' +
    '<div class="small" style="padding:4px 14px">Stream</div>' +
    posts.map(function (p) {
      return '<div class="txn" style="display:block"><b>' + esc(p.who) + '</b> <span class="small">· ' + p.t +
        '</span><br>' + esc(p.text) + '</div>';
    }).join('') +
    (arrived('e1') ? '<div class="info" style="margin:10px 14px">You opened this app yourself, from your own home ' +
      'screen. The announcement here matches the email word for word.</div>' : '');
}

/* ---------------------------------------------------------------------------
 * GCash
 * ------------------------------------------------------------------------ */

function peso(n) { return '₱' + Math.abs(n).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ','); }

function gcashHtml() {
  var bal = 1856.50;
  var rows = [
    { t: 'Oct 6 · 19:12', what: 'Paid · Jollibee CvSU', amt: -189, ref: '4022 907 115' },
    { t: 'Oct 5 · 08:03', what: 'Cash in · 7-Eleven Indang', amt: 1000, ref: '4022 511 230' }
  ];
  if (S.danger.installed_app) {
    bal -= 1500;
    rows.unshift({ t: 'Oct 7 · 15:34', what: 'Sent to 0917 555 0144', amt: -1500, ref: '4023 266 410' });
  }
  return appBar('GCash') +
    (S.danger.installed_app ? '<div class="alert-strip">₱1,500 left your wallet at 15:34. You didn\'t send it: ' +
      'the "remote help" app let the caller see your screen and your MPIN.</div>' : '') +
    '<div class="wallet"><div class="small" style="color:#DBEAFE">Available balance</div>' +
    '<div class="bal">' + peso(bal) + '</div></div>' +
    '<div class="small" style="padding:4px 14px">Transaction history</div>' +
    rows.map(function (r) {
      return '<div class="txn"><span>' + esc(r.what) + '<br><span class="small">' + r.t + ' · Ref. ' + r.ref +
        '</span></span><span class="amt ' + (r.amt > 0 ? 'in' : 'out') + '">' + (r.amt > 0 ? '+' : '−') + peso(r.amt) + '</span></div>';
    }).join('');
}

/* ---------------------------------------------------------------------------
 * Phone: contacts, recents, and calls
 * ------------------------------------------------------------------------ */

function phoneHtml() {
  var html = appBar('Phone');
  if (arrived('e6')) {
    var e = ev('e6');
    html += '<div class="small" style="padding:10px 14px 0">Recent</div>' +
      '<div class="list-row"><span class="avatar">?</span><span class="list-main">' +
      '<span class="list-top"><span>+63 917 555 0144' + chip('e6') + '</span><span class="t">15:20</span></span>' +
      '<span class="list-sub">"Rico from LinkPH" · wanted you to install an app</span></span></div>';
  }
  html += '<div class="small" style="padding:10px 14px 0">Contacts</div>' +
    contactRow('linkph', 'LinkPH Hotline', '*88 · listed in the LinkPH app on your phone') +
    contactRow('bea', 'Bea Lacson', 'Classmate · saved number') +
    contactRow('itso', 'CvSU ITSO Help Desk', '(046) 415 0010 · from the contact page on cvsu.edu.ph') +
    contactRow('mama', 'Mama', 'Mobile');
  if (arrived('e6')) {
    html += '<div class="view" style="padding-bottom:110px"><div class="info">Decide what the 15:20 call was ' +
      'once you\'ve checked it.</div></div>' + verdictBar(ev('e6'));
  }
  return html;
}

function contactRow(key, name, sub) {
  return '<div class="list-row" onclick="startCall(\'' + key + '\')"><span class="avatar">' + name.charAt(0) +
    '</span><span class="list-main"><span class="list-top">' + esc(name) + '<span class="t">✆ Call</span></span>' +
    '<span class="list-sub">' + esc(sub) + '</span></span></div>';
}

var call = { node: null, script: null, who: '', num: '' };

function showCall(who, num, state) {
  var c = $('call');
  c.innerHTML = '<div class="call-who">' + esc(who) + '</div><div class="call-num">' + esc(num) + '</div>' +
    '<div class="call-state" id="call-state">' + state + '</div>' +
    '<div class="call-log" id="call-log"></div><div class="call-choices" id="call-choices"></div>' +
    '<div class="call-btns" id="call-btns"></div>';
  c.className = 'call show';
}

function callLine(kind, text) {
  var log = $('call-log');
  var d = document.createElement('div');
  d.className = 'line ' + kind;
  d.innerHTML = text;
  log.appendChild(d);
  log.scrollTop = log.scrollHeight;
}

function endCall(note) {
  $('call').className = 'call';
  S.ringing = false;
  save();
  if (note) toast(note, '');
  // After the 15:20 call, land on the Phone app where it gets its verdict.
  if (arrived('e6') && !S.verdicts.e6) view = { name: 'phone' };
  render();
}

/* --- the 15:20 call --- */

function openIncomingCall() {
  showCall('+63 917 555 0144', 'Mobile · not in your contacts', 'INCOMING CALL');
  $('call-btns').innerHTML = '<button class="round end" onclick="declineCall()">✕</button>' +
    '<button class="round accept" onclick="answerCall()">✆</button>';
}

function declineCall() {
  $('call-state').textContent = 'DECLINED';
  endCall('You declined. A voicemail arrives: "Hi, this is Rico from LinkPH student promos, your 50GB is waiting, call me back on this number." ' +
    'Check it before you decide.');
}

function answerCall() {
  $('call-state').textContent = '00:01';
  $('call-btns').innerHTML = '<button class="round end" onclick="hangUp()">✕</button>';
  runNode(scamScript(), 'intro');
}

function hangUp() {
  callLine('note', 'You hung up.');
  setTimeout(function () { endCall('Call ended. Now check it through a number you already trust.'); }, 600);
}

function scamScript() {
  var why = S.danger.gave_number
    ? 'You registered for the 50GB student promo at lunch, so you\'re on my list.'
    : 'Your number is on the CvSU list the Student Council sent us for the 50GB promo.';
  return {
    intro: { them: ['Hello po! Is this Juan dela Cruz, BS Computer Science 3-B?'],
      choices: [{ you: 'Yes. Who\'s this?', next: 'who' }] },
    who: { them: ['This is Rico from LinkPH Student Promos. Congratulations, your 50GB is ready to activate!', why],
      choices: [{ you: 'How do you know my course and my number?', next: 'how' },
        { you: 'How do I activate it?', next: 'ask' }] },
    how: { them: ['From your registration po. It\'s all in our system.'],
      choices: [{ you: 'How do I activate it?', next: 'ask' }] },
    ask: { them: ['I\'ll text you a link for "LinkPH Load Activator". Install it and read me the 9-digit code ' +
        'it shows, then I can push the 50GB to your phone directly. Two minutes lang po.'],
      choices: [{ you: 'Okay, send the link.', next: 'install' },
        { you: 'Why can\'t it just arrive like normal load?', next: 'why' },
        { you: 'I\'ll call LinkPH\'s hotline myself and ask about it.', next: 'push' }] },
    why: { them: ['Student promos need manual activation po, the system can\'t send that much data automatically. ' +
        'But slots close at 5 PM, and there are only a few left.'],
      choices: [{ you: 'Okay, send the link.', next: 'install' },
        { you: 'I\'ll call LinkPH\'s hotline myself and ask about it.', next: 'push' }] },
    push: { them: ['The hotline doesn\'t handle student promos po, only our team does. If you hang up, your slot ' +
        'goes to the next student.'],
      choices: [{ you: 'Then I\'ll take that chance. Bye.', next: 'hang' },
        { you: 'Fine. Send the link.', next: 'install' }] },
    install: { them: [], note: 'SMS from 0917 555 0144: linkph-activator.example/app.apk',
      choices: [{ you: 'Installed. The code is 418 220 913.', next: 'installed' },
        { you: 'Wait. I\'m hanging up.', next: 'hang' }] },
    installed: { them: ['Perfect po, I\'m connected. Don\'t touch your phone for a few minutes while it activates.'],
      act: 'installed_app', end: 'The call ends. Your screen flickers for a moment.' },
    hang: { them: [], end: 'You hung up. Now check it through a number you already trust.' }
  };
}

function runNode(script, key) {
  var n = script[key];
  $('call-choices').innerHTML = '';
  var delay = 0;
  (n.them || []).forEach(function (line) {
    delay += 650;
    setTimeout(function () { callLine('them', esc(line)); }, delay);
  });
  if (n.note) setTimeout(function () { callLine('note', esc(n.note)); }, delay += 500);
  setTimeout(function () {
    if (n.act) danger(n.act);
    if (n.end) { setTimeout(function () { endCall(n.end); }, 900); return; }
    $('call-choices').innerHTML = n.choices.map(function (c, i) {
      return '<button onclick="pickLine(' + i + ')">' + esc(c.you) + '</button>';
    }).join('');
    call.node = n;
    call.script = script;
  }, delay + 350);
}

function pickLine(i) {
  var c = call.node.choices[i];
  callLine('you', esc(c.you));
  $('call-choices').innerHTML = '';
  setTimeout(function () { runNode(call.script, c.next); }, 400);
}

/* --- calls you make --- */

function startCall(key) {
  var names = { bea: 'Bea Lacson', itso: 'CvSU ITSO Help Desk', linkph: 'LinkPH Hotline', mama: 'Mama' };
  var nums = { bea: '0918 220 4471', itso: '(046) 415 0010', linkph: '*88', mama: '0917 330 1289' };
  var name = names[key];
  var num = nums[key];
  showCall(name, num, 'CALLING…');
  $('call-btns').innerHTML = '<button class="round end" onclick="endCall()">✕</button>';
  var lines = callScript(key);
  var delay = 900;
  setTimeout(function () { $('call-state').textContent = '00:01'; }, delay);
  lines.forEach(function (l) {
    delay += 900;
    setTimeout(function () { callLine(l[0], esc(l[1])); }, delay);
  });
  setTimeout(function () {
    if (key === 'linkph' && arrived('e6')) clue('e6_verified');
    callLine('note', 'Tap ✕ to hang up.');
  }, delay + 400);
}

function callScript(key) {
  if (key === 'mama') {
    return [['them', 'Anak! Kumain ka na? Huwag kang magpapagabi ha.'], ['you', 'Opo, Ma. Bye!']];
  }
  if (key === 'bea') {
    if (!arrived('e7')) return [['them', 'Hello? Juan? Busy ako sa thesis, text na lang ha!']];
    return [
      ['you', 'Bea, did you just share your Chapter 3 with me?'],
      ['them', 'Ha? Hindi pa tapos yung Chapter 3 ko. Wait…'],
      ['them', 'I can\'t get into my CvSU email since yesterday. I signed in to some scholarship link on Facebook. ' +
        'Was that it?'],
      ['note', 'Bea\'s CvSU account is being used by someone else.']
    ];
  }
  if (key === 'linkph') {
    if (!arrived('e6')) return [['them', 'Thank you for calling LinkPH. For load and promos, press 1.'], ['you', 'Sorry, wrong number.']];
    var lines = [
      ['you', 'Someone called me from 0917 555 0144 as "Rico from LinkPH Student Promos". He wanted me to install ' +
        'an activator app for a 50GB CvSU promo.'],
      ['them', 'Thank you for checking with us. LinkPH has no 50GB promo with CvSU or its Student Council. Our ' +
        'promos are only in the LinkPH app or by dialling *88, and we never ask anyone to install an app or read ' +
        'us a code.'],
      ['them', 'Please forward that number to our report line. You\'re not the first student to call today.']
    ];
    if (S.danger.installed_app) {
      lines.push(['them', 'You installed it? Uninstall it now and turn on airplane mode. From another device, ' +
        'change your CvSU password and your GCash MPIN, then call GCash using the hotline inside its app.']);
    }
    return lines;
  }
  if (key === 'itso') {
    if (arrived('e6')) {
      return [['you', 'A "LinkPH promo team" called me about a CvSU 50GB promo.'],
        ['them', 'CvSU has no promo with LinkPH. The best check is LinkPH itself: call the hotline in their app. ' +
          'And please report the poster in the canteen to us.']];
    }
    return [['them', 'ITSO help desk, good day! How can we help?'], ['you', 'Sorry, wrong number.']];
  }
  return [['them', 'Hello?']];
}

/** A decorative, fixed QR-style pattern. It encodes nothing. */
function qrSvg() {
  var cells = '';
  var seed = 11;
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
  return '<svg class="qr" viewBox="0 0 21 21" fill="#111">' + cells + '</svg>';
}

/* ---------------------------------------------------------------------------
 * 21:00 · Replay
 * ------------------------------------------------------------------------ */

// Two attackers worked the campus today. One card of each is placed for the
// student; they place the other three. The genuine three are already
// settled by their verdicts during the day, so they are set aside.
var ATTACKERS = {
  a: { name: 'Attacker A', what: 'after CvSU logins', anchor: 'e2' },
  b: { name: 'Attacker B', what: 'the one who called you', anchor: 'e4' }
};
var TO_PLACE = ['e3', 'e6', 'e7'];

function evidence(e) {
  var rows = [];
  var domain = e.link ? e.link.owner : (e.qr ? e.qr.host : null);
  if (e.id === 'e7') rows.push(['Sent from', 'Bea\'s real CvSU account']);
  if (domain && WHOIS[domain]) rows.push(['Link domain registered by', WHOIS[domain].contact]);
  if (e.id === 'e6') rows.push(['Knew about you', 'your name, course and number']);
  rows.push(['Asked for', e.id === 'e4' ? 'your name, number and course' : e.asked]);
  return rows.map(function (r) {
    return '<div class="ev-row"><span class="k">' + esc(r[0]) + '</span><b>' + esc(r[1]) + '</b></div>';
  }).join('');
}

function cardTitle(e) {
  if (e.app === 'mail') return e.subject;
  if (e.app === 'camera') return 'FREE 50GB poster in the canteen';
  if (e.app === 'call') return '"Rico from LinkPH" calls';
  return e.from + ' messages you';
}

function rcard(e, extra) {
  return '<div class="rcard ' + (extra || '') + '">' +
    '<div class="rcard-top"><span>' + e.time + ' · ' + APP_META[e.app].name + '</span></div>' +
    '<div class="rcard-title">' + esc(cardTitle(e)) + '</div>' + evidence(e) + '</div>';
}

function placedIn(k) {
  var ids = [ATTACKERS[k].anchor];
  TO_PLACE.forEach(function (id) { if (S.groups[id] === k) ids.push(id); });
  return ids;
}

function replayHtml() {
  var genuine = EVENTS.filter(function (e) { return e.group === 'g'; }).map(function (e) {
    return '<div class="g-row">✓ ' + e.time + ' · ' + esc(cardTitle(e)) + '</div>';
  }).join('');

  var boxes = ['a', 'b'].map(function (k) {
    var a = ATTACKERS[k];
    return '<div class="atk atk-' + k + '"><div class="atk-head"><b>' + a.name + '</b> · ' + a.what + '</div>' +
      placedIn(k).map(function (id) {
        return rcard(ev(id), id === a.anchor ? 'anchor' : 'placed');
      }).join('') + '</div>';
  }).join('');

  var left = TO_PLACE.filter(function (id) { return !S.groups[id]; });
  var toPlace = left.length
    ? '<div class="section-h">Still to place</div>' + left.map(function (id) {
        return '<div id="card-' + id + '">' + rcard(ev(id)) +
          '<div class="groups" style="margin:0 14px 4px"><button class="a" onclick="place(\'' + id + '\',\'a\')">→ Attacker A</button>' +
          '<button class="b" onclick="place(\'' + id + '\',\'b\')">→ Attacker B</button></div></div>';
      }).join('')
    : '';

  return '<div class="replay-head"><h1>21:00 · Replay your day</h1>' +
    '<p>Two attackers worked the campus today. One card for each is already placed. Put the other ' +
    '<b>three</b> with the attacker who sent them, using the evidence on each card.</p></div>' +
    '<div class="section-h">You marked these genuine</div><div class="g-list">' + genuine + '</div>' +
    boxes + toPlace +
    '<div id="replay-result" class="banner"></div>' +
    '<div id="consequences" class="banner"></div><div style="height:30px"></div>';
}

var PLACE_HINTS = {
  e3: 'Look at who registered its link\'s domain. Which attacker\'s domains came from that same contact?',
  e6: 'The caller never asked for a login. He knew exactly what one card collected. Which card was that?',
  e7: 'It came from Bea\'s real account, but look at who registered its link\'s domain.'
};

function place(id, k) {
  if (S.groups[id]) return;
  if (ev(id).group !== k) {
    shake($('card-' + id));
    toast('<b>Not that one.</b> ' + esc(PLACE_HINTS[id]), 'bad');
    return;
  }
  S.groups[id] = k;
  save();
  var y = window.scrollY;
  render();
  window.scrollTo(0, y);
  var done = TO_PLACE.every(function (x) { return S.groups[x]; });
  if (done) {
    $('toast').className = 'toast';
    S.replayDone = true;
    save();
    showReplayResult();
    var r = $('replay-result');
    if (r) r.scrollIntoView({ behavior: 'smooth', block: 'start' });
  } else {
    toast('<b>Placed.</b> ' + (TO_PLACE.filter(function (x) { return !S.groups[x]; }).length) + ' to go.', 'good');
  }
}

function showReplayResult() {
  var r = $('replay-result');
  r.className = 'banner show good';
  r.innerHTML = '<b>What happened today</b><br>' +
    '<span class="white"><b>Attacker A</b> wanted CvSU logins. The fake Registrar, the fake scholarship Page and ' +
    'the thesis file all linked to domains registered by ' + REGISTRANT_A + ' within two days. Bea fell for it ' +
    'yesterday, so the 18:00 email came from her real account and passed every check.</span><br>' +
    '<span class="white"><b>Attacker B</b> wanted your details, then your phone. The canteen form collected name, ' +
    'number and course. At 15:20 "Rico" knew exactly those three things, and nothing else.</span><br>' +
    'Day report code: <b class="mono">CYBERITY{0n3_d4y_r3pl4y3d}</b>';

  var c = $('consequences');
  var notes = [];
  if (S.danger.gave_password) notes.push('At ' + S.danger.gave_password + ' you typed your CvSU password into a fake page. ' +
    'The attacker signed in from ' + ATTACKER_IP + ' and could now message your classmates as you, exactly like Bea\'s account did.');
  if (S.danger.gave_number) notes.push('At lunch the poster form got your name, number and course. That is the script ' +
    'the 15:20 caller read from.');
  if (S.danger.installed_app) notes.push('At 15:20 you installed the "remote help" app. The caller watched your screen and sent ' +
    '₱1,500 out of your GCash at 15:34.');
  c.className = 'banner show ' + (notes.length ? 'warn' : 'good');
  c.innerHTML = notes.length
    ? '<b>What your choices unlocked</b><br>' + notes.join('<br>')
    : '<b>What your choices unlocked:</b> nothing. You gave the attackers no password, no details and no access all day.';

  if (!S.clues.replay_done) {
    clue('replay_done');
    Cyberity.flagDiscovered('replay');
  }
}

// Re-show the result if the replay was already solved.
(function () {
  var orig = render;
  render = function () {
    orig();
    if (view.name === 'replay' && S.replayDone) showReplayResult();
  };
})();
