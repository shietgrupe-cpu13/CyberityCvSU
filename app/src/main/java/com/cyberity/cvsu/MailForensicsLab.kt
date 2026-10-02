package com.cyberity.cvsu

// ===========================================================================
// LEVEL 302 CONTENT — Identifying Suspicious Emails, as a mail forensics bench
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 301:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 03/mail_forensics.
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
    briefing = "Knowing what phishing is only helps if you can prove a message is fake. " +
            "That proof is almost never in the wording. It is in the details most people " +
            "skip: the real domain in a link, a single swapped letter, where a link actually " +
            "goes, and what the mail server says about who sent it.\n\n" +
            "A student forwarded four emails that made it past the CvSU filters. Each one " +
            "hides a different giveaway: a fake domain, a swapped letter, a link that " +
            "goes somewhere else, or a forged sender. You are on the forensics bench at the " +
            "IT Services Office, and your own eyes are the last filter.\n\n" +
            "Revealing a link shows you where it goes; opening it in the sandbox browser " +
            "shows you what it serves. Both are safe here. Typing a password into one of " +
            "those pages is not, and costs a heart. So does running an attachment.\n\n" +
            "Each task explains what to look for, then hands you the bench. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 03/mail_forensics",
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
                    "**Tap the link** to reveal the **full address** and submit the **domain it really " +
                    "belongs to**.",
            guide = listOf(
                "Every web address has **one owner**, and you find it in the **domain**. In an " +
                        "address like https://portal.cvsu.edu.ph/grades, the domain is everything " +
                        "between https:// and the **first single slash**: portal.cvsu.edu.ph.",
                "Domains are read **right to left**. The right-hand end is the part someone " +
                        "actually registered and paid for — cvsu.edu.ph. Anything to the left of " +
                        "it is a **label the owner chose freely**, and an owner can type any words " +
                        "there, including the name of a government agency.",
                "So ched.gov.ph.example.com does **not** belong to CHED. It belongs to " +
                        "**example.com**, and \"ched.gov.ph\" is just decoration in front of it. " +
                        "Attackers rely on people reading left to right and **stopping at the " +
                        "first familiar name**.",
                "There is a second trick on top of that: the **blue text of a link** can be " +
                        "different from where it really goes. The bench reveals the **real " +
                        "destination** when you tap a link, without ever opening it."
            ),
            steps = listOf(
                "Open the bench. **Four emails** are listed, E1 to E4.",
                "Open **E1**, the scholarship stipend email.",
                "**Tap the blue link** in the message. A box appears showing where it **really " +
                        "goes**.",
                "In that revealed address, find the **first single slash** after https:// and " +
                        "ignore everything after it.",
                "Read what's left **from the right**. Take the **last two pieces** — that is the " +
                        "real domain.",
                "Optional: tap **OPEN THIS PAGE IN THE SANDBOX** to see what it serves. Look, but " +
                        "**don't type anything** into it.",
                "**Swipe the bar** at the top down and type the **real domain**."
            ),
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
            objective = "E2's sender domain looks exactly like the bank's. **Open the domain " +
                    "lookup tool**, compare it with the real one, and **identify what was changed**.",
            guide = listOf(
                "Some fake domains don't add extra words at all. They **copy the real domain " +
                        "and change one character** to something that looks the same. This is " +
                        "called a **lookalike** (or **homoglyph**) domain.",
                "The classic swaps are a **capital I for a lowercase l**, the digit **0 for the " +
                        "letter o**, and **rn for m**. In most email fonts these are almost " +
                        "impossible to tell apart at a glance, especially on a small phone " +
                        "screen.",
                "Reading slowly helps, but investigators use a tool that **spells the domain " +
                        "out one character at a time**. A domain lookup also shows **when the " +
                        "domain was registered**. A real bank's domain is decades old. A " +
                        "**domain registered a few days ago** that claims to be that bank is a " +
                        "strong warning sign on its own."
            ),
            steps = listOf(
                "Open the bench and open **E2**, the bank sign-in alert.",
                "Look at the **From address**. It probably looks completely normal.",
                "Tap **OPEN DOMAIN LOOKUP** below the message.",
                "Compare the **two character breakdowns**: the sender's domain on top, the " +
                        "bank's real domain below. The highlighted character is the one to check.",
                "Also compare the **Registered** dates under each one.",
                "**Swipe the bar** down and pick **what was changed**."
            ),
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
            objective = "E3's link shows the real CvSU portal address as its text. **Tap it** to " +
                    "see the **actual destination** and submit where it really leads.",
            guide = listOf(
                "A link has **two parts**: the **text you see** and the **destination it opens**. " +
                        "The sender types both, and **they don't have to match**. A link can " +
                        "display portal.cvsu.edu.ph and still open a completely different " +
                        "server.",
                "E3 even comes from a believable sender and talks about something you'd " +
                        "expect — grades being posted. That's why **checking the destination " +
                        "matters more than how the email looks**.",
                "A destination made of **bare numbers**, like 10.0.0.1, is an **IP address**: " +
                        "the server itself, with no name. Real university and bank sites use " +
                        "names. A sign-in page served from a **naked number address**, often " +
                        "without https, is a strong warning sign.",
                "On your own phone, **long-press a link** to preview where it goes before you " +
                        "open it. Here, tapping the link on the bench reveals the destination " +
                        "safely."
            ),
            steps = listOf(
                "Open the bench and open **E3**, the grades email.",
                "Notice the **link text**: it shows the real portal address.",
                "**Tap the link** to reveal where it **really goes**.",
                "Compare the revealed destination with the link text. They are **not the same**.",
                "Optional: open the page **in the sandbox** and check the **Site owner** line " +
                        "above it. **Don't sign in.**",
                "**Swipe the bar** down and type the **real destination address** (just the " +
                        "number part, without http:// or /grades)."
            ),
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
            objective = "E4's From line says registrar@cvsu.edu.ph, exactly. **Run the header " +
                    "analyzer** on it. What **proves CvSU didn't send it**?",
            guide = listOf(
                "The **From line** of an email works like the return address on an envelope: " +
                        "**the sender writes it themselves**. Anyone can put " +
                        "registrar@cvsu.edu.ph there, the same way anyone can write the " +
                        "Registrar's name on an envelope. This is called **spoofing**.",
                "What the sender **can't** fake is the check done by the **receiving mail " +
                        "server**. It records the result in the email's **headers**, the hidden " +
                        "technical lines above the message, under **Authentication-Results**.",
                "**SPF** checks whether the server that delivered the mail is on the domain's " +
                        "**list of approved senders**. **DKIM** checks a digital signature the " +
                        "real domain adds to its mail. **DMARC** tells receivers what to do " +
                        "when those checks fail. **spf=fail** and **dmarc=fail** for cvsu.edu.ph " +
                        "means **CvSU's mail system did not send this message**.",
                "Two more header lines are worth reading: **Reply-To**, where your reply would " +
                        "actually go, and **Return-Path**, where the mail really came from."
            ),
            steps = listOf(
                "Open the bench and open **E4**, the unpaid balance notice.",
                "Look at the **From** line. It shows the Registrar's real address.",
                "Tap **RUN HEADER ANALYZER** below the message.",
                "Read the **highlighted lines**: Reply-To, the **spf** result and the **dmarc** " +
                        "result.",
                "**Don't tap the attachment.** You'll deal with it in the next task.",
                "**Swipe the bar** down and choose what **proves** CvSU didn't send it."
            ),
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
            objective = "**Turn on Tag mode** in E4 and **tap every warning sign** in the message, " +
                    "headers included. There are **six**. When you've found them all, the bench " +
                    "generates a **report code**. Submit it.",
            guide = listOf(
                "A **tell** is a single warning sign in a message. **One tell alone can be " +
                        "innocent** — real offices send urgent reminders too. What gives phishing " +
                        "away is **several tells together**.",
                "The common ones are: a **generic greeting** like \"Dear Student\" when the " +
                        "sender should know your name; **threats and false urgency** (\"pay " +
                        "TODAY or be dropped\"); a request to **pay or reply outside official " +
                        "channels**; and an **attachment that isn't what it claims**.",
                "Watch file names closely. **statement.pdf.exe** ends in **.exe**, so it is a " +
                        "**program**, not a document. Phones and computers often hide the last " +
                        "part of a long name, so you only see \"...pdf\". **Never open it** — in " +
                        "this lab, running it **costs a heart**.",
                "The **headers** count too. A **Reply-To** that points to a **free webmail " +
                        "address**, and **failed SPF/DMARC** results, are tells even when the " +
                        "message itself reads perfectly."
            ),
            steps = listOf(
                "Open the bench and open **E4** again.",
                "If the headers aren't showing, tap **RUN HEADER ANALYZER** first. The tag " +
                        "button only appears after that.",
                "Tap **TURN ON TAG MODE**. A counter shows how many tells you've found out of " +
                        "**six**.",
                "**Tap each warning sign** in the message: the greeting, the threat, the " +
                        "payment request and the attachment. With Tag mode on, tapping the " +
                        "attachment **tags it instead of running it**.",
                "Scroll to the headers and tap the **two header tells**: Reply-To and the " +
                        "SPF result.",
                "When all six are tagged, a **report code** appears. **Swipe the bar** down and " +
                        "type it **exactly as written**, braces included."
            ),
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
            successFeedback = "All six: a generic \"Dear Student\" greeting, a threat with a " +
                    "same-day deadline, payment by GCash reply, a .pdf.exe attachment, replies " +
                    "going to a free webmail address, and failed SPF/DMARC checks. One tell " +
                    "alone can be innocent. Several together almost never are.",
            failureFeedback = "Not the report code. It appears once all six tells are tagged."
        )
    )
)
