package com.cyberity.cvsu

// Level 203 content. Uses the shared LabModel / LabScreen engine.
// Simulation pages live in assets/simulations/UNIT 02/multi_factor_authentication/.
internal val multiFactorAuthenticationModule = PasswordModule("Multi-Factor Authentication", "Choose a factor and practise responding to unexpected requests.", listOf(
        PasswordExercise("Two secrets", "A setup wizard proposes a password plus the answer to a secret question. It calls this two-factor authentication.", "Factors are something you know, have, or are. Two knowledge secrets are still one factor category. A password plus an authenticator or security key adds possession. A supported passkey uses a cryptographic credential bound to the genuine service.", "A possession factor adds protection beyond another remembered secret."),
        PasswordExercise("Push fatigue", "Continue the same student account after enrolling a possession factor. Its legacy approval channel receives six requests you did not initiate. A caller says to approve one to stop the notifications.", "Repeated prompts can pressure people into authorising an attacker. Deny requests you did not initiate, open the official account security page, review sessions, and report suspicious activity. Never give a caller a one-time code or backup code.", "An unrequested prompt is an incident to investigate, not an approval to grant."),
        PasswordExercise("Phishing-resistant sign-in", "Continue the same student account after denying the suspicious prompts. The portal still allows legacy typed-code sign-in, and a fake page can relay a fresh code. Register a service-bound passkey and protect recovery.", "MFA reduces many password attacks, but typed one-time codes can still be phished. Passkeys and FIDO security keys bind authentication to the legitimate service. Keep recovery codes in a protected place and maintain a recovery method; never share codes.", "Service-bound authentication resists the fake-page relay described in this case.")
    ))

val multiFactorAuthenticationClueLabels: Map<String, String>
    get() = passwordSecurityClueLabels(203)

fun multiFactorAuthenticationLab(): LabDefinition = passwordSecurityLab(203)
