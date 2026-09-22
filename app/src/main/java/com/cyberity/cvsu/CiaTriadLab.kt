package com.cyberity.cvsu

// ===========================================================================
// LEVEL 103 CONTENT — CIA Triad, as a registrar incident desk
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, exactly as levels 101-102.

/** Clue ids reported by the incident desk through the JS bridge. */
object CiaClues {
    const val TICKET_OPENED_301 = "ticket_opened_inc301"
    const val TICKET_OPENED_302 = "ticket_opened_inc302"
    const val TICKET_OPENED_303 = "ticket_opened_inc303"
    const val ACCESS_LOG_OPENED = "access_log_opened"
    const val SHARING_RESTRICTED = "sharing_restricted"
    const val INTEGRITY_CHECK_RUN = "integrity_check_run"
    const val DIFF_VIEWED = "diff_viewed"
    const val DISK_USAGE_OPENED = "disk_usage_opened"

    /** Dangerous: blessing tampered files as the new trusted baseline. */
    const val BASELINE_OVERWRITTEN = "baseline_overwritten"
}

val ciaClueLabels: Map<String, String> = mapOf(
    CiaClues.TICKET_OPENED_301 to "Opened the exposed-file ticket",
    CiaClues.TICKET_OPENED_302 to "Opened the changed-grade ticket",
    CiaClues.TICKET_OPENED_303 to "Opened the portal-outage ticket",
    CiaClues.ACCESS_LOG_OPENED to "Read the file access log",
    CiaClues.SHARING_RESTRICTED to "Restricted the file's sharing",
    CiaClues.INTEGRITY_CHECK_RUN to "Ran the integrity checker",
    CiaClues.DIFF_VIEWED to "Viewed the tampered file's changes",
    CiaClues.DISK_USAGE_OPENED to "Inspected the database server's disk"
)

fun ciaTriadLab(): LabDefinition = LabDefinition(
    levelId = 103,
    title = "Registrar Incident Desk",
    subtitle = "CIA Triad · Enrollment Week",
    briefing = "Three tickets reached the Registrar's IT desk during enrollment week. Each one " +
            "breaks a different pillar of the CIA triad — confidentiality, integrity, or " +
            "availability. Use the admin tools to find what broke, prove it, and fix it.\n\n" +
            "Each task explains what to look for, then hands you the desk. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "cia_triad",
    startPage = "desk.html",
    dangerousClues = listOf(CiaClues.BASELINE_OVERWRITTEN),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Map the pillars",
            objective = "Open all three tickets and read what each reporter describes. " +
                    "Match every ticket to the pillar it breaks.",
            guide = listOf(
                "The CIA triad is the frame the whole field is built on, and it has nothing to " +
                        "do with the agency. Confidentiality means only the people who should " +
                        "see data can see it. Integrity means the data is accurate and has not " +
                        "been altered without authorisation. Availability means it is there when " +
                        "the people who need it need it.",
                "It earns its place because it turns a vague \"security incident\" into a " +
                        "specific question with a specific fix. Ask of any incident: was the data " +
                        "seen, changed, or unreachable? Each answer points at a different " +
                        "toolbox — access control, verification, resilience.",
                "One incident can damage more than one pillar, and often does in sequence. But " +
                        "every ticket has a primary pillar — the one the reporter is actually " +
                        "describing — and that is the one that decides your first move. Watch " +
                        "for what each reporter explicitly rules out; it is as useful as what " +
                        "they report."
            ),
            steps = listOf(
                "Open the simulation. Three tickets are waiting on the Registrar IT desk, each " +
                        "marked new until you read it.",
                "Tap a ticket, read the reporter's own words, and expand the evidence panels " +
                        "attached to it.",
                "Use ← Desk to go back, and do the same for the other two.",
                "For each one, ask a single question: was data seen by the wrong people, " +
                        "changed without permission, or simply out of reach?",
                "Note what each reporter says did NOT happen — INC-303's reporter rules out two " +
                        "of the three pillars for you.",
                "Come back and match all three at once."
            ),
            entryPage = "desk.html",
            requiredClues = listOf(
                CiaClues.TICKET_OPENED_301,
                CiaClues.TICKET_OPENED_302,
                CiaClues.TICKET_OPENED_303
            ),
            lockedMessage = "Open all three tickets before you classify them.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "INC-301 Availability · INC-302 Confidentiality · INC-303 Integrity",
                    "INC-301 Integrity · INC-302 Availability · INC-303 Confidentiality",
                    "INC-301 Confidentiality · INC-302 Integrity · INC-303 Availability",
                    "INC-301 Confidentiality · INC-302 Availability · INC-303 Integrity"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Ask one question per ticket: was data seen, changed, or unreachable?",
                "Seen by the wrong people = confidentiality. Changed without permission = " +
                        "integrity. Can't get to it = availability."
            ),
            successFeedback = "Seen, changed, unreachable. Every security incident damages at " +
                    "least one of these three, and naming the right one tells you which fix to reach for.",
            failureFeedback = "At least one ticket is mismatched. For each, ask whether the data " +
                    "was exposed, altered, or simply couldn't be reached."
        ),

        LabTask(
            id = "t2",
            title = "Lock down the leak",
            objective = "INC-301: the enrollment masterlist is exposed. In the file sharing " +
                    "settings, restrict the file, then read the access log and submit how many " +
                    "times it was accessed by someone who wasn't signed in.",
            guide = listOf(
                "Confidentiality is enforced with access control, and the setting behind most " +
                        "real-world leaks is the friendliest one: \"anyone with the link\". " +
                        "Nobody has to be attacked for that to fail. The file only has to be " +
                        "shared once too widely, and links travel further than the person who " +
                        "shared them ever intended.",
                "The principle to apply is least privilege: grant the smallest access that " +
                        "still lets the work happen, and nothing beyond it. \"CvSU only\" sounds " +
                        "safe because it requires a sign-in — but on a file holding 1,284 home " +
                        "addresses it still means every student on campus can open it.",
                "It is also worth seeing where the fix stops. Restricting the link prevents " +
                        "further access; it does nothing about copies already downloaded. " +
                        "Confidentiality is something you set up front, not something you " +
                        "restore afterwards."
            ),
            steps = listOf(
                "Open the simulation, open INC-301, and expand the file details evidence.",
                "Go to File sharing — either from the button at the bottom of the ticket, or " +
                        "from Admin tools on the desk.",
                "Read all three access options, pick the one that gives the least access that " +
                        "still works, and tap SAVE SHARING.",
                "Read the banner it gives you — it says exactly what that choice still allows.",
                "Expand the Access log below the sharing options.",
                "Count every row whose user is anonymous. Downloads count as well as views.",
                "Come back and submit that number."
            ),
            entryPage = "share.html",
            requiredClues = listOf(CiaClues.SHARING_RESTRICTED, CiaClues.ACCESS_LOG_OPENED),
            lockedMessage = "Restrict the file's sharing and open its access log first.",
            answer = LabAnswer.Text(
                accepted = listOf("4", "four"),
                placeholder = "e.g. 2"
            ),
            hints = listOf(
                "Only one sharing option limits the file to the people who need it.",
                "Count every row whose user is 'anonymous' — views and downloads both count."
            ),
            successFeedback = "Four anonymous accesses, one of them a download. Restricting the " +
                    "link stops new exposure, but it can't un-leak a downloaded copy — that's why " +
                    "confidentiality is protected up front, with least-privilege sharing.",
            failureFeedback = "Not the right count. Open the access log and count every row " +
                    "made by an anonymous user, not just the views."
        ),

        LabTask(
            id = "t3",
            title = "Prove the tampering",
            objective = "INC-302: a grade changed after it was submitted. Run the integrity " +
                    "checker on the record files and submit the name of the file that no longer " +
                    "matches its baseline.",
            guide = listOf(
                "Integrity is not about preventing change — records are meant to change. It is " +
                        "about being able to prove which changes were authorised, and that " +
                        "requires something trustworthy to compare against.",
                "A hash, here SHA-256, is a fingerprint of a file's exact contents. Change a " +
                        "single character and the fingerprint changes completely and " +
                        "unpredictably. Sign a baseline of those fingerprints while the files " +
                        "are known to be good, and any later change becomes detectable. The " +
                        "hash won't tell you what changed — only, with certainty, that " +
                        "something did.",
                "This is why tampering is invisible to the eye and obvious to a checker. The " +
                        "altered grade sheet still opens, still looks like a grade sheet, and " +
                        "still has a plausible number in it. Only the fingerprint gives it away."
            ),
            steps = listOf(
                "Open the simulation and open the Integrity checker from Admin tools, or from " +
                        "INC-302's button.",
                "Read the baseline note at the top: when the fingerprints were signed, and by " +
                        "what.",
                "Tap COMPUTE on every file, not just the first one.",
                "Watch for the red MISMATCH verdict — the rest will come back MATCH.",
                "Do not touch \"ACCEPT CURRENT FILES AS NEW BASELINE\". That signs the tampered " +
                        "file as trusted and destroys the only proof it was changed. It costs " +
                        "you a heart.",
                "Come back and submit the name of the mismatched file."
            ),
            entryPage = "verify.html",
            requiredClues = listOf(CiaClues.INTEGRITY_CHECK_RUN),
            lockedMessage = "Compute at least one file's hash in the integrity checker.",
            answer = LabAnswer.Text(
                accepted = listOf("grades_bsit3a_2026.csv", "grades_bsit3a_2026"),
                placeholder = "e.g. records_2026.csv"
            ),
            hints = listOf(
                "Compute every file, not just the first one.",
                "A single changed character produces a completely different hash."
            ),
            successFeedback = "The hash is the proof. The file looks normal when you open it, " +
                    "but its fingerprint no longer matches the one signed before the change.",
            failureFeedback = "That file still matches its baseline. Compute every file and " +
                    "look for the red mismatch."
        ),

        LabTask(
            id = "t4",
            title = "Trace the change",
            objective = "Open the changes on the tampered file and read its audit trail. The " +
                    "tool that made the edit left a flag behind — submit it.",
            guide = listOf(
                "Detecting a change is the first half of integrity. An audit trail is the " +
                        "second: it records who made a request, when, from where, and with what " +
                        "tool. That is what turns \"this file changed\" into an account of what " +
                        "actually happened, which is what an investigation — or a disciplinary " +
                        "case — needs.",
                "A diff shows the change itself: the value before and the value after, side by " +
                        "side. Together they answer both questions. What changed, and by whose " +
                        "hand.",
                "Read the source address on this one carefully, and compare it with the " +
                        "brute-force alert from the Threat Console level. A password stolen in " +
                        "one incident being used to edit a record in another is exactly how real " +
                        "intrusions move: a confidentiality failure becomes the way into an " +
                        "integrity failure. Pillars fall in sequence, not in isolation."
            ),
            steps = listOf(
                "Open the simulation and return to the Integrity checker.",
                "On the mismatched file, tap VIEW CHANGES.",
                "Read the diff: the red line is what the value was, the green line is what it " +
                        "is now.",
                "Below the diff, read the audit trail for that row — time, user, source address " +
                        "and user-agent.",
                "Ask yourself what kind of client that user-agent describes, and whether a " +
                        "professor grading papers would produce it.",
                "Come back and submit the flag in full, CYBERITY{...} wrapper included."
            ),
            requiredClues = listOf(CiaClues.DIFF_VIEWED),
            lockedMessage = "Open 'View changes' on the mismatched file.",
            answer = LabAnswer.Flag(
                sha256 = "68463bf03420cf8fdabc2b4718941f61fda62967223c4ffe0147351711d3e3ce"
            ),
            hints = listOf(
                "The diff shows what changed. The audit trail below it shows how.",
                "Look at the user-agent of the request that made the edit."
            ),
            successFeedback = "Captured. Notice the source address: it's the same one from last " +
                    "night's brute-force alert. The stolen password was used to change a grade — " +
                    "a confidentiality failure turned into an integrity failure.",
            failureFeedback = "Not the flag. It's written in full in the audit trail, " +
                    "wrapper included."
        ),

        LabTask(
            id = "t5",
            title = "Restore availability",
            objective = "INC-303: the portal is down with enrollment closing Friday. Check the " +
                    "service status, find the cause, and choose the fix.",
            guide = listOf(
                "Availability is the pillar people forget until it is the only one that " +
                        "matters. A record nobody can reach on the day enrollment closes has " +
                        "failed as completely as one that leaked — the difference is that this " +
                        "failure is noticed immediately, by everyone, at the counter.",
                "Availability incidents are frequently not attacks at all. Full disks, expired " +
                        "certificates, a setting left switched on after maintenance: unglamorous " +
                        "causes with the same effect as a deliberate denial of service. So check " +
                        "whether the traffic is actually abnormal before you assume an attacker " +
                        "is involved.",
                "Two rules for the fix. Treat the cause, not the symptom — restarting a service " +
                        "whose disk is still full just buys you minutes. And never trade one " +
                        "pillar for another: freeing space by deleting live records fixes " +
                        "availability by destroying integrity, which is a worse incident than " +
                        "the one you were called about."
            ),
            steps = listOf(
                "Open the simulation and open Service status from Admin tools.",
                "Expand each service in turn and note which are up, degraded and down.",
                "On portal-web, check the request rate against what is normal for enrollment " +
                        "week before blaming traffic.",
                "Expand records-db and read the disk usage: what is on the disk, and how large " +
                        "is each thing?",
                "Compare the size of the records themselves against everything else on that " +
                        "disk.",
                "Read the change log at the bottom — something was switched on during " +
                        "maintenance and never switched back.",
                "Come back and choose the fix that removes that cause without destroying data."
            ),
            entryPage = "desk.html",
            requiredClues = listOf(CiaClues.DISK_USAGE_OPENED),
            lockedMessage = "Open the service status and inspect the failing server.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Block all off-campus traffic until enrollment closes",
                    "Delete the records database and restore last month's backup to free space",
                    "Turn off the leftover debug logging, archive and clear the log, then restart records-db",
                    "Tell students to try again after enrollment week"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Traffic is normal. Something on the server itself ran out.",
                "Fix the cause, not the symptom — and don't destroy good data to do it."
            ),
            successFeedback = "Availability failures aren't always attacks. A debug log left on " +
                    "after maintenance filled the disk. Removing the cause and capping the log " +
                    "restores service without losing a single record.",
            failureFeedback = "That doesn't address why the database stopped. Read the disk " +
                    "usage on records-db and fix the thing that filled it."
        )
    )
)
