package com.cyberity.cvsu

// ===========================================================================
// LEVEL 305 CONTENT — Phishing Simulation, the unit 3 capstone
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same teaching format as
// the rest: every task explains first (guide), says what to do (steps), asks.
//
// 301-304 each taught one skill against messages that were nearly all hostile.
// 305 is the assessment: an unscreened shared mailbox where half the mail is
// genuine, so a student who flags everything fails exactly as they would in a
// real help desk. Nothing here is new material — it is the four skills used
// together, under the student's own judgement.

/** Clue ids reported by the help desk mailbox through the JS bridge. */
object PhishSimClues {
    const val OPENED_M1 = "opened_m1"
    const val OPENED_M2 = "opened_m2"
    const val OPENED_M4 = "opened_m4"
    const val OPENED_M5 = "opened_m5"
    const val OPENED_M6 = "opened_m6"
    const val OPENED_M3 = "opened_m3"
    const val OPENED_M7 = "opened_m7"
    const val OPENED_M8 = "opened_m8"
    const val M365_PAGE_OPENED = "payload_page_opened_m2"
    const val LINK_CHECKED_M1 = "link_checked_m1"
    const val LINK_CHECKED_M2 = "link_checked_m2"
    const val REPLY_TO_SHOWN_M4 = "reply_to_shown_m4"
    const val PAYLOAD_PAGE_OPENED = "payload_page_opened"
    const val PAYLOAD_PAGE_INSPECTED = "payload_page_inspected"
    const val ALL_EIGHT_TRIAGED = "all_eight_triaged"

    /** Dangerous: answering the impersonated dean instead of verifying. */
    const val REPLIED_TO_BEC = "replied_to_bec"

    /** Dangerous: typing a password into one of the sandbox pages. */
    const val CREDENTIALS_SUBMITTED = "credentials_submitted"

    /** Dangerous: opening the .html "mailbox quota" attachment. */
    const val ATTACHMENT_OPENED = "attachment_opened"
}

val phishSimClueLabels: Map<String, String> = mapOf(
    PhishSimClues.OPENED_M1 to "Read M1 (enrollment slot confirmed)",
    PhishSimClues.OPENED_M2 to "Read M2 (password expires today)",
    PhishSimClues.OPENED_M4 to "Read M4 (request from the Dean)",
    PhishSimClues.OPENED_M5 to "Read M5 (scheduled maintenance)",
    PhishSimClues.OPENED_M6 to "Read M6 (a document was shared with you)",
    PhishSimClues.OPENED_M3 to "Read M3 (scholarship requirements)",
    PhishSimClues.OPENED_M7 to "Read M7 (library overdue notice)",
    PhishSimClues.OPENED_M8 to "Read M8 (mailbox quota warning)",
    PhishSimClues.M365_PAGE_OPENED to "Opened M2's page in the sandbox",
    PhishSimClues.LINK_CHECKED_M1 to "Checked where M1's link goes",
    PhishSimClues.LINK_CHECKED_M2 to "Checked where M2's link goes",
    PhishSimClues.REPLY_TO_SHOWN_M4 to "Revealed M4's Reply-To address",
    PhishSimClues.PAYLOAD_PAGE_OPENED to "Opened M6's page in the sandbox",
    PhishSimClues.PAYLOAD_PAGE_INSPECTED to "Inspected the sandbox page's code",
    PhishSimClues.ALL_EIGHT_TRIAGED to "Triaged all eight messages correctly"
)

fun phishingSimLab(): LabDefinition = LabDefinition(
    levelId = 305,
    title = "The Handover Shift",
    subtitle = "Phishing Simulation · ITSO help desk",
    briefing = "You are covering the ITSO help desk for one shift. Eight messages are sitting " +
            "in the shared mailbox and nobody has screened them. There is no list of which " +
            "ones are suspicious, because that is the job.\n\n" +
            "Half of this mail is real. That matters more than it sounds: a help desk that " +
            "flags everything is as useless as one that flags nothing — people stop reading " +
            "the warnings, and someone misses a genuine deadline. You are judged on both " +
            "kinds of mistake.\n\n" +
            "Everything you need you already learned in 301 to 304: read the real domain, " +
            "check where a link goes, read the headers, and judge the request rather than the " +
            "presentation. Open a message with the button, and swipe the bar at the top of " +
            "the simulation down when you are ready to answer.",
    assetDir = "phish_sim",
    startPage = "inbox.html",
    dangerousClues = listOf(
        PhishSimClues.REPLIED_TO_BEC,
        PhishSimClues.CREDENTIALS_SUBMITTED,
        PhishSimClues.ATTACHMENT_OPENED
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Not everything strange is phishing",
            objective = "M1 is urgent, badly written and has a deadline tonight. M5 is a " +
                    "maintenance notice nobody asked for. Read both, check where M1's link " +
                    "goes, and decide what actually makes M1 genuine.",
            guide = listOf(
                "Every level so far has shown you mail that turned out to be hostile, which " +
                        "quietly teaches the wrong reflex: that urgency, poor grammar and an " +
                        "unexpected arrival are proof. They are not. Real institutions send " +
                        "rushed, ugly, badly timed mail constantly, and a registrar with a " +
                        "deadline tonight sounds exactly like a scammer with a deadline tonight.",
                "The cost of getting this backwards is real. Flag everything and the warnings " +
                        "stop being read, a genuine enrollment deadline gets missed, and the " +
                        "next real alert is ignored because the last four were nothing. A help " +
                        "desk is judged on false alarms as much as on misses.",
                "So the test is never how the message feels. It is what the message asks you " +
                        "to do, and where it actually sends you. A genuine notice points at the " +
                        "service's own address and asks for nothing you would not normally " +
                        "give it. A hostile one needs something from you — a password, a " +
                        "payment, a reply — and sends you somewhere it controls."
            ),
            steps = listOf(
                "Open the mailbox and read M1, the enrollment slot confirmation.",
                "Tap its link to reveal where it really goes, and read that address carefully.",
                "Read M5, the maintenance notice, and notice what it asks you to do.",
                "Ask of both: what does this want from me, and who owns the place it sends me?",
                "Swipe down and choose what makes M1 genuine despite how it reads."
            ),
            entryPage = "inbox.html",
            requiredClues = listOf(
                PhishSimClues.OPENED_M1,
                PhishSimClues.OPENED_M5,
                PhishSimClues.LINK_CHECKED_M1
            ),
            lockedMessage = "Read M1 and M5, and check where M1's link goes.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "It is polite and well written",
                    "Its link goes to the university's own portal domain, and it asks for nothing you wouldn't normally give the registrar",
                    "It arrived during office hours",
                    "It has no attachment"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Everything about the tone is a red herring here. Look at the destination.",
                "Ask what the message actually wants from you. M1 wants you to log in where " +
                        "you always log in."
            ),
            successFeedback = "Right. The deadline is real, the writing is bad, and it is still " +
                    "genuine — because the link lands on the portal the university actually " +
                    "owns and the request is ordinary. Judge the destination and the ask, never " +
                    "the tone. Calling this one phishing costs a student their enrollment slot.",
            failureFeedback = "Tone, timing and attachments prove nothing either way — plenty " +
                    "of phishing is polite and arrives at 10am. Look at where the link goes and " +
                    "what the message is asking for."
        ),

        LabTask(
            id = "t2",
            title = "The name is not the address",
            objective = "M2 says it is from Microsoft 365 and your password expires today. " +
                    "Reveal where its button really goes and submit the domain that owns it.",
            guide = listOf(
                "The name you see on a message is a display name, and the sender types it " +
                        "themselves. It can say Microsoft 365, ITSO, or the name of your own " +
                        "lecturer, and your mail app will show it without checking anything. " +
                        "This is the same trick as the sender ID in 304, wearing a different hat.",
                "Password-expiry mail is the most copied template in the world because it " +
                        "works on everyone: it is plausible, it is boring, and it has a built-in " +
                        "deadline. The page it leads to is a pixel-accurate copy of a sign-in " +
                        "screen, and the only thing that differs is the address bar.",
                "Read that address the way 302 taught: find the first single slash, then read " +
                        "the two pieces immediately before it. Everything to the left of those " +
                        "is decoration the attacker chose, and it very often contains the real " +
                        "institution's name to make the address look right at a glance."
            ),
            steps = listOf(
                "Open M2 from the mailbox.",
                "Tap the sign-in button to reveal its real destination instead of following it.",
                "Find the first single slash in that address.",
                "Read the two pieces just before it — that is who owns the page.",
                "Swipe down and type that domain."
            ),
            entryPage = "message.html#m2",
            requiredClues = listOf(PhishSimClues.LINK_CHECKED_M2),
            lockedMessage = "Open M2 and reveal where its button goes.",
            answer = LabAnswer.Text(
                accepted = listOf("m365-renew.example", "www.m365-renew.example"),
                placeholder = "e.g. example.com"
            ),
            hints = listOf(
                "\"cvsu-edu-ph\" is a label the attacker chose, not a domain. Hyphens are not dots.",
                "Ignore everything after the first single slash, then read the end of what's left."
            ),
            successFeedback = "m365-renew.example. The words cvsu-edu-ph sit in front of it to " +
                    "make the address read correctly at a glance, but they are just a subdomain " +
                    "label the attacker owns. Nothing to the left of the real domain is ever " +
                    "evidence of anything.",
            failureFeedback = "That part is decoration the sender chose. Find the first single " +
                    "slash and read the two pieces immediately before it."
        ),

        LabTask(
            id = "t3",
            title = "Phishing with no link at all",
            objective = "M4 appears to come from Dean Ramirez, asks you to buy ₱8,000 of load " +
                    "and send the codes, and contains no link and no attachment. Reveal its " +
                    "Reply-To address and decide what settles it.",
            guide = listOf(
                "Everything so far had something to click. This one does not, and that is " +
                        "deliberate: with no link and no attachment there is nothing for a " +
                        "filter to score, so this style walks through defences that catch the " +
                        "rest. It is called business email compromise, and in schools it usually " +
                        "arrives as a senior person needing a small favour quietly.",
                "The engineering is social, not technical. It uses authority so you do not " +
                        "question it, urgency so you do not check, secrecy so you do not ask a " +
                        "colleague, and a small amount so it stays under the level where you " +
                        "would stop and think. Load, gift cards and e-wallet transfers are the " +
                        "usual form because they are irreversible and untraceable.",
                "The defence is out-of-band verification: contact the person through a channel " +
                        "you already had, not one this message gave you. Call the number in the " +
                        "directory, or walk to the office. Never reply — a reply goes to whatever " +
                        "address the sender put in Reply-To, which is the whole point of the " +
                        "message."
            ),
            steps = listOf(
                "Open M4 from the mailbox.",
                "Tap \"Show full headers\" and read the Reply-To line against the From line.",
                "Notice what is being asked for, and how it would be recovered if you sent it.",
                "Do not reply, even to check — replying here costs a heart, and in real life " +
                        "it starts the conversation the attacker wants.",
                "Swipe down and choose what settles this one."
            ),
            entryPage = "message.html#m4",
            requiredClues = listOf(
                PhishSimClues.OPENED_M4,
                PhishSimClues.REPLY_TO_SHOWN_M4
            ),
            lockedMessage = "Open M4 and reveal its full headers.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "It has no link, so it is safe",
                    "The Dean would never send email at that hour",
                    "Replies go to a free webmail address, and the request is for untraceable value under pressure and secrecy",
                    "The grammar is poor"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "The absence of a link is what makes this dangerous, not what makes it safe.",
                "Look at where a reply would actually land, and at what is being asked for."
            ),
            successFeedback = "Reply-To points at a free webmail account, and the ask is load " +
                    "codes — value that cannot be recalled once sent — wrapped in authority, " +
                    "urgency and \"don't mention this yet\". Verify through a channel you " +
                    "already had: the directory number, or the office itself. Never the reply " +
                    "button.",
            failureFeedback = "Having no link makes it harder for a filter to catch, not safer. " +
                    "Look at the Reply-To line and at what the message wants you to send."
        ),

        LabTask(
            id = "t4",
            title = "Follow the payload",
            objective = "M6 says a thesis document was shared with you. Open its link in the " +
                    "sandbox, inspect the page, and submit the flag hidden in the code.",
            guide = listOf(
                "File-sharing notifications are the most effective phishing template aimed at " +
                        "students, because they are ordinary. You genuinely do get these from " +
                        "classmates and advisers, and the one thing you are expected to do with " +
                        "them is click. Curiosity about your own thesis does the rest.",
                "The page behind it is a sign-in copy, and it has one detail worth understanding: " +
                        "after it takes your password it forwards you to the real service. You " +
                        "land on a genuine site, assume the first page glitched, sign in again " +
                        "and think nothing more about it. The theft leaves no moment that feels " +
                        "wrong, which is why it is often found weeks later.",
                "Reading the page's own code is how an analyst proves where the typed password " +
                        "goes, and kits leave their markings there — campaign identifiers, the " +
                        "collector address, comments the author never removed. That debris is " +
                        "what links one incident to the next."
            ),
            steps = listOf(
                "Open M6 from the mailbox.",
                "Tap the link and open the page in the sandbox browser.",
                "Read the address panel above it: who actually owns that site?",
                "Do not type a password — that costs a heart here, and your account outside.",
                "Tap INSPECT PAGE, find the CYBERITY{...} value, and type it in below with " +
                        "the braces."
            ),
            entryPage = "message.html#m6",
            requiredClues = listOf(
                PhishSimClues.PAYLOAD_PAGE_OPENED,
                PhishSimClues.PAYLOAD_PAGE_INSPECTED
            ),
            lockedMessage = "Open M6's link in the sandbox, then inspect the page code.",
            answer = LabAnswer.Flag(
                sha256 = "1079c6ad9a7b65e94a1166618705887dee98f4b639b9c2d769616f81c75afd72"
            ),
            hints = listOf(
                "INSPECT PAGE sits under the sign-in form.",
                "The flag is in the hidden field the form submits alongside your password."
            ),
            successFeedback = "Captured. The form posts your password to a collector and then " +
                    "forwards you to the real service, so nothing ever looks broken. Open shared " +
                    "files from the app or site you already use, never from the link in the " +
                    "notification.",
            failureFeedback = "Not the flag. Open the page in the sandbox, tap INSPECT PAGE, and " +
                    "read the hidden field in the form."
        ),

        LabTask(
            id = "t5",
            title = "File the shift report",
            objective = "Mark every one of the eight messages as GENUINE or PHISHING. When all " +
                    "eight match the evidence, the mailbox issues a shift report code. Submit it.",
            guide = listOf(
                "This is the whole unit at once, and it is the shape the job actually takes: a " +
                        "mixed pile, no labels, and a decision required on every item including " +
                        "the boring ones. Four of these are genuine. A report that condemns all " +
                        "eight is wrong in four places, and wrong in the direction that gets the " +
                        "help desk ignored.",
                "Work one at a time and make each verdict for a reason you could defend out " +
                        "loud — the destination of the link, the Reply-To address, the domain " +
                        "that owns the page, the nature of the request. A verdict you cannot " +
                        "explain is a guess, and the mailbox will not tell you which ones you " +
                        "got wrong, only how many. That is on purpose: on a real desk nobody " +
                        "marks your work item by item either.",
                "One habit to carry out of this unit: when you are unsure, the answer is not a " +
                        "coin flip. It is to verify through a channel you already trust — type " +
                        "the address yourself, open the app you installed, ring the number in " +
                        "the directory. Checking costs a minute. Being wrong costs an account."
            ),
            steps = listOf(
                "Open the mailbox and read all eight messages; a message can only be marked " +
                        "once you have opened it.",
                "Use the tools inside each one — reveal links, show headers, open pages in the " +
                        "sandbox — before deciding.",
                "Mark each message GENUINE or PHISHING on its card.",
                "Check the counter at the top: it tells you how many do not match the evidence, " +
                        "but not which.",
                "When all eight are right, the shift report code appears. Swipe down and type it in."
            ),
            entryPage = "inbox.html",
            requiredClues = listOf(PhishSimClues.ALL_EIGHT_TRIAGED),
            lockedMessage = "Mark all eight messages correctly to get the shift report code.",
            answer = LabAnswer.Flag(
                sha256 = "db1620713c55787c3d9bc865d47c952b4ae637bcd57d68b5ed37c529d17a6512"
            ),
            hints = listOf(
                "Four are genuine. If you have marked more than four as phishing, you are " +
                        "flagging real mail.",
                "Re-read the ones you marked on feel rather than on evidence — the maintenance " +
                        "notice and the library notice ask for nothing at all."
            ),
            successFeedback = "Shift report filed: four real, four fake. That is the unit — you " +
                    "read a domain right to left, followed a link to where it truly went, caught " +
                    "a forged sender and a spoofed sender ID, refused an OTP, and left the " +
                    "genuine mail alone. The last part is the one most people never learn.",
            failureFeedback = "Not the report code. It only appears once all eight verdicts " +
                    "match the evidence — check the counter at the top of the mailbox."
        )
    )
)
