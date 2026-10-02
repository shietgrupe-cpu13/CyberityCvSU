/* Cyberity — Hardening Review simulation (level 104).
 * Local only. No network, no real accounts, no real configuration.
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

/* Remembers what the student changed while they move between pages. Falls
 * back to page memory if DOM storage is unavailable, so toggles still work. */
var Store = (function () {
  var memory = {};
  return {
    get: function (key) {
      try {
        var v = window.sessionStorage.getItem('hr_' + key);
        if (v !== null) return v;
      } catch (e) { /* fall through */ }
      return memory.hasOwnProperty(key) ? memory[key] : null;
    },
    set: function (key, value) {
      memory[key] = value;
      try { window.sessionStorage.setItem('hr_' + key, value); } catch (e) { /* memory only */ }
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

/* ---------------------------------------------------------------------------
 * Least privilege · portal accounts
 * ------------------------------------------------------------------------ */
var PERMISSIONS = {
  'enrollment.view': 'See enrollment forms',
  'enrollment.encode': 'Type in enrollment forms',
  'grades.submit_own': 'Submit grades for own classes',
  'grades.view_all': 'See every student\'s grades',
  'grades.edit_locked': 'Change grades after they are locked',
  'records.export': 'Download full student records',
  'sharing.manage': 'Change who can open Registrar files',
  'profile.view': 'See student contact profiles',
  'users.manage': 'Create accounts and assign roles'
};

var ADMIN_PERMS = ['enrollment.view', 'enrollment.encode', 'grades.view_all',
  'grades.edit_locked', 'records.export', 'sharing.manage', 'users.manage'];

var USERS = [
  {
    id: 'staff',
    account: 'registrar.staff@cvsu.edu.ph',
    title: 'Records Officer',
    role: 'Registrar Admin',
    job: 'Full-time Registrar employee. Maintains all student records, corrects grades ' +
      'through the approved grade-change form, manages Registrar accounts.',
    history: 'role assigned 2021-06 · reviewed 2026-01',
    perms: ADMIN_PERMS,
    needs: ADMIN_PERMS
  },
  {
    id: 'reyes',
    account: 'a.reyes@cvsu.edu.ph',
    title: 'Faculty · ITEC 106',
    role: 'Faculty',
    job: 'Teaches ITEC 106. Submits end-of-term grades for her own classes.',
    history: 'role assigned 2019-08 · reviewed 2026-01',
    perms: ['grades.submit_own'],
    needs: ['grades.submit_own']
  },
  {
    id: 'jdcruz',
    account: 'j.dcruz@cvsu.edu.ph',
    title: 'Student Assistant · 10 hrs/week',
    role: 'Registrar Admin',
    job: 'Encodes paper enrollment forms into the portal during enrollment week. ' +
      'Views forms to check them. No other duties.',
    history: 'role assigned 2025-08 "temporary, enrollment rush" · never reviewed',
    perms: ADMIN_PERMS,
    needs: ['enrollment.view', 'enrollment.encode'],
    clue: 'user_opened_jdcruz',
    jobClue: 'job_desc_opened'
  },
  {
    id: 'guidance',
    account: 'guidance.office@cvsu.edu.ph',
    title: 'Guidance Counselor',
    role: 'Guidance',
    job: 'Contacts students referred for counseling. Needs student contact details only.',
    history: 'role assigned 2022-02 · reviewed 2026-01',
    perms: ['profile.view'],
    needs: ['profile.view']
  }
];

/** Working copy of each account's permissions, restored from storage. */
var editing = {};

function sameSet(a, b) {
  if (a.length !== b.length) return false;
  for (var i = 0; i < a.length; i++) if (b.indexOf(a[i]) === -1) return false;
  return true;
}

function renderRoles(mountId) {
  var html = USERS.map(function (u, i) {
    var saved = Store.get('perms_' + u.id);
    editing[u.id] = saved !== null ? (saved === '' ? [] : saved.split(',')) : u.perms.slice();

    var permsHtml = Object.keys(PERMISSIONS).map(function (p) {
      var on = editing[u.id].indexOf(p) !== -1;
      return '' +
        '<div class="perm' + (on ? ' on' : '') + '" id="p-' + u.id + '-' + p.replace('.', '_') + '"' +
          ' onclick="togglePerm(\'' + u.id + '\', \'' + p + '\')">' +
          '<span class="check">' + (on ? '&#10003;' : '') + '</span>' +
          '<span>' +
            '<div class="perm-name">' + escapeHtml(p) + '</div>' +
            '<div class="perm-sub">' + escapeHtml(PERMISSIONS[p]) + '</div>' +
          '</span>' +
        '</div>';
    }).join('');

    return '' +
      '<div class="evidence" id="user-' + i + '">' +
        '<div class="evidence-head" onclick="toggleEvidence(\'user-' + i + '\', \'' + (u.clue || '') + '\')">' +
          '<span>' +
            '<span style="font-size:14px;font-weight:700">' + escapeHtml(u.account) + '</span>' +
            '<span class="evidence-sub">' + escapeHtml(u.title) + ' · role: ' + escapeHtml(u.role) + '</span>' +
          '</span>' +
          '<span class="evidence-caret">expand</span>' +
        '</div>' +
        '<div class="evidence-body">' +
          '<div class="evidence" id="job-' + i + '" style="background:#000a2a">' +
            '<div class="evidence-head" onclick="toggleEvidence(\'job-' + i + '\', \'' + (u.jobClue || '') + '\')">' +
              '<span class="evidence-name">Job description</span>' +
              '<span class="evidence-caret">expand</span>' +
            '</div>' +
            '<div class="evidence-body">' +
              '<div style="font-size:13px">' + escapeHtml(u.job) + '</div>' +
              '<div class="note">' + escapeHtml(u.history) + '</div>' +
            '</div>' +
          '</div>' +
          '<div class="section-label" style="padding:6px 0 2px">Permissions</div>' +
          permsHtml +
          '<button class="cta" onclick="savePerms(\'' + u.id + '\')">SAVE ACCOUNT</button>' +
          '<div class="banner" id="banner-' + u.id + '"></div>' +
        '</div>' +
      '</div>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
}

function togglePerm(userId, perm) {
  var list = editing[userId];
  var at = list.indexOf(perm);
  if (at === -1) list.push(perm); else list.splice(at, 1);
  var row = document.getElementById('p-' + userId + '-' + perm.replace('.', '_'));
  var on = at === -1;
  row.classList.toggle('on', on);
  row.querySelector('.check').innerHTML = on ? '&#10003;' : '';
}

function savePerms(userId) {
  var u = USERS.filter(function (x) { return x.id === userId; })[0];
  var list = editing[userId];
  var banner = document.getElementById('banner-' + userId);
  banner.className = 'banner show';

  var missing = u.needs.filter(function (p) { return list.indexOf(p) === -1; });
  var extra = list.filter(function (p) { return u.needs.indexOf(p) === -1; });

  if (missing.length > 0) {
    banner.classList.add('bad');
    banner.textContent = 'Not saved — this person can no longer do their job without: ' +
      missing.join(', ') + '.';
    return;
  }

  Store.set('perms_' + userId, list.join(','));

  if (extra.length > 0) {
    banner.classList.add('warn');
    banner.textContent = 'Saved, but ' + extra.length + ' permission(s) go beyond what the ' +
      'job description needs.';
    return;
  }

  banner.classList.add('good');
  if (sameSet(list, u.perms)) {
    banner.textContent = 'Saved. No change — this account already matches the job.';
  } else {
    banner.textContent = 'Saved. The account now has exactly what the job needs.';
    if (userId === 'jdcruz') Cyberity.clueFound('least_privilege_applied');
  }
}

/* ---------------------------------------------------------------------------
 * Fail-safe defaults · grade lock policy
 * ------------------------------------------------------------------------ */
var LOCK_POLICIES = {
  allow_edit: {
    tone: 'bad',
    text: 'Test: lock service offline -> edit ALLOWED. This is the setting that let the 02:14 edit through.'
  },
  allow_edit_and_log: {
    tone: 'warn',
    text: 'Test: lock service offline -> edit ALLOWED, warning logged. The log records the damage; it doesn\'t prevent it.'
  },
  ask_user: {
    tone: 'warn',
    text: 'Test: lock service offline -> "Grade may be locked. Continue?" -> edit ALLOWED. The attacker is the user — they will click Continue.'
  },
  deny_edit: {
    tone: 'good',
    text: 'Test: lock service offline -> edit DENIED, "Try again later" shown. Staff wait a few minutes; attackers get nothing.'
  }
};

function initFailsafe() {
  var saved = Store.get('lock_policy') || 'allow_edit';
  document.getElementById('policy').value = saved;
  paintPolicy(saved);
}

function paintPolicy(value) {
  document.getElementById('policy-line').textContent = 'on_lock_service_error: ' + value;
}

function saveAndTest() {
  var value = document.getElementById('policy').value;
  Store.set('lock_policy', value);
  paintPolicy(value);

  var p = LOCK_POLICIES[value];
  var banner = document.getElementById('policy-banner');
  banner.className = 'banner show ' + p.tone;
  banner.textContent = p.text;

  if (value === 'deny_edit') Cyberity.clueFound('failsafe_set');
}

/* ---------------------------------------------------------------------------
 * Defence in depth · layers and attack replay
 * ------------------------------------------------------------------------ */
var LAYERS = [
  {
    id: 'network',
    tag: 'Layer 1 · Network',
    name: 'Admin pages on campus network / VPN only',
    sub: 'Off-campus addresses never reach the admin portal',
    initial: false,
    stop: 'BLOCKED  request from 45.61.87.200 is off campus',
    pass: 'passed   admin portal open to the whole internet'
  },
  {
    id: 'mfa',
    tag: 'Layer 2 · Authentication',
    name: 'MFA required for admin roles',
    sub: 'A password alone can\'t open an admin session',
    initial: false,
    stop: 'BLOCKED  stolen password, but no second factor',
    pass: 'passed   stolen password was enough to sign in'
  },
  {
    id: 'alert',
    tag: 'Layer 3 · Monitoring',
    name: 'Alert on any change to a locked grade',
    sub: 'Registrar is paged the moment it happens',
    initial: false,
    stop: 'CAUGHT   Registrar paged at 02:14, session killed',
    pass: 'passed   nobody noticed until the professor did, 2 days later'
  },
  {
    id: 'integrity',
    tag: 'Layer 4 · Integrity',
    name: 'Nightly signed hashes of record files',
    sub: 'Proves exactly which file changed',
    initial: true,
    danger: true,
    stop: 'CAUGHT   hash mismatch on grades_bsit3a_2026.csv',
    pass: 'passed   no baseline — the change can\'t be proven'
  }
];

function layerOn(layer) {
  var saved = Store.get('layer_' + layer.id);
  return saved === null ? layer.initial : saved === '1';
}

function renderLayers(mountId) {
  var html = LAYERS.map(function (l) {
    return '' +
      '<div class="layer' + (layerOn(l) ? ' on' : '') + '" id="layer-' + l.id + '" onclick="toggleLayer(\'' + l.id + '\')">' +
        '<span class="layer-text">' +
          '<div class="layer-tag">' + escapeHtml(l.tag) + '</div>' +
          '<div class="layer-name">' + escapeHtml(l.name) + '</div>' +
          '<div class="layer-sub">' + escapeHtml(l.sub) + '</div>' +
        '</span>' +
        '<span class="switch"></span>' +
      '</div>';
  }).join('');
  document.getElementById(mountId).innerHTML = html;
}

function toggleLayer(id) {
  var l = LAYERS.filter(function (x) { return x.id === id; })[0];
  var on = !layerOn(l);
  Store.set('layer_' + id, on ? '1' : '0');
  document.getElementById('layer-' + id).classList.toggle('on', on);

  var banner = document.getElementById('layer-banner');
  if (l.danger && !on) {
    banner.className = 'banner show bad';
    banner.textContent = 'You just switched off the only layer that caught the real attack. ' +
      'Removing a working control never adds depth.';
    Cyberity.clueFound('integrity_layer_disabled');
  } else {
    banner.className = 'banner';
  }
}

function replayAttack() {
  var out = document.getElementById('replay-out');
  var lines = ['replay  Sat 09-13 02:14  j.dcruz (stolen password) from 45.61.87.200', ''];
  var holding = 0;

  LAYERS.forEach(function (l) {
    var on = layerOn(l);
    if (on) holding++;
    var line = escapeHtml(l.tag.split(' · ')[1].toLowerCase() + '  ' + (on ? l.stop : l.pass));
    lines.push('<span class="' + (on ? 'ok' : 'hot') + '">' + line + '</span>');
  });

  lines.push('');
  if (holding === LAYERS.length) {
    lines.push('<span class="ok">result  ' + holding + ' of ' + LAYERS.length +
      ' layers stop or catch this attack independently</span>');
    lines.push('<span class="ok">report  CYBERITY{l4y3rs_n0t_w4lls}</span>');
  } else {
    lines.push('<span class="' + (holding === 0 ? 'hot' : 'warn') + '">result  ' + holding +
      ' of ' + LAYERS.length + ' layers hold · report withheld until every layer is on</span>');
  }

  out.innerHTML = lines.join('\n');
  document.getElementById('replay-box').style.display = 'block';
  Cyberity.clueFound('attack_replayed');
  if (holding === LAYERS.length) Cyberity.flagDiscovered('replay_report');
}
