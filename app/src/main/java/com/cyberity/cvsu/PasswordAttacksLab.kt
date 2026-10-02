package com.cyberity.cvsu

// Level 202 content. Uses the shared LabModel / LabScreen engine.
// Simulation pages live in assets/simulations/UNIT 02/password_attacks/.
internal val passwordAttacksModule = PasswordModule("Password Attacks", "Read three login records and choose the right defence.", listOf(
        PasswordExercise("Dictionary guessing", "One username receives Campus2026!, Password1!, Welcome1!, and similar common guesses. All fail.", "Dictionary attacks try common words and predictable variations. Brute force explores combinations systematically. Both can guess online, where rate limits help, or offline against stolen hashes, where a website login limit no longer applies.", "The guess list is dictionary-based, and online throttling reduces repeated attempts."),
        PasswordExercise("Leaked pairs", "A login report shows thousands of different emails. Each gets one password from a leaked shopping-site credential list. Several succeed.", "Credential stuffing tests stolen username/password pairs on other services. A password can be long and still fail this way if reused. Unique credentials break that link; MFA adds another barrier. Investigate successful logins as possible compromises.", "Stolen pairs explain the pattern; account-specific passwords limit the damage."),
        PasswordExercise("Stolen database", "The attacker has a copy of the password hashes and is testing guesses on their own computer. The portal blocks repeated login attempts.", "An offline attacker does not need the login page. Unique salts stop identical passwords producing identical stored values and defeat reusable precomputed tables. A suitable slow password hashing scheme raises the cost per guess. Salts do not make weak passwords strong.", "Offline guessing needs strong storage and breach response, alongside long unique passwords.")
    ))

val passwordAttacksClueLabels: Map<String, String>
    get() = passwordSecurityClueLabels(202)

fun passwordAttacksLab(): LabDefinition = passwordSecurityLab(202)
