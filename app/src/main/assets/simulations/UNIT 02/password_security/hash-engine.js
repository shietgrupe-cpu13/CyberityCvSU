"use strict";
// Actual SHA-256 comparison, not a list of hard-coded correct answers.
// The clock and random nonce vary the fictional target; the hash function does not change.
const HashExercise = (() => {
    const dictionary = ["password", "welcome", "dragon", "sunshine", "football", "letmein", "monkey", "qwerty", "student", "admin", "iloveyou", "summer"];
    let sequence = 0;
    const previousPasswords = {};
    async function digest(value) {
        if (typeof AndroidLab !== "undefined" && typeof AndroidLab.hashCandidate === "function") {
            const hash = AndroidLab.hashCandidate(value);
            if (!/^[a-f0-9]{64}$/.test(hash)) throw new Error("Hash service unavailable");
            return hash;
        }
        const bytes = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(value));
        return Array.from(new Uint8Array(bytes), n => n.toString(16).padStart(2, "0")).join("");
    }
    function candidates(mode, year) {
        if (mode === "dictionary") return dictionary.slice();
        if (mode === "pattern") return ["CvSU", "Campus", "Student", "Welcome"].flatMap(word =>
            [year - 1, year, year + 1].flatMap(y => ["!", "@"].map(suffix => word + y + suffix)));
        if (mode === "pin") return Array.from({length: 100}, (_, n) => n.toString().padStart(2, "0"));
        return [];
    }
    async function create(index, now = Date.now()) {
        const nonce = new Uint32Array(1);
        crypto.getRandomValues(nonce);
        const serial = ++sequence;
        const year = new Date(now).getFullYear();
        const mode = ["dictionary", "pattern", "pin"][index];
        const pool = candidates(mode, year);
        const storageKey = "cyberity_training_previous_" + mode;
        let previous = previousPasswords[mode];
        try { previous = sessionStorage.getItem(storageKey) || previous; } catch (_) {}
        let position = (Math.floor(now) + nonce[0] + serial) % pool.length;
        if (pool[position] === previous) position = (position + 1) % pool.length;
        const password = pool[position];
        previousPasswords[mode] = password;
        try { sessionStorage.setItem(storageKey, password); } catch (_) {}
        // Include a per-record salt even in the dictionary exercise so each record has a fresh hash.
        const salt = now.toString(36) + "-" + nonce[0].toString(36) + "-" + serial;
        const hash = await digest(salt + ":" + password);
        return {mode, year, salt, hash, pool, created: now, formula: "SHA-256(salt + ':' + candidate)"};
    }
    async function compare(record, candidate) {
        const hash = await digest(record.salt + ":" + candidate);
        return {candidate, hash, match: hash === record.hash};
    }
    return {digest, candidates, create, compare};
})();
