package com.cyberity.cvsu

// ===========================================================================
// LEVEL 104 CONTENT — Security Principles, as a post-incident hardening review
// ===========================================================================
// Content only. Engine is LabModel.kt / LabScreen.kt, exactly as levels 101-103.
// Story continues from 103: the tampered grade is traced to design weaknesses.

/** Clue ids reported by the hardening review through the JS bridge. */
object PrincipleClues {
    const val USER_OPENED_JDCRUZ = "user_opened_jdcruz"
    const val JOB_DESC_OPENED = "job_desc_opened"
    const val LEAST_PRIVILEGE_APPLIED = "least_privilege_applied"
    const val LOCK_LOG_OPENED = "lock_log_opened"
    const val FAILSAFE_SET = "failsafe_set"
    const val ATTACK_REPLAYED = "attack_replayed"

    /** Dangerous: switching off the one layer that actually caught the attack. */
    const val INTEGRITY_LAYER_DISABLED = "integrity_layer_disabled"
}

val principleClueLabels: Map<String, String> = mapOf(
    PrincipleClues.USER_OPENED_JDCRUZ to "Opened the student assistant's account",
    PrincipleClues.JOB_DESC_OPENED to "Read the student assistant's job description",
    PrincipleClues.LEAST_PRIVILEGE_APPLIED to "Trimmed the account to least privilege",
    PrincipleClues.LOCK_LOG_OPENED to "Read the grade-lock log",
    PrincipleClues.FAILSAFE_SET to "Set the grade lock to fail safe",
    PrincipleClues.ATTACK_REPLAYED to "Replayed the attack against the defences"
)

fun hardeningReviewLab(): LabDefinition = LabDefinition(
    levelId = 104,
    title = "Hardening Review",
    subtitle = "Security Principles · Post-incident",
    briefing = "The grade was restored, but the Registrar wants to know why the attack worked " +
            "at all — and to make sure it can't work again. Review who had access, how the " +
            "grade lock behaves when it fails, and how many defences stood in the way.",
    assetDir = "hardening_review",
    startPage = "review.html",
    dangerousClues = listOf(PrincipleClues.INTEGRITY_LAYER_DISABLED),
    tasks = listOf(

        LabTask(
            id = "t1",
            title = "Find the excess access",
            objective = "Open the portal accounts and compare each person's role with their " +
                    "actual job. Which account has far more access than its job needs?",
            entryPage = "review.html",
            requiredClues = listOf(PrincipleClues.USER_OPENED_JDCRUZ),
            lockedMessage = "Open the accounts and compare each role to the person's job.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "registrar.staff — Records Officer",
                    "a.reyes — Faculty",
                    "j.dcruz — Student Assistant",
                    "guidance.office — Guidance Counselor"
                ),
                correctIndex = 2
            ),
            hints = listOf(
                "Read the job description on each account, then look at the permission list.",
                "Whose job is typing in forms, but whose role can edit locked grades?"
            ),
            successFeedback = "A student assistant who encodes forms was holding a full admin " +
                    "role — granted 'temporarily' last enrollment and never removed. When that " +
                    "password was stolen, the attacker got every one of those permissions.",
            failureFeedback = "That account's permissions fit its job. Look for the one whose " +
                    "role is far wider than what the job description describes."
        ),

        LabTask(
            id = "t2",
            title = "Apply least privilege",
            objective = "Edit the over-privileged account so it keeps only what the job needs " +
                    "and save it. Then submit how many permissions you removed.",
            entryPage = "roles.html",
            requiredClues = listOf(
                PrincipleClues.JOB_DESC_OPENED,
                PrincipleClues.LEAST_PRIVILEGE_APPLIED
            ),
            lockedMessage = "Read the job description, then save the account with only the " +
                    "permissions the job needs.",
            answer = LabAnswer.Text(
                accepted = listOf("5", "five"),
                placeholder = "e.g. 3"
            ),
            hints = listOf(
                "The job description lists exactly two duties.",
                "Keep the two enrollment permissions. Everything else goes."
            ),
            successFeedback = "Five permissions gone. Least privilege doesn't stop passwords " +
                    "being stolen — it shrinks what a stolen password can do. With this role, " +
                    "the attacker could only have typed enrollment forms.",
            failureFeedback = "Not the right count. Compare the permissions the account started " +
                    "with to the ones you kept."
        ),

        LabTask(
            id = "t3",
            title = "Fail safe, not open",
            objective = "The grade was locked, yet it was edited. Read the grade-lock log, set " +
                    "the lock policy to a fail-safe default, then explain why the lock didn't hold.",
            entryPage = "failsafe.html",
            requiredClues = listOf(PrincipleClues.LOCK_LOG_OPENED, PrincipleClues.FAILSAFE_SET),
            lockedMessage = "Read the lock log and save a fail-safe lock policy first.",
            answer = LabAnswer.Choice(
                options = listOf(
                    "The attacker guessed the grade sheet's lock password",
                    "The lock check timed out, and the policy defaulted to allowing the edit",
                    "The professor unlocked the grade sheet before the attack",
                    "The integrity checker was switched off that night"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Look at what happened one second before the UPDATE.",
                "When the lock service can't answer, what does on_error tell the portal to do?"
            ),
            successFeedback = "The lock service sat on the database server whose disk was " +
                    "filling up. It stopped answering, and the portal treated 'no answer' as " +
                    "'yes'. A fail-safe default denies when a check can't complete.",
            failureFeedback = "The log shows what actually happened. Read the two lines right " +
                    "before the UPDATE."
        ),

        LabTask(
            id = "t4",
            title = "Stack the layers",
            objective = "Switch on the defences that were missing, then replay the 02:14 " +
                    "attack. When every layer holds, the replay report shows a flag — submit it.",
            entryPage = "layers.html",
            requiredClues = listOf(PrincipleClues.ATTACK_REPLAYED),
            lockedMessage = "Replay the attack at least once.",
            answer = LabAnswer.Flag(
                sha256 = "6fa00f34533e0f21140213d67d0d2393d432b7e161cb689345c1f99ef9551149"
            ),
            hints = listOf(
                "Replay with the current settings first and see which layers let it through.",
                "Every layer should be on. The one that was already on should stay on."
            ),
            successFeedback = "Four independent layers, each able to stop or catch the attack " +
                    "on its own. That's defence in depth: no single control has to be perfect.",
            failureFeedback = "Not the flag. It only appears in the replay report when every " +
                    "layer is switched on."
        ),

        LabTask(
            id = "t5",
            title = "Write the fix list",
            objective = "Match each fix you made to the security principle behind it.",
            entryPage = "review.html",
            answer = LabAnswer.Choice(
                options = listOf(
                    "Trim role: defence in depth · Add layers: fail-safe · Lock policy: least privilege",
                    "Trim role: least privilege · Add layers: defence in depth · Lock policy: fail-safe defaults",
                    "Trim role: fail-safe defaults · Add layers: least privilege · Lock policy: defence in depth",
                    "Trim role: least privilege · Add layers: fail-safe defaults · Lock policy: defence in depth"
                ),
                correctIndex = 1
            ),
            hints = listOf(
                "Which fix reduced what one account can do? Which fix added more barriers? " +
                        "Which fix changed what happens when something breaks?"
            ),
            successFeedback = "Least privilege limits the damage, defence in depth means one " +
                    "failure isn't fatal, and fail-safe defaults make failures deny instead of " +
                    "allow. Together they turn a single stolen password into a dead end.",
            failureFeedback = "At least one fix is matched to the wrong principle. Think about " +
                    "what each fix changed: how much access, how many barriers, or what happens on failure."
        )
    )
)
