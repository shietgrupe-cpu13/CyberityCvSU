package com.cyberity.cvsu

// Level 205 content. Uses the shared LabModel / LabScreen engine.
// Simulation pages live in assets/simulations/UNIT 02/password_security_challenge/.
internal val passwordSecurityChallengeModule = PasswordModule("Password Security Challenge", "Investigate and secure one registrar account through three connected tasks.", listOf(
        PasswordExercise("Recover the registrar credential", "The registrar.training@campus.example mailbox has an unfamiliar desktop session and a forwarding rule to an outside address. Its recovered database record contains a salted hash of a common-word password. Investigate that same account through all three tasks.", "Use dictionary candidates or individual guesses to recover the matching password from this account's salted hash. Compare the full digest yourself. This is the investigation step; a recovered weak password still needs replacement, MFA, and containment.", "The weak registrar credential is confirmed. Continue to replace it and enrol sign-in protection for this same account."),
        PasswordExercise("Secure the registrar sign-in", "Continue the same registrar incident. The recovered password is still in use and the account has no MFA. Replace the credential and enrol another sign-in protection.", "Password recovery proves the weakness but does not fix it. Generate a unique long replacement for this account and enrol a passkey or authenticator. A new password alone does not remove an attacker's existing sessions or recovery methods.", "The registrar credential and sign-in protection are repaired. Continue to remove the attacker session and mailbox persistence."),
        PasswordExercise("Contain the registrar incident", "Finish the same registrar incident: an unfamiliar desktop session remains active, attacker forwarding is enabled, and recovery points to an unknown mailbox. Backup codes may have been exposed.", "Revoke attacker sessions, remove their forwarding and recovery changes, replace exposed backup codes, and report the incident. Keep the owner's trusted session. Review other accounts where the weak credential was reused.", "The registrar account is secured: credential replaced, MFA enrolled, attacker session revoked, forwarding removed, recovery restored, backup codes rotated, and incident reported.")
    ))

val passwordSecurityChallengeClueLabels: Map<String, String>
    get() = passwordSecurityClueLabels(205)

fun passwordSecurityChallengeLab(): LabDefinition = passwordSecurityLab(205)
