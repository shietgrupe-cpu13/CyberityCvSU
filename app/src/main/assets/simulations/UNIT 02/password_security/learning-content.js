const UnitTwoLearning = {
  "201_0": {
    "reading": [
      "Why predictability matters",
      "A password is a secret used to prove control of an account. Attackers do not start by trying every possible string. They first try names, common words, dates, and patterns that people often choose. A campus name followed by the current year and an exclamation mark follows exactly that kind of pattern. Adding a symbol does not remove the predictable parts.",
      "Length gives a randomly generated password more possible combinations, while unique credentials stop a breach at one service from opening another. In this exercise, enter or generate a unique fictional replacement of at least 20 characters, confirm it, save it, and test sign-in for Maya's portal account. That length is an exercise target, not a guarantee of safety: a long reused or phished password can still be stolen."
    ],
    "outcome": [
      "Predictable patterns versus generated secrets",
      "The campus-name example is predictable because it combines public context with a familiar suffix. A random generated secret is harder to guess because its characters were not chosen from personal information. A long credential reused elsewhere still carries a reuse risk. The lesson is to combine length, unpredictability, uniqueness, and safe handling."
    ]
  },
  "201_1": {
    "reading": [
      "Why reuse spreads a breach",
      "An account password can be strong against guessing and still fail after another service leaks it. When someone reuses the same credential, the attacker no longer needs to guess: they can test the leaked email and password at other services. This is why two long identical passwords are not two independent protections.",
      "A password manager can generate and store a different credential for each account. Protect the manager itself with strong authentication and maintain a recovery method. Change the affected credential at the breached service and at every account where it was reused; changing only one side leaves the other exposed."
    ],
    "outcome": [
      "Breaking the link between accounts",
      "The reused credential lets the same leaked value cross from shopping to the portal. The unique credential breaks that exact link, although it still needs protection against phishing and other theft. Generating different replacements for both exposed accounts removes the old reusable value; storing them privately helps prevent a new exposure."
    ]
  },
  "201_2": {
    "reading": [
      "Password rules that help people",
      "A useful password policy makes secure behaviour easier. It permits long values and password manager paste or autofill, rejects commonly used or breached passwords, and limits online guessing. Requiring a new month in a password often encourages a predictable rotation rather than a genuinely new secret.",
      "A reset is appropriate when compromise is suspected or confirmed. Routine changes alone do not revoke stolen sessions or repair an account's recovery settings. Policy controls and incident response solve different problems, so a good system needs both."
    ],
    "outcome": [
      "Why the repaired policy works",
      "Long-password support expands the available search space; a blocklist rejects known weak choices; autofill supports unique generated values. Predictable monthly suffixes are easy to anticipate. Resetting on compromise responds to actual exposure and should be accompanied by review of sessions and recovery when an incident occurred."
    ]
  },
  "202_0": {
    "reading": [
      "Recognising how a password is attacked",
      "Dictionary guessing tries likely words and common variations. Brute force systematically enumerates combinations within a chosen format. Credential stuffing tests username and password pairs already exposed elsewhere. These techniques are distinguished by the source of the candidates, not just the number of failed logins.",
      "The case file shows several familiar password patterns against one account. That supports dictionary guessing. Limiting online attempts can slow repeated login requests, but it does not secure a stolen password database. You must identify the attack setting before choosing a defence."
    ],
    "outcome": [
      "Reading the source of the guesses",
      "Common-word variations belong to dictionary guessing. Enumerating every two-digit value is a small brute-force search. Testing a known leaked email/password pair elsewhere is credential stuffing. A correct classification helps select controls, while the surrounding evidence tells you whether any login actually succeeded."
    ]
  },
  "202_1": {
    "reading": [
      "Recognising stolen credential pairs",
      "When many different usernames receive a known password from a leak, the attack may be credential stuffing. The attacker is reusing information that was already stolen rather than discovering a new secret independently. Successful logins in that pattern must be investigated as possible compromises.",
      "Unique credentials reduce spread between services. MFA adds a further barrier, and incident response must review successful sessions and account changes. Login alerts are useful evidence, but receiving an alert does not remove the attacker's access."
    ],
    "outcome": [
      "Why a leaked pair is different from a guess",
      "A previously leaked email/password combination is a credential-stuffing input. A list of common words is dictionary input. Exhaustively enumerated combinations are brute-force input. Strong length alone cannot save a credential that is already known, so repair requires removing reuse and examining successful access."
    ]
  },
  "202_2": {
    "reading": [
      "Understanding hashes before you crack them",
      "A hash function transforms an input into a digest. SHA-256 produces a 256-bit digest, displayed here as 64 hexadecimal characters. Repeating exactly the same input with the same function produces the same digest. Even a small change to the input generally changes many positions in the output.",
      "A password hash is not a secret message that you decrypt. In an offline attack, you hash a candidate with the same recorded salt and compare the digest with the stored target. A salt is public, account-specific input that stops a single precomputed digest being reused across differently salted records; it does not make a short predictable password strong."
    ],
    "outcome": [
      "What the two digests demonstrate",
      "The outputs have the same length even though the input changed. The changed positions demonstrate why you must compare the complete digest and use the exact input, including the salt format. Fast SHA-256 is used here to make the lesson visible; real account storage needs an appropriate slow salted password hashing scheme. Offline guessing is unaffected by a login-page attempt limit."
    ]
  },
  "203_0": {
    "reading": [
      "Understanding authentication factors",
      "A factor describes the kind of evidence an account uses: something you know, something you have, or something you are. A password and a secret-question answer are both knowledge secrets. Asking for two knowledge secrets does not create a possession factor.",
      "An authenticator app or security key can demonstrate possession of an enrolled device or credential. The training proof below is only a simulation of enrolment; real services use their own enrolment and verification process. Protect recovery because it may offer another route into the account."
    ],
    "outcome": [
      "Why the second factor matters",
      "The password belongs to knowledge, an authenticator belongs to possession, and a fingerprint is a biometric example. The added factor means a stolen password alone may not be sufficient. It does not remove the need for safe recovery, careful approvals, or protection against phishing."
    ]
  },
  "203_1": {
    "reading": [
      "Unexpected approval prompts are evidence",
      "An MFA approval should correspond to a sign-in you started. Several prompts that arrive while you are doing nothing may indicate that someone knows the first factor and is trying to get you to approve their login. A caller asking you to approve or share a code is not proof of legitimacy.",
      "Deny the requests, inspect the account through the official service, and report the suspicious activity. Do not approve just to stop notifications. If you discover an unfamiliar successful session, contain it and review the credential and recovery paths."
    ],
    "outcome": [
      "Why denying is only the first step",
      "Denying the requests blocks the approvals you did not initiate. Reviewing history can show whether a separate login succeeded. Reporting helps the incident be investigated. Approving an unexpected request or sharing a code can provide the attacker with the evidence they need."
    ]
  },
  "203_2": {
    "reading": [
      "Understanding phishing-resistant sign-in",
      "A typed one-time code can be stolen by a fake page and relayed while it is still valid. This is one reason MFA methods differ in their resistance to phishing. A passkey or FIDO security key binds authentication to the genuine service rather than simply asking you to type a reusable response into any page.",
      "A passkey still needs safe enrolment and recovery. Only enrol through the official service, keep a supported recovery path, and protect any backup codes. The simulator registers a fictional credential for a fictional portal; it does not change your real device credentials."
    ],
    "outcome": [
      "What service binding adds",
      "The passkey and FIDO key use a credential associated with the service. A typed authenticator code can still be relayed through a phishing page. Protecting backup codes closes a recovery weakness that the primary sign-in method alone does not address."
    ]
  },
  "204_0": {
    "reading": [
      "Recovery is another door into an account",
      "A recovery mailbox, phone number, or backup code can restore access when normal sign-in fails. That makes recovery useful to the owner and valuable to an attacker. An abandoned mailbox or backup codes posted in a shared folder may bypass protections that otherwise work well.",
      "Keep recovery methods current and under your control. Protect the recovery mailbox with a unique credential and MFA. When backup codes were exposed, replace them so the old copies no longer work, and store the replacements privately."
    ],
    "outcome": [
      "Why moving a file is not enough",
      "A private replacement code is useful only after the exposed original is invalidated. A protected current recovery mailbox keeps the alternate access path with the owner. A shared folder exposes the secret to other people, and an abandoned address may no longer be under reliable control."
    ]
  },
  "204_1": {
    "reading": [
      "A browser window is not a session",
      "A session represents authenticated access after sign-in. Closing the visible browser window does not necessarily tell the server to end that access. A shared computer may retain cookies or saved credentials, so the owner should sign out and avoid saving their credential there.",
      "Account settings can list active sessions and offer revocation. Use the device and activity evidence to distinguish a trusted owner session from a shared or unfamiliar one. Preserve the owner's trusted access while removing the session that should no longer be active."
    ],
    "outcome": [
      "Why the session decision follows evidence",
      "The personal phone is identified as trusted owner access. The abandoned library session and unfamiliar desktop need removal in these case facts. Revoking a session acts on server-side access; merely closing a tab or browser is not reliable containment."
    ]
  },
  "204_2": {
    "reading": [
      "Removing attacker persistence",
      "An attacker who successfully signs in may do more than read data. They may add a recovery method, create a forwarding rule, or leave an authenticated session active. These changes can preserve access or leak future messages even after the password is changed.",
      "Use a trusted route to replace exposed credentials and revoke attacker sessions. Inspect recovery and connected applications, remove forwarding changes, and report the incident. Review other services where the same credential was reused. If you cannot regain access, use the official support process rather than instructions from a suspicious message."
    ],
    "outcome": [
      "Why one repair is not enough",
      "A password reset removes a known credential but does not necessarily end existing sessions. Revocation closes session access. Repairing recovery and forwarding removes separate persistence and leakage paths. Reporting and reuse review help investigate the incident beyond the single account."
    ]
  },
  "205_0": {
    "reading": [
      "Investigate before repairing",
      "This capstone follows one fictional registrar mailbox across three tasks. The first task gives you the salt and hash from its weak credential. Your job is to discover which candidate produces the same digest, not to decrypt a hidden message. You can test one guess at a time or calculate a bounded candidate list.",
      "Read the record's formula carefully: this exercise hashes the salt, a colon, and the candidate in that order. Compare every character of the candidate digest with the target. The tool never labels or highlights the correct row, and calculating a guess does not itself complete the task."
    ],
    "outcome": [
      "Why evidence comes before a fix",
      "A successful comparison establishes that the supplied weak candidate matches this fictional record. It does not demonstrate that the account is now safe. The public salt is needed to reproduce the hash input; the digest is the comparison target; the candidate is the guessed input. The confirmed weakness becomes the reason to replace the credential and improve authentication."
    ]
  },
  "205_1": {
    "reading": [
      "Secure the same registrar sign-in",
      "The investigation confirmed a weak password on the registrar mailbox. Changing an unrelated account would not close this incident, so the simulator carries the same account and incident identifier into this task. Generate a unique replacement and enrol another sign-in protection.",
      "A supported passkey provides service-bound authentication. An authenticator app adds a possession factor when used alongside the password. Either still requires careful recovery and session handling. The attacker may already have an active session, so do not assume a new credential alone finishes the response."
    ],
    "outcome": [
      "What was fixed, and what remains",
      "The unique generated credential repairs the weak known secret, and the enrolled sign-in method strengthens future authentication. Existing sessions, forwarding rules, and recovery settings are separate access paths. The next task addresses them on this same mailbox."
    ]
  },
  "205_2": {
    "reading": [
      "Contain and explain the registrar incident",
      "The final task retains the owner phone session while removing the unfamiliar desktop session. It also removes attacker forwarding, restores the owner's protected recovery mailbox, invalidates exposed backup codes, and records the training incident report. These actions target distinct ways an attacker can remain connected.",
      "Containment should follow the evidence. Do not revoke trusted access simply because another session was suspicious. A good incident report explains the observed weakness, the matching evidence, the repairs performed, and remaining uncertainty. The simulator records the training workflow locally; it does not send a report to a real person."
    ],
    "outcome": [
      "The complete chain of reasoning",
      "Hash comparison confirmed a weak registrar credential. A generated replacement and MFA strengthened sign-in. Revoking the unfamiliar session removed ongoing access; forwarding removal stopped that leakage path; protected recovery and rotated backup codes removed alternate access paths. The trusted owner session remained. These are complementary repairs, not interchangeable buttons."
    ]
  },
  "250_0": {
    "reading": [
      "Dictionary practice: hash, compare, decide",
      "Dictionary guessing begins with a list of likely words. In this round, the fictional password is one entry in the supplied classroom list. The record changes whenever you generate a new attempt, so memorising a previous answer will not solve the next record.",
      "The salt is public and must be included exactly. Use an individual guess if you want to understand one comparison, or load and edit the list to compare several. The tool calculates every selected candidate with a neutral appearance. You decide which full digest equals the target and submit that candidate."
    ],
    "outcome": [
      "What a dictionary result proves",
      "A matching digest shows that this supplied candidate reproduces the fictional record. The attack worked because the password came from a short likely-word list. A public salt changes each record's digest, but the weak word can still be found by computing guesses for that salt. Unique generated credentials and suitable slow storage address different parts of the risk."
    ]
  },
  "250_1": {
    "reading": [
      "Pattern practice: use context without guessing blindly",
      "People sometimes build credentials from campus words, years, and familiar symbols. An attacker can prioritise that pattern instead of enumerating all possible text. This exercise supplies four words, three years around the reference year, and two suffixes, giving 24 candidates.",
      "The current time and a random nonce change the training target between attempts. That variation is for replay; it is not a production password-generation recommendation. The hashing function remains SHA-256, and the recorded salt determines the input used for comparisons."
    ],
    "outcome": [
      "Why context narrows a search",
      "The campus pattern reduces the supplied search to 4 × 3 × 2 possible candidates. That count depends on the classroom format, not on a universal cracking-time estimate. Changing a year or adding a common suffix does not create the unpredictability of a generated random secret."
    ]
  },
  "250_2": {
    "reading": [
      "Brute-force practice: count the search space",
      "Brute force enumerates combinations in a specified format. A two-digit training PIN has 10 possibilities for each position, or 100 values from 00 to 99. Leading zeroes matter: the string 07 is not the same hash input as the string 7.",
      "This deliberately small exercise makes the whole search visible. Real costs depend on the input distribution, hardware, and password storage scheme, so the timer and animation are not estimates of real cracking speed. Longer unpredictable secrets and slow salted hashing make the corresponding work much larger or more expensive."
    ],
    "outcome": [
      "What the small PIN teaches",
      "Only 100 values fit the two-digit format. A full comparison must preserve every character, including leading zeroes and the recorded salt. A salt does not expand the password's candidate space. The lesson is to choose unpredictable credentials and use storage designed to make each offline guess costly."
    ]
  }
};

const UnitTwoRecaps = {
  "201_0": [
    "You replaced Maya's predictable campus password with a unique generated credential.",
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
