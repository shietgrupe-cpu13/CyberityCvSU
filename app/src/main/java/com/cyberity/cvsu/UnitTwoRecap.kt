package com.cyberity.cvsu

/** Short takeaways also available after returning to the native task panel. */
private val unitTwoRecaps = mapOf(
    "201_0" to listOf("You calculated the toy RSA modulus and totient, then replaced the predictable campus password.", "Familiar names and dates remain guessable even with a symbol.", "Use a password manager to create a long, unique password for each account."),
    "201_1" to listOf("You replaced the reused passwords on both exposed accounts.", "A leaked password can open every account where it was reused.", "Replace exposed passwords everywhere they were reused and store different replacements privately."),
    "201_2" to listOf("You repaired a policy that encouraged predictable monthly changes.", "Rules should support unique passwords rather than push people toward familiar patterns.", "Allow long passwords and manager autofill; change credentials when exposure is suspected."),
    "202_0" to listOf("You identified common-word guessing against one account.", "Attackers often try likely passwords before exploring every combination.", "Use a unique generated password and enable MFA; services should limit repeated login attempts."),
    "202_1" to listOf("You identified stolen login pairs being tried across accounts.", "Credential reuse lets one breach spread to other services.", "Avoid reuse, enable MFA, and review successful unfamiliar logins."),
    "202_2" to listOf("You compared two hash outputs and selected slow salted storage for exposed credentials.", "An attacker with copied hashes can guess without using the login page.", "Use unique passwords; service builders should use suitable slow salted password hashing and respond to breaches."),
    "203_0" to listOf("You enrolled a possession factor alongside a password.", "Two remembered secrets are still the same factor category.", "Add supported MFA and keep recovery methods protected."),
    "203_1" to listOf("You denied unexpected prompts, checked activity, and reported the attempt.", "Approving a prompt you did not initiate can grant attacker access.", "Deny unexpected requests and review account security through the official service."),
    "203_2" to listOf("You registered a service-bound passkey and protected recovery codes.", "Typed one-time codes can be relayed by a fake login page.", "Use supported passkeys or security keys and keep a protected recovery route."),
    "204_0" to listOf("You restored owner-controlled recovery and replaced exposed backup codes.", "Recovery can provide another route into an account.", "Protect the recovery mailbox, keep it current, and store backup codes privately."),
    "204_1" to listOf("You revoked the abandoned library session while keeping trusted access.", "Closing a browser window does not necessarily end a signed-in session.", "Sign out on shared devices and revoke sessions you no longer trust."),
    "204_2" to listOf("You removed compromised credentials, attacker sessions, and account persistence.", "A password reset alone may leave other attacker access intact.", "Use a trusted device to review sessions, recovery, forwarding, connected apps, and reused credentials."),
    "205_0" to listOf("You recovered the weak fictional registrar password by comparing its full salted hash.", "Recovering a password reveals weakness but does not secure the account.", "Treat confirmed exposure as an incident; replace the credential and inspect other access paths."),
    "205_1" to listOf("You replaced the registrar password and enrolled extra sign-in protection.", "A stronger sign-in does not remove sessions already held by an attacker.", "Use a unique credential with supported MFA, then continue reviewing active sessions and recovery."),
    "205_2" to listOf("You removed the attacker session and forwarding, restored recovery, and rotated backup codes.", "Containment must remove persistent access as well as fix sign-in.", "Review all account access paths and report the incident through the official support route."),
    "250_0" to listOf("You recovered a fictional common-word password using dictionary candidates.", "A public salt changes the hash but does not make a weak password unpredictable.", "Choose unique generated credentials; only test accounts and records you are authorized to assess."),
    "250_1" to listOf("You recovered a password built from a campus word, year, and suffix.", "A familiar format narrows the likely guesses.", "Avoid predictable personal or campus patterns and use a password manager."),
    "250_2" to listOf("You tested the two-digit PIN space while preserving leading zeroes.", "Every character changes the hash input; this tiny classroom space has only 100 possibilities.", "Use longer unpredictable credentials. Classroom timers do not estimate real cracking speed.")
)

fun unitTwoRecap(levelId: Int, taskIndex: Int): String {
    val recap = unitTwoRecaps.getValue("${levelId}_${taskIndex}")
    return "What happened\n${recap[0]}\n\nWhy it matters\n${recap[1]}\n\nWhat to do in real life\n${recap[2]}"
}
