const UnitTwoLearning = {
  "201_0": {
    "topic": [
      "Why predictability matters",
      "A password is a secret used to prove control of an account. Attackers do not start by trying every possible string. They first try names, common words, dates, and patterns that people often choose. A campus name followed by the current year and an exclamation mark follows exactly that kind of pattern. Adding a symbol does not remove the predictable parts.",
      "Length gives a randomly generated password more possible combinations, while unique credentials stop a breach at one service from opening another. In this exercise, calculate n and φ(n) from the randomized p and q shown in the simulation, then type a fictional replacement or use the generator and keep it unique with at least 20 characters. That length is an exercise target, not a guarantee of safety: a long reused or phished password can still be stolen.",
      "Compare the fictional credentials in your tray. Place each under the property it demonstrates, then use the generator below to repair the portal credential.",
      "Predictable patterns versus generated secrets",
      "The campus-name example is predictable because it combines public context with a familiar suffix. A random generated secret is harder to guess because its characters were not chosen from personal information. A long credential reused elsewhere still carries a reuse risk. The lesson is to combine length, unpredictability, uniqueness, and safe handling."
    ],
    "zones": [
      "Predictable pattern",
      "Generated unique secret",
      "Reuse risk"
    ],
    "cards": [
      [
        "CvSU2026!",
        "Predictable pattern",
        "A campus name, year, and common suffix are predictable."
      ],
      [
        "20 random characters, unique to this portal",
        "Generated unique secret",
        "The generator avoids personal context, and uniqueness limits spread."
      ],
      [
        "A long password used on both portal and shopping",
        "Reuse risk",
        "Length does not stop a known leaked value being reused."
      ],
      [
        "Student name followed by birthday",
        "Predictable pattern",
        "Personal information makes a candidate predictable."
      ],
      [
        "Welcome2026! for a campus login",
        "Predictable pattern",
        "A common word and year are familiar patterns."
      ],
      [
        "24 random characters made for this account",
        "Generated unique secret",
        "A generated account-specific value avoids the stated patterns and reuse."
      ],
      [
        "Randomly selected words used only for this portal",
        "Generated unique secret",
        "Random selection and uniqueness matter more than a familiar quotation."
      ],
      [
        "The same generated password on three services",
        "Reuse risk",
        "Randomness does not stop spread after a reused credential leaks."
      ],
      [
        "A shopping password copied to the portal",
        "Reuse risk",
        "Copying another account's credential creates reuse."
      ]
    ]
  },
  "201_1": {
    "topic": [
      "Why reuse spreads a breach",
      "An account password can be strong against guessing and still fail after another service leaks it. When someone reuses the same credential, the attacker no longer needs to guess: they can test the leaked email and password at other services. This is why two long identical passwords are not two independent protections.",
      "A password manager can generate and store a different credential for each account. Protect the manager itself with strong authentication and maintain a recovery method. Change the affected credential at the breached service and at every account where it was reused; changing only one side leaves the other exposed.",
      "Sort the vault records by whether the breach could spread to them. Then generate two different replacement credentials and choose protected storage.",
      "Breaking the link between accounts",
      "The reused credential lets the same leaked value cross from shopping to the portal. The unique credential breaks that exact link, although it still needs protection against phishing and other theft. Generating different replacements for both exposed accounts removes the old reusable value; storing them privately helps prevent a new exposure."
    ],
    "zones": [
      "Breach can spread",
      "Different credential"
    ],
    "cards": [
      [
        "Portal and shopping share the leaked password",
        "Breach can spread",
        "The same known value can be tested at either service."
      ],
      [
        "Portal has its own new generated password",
        "Different credential",
        "A different credential breaks this exact leaked-pair link."
      ],
      [
        "Club account reuses the leaked shopping password",
        "Breach can spread",
        "Reused credentials extend the exposure to another service."
      ],
      [
        "Email reuses the breached shopping credential",
        "Breach can spread",
        "The leaked value can be tested against email."
      ],
      [
        "Library account shares the exposed portal password",
        "Breach can spread",
        "Sharing the credential links the accounts."
      ],
      [
        "Changing the portal but leaving the leaked value on the club account",
        "Breach can spread",
        "The unchanged club credential remains exposed."
      ],
      [
        "Email has its own generated credential",
        "Different credential",
        "The shopping leak does not provide this different value."
      ],
      [
        "Club account has a different generated credential",
        "Different credential",
        "There is no identical credential link in this record."
      ],
      [
        "Replacing both exposed credentials with distinct generated values",
        "Different credential",
        "Distinct replacements remove the known reusable value."
      ]
    ]
  },
  "201_2": {
    "topic": [
      "Password rules that help people",
      "A useful password policy makes secure behaviour easier. It permits long values and password manager paste or autofill, rejects commonly used or breached passwords, and limits online guessing. Requiring a new month in a password often encourages a predictable rotation rather than a genuinely new secret.",
      "A reset is appropriate when compromise is suspected or confirmed. Routine changes alone do not revoke stolen sessions or repair an account's recovery settings. Policy controls and incident response solve different problems, so a good system needs both.",
      "Sort helpful and harmful policy changes, then configure the portal's minimum length, blocklist, autofill, and reset condition.",
      "Why the repaired policy works",
      "Long-password support expands the available search space; a blocklist rejects known weak choices; autofill supports unique generated values. Predictable monthly suffixes are easy to anticipate. Resetting on compromise responds to actual exposure and should be accompanied by review of sessions and recovery when an incident occurred."
    ],
    "zones": [
      "Supports secure use",
      "Encourages weakness"
    ],
    "cards": [
      [
        "Allow paste and password manager autofill",
        "Supports secure use",
        "Autofill makes unique generated credentials practical."
      ],
      [
        "Block common or breached passwords",
        "Supports secure use",
        "Known weak choices should be rejected."
      ],
      [
        "Require only a new month suffix",
        "Encourages weakness",
        "A predictable suffix is easy to anticipate."
      ],
      [
        "Permit long password values",
        "Supports secure use",
        "Users need room for long generated secrets or passphrases."
      ],
      [
        "Limit repeated online login attempts",
        "Supports secure use",
        "Throttling slows guesses against the login service."
      ],
      [
        "Reset a credential after confirmed exposure",
        "Supports secure use",
        "The reset responds to actual compromise."
      ],
      [
        "Ban password manager paste",
        "Encourages weakness",
        "This makes unique generated credentials harder to use."
      ],
      [
        "Require the campus name in every password",
        "Encourages weakness",
        "A mandatory public pattern narrows likely guesses."
      ],
      [
        "Reuse one password to simplify monthly resets",
        "Encourages weakness",
        "Reuse spreads exposure between services."
      ]
    ]
  },
  "202_0": {
    "topic": [
      "Recognising how a password is attacked",
      "Dictionary guessing tries likely words and common variations. Brute force systematically enumerates combinations within a chosen format. Credential stuffing tests username and password pairs already exposed elsewhere. These techniques are distinguished by the source of the candidates, not just the number of failed logins.",
      "The case file shows several familiar password patterns against one account. That supports dictionary guessing. Limiting online attempts can slow repeated login requests, but it does not secure a stolen password database. You must identify the attack setting before choosing a defence.",
      "Match the attack traces in your tray to their technique, then choose a response to the case file.",
      "Reading the source of the guesses",
      "Common-word variations belong to dictionary guessing. Enumerating every two-digit value is a small brute-force search. Testing a known leaked email/password pair elsewhere is credential stuffing. A correct classification helps select controls, while the surrounding evidence tells you whether any login actually succeeded."
    ],
    "zones": [
      "Dictionary",
      "Brute force",
      "Credential stuffing"
    ],
    "cards": [
      [
        "Try password, welcome, and common variations",
        "Dictionary",
        "These candidates come from likely words."
      ],
      [
        "Try every two-digit value from 00 to 99",
        "Brute force",
        "Every combination in the format is enumerated."
      ],
      [
        "Try a leaked email/password pair on another site",
        "Credential stuffing",
        "A known credential pair is being reused."
      ],
      [
        "Try common words with a final exclamation mark",
        "Dictionary",
        "Likely words and familiar variations form a dictionary search."
      ],
      [
        "Try the most popular password phrases",
        "Dictionary",
        "The guesses are selected for their likelihood."
      ],
      [
        "Enumerate all values in a three-digit format",
        "Brute force",
        "Every combination in a defined format is tried."
      ],
      [
        "Enumerate every allowed two-letter combination",
        "Brute force",
        "Systematic enumeration is brute force."
      ],
      [
        "Test an exposed email and its known password on the portal",
        "Credential stuffing",
        "The previously leaked pair is reused at a new service."
      ],
      [
        "Replay a breach's credential pairs against a club site",
        "Credential stuffing",
        "The candidate source is leaked username/password pairs."
      ]
    ]
  },
  "202_1": {
    "topic": [
      "Recognising stolen credential pairs",
      "When many different usernames receive a known password from a leak, the attack may be credential stuffing. The attacker is reusing information that was already stolen rather than discovering a new secret independently. Successful logins in that pattern must be investigated as possible compromises.",
      "Unique credentials reduce spread between services. MFA adds a further barrier, and incident response must review successful sessions and account changes. Login alerts are useful evidence, but receiving an alert does not remove the attacker's access.",
      "Sort login records by their candidate source, then respond to the successful leaked-pair logins.",
      "Why a leaked pair is different from a guess",
      "A previously leaked email/password combination is a credential-stuffing input. A list of common words is dictionary input. Exhaustively enumerated combinations are brute-force input. Strong length alone cannot save a credential that is already known, so repair requires removing reuse and examining successful access."
    ],
    "zones": [
      "Dictionary",
      "Brute force",
      "Credential stuffing"
    ],
    "cards": [
      [
        "Thousands of leaked email/password pairs",
        "Credential stuffing",
        "The candidates already belonged to breached accounts."
      ],
      [
        "Every combination in a fixed small format",
        "Brute force",
        "The search is exhaustive within that format."
      ],
      [
        "A common-word password list",
        "Dictionary",
        "Likely words determine the guesses."
      ],
      [
        "One guessed word tried from a popular-password list",
        "Dictionary",
        "The word was chosen as a likely password."
      ],
      [
        "Try common words with number suffixes",
        "Dictionary",
        "These are dictionary variations."
      ],
      [
        "Try every numeric value of a fixed length",
        "Brute force",
        "The entire defined numeric space is enumerated."
      ],
      [
        "Cycle every two-character combination",
        "Brute force",
        "The list exhausts the selected format."
      ],
      [
        "Use a leaked shopping email/password on email",
        "Credential stuffing",
        "The known pair is tested at a different service."
      ],
      [
        "Use previously exposed portal credential pairs on a forum",
        "Credential stuffing",
        "The attacker is reusing stolen pairs."
      ]
    ]
  },
  "202_2": {
    "topic": [
      "Understanding hashes before you crack them",
      "A hash function transforms an input into a digest. SHA-256 produces a 256-bit digest, displayed here as 64 hexadecimal characters. Repeating exactly the same input with the same function produces the same digest. Even a small change to the input generally changes many positions in the output.",
      "A password hash is not a secret message that you decrypt. In an offline attack, you hash a candidate with the same recorded salt and compare the digest with the stored target. A salt is public, account-specific input that stops a single precomputed digest being reused across differently salted records; it does not make a short predictable password strong.",
      "First sort controls by whether they address online login attempts or copied hashes. Then calculate two classroom examples that differ by one character and compare their complete digests.",
      "What the two digests demonstrate",
      "The outputs have the same length even though the input changed. The changed positions demonstrate why you must compare the complete digest and use the exact input, including the salt format. Fast SHA-256 is used here to make the lesson visible; real account storage needs an appropriate slow salted password hashing scheme. Offline guessing is unaffected by a login-page attempt limit."
    ],
    "zones": [
      "Online login attempts",
      "Copied-hash defence"
    ],
    "cards": [
      [
        "Rate-limit the login page",
        "Online login attempts",
        "A website limit acts on requests to that website."
      ],
      [
        "Use a slow password hashing scheme",
        "Copied-hash defence",
        "It raises the work needed for each offline guess."
      ],
      [
        "Use unique per-record salts",
        "Copied-hash defence",
        "Different salts prevent reuse of a single precomputed digest across records."
      ],
      [
        "Temporary login throttling after repeated failures",
        "Online login attempts",
        "A throttle acts on website requests."
      ],
      [
        "Limit requests per account at the sign-in endpoint",
        "Online login attempts",
        "The control reduces online attempts."
      ],
      [
        "Add monitoring for repeated login requests",
        "Online login attempts",
        "The monitoring observes activity at the service."
      ],
      [
        "Apply an appropriate password-hash work factor",
        "Copied-hash defence",
        "It increases computation per offline guess."
      ],
      [
        "Avoid unsalted fast password digests in account storage",
        "Copied-hash defence",
        "Fast reusable digests make copied records easier to attack."
      ],
      [
        "Use account-specific salt values with slow hashing",
        "Copied-hash defence",
        "Salts and slow hashing address copied-record attacks."
      ]
    ]
  },
  "203_0": {
    "topic": [
      "Understanding authentication factors",
      "A factor describes the kind of evidence an account uses: something you know, something you have, or something you are. A password and a secret-question answer are both knowledge secrets. Asking for two knowledge secrets does not create a possession factor.",
      "An authenticator app or security key can demonstrate possession of an enrolled device or credential. The training proof below is only a simulation of enrolment; real services use their own enrolment and verification process. Protect recovery because it may offer another route into the account.",
      "Sort authentication examples into factor categories, then enrol a possession factor and verify its displayed training proof.",
      "Why the second factor matters",
      "The password belongs to knowledge, an authenticator belongs to possession, and a fingerprint is a biometric example. The added factor means a stolen password alone may not be sufficient. It does not remove the need for safe recovery, careful approvals, or protection against phishing."
    ],
    "zones": [
      "Something you know",
      "Something you have",
      "Something you are"
    ],
    "cards": [
      [
        "Account password",
        "Something you know",
        "The account verifies a knowledge secret."
      ],
      [
        "Enrolled authenticator device",
        "Something you have",
        "The account verifies possession of the enrolled method."
      ],
      [
        "Fingerprint biometric",
        "Something you are",
        "A fingerprint is a biometric example."
      ],
      [
        "Secret-question answer",
        "Something you know",
        "A remembered answer is a knowledge secret."
      ],
      [
        "Memorised account PIN",
        "Something you know",
        "The PIN is something the claimant knows."
      ],
      [
        "Enrolled hardware security key",
        "Something you have",
        "The authentication uses possession of the enrolled key."
      ],
      [
        "Enrolled authenticator on a phone",
        "Something you have",
        "The phone holds the enrolled authentication method."
      ],
      [
        "Facial biometric",
        "Something you are",
        "The example uses a biometric characteristic."
      ],
      [
        "Iris biometric",
        "Something you are",
        "An iris measurement is a biometric characteristic, not a remembered secret or a held device."
      ]
    ]
  },
  "203_1": {
    "topic": [
      "Unexpected approval prompts are evidence",
      "An MFA approval should correspond to a sign-in you started. Several prompts that arrive while you are doing nothing may indicate that someone knows the first factor and is trying to get you to approve their login. A caller asking you to approve or share a code is not proof of legitimacy.",
      "Deny the requests, inspect the account through the official service, and report the suspicious activity. Do not approve just to stop notifications. If you discover an unfamiliar successful session, contain it and review the credential and recovery paths.",
      "Separate safe responses from responses that grant access. Then deny the simulated prompts, inspect the official history, and record the training incident.",
      "Why denying is only the first step",
      "Denying the requests blocks the approvals you did not initiate. Reviewing history can show whether a separate login succeeded. Reporting helps the incident be investigated. Approving an unexpected request or sharing a code can provide the attacker with the evidence they need."
    ],
    "zones": [
      "Safe response",
      "Grants attacker access"
    ],
    "cards": [
      [
        "Deny a prompt you did not initiate",
        "Safe response",
        "There is no owner-initiated login to approve."
      ],
      [
        "Share an OTP with an unexpected caller",
        "Grants attacker access",
        "The caller can use the code while it remains valid."
      ],
      [
        "Open official security settings to review activity",
        "Safe response",
        "A trusted route lets you examine the actual account."
      ],
      [
        "Approve a prompt to silence notifications",
        "Grants attacker access",
        "Approval can authorise a login you did not initiate."
      ],
      [
        "Read a backup code to an unknown caller",
        "Grants attacker access",
        "A backup code may provide an alternate access route."
      ],
      [
        "Enter an OTP into a caller's unofficial page",
        "Grants attacker access",
        "A fake page can capture and relay the code."
      ],
      [
        "Deny unexpected approvals and inspect the official account",
        "Safe response",
        "The owner does not grant the unrequested access."
      ],
      [
        "Report the unexpected prompt campaign",
        "Safe response",
        "Reporting lets the incident be investigated."
      ],
      [
        "Check whether an unfamiliar login succeeded",
        "Safe response",
        "Successful access needs review and containment."
      ]
    ]
  },
  "203_2": {
    "topic": [
      "Understanding phishing-resistant sign-in",
      "A typed one-time code can be stolen by a fake page and relayed while it is still valid. This is one reason MFA methods differ in their resistance to phishing. A passkey or FIDO security key binds authentication to the genuine service rather than simply asking you to type a reusable response into any page.",
      "A passkey still needs safe enrolment and recovery. Only enrol through the official service, keep a supported recovery path, and protect any backup codes. The simulator registers a fictional credential for a fictional portal; it does not change your real device credentials.",
      "Sort the sign-in methods by whether they use typed codes or service-bound cryptographic authentication. Then register the training passkey and choose protected recovery storage.",
      "What service binding adds",
      "The passkey and FIDO key use a credential associated with the service. A typed authenticator code can still be relayed through a phishing page. Protecting backup codes closes a recovery weakness that the primary sign-in method alone does not address."
    ],
    "zones": [
      "Service-bound method",
      "Typed code method"
    ],
    "cards": [
      [
        "Passkey for the official portal",
        "Service-bound method",
        "The credential is associated with the legitimate service."
      ],
      [
        "FIDO security key",
        "Service-bound method",
        "The authentication is bound to the service."
      ],
      [
        "Type an authenticator OTP into a page",
        "Typed code method",
        "A phisher may relay a code entered into their page."
      ],
      [
        "Enrolled passkey for the registrar portal",
        "Service-bound method",
        "The credential is associated with the service."
      ],
      [
        "Enrolled FIDO authenticator for the campus service",
        "Service-bound method",
        "The authentication is bound to the legitimate service."
      ],
      [
        "Security key used through the official service-bound flow",
        "Service-bound method",
        "The flow verifies the service, rather than accepting an arbitrary typed code."
      ],
      [
        "SMS sign-in code entered into a page",
        "Typed code method",
        "The user supplies a typed code."
      ],
      [
        "Six-digit authenticator code entered into a form",
        "Typed code method",
        "The code can be captured and relayed by a fake page."
      ],
      [
        "Recovery code typed into a recovery form",
        "Typed code method",
        "This is a typed alternate-access code, not a service-bound passkey."
      ]
    ]
  },
  "204_0": {
    "topic": [
      "Recovery is another door into an account",
      "A recovery mailbox, phone number, or backup code can restore access when normal sign-in fails. That makes recovery useful to the owner and valuable to an attacker. An abandoned mailbox or backup codes posted in a shared folder may bypass protections that otherwise work well.",
      "Keep recovery methods current and under your control. Protect the recovery mailbox with a unique credential and MFA. When backup codes were exposed, replace them so the old copies no longer work, and store the replacements privately.",
      "Sort recovery records by whether the owner controls and protects them. Then update the mailbox, replace exposed codes, and select private storage.",
      "Why moving a file is not enough",
      "A private replacement code is useful only after the exposed original is invalidated. A protected current recovery mailbox keeps the alternate access path with the owner. A shared folder exposes the secret to other people, and an abandoned address may no longer be under reliable control."
    ],
    "zones": [
      "Owner-controlled protection",
      "Exposed or unreliable"
    ],
    "cards": [
      [
        "Current recovery mailbox with unique password and MFA",
        "Owner-controlled protection",
        "The owner protects the alternate access path."
      ],
      [
        "Backup codes in a shared class folder",
        "Exposed or unreliable",
        "Other people can copy the recovery secret."
      ],
      [
        "Abandoned recovery mailbox",
        "Exposed or unreliable",
        "The owner no longer reliably controls the route."
      ],
      [
        "New backup codes kept in a protected vault",
        "Owner-controlled protection",
        "Private storage limits exposure of the recovery secret."
      ],
      [
        "Protected offline backup-code copy under owner control",
        "Owner-controlled protection",
        "The owner controls and protects the recovery material."
      ],
      [
        "Current protected owner recovery address",
        "Owner-controlled protection",
        "The owner controls the alternate access path."
      ],
      [
        "Backup codes in a public note",
        "Exposed or unreliable",
        "The codes are exposed to other readers."
      ],
      [
        "Recovery address changed to an unknown mailbox",
        "Exposed or unreliable",
        "The route is not controlled by the owner in this case."
      ],
      [
        "Old exposed codes moved without being replaced",
        "Exposed or unreliable",
        "Previously copied codes may still work until invalidated."
      ]
    ]
  },
  "204_1": {
    "topic": [
      "A browser window is not a session",
      "A session represents authenticated access after sign-in. Closing the visible browser window does not necessarily tell the server to end that access. A shared computer may retain cookies or saved credentials, so the owner should sign out and avoid saving their credential there.",
      "Account settings can list active sessions and offer revocation. Use the device and activity evidence to distinguish a trusted owner session from a shared or unfamiliar one. Preserve the owner's trusted access while removing the session that should no longer be active.",
      "Place session records under Keep or Revoke, then revoke the library session and choose the correct habit for shared devices.",
      "Why the session decision follows evidence",
      "The personal phone is identified as trusted owner access. The abandoned library session and unfamiliar desktop need removal in these case facts. Revoking a session acts on server-side access; merely closing a tab or browser is not reliable containment."
    ],
    "zones": [
      "Keep trusted access",
      "Revoke access"
    ],
    "cards": [
      [
        "Owner phone, confirmed trusted",
        "Keep trusted access",
        "The case identifies this as legitimate owner access."
      ],
      [
        "Library computer left signed in",
        "Revoke access",
        "The owner has left a shared device."
      ],
      [
        "Unfamiliar desktop after suspicious activity",
        "Revoke access",
        "This case provides evidence of untrusted access."
      ],
      [
        "Owner laptop confirmed by device and activity history",
        "Keep trusted access",
        "The case confirms this is trusted access."
      ],
      [
        "Owner tablet recognised and currently used",
        "Keep trusted access",
        "The owner has verified this session."
      ],
      [
        "Confirmed owner browser on a personal device",
        "Keep trusted access",
        "This record identifies legitimate access."
      ],
      [
        "Shared laboratory computer left signed in",
        "Revoke access",
        "Access remains on a device the owner has left."
      ],
      [
        "Unfamiliar browser after the suspicious login",
        "Revoke access",
        "The incident evidence identifies untrusted access."
      ],
      [
        "Unused shared-device session still active",
        "Revoke access",
        "The owner no longer needs this shared-device access."
      ]
    ]
  },
  "204_2": {
    "topic": [
      "Removing attacker persistence",
      "An attacker who successfully signs in may do more than read data. They may add a recovery method, create a forwarding rule, or leave an authenticated session active. These changes can preserve access or leak future messages even after the password is changed.",
      "Use a trusted route to replace exposed credentials and revoke attacker sessions. Inspect recovery and connected applications, remove forwarding changes, and report the incident. Review other services where the same credential was reused. If you cannot regain access, use the official support process rather than instructions from a suspicious message.",
      "Sort repairs by which access path they close. Then complete all five containment actions in the account panel.",
      "Why one repair is not enough",
      "A password reset removes a known credential but does not necessarily end existing sessions. Revocation closes session access. Repairing recovery and forwarding removes separate persistence and leakage paths. Reporting and reuse review help investigate the incident beyond the single account."
    ],
    "zones": [
      "Credential access",
      "Session access",
      "Recovery or forwarding"
    ],
    "cards": [
      [
        "Replace the exposed password",
        "Credential access",
        "It removes the known sign-in secret."
      ],
      [
        "Revoke the unfamiliar desktop session",
        "Session access",
        "It ends that existing authenticated access."
      ],
      [
        "Remove unknown recovery and forwarding changes",
        "Recovery or forwarding",
        "It closes separate persistence and leakage paths."
      ],
      [
        "Reset the known leaked sign-in secret",
        "Credential access",
        "The repair targets credential-based access."
      ],
      [
        "Replace reused credentials on affected services",
        "Credential access",
        "It removes the exposed credential links."
      ],
      [
        "End the attacker browser session",
        "Session access",
        "It revokes ongoing authenticated access."
      ],
      [
        "Revoke the unfamiliar library session",
        "Session access",
        "It ends that session's access."
      ],
      [
        "Restore an owner-controlled recovery mailbox",
        "Recovery or forwarding",
        "It repairs an alternate access route."
      ],
      [
        "Delete attacker mailbox forwarding",
        "Recovery or forwarding",
        "It stops the rule's message leakage."
      ]
    ]
  },
  "205_0": {
    "topic": [
      "Investigate before repairing",
      "This capstone follows one fictional registrar mailbox across three tasks. The first task gives you the salt and hash from its weak credential. Your job is to discover which candidate produces the same digest, not to decrypt a hidden message. You can test one guess at a time or calculate a bounded candidate list.",
      "Read the record's formula carefully: this exercise hashes the salt, a colon, and the candidate in that order. Compare every character of the candidate digest with the target. The tool never labels or highlights the correct row, and calculating a guess does not itself complete the task.",
      "Match the hashing terms to their roles, recover the weak registrar credential, and submit the chosen candidate. In the next task you will secure the same account.",
      "Why evidence comes before a fix",
      "A successful comparison establishes that the supplied weak candidate matches this fictional record. It does not demonstrate that the account is now safe. The public salt is needed to reproduce the hash input; the digest is the comparison target; the candidate is the guessed input. The confirmed weakness becomes the reason to replace the credential and improve authentication."
    ],
    "zones": [
      "Guessed input",
      "Public input",
      "Comparison output"
    ],
    "cards": [
      [
        "Candidate password",
        "Guessed input",
        "The candidate is the possible secret being tested."
      ],
      [
        "Recorded salt",
        "Public input",
        "The salt must be reproduced exactly in the hash input."
      ],
      [
        "Stored SHA-256 digest",
        "Comparison output",
        "The calculated candidate digest is compared with this output."
      ],
      [
        "A common word proposed as the password",
        "Guessed input",
        "It is a guessed candidate, not proof by itself."
      ],
      [
        "A manually entered fictional password guess",
        "Guessed input",
        "The candidate still needs hashing and comparison."
      ],
      [
        "Salt copied from the account record",
        "Public input",
        "The recorded public input must be reproduced exactly."
      ],
      [
        "The public per-record salt value",
        "Public input",
        "The salt is part of the hash input."
      ],
      [
        "Full digest computed for a candidate",
        "Comparison output",
        "It is an output to compare with the target."
      ],
      [
        "64-character hexadecimal target digest",
        "Comparison output",
        "This is the stored comparison output."
      ]
    ]
  },
  "205_1": {
    "topic": [
      "Secure the same registrar sign-in",
      "The investigation confirmed a weak password on the registrar mailbox. Changing an unrelated account would not close this incident, so the simulator carries the same account and incident identifier into this task. Generate a unique replacement and enrol another sign-in protection.",
      "A supported passkey provides service-bound authentication. An authenticator app adds a possession factor when used alongside the password. Either still requires careful recovery and session handling. The attacker may already have an active session, so do not assume a new credential alone finishes the response.",
      "Place the repairs under the weaknesses they address, then generate the registrar replacement and enrol its selected sign-in protection.",
      "What was fixed, and what remains",
      "The unique generated credential repairs the weak known secret, and the enrolled sign-in method strengthens future authentication. Existing sessions, forwarding rules, and recovery settings are separate access paths. The next task addresses them on this same mailbox."
    ],
    "zones": [
      "Weak credential",
      "Missing sign-in protection",
      "Existing access"
    ],
    "cards": [
      [
        "Generate a unique replacement credential",
        "Weak credential",
        "A replacement removes the recovered weak secret."
      ],
      [
        "Enrol a passkey or authenticator",
        "Missing sign-in protection",
        "It strengthens subsequent sign-ins."
      ],
      [
        "Revoke an attacker session in the next task",
        "Existing access",
        "Session removal targets access that is already active."
      ],
      [
        "Replace the known weak registrar secret",
        "Weak credential",
        "The repair removes the recovered sign-in value."
      ],
      [
        "Stop reusing the exposed registrar credential",
        "Weak credential",
        "It removes a credential reuse link."
      ],
      [
        "Register an authenticator for subsequent sign-ins",
        "Missing sign-in protection",
        "The enrolment adds sign-in protection."
      ],
      [
        "Register a supported service-bound passkey",
        "Missing sign-in protection",
        "The method strengthens later authentication."
      ],
      [
        "End the existing unfamiliar browser session",
        "Existing access",
        "It targets access that is already authenticated."
      ],
      [
        "Revoke the attacker's active desktop session",
        "Existing access",
        "It closes current session access."
      ]
    ]
  },
  "205_2": {
    "topic": [
      "Contain and explain the registrar incident",
      "The final task retains the owner phone session while removing the unfamiliar desktop session. It also removes attacker forwarding, restores the owner's protected recovery mailbox, invalidates exposed backup codes, and records the training incident report. These actions target distinct ways an attacker can remain connected.",
      "Containment should follow the evidence. Do not revoke trusted access simply because another session was suspicious. A good incident report explains the observed weakness, the matching evidence, the repairs performed, and remaining uncertainty. The simulator records the training workflow locally; it does not send a report to a real person.",
      "Arrange the investigation, sign-in repair, and containment phases in order. Then finish the account controls and read the incident debrief.",
      "The complete chain of reasoning",
      "Hash comparison confirmed a weak registrar credential. A generated replacement and MFA strengthened sign-in. Revoking the unfamiliar session removed ongoing access; forwarding removal stopped that leakage path; protected recovery and rotated backup codes removed alternate access paths. The trusted owner session remained. These are complementary repairs, not interchangeable buttons."
    ],
    "zones": [
      "1 · Investigate",
      "2 · Secure sign-in",
      "3 · Contain"
    ],
    "cards": [
      [
        "Confirm the weak credential from its hash",
        "1 · Investigate",
        "Evidence establishes what needs repair."
      ],
      [
        "Replace the credential and enrol MFA",
        "2 · Secure sign-in",
        "Repair the sign-in weakness."
      ],
      [
        "Revoke access and restore recovery settings",
        "3 · Contain",
        "Remove ongoing access and alternate attacker routes."
      ],
      [
        "Inspect the copied credential record",
        "1 · Investigate",
        "Evidence is gathered in the investigation phase."
      ],
      [
        "Compare candidate digests with the stored target",
        "1 · Investigate",
        "Comparison establishes the weak credential."
      ],
      [
        "Generate the registrar replacement password",
        "2 · Secure sign-in",
        "This repairs future credential-based sign-in."
      ],
      [
        "Enrol registrar MFA",
        "2 · Secure sign-in",
        "This strengthens subsequent authentication."
      ],
      [
        "Remove attacker forwarding and recovery changes",
        "3 · Contain",
        "This closes persistence and leakage paths."
      ],
      [
        "Invalidate exposed backup codes and report the incident",
        "3 · Contain",
        "These actions finish containment and response in this training workflow."
      ]
    ]
  },
  "250_0": {
    "topic": [
      "Dictionary practice: hash, compare, decide",
      "Dictionary guessing begins with a list of likely words. In this round, the fictional password is one entry in the supplied classroom list. The record changes whenever you generate a new attempt, so memorising a previous answer will not solve the next record.",
      "The salt is public and must be included exactly. Use an individual guess if you want to understand one comparison, or load and edit the list to compare several. The tool calculates every selected candidate with a neutral appearance. You decide which full digest equals the target and submit that candidate.",
      "Match candidate, salt, and digest to their meanings before starting the two-minute round. Then recover the dictionary password.",
      "What a dictionary result proves",
      "A matching digest shows that this supplied candidate reproduces the fictional record. The attack worked because the password came from a short likely-word list. A public salt changes each record's digest, but the weak word can still be found by computing guesses for that salt. Unique generated credentials and suitable slow storage address different parts of the risk."
    ],
    "zones": [
      "Guessed input",
      "Public input",
      "Comparison output"
    ],
    "cards": [
      [
        "Candidate password",
        "Guessed input",
        "You choose the candidate to hash."
      ],
      [
        "Recorded salt",
        "Public input",
        "It is public input specific to this training record."
      ],
      [
        "Full candidate digest",
        "Comparison output",
        "Compare the complete output with the target."
      ],
      [
        "A word selected from the classroom dictionary",
        "Guessed input",
        "It is a candidate to be tested."
      ],
      [
        "A learner's individual password guess",
        "Guessed input",
        "It is an input proposed as the secret."
      ],
      [
        "The displayed salt string",
        "Public input",
        "This public input identifies the record's hash construction."
      ],
      [
        "A salt included before the colon and candidate",
        "Public input",
        "It is public input to this exercise's formula."
      ],
      [
        "The stored target digest",
        "Comparison output",
        "It is the output against which candidates are compared."
      ],
      [
        "The complete SHA-256 output for a guess",
        "Comparison output",
        "The full digest is the comparison output."
      ]
    ]
  },
  "250_1": {
    "topic": [
      "Pattern practice: use context without guessing blindly",
      "People sometimes build credentials from campus words, years, and familiar symbols. An attacker can prioritise that pattern instead of enumerating all possible text. This exercise supplies four words, three years around the reference year, and two suffixes, giving 24 candidates.",
      "The current time and a random nonce change the training target between attempts. That variation is for replay; it is not a production password-generation recommendation. The hashing function remains SHA-256, and the recorded salt determines the input used for comparisons.",
      "Sort candidate strategies by the source of their guesses, then build or edit the campus-pattern list and identify the matching digest.",
      "Why context narrows a search",
      "The campus pattern reduces the supplied search to 4 × 3 × 2 possible candidates. That count depends on the classroom format, not on a universal cracking-time estimate. Changing a year or adding a common suffix does not create the unpredictability of a generated random secret."
    ],
    "zones": [
      "Dictionary",
      "Pattern",
      "Brute force"
    ],
    "cards": [
      [
        "Common-password word list",
        "Dictionary",
        "The guesses are selected as likely words."
      ],
      [
        "Campus word + year + suffix",
        "Pattern",
        "Context and a repeated format narrow the guesses."
      ],
      [
        "Every two-digit value",
        "Brute force",
        "The defined format is enumerated exhaustively."
      ],
      [
        "Popular-password phrases",
        "Dictionary",
        "The candidates were selected as likely words."
      ],
      [
        "Common-word list with familiar variants",
        "Dictionary",
        "This is dictionary-based candidate selection."
      ],
      [
        "Student word plus nearby year and symbol",
        "Pattern",
        "A repeated context-based format narrows the candidates."
      ],
      [
        "Campus name combined with a year suffix",
        "Pattern",
        "The guesses follow a contextual pattern."
      ],
      [
        "All values from 00 to 99",
        "Brute force",
        "Every value of the two-digit format is included."
      ],
      [
        "Every two-letter combination in a given alphabet",
        "Brute force",
        "The defined space is exhaustively enumerated."
      ]
    ]
  },
  "250_2": {
    "topic": [
      "Brute-force practice: count the search space",
      "Brute force enumerates combinations in a specified format. A two-digit training PIN has 10 possibilities for each position, or 100 values from 00 to 99. Leading zeroes matter: the string 07 is not the same hash input as the string 7.",
      "This deliberately small exercise makes the whole search visible. Real costs depend on the input distribution, hardware, and password storage scheme, so the timer and animation are not estimates of real cracking speed. Longer unpredictable secrets and slow salted hashing make the corresponding work much larger or more expensive.",
      "Classify tiny exhaustive searches by their format, then enumerate the two-digit candidates and compare the full salted hashes.",
      "What the small PIN teaches",
      "Only 100 values fit the two-digit format. A full comparison must preserve every character, including leading zeroes and the recorded salt. A salt does not expand the password's candidate space. The lesson is to choose unpredictable credentials and use storage designed to make each offline guess costly."
    ],
    "zones": [
      "Preserves PIN format",
      "Changes the input"
    ],
    "cards": [
      [
        "07 as two characters",
        "Preserves PIN format",
        "The leading zero is part of the secret string."
      ],
      [
        "7 as one character",
        "Changes the input",
        "Removing the zero changes the hash input."
      ],
      [
        "00 through 99 as two-character strings",
        "Preserves PIN format",
        "The list covers all 100 two-digit values."
      ],
      [
        "09 as a two-character string",
        "Preserves PIN format",
        "The leading zero is retained."
      ],
      [
        "42 as a two-character string",
        "Preserves PIN format",
        "The input fits the two-digit format."
      ],
      [
        "00 with both zeroes retained",
        "Preserves PIN format",
        "Both characters are part of the input."
      ],
      [
        "9 substituted for 09",
        "Changes the input",
        "Dropping the zero changes the input."
      ],
      [
        "A space added after the PIN",
        "Changes the input",
        "The extra character changes the hash input."
      ],
      [
        "Two-digit PIN converted to an unpadded integer",
        "Changes the input",
        "Leading zeroes can be lost during conversion."
      ]
    ]
  }
};

// Short, scenario-specific takeaways shown after completion.
const UnitTwoRecaps = {
  "201_0": [
    "You calculated the toy RSA modulus and totient, then replaced the predictable campus password.",
    "Familiar names and dates remain guessable even with a symbol.",
    "Use a password manager to create a long, unique password for each account."
  ],
  "201_1": [
    "You replaced the reused passwords on both exposed accounts.",
    "A leaked password can open every account where it was reused.",
    "Replace exposed passwords everywhere they were reused and store different replacements privately."
  ],
  "201_2": [
    "You repaired a policy that encouraged predictable monthly changes.",
    "Rules should support unique passwords rather than push people toward familiar patterns.",
    "Allow long passwords and manager autofill; change credentials when exposure is suspected."
  ],
  "202_0": [
    "You identified common-word guessing against one account.",
    "Attackers often try likely passwords before exploring every combination.",
    "Use a unique generated password and enable MFA; services should limit repeated login attempts."
  ],
  "202_1": [
    "You identified stolen login pairs being tried across accounts.",
    "Credential reuse lets one breach spread to other services.",
    "Avoid reuse, enable MFA, and review successful unfamiliar logins."
  ],
  "202_2": [
    "You compared two hash outputs and selected slow salted storage for exposed credentials.",
    "An attacker with copied hashes can guess without using the login page.",
    "Use unique passwords; service builders should use suitable slow salted password hashing and respond to breaches."
  ],
  "203_0": [
    "You enrolled a possession factor alongside a password.",
    "Two remembered secrets are still the same factor category.",
    "Add supported MFA and keep recovery methods protected."
  ],
  "203_1": [
    "You denied unexpected prompts, checked activity, and reported the attempt.",
    "Approving a prompt you did not initiate can grant attacker access.",
    "Deny unexpected requests and review account security through the official service."
  ],
  "203_2": [
    "You registered a service-bound passkey and protected recovery codes.",
    "Typed one-time codes can be relayed by a fake login page.",
    "Use supported passkeys or security keys and keep a protected recovery route."
  ],
  "204_0": [
    "You restored owner-controlled recovery and replaced exposed backup codes.",
    "Recovery can provide another route into an account.",
    "Protect the recovery mailbox, keep it current, and store backup codes privately."
  ],
  "204_1": [
    "You revoked the abandoned library session while keeping trusted access.",
    "Closing a browser window does not necessarily end a signed-in session.",
    "Sign out on shared devices and revoke sessions you no longer trust."
  ],
  "204_2": [
    "You removed compromised credentials, attacker sessions, and account persistence.",
    "A password reset alone may leave other attacker access intact.",
    "Use a trusted device to review sessions, recovery, forwarding, connected apps, and reused credentials."
  ],
  "205_0": [
    "You recovered the weak fictional registrar password by comparing its full salted hash.",
    "Recovering a password reveals weakness but does not secure the account.",
    "Treat confirmed exposure as an incident; replace the credential and inspect other access paths."
  ],
  "205_1": [
    "You replaced the registrar password and enrolled extra sign-in protection.",
    "A stronger sign-in does not remove sessions already held by an attacker.",
    "Use a unique credential with supported MFA, then continue reviewing active sessions and recovery."
  ],
  "205_2": [
    "You removed the attacker session and forwarding, restored recovery, and rotated backup codes.",
    "Containment must remove persistent access as well as fix sign-in.",
    "Review all account access paths and report the incident through the official support route."
  ],
  "250_0": [
    "You recovered a fictional common-word password using dictionary candidates.",
    "A public salt changes the hash but does not make a weak password unpredictable.",
    "Choose unique generated credentials; only test accounts and records you are authorized to assess."
  ],
  "250_1": [
    "You recovered a password built from a campus word, year, and suffix.",
    "A familiar format narrows the likely guesses.",
    "Avoid predictable personal or campus patterns and use a password manager."
  ],
  "250_2": [
    "You tested the two-digit PIN space while preserving leading zeroes.",
    "Every character changes the hash input; this tiny classroom space has only 100 possibilities.",
    "Use longer unpredictable credentials. Classroom timers do not estimate real cracking speed."
  ]
};
