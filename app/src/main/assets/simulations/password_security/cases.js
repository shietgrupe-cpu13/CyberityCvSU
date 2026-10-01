const passwordCases = {
  "201_0": {
    "id": 201,
    "index": 0,
    "title": "Strong Passwords",
    "caseTitle": "Predictable substitutions",
    "evidence": "A campus account uses CvSU2026! (9 characters). Its owner says the capital letters and symbol make it safe.",
    "guide": "Length and unpredictability matter together. A campus name, year, and predictable substitutions remain guessable. Generate a long random password for each account, or use a passphrase made from randomly selected words. Never use these classroom examples as real passwords. First calculate the classroom RSA values n = p × q and φ(n) = (p − 1) × (q − 1), using the randomized primes shown in the simulation. RSA encryption is different from password hashing. Then type a fictional replacement or use the generator, calculate its salted classroom hash, and paste the complete hash into the verification field before saving and receiving the case code.",
    "options": [
      "Change it to CvSU2027!",
      "Generate a unique 20-character random password",
      "Reuse a long password from another account"
    ],
    "correct": 1,
    "feedback": "The toy RSA calculation demonstrates encryption arithmetic; the separate long, unique password replaces the old campus-name credential."
  },
  "201_1": {
    "id": 201,
    "index": 1,
    "title": "Strong Passwords",
    "caseTitle": "Reuse in the vault",
    "evidence": "Continue the same portal audit. Task 1 replaced the predictable portal credential. The linked shopping account still uses the old exposed credential; replace that remaining reused value and protect both account credentials in the vault.",
    "guide": "Length cannot protect a reused credential once another service leaks it. A password manager helps maintain a different generated password for every account. Secure the vault itself with a strong master password and MFA, and use its official app.",
    "options": [
      "Keep it because it is long",
      "Add the same suffix on both sites",
      "Generate different passwords for both accounts"
    ],
    "correct": 2,
    "feedback": "A breach at one service now has no reusable password for the other."
  },
  "201_2": {
    "id": 201,
    "index": 2,
    "title": "Strong Passwords",
    "caseTitle": "Misleading policy",
    "evidence": "The portal forces a password change every month. Students keep rotating CampusJan! into CampusFeb! and CampusMar!.",
    "guide": "Routine expiry encourages predictable changes. Change a password when it is compromised or there is evidence of exposure. A better policy allows long passwords and paste/autofill, blocks common or breached values, and limits online guesses.",
    "options": [
      "Allow long passwords, block breached values, and change on compromise",
      "Require a new month suffix",
      "Disable password manager autofill"
    ],
    "correct": 0,
    "feedback": "The policy addresses guessability and compromise without training predictable rotations."
  },
  "202_0": {
    "id": 202,
    "index": 0,
    "title": "Password Attacks",
    "caseTitle": "Dictionary guessing",
    "evidence": "One username receives Campus2026!, Password1!, Welcome1!, and similar common guesses. All fail.",
    "guide": "Dictionary attacks try common words and predictable variations. Brute force explores combinations systematically. Both can guess online, where rate limits help, or offline against stolen hashes, where a website login limit no longer applies.",
    "options": [
      "Credential stuffing from known email/password pairs",
      "Dictionary guessing; block common passwords and rate-limit attempts",
      "Normal use; disable alerts"
    ],
    "correct": 1,
    "feedback": "The guess list is dictionary-based, and online throttling reduces repeated attempts."
  },
  "202_1": {
    "id": 202,
    "index": 1,
    "title": "Password Attacks",
    "caseTitle": "Leaked pairs",
    "evidence": "A login report shows thousands of different emails. Each gets one password from a leaked shopping-site credential list. Several succeed.",
    "guide": "Credential stuffing tests stolen username/password pairs on other services. A password can be long and still fail this way if reused. Unique credentials break that link; MFA adds another barrier. Investigate successful logins as possible compromises.",
    "options": [
      "Reset affected credentials, remove reuse, add MFA, and review sessions",
      "Require the same stronger password on every site",
      "Classify it as exhaustive brute force"
    ],
    "correct": 0,
    "feedback": "Stolen pairs explain the pattern; account-specific passwords limit the damage."
  },
  "202_2": {
    "id": 202,
    "index": 2,
    "title": "Password Attacks",
    "caseTitle": "Stolen database",
    "evidence": "The attacker has a copy of the password hashes and is testing guesses on their own computer. The portal blocks repeated login attempts.",
    "guide": "An offline attacker does not need the login page. Unique salts stop identical passwords producing identical stored values and defeat reusable precomputed tables. A suitable slow password hashing scheme raises the cost per guess. Salts do not make weak passwords strong. Before the challenge, calculate two classroom SHA-256 examples, change one character, and compare the full outputs. Then configure storage that raises the cost of offline guessing.",
    "options": [
      "Lower the portal login limit only",
      "Store passwords as plain text for easier resets",
      "Use salted, slow password hashing and reset exposed credentials"
    ],
    "correct": 2,
    "feedback": "Offline guessing needs strong storage and breach response, alongside long unique passwords."
  },
  "203_0": {
    "id": 203,
    "index": 0,
    "title": "Multi-Factor Authentication",
    "caseTitle": "Two secrets",
    "evidence": "A setup wizard proposes a password plus the answer to a secret question. It calls this two-factor authentication.",
    "guide": "Factors are something you know, have, or are. Two knowledge secrets are still one factor category. A password plus an authenticator or security key adds possession. A supported passkey uses a cryptographic credential bound to the genuine service.",
    "options": [
      "Keep password plus secret question as MFA",
      "Use a password plus an authenticator app or security key",
      "Use two different passwords"
    ],
    "correct": 1,
    "feedback": "A possession factor adds protection beyond another remembered secret."
  },
  "203_1": {
    "id": 203,
    "index": 1,
    "title": "Multi-Factor Authentication",
    "caseTitle": "Push fatigue",
    "evidence": "Continue the same student account after enrolling a possession factor. Its legacy approval channel receives six requests you did not initiate. A caller says to approve one to stop the notifications.",
    "guide": "Repeated prompts can pressure people into authorising an attacker. Deny requests you did not initiate, open the official account security page, review sessions, and report suspicious activity. Never give a caller a one-time code or backup code.",
    "options": [
      "Approve once to stop the prompts",
      "Tell the caller the current code",
      "Deny, review account activity, and report the attempt"
    ],
    "correct": 2,
    "feedback": "An unrequested prompt is an incident to investigate, not an approval to grant."
  },
  "203_2": {
    "id": 203,
    "index": 2,
    "title": "Multi-Factor Authentication",
    "caseTitle": "Phishing-resistant sign-in",
    "evidence": "Continue the same student account after denying the suspicious prompts. The portal still allows legacy typed-code sign-in, and a fake page can relay a fresh code. Register a service-bound passkey and protect recovery.",
    "guide": "MFA reduces many password attacks, but typed one-time codes can still be phished. Passkeys and FIDO security keys bind authentication to the legitimate service. Keep recovery codes in a protected place and maintain a recovery method; never share codes.",
    "options": [
      "Use a supported passkey or FIDO security key and protect recovery",
      "Assume every MFA method prevents phishing",
      "Disable MFA to simplify recovery"
    ],
    "correct": 0,
    "feedback": "Service-bound authentication resists the fake-page relay described in this case."
  },
  "204_0": {
    "id": 204,
    "index": 0,
    "title": "Account Protection",
    "caseTitle": "Recovery audit",
    "evidence": "The student portal recovery address is an abandoned mailbox. Backup codes are saved in a shared class folder.",
    "guide": "Recovery can bypass normal sign-in. Protect the recovery mailbox with its own unique password and MFA. Keep recovery information current, and store backup codes securely, such as in an encrypted vault or a protected offline location.",
    "options": [
      "Move codes to a public note",
      "Secure and update recovery, then replace exposed backup codes",
      "Disable all recovery methods"
    ],
    "correct": 1,
    "feedback": "Recovery now belongs to the account owner, and shared codes can no longer be used."
  },
  "204_1": {
    "id": 204,
    "index": 1,
    "title": "Account Protection",
    "caseTitle": "Shared computer",
    "evidence": "You closed the browser at the library without signing out. Account settings still lists an active library-computer session.",
    "guide": "Closing a window does not necessarily end a session. Sign out on shared devices, avoid saving credentials, and revoke sessions you cannot trust from the official security page. Private browsing is not protection against a compromised device.",
    "options": [
      "Trust the closed window",
      "Change only the profile picture",
      "Revoke the library session and sign out on shared devices"
    ],
    "correct": 2,
    "feedback": "Revocation removes the active session rather than relying on a closed browser window."
  },
  "204_2": {
    "id": 204,
    "index": 2,
    "title": "Account Protection",
    "caseTitle": "Breach response",
    "evidence": "After repairing recovery and closing the library session on this same account, a subsequent unfamiliar login changes the recovery phone and adds an outside forwarding rule. Contain this confirmed breach.",
    "guide": "Use a trusted device and the official service to regain control. Change compromised passwords, revoke sessions, remove unknown recovery methods, inspect forwarding and connected apps, and contact support if locked out. Review other accounts where the credential was reused.",
    "options": [
      "Reset credentials, revoke sessions, restore recovery, remove forwarding, and report",
      "Reset the password and ignore account settings",
      "Reply to the suspicious login email with your password"
    ],
    "correct": 0,
    "feedback": "The response removes persistence as well as the stolen credential."
  },
  "205_0": {
    "id": 205,
    "index": 0,
    "title": "Password Security Challenge",
    "caseTitle": "Recover the registrar credential",
    "evidence": "The registrar.training@campus.example mailbox has an unfamiliar desktop session and a forwarding rule to an outside address. Its recovered database record contains a salted hash of a common-word password. Investigate that same account through all three tasks.",
    "guide": "Use dictionary candidates or individual guesses to recover the matching password from this account's salted hash. Compare the full digest yourself. This is the investigation step; a recovered weak password still needs replacement, MFA, and containment.",
    "feedback": "The weak registrar credential is confirmed. Continue to replace it and enrol sign-in protection for this same account."
  },
  "205_1": {
    "id": 205,
    "index": 1,
    "title": "Password Security Challenge",
    "caseTitle": "Secure the registrar sign-in",
    "evidence": "Continue the same registrar incident. The recovered password is still in use and the account has no MFA. Replace the credential and enrol another sign-in protection.",
    "guide": "Password recovery proves the weakness but does not fix it. Generate a unique long replacement for this account and enrol a passkey or authenticator. A new password alone does not remove an attacker's existing sessions or recovery methods.",
    "feedback": "The registrar credential and sign-in protection are repaired. Continue to remove the attacker session and mailbox persistence."
  },
  "205_2": {
    "id": 205,
    "index": 2,
    "title": "Password Security Challenge",
    "caseTitle": "Contain the registrar incident",
    "evidence": "Finish the same registrar incident: an unfamiliar desktop session remains active, attacker forwarding is enabled, and recovery points to an unknown mailbox. Backup codes may have been exposed.",
    "guide": "Revoke attacker sessions, remove their forwarding and recovery changes, replace exposed backup codes, and report the incident. Keep the owner's trusted session. Review other accounts where the weak credential was reused.",
    "feedback": "The registrar account is secured: credential replaced, MFA enrolled, attacker session revoked, forwarding removed, recovery restored, backup codes rotated, and incident reported."
  },
  "250_0": {
    "id": 250,
    "index": 0,
    "title": "Hash Cracking Practice",
    "caseTitle": "Dictionary lab",
    "evidence": "A fictional account has a salted SHA-256 hash. Its password is one entry in the supplied common-password word list.",
    "guide": "A hash is not decrypted. Hash each candidate with the displayed salt, compare the digest with the target, and recover the matching candidate. This is offline dictionary guessing: login throttling cannot stop work against a copied hash.",
    "feedback": "The dictionary matched a weak credential. Replace it with a unique generated password and use MFA.",
    "remediation": []
  },
  "250_1": {
    "id": 250,
    "index": 1,
    "title": "Hash Cracking Practice",
    "caseTitle": "Pattern lab",
    "evidence": "A fictional campus password uses one supplied campus word, a year near the current year, and ! or @. The salt and target change on every attempt.",
    "guide": "Use the campus-pattern generator rather than unrelated words. The current year helps generate likely guesses, but the salt and hash must be included exactly. A salt prevents reuse of precomputed digests; it does not rescue a predictable password.",
    "feedback": "The campus pattern was recovered. Predictable dates and suffixes remain guessable even with a salt.",
    "remediation": []
  },
  "250_2": {
    "id": 250,
    "index": 2,
    "title": "Hash Cracking Practice",
    "caseTitle": "Brute-force lab",
    "evidence": "A fictional training PIN contains exactly two digits, from 00 through 99. Its salted target hash is newly generated for this attempt.",
    "guide": "Brute force enumerates every value in a defined search space. This deliberately tiny PIN has only 100 candidates. Longer unpredictable secrets have much larger spaces. Raw SHA-256 is fast and used here to demonstrate weak storage; production password storage needs an appropriate slow password hashing scheme.",
    "feedback": "All 100 two-digit combinations fit a tiny search space. Longer unique passwords and slow salted storage raise guessing cost.",
    "remediation": []
  }
};
