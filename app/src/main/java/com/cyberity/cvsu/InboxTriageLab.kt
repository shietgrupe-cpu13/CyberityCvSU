package com.cyberity.cvsu

// ===========================================================================
// LEVEL 101 CONTENT — Inbox Triage, as an investigation lab
// ===========================================================================
// Only content lives here. The engine is LabModel.kt / LabScreen.kt, so a new
// level means a new file like this one plus a new assets folder.

/** Clue ids reported by the simulation through the JS bridge. */
object InboxClues {
    const val EMAIL_OPENED_IT = "email_opened_it"
    const val EMAIL_OPENED_HR = "email_opened_hr"
    const val EMAIL_OPENED_M365 = "email_opened_m365"
    const val EMAIL_OPENED_FIN = "email_opened_fin"
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
    InboxClues.EMAIL_OPENED_IT to "Opened the IT Support message",
    InboxClues.EMAIL_OPENED_HR to "Opened the HR message",
    InboxClues.EMAIL_OPENED_M365 to "Opened the suspension notice",
    InboxClues.EMAIL_OPENED_FIN to "Opened the Finance message",
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
            "click things, read what's underneath. Nothing here reaches the real internet.\n\n" +
            "Each task explains what to look for, then hands you the mailbox. Open it with " +
            "the button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "inbox_triage",
    startPage = "inbox.html",
    dangerousClues = listOf(InboxClues.CREDENTIALS_SUBMITTED),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Triage the inbox",
            objective = "Four messages are waiting. Open them and work out which one is a " +
                    "phishing attempt, then name the sender.",
            guide = listOf(
                "Triage is the first thing anyone does with a mailbox they are responsible " +
                        "for: sort the ordinary from the suspicious, quickly, without opening " +
                        "every attachment or clicking every link. Most phishing is caught right " +
                        "here, before any technical analysis happens at all.",
                "The tell is rarely the sender's name — names are easy to fake. It is what the " +
                        "message wants from you. Ordinary mail informs you and lets you decide " +
                        "when to act. Hostile mail pressures you: an account about to close, a " +
                        "deadline in hours, a consequence if you ignore it. That urgency is " +
                        "manufactured, and its whole purpose is to stop you checking.",
                "So read all four before you judge any of them. A message only looks unusual " +
                        "next to the ones that are normal."
            ),
            steps = listOf(
                "Open the simulation. The mailbox lists four unread messages.",
                "Tap a message to open it, read it, then use ← Inbox to go back. Do this " +
                        "for all four.",
                "For each one, ask what it is asking you to DO, not who it claims to be from.",
                "Find the one that threatens a consequence and puts you on a clock.",
                "Swipe the bar at the top of the simulation down, and name that sender below."
            ),
            entryPage = "inbox.html",
            // All four, not just the hostile one — otherwise the choices unlocking
            // the moment the suspension notice is opened gives the answer away.
            requiredClues = listOf(
                InboxClues.EMAIL_OPENED_IT,
                InboxClues.EMAIL_OPENED_HR,
                InboxClues.EMAIL_OPENED_M365,
                InboxClues.EMAIL_OPENED_FIN
            ),
            lockedMessage = "Open all four messages first — you can't call a verdict on a subject line.",
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
            guide = listOf(
                "An email address has two parts, and an attacker treats them very differently. " +
                        "The display name — \"Microsoft 365 Security\" — is free text the sender " +
                        "types in themselves, so it can say absolutely anything. The domain after " +
                        "the @ has to be one they actually control.",
                "That makes the domain the only part worth trusting, and it is where lookalikes " +
                        "live: a zero standing in for the letter o, an r and an n pressed together " +
                        "to read as an m, or an extra word like -support bolted onto a real brand " +
                        "name. Read it one character at a time. Skimming is exactly how these get " +
                        "through.",
                "Message headers go further: they record the servers the message actually passed " +
                        "through on its way to you, which is much harder to forge than a From line."
            ),
            steps = listOf(
                "Open the simulation and open the suspension notice.",
                "Tap the From row — it expands to reveal the address the message was really " +
                        "sent from.",
                "Compare the domain after the @ with the organisation the message claims to be. " +
                        "Read it character by character.",
                "Tap \"Show message headers\" to see the routing the mail servers recorded.",
                "Come back and type the sending domain exactly as it appears."
            ),
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
            guide = listOf(
                "A link's text and a link's destination are two separate things — and so are " +
                        "the page you land on and the place your password ends up. Every login " +
                        "form has a destination address of its own, and nothing forces it to " +
                        "match the site showing in the address bar.",
                "The padlock is not a verdict either. It means the connection to that host is " +
                        "encrypted; it says nothing about who is on the other end. Certificates " +
                        "for a domain you own take minutes to get and cost nothing, padlock " +
                        "included. A phishing page can be perfectly encrypted and still be a " +
                        "phishing page.",
                "One rule for the whole exercise: investigating a suspicious page is safe, " +
                        "typing credentials into one is not. Never submit real details to a page " +
                        "you are still assessing."
            ),
            steps = listOf(
                "Open the simulation, open the suspension notice, and tap VERIFY ACCOUNT to " +
                        "follow the link in the sandboxed browser.",
                "Tap the address bar to break the URL into its scheme, host and path.",
                "Tap the ⓘ button to open Page details.",
                "Read the \"Form posts to\" row and compare that host with the one in the " +
                        "address bar.",
                "Do not fill in the login form — submitting credentials to it costs a heart, " +
                        "exactly as it would cost you an account in real life.",
                "Come back and submit the host that actually collects the credentials."
            ),
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
            guide = listOf(
                "Phishing pages are almost never written from scratch. They come from kits that " +
                        "get sold, copied and reused across campaigns — and kits carry debris: " +
                        "template names, staging paths, and the developer's own comments left " +
                        "sitting in the HTML.",
                "Comments never appear on screen, but they ship with the page all the same. " +
                        "Reading source is how an analyst links one incident to another and works " +
                        "out which kit is in circulation, which is the difference between " +
                        "cleaning up one email and shutting down a campaign.",
                "Capturing a specific string hidden in a system is the CTF discipline, and the " +
                        "string is called a flag. You will do a lot of this."
            ),
            steps = listOf(
                "Open the simulation and return to the fake verification page.",
                "Open Page details with the ⓘ button.",
                "Tap \"View page source\" at the bottom of that panel.",
                "Scan the greyed-out comment lines — those are the parts a browser never draws.",
                "Come back and submit the flag in full, CYBERITY{...} wrapper included."
            ),
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
            guide = listOf(
                "Identifying it was half the job. What you do next decides whether everyone " +
                        "else who received the same message is protected or left to work it out " +
                        "alone — and a campus phishing run is never sent to one person.",
                "A report carries evidence with it: the sending domain, the headers, the link. " +
                        "With those, IT can search every mailbox for the same message, block the " +
                        "domain at the gateway, and reach the people who already clicked before " +
                        "the accounts are used. Deleting it quietly protects exactly one inbox " +
                        "and throws that evidence away.",
                "There is also a rule worth keeping: never reply to a suspicious message, and " +
                        "never forward it to colleagues as a warning. Replying confirms your " +
                        "address is live, and a forwarded copy is a live link in someone else's " +
                        "inbox."
            ),
            steps = listOf(
                "Open the simulation if you want to re-read the message before you decide.",
                "Weigh each option by who it protects — your mailbox only, or everyone who " +
                        "received the same thing.",
                "Ask what evidence IT would need from you in order to act at all."
            ),
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
