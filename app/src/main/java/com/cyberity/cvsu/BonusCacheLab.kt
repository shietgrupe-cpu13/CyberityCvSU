package com.cyberity.cvsu

// ===========================================================================
// LEVEL 450 CONTENT — Bonus XP Cache, the reward between 404 and 405
// ===========================================================================
// A reward, not a test. Four single-digit clues, one from each of 401-404,
// open a combination lock; the chest deals out a field guide of every
// malware family in Unit 4. Wrong codes only shake the lock in the page,
// and the one "answer" is a single Claim option, so nothing here can cost
// a heart. Assets live in assets/simulations/UNIT 04/bonus_cache.

/** Clue ids reported by the cache page through the JS bridge. */
object BonusCacheClues {
    const val CACHE_VIEWED = "cache_viewed"
    const val CACHE_OPENED = "cache_opened"
    const val GUIDE_READ = "guide_read"
}

val bonusCacheClueLabels: Map<String, String> = mapOf(
    BonusCacheClues.CACHE_VIEWED to "Found the cache",
    BonusCacheClues.CACHE_OPENED to "Cracked the lock",
    BonusCacheClues.GUIDE_READ to "Read all eight field-guide cards"
)

fun bonusCacheLab(): LabDefinition = LabDefinition(
    levelId = 450,
    title = "Bonus XP Cache",
    subtitle = "Reward · Unit 4",
    briefing = "You made it through the malware unit. Before the final simulation, there's a " +
            "locked cache waiting for you.\n\n" +
            "Its combination is four digits, and each one is the answer to a clue from a level " +
            "you've already played. Get a digit right and it turns green. Get one wrong and the " +
            "lock just shakes. Nothing here costs a heart.\n\n" +
            "Inside: your bonus XP, and a field guide to every kind of malware in this unit.",
    assetDir = "UNIT 04/bonus_cache",
    startPage = "cache.html",
    tasks = listOf(
        LabTask(
            id = "t1",
            title = "Crack the cache",
            objective = "**Open the cache**, set the **four digits** from the clues, and tap " +
                    "**TRY THE LOCK**. When it opens, **claim your XP**.",
            guide = listOf(
                "Each clue comes from one level: **401**, **402**, **403** and **404**. Every " +
                        "answer is a **single digit**.",
                "Inside the chest is a **field guide**: one card for each kind of malware in " +
                        "this unit. **Tap a card** to flip it and see **what it does** and " +
                        "**how to stop it**. It's a quick recap before **Lab PC 14**."
            ),
            steps = listOf(
                "Open the **cache**.",
                "Read the **four clues** under the lock.",
                "Use **▲** and **▼** to set each digit, then tap **TRY THE LOCK**. Right digits " +
                        "turn **green**.",
                "When the chest opens, **flip the cards** if you like.",
                "**Swipe the bar** down and tap **Claim** to collect your XP."
            ),
            entryPage = "cache.html",
            requiredClues = listOf(BonusCacheClues.CACHE_OPENED),
            lockedMessage = "Crack the lock on the cache first.",
            answer = LabAnswer.Choice(
                options = listOf("Claim the cache's bonus XP"),
                correctIndex = 0
            ),
            hints = listOf(
                "The vault had one legitimate file. The wizard had three red flags.",
                "Two backups survived, and the defense stack faced four attacks."
            ),
            successFeedback = "Cache claimed. Keep the field guide in mind: in Lab PC 14 you'll " +
                    "meet a trojan, ransomware and a worm on the same machine.",
            failureFeedback = "Tap Claim to collect your XP."
        )
    )
)
