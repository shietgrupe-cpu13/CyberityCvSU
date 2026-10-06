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
