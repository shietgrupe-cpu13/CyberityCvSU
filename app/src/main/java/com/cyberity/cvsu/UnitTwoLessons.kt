package com.cyberity.cvsu

// Original teaching examples; references support the concepts, not the fictional incidents.
internal fun strongPasswordsLesson() = reviewedLesson(201, "Passwords That Survive Guessing", listOf(
    LessonPage("Length is useful; predictability still matters", LessonIcon.LOCK,
        listOf("A long password can still follow an obvious pattern. A school name, a year and a final symbol give an attacker clues about which guesses to try first. Adding punctuation is not the same as making a secret unpredictable.",
            "For the lab, generate an account-specific replacement instead of editing the old pattern. Its length target is an exercise requirement, not a promise of safety. Use fictional credentials only."),
        listOf("nist-strength"), task = 1,
        visual = LessonVisual.DoDont(listOf("Generate a fresh, unpredictable secret"), listOf("Rotate School2025! into School2026!")),
        keyTerms = listOf(KeyTerm("Predictability", "How easily an attacker can narrow the likely guesses."))),
    LessonPage("One account, one password", LessonIcon.SHIELD,
        listOf("Reusing a credential connects otherwise separate accounts: a leak from one service can expose another. A password manager makes it practical to keep distinct generated passwords and use autofill.",
            "Imagine a club forum and a research portal sharing a secret. Replacing only the portal password leaves the forum's exposed credential unchanged. In the vault task, inspect both records rather than assuming the first repair fixed everything."),
        listOf("mitre-stuffing", "nist-passwords"), task = 2,
        visual = lessonEvidence("FICTIONAL VAULT AUDIT", listOf("Research portal: newly generated", "Club forum: old shared value", "Two records need independent review"))),
    LessonPage("Make the policy address the threat", LessonIcon.SEARCH,
        listOf("NIST's 2025 guidance supports long passwords, managers and paste/autofill, screening common or compromised choices, and limiting failed online attempts. It rejects arbitrary composition rules and routine scheduled password changes; change a password when compromise is suspected or known.",
            "In the policy task, ask what each rule prevents. A monthly change that produces the next month's name leaves a predictable pattern. The lesson describes published guidance; an individual service may impose a different policy."),
        listOf("nist-passwords", "nist-strength"), task = 3,
        keyTerms = listOf(KeyTerm("Blocklist", "Common, expected or compromised passwords a service refuses.")))
), listOf(
    LessonQuestion("A password contains a school name and next year's date. What is the concern?",
        listOf("It has no possible hash", "Its structure helps an attacker choose guesses", "All long passwords are unsafe"), 1,
        "Length helps, but a familiar structure reduces unpredictability."),
    LessonQuestion("Which policy best follows the guidance in this lesson?",
        listOf("Allow managers, block common choices and limit guesses", "Require a new predictable variation each week", "Permit unlimited attempts"), 0,
        "These controls address usability, common guesses and online attack rate together.")
))

internal fun passwordAttacksLesson() = reviewedLesson(202, "Identify the Password Attack", listOf(
    LessonPage("Read the shape of the guesses", LessonIcon.SEARCH,
        listOf("An online guessing attack sends candidates to a service that checks them. A dictionary uses likely words and variations; exhaustive brute force explores a defined set of combinations. The label comes from the evidence, not merely the number of failed logins.",
            "Compare which accounts were targeted, which candidates appeared and whether attempts succeeded. Service-side throttling slows online attempts. A cooldown enforced only by a normal app can be bypassed by a different client."),
        listOf("mitre-bruteforce"), task = 1,
        visual = lessonEvidence("FICTIONAL LOGIN SAMPLE", listOf("One account", "Candidates: welcome, autumn, research", "Several failures; no success recorded")),
        keyTerms = listOf(KeyTerm("Dictionary guessing", "Trying likely passwords instead of every possible value."))),
    LessonPage("Recognize stolen credential pairs", LessonIcon.LOCK,
        listOf("Credential stuffing takes username/password pairs stolen elsewhere and tries them on another service. Each account may receive only a few attempts. A reused password can fail this way even when it is long.",
            "A report with many accounts and a known leaked-pair list suggests reuse rather than one account receiving every possible guess. Examine successful sign-ins as well as failures. Independent passwords and MFA reduce this risk."),
        listOf("mitre-stuffing"), task = 2,
        keyTerms = listOf(KeyTerm("Credential stuffing", "Replaying stolen account/password pairs against another service."))),
    LessonPage("A copied database changes the defense", LessonIcon.CODE,
        listOf("Offline guessing checks a copied password hash locally, without asking the login service. Login throttling cannot slow those local comparisons. Salted storage with an appropriate expensive password-hashing algorithm raises the work required for each guess.",
            "A salt is not a secret password or encryption key. It prevents useful reuse of precomputed hash tables across differently salted records. It does not turn a predictable password into an unpredictable one."),
        listOf("mitre-cracking", "owasp-storage"), task = 3,
        visual = LessonVisual.DoDont(listOf("Use suitable salted password storage"), listOf("Rely on the login limit to protect a copied hash")))
), listOf(
    LessonQuestion("A leaked forum password is tried on the same user's mail account. What is this?",
        listOf("Credential stuffing", "Network segmentation", "Certificate validation"), 0,
        "The attacker is reusing a stolen credential pair on another service."),
    LessonQuestion("Why does a login cooldown not stop an attacker comparing copied hashes?",
        listOf("Hashes cannot be compared", "Offline attacks need no password candidates", "The comparisons do not contact that login service"), 2,
        "Offline comparison takes place on the attacker's computer, outside the service's request limit.")
))

internal fun multiFactorLesson() = reviewedLesson(203, "Choose and Use a Second Factor", listOf(
    LessonPage("Two steps are not necessarily two factors", LessonIcon.SHIELD,
        listOf("Authentication factors describe different evidence: something you know, have or are. A password and a secret answer are both knowledge. Adding another remembered secret does not add an independent factor category.",
            "A possession-based authenticator can add another barrier. Compare the category of each item in the setup task rather than counting screens. Protect the enrolled authenticator and the account's recovery path."),
        listOf("owasp-mfa"), task = 1,
        keyTerms = listOf(KeyTerm("Factor", "A category of evidence used to authenticate a person."))),
    LessonPage("An unexpected prompt is not permission", LessonIcon.WARNING,
        listOf("Push fatigue pressures a person with repeated approval requests. Approving one just to stop the notifications can authorize someone else's login. Deny a request you did not initiate and investigate through the official service.",
            "In this fictional case, a caller claims an urgent fix requires your approval. Use a separately verified support channel, review activity and report the requests. Never hand the caller an authenticator or recovery code."),
        listOf("owasp-mfa"), task = 2,
        visual = LessonVisual.DoDont(listOf("Deny and investigate unrequested prompts"), listOf("Approve one to silence the caller"))),
    LessonPage("Bind sign-in to the genuine service", LessonIcon.LOCK,
        listOf("Typed one-time codes can be relayed by a phishing page. Passkeys use a public-key credential scoped to the legitimate service, so an impostor domain cannot request the same authentication. This is phishing resistance, not a guarantee against every account attack.",
            "A passkey can be synchronized or tied to a device. Local unlocking may use a biometric or PIN; the private credential is not a password typed into the website. In the lab, compare service binding and keep an approved recovery method."),
        listOf("fido-passkeys", "owasp-mfa"), task = 3,
        keyTerms = listOf(KeyTerm("Phishing resistance", "Authentication that prevents an impostor service from collecting a usable sign-in proof.")))
), listOf(
    LessonQuestion("Which pair adds a different factor category?",
        listOf("Password plus secret answer", "Password plus authenticator possession", "Two remembered PINs"), 1,
        "Possession adds evidence beyond another knowledge secret."),
    LessonQuestion("Why does a passkey resist the fake-domain relay described here?",
        listOf("The credential is scoped to the genuine service", "It turns every network into a trusted network", "It makes recovery unnecessary"), 0,
        "Service binding is the protection; recovery and device security still matter.")
))

internal fun accountProtectionLesson() = reviewedLesson(204, "Recovery and Sessions Are Part of Security", listOf(
    LessonPage("Audit the route back into the account", LessonIcon.LOCK,
        listOf("A recovery route can restore access when the normal factor is lost, so it needs protection too. Check that the recovery address belongs to you and that issued backup codes are not exposed in a shared folder.",
            "Keep a current, protected recovery method. A reset token or backup code is a secret, not something to send to someone claiming to help. Availability differs by service: the fictional lab's recovery codes do not mean Cyberity issues recovery codes."),
        listOf("owasp-mfa", "owasp-recovery"), task = 1,
        visual = LessonVisual.DoDont(listOf("Review recovery ownership and protected storage"), listOf("Leave recovery secrets in a shared class folder"))),
    LessonPage("Close the session, not just the window", LessonIcon.SHIELD,
        listOf("After sign-in, a session lets a service recognize later requests. Closing a browser window does not reliably invalidate its server-side session. Logout and revocation must make the session unusable at the service.",
            "On a shared workstation, use the official logout action and avoid saving credentials. If you already left, use the service's session controls from a trusted device. The task asks you to inspect an active session, not assume a closed tab ended it."),
        listOf("owasp-sessions"), task = 2,
        keyTerms = listOf(KeyTerm("Session", "The service's continuity of authenticated access after sign-in."))),
    LessonPage("Remove what the attacker changed", LessonIcon.WARNING,
        listOf("Account recovery after compromise includes more than a password reset. From a trusted device, use the official recovery route and inspect account settings, connected access and mailbox behavior. Seek the provider's support if you cannot regain control.",
            "An attacker may add forwarding to keep receiving mail. An unfamiliar forwarding destination is evidence to investigate and remove through the authorized account controls. Record what changed so the report explains the incident."),
        listOf("ms-recovery", "mitre-forwarding"), task = 3,
        visual = lessonEvidence("FICTIONAL ACCOUNT REVIEW", listOf("Recovery address changed", "Unknown forwarding destination", "Owner did not authorize either change")))
), listOf(
    LessonQuestion("You closed a shared computer's browser. What should you verify?",
        listOf("Whether the service session is actually ended", "Whether the window was a dark color", "Whether your password is longer than its username"), 0,
        "Window closure is not reliable server-side session invalidation."),
    LessonQuestion("An account breach added a forwarding rule. Why review it after changing the password?",
        listOf("Every forwarding rule is malicious", "The setting can continue sending mail to an attacker", "A new password automatically removes all account settings"), 1,
        "An unauthorized forwarding rule can outlast the original credential compromise.")
))

internal fun passwordIncidentLesson() = reviewedLesson(205, "Connect Investigation to Account Repair", listOf(
    LessonPage("Evidence first, within the training scope", LessonIcon.SEARCH,
        listOf("Recovering a candidate from a copied hash is offline password cracking. A match confirms that candidate for the displayed record; it does not establish who used the account or make real-world access authorized.",
            "This challenge keeps one fictional account across three tasks. Record the target, salt and matching result together. Work only with the supplied training record; do not test recovered candidates on real accounts."),
        listOf("mitre-cracking"), task = 1,
        keyTerms = listOf(KeyTerm("Evidence scope", "What a particular record proves, and what it does not."))),
    LessonPage("Repair the credential and the sign-in method", LessonIcon.LOCK,
        listOf("A recovered weak credential describes a problem, not a repair. Replace it with an independent generated secret and enroll an approved authenticator. Service-bound passkeys reduce phishing exposure where the service supports them.",
            "In the challenge, verify that both changes apply to the same account you investigated. Completing enrollment matters: beginning a setup screen is not proof that the new authenticator works."),
        listOf("nist-passwords", "fido-passkeys"), task = 2,
        visual = LessonVisual.DoDont(listOf("Verify replacement and authenticator enrollment"), listOf("Treat a recovered weak password as the final fix"))),
    LessonPage("Close persistence and verify ownership", LessonIcon.SHIELD,
        listOf("Account manipulation can preserve attacker access by changing authentication or account settings. Forwarding rules can continue collecting mail. Review both the sign-in path and the account's retained changes.",
            "Use the fictional incident checklist to revoke the attacker session, restore authorized recovery, remove unauthorized forwarding and replace exposed recovery codes. Then verify the owner can still sign in and report the completed containment."),
        listOf("mitre-account", "mitre-forwarding", "owasp-recovery"), task = 3,
        keyTerms = listOf(KeyTerm("Persistence", "A retained change or access path that lets an attacker continue after the initial compromise.")))
), listOf(
    LessonQuestion("What does a candidate matching a supplied hash establish?",
        listOf("Who attacked the account", "That all sessions were revoked", "That the candidate matches this training record"), 2,
        "Hash comparison is evidence about the record, not an identity or containment verdict."),
    LessonQuestion("Which result best shows a complete repair in this challenge?",
        listOf("The weak password was found", "Sign-in was repaired and unauthorized retained access was removed", "Only the lesson was read"), 1,
        "Investigation must lead to verified account repair and containment.")
))

internal fun hashPracticeLesson() = reviewedLesson(250, "Compare Hashes Without Confusing Them with Encryption", listOf(
    LessonPage("Compute a candidate; compare the full result", LessonIcon.CODE,
        listOf("Password cracking compares candidate hashes with an acquired target. It does not decrypt a hash. A dictionary supplies likely words; each must be processed with the record's specified salt and algorithm.",
            "Our fictional lab uses SHA-256(salt + ':' + candidate). Preserve the colon, exact spelling and full digest. A different input is a different test. This formula is a classroom mechanism, not a production password-storage recommendation."),
        listOf("mitre-cracking", "owasp-storage"), task = 1,
        visual = LessonVisual.Source(listOf("Training formula only:", "salt + ':' + candidate", "SHA-256 → compare full digest"))),
    LessonPage("Use a hypothesis, not a random guess", LessonIcon.SEARCH,
        listOf("A pattern-based search concentrates on likely structures. It is not proof that every password follows a campus word and a date. A public salt requires recomputing candidates for that record; it does not hide the pattern.",
            "Try an original classroom pattern such as a club name, a nearby year and a suffix. Keep the salt with its target when comparing. If the record refreshes, an earlier match cannot simply be copied as the new answer."),
        listOf("nist-strength", "owasp-storage"), task = 2,
        keyTerms = listOf(KeyTerm("Salt", "A per-record value included in hashing; it is stored alongside the password hash."))),
    LessonPage("A tiny search space is easy to exhaust", LessonIcon.LOCK,
        listOf("Two decimal digits provide 10 × 10 = 100 candidates, including leading-zero values. Exhaustive search covers the entire defined set. This arithmetic is about the exercise's PIN, not every real authenticator's PIN policy.",
            "Fast raw SHA-256 permits cheap repeated comparisons. Real password storage needs a suitable expensive scheme such as Argon2id or an appropriately configured alternative. Online throttling cannot constrain a copied hash; the practice stays inside fictional records."),
        listOf("owasp-storage", "mitre-bruteforce"), task = 3,
        visual = lessonEvidence("CLASSROOM SEARCH SPACE", listOf("00–09: ten candidates", "Ten first-digit choices × ten second-digit choices", "100 total candidates")))
), listOf(
    LessonQuestion("The lab refreshes its salt and target. What must you do?",
        listOf("Compare against the new record using its salt", "Reuse the old digest regardless of salt", "Treat the salt as a secret decryption key"), 0,
        "Each comparison must use the current record's inputs."),
    LessonQuestion("Why is fast SHA-256 used in this exercise?",
        listOf("It is the recommended production password scheme", "It makes a small comparison exercise practical while showing weak storage", "It prevents all offline guessing"), 1,
        "The classroom demonstration is deliberately fast; production storage should make guesses expensive.")
))
