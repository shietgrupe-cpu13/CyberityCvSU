package com.cyberity.cvsu

// ===========================================================================
// LEVEL 301 CONTENT — What is Phishing?, as the ITSO phish-report desk
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same as the Unit 1 labs.
//
// Level 101 had students investigate one phishing email. This level widens
// the lens: what makes something phishing, the main types (mass, spear,
// whaling, clone), and why it gets past technical controls.

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

    /** Dangerous: typing a password into the fake sign-in page. */
    const val CREDENTIALS_SUBMITTED = "credentials_submitted"
}

val phishDeskClueLabels: Map<String, String> = mapOf(
    PhishDeskClues.REPORT_OPENED_R1 to "Opened report R1 (wallet suspension)",
    PhishDeskClues.REPORT_OPENED_R2 to "Opened report R2 (thesis revisions)",
    PhishDeskClues.REPORT_OPENED_R3 to "Opened report R3 (urgent payment)",
    PhishDeskClues.REPORT_OPENED_R4 to "Opened report R4 (re-sent schedule)",
    PhishDeskClues.REPORT_OPENED_R5 to "Opened report R5 (enrollment schedule)",
    PhishDeskClues.GATEWAY_OPENED_R2 to "Read the mail gateway scan for R2",
    PhishDeskClues.HEADERS_OPENED_R3 to "Read the full headers of R3",
    PhishDeskClues.ATTACHMENT_PREVIEWED to "Previewed R4's attachment in the sandbox",
    PhishDeskClues.PAGE_INSPECTED to "Inspected the attachment's page code"
)

fun phishDeskLab(): LabDefinition = LabDefinition(
    levelId = 301,
    title = "Phish Report Desk",
    subtitle = "What is Phishing? · ITSO",
    briefing = "Phishing is a message that pretends to be someone you trust so you'll hand " +
            "over something: a password, money, or access. This week, five messages were " +
            "forwarded to the CvSU IT Services Office's phish desk. Four of them are phishing " +
            "and one is genuine. Work out which is which, what type each one is, and why the " +
            "mail filters let them through.",
    assetDir = "phish_desk",
    startPage = "queue.html",
    dangerousClues = listOf(PhishDeskClues.CREDENTIALS_SUBMITTED),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Find the real one",
            objective = "Open all five reported messages. One of them is a genuine message " +
                    "that a student reported by mistake. Which one?",
            entryPage = "queue.html",
            requiredClues = listOf(
                PhishDeskClues.REPORT_OPENED_R1,
                PhishDeskClues.REPORT_OPENED_R2,
                PhishDeskClues.REPORT_OPENED_R3,
                PhishDeskClues.REPORT_OPENED_R4,
                PhishDeskClues.REPORT_OPENED_R5
            ),
            lockedMessage = "Open all five reports before you decide.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "R1 · GCash wallet will be suspended",
                    "R2 · Re: Chapter 3 revisions",
                    "R3 · Confidential payment request",
                    "R4 · RE-SENT: Enrollment schedule (corrected)",
                    "R5 · Enrollment schedule, 2nd semester"
                ),
                correctIndex = 4
            ),
            hints = listOf(
                "R4 and R5 look almost the same. Compare their sender addresses letter by letter.",
                "The genuine one comes from the real cvsu.edu.ph domain and asks you to do " +
                        "nothing except read a schedule."
            ),
            successFeedback = "R5 is the real registrar notice. It comes from cvsu.edu.ph, " +
                    "carries an ordinary PDF, and asks for nothing. R4 is a copy of it built to " +
                    "look identical, which is exactly what makes it dangerous.",
            failureFeedback = "That one is phishing. Look for the message that asks you for " +
                    "nothing and comes from the university's real domain."
        ),

        LabTask(
            id = "t2",
            title = "Name the type",
            objective = "The other four are phishing, but not the same kind. Match each report " +
                    "to its type: mass phishing, spear phishing, whaling, or clone phishing.",
            requiredClues = listOf(
                PhishDeskClues.REPORT_OPENED_R1,
                PhishDeskClues.REPORT_OPENED_R2,
                PhishDeskClues.REPORT_OPENED_R3,
                PhishDeskClues.REPORT_OPENED_R4
            ),
            lockedMessage = "Open reports R1 to R4 first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "R1 Spear · R2 Mass · R3 Clone · R4 Whaling",
                    "R1 Mass · R2 Spear · R3 Whaling · R4 Clone",
                    "R1 Mass · R2 Whaling · R3 Spear · R4 Clone",
                    "R1 Clone · R2 Spear · R3 Mass · R4 Whaling"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Look at who each message was sent to, and how much the sender knows about them.",
                "Mass = thousands of strangers. Spear = one person, researched. Whaling = a " +
                        "senior person with authority over money. Clone = a copy of a real message."
            ),
            successFeedback = "R1 went to 4,212 people with 'Dear Customer'. R2 knew one " +
                    "student's thesis title and adviser. R3 aimed at the official who can release " +
                    "funds. R4 copied the registrar's real email. The more research, the more " +
                    "convincing, and the harder to spot.",
            failureFeedback = "At least one is mismatched. Check the To line of each message " +
                    "and how personal the message is."
        ),

        LabTask(
            id = "t3",
            title = "Why the filter missed it",
            objective = "R2 passed every check the mail gateway ran. Open its gateway scan and " +
                    "submit the real CvSU account it was sent from.",
            entryPage = "report.html#r2",
            requiredClues = listOf(PhishDeskClues.GATEWAY_OPENED_R2),
            lockedMessage = "Open the mail gateway scan on report R2.",
            answer = LabAnswer.Text(
                accepted = listOf("m.santos@cvsu.edu.ph"),
                placeholder = "name@cvsu.edu.ph"
            ),
            hints = listOf(
                "Expand 'Mail gateway scan' under the message.",
                "The sender authentication lines show which mailbox actually sent it."
            ),
            successFeedback = "The email really came from m.santos@cvsu.edu.ph. The adviser's " +
                    "account had been taken over, so sender checks passed and the link was too new " +
                    "to be on any blocklist. Filters check where mail comes from and what it " +
                    "contains. They can't tell that a trusted account is being misused.",
            failureFeedback = "Not the sending account. Read the SPF and DKIM lines in the " +
                    "gateway scan for R2."
        ),

        LabTask(
            id = "t4",
            title = "Follow the hook",
            objective = "R4's attachment isn't a PDF. Preview it in the sandbox, then inspect " +
                    "the page to see where anything typed into it would go. The attacker left a " +
                    "flag in the code. Submit it.",
            entryPage = "report.html#r4",
            requiredClues = listOf(
                PhishDeskClues.ATTACHMENT_PREVIEWED,
                PhishDeskClues.PAGE_INSPECTED
            ),
            lockedMessage = "Preview R4's attachment and inspect its page code.",
            answer = LabAnswer.Flag(
                sha256 = "765f29add5dd1658f18a27475ac4a46e3d907e0e693ee9710c6130cdd7a99b60"
            ),
            hints = listOf(
                "Tap the attachment on R4, then use 'Inspect page' in the sandbox.",
                "Look at the form's hidden fields. Don't type a password into the page."
            ),
            successFeedback = "Captured. The 'PDF' was a web page dressed as the CvSU portal, " +
                    "and its form sends whatever you type to a server the attacker controls. " +
                    "That is the hook. The password is the catch.",
            failureFeedback = "Not the flag. It's in the page code, wrapper included."
        ),

        LabTask(
            id = "t5",
            title = "No link, no attachment",
            objective = "R3 has no link and no attachment, so there is nothing for a filter to " +
                    "scan. Read its full headers. What is it after, and what should the " +
                    "administrator do?",
            entryPage = "report.html#r3",
            requiredClues = listOf(PhishDeskClues.HEADERS_OPENED_R3),
            lockedMessage = "Open the full headers on report R3.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Nothing. Without a link or attachment it can't do any harm",
                    "Hidden malware. Run an antivirus scan on the email",
                    "A ₱385,000 transfer. Don't reply. Confirm with the President's office using a known phone number",
                    "The administrator's password. Change it immediately"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Compare the From address with the Reply-To address.",
                "The message only asks for one thing: a reply saying the payment was sent."
            ),
            successFeedback = "The catch is money, and a reply is enough to land it. Replies go " +
                    "to a Gmail-style address, not the President. Checking the request through a " +
                    "channel you already trust, like a known phone number, defeats it.",
            failureFeedback = "Read what the message actually asks for, and where a reply " +
                    "would go."
        )
    )
)
