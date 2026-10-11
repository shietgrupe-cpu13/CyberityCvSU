package com.cyberity.cvsu

import androidx.compose.runtime.Immutable

// ===========================================================================
// LESSONS — the reading shown before a level
// ===========================================================================
// A lesson teaches the ideas a level then tests. Each page names the lab task
// it prepares the student for, and cites the sources it was paraphrased from,
// so every point can be traced: source → lesson page → lab task. A short
// check at the end has nothing at stake: no hearts, no XP.

/** A published reference a lesson page was written from. */
@Immutable
data class LessonSource(
    val id: String,
    /** Short name shown under each page, e.g. "CISA". */
    val short: String,
    /** Full citation shown on the sources page. */
    val citation: String,
    val url: String
)

/** Picture at the top of a page. Mapped to an icon by LessonScreen. */
enum class LessonIcon { PHISHING, WARNING, EMAIL, LINK, CODE, REPORT, SHIELD, SEARCH, LOCK }

@Immutable
data class KeyTerm(val term: String, val meaning: String)

/** The illustration on a page — an example to look at rather than read about. */
@Immutable
sealed interface LessonVisual {

    /** A mock email, with the warning signs listed underneath. */
    data class Email(
        val senderName: String,
        val senderAddress: String,
        val subject: String,
        val body: String,
        val button: String?,
        val warningSigns: List<String>
    ) : LessonVisual

    /** An email address split into the part anyone can type and the part that counts. */
    data class Address(
        val displayName: String,
        val username: String,
        val domain: String
    ) : LessonVisual

    /** What a link says against where it goes, plus what the padlock does and doesn't mean. */
    data class Link(
        val shownText: String,
        val realDestination: String
    ) : LessonVisual

    /** A few lines of page source. Comment lines are drawn dimmed. */
    data class Source(val lines: List<String>) : LessonVisual

    /** What to do, and what not to. */
    data class DoDont(val dos: List<String>, val donts: List<String>) : LessonVisual

    /** A few log or alert lines. [highlights] are 0-based line numbers drawn in the accent colour. */
    data class Log(
        val title: String,
        val lines: List<String>,
        val highlights: List<Int> = emptyList(),
        val caption: String
    ) : LessonVisual
}

@Immutable
data class LessonPage(
    val title: String,
    val icon: LessonIcon,
    val paragraphs: List<String>,
    /** Ids into [Lesson.sources]. */
    val sourceIds: List<String>,
    /** 1-based lab task this page prepares for. Null for an introduction. */
    val task: Int? = null,
    val visual: LessonVisual? = null,
    val keyTerms: List<KeyTerm> = emptyList()
)

/** One question in the end-of-lesson check. Nothing is lost for a wrong answer. */
@Immutable
data class LessonQuestion(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    /** Shown once answered, right or wrong. */
    val explanation: String
)

@Immutable
data class Lesson(
    val levelId: Int,
    val title: String,
    val pages: List<LessonPage>,
    val sources: List<LessonSource>,
    val questions: List<LessonQuestion>
)

/** The lesson for a level, or null when that level has none yet. */
fun lessonFor(levelId: Int): Lesson? = when (levelId) {
    101 -> inboxTriageLesson()
    102 -> threatConsoleLesson()
    103 -> ciaTriadLesson()
    104 -> hardeningReviewLesson()
    106 -> riskRegisterLesson()
    601 -> securityIncidentsLesson()
    602 -> detectionLesson()
    603 -> containmentLesson()
    else -> null
}

// ---------------------------------------------------------------------------
// LEVEL 101 — Inbox Triage
// ---------------------------------------------------------------------------
// Examples here are deliberately different from the lab's own messages, so
// reading the lesson teaches the skill without handing over the answers.

private fun inboxTriageLesson(): Lesson = Lesson(
    levelId = 101,
    title = "Reading a Phishing Email",
    sources = listOf(
        LessonSource(
            id = "nist-glossary",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"Phishing.\" Glossary " +
                    "(definition from NIST SP 800-12 Rev. 1).",
            url = "https://csrc.nist.gov/glossary/term/phishing"
        ),
        LessonSource(
            id = "phish-scale",
            short = "NIST TN 2276",
            citation = "Dawkins, S. & Jacobs, J. (2023). NIST Phish Scale User Guide " +
                    "(NIST Technical Note 2276), Appendix B: cues B.2.2–B.2.4, B.4.4–B.4.5.",
            url = "https://doi.org/10.6028/NIST.TN.2276"
        ),
        LessonSource(
            id = "cisa-report",
            short = "CISA",
            citation = "Cybersecurity and Infrastructure Security Agency. \"Recognize and " +
                    "Report Phishing.\" Secure Our World.",
            url = "https://www.cisa.gov/secure-our-world/recognize-and-report-phishing"
        ),
        LessonSource(
            id = "cisa-se",
            short = "CISA",
            citation = "Cybersecurity and Infrastructure Security Agency. \"Avoiding Social " +
                    "Engineering and Phishing Attacks.\"",
            url = "https://www.cisa.gov/news-events/news/avoiding-social-engineering-and-phishing-attacks"
        ),
        LessonSource(
            id = "rfc",
            short = "IETF RFC 5322 / 5321",
            citation = "IETF (2008). RFC 5322: Internet Message Format, §3.4 (display name " +
                    "and address); RFC 5321: SMTP, §4.4 (Received trace headers).",
            url = "https://www.rfc-editor.org/rfc/rfc5322#section-3.4"
        ),
        LessonSource(
            id = "fbi",
            short = "FBI IC3",
            citation = "FBI Internet Crime Complaint Center (2019). \"Cyber Actors Exploit " +
                    "'Secure' Websites in Phishing Campaigns.\" PSA I-061019-PSA.",
            url = "https://www.ic3.gov/PSA/2019/PSA190610"
        ),
        LessonSource(
            id = "cova",
            short = "Cova et al. (2008)",
            citation = "Cova, M., Kruegel, C. & Vigna, G. (2008). \"There Is No Free Phish: " +
                    "An Analysis of 'Free' and Live Phishing Kits.\" USENIX WOOT '08.",
            url = "https://seclab.cs.ucsb.edu/publications/cova2008there_is/"
        ),
        LessonSource(
            id = "mdn",
            short = "MDN",
            citation = "MDN Web Docs. \"Basic HTML syntax: HTML comments.\" Mozilla.",
            url = "https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Structuring_content/Basic_HTML_syntax#html_comments"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "What phishing is",
            icon = LessonIcon.PHISHING,
            paragraphs = listOf(
                "**Phishing** is a fake message — usually an email or a web page — from " +
                        "someone **pretending to be a trusted business or person**, sent to get " +
                        "passwords, bank details or other sensitive data.",
                "The same message usually lands in **many inboxes at once**, so spotting and " +
                        "reporting one protects more than just you."
            ),
            keyTerms = listOf(
                KeyTerm("Phishing", "A fraudulent message that impersonates someone trusted to steal sensitive data."),
                KeyTerm("Social engineering", "Tricking people, rather than breaking machines, to get access."),
                KeyTerm("Triage", "Quickly sorting messages into ordinary and suspicious before looking deeper.")
            ),
            sourceIds = listOf("nist-glossary", "cisa-se")
        ),
        LessonPage(
            title = "Read what it wants from you",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "Names and logos are easy to copy. A stronger signal is **what the message " +
                        "pushes you to do** — and how fast.",
                "NIST lists a **sense of urgency** and **threatening language** among the most " +
                        "common phishing cues. Ordinary mail informs you; pressure is there to " +
                        "**stop you from checking**."
            ),
            visual = LessonVisual.Email(
                senderName = "Library Services",
                senderAddress = "notices@library-desk.example",
                subject = "FINAL NOTICE: borrowing access ends in 2 hours",
                body = "Your library account has unpaid fines. Pay **within 2 hours** or your " +
                        "account will be **suspended** and your clearance **put on hold**.",
                button = "PAY NOW",
                warningSigns = listOf(
                    "A deadline measured in hours",
                    "A penalty if you ignore it",
                    "Wants you to act through its own button"
                )
            ),
            sourceIds = listOf("phish-scale", "cisa-report"),
            task = 1
        ),
        LessonPage(
            title = "Trust the domain, not the name",
            icon = LessonIcon.EMAIL,
            paragraphs = listOf(
                "Attackers register **lookalike domains**: a digit in place of a letter, letters " +
                        "that blur together, or an extra word added to a brand. CISA's example " +
                        "is **amazan.com** posing as amazon.com.",
                "**Read the domain one character at a time.** For more proof, the **message " +
                        "headers** record every mail server the message passed through."
            ),
            visual = LessonVisual.Address(
                displayName = "Help Desk",
                username = "help",
                domain = "school-support.example"
            ),
            keyTerms = listOf(
                KeyTerm("Display name", "The friendly name shown in the inbox. The sender types it, so it proves nothing."),
                KeyTerm("Domain", "The part after the @. It shows which organisation's mail system sent it."),
                KeyTerm("Headers", "Hidden lines added by each mail server, showing the route a message took.")
            ),
            sourceIds = listOf("rfc", "phish-scale", "cisa-report"),
            task = 2
        ),
        LessonPage(
            title = "Links and padlocks can mislead",
            icon = LessonIcon.LINK,
            paragraphs = listOf(
                "A link's **text** and its **real destination** can be different. So can the " +
                        "page you see and the place a login form **sends what you type**.",
                "The FBI warns that phishing sites use HTTPS precisely because people trust " +
                        "the padlock. **Looking is safe; typing your password is not.**"
            ),
            visual = LessonVisual.Link(
                shownText = "https://portal.school.example/login",
                realDestination = "https://portal-login.account-check.example/"
            ),
            sourceIds = listOf("phish-scale", "fbi", "cisa-report"),
            task = 3
        ),
        LessonPage(
            title = "Phishing pages leave fingerprints",
            icon = LessonIcon.CODE,
            paragraphs = listOf(
                "Many phishing pages are built from **phishing kits** — ready-made bundles " +
                        "of pages and scripts that are shared and reused across many sites.",
                "A page's **source code** holds things the browser never displays, like " +
                        "developer **comments**. Analysts read it to spot reused kits."
            ),
            visual = LessonVisual.Source(
                lines = listOf(
                    "<h1>Sign in to continue</h1>",
                    "<!-- template: login-kit v3 -->",
                    "<!-- TODO: swap logo per target -->",
                    "<input type=\"password\">"
                )
            ),
            keyTerms = listOf(
                KeyTerm("Phishing kit", "A ready-made phishing website that attackers copy and reuse."),
                KeyTerm("Flag", "A hidden string you must find in a Capture the Flag (CTF) exercise.")
            ),
            sourceIds = listOf("cova", "mdn"),
            task = 4
        ),
        LessonPage(
            title = "What to do once you're sure",
            icon = LessonIcon.REPORT,
            paragraphs = listOf(
                "A report helps protect **everyone else** who received the same message. " +
                        "Deleting it alone only cleans up your own inbox."
            ),
            visual = LessonVisual.DoDont(
                dos = listOf(
                    "Report it to IT or security, or use the mail app's report button",
                    "Delete it once it's reported",
                    "Unsure? Go to the real site yourself"
                ),
                donts = listOf(
                    "Reply to the sender",
                    "Click anything in it — not even unsubscribe",
                    "Forward it to friends as a warning"
                )
            ),
            sourceIds = listOf("cisa-report", "cisa-se"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "An email signed \"CvSU Registrar\" comes from registrar@cvsu-edu-ph.example. " +
                    "The university's real domain is cvsu.edu.ph. Which detail tells you whether CvSU really sent it?",
            options = listOf(
                "The word \"cvsu\" at the start of the sender's address",
                "Everything after the @, compared with cvsu.edu.ph",
                "The official CvSU logo at the top of the message",
                "The display name \"CvSU Registrar\" in the inbox"
            ),
            correctIndex = 1,
            explanation = "cvsu-edu-ph.example is a different domain from cvsu.edu.ph, so it " +
                    "didn't come from CvSU. A familiar word in the address, a logo and a " +
                    "display name are all easy for an attacker to copy."
        ),
        LessonQuestion(
            prompt = "A sign-in page shows a padlock and its address starts with https://. " +
                    "What does that padlock actually confirm?",
            options = listOf(
                "The site belongs to the organisation it claims to be",
                "Your browser checked the site and found it safe",
                "What you send can't be read by others along the way",
                "The page is safe for typing in your password"
            ),
            correctIndex = 2,
            explanation = "HTTPS protects data in transit between you and that site — whoever " +
                    "runs it. Phishing sites get certificates too, so the padlock says nothing " +
                    "about who is on the other end."
        ),
        LessonQuestion(
            prompt = "Four lines from four different emails. Which one is the strongest sign " +
                    "of phishing?",
            options = listOf(
                "\"Reminder: enrollment for next term closes this Friday.\"",
                "\"Hi Ana, attached are the notes from yesterday's meeting.\"",
                "\"Office hours move to 1 PM next week; see the schedule.\"",
                "\"Confirm your details now or your account is deleted at 5 PM.\""
            ),
            correctIndex = 3,
            explanation = "A deadline alone is normal — enrollment really closes. The warning " +
                    "sign is the threat: act right now or lose something. That pressure is " +
                    "designed to stop you checking first."
        )
    )
)

// ---------------------------------------------------------------------------
// LEVEL 102 — Threat Console
// ---------------------------------------------------------------------------
// The senior analyst's briefing before the night shift. Same rule as 101: the
// example alerts, accounts and addresses differ from the console's own.

private fun threatConsoleLesson(): Lesson = Lesson(
    levelId = 102,
    title = "Reading a Security Alert",
    sources = listOf(
        LessonSource(
            id = "nist-ir",
            short = "NIST SP 800-61r3",
            citation = "Nelson, A., Rekhi, S., Souppaya, M. & Scarfone, K. (2025). Incident " +
                    "Response Recommendations and Considerations for Cybersecurity Risk " +
                    "Management (NIST SP 800-61 Rev. 3): DE.AE-02, DE.AE-03, DE.AE-08, " +
                    "RS.MA-02, RS.MI-01, RS.MI-02.",
            url = "https://doi.org/10.6028/NIST.SP.800-61r3"
        ),
        LessonSource(
            id = "nist-fp",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"False positive.\" Glossary " +
                    "(definition from NIST SP 800-86).",
            url = "https://csrc.nist.gov/glossary/term/false_positive"
        ),
        LessonSource(
            id = "mitre",
            short = "MITRE ATT&CK",
            citation = "MITRE ATT&CK. \"Brute Force\" (T1110), including Password Guessing " +
                    "(T1110.001): detection and mitigations.",
            url = "https://attack.mitre.org/techniques/T1110/"
        ),
        LessonSource(
            id = "ra10175",
            short = "RA 10175",
            citation = "Republic of the Philippines. Republic Act No. 10175, Cybercrime " +
                    "Prevention Act of 2012, Sec. 4(a)(1): Illegal Access.",
            url = "https://lawphil.net/statutes/repacts/ra2012/ra_10175_2012.html"
        ),
        LessonSource(
            id = "nist-ioc",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"Indicator.\" Glossary " +
                    "(definition from NIST SP 800-150).",
            url = "https://csrc.nist.gov/glossary/term/indicator"
        ),
        LessonSource(
            id = "nist-logs",
            short = "NIST SP 800-92",
            citation = "Kent, K. & Souppaya, M. (2006). Guide to Computer Security Log " +
                    "Management (NIST SP 800-92).",
            url = "https://doi.org/10.6028/NIST.SP.800-92"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "Welcome to the night shift",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "A **Security Operations Centre (SOC)** watches an organisation's systems " +
                        "around the clock. Its tools raise **alerts** whenever something " +
                        "looks unusual.",
                "Your first job on every alert is **triage**: check whether it's real, then " +
                        "decide **how urgent** it is. That way the real attacks get attention first."
            ),
            keyTerms = listOf(
                KeyTerm("SOC", "The team that monitors systems and responds to security alerts."),
                KeyTerm("Alert", "A warning a security tool raises when activity matches one of its rules."),
                KeyTerm("Triage", "Checking whether an alert is real and deciding how urgent it is.")
            ),
            sourceIds = listOf("nist-ir")
        ),
        LessonPage(
            title = "Most alerts are noise",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "A **false positive** is a tool calling normal activity malicious. Alert rules " +
                        "can't see context, so they fire on backups, updates and scans too.",
                "You prove one with evidence that explains **all of it** — the host, the " +
                        "timing and the targets. **Never judge it by the alert's title.**"
            ),
            visual = LessonVisual.Log(
                title = "ALERT · PORT SCAN DETECTED",
                lines = listOf(
                    "host    IT-SCAN-01",
                    "target  every lab subnet",
                    "time    Mon 09:00 - 09:41",
                    "notes   IT vulnerability scanner,",
                    "        weekly job, Mondays 09:00"
                ),
                highlights = listOf(3, 4),
                caption = "The asset notes explain the host, the time and the targets."
            ),
            keyTerms = listOf(
                KeyTerm("False positive", "An alert on activity that turns out to be harmless."),
                KeyTerm("True positive", "An alert on activity that really is an attack.")
            ),
            sourceIds = listOf("nist-fp", "nist-ir"),
            task = 1
        ),
        LessonPage(
            title = "Name the threat",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "Before you can respond, you have to **name the kind of threat** — that " +
                        "decides who acts and what gets locked down. **Where the evidence " +
                        "shows up** usually tells you its name.",
                "Guessing a password to get into someone's account is **illegal access** under " +
                        "the Philippines' **Cybercrime Prevention Act**, even if the attempts " +
                        "come from a script."
            ),
            keyTerms = listOf(
                KeyTerm("Malware", "Hostile code on a computer. Look in its running programs and changed files."),
                KeyTerm("Credential attack", "Guessing or stealing a login. Look in the sign-in logs."),
                KeyTerm("Denial of service", "Flooding a service so real users can't reach it. Look for traffic from many sources."),
                KeyTerm("Policy breach", "A person breaking a security rule. No outside attacker is involved.")
            ),
            sourceIds = listOf("nist-ir", "mitre", "ra10175"),
            task = 2
        ),
        LessonPage(
            title = "Indicators you can search",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "An **indicator of compromise (IOC)** is a fact you can search for, like an " +
                        "address, an account or a file. Sign-in logs record **where each attempt came from**.",
                "When a log shows many failures from one source and then a **success**, that " +
                        "source is the attacker. **Copy it exactly** — one wrong digit and your " +
                        "search finds nothing."
            ),
            visual = LessonVisual.Log(
                title = "SIGN-IN LOG",
                lines = listOf(
                    "09:14:02 FAIL user=f.lim src=198.51.100.23",
                    "09:14:03 FAIL user=f.lim src=198.51.100.23",
                    "09:14:03 FAIL user=f.lim src=198.51.100.23",
                    "09:14:04 OK   user=f.lim src=198.51.100.23"
                ),
                highlights = listOf(3),
                caption = "src is where an attempt came from: the attacker. user is the account " +
                        "under attack: the victim."
            ),
            keyTerms = listOf(
                KeyTerm("IOC", "Indicator of compromise: a searchable sign that an attack happened."),
                KeyTerm("Log", "A record a system keeps of events, such as each sign-in attempt.")
            ),
            sourceIds = listOf("nist-ioc", "nist-logs", "mitre"),
            task = 3
        ),
        LessonPage(
            title = "Pivot on what you found",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "An alert shows one system. **Pivoting** means searching for the same IOC " +
                        "across **every** log, which shows what else the attacker touched.",
                "**Read every row** that comes back, including the quiet ones. The row that " +
                        "matters is rarely the one you were looking for."
            ),
            visual = LessonVisual.Log(
                title = "SEARCH: 198.51.100.23 · 4 RESULTS",
                lines = listOf(
                    "09:14:02 sso   FAIL user=f.lim",
                    "09:14:04 sso   OK   user=f.lim",
                    "09:15:30 files READ grades_2026.xlsx",
                    "09:17:12 sso   FAIL user=r.cruz"
                ),
                highlights = listOf(2, 3),
                caption = "The alert only showed sign-ins. The search also shows a file being " +
                        "read and a second account under attack."
            ),
            keyTerms = listOf(
                KeyTerm("Pivoting", "Searching for one IOC across all logs to see everything it touched.")
            ),
            sourceIds = listOf("nist-ir", "nist-logs"),
            task = 4
        ),
        LessonPage(
            title = "Cut off what was taken",
            icon = LessonIcon.LOCK,
            paragraphs = listOf(
                "**Containment** stops an attack from spreading while you investigate. The " +
                        "right action depends on **what the attacker took**.",
                "If malware is running, **cut that machine off the network**. If a password " +
                        "was stolen, it works **from anywhere**, so you **reset it and end its " +
                        "sessions**."
            ),
            visual = LessonVisual.DoDont(
                dos = listOf(
                    "Match the action to what was taken",
                    "Cut off the attacker's access first",
                    "Use MFA so a guessed password isn't enough"
                ),
                donts = listOf(
                    "Silence the alert so it stops firing",
                    "Wait for someone's reply before acting",
                    "Clean up before access has been cut off"
                )
            ),
            keyTerms = listOf(
                KeyTerm("Containment", "Stopping an attack from spreading or doing more damage."),
                KeyTerm("MFA", "Multi-factor authentication: a second proof of identity on top of the password.")
            ),
            sourceIds = listOf("nist-ir", "mitre"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "A \"port scan\" alert fires every Monday at 9:00 from IT-SCAN-01. Its asset " +
                    "notes say it's IT's weekly vulnerability scanner. What is the alert?",
            options = listOf(
                "A false positive: the scheduled scan explains the host, the time and the targets",
                "A true positive: port scans are the first step of most network attacks",
                "Too noisy to judge, so close it without checking because it fires weekly",
                "A real attack, because the alert title says a port scan was detected"
            ),
            correctIndex = 0,
            explanation = "The notes explain who ran it, when and against what, so it's a " +
                    "false positive. But you still check it each time: closing alerts just " +
                    "because they're familiar is how a real attack gets missed."
        ),
        LessonQuestion(
            prompt = "You think someone is guessing a student's password. Which evidence " +
                    "would show it best?",
            options = listOf(
                "The list of programs running on the student's laptop",
                "A graph of the campus network's total traffic today",
                "The sign-in log for that student's account",
                "The history of files changed on the shared drive"
            ),
            correctIndex = 2,
            explanation = "Password guessing shows up where sign-ins are recorded: many " +
                    "failures from one source, sometimes followed by a success. The other " +
                    "places show malware or heavy traffic instead."
        ),
        LessonQuestion(
            prompt = "Antivirus finds malware running on a lab PC. No one typed a password " +
                    "on it. What should happen first?",
            options = listOf(
                "Reset every password on campus, just to be safe",
                "Raise the antivirus alert level so it stops firing",
                "Ask the last student who used it what they downloaded",
                "Disconnect that PC from the network to contain it"
            ),
            correctIndex = 3,
            explanation = "The malware is on that PC, so isolating the PC is what stops it from " +
                    "spreading. Resetting passwords targets something that wasn't taken, and " +
                    "the question can wait until the PC is cut off."
        )
    )
)

// ---------------------------------------------------------------------------
// LEVEL 103 — CIA Triad
// ---------------------------------------------------------------------------
// The briefing before enrollment week. The lab's three tickets are all at the
// Registrar, so every example here comes from another office.

private fun ciaTriadLesson(): Lesson = Lesson(
    levelId = 103,
    title = "Seen, Changed, or Unreachable?",
    sources = listOf(
        LessonSource(
            id = "fips199",
            short = "NIST FIPS 199",
            citation = "NIST (2004). Standards for Security Categorization of Federal " +
                    "Information and Information Systems (FIPS 199): definitions of " +
                    "confidentiality, integrity and availability.",
            url = "https://doi.org/10.6028/NIST.FIPS.199"
        ),
        LessonSource(
            id = "ra10173",
            short = "RA 10173",
            citation = "Republic of the Philippines. Republic Act No. 10173, Data Privacy " +
                    "Act of 2012, Sec. 20(a): Security of Personal Information.",
            url = "https://lawphil.net/statutes/repacts/ra2012/ra_10173_2012.html"
        ),
        LessonSource(
            id = "sp80053",
            short = "NIST SP 800-53r5",
            citation = "Joint Task Force (2020). Security and Privacy Controls for " +
                    "Information Systems and Organizations (NIST SP 800-53 Rev. 5): AC-6 " +
                    "Least Privilege, AU-3 Content of Audit Records, SI-7 Information Integrity.",
            url = "https://doi.org/10.6028/NIST.SP.800-53r5"
        ),
        LessonSource(
            id = "fips180",
            short = "NIST FIPS 180-4",
            citation = "NIST (2015). Secure Hash Standard (FIPS 180-4).",
            url = "https://doi.org/10.6028/NIST.FIPS.180-4"
        ),
        LessonSource(
            id = "mdn-ua",
            short = "MDN",
            citation = "MDN Web Docs. \"User-Agent\" HTTP header. Mozilla.",
            url = "https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/User-Agent"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "The CIA triad",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "Security protects three things about data, and every incident damages at " +
                        "least one of them. Together they're called the **CIA triad**.",
                "The Philippines' **Data Privacy Act** puts the same three duties on every " +
                        "school: protect personal data against unlawful **disclosure**, " +
                        "**alteration** and **destruction**."
            ),
            keyTerms = listOf(
                KeyTerm("Confidentiality", "Only the people allowed to see the data can see it."),
                KeyTerm("Integrity", "The data is accurate and hasn't been changed without permission."),
                KeyTerm("Availability", "The data is there when the people who need it need it.")
            ),
            sourceIds = listOf("fips199", "ra10173")
        ),
        LessonPage(
            title = "Seen, changed, or unreachable?",
            icon = LessonIcon.REPORT,
            paragraphs = listOf(
                "Ask one question of every report: was the data **seen** by the wrong " +
                        "people, **changed** without permission, or **out of reach**?",
                "One incident can hit more than one pillar, but each report has a **main " +
                        "pillar**. Notice what the reporter **rules out** — it narrows the " +
                        "answer as much as what they report."
            ),
            visual = LessonVisual.Log(
                title = "THREE REPORTS FROM AROUND CAMPUS",
                lines = listOf(
                    "CLINIC   Student list posted in a",
                    "         public group chat",
                    "ORG      Budget total changed overnight",
                    "LIBRARY  E-book site down since 8 AM"
                ),
                caption = "Seen by the wrong people, changed without permission, out of reach: " +
                        "confidentiality, integrity, availability."
            ),
            sourceIds = listOf("fips199"),
            task = 1
        ),
        LessonPage(
            title = "Share less, not \"safely\"",
            icon = LessonIcon.LOCK,
            paragraphs = listOf(
                "**Least privilege** means giving only the access the work needs. For shared " +
                        "files, the setting behind most leaks is **\"anyone with the link\"**.",
                "In an access log, **anonymous** means someone opened the file **without " +
                        "signing in**. Restricting the link stops new access, but it **can't " +
                        "recall copies already downloaded**."
            ),
            visual = LessonVisual.Log(
                title = "WHO CAN OPEN THIS FILE?",
                lines = listOf(
                    "Anyone with the link   no sign-in",
                    "Everyone at CvSU       any CvSU account",
                    "Specific people        named accounts only"
                ),
                highlights = listOf(2),
                caption = "Pick the smallest group that still lets the work get done."
            ),
            keyTerms = listOf(
                KeyTerm("Least privilege", "Giving each person or file only the access the work needs, and nothing more.")
            ),
            sourceIds = listOf("sp80053", "ra10173"),
            task = 2
        ),
        LessonPage(
            title = "A fingerprint for every file",
            icon = LessonIcon.CODE,
            paragraphs = listOf(
                "A **hash** like **SHA-256** is a fingerprint of a file's exact contents. " +
                        "Change **one character** and the whole fingerprint changes.",
                "Record the hashes while the files are known to be good. That record is the " +
                        "**baseline**. **Never re-sign the baseline** during an investigation: " +
                        "it erases your only proof of what changed."
            ),
            visual = LessonVisual.Log(
                title = "SHA-256 · FIRST 16 CHARACTERS",
                lines = listOf(
                    "budget = 15000  6639aff204b6d4b1…",
                    "budget = 18000  b31687f6395601bd…"
                ),
                caption = "One character apart in the file, nothing alike in the fingerprint."
            ),
            keyTerms = listOf(
                KeyTerm("Hash", "A fixed-length fingerprint of data. Any change gives a different hash."),
                KeyTerm("Baseline", "The trusted record of what something looked like when it was known to be good.")
            ),
            sourceIds = listOf("fips180", "sp80053"),
            task = 3
        ),
        LessonPage(
            title = "Who changed it, and how",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "A **diff** shows **what** changed: the value before and after. An **audit " +
                        "trail** shows **who** made the change, **when**, **from where** and " +
                        "**with what tool**.",
                "The **user-agent** names the program that sent a request. A person editing " +
                        "in a browser shows a browser. A **script's name** there means no " +
                        "person was typing."
            ),
            visual = LessonVisual.Log(
                title = "AUDIT TRAIL · BUDGET SHEET",
                lines = listOf(
                    "time  : Tue 23:52",
                    "user  : org.treasurer",
                    "from  : 198.51.100.77 (off campus)",
                    "ua    : python-requests/2.31.0"
                ),
                highlights = listOf(3),
                caption = "The account is the treasurer's, but the tool is a script."
            ),
            keyTerms = listOf(
                KeyTerm("Audit trail", "A record of who did what, when, from where and how."),
                KeyTerm("User-agent", "A label each request carries, naming the program that sent it.")
            ),
            sourceIds = listOf("sp80053", "mdn-ua"),
            task = 4
        ),
        LessonPage(
            title = "Bring it back without breaking it",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "Outages are often **not attacks**: a full disk, an expired certificate, a " +
                        "setting left on after maintenance. Check whether traffic is really " +
                        "abnormal before blaming an attacker.",
                "Fix the **cause**, not the symptom. And **never trade one pillar for " +
                        "another** — deleting good records to free space breaks integrity."
            ),
            visual = LessonVisual.DoDont(
                dos = listOf(
                    "Check if the traffic is really unusual",
                    "Find what ran out: disk, memory, certificate",
                    "Remove the cause, then restart"
                ),
                donts = listOf(
                    "Assume every outage is an attack",
                    "Restart again and again without a fix",
                    "Delete good data to make room"
                )
            ),
            sourceIds = listOf("fips199"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "A student's clinic record now lists an allergy they never had. Nobody " +
                    "outside the clinic saw it, and the system works fine. Which pillar is broken?",
            options = listOf(
                "Confidentiality: the record was seen by the wrong people",
                "Integrity: the record was changed without permission",
                "Availability: the right record couldn't be reached",
                "None of them: the record still opens normally"
            ),
            correctIndex = 1,
            explanation = "The data is wrong, so integrity is broken. Nobody unauthorised saw it " +
                    "and it's still reachable, so confidentiality and availability are fine. A " +
                    "record that opens normally can still be wrong."
        ),
        LessonQuestion(
            prompt = "An org's member list was shared with \"Anyone with the link\", and the link " +
                    "reached a public group chat. You switch it to \"Specific people\". What " +
                    "does this NOT fix?",
            options = listOf(
                "New visitors opening the link from the group chat",
                "People outside the org opening the file from now on",
                "Anonymous visitors viewing the file again next week",
                "Copies people already downloaded before the change"
            ),
            correctIndex = 3,
            explanation = "Restricting the link blocks every new visit, signed in or not. A copy " +
                    "already downloaded is outside your control, which is why confidentiality " +
                    "has to be set up front."
        ),
        LessonQuestion(
            prompt = "Which of these proves a file was changed after it was approved?",
            options = listOf(
                "Its SHA-256 no longer matches the signed baseline",
                "Its \"last modified\" date now shows today",
                "It takes longer to open than it used to",
                "The person who approved it says it looks different"
            ),
            correctIndex = 0,
            explanation = "A hash that no longer matches a trusted baseline proves the contents " +
                    "changed. Dates change when files are copied and can be edited, and " +
                    "memory or speed proves nothing."
        )
    )
)

// ---------------------------------------------------------------------------
// LEVEL 104 — Security Principles
// ---------------------------------------------------------------------------
// A design review, not an incident. The lab works on the Registrar portal,
// so the examples come from the library, an org, the Wi-Fi and exam papers.

private fun hardeningReviewLesson(): Lesson = Lesson(
    levelId = 104,
    title = "Design It So a Stolen Password Isn't Enough",
    sources = listOf(
        LessonSource(
            id = "saltzer",
            short = "Saltzer & Schroeder (1975)",
            citation = "Saltzer, J. H. & Schroeder, M. D. (1975). \"The Protection of " +
                    "Information in Computer Systems.\" Proceedings of the IEEE, 63(9). Design " +
                    "principles: fail-safe defaults, least privilege.",
            url = "https://web.mit.edu/Saltzer/www/publications/protection/"
        ),
        LessonSource(
            id = "sp800160",
            short = "NIST SP 800-160v1r1",
            citation = "Ross, R., Winstead, M. & McEvilley, M. (2022). Engineering Trustworthy " +
                    "Secure Systems (NIST SP 800-160 Vol. 1 Rev. 1), Appendix E: E.9 Defense in " +
                    "Depth, E.16 Least Privilege, E.22 Protective Defaults, E.23 Protective Failure.",
            url = "https://doi.org/10.6028/NIST.SP.800-160v1r1"
        ),
        LessonSource(
            id = "sp80053",
            short = "NIST SP 800-53r5",
            citation = "Joint Task Force (2020). Security and Privacy Controls for " +
                    "Information Systems and Organizations (NIST SP 800-53 Rev. 5): AC-6 Least " +
                    "Privilege, AC-6(7) Review of User Privileges, SC-24 Fail in Known State.",
            url = "https://doi.org/10.6028/NIST.SP.800-53r5"
        ),
        LessonSource(
            id = "nist-did",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"Defense-in-depth.\" Glossary " +
                    "(definition from NIST SP 800-53 Rev. 5).",
            url = "https://csrc.nist.gov/glossary/term/defense_in_depth"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "Fix the design, not just the incident",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "A fix protects one system. A **security principle** is the reason behind the " +
                        "fix, so it works on **any** system you're asked to protect.",
                "Passwords get stolen eventually. These three principles don't stop that — " +
                        "they decide **how much a stolen password is worth**."
            ),
            keyTerms = listOf(
                KeyTerm("Least privilege", "Every account gets only the access its job needs."),
                KeyTerm("Fail-safe default", "When a check can't finish, the answer is no."),
                KeyTerm("Defence in depth", "Several different barriers, so no single one has to be perfect.")
            ),
            sourceIds = listOf("saltzer", "sp800160")
        ),
        LessonPage(
            title = "Roles drift away from jobs",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "A **permission** is one thing an account can do. A **role** is a bundle of " +
                        "them handed out together, so it often carries more than the job needs.",
                "Jobs change, but access stays: that's **privilege creep**. Check the **job " +
                        "description** against the **permission list**, not how much you trust " +
                        "the person. Be wary of the word **\"temporary\"**."
            ),
            visual = LessonVisual.Log(
                title = "ACCOUNT REVIEW · LIBRARY",
                lines = listOf(
                    "account   lib.assist2",
                    "job       check books in and out",
                    "role      Library Admin",
                    "note      \"temporary, for exam week\"",
                    "reviewed  never"
                ),
                highlights = listOf(2, 3, 4),
                caption = "The role was meant for one busy week, and nobody took it back."
            ),
            keyTerms = listOf(
                KeyTerm("Permission", "One action an account is allowed to do."),
                KeyTerm("Role", "A bundle of permissions given out together."),
                KeyTerm("Privilege creep", "Access that piles up as jobs change and is never removed.")
            ),
            sourceIds = listOf("sp80053", "saltzer"),
            task = 1
        ),
        LessonPage(
            title = "Trim to the job",
            icon = LessonIcon.LOCK,
            paragraphs = listOf(
                "In Level 103 you limited **who can open a file**. Least privilege for " +
                        "accounts limits **what an account can do**.",
                "It shrinks the **blast radius**: what a stolen password can reach. But cut " +
                        "something the job needs and the work stops — and people find ways " +
                        "around security that blocks them."
            ),
            visual = LessonVisual.Log(
                title = "TRIMMED TO THE JOB",
                lines = listOf(
                    "✓ loans.checkout   job: lend books",
                    "✓ loans.return     job: take returns",
                    "✗ catalog.edit     not in the job",
                    "✗ fines.waive      not in the job",
                    "✗ users.manage     not in the job"
                ),
                highlights = listOf(0, 1),
                caption = "Start from the job description, not from the role."
            ),
            keyTerms = listOf(
                KeyTerm("Blast radius", "How much damage one stolen account can do.")
            ),
            sourceIds = listOf("sp80053", "sp800160"),
            task = 2
        ),
        LessonPage(
            title = "Fail safe, not open",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "Every check fails sometimes. A **fail-safe default** denies when the check " +
                        "can't finish. A **fail-open** default allows.",
                "**\"Ask the user\"** isn't a safe middle: during an attack, the user may be the " +
                        "attacker. And a **warning in a log** records the damage — it doesn't " +
                        "stop it."
            ),
            visual = LessonVisual.Log(
                title = "WI-FI LOGIN SERVER DOWN · POLICY?",
                lines = listOf(
                    "allow_all  any device can join",
                    "ask_user   the device decides",
                    "log_only   join, and log a warning",
                    "deny_new   only signed-in devices stay"
                ),
                highlights = listOf(3),
                caption = "Only deny_new keeps strangers out while the check is down."
            ),
            keyTerms = listOf(
                KeyTerm("Fail-safe default", "A failed check denies access."),
                KeyTerm("Fail-open", "A failed check allows access, usually so nobody gets locked out.")
            ),
            sourceIds = listOf("saltzer", "sp80053", "sp800160"),
            task = 3
        ),
        LessonPage(
            title = "Layers that fail differently",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "**Defence in depth** only works if the layers are **different**. Four password " +
                        "rules are one layer: one stolen password beats them all.",
                "A layer that only **detects** still counts — it turns a breach found days " +
                        "later into one caught in minutes. Add layers; **never remove one that " +
                        "works**."
            ),
            visual = LessonVisual.Log(
                title = "EXAM PAPERS · DEPARTMENT OFFICE",
                lines = listOf(
                    "Layer 1  locked cabinet",
                    "Layer 2  chair keeps the only key",
                    "Layer 3  CCTV watching the cabinet",
                    "Layer 4  sealed, signed envelopes"
                ),
                highlights = listOf(2, 3),
                caption = "A copied key beats layers 1 and 2. The camera still records it, and a " +
                        "broken seal still shows it."
            ),
            keyTerms = listOf(
                KeyTerm("Defence in depth", "Several independent barriers, each able to stop or catch an attack.")
            ),
            sourceIds = listOf("nist-did", "sp800160"),
            task = 4
        ),
        LessonPage(
            title = "Three questions, three principles",
            icon = LessonIcon.REPORT,
            paragraphs = listOf(
                "Name a fix's principle by the **question it answers**. That's what lets you " +
                        "reuse it on the next system.",
                "Together they make a stolen password a **dead end**: it reaches little, meets " +
                        "several barriers, and anything that breaks says no."
            ),
            keyTerms = listOf(
                KeyTerm("Least privilege", "Answers: how much can one account do?"),
                KeyTerm("Defence in depth", "Answers: how many barriers must an attack get past?"),
                KeyTerm("Fail-safe default", "Answers: what happens when a check breaks?")
            ),
            sourceIds = listOf("saltzer", "sp800160"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "A student who graduated last year still has admin rights on the org's page, " +
                    "kept \"until the new officers settle in\". What is this?",
            options = listOf(
                "A fail-open default: access is allowed when nobody checks it",
                "Missing defence in depth: the page has only one barrier",
                "Privilege creep: the access outlived the job it was for",
                "A password problem: the admin password is too old"
            ),
            correctIndex = 2,
            explanation = "The access was right while they were an officer. The job ended and the " +
                    "access didn't. Regular access reviews catch exactly this."
        ),
        LessonQuestion(
            prompt = "When the campus Wi-Fi login server goes down, the network lets every device " +
                    "join until it's fixed. Which principle does that break?",
            options = listOf(
                "Fail-safe defaults: a failed check should deny, not allow",
                "Least privilege: staff accounts can do more than they need",
                "Defence in depth: the Wi-Fi depends on a single barrier",
                "None: keeping everyone online is the secure choice"
            ),
            correctIndex = 0,
            explanation = "The check failed and the answer became yes. A fail-safe design keeps " +
                    "new devices out until the check works again."
        ),
        LessonQuestion(
            prompt = "Which set adds the most real depth to staff sign-in?",
            options = listOf(
                "A longer password, a special character, and a change every 90 days",
                "A password, a security question, and a password hint",
                "A password, a CAPTCHA, and a reminder to sign out",
                "A password, an MFA app, and an alert on new-country sign-ins"
            ),
            correctIndex = 3,
            explanation = "The first two are all things you know, so one stolen secret beats them. " +
                    "An MFA app adds something you have, and the alert catches what gets past both."
        )
    )
)

// ---------------------------------------------------------------------------
// LEVEL 106 — Risk Assessment
// ---------------------------------------------------------------------------
// Budget season for a student org. The lab scores the Registrar's findings,
// so none of its findings, controls or treatments appear here.

private fun riskRegisterLesson(): Lesson = Lesson(
    levelId = 106,
    title = "Rank It, Fund It, Own What's Left",
    sources = listOf(
        LessonSource(
            id = "sp80030",
            short = "NIST SP 800-30r1",
            citation = "Joint Task Force Transformation Initiative (2012). Guide for Conducting " +
                    "Risk Assessments (NIST SP 800-30 Rev. 1), Ch. 2: risk as a function of " +
                    "impact and likelihood; threats and vulnerabilities.",
            url = "https://doi.org/10.6028/NIST.SP.800-30r1"
        ),
        LessonSource(
            id = "ra10173",
            short = "RA 10173",
            citation = "Republic of the Philippines. Republic Act No. 10173, Data Privacy " +
                    "Act of 2012, Sec. 20(c): weighing risks and the cost of security.",
            url = "https://lawphil.net/statutes/repacts/ra2012/ra_10173_2012.html"
        ),
        LessonSource(
            id = "nist-rr",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"Residual risk.\" Glossary " +
                    "(definition from NIST SP 800-30 Rev. 1).",
            url = "https://csrc.nist.gov/glossary/term/residual_risk"
        ),
        LessonSource(
            id = "sp80039",
            short = "NIST SP 800-39",
            citation = "Joint Task Force Transformation Initiative (2011). Managing Information " +
                    "Security Risk (NIST SP 800-39), Ch. 3: risk acceptance, avoidance, " +
                    "mitigation, and sharing or transfer.",
            url = "https://doi.org/10.6028/NIST.SP.800-39"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "Risk you can rank",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "Every worry feels urgent to whoever raised it, and there's never money for " +
                        "all of them. A **risk assessment** turns that pile into an **order you " +
                        "can defend**.",
                "The **Data Privacy Act** asks for the same thing: security should weigh the " +
                        "**risks** and the **cost** of protecting against them."
            ),
            keyTerms = listOf(
                KeyTerm("Risk", "How likely something bad is, combined with how bad it would be."),
                KeyTerm("Risk register", "The list where every risk, its score, owner and treatment are recorded.")
            ),
            sourceIds = listOf("sp80030", "ra10173")
        ),
        LessonPage(
            title = "Asset, threat, vulnerability",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "Split every risk into three parts: the **asset** you protect, the **threat** " +
                        "that could harm it, and the **vulnerability** it would use.",
                "Only the **vulnerability** is yours to change. You can't remove thieves, and " +
                        "you won't stop owning what you need — so every fix targets the weakness."
            ),
            visual = LessonVisual.Log(
                title = "FINDING · AV ROOM",
                lines = listOf(
                    "asset          12 borrowed projectors",
                    "threat         someone walking in",
                    "vulnerability  lock broken since June"
                ),
                highlights = listOf(2),
                caption = "You can't remove thieves or stop owning projectors. You can fix the lock."
            ),
            keyTerms = listOf(
                KeyTerm("Asset", "Something of value you need to protect."),
                KeyTerm("Threat", "Who or what could cause harm."),
                KeyTerm("Vulnerability", "The weakness a threat would use. The part you can fix.")
            ),
            sourceIds = listOf("sp80030"),
            task = 1
        ),
        LessonPage(
            title = "Likelihood × impact",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "A common way to score risk is **likelihood × impact**. Look at **both**: " +
                        "a frequent but trivial risk and a rare but severe one can tie.",
                "Score **likelihood** from evidence: has it happened here, and how often? " +
                        "Score **impact** from how many people it reaches and **how sensitive** " +
                        "the data is."
            ),
            visual = LessonVisual.Log(
                title = "LIKELIHOOD × IMPACT = RISK",
                lines = listOf(
                    "lost org laptop     4 × 3 = 12",
                    "flooded storeroom   1 × 5 =  5",
                    "broken projector    5 × 1 =  5"
                ),
                highlights = listOf(0),
                caption = "The risk that's both fairly likely and fairly bad comes first."
            ),
            keyTerms = listOf(
                KeyTerm("Likelihood", "How probable it is, based on what has actually happened."),
                KeyTerm("Impact", "How bad it would be: how many people, and how sensitive the data.")
            ),
            sourceIds = listOf("sp80030"),
            task = 2
        ),
        LessonPage(
            title = "Most risk removed per peso",
            icon = LessonIcon.REPORT,
            paragraphs = listOf(
                "**Residual risk** is what's left after a fix. The best plan is the one that " +
                        "leaves the **lowest total** within the budget, not the one that sounds " +
                        "most impressive.",
                "The obvious fix is sometimes unaffordable, and a cheaper one can target the " +
                        "same risk. One big control that **partly** covers two risks can lose " +
                        "to small fixes that **fully** cover each."
            ),
            visual = LessonVisual.Log(
                title = "BUDGET ₱40,000 · RISK NOW 30",
                lines = listOf(
                    "Plan A  one big system  ₱38k  left 19",
                    "Plan B  3 small fixes   ₱31k  left 11"
                ),
                highlights = listOf(1),
                caption = "Compare what's left after each plan, not how impressive it sounds."
            ),
            keyTerms = listOf(
                KeyTerm("Residual risk", "The part of a risk that remains after controls are applied.")
            ),
            sourceIds = listOf("nist-rr", "ra10173"),
            task = 3
        ),
        LessonPage(
            title = "Four ways to treat a risk",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "Every decision about a risk falls into one of four boxes. Naming the box makes " +
                        "the decision **clear** and lets someone else **review** it.",
                "**Mitigate** lowers it, **transfer** moves who pays, **avoid** stops the " +
                        "activity, and **accept** knowingly lives with it."
            ),
            visual = LessonVisual.Log(
                title = "FOUR WAYS TO TREAT A RISK",
                lines = listOf(
                    "mitigate  fix the AV-room lock",
                    "transfer  venue contract covers damage",
                    "avoid     cancel the outdoor night event",
                    "accept    keep old projector, review June"
                ),
                caption = "Accepting is a real choice, as long as someone signs it and dates it."
            ),
            sourceIds = listOf("sp80039"),
            task = 4
        ),
        LessonPage(
            title = "Risk never hits zero",
            icon = LessonIcon.LOCK,
            paragraphs = listOf(
                "Some risk always remains, and removing the last of it costs more than it's " +
                        "worth. What's left is **owned**: a score, a **named owner** who signs " +
                        "off, and a **date** to review it.",
                "A treated risk **stays on the register**. Deleting it doesn't reduce it — it " +
                        "just hides the history the next person needs."
            ),
            visual = LessonVisual.DoDont(
                dos = listOf(
                    "Write down the score that's left",
                    "Have the risk owner sign off on it",
                    "Set a date to look at it again"
                ),
                donts = listOf(
                    "Keep spending until it reaches zero",
                    "Delete a risk once it's been treated",
                    "Call a risk nobody looked at \"accepted\""
                )
            ),
            sourceIds = listOf("nist-rr", "sp80039"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "A finding says: \"Exam files are on a shared drive that anyone on campus " +
                    "can open.\" Which part is the vulnerability?",
            options = listOf(
                "The exam files stored on the drive",
                "Students who would want the answers",
                "The department that manages the drive",
                "Open sharing on the drive for all of campus"
            ),
            correctIndex = 3,
            explanation = "The files are the asset and the students are the threat. The open " +
                    "sharing is the weakness, and it's the only part you can fix."
        ),
        LessonQuestion(
            prompt = "Risk A is likely (4) with minor impact (2). Risk B is unlikely (2) with " +
                    "severe impact (5). Which ranks higher?",
            options = listOf(
                "A, because it happens more often",
                "B, because 2 × 5 = 10 beats 4 × 2 = 8",
                "They tie, since each has one high number",
                "A, because it's cheaper to fix"
            ),
            correctIndex = 1,
            explanation = "Score both halves: 10 beats 8. Ranking by how often something happens, " +
                    "or by how cheap it is to fix, ignores half the risk."
        ),
        LessonQuestion(
            prompt = "After two thefts, the org stops holding its fundraiser at night. Which " +
                    "treatment is this?",
            options = listOf(
                "Mitigate: it lowers the chance of a theft",
                "Transfer: someone else now carries the cost",
                "Avoid: the risky activity no longer happens",
                "Accept: the org decides to live with thefts"
            ),
            correctIndex = 2,
            explanation = "Stopping the activity removes the risk instead of shrinking it, so " +
                    "it's avoidance. The cost is what the org gives up: the night event."
        )
    )
)

// ---------------------------------------------------------------------------
// LEVEL 601 — Security Incidents
// ---------------------------------------------------------------------------
// The briefing before a shift on the ITSO hotline. The lab's reports are
// Prof. Ramos's mailbox, the OJT forms and a stolen laptop, so the examples
// here are an org page, grade printouts and an encrypted tablet.

private fun securityIncidentsLesson(): Lesson = Lesson(
    levelId = 601,
    title = "The First Hour",
    sources = listOf(
        LessonSource(
            id = "nist-ir",
            short = "NIST SP 800-61r3",
            citation = "Nelson, A., Rekhi, S., Souppaya, M. & Scarfone, K. (2025). Incident " +
                    "Response Recommendations and Considerations for Cybersecurity Risk " +
                    "Management (NIST SP 800-61 Rev. 3): RS.MA-02, RS.MA-03, RS.CO-02.",
            url = "https://doi.org/10.6028/NIST.SP.800-61r3"
        ),
        LessonSource(
            id = "nist-incident",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"Event\" (definition from " +
                    "SP 800-61r3) and \"Incident\" (definition from 44 U.S.C. Sec. 3552). Glossary.",
            url = "https://csrc.nist.gov/glossary/term/incident"
        ),
        LessonSource(
            id = "sp80086",
            short = "NIST SP 800-86",
            citation = "Kent, K., Chevalier, S., Grance, T. & Dang, H. (2006). Guide to " +
                    "Integrating Forensic Techniques into Incident Response (NIST SP 800-86): " +
                    "volatile data and preserving evidence.",
            url = "https://doi.org/10.6028/NIST.SP.800-86"
        ),
        LessonSource(
            id = "ra10173",
            short = "RA 10173",
            citation = "Republic of the Philippines. Republic Act No. 10173, Data Privacy " +
                    "Act of 2012, Sec. 3(l): sensitive personal information; Sec. 20(f): " +
                    "notification of breach.",
            url = "https://lawphil.net/statutes/repacts/ra2012/ra_10173_2012.html"
        ),
        LessonSource(
            id = "npc1603",
            short = "NPC Circular 16-03",
            citation = "National Privacy Commission (2016). NPC Circular 16-03, Personal Data " +
                    "Breach Management, Sec. 11 (when notification is required) and Sec. 17(A) " +
                    "(notify the Commission within 72 hours).",
            url = "https://privacy.gov.ph/wp-content/uploads/2022/01/sgd-npc-circular-16-03-personal-data-breach-management.pdf"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "Whoever picks up the phone",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "Every defence fails eventually. The first person to hear about it is rarely " +
                        "an expert — it's **whoever picks up the phone**.",
                "In Level 102 you triaged alerts from tools. Here the reports come from " +
                        "**people**, and the first hour decides how bad it gets."
            ),
            keyTerms = listOf(
                KeyTerm("Sort", "Decide which reports are real incidents."),
                KeyTerm("Preserve", "Keep the evidence exactly as it is."),
                KeyTerm("Tell", "Report through the official channel to the right people."),
                KeyTerm("Prioritise", "Work the most urgent incident first."),
                KeyTerm("Record", "Write down the facts for the people who take over.")
            ),
            sourceIds = listOf("nist-ir")
        ),
        LessonPage(
            title = "Event or incident?",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "An **event** is anything that can be observed on a system. An **incident** " +
                        "actually or nearly harms confidentiality, integrity or availability — " +
                        "**or breaks a security policy**.",
                "An attack that was **blocked** means the defence worked. Judge each report by " +
                        "**what was harmed**, not by how worried the caller sounds."
            ),
            visual = LessonVisual.Log(
                title = "FOUR REPORTS FROM AROUND CAMPUS",
                lines = listOf(
                    "EVENT     spam filter caught 40 phish",
                    "EVENT     planned reboot for patching",
                    "INCIDENT  grade printouts left in hall",
                    "INCIDENT  org page posting scam links"
                ),
                highlights = listOf(2, 3),
                caption = "The two incidents involve real harm: data seen, and an account misused."
            ),
            keyTerms = listOf(
                KeyTerm("Event", "Anything observable on a system or network."),
                KeyTerm("Incident", "An event that harms, or nearly harms, data or systems, or breaks a security policy.")
            ),
            sourceIds = listOf("nist-incident"),
            task = 1
        ),
        LessonPage(
            title = "Don't touch the evidence",
            icon = LessonIcon.LOCK,
            paragraphs = listOf(
                "Investigators rebuild what happened from emails, logs and what's still running. " +
                        "Deleting erases the **sender and headers**. Restarting wipes **memory** " +
                        "and changes **timestamps**. Forwarding **spreads the link**.",
                "In Level 101 you deleted a phishing email **after** reporting it — fine for an " +
                        "ordinary user. A first responder deletes **nothing** before " +
                        "investigators have seen it."
            ),
            visual = LessonVisual.DoDont(
                dos = listOf(
                    "Leave the device and account as they are",
                    "Write down what you saw, and when",
                    "Report it through the official channel"
                ),
                donts = listOf(
                    "Delete messages to tidy up",
                    "Restart or \"clean\" the computer",
                    "Forward the link as a warning"
                )
            ),
            keyTerms = listOf(
                KeyTerm("Volatile data", "Information that disappears when a computer is shut down or restarted.")
            ),
            sourceIds = listOf("sp80086"),
            task = 2
        ),
        LessonPage(
            title = "Tell the right people",
            icon = LessonIcon.EMAIL,
            paragraphs = listOf(
                "Report through the **official channel**. Never post about it publicly, and " +
                        "never confront the person you think caused it.",
                "If **personal data** is involved, the school's **DPO** must know. A breach of " +
                        "**sensitive personal information** that could cause serious harm goes " +
                        "to the **NPC within 72 hours** of the school first knowing."
            ),
            keyTerms = listOf(
                KeyTerm("DPO", "Data Protection Officer: the person responsible for personal data at the school."),
                KeyTerm("NPC", "National Privacy Commission: the government office that enforces the Data Privacy Act."),
                KeyTerm("Sensitive personal information", "Includes age or birthday, health, education records and government ID numbers like PhilHealth or SSS.")
            ),
            sourceIds = listOf("ra10173", "npc1603", "nist-ir"),
            task = 3
        ),
        LessonPage(
            title = "What goes first?",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "Ask first: **is it still happening?** Harm that's ongoing comes first, because " +
                        "every minute adds victims.",
                "Then weigh **how many people** and **how sensitive** the data is. Last, check " +
                        "what **already limits the damage**, like encryption or a remote lock."
            ),
            visual = LessonVisual.Log(
                title = "THREE INCIDENTS · WHICH FIRST?",
                lines = listOf(
                    "1  org page: scam posts still going up",
                    "2  grade printouts seen by 200 people",
                    "3  tablet left in a jeepney, encrypted"
                ),
                highlights = listOf(0),
                caption = "Still happening beats bigger but finished. Encryption buys time."
            ),
            sourceIds = listOf("nist-ir"),
            task = 4
        ),
        LessonPage(
            title = "Write what you saw",
            icon = LessonIcon.REPORT,
            paragraphs = listOf(
                "The report hands the case over, so write **facts, not guesses**. \"Nothing " +
                        "done yet\" is a valid answer.",
                "Two times matter: when it **started** and when it was **discovered**. The gap " +
                        "between them is how long the attacker went **unseen**."
            ),
            visual = LessonVisual.Log(
                title = "INCIDENT REPORT · ORG PAGE",
                lines = listOf(
                    "what        page posting scam links",
                    "started     Sat 11:05 PM, first post",
                    "discovered  Mon 7:40 AM, member msg",
                    "reported by org secretary, 7:52 AM",
                    "ongoing?    yes, 3 new posts today",
                    "done        nothing changed yet"
                ),
                highlights = listOf(1, 2),
                caption = "Started to discovered: over 56 hours unseen."
            ),
            sourceIds = listOf("nist-ir"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "The library door logged a badge that was refused entry at 2 AM. Nobody got " +
                    "in. Event or incident?",
            options = listOf(
                "An event: the lock did its job and nothing was harmed",
                "An incident: someone tried to get in after hours",
                "An incident: any 2 AM activity must be escalated",
                "Neither: refused badges aren't worth recording"
            ),
            correctIndex = 0,
            explanation = "The control worked and nothing was harmed, so it's an event. It's still " +
                    "worth logging: if the same badge keeps trying, the pattern may become an incident."
        ),
        LessonQuestion(
            prompt = "A staff member says their PC is acting strangely since they opened an " +
                    "attachment. What do you tell them?",
            options = listOf(
                "Restart it, so whatever is running stops",
                "Delete the email so nobody opens it again",
                "Run a full scan, then restart it tonight",
                "Stop using it, leave it on, report it now"
            ),
            correctIndex = 3,
            explanation = "Restarting and scanning change what investigators need to see, and " +
                    "deleting removes the email's evidence. Stop, preserve, report."
        ),
        LessonQuestion(
            prompt = "Which of these is sensitive personal information under the Data Privacy Act?",
            options = listOf(
                "A club's weekly meeting schedule",
                "A student's date of birth",
                "A section's class timetable",
                "A student's school email address"
            ),
            correctIndex = 1,
            explanation = "The law lists age as sensitive personal information. A school email is " +
                    "personal information, but not sensitive, and schedules aren't about one person."
        )
    )
)

// ---------------------------------------------------------------------------
// LEVEL 602 — Detection
// ---------------------------------------------------------------------------
// The briefing before the weekend replay. The lab's case is Prof. Ramos, a
// Frankfurt sign-in and a muted mail-forwarding rule, so the examples here are
// a library alert queue, a librarian's account and an admin-group rule.

private fun detectionLesson(): Lesson = Lesson(
    levelId = 602,
    title = "The Logs Saw It",
    sources = listOf(
        LessonSource(
            id = "nist-ir",
            short = "NIST SP 800-61r3",
            citation = "Nelson, A., Rekhi, S., Souppaya, M. & Scarfone, K. (2025). Incident " +
                    "Response Recommendations and Considerations for Cybersecurity Risk " +
                    "Management (NIST SP 800-61 Rev. 3): DE.CM, DE.CM-09, DE.AE-02, DE.AE-03.",
            url = "https://doi.org/10.6028/NIST.SP.800-61r3"
        ),
        LessonSource(
            id = "nist-logs",
            short = "NIST SP 800-92",
            citation = "Kent, K. & Souppaya, M. (2006). Guide to Computer Security Log " +
                    "Management (NIST SP 800-92).",
            url = "https://doi.org/10.6028/NIST.SP.800-92"
        ),
        LessonSource(
            id = "nist-fn",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"False negative.\" Glossary " +
                    "(definitions from NIST SP 800-83r1 and SP 800-86).",
            url = "https://csrc.nist.gov/glossary/term/false_negative"
        ),
        LessonSource(
            id = "nist-ioc",
            short = "NIST Glossary",
            citation = "NIST Computer Security Resource Center. \"Indicator.\" Glossary " +
                    "(definition from NIST SP 800-150).",
            url = "https://csrc.nist.gov/glossary/term/indicator"
        ),
        LessonSource(
            id = "sp80053",
            short = "NIST SP 800-53r5",
            citation = "Joint Task Force (2020). Security and Privacy Controls for Information " +
                    "Systems and Organizations (NIST SP 800-53 Rev. 5): SI-4, System " +
                    "Monitoring, and SI-4(13), Analyze Traffic and Event Patterns.",
            url = "https://doi.org/10.6028/NIST.SP.800-53r5"
        ),
        LessonSource(
            id = "mitre-va",
            short = "MITRE ATT&CK",
            citation = "MITRE ATT&CK. \"Valid Accounts\" (T1078): detection of geographic " +
                    "anomalies and impossible travel in sign-in logs.",
            url = "https://attack.mitre.org/techniques/T1078/"
        ),
        LessonSource(
            id = "mitre-groups",
            short = "MITRE ATT&CK",
            citation = "MITRE ATT&CK. \"Account Manipulation: Additional Local or Domain " +
                    "Groups\" (T1098.007): detecting unauthorized additions to privileged groups.",
            url = "https://attack.mitre.org/techniques/T1098/007/"
        ),
        LessonSource(
            id = "ra10173",
            short = "RA 10173",
            citation = "Republic of the Philippines. Republic Act No. 10173, Data Privacy " +
                    "Act of 2012, Sec. 20(c)(4): regular monitoring for security breaches.",
            url = "https://lawphil.net/statutes/repacts/ra2012/ra_10173_2012.html"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "The logs saw it",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "In Level 601, an attack ran for days before anyone noticed. **Detection** is " +
                        "the work of shrinking the gap between **started** and **discovered**.",
                "Most of it runs on a **SIEM**, the system behind the alerts you triaged in " +
                        "Level 102. For a school it isn't optional: the **Data Privacy Act** " +
                        "requires **regular monitoring for security breaches**."
            ),
            keyTerms = listOf(
                KeyTerm("Detection", "Noticing an attack while it's happening, not days later."),
                KeyTerm("SIEM", "Security information and event management: collects logs from many systems and raises alerts on them.")
            ),
            sourceIds = listOf("nist-ir", "nist-logs", "ra10173")
        ),
        LessonPage(
            title = "Wrong in both directions",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "Level 102 covered **false positives**. The opposite is worse: a **false " +
                        "negative** is a real attack the tool never flags at all.",
                "Too many false alarms cause **alert fatigue**: people skim, and the real one " +
                        "slips past. The **severity** label is only the tool's guess too. What " +
                        "settles an alert is whether something **explains it**."
            ),
            visual = LessonVisual.Log(
                title = "ALERT QUEUE · LIBRARY · SUNDAY",
                lines = listOf(
                    "HIGH  mass file delete  LIB-SRV-02",
                    "      note: nightly backup rotation",
                    "LOW   unknown USB drive REG-PC-07",
                    "      note: no ticket, user on leave"
                ),
                highlights = listOf(2, 3),
                caption = "The HIGH alert is a scheduled job. Nothing explains the LOW one."
            ),
            keyTerms = listOf(
                KeyTerm("False negative", "A real attack that the tool fails to flag."),
                KeyTerm("Alert fatigue", "Missing real alerts because there are too many false ones to read carefully.")
            ),
            sourceIds = listOf("nist-fn", "nist-ir"),
            task = 1
        ),
        LessonPage(
            title = "Normal for whom?",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "A **baseline** is what's normal for one account: where it signs in from, on " +
                        "which devices, at what hours. A sign-in that breaks **several habits " +
                        "at once** deserves a hard look.",
                "The classic sign is **impossible travel**: two successful sign-ins too far " +
                        "apart for one person to cover in the time between them."
            ),
            visual = LessonVisual.Log(
                title = "SIGN-IN LOG · m.reyes",
                lines = listOf(
                    "usual  Davao · phone, laptop · 7AM-6PM",
                    "07:41 OK  Davao      phone   192.0.2.14",
                    "08:01 OK  Singapore  new PC  192.0.2.88"
                ),
                highlights = listOf(2),
                caption = "Twenty minutes after the phone signed in from Davao, a device the " +
                        "account has never used signed in from another country."
            ),
            keyTerms = listOf(
                KeyTerm("Baseline", "What's normal for an account: its usual places, devices and hours."),
                KeyTerm("Impossible travel", "Two sign-ins too far apart for one person to have made both.")
            ),
            sourceIds = listOf("mitre-va", "sp80053", "nist-ir"),
            task = 2
        ),
        LessonPage(
            title = "Count what got in",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "In Level 102 you **pivoted** on an IOC to see what else it touched. Here you " +
                        "use it to measure **scope**: how many accounts the attacker actually reached.",
                "Count **successes** only. A **failed** sign-in means the attacker tried but " +
                        "didn't get in. Leave out the account you started from: you already " +
                        "know about it."
            ),
            visual = LessonVisual.Log(
                title = "SEARCH: 192.0.2.88 · 5 RESULTS",
                lines = listOf(
                    "08:01 OK    user=m.reyes",
                    "08:09 FAIL  user=p.garcia",
                    "08:11 OK    user=k.villa",
                    "08:14 OK    user=d.santos",
                    "08:20 FAIL  user=t.lopez"
                ),
                highlights = listOf(2, 3),
                caption = "Two more accounts were entered. The two failures were tries that " +
                        "didn't work."
            ),
            keyTerms = listOf(
                KeyTerm("Scope", "How far an incident reached: which accounts, machines and data.")
            ),
            sourceIds = listOf("nist-ioc", "nist-ir"),
            task = 3
        ),
        LessonPage(
            title = "Muted isn't fixed",
            icon = LessonIcon.LOCK,
            paragraphs = listOf(
                "A tool can see an attack and still miss it: the alert was **muted**, given " +
                        "the wrong **severity**, buried in **false alarms**, or sent to an " +
                        "inbox nobody watches.",
                "Muting a noisy rule leaves a **blind spot**. **Tuning** adds conditions so " +
                        "it ignores normal activity and still fires on the attack. The number " +
                        "to shrink is **time to detect**."
            ),
            visual = LessonVisual.Log(
                title = "DETECTION RULES · STATUS",
                lines = listOf(
                    "ON     10 failed sign-ins in 5 min",
                    "ON     malware found on a PC",
                    "MUTED  admin sign-in after hours",
                    "       Mar 3, j.cruz: \"fires nightly,",
                    "       backup job uses admin acct\""
                ),
                highlights = listOf(2, 3, 4),
                caption = "Muting silenced the backup job, and every real after-hours admin " +
                        "sign-in with it."
            ),
            keyTerms = listOf(
                KeyTerm("Blind spot", "Activity no rule is watching, often left by muting a noisy one."),
                KeyTerm("Tuning", "Adding conditions so a rule ignores normal activity but still catches attacks."),
                KeyTerm("Time to detect", "How long an attacker was inside before anyone noticed.")
            ),
            sourceIds = listOf("nist-ir", "sp80053"),
            task = 4
        ),
        LessonPage(
            title = "Specific, then tested",
            icon = LessonIcon.CODE,
            paragraphs = listOf(
                "Too **broad**, and a rule fires on everyone's normal work until someone " +
                        "mutes it. Too **narrow**, and it misses the attack. Build it on what " +
                        "makes the attack **different** from normal.",
                "Then **test** it on past logs before switching it on. The goal is **every** " +
                        "attack caught with as few false alarms as possible."
            ),
            visual = LessonVisual.Log(
                title = "RULE TEST · ADMIN GROUP · 30 DAYS",
                lines = listOf(
                    "rule                   caught  false",
                    "any change             2 of 2    41",
                    "change with no ticket  2 of 2     0",
                    "change at night only   1 of 2     0"
                ),
                highlights = listOf(2),
                caption = "IT adds people to the admin group every week, always with a " +
                        "ticket. Attackers don't file tickets."
            ),
            keyTerms = listOf(
                KeyTerm("Detection rule", "The conditions that decide when a SIEM raises an alert.")
            ),
            sourceIds = listOf("mitre-groups", "sp80053", "nist-ir"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "A stolen student account downloads 2,000 e-library files overnight. No " +
                    "rule fires. What is this?",
            options = listOf(
                "A false positive: nothing alerted, so nothing was wrong",
                "A false negative: a real attack that raised no alert",
                "A true positive: the download was real, just unflagged",
                "Alert fatigue: the desk was too busy to look at it"
            ),
            correctIndex = 1,
            explanation = "A false negative is a real attack the tool never flags. A true " +
                    "positive needs an alert to exist, and alert fatigue means an alert fired " +
                    "but nobody read it."
        ),
        LessonQuestion(
            prompt = "A teacher's laptop signs in from Imus at 8:02 AM. At 8:20 AM the same " +
                    "account signs in from Seoul on a phone it has never used. What gives " +
                    "the second sign-in away?",
            options = listOf(
                "It came at 8:20 AM, an hour when teachers rarely sign in",
                "Any sign-in from outside the Philippines is an attack",
                "Imus to Seoul in 18 minutes is impossible, on a new device",
                "Two sign-ins in one morning mean the password was shared"
            ),
            correctIndex = 2,
            explanation = "No one gets from Imus to Seoul in 18 minutes, and the phone is new " +
                    "to the account: two habits broken at once. Teachers do travel, so a " +
                    "foreign country alone proves nothing."
        ),
        LessonQuestion(
            prompt = "A \"USB storage plugged in\" alert fires 50 times a week, almost all from " +
                    "the AV room PC, where staff copy slides from approved drives. What's the " +
                    "best fix?",
            options = listOf(
                "Keep the rule, but skip the AV room's approved drives",
                "Mute the rule until the weekly count drops below 10",
                "Lower it to LOW so it stops interrupting the desk",
                "Delete the rule and let antivirus handle USB drives"
            ),
            correctIndex = 0,
            explanation = "Tuning removes the known, approved noise and keeps watching every " +
                    "other drive. Muting or deleting leaves a blind spot, and a LOW label " +
                    "just buries it."
        )
    )
)

// ---------------------------------------------------------------------------
// LEVEL 603 — Containment
// ---------------------------------------------------------------------------
// The briefing before Monday's containment. The lab's case is four hijacked
// staff accounts, a forwarding rule and a rogue MFA device, so the examples
// here carry on 601's hijacked org page: scheduled posts and a connected app.

private fun containmentLesson(): Lesson = Lesson(
    levelId = 603,
    title = "Stop It Spreading",
    sources = listOf(
        LessonSource(
            id = "nist-ir",
            short = "NIST SP 800-61r3",
            citation = "Nelson, A., Rekhi, S., Souppaya, M. & Scarfone, K. (2025). Incident " +
                    "Response Recommendations and Considerations for Cybersecurity Risk " +
                    "Management (NIST SP 800-61 Rev. 3): RS.MI, RS.MI-01, RS.MI-02, RS.AN-07, " +
                    "RS.AN-08.",
            url = "https://doi.org/10.6028/NIST.SP.800-61r3"
        ),
        LessonSource(
            id = "sp80086",
            short = "NIST SP 800-86",
            citation = "Kent, K., Chevalier, S., Grance, T. & Dang, H. (2006). Guide to " +
                    "Integrating Forensic Techniques into Incident Response (NIST SP 800-86): " +
                    "preserving integrity and maintaining the chain of custody.",
            url = "https://doi.org/10.6028/NIST.SP.800-86"
        ),
        LessonSource(
            id = "ree",
            short = "Rules on Electronic Evidence",
            citation = "Supreme Court of the Philippines (2001). Rules on Electronic Evidence, " +
                    "A.M. No. 01-7-01-SC, Rule 5 Sec. 2 (manner of authentication) and Rule 7 " +
                    "Sec. 1 (factors for assessing evidentiary weight).",
            url = "https://lawphil.net/courts/supreme/am/am_01-7-01_2001.html"
        ),
        LessonSource(
            id = "mitre-filter",
            short = "MITRE ATT&CK",
            citation = "MITRE ATT&CK. \"Filter Network Traffic\" (M1037): firewall rules and " +
                    "blocking traffic to known malicious IP addresses and domains.",
            url = "https://attack.mitre.org/mitigations/M1037/"
        ),
        LessonSource(
            id = "mitre-web",
            short = "MITRE ATT&CK",
            citation = "MITRE ATT&CK. \"Restrict Web-Based Content\" (M1021): restricting " +
                    "access to domains associated with phishing campaigns, and DNS filtering.",
            url = "https://attack.mitre.org/mitigations/M1021/"
        ),
        LessonSource(
            id = "mitre-am",
            short = "MITRE ATT&CK",
            citation = "MITRE ATT&CK. \"Account Manipulation\" (T1098), a persistence " +
                    "technique, including Device Registration (T1098.005).",
            url = "https://attack.mitre.org/techniques/T1098/"
        )
    ),
    pages = listOf(
        LessonPage(
            title = "Stop it spreading",
            icon = LessonIcon.SHIELD,
            paragraphs = listOf(
                "Remember the org page from Level 601's lesson? It's **still posting** scam " +
                        "links. **Containment** stops harm like that from **spreading** while " +
                        "the rest is worked out. Cleanup and blame come later.",
                "Level 102 matched the action to what was taken. With the harm still running, " +
                        "weigh each action three ways: **what it stops**, **who else it hits**, " +
                        "and **what evidence it destroys**."
            ),
            keyTerms = listOf(
                KeyTerm("Containment", "Stopping an incident from spreading or doing more harm."),
                KeyTerm("Eradication", "Removing what the attacker left behind, like malware or hidden access.")
            ),
            sourceIds = listOf("nist-ir")
        ),
        LessonPage(
            title = "Right size, right target",
            icon = LessonIcon.WARNING,
            paragraphs = listOf(
                "Pick the **smallest action that fully stops** the harm. Too big, and innocent " +
                        "people pay for it. Aimed at the **wrong target**, it stops nothing and " +
                        "can wipe out evidence.",
                "If the harm comes from an account, act on the **account**. As in Levels 204 " +
                        "and 205, that means ending its **sessions** too, because a new " +
                        "password alone leaves them open."
            ),
            visual = LessonVisual.Log(
                title = "ORG PAGE STILL POSTING · OPTIONS",
                lines = listOf(
                    "TOO BIG  delete the whole page",
                    "WRONG    reformat the officer's laptop",
                    "RIGHT    sign out every session, then",
                    "         reset the page's password"
                ),
                highlights = listOf(2, 3),
                caption = "Deleting the page loses the evidence and every follower. The " +
                        "attacker isn't on the laptop."
            ),
            keyTerms = listOf(
                KeyTerm("Proportionate", "No bigger than the harm it has to stop.")
            ),
            sourceIds = listOf("nist-ir"),
            task = 1
        ),
        LessonPage(
            title = "Snapshot, then scrub",
            icon = LessonIcon.LOCK,
            paragraphs = listOf(
                "In Level 601 a first responder touched **nothing**. Containment has to " +
                        "change things, so **save the evidence first**: copy it somewhere " +
                        "safe, then remove what the attacker set up.",
                "Note **who saved it and when**. Under the **Rules on Electronic Evidence**, " +
                        "whoever presents digital evidence must prove it's **authentic**, and " +
                        "courts weigh how it was **stored and handled**."
            ),
            visual = LessonVisual.Log(
                title = "EVIDENCE LOCKER · ORG PAGE",
                lines = listOf(
                    "item   12 scheduled scam posts",
                    "saved  Mon 8:05 AM by j.navarro",
                    "where  ITSO evidence locker",
                    "then   posts deleted Mon 8:07 AM"
                ),
                highlights = listOf(1, 3),
                caption = "Saved first, deleted second, each with a time and a name."
            ),
            keyTerms = listOf(
                KeyTerm("Chain of custody", "A record of who handled evidence, when, and what they did with it.")
            ),
            sourceIds = listOf("sp80086", "nist-ir", "ree"),
            task = 2
        ),
        LessonPage(
            title = "Every door, then the source",
            icon = LessonIcon.LINK,
            paragraphs = listOf(
                "Contain **every** affected account **at the same time**. Close one door at a " +
                        "time and the attacker notices, then hurries through the rest.",
                "Then block the **IOCs**. A **firewall block** stops an IP address reaching " +
                        "your systems. A **DNS block** stops your own devices finding a domain. " +
                        "Block exactly what the attacker used: a whole country also cuts off " +
                        "people who are abroad."
            ),
            visual = LessonVisual.Log(
                title = "TWO BLOCKS · WHAT EACH STOPS",
                lines = listOf(
                    "FIREWALL  192.0.2.150",
                    "  stops   it reaching campus systems",
                    "DNS       free-load.example",
                    "  stops   campus devices finding it"
                ),
                highlights = listOf(0, 2),
                caption = "Each block covers a different direction: traffic coming in, and " +
                        "lookups going out."
            ),
            keyTerms = listOf(
                KeyTerm("Firewall block", "Stops traffic to or from an IP address at the edge of the network."),
                KeyTerm("DNS block", "Stops devices on your network from looking up a domain, so its links go nowhere.")
            ),
            sourceIds = listOf("mitre-filter", "mitre-web", "nist-ir"),
            task = 3
        ),
        LessonPage(
            title = "Pull back the bait",
            icon = LessonIcon.EMAIL,
            paragraphs = listOf(
                "A DNS block only helps devices on **your network**. The phishing message " +
                        "itself must be **purged**: found in every inbox by sender, subject " +
                        "and link, and removed.",
                "Then see who **acted**. A click alone is rarely a compromise; **typing a " +
                        "password** into the fake page is. Each new victim gets **the same " +
                        "containment**, so the scope grows as you learn."
            ),
            visual = LessonVisual.Log(
                title = "LINK LOG · free-load.example",
                lines = listOf(
                    "09:02  CLICKED    s.rivera",
                    "09:05  CLICKED    m.tan",
                    "09:06  SUBMITTED  m.tan",
                    "09:11  CLICKED    a.cruz"
                ),
                highlights = listOf(2),
                caption = "Three people clicked. One typed a password, so that account needs " +
                        "containing too."
            ),
            keyTerms = listOf(
                KeyTerm("Purge", "Removing every copy of a message from all the mailboxes it reached.")
            ),
            sourceIds = listOf("nist-ir"),
            task = 4
        ),
        LessonPage(
            title = "Test the way back in",
            icon = LessonIcon.SEARCH,
            paragraphs = listOf(
                "Attackers expect to be found, so they leave a **way back in**: " +
                        "**persistence**. Level 204 showed forwarding rules and recovery " +
                        "addresses. Also check for an extra **MFA device** or a **connected app**.",
                "NIST calls removing these **eradication**, but do it now: until they're " +
                        "gone, the attacker can walk back in. You're done when a **test** " +
                        "proves it, not when it feels done."
            ),
            visual = LessonVisual.Log(
                title = "SIGN-IN METHODS · ORG PAGE",
                lines = listOf(
                    "phone  j.navarro's, added 2024",
                    "email  officers@cvsu.example, 2024",
                    "app    PostPal scheduler, Sun 1:14 AM"
                ),
                highlights = listOf(2),
                caption = "Nobody in the org added PostPal. With it, the attacker can keep " +
                        "posting after the reset."
            ),
            keyTerms = listOf(
                KeyTerm("Persistence", "A hidden way back in that an attacker leaves behind.")
            ),
            sourceIds = listOf("mitre-am", "nist-ir"),
            task = 5
        )
    ),
    questions = listOf(
        LessonQuestion(
            prompt = "A student's LMS account is posting scam links in 30 class forums. What's " +
                    "the right-sized first move?",
            options = listOf(
                "Take the LMS offline for everyone until it's cleaned",
                "Delete all 30 forums so the links vanish at once",
                "Ask the student to change their password tonight",
                "End the account's sessions and block sign-in now"
            ),
            correctIndex = 3,
            explanation = "The harm comes from one account, so act on it now. Taking the LMS " +
                    "offline punishes everyone, deleting the forums destroys evidence and class " +
                    "work, and a password change tonight leaves the sessions running."
        ),
        LessonQuestion(
            prompt = "On a hijacked account you find a hidden rule that deletes security " +
                    "warnings as they arrive. What comes first?",
            options = listOf(
                "Save the rule and its logs, noting who saved them and when",
                "Delete the rule at once, since every minute it runs counts",
                "Leave it running so investigators can watch the attacker",
                "Screenshot it on your phone and post it to the group chat"
            ),
            correctIndex = 0,
            explanation = "Saving takes seconds, then the rule goes. Deleting first destroys " +
                    "the evidence, leaving it running lets the harm continue, and a group-chat " +
                    "screenshot leaks the case and proves nothing about who handled it."
        ),
        LessonQuestion(
            prompt = "When is an account takeover fully contained?",
            options = listOf(
                "When the password has been changed to a strong new one",
                "When the owner says everything looks normal again",
                "When a test shows every way back in has been closed",
                "When the alert that first found it has stopped firing"
            ),
            correctIndex = 2,
            explanation = "A new password leaves sessions and added devices in place, owners " +
                    "can't see hidden rules, and a quiet alert proves nothing. Only testing " +
                    "every way back in shows the attacker is out."
        )
    )
)
