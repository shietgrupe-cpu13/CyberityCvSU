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
            "is to work out what each alert actually is, not what the alert title claims.\n\n" +
            "Each task explains what to look for, then hands you the console. Open it with " +
            "the button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "threat_console",
    startPage = "console.html",
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Clear the noise",
            objective = "Not every alert is an attack. Open the alerts, read the evidence " +
                    "attached to each, and identify the one that is a false positive.",
            guide = listOf(
                "A SOC — Security Operations Centre — is where an organisation's alerts land, " +
                        "and the queue is always longer than the shift. Most of what fires is " +
                        "not an attack: a backup job, a patch cycle, a lecturer moving a large " +
                        "dataset at an odd hour. Triage is deciding quickly, and with evidence, " +
                        "which alerts actually deserve a human.",
                "An alert that fires on real but legitimate activity is called a false " +
                        "positive. You prove one by finding something that accounts for what " +
                        "happened — a schedule, a documented process, the host's role. You do " +
                        "not prove it from the alert title, which was written by a rule long " +
                        "before anyone knew the context.",
                "\"Accounts for\" is the strict part. Half an explanation is not an " +
                        "explanation: if a backup window explains the volume but not the " +
                        "destination, you keep digging. Closing a real attack as a false " +
                        "positive is the most expensive mistake in this job."
            ),
            steps = listOf(
                "Open the simulation. Five alerts are queued, each with a severity chip and " +
                        "the host it fired on.",
                "Tap an alert to open it, then tap each evidence panel to expand it.",
                "Use ← Queue to go back, and do the same for the rest.",
                "Pay attention to \"Asset notes\" — that is where a host's role and its " +
                        "scheduled jobs are recorded.",
                "Find the alert whose evidence accounts for everything: the volume, the " +
                        "destination and the timing.",
                "Swipe the console down and name that alert below."
            ),
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
            guide = listOf(
                "Classifying a threat means naming the technique behind it, and that name " +
                        "decides everything that follows: who gets called, what gets isolated, " +
                        "what gets reset. \"Something bad on LIB-PC-14\" cannot be acted on. " +
                        "\"Credential attack\" can.",
                "Four families cover most of what you will see. Malware is hostile code " +
                        "running on a host. A credential attack is someone guessing or stealing " +
                        "a login. Denial of service is drowning a service in traffic so real " +
                        "users can't reach it. A policy breach is a person doing something they " +
                        "shouldn't — no attacker involved at all.",
                "The evidence usually names the family for you. Malware shows up in processes " +
                        "and changed files. A credential attack shows up in authentication logs. " +
                        "Denial of service shows up as volume from many sources at once. What " +
                        "the evidence does NOT show is just as telling as what it does."
            ),
            steps = listOf(
                "Open the simulation and open alert A3 on LIB-PC-14.",
                "Expand every evidence panel attached to it, not just the first.",
                "In the authentication log, count the failures and check the timestamps — how " +
                        "long did all of them take?",
                "Ask whether a person could physically do that, or whether it took a machine.",
                "Note what the process and file evidence shows, and what it does not.",
                "Come back and classify the threat."
            ),
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
            guide = listOf(
                "An indicator of compromise — an IOC — is a concrete, searchable fact about an " +
                        "attack: an address, a file hash, a domain, an account name. It is what " +
                        "turns analysis into action. \"We were attacked\" cannot be blocked or " +
                        "searched for; an address can be both.",
                "Authentication logs record where every attempt came from, not just whether it " +
                        "worked. When hundreds of failures all share one source, that source is " +
                        "the attacker's machine. The account named in the log is the target, not " +
                        "the culprit — the student whose account it is has done nothing.",
                "Copy indicators exactly. One wrong digit and the search in the next task " +
                        "returns nothing, and \"nothing found\" is very easy to mistake for " +
                        "\"nothing happened\"."
            ),
            steps = listOf(
                "Open the simulation, open alert A3, and expand the Authentication log panel.",
                "Read the failed sign-in rows and find the src field on them.",
                "Check whether every failure shares one source, or whether they come from many.",
                "Note the address down character by character.",
                "Come back and submit it."
            ),
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
            guide = listOf(
                "Pivoting means taking one indicator and asking what else it touched. A single " +
                        "alert shows you one host on one night. The same indicator searched " +
                        "across every log shows you the shape of the whole intrusion: where " +
                        "else the attacker reached, what worked, and what they did once they " +
                        "were in.",
                "This is the step that turns an incident from \"one compromised account\" into " +
                        "a scope you can actually report — and it is why extracting the " +
                        "indicator carefully in the last task mattered.",
                "The archive works like a real log search. It holds every event from every " +
                        "host, and returns only the rows that match what you type. Read all of " +
                        "them: the interesting row is rarely the one you expected."
            ),
            steps = listOf(
                "Open the simulation. This task opens straight into the log archive — if you " +
                        "end up on the queue, use the log archive link at the top.",
                "Type the source address from Task 3 into the query box.",
                "Tap RUN.",
                "Read every row that comes back, not only the failures — including anything " +
                        "that reads like an audit entry.",
                "Come back and submit the flag in full, CYBERITY{...} wrapper included."
            ),
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
            guide = listOf(
                "Containment is the step between finding an intrusion and cleaning up after " +
                        "it: cut the attacker's access now, before anything else is decided. " +
                        "Done late, your investigation is still running while the attacker is " +
                        "still working.",
                "The right action follows from what was actually taken. If hostile code is " +
                        "running on a machine, you isolate the machine. If a credential was " +
                        "stolen, the machine barely matters — a working password can be used " +
                        "from anywhere in the world, and reimaging the PC it happened to be " +
                        "typed on takes nothing back from the attacker.",
                "Two traps worth naming. Raising a threshold so the alert stops firing is not " +
                        "containment, it is unplugging the smoke detector. And asking the " +
                        "account owner to confirm is a reasonable thing to do later — it is " +
                        "just not an action that removes anyone's access."
            ),
            steps = listOf(
                "Open the simulation if you want to re-read alert A3 before you decide.",
                "Ask what the attacker walked away with: access to a machine, or a working " +
                        "credential?",
                "Weigh each option by whether it actually takes that back from them."
            ),
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
