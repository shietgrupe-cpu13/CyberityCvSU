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
