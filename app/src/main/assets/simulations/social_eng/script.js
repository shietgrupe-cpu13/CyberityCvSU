/* Cyberity — Campus Security Review simulation (level 303).
 * Local only. Fictional incidents, fictional people, no network.
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

/* Read state: sessionStorage first, window.name as the fallback that survives
 * navigation even where DOM storage is unavailable. */
var Store = {
  KEY: 'se_read',
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
 * The four incidents
 * ------------------------------------------------------------------------ */
var INCIDENTS = {
  a: {
    id: 'a', code: 'A', day: 'Monday 14:10', channel: 'IN PERSON',
    title: 'Courier followed a student assistant into the MIS building',
    reporter: 'Security Officer R. Bautista',
    summary: 'A man in a delivery uniform carrying two boxes entered through the badge door ' +
      'behind a student assistant. He was not signed in at the guard house.',
    report: 'The student assistant said he had his hands full and asked her to hold the door. ' +
      'He said the boxes were printer toner for the MIS office. She let him through because he ' +
      'looked like every other delivery rider who comes here. CCTV shows him leaving nine ' +
      'minutes later, still carrying both boxes.',
    evidence: [
      {
        name: 'Security officer\'s notes',
        sub: 'Written up the same afternoon',
        rows: [
          'visitor      : male, delivery uniform, two sealed boxes, no company logo',
          'claimed      : toner delivery for the MIS office',
          'time inside  : 14:10 to 14:15 (4 min 40 s unaccompanied)',
          'taken        : nothing. Both boxes left with him, still sealed',
          'guard house  : not signed in; no delivery scheduled this week'
        ],
        hot: [4]
      }
    ],
    tools: [
      { href: 'doorway.html', label: 'STAND AT THE DOOR (10 SECONDS)' },
      { href: 'cctv.html', label: 'REVIEW CAMERA 3 FOOTAGE' }
    ]
  },

  b: {
    id: 'b', code: 'B', day: 'Tuesday 09:40', channel: 'PHONE CALL',
    title: 'Caller claiming to be from ITSO asked library staff for her password',
    reporter: 'Library staff · L. Ramos',
    summary: 'A caller said he was migrating library accounts and needed her password to keep ' +
      'her account active before a deadline.',
    report: 'She did not give the password, but only because the call felt rushed. She did not ' +
      'report it until the next morning, when she mentioned it to a colleague. The ITSO has no ' +
      'staff member named Marco and no migration is running.',
    evidence: [
      {
        name: 'Call transcript',
        sub: 'Recorded on the library landline · 00:52',
        clue: 'transcript_opened',
        chat: [
          { who: 'them', name: 'Caller', text: 'Good morning ma\'am, this is Marco from ITSO. ' +
            'Am I speaking with Ma\'am Lorna at the circulation desk?' },
          { who: 'us', name: 'L. Ramos', text: 'Yes, sir. Who is this again?' },
          { who: 'them', name: 'Caller', text: 'Marco po, from the IT Services Office. We are ' +
            'migrating the library system accounts today. I can see your account on my screen, ' +
            'Ramos, Lorna — circulation.' },
          { who: 'us', name: 'L. Ramos', text: 'Ah, okay.' },
          { who: 'them', name: 'Caller', text: 'The migration closes at 10:00. Any account we ' +
            'have not moved by then gets locked and I would have to file a ticket, which takes ' +
            'three days. I do not want that to happen to you, ma\'am.' },
          { who: 'us', name: 'L. Ramos', text: 'What do you need from me?' },
          { who: 'them', name: 'Caller', text: 'Just your current password so I can carry the ' +
            'account over. It stays encrypted on our side, of course.' },
          { who: 'us', name: 'L. Ramos', text: 'Sir, can I call you back? It is busy here.' },
          { who: 'them', name: 'Caller', text: 'Ma\'am, we only have twenty minutes left. I am ' +
            'trying to help you here.' }
        ],
        note: 'Authority in the first line, urgency in the fifth, and the ask in the seventh. ' +
          'The detail he "sees on his screen" — her name and desk — is printed on the library ' +
          'staff page.'
      }
    ]
  },

  c: {
    id: 'c', code: 'C', day: 'Wednesday 11:25', channel: 'PHYSICAL',
    title: 'Three flash drives found in the canteen, one plugged into a lab PC',
    reporter: 'Lab technician · A. Dizon',
    summary: 'Drives labelled "SCHOLARSHIP GRANTEES 2026 — CONFIDENTIAL" were left on tables ' +
      'in the canteen. A student plugged one into a computer laboratory PC.',
    report: 'The student said she wanted to find out whose drive it was so she could return it. ' +
      'The PC was rebuilt the same day. Two more drives were handed in unopened. One was kept ' +
      'for analysis.',
    evidence: [
      {
        name: 'Endpoint alert',
        sub: 'LAB2-PC14 · 11:26',
        rows: [
          '11:26:03  removable media attached (mass storage + keyboard device)',
          '11:26:04  keystrokes injected: powershell -w hidden -f E:\\\\.sys\\\\run.ps1',
          '11:26:09  process read: browser saved-password store',
          '11:26:11  outbound connection attempt · blocked by lab firewall'
        ],
        hot: [1, 2, 3],
        note: 'The drive announced itself as a keyboard as well as storage, so it could type ' +
          'its own commands one second after being plugged in.'
      }
    ],
    tool: { href: 'usb.html', label: 'OPEN THE DRIVE ANALYSIS' }
  },

  d: {
    id: 'd', code: 'D', day: 'Thursday 20:15', channel: 'CHAT',
    title: 'Message from a classmate\'s account asking for a verification code',
    reporter: 'Student · K. Mendoza',
    summary: 'A Messenger chat from a classmate asked him to forward a six-digit code that had ' +
      'just arrived on his own phone.',
    report: 'He did not send it. He called the classmate, who had lost access to her account ' +
      'that afternoon and was already warning people.',
    evidence: [
      {
        name: 'Chat thread',
        sub: 'Messenger · 20:15',
        chat: [
          { who: 'them', name: 'Aira (classmate)', text: 'Uy! Are you awake? Sorry to bother.' },
          { who: 'us', name: 'You', text: 'Yeah, what\'s up?' },
          { who: 'them', name: 'Aira (classmate)', text: 'I\'m signing up for the on-the-job ' +
            'training portal and I put your number as my backup contact by mistake 😅' },
          { who: 'them', name: 'Aira (classmate)', text: 'A 6-digit code will arrive on your ' +
            'phone. Pakisend naman, the form expires in 5 minutes and I lose my slot.' },
          { who: 'us', name: 'You', text: 'Wait, I got a text. It says do not share this code ' +
            'with anyone.' },
          { who: 'them', name: 'Aira (classmate)', text: 'I know, that\'s automatic lang. It\'s ' +
            'my account naman, not yours. Please, 2 minutes na lang 🙏' }
        ],
        note: 'Familiar name, small favour, a deadline, and reassurance that the warning does ' +
          'not apply. The code arrived on his phone, not hers.'
      },
      {
        name: 'Account check',
        sub: 'What that account has been doing',
        clue: 'chat_checked',
        rows: [
          '16:40  account owner reports she can no longer sign in',
          '20:02  account online again from a device never used before',
          '20:05  same message sent to 40 contacts in 9 minutes',
          '20:15  message reaches K. Mendoza',
          '',
          'the code in question: SMS from the e-wallet, sent to K. Mendoza\'s own number'
        ],
        hot: [1, 2, 5],
        note: 'The code unlocks his account, not hers. A one-time code only ever proves that ' +
          'whoever types it controls that phone number.'
      }
    ]
  }
};

var ORDER = ['a', 'b', 'c', 'd'];

/* ---------------------------------------------------------------------------
 * Desk
 * ------------------------------------------------------------------------ */
function renderDesk(mountId) {
  var html = ORDER.map(function (key) {
    var i = INCIDENTS[key];
    var read = Store.has(key);
    return '' +
      '<a class="ticket-row ' + (read ? 'read' : 'unread') + '" href="incident.html#' + i.id + '">' +
        '<span class="sev-bar ' + (read ? 'seen' : 'new') + '"></span>' +
        '<span class="ticket-body">' +
          '<span class="ticket-meta">' +
            '<span class="ticket-id">' + escapeHtml(i.code) + '</span>' +
            '<span class="kind">' + escapeHtml(i.channel) + '</span>' +
            '<span style="margin-left:auto">' + escapeHtml(i.day) + '</span>' +
          '</span>' +
          '<span class="ticket-title">' + escapeHtml(i.title) + '</span>' +
          '<span class="ticket-from">' + escapeHtml(i.summary) + '</span>' +
          '<span class="mail-badge">' + (read ? 'REVIEWED' : 'NOT REVIEWED YET') + '</span>' +
        '</span>' +
      '</a>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;

  var left = ORDER.filter(function (k) { return !Store.has(k); }).length;
  var counter = document.getElementById('review-count');
  if (counter) {
    counter.textContent = left === 0
      ? 'All four reviewed — now look at them as one week'
      : left + ' of 4 still to review';
  }
}

/* ---------------------------------------------------------------------------
 * Incident detail
 * ------------------------------------------------------------------------ */
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

function renderChat(ev) {
  return '<div class="chat">' + ev.chat.map(function (m) {
    return '<div class="bubble-row ' + m.who + '">' +
      '<div class="bubble">' +
        '<div class="bubble-name">' + escapeHtml(m.name) + '</div>' +
        escapeHtml(m.text) +
      '</div></div>';
  }).join('') + '</div>';
}

function renderIncident(mountId) {
  var key = (window.location.hash || '#a').substring(1);
  var i = INCIDENTS[key] || INCIDENTS.a;

  Store.mark(i.id);
  Cyberity.clueFound('incident_opened_' + i.id);

  var html =
    '<div class="mail-card">' +
      '<h2>' + escapeHtml(i.title) + '</h2>' +
      '<div class="detail-sub">' + escapeHtml(i.code) + ' · ' + escapeHtml(i.day) +
        ' · ' + escapeHtml(i.channel) + ' · reported by ' + escapeHtml(i.reporter) + '</div>' +
      '<div class="report">' + escapeHtml(i.report) + '</div>' +
    '</div>';

  html += '<div class="section-label">Evidence</div>';
  i.evidence.forEach(function (ev, index) {
    var boxId = 'ev-' + index;
    html +=
      '<div class="evidence" id="' + boxId + '">' +
        '<div class="evidence-head" onclick="toggleEvidence(\'' + boxId + '\', \'' + (ev.clue || '') + '\')">' +
          '<span>' +
            '<span class="evidence-name">' + escapeHtml(ev.name) + '</span>' +
            '<span class="evidence-sub">' + escapeHtml(ev.sub) + '</span>' +
          '</span>' +
          '<span class="evidence-caret">expand</span>' +
        '</div>' +
        '<div class="evidence-body">' +
          (ev.chat ? renderChat(ev) : '<div class="rows">' + renderRows(ev) + '</div>') +
          (ev.note ? '<div class="note">' + escapeHtml(ev.note) + '</div>' : '') +
        '</div>' +
      '</div>';
  });

  if (i.tool) {
    html += '<a class="cta" href="' + i.tool.href + '">' + escapeHtml(i.tool.label) + '</a>';
  }
  if (i.tools) {
    i.tools.forEach(function (t) {
      html += '<a class="cta" href="' + t.href + '">' + escapeHtml(t.label) + '</a>';
    });
  }

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('incident-code').textContent = i.code + ' · ' + i.day;
  window.scrollTo(0, 0);
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
 * Incident C · the drive
 * ------------------------------------------------------------------------ */
function analyseDrive() {
  var out = document.getElementById('analysis');
  if (out.classList.contains('open')) return;
  out.classList.add('open');
  out.scrollIntoView({ behavior: 'smooth', block: 'start' });
  Cyberity.clueFound('usb_analyzed');
  Cyberity.flagDiscovered('usb_script');
}

/** The dangerous action: plugging a found drive in to "just look". */
function plugIn() {
  var banner = document.getElementById('plug-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation. This is exactly what the student did. One ' +
    'second after plugging in, the drive typed its own commands: no file was opened and no ' +
    'warning appeared. Found drives go to the ITSO unopened — curiosity is the payload.';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('usb_plugged_in');
}


/* ===========================================================================
 * INCIDENT A · the doorway, ten seconds of it
 *
 * Before reviewing anyone else's decision, the student makes it themselves.
 * No option costs a heart: the point is that refusing feels rude, not that
 * the student assistant was careless.
 * ======================================================================== */
var DOOR_CHOICES = {
  hold: {
    label: 'Hold the door open for him',
    tone: 'bad',
    text: 'He thanks you and walks in. This is what happened on Monday, and nobody would ' +
      'blame you for it — his hands were full and he looked like every other rider. He was ' +
      'alone in the corridor for four minutes and left with both boxes still sealed.'
  },
  badge: {
    label: 'Say sorry, ask him to sign in at the guard house',
    tone: 'good',
    text: 'He says it is fine and walks back toward the guard house. A real courier signs in ' +
      'every day and thinks nothing of it. This is the whole attack, stopped, for the price of ' +
      'one slightly awkward sentence.'
  },
  guard: {
    label: 'Let the door close and call the guard to escort him',
    tone: 'good',
    text: 'The guard comes up, finds no delivery scheduled, and the man leaves without an ' +
      'argument. Slower than option two, and just as effective. Either way the door closed ' +
      'behind one person.'
  }
};

function renderDoorway(mountId) {
  document.getElementById(mountId).innerHTML =
    '<div class="scene-card">' +
      '<div class="scene-time">Monday 14:10 · MIS building side entrance</div>' +
      doorSvg() +
    '</div>' +
    '<div class="speech">"Ma\'am, pakibukas naman — puno ang kamay ko. Toner po para sa MIS."</div>' +
    '<div class="note">Your badge has just opened the door. He is two steps behind you, ' +
      'carrying two boxes. What do you do?</div>' +
    '<div id="door-choices">' +
      Object.keys(DOOR_CHOICES).map(function (k) {
        return '<button class="choice-btn" onclick="chooseDoor(\'' + k + '\')">' +
          escapeHtml(DOOR_CHOICES[k].label) + '</button>';
      }).join('') +
    '</div>' +
    '<div class="banner" id="door-result"></div>' +
    '<div id="door-after"></div>';
}

function chooseDoor(key) {
  var c = DOOR_CHOICES[key];
  var banner = document.getElementById('door-result');
  banner.className = 'banner show ' + (c.tone === 'good' ? 'good' : 'warn');
  banner.textContent = c.text;

  document.getElementById('door-after').innerHTML =
    '<div class="note">There is no heart lost here. Holding a door is ordinary politeness — ' +
      'that is exactly what the technique spends. The control has to be the rule (one badge, ' +
      'one person; every visitor signs in), not a judgement call made in two seconds with ' +
      'somebody watching you.</div>' +
    '<a class="cta" href="cctv.html">NOW REVIEW WHAT ACTUALLY HAPPENED</a>';

  Cyberity.clueFound('doorway_faced');
}

/* ===========================================================================
 * INCIDENT A · camera 3, frame by frame
 * ======================================================================== */
function doorSvg(opts) {
  opts = opts || {};
  var staff = opts.staff, visitor = opts.visitor, doorOpen = opts.doorOpen;
  var phone = opts.phone, prop = opts.prop;

  function person(x, colour, boxes, raised) {
    return '<g transform="translate(' + x + ',0)">' +
      '<circle cx="0" cy="58" r="9" fill="' + colour + '"/>' +
      '<rect x="-9" y="69" width="18" height="30" rx="7" fill="' + colour + '"/>' +
      (boxes ? '<rect x="-14" y="74" width="28" height="18" rx="2" fill="#c8a46a" ' +
        'stroke="#8a6f44"/>' : '') +
      (raised ? '<rect x="7" y="52" width="7" height="12" rx="2" fill="#0d1b3f" ' +
        'stroke="#6CB8EC"/>' : '') +
      '</g>';
  }

  return '<svg viewBox="0 0 300 130" class="cctv-svg" role="img" ' +
      'aria-label="Simulated camera view of a corridor door">' +
    '<rect x="0" y="0" width="300" height="130" fill="#0a1330"/>' +
    '<line x1="0" y1="100" x2="300" y2="100" stroke="#24356e" stroke-width="2"/>' +
    '<rect x="28" y="30" width="46" height="70" fill="' + (doorOpen ? '#04102e' : '#16255a') +
      '" stroke="#3a5199"/>' +
    '<circle cx="80" cy="66" r="4" fill="' + (doorOpen ? '#27E0A8' : '#FF5C7A') + '"/>' +
    (prop === 'whiteboard'
      ? '<rect x="150" y="34" width="86" height="46" fill="#e9edf3" stroke="#93a2c4"/>' +
        '<line x1="158" y1="46" x2="222" y2="46" stroke="#8fa0c6" stroke-width="3"/>' +
        '<line x1="158" y1="56" x2="206" y2="56" stroke="#8fa0c6" stroke-width="3"/>' +
        '<line x1="158" y1="66" x2="214" y2="66" stroke="#8fa0c6" stroke-width="3"/>'
      : '') +
    (prop === 'desk'
      ? '<rect x="150" y="70" width="90" height="8" fill="#2c3f7d"/>' +
        '<rect x="176" y="40" width="44" height="30" rx="2" fill="#0f1c44" stroke="#4a63b0"/>' +
        '<rect x="214" y="44" width="12" height="10" fill="#FFC46B"/>'
      : '') +
    (staff !== undefined ? person(staff, '#6CB8EC', false, false) : '') +
    (visitor !== undefined ? person(visitor, '#FFC46B', opts.boxes !== false, phone) : '') +
    '</svg>';
}

var FRAMES = [
  {
    time: '14:10:22',
    caption: 'J. Ramos badges in. A man with two boxes waits behind her.',
    scene: { staff: 55, visitor: 20, doorOpen: true }
  },
  {
    time: '14:10:24',
    caption: 'He steps through on the same opening. The door has not closed between them.',
    scene: { staff: 95, visitor: 58, doorOpen: true }
  },
  {
    time: '14:11:05',
    caption: 'She turns into the stairwell. He is alone in the corridor; the MIS office is ' +
      'closed for lunch.',
    scene: { visitor: 110, doorOpen: false }
  },
  {
    time: '14:11:40',
    caption: 'He stops at the corridor whiteboard and raises his phone.',
    scene: { visitor: 132, doorOpen: false, phone: true, prop: 'whiteboard' },
    detail: {
      title: 'What the whiteboard was showing',
      rows: [
        'LIBRARY SYSTEM MIGRATION — this week',
        '  contact: L. Ramos, circulation desk',
        '  ITSO ticket queue: #4471, #4488',
        '',
        'GUEST WIFI  ssid: CvSU-Guest   pass: Indang2026'
      ],
      hot: [1, 4],
      note: 'Nothing here is secret enough to lock up. It is enough to sound like a colleague ' +
        'on the phone the next morning.'
    }
  },
  {
    time: '14:13:18',
    caption: 'He leans over an unattended desk and photographs something on the monitor.',
    scene: { visitor: 175, doorOpen: false, phone: true, prop: 'desk' },
    detail: {
      title: 'The sticky note on the monitor',
      rows: [
        'lib-circ  /  Cvsu@2026!',
        '(handwritten, stuck to the bezel)'
      ],
      hot: [0],
      note: 'A password on a sticky note is a small, human shortcut. It survives every ' +
        'technical control the campus paid for.'
    }
  },
  {
    time: '14:15:02',
    caption: 'He leaves through the main entrance. Both boxes are still sealed.',
    scene: { visitor: 245, doorOpen: false }
  }
];

var frameIndex = 0;

function renderCctv(mountId) {
  var f = FRAMES[frameIndex];
  var dots = FRAMES.map(function (_, i) {
    return '<span class="dot' + (i === frameIndex ? ' on' : '') + (i < frameIndex ? ' seen' : '') + '"></span>';
  }).join('');

  document.getElementById(mountId).innerHTML =
    '<div class="scene-card">' +
      '<div class="scene-time"><span class="rec"></span>CAM 3 · MIS corridor · ' +
        escapeHtml(f.time) + '</div>' +
      doorSvg(f.scene) +
    '</div>' +
    '<div class="frame-dots">' + dots + '</div>' +
    '<div class="caption">' + escapeHtml(f.caption) + '</div>' +
    (f.detail ? '<button class="cta ghost" onclick="showDetail()">ZOOM: WHAT HIS PHONE ' +
      'WOULD HAVE CAPTURED</button><div id="frame-detail"></div>' : '<div id="frame-detail"></div>') +
    '<div class="frame-nav">' +
      '<button class="nav-btn" onclick="stepFrame(-1)"' + (frameIndex === 0 ? ' disabled' : '') +
        '>&#8592; PREV</button>' +
      '<button class="nav-btn primary" onclick="stepFrame(1)"' +
        (frameIndex === FRAMES.length - 1 ? ' disabled' : '') + '>NEXT &#8594;</button>' +
    '</div>' +
    (frameIndex === FRAMES.length - 1
      ? '<a class="cta" href="badge.html">RECONCILE WITH THE DOOR BADGE LOG</a>'
      : '');

  if (frameIndex === FRAMES.length - 1) Cyberity.clueFound('cctv_reviewed');
}

function stepFrame(delta) {
  frameIndex = Math.min(FRAMES.length - 1, Math.max(0, frameIndex + delta));
  renderCctv('detail');
  window.scrollTo(0, 0);
}

function showDetail() {
  var f = FRAMES[frameIndex];
  if (!f.detail) return;
  document.getElementById('frame-detail').innerHTML =
    '<div class="evidence open">' +
      '<div class="evidence-head"><span>' +
        '<span class="evidence-name">' + escapeHtml(f.detail.title) + '</span>' +
        '<span class="evidence-sub">Still readable in the photo</span>' +
      '</span></div>' +
      '<div class="evidence-body">' +
        '<div class="rows">' + renderRows(f.detail) + '</div>' +
        '<div class="note">' + escapeHtml(f.detail.note) + '</div>' +
      '</div>' +
    '</div>';
}

/* ===========================================================================
 * INCIDENT A · reconcile the camera against the badge log
 *
 * The finding is not handed over: the student decides, per door event, whether
 * the badge log explains it. Wrong calls are corrected inline and cost nothing.
 * ======================================================================== */
var DOOR_EVENTS = [
  {
    id: 'd1', time: '14:10:22', what: 'Side door opens · one person enters (J. Ramos)',
    matched: true, log: 'badge log 14:10:22 · card 0041 · J. Ramos · GRANTED'
  },
  {
    id: 'd2', time: '14:10:24', what: 'Second person enters through the same opening',
    matched: false, log: 'badge log · no swipe recorded at 14:10:24'
  },
  {
    id: 'd3', time: '14:12:55', what: 'Stairwell door opens · visitor moves to 2nd floor',
    matched: false, log: 'badge log · no swipe. Door was propped with a fire extinguisher'
  },
  {
    id: 'd4', time: '14:15:02', what: 'Main entrance · visitor exits',
    matched: true, log: 'exit is not badge-controlled · no swipe expected'
  }
];

var decided = {};

function renderBadge(mountId) {
  document.getElementById(mountId).innerHTML =
    '<div class="note">Four door events came off camera 3. For each one, decide whether the ' +
      'badge log explains it.</div>' +
    '<div id="events">' + DOOR_EVENTS.map(eventRow).join('') + '</div>' +
    '<div class="banner" id="recon-banner"></div>' +
    '<div class="section-label">Visitor book · guard house</div>' +
    '<div class="book">' +
      '<div class="book-row"><span>13:20</span><span>M. Alcantara · supplier · MIS</span></div>' +
      '<div class="book-row"><span>13:45</span><span>R. Perez · aircon repair · Library</span></div>' +
      '<div class="book-row empty"><span>&nbsp;</span><span>&nbsp;</span></div>' +
      '<div class="book-row empty"><span>&nbsp;</span><span>&nbsp;</span></div>' +
      '<div class="book-row"><span>15:30</span><span>J. Sy · courier · Registrar</span></div>' +
    '</div>' +
    '<div class="note">Nothing was written between 13:45 and 15:30.</div>';
}

function eventRow(e) {
  return '<div class="event" id="ev-' + e.id + '">' +
    '<div class="event-top"><span class="mono">' + escapeHtml(e.time) + '</span></div>' +
    '<div class="event-what">' + escapeHtml(e.what) + '</div>' +
    '<div class="event-actions" id="act-' + e.id + '">' +
      '<button class="small-btn" onclick="callEvent(\'' + e.id + '\', true)">BADGE LOG EXPLAINS IT</button>' +
      '<button class="small-btn" onclick="callEvent(\'' + e.id + '\', false)">NO MATCHING SWIPE</button>' +
    '</div>' +
    '<div class="event-log" id="log-' + e.id + '"></div>' +
  '</div>';
}

function callEvent(id, saysMatched) {
  var e = DOOR_EVENTS.filter(function (x) { return x.id === id; })[0];
  var right = (saysMatched === e.matched);
  var box = document.getElementById('ev-' + id);
  var log = document.getElementById('log-' + id);

  if (!right) {
    log.innerHTML = '<span class="warn">Not quite — check again: ' + escapeHtml(e.log) + '</span>';
    box.classList.add('wrong');
    return;
  }

  decided[id] = true;
  box.classList.remove('wrong');
  box.classList.add(e.matched ? 'ok' : 'flag');
  document.getElementById('act-' + id).innerHTML =
    '<span class="verdict ' + (e.matched ? 'ok' : 'hot') + '">' +
    (e.matched ? 'ACCOUNTED FOR' : 'UNEXPLAINED') + '</span>';
  log.innerHTML = '<span class="' + (e.matched ? 'ok' : 'hot') + '">' + escapeHtml(e.log) + '</span>';

  if (Object.keys(decided).length === DOOR_EVENTS.length) {
    var banner = document.getElementById('recon-banner');
    banner.className = 'banner show good';
    banner.textContent = 'Reconciliation complete. Two door events have no swipe behind them, ' +
      'and both are the same man: once behind somebody else\'s badge, once through a door ' +
      'propped open. The systems worked perfectly — they were simply never asked.';
    banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
    Cyberity.clueFound('badge_log_opened');
  }
}
