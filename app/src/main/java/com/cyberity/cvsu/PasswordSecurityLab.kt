package com.cyberity.cvsu

// Unit 2 content uses the shared lab engine. All accounts and credentials are fictional.
// Teaching reference: https://pages.nist.gov/800-63-4/sp800-63b.html
internal data class PasswordExercise(
    val title: String,
    val evidence: String,
    val guide: String,
    val feedback: String
)

internal data class PasswordModule(
    val title: String,
    val briefing: String,
    val exercises: List<PasswordExercise>
)

private val passwordModules = mapOf(
    201 to strongPasswordsModule,
    202 to passwordAttacksModule,
    203 to multiFactorAuthenticationModule,
    204 to accountProtectionModule,
    205 to passwordSecurityChallengeModule,
    250 to hashCrackingPracticeModule
)

fun passwordSecurityClueLabels(levelId: Int): Map<String, String> =
    passwordModules.getValue(levelId).exercises.mapIndexed { index, exercise ->
        "password_${levelId}_${index}" to exercise.title
    }.toMap()

fun passwordSecurityLab(levelId: Int): LabDefinition {
    val module = passwordModules.getValue(levelId)
    val caseContext = when (levelId) {
        201 -> "Help **Maya Santos** audit her student portal, linked shopping account, and password policy."
        202 -> "Work with **Jules Reyes** at campus IT: investigate guessing alerts, leaked credential pairs, and a copied database."
        203 -> "Help **Maya Santos** enrol sign-in protection, investigate unexpected approval prompts, and upgrade her sign-in."
        204 -> "Help **Maya Santos** protect recovery, close abandoned device access, and contain a subsequent breach."
        205 -> "Help **Elena Cruz** investigate and secure the registrar training mailbox, then remove persistent attacker access."
        else -> "Join **Elena Cruz** for an authorised audit of three fictional legacy records: a common-word password, a campus-pattern password, and a two-digit recovery PIN."
    }
    return LabDefinition(
        levelId = levelId,
        title = module.title,
        subtitle = "Unit 2 · Password & Account Security",
        briefing = module.briefing + "\n\n" + caseContext.replace("**", "") +
            "\n\nAsk about the person's situation, inspect the evidence, and investigate or repair the account. All three tasks continue the same case; verified findings carry forward." +
            "\n\nAfter your work is verified, submit the single CASE code in the task panel. Read the outcome before moving on. Hints are optional." +
            if (levelId == 250) "\n\nStart each two-minute round when ready. Retries generate a fresh target and cost no hearts." else "",
        assetDir = "UNIT 02/" + when (levelId) {
            201 -> "strong_passwords"
            202 -> "password_attacks"
            203 -> "multi_factor_authentication"
            204 -> "account_protection"
            205 -> "password_security_challenge"
            250 -> "hash_cracking_practice"
            else -> error("Unknown Unit 2 level: $levelId")
        },
        startPage = "case_${levelId}_0.html",
        tasks = module.exercises.mapIndexed { index, exercise ->
            val code = "CASE-${levelId}-${index + 1}"
            LabTask(
                id = "password_${levelId}_${index}",
                title = exercise.title,
                objective = caseContext + "\n\n" + exercise.evidence + "\n\n" +
                    if (index == 0) "Start the case in the simulation. Investigate or repair the account to earn this task's **CASE code**."
                    else "Continue the same case from Task $index. Its verified results carry forward. Complete the requested work to earn this task's **CASE code**.",
                guide = if (levelId == 205) listOf("Investigate the case evidence and meet this task's objective. Optional hints and lesson references are available if you need them.")
                    else listOf(exercise.guide) + unitTwoReading(levelId, index),
                steps = unitTwoTaskSteps(levelId, index),
                entryPage = "case_${levelId}_${index}.html",
                requiredClues = listOf("password_${levelId}_${index}"),
                lockedMessage = "Resolve this case in the simulation to unlock submission.",
                answer = LabAnswer.Text(listOf(code), "CASE-..."),
                hints = listOf(exercise.guide, "Read the success panel in the simulation; it contains this case's code."),
                successFeedback = exercise.feedback + "\n\n" + unitTwoDebrief(levelId, index),
                failureFeedback = "Copy the case code displayed after resolving this specific case."
            )
        }
    )
}
