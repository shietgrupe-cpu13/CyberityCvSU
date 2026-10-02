package com.cyberity.cvsu

// Level 201 content. Uses the shared LabModel / LabScreen engine.
// Simulation pages live in assets/simulations/UNIT 02/strong_passwords/.
internal val strongPasswordsModule = PasswordModule("Strong Passwords", "Build a unique credential, then keep it safe.", listOf(
        PasswordExercise("Predictable substitutions", "A campus account uses CvSU2026! (9 characters). Its owner says the capital letters and symbol make it safe.", "A campus name, year, and familiar symbol remain predictable. **Generate a unique 20-character replacement** for this fictional portal, check that it is not the old credential, and keep it separate from shopping. The 20-character target is for this exercise, not a guarantee of safety. Never enter a real password in the lab.", "The predictable portal credential is replaced with a generated, account-specific secret. Next, remove the old reused value from the linked shopping account."),
        PasswordExercise("Reuse in the vault", "Continue the same portal audit. Task 1 replaced the predictable portal credential. The linked shopping account still uses the old exposed credential; replace that remaining reused value and protect both account credentials in the vault.", "Length cannot protect a reused credential once another service leaks it. A password manager helps maintain a different generated password for every account. Secure the vault itself with a strong master password and MFA, and use its official app.", "A breach at one service now has no reusable password for the other."),
        PasswordExercise("Misleading policy", "The portal forces a password change every month. Students keep rotating CampusJan! into CampusFeb! and CampusMar!.", "Routine expiry encourages predictable changes. Change a password when it is compromised or there is evidence of exposure. A better policy allows long passwords and paste/autofill, blocks common or breached values, and limits online guesses.", "The policy addresses guessability and compromise without training predictable rotations.")
    ))

val strongPasswordsClueLabels: Map<String, String>
    get() = passwordSecurityClueLabels(201)

fun strongPasswordsLab(): LabDefinition = passwordSecurityLab(201)
