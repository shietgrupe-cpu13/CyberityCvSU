package com.cyberity.cvsu

// ===========================================================================
// LEVEL 102 CONTENT — Cybersecurity Threats, as a SOC alert console
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, exactly as level 101.

/** Clue ids reported by the threat console through the JS bridge. */
object ThreatClues {
    const val ALERT_OPENED_A1 = "alert_opened_a1"
    const val ALERT_OPENED_A3 = "alert_opened_a3"
    const val ASSET_NOTES_OPENED = "asset_notes_opened"
    const val AUTH_LOG_OPENED = "auth_log_opened"
    const val PROCESS_TREE_OPENED = "process_tree_opened"
    const val NETWORK_OPENED = "network_opened"
    const val LOG_SEARCH_USED = "log_search_used"
}

val threatClueLabels: Map<String, String> = mapOf(
    ThreatClues.ALERT_OPENED_A1 to "Opened the traffic-spike alert",
    ThreatClues.ALERT_OPENED_A3 to "Opened the sign-in failure alert",
    ThreatClues.ASSET_NOTES_OPENED to "Read the asset notes",
    ThreatClues.AUTH_LOG_OPENED to "Opened the authentication log",
    ThreatClues.PROCESS_TREE_OPENED to "Opened the process tree",
    ThreatClues.NETWORK_OPENED to "Opened the network connections",
    ThreatClues.LOG_SEARCH_USED to "Queried the raw log archive"
)

fun threatConsoleLab(): LabDefinition = LabDefinition(
    levelId = 102,
    title = "Threat Console",
    subtitle = "Campus SOC · Night Shift",
    briefing = "Five alerts fired on the campus network overnight and nobody has looked at " +
            "them yet. Each one has evidence attached — logs, processes, connections. Your job " +
            "is to work out what each alert actually is, not what the alert title claims.",
    assetDir = "threat_console",
    startPage = "console.html",
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Clear the noise",
            objective = "Not every alert is an attack. Open the alerts, read the evidence " +
                    "attached to each, and identify the one that is a false positive.",
            entryPage = "console.html",
            requiredClues = listOf(ThreatClues.ALERT_OPENED_A1),
            lockedMessage = "Open the alerts and read their evidence before ruling any of them out.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "A1 · REG-SRV-02 · Outbound traffic spike",
                    "A2 · LAB-PC-07 · Mass file rename",
                    "A3 · LIB-PC-14 · Repeated sign-in failures",
                    "A4 · STAFF-LAPTOP-9 · USB storage connected",
                    "A5 · WEB-01 · Connection flood"
                ),
                correctIndex = 0
            ),
            hints = listOf(
                "One alert's evidence explains itself completely. Check the asset notes.",
                "Traffic is only suspicious if nothing scheduled accounts for it."
            ),
            successFeedback = "The spike matched the registrar server's nightly backup window, " +
                    "destination included. Triage is mostly this: proving normal activity is " +
                    "normal, so the real alerts get your attention.",
            failureFeedback = "That alert has evidence of genuine hostile activity. Look for " +
                    "the one where a legitimate scheduled process accounts for everything."
        ),

        LabTask(
            id = "t2",
            title = "Classify the threat",
            objective = "Alert A3 on LIB-PC-14 is real. Read its evidence and classify what " +
                    "kind of threat it is.",
            requiredClues = listOf(ThreatClues.ALERT_OPENED_A3),
            lockedMessage = "Open alert A3 and read what the evidence actually shows.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Malware infection",
                    "Credential attack — automated password guessing",
                    "Denial of service",
                    "Physical media policy breach"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Count the failures, then look at how long they took.",
                "No human types 312 passwords in six minutes."
            ),
            successFeedback = "Hundreds of failures against one account in minutes, then a " +
                    "success — that's a brute-force credential attack, and the success at the " +
                    "end is the part that matters. The account is now compromised.",
            failureFeedback = "Look again at what the evidence shows: what is being attacked, " +
                    "and by what method? The file and process evidence is clean on this host."
        ),

        LabTask(
            id = "t3",
            title = "Find the source",
            objective = "Open the authentication evidence on A3 and submit the address the " +
                    "sign-in attempts came from.",
            requiredClues = listOf(ThreatClues.AUTH_LOG_OPENED),
            lockedMessage = "Expand the authentication log inside alert A3.",
            answer = LabAnswer.Text(
                accepted = listOf("45.61.87.200"),
                placeholder = "e.g. 10.0.0.1"
            ),
            hints = listOf(
                "Every failed sign-in row carries the address it came from.",
                "All 312 failures share one source. That's the machine, not the student."
            ),
            successFeedback = "One source, one target account. An indicator like this is what " +
                    "you pivot on — everything that address touched is now worth checking.",
            failureFeedback = "That isn't the source of the attempts. Expand the authentication " +
                    "log and read the src field on the failed rows."
        ),

        LabTask(
            id = "t4",
            title = "Pivot on the indicator",
            objective = "The raw log archive holds every event from every host. Query it for " +
                    "the address you just identified and submit the flag hidden in the results.",
            entryPage = "logs.html",
            requiredClues = listOf(ThreatClues.LOG_SEARCH_USED),
            lockedMessage = "The archive shows nothing until you query it. Search for your indicator.",
            answer = LabAnswer.Flag(
                sha256 = "2535842ccb35f3ca865618379b8426b6ba6d5900f0674605ed52b0c567793e5a"
            ),
            hints = listOf(
                "The archive only returns rows matching your query — paste the address in.",
                "Read every returned row, not just the failures. One of them is an audit entry."
            ),
            successFeedback = "That's the pivot: one indicator, searched across every host, " +
                    "turns a single alert into the full picture of what the attacker touched.",
            failureFeedback = "Not the flag. Query the archive for the source address and read " +
                    "the rows it returns carefully."
        ),

        LabTask(
            id = "t5",
            title = "Contain it",
            objective = "You've confirmed a compromised account on a shared library machine. " +
                    "Choose the containment action.",
            entryPage = "console.html",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Wipe and reimage LIB-PC-14",
                    "Block the source address and reset the compromised account's password",
                    "Email the student to ask whether the sign-ins were theirs",
                    "Raise the account's lockout threshold so it stops alerting"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "The attacker got in. Which asset was actually compromised — the machine, or " +
                        "the credential?"
            ),
            successFeedback = "Cut the access, then invalidate what was stolen. Reimaging the " +
                    "shared PC does nothing here — the attacker holds a working password and " +
                    "can use it from anywhere.",
            failureFeedback = "Think about what the attacker walked away with, and which action " +
                    "actually takes it back from them."
        )
    )
)
