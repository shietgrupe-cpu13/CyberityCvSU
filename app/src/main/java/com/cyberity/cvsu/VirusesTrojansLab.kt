package com.cyberity.cvsu

// ===========================================================================
// LEVEL 402 CONTENT — Viruses & Trojans, on the org's shared flash drive
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 401:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 04/org_flashdrive.
//
// 401 named the families. This level goes inside the two oldest ones, and
// gives every task its own tool so it doesn't feel like one long list:
//   map.html     scrub a timeline on a campus map, then build a cleanup list
//   macro.html   label each line of the virus by the job it does
//   wizard.html  click through a trojan's setup wizard and report red flags
//   hash.html    compare a download's SHA-256 character by character
// 405 later puts a trojan and a worm side by side on one machine.

/** Clue ids reported by the flash drive simulation through the JS bridge. */
object VirusesTrojansClues {
    const val PATIENT_ZERO_SEEN = "patient_zero_seen"
    const val TIMELINE_COMPLETE = "timeline_complete"
    const val MACRO_VIEWED = "macro_viewed"
    const val MACRO_DISSECTED = "macro_dissected"
    const val WIZARD_OPENED = "wizard_opened"
    const val WIZARD_FLAGS_ALL = "wizard_flags_all"
    const val WIZARD_CANCELLED = "wizard_cancelled"
    const val HASH_VIEWED = "hash_viewed"
    const val HASH_D1_MATCH = "hash_d1_match"
    const val HASH_D2_MISMATCH = "hash_d2_mismatch"
    const val CLEANUP_OPENED = "cleanup_opened"
    const val CLEANUP_CORRECT = "cleanup_correct"

    /** Dangerous: clicking "Enable Content" on the infected document. */
    const val CONTENT_ENABLED = "content_enabled"

    /** Dangerous: agreeing to the trojan installer's last screen. */
    const val INSTALLER_RUN = "installer_run"
}

val virusesTrojansClueLabels: Map<String, String> = mapOf(
    VirusesTrojansClues.PATIENT_ZERO_SEEN to "Found the moment the first PC was infected",
    VirusesTrojansClues.TIMELINE_COMPLETE to "Played the drive's whole week on the map",
    VirusesTrojansClues.MACRO_VIEWED to "Opened the macro dissector",
    VirusesTrojansClues.MACRO_DISSECTED to "Labelled every line of the virus correctly",
    VirusesTrojansClues.WIZARD_OPENED to "Opened the StatWise FULL setup wizard",
    VirusesTrojansClues.WIZARD_FLAGS_ALL to "Reported all three red flags in the wizard",
    VirusesTrojansClues.WIZARD_CANCELLED to "Cancelled the trojan installer",
    VirusesTrojansClues.HASH_VIEWED to "Opened the hash comparer",
    VirusesTrojansClues.HASH_D1_MATCH to "Confirmed the vendor's copy matches",
    VirusesTrojansClues.HASH_D2_MISMATCH to "Found where the mirror's hash differs",
    VirusesTrojansClues.CLEANUP_OPENED to "Started the cleanup list",
    VirusesTrojansClues.CLEANUP_CORRECT to "Submitted the correct cleanup list"
)

fun virusesTrojansLab(): LabDefinition = LabDefinition(
    levelId = 402,
    title = "Org Flash Drive",
    subtitle = "Viruses & Trojans · CvSU CS Society",
    briefing = "Viruses and trojans are the two oldest kinds of malware, and they get " +
            "in the same way: a person lets them in. A virus hides inside a file you " +
            "already trust and spreads every time that file is opened somewhere new. A trojan " +
            "is the program you choose to run, dressed up as something you want.\n\n" +
            "The CvSU Computer Science Society shares one flash drive among its officers. " +
            "This week, documents on it started growing for no reason, and the org room PC " +
            "has two different downloads of the statistics program everyone needs for " +
            "thesis data, plus a \"free full version\". ITSO hands you four tools: an " +
            "outbreak map, a macro dissector, the setup wizard itself, and a " +
            "hash comparer.\n\n" +
            "Looking, labelling and comparing are free. Clicking Enable Content, or " +
            "letting the trojan's installer finish, costs a heart.\n\n" +
            "Each task explains what to look for, then hands you the right tool. Open it " +
            "with the button, and swipe the bar at the top of it down when you are ready to " +
            "answer.",
    assetDir = "UNIT 04/org_flashdrive",
    startPage = "map.html",
    dangerousClues = listOf(
        VirusesTrojansClues.CONTENT_ENABLED,
        VirusesTrojansClues.INSTALLER_RUN
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Patient zero",
            objective = "**Play the drive's week** on the outbreak map and find **where the " +
                    "infection started**. Submit the **name of the first infected file**.",
            guide = listOf(
                "A **computer virus** is code that **attaches itself to another file**, called " +
                        "its **host**, and **copies itself into more hosts**. It can't move on " +
                        "its own: it rides inside a document or program and runs only when " +
                        "**someone opens that host**.",
                "This one is a **macro virus**. **Macros** are small programs stored inside " +
                        "Word documents. When someone opens an infected document and clicks " +
                        "**Enable Content**, the virus copies itself into **Normal.dotm**, the " +
                        "**template** behind every Word document on that computer. From then " +
                        "on, **every document saved there is infected**.",
                "Every copy is **the same block of code**, so every infected file grows by " +
                        "**the same amount** even when nobody changed its text. The **first " +
                        "infected file**, often called **patient zero**, tells you **how it got " +
                        "in** and **where to start looking**."
            ),
            steps = listOf(
                "Open the **outbreak map**. Six computers are shown; the **USB** icon is the " +
                        "flash drive.",
                "**Drag the slider** (or tap **Later →**) through **all eight events**. Watch " +
                        "the drive move and the computers change colour.",
                "Read each event card. Notice the **+38 KB** that keeps appearing.",
                "Find the **first** event where **Enable Content** was clicked and a " +
                        "computer turned **red**.",
                "**Swipe the bar** down and type the **file** that was opened at that moment."
            ),
            entryPage = "map.html",
            requiredClues = listOf(VirusesTrojansClues.TIMELINE_COMPLETE),
            lockedMessage = "Play all eight events on the outbreak map first.",
            answer = LabAnswer.Text(
                accepted = listOf(
                    "Org_Budget_2026.docm",
                    "Org_Budget_2026",
                    "Org Budget 2026.docm",
                    "Org Budget 2026"
                ),
                placeholder = "e.g. File_Name.docm"
            ),
            hints = listOf(
                "It's event 3, on Tuesday morning.",
                "It's the \"free budget template\" the treasurer downloaded the night before."
            ),
            successFeedback = "Org_Budget_2026.docm, a free template from an unofficial site. " +
                    "It sat harmlessly on the drive until one click on Enable Content infected " +
                    "ORGPC-01's template. Thirteen minutes later the meeting minutes were the " +
                    "first copy, 38 KB heavier.",
            failureFeedback = "Not that one. Find the earliest event where Enable Content was " +
                    "clicked, and type the file opened at that moment."
        ),

        LabTask(
            id = "t2",
            title = "Dissect the virus",
            objective = "Open the **macro dissector** and **label every highlighted line** by " +
                    "the job it does. When all six are right, it gives you the **build tag**. " +
                    "Submit it.",
            guide = listOf(
                "Viruses are built from a few **parts**, each with one job. **Replication** " +
                        "makes copies: here, into the template and into every saved document. " +
                        "The **trigger** decides **when** to strike: a date, a count, a certain " +
                        "file. The **payload** is **the damage** done when the trigger fires.",
                "Then there's **camouflage**: lines that exist only so **you won't notice**. " +
                        "Turning off Word's warning pop-ups, or making sure your document still " +
                        "saves normally, keeps everything looking fine while the virus works.",
                "Until the trigger fires, a virus is **dormant**: it spreads quietly and does " +
                        "nothing you'd see. That's deliberate. The longer it hides, the **more " +
                        "copies** exist by the time anyone notices."
            ),
            steps = listOf(
                "Open the **macro dissector**. **Don't click Enable Content** on the yellow " +
                        "bar; that costs a heart.",
                "**Tap a dashed line** of code to select it.",
                "Tap the job it does: **REPLICATION**, **TRIGGER**, **PAYLOAD** or " +
                        "**CAMOUFLAGE**. Tap a line again to change its label.",
                "Label **all six** dashed lines, then tap **CHECK MY LABELS**.",
                "When all six are right, a **CYBERITY{...}** build tag appears. **Swipe the " +
                        "bar** down and type it **exactly as written**."
            ),
            entryPage = "macro.html",
            requiredClues = listOf(VirusesTrojansClues.MACRO_DISSECTED),
            lockedMessage = "Label all six lines correctly in the macro dissector first.",
            answer = LabAnswer.Flag(
                sha256 = "0daf1338b7ff1c304dbfc83e9be6d89ed9ecc98d557d03b52b3077464b472912"
            ),
            hints = listOf(
                "Two lines contain CopySelfTo. What do they make?",
                "DisplayAlerts = False hides warnings. ActiveDocument.Save makes your save " +
                        "still work. Neither one does damage."
            ),
            successFeedback = "Dissected. Replication: copy into the template, then into every " +
                    "document saved. Trigger: the 15th. Payload: delete your recent documents. " +
                    "Camouflage: no warnings, and saving still works. It's the 29th, so it's " +
                    "been dormant, and on the 15th it would have struck on every infected " +
                    "computer at once.",
            failureFeedback = "Not the build tag. It appears in the dissector once all six " +
                    "labels are correct."
        ),

        LabTask(
            id = "t3",
            title = "The free full version",
            objective = "Click through the **StatWise FULL setup wizard**. **Report every red " +
                    "flag** you see, then **cancel before it finishes**, and decide **what the " +
                    "installer is really for**.",
            guide = listOf(
                "A **trojan**, named after the wooden horse of the old story, is malware " +
                        "**disguised as something you want**. It doesn't copy itself like a " +
                        "virus. It waits for **you to run it**, and an **installer** is the " +
                        "perfect disguise, because installers are expected to ask for " +
                        "**administrator rights**.",
                "\"**Free full version**\", \"**cracked**\" and \"**activated**\" are the " +
                        "classic wrapping for paid software. The program inside often **works " +
                        "perfectly**. That's the point: you keep it, and nobody suspects the " +
                        "**extra piece** that came with it.",
                "Watch for: an **unknown publisher** on the Windows permission prompt; " +
                        "**extra programs ticked for you**, especially anything that **runs at " +
                        "startup**; and any request to **turn off your antivirus**. A real " +
                        "installer never needs that."
            ),
            steps = listOf(
                "Open the **setup wizard**. It looks like a real installer window.",
                "On **each screen**, read everything. If something is wrong, tap **REPORT RED " +
                        "FLAG**. Reporting a normal screen costs nothing.",
                "Use **Yes** / **Next** to move on and **Back** to look again.",
                "Find **all three** red flags. The counter shows how many you have.",
                "Then press **Cancel** (or **No**). **Don't press Disable & Continue**; that " +
                        "installs it and costs a heart.",
                "**Swipe the bar** down and choose **what the installer is really for**."
            ),
            entryPage = "wizard.html",
            requiredClues = listOf(
                VirusesTrojansClues.WIZARD_FLAGS_ALL,
                VirusesTrojansClues.WIZARD_CANCELLED
            ),
            lockedMessage = "Report all three red flags in the wizard, then cancel it.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The statistics program. That's what it says it installs",
                    "The QuickSearch toolbar, to change your browser homepage",
                    "The \"Remote Helper Service\" that runs at startup, with antivirus " +
                            "switched off: a hidden way back into the PC",
                    "Nothing. It's a normal installer for a cracked program"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Which component would still be running after every restart?",
                "Why would an installer need your antivirus off, if not to hide that component?"
            ),
            successFeedback = "The Remote Helper Service. StatWise itself is the disguise and " +
                    "really works. The toolbar is just adware tagging along. The startup " +
                    "service, installed with antivirus switched off and administrator rights, " +
                    "is the backdoor the uploader wanted on CvSU's org PC.",
            failureFeedback = "Look again at the components screen and the antivirus screen. " +
                    "Which piece needed both to stay hidden?"
        ),

        LabTask(
            id = "t4",
            title = "Same name, different file",
            objective = "Two downloads are both called **StatWise_Setup_2026**. **Compare each " +
                    "one's SHA-256** with the vendor's published hash, **character by " +
                    "character**, and choose **which to install**.",
            guide = listOf(
                "Even a file with **exactly the right name** can be swapped. Download mirrors " +
                        "and file-sharing sites can host a **modified copy**. To let you check, " +
                        "vendors publish a **hash** of each installer, usually **SHA-256**, " +
                        "which you met in Unit 2.",
                "Change **one byte** of a file and its SHA-256 changes **almost completely**. " +
                        "So if your download's hash matches the published one in **all 64 " +
                        "characters**, you have the vendor's file, exactly.",
                "**All 64 is the rule.** Attackers know people only glance at **the first and " +
                        "last few characters**, and making those few line up is **cheap**: they " +
                        "just keep tweaking the file until the ends look right. Matching all 64 " +
                        "is practically impossible. Compare **every character**, or let a tool " +
                        "do it."
            ),
            steps = listOf(
                "Open the **hash comparer**. The **grey** rows are the vendor's published " +
                        "hash, the **white** rows are the download's.",
                "Start with **Copy 1 · vendor site**. Compare every character. If they all " +
                        "match, tap **ALL 64 CHARACTERS MATCH**.",
                "Switch to **Copy 2 · mirror**. Look at the start and the end first. Then " +
                        "compare the **middle**.",
                "Tap the **first white character** that **differs** from the grey one above it.",
                "Also check each copy's **source** and **signature**.",
                "**Swipe the bar** down and choose **which one to install**."
            ),
            entryPage = "hash.html",
            requiredClues = listOf(
                VirusesTrojansClues.HASH_D1_MATCH,
                VirusesTrojansClues.HASH_D2_MISMATCH
            ),
            lockedMessage = "Confirm Copy 1 matches and find where Copy 2 differs first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Copy 2 from the mirror: its hash starts and ends the same as the " +
                            "published one",
                    "Copy 1 from statwise.example: signed by StatWise Inc. and all 64 " +
                            "characters match",
                    "Either one, since they have the same file name",
                    "Neither. Hashes can't tell you anything about a file"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Copy 2 starts with c45918 and ends with 15073c, just like the published hash. " +
                        "Look at character 7.",
                "Only one copy is signed, and only one matches all 64 characters."
            ),
            successFeedback = "Copy 1. Vendor's own site, valid StatWise Inc. signature, and " +
                    "an exact 64-character match. Copy 2 shares only the first six and last " +
                    "six characters: 51 of the 64 in between differ. That's someone counting " +
                    "on a quick glance. A partial match is a mismatch.",
            failureFeedback = "Not that one. A hash either matches in all 64 characters or it " +
                    "doesn't match. Check which copy does, and which is signed."
        ),

        LabTask(
            id = "t5",
            title = "Clean every host",
            objective = "Back on the map, **build the cleanup list**: tap **every computer** " +
                    "whose Word template is now infected. When it's right, ITSO gives you a " +
                    "**ticket code**. Submit it.",
            guide = listOf(
                "Viruses move at **human speed**. They go where people **carry files** and " +
                        "**open them**: on flash drives, as email attachments, in shared " +
                        "folders. That's the opposite of a **worm**, which you'll meet in level 5 of this unit, " +
                        "spreading over the network **with nobody involved**.",
                "This macro virus runs only where **an infected Word document is opened**. " +
                        "**Plugging in the drive isn't enough.** Opening a **picture** from it " +
                        "isn't enough. **Copying** an infected file without opening it isn't " +
                        "enough either.",
                "Cleanup has to reach **every** infected computer: clean its **template**, " +
                        "replace its infected documents, and warn its user. **Miss one**, and " +
                        "the next document saved there **starts the outbreak again**."
            ),
            steps = listOf(
                "Open the **outbreak map** and replay the week if you need to.",
                "For each computer, note **what was opened** there, not just whether the " +
                        "drive visited.",
                "Tap **BUILD THE CLEANUP LIST**. The colours are hidden in this mode.",
                "Tap **every computer** with an infected template, on the map or in the " +
                        "list, then tap **SUBMIT CLEANUP LIST**.",
                "When it's accepted, **swipe the bar** down and type the **ticket code** " +
                        "**exactly as written**."
            ),
            entryPage = "map.html",
            requiredClues = listOf(VirusesTrojansClues.CLEANUP_CORRECT),
            lockedMessage = "Submit a correct cleanup list on the outbreak map first.",
            answer = LabAnswer.Flag(
                sha256 = "df26ab1130e933340e646e9f3ef3515149d1618ea9e688a7fb9a77ba50ed9daa"
            ),
            hints = listOf(
                "The treasurer copied the file but never opened it in Word. The print kiosk " +
                        "only printed the logo.",
                "Four computers had an infected document opened on them."
            ),
            successFeedback = "ORGPC-01, LIB-PC-07, LAB1-PC-03 and ADVISER-LAPTOP. Each had an " +
                    "infected document opened, so each template is infected. The treasurer's " +
                    "laptop only carried the file, and the kiosk only printed a PNG. Neither " +
                    "ran the macro. Reformatting the drive alone would have left four computers " +
                    "infecting every new document.",
            failureFeedback = "Not the ticket code. It appears on the map once your cleanup " +
                    "list is exactly right."
        )
    )
)
