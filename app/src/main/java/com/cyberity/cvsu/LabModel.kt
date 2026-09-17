package com.cyberity.cvsu

import androidx.compose.runtime.Immutable
import java.security.MessageDigest
import java.util.Locale

// ===========================================================================
// REUSABLE LAB ENGINE — model + validation
// ===========================================================================
// A "lab" is a level made of sequential tasks played against an interactive
// HTML simulation bundled in app/src/main/assets. Nothing here is specific to
// Inbox Triage: level content lives in its own file (see InboxTriageLab.kt).

/** Root of every bundled simulation. Anything outside this prefix is blocked. */
const val LAB_ASSET_ROOT = "file:///android_asset/simulations/"

/**
 * How a task's submission is checked.
 *
 * [Text]   free typing, compared against accepted values after normalising.
 * [Flag]   CTF-style. Only the hash of the expected flag is stored, so the
 *          answer isn't sitting in the Kotlin source in plain text.
 * [Choice] a short decision list — used for judgement calls, not for recall.
 */
@Immutable
sealed interface LabAnswer {

    data class Text(
        val accepted: List<String>,
        val placeholder: String = "Type your answer"
    ) : LabAnswer

    data class Flag(
        val sha256: String,
        val placeholder: String = "CYBERITY{...}"
    ) : LabAnswer

    data class Choice(
        val options: List<String>,
        val correctIndex: Int
    ) : LabAnswer
}

/**
 * One objective in a lab.
 *
 * [requiredClues] are ids the simulation must have reported through the JS
 * bridge before the answer box unlocks — this is what stops the level being a
 * quiz: you cannot answer until you have actually investigated.
 */
@Immutable
data class LabTask(
    val id: String,
    val title: String,
    val objective: String,
    val answer: LabAnswer,
    val successFeedback: String,
    val failureFeedback: String,
    val requiredClues: List<String> = emptyList(),
    val lockedMessage: String = "Investigate the simulation before you can answer.",
    val hints: List<String> = emptyList(),
    /** Page loaded into the WebView when this task opens. Null keeps the current page. */
    val entryPage: String? = null
)

@Immutable
data class LabDefinition(
    val levelId: Int,
    val title: String,
    val subtitle: String,
    val briefing: String,
    /** Folder under assets/simulations/, e.g. "inbox_triage". */
    val assetDir: String,
    val startPage: String,
    val tasks: List<LabTask>,
    /** Clue ids that are dangerous actions: each costs a heart, once per attempt. */
    val dangerousClues: List<String> = emptyList()
) {
    val baseUrl: String get() = "$LAB_ASSET_ROOT$assetDir/"
}


/** Human-readable label for a clue id, shown in the evidence log. */
@Immutable
data class LabClue(val id: String, val label: String)

// ===========================================================================
// VALIDATION
// ===========================================================================

object LabValidator {

    /** Lowercase, trim, collapse whitespace. Case-insensitive answers by design. */
    fun normalize(input: String): String =
        input.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

    /**
     * Flags are normalised further: whitespace removed entirely, and the
     * CYBERITY{} wrapper added if the student typed only the inner token.
     */
    fun normalizeFlag(input: String): String {
        val bare = input.trim().lowercase(Locale.ROOT).replace(" ", "")
        return if (bare.startsWith("cyberity{")) bare else "cyberity{$bare}"
    }

    fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }

    fun isCorrect(answer: LabAnswer, submitted: String, choiceIndex: Int?): Boolean =
        when (answer) {
            is LabAnswer.Text ->
                answer.accepted.any { normalize(it) == normalize(submitted) }

            is LabAnswer.Flag ->
                sha256(normalizeFlag(submitted)).equals(answer.sha256, ignoreCase = true)

            is LabAnswer.Choice ->
                choiceIndex != null && choiceIndex == answer.correctIndex
        }
}
