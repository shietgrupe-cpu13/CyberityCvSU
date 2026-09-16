package com.cyberity.cvsu

// ===========================================================================
// LEVEL 101 CONTENT — Inbox Triage, as an investigation lab
// ===========================================================================
// Only content lives here. The engine is LabModel.kt / LabScreen.kt, so a new
// level means a new file like this one plus a new assets folder.

/** Clue ids reported by the simulation through the JS bridge. */
object InboxClues {
    const val EMAIL_OPENED_M365 = "email_opened_m365"
    const val SENDER_INSPECTED = "sender_inspected"
    const val HEADERS_EXPANDED = "headers_expanded"
    const val LINK_FOLLOWED = "link_followed"
    const val URL_INSPECTED = "url_inspected"
    const val PAGE_INFO_OPENED = "page_info_opened"
    const val SOURCE_VIEWED = "source_viewed"
    const val CREDENTIALS_SUBMITTED = "credentials_submitted"
}

/** Labels for the evidence log. Unknown ids fall back to the raw id. */
val inboxClueLabels: Map<String, String> = mapOf(
    InboxClues.EMAIL_OPENED_M365 to "Opened the suspension notice",
    InboxClues.SENDER_INSPECTED to "Inspected the sender address",
    InboxClues.HEADERS_EXPANDED to "Expanded the message headers",
    InboxClues.LINK_FOLLOWED to "Followed the verification link",
    InboxClues.URL_INSPECTED to "Inspected the address bar",
    InboxClues.PAGE_INFO_OPENED to "Opened page details",
    InboxClues.SOURCE_VIEWED to "Read the page source",
    InboxClues.CREDENTIALS_SUBMITTED to "Submitted credentials to the fake portal"
)

fun inboxTriageLab(): LabDefinition = LabDefinition(
    levelId = 101,
    title = "Inbox Triage",
    subtitle = "Security Operations Lab",
    briefing = "A campus inbox has four unread messages. One of them is hostile. " +
            "You have a live copy of the mailbox and a sandboxed browser — open things, " +
            "click things, read what's underneath. Nothing here reaches the real internet.",
    assetDir = "inbox_triage",
    startPage = "inbox.html",
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Triage the inbox",
            objective = "Four messages are waiting. Open them and work out which one is a " +
                    "phishing attempt, then name the sender.",
            entryPage = "inbox.html",
            requiredClues = listOf(InboxClues.EMAIL_OPENED_M365),
            lockedMessage = "Open the messages first — you can't call a verdict on a subject line.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "IT Support",
                    "HR Department",
                    "Microsoft 365 Security",
                    "Finance Department"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Three of these messages ask you to read something. One asks you to act, fast.",
                "Manufactured urgency plus an account threat is the oldest lure there is."
            ),
            successFeedback = "The suspension notice is the hostile one. Threatening account " +
                    "loss on a deadline is pressure engineering — it exists to stop you checking.",
            failureFeedback = "Not that one. Open each message and compare what it's asking " +
                    "you to do, not just who it claims to be from."
        ),

        LabTask(
            id = "t2",
            title = "Inspect the sender",
            objective = "The display name says Microsoft. Inspect the From field and submit " +
                    "the domain the message was actually sent from.",
            requiredClues = listOf(InboxClues.SENDER_INSPECTED),
            lockedMessage = "Tap the From row inside the message to expand the real address.",
            answer = LabAnswer.Text(
                accepted = listOf(
                    "micr0soft-support.example",
                    "@micr0soft-support.example",
                    "security@micr0soft-support.example"
                ),
                placeholder = "e.g. example-domain.com"
            ),
            hints = listOf(
                "The display name is free text. The domain after the @ is not.",
                "Read the domain character by character — one of them isn't a letter."
            ),
            successFeedback = "A zero standing in for the letter o. Display names are decoration; " +
                    "the domain is the only part of a From field an attacker can't fake.",
            failureFeedback = "That isn't the sending domain. Expand the From row and copy the " +
                    "part after the @ exactly as written."
        ),

        LabTask(
            id = "t3",
            title = "Follow the link",
            objective = "Open the verification link in the sandboxed browser and find out where " +
                    "the login form actually sends the credentials. Submit that host.",
            requiredClues = listOf(InboxClues.PAGE_INFO_OPENED),
            lockedMessage = "Open the link, then use the page details panel in the browser.",
            answer = LabAnswer.Text(
                accepted = listOf(
                    "harvest.micr0soft-support.example",
                    "https://harvest.micr0soft-support.example/collect.php",
                    "harvest.micr0soft-support.example/collect.php"
                ),
                placeholder = "e.g. host.example"
            ),
            hints = listOf(
                "The address bar tells you where you are, not where the form posts to.",
                "The page details panel lists the form destination. They don't match."
            ),
            successFeedback = "The page you can see and the host collecting the password are " +
                    "two different machines. A padlock only proves the connection is encrypted — " +
                    "it says nothing about who is on the other end.",
            failureFeedback = "That's the page address, not the collection host. Open page " +
                    "details and look at where the form posts."
        ),

        LabTask(
            id = "t4",
            title = "Find the hidden flag",
            objective = "The attacker left something behind in the page they built. Read the " +
                    "page source in the browser and submit the flag you find.",
            answer = LabAnswer.Flag(
                sha256 = "e3771e01cae07a0e07f6e9275de5f054782e1c784dc67888c23aff53dd6bc1c4"
            ),
            hints = listOf(
                "Page details has a second panel most people never open.",
                "Developers leave comments in HTML. Comments don't render — but they ship."
            ),
            successFeedback = "Captured. Attackers reuse kits, and kits carry fingerprints: " +
                    "comments, template names, staging paths. That debris is how incidents get " +
                    "linked to each other.",
            failureFeedback = "Not the flag. It's written in full inside the page source, " +
                    "wrapper included."
        ),

        LabTask(
            id = "t5",
            title = "Close the incident",
            objective = "You've confirmed the message is hostile. Decide what happens to it now.",
            entryPage = "inbox.html",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Delete it immediately so nobody in the office can click it",
                    "Reply asking the sender to confirm who they are",
                    "Report it to IT security with the headers, then delete it",
                    "Forward it to your classmates so they know to watch out"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Deleting it protects you. It doesn't protect the other four hundred inboxes " +
                        "that got the same message."
            ),
            successFeedback = "Report first, delete second. Your headers let IT search every " +
                    "mailbox for the same sender and block the domain at the gateway — deleting " +
                    "quietly throws that evidence away.",
            failureFeedback = "Think past your own mailbox. Who else received this, and what " +
                    "do they need from you to act on it?"
        )
    )
)
