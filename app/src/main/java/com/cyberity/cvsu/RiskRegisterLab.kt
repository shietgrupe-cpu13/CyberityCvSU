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
            "risk, rank them, and build a plan that removes the most risk for the money.",
    assetDir = "risk_register",
    startPage = "register.html",
    dangerousClues = listOf(RiskClues.RISK_HIDDEN),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Break down a risk",
            objective = "Open finding F-01 and read its evidence. Every risk has an asset, a " +
                    "threat, and a vulnerability — identify all three.",
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
