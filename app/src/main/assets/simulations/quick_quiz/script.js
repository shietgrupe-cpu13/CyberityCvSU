/* Cyberity — Sixty Seconds (level 350).
 * Local only. Six recall questions from Unit 3 against one 60-second clock.
 *
 * The clock is wall-clock based rather than a tick count, so a WebView that
 * throttles timers in the background cannot hand the student extra seconds.
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

var TOTAL_SECONDS = 60;
var PASS_MARK = 5;
var PASS_CODE = 'CYBERITY{s1xty_s3c0nds_fl4t}';

/* ---------------------------------------------------------------------------
 * The six questions. Kept short on purpose: at ten seconds each, a long stem
 * tests reading speed rather than recall.
 * ------------------------------------------------------------------------ */
var QUESTIONS = [
  {
    from: '302',
    q: 'Which part of this address decides who owns the page?<br>' +
       '<span class="mono q-url">cvsu.edu.ph.grades-portal.example/login</span>',
    options: ['cvsu.edu.ph', 'grades-portal.example', 'login', 'ph'],
    correct: 1,
    why: 'The owner is the two pieces just before the first single slash. Everything to ' +
      'the left is a label the attacker chose.'
  },
  {
    from: '304',
    q: 'A text lands in the same thread as your real GCash receipts. What does that prove?',
    options: [
      'It is genuine — only GCash can post there',
      'Nothing. The sender ID is a label anyone can set',
      'Your SIM was cloned',
      'Your phone is infected'
    ],
    correct: 1,
    why: 'Your phone groups messages by sender ID and never checks who set it, so a scam ' +
      'text lands under the real receipts.'
  },
  {
    from: '304',
    q: 'You have won a prize you never entered for. What gives every version of this away?',
    options: [
      'Bad grammar',
      'It comes from a mobile number',
      'You must send money or details before you receive anything',
      'The amount is large'
    ],
    correct: 2,
    why: 'Pay-to-receive is the constant. Fees, taxes and "refundable" deposits are the ' +
      'same move wearing different words.'
  },
  {
    from: '304',
    q: 'A caller from your bank’s fraud desk needs the OTP to cancel a fraudulent charge. What settles it?',
    options: [
      'He knew the last four digits of the card',
      'He called from a landline',
      'No bank ever asks for an OTP — the request itself is the proof',
      'He would not give an employee number'
    ],
    correct: 2,
    why: 'An OTP exists to prove the account owner authorises a transaction, so anyone ' +
      'asking for yours is authorising theirs.'
  },
  {
    from: '303',
    q: 'Someone with full hands asks you to hold the badge door. What is the risk called?',
    options: ['Phishing', 'Tailgating', 'Vishing', 'Smishing'],
    correct: 1,
    why: 'Tailgating: following an authorised person through a controlled door. The badge ' +
      'log shows one swipe and two people.'
  },
  {
    from: '304',
    q: 'You get an obvious scam text. What actually reduces what happens next?',
    options: [
      'Reply STOP to be removed from the list',
      'Reply to ask who they are',
      'Don’t reply. Block, report to your telco, and warn the people around you',
      'Nothing — they stop on their own'
    ],
    correct: 2,
    why: 'Any reply confirms a real person reads the number, and confirmed numbers get ' +
      'sold on. Reporting is the only step that touches the sender.'
  }
];

/* ---------------------------------------------------------------------------
 * Run state
 * ------------------------------------------------------------------------ */
var run = null;
var ticker = null;

function startQuiz() {
  run = {
    index: 0,
    answers: [],
    endsAt: Date.now() + TOTAL_SECONDS * 1000,
    over: false
  };
  Cyberity.clueFound('quiz_attempted');

  var label = document.querySelector('.timer-label');
  if (label) label.textContent = 'Time remaining';

  if (ticker) clearInterval(ticker);
  ticker = setInterval(tick, 100);

  renderQuestion();
  tick();
}

function secondsLeft() {
  if (!run) return TOTAL_SECONDS;
  return Math.max(0, (run.endsAt - Date.now()) / 1000);
}

function tick() {
  if (!run || run.over) return;

  var left = secondsLeft();
  var bar = document.getElementById('timer-fill');
  var num = document.getElementById('timer-num');

  if (bar) bar.style.width = (left / TOTAL_SECONDS * 100) + '%';
  if (num) num.textContent = Math.ceil(left) + 's';

  var strip = document.getElementById('timer');
  if (strip) {
    strip.className = 'timer' +
      (left <= 10 ? ' critical' : (left <= 20 ? ' low' : ''));
  }

  if (left <= 0) finish(true);
}

function answer(choice) {
  if (!run || run.over) return;
  run.answers[run.index] = choice;
  run.index++;

  if (run.index >= QUESTIONS.length) {
    finish(false);
  } else {
    renderQuestion();
  }
}

function renderQuestion() {
  var q = QUESTIONS[run.index];

  var options = q.options.map(function (text, i) {
    return '<button class="quiz-option" onclick="answer(' + i + ')">' +
      escapeHtml(text) + '</button>';
  }).join('');

  document.getElementById('stage').innerHTML =
    '<div class="quiz-count">Question ' + (run.index + 1) + ' of ' + QUESTIONS.length +
      ' <span class="quiz-from">from ' + escapeHtml(q.from) + '</span></div>' +
    '<div class="artifact a-msg quiz-card">' +
      '<span class="artifact-tag">QUESTION</span>' +
      '<div class="quiz-q">' + q.q + '</div>' +
    '</div>' +
    '<div class="quiz-options">' + options + '</div>';
}

function finish(timedOut) {
  run.over = true;
  if (ticker) { clearInterval(ticker); ticker = null; }

  var score = 0;
  QUESTIONS.forEach(function (q, i) {
    if (run.answers[i] === q.correct) score++;
  });

  var passed = score >= PASS_MARK;

  var rows = QUESTIONS.map(function (q, i) {
    var given = run.answers[i];
    var right = given === q.correct;
    var state = given === undefined
      ? '<span class="res-mark skipped">NOT REACHED</span>'
      : (right ? '<span class="res-mark ok">CORRECT</span>'
               : '<span class="res-mark bad">WRONG</span>');

    return '<div class="res-row">' +
        '<div class="res-top">' +
          '<span class="res-n">Q' + (i + 1) + '</span>' + state +
        '</div>' +
        '<div class="res-q">' + q.q + '</div>' +
        (right ? '' :
          '<div class="res-answer">Answer: <b>' + escapeHtml(q.options[q.correct]) + '</b></div>' +
          '<div class="note">' + escapeHtml(q.why) + '</div>') +
      '</div>';
  }).join('');

  var headline = timedOut
    ? 'Time. You answered ' + run.answers.filter(function (a) { return a !== undefined; }).length +
      ' of ' + QUESTIONS.length + '.'
    : 'All six answered with ' + Math.ceil(secondsLeft()) + 's to spare.';

  document.getElementById('stage').innerHTML =
    '<div class="score-card ' + (passed ? 'passed' : 'failed') + '">' +
      '<div class="score-line">' + escapeHtml(headline) + '</div>' +
      '<div class="score-big">' + score + ' / ' + QUESTIONS.length + '</div>' +
      '<div class="score-sub">' +
        (passed ? 'Pass mark is ' + PASS_MARK + '. Well inside it.'
                : 'Pass mark is ' + PASS_MARK + '. Read the answers below, then run it again.') +
      '</div>' +
    '</div>' +
    (passed
      ? '<div class="banner show good">Pass code: <b class="mono">' + PASS_CODE + '</b></div>'
      : '') +
    '<button class="cta" onclick="startQuiz()">' +
      (passed ? 'RUN IT AGAIN' : 'TRY AGAIN — COSTS NOTHING') + '</button>' +
    '<div class="section-label">Every question</div>' +
    rows +
    '<div class="appui"><b>Retries are free</b>This one is timed, not punishing — no ' +
      'hearts are spent here however many times you run it.</div>';

  // Freeze the clock so the results screen doesn't still read "time remaining".
  var bar = document.getElementById('timer-fill');
  if (bar && timedOut) bar.style.width = '0%';
  var label = document.querySelector('.timer-label');
  if (label) label.textContent = timedOut ? 'Time ran out' : 'Run finished';
  if (timedOut) {
    var num = document.getElementById('timer-num');
    if (num) num.textContent = '0s';
  }

  if (passed) {
    Cyberity.clueFound('quiz_passed');
    Cyberity.flagDiscovered('pass_code');
  }

  window.scrollTo(0, 0);
}

/* ---------------------------------------------------------------------------
 * Opening screen
 * ------------------------------------------------------------------------ */
function renderStart(mountId) {
  document.getElementById(mountId).innerHTML =
    '<div class="quiz-intro">' +
      '<div class="quiz-big">6 questions</div>' +
      '<div class="quiz-big accent">60 seconds</div>' +
      '<div class="quiz-intro-note">One clock for the whole set — it does not reset ' +
        'between questions. Five correct passes. Tapping an option moves you straight on, ' +
        'so read before you tap.</div>' +
    '</div>' +
    '<button class="cta" onclick="startQuiz()">START</button>' +
    '<div class="appui"><b>Nothing to lose</b>Running out of time costs no hearts, and you ' +
      'can restart as often as you like.</div>';
}
