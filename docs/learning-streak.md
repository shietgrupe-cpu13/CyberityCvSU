# Learning streak

The Learn header and Profile dashboard show a flame and the student's consecutive
learning days. Complete a level or quiz to light the flame; replays count, but the
optional walkthrough (200) does not. Multiple completions on one day count once.
All students use Philippine calendar days (UTC+8). Yesterday's streak remains
visible with a gray flame until today's goal is completed. Missing a full day
resets the visible count to zero; the next completion starts at one.

Streak fields live in the existing private `userProgress/{uid}` document:

- `streakDays`: consecutive learning days at the last confirmed activity.
- `streakLastActivityAt`: Firestore server timestamp for that activity.
- `streakLevelId`: known completed level associated with the activity.

The completion transaction updates these fields alongside completion and EXP.
Firestore validates the count using request time, rather than trusting the device
date. Concurrent completions serialize and same-day retries cannot add days.
The UI listens to confirmed progress changes and reevaluates the date every
30 seconds using server time anchored to Android's monotonic uptime. It never
uses the phone's date or timezone. Until server time is available after a restart,
the flame remains inactive. Every completion refreshes the server clock before
its transaction; a failed refresh follows the existing save retry path. Firestore
also validates the clock anchor itself against request time.
Cached state can display offline after time synchronization; completing a day requires a
successful server save. A date-boundary rejection can use the existing save retry.

Existing students start at zero: there are no historic activity dates to backfill.
The saved count is historical until the next activity; the UI derives expiration
without a scheduled write. Existing account export/deletion includes these fields
because it already covers the whole progress document. Streak is private and does
not affect leaderboard rankings, EXP, hearts, or certificate eligibility.

Rules retain compatibility with old clients that do not submit streak fields.
As with existing progress, client reports do not prove that a task was solved.
No existing student records need manual modification or migration.

## Administrator corrections in Firebase Console

Project administrators with Firestore data-write permissions can edit
`userProgress/{uid}` directly. Console/IAM access is separate from app security
rules: being a GitHub collaborator or having `users/{uid}.isTester=true` does not
grant database administrator access.

To restore a streak, set `streakDays` to the desired positive integer and
`streakLastActivityAt` to today's real timestamp (Philippine date). The app updates
through its listener. Same-day completions retain the corrected count; tomorrow's
completion adds one. Editing only the count on an expired activity date will still
show zero because normal expiration remains active. Do not set a future date.

To reset a streak, set `streakDays` to the number 0. The flame becomes inactive,
and the next confirmed activity starts at 1. Leave existing completion/EXP data
and `streakLevelId` alone; the next activity writes its own level ID. If adding
fields manually for an older user, use a number for the count and a timestamp for
the activity date. This correction does not freeze the streak or grant EXP.

No production student's streak was changed while implementing these controls.

Validation: debug APK built successfully; all 52 JVM tests and all 8 Firestore
test groups passed. Device interaction has not yet been checked for this change.
