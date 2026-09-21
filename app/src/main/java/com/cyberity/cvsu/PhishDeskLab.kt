package com.cyberity.cvsu

// ===========================================================================
// LEVEL 301 CONTENT — What is Phishing?, as the ITSO phish-report desk
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 101-102:
// every task teaches first (guide), says exactly what to do (steps), then asks.
//
// Level 101 had students investigate one phishing email. This level widens
// the lens: what makes a message phishing at all, the four shapes it takes,
// and why the mail filters can't catch it for you.

/** Clue ids reported by the phish desk through the JS bridge. */
object PhishDeskClues {
    const val REPORT_OPENED_R1 = "report_opened_r1"
    const val REPORT_OPENED_R2 = "report_opened_r2"
    const val REPORT_OPENED_R3 = "report_opened_r3"
    const val REPORT_OPENED_R4 = "report_opened_r4"
    const val REPORT_OPENED_R5 = "report_opened_r5"
    const val GATEWAY_OPENED_R2 = "gateway_opened_r2"
    const val HEADERS_OPENED_R3 = "headers_opened_r3"
    const val ATTACHMENT_PREVIEWED = "attachment_previewed"
    const val PAGE_INSPECTED = "page_inspected"
    const val SITE_VISITED_R1 = "site_visited_r1"
    const val SITE_VISITED_R2 = "site_visited_r2"

    /** Dangerous: typing a password into the fake sign-in page. */
    const val CREDENTIALS_SUBMITTED = "credentials_submitted"
}

val phishDeskClueLabels: Map<String, String> = mapOf(
    PhishDeskClues.REPORT_OPENED_R1 to "Read R1 · wallet suspension",
    PhishDeskClues.REPORT_OPENED_R2 to "Read R2 · thesis revisions",
    PhishDeskClues.REPORT_OPENED_R3 to "Read R3 · urgent payment",
    PhishDeskClues.REPORT_OPENED_R4 to "Read R4 · re-sent schedule",
    PhishDeskClues.REPORT_OPENED_R5 to "Read R5 · enrollment schedule",
    PhishDeskClues.GATEWAY_OPENED_R2 to "Opened the mail gateway scan on R2",
    PhishDeskClues.HEADERS_OPENED_R3 to "Opened the full headers on R3",
    PhishDeskClues.ATTACHMENT_PREVIEWED to "Previewed R4's attachment safely",
    PhishDeskClues.PAGE_INSPECTED to "Inspected the attachment's page code",
    PhishDeskClues.SITE_VISITED_R1 to "Opened R1's link in the sandbox browser",
    PhishDeskClues.SITE_VISITED_R2 to "Opened R2's link in the sandbox browser"
)

fun phishDeskLab(): LabDefinition = LabDefinition(
    levelId = 301,
    title = "Phish Report Desk",
    subtitle = "What is Phishing? · ITSO",
    briefing = "Phishing is a message that pretends to be someone you trust so that you hand " +
            "over something valuable: a password, money, or a way into the network. It is the " +
            "most common attack on any campus, because it skips the technology entirely and " +
            "aims at the person reading.\n\n" +
            "You are on the desk at the CvSU IT Services Office. Five messages were forwarded " +
            "here this week by students and staff who weren't sure about them. Four are " +
            "phishing. One is a perfectly ordinary email that somebody reported just to be " +
            "safe.\n\n" +
            "Every link here opens in a sandbox browser: a safe copy of the real page that " +
            "cannot send anything anywhere. Open them. Seeing what a phishing link serves is " +
            "the whole point, and it is the only place you will ever get to do it safely.\n\n" +
            "Each task explains what to look for, then hands you the report desk. Open it with " +
            "the button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "phish_desk",
    startPage = "queue.html",
    dangerousClues = listOf(PhishDeskClues.CREDENTIALS_SUBMITTED),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Find the genuine one",
            objective = "Read all five reported messages, then pick the one that is NOT " +
                    "phishing.",
            guide = listOf(
                "Before you can spot a fake, you need to know what the real thing looks like. " +
                        "Genuine messages from an office you already deal with — the registrar, " +
                        "the library, your department — tell you something and then leave the " +
                        "next move to you. They inform. Read the schedule, come to the counter, " +
                        "check the portal you already use.",
                "Phishing always wants an action, and it wants it from you now: sign in here, " +
                        "open this file, send this payment, reply to confirm. That request is " +
                        "the whole point of the message, so it is the fastest thing to look for.",
                "You can also open the links. Each one opens in the sandbox browser, where " +
                        "the page is a safe copy: look at what it asks for and at the address " +
                        "above it. Just don't type a password into any of them — in this lab " +
                        "that costs you a heart, and in real life it costs the account.",
                "The second thing to check is the address the message came from. Everything " +
                        "before the @ is decoration anyone can type. The part after the @ is the " +
                        "domain, and only its owner can send from it. Two of these reports look " +
                        "like the same registrar announcement — reading their domains slowly, " +
                        "one character at a time, is what separates them."
            ),
            steps = listOf(
                "Open the report desk. Five reports are listed, R1 to R5.",
                "Tap each one, read it, then use ← Reports to come back. Read all five before " +
                        "you decide anything.",
                "For each message ask: what is it asking me to DO? Sign in, pay, reply, open " +
                        "a file — or nothing at all?",
                "Where a message has a blue link, tap it. The sandbox browser shows the page " +
                        "it opens, and the address bar above it shows who really owns that page.",
                "Compare R4 and R5 carefully. They say almost the same thing, but their " +
                        "sender addresses are not the same.",
                "Swipe the bar at the top down and pick the message that asks you for nothing."
            ),
            entryPage = "queue.html",
            requiredClues = listOf(
                PhishDeskClues.REPORT_OPENED_R1,
                PhishDeskClues.REPORT_OPENED_R2,
                PhishDeskClues.REPORT_OPENED_R3,
                PhishDeskClues.REPORT_OPENED_R4,
                PhishDeskClues.REPORT_OPENED_R5
            ),
            lockedMessage = "Read all five reports first — a message only looks odd next to " +
                    "the ones that are normal.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "R1 · from gcash-ph-alerts.example · asks you to verify your wallet in 24 hours",
                    "R2 · from cvsu.edu.ph · asks you to sign in to read thesis comments",
                    "R3 · from cvsu-edu.ph.example · asks for a ₱385,000 transfer today",
                    "R4 · from cvsu.edu-ph.example · asks you to open the corrected attachment",
                    "R5 · from cvsu.edu.ph · asks you to read a schedule, nothing else"
                ),
                correctIndex = 4
            ),
            hints = listOf(
                "Four of these want you to do something urgently. One just tells you when " +
                        "enrollment is.",
                "R5 comes from the university's real domain, cvsu.edu.ph, and asks for no " +
                        "sign-in, no payment and no reply."
            ),
            successFeedback = "R5 is the real registrar notice: real domain, ordinary PDF, " +
                    "nothing asked of you. Notice that R2 also comes from the real cvsu.edu.ph " +
                    "domain — a genuine address is not proof on its own, which is exactly what " +
                    "the next tasks are about.",
            failureFeedback = "That one is phishing. Go back and look for the message that " +
                    "asks you to do nothing at all, and comes from the plain cvsu.edu.ph domain."
        ),

        LabTask(
            id = "t2",
            title = "How well did they know her?",
            objective = "R2 was written for one specific student. Read it again, look at who " +
                    "it was sent to, and name the kind of phishing it is.",
            guide = listOf(
                "Phishing comes in four common shapes, and they differ by how much homework " +
                        "the attacker did.",
                "MASS phishing is the same message sent to thousands of strangers at once. It " +
                        "has to stay generic — \"Dear Customer\" — because the sender has no idea " +
                        "who is reading. SPEAR phishing is the opposite: one target, researched " +
                        "first, so the message can mention real names, real deadlines and real " +
                        "work. It is far more convincing, because everything in it checks out.",
                "WHALING is spear phishing aimed at someone senior — a dean, a president, an " +
                        "administrator — usually to move money, because those people can " +
                        "authorise it. CLONE phishing takes a real message the target already " +
                        "received and re-sends a near-identical copy with the link or attachment " +
                        "swapped for a hostile one.",
                "The quickest way to tell them apart is the To line and the level of detail. " +
                        "Thousands of recipients and no name means mass. One recipient and " +
                        "details only an insider would know means spear."
            ),
            steps = listOf(
                "Open the report desk and open R2 again.",
                "Look at the To line: how many people received this message?",
                "Look at what the sender knows: the student's first name, her thesis title, " +
                        "her adviser, her defense date.",
                "Ask yourself how long it would take to collect those details for one person, " +
                        "and whether that effort makes sense for thousands.",
                "Swipe down and name the shape this attack takes."
            ),
            entryPage = "report.html#r2",
            requiredClues = listOf(PhishDeskClues.REPORT_OPENED_R2),
            lockedMessage = "Open report R2 and read it before naming the type.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Mass phishing — the same message blasted out to thousands of strangers",
                    "Spear phishing — written for one person, using real details about them",
                    "Whaling — aimed at a senior official who can authorise payments",
                    "Clone phishing — a copy of a real message with the attachment swapped"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Check the To line. Is it one person, or thousands?",
                "The sender knew her thesis title, her adviser and her defense date. That is " +
                        "research about one target."
            ),
            successFeedback = "Spear phishing. One recipient, and details that took real " +
                    "research: her thesis title, her adviser's name, her defense date. Nothing " +
                    "in it feels wrong, which is precisely why it works. For comparison, R1 is " +
                    "mass phishing, R3 is whaling, and R4 is a clone of R5.",
            failureFeedback = "Not that one. Look again at the To line — this message went to " +
                    "exactly one student, and it mentions her own thesis by name."
        ),

        LabTask(
            id = "t3",
            title = "Why the filter let it through",
            objective = "R2 passed every automatic check the mail system ran. Open its mail " +
                    "gateway scan and submit the CvSU account that actually sent it.",
            guide = listOf(
                "Mail filters do two useful things. They check attachments and links against " +
                        "lists of known-bad files and sites, and they check whether the sending " +
                        "server is allowed to send mail for the domain in the From line. That " +
                        "second check has names you'll see in the scan: SPF and DKIM. When they " +
                        "say \"pass\", it means the message really did come from that domain's " +
                        "own mail system.",
                "Here is the gap. Those checks answer \"did this come from where it claims?\" " +
                        "They cannot answer \"is the person at the keyboard the account's real " +
                        "owner?\" If an attacker steals someone's password, every check passes " +
                        "perfectly, because the mail genuinely is coming from inside.",
                "That is what happened to R2. The adviser's own account was taken over last " +
                        "week, so the message sailed through with a clean verdict. A clean scan " +
                        "means nothing was detected — not that the message is safe."
            ),
            steps = listOf(
                "Open the report desk and open R2.",
                "Scroll to the Investigation section below the message.",
                "Tap \"Mail gateway scan\" to expand it and read the verdict lines.",
                "The SPF line names the mailbox the message was really sent from.",
                "Swipe down and type that address, exactly as it is written in the scan."
            ),
            entryPage = "report.html#r2",
            requiredClues = listOf(PhishDeskClues.GATEWAY_OPENED_R2),
            lockedMessage = "Expand the \"Mail gateway scan\" box on report R2 first.",
            answer = LabAnswer.Text(
                accepted = listOf(
                    "m.santos@cvsu.edu.ph",
                    "m.santos",
                    "santos"
                ),
                placeholder = "name@cvsu.edu.ph"
            ),
            hints = listOf(
                "It's the first line of the gateway scan, the one starting with SPF.",
                "Tap R2's link too: the page it opens asks for the same CvSU password the " +
                        "attacker used to get into this mailbox in the first place.",
                "The sending mailbox belongs to the thesis adviser herself."
            ),
            successFeedback = "m.santos@cvsu.edu.ph — the adviser's real account. Her password " +
                    "was phished the week before, so the attacker was sending from inside a " +
                    "trusted mailbox. Filters check where mail comes from, never who is really " +
                    "typing it. That last check is you.",
            failureFeedback = "Not quite. Expand the mail gateway scan on R2 and copy the " +
                    "address on the SPF line, the one ending in @cvsu.edu.ph."
        ),

        LabTask(
            id = "t4",
            title = "Follow the hook",
            objective = "R4's attachment is not the PDF it claims to be. Preview it safely, " +
                    "inspect its code, and submit the flag the attacker left behind.",
            guide = listOf(
                "Every phishing message has a hook: the one thing it needs you to do. Usually " +
                        "it is a link or an attachment, and the attachment here is worth a close " +
                        "look. Its name ends in .pdf.html. Phones and computers hide the ending " +
                        "of long file names, so a student glancing at it sees \"...pdf\" and " +
                        "expects a document. What actually opens is a web page.",
                "That page is built to look exactly like the CvSU portal sign-in, complete with " +
                        "a drawn-in address bar showing the real portal address. It is a picture, " +
                        "not a real address bar. Anything typed into the form is sent straight " +
                        "to the attacker, and the page then forwards you to the genuine portal " +
                        "so nothing seems to have gone wrong.",
                "You will open it in a sandbox, which is a safe copy that cannot send anything " +
                        "anywhere. Investigators do this to study an attack without becoming a " +
                        "victim of it. Do not type a password into it even here — the lab " +
                        "watches for that, and it costs you a heart."
            ),
            steps = listOf(
                "Open the report desk and open R4.",
                "Tap the attachment at the bottom of the message to open it in the sandbox.",
                "Look at the fake sign-in page, but do not type anything into it.",
                "Tap INSPECT PAGE to reveal the code behind the form.",
                "Find the hidden field holding a CYBERITY{...} value, and type it in below " +
                        "exactly as written, braces included."
            ),
            entryPage = "report.html#r4",
            requiredClues = listOf(
                PhishDeskClues.ATTACHMENT_PREVIEWED,
                PhishDeskClues.PAGE_INSPECTED
            ),
            lockedMessage = "Preview R4's attachment, then tap INSPECT PAGE to see its code.",
            answer = LabAnswer.Flag(
                sha256 = "765f29add5dd1658f18a27475ac4a46e3d907e0e693ee9710c6130cdd7a99b60"
            ),
            hints = listOf(
                "The attachment is at the bottom of R4. The INSPECT PAGE button is under the " +
                        "fake sign-in form.",
                "In the code, look for the line containing name=\"kit\" — the flag is its value."
            ),
            successFeedback = "Captured. The form sends whatever is typed to the attacker's " +
                    "server, then sends the student on to the real portal so the theft goes " +
                    "unnoticed. The bait was a familiar announcement, the hook was the " +
                    "attachment, and the catch would have been a CvSU password.",
            failureFeedback = "Not the flag. It sits in the page code after INSPECT PAGE, on " +
                    "the hidden line with name=\"kit\". Copy it with the CYBERITY{ } wrapper."
        ),

        LabTask(
            id = "t5",
            title = "No link, no attachment",
            objective = "R3 contains no link and no attachment, so there is nothing for any " +
                    "scanner to check. Read its full headers and decide what the administrator " +
                    "should do.",
            guide = listOf(
                "Not every phishing message carries a payload. Some of the most expensive ones " +
                        "are plain text, because plain text is invisible to security software: " +
                        "there is no file to scan and no link to check. All the attacker wants " +
                        "is a reply and an action at the other end.",
                "R3 is that kind of attack. It claims to come from the University President, " +
                        "it is marked confidential so the reader won't check with anyone, and it " +
                        "sets a deadline of 3:00 PM so there is no time to think. The catch is " +
                        "₱385,000, and the hook is simply hitting reply and paying.",
                "The headers give it away in two places. The domain is cvsu-edu.ph.example, " +
                        "which is not cvsu.edu.ph. And the Reply-To line — the address a reply " +
                        "would actually go to — is a free webmail account, not the President's " +
                        "office at all. The defence for this kind of request never lives in the " +
                        "message itself: you confirm it through a channel you already trust, " +
                        "like phoning the office on a number you looked up yourself."
            ),
            steps = listOf(
                "Open the report desk and open R3.",
                "Read what the message actually asks for, and by when.",
                "In the Investigation section, tap \"Full headers\" to expand it.",
                "Compare the From address with the Reply-To address — they are not the same.",
                "Swipe down and choose the response that checks the request without trusting " +
                        "the message."
            ),
            entryPage = "report.html#r3",
            requiredClues = listOf(PhishDeskClues.HEADERS_OPENED_R3),
            lockedMessage = "Expand the \"Full headers\" box on report R3 first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Nothing — with no link or attachment, the message can't do any harm",
                    "Run an antivirus scan on the email to find the hidden malware",
                    "Don't reply. Phone the President's office on a number you looked up " +
                            "yourself and confirm before any money moves",
                    "Reply asking the sender to confirm that the request is genuine"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "The message wants a reply and a payment. Where would a reply actually go?",
                "If the request is fake, asking the sender to confirm it just asks the " +
                        "attacker whether the attacker is real."
            ),
            successFeedback = "Exactly right. A reply goes to the attacker's webmail account, " +
                    "so replying proves nothing. Checking through a separate, known channel — " +
                    "a phone number you looked up, not one from the email — defeats this whole " +
                    "class of attack, and it costs one minute.",
            failureFeedback = "Look again at where a reply would go, and remember there is no " +
                    "malware here to scan for. The request itself is the attack."
        )
    )
)
