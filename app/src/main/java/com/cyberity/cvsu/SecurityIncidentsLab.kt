package com.cyberity.cvsu

// ===========================================================================
// LEVEL 601 CONTENT — Security Incidents, Monday morning at the ITSO desk
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, same format as 401-404:
// every task teaches first (guide), says exactly what to do (steps), then asks.
// Simulation assets live in assets/simulations/UNIT 06/incident_desk.
//
// Unit 6's opener. It sets the line between an event and an incident, and the
// first responder's three habits: don't destroy evidence, tell the right
// people, and work the incident that is still happening first. It ends with
// the report that hands the case to Detection (602) and Containment (603).

/** Clue ids reported by the incident desk through the JS bridge. */
object SecurityIncidentsClues {
    const val OPENED_T1 = "opened_t1"
    const val OPENED_T2 = "opened_t2"
    const val OPENED_T3 = "opened_t3"
    const val OPENED_T4 = "opened_t4"
    const val OPENED_T5 = "opened_t5"
    const val OPENED_T6 = "opened_t6"
    const val OPENED_T7 = "opened_t7"
    const val TRIAGE_COMPLETE = "triage_complete"
    const val EMAIL_T4 = "email_t4"
    const val RULES_T4 = "rules_t4"
    const val OUTBOX_T4 = "outbox_t4"
    const val FILE_T3 = "file_t3"
    const val POLICY_T3 = "policy_t3"
    const val UPDATE_T3 = "update_t3"
    const val DEVICE_T6 = "device_t6"
    const val CHAT_T4 = "chat_t4"
    const val REPORT_COMPLETE = "report_complete"

    /** Dangerous: well-meaning "fixes" that destroy evidence or spread the harm. */
    const val DELETED_EMAIL = "deleted_email"
    const val FORWARDED_EMAIL = "forwarded_email"
    const val RESTARTED_PC = "restarted_pc"
    const val POSTED_WALL = "posted_wall"
}

val securityIncidentsClueLabels: Map<String, String> = mapOf(
    SecurityIncidentsClues.OPENED_T1 to "Opened TKT-0139 · failed portal login",
    SecurityIncidentsClues.OPENED_T2 to "Opened TKT-0140 · antivirus on LAB04",
    SecurityIncidentsClues.OPENED_T3 to "Opened TKT-0141 · grades sheet on Facebook",
    SecurityIncidentsClues.OPENED_T4 to "Opened TKT-0144 · strange replies to a professor",
    SecurityIncidentsClues.OPENED_T5 to "Opened TKT-0142 · portal down Saturday",
    SecurityIncidentsClues.OPENED_T6 to "Opened TKT-0143 · stolen Guidance laptop",
    SecurityIncidentsClues.OPENED_T7 to "Opened TKT-0145 · firewall blocked connections",
    SecurityIncidentsClues.TRIAGE_COMPLETE to "Sorted all seven tickets correctly",
    SecurityIncidentsClues.EMAIL_T4 to "Read the email Prof. Ramos clicked",
    SecurityIncidentsClues.RULES_T4 to "Checked her mailbox rules",
    SecurityIncidentsClues.OUTBOX_T4 to "Checked her sent log",
    SecurityIncidentsClues.FILE_T3 to "Checked what the leaked sheet contains",
    SecurityIncidentsClues.POLICY_T3 to "Read the CvSU incident reporting policy",
    SecurityIncidentsClues.UPDATE_T3 to "Read the Registrar's follow-up",
    SecurityIncidentsClues.DEVICE_T6 to "Checked the stolen laptop's device record",
    SecurityIncidentsClues.CHAT_T4 to "Read the chat with Prof. Ramos",
    SecurityIncidentsClues.REPORT_COMPLETE to "Filed a complete incident report"
)

fun securityIncidentsLab(): LabDefinition = LabDefinition(
    levelId = 601,
    title = "Incident Desk",
    subtitle = "Security Incidents · ITSO",
    briefing = "Every defence in this course can fail. When one does, what happens in the " +
            "first hour decides how bad it gets, and the first person to hear about it is " +
            "rarely a security expert. It is whoever is sitting at the help desk.\n\n" +
            "It is Monday, 8:02 AM. You are the student assistant at the CvSU IT Services " +
            "Office service desk, and the staff member on duty is on leave. Seven reports " +
            "came in over the weekend. Some are real security incidents. Most are the normal " +
            "noise of a campus network.\n\n" +
            "Your job: sort them, protect the evidence, tell the right people, decide what " +
            "goes first, and write the report. Reading tickets is free. Actions that destroy " +
            "evidence or spread the damage cost a heart.\n\n" +
            "Each task explains what to look for, then hands you the desk. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "UNIT 06/incident_desk",
    startPage = "desk.html",
    dangerousClues = listOf(
        SecurityIncidentsClues.DELETED_EMAIL,
        SecurityIncidentsClues.FORWARDED_EMAIL,
        SecurityIncidentsClues.RESTARTED_PC,
        SecurityIncidentsClues.POSTED_WALL
    ),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Event or incident?",
            objective = "**Open all seven tickets**, then use the **triage board** to tag each " +
                    "one an **Event** or an **Incident**. When the board is right, answer " +
                    "**what separates the two**.",
            guide = listOf(
                "An **event** is anything observable that happens on a system or network: a " +
                        "login, a blocked download, a firewall drop, a server restart. A campus " +
                        "network produces **thousands of events a day**, and almost all of them " +
                        "are normal.",
                "A **security incident** is an event that **actually harms**, or is **about " +
                        "to harm**, the **confidentiality, integrity or availability** of " +
                        "information or systems, or that **breaks a security policy**. Data " +
                        "seen by people who shouldn't see it, an account in the wrong hands, a " +
                        "device with sensitive data gone missing.",
                "Two traps. A **scary-sounding** event isn't automatically an incident: a " +
                        "control that **blocked** an attack means the defence **worked**. And a " +
                        "**boring-sounding** report can be the worst one in the queue. Judge " +
                        "by **what was harmed**, not by how loud the ticket is."
            ),
            steps = listOf(
                "Open the desk. **Seven tickets** are waiting.",
                "Tap each ticket. Read the report, then open its **evidence**. Use **← Desk** " +
                        "to go back.",
                "For each one ask: did anything **actually get exposed, changed, taken or " +
                        "broken**, or did it just **happen**?",
                "Back on the desk, tap **OPEN THE TRIAGE BOARD**, tag all seven, and submit. " +
                        "The board says **how many** are right, not which.",
                "**Swipe the bar** down and pick **what makes something an incident**."
            ),
            entryPage = "desk.html",
            requiredClues = listOf(
                SecurityIncidentsClues.OPENED_T1,
                SecurityIncidentsClues.OPENED_T2,
                SecurityIncidentsClues.OPENED_T3,
                SecurityIncidentsClues.OPENED_T4,
                SecurityIncidentsClues.OPENED_T5,
                SecurityIncidentsClues.OPENED_T6,
                SecurityIncidentsClues.OPENED_T7,
                SecurityIncidentsClues.TRIAGE_COMPLETE
            ),
            lockedMessage = "Open all seven tickets and get the triage board right first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "They sounded the most alarming",
                    "Something was actually exposed, taken over or lost: confidentiality, " +
                            "integrity or availability was harmed",
                    "A security tool raised an alert about them",
                    "They happened outside office hours"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "The antivirus popup and the firewall alert sound scary. What did they actually " +
                        "let through?",
                "The portal outage was announced on Wednesday. Is a planned outage harm?"
            ),
            successFeedback = "Three incidents: the grades sheet (confidentiality), the " +
                    "professor's mailbox (an account in an attacker's hands) and the stolen " +
                    "laptop (a device with sensitive data gone). The antivirus block and the " +
                    "firewall drops were defences doing their job, the failed login was a typo, " +
                    "and the outage was planned. Loud isn't the same as harmful.",
            failureFeedback = "Not that. Two of the four events raised alerts, and one incident " +
                    "was reported almost apologetically. Ask what was actually harmed."
        ),

        LabTask(
            id = "t2",
            title = "Don't touch it",
            objective = "TKT-0144: Prof. Ramos thinks the strange replies are \"probably spam\". " +
                    "**Read the email she clicked** and **check her mailbox rules**, then decide " +
                    "**what to do right now**.",
            guide = listOf(
                "The first person to see an incident is usually **not** the person who will " +
                        "investigate it. Their job is to **preserve the evidence** and **report " +
                        "fast**. Investigators rebuild what happened from **logs, emails, files " +
                        "and memory**, and every well-meaning \"fix\" can **erase** them.",
                "Deleting the phishing email destroys the **sender, links and headers** that " +
                        "show where it came from and who else got it. Restarting or \"cleaning\" " +
                        "a PC wipes **what was running** and changes **timestamps**. Forwarding a " +
                        "live phishing link \"as a warning\" hands the link to **more people**.",
                "So the first responder's rule is: **don't change anything**, **write down what " +
                        "you saw and when**, and **report it** through the official channel. " +
                        "Stopping the attacker is **containment**, and that is the incident " +
                        "team's call (Level 603)."
            ),
            steps = listOf(
                "Open TKT-0144 and read Prof. Ramos's report.",
                "Tap **THE EMAIL SHE CLICKED**. Note the **link's real address** and **when** " +
                        "she entered her password.",
                "Tap **MAILBOX RULES**. Find the rule she didn't make: **when** it was created " +
                        "and **from where**.",
                "Look at the **action buttons** under the evidence, but think before tapping. " +
                        "Some of them cost a heart.",
                "**Swipe the bar** down and choose **what to do right now**."
            ),
            entryPage = "ticket.html#t4",
            requiredClues = listOf(
                SecurityIncidentsClues.EMAIL_T4,
                SecurityIncidentsClues.RULES_T4
            ),
            lockedMessage = "Open the email Prof. Ramos clicked and her mailbox rules first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Delete the phishing email so nobody else clicks it",
                    "Forward it to all staff with a warning not to click",
                    "Run a cleaner on her PC and restart it",
                    "Change nothing, write down what you saw and when, and report it to the " +
                            "ITSO incident line now"
                ),
                correctIndex = 3
            ),
            hints = listOf(
                "Which of these leaves the investigators the most to work with?",
                "Only one option doesn't change, delete or spread anything."
            ),
            successFeedback = "Change nothing, record, report. The email holds the attacker's " +
                    "domain and the list of who else received it; the hidden forwarding rule " +
                    "proves someone logged in as her at 2:10 AM Saturday from abroad. All of it " +
                    "survives because you didn't \"help\". And it isn't spam: her account is in " +
                    "an attacker's hands.",
            failureFeedback = "That one changes or spreads the evidence. The responder's first " +
                    "job is to keep things as they are and get the right people involved."
        ),

        LabTask(
            id = "t3",
            title = "Who do you tell?",
            objective = "TKT-0141: a student found the BSIT grades sheet on the Freedom Wall. " +
                    "**Check what's in the file** and **read the incident policy**, then choose " +
                    "**who must be told, and how fast**.",
            guide = listOf(
                "Reporting an incident goes through the **official channel**, never around it. " +
                        "At CvSU that means the **ITSO incident line**, which brings in the " +
                        "right people. **Never** post about it publicly, and **never** confront " +
                        "or tip off the person you think caused it.",
                "When **personal data** is involved, the **Data Privacy Act of 2012 (RA 10173)** " +
                        "applies. Every school has a **Data Protection Officer (DPO)**. The " +
                        "National Privacy Commission (**NPC**) requires a breach of **sensitive " +
                        "personal information** that could cause serious harm to be reported to " +
                        "the **NPC within 72 hours** of the school learning about it, and the " +
                        "**affected people** to be told.",
                "Under the law, **sensitive personal information** includes a person's **age or " +
                        "birthday**, **education** records and government-issued **ID numbers**. " +
                        "A grades sheet is exactly that. And the **72 hours** start when the " +
                        "school **first knew**, not when someone got around to the ticket."
            ),
            steps = listOf(
                "Open TKT-0141 and read the student's report.",
                "Tap **WHAT'S IN THE FILE**. Count the **students** and note which **kinds of " +
                        "data** are in it.",
                "Tap **INCIDENT POLICY** and find **who** must be told and **how fast**.",
                "**Don't post** a public warning. It costs a heart.",
                "**Swipe the bar** down and choose who must be told."
            ),
            entryPage = "ticket.html#t3",
            requiredClues = listOf(
                SecurityIncidentsClues.FILE_T3,
                SecurityIncidentsClues.POLICY_T3
            ),
            lockedMessage = "Check what's in the file and read the incident policy first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Only the Registrar staffer who shared it, so they can quietly fix it",
                    "The Freedom Wall, so every student can protect themselves",
                    "The ITSO incident line and the Data Protection Officer, who reports it to " +
                            "the NPC within 72 hours and notifies the affected students",
                    "Nobody yet. Wait until someone actually misuses the data"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "The policy has a section just for personal data.",
                "Birthdays, home addresses, student numbers and grades of 1,214 students: who " +
                        "outside CvSU must hear about that, and within how many hours?"
            ),
            successFeedback = "ITSO and the DPO. The sheet holds 1,214 students' birthdays, home " +
                    "addresses, student numbers and grades: sensitive personal information. The " +
                    "ticket came in Saturday at 9:40 PM, so the 72-hour clock has been running " +
                    "for over 58 hours. The DPO needs to know this morning.",
            failureFeedback = "Not that one. Quiet fixes hide the breach, public posts spread it, " +
                    "and waiting runs out the legal clock. Read the policy's personal-data " +
                    "section."
        ),

        LabTask(
            id = "t4",
            title = "What goes first?",
            objective = "Three confirmed incidents, one of you. **Check the latest on each** " +
                    "(the sent log, the Registrar's follow-up, the laptop's device record), then " +
                    "**put them in order**.",
            guide = listOf(
                "Incidents are handled by **priority**, not first-come, first-served. The first " +
                        "question is always: **is it still happening right now?** Harm that is " +
                        "**ongoing or spreading** comes first, because every minute adds victims.",
                "Next: **how many people** are affected and **how sensitive** is the data? A " +
                        "leak of a thousand students' personal records outranks one person's " +
                        "inconvenience.",
                "Finally, what **protections** already limit the damage? A stolen device with " +
                        "**full-disk encryption** and **remote lock** is a loss, but the data on " +
                        "it is far harder to read than a public link anyone can open."
            ),
            steps = listOf(
                "Open **TKT-0144** (Prof. Ramos) and tap **SENT LOG**. Is anything happening " +
                        "**right now**?",
                "Open **TKT-0141** (grades sheet) and tap **FOLLOW-UP**. Is the link **still " +
                        "open**?",
                "Open **TKT-0143** (stolen laptop) and tap **DEVICE RECORD**. What protects " +
                        "the data on it?",
                "Rank them: **still happening** first, then **people and data**, then " +
                        "**protections**.",
                "**Swipe the bar** down and pick the order."
            ),
            entryPage = "desk.html",
            requiredClues = listOf(
                SecurityIncidentsClues.OUTBOX_T4,
                SecurityIncidentsClues.UPDATE_T3,
                SecurityIncidentsClues.DEVICE_T6
            ),
            lockedMessage = "Check the sent log, the Registrar's follow-up and the device record " +
                    "first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "In the order they came in: grades sheet, laptop, mailbox",
                    "Most people first: grades sheet, mailbox, laptop",
                    "Still happening first: mailbox, grades sheet, laptop",
                    "Theft first: laptop, grades sheet, mailbox"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Only one of the three is still causing damage at 8 AM Monday.",
                "Look at the timestamps at the bottom of Prof. Ramos's sent log."
            ),
            successFeedback = "Mailbox first. The \"probably spam\" ticket is the only one still " +
                    "happening: her account has sent 52 phishing emails to students since 7:30 " +
                    "AM and is still going, from an address students trust. The grades link was " +
                    "closed Sunday, but 1,214 students were exposed and the DPA clock is " +
                    "running, so it's a close second. The laptop is encrypted and lockable, so " +
                    "it comes third.",
            failureFeedback = "Not that order. Check which incident is still causing harm right " +
                    "now. That one goes first, whatever order the tickets arrived in."
        ),

        LabTask(
            id = "t5",
            title = "File the report",
            objective = "Open the **incident report form** for TKT-0144, **read the chat** with " +
                    "Prof. Ramos, and fill in **every field** from the facts. A complete, " +
                    "correct report gives a **report code**. Submit it.",
            guide = listOf(
                "The **incident report** is how the case is handed over. The incident team " +
                        "acts on what it says, so it must be **factual, specific and complete**. " +
                        "Write what you **saw**, not what you **guess**.",
                "Every report answers the same questions: **what** happened, **when** it " +
                        "started and **when** it was discovered, **who** reported it, **what** " +
                        "is affected, **is it still happening**, and **what has already been " +
                        "done**, including \"nothing\".",
                "Two times matter, and they're rarely the same. **When it started** is the " +
                        "earliest moment the attacker had a foothold. **When it was " +
                        "discovered** is when someone noticed. The gap between them is how " +
                        "long the attacker went unseen."
            ),
            steps = listOf(
                "On the desk, tap **OPEN THE INCIDENT REPORT FORM**.",
                "Tap **CHAT WITH PROF. RAMOS** and read the whole conversation.",
                "Fill in each field from the chat and the evidence you've already seen on " +
                        "TKT-0144.",
                "Tap **SUBMIT REPORT**. The form tells you **how many** fields are right, not " +
                        "which.",
                "When every field is right, a **report code** appears. **Swipe the bar** down " +
                        "and type it **exactly as written**, braces included."
            ),
            entryPage = "report.html",
            requiredClues = listOf(
                SecurityIncidentsClues.CHAT_T4,
                SecurityIncidentsClues.REPORT_COMPLETE
            ),
            lockedMessage = "Read the chat and submit a complete, correct report first.",
            answer = LabAnswer.Flag(
                sha256 = "2a167341dd2af99230475aad6b6280a75dbe232a5a080f8789d4a92b70ee58f4"
            ),
            hints = listOf(
                "She entered her password on Friday afternoon. She only noticed something on " +
                        "Monday morning.",
                "What has been done so far? Remember what you chose in task 2."
            ),
            successFeedback = "Report filed. Account takeover by phishing: started Friday 4:47 " +
                    "PM, discovered Monday 7:55 AM, still sending, nothing changed, reported at " +
                    "8:15. That's two and a half days the attacker went unseen, and the " +
                    "incident team now knows exactly where to start. Next: Detection, and how " +
                    "to catch it in hours instead of days.",
            failureFeedback = "Not the report code. It appears on the form once every field is " +
                    "correct."
        )
    )
)
