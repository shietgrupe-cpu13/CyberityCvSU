package com.cyberity.cvsu

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

/** The spendable balance: the starting grant plus what levels paid out, less hints bought. */
fun xpBalance(earned: Int, spent: Int): Int = (STARTING_XP + earned - spent).coerceAtLeast(0)

/** Price of the 1st, 2nd and 3rd hint on a task. Any further hint costs the same as the 3rd. */
private val HINT_COSTS = listOf(15, 30, 50)

/** What the next hint on a task costs, given how many are already [opened] on it. */
fun nextHintCost(opened: Int): Int = HINT_COSTS.getOrElse(opened) { HINT_COSTS.last() }

/**
 * What a level pays out. Hints are not deducted here — they were already paid
 * for out of the balance when they were opened — but using one still forfeits
 * the no-hints bonus, so a hint costs its price plus that bonus.
 */
fun scoreLevelXp(
    completed: Boolean,
    heartsLost: Int,
    hintsUsed: Int,
    allCluesFound: Boolean
): Int {
    if (!completed) return 0
    var xp = LEVEL_XP
    if (heartsLost == 0) xp += BONUS_NO_HEART_LOST
    if (allCluesFound) xp += BONUS_ALL_CLUES
    if (hintsUsed == 0) xp += BONUS_NO_HINTS
    return xp
}
