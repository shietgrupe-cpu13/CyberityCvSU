# Unit 02 and Unit 05 lesson source review

Reviewed 12 October 2026. The twelve new lessons use the existing Unit 01
LessonScreen flow: task preparation pages, clickable Sources, a no-stakes check,
and return to the level. Each lesson has three pages, one for each lab task,
and two questions with explanations. No hearts or XP are changed by these checks.

Content is in UnitTwoLessons.kt and UnitFiveLessons.kt. LessonReferences.kt stores
publication details; lessonFor in LessonContent.kt routes all twelve level IDs.
Existing Unit 01 and Unit 06 lessons are preserved. Existing lab guides remain.

## Where the material came from

These are publicly accessible primary references from the publishing organizations,
not search-result summaries, anonymous tutorials or AI-generated citations.
OWASP's Cheat Sheet Series is an open-source project with a CC BY-SA 4.0 license.
The source set also includes government publications, Internet standards and
public vendor documentation; public access does not mean every source is
open-source software or has the same redistribution license. The lesson text,
fictional incidents and illustrations are original paraphrases, not reproduced
source articles. Publisher attribution and links appear in the app.

Each link below was opened and the relevant content checked. Older foundational
publications are labelled with their dates and used only for the stated concepts.
Living documents carry a review date rather than an invented publication date.

| Source / publisher | Checked material | Used in levels |
| --- | --- | --- |
| [NIST SP 800-63B-4 §3.1.1 (2025)](https://pages.nist.gov/800-63-4/sp800-63b/authenticators/#passwords) | Password policy, managers, compromise-driven changes | 201, 205 |
| [NIST SP 800-63B-4 Appendix A](https://pages.nist.gov/800-63-4/sp800-63b/passwords/) | Predictable patterns and length limitations | 201, 250 |
| [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) | Hashing vs encryption, salts, cost factors; raw SHA-256 unsuitable for password storage | 202, 250 |
| [MITRE T1110](https://attack.mitre.org/techniques/T1110/) | Online/offline brute-force mechanisms and mitigations | 202, 250 |
| [MITRE T1110.004](https://attack.mitre.org/techniques/T1110/004/) | Credential stuffing using breached account/password pairs | 201, 202 |
| [MITRE T1110.002](https://attack.mitre.org/techniques/T1110/002/) | Offline password cracking and hash comparison | 202, 205, 250 |
| [OWASP Multifactor Authentication](https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html) | Independent factor categories, push fatigue, recovery and phishing limitations | 203, 204 |
| [FIDO Alliance Passkeys](https://fidoalliance.org/passkeys/) | Public-key credentials, service binding, synchronized/device-bound passkeys | 203, 205 |
| [OWASP Session Management](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html) | Manual session expiration; limits of logout on browser close | 204 |
| [OWASP Forgot Password](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html) | Protected reset codes and account recovery after suspected compromise | 204, 205 |
| [Microsoft compromised-account recovery](https://support.microsoft.com/en-us/accounts-billing/manage/how-to-recover-a-hacked-or-compromised-microsoft-account) | Device safety, recovery, and account settings review (steps 1–3); used as a provider example | 204 |
| [MITRE T1114.003](https://attack.mitre.org/techniques/T1114/003/) | Unauthorized mailbox forwarding as retained collection | 204, 205 |
| [MITRE T1098](https://attack.mitre.org/techniques/T1098/) | Account manipulation to retain access | 205 |
| [IETF RFC 1034 (1987)](https://www.rfc-editor.org/rfc/rfc1034) | DNS resource records and resolver operation (§§3, 5); foundational, subsequently updated | 501, 504 |
| [IETF RFC 9293 (2022)](https://www.rfc-editor.org/rfc/rfc9293) | TCP endpoint/port fields and connections (§§2.2, 3.1) | 501 |
| [IANA service registry](https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml) | Registered domain and https ports; registry warns port registration is not an endorsement of traffic | 501 |
| [IETF RFC 8446 (2018)](https://www.rfc-editor.org/rfc/rfc8446) | TLS content protection and traffic-analysis limitations (§1, Appendix E) | 503, 504 |
| [IETF RFC 9525 (2023)](https://www.rfc-editor.org/rfc/rfc9525) | Intended service identity and certificate identifier matching (§§4–6); supersedes RFC 6125 | 503, 504 |
| [NIST SP 800-41 Rev. 1 (2009)](https://nvlpubs.nist.gov/nistpubs/legacy/sp/nistspecialpublication800-41r1.pdf) | Filtering/state tables (§2.1), policy (§4), rule specificity/order/testing (§§5.3–5.4) | 501, 505 |
| [NIST SP 800-153 (2012)](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-153.pdf) | WLAN segmentation (§2), passive monitoring, rogue devices and inventory correlation (§3) | 502, 504, 550 |
| [Apple router/access-point settings](https://support.apple.com/en-us/102766) | Current Security section: WPA3, WPA2/WPA3 transition, WPA2-AES, obsolete WEP/TKIP | 502 |
| [IETF RFC 826 (1982)](https://www.rfc-editor.org/rfc/rfc826) | ARP protocol-to-link address resolution | 504 |
| [Cisco Dynamic ARP Inspection](https://www.cisco.com/c/en/us/support/docs/switches/lan-switch-software/222274-troubleshoot-dynamic-arp-inspection-dai.html) | Validating ARP against trusted DHCP-snooping bindings and prerequisites | 504 |
| [IETF RFC 9190 (2022)](https://www.rfc-editor.org/rfc/rfc9190) | EAP-TLS 1.3 identity verification and certificate validation (§2.2, §§5.2–5.3, referencing RFC 5216) | 550 |
| [Netfilter Packet Filtering HOWTO §7](https://www.netfilter.org/documentation/HOWTO/packet-filtering-HOWTO-7.html) | Historical state-match documentation used only for connection-state definitions, not current deployment commands | 505 |

## Important interpretation checks

- NIST SP 800-153 predates WPA3. WPA3/transition content comes from the current
  Apple guidance, not from that older NIST publication.
- The lab's first-match model is explicitly scoped to that lab. NIST notes that
  firewall evaluation models differ; the lesson does not claim all firewalls
  behave identically. Netfilter is cited for connection state, not as a claim
  that every real chain's accept semantics match our fictional engine.
- Rogue DNS configuration is distinguished from poisoned recursive caches.
  ARP interception does not automatically decrypt validated TLS.
- Certificates authenticate service identifiers, not every claim a site makes.
  RFC 9525 is the current general service-identity reference; RFC 9190 refers back
  to RFC 5216 for EAP-TLS certificate rules.
- Passkeys are not equated with all possible MFA deployments. Typed codes can
  be phished. Recovery capabilities are service-specific; the fictional lab's
  backup codes are not presented as a Cyberity account feature.
- Raw SHA-256 and a two-digit PIN are deliberately weak classroom examples.
  The salt:input formula and 100-candidate count were checked against bundled
  password_security/hash-engine.js. No real credentials or external attack
  targets are introduced, and none of the lesson examples provide a live answer.
- CISA password URLs returned retrieval errors during review. They were excluded
  from the new reference set rather than marked verified. NIST's final 2025
  password publication was used rather than a draft or older revision. The
  nftables wiki returned repeated retrieval errors after its initial response;
  the reachable official Netfilter HOWTO was used for state definitions instead.

## Validation

Debug build and both LessonContentTest checks passed for all 22 lesson-enabled
levels (including the twelve new lessons). There are 36 new preparation pages and
24 new no-stakes questions. No cloud deployment is needed for bundled lessons.

LessonContentTest includes all six levels from each unit. It verifies that every
lab task has preparation, every page cites valid references, every included
reference is used, source IDs are unique, and quick-check answers/options are
valid with varied correct positions. This checks structural coverage; it does
not replace the manual source review above or claim automated fact verification.

Manual phone check: open Learn → Unit 02 / Unit 05 → each level → Read lesson;
check all three task pages, Sources links, both questions and explanations,
Back/Continue to lab, scrolling and larger text. Unit 01 uses the same UI.

# Unit 03 lesson source review

Reviewed 14 October 2026. Five lessons for levels 301–305 (Phishing & Social
Engineering), in UnitThreeLessons.kt, routed through lessonFor in
LessonContent.kt, with publication details added to LessonReferences.kt. Each
lesson has one page per lab task (5, 5, 5, 5 and 6 pages) and three no-stakes
questions. Level 350 (Sixty Seconds) is a recall quiz over 301–304 and has no
lesson, so the lesson can't hand over its answers.

All names, domains, amounts and incidents in the lessons are fictional and
deliberately different from the lab evidence. Key words are bolded, matching
the Unit 01 lessons.

| Source / publisher | Checked material | Used in levels |
| --- | --- | --- |
| [MITRE T1566](https://attack.mitre.org/techniques/T1566/) | Spearphishing vs. mass campaigns; mail sent from compromised legitimate accounts; email, service and voice sub-techniques | 301, 305 |
| [MITRE T1566.001](https://attack.mitre.org/techniques/T1566/001/) | Attachments disguised as documents; reliance on user execution | 301 |
| [MITRE T1036.007](https://attack.mitre.org/techniques/T1036/007/) | Double file extensions; hidden known extensions | 301 |
| [MITRE T1598](https://attack.mitre.org/techniques/T1598/) | Phishing for information by conversation or call; posing as a legitimate source; reconnaissance used during later targeting | 301, 303, 305 |
| [MITRE T1586.001](https://attack.mitre.org/techniques/T1586/001/) | Hijacked social media accounts used to reach trusting connections | 303 |
| [MITRE T1583.001](https://attack.mitre.org/techniques/T1583/001/) | Lookalike/homoglyph domains; tracking and pivoting on registration data; privacy services | 302, 305 |
| [MITRE T1091](https://attack.mitre.org/techniques/T1091/) | Malware on removable media, files renamed to look legitimate, Autorun; disable Autorun and limit removable media | 303 |
| [NIST TN 2276 (2023)](https://doi.org/10.6028/NIST.TN.2276) | Cues B.2.3 (URL hyperlinking), B.4.3 (requests for sensitive information), B.4.4 (urgency), B.3.2 (logo imitation); premise alignment | 301, 302 |
| [IETF RFC 5322 §§3.4–3.4.1](https://www.rfc-editor.org/rfc/rfc5322#section-3.4) | Display name, local-part and the domain that identifies where mail is delivered | 302 |
| [IETF RFC 9989 (May 2026)](https://www.rfc-editor.org/rfc/rfc9989) | DMARC validates the Author (From) domain via aligned SPF/DKIM (§§1, 3.2.2, 4.4); visually similar domains out of scope (§2.2). Obsoletes RFC 7489 | 301, 302 |
| [Unicode UTS #39](https://www.unicode.org/reports/tr39/) | Mixed-script and whole-script confusables (§4) | 302 |
| [Chromium IDN display policy](https://chromium.googlesource.com/chromium/src/+/main/docs/idn.md) | When Chrome shows a label as xn-- punycode (mixed scripts, whole-script confusables) | 302 |
| [IETF RFC 9083 (2021)](https://www.rfc-editor.org/rfc/rfc9083) | RDAP registration events, registrant/registrar entity roles, redacted contacts (§§3, 4.5, 5.1) | 305 |
| [FBI IC3 PSA I-091124-PSA (2024)](https://www.ic3.gov/PSA/2024/PSA240911) | Business email compromise through real accounts; verify requests through a secondary channel | 301 |
| [FBI IC3 PSA I-011822-PSA (2022)](https://www.ic3.gov/PSA/2022/PSA220118) | Tampered QR codes; check the URL after scanning; don't install apps from QR codes | 302, 305 |
| [CISA Recognize and Report Phishing](https://www.cisa.gov/secure-our-world/recognize-and-report-phishing) | Urgency, requests for information, lookalike addresses; typos no longer reliable; report, delete, don't reply; verify through a channel you already trust | 301, 302, 304, 305 |
| [FTC phishing (updated Oct 2024)](https://consumer.ftc.gov/articles/how-recognize-and-avoid-phishing-scams) | Payment-link lures; legitimate companies don't ask to update payment details by link; contact the company through a known number or site | 304, 305 |
| [FTC prize scams (2025)](https://consumer.ftc.gov/articles/fake-prize-sweepstakes-and-lottery-scams) | Real prizes are free; paying a fee to collect is a scam; pressure and requests for account details | 304 |
| [FCC caller ID spoofing (2020)](https://www.fcc.gov/consumers/guides/spoofing-and-caller-id) | Displayed numbers can be faked; hang up and call the number on your statement or official site | 303, 304, 305 |
| [ACMA SMS sender ID register](https://www.acma.gov.au/sms-sender-id-register) | Fake sender IDs; scam texts appear in the same thread as genuine ones | 304 |
| [UK Government Security Group: Tailgating (2024)](https://www.security.gov.uk/wp-content/uploads/2024/04/tailgating.pdf) | Following through doors, heavy-box and delivery/maintenance pretexts; challenge or report | 303 |
| [Tischer et al., IEEE S&P 2016](https://research.google/pubs/users-really-do-plug-in-usb-drives-they-find/) | 297 drives on a campus, 45–98% plugged in, first in under six minutes, altruism as motive | 303 |
| [Meta Newsroom (2018, updated 2019)](https://about.fb.com/news/2018/06/transparency-for-ads-and-pages/) | Page creation date, name changes and active ads | 305 |
| [GCash Help Center](https://help.gcash.com/hc/en-us/articles/19578518834457) | GCash never asks for MPIN/OTP; unexpected prizes and pressure as scam signs | 303, 304 |
| [BPI (15 Dec 2025)](https://www.bpi.com.ph/about-bpi/news/bpi-reminds-customers-how-to-spot-scams-this-holiday-season) | Banks don't ask for OTPs/passwords by phone or verify accounts via unsolicited calls; no clickable links by text | 304 |
| [RA 12010, AFASA (2024)](https://lawphil.net/statutes/repacts/ra2024/ra_12010_2024.html) | §4(b): obtaining financial-account credentials by deception, including electronic communications, is a social engineering scheme | 303 |
| [PNA: I-ARC hotline 1326](https://alpha.pna.gov.ph/articles/1207775) | CICC-led 24/7 hotline for reporting phishing, text scams and caller ID spoofing | 304 |

## Unit 03 interpretation checks

- RFC 7489 was obsoleted by RFC 9989 in May 2026; the newer RFC is cited.
- The Chromium punycode rule is described only for mixed-script and
  whole-script lookalikes. The lesson says same-alphabet swaps (rn for m) have
  nothing for that rule to catch, not that browsers never warn about them.
- RA 12010 is described at the level of its §4(b) text. The Act doesn't name
  OTPs specifically, so the lesson ties the OTP rule to GCash and BPI, not to
  the Act.
- The cisa.gov news page used by Unit 01 ("Avoiding Social Engineering and
  Phishing Attacks") and the NIST glossary returned retrieval errors on
  14 October 2026, so Unit 03 cites the reachable CISA Secure Our World page
  and the UK government tailgating guidance instead.
- The USB study's 45–98% is the paper's estimated range for drives connected,
  not the share of people who opened files.

Validation: the five lessons compiled against LessonContent/LessonReferences
with a standalone Kotlin compiler, and the LessonContentTest rules were
re-run for 301–305 outside Gradle. The full Android build and LessonContentTest
still need to run in Android Studio, followed by a phone check of each lesson.
