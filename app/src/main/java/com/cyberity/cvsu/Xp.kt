package com.cyberity.cvsu

import androidx.compose.runtime.Immutable

// ===========================================================================
// XP
// ===========================================================================
// XP is a balance, not a running score. Finishing a level credits it; opening
// a hint debits it there and then, so a hint is bought with XP the student
// already earned rather than discounted off a level they haven't finished.
// That also means a hint stays paid for after leaving a level — there is no
// refund for backing out.

/**
 * What every student starts with. Hints are bought out of the balance, so
 * without this the very first level — the one a beginner is most likely to
 * need help on — would have every hint locked.
 */
const val STARTING_XP = 50

const val LEVEL_XP = 100
const val BONUS_NO_HEART_LOST = 25
const val BONUS_ALL_CLUES = 10
const val BONUS_NO_HINTS = 15
const val MAX_LEVEL_XP = LEVEL_XP + BONUS_NO_HEART_LOST + BONUS_ALL_CLUES + BONUS_NO_HINTS

/**
 * Taken off the payout for each failed run of a level that can be re-run
 * inside one attempt (the Level 350 quiz). It only eats into the bonuses:
 * finishing always pays at least [LEVEL_XP].
 */
const val RETRY_PENALTY = 10

/** The spendable balance: the starting grant plus what levels paid out, less hints bought. */
fun xpBalance(earned: Int, spent: Int): Int = (STARTING_XP + earned - spent).coerceAtLeast(0)

/** Price of the 1st, 2nd and 3rd hint on a task. Any further hint costs the same as the 3rd. */
private val HINT_COSTS = listOf(15, 30, 50)

/** What the next hint on a task costs, given how many are already [opened] on it. */
fun nextHintCost(opened: Int): Int = HINT_COSTS.getOrElse(opened) { HINT_COSTS.last() }

/**
 * A level's payout, kept in parts so the result screen can show the student
 * how the figure was arrived at rather than just the sum. A bonus that wasn't
 * earned is present as zero, which is what lets the breakdown list it as
 * missed instead of hiding it.
 */
@Immutable
data class XpAward(
    val base: Int,
    val noHeartBonus: Int,
    val cluesBonus: Int,
    val noHintsBonus: Int,
    /** XP taken off for failed runs, already capped so the total never drops below the base. */
    val retryPenalty: Int = 0
) {
    val total: Int get() = base + noHeartBonus + cluesBonus + noHintsBonus - retryPenalty
}

/**
 * What a level pays out, itemised. Hints are not deducted here — they were
 * already paid for out of the balance when they were opened — but using one
 * still forfeits the no-hints bonus, so a hint costs its price plus that bonus.
 */
fun awardFor(
    completed: Boolean,
    heartsLost: Int,
    hintsUsed: Int,
    allCluesFound: Boolean,
    failedRuns: Int = 0
): XpAward {
    if (!completed) return XpAward(0, 0, 0, 0)
    val noHeart = if (heartsLost == 0) BONUS_NO_HEART_LOST else 0
    val clues = if (allCluesFound) BONUS_ALL_CLUES else 0
    val noHints = if (hintsUsed == 0) BONUS_NO_HINTS else 0
    val bonuses = noHeart + clues + noHints
    return XpAward(
        base = LEVEL_XP,
        noHeartBonus = noHeart,
        cluesBonus = clues,
        noHintsBonus = noHints,
        retryPenalty = (failedRuns.coerceAtLeast(0) * RETRY_PENALTY).coerceAtMost(bonuses)
    )
}

/** The payout as a single figure. */
fun scoreLevelXp(
    completed: Boolean,
    heartsLost: Int,
    hintsUsed: Int,
    allCluesFound: Boolean,
    failedRuns: Int = 0
): Int = awardFor(completed, heartsLost, hintsUsed, allCluesFound, failedRuns).total
