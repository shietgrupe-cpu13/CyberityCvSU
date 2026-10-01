package com.cyberity.cvsu

// Unit 2 content uses the shared lab engine. All accounts and credentials are fictional.
// Teaching reference: https://pages.nist.gov/800-63-4/sp800-63b.html
private data class PasswordExercise(
    val title: String,
    val evidence: String,
    val guide: String,
    val feedback: String
)

private data class PasswordModule(
    val title: String,
    val briefing: String,
    val exercises: List<PasswordExercise>
)

private val passwordModules = mapOf(
    201 to PasswordModule("Strong Passwords", "Build a unique credential, then keep it safe.", listOf(
        PasswordExercise("Predictable substitutions", "A campus account uses CvSU2026! (9 characters). Its owner says the capital letters and symbol make it safe.", "Length and unpredictability matter together. A campus name, year, and predictable substitutions remain guessable. Generate a long random password for each account, or use a passphrase made from randomly selected words. Never use these classroom examples as real passwords. First calculate the classroom RSA values n = p × q and φ(n) = (p − 1) × (q − 1), using the randomized primes shown in the simulation. RSA encryption is different from password hashing. Then type a fictional replacement or use the generator, calculate its salted classroom hash, and paste the complete hash into the verification field before saving and receiving the case code.", "The toy RSA calculation demonstrates encryption arithmetic; the separate long, unique password replaces the old campus-name credential."),
        PasswordExercise("Reuse in the vault", "Continue the same portal audit. Task 1 replaced the predictable portal credential. The linked shopping account still uses the old exposed credential; replace that remaining reused value and protect both account credentials in the vault.", "Length cannot protect a reused credential once another service leaks it. A password manager helps maintain a different generated password for every account. Secure the vault itself with a strong master password and MFA, and use its official app.", "A breach at one service now has no reusable password for the other."),
        PasswordExercise("Misleading policy", "The portal forces a password change every month. Students keep rotating CampusJan! into CampusFeb! and CampusMar!.", "Routine expiry encourages predictable changes. Change a password when it is compromised or there is evidence of exposure. A better policy allows long passwords and paste/autofill, blocks common or breached values, and limits online guesses.", "The policy addresses guessability and compromise without training predictable rotations.")
    )),
    202 to PasswordModule("Password Attacks", "Read three login records and choose the right defence.", listOf(
        PasswordExercise("Dictionary guessing", "One username receives Campus2026!, Password1!, Welcome1!, and similar common guesses. All fail.", "Dictionary attacks try common words and predictable variations. Brute force explores combinations systematically. Both can guess online, where rate limits help, or offline against stolen hashes, where a website login limit no longer applies.", "The guess list is dictionary-based, and online throttling reduces repeated attempts."),
        PasswordExercise("Leaked pairs", "A login report shows thousands of different emails. Each gets one password from a leaked shopping-site credential list. Several succeed.", "Credential stuffing tests stolen username/password pairs on other services. A password can be long and still fail this way if reused. Unique credentials break that link; MFA adds another barrier. Investigate successful logins as possible compromises.", "Stolen pairs explain the pattern; account-specific passwords limit the damage."),
        PasswordExercise("Stolen database", "The attacker has a copy of the password hashes and is testing guesses on their own computer. The portal blocks repeated login attempts.", "An offline attacker does not need the login page. Unique salts stop identical passwords producing identical stored values and defeat reusable precomputed tables. A suitable slow password hashing scheme raises the cost per guess. Salts do not make weak passwords strong.", "Offline guessing needs strong storage and breach response, alongside long unique passwords.")
    )),
    203 to PasswordModule("Multi-Factor Authentication", "Choose a factor and practise responding to unexpected requests.", listOf(
        PasswordExercise("Two secrets", "A setup wizard proposes a password plus the answer to a secret question. It calls this two-factor authentication.", "Factors are something you know, have, or are. Two knowledge secrets are still one factor category. A password plus an authenticator or security key adds possession. A supported passkey uses a cryptographic credential bound to the genuine service.", "A possession factor adds protection beyond another remembered secret."),
        PasswordExercise("Push fatigue", "Continue the same student account after enrolling a possession factor. Its legacy approval channel receives six requests you did not initiate. A caller says to approve one to stop the notifications.", "Repeated prompts can pressure people into authorising an attacker. Deny requests you did not initiate, open the official account security page, review sessions, and report suspicious activity. Never give a caller a one-time code or backup code.", "An unrequested prompt is an incident to investigate, not an approval to grant."),
        PasswordExercise("Phishing-resistant sign-in", "Continue the same student account after denying the suspicious prompts. The portal still allows legacy typed-code sign-in, and a fake page can relay a fresh code. Register a service-bound passkey and protect recovery.", "MFA reduces many password attacks, but typed one-time codes can still be phished. Passkeys and FIDO security keys bind authentication to the legitimate service. Keep recovery codes in a protected place and maintain a recovery method; never share codes.", "Service-bound authentication resists the fake-page relay described in this case.")
    )),
    204 to PasswordModule("Account Protection", "Recover safely and close the doors an attacker left open.", listOf(
        PasswordExercise("Recovery audit", "The student portal recovery address is an abandoned mailbox. Backup codes are saved in a shared class folder.", "Recovery can bypass normal sign-in. Protect the recovery mailbox with its own unique password and MFA. Keep recovery information current, and store backup codes securely, such as in an encrypted vault or a protected offline location.", "Recovery now belongs to the account owner, and shared codes can no longer be used."),
        PasswordExercise("Shared computer", "You closed the browser at the library without signing out. Account settings still lists an active library-computer session.", "Closing a window does not necessarily end a session. Sign out on shared devices, avoid saving credentials, and revoke sessions you cannot trust from the official security page. Private browsing is not protection against a compromised device.", "Revocation removes the active session rather than relying on a closed browser window."),
        PasswordExercise("Breach response", "After repairing recovery and closing the library session on this same account, a subsequent unfamiliar login changes the recovery phone and adds an outside forwarding rule. Contain this confirmed breach.", "Use a trusted device and the official service to regain control. Change compromised passwords, revoke sessions, remove unknown recovery methods, inspect forwarding and connected apps, and contact support if locked out. Review other accounts where the credential was reused.", "The response removes persistence as well as the stolen credential.")
    )),
    205 to PasswordModule("Password Security Challenge", "Investigate and secure one registrar account through three connected tasks.", listOf(
        PasswordExercise("Recover the registrar credential", "The registrar.training@campus.example mailbox has an unfamiliar desktop session and a forwarding rule to an outside address. Its recovered database record contains a salted hash of a common-word password. Investigate that same account through all three tasks.", "Use dictionary candidates or individual guesses to recover the matching password from this account's salted hash. Compare the full digest yourself. This is the investigation step; a recovered weak password still needs replacement, MFA, and containment.", "The weak registrar credential is confirmed. Continue to replace it and enrol sign-in protection for this same account."),
        PasswordExercise("Secure the registrar sign-in", "Continue the same registrar incident. The recovered password is still in use and the account has no MFA. Replace the credential and enrol another sign-in protection.", "Password recovery proves the weakness but does not fix it. Generate a unique long replacement for this account and enrol a passkey or authenticator. A new password alone does not remove an attacker's existing sessions or recovery methods.", "The registrar credential and sign-in protection are repaired. Continue to remove the attacker session and mailbox persistence."),
        PasswordExercise("Contain the registrar incident", "Finish the same registrar incident: an unfamiliar desktop session remains active, attacker forwarding is enabled, and recovery points to an unknown mailbox. Backup codes may have been exposed.", "Revoke attacker sessions, remove their forwarding and recovery changes, replace exposed backup codes, and report the incident. Keep the owner's trusted session. Review other accounts where the weak credential was reused.", "The registrar account is secured: credential replaced, MFA enrolled, attacker session revoked, forwarding removed, recovery restored, backup codes rotated, and incident reported.")
    )),
    250 to PasswordModule("Hash Cracking Practice", "Practise dictionary, pattern, and brute-force attacks against freshly generated fictional hashes.", listOf(
        PasswordExercise("Dictionary lab", "A fictional account has a salted SHA-256 hash. Its password is one entry in the supplied common-password word list.", "A hash is not decrypted. Hash each candidate with the displayed salt, compare the digest with the target, and recover the matching candidate. This is offline dictionary guessing: login throttling cannot stop work against a copied hash.", "The dictionary matched a weak credential. Replace it with a unique generated password and use MFA."),
        PasswordExercise("Pattern lab", "A fictional campus password uses one supplied campus word, a year near the current year, and ! or @. The salt and target change on every attempt.", "Use the campus-pattern generator rather than unrelated words. The current year helps generate likely guesses, but the salt and hash must be included exactly. A salt prevents reuse of precomputed digests; it does not rescue a predictable password.", "The campus pattern was recovered. Predictable dates and suffixes remain guessable even with a salt."),
        PasswordExercise("Brute-force lab", "A fictional training PIN contains exactly two digits, from 00 through 99. Its salted target hash is newly generated for this attempt.", "Brute force enumerates every value in a defined search space. This deliberately tiny PIN has only 100 candidates. Longer unpredictable secrets have much larger spaces. Raw SHA-256 is fast and used here to demonstrate weak storage; production password storage needs an appropriate slow password hashing scheme.", "All 100 two-digit combinations fit a tiny search space. Longer unique passwords and slow salted storage raise guessing cost.")
    ))
)

fun passwordSecurityClueLabels(levelId: Int): Map<String, String> =
    passwordModules.getValue(levelId).exercises.mapIndexed { index, exercise ->
        "password_${levelId}_${index}" to exercise.title
    }.toMap()

fun passwordSecurityLab(levelId: Int): LabDefinition {
    val module = passwordModules.getValue(levelId)
    val caseContext = when (levelId) {
        201 -> "One student portal credential audit, including its linked shopping account and portal policy."
        202 -> "One campus portal incident: initial guessing alerts, leaked-pair escalation, and a copied database."
        203 -> "One student's sign-in setup: enrol protection, investigate legacy approval prompts, and upgrade sign-in."
        204 -> "One student account: repair recovery, revoke abandoned access, and contain a subsequent breach."
        205 -> "One registrar mailbox incident: prove credential exposure, secure sign-in, and remove persistent attacker access."
        else -> "One authorized registrar audit of three legacy records: a common-word password, a campus-pattern password, and a two-digit recovery PIN."
    }
    return LabDefinition(
        levelId = levelId,
        title = module.title,
        subtitle = "Unit 2 · Password & Account Security",
        briefing = if (levelId == 205)
            "$caseContext\n\nYour mission is to investigate and close this incident. The account must end with a replaced credential, enrolled sign-in protection, trusted recovery, no attacker session or forwarding, and rotated backup codes. Verify your practical work to capture a task-specific flag. Paste that earned flag into the simulation to claim the CASE proof code. Hints are optional; the explanation appears after success."
            else module.briefing + "\n\n$caseContext All three tasks continue the same case. Verified findings and account changes carry forward in a case journal; finish them in order." + if (levelId == 250)
            "\n\nRead the lesson, inspect the salt and target hash, select a candidate strategy, and run the offline comparison tool. Recover the matching password and submit it inside the simulation. Each new attempt uses the current time and a random nonce to vary the target. SHA-256 stays the same. In the final challenge, recover one registrar credential, replace it and enrol MFA, then revoke the attacker session and restore recovery for that same account." +
                if (levelId == 250) "\n\nStart each two-minute round when ready. Retries generate a fresh target and cost no hearts." else ""
            else "\n\nRead the lesson beside the evidence and use the account controls. Generate credentials, configure protections, or revoke access as the case requires. Save the repaired settings, capture the earned flag, and paste it into the simulation. After flag verification, submit the CASE code in the task panel.",
        assetDir = "password_security",
        startPage = "case_${levelId}_0.html",
        tasks = module.exercises.mapIndexed { index, exercise ->
            val code = "CASE-${levelId}-${index + 1}"
            LabTask(
                id = "password_${levelId}_${index}",
                title = exercise.title,
                objective = caseContext + "\n\n" + exercise.evidence + "\n\n" +
                    if (index == 0) "Start the case in the simulation. Complete the practical task and paste its earned flag to claim the CASE proof code."
                    else "Continue the same case from Task $index. Its verified results carry forward. Complete the requested work and paste this task's earned flag to claim its CASE proof code.",
                guide = if (levelId == 205) listOf("Investigate the case evidence and meet this task's objective. Optional hints and lesson references are available if you need them.")
                    else listOf(exercise.guide) + unitTwoReading(levelId, index),
                steps = if (levelId == 205) emptyList() else listOf(
                    "Open the simulation and inspect this case file.",
                    "Read the case evidence and inspect the records or account settings in the simulation.",
                    when {
                        levelId == 205 && index > 0 -> "Continue the same registrar incident. Apply the requested account repairs and save the changes."
                        levelId == 205 || levelId == 250 -> "Test individual guesses or an editable candidate list. Compare full digests yourself and submit the recovered password."
                        else -> "Use the account controls to repair the case, then save the changes. The stolen-database task includes a hashing practice step."
                    },
                    "Verify your investigation or account repair. Paste the captured flag to unlock the CASE code. Read the recap, then return here to submit the CASE code."
                ),
                entryPage = "case_${levelId}_${index}.html",
                requiredClues = listOf("password_${levelId}_${index}"),
                lockedMessage = "Resolve this case in the simulation to unlock submission.",
                answer = LabAnswer.Text(listOf(code), "CASE-..."),
                hints = listOf(exercise.guide, "Read the success panel in the simulation; it contains this case's code."),
                successFeedback = exercise.feedback + "\n\n" + unitTwoDebrief(levelId, index),
                failureFeedback = "Copy the case code displayed after resolving this specific case."
            )
        }
    )
}
