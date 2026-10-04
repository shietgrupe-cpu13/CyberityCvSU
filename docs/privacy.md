# Privacy implementation and integration

Privacy contains leaderboard visibility, JSON download, and permanent account
deletion. It describes current data use; an approved institutional policy is
still needed. Problem reports have no configured support recipient.

Hiding stores a private preference and deletes the leaderboard entry atomically.
Showing reconstructs it from server profile/progress data. Publishing reads the
preference in a transaction; rules use getAfter to stop old clients republishing
hidden entries. Export reads owned data from the server and uses Android's file
picker to save JSON. It includes profile/progress, leaderboard, privacy/session
records, student ID claims, account UID/email/verification and timestamps.
Passwords, tokens and authenticator secrets are excluded. Saved exports remain
under the user's control after deletion.

Deletion requires password/MFA verification and an explicit confirmation. It
stores deleting=true, leaderboardVisible=false and deletionAuthTime, blocking
normal writes. Cleanup requires the verified owner and the same fresh auth_time.
Older tokens cannot reset the marker or use cleanup. A new verification advances
the marker's auth_time and permits retry. Session/ID documents are drained in
400-record batches, profile/progress/leaderboard removed, local account/progress
preferences and reminders cleared, then Auth deleted last. Local cleanup happens
before Auth deletion because its callback can immediately unmount the screen.
Firestore's offline cache is not remotely erased.

The deletion marker and any existing session cutoff remain as access-blocking
tombstones. Removing them could reopen access for old tokens. After interrupted
deletion, startup routes to Privacy recovery instead of profile creation. Partial
cleanup is permanent; retry completes deletion, not rollback.

Both emulator rule suites and Firebase compilation passed. Rules were deployed
to cyberitycvsu on 2026-10-04; the app built and was installed on the connected
phone. Three phone tests passed for stable windows, Privacy/Help navigation and
verification cancellation. Twelve reminder tests passed. Rule tests cover
atomic hide/show, stale publications, ownership, fresh cleanup/retry, blocked
normal writes, and retained monotonic deletion markers.

Actual account deletion and password/MFA changes still require a dedicated test
account. No real account was deleted during validation. File export delivery,
background reminder delivery/reboot, and the report chooser need end-to-end
device checks.
