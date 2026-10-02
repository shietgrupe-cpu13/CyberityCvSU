package com.cyberity.cvsu

// ===========================================================================
// LEVEL 403 CONTENT — Ransomware, one night in the CEIT department
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 401-402:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 04/ransom_night.
//
// 405 covers the first response on one PC (disconnect, don't pay, keep the
// note). This level goes past that, with one tool per task:
//   monitor.html  live counter + network diagram: isolate the source in time
//   chat.html     the gang's support portal: double extortion
//   backups.html  test-restore five "backups": sync and connected drives fail
//   restore.html  restore from before the break-in, not before the encryption
//   plan.html     build a 3-2-1 plan with one copy offline or immutable

/** Clue ids reported by the ransomware simulation through the JS bridge. */
object RansomwareClues {
    const val MONITOR_VIEWED = "monitor_viewed"
    const val CONTAINED = "contained"
    const val CHAT_OPENED = "chat_opened"
    const val LEAK_THREAT = "leak_threat"
    const val CHAT_COMPLETE = "chat_complete"
    const val BACKUPS_OPENED = "backups_opened"
    const val TESTED_ALL = "tested_all"
    const val RESTORE_OPENED = "restore_opened"
    const val RESTORE_TRIED = "restore_tried"
    const val CLEAN_RESTORE = "clean_restore"
    const val PLAN_OPENED = "plan_opened"
    const val PLAN_VALID = "plan_valid"

    /** Dangerous: agreeing to pay in the gang's chat. */
    const val RANSOM_PAID = "ransom_paid"

    /** Dangerous: plugging the last clean offline backup into the infected PC. */
    const val BACKUP_EXPOSED = "backup_exposed"
}

val ransomwareClueLabels: Map<String, String> = mapOf(
    RansomwareClues.MONITOR_VIEWED to "Opened the live share monitor",
    RansomwareClues.CONTAINED to "Isolated the machine doing the encrypting",
    RansomwareClues.CHAT_OPENED to "Opened the gang's support portal",
    RansomwareClues.LEAK_THREAT to "Found out what they stole",
    RansomwareClues.CHAT_COMPLETE to "Asked the gang every question",
    RansomwareClues.BACKUPS_OPENED to "Opened the backup test bench",
    RansomwareClues.TESTED_ALL to "Test-restored all five backups",
    RansomwareClues.RESTORE_OPENED to "Opened the restore point picker",
    RansomwareClues.RESTORE_TRIED to "Test-restored a snapshot",
    RansomwareClues.CLEAN_RESTORE to "Found the clean restore point",
    RansomwareClues.PLAN_OPENED to "Opened the 3-2-1 plan builder",
    RansomwareClues.PLAN_VALID to "Built a plan that passes every rule"
)

fun ransomwareLab(): LabDefinition = LabDefinition(
    levelId = 403,
    title = "Ransomware at 2 AM",
    subtitle = "Ransomware · CEIT Department",
    briefing = "Ransomware is malware that encrypts your files and demands payment " +
            "for the key. Modern encryption can't be cracked or guessed, so without the key " +
            "the files are gone. That leaves exactly two ways back: pay the criminals, or " +
            "restore from a backup they couldn't reach.\n\n" +
            "It's Thursday, 2:14 AM. ITSO's on-call phone goes off: files on the CEIT " +
            "department's shared drive are being renamed to .lockd, and the count is still " +
            "climbing. Grade sheets, enrollment records and five years of theses are on that " +
            "share.\n\n" +
            "You have five tools: a live share monitor, the gang's own support chat, a " +
            "backup test bench, a restore point picker and a backup plan builder. " +
            "Agreeing to pay, or plugging the last clean backup into the infected PC, " +
            "costs a heart.\n\n" +
            "Each task explains what to look for, then hands you the right tool. Open it with " +
            "the button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 04/ransom_night",
    startPage = "monitor.html",
    dangerousClues = listOf(
        RansomwareClues.RANSOM_PAID,
        RansomwareClues.BACKUP_EXPOSED
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Stop the counter",
            objective = "Files are being encrypted **right now**. Find the machine doing it in the " +
                    "**live feed**, **unplug its cable** on the network diagram, and submit its " +
                    "**name**.",
            guide = listOf(
                "Ransomware encrypts **everything it can reach**: the infected PC's own disk, " +
                        "and every **shared folder** and **mapped drive** it has access to. One " +
                        "infected PC on a department network can lock the whole department's " +
                        "files. That's why **every second counts**.",
                "The first move is **containment**: cut the infected machine off from the " +
                        "network so it can't reach anything else. You need to find **the " +
                        "source**, the machine actually writing the encrypted files. Cutting the " +
                        "**internet** doesn't help: the ransomware already has its key and its " +
                        "targets **inside** the network.",
                "**Isolate, don't power off.** Leave the infected PC **turned on** but " +
                        "disconnected. What's in its memory is **evidence** of how the attack " +
                        "worked, and switching it off erases it."
            ),
            steps = listOf(
                "Open the **live share monitor**. Watch the **red counter** climb.",
                "Read the **file activity feed**. Most lines are encrypted writes ending in " +
                        "**.lockd**; a few are ordinary late-night activity.",
                "Find the **one machine** writing every .lockd file.",
                "On the **network diagram**, **tap that machine** to unplug its cable. A wrong " +
                        "guess doesn't cost a heart, but the counter keeps going.",
                "When the counter turns **green**, **swipe the bar** down and type the " +
                        "**machine's name**."
            ),
            entryPage = "monitor.html",
            requiredClues = listOf(RansomwareClues.CONTAINED),
            lockedMessage = "Stop the encryption on the live share monitor first.",
            answer = LabAnswer.Text(
                accepted = listOf("CEIT-SEC-02", "CEIT SEC 02", "CEITSEC02", "SEC-02"),
                placeholder = "e.g. CEIT-XXX-00"
            ),
            hints = listOf(
                "Every line ending in .lockd starts with the same machine name.",
                "It's the secretary's PC."
            ),
            successFeedback = "CEIT-SEC-02, the secretary's PC. Pulling one cable stopped " +
                    "everything, while cutting the internet or the share wouldn't have. It stays " +
                    "powered on for the investigation. Next question: what do the people behind " +
                    "this actually want?",
            failureFeedback = "Not that one. Look at which machine name appears on every .lockd " +
                    "line in the feed."
        ),

        LabTask(
            id = "t2",
            title = "Talk to the gang",
            objective = "The ransom note links to a **support portal**. **Ask the gang " +
                    "questions**, including **what happens if you just restore from backup**, " +
                    "and find out **why backups alone don't end this**.",
            guide = listOf(
                "Ransomware gangs run like businesses, complete with **support chats**, " +
                        "**discounts** for paying fast and **deadlines** that double the price. " +
                        "All of it is **pressure**, designed to make a scared person decide " +
                        "before thinking.",
                "Most groups now use **double extortion**. Before they encrypt anything, they " +
                        "**steal a copy** of the data. Then they threaten **two things**: you " +
                        "won't get your files back, **and** they'll **publish** what they took. " +
                        "A good backup solves the first problem but **not the second**.",
                "Stolen **student records** make this a **data breach** too. In the " +
                        "Philippines, the **Data Privacy Act** requires reporting a breach " +
                        "like this to the **National Privacy Commission** within **72 hours**. " +
                        "And paying guarantees nothing: the stolen copy stays in criminal hands."
            ),
            steps = listOf(
                "Open the **support portal**. Read the **ransom note** at the top.",
                "Tap the **questions** to ask the gang. Asking is free.",
                "Make sure you ask: **\"We have backups. Why would we pay?\"** Read the reply " +
                        "and the **sample** they attach.",
                "**Don't tap \"OK. We will pay.\"** It costs a heart.",
                "**Swipe the bar** down and choose **why a backup doesn't end this**."
            ),
            entryPage = "chat.html",
            requiredClues = listOf(RansomwareClues.LEAK_THREAT),
            lockedMessage = "Ask the gang what happens if you restore from backup first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "It does. Once the files are restored, the gang has nothing left",
                    "They stole a copy of the student records before encrypting, and threaten " +
                            "to publish it. A restore can't take that back",
                    "Backups can't be restored once ransomware has been on the network",
                    "The gang will encrypt the backup as soon as it's restored"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "What did they say they did BEFORE they encrypted?",
                "Look at the file they attached as a \"sample\"."
            ),
            successFeedback = "Double extortion. They took 18 GB of student records first, so " +
                    "restoring the files solves only half the problem. That's also a personal " +
                    "data breach: CvSU must notify the National Privacy Commission and the " +
                    "affected students. Paying wouldn't un-steal anything.",
            failureFeedback = "Ask the gang what happens if you just restore, and read what " +
                    "they say they did before encrypting."
        ),

        LabTask(
            id = "t3",
            title = "Which backups survived?",
            objective = "CEIT had **five things** it called backups. **Test-restore every one** " +
                    "on a clean PC and work out **which survived, and why**.",
            guide = listOf(
                "A backup only helps against ransomware if the ransomware **couldn't reach it**. " +
                        "Anything the infected PC could **write to**, like a USB drive left " +
                        "plugged in or a backup folder mapped as a drive, gets **encrypted " +
                        "along with everything else**.",
                "**Sync is not backup.** A cloud sync folder copies **every change** right " +
                        "away, and encryption is a change. Within minutes the cloud holds the " +
                        "**encrypted** files too. A real backup keeps **older versions** that " +
                        "later changes can't overwrite.",
                "The backups that survive are **offline**, unplugged and stored away, or " +
                        "**immutable**: versions that can't be changed or deleted for a set " +
                        "time, **even with an admin password**. Attackers hunt for admin " +
                        "passwords precisely so they can delete backups first."
            ),
            steps = listOf(
                "Open the **backup test bench**. Five backups are listed.",
                "Tap **TEST RESTORE ON A CLEAN PC** on **each one** and read the result.",
                "Notice **how each was connected** when the encryption ran.",
                "**Don't** tap **PLUG INTO CEIT-SEC-02** on the offline disk. Restoring onto the " +
                        "infected PC costs a heart.",
                "**Swipe the bar** down and choose **which survived, and why**."
            ),
            entryPage = "backups.html",
            requiredClues = listOf(RansomwareClues.TESTED_ALL),
            lockedMessage = "Test-restore all five backups first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The cloud sync folder, because it's stored off campus",
                    "The USB drive, because it copied the files every single night",
                    "The offline disk and the immutable cloud backup: the ransomware couldn't " +
                            "reach or change either one",
                    "All five. Backups can't be encrypted"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Which ones were connected to the infected PC, or deletable with an admin " +
                        "password?",
                "Being off campus isn't enough if it syncs every change."
            ),
            successFeedback = "The offline disk and the immutable cloud backup. The USB drive " +
                    "was plugged in, the NAS snapshots were deleted with a stolen admin " +
                    "password, and the sync folder faithfully copied the encryption. Three of " +
                    "CEIT's five \"backups\" weren't really backups at all.",
            failureFeedback = "Look at how each backup was connected when the encryption ran. " +
                    "Only the ones the ransomware couldn't reach or change survived."
        ),

        LabTask(
            id = "t4",
            title = "Before the break-in",
            objective = "The immutable backup has four snapshots. **Pick one**, **restore it to a " +
                    "test PC** and **scan it**. When you find the one that's **safe to put back**, " +
                    "submit its **recovery code**.",
            guide = listOf(
                "The encryption is the **end** of a ransomware attack, not the beginning. " +
                        "Attackers usually get in **days earlier**, then quietly look around, " +
                        "steal data and delete backups. That quiet stretch is called **dwell " +
                        "time**.",
                "Anything backed up **during** that time includes **whatever the attacker " +
                        "installed**, like a remote-access tool. Restore that snapshot and the " +
                        "files come back, but **so does the attacker's way in**.",
                "So the rule is: **restore from before the break-in, not just before the " +
                        "encryption**. That usually means losing a few more days of work. Losing " +
                        "work is recoverable; letting the attacker back in isn't."
            ),
            steps = listOf(
                "Open the **restore point picker** and read the **week's timeline**. Red lines " +
                        "are the attacker; blue are backups.",
                "Find **when the attacker first got in**.",
                "Pick a **snapshot** and tap **RESTORE TO A TEST PC AND SCAN**. Try as many as " +
                        "you like; testing is free.",
                "When a restore scans **clean**, a **recovery code** appears.",
                "**Swipe the bar** down and type it **exactly as written**."
            ),
            entryPage = "restore.html",
            requiredClues = listOf(RansomwareClues.CLEAN_RESTORE),
            lockedMessage = "Find a snapshot that restores and scans clean first.",
            answer = LabAnswer.Flag(
                sha256 = "1c25247c96acc13ee475c49ecdbea22464948b4d6fe493282a3c8c9e9f3f5059"
            ),
            hints = listOf(
                "The encryption was Thursday. When was the email attachment opened?",
                "Every snapshot after Monday 09:12 contains RemoteSupport_svc.exe."
            ),
            successFeedback = "Sunday 23:00, the last snapshot before Monday's email. The " +
                    "Monday to Wednesday snapshots all carry the remote-access tool installed " +
                    "at 09:12 on Monday. Restoring any of them would have handed the attacker " +
                    "their door back. Three days of edits is the price, and it's worth it.",
            failureFeedback = "Not the recovery code. It appears only when a restored snapshot " +
                    "scans clean."
        ),

        LabTask(
            id = "t5",
            title = "Never again",
            objective = "Open the **plan builder** and set up **two backup copies** so the plan " +
                    "passes **every rule** on the checklist. Then decide **which copy actually " +
                    "beats ransomware**.",
            guide = listOf(
                "The classic backup rule is **3-2-1**: keep **3** copies of your data, on **2** " +
                        "different kinds of storage, with **1** copy **off-site**. It protects " +
                        "against a dead disk, a fire, or a stolen laptop.",
                "Ransomware added one more rule: at least **1** copy must be **offline** or " +
                        "**immutable**, so that nobody on the network, not even someone with the " +
                        "admin password, can encrypt or delete it. Some people write this as " +
                        "**3-2-1-1**.",
                "And one last habit: **test your restores**. CEIT found out at 2 AM that three " +
                        "of its five backups didn't work. A backup you've never restored is only " +
                        "a hope."
            ),
            steps = listOf(
                "Open the **plan builder**. **Copy 1** is the live share; it's fixed.",
                "For **Copy 2** and **Copy 3**, choose a **medium**, **where** it's kept, and its " +
                        "**protection**.",
                "Watch the **checklist**. Each rule turns **green** when your plan meets it.",
                "Get **all four** rules green.",
                "**Swipe the bar** down and choose **which copy actually beats ransomware**."
            ),
            entryPage = "plan.html",
            requiredClues = listOf(RansomwareClues.PLAN_VALID),
            lockedMessage = "Build a plan that turns every checklist rule green first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The live share, because it's the newest",
                    "Any of them, as long as there are three",
                    "The copy that's offline or immutable, because ransomware and a stolen " +
                            "admin password can't reach or change it",
                    "The copy kept on campus, because it's fastest to restore"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Three copies all connected to the network can be encrypted together.",
                "Which rule on the checklist was added because of ransomware?"
            ),
            successFeedback = "The offline or immutable copy. 3-2-1 protects you from disks " +
                    "dying and buildings burning; the extra 1 protects you from someone inside " +
                    "your network with your admin password. Add monthly test restores, and the " +
                    "next 2 AM page ends with a restore instead of a ransom.",
            failureFeedback = "Think back to the backup bench. Which kind of copy was still " +
                    "standing in the morning?"
        )
    )
)
