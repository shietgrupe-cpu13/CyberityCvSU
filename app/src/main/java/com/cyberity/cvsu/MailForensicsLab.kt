package com.cyberity.cvsu

// ===========================================================================
// LEVEL 302 CONTENT — Identifying Suspicious Emails, as a mail forensics bench
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt.
//
// Level 301 taught what phishing is. This level trains the reading skills:
// the real domain in a link, lookalike characters, where a link actually
// goes, what SPF/DMARC results prove, and spotting every tell in a message.

/** Clue ids reported by the forensics bench through the JS bridge. */
object MailForensicsClues {
    const val MAIL_OPENED_E1 = "mail_opened_e1"
    const val MAIL_OPENED_E2 = "mail_opened_e2"
    const val MAIL_OPENED_E3 = "mail_opened_e3"
    const val MAIL_OPENED_E4 = "mail_opened_e4"
    const val LINK_INSPECTED_E1 = "link_inspected_e1"
    const val DOMAIN_LOOKUP_E2 = "domain_lookup_e2"
    const val LINK_INSPECTED_E3 = "link_inspected_e3"
    const val HEADERS_ANALYZED_E4 = "headers_analyzed_e4"
    const val ALL_TELLS_E4 = "all_tells_e4"
    const val SITE_VISITED_E1 = "site_visited_e1"
    const val SITE_VISITED_E2 = "site_visited_e2"
    const val SITE_VISITED_E3 = "site_visited_e3"

    /** Dangerous: running the "PDF" attachment. */
    const val ATTACHMENT_RUN = "attachment_run"

    /** Dangerous: typing credentials into one of the fake pages. */
    const val CREDENTIALS_SUBMITTED = "credentials_submitted"
}

val mailForensicsClueLabels: Map<String, String> = mapOf(
    MailForensicsClues.MAIL_OPENED_E1 to "Opened E1 (scholarship stipend)",
    MailForensicsClues.MAIL_OPENED_E2 to "Opened E2 (bank login alert)",
    MailForensicsClues.MAIL_OPENED_E3 to "Opened E3 (grades posted)",
    MailForensicsClues.MAIL_OPENED_E4 to "Opened E4 (unpaid balance)",
    MailForensicsClues.LINK_INSPECTED_E1 to "Checked where E1's link really goes",
    MailForensicsClues.DOMAIN_LOOKUP_E2 to "Looked up E2's sender domain",
    MailForensicsClues.LINK_INSPECTED_E3 to "Checked where E3's link really goes",
    MailForensicsClues.HEADERS_ANALYZED_E4 to "Ran the header analyzer on E4",
    MailForensicsClues.ALL_TELLS_E4 to "Tagged all six tells in E4",
    MailForensicsClues.SITE_VISITED_E1 to "Opened E1's page in the sandbox browser",
    MailForensicsClues.SITE_VISITED_E2 to "Opened E2's page in the sandbox browser",
    MailForensicsClues.SITE_VISITED_E3 to "Opened E3's page in the sandbox browser"
)

fun mailForensicsLab(): LabDefinition = LabDefinition(
    levelId = 302,
    title = "Mail Forensics Bench",
    subtitle = "Identifying Suspicious Emails · ITSO",
    briefing = "A student forwarded four emails that made it past the filters. Each one hides " +
            "a different giveaway: a fake domain, a swapped letter, a link that goes " +
            "somewhere else, or a forged sender. Use the bench tools to find the proof. Your " +
            "own eyes are the last filter.\n\n" +
            "Revealing a link shows you where it goes; opening it in the sandbox browser shows " +
            "you what it serves. Both are safe here. Typing a password into one of those pages " +
            "is not, and costs a heart.",
    assetDir = "mail_forensics",
    startPage = "inbox.html",
    dangerousClues = listOf(
        MailForensicsClues.ATTACHMENT_RUN,
        MailForensicsClues.CREDENTIALS_SUBMITTED
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Read the domain right to left",
            objective = "E1's link starts with ched.gov.ph, but that isn't the site it opens. " +
                    "Tap the link to reveal the full address and submit the domain it really " +
                    "belongs to.",
            entryPage = "mail.html#e1",
            requiredClues = listOf(MailForensicsClues.LINK_INSPECTED_E1),
            lockedMessage = "Open E1 and tap its link to see the full address.",
            answer = LabAnswer.Text(
                accepted = listOf("stipend-verify.com", "www.stipend-verify.com"),
                placeholder = "e.g. example.com"
            ),
            hints = listOf(
                "Ignore everything after the first single slash (/). Only the part before it " +
                        "is the site.",
                "Read that part from the right. The domain is the last two pieces before the " +
                        "first slash."
            ),
            successFeedback = "stipend-verify.com. Anyone who owns a domain can put any words " +
                    "in front of it, including ched.gov.ph. The real owner is always at the " +
                    "right-hand end, just before the first slash.",
            failureFeedback = "That's not the part that decides who owns the site. Find the " +
                    "first single slash and read the two pieces just before it."
        ),

        LabTask(
            id = "t2",
            title = "Spot the swapped letter",
            objective = "E2's sender domain looks exactly like the bank's. Open the domain " +
                    "lookup tool, compare it with the real one, and identify what was changed.",
            entryPage = "lookup.html",
            requiredClues = listOf(MailForensicsClues.DOMAIN_LOOKUP_E2),
            lockedMessage = "Look up E2's sender domain in the domain lookup tool.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Nothing. It's the bank's real domain",
                    "'rn' was used in place of the letter 'm'",
                    "The first letter is a capital i (I), not a lowercase L (l)",
                    "A zero (0) was used in place of the letter 'o'"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "The lookup tool spells out every character one by one.",
                "Compare the very first character of both domains."
            ),
            successFeedback = "Capital I and lowercase l look identical in most email fonts. " +
                    "The lookup also shows the fake domain was registered this week, while the " +
                    "bank's real domain is decades old. A brand-new domain claiming to be an " +
                    "old institution is a strong warning sign.",
            failureFeedback = "Not that one. Look at the character-by-character breakdown in " +
                    "the lookup tool."
        ),

        LabTask(
            id = "t3",
            title = "Where does it really go?",
            objective = "E3's link shows the real CvSU portal address as its text. Tap it to " +
                    "see the actual destination and submit where it really leads.",
            entryPage = "mail.html#e3",
            requiredClues = listOf(MailForensicsClues.LINK_INSPECTED_E3),
            lockedMessage = "Open E3 and tap its link to see the real destination.",
            answer = LabAnswer.Text(
                accepted = listOf("45.61.87.200"),
                placeholder = "e.g. 10.0.0.1"
            ),
            hints = listOf(
                "The blue text of a link can say anything. Only the destination matters.",
                "The real destination is a bare number address, not a name."
            ),
            successFeedback = "45.61.87.200. The same address from the brute-force alert " +
                    "and the tampered grade in Unit 1. Link text is just a label the sender types. " +
                    "On a phone, long-press a link to preview its real destination before " +
                    "opening it.",
            failureFeedback = "That's the text shown on the link, not where it goes. Read " +
                    "the revealed destination."
        ),

        LabTask(
            id = "t4",
            title = "Forged sender",
            objective = "E4's From line says registrar@cvsu.edu.ph, exactly. Run the header " +
                    "analyzer on it. What proves CvSU didn't send it?",
            entryPage = "mail.html#e4",
            requiredClues = listOf(MailForensicsClues.HEADERS_ANALYZED_E4),
            lockedMessage = "Run the header analyzer on E4.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Nothing. The From address is correct, so it's genuine",
                    "SPF and DMARC failed: the server that sent it isn't allowed to send mail for cvsu.edu.ph",
                    "It was sent at night",
                    "The subject line is in capital letters"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "The From line is typed by the sender and can say anything.",
                "Look at the Authentication-Results lines and what they say about cvsu.edu.ph."
            ),
            successFeedback = "The From line is just text the sender types. SPF checks whether " +
                    "the sending server is on the domain's approved list, and DMARC says what to " +
                    "do when it isn't. Both failed: the mail came from a bulk mailer that CvSU " +
                    "never authorized.",
            failureFeedback = "That isn't proof. Read the authentication results in the " +
                    "header analyzer."
        ),

        LabTask(
            id = "t5",
            title = "Tag every tell",
            objective = "Turn on Tag mode in E4 and tap every warning sign in the message, " +
                    "headers included. There are six. When you've found them all, the bench " +
                    "generates a report code. Submit it.",
            entryPage = "mail.html#e4",
            requiredClues = listOf(MailForensicsClues.ALL_TELLS_E4),
            lockedMessage = "Find all six tells in E4 using Tag mode.",
            answer = LabAnswer.Flag(
                sha256 = "aef65b755784ba47cf8ccf5ea3d3d309010c8182fb8a8d8010f9be6233fec989"
            ),
            hints = listOf(
                "Pressure and threats count. So does a request to pay outside official channels.",
                "Check the attachment name and the Reply-To address too. Don't run the attachment."
            ),
            successFeedback = "All six: a threatening subject, a generic greeting, a short " +
                    "deadline, payment by GCash reply, a .pdf.exe attachment, and replies going " +
                    "to a free webmail address. One tell alone can be innocent. Several together " +
                    "almost never are.",
            failureFeedback = "Not the report code. It appears once all six tells are tagged."
        )
    )
)
