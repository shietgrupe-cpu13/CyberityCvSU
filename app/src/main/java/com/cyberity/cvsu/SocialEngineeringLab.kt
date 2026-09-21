package com.cyberity.cvsu

// ===========================================================================
// LEVEL 303 CONTENT — Social Engineering, as a campus security review
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 101-102:
// every task teaches first (guide), says exactly what to do (steps), then asks.
//
// 301 and 302 lived inside a mailbox. This level leaves it: a door, a phone
// call, a flash drive in the canteen, and a chat from a friend. Four incidents
// that turn out to be one person, working a chain.

/** Clue ids reported by the security review through the JS bridge. */
object SocialEngClues {
    const val INCIDENT_OPENED_A = "incident_opened_a"
    const val INCIDENT_OPENED_B = "incident_opened_b"
    const val INCIDENT_OPENED_C = "incident_opened_c"
    const val INCIDENT_OPENED_D = "incident_opened_d"
    const val DOORWAY_FACED = "doorway_faced"
    const val CCTV_REVIEWED = "cctv_reviewed"
    const val BADGE_LOG_OPENED = "badge_log_opened"
    const val TRANSCRIPT_OPENED = "transcript_opened"
    const val USB_ANALYZED = "usb_analyzed"
    const val CHAT_CHECKED = "chat_checked"

    /** Dangerous: plugging the found drive into a lab machine to "just look". */
    const val USB_PLUGGED_IN = "usb_plugged_in"
}

val socialEngClueLabels: Map<String, String> = mapOf(
    SocialEngClues.INCIDENT_OPENED_A to "Reviewed A · the courier at the door",
    SocialEngClues.INCIDENT_OPENED_B to "Reviewed B · the helpful phone call",
    SocialEngClues.INCIDENT_OPENED_C to "Reviewed C · the flash drives in the canteen",
    SocialEngClues.INCIDENT_OPENED_D to "Reviewed D · the message from a classmate",
    SocialEngClues.DOORWAY_FACED to "Made the call at the door yourself",
    SocialEngClues.CCTV_REVIEWED to "Watched camera 3 to the last frame",
    SocialEngClues.BADGE_LOG_OPENED to "Reconciled all four door events against the badge log",
    SocialEngClues.TRANSCRIPT_OPENED to "Read the call transcript",
    SocialEngClues.USB_ANALYZED to "Analysed the drive safely, without plugging it in",
    SocialEngClues.CHAT_CHECKED to "Checked the classmate's account history"
)

fun socialEngineeringLab(): LabDefinition = LabDefinition(
    levelId = 303,
    title = "Campus Security Review",
    subtitle = "Social Engineering · ITSO + Security Office",
    briefing = "Social engineering is hacking the person instead of the system. There is no " +
            "malware in most of it and nothing for a filter to catch — just someone using " +
            "trust, hurry, or plain politeness to get what they want.\n\n" +
            "Four incidents were logged on campus this week. A courier at a locked door, a " +
            "phone call to the library, flash drives left in the canteen, and a message from " +
            "a classmate. Separately they look like bad luck. Your job is to work out what " +
            "each one actually was.\n\n" +
            "Each task explains what to look for, then hands you the review desk. Open it with " +
            "the button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "social_eng",
    startPage = "desk.html",
    dangerousClues = listOf(SocialEngClues.USB_PLUGGED_IN),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "The courier at the door",
            objective = "Review incident A: the CCTV notes and the door badge log. Name the " +
                    "technique the visitor used to get inside.",
            guide = listOf(
                "Locks, badges and passwords are all built on one assumption: that the person " +
                        "using them is the person they belong to. Social engineering attacks that " +
                        "assumption directly, and the tools are ordinary human habits.",
                "Holding a door for someone whose hands are full is good manners. Doing it at a " +
                        "badge-controlled door is how an outsider walks into a server room, and " +
                        "the technique has a name: tailgating, sometimes called piggybacking. " +
                        "The uniform and the boxes are the costume — what actually opens the door " +
                        "is a polite student who doesn't want to seem rude.",
                "You will stand in that doorway yourself before you review the footage. There " +
                        "is no trick answer and no heart at stake — the point is to notice how " +
                        "much social weight sits on a two-second decision with somebody waiting.",
                "Nobody in incident A did anything malicious. That is exactly what makes this " +
                        "class of attack work: the person who lets the attacker in is helpful, " +
                        "not careless. The fix isn't to stop being kind, it is to make checking " +
                        "normal — every visitor signs in, and a badge opens the door for one " +
                        "person at a time."
            ),
            steps = listOf(
                "Open the review desk, tap incident A, and read the security officer's notes.",
                "Tap STAND AT THE DOOR and make the call yourself before you judge anyone " +
                        "else's. No answer there costs a heart.",
                "Tap REVIEW CAMERA 3 FOOTAGE and step through the clip with NEXT. Watch the " +
                        "door between 14:10:22 and 14:10:24.",
                "On the two frames where he raises his phone, use ZOOM to see what he could " +
                        "read.",
                "At the last frame, tap RECONCILE WITH THE DOOR BADGE LOG and rule on all " +
                        "four door events yourself.",
                "Swipe the bar at the top down and name what the visitor did."
            ),
            entryPage = "desk.html",
            requiredClues = listOf(
                SocialEngClues.INCIDENT_OPENED_A,
                SocialEngClues.CCTV_REVIEWED,
                SocialEngClues.BADGE_LOG_OPENED
            ),
            lockedMessage = "Watch the footage to the end, then reconcile all four door " +
                    "events against the badge log.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Tailgating — walking in behind someone who used their own badge",
                    "Phishing — a deceptive message sent to many people",
                    "Baiting — leaving something tempting for the victim to pick up",
                    "Brute force — trying badge numbers until one opened the door"
                ),
                correctIndex = 0
            ),
            hints = listOf(
                "Two of the four door events have no swipe behind them. Look at what the " +
                        "camera shows at 14:10:24.",
                "No badge was stolen and no system was attacked. A person opened the door."
            ),
            successFeedback = "Tailgating. One badge, two people, and no visitor entry in the " +
                    "book. The student assistant did nothing wrong by normal standards — which " +
                    "is why the control has to be the rule, not the individual's judgement in " +
                    "the moment.",
            failureFeedback = "No message and no system was involved here. Look again at the " +
                    "badge log: one swipe, two people through the door."
        ),

        LabTask(
            id = "t2",
            title = "The helpful caller",
            objective = "Read the recorded call in incident B. Decide what the library staff " +
                    "member should have done at the moment the caller asked for the password.",
            guide = listOf(
                "Pretexting is inventing a role and a reason to be asking. The role gives the " +
                        "attacker authority — IT support, the registrar, a bank officer — and " +
                        "the reason gives them urgency, so the target acts before thinking. The " +
                        "caller in B does both in under a minute.",
                "Listen for the tells. The caller knows small true details, which makes the role " +
                        "believable, but those details are public: the office name, a staff " +
                        "member's first name, a system everyone uses. Then comes the pressure — " +
                        "a deadline, an account about to be locked, an apology for the rush — " +
                        "and finally the ask, which is always something a real IT office would " +
                        "never need: your password, a code sent to you, or permission to " +
                        "install remote-access software.",
                "The defence is the same every time, and it doesn't require you to be suspicious " +
                        "or rude: hang up and call back on a number you looked up yourself. A " +
                        "real colleague will not mind. An attacker cannot survive it, because " +
                        "they don't control the number in the campus directory."
            ),
            steps = listOf(
                "Open the review desk and open incident B.",
                "Expand \"Call transcript\" and read the whole conversation.",
                "Mark where the caller establishes authority, where he adds time pressure, and " +
                        "what he finally asks for.",
                "Ask yourself which of these the real ITSO would ever need over the phone.",
                "Swipe down and choose what the staff member should have done."
            ),
            entryPage = "incident.html#b",
            requiredClues = listOf(SocialEngClues.TRANSCRIPT_OPENED),
            lockedMessage = "Open the call transcript on incident B first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Give the password — the caller already knew her name and the office",
                    "Ask the caller for his employee number, then continue if he answers",
                    "Hang up and call ITSO on the number in the campus directory before doing anything",
                    "Refuse politely and say nothing to anyone, to avoid embarrassing a colleague"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Everything the caller knows about her is public information.",
                "An attacker can invent an employee number as easily as a name. What can't they " +
                        "control?"
            ),
            successFeedback = "Call back on a number you looked up. It costs a minute and it " +
                    "defeats the entire technique, because the attacker controls what he says " +
                    "but not the campus directory. Reporting it matters too — the ITSO only " +
                    "found out about this call because she mentioned it the next day.",
            failureFeedback = "An employee number can be invented, and a name being known " +
                    "proves nothing. Look for the option that verifies through something the " +
                    "caller doesn't control."
        ),

        LabTask(
            id = "t3",
            title = "Nothing is free",
            objective = "Three flash drives were found in the canteen and one was plugged into " +
                    "a lab PC. Analyse the drive safely and submit the flag hidden in what it " +
                    "ran.",
            guide = listOf(
                "Baiting is leaving something desirable where the target will find it and let " +
                        "curiosity do the rest. A drive labelled SCHOLARSHIP GRANTEES 2026 — " +
                        "CONFIDENTIAL is not litter; it is chosen so that whoever picks it up " +
                        "wants to look inside, and maybe feels they are doing the right thing by " +
                        "finding the owner.",
                "A modern flash drive doesn't need you to open a file. It can tell the computer " +
                        "it is a keyboard and type commands by itself the moment it is plugged " +
                        "in, faster than you can read them. That is why \"I'll just check what's " +
                        "on it\" is the mistake, not opening the documents afterwards.",
                "Investigators analyse a found drive on an isolated machine that can't reach " +
                        "the network, or read its contents without letting it run anything. The " +
                        "review desk here does the same: ANALYSE SAFELY lists what is on the " +
                        "drive and what it would execute, without executing it."
            ),
            steps = listOf(
                "Open the review desk and open incident C.",
                "Tap OPEN THE DRIVE ANALYSIS to see the drive the student handed in.",
                "Use ANALYSE SAFELY. Do not use the PLUG IN button — that is the mistake the " +
                        "student made, and here it costs a heart.",
                "Read the hidden startup script and the endpoint log under it.",
                "Find the CYBERITY{...} value in the script and type it in below, braces " +
                        "included."
            ),
            entryPage = "incident.html#c",
            requiredClues = listOf(SocialEngClues.USB_ANALYZED),
            lockedMessage = "Use ANALYSE SAFELY on the drive first.",
            answer = LabAnswer.Flag(
                sha256 = "531797896f5efb58c69faacc05147e549db0f45deb3ec15d8e43335c2b33f704"
            ),
            hints = listOf(
                "The visible files are decoration. Look at the hidden startup script the " +
                        "analysis lists below them.",
                "The flag is one of the arguments the script passes when it phones home."
            ),
            successFeedback = "Captured. The scholarship list was two harmless-looking " +
                    "documents; the real payload was a hidden script that copied saved browser " +
                    "passwords and sent them out. The student who plugged it in was trying to " +
                    "find the owner — good intentions are not a security control.",
            failureFeedback = "Not the flag. Run ANALYSE SAFELY and read the hidden startup " +
                    "script — the value is in the line where it contacts the attacker."
        ),

        LabTask(
            id = "t4",
            title = "A code from a friend",
            objective = "Incident D is a Messenger chat from a classmate's account asking for " +
                    "a verification code. Check the account and decide what is really going on.",
            guide = listOf(
                "The strongest lever in social engineering isn't fear, it's familiarity. A " +
                        "message from a name you know skips every suspicion you would apply to a " +
                        "stranger, and hijacked accounts are cheap and plentiful.",
                "Look at what is being asked for here: a one-time code that was texted to the " +
                        "victim's own phone. A verification code only ever proves one thing — " +
                        "that whoever types it controls that phone number. So there is no " +
                        "honest reason for another person to need yours, not a friend, not a " +
                        "bank, not a delivery rider, not the ITSO. Anyone asking is trying to " +
                        "get into something of yours.",
                "The story around the request is built to rush you: the sender is in a hurry, " +
                        "something will be lost, and there is a small favour framing that makes " +
                        "refusing feel petty. Slow it down and the whole thing collapses — one " +
                        "phone call to the real classmate ends it in ten seconds."
            ),
            steps = listOf(
                "Open the review desk and open incident D.",
                "Read the chat from the top. Notice how quickly it moves to the request.",
                "Expand \"Account check\" to see what that account has been doing lately.",
                "Ask what the code actually proves, and to whom.",
                "Swipe down and pick the best reading of the situation."
            ),
            entryPage = "incident.html#d",
            requiredClues = listOf(SocialEngClues.CHAT_CHECKED),
            lockedMessage = "Open incident D and expand the account check first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "A classmate in a hurry — send the code, then remind her to be careful",
                    "A hijacked account. The code would let the attacker into the victim's own account; don't send it, and phone the classmate",
                    "A harmless mistake — verification codes are public and expire anyway",
                    "A billing problem with the e-wallet that the classmate can fix with the code"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "The account check shows the same message went to 40 contacts in nine minutes.",
                "The code was sent to the victim's phone, not the classmate's. Ask what it " +
                        "unlocks."
            ),
            successFeedback = "A hijacked account working down a contact list. The code in that " +
                    "text is what the attacker needs to finish taking over the victim's own " +
                    "account — which then asks the next 40 people. Codes are never shared, and " +
                    "a real friend can wait the ten seconds it takes to phone them.",
            failureFeedback = "Look at the account check and at who the code was actually sent " +
                    "to. A one-time code proves control of a phone number — whose?"
        ),

        LabTask(
            id = "t5",
            title = "One person, four moves",
            objective = "Put the week together. The four incidents share a thread — decide " +
                    "what the visitor in A was really after, and why the order matters.",
            guide = listOf(
                "Real social engineering is rarely a single trick. It is a chain, where each " +
                        "step buys something small that makes the next step believable, and no " +
                        "single step looks like an attack on its own.",
                "Read the week in order. Monday: a courier gets inside the MIS building and is " +
                        "alone in a corridor for four minutes — long enough to read what is " +
                        "written on the whiteboard and stuck to a monitor. Tuesday: a caller " +
                        "already knows the staff member's name and the system she uses. " +
                        "Wednesday: drives appear in the canteen with a label aimed at students. " +
                        "Thursday: a hijacked account harvests codes.",
                "Each incident feeds the next: information first, then access, then credentials. " +
                        "Defending against this means reporting the small things. The courier at " +
                        "the door is the cheapest moment to stop the whole chain, and it costs " +
                        "nothing but a question."
            ),
            steps = listOf(
                "Open the review desk and re-read the summary line of all four incidents.",
                "Look at the dates and put them in order.",
                "In incident A, read what the CCTV notes say the visitor did while he was " +
                        "alone in the corridor.",
                "Ask what he could have learned there that the caller in B already knew.",
                "Swipe down and choose what the first visit was for."
            ),
            requiredClues = listOf(
                SocialEngClues.INCIDENT_OPENED_A,
                SocialEngClues.INCIDENT_OPENED_B,
                SocialEngClues.INCIDENT_OPENED_C,
                SocialEngClues.INCIDENT_OPENED_D
            ),
            lockedMessage = "Review all four incidents before drawing the thread together.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Stealing hardware — he was looking for equipment to carry out",
                    "Nothing: a real courier who took a wrong turn",
                    "Gathering information — names, systems and anything written down, to make the later approaches convincing",
                    "Installing malware directly on a server in the four minutes he had"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Nothing was taken on Monday. Something was learned.",
                "How did Tuesday's caller know the staff member's name and which system she used?"
            ),
            successFeedback = "Reconnaissance. He left with no equipment and no files — just " +
                    "names, a system, and a password on a sticky note, which is what made every " +
                    "later approach sound legitimate. That is why reporting the small, awkward, " +
                    "'probably nothing' moment matters more than any single control: it is the " +
                    "only step where the chain is still cheap to break.",
            failureFeedback = "Nothing was carried out of the building and no system was " +
                    "touched that day. Ask what the Tuesday caller knew, and where he could " +
                    "have learned it."
        )
    )
)
