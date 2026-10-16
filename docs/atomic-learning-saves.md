# Completion and hint purchase saving

Implemented locally on 16 October 2026. Uses the existing Android Firestore SDK
and userProgress collection; no new service, database, dependency or billing plan.
The existing live database was confirmed as Standard edition on the free tier
during the phone-access check. Local emulator verification also uses Standard edition.

## Completion

LearnScreen now sends completion and the first-clear EXP award through one
ProgressRepository.completeLevel transaction. The transaction reads the stored
awards, preserves any existing first award, and saves completion plus the award
together. Cached progress changes only after acknowledgement. A completed legacy
level without an award can receive its missing award when completed again.

Before sending, a small per-account pending-award journal is committed to local
storage. The result dialog offers Retry save or Save later after a failure. Learn
retries queued results on opening, including after process death. This journal is
not a server backup: clearing application data/uninstalling before confirmation
can lose an unsaved result. An acknowledgement lost after a successful commit is
safe to retry; the server preserves the original award. Replaying a level while
its result is pending retains the queued award rather than replacing it.

Successful responses merge monotonic completion/EXP/spending state so a late
response cannot refund spending or erase a newer completion. Initial load errors
no longer replace existing progress with empty/zero values.

## Hint purchase

Each lab run has an attempt UUID. A purchase is identified by a SHA-256 digest of
attempt ID, level ID, task ID and hint number. On confirmation, the app opens the
hint and updates the balance; repeated taps are disabled while waiting. Retrying
the same request reads its existing receipt instead of debiting again. Concurrent
requests may require retry after contention, but cannot apply the same debit twice.
The welcome tutorial continues to use practice EXP and makes no database purchase.

New document: userProgress/{uid}/hintPurchases/{purchaseId}.
Required fields: attemptId (36-character UUID), levelId (known curriculum ID),
taskId (1-100 characters), hintNumber (1-100), cost (15/30/50 according to number),
spentBefore, spentAfter and server createdAt. Parent progress adds lastHintPurchase.
The receipt and xpSpent debit are committed in the same transaction. Rules require
both records to agree and prevent receipt updates or ordinary deletion. Read/list
is restricted to the owner, including authenticated account-deletion recovery.
Export includes receipts and account deletion drains them before deleting progress.

This records paid hints, not full attempt/decision histories. Leaving a level
still abandons that run; a new run can charge for its own hints. This does not add
whole-level resume or change first-clear grading, hearts or developer unlocking.
Client-reported grading retains its documented integrity limitations.

## Compatibility and rollout

Firestore rules were deployed to cyberitycvsu on 16 October 2026 after explicit
user approval. Firebase confirmed successful compilation and release to
cloud.firestore. The deployed file matches commit d7dddee; SHA-256:
0A3B942B8031420B2C2B4C07A530512A72FC8D6506855809B4D5A267F53996EE.
The matching APK has not been installed or distributed in this session. Phone
testing remains paused. Updated clients are required for hint purchases:

- New APK against old rules: completion transactions use the existing compatible
  fields, but hint purchases fail safely because receipt writes are not allowed.
- Old APK against new rules: ordinary completion reads/writes continue, but old
  receipt-free hint deductions are rejected. Require the updated APK before
  students use hints; do not deploy the rules silently while old builds are in use.
- No existing records require migration. New purchases add receipts going forward.

## Validation and device checks

- Final Android debug build succeeded and all 38 existing JVM tests passed,
  including the pending-award preservation refinement.
- All five Firestore emulator tests passed: existing session/privacy/score suites,
  plus purchase/debit matching, ownership, immutable receipts, concurrent purchase
  retries, deletion recovery, and repeated/legacy completion awards. Tests load
  firestore.rules explicitly through initializeTestEnvironment, despite the
  emulator startup notice about default rules in its launcher configuration.
- Rule validation was simplified to avoid evaluating the complete new-award
  catalog when no award changes. Full-curriculum purchase coverage remains in the
  existing score suite. Some deliberately rejected attack writes report the
  evaluator limit; valid purchase and full-history operations pass.

Pending device checks with disposable accounts: disconnect before Finish, retry,
close/reopen with an unconfirmed result, tap hint repeatedly, lose connectivity
around purchase confirmation, retry the same hint, and export/delete receipts.
Check that EXP is deducted once and the hint opens only after confirmation.

## Agreed assessment direction

The user prefers pre-test and post-test inside the e-learning app. This is planned,
not implemented in this saving change. Proposed next design: a baseline before
the course, a final assessment after required levels, equivalent question coverage,
saved score/attempt/version records and a comparison of results. Keep assessment
scores distinct from spendable EXP. Define retake and certificate eligibility
rules before implementing the assessment flow.
