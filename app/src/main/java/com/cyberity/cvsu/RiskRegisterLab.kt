package com.cyberity.cvsu

// ===========================================================================
// LEVEL 106 CONTENT — Risk Assessment, as a registrar risk register
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, exactly as levels 101-104.
// Story continues from 104: the fixes are known, the budget isn't big enough
// for all of them, so the risks have to be scored and prioritised.

/** Clue ids reported by the risk register through the JS bridge. */
object RiskClues {
    const val FINDING_OPENED_F01 = "finding_opened_f01"
    const val FINDING_OPENED_F02 = "finding_opened_f02"
    const val FINDING_OPENED_F03 = "finding_opened_f03"
    const val FINDING_OPENED_F04 = "finding_opened_f04"
    const val RUBRIC_OPENED = "rubric_opened"
    const val RISKS_SCORED = "risks_scored"
    const val TREATMENT_GUIDE_OPENED = "treatment_guide_opened"
    const val PLAN_SUBMITTED = "plan_submitted"

    /** Dangerous: deleting a risk from the register so the report looks better. */
    const val RISK_HIDDEN = "risk_hidden"
}

val riskClueLabels: Map<String, String> = mapOf(
    RiskClues.FINDING_OPENED_F01 to "Opened finding F-01",
    RiskClues.FINDING_OPENED_F02 to "Opened finding F-02",
    RiskClues.FINDING_OPENED_F03 to "Opened finding F-03",
    RiskClues.FINDING_OPENED_F04 to "Opened finding F-04",
    RiskClues.RUBRIC_OPENED to "Read the scoring rubric",
    RiskClues.RISKS_SCORED to "Scored every finding on the risk matrix",
    RiskClues.TREATMENT_GUIDE_OPENED to "Read the risk treatment guide",
    RiskClues.PLAN_SUBMITTED to "Submitted a treatment plan within budget"
)

fun riskRegisterLab(): LabDefinition = LabDefinition(
    levelId = 106,
    title = "Risk Register",
    subtitle = "Risk Assessment · Budget Season",
    briefing = "The hardening review produced four findings, and the Registrar has ₱150,000 " +
            "to fix them before next enrollment — not enough for everything. Score each " +
            "risk, rank them, and build a plan that removes the most risk for the money.\n\n" +
            "Each task explains what to look for, then hands you the register. Open it with the " +
            "button, and swipe the bar at the top of it down when you are ready to answer.",
    assetDir = "risk_register",
    startPage = "register.html",
    dangerousClues = listOf(RiskClues.RISK_HIDDEN),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Break down a risk",
            objective = "Open finding F-01 and read its evidence. Every risk has an asset, a " +
                    "threat, and a vulnerability — identify all three.",
            guide = listOf(
                "\"Risk\" on its own is a feeling, and feelings can't be ranked or budgeted for. " +
                        "The assessment starts by breaking each one into three named parts. The " +
                        "asset is what you are protecting. The threat is who or what could harm " +
                        "it. The vulnerability is the specific weakness the threat would use to " +
                        "get there.",
                "Separating them matters because only one of the three is yours to change. You " +
                        "cannot remove attackers from the world, and the Registrar cannot stop " +
                        "holding student records — that is the entire purpose of the office. The " +
                        "vulnerability is the part you control, which is why every control you " +
                        "will ever buy is aimed at it rather than at the threat.",
                "The three are easy to mix up when they are written as one sentence, so pull " +
                        "them apart deliberately. A test that works: the asset is what you would " +
                        "be apologising for losing, the threat is who you would be blaming, and " +
                        "the vulnerability is the only one you could write a purchase order " +
                        "against."
            ),
            steps = listOf(
                "Open the simulation. The register lists the four findings the hardening review " +
                        "produced, each with its owner.",
                "Tap F-01 to open it.",
                "Read the summary line, then the evidence block underneath. The highlighted " +
                        "lines are the ones that decide this finding's score later.",
                "Pick the three parts out of what you have read: what is being protected, who " +
                        "or what could harm it, and the weakness that would let them.",
                "Do not tap REMOVE FROM REGISTER. Deleting a risk doesn't reduce it, the " +
                        "simulation blocks it anyway, and it costs you a heart.",
                "Come back and choose the breakdown that puts all three in the right place."
            ),
            entryPage = "register.html",
            requiredClues = listOf(RiskClues.FINDING_OPENED_F01),
            lockedMessage = "Open finding F-01 in the register first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Asset: password-only sign-in · Threat: grade records · Vulnerability: attacker with a stolen password",
                    "Asset: attacker with a stolen password · Threat: password-only sign-in · Vulnerability: grade records",
                    "Asset: grade records · Threat: attacker with a stolen password · Vulnerability: password-only sign-in",
                    "Asset: grade records · Threat: password-only sign-in · Vulnerability: attacker with a stolen password"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "The asset is what you're protecting. The threat is who or what could harm it.",
                "The vulnerability is the weakness the threat uses — the thing you can fix."
            ),
            successFeedback = "Asset, threat, vulnerability. You can't remove attackers, and you " +
                    "can't stop owning grades — the vulnerability is the only part you control, " +
                    "which is why fixes always target it.",
            failureFeedback = "Something's swapped. Ask: what are we protecting, who wants to " +
                    "harm it, and what weakness lets them?"
        ),

        LabTask(
            id = "t2",
            title = "Score and rank",
            objective = "Place every finding on the risk matrix using the rubric and the " +
                    "evidence in each finding. Then submit the ID of the highest-risk finding.",
            guide = listOf(
                "Scoring exists because every finding feels urgent to the person who reported " +
                        "it. Risk = likelihood × impact turns that pile into an order. Both " +
                        "halves are needed: a catastrophic event that cannot realistically happen " +
                        "and a trivial one that happens weekly are both mis-handled if you look " +
                        "at only one number.",
                "Likelihood is a reading of evidence, not a gut estimate, and the rubric here " +
                        "anchors it in history — has this already happened at CvSU, how recently, " +
                        "and how often. Where there is no history the question changes rather " +
                        "than disappearing: does a public exploit exist, and is the weakness " +
                        "actually reachable from where an attacker stands?",
                "Impact is about reach and the kind of data behind it. Grades of every enrolled " +
                        "student are not in the same class as lab machines that hold no records " +
                        "and are wiped nightly. Score what the evidence states rather than what " +
                        "sounds alarming — the output of this step is an order you have to defend " +
                        "to whoever controls the budget, so it has to come from the written " +
                        "record."
            ),
            steps = listOf(
                "Open the simulation and open Risk matrix from the tools on the register.",
                "Expand the Scoring rubric first and read both scales. Every number from 1 to 5 " +
                        "has a written meaning on each axis.",
                "Use the chips at the top to choose which finding you are placing. That " +
                        "finding's evidence appears just below them, with the deciding lines " +
                        "highlighted.",
                "Read the history line for likelihood and the reach or data line for impact, " +
                        "then tap the cell where the two meet. Likelihood runs up the left side, " +
                        "impact across the bottom.",
                "Read the banner under the grid — it either confirms the placement matches the " +
                        "rubric, or tells you to read both again.",
                "Work through all four, adjusting until every chip is marked done.",
                "Look at the ranking below the matrix and come back with the ID sitting at the top."
            ),
            entryPage = "matrix.html",
            requiredClues = listOf(RiskClues.RUBRIC_OPENED, RiskClues.RISKS_SCORED),
            lockedMessage = "Read the rubric and place all four findings where the evidence puts them.",
            answer = LabAnswer.Text(
                accepted = listOf("F-01", "F01", "F 01"),
                placeholder = "e.g. F-00"
            ),
            hints = listOf(
                "Likelihood comes from history: has it happened here, and how often?",
                "Risk = likelihood × impact. Read the ranking under the matrix."
            ),
            successFeedback = "F-01 scores 20: it has already happened once, and it can touch " +
                    "every student's grades. Scoring turns 'everything feels urgent' into an order.",
            failureFeedback = "That isn't the top score. Check the ranking under the matrix — " +
                    "the highest likelihood × impact wins."
        ),

        LabTask(
            id = "t3",
            title = "Spend the budget",
            objective = "Build a treatment plan within ₱150,000 that leaves the lowest total " +
                    "residual risk. When the CISO approves the best possible plan, the approval " +
                    "memo carries a flag — submit it.",
            guide = listOf(
                "This is the step where risk assessment turns into a budget meeting. There is " +
                        "never enough money for every control, and the goal is not to buy as much " +
                        "security as the budget allows. It is to remove as much risk as possible " +
                        "with it — those are different targets, and they usually point at " +
                        "different shopping lists.",
                "The number that decides it is residual risk: what each finding still scores " +
                        "after your controls are applied. Add it up across all four and you have " +
                        "one figure to minimise. Without that total you are comparing adjectives " +
                        "— \"comprehensive\", \"enterprise-grade\" — instead of comparing outcomes.",
                "Two traps are laid here on purpose. One control is priced far beyond the whole " +
                        "budget, because the obvious fix is often the unaffordable one and there " +
                        "is usually a cheaper control that addresses the same finding a different " +
                        "way. Another sounds the most impressive on the page, covers two findings " +
                        "at once, and still leaves more risk standing than several cheap targeted " +
                        "ones — because covering a risk partly is not the same as covering it."
            ),
            steps = listOf(
                "Open the simulation and open Treatment plan from the register.",
                "Read all six controls. Each shows its cost on top, and underneath what it would " +
                        "do to a finding's score — for example F-04 12 → 3.",
                "Find the two controls aimed at the same finding at very different prices, and " +
                        "the one that touches two findings at once.",
                "Tap controls to switch them on and off. Watch the spend bar at the top and the " +
                        "Total residual risk line at the bottom of the before → after list.",
                "Tap SUBMIT PLAN TO CISO. Over budget is rejected outright; within budget but " +
                        "not the best available gets sent back with the total it left behind.",
                "Keep recombining until the CISO approves it, then read the memo that appears.",
                "Come back and submit the flag from the memo in full, CYBERITY{...} wrapper " +
                        "included."
            ),
            entryPage = "plan.html",
            requiredClues = listOf(RiskClues.PLAN_SUBMITTED),
            lockedMessage = "Submit a plan that stays within budget.",
            answer = LabAnswer.Flag(
                sha256 = "c0f52757df5ad74c067e1361152af6869cd2508dad1eb31f8ae7744be2c57e5b"
            ),
            hints = listOf(
                "One expensive control covers two findings, but only partly. Compare it with " +
                        "several cheaper, targeted ones.",
                "Replacing every lab PC is far over budget. There's a cheaper control for F-02."
            ),
            successFeedback = "Four targeted controls beat one expensive broad one. Good risk " +
                    "management isn't buying the most security — it's buying the most risk " +
                    "reduction per peso.",
            failureFeedback = "Not the flag. The memo only shows it for the plan with the " +
                    "lowest possible residual risk inside the budget."
        ),

        LabTask(
            id = "t4",
            title = "Name the treatment",
            objective = "Read the risk treatment guide on the plan page, then classify these " +
                    "four decisions: turn on MFA, buy cyber insurance, stop posting records " +
                    "online entirely, and leave the lab PCs as-is until next year.",
            guide = listOf(
                "Whatever you decide about a risk, the decision lands in one of four boxes: " +
                        "mitigate, transfer, avoid, accept. Naming the box is not paperwork. It " +
                        "forces you to state what you are actually doing, and it makes the " +
                        "decision reviewable by someone who wasn't in the room.",
                "They are easy to tell apart once you ask the right question of each. Mitigate " +
                        "lowers likelihood or impact — every control on the plan page is a " +
                        "mitigation. Transfer shifts who carries the cost, usually by contract; " +
                        "insurance pays for the breach, it does not prevent one. Avoid stops the " +
                        "risky activity altogether, so the risk stops existing rather than " +
                        "shrinking — the most complete option, and usually the most expensive in " +
                        "what the organisation gives up.",
                "Accept is the one that gets misread. It is a legitimate, common choice: the " +
                        "risk owner signs off and it comes back next cycle. What makes it " +
                        "acceptance rather than negligence is that somebody decided it and wrote " +
                        "it down. A risk nobody looked at is not accepted — it is just unmanaged, " +
                        "and the register exists to tell those two apart."
            ),
            steps = listOf(
                "You should still be on the Treatment plan page. If not, open it again from the " +
                        "register.",
                "Scroll to the bottom and expand the Risk treatment guide.",
                "Read all four definitions, and the note underneath about what every control on " +
                        "that page counts as.",
                "Take the four decisions in the question one at a time and ask which it is: does " +
                        "it lower the risk, shift who pays, stop the activity, or knowingly live " +
                        "with it?",
                "Come back and match all four at once."
            ),
            requiredClues = listOf(RiskClues.TREATMENT_GUIDE_OPENED),
            lockedMessage = "Open the risk treatment guide on the plan page.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "MFA: Avoid · Insurance: Mitigate · Stop posting: Transfer · Lab PCs: Accept",
                    "MFA: Mitigate · Insurance: Accept · Stop posting: Avoid · Lab PCs: Transfer",
                    "MFA: Transfer · Insurance: Transfer · Stop posting: Mitigate · Lab PCs: Avoid",
                    "MFA: Mitigate · Insurance: Transfer · Stop posting: Avoid · Lab PCs: Accept"
                ),
                correctIndex = 3
            ),
            hints = listOf(
                "Insurance doesn't stop the breach — it moves who pays for it.",
                "Stopping the activity completely removes the risk rather than reducing it."
            ),
            successFeedback = "Mitigate, transfer, avoid, accept — every risk ends up in one of " +
                    "these four. Accepting is a legitimate choice, as long as it's a decision " +
                    "and not an oversight.",
            failureFeedback = "At least one is mismatched. Ask of each: does it reduce the " +
                    "risk, shift its cost, remove the activity, or knowingly live with it?"
        ),

        LabTask(
            id = "t5",
            title = "Own the residual",
            objective = "Even the best plan leaves residual risk — F-03 still scores 6. " +
                    "Decide what the Registrar should do with it.",
            guide = listOf(
                "Residual risk is what remains after the controls are in place, and it is never " +
                        "zero. Spending until it is isn't an option that exists: the cost of " +
                        "removing the last of something climbs steeply, and there is always a " +
                        "final sliver no budget can buy away.",
                "So an honest assessment does not end with \"fixed\". It ends with a number, a " +
                        "named owner who has signed off on living with it, and a date to look " +
                        "again. The date matters as much as the signature, because both halves " +
                        "of the score drift — a weakness nobody was exploiting last year becomes " +
                        "routine, and a system that held little data grows.",
                "A treated risk also stays on the register. Striking it off because it has been " +
                        "dealt with throws away the history that made it scorable in the first " +
                        "place, and leaves the next person assessing it with nothing. That is the " +
                        "same instinct as deleting a finding to tidy up the report, only slower " +
                        "and easier to justify."
            ),
            steps = listOf(
                "Open the simulation and look at the Risk: before → after list on the plan page.",
                "Find F-03. It is the largest number still standing after the best plan you can " +
                        "afford.",
                "Ask what honestly happens to it now: it cannot be spent down to zero, and it " +
                        "has not gone away by being treated.",
                "Come back and choose what the Registrar should do with what is left."
            ),
            answer = LabAnswer.Choice(
                options = listOf(
                    "Keep spending until every risk reaches zero",
                    "Remove F-03 from the register, since it has been treated",
                    "Have the risk owner formally accept it and review it next cycle",
                    "Stop tracking risks until another incident happens"
                ),
                correctIndex = 2
            ),
            hints = listOf("Zero risk isn't a budget line. What's the honest way to live with what's left?"),
            successFeedback = "Residual risk is accepted on purpose, by a named owner, and " +
                    "looked at again next cycle. That's what separates a managed risk from an " +
                    "ignored one.",
            failureFeedback = "Risk never reaches zero, and a treated risk doesn't disappear. " +
                    "Someone has to own what's left."
        )
    )
)
