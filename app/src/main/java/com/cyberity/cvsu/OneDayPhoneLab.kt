package com.cyberity.cvsu

// ===========================================================================
// LEVEL 305 CONTENT — Phishing Simulation, the Unit 3 capstone: One Day, One Phone
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same teaching format as
// the rest: every task explains first (guide), says what to do (steps), asks.
// Simulation assets live in assets/simulations/UNIT 03/one_day.
//
// The earlier Unit 3 levels each taught one skill, mostly against messages
// that were all hostile. 305 is the final test: one day on the student's phone, 07:40 to 21:00, with nothing
// labelled. Eight things arrive across email, Messenger, a canteen poster and
// a live call; three are genuine, and the other five belong to two campaigns
// run by two attackers. Each part of the day is one task (phone.html#b1 to #b5),
// and the day ends with a replay that sorts everything into campaigns.
//
// Dangerous choices cost a heart and change the rest of the day: a password
// on a fake page brings a sign-in alert, the poster form hands the caller
// your details, and the "remote help" app empties your GCash.

/** Clue ids reported by the phone through the JS bridge. */
object OneDayClues {
    const val E1_CHECKED_APP = "e1_checked_app"
    const val E2_LINK_PREVIEWED = "e2_link_previewed"
    const val MORNING_DONE = "b1_done"
    const val E3_LINK_PREVIEWED = "e3_link_previewed"
    const val E3_PAGE_INFO = "e3_page_info"
    const val E3_OFFICIAL_CHECKED = "e3_official_checked"
    const val E3_REPORTED = "e3_reported"
    const val MIDDAY_DONE = "b2_done"
    const val E4_SCANNED = "e4_scanned"
    const val LUNCH_DONE = "b3_done"
    const val E6_VERIFIED = "e6_verified"
    const val AFTERNOON_DONE = "b4_done"
    const val E7_DETAILS_CHECKED = "e7_details_checked"
    const val E7_LINK_PREVIEWED = "e7_link_previewed"
    const val E2_WHOIS = "e2_whois"
    const val E7_WHOIS = "e7_whois"
    const val EVENING_DONE = "b5_done"
    const val REPLAY_DONE = "replay_done"

    /** Dangerous: typing the CvSU password into one of the fake sign-in pages. */
    const val GAVE_PASSWORD = "gave_password"

    /** Dangerous: submitting the canteen poster's registration form. */
    const val GAVE_NUMBER = "gave_number"

    /** Dangerous: installing the caller's "remote help" app and reading him the code. */
    const val INSTALLED_APP = "installed_app"
}

val oneDayClueLabels: Map<String, String> = mapOf(
    OneDayClues.E1_CHECKED_APP to "Checked the 07:40 announcement in the Classroom app",
    OneDayClues.E2_LINK_PREVIEWED to "Previewed where the Registrar email's link goes",
    OneDayClues.MORNING_DONE to "Decided both morning messages correctly",
    OneDayClues.E3_LINK_PREVIEWED to "Previewed the scholarship Page's link",
    OneDayClues.E3_PAGE_INFO to "Read the scholarship Page's transparency info",
    OneDayClues.E3_OFFICIAL_CHECKED to "Found CvSU's official page from cvsu.edu.ph",
    OneDayClues.E3_REPORTED to "Reported the fake scholarship Page to ITSO",
    OneDayClues.MIDDAY_DONE to "Decided the midday message correctly",
    OneDayClues.E4_SCANNED to "Scanned the canteen poster's QR code",
    OneDayClues.LUNCH_DONE to "Decided both lunchtime items correctly",
    OneDayClues.E6_VERIFIED to "Called LinkPH's hotline from its own app",
    OneDayClues.AFTERNOON_DONE to "Decided the 15:20 call correctly",
    OneDayClues.E7_DETAILS_CHECKED to "Read the thesis email's authentication results",
    OneDayClues.E7_LINK_PREVIEWED to "Previewed where the thesis link goes",
    OneDayClues.E2_WHOIS to "Looked up who registered the Registrar link's domain",
    OneDayClues.E7_WHOIS to "Looked up who registered the thesis link's domain",
    OneDayClues.EVENING_DONE to "Decided both evening emails correctly",
    OneDayClues.REPLAY_DONE to "Matched every attack to its attacker"
)

fun oneDayPhoneLab(): LabDefinition = LabDefinition(
    levelId = 305,
    title = "One Day, One Phone",
    subtitle = "Phishing Simulation · Unit 3 capstone",
    briefing = "This is the Unit 3 final, and nothing in it is labelled.\n\n" +
            "It is one Tuesday on campus, from 07:40 to 21:00, on your own phone. Emails, a " +
            "Messenger chat, a poster in the canteen and a phone call reach you in order. Some " +
            "are exactly what they look like, and treating those as genuine matters as much " +
            "as catching the rest. The others come from two attackers working the whole " +
            "campus, and they connect: what you give away in the morning comes back to you " +
            "before night.\n\n" +
            "For each one, decide: genuine, or report it to ITSO. You can check anything as " +
            "much as you like. But typing your password into a fake page, filling in a form " +
            "that isn't what it claims, or giving a caller access each costs a heart, and " +
            "changes the rest of your day.\n\n" +
            "Each task is one part of the day. Open the phone with the button, deal with what " +
            "arrives, and swipe the bar at the top down when you are ready to answer.",
    assetDir = "UNIT 03/one_day",
    startPage = "phone.html#b1",
    dangerousClues = listOf(
        OneDayClues.GAVE_PASSWORD,
        OneDayClues.GAVE_NUMBER,
        OneDayClues.INSTALLED_APP
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Morning: check where you already are",
            objective = "Two emails arrive before your first class: a **Classroom announcement** at " +
                    "07:40 and a **Registrar notice** at 09:15. Decide each one. **What actually " +
                    "proved the Classroom announcement was genuine?**",
            guide = listOf(
                "A name on a message is **typed by whoever sent it**. \"Google Classroom\", " +
                        "\"Office of the Registrar\", your professor's name: genuine messages " +
                        "carry them, and so do fake ones, so a name alone settles nothing in " +
                        "either direction.",
                "What settles it is **checking through a channel the message didn't give you**. " +
                        "If a notice claims to come from an app or a service you already use, " +
                        "**open that app yourself**, from your own home screen, and see whether " +
                        "the same thing is there. Don't use the link in the message to do it.",
                "For an email you can't check that way, use what you already know: **hold the " +
                        "link** and read who owns the destination, and read the **authentication " +
                        "results**. A pass only proves the mail came from the domain it names. " +
                        "Ask whether that domain is one you trust."
            ),
            steps = listOf(
                "Open the phone. Two notifications arrive.",
                "Open **Mail** and read the Classroom announcement. Tap **Open the Classroom app** " +
                        "and compare what you find there.",
                "Read the Registrar email. Use **Show details** and **Hold the link**.",
                "Mark each one **Genuine** or **Report to ITSO**. A wrong call explains itself and " +
                        "lets you try again.",
                "**Swipe the bar** down and choose what proved the Classroom announcement genuine."
            ),
            entryPage = "phone.html#b1",
            requiredClues = listOf(
                OneDayClues.E1_CHECKED_APP,
                OneDayClues.E2_LINK_PREVIEWED,
                OneDayClues.MORNING_DONE
            ),
            lockedMessage = "Check the announcement in the Classroom app, preview the Registrar link, and decide both.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Its sender name said Google Classroom, which only Google is allowed to use",
                    "It had no attachment, and phishing emails always carry a file to open",
                    "It named your real professor and class, which only CvSU could know",
                    "The same post was waiting in the Classroom app you opened yourself"
                ),
                correctIndex = 3
            ),
            hints = listOf(
                "Anyone can type a name, and class details aren't secret. What did you check that a " +
                        "faker couldn't?",
                "Which tool took you outside the email?"
            ),
            successFeedback = "Right. A name can be typed and class details leak easily, so neither " +
                    "proves anything. The same post inside the app you opened yourself does. " +
                    "Meanwhile the \"Registrar\" passed its checks only for cvsu-records.example, " +
                    "and its link belonged to records-update.example.",
            failureFeedback = "That is something a scammer could copy. Think about what you checked " +
                    "outside the email itself."
        ),

        LabTask(
            id = "t2",
            title = "Midday: an official-looking page",
            objective = "At 11:30 the **\"CvSU Scholarship Office\"** Page messages you: you're shortlisted " +
                    "for a ₱5,000 allowance. **Check who is really behind the Page**, then **report it**. " +
                    "ITSO's reply contains your ticket code. Submit it.",
            guide = listOf(
                "Anyone can make a Facebook Page, give it a school's name and logo, and message " +
                        "students from it. **Fake institution pages** are one of the most common scams " +
                        "in the Philippines: fake scholarship offices, fake registrars, fake " +
                        "\"official\" giveaways. A Page's name and photo prove nothing.",
                "Facebook shows you more than the Page wants you to see. **Page transparency** lists " +
                        "when the Page was **created**, every **name it has had**, and the **ads it is " +
                        "running**. A real university office has years of history. A Page made this " +
                        "week that used to be a meme page is not an office.",
                "Then find the **official** page **without using the message**: type the school's " +
                        "website yourself and follow its own link. If the offer isn't there, it isn't " +
                        "real. A scholarship that needs your password on someone else's domain never is."
            ),
            steps = listOf(
                "Open **Messenger** and read the message from the Page.",
                "**Hold the link** and read who owns it.",
                "Tap **Page transparency** and read when it was created and what it used to be called.",
                "Tap **Find the official page** to reach CvSU's real page from cvsu.edu.ph.",
                "Tap **Report to ITSO**, then find the **CYBERITY{...}** ticket in ITSO's reply.",
                "**Swipe the bar** down and type it with the braces."
            ),
            entryPage = "phone.html#b2",
            requiredClues = listOf(
                OneDayClues.E3_PAGE_INFO,
                OneDayClues.E3_OFFICIAL_CHECKED,
                OneDayClues.E3_REPORTED
            ),
            lockedMessage = "Check the Page's transparency and the official page, then report it.",
            answer = LabAnswer.Flag(
                sha256 = "e50120136f3404a12c02bd7624fd62e07a64ba866881847e681b619db1002be0"
            ),
            hints = listOf(
                "The Messenger tools include a way to see the Page's history.",
                "The ticket code appears in ITSO's reply after you report."
            ),
            successFeedback = "Logged. The Page was three days old and used to be \"Pinoy Memes Hub\". " +
                    "CvSU's real page, reached from cvsu.edu.ph, has never heard of the allowance. " +
                    "A school's name and logo cost an attacker nothing.",
            failureFeedback = "Not the ticket code. Report the message after checking the Page, then " +
                    "read ITSO's reply under the chat."
        ),

        LabTask(
            id = "t3",
            title = "Lunch: the poster",
            objective = "A **FREE 50GB** poster in the canteen has a QR code, and a library email arrives " +
                    "at 13:40. Decide both. **What domain does the poster's QR code really open?**",
            guide = listOf(
                "**QR phishing** (\"quishing\") moves the link off the screen and onto paper, where " +
                        "nothing filters it. A poster in a busy place, a free offer, a deadline " +
                        "and a code to scan: no email gateway ever sees it.",
                "Before you open what a QR code points to, **read the address your scanner shows**, " +
                        "the same way you read any link: who owns it, and is that who the poster " +
                        "says is giving the offer away?",
                "Not every phishing page wants a password. Some only want **your details**: name, " +
                        "number, course. That sounds harmless, but it is exactly the information " +
                        "that makes the *next* scam convincing. Hold on to that thought for the " +
                        "afternoon."
            ),
            steps = listOf(
                "Open the **Camera** and look at the poster.",
                "Tap **Scan the QR code** and read the address the scanner shows.",
                "**Don't fill in the form** if you open it. Submitting it costs a heart.",
                "Open **Mail** and decide the 13:40 library email. Hold its link too.",
                "Mark the poster and the email **Genuine** or **Report to ITSO**.",
                "**Swipe the bar** down and type the **domain** the QR code opens."
            ),
            entryPage = "phone.html#b3",
            requiredClues = listOf(
                OneDayClues.E4_SCANNED,
                OneDayClues.LUNCH_DONE
            ),
            lockedMessage = "Scan the poster's QR code and decide both lunchtime items.",
            answer = LabAnswer.Text(
                accepted = listOf("cvsu-freeload.example", "www.cvsu-freeload.example"),
                placeholder = "e.g. example.com"
            ),
            hints = listOf(
                "The scanner shows a pill with the address when you scan.",
                "Type just the domain, without https:// or the path."
            ),
            successFeedback = "cvsu-freeload.example. Not the Student Council, not a telco. The " +
                    "library email, though, linked to library.cvsu.edu.ph and asked for nothing " +
                    "new, so it was exactly what it looked like.",
            failureFeedback = "That isn't the address the scanner shows. Scan the code again and " +
                    "read the pill."
        ),

        LabTask(
            id = "t4",
            title = "Afternoon: the promo call",
            objective = "At 15:20 an unknown number calls: **\"Rico from LinkPH Student Promos\"**, " +
                    "who knows your name, course, section and number. Handle the call, **check it with " +
                    "LinkPH directly**, and decide. **What did knowing your details prove about him?**",
            guide = listOf(
                "**Vishing** works because a voice feels personal and a live call leaves no time to " +
                        "think. This caller doesn't borrow fear. He borrows **good news**: the free " +
                        "data you were promised, a deadline, a few slots left.",
                "He opens with **details about you** to sound legitimate. Those details prove nothing. " +
                        "Names, numbers and courses leak from forms, group chats, class lists and " +
                        "\"free promo\" registrations. A caller who knows them has **found** them, not " +
                        "that he works where he claims.",
                "Then the ask: **install an app and read him a code**. That is how remote-access tools " +
                        "hand someone else your screen. No real promo needs it. **Hang up and call the " +
                        "company yourself**, on the number in its own app or on the back of your SIM " +
                        "pack, never one the caller gives you."
            ),
            steps = listOf(
                "When the phone rings, **answer** (or decline and check the voicemail).",
                "Listen, and choose your lines. Asking questions is fine.",
                "**Don't install the app.** Installing it costs a heart, and costs your wallet more.",
                "Hang up, open **Phone**, and call the **LinkPH Hotline**, the number listed in the " +
                        "LinkPH app.",
                "Mark the 15:20 call **Genuine** or **Report to ITSO**.",
                "**Swipe the bar** down and choose what his knowledge proved."
            ),
            entryPage = "phone.html#b4",
            requiredClues = listOf(
                OneDayClues.E6_VERIFIED,
                OneDayClues.AFTERNOON_DONE
            ),
            lockedMessage = "Call LinkPH's hotline from its own app and decide the 15:20 call.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "He works for LinkPH, because only LinkPH has your SIM registration details",
                    "Your phone is infected, because the poster's QR code installed a tracker",
                    "Nothing about him: details like these leak, and the poster form took them",
                    "The promo is real, because the Student Council's name was on the poster"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "What did the lunchtime poster's form ask for?",
                "What did LinkPH tell you on its own hotline?"
            ),
            successFeedback = "Right. Name, number and course were exactly what the canteen form " +
                    "collected, from you or from the classmates who filled it in. Knowing your " +
                    "details is not credentials. LinkPH has no such promo and never asks anyone to " +
                    "install an app.",
            failureFeedback = "That doesn't fit what you found. Think about what the poster's form " +
                    "asked for, and what LinkPH told you on its own hotline."
        ),

        LabTask(
            id = "t5",
            title = "Evening: who registered it?",
            objective = "At 18:00 Bea's CvSU account shares a thesis file with you, and at 19:30 ITSO " +
                    "announces maintenance. Decide both. Then **look up who registered** the thesis " +
                    "link's domain and this morning's Registrar link. **What registrant contact do they " +
                    "share?**",
            guide = listOf(
                "The thesis email passes every check for cvsu.edu.ph, because it really came from " +
                        "Bea's account. A pass only says *where* it came from. It can't say who is " +
                        "typing. So the decision rests on the same two questions as always: what does " +
                        "it want, and where does its link really go?",
                "Every domain has a public registration record, and a **domain lookup (WHOIS)** shows " +
                        "it: **when** it was registered, through **which registrar**, and often a " +
                        "**contact** for whoever registered it. A real office's domain is years old. A " +
                        "phishing domain is usually days old.",
                "Attackers change their stories, names and channels freely, but registering domains " +
                        "takes an account, and they reuse it. When two lookalike domains were registered " +
                        "the same week by the **same contact**, they belong to one campaign, and the " +
                        "next domain from that contact can be blocked before anyone receives it."
            ),
            steps = listOf(
                "Open **Mail** and read the **18:00 thesis email**. Use **Show details** and **Hold " +
                        "the link**.",
                "Under the link preview, tap **Look up drive-cvsu.example** and read the record.",
                "Open the **09:15 Registrar email** again, hold its link, and **look up its domain** too.",
                "Optional: call Bea from **Phone** and ask her about the file.",
                "Decide the thesis email and the **19:30 ITSO email**.",
                "**Swipe the bar** down and type the **registrant contact** both domains share."
            ),
            entryPage = "phone.html#b5",
            requiredClues = listOf(
                OneDayClues.E7_DETAILS_CHECKED,
                OneDayClues.E7_WHOIS,
                OneDayClues.E2_WHOIS,
                OneDayClues.EVENING_DONE
            ),
            lockedMessage = "Look up both domains and decide both evening emails.",
            answer = LabAnswer.Text(
                accepted = listOf("regbox88@mail.example", "regbox88"),
                placeholder = "name@example.com"
            ),
            hints = listOf(
                "The Look up button appears under a link once you hold it.",
                "It is an email address on the Registrant contact line, the same on both records."
            ),
            successFeedback = "regbox88@mail.example. The fake Registrar's domain, the fake " +
                    "scholarship Page's domain and Bea's thesis link were all registered by the same " +
                    "contact within two days. One attacker, three stories, and Bea is one of the " +
                    "people who already fell for it.",
            failureFeedback = "Not that one. Look up both domains and read the Registrant contact line " +
                    "on each."
        ),

        LabTask(
            id = "t6",
            title = "21:00: replay your day",
            objective = "**Two attackers** worked the campus today. One card for each is already placed. " +
                    "**Put the other three** with the attacker who sent them, then submit the **day " +
                    "report code**.",
            guide = listOf(
                "Real attacks are rarely one message. One attacker usually runs a **campaign**: " +
                        "several lures, often across several channels, sometimes feeding each other. " +
                        "Seeing that two messages come from the same attacker is how a help desk " +
                        "stops the third.",
                "The story and the channel don't tell you much, because an attacker can change those " +
                        "freely. Look at what they **can't** change as easily: **who registered** the " +
                        "domains their links use, **what they want** from you, and whether one message " +
                        "**already knew** what another one collected.",
                "The replay also shows **what your own choices unlocked**. Every detail or password " +
                        "given away in the morning came back later in the day. That is the real cost " +
                        "of \"it's only my number\"."
            ),
            steps = listOf(
                "Open the phone. It is **21:00** and the replay is waiting.",
                "The three messages you marked genuine are set aside at the top.",
                "Read the two attacker boxes. **Attacker A** starts with the Registrar email, " +
                        "**Attacker B** with the canteen poster.",
                "For each of the **three cards still to place**, read its evidence and tap **→ Attacker A** " +
                        "or **→ Attacker B**. A wrong choice explains itself.",
                "Read **what happened today** and **what your choices unlocked**, and find the **day " +
                        "report code**.",
                "**Swipe the bar** down and type it with the braces."
            ),
            entryPage = "phone.html#replay",
            requiredClues = listOf(OneDayClues.REPLAY_DONE),
            lockedMessage = "Place the three remaining cards in the replay.",
            answer = LabAnswer.Flag(
                sha256 = "b230ff137b14944eaa62e1f41af5149813d3494f674e3f010277922467852d92"
            ),
            hints = listOf(
                "Compare the \"Link domain registered by\" line on each card with the anchor cards.",
                "One card has no link at all. What did it already know, and which card collected that?"
            ),
            successFeedback = "That's the day. Attacker A harvested CvSU logins through a fake " +
                    "Registrar, a fake scholarship Page and Bea's stolen account. Attacker B used a " +
                    "free-load poster to collect details, then called with them. Three things were " +
                    "simply real, and treating them that way mattered as much as catching the rest.",
            failureFeedback = "Not the day report code. It appears once all three cards are placed."
        )
    )
)
