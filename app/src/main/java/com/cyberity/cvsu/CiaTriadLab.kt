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
            "availability. Use the admin tools to find what broke, prove it, and fix it.",
    assetDir = "cia_triad",
    startPage = "desk.html",
    dangerousClues = listOf(CiaClues.BASELINE_OVERWRITTEN),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Map the pillars",
            objective = "Open all three tickets and read what each reporter describes. " +
                    "Match every ticket to the pillar it breaks.",
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
