"use strict";
const item = passwordCases[document.body.dataset.case];
const learning = UnitTwoLearning[document.body.dataset.case];
const isHash = (item.id === 205 && item.index === 0) || item.id === 250;
const timed = item.id === 250;
const byId = id => document.getElementById(id);
const isFinalChallenge = item.id === 205;
if (item.index === 0) UnitTwoCaseFlow.begin(item.id);
let resolved = false, record = null, deadline = null, timer = null;
let generation = 0, working = false, tested = 0;
let roundExpired = false;
let workspace = null;
let hashAttemptCount = 0;
let pendingProof = null;

const pause = () => new Promise(resolve => setTimeout(resolve, 35));
function notify() {
    try { AndroidLab.notifyClueFound("password_" + item.id + "_" + item.index); } catch (_) {}
}
function feedback(message) { byId("feedback").textContent = message; }
function complete() {
    if (resolved || pendingProof) return;
    const prerequisite = UnitTwoCaseFlow.prerequisite(item.id,item.index);
    if (prerequisite) {feedback(prerequisite); return;}
    const workspaceResult = workspace ? {...workspace.state, settings: Object.fromEntries(
        Object.entries(workspace.controls).filter(([, control]) => ["INPUT", "SELECT", "input", "select"].includes(control.tagName || control.tag))
            .map(([id, control]) => [id, control.type === "checkbox" ? control.checked : control.value]))} : null;
    const random = crypto.getRandomValues(new Uint32Array(2));
    pendingProof = {flag: "CYBERITY{" + item.id + "_" + (item.index + 1) + "_" + Array.from(random,n => n.toString(16).padStart(8,"0")).join("") + "}", result: workspaceResult || {strategy: record?.mode, hash: record?.hash}};
    clearInterval(timer); deadline = null; generation++; working = false;
    if (isHash) byId("timer").textContent = "Investigation complete · verify the captured flag";
    document.querySelectorAll("button, input, select, textarea").forEach(control => control.disabled = true);
    byId("flag-panel").hidden = false;
    byId("earned-flag").textContent = pendingProof.flag;
    byId("flag-input").disabled = false; byId("submit-flag").disabled = false;
    byId("phase").textContent = "Evidence verified · submit your flag";
    feedback("Practical work verified. Copy the earned flag and paste it below to unlock your CASE code.");
    byId("flag-panel").scrollIntoView({behavior: "smooth", block: "nearest"});
}
function submitFlag() {
    if (resolved) return;
    if (!pendingProof) {feedback("Complete the practical task to earn a flag first."); return;}
    if (byId("flag-input").value.trim() !== pendingProof.flag) {feedback("That flag does not match this task and attempt. Paste the complete earned flag."); return;}
    if (!UnitTwoCaseFlow.finish(item.id,item.index,item.feedback,pendingProof.result)) {
        feedback(UnitTwoCaseFlow.prerequisite(item.id,item.index)); return;
    }
    if (workspace) workspace.commit();
    if (item.id === 205 && item.index === 0) CampusIncident.update({recovered: true});
    resolved = true;
    clearInterval(timer);
    generation++;
    working = false;
    byId("result").hidden = false;
    byId("code").textContent = "CASE-" + item.id + "-" + (item.index + 1);
    byId("phase").textContent = "3 / 3 · Resolved";
    feedback(item.feedback);
    const explain = (tag, text) => {
        const element = document.createElement(tag); element.textContent = text; byId("debrief").appendChild(element);
    };
    explain("h3", learning.topic[4]);
    explain("p", learning.topic[5]);

    explain("h3", "Your case outcome");
    explain("p", item.feedback);
    if (item.index === 2) {
        explain("h3", isFinalChallenge ? "Incident closure report" : "Your connected case results");
        const caseRun = UnitTwoCaseFlow.read(item.id);
        [0, 1, 2].forEach(index => explain("p", "Task " + (index + 1) + ": " + caseRun.outcomes[index].summary));
    }
    if (item.index === 2) {
        explain("h3", "Carry this into the next lesson");
        explain("p", "Use a unique unpredictable credential for each account, add supported sign-in protection, and protect recovery. If an account is compromised, inspect sessions and account changes rather than relying on a password change alone. When analysing a hash, reproduce its input exactly and compare the whole digest; the training timer does not predict real cracking speed.");
    }
    explain("h3", "Quick recap");
    const recap = UnitTwoRecaps[item.id + "_" + item.index];
    ["What happened", "Why it matters", "What to do in real life"].forEach((heading, index) => {
        explain("h4", heading);
        explain("p", recap[index]);
    });
    document.querySelectorAll("button, input, select, textarea").forEach(control => control.disabled = true);
    notify();
    byId("result").scrollIntoView({behavior: "smooth", block: "nearest"});
}
function apply() {
    const prerequisite = UnitTwoCaseFlow.prerequisite(item.id, item.index);
    if (prerequisite) { feedback(prerequisite); return; }
    if (workspace && !resolved && !pendingProof) {
        const problem = workspace.validate();
        if (problem) { feedback(problem); return; }
        complete();
        return;
    }

}
function expired() {
    return timed && (!deadline || Date.now() >= deadline);
}
function tick() {
    if (!deadline || resolved) return;
    const seconds = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
    byId("timer").textContent = seconds + "s remaining";
    if (!seconds) {
        generation++;
        working = false;
        clearInterval(timer);
        deadline = null;
        roundExpired = true;
        setControls();
        feedback("Round expired. Choose New attempt for a fresh hash, then start when ready.");
    }
}
function setControls() {
    byId("run").disabled = !record || resolved || working || expired();
    byId("verify").disabled = !record || resolved || working || expired();
    byId("strategy").disabled = working || resolved;
    byId("start").disabled = !record || resolved || Boolean(deadline) || roundExpired;
    byId("new").disabled = resolved;
    byId("test-guess").disabled = !record || resolved || working || expired();
    byId("preview-list").disabled = !record || resolved || working;
    byId("candidate-list").disabled = working || resolved;
}
function row(result) {
    const tr = document.createElement("tr");
    const candidateCell = document.createElement("td");
    const pick = document.createElement("button");
    pick.className = "candidate-pick"; pick.textContent = result.candidate;
    pick.setAttribute("aria-label", "Select candidate " + result.candidate);
    pick.addEventListener("click", () => {
        if (resolved) return;
        byId("recovered").value = result.candidate;
        feedback("Candidate selected. Compare its digest with the target before verifying.");
    });
    candidateCell.appendChild(pick); tr.appendChild(candidateCell);
    const hashCell = document.createElement("td"); hashCell.textContent = result.hash; tr.appendChild(hashCell);
    byId("log").appendChild(tr);
}
async function newAttempt() {
    if (resolved || pendingProof) return;
    hashAttemptCount++;
    const token = ++generation;
    record = null; working = false; deadline = null; tested = 0;
    roundExpired = false;
    if (item.id === 205) CampusIncident.update({recovered: false, secured: false, contained: false});
    clearInterval(timer);
    byId("log").replaceChildren();
    byId("recovered").value = "";
    byId("single-guess").value = "";
    byId("candidate-list").value = "";
    byId("count").textContent = "0 candidates tested";
    byId("target").textContent = "Generating a fresh record…";
    byId("timer").textContent = timed ? "Two minutes · starts when you tap Start round" : "Untimed investigation";
    byId("phase").textContent = "1 / 3 · Inspect the lesson and record";
    byId("strategy").value = "dictionary";
    document.querySelectorAll(".repair-check").forEach(control => control.checked = false);
    setControls();
    try {
        const fresh = await HashExercise.create(item.index);
        if (token !== generation) return;
        record = fresh;
        if (item.id === 205) CampusIncident.begin(record);
        byId("target").textContent = record.hash;
        byId("salt").textContent = record.salt;
        byId("formula").textContent = record.formula;
        byId("space").textContent = item.index === 0 ? "Common-word dictionary: 12 candidates" :
            item.index === 1 ? "Campus pattern: 4 words × 3 years × 2 suffixes = 24 candidates" : "Two-digit PIN: 00–99 = 100 candidates";
        byId("year").textContent = String(record.year);
        feedback("New fictional record ready. The salt is public; include it in every hash comparison.");
        setControls();
    } catch (_) {
        feedback("The offline hash service could not start. Reopen the simulation or choose New attempt.");
        byId("target").textContent = "Hash service unavailable";
    }
}
async function runSearch() {
    if (!record || working || resolved || pendingProof || expired()) return;
    const token = generation, target = record;
    const candidates = byId("strategy").value === "custom" ?
        Array.from(new Set(byId("candidate-list").value.split(/\r?\n/).filter(value => value.length > 0))) :
        HashExercise.candidates(byId("strategy").value, target.year);
    if (!candidates.length || candidates.length > 100 || candidates.some(value => value.length > 128)) {
        feedback("Supply 1–100 classroom candidates, one per line, with no more than 128 characters each."); return;
    }
    working = true;
    byId("log").replaceChildren();
    tested = 0;
    byId("phase").textContent = "2 / 3 · Hash candidates and compare";
    setControls();
    feedback("Comparing guesses with the stored hash…");
    try {
        for (const candidate of candidates) {
            if (token !== generation || resolved || expired()) { tick(); return; }
            const result = await HashExercise.compare(target, candidate);
            if (token !== generation || expired()) { tick(); return; }
            tested++;
            row(result);
            byId("count").textContent = tested + " candidates tested";
            await pause();
        }
        feedback("Candidate set calculated. Compare each full digest with the target yourself, then enter the candidate you think matches. If none match, choose another strategy.");
    } catch (_) { feedback("Hash comparison failed. Try again or generate a new attempt."); }
    finally {
        if (token === generation) { working = false; setControls(); }
    }
}
async function verify() {
    if (!record || working || resolved || expired()) return;
    const prerequisite = UnitTwoCaseFlow.prerequisite(item.id, item.index);
    if (prerequisite) { feedback(prerequisite); return; }
    const candidate = byId("recovered").value;
    if (!candidate || candidate.length > 128) { feedback("Enter the recovered classroom password (up to 128 characters)."); return; }
    const token = generation;
    working = true; setControls();
    try {
        const result = await HashExercise.compare(record, candidate);
        if (token !== generation || expired()) { tick(); return; }
        row(result);
        if (!result.match) { feedback("That candidate produces a different hash. Compare the full candidate digests with the target, including the displayed salt."); return; }
        complete();
    } catch (_) { feedback("Could not verify this candidate. Try again."); }
    finally { if (token === generation) { working = false; setControls(); } }
}
async function testGuess() {
    if (!record || working || resolved || expired()) return;
    const candidate = byId("single-guess").value;
    if (!candidate || candidate.length > 128) { feedback("Enter a fictional guess of up to 128 characters."); return; }
    const token = generation, target = record;
    working = true; setControls();
    try {
        const result = await HashExercise.compare(target, candidate);
        if (token !== generation || expired()) { tick(); return; }
        row(result); tested++;
        byId("count").textContent = tested + " candidates tested";
        feedback("Your guess has been hashed with the public salt. Compare the full digest with the target yourself.");
    } catch (_) { feedback("Could not calculate the guess. Try again."); }
    finally { if (token === generation) { working = false; setControls(); } }
}
byId("unit").textContent = "UNIT 2 / " + item.title + " / CASE " + (item.index + 1) + " OF 3";
byId("title").textContent = item.caseTitle;
byId("evidence").textContent = item.evidence;
byId("guide").textContent = item.guide;
byId("reading-title").textContent = learning.topic[0];
byId("reading-one").textContent = learning.topic[1];
byId("reading-two").textContent = learning.topic[2];

const caseContext = UnitTwoCaseFlow.describe(item.id, item.index);
const journal = document.createElement("section"); journal.className = "card case-journal";
function journalText(tag, value) {
    const el = document.createElement(tag); el.textContent = value; journal.appendChild(el); return el;
}
journalText("h2", caseContext.title);
journalText("p", "Case " + (caseContext.run?.id || "not started") + " · " + caseContext.account);
journalText("p", "Task " + (item.index + 1) + " of 3: " + caseContext.stage);
if (caseContext.previous) journalText("p", "Carried forward: " + caseContext.previous.summary);
if (caseContext.previous?.state.factor) journalText("p", "Enrolled sign-in protection carried forward: " + caseContext.previous.state.factor);
if (caseContext.previous?.state.hash) journalText("p", "Verified audit digest from the previous record: " + caseContext.previous.state.hash);
if (caseContext.previous?.state.settings?.["recovery-address"]) journalText("p", "Recovery mailbox carried forward: " + caseContext.previous.state.settings["recovery-address"]);
const blocked = UnitTwoCaseFlow.prerequisite(item.id, item.index);
if (blocked) journalText("p", blocked);
if (item.index === 0) journalText("p", "All three tasks continue this case. Verified findings and repairs carry into the next task.");
byId("case-journal").appendChild(journal);
byId("phase").textContent = "1 / 3 · Learn and inspect";
byId("decision-panel").hidden = isHash;
byId("hash-panel").hidden = !isHash;
if (isHash) {
    byId("start").hidden = !timed;
    byId("repair-panel").hidden = true;
    byId("test-guess").addEventListener("click", testGuess);
    byId("preview-list").addEventListener("click", () => {
        if (!record || working || resolved) return;
        const values = HashExercise.candidates(byId("strategy").value, record.year);
        if (!values.length) { feedback("Enter your own candidate list below, one per line."); return; }
        byId("candidate-list").value = values.join("\n");
        byId("strategy").value = "custom";
        feedback("Candidate list loaded. Edit it or calculate it as-is. All rows use the same neutral appearance.");
    });
    byId("new").addEventListener("click", newAttempt);
    byId("run").addEventListener("click", runSearch);
    byId("verify").addEventListener("click", verify);
    byId("start").addEventListener("click", () => {
        if (!record || resolved || deadline || roundExpired) return;
        deadline = Date.now() + 120000; clearInterval(timer);
        timer = setInterval(tick, 250); tick(); setControls();
    });
    newAttempt();
} else {
    workspace = AccountWorkspace.mount(byId("workspace"), item.id + "_" + item.index, feedback);
    byId("workspace").hidden = !workspace;
    if (workspace) {
        byId("apply").textContent = "Save account changes";
        byId("apply").disabled = false;
    }
    byId("apply").addEventListener("click", apply);
}
document.addEventListener("visibilitychange", tick);
window.addEventListener("pagehide", () => { generation++; clearInterval(timer); });

byId("submit-flag").addEventListener("click", submitFlag);
