package com.cyberity.cvsu

// ===========================================================================
// LEVEL 302 CONTENT — Identifying Suspicious Emails, as the ITSO bait workshop
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 301:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 03/bait_workshop.
//
// 101 and 305 have the student read suspicious mail as a defender. This level
// flips the view: the student joins the ITSO awareness team and builds the
// pieces of a sanctioned training phishing email, one trick per station —
// borrowed names in a URL, lookalike letters (and why browsers expose the
// Unicode ones), masked links and QR codes, and what SPF/DKIM/DMARC really
// prove — then catches a classmate's bait against the clock.
//
// Every domain is fictional (.example). Drills go only to volunteers; sending
// to every student is the level's one dangerous action.

/** Clue ids reported by the bait workshop through the JS bridge. */
object BaitWorkshopClues {
    const val URL_BUILT = "url_built"
    const val OWNER_TEST_PASSED = "owner_test_passed"
    const val LENS_USED = "lens_used"
    const val XRAY_RUN = "xray_run"
    const val MASK_BUILT = "mask_built"
    const val REVEAL_LONGPRESS = "reveal_longpress"
    const val REVEAL_HOVER = "reveal_hover"
    const val REVEAL_QR = "reveal_qr"
    const val SPOOF_FORGED = "spoof_forged"
    const val SPOOF_OWN_DOMAIN = "spoof_owndomain"
    const val FLIP_DONE = "flip_done"

    /** Dangerous: sending the drill email to every student instead of the volunteers. */
    const val SENT_TO_ALL = "sent_to_all"
}

val baitWorkshopClueLabels: Map<String, String> = mapOf(
    BaitWorkshopClues.URL_BUILT to "Built an address that reads as CHED",
    BaitWorkshopClues.OWNER_TEST_PASSED to "Found the owner of all three test addresses",
    BaitWorkshopClues.LENS_USED to "Inspected three lookalikes under the lens",
    BaitWorkshopClues.XRAY_RUN to "Ran the browser x-ray on every lookalike",
    BaitWorkshopClues.MASK_BUILT to "Masked a link behind the portal's address",
    BaitWorkshopClues.REVEAL_LONGPRESS to "Checked the masked link with a phone hold",
    BaitWorkshopClues.REVEAL_HOVER to "Checked the masked link with a laptop hover",
    BaitWorkshopClues.REVEAL_QR to "Checked the QR version with a scanner",
    BaitWorkshopClues.SPOOF_FORGED to "Sent a forged Registrar address through the gateway",
    BaitWorkshopClues.SPOOF_OWN_DOMAIN to "Sent from the drill's own lookalike domain",
    BaitWorkshopClues.FLIP_DONE to "Caught all five tells in Kim's bait"
)

fun baitWorkshopLab(): LabDefinition = LabDefinition(
    levelId = 302,
    title = "Bait Workshop",
    subtitle = "Identifying Suspicious Emails · ITSO",
    briefing = "The fastest way to understand a trick is to try to pull it off. This semester " +
            "the IT Services Office is running a phishing drill: a practice email sent only to " +
            "volunteers who signed up, so they can learn from a click that costs nothing.\n\n" +
            "You have joined the awareness team, and you are building that email. Each station " +
            "in the workshop is one trick real attackers use: an address that borrows a name, " +
            "a letter swapped for its lookalike, a link that says one thing and opens another, " +
            "and a sender that passes the mail server's checks. At the last station a teammate " +
            "turns them all on you.\n\n" +
            "Every domain here is fictional and nothing is ever sent. One rule is real: drills " +
            "go only to the volunteers. Sending to everyone costs a heart.\n\n" +
            "Each task explains the trick, then hands you the workshop. Open it with the button, " +
            "and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 03/bait_workshop",
    startPage = "studio.html",
    dangerousClues = listOf(BaitWorkshopClues.SENT_TO_ALL),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Borrow a name you don't own",
            objective = "At **Station 1**, build an address that a student would read as **CHED**, " +
                    "using only the domain ITSO registered. Then pass the **pressure test**. " +
                    "**Which part of an address decides who owns it?**",
            guide = listOf(
                "Every web address has **one owner**, and you can't fake it: it is the " +
                        "**registered domain**, the name someone paid for. Nobody can register " +
                        "a name that is taken, and **.gov.ph** names go only to government " +
                        "agencies. So the drill can't use ched.gov.ph.",
                "What an owner **can** do is put **any words they like in front** of their own " +
                        "domain. If ITSO owns stipend-claim.example, it can create " +
                        "ched.gov.ph.stipend-claim.example, and it is still ITSO's page.",
                "That works because people read **left to right** and stop at the **first " +
                        "familiar name**. The owner sits at the other end: find the **first single " +
                        "slash** after https://, then read the **name just before it, from the " +
                        "right**. Words after that slash are only folders on the owner's server."
            ),
            steps = listOf(
                "Open the workshop and tap **Station 1 · URL builder**.",
                "Try picking **ched.gov.ph** as the registered domain and read why it is refused.",
                "Keep **stipend-claim.example** and tap label tiles until the address **starts** " +
                        "with ched.gov.ph. Watch **A student reads** and **Real owner** change.",
                "Optional: pick the path **/ched.gov.ph/claim** and see that the owner stays " +
                        "the same.",
                "In the **pressure test**, tap the piece that decides the owner in each of the " +
                        "**three** addresses. One of them is genuine.",
                "**Swipe the bar** down and choose which part decides the owner."
            ),
            entryPage = "url.html",
            requiredClues = listOf(
                BaitWorkshopClues.URL_BUILT,
                BaitWorkshopClues.OWNER_TEST_PASSED
            ),
            lockedMessage = "Build the CHED address and pass the pressure test at Station 1.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The first name in the address, because that is the part readers see first",
                    "The path after the slash, because that is where the page itself is stored",
                    "The registered name just before the first single slash, read from the right",
                    "The https:// at the start, because only a site's real owner can get it"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Which part of your address were you unable to choose freely?",
                "Find the first single slash after https://, then look just before it."
            ),
            successFeedback = "Right. You put ched.gov.ph at the front, and the page still " +
                    "belonged to stipend-claim.example. The front of an address is decoration " +
                    "the owner picks; the registered name before the first single slash is the " +
                    "one part nobody can borrow.",
            failureFeedback = "That part was free for you to choose, so it can't prove who owns " +
                    "the page. Think about the one piece the registrar refused to give you."
        ),

        LabTask(
            id = "t2",
            title = "One letter off",
            objective = "At **Station 2**, inspect lookalikes of **microsoft.com** under the lens, " +
                    "then run the **browser x-ray**. The Cyrillic one fooled almost everyone in " +
                    "the inbox. **Why is it still a weak choice for the drill?**",
            guide = listOf(
                "A **lookalike** (or **homoglyph**) domain copies a real one and swaps a " +
                        "character for one that looks the same: **rn for m**, a zero for o, a " +
                        "lowercase l for i. On a phone screen, at inbox size, most people never " +
                        "see the difference.",
                "The strongest swaps come from **other alphabets**. The Cyrillic **і** looks " +
                        "exactly like the Latin i, but to a computer it is a different " +
                        "character, so mіcrosoft.com is a different domain that anyone can " +
                        "register.",
                "Browsers know this. When a name **mixes alphabets**, the address bar shows " +
                        "its raw form, which starts with **xn--**. Same-alphabet swaps like rn for " +
                        "m get no such warning, which is why attackers keep using them. If you " +
                        "ever see **xn--** where a brand name should be, stop."
            ),
            steps = listOf(
                "Open the workshop and tap **Station 2 · Letter swap**.",
                "Pick a candidate and look at the **inbox line**. Can you see what changed?",
                "Drag the **lens** to the right until the swapped character lights up, and read " +
                        "how many volunteers it fooled.",
                "Inspect **at least three** candidates, including the one that looks perfect.",
                "Tap **RUN BROWSER X-RAY** and compare what the address bar shows.",
                "**Swipe the bar** down and choose why the Cyrillic lookalike fails."
            ),
            entryPage = "letters.html",
            requiredClues = listOf(
                BaitWorkshopClues.LENS_USED,
                BaitWorkshopClues.XRAY_RUN
            ),
            lockedMessage = "Inspect three lookalikes under the lens and run the browser x-ray.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Browsers show names that mix alphabets as xn-- code, which exposes the swap",
                    "Mail filters reject every sender domain that uses a non-Latin alphabet",
                    "Cyrillic letters show up as empty boxes on most Android phone screens",
                    "Domain registries refuse to issue any name that copies a famous brand"
                ),
                correctIndex = 0
            ),
            hints = listOf(
                "It looked perfect in the inbox. Where did it stop looking perfect?",
                "Compare the Cyrillic row in the x-ray table with the other four."
            ),
            successFeedback = "Right. In the inbox it fooled 39 of 40 people, but the moment " +
                    "anyone opens the link the address bar shows xn--mcrosoft-thh.com. Swaps " +
                    "within one alphabet, like rn for m, get no such warning, so read sender " +
                    "domains slowly, letter by letter.",
            failureFeedback = "The x-ray table shows what really happens to it. Look at what the " +
                    "address bar displays for the Cyrillic candidate."
        ),

        LabTask(
            id = "t3",
            title = "The label and the link",
            objective = "At **Station 3**, make the drill link **show the portal's address** while it " +
                    "**opens the drill page**. Then check it **all three ways** a student could. " +
                    "The drill's code is in the link's real destination. Submit it.",
            guide = listOf(
                "A link is **two things the sender types separately**: the **text** you see and " +
                        "the **destination** it opens. Nothing forces them to match. Text that " +
                        "reads portal.cvsu.edu.ph can open any server in the world, and a " +
                        "button can say anything at all.",
                "That is why the text is never evidence. A careful reader checks the " +
                        "destination instead: on a phone, **hold the link** to preview it; on a " +
                        "laptop, **rest the pointer** on it and read the bottom-left corner of " +
                        "the browser. Then apply Station 1: who owns that address?",
                "**QR codes** go one step further. There is no text to compare at all, the " +
                        "scanner shows the address only for a moment, and many mail filters " +
                        "can't read a link inside an image. A QR code in an email asking you to " +
                        "sign in deserves the same suspicion as an unknown link."
            ),
            steps = listOf(
                "Open the workshop and tap **Station 3 · Mask the link**.",
                "Set the **link text** to one that looks like the portal's address.",
                "Set the **destination** to the **drill landing page**. The banner confirms when " +
                        "the mask is complete.",
                "Open each check: **Phone · hold**, **Laptop · hover** and **QR scanner**. " +
                        "Compare what each one shows with the text in the preview.",
                "In the phone preview, find the **CYBERITY{...}** code in the destination.",
                "**Swipe the bar** down and type it with the braces."
            ),
            entryPage = "mask.html",
            requiredClues = listOf(
                BaitWorkshopClues.MASK_BUILT,
                BaitWorkshopClues.REVEAL_LONGPRESS,
                BaitWorkshopClues.REVEAL_HOVER,
                BaitWorkshopClues.REVEAL_QR
            ),
            lockedMessage = "Build the mask, then open all three checks at Station 3.",
            answer = LabAnswer.Flag(
                sha256 = "ab54fe3c34d25b5030e83224caa6634d65413af09d1783165c83b5220fad27ac"
            ),
            hints = listOf(
                "The mask needs portal-looking text and the drill landing page as the destination.",
                "The code is at the end of the address in the Phone · hold preview."
            ),
            successFeedback = "Captured. The volunteer sees portal.cvsu.edu.ph; the hold " +
                    "preview and the hover corner both show grades.cvsu-portal.example. Check " +
                    "the destination, never the label, and treat a sign-in QR code in an email " +
                    "as an unchecked link.",
            failureFeedback = "Not the drill code. Hold the link in the phone preview and read " +
                    "the end of its real destination."
        ),

        LabTask(
            id = "t4",
            title = "Pass every check",
            objective = "At **Station 4**, send the drill email **twice**: once forging " +
                    "**registrar@cvsu.edu.ph**, once from the drill's **own lookalike domain**. " +
                    "One is delivered with three passes. **Which domain did DMARC actually verify?**",
            guide = listOf(
                "The **From line** is typed by the sender, like a return address on an " +
                        "envelope. What the sender can't fake is the check run by the " +
                        "**receiving mail server**, recorded in the headers under " +
                        "**Authentication-Results**.",
                "**SPF** asks whether the sending server is on the domain's list of approved " +
                        "senders. **DKIM** checks a digital signature added by the domain. " +
                        "**DMARC** checks that those results **line up with the domain in the From " +
                        "address**, and tells the receiver what to do when they don't.",
                "Read that last part carefully. DMARC proves the mail really came from **the " +
                        "domain it names**. It never asks whether that is **a domain you trust**. " +
                        "An attacker who registers their own lookalike can pass all three checks " +
                        "honestly. **dmarc=pass** means \"this is who it says\", so you still have " +
                        "to read **who it says**."
            ),
            steps = listOf(
                "Open the workshop and tap **Station 4 · Spoof attempt**.",
                "Leave **Send to** on **Drill volunteers**. The other option is not a drill.",
                "Choose **registrar@cvsu.edu.ph** and tap **SEND THROUGH THE GATEWAY**. Watch " +
                        "each check.",
                "Choose **registrar@cvsu-registrar.example** and send again.",
                "Read the **Authentication-Results** line for the email that was delivered, " +
                        "and look at how it lands in the inbox.",
                "**Swipe the bar** down and type the **domain DMARC verified** for the delivered " +
                        "email."
            ),
            entryPage = "spoof.html",
            requiredClues = listOf(
                BaitWorkshopClues.SPOOF_FORGED,
                BaitWorkshopClues.SPOOF_OWN_DOMAIN
            ),
            lockedMessage = "Send both versions through the gateway at Station 4.",
            answer = LabAnswer.Text(
                accepted = listOf("cvsu-registrar.example"),
                placeholder = "e.g. example.com"
            ),
            hints = listOf(
                "The delivered email's Authentication-Results line ends with header.from=…",
                "It is the drill's own domain, not CvSU's."
            ),
            successFeedback = "cvsu-registrar.example. Forging cvsu.edu.ph failed every check, " +
                    "because only CvSU's servers may send for CvSU. The lookalike passed all " +
                    "three, because the checks only confirm a message came from the domain it " +
                    "names. A pass on a domain you don't recognise is not good news.",
            failureFeedback = "Not that one. Read header.from= at the end of the delivered " +
                    "email's Authentication-Results line."
        ),

        LabTask(
            id = "t5",
            title = "Flip sides",
            objective = "At **Station 5**, Kim from the awareness team has built bait aimed at **you**. " +
                    "Start the clock and **tag all five tells** before it runs out. Submit the " +
                    "**drill report code**.",
            guide = listOf(
                "Everything you built is in this one email, the way it would be in a real " +
                        "attack. A **tell** is a single sign of a trick: a lookalike sender, a " +
                        "check that passed for the wrong domain, a link whose text and " +
                        "destination differ, a QR code, and the **pressure** that keeps you from " +
                        "checking any of them.",
                "Not everything unusual is a tell. Your **name in the greeting**, the **time it " +
                        "was sent**, a **logo** and a company **address in the footer** cost an " +
                        "attacker nothing to copy, so they prove nothing either way. Tagging " +
                        "one costs you ten seconds.",
                "Real inboxes don't give you a timer, but they do give you pressure. The habit " +
                        "to keep: when an email wants something from you quickly, that is the " +
                        "moment to slow down and check the sender, the destination and the checks."
            ),
            steps = listOf(
                "Open the workshop and tap **Station 5 · Flip sides**.",
                "Tap **START**. You have **90 seconds**.",
                "Tap every tell. For the link, tap once to **preview** where it goes, then again " +
                        "to tag it.",
                "Don't forget the **header line** and the **QR code**.",
                "If time runs out, tap **RESTART**. Nothing is lost.",
                "When all five are tagged, **swipe the bar** down and type the **report code**."
            ),
            entryPage = "flip.html",
            requiredClues = listOf(BaitWorkshopClues.FLIP_DONE),
            lockedMessage = "Tag all five tells in Kim's email at Station 5.",
            answer = LabAnswer.Flag(
                sha256 = "3ee0150b0a2315885f789ecfb317fdc102dd227e984d1b27eb94659263dd17a4"
            ),
            hints = listOf(
                "One tell is in the sender address and one is in the header line.",
                "The greeting, time, logo and footer are not tells."
            ),
            successFeedback = "Caught them all: rn standing in for m, a DMARC pass for the " +
                    "lookalike's own domain, a two-hour threat, link text that hid its " +
                    "destination, and a QR code. You built every one of those tricks, so you " +
                    "know exactly where to look.",
            failureFeedback = "Not the report code. It appears once all five tells are tagged " +
                    "before the clock runs out."
        )
    )
)
