/* Cyberity — Risk Register simulation (level 106).
 * Local only. No network, no real budgets, no real systems.
 */

/* ---------------------------------------------------------------------------
 * Bridge to Android. Same contract as the other labs.
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
    flagDiscovered: function (token) { safe(function () { AndroidLab.notifyFlagDiscovered(token); }); }
  };
})();

/* Remembers what the student did while they move between pages. Falls back to
 * page memory if DOM storage is unavailable. */
var Store = (function () {
  var memory = {};
  return {
    get: function (key) {
      try {
        var v = window.sessionStorage.getItem('rr_' + key);
        if (v !== null) return v;
      } catch (e) { /* fall through */ }
      return memory.hasOwnProperty(key) ? memory[key] : null;
    },
    set: function (key, value) {
      memory[key] = value;
      try { window.sessionStorage.setItem('rr_' + key, value); } catch (e) { /* memory only */ }
    }
  };
})();

function escapeHtml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

/** Expands/collapses a box; reports [clue] the first time it opens. */
function toggleEvidence(boxId, clue) {
  var box = document.getElementById(boxId);
  var first = !box.classList.contains('open') && box.getAttribute('data-seen') !== '1';
  box.classList.toggle('open');
  var caret = box.querySelector('.evidence-caret');
  if (caret) caret.textContent = box.classList.contains('open') ? 'collapse' : 'expand';
  if (first) {
    box.setAttribute('data-seen', '1');
    if (clue) Cyberity.clueFound(clue);
  }
}

function rowsHtml(rows, hot) {
  return rows.map(function (line, i) {
    var safe = escapeHtml(line);
    return hot && hot.indexOf(i) !== -1 ? '<span class="warn">' + safe + '</span>' : safe;
  }).join('\n');
}

/* ---------------------------------------------------------------------------
 * Findings — the rubric-correct likelihood/impact live here and nowhere else.
 * ------------------------------------------------------------------------ */
var FINDINGS = {
  f01: {
    code: 'F-01',
    title: 'Admin portal accepts password-only sign-in',
    owner: 'Registrar IT',
    summary: 'Admin roles sign in with a password alone. Admin roles can change any ' +
      'student\'s grade.',
    evidence: [
      'history : INC-302 (09-13) — stolen password used to change a grade',
      'count   : 1 occurrence in the last 12 months',
      'reach   : admin roles can edit grades of every enrolled student',
      'records : ~9,400 students'
    ],
    hot: [1, 2],
    likelihood: 4,
    impact: 5
  },
  f02: {
    code: 'F-02',
    title: 'Lab PCs run an unsupported operating system',
    owner: 'CL-2 Computer Lab',
    summary: '40 lab PCs no longer receive security updates.',
    evidence: [
      'history : no incidents recorded',
      'exploit : public exploit exists for the unpatched OS',
      'reach   : PCs are reachable from the campus network',
      'data    : PCs store no student records; wiped nightly'
    ],
    hot: [0, 1, 3],
    likelihood: 3,
    impact: 2
  },
  f03: {
    code: 'F-03',
    title: 'Records shared through public links',
    owner: 'Registrar Office',
    summary: 'Staff routinely share files with "anyone with the link".',
    evidence: [
      'history : INC-301 (09-14) — enrollment masterlist exposed',
      'count   : 1 occurrence in the last 12 months',
      'audit   : 37 more files still set to "anyone with the link"',
      'data    : names, student numbers, home addresses — not grades'
    ],
    hot: [1, 3],
    likelihood: 4,
    impact: 4
  },
  f04: {
    code: 'F-04',
    title: 'Records database has no disk monitoring',
    owner: 'Registrar IT',
    summary: 'Nothing warns anyone before the database server\'s disk fills up.',
    evidence: [
      'history : INC-303 (09-15) — disk full, portal down',
      'count   : 1 occurrence in the last 12 months',
      'effect  : enrollment portal offline until someone notices',
      'data    : records intact; nothing exposed or changed'
    ],
    hot: [1, 2, 3],
    likelihood: 4,
    impact: 3
  }
};

var FINDING_ORDER = ['f01', 'f02', 'f03', 'f04'];

function scoreClass(score) {
  if (score <= 6) return 'low';
  if (score <= 12) return 'mid';
  return 'high';
}

function renderRegister(mountId) {
  var html = FINDING_ORDER.map(function (id) {
    var f = FINDINGS[id];
    return '' +
      '<a class="ticket-row" href="finding.html#' + id + '">' +
        '<span class="sev-bar medium"></span>' +
        '<span class="ticket-body">' +
          '<span class="ticket-meta">' +
            '<span class="ticket-id">' + f.code + '</span>' +
            '<span>' + escapeHtml(f.owner) + '</span>' +
          '</span>' +
          '<span class="ticket-title">' + escapeHtml(f.title) + '</span>' +
          '<span class="ticket-from">' + escapeHtml(f.summary) + '</span>' +
        '</span>' +
      '</a>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
}

function renderFinding(mountId) {
  var id = (window.location.hash || '#f01').substring(1);
  var f = FINDINGS[id] || FINDINGS.f01;
  if (!FINDINGS[id]) id = 'f01';

  Cyberity.clueFound('finding_opened_' + id);

  document.getElementById('finding-code').textContent = f.code;
  document.getElementById(mountId).innerHTML =
    '<h2>' + escapeHtml(f.title) + '</h2>' +
    '<div class="detail-sub">' + f.code + ' · owner: ' + escapeHtml(f.owner) + '</div>' +
    '<div class="report">' + escapeHtml(f.summary) + '</div>' +
    '<div class="section-label" style="padding:0 0 6px">Evidence</div>' +
    '<div class="rows">' + rowsHtml(f.evidence, f.hot) + '</div>' +
    '<a class="cta" href="matrix.html">Score it on the risk matrix</a>' +
    '<button class="cta danger" onclick="hideRisk()">REMOVE FROM REGISTER</button>' +
    '<div class="banner" id="hide-banner"></div>';
}

/** The dangerous action: making the report look better by deleting a risk. */
function hideRisk() {
  var banner = document.getElementById('hide-banner');
  banner.className = 'banner show bad';
  banner.textContent = 'Blocked by the simulation. Deleting a risk doesn\'t reduce it — it ' +
    'only guarantees nobody is watching when it happens again.';
  Cyberity.clueFound('risk_hidden');
}

/* ---------------------------------------------------------------------------
 * Risk matrix
 * ------------------------------------------------------------------------ */
var activeFinding = 'f01';

function placement(id) {
  var v = Store.get('place_' + id);
  if (!v) return null;
  var parts = v.split(',');
  return { l: parseInt(parts[0], 10), i: parseInt(parts[1], 10) };
}

function placedCorrectly(id) {
  var p = placement(id);
  return p !== null && p.l === FINDINGS[id].likelihood && p.i === FINDINGS[id].impact;
}

function selectFinding(id) {
  activeFinding = id;
  renderMatrix();
}

function placeAt(l, i) {
  // Stays on the same finding so the student sees the rubric feedback for it.
  Store.set('place_' + activeFinding, l + ',' + i);
  renderMatrix();

  var allRight = FINDING_ORDER.every(placedCorrectly);
  if (allRight) Cyberity.clueFound('risks_scored');
}

function renderMatrix() {
  // Finding chips
  document.getElementById('chips').innerHTML = FINDING_ORDER.map(function (id) {
    var cls = 'chip' + (id === activeFinding ? ' active' : '') + (placedCorrectly(id) ? ' done' : '');
    return '<button class="' + cls + '" onclick="selectFinding(\'' + id + '\')">' + FINDINGS[id].code + '</button>';
  }).join('');

  // Evidence for the finding being placed
  var f = FINDINGS[activeFinding];
  document.getElementById('active-evidence').innerHTML =
    '<div style="font-size:13px;font-weight:700;margin-bottom:6px">' + f.code + ' · ' + escapeHtml(f.title) + '</div>' +
    '<div class="rows">' + rowsHtml(f.evidence, f.hot) + '</div>';

  // Grid: likelihood 5 at the top, impact 1 on the left.
  var html = '';
  for (var l = 5; l >= 1; l--) {
    html += '<div class="axis-num">' + l + '</div>';
    for (var i = 1; i <= 5; i++) {
      var pins = FINDING_ORDER.filter(function (id) {
        var p = placement(id);
        return p !== null && p.l === l && p.i === i;
      }).map(function (id) {
        return '<span class="tag-pin">' + FINDINGS[id].code + '</span>';
      }).join('');
      html += '<div class="cell ' + scoreClass(l * i) + '" onclick="placeAt(' + l + ',' + i + ')">' +
        (pins || '<span style="opacity:0.45">' + (l * i) + '</span>') + '</div>';
    }
  }
  html += '<div></div>';
  for (var n = 1; n <= 5; n++) html += '<div class="axis-num">' + n + '</div>';
  document.getElementById('matrix').innerHTML = html;

  // Feedback on the active finding
  var fb = document.getElementById('place-banner');
  var p = placement(activeFinding);
  if (p === null) {
    fb.className = 'banner';
  } else if (placedCorrectly(activeFinding)) {
    fb.className = 'banner show good';
    fb.textContent = f.code + ' placed at likelihood ' + p.l + ' × impact ' + p.i + ' = ' + (p.l * p.i) + '. Matches the rubric.';
  } else {
    fb.className = 'banner show warn';
    fb.textContent = f.code + ' at ' + p.l + ' × ' + p.i + " doesn't match what the rubric says about this evidence. Re-read both.";
  }

  // Ranking of everything placed so far
  var placed = FINDING_ORDER.filter(function (id) { return placement(id) !== null; });
  placed.sort(function (a, b) {
    var pa = placement(a), pb = placement(b);
    return (pb.l * pb.i) - (pa.l * pa.i);
  });
  document.getElementById('ranking').innerHTML = placed.length === 0
    ? '<div class="note">Place findings on the matrix to rank them.</div>'
    : placed.map(function (id) {
        var q = placement(id);
        return '<div class="rank-row"><span>' + FINDINGS[id].code + ' · ' + escapeHtml(FINDINGS[id].title) +
          '</span><span class="rank-score">' + (q.l * q.i) + '</span></div>';
      }).join('');
}

/* ---------------------------------------------------------------------------
 * Treatment plan
 * ------------------------------------------------------------------------ */
var BUDGET = 150000;

/** Inherent scores come from the rubric, not the student's placement. */
function inherentScore(id) {
  return FINDINGS[id].likelihood * FINDINGS[id].impact;
}

var CONTROLS = [
  { id: 'mfa', name: 'MFA for all admin roles', cost: 40000, effect: { f01: 4 } },
  { id: 'replace', name: 'Replace all 40 lab PCs', cost: 900000, effect: { f02: 1 } },
  { id: 'links', name: 'Sharing policy + auto-expiring public links', cost: 25000, effect: { f03: 6 } },
  { id: 'monitor', name: 'Disk monitoring + log rotation', cost: 15000, effect: { f04: 3 } },
  { id: 'isolate', name: 'Upgrade lab OS + isolate lab network', cost: 60000, effect: { f02: 2 } },
  { id: 'soc', name: '24/7 outsourced security monitoring', cost: 140000, effect: { f01: 8, f03: 10 } }
];

var BEST_TOTAL = 15;

function selected(id) { return Store.get('ctl_' + id) === '1'; }

function peso(n) {
  return '₱' + String(n).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

function residuals() {
  var r = {};
  FINDING_ORDER.forEach(function (id) { r[id] = inherentScore(id); });
  CONTROLS.forEach(function (c) {
    if (!selected(c.id)) return;
    Object.keys(c.effect).forEach(function (fid) {
      r[fid] = Math.min(r[fid], c.effect[fid]);
    });
  });
  return r;
}

function spent() {
  return CONTROLS.reduce(function (sum, c) { return sum + (selected(c.id) ? c.cost : 0); }, 0);
}

function renderPlan() {
  document.getElementById('controls').innerHTML = CONTROLS.map(function (c) {
    var effects = Object.keys(c.effect).map(function (fid) {
      return FINDINGS[fid].code + ' ' + inherentScore(fid) + ' → ' + c.effect[fid];
    }).join(' · ');
    return '' +
      '<div class="layer' + (selected(c.id) ? ' on' : '') + '" onclick="toggleControl(\'' + c.id + '\')">' +
        '<span class="layer-text">' +
          '<div class="layer-cost">' + peso(c.cost) + '</div>' +
          '<div class="layer-name">' + escapeHtml(c.name) + '</div>' +
          '<div class="layer-sub">' + escapeHtml(effects) + '</div>' +
        '</span>' +
        '<span class="switch"></span>' +
      '</div>';
  }).join('');

  var s = spent();
  var over = s > BUDGET;
  document.getElementById('budget-top').innerHTML =
    '<span>Spent <b class="' + (over ? 'over' : '') + '">' + peso(s) + '</b></span>' +
    '<span>Budget ' + peso(BUDGET) + '</span>';
  var fill = document.getElementById('bar-fill');
  fill.style.width = Math.min(100, (s / BUDGET) * 100) + '%';
  fill.className = 'bar-fill' + (over ? ' over' : '');

  var r = residuals();
  var total = 0;
  document.getElementById('residual').innerHTML = FINDING_ORDER.map(function (id) {
    total += r[id];
    return '<div class="rank-row"><span>' + FINDINGS[id].code + ' · ' + escapeHtml(FINDINGS[id].title) +
      '</span><span class="rank-score">' + inherentScore(id) + ' → ' + r[id] + '</span></div>';
  }).join('') +
    '<div class="rank-row"><span><b>Total residual risk</b></span><span class="rank-score">' + total + '</span></div>';
}

function toggleControl(id) {
  Store.set('ctl_' + id, selected(id) ? '0' : '1');
  document.getElementById('memo').style.display = 'none';
  document.getElementById('plan-banner').className = 'banner';
  renderPlan();
}

function submitPlan() {
  var banner = document.getElementById('plan-banner');
  var memo = document.getElementById('memo');
  var s = spent();

  if (s > BUDGET) {
    banner.className = 'banner show bad';
    banner.textContent = 'Rejected: the plan is ' + peso(s - BUDGET) + ' over budget.';
    memo.style.display = 'none';
    return;
  }

  var r = residuals();
  var total = FINDING_ORDER.reduce(function (sum, id) { return sum + r[id]; }, 0);
  Cyberity.clueFound('plan_submitted');

  if (total <= BEST_TOTAL) {
    banner.className = 'banner show good';
    banner.textContent = 'Approved. Nothing inside this budget removes more risk.';
    document.getElementById('memo-body').innerHTML =
      'FROM   : CISO, CvSU\n' +
      'RE     : FY treatment plan — Registrar\n' +
      'SPEND  : ' + peso(s) + ' of ' + peso(BUDGET) + '\n' +
      'RESID. : ' + total + ' (from ' + FINDING_ORDER.reduce(function (sum, id) { return sum + inherentScore(id); }, 0) + ')\n' +
      'STATUS : APPROVED\n' +
      '<span class="ok">REF    : CYBERITY{r1sk_1s_4_budg3t}</span>';
    memo.style.display = 'block';
    Cyberity.flagDiscovered('ciso_memo');
  } else {
    banner.className = 'banner show warn';
    banner.textContent = 'Within budget, total residual ' + total + '. The CISO sends it back: ' +
      'another combination leaves less risk for the same money.';
    memo.style.display = 'none';
  }
}
