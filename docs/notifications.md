# Learning notifications and settings screens

Notifications now has working local learning reminders on Spark. No Firebase
deployment or push messaging setup is needed. Security alerts remain explicitly
planned; the app does not claim to monitor sign-ins in the background.

## Using reminders

Open Settings > Notifications, choose weekdays and a local time, then enable
Learning reminders. On Android 13+, approve the notification permission prompt.
The screen detects blocked app/channel notifications and links to Android's
notification settings. Send a test notification checks immediate delivery.
Preferences apply to this phone and are excluded from backup/device transfer.

The app registers one inexact AlarmManager alarm for the next selected day. It
reschedules after delivery, reboot, app update, and clock/time-zone changes, and
restores the schedule on app startup. Turning reminders off cancels the alarm
and the current reminder notification. The receiver checks the saved preference
and signed-in verified account before posting a generic learning reminder.
Tapping opens Cyberity's existing startup/sign-in flow.

Times are approximate: Android and manufacturer battery policies can delay
delivery. Force-stopping the app prevents normal delivery until it is reopened.
Offline reminders work, but do not check remote account security; a remotely
revoked session may receive a generic reminder before reconnecting. No account
details appear in reminder text. These notifications are separate from Firebase
token revocation and security alerts.

## Settings navigation

Appearance, Learning, Security, Notifications, Privacy, and Help & About each
open a dedicated full-screen destination from the dashboard. Existing theme,
text size, tutorial replay, authenticator, password reset, device list, and
logout actions remain connected. Other unimplemented preferences stay labeled
Planned, rather than presenting switches that do nothing.

## Validation

The debug APK builds successfully. Eight ReminderSchedule unit tests cover
empty/invalid schedules, later today, skipped weekdays, weekly recurrence,
year rollover, local time zones, and daylight-saving changes.

Device delivery and visual checks remain necessary; no device was connected
during implementation. Check Android notification permission grant/denial,
channel blocking, selected-day/time persistence, disabling reminders, test
notification tap, background delivery, reboot, and time-zone changes. Navigate
all categories in light/dark mode and with large font settings, and verify Back
and Close along with the existing security confirmation/MFA dialogs.

Follow-up Security navigation fix: all settings destinations now share one
Dialog window, so entering Security cannot briefly expose the underlying Learn
screen by dismissing and recreating that window. The debug APK was rebuilt and
installed on a connected phone. SettingsWindowTest passed on that phone,
verifying identical window views before entry, inside Security, and after Back.

References:
- https://developer.android.com/develop/ui/compose/notifications/notification-permission
- https://developer.android.com/develop/background-work/services/alarms
