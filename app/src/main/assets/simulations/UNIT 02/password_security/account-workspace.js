"use strict";
const UnitTwoStorage = (() => {
    const prefix = 'cyberity_unit2:';
    let memory = {};
    function fallback() {
        try {
            if (window.name.startsWith(prefix)) {
                const saved = JSON.parse(window.name.slice(prefix.length));
                if (saved && typeof saved === 'object' && !Array.isArray(saved)) memory = saved;
            }
        } catch (_) {}
        return memory;
    }
    function getItem(key) {
        const saved = fallback();
        if (Object.prototype.hasOwnProperty.call(saved, key)) return saved[key];
        try { return sessionStorage.getItem(key); } catch (_) { return null; }
    }
    function setItem(key, value) {
        memory = {...fallback(), [key]: value};
        try { window.name = prefix + JSON.stringify(memory); } catch (_) {}
        try { sessionStorage.setItem(key, value); } catch (_) {}
    }
    return {getItem, setItem};
})();

// One fictional registrar account carries its state across the capstone's three task pages.
const CampusIncident = (() => {
    const key = "cyberity_unit2_incident_v1";
    let memory = null;
    function read() {
        try { return JSON.parse(UnitTwoStorage.getItem(key)) || memory; } catch (_) { return memory; }
    }
    function write(value) {
        memory = value;
        UnitTwoStorage.setItem(key, JSON.stringify(value));
        return value;
    }
    function begin(record) {
        return write({account: "registrar.training@campus.example", incident: record.salt,
            record, recovered: false, secured: false, contained: false});
    }
    function update(changes) { return write(Object.assign({}, read(), changes)); }
    return {read, begin, update};
})();

// Each level is one continuing case. Only verified stages enter the case journal.
const UnitTwoCaseFlow = (() => {
    const scenarios = {
        201: ["Student portal credential audit", "student.training@campus.example", ["Replace the predictable portal credential", "Remove reuse on the linked shopping account", "Repair the portal password policy"]],
        202: ["Campus portal attack investigation", "portal.campus.example", ["Classify the first guessing alert", "Investigate the leaked-pair escalation", "Respond to the copied password database"]],
        203: ["Student sign-in protection", "student.training@campus.example", ["Enrol a genuine second factor", "Respond to unexpected prompts on that account", "Upgrade sign-in and protect recovery"]],
        204: ["Student account recovery and containment", "student.training@campus.example", ["Repair exposed recovery methods", "Close the abandoned library session", "Contain the subsequent confirmed breach"]],
        205: ["Registrar mailbox incident", "registrar.training@campus.example", ["Prove the exposed credential", "Secure sign-in", "Remove persistent attacker access"]],
        250: ["Authorized registrar credential audit", "registrar.training@campus.example", ["Assess the legacy common-word record", "Assess the legacy campus-pattern record", "Assess the legacy two-digit recovery PIN"]]
    };
    const memory = {};
    const storageKey = id => "cyberity_unit2_case_" + id;
    function read(id) {
        try { return JSON.parse(UnitTwoStorage.getItem(storageKey(id))) || memory[id]; }
        catch (_) { return memory[id]; }
    }
    function write(id, value) {
        memory[id] = value;
        UnitTwoStorage.setItem(storageKey(id), JSON.stringify(value));
        return value;
    }
    function begin(id) {
        return write(id, {id: Date.now().toString(36) + "-" + crypto.getRandomValues(new Uint32Array(1))[0].toString(36), completed: [], outcomes: {}});
    }
    function prerequisite(id, index) {
        const run = read(id);
        return index > 0 && (!run || Array.from({length: index}, (_, n) => n).some(n => !run.completed.includes(n))) ?
            "Complete the earlier task in this same case first. Return to the task panel and continue in order." : "";
    }
    function finish(id, index, outcome, state = {}) {
        if (prerequisite(id, index)) return false;
        const run = read(id) || begin(id);
        if (!run.completed.includes(index)) run.completed.push(index);
        run.outcomes[index] = {summary: outcome, state};
        write(id, run);
        return true;
    }
    function describe(id, index) {
        const [title, account, stages] = scenarios[id];
        const run = read(id);
        return {title, account, stage: stages[index], run, previous: index > 0 ? run?.outcomes[index - 1] : null};
    }
    return {begin, read, prerequisite, finish, describe};
})();

const AccountWorkspace = (() => {
    function mount(container, key, report) {
        const controls = {}, state = {};
        function text(tag, content) {
            const el = document.createElement(tag); el.textContent = content; container.appendChild(el); return el;
        }
        function field(id, label, type = "text", value = "") {
            const el = document.createElement("input");
            el.id = id; el.type = type; el.value = value; el.autocomplete = "off";
            const wrapper = document.createElement("label"); wrapper.textContent = label;
            wrapper.dataset.control = id;
            wrapper.appendChild(el); container.appendChild(wrapper); controls[id] = el; return el;
        }
        function select(id, label, options) {
            const wrapper = document.createElement("label"); wrapper.textContent = label; wrapper.htmlFor = id;
            wrapper.dataset.control = id;
            const el = document.createElement("select"); el.id = id;
            el.dataset.control = id;
            options.forEach(([value, name]) => {
                const option = document.createElement("option"); option.value = value; option.textContent = name; el.appendChild(option);
            });
            el.value = options[0][0]; container.appendChild(wrapper); container.appendChild(el); controls[id] = el; return el;
        }
        function check(id, label) { return field(id, label, "checkbox"); }
        function action(id, label, callback) {
            const button = text("button", label); button.id = id;
            button.dataset.control = id;
            button.addEventListener("click", callback); controls[id] = button; return button;
        }
        function status(id, initial) { const el = text("p", initial); el.id = id; el.dataset.control = id; el.className = "workspace-status"; controls[id] = el; return el; }
        function generated(length = 20) {
            const chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#";
            const random = new Uint32Array(length); crypto.getRandomValues(random);
            return Array.from(random, n => chars[n % chars.length]).join("");
        }
        const repairs = [];
        function passwordRepair(prefix, label, oldPassword, applyId, minimum = 15, forbidden = () => []) {
            text("h3", label);
            text("p", "Use a made-up training password, never a real account password. Enter a unique replacement, confirm it, save it, then test the fictional sign-in.");
            const next = field(prefix + "-new", "New training password", "password");
            const confirm = field(prefix + "-confirm", "Confirm new training password", "password");
            next.maxLength = confirm.maxLength = 128;
            action(prefix + "-suggest", "Suggest a training password", () => {
                next.value = generated(Math.max(20, minimum));
                result.textContent = "Fictional suggestion: " + next.value + " · copy it into confirmation, then save";
                report("Suggestion filled. Confirm it yourself before saving.");
            });
            const result = status(prefix + "-status", "Old credential still active · replacement not saved");
            const problem = () => next.value.length < minimum || next.value.length > 128 ? "Use " + minimum + "–128 characters for this training replacement." :
                next.value !== confirm.value ? "The new password and confirmation do not match." :
                next.value === oldPassword || forbidden().includes(next.value) ? "Choose a different password; the old or linked credential cannot be reused." :
                /^(.)\1+$/.test(next.value) || /^(password|cvsu|campus|welcome|qwerty|123456)/i.test(next.value) ? "That value follows a common or campus-name pattern. Use a less predictable training passphrase." : "";
            action(applyId, "Save " + label.toLowerCase(), () => {
                const issue = problem(); if (issue) { report(issue); return; }
                state[prefix] = {password: next.value, tested: false};
                controls[prefix + "-login"].disabled = false;
                controls[prefix + "-test"].disabled = false;
                result.textContent = "Replacement saved · old credential rejected · test sign-in pending";
                report("Replacement saved for this fictional service. Test the new credential below.");
            });
            const login = field(prefix + "-login", "Test sign-in with the new training password", "password");
            login.maxLength = 128;
            login.disabled = true;
            action(prefix + "-test", "Test fictional sign-in", () => {
                const saved = state[prefix];
                if (problem() || !saved || saved.password !== next.value || login.value !== saved.password) {
                    if (saved) saved.tested = false;
                    result.textContent = "Sign-in rejected · check the saved replacement";
                    report("Save a valid replacement, then enter that same value in the test sign-in field."); return;
                }
                saved.tested = true;
                result.textContent = "New credential accepted · old credential rejected · account repair verified";
                report("Fictional sign-in passed with the replacement credential.");
            });
            controls[prefix + "-test"].disabled = true;
            const valid = () => !problem() && state[prefix]?.password === next.value && state[prefix]?.tested && login.value === next.value;
            const repair = {valid, value: () => state[prefix]?.password}; repairs.push(repair); return repair;
        }
        let validate = () => "";
        text("h2", "Account controls");
        text("p", "Only the fictional account changes here. Apply the settings, then save your work.");
        if (key === "201_0") {
            text("h3", "Maya's portal credential");
            text("p", "The old value CvSU2026! follows a public campus-name pattern. Enter a new secret for this portal only, or use the training generator. The linked shopping account will be repaired in the next task.");
            check("unique", "Keep this replacement unique to the portal");
            validate = () => !controls.unique.checked ? "Keep this credential unique to the portal." : "";

        } else if (key === "201_1") {
            const previous = UnitTwoCaseFlow.read(201)?.outcomes[0]?.state;
            status("portal-password", "Portal: " + (previous?.password || "credential not yet repaired"));
            text("p", "Your portal replacement from Task 1 is preserved. The audit also found the old credential on the linked shopping account. Give shopping a different replacement and protect both in the vault.");
            state.portal = previous?.password;
            status("shop-password", "Shopping account: same reused credential");
            text("p", "Keep the already repaired portal credential. Use the shopping replacement form below to remove reuse on the other service.");
            select("vault", "Where to store them", [["shared", "Shared class document"], ["manager", "Protected password manager"]]);
            validate = () => !state.portal || !state.shop || state.portal === state.shop ? "Replace both reused credentials with different generated passwords." :
                controls.vault.value !== "manager" ? "Store the unique credentials in a protected password manager." : "";
        } else if (key === "201_2") {
            select("minimum", "Minimum password length", [["8", "8 characters"], ["15", "15 characters"], ["20", "20 characters"]]);
            select("rotation", "Force a password reset", [["monthly", "Every month"], ["compromise", "When compromise is suspected or confirmed"]]);
            check("blocklist", "Reject common and breached passwords");
            check("autofill", "Allow password manager paste and autofill");
            field("policy-sample", "Try a fictional password against the proposed policy", "password");
            const policyResult = status("policy-result", "Run both checks: reject a weak password, then accept a long password. Load a sample and press Test after each one.");
            const policyChecks = status("policy-checks", "Weak password: pending · Long password: pending");
            action("policy-weak-sample", "Use weak sample", () => {
                controls["policy-sample"].value = "passwordpasswordpassword";
                report("Weak sample loaded. Press Test proposed password policy to check that the blocklist rejects it.");
            });
            action("policy-long-sample", "Use long sample", () => {
                controls["policy-sample"].value = "Ocean!Lantern-Bicycle-42";
                report("Long sample loaded. Press Test proposed password policy to check that it is accepted.");
            });
            action("test-policy", "Test proposed password policy", () => {
                const sample = controls["policy-sample"].value;
                const settings = [controls.minimum.value, controls.rotation.value, controls.blocklist.checked, controls.autofill.checked].join(":");
                if (state.policySettings !== settings) {state.policyTests = {}; state.policySettings = settings;}
                const weak = /^(password|cvsu|campus|welcome|123456)/i.test(sample);
                const accepted = sample.length >= Number(controls.minimum.value) && !(controls.blocklist.checked && weak);
                if (weak && !accepted) state.policyTests.weak = true;
                if (!weak && sample.length >= 15 && accepted) state.policyTests.long = true;
                policyChecks.textContent = "Weak password: " + (state.policyTests.weak ? "rejected ✓" : "pending") +
                    " · Long password: " + (state.policyTests.long ? "accepted ✓" : "pending");
                policyResult.textContent = accepted ? "Training password accepted by this policy." :
                    "Training password rejected" + (sample.length < Number(controls.minimum.value) ? ": use at least " + controls.minimum.value + " characters." : " by the blocklist.");
                report(state.policyTests.weak && state.policyTests.long ? "Both tests passed. Select VERIFY TASK AND SAVE CASE to finish." :
                    !state.policyTests.weak ? "Next: use the weak sample and test that it is rejected." : "Next: use the long sample and test that it is accepted.");
            });
            validate = () => Number(controls.minimum.value) < 15 || controls.rotation.value !== "compromise" || !controls.blocklist.checked || !controls.autofill.checked ?
                "Enable long passwords, breached-password blocking and autofill, with resets on compromise." :
                state.policySettings !== [controls.minimum.value, controls.rotation.value, controls.blocklist.checked, controls.autofill.checked].join(":") || !state.policyTests?.weak || !state.policyTests?.long ?
                    "Test that the configured policy rejects a known weak value and accepts a long unique training value." : "";
        } else if (key === "202_0" || key === "202_1") {
            const isStuffing = key === "202_1";
            text("h3", "Portal login investigation");
            const log = status("login-records", "Login records not opened.");
            action("inspect-logins", "Open portal login records", () => {
                state.inspected = true;
                log.textContent = isStuffing ?
                    "03:14 · student01 · leaked shopping pair · success\n03:14 · student02 · leaked shopping pair · failed\n03:15 · student03 · leaked shopping pair · success\nThousands of accounts, one known leaked pair per account." :
                    "02:10 · student01 · Campus2026! · failed\n02:11 · student01 · Password1! · failed\n02:12 · student01 · Welcome1! · failed\nOne account, many common-word variations; no successful login.";
            });
            select("attack-classification", "Record your investigation finding", [["unclassified", "Not yet classified"],
                ["dictionary", "Dictionary guessing"], ["stuffing", "Credential stuffing"], ["brute", "Exhaustive brute force"]]);
            if (isStuffing) {
                check("reset-affected", "Replace exposed credentials on affected accounts");
                check("remove-reuse", "Issue a different credential for each affected service");
                check("protect-signin", "Enable additional sign-in protection");
                action("inspect-sessions", "Review sessions from successful unfamiliar logins", () => {
                    state.sessions = true; report("Two successful unfamiliar sessions identified for containment.");
                });
            } else {
                check("throttle", "Limit repeated online login attempts");
                check("block-common", "Reject common and exposed password values");
            }
            validate = () => !state.inspected ? "Inspect the login records before recording a finding." :
                controls["attack-classification"].value !== (isStuffing ? "stuffing" : "dictionary") ?
                    "Your finding does not fit the account and candidate pattern in the records." :
                    isStuffing ? (!controls["reset-affected"].checked || !controls["remove-reuse"].checked || !controls["protect-signin"].checked || !state.sessions ?
                        "Replace exposed credentials, remove reuse, protect sign-in, and inspect successful sessions." : "") :
                        (!controls.throttle.checked || !controls["block-common"].checked ? "Apply online throttling and common-password blocking." : "");
        } else if (key === "202_2") {
            text("h3", "Hashing before cracking");
            field("sample-a", "First classroom example", "text", "campus-demo");
            field("sample-b", "Change one character", "text", "campus-demO");
            const first = status("digest-a", "First digest not calculated");
            const second = status("digest-b", "Second digest not calculated");
            const difference = status("digest-change", "Compare the two full digests.");
            let calculating = false;
            action("calculate-demo", "Calculate both SHA-256 digests", async () => {
                if (calculating) return;
                const a = controls["sample-a"].value, b = controls["sample-b"].value;
                if (!a || !b || a.length > 128 || b.length > 128 || a === b) { report("Use two different classroom strings of up to 128 characters."); return; }
                calculating = true;
                state.demo = null;
                controls["calculate-demo"].disabled = true;
                try {
                    const [ha, hb] = await Promise.all([HashExercise.digest(a), HashExercise.digest(b)]);
                    first.textContent = ha; second.textContent = hb;
                    difference.textContent = Array.from(ha).filter((char, index) => char !== hb[index]).length + " of 64 hex positions changed. Both outputs remain 64 characters.";
                    state.demo = {a, b}; report("Now select storage that slows offline guessing. A hash is compared, not decrypted.");
                } catch (_) { report("Digest calculation failed. Try again."); }
                finally { calculating = false; controls["calculate-demo"].disabled = false; }
            });
            select("storage", "Account password storage", [["sha256", "Fast SHA-256 only"], ["argon", "Argon2id with unique salts and an appropriate cost"]]);
            validate = () => calculating || !state.demo || state.demo.a !== controls["sample-a"].value || state.demo.b !== controls["sample-b"].value ?
                "Calculate two different examples first so you can see how hash comparisons work." :
                controls.storage.value !== "argon" || !state.reset ? "Choose slow salted password storage and reset the exposed credentials." : "";
        } else if (key === "203_0") {
            select("factor", "Add a second sign-in method", [["question", "Secret question (something you know)"], ["app", "Authenticator app (something you have)"], ["key", "Security key (something you have)"]]);
            const proof = status("enrollment-code", "No possession factor enrolled.");
            action("enroll-factor", "Enrol the selected factor", () => {
                if (controls.factor.value === "question") { report("A secret question is another knowledge secret, not a possession factor."); return; }
                state.factor = controls.factor.value;
                state.code = String(crypto.getRandomValues(new Uint32Array(1))[0] % 1000000).padStart(6, "0");
                proof.textContent = "Training authenticator proof: " + state.code;
            });
            field("factor-proof", "Enter the training authenticator proof");
            validate = () => !state.code || state.factor !== controls.factor.value || controls["factor-proof"].value !== state.code ?
                "Enrol a possession factor and enter its displayed training proof." : "";
        } else if (key === "203_1") {
            const prompts = status("prompt-state", "6 unrequested approval prompts pending");
            action("deny-prompts", "Deny all unexpected prompts", () => { state.denied = true; prompts.textContent = "6 prompts denied; no attacker approval granted"; });
            action("review-logins", "Inspect official login history", () => { state.reviewed = true; report("Official history shows a new unfamiliar attempt. No code was shared."); });
            action("report-prompts", "Report suspicious prompts to campus IT", () => { state.reported = true; report("Training incident recorded locally."); });
            validate = () => !state.denied || !state.reviewed || !state.reported ? "Deny unexpected prompts, inspect official activity, and report the attempt." : "";
        } else if (key === "203_2") {
            const passkey = status("passkey-state", "Phishable code-based sign-in only");
            field("passkey-device", "Name Maya's personal device for the passkey");
            check("device-unlocked", "Approve the simulated device-unlock prompt on Maya's personal device");
            action("enroll-passkey", "Register a passkey for the official portal", () => {
                const device = controls["passkey-device"].value.trim();
                if (device.length < 2 || device.length > 60 || !controls["device-unlocked"].checked) {report("Name the personal device and approve its simulated unlock prompt."); return;}
                state.passkey = device; passkey.textContent = "Passkey registered for portal.campus.example · " + device;
            });
            select("backup-location", "Recovery code storage", [["chat", "Class group chat"], ["private", "Protected vault or protected offline copy"]]);
            validate = () => !state.passkey || state.passkey !== controls["passkey-device"].value.trim() || !controls["device-unlocked"].checked || controls["backup-location"].value !== "private" ? "Register the service-bound passkey and protect its recovery codes." : "";
        } else if (key === "204_0") {
            field("recovery-mailbox", "Enter Maya's current protected training mailbox (use a .example address)", "email");
            select("recovery-address", "Recovery mailbox", [["abandoned", "Abandoned mailbox"], ["protected", "Current mailbox with unique password and MFA"]]);
            select("backup-location", "Backup code storage", [["shared", "Shared class folder"], ["private", "Protected vault or protected offline copy"]]);
            action("rotate-codes", "Replace the exposed backup codes", () => { state.rotated = true; report("Old shared codes invalidated; fresh training codes issued."); });
            validate = () => !/^[^\s@]+@[^\s@]+\.example$/.test(controls["recovery-mailbox"].value) || controls["recovery-address"].value !== "protected" || controls["backup-location"].value !== "private" || !state.rotated ?
                "Update recovery to a protected mailbox, invalidate exposed codes, and store new codes privately." : "";
        } else if (key === "204_1") {
            status("personal-session", "Personal phone · trusted · active");
            const library = status("library-session", "Library computer · still active");
            action("revoke-library", "Revoke the library-computer session", () => { state.revoked = true; library.textContent = "Library computer · revoked"; });
            select("shared-device", "When leaving shared computers", [["close", "Close the browser only"], ["signout", "Sign out and avoid saving credentials"]]);
            validate = () => !state.revoked || controls["shared-device"].value !== "signout" ? "Revoke the active shared-computer session and choose explicit sign-out." : "";
        } else if (key === "204_2") {
            const summary = status("breach-state", "Compromised credential · attacker session · unknown recovery phone · forwarding active");
            [["revoke-attacker", "Revoke attacker sessions", "revoked"],
                ["restore-recovery", "Remove unknown recovery phone", "recovery"], ["remove-forwarding", "Remove attacker forwarding rule", "forwarding"],
                ["report-breach", "Report the incident and review reused credentials", "reported"]].forEach(([id, label, flag]) =>
                action(id, label, () => { state[flag] = true; summary.textContent = ["reset", "revoked", "recovery", "forwarding", "reported"].filter(flag=>state[flag]).length + " of 5 containment actions completed"; }));
            validate = () => ["reset", "revoked", "recovery", "forwarding", "reported"].some(flag => !state[flag]) ? "Close all five attacker access paths and response actions." : "";
        } else if (key === "205_1" || key === "205_2") {
            const incident = CampusIncident.read();
            text("h3", incident ? incident.account + " · incident " + incident.incident : "Registrar account · incident not yet investigated");
            if (key === "205_1") {
                const credential = status("incident-credential", "Recovered weak password · still in use");
                text("p", "Replace the recovered weak credential using the password form below, then verify sign-in before closing this task.");
                select("incident-factor", "Registrar sign-in protection", [["off", "Password only"], ["passkey", "Service-bound passkey"], ["app", "Authenticator app"]]);
                action("enroll-incident-factor", "Enrol registrar sign-in protection", () => {
                    if (controls["incident-factor"].value === "off") { report("Choose an additional protection before enrolling."); return; }
                    state.factor = controls["incident-factor"].value; report("Selected training protection enrolled for this registrar account.");
                });
                validate = () => !CampusIncident.read()?.recovered ? "Recover this account's hash in the previous task first." :
                    !state.password || !state.factor || state.factor !== controls["incident-factor"].value ? "Replace the recovered weak credential and enrol sign-in protection." : "";
            } else {
                const sessions = status("incident-sessions", "Owner phone active · unfamiliar desktop active");
                action("revoke-incident-session", "Revoke the unfamiliar desktop session", () => { state.revoked = true; sessions.textContent = "Owner phone active · unfamiliar desktop revoked"; });
                action("remove-incident-rule", "Remove forwarding to attacker@outside.example", () => { state.forwarding = true; report("Attacker forwarding removed from this registrar mailbox."); });
                select("incident-recovery", "Registrar recovery address", [["unknown", "Unknown attacker recovery mailbox"], ["owner", "Owner's protected recovery mailbox"]]);
                action("rotate-incident-codes", "Invalidate exposed registrar backup codes", () => { state.rotated = true; report("Old codes invalidated; new training codes stored privately."); });
                action("file-incident", "File the incident report and review reused credentials", () => { state.reported = true; report("Local training incident report prepared."); });
                validate = () => !CampusIncident.read()?.secured ? "Secure this same account's credential and MFA in the previous task first." :
                    !state.revoked || !state.forwarding || controls["incident-recovery"].value !== "owner" || !state.rotated || !state.reported ?
                        "Remove the attacker session and forwarding, restore protected recovery, rotate codes, and report." : "";
            }
        } else return null;
        // Credential-changing tasks require a real form submission and a tested
        // replacement. A button or checkbox alone cannot repair the account.
        if (key === "201_0") {
            const repair = passwordRepair("portal", "Portal credential", "CvSU2026!", "save-portal", 20);
            const previousValidate = validate;
            validate = () => {
                state.password = repair.valid() ? repair.value() : null;
                return !repair.valid() ? "Save and test the confirmed portal replacement." : previousValidate();
            };
        } else if (key === "201_1") {
            const previous = UnitTwoCaseFlow.read(201)?.outcomes[0]?.state?.password;
            const repair = passwordRepair("shopping", "Shopping credential", "CvSU2026!", "save-shopping", 15, () => [previous, state.portal]);
            validate = () => {
                state.portal = previous;
                state.shop = repair.valid() ? repair.value() : null;
                return !state.portal || !repair.valid() ? "Preserve the repaired portal credential and save and test a different shopping credential." :
                    controls.vault.value !== "manager" ? "Store the credentials in a protected password manager." : "";
            };
        } else if (["202_1", "202_2", "204_2", "205_1"].includes(key)) {
            const applyId = key === "202_2" ? "reset-exposed" : key === "204_2" ? "reset-credential" : "save-replacement";
            const repair = passwordRepair("replacement", key === "202_1" ? "Student01 portal credential" : "Replacement credential", "CvSU2026!", applyId);
            if (key === "202_1") passwordRepair("second-student", "Student03 portal credential", "CvSU2026!", "save-second-student", 15, () => [repair.value()]);
            const previousValidate = validate;
            validate = () => {
                if (!repairs.every(repair=>repair.valid())) return "Save and test a confirmed unique replacement for each affected account.";
                if (key === "202_2" || key === "204_2") state.reset = true;
                if (key === "205_1") state.password = repair.value();
                return previousValidate();
            };
        }
        function progress() {
            const yes = id => Boolean(controls[id]?.checked);
            const selected = (id, value) => controls[id]?.value === value;
            const repaired = () => repairs.length > 0 && repairs.every(repair => repair.valid());
            const steps = {
                '201_0': [['Save a unique replacement', Boolean(state.portal)], ['Test the new sign-in', repaired()], ['Keep it portal-only', yes('unique')]],
                '201_1': [['Carry forward the portal repair', Boolean(state.portal)], ['Save and test a different shop password', repaired()], ['Protect both in the vault', selected('vault','manager')]],
                '201_2': [['Allow long passwords', Number(controls.minimum?.value) >= 15], ['Block breached values and support autofill', yes('blocklist') && yes('autofill')], ['Reset on compromise', selected('rotation','compromise')], ['Test rejection and acceptance', Boolean(state.policyTests?.weak && state.policyTests?.long) && state.policySettings === [controls.minimum?.value, controls.rotation?.value, controls.blocklist?.checked, controls.autofill?.checked].join(':')]],
                '202_0': [['Inspect the login records', Boolean(state.inspected)], ['Classify the guessing pattern', selected('attack-classification','dictionary')], ['Apply throttling and blocking', yes('throttle') && yes('block-common')]],
                '202_1': [['Inspect and classify the leaked pairs', Boolean(state.inspected) && selected('attack-classification','stuffing')], ['Replace and test the credential', repaired()], ['Remove reuse and protect sign-in', yes('reset-affected') && yes('remove-reuse') && yes('protect-signin')], ['Review successful sessions', Boolean(state.sessions)]],
                '202_2': [['Calculate and compare both hashes', Boolean(state.demo) && state.demo.a === controls['sample-a']?.value && state.demo.b === controls['sample-b']?.value], ['Configure slow salted storage', selected('storage','argon')], ['Save and test the replacement', repaired()]],
                '203_0': [['Enrol a possession factor', Boolean(state.code) && state.factor === controls.factor?.value], ['Prove the enrolled factor', Boolean(state.code) && controls['factor-proof']?.value === state.code]],
                '203_1': [['Deny unexpected requests', Boolean(state.denied)], ['Inspect official login history', Boolean(state.reviewed)], ['Report the suspicious attempt', Boolean(state.reported)]],
                '203_2': [['Register the portal passkey', Boolean(state.passkey) && state.passkey === controls['passkey-device']?.value.trim() && yes('device-unlocked')], ['Protect recovery codes', selected('backup-location','private')]],
                '204_0': [['Restore a protected recovery mailbox', /^[^\s@]+@[^\s@]+\.example$/.test(controls['recovery-mailbox']?.value || '') && selected('recovery-address','protected')], ['Invalidate exposed codes', Boolean(state.rotated)], ['Store replacements privately', selected('backup-location','private')]],
                '204_1': [['Revoke the abandoned session', Boolean(state.revoked)], ['Choose explicit shared-device sign-out', selected('shared-device','signout')]],
                '204_2': [['Replace and test the compromised credential', repaired()], ['Revoke attacker sessions', Boolean(state.revoked)], ['Restore recovery and remove forwarding', Boolean(state.recovery) && Boolean(state.forwarding)], ['Record the incident', Boolean(state.reported)]],
                '205_1': [['Save and test a unique registrar password', repaired()], ['Enrol the selected sign-in protection', Boolean(state.factor) && state.factor === controls['incident-factor']?.value]],
                '205_2': [['Revoke the unfamiliar session', Boolean(state.revoked)], ['Remove forwarding and restore recovery', Boolean(state.forwarding) && selected('incident-recovery','owner')], ['Rotate backup codes', Boolean(state.rotated)], ['File the incident report', Boolean(state.reported)]]
            };
            return steps[key];
        }
        // Keep one practical panel in view. Completed panels remain available
        // through Back; every underlying validation still checks the whole case.
        function createStepper() {
            const groups = {
                '201_0': [[], ['portal-login','portal-test'], ['unique']],
                '201_1': [['portal-password'], ['shopping-new','shopping-confirm','shopping-suggest','save-shopping','shopping-login','shopping-test','shopping-status','shop-password'], ['vault']],
                '201_2': [['minimum'], ['blocklist','autofill'], ['rotation'], ['policy-sample','policy-weak-sample','policy-long-sample','test-policy','policy-result','policy-checks']],
                '202_0': [['inspect-logins','login-records'], ['attack-classification'], ['throttle','block-common']],
                '202_1': [['inspect-logins','login-records','attack-classification'], ['replacement-new','replacement-confirm','replacement-suggest','save-replacement','replacement-login','replacement-test','replacement-status','second-student-new','second-student-confirm','second-student-suggest','save-second-student','second-student-login','second-student-test','second-student-status'], ['reset-affected','remove-reuse','protect-signin'], ['inspect-sessions']],
                '202_2': [['sample-a','sample-b','calculate-demo','digest-a','digest-b','digest-change'], ['storage'], ['replacement-new','replacement-confirm','replacement-suggest','reset-exposed','replacement-login','replacement-test','replacement-status']],
                '203_0': [['factor','enroll-factor'], ['enrollment-code','factor-proof']],
                '203_1': [['deny-prompts','prompt-state'], ['review-logins'], ['report-prompts']],
                '203_2': [['passkey-device','device-unlocked','enroll-passkey','passkey-state'], ['backup-location']],
                '204_0': [['recovery-mailbox','recovery-address'], ['rotate-codes'], ['backup-location']],
                '204_1': [['personal-session','library-session','revoke-library'], ['shared-device']],
                '204_2': [['replacement-new','replacement-confirm','replacement-suggest','reset-credential','replacement-login','replacement-test','replacement-status','breach-state'], ['revoke-attacker'], ['restore-recovery','remove-forwarding'], ['report-breach']],
                '205_1': [['replacement-new','replacement-confirm','replacement-suggest','save-replacement','replacement-login','replacement-test','replacement-status','incident-credential'], ['incident-factor','enroll-incident-factor']],
                '205_2': [['revoke-incident-session','incident-sessions'], ['remove-incident-rule','incident-recovery'], ['rotate-incident-codes'], ['file-incident']]
            }[key];
            const original = Array.from(container.children);
            container.replaceChildren();
            container.classList.add('focused-workspace');
            const heading = text('h2','');
            const dots = text('div',''); dots.className='workspace-step-dots';
            const panels = groups.map(()=>{const panel=text('section',''); panel.className='workspace-step'; return panel;});
            const navigation=text('div',''); navigation.className='workspace-navigation';
            const back=document.createElement('button'); back.textContent='BACK';
            const next=document.createElement('button'); next.textContent='NEXT STEP'; next.className='primary';
            navigation.appendChild(back); navigation.appendChild(next);
            const context=text('details',''); context.className='workspace-context';
            const summary=document.createElement('summary'); summary.textContent='Account context and tools'; context.appendChild(summary);
            for (const node of original) {
                const id=node.dataset.control;
                if (!id) context.appendChild(node);
                else {const group=groups.findIndex(ids=>ids.includes(id)); panels[Math.max(0,group)].appendChild(node);}
            }
            let active=0;
            function refresh() {
                if (key === '201_2' && state.policySettings !== [controls.minimum.value, controls.rotation.value, controls.blocklist.checked, controls.autofill.checked].join(':')) {
                    state.policyTests = {};
                    controls['policy-checks'].textContent = 'Weak password: pending · Long password: pending';
                }
                const steps=progress();
                heading.textContent='Step ' + (active+1) + ' of ' + steps.length + ' · ' + steps[active][0];
                dots.replaceChildren();
                steps.forEach(([,done],index)=>{
                    const dot=document.createElement('span'); dot.className='workspace-dot' + (done ? ' done' : index === active ? ' current' : '');
                    dot.textContent=done ? '✓' : String(index+1); dots.appendChild(dot);
                });
                panels.forEach((panel,index)=>panel.hidden=index!==active);
                back.disabled=active===0;
                next.hidden=active===steps.length-1;
                next.disabled=!steps[active][1];
            }
            back.addEventListener('click',()=>{if(active>0) active--; refresh();});
            next.addEventListener('click',()=>{if(progress()[active][1] && active<panels.length-1) active++; refresh();});
            refresh(); return refresh;
        }
        const refreshSteps=createStepper();
        return {controls, state, validate, progress, refreshSteps, commit() {
            if (key === "205_1") CampusIncident.update({secured: true});
            if (key === "205_2") CampusIncident.update({contained: true});
        }};
    }
    return {mount};
})();
