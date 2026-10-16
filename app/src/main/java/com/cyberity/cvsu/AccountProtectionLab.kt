package com.cyberity.cvsu

// Level 204 content. Uses the shared LabModel / LabScreen engine.
// Simulation pages live in assets/simulations/UNIT 02/account_protection/.
internal val accountProtectionModule = PasswordModule("Account Protection", "Recover safely and close the doors an attacker left open.", listOf(
        PasswordExercise("Recovery audit", "The student portal recovery address is an abandoned mailbox. Backup codes are saved in a shared class folder.", "Recovery can bypass normal sign-in. Protect the recovery mailbox with its own unique password and MFA. Keep recovery information current, and store backup codes securely, such as in an encrypted vault or a protected offline location.", "Recovery now belongs to the account owner, and shared codes can no longer be used."),
        PasswordExercise("Shared computer", "You closed the browser at the library without signing out. Account settings still lists an active library-computer session.", "Closing a window does not necessarily end a session. Sign out on shared devices, avoid saving credentials, and revoke sessions you cannot trust from the official security page. Private browsing is not protection against a compromised device.", "Revocation removes the active session rather than relying on a closed browser window."),
        PasswordExercise("Breach response", "After repairing recovery and closing the library session on this same account, a subsequent unfamiliar login changes the recovery phone and adds an outside forwarding rule. Contain this confirmed breach.", "Use a trusted device and the official service to regain control. Change compromised passwords, revoke sessions, remove unknown recovery methods, inspect forwarding and connected apps, and contact support if locked out. Review other accounts where the credential was reused.", "The response removes persistence as well as the stolen credential.")
    ))

val accountProtectionClueLabels: Map<String, String>
    get() = passwordSecurityClueLabels(204)

fun accountProtectionLab(): LabDefinition = passwordSecurityLab(204)
