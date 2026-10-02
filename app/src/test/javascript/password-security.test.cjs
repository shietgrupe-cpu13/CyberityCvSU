const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const {createHash, webcrypto} = require("node:crypto");
const root = path.resolve(__dirname, "../../main/assets/simulations/UNIT 02/password_security");
const levelFolders = {"201":"strong_passwords","202":"password_attacks","203":"multi_factor_authentication","204":"account_protection","205":"password_security_challenge","250":"hash_cracking_practice"};
const pagePath = key => path.join(root, "..", levelFolders[key.split("_")[0]], "case_" + key + ".html");
const source = name => fs.readFileSync(name.startsWith("case_") ? pagePath(name.slice(5, -5)) : path.join(root, name), "utf8");
const casesSource = source("cases.js"), engineSource = source("hash-engine.js");
const workspaceSource = source("account-workspace.js"), script = source("script.js");
const learningSource = source("learning-content.js");
const peopleSource = source("people.js");
const cases = vm.runInNewContext(casesSource + ";passwordCases");
const learning = vm.runInNewContext(learningSource + ";UnitTwoLearning");
const hash = text => createHash("sha256").update(text, "utf8").digest("hex");
function storage() {
    const values = new Map();
    return {getItem: key => values.get(key) || null, setItem: (key, value) => values.set(key, value)};
}
function harness(key, session = storage()) {
    let now = Date.UTC(2026, 9, 1, 10);
    const clues = [], elements = {}, all = [];
    function element(tag = "div") {
        const el = {tag, textContent: "", hidden: true, disabled: false, children: [], listeners: {},
            value: "", checked: false, className: "", attributes: {}, dataset: {},
            parentNode: {insertBefore() {}},
            classList: {add() {}, remove() {}, toggle() {}},
            setPointerCapture() {}, releasePointerCapture() {},
            closest(selector) { return selector === "[data-drop-zone]" && this.dataset.dropZone !== undefined ? this : null; },
            replaceChildren() { this.children = []; },
            appendChild(child) { this.children.push(child); return child; },
            removeChild(child) { this.children.splice(this.children.indexOf(child), 1); },
            get firstChild() { return this.children[0]; },
            setAttribute(name, value) { this.attributes[name] = value; },
            scrollIntoView() {}, addEventListener(type, callback) { this.listeners[type] = callback; }
        }; all.push(el); return el;
    }
    const html = fs.readFileSync(pagePath(key), "utf8");
    for (const match of html.matchAll(/id="([^"]+)"/g)) elements[match[1]] = element();
    const sandbox = {
        document: {body: {dataset: {case: key}}, getElementById: id => {
            const el = elements[id] || all.find(el => el.id === id);
            assert.ok(el, "missing HTML element " + id); return el;
        }, createElement: element, createTextNode: text => ({textContent: text}), addEventListener() {},
        querySelectorAll: () => all.filter(el => ["button", "input", "select", "textarea"].includes(el.tag))},
        window: {addEventListener() {}}, sessionStorage: session,
        AndroidLab: {notifyClueFound: clue => clues.push(clue), hashCandidate: hash},
        crypto: webcrypto, TextEncoder, Uint8Array, Uint32Array,
        Date: class extends Date {constructor(value) {super(value === undefined ? now : value);} static now() {return now;}},
        setInterval: () => 1, clearInterval() {}, setTimeout: callback => {callback(); return 1;}
    };
    vm.createContext(sandbox);
    vm.runInContext(casesSource + "\n" + engineSource + "\n" + workspaceSource + "\n" + learningSource + "\n" + peopleSource + "\n" + script, sandbox);
    return {elements, clues, all, session, run: code => vm.runInContext(code, sandbox), advance: ms => now += ms};
}

function assertVerified(h) {
    assert.equal(h.clues.length, 1, "verified practical work issues one proof directly");
    assert.equal(h.elements.result.hidden, false);
    assert.ok(h.elements.code.textContent.startsWith("CASE-"));
}
async function ready(h) { for (let i = 0; i < 4; i++) await h.run("Promise.resolve()"); }
async function recovered(h) {
    const record = h.run("record"); assert.ok(record);
    const password = Array.from(record.pool).find(candidate => hash(record.salt + ":" + candidate) === record.hash);
    assert.ok(password, "target must have a recoverable candidate"); return password;
}
async function configure(h, key) {
    const controls = h.run("workspace.controls");
    const click = id => controls[id].listeners.click();
    const check = id => controls[id].checked = true;
    const select = (id, value) => controls[id].value = value;
    if (key === "201_0") {
        await click("generate-password"); check("unique"); h.run("apply()");
        assert.equal(h.clues.length, 0, "short generated password is insufficient");
        select("length", "20"); await click("generate-password");

    } else if (key === "201_1") {
        await click("replace-portal"); await click("replace-shop"); select("vault", "manager");
    } else if (key === "201_2") {
        select("minimum", "15"); select("rotation", "compromise"); check("blocklist"); check("autofill");
    } else if (key === "202_0" || key === "202_1") {
        select("attack-classification", key === "202_0" ? "dictionary" : "stuffing");
        h.run("apply()"); assert.equal(h.clues.length, 0, "finding requires inspected evidence");
        await click("inspect-logins");
        select("attack-classification", "brute"); h.run("apply()");
        assert.equal(h.clues.length, 0, "incorrect investigation findings cannot issue proof");
        select("attack-classification", key === "202_0" ? "dictionary" : "stuffing");
        if (key === "202_0") { check("throttle"); check("block-common"); }
        else { check("reset-affected"); check("remove-reuse"); check("protect-signin"); await click("inspect-sessions"); }
    } else if (key === "202_2") {
        await click("calculate-demo");
        assert.equal(controls["digest-a"].textContent, hash("campus-demo"));
        assert.equal(controls["digest-b"].textContent, hash("campus-demO"));
        select("storage", "argon"); await click("reset-exposed");
        controls["sample-b"].value = "edited-after-calculation"; h.run("apply()");
        assert.equal(h.clues.length, 0, "edited examples must be recalculated");
        await click("calculate-demo");
    } else if (key === "203_0") {
        await click("enroll-factor"); assert.equal(h.run("workspace.state.code"), undefined);
        select("factor", "app"); await click("enroll-factor");
        controls["factor-proof"].value = "incorrect"; h.run("apply()");
        assert.equal(h.clues.length, 0, "MFA proof must verify the enrolled factor");
        controls["factor-proof"].value = h.run("workspace.state.code");
    } else if (key === "203_1") {
        await click("deny-prompts"); await click("review-logins"); await click("report-prompts");
    } else if (key === "203_2") {
        await click("enroll-passkey"); select("backup-location", "private");
    } else if (key === "204_0") {
        select("recovery-address", "protected"); select("backup-location", "private"); await click("rotate-codes");
    } else if (key === "204_1") {
        await click("revoke-library"); select("shared-device", "signout");
        assert.equal(controls["personal-session"].textContent, "Personal phone · trusted · active");
        assert.ok(controls["library-session"].textContent.includes("revoked"));
    } else if (key === "204_2") {
        for (const id of ["reset-credential", "revoke-attacker", "restore-recovery", "remove-forwarding", "report-breach"]) await click(id);
    } else if (key === "205_1") {
        await click("replace-incident-password"); select("incident-factor", "passkey"); await click("enroll-incident-factor");
    } else if (key === "205_2") {
        for (const id of ["revoke-incident-session", "remove-incident-rule", "rotate-incident-codes", "file-incident"]) await click(id);
        select("incident-recovery", "owner");
    } else throw new Error("Unexpected workspace " + key);
}
(async () => {
    const typography = harness("201_0");
    typography.run('setLessonText(byId("guide"), "Use **unique credentials** and <script>literal text</script>.")');
    assert.equal(typography.elements.guide.children[1].tag, "strong");
    assert.equal(typography.elements.guide.children[1].textContent, "unique credentials");
    assert.equal(typography.elements.guide.children[2].textContent, " and <script>literal text</script>.");
    const sessions = new Map();
    const caseIds = new Map();
    const capstoneSession = storage();
    sessions.set(205, capstoneSession);
    let incidentId;
    for (const [key, item] of Object.entries(cases)) {
        if (!sessions.has(item.id)) sessions.set(item.id, storage());
        const h = harness(key, sessions.get(item.id)); await ready(h);
        const owner = h.all.find(el => el.id === "ask-owner");
        const reply = h.all.find(el => el.id === "owner-reply");
        assert.ok(owner && reply, "every task must include its fictional person's conversation");
        assert.equal(reply.hidden, true); owner.listeners.click(); assert.equal(reply.hidden, false);
        assert.ok(reply.textContent.length > 30); owner.listeners.click(); assert.equal(reply.hidden, true);
        if (key === "201_1") assert.equal(h.run("workspace.state.portal"),
            h.run("UnitTwoCaseFlow.read(201).outcomes[0].state.password"), "the repaired portal credential must carry into Task 2");
        const runId = h.run("UnitTwoCaseFlow.read(item.id).id");
        if (item.index === 0) caseIds.set(item.id, runId);
        else assert.equal(runId, caseIds.get(item.id), "all tasks must retain the same case identity");
        assert.equal(h.elements.evidence.textContent, item.evidence);
        const page = source("case_" + key + ".html");
        assert.ok(page.includes("people.js"), "every task must load the person simulation");
        assert.ok(!page.includes('id="flag-panel"'), "one proof code replaces the redundant flag step");
        assert.ok(!page.includes("drag-lesson.js") && !page.includes('id="sorting"') && !page.includes('id="choices"'));
        if (h.run("workspace !== null")) {
            h.run("apply()"); assert.equal(h.clues.length, 0, "unrepaired account cannot complete");
            await configure(h, key); h.run("apply()");
            if (item.id === 205) assert.equal(h.run("CampusIncident.read().incident"), incidentId, "same account must persist between tasks");
        } else {
            assert.equal(h.run("isHash"), true, "every non-hash task must use practical controls");
            h.run("complete()"); assert.equal(h.clues.length, 0, "hash evidence must be verified before proof is issued");
            const answer = await recovered(h);
            if (item.id === 205) incidentId = h.run("CampusIncident.read().incident");
            h.elements.recovered.value = answer;
            if (item.id === 250) {
                await h.run("verify()"); assert.equal(h.clues.length, 0);
                h.elements.start.listeners.click();
            }
            h.elements["single-guess"].value = answer; await h.run("testGuess()");
            assert.equal(h.clues.length, 0, "hash calculation must not reveal or grant correctness");
            assert.equal(h.elements.log.children.length, 1);
            h.elements.recovered.value = "definitely-wrong"; await h.run("verify()");
            assert.equal(h.clues.length, 0);
            h.elements.strategy.value = ["dictionary", "pattern", "pin"][item.index];
            h.elements["preview-list"].listeners.click();
            assert.equal(h.elements.strategy.value, "custom");
            await h.run("runSearch()");
            const rows = h.elements.log.children;
            assert.equal(rows.length, h.run("HashExercise.candidates(record.mode, record.year).length"));
            assert.ok(rows.some(row => row.children[1].textContent === h.run("record.hash")));
            assert.ok(rows.every(row => row.children.length === 2 && row.children[1].textContent.length === 64 && row.className === ""));
            // Every row is selectable, with no styling that identifies a matching digest.
            rows[0].children[0].children[0].listeners.click();
            assert.equal(h.elements.recovered.value, rows[0].children[0].children[0].textContent);
            h.elements.recovered.value = answer; await h.run("verify()");
        }
        assertVerified(h);
        assert.deepEqual(h.clues, ["password_" + item.id + "_" + item.index]);
        assert.equal(h.run("UnitTwoCaseFlow.read(item.id).completed.length"), item.index + 1);
        if (key === "201_0") assert.ok(h.run("UnitTwoCaseFlow.read(201).outcomes[0].state.password.length >= 20"));
        if (item.index === 2) assert.ok(h.elements.debrief.children.some(el => el.textContent ===
            (item.id === 205 ? "Incident closure report" : "Your connected case results")));
        assert.equal(h.elements.code.textContent, "CASE-" + item.id + "-" + (item.index + 1));
        const recap = h.run("UnitTwoRecaps[document.body.dataset.case]");
        assert.equal(recap.length, 3);
        for (const text of recap) assert.ok(h.elements.debrief.children.some(el => el.textContent === text));
        for (const heading of ["What happened", "Why it matters", "What to do in real life"]) {
            assert.ok(h.elements.debrief.children.some(el => el.textContent === heading));
        }
        h.run("complete()"); assert.equal(h.clues.length, 1);
    }
    assert.ok(JSON.parse(capstoneSession.getItem("cyberity_unit2_incident_v1")).contained);
    for (const id of [201, 202, 203, 204, 205, 250]) {
        for (const index of [1, 2]) {
            const disconnected = harness(id + "_" + index); await ready(disconnected); 
            assert.ok(disconnected.run("UnitTwoCaseFlow.prerequisite(item.id,item.index)"));
            disconnected.run("complete()");
            assert.equal(disconnected.clues.length, 0, "later tasks cannot finish without this case's earlier results");
        }
    }
    // Later stages cannot skip investigation, even if all local controls are configured.
    for (const key of ["205_1", "205_2"]) {
        const h = harness(key); await configure(h, key); h.run("apply()"); assert.equal(h.clues.length, 0);
    }
    for (const key of ["250_0", "250_1", "250_2"]) {
        const h = harness(key); await ready(h);
        for (let prior = 0; prior < Number(key.split("_")[1]); prior++) h.run("UnitTwoCaseFlow.finish(250," + prior + ",'Previously verified')");
        
        const old = h.run("record.hash"), oldAnswer = await recovered(h);
        h.elements.start.listeners.click(); h.advance(120001);
        h.elements.recovered.value = oldAnswer; await h.run("verify()"); h.run("tick()");
        assert.equal(h.clues.length, 0);
        await h.run("newAttempt()");

        
        assert.notEqual(h.run("record.hash"), old);
        assert.notEqual(await recovered(h), oldAnswer);
        h.elements.start.listeners.click(); h.elements.recovered.value = await recovered(h); await h.run("verify()");
        assertVerified(h);
        assert.equal(h.clues.length, 1);
    }
    const h = harness("205_0"); await ready(h);
    h.elements.strategy.value = "custom"; h.elements["candidate-list"].value = "nonsense";
    await h.run("runSearch()"); assert.equal(h.clues.length, 0);
    h.elements["candidate-list"].value = Array(101).fill(0).map((_, n) => "guess" + n).join("\n");
    await h.run("runSearch()"); assert.equal(h.elements.log.children.length, 1, "oversized lists must be rejected");
    h.elements.strategy.value = "pin";
    const pending = h.run("runSearch()"); await h.run("newAttempt()"); await pending;
    assert.equal(h.elements.log.children.length, 0, "cancelled search must not alter a fresh attempt");
    assert.equal(await h.run('HashExercise.digest("abc")'), hash("abc"));
    const fallback = {crypto: webcrypto, TextEncoder, Uint8Array, Uint32Array, Date};
    vm.createContext(fallback); vm.runInContext(engineSource, fallback);
    assert.equal(await vm.runInContext('HashExercise.digest("abc")', fallback), hash("abc"));
    const demoRace = harness("202_2"); demoRace.run("UnitTwoCaseFlow.finish(202,0,'Verified'); UnitTwoCaseFlow.finish(202,1,'Verified')");  await configure(demoRace, "202_2");
    demoRace.run("let releaseDigest; const digestGate = new Promise(resolve => releaseDigest = resolve); const digestBefore = HashExercise.digest; HashExercise.digest = async value => {await digestGate; return digestBefore(value);}");
    // The in-progress calculation must invalidate the prior demonstration.
    const calculating = demoRace.run("workspace.controls['calculate-demo'].listeners.click()");
    assert.ok(demoRace.run("workspace.validate()"));
    demoRace.run("apply()"); assert.equal(demoRace.clues.length, 0);
    demoRace.run("releaseDigest()"); await calculating;
    assert.equal(demoRace.run("workspace.validate()"), "");
    const gate = harness("205_0"); await ready(gate);
    gate.elements.recovered.value = await recovered(gate); await gate.run("verify()");
    assertVerified(gate);
    assert.equal(gate.clues.length, 1, "the final challenge evaluates incident work without mandatory card practice");
    const fresh = harness("201_0"), controls = fresh.run("workspace.controls");
    assert.equal(controls["rsa-n"], undefined, "password lesson must not require RSA");
    assert.equal(controls["hash-password"], undefined, "hashing belongs in the attack lesson");
    fresh.run("complete()"); assert.equal(fresh.clues.length, 0, "unrepaired settings cannot grant proof");
    await configure(fresh, "201_0"); fresh.run("apply()"); assertVerified(fresh);
    const conversation = harness("201_0");
    const ask = conversation.all.find(el => el.id === "ask-owner");
    assert.ok(ask); ask.listeners.click();
    const reply = conversation.all.find(el => el.id === "owner-reply");
    assert.equal(reply.hidden, false); assert.ok(reply.textContent.includes("shopping"));
    console.log("PASS: all 18 scenarios, six connected cases, prerequisite gates, carried account state, practical evidence and account repair, incident closure reports, recaps, login investigations, and hash workflows.");
})().catch(error => {console.error(error); process.exitCode = 1;});
