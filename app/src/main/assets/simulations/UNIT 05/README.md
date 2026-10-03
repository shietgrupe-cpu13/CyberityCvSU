# Unit 05 — Network Security

## Repository findings and topic decision

`LearnScreen.kt` already specifies Network Security and the six level IDs below.
Unit 05 previously had curriculum placeholders, no registered content and no
simulation assets. Unit 06 already implements Incident Response, so this unit
bridges malware prevention to network defenses rather than replacing incident
response with a new final-unit topic. No separate capstone or topic specification
was found in the repository.

The architecture was checked against the Level 0 tutorial, Units 01–04, the
Unit 2 walkthrough/password workspace, the shared models and lab screen, the
Learn tab's navigation and progression, progress/cache repositories, XP and
hearts utilities, theme definitions, manifest and simulation resources.

## Architecture inherited from Units 0–04

- `HomeActivity.kt` hosts the Compose Learn tab; `LearnScreen.kt` represents units
  with `LearningUnit`/`LearningLevel` and dispatches `contentFor(levelId)`.
- The tutorial and investigation labs use `LabDefinition`, `LabTask` and the
  generic Compose `LabScreen`; no per-unit Activity, Fragment, XML screen or
  ViewModel is needed. Unit 1 also has a scenario quiz. Unit 2 has an introductory
  walkthrough and specialized local password interactions.
- Lab flow is briefing → taught tasks → investigation in a WebView sheet →
  evidence-gated submission → correct/incorrect feedback → next task → results.
  Choices, normalized text and hashed flags are shared answer types.
- `AndroidLab.notifyClueFound` reports evidence on the main thread. Required
  clues unlock the answer. Dangerous clues cost a heart once per attempt and do
  not count as evidence. Incorrect answers cost hearts; hints spend XP. Shared
  result scoring includes completion and evidence/no-hint/no-heart bonuses.
- Back/exit confirmation, the animated simulation sheet, progress indicators,
  task cards, buttons, typography, settings and completion UI remain owned by
  the shared screen. The workspace uses the existing Terminal Teal light/dark
  palette and `AndroidLab.theme()`; browser text scaling remains shared.
- Content is bundled Kotlin plus local HTML/CSS/JavaScript. No Firestore content
  fetch or new Android permission is required. Example domains and documentation
  IPs are evidence text, never remote navigation or network requests.
- Attempt state/evidence lives in the shared screen and current WebView. Like
  Unit 06, each lab also stores tool inspection, selected settings, verification
  and replay counts in `sessionStorage`, with `window.name` as the file-origin
  fallback. Closing the simulation sheet and navigating between tools retains
  work. Only a new lab entry (`desk.html?fresh=1`) resets the workspace; ordinary
  Console links preserve it. As in the existing engine, in-progress attempts are
  not restored after process death.
- Completion, per-level XP, hint spending and hearts use the existing
  `userProgress/{uid}` document and per-user SharedPreferences cache. Replays
  award no extra XP. The curriculum orders unlocks by completed IDs, including
  405 → 501 and 505 → 601. The existing `DEV_UNLOCK_ALL_LEVELS` display switch
  remains enabled in this repository.

## Implemented learning path

| Level | Workspace | Three investigated tasks |
| --- | --- | --- |
| 501 Network Basics | Campus Packet Desk | DNS and routing; source/destination ports; narrow portal-access repair |
| 502 Wi-Fi Security | Library Wi-Fi Audit | WEP/open/WPA2 comparison; mixed-fleet WPA2/WPA3 upgrade; guest isolation |
| 503 Safe Browsing | Sandbox Browser Bench | HTTP vs HTTPS visibility; hostname mismatch; valid TLS on a phishing domain |
| 504 Network Threats | Campus Network Watch | Passive sniffing; forged gateway ARP mapping; unauthorized resolver repair |
| 550 Cyber Challenge | Rogue Access Point Challenge | Radio/inventory correlation; evidence-backed report; official profile reconnect |
| 505 Network Security Simulation | Firewall Operations | Destination-scoped exceptions; first-match shadowing; stateful return-path proof |

Each stage has inspectable evidence and a replay. Repair tasks allow failed
configurations to be tested and adjusted. Replays show individual service and
security outcomes; an answer unlocks only after all records were inspected and
the replay passed. The final firewall report uses the shared hashed flag answer.
Browser identity bypasses and rogue-portal sign-in demonstrate the shared heart
penalty without requesting or transmitting real credentials.

Following Unit 06, **each lab folder owns its own `script.js`, `styles.css`,
console home and named tool pages**. The script contains that lab's fictional
evidence, control options, policy evaluation, state and UI/bridge behavior. There
is no shared `network_security` asset dependency. `NetworkSecurityLab.kt` supplies
teaching, objectives, steps, hints, answers and feedback.

| Folder | Console | Tool pages |
| --- | --- | --- |
| `network_basics` | `desk.html` | `trace.html`, `ports.html`, `repair.html` |
| `wifi_security` | `desk.html` | `audit.html`, `upgrade.html`, `guests.html` |
| `safe_browsing` | `desk.html` | `traffic.html`, `certificate.html`, `identity.html` |
| `network_threats` | `desk.html` | `capture.html`, `gateway.html`, `resolver.html` |
| `rogue_ap` | `desk.html` | `survey.html`, `report.html`, `reconnect.html` |
| `firewall_ops` | `desk.html` | `policy.html`, `order.html`, `verify.html` |

Consoles show completed tools and replay counts. The packet desk has an
interactive hop walk and source/destination connection table. The wireless
controller shows client compatibility and guest-route outcomes. The browser
bench presents an address bar, certificate identity and observer evidence.
Network Watch compares trusted/observed values and updates the threat path.
The radio desk shows signal measurements beside inventory and uplink evidence.
Firewall Operations renders the live rule list and per-packet matching verdicts,
allows removal of the broad shadowing allow and issues a consolidated report.
The rogue report/reconnect require preceding verified work, and the firewall
report requires both earlier tools verified. Changing a configuration clears
its current workspace verification and requires another replay.

## Fluid case flow and simulated people

The investigation flow also follows Unit 01's reported campus incidents and
Unit 06's conversations, action consequences and persistent shift state:

- Maya Santos (library assistant), Jun Bautista (library technician), Paolo
  Reyes (student) and Ana Mendoza (ITSO analyst/reviewer) are fictional
  teammates. Each tool starts with a relevant report. Optional reply choices
  explain symptoms and operational constraints; replay results trigger a
  specific success/failure response. Conversations are local, never sent.
- A sticky case-status bar shows evidence progress, whether checks are running
  or verified, and the next action. The next-unread-evidence control opens one
  record at a time. Console resume links follow actual verified progress.
- Replays reveal check results in short steps and support Show All Results.
  Reduced-motion users get immediate results. Verification is emitted only
  after the entire check set finishes; skipping completes it exactly once.
- Changing a setting or leaving a tool cancels unfinished timers. A dangerous
  action cancels pending verification. Editing an earlier firewall policy or
  radio finding invalidates dependent local approvals, so the downstream
  report/reconnection needs verification again.
- The rule list supports moving rules and removing the broad allow. Custom
  orders are evaluated from actual first matches; the final deny stays pinned
  last. Presets remain available for comparison.
- A bounded shift journal retains the last twelve replay outcomes with a
  simulated shift time, tool and checks. The return-to-task handoff is explicit
  and does not replace the shared Android answer/progression flow.
- Existing light/dark mode and font scaling remain supported. Mobile layouts
  wrap controls and keep wide tables inside their own scrolling region. Keyboard
  focus, descriptive rule-control labels, live case status and reduced-motion
  styling support accessible interaction.

## Content references

Technical explanations were checked against primary guidance:

- [Mozilla: How HTTPS protects you, and its limits](https://blog.mozilla.org/en/firefox/https-protect/)
- [Mozilla: Secure connection failed](https://support.mozilla.org/en-US/kb/Secure%20Connection%20Failed)
- [NSA: WPA3 technical report](https://www.nsa.gov/portals/75/documents/what-we-do/cybersecurity/professional-resources/ctr-cybersecurity-technical-report-wpa3.pdf?lang=en&v=1)
- [CISA: Wi-Fi security practices](https://www.cisa.gov/sites/default/files/2023-04/emergency-services-sector-cybersecurity-best-practices-factsheet_042023_508.pdf)

The mixed-fleet transition policy, hostnames, radio inventory, switch bindings
and packet rules are explicit classroom scenario assumptions. WPA3-only is a
later migration goal; transition mode retains WPA2 limitations. Valid HTTPS
protects content in transit without guaranteeing an honest website. A
certificate error alone does not prove interception. Radio names/BSSIDs require
independent corroboration. Being in a traffic path does not automatically break
valid TLS.

## Verification

- `NetworkSecurityContentTest`: all six dispatches, entry pages, teaching fields,
  required clues, choices, final flag hash and progression/state reconstruction.
- `UnitSimulationAssetsTest`: Unit 05 joins the existing local-page/dependency
  checks covering the other units.
- `node --test --test-isolation=none app/src/test/javascript/network-security.test.cjs`: exhaustive
  offered configuration combinations, packet exposures, rule-order shadowing,
  established replies, all 18 workspace bridge flows, fresh attempts and
  dangerous actions, local JS/CSS/page dependencies, navigation state, storage
  fallback, fresh-attempt reset, dedicated tool actions and connected-case
prerequisites. The DOM harness checks behavior without a browser dependency.
- Android test task: `:app:testDebugUnitTest --offline` with Android Studio's JBR.

Browser/emulator visual verification is still needed on a device: no browser
surface was connected in this session. Check narrow screens, large text, both
themes, simulation close/reopen, exhausted hearts, exit/retry, completion and
replay in the real app. These flows use the existing shared engine.
