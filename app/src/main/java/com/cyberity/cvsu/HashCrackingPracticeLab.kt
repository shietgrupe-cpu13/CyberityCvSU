package com.cyberity.cvsu

// Level 250 content. Uses the shared LabModel / LabScreen engine.
// Simulation pages live in assets/simulations/UNIT 02/hash_cracking_practice/.
internal val hashCrackingPracticeModule = PasswordModule("Hash Cracking Practice", "Practise dictionary, pattern, and brute-force attacks against freshly generated fictional hashes.", listOf(
        PasswordExercise("Dictionary lab", "A fictional account has a salted SHA-256 hash. Its password is one entry in the supplied common-password word list.", "A hash is not decrypted. Hash each candidate with the displayed salt, compare the digest with the target, and recover the matching candidate. This is offline dictionary guessing: login throttling cannot stop work against a copied hash.", "The dictionary matched a weak credential. Replace it with a unique generated password and use MFA."),
        PasswordExercise("Pattern lab", "A fictional campus password uses one supplied campus word, a year near the current year, and ! or @. The salt and target change on every attempt.", "Use the campus-pattern generator rather than unrelated words. The current year helps generate likely guesses, but the salt and hash must be included exactly. A salt prevents reuse of precomputed digests; it does not rescue a predictable password.", "The campus pattern was recovered. Predictable dates and suffixes remain guessable even with a salt."),
        PasswordExercise("Brute-force lab", "A fictional training PIN contains exactly two digits, from 00 through 99. Its salted target hash is newly generated for this attempt.", "Brute force enumerates every value in a defined search space. This deliberately tiny PIN has only 100 candidates. Longer unpredictable secrets have much larger spaces. Raw SHA-256 is fast and used here to demonstrate weak storage; production password storage needs an appropriate slow password hashing scheme.", "All 100 two-digit combinations fit a tiny search space. Longer unique passwords and slow salted storage raise guessing cost.")
    ))

val hashCrackingPracticeClueLabels: Map<String, String>
    get() = passwordSecurityClueLabels(250)

fun hashCrackingPracticeLab(): LabDefinition = passwordSecurityLab(250)
