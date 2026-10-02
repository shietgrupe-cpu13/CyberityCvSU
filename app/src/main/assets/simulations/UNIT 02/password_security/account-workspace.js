"use strict";
// One fictional registrar account carries its state across the capstone's three task pages.
const CampusIncident = (() => {
    const key = "cyberity_unit2_incident_v1";
    let memory = null;
    function read() {
        try { return JSON.parse(sessionStorage.getItem(key)) || memory; } catch (_) { return memory; }
    }
    function write(value) {
        memory = value;
        try { sessionStorage.setItem(key, JSON.stringify(value)); } catch (_) {}
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
        try { return JSON.parse(sessionStorage.getItem(storageKey(id))) || memory[id]; }
        catch (_) { return memory[id]; }
    }
    function write(id, value) {
        memory[id] = value;
        try { sessionStorage.setItem(storageKey(id), JSON.stringify(value)); } catch (_) {}
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
            wrapper.appendChild(el); container.appendChild(wrapper); controls[id] = el; return el;
        }
        function select(id, label, options) {
            const wrapper = document.createElement("label"); wrapper.textContent = label; wrapper.htmlFor = id;
            const el = document.createElement("select"); el.id = id;
            options.forEach(([value, name]) => {
                const option = document.createElement("option"); option.value = value; option.textContent = name; el.appendChild(option);
            });
            el.value = options[0][0]; container.appendChild(wrapper); container.appendChild(el); controls[id] = el; return el;
        }
        function check(id, label) { return field(id, label, "checkbox"); }
        function action(id, label, callback) {
            const button = text("button", label); button.id = id;
            button.addEventListener("click", callback); controls[id] = button; return button;
        }
        function status(id, initial) { const el = text("p", initial); el.id = id; el.className = "workspace-status"; controls[id] = el; return el; }
        function generated(length = 20) {
            const chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#";
            const random = new Uint32Array(length); crypto.getRandomValues(random);
            return Array.from(random, n => chars[n % chars.length]).join("");
        }
        let validate = () => "";
        text("h2", "Account controls");
        text("p", "Only the fictional account changes here. Apply the settings, then save your work.");
        if (key === "201_0") {
            text("h3", "Maya's portal credential");
            text("p", "The old value CvSU2026! follows a public campus-name pattern. Generate a new secret for this portal only. The linked shopping account will be repaired in the next task.");
            select("length", "Generated password length", [["8", "8 characters"], ["12", "12 characters"], ["20", "20 characters"], ["24", "24 characters"]]);
            const output = status("built-password", "Current password: CvSU2026!");
            action("generate-password", "Generate portal replacement", () => {
                state.password = generated(Number(controls.length.value));
                state.generatedLength = state.password.length;
                output.textContent = "Fictional replacement: " + state.password + " · " + state.password.length + " characters";
                report("Check the length, confirm that this secret belongs only to the portal, then save the repair.");
            });
            check("unique", "Keep this generated credential unique to the portal");
            validate = () => !state.password || state.password.length < 20 ? "Generate a replacement of at least 20 characters for this classroom exercise." :
                !controls.unique.checked ? "Keep this credential unique to the portal." : "";

        } else if (key === "201_1") {
            const previous = UnitTwoCaseFlow.read(201)?.outcomes[0]?.state;
            const portal = status("portal-password", "Portal: " + (previous?.password || "credential not yet repaired"));
            text("p", "Your portal replacement from Task 1 is preserved. The audit also found the old credential on the linked shopping account. Give shopping a different replacement and protect both in the vault.");
            state.portal = previous?.password;
            const shop = status("shop-password", "Shopping account: same reused credential");
            action("replace-portal", "Generate a new portal credential", () => { state.portal = generated(); portal.textContent = "Portal: " + state.portal; });
            action("replace-shop", "Generate a different shopping credential", () => { state.shop = generated(); shop.textContent = "Shopping: " + state.shop; });
            select("vault", "Where to store them", [["shared", "Shared class document"], ["manager", "Protected password manager"]]);
            validate = () => !state.portal || !state.shop || state.portal === state.shop ? "Replace both reused credentials with different generated passwords." :
                controls.vault.value !== "manager" ? "Store the unique credentials in a protected password manager." : "";
        } else if (key === "201_2") {
            select("minimum", "Minimum password length", [["8", "8 characters"], ["15", "15 characters"], ["20", "20 characters"]]);
            select("rotation", "Force a password reset", [["monthly", "Every month"], ["compromise", "When compromise is suspected or confirmed"]]);
            check("blocklist", "Reject common and breached passwords");
            check("autofill", "Allow password manager paste and autofill");
            validate = () => Number(controls.minimum.value) < 15 || controls.rotation.value !== "compromise" || !controls.blocklist.checked || !controls.autofill.checked ?
                "Enable long passwords, breached-password blocking and autofill, with resets on compromise." : "";
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
            action("reset-exposed", "Reset the exposed fictional credentials", () => { state.reset = true; report("Exposed credentials replaced. Login throttling alone cannot protect copied hashes."); });
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
            action("enroll-passkey", "Register a passkey for the official portal", () => { state.passkey = true; passkey.textContent = "Passkey registered for portal.campus.example"; });
            select("backup-location", "Recovery code storage", [["chat", "Class group chat"], ["private", "Protected vault or protected offline copy"]]);
            validate = () => !state.passkey || controls["backup-location"].value !== "private" ? "Register the service-bound passkey and protect its recovery codes." : "";
        } else if (key === "204_0") {
            select("recovery-address", "Recovery mailbox", [["abandoned", "Abandoned mailbox"], ["protected", "Current mailbox with unique password and MFA"]]);
            select("backup-location", "Backup code storage", [["shared", "Shared class folder"], ["private", "Protected vault or protected offline copy"]]);
            action("rotate-codes", "Replace the exposed backup codes", () => { state.rotated = true; report("Old shared codes invalidated; fresh training codes issued."); });
            validate = () => controls["recovery-address"].value !== "protected" || controls["backup-location"].value !== "private" || !state.rotated ?
                "Update recovery to a protected mailbox, invalidate exposed codes, and store new codes privately." : "";
        } else if (key === "204_1") {
            status("personal-session", "Personal phone · trusted · active");
            const library = status("library-session", "Library computer · still active");
            action("revoke-library", "Revoke the library-computer session", () => { state.revoked = true; library.textContent = "Library computer · revoked"; });
            select("shared-device", "When leaving shared computers", [["close", "Close the browser only"], ["signout", "Sign out and avoid saving credentials"]]);
            validate = () => !state.revoked || controls["shared-device"].value !== "signout" ? "Revoke the active shared-computer session and choose explicit sign-out." : "";
        } else if (key === "204_2") {
            const summary = status("breach-state", "Compromised credential · attacker session · unknown recovery phone · forwarding active");
            [["reset-credential", "Replace compromised credential", "reset"], ["revoke-attacker", "Revoke attacker sessions", "revoked"],
                ["restore-recovery", "Remove unknown recovery phone", "recovery"], ["remove-forwarding", "Remove attacker forwarding rule", "forwarding"],
                ["report-breach", "Report the incident and review reused credentials", "reported"]].forEach(([id, label, flag]) =>
                action(id, label, () => { state[flag] = true; summary.textContent = Object.keys(state).length + " of 5 containment actions completed"; }));
            validate = () => ["reset", "revoked", "recovery", "forwarding", "reported"].some(flag => !state[flag]) ? "Close all five attacker access paths and response actions." : "";
        } else if (key === "205_1" || key === "205_2") {
            const incident = CampusIncident.read();
            text("h3", incident ? incident.account + " · incident " + incident.incident : "Registrar account · incident not yet investigated");
            if (key === "205_1") {
                const credential = status("incident-credential", "Recovered weak password · still in use");
                action("replace-incident-password", "Generate a unique registrar credential", () => { state.password = generated(); credential.textContent = "New training credential: " + state.password; });
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
        return {controls, state, validate, commit() {
            if (key === "205_1") CampusIncident.update({secured: true});
            if (key === "205_2") CampusIncident.update({contained: true});
        }};
    }
    return {mount};
})();
