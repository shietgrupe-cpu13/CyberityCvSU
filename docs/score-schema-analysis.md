# Score schema analysis

Existing Standard Firestore database `(default)` in asia-southeast1; Spark free tier.
Progress fields: completedLevels (integer list), levelXp (string level ID -> integer),
xpSpent (integer), hearts (integer 0–5), heartsUpdatedAt (server timestamp).
The real grading maximum is MAX_LEVEL_XP=150; curriculum node xpReward values
are older descriptive metadata and are not the amounts paid by grading.
Level 200 is a walkthrough with no reward. STARTING_XP=50 funds early hints.
Hint prices are 15, 30 and 50. Progress is private to its owner.

Leaderboard is an authenticated top-XP query (descending xp, limit) with client
tester filtering. Public records contain displayName, xp, isTester, updatedAt.
Privacy hide deletes the entry; show recreates it inside a preferences transaction.
Profile writes claim studentIds in a transaction; onboarding uses merge updates.
Deletion queries owned studentIds, enumerates sessions, and deletes progress/profile.

Rules should retain all existing cutoff/deletion protections. New score validation
must cover both create/update, prevent unknown IDs/oversized awards/re-awards,
preserve completion and spending, and require the public entry to match after-state
profile and bounded progress. Cloud Functions and billing changes are excluded.
Neither rules nor App Check establish that the client really completed a lab.
