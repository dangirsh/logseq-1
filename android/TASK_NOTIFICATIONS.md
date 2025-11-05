# Task Notifications for Logseq Android

This feature adds system notifications for tasks with SCHEDULED or DEADLINE timestamps, similar to Orgzly's notification functionality.

## Features

- **Automatic Task Detection**: Parses markdown and org files to find tasks with SCHEDULED/DEADLINE timestamps
- **Periodic Checking**: Configurable interval for checking due tasks (default: 15 minutes)
- **Priority Notifications**: Uses high-priority notifications to ensure visibility
- **Customizable**: Users can enable/disable notifications and adjust check frequency
- **Battery Efficient**: Uses Android WorkManager for optimized background processing

## Architecture

### Components

1. **Task.kt** - Data model representing a task with scheduling information
2. **TaskParser.kt** - Parses Logseq markdown/org files to extract tasks with timestamps
3. **TaskNotificationWorker.kt** - Background worker that checks for due tasks and shows notifications
4. **TaskNotificationScheduler.kt** - Manages periodic scheduling using WorkManager
5. **TaskNotificationPlugin.kt** - Capacitor plugin exposing functionality to JavaScript

### Task Format Support

The parser supports standard Org-mode timestamp formats:

```markdown
- TODO Buy groceries
  DEADLINE: <2025-11-05 Tue 18:00>

- DOING Write report
  SCHEDULED: <2025-11-04 Mon>
```

Supported markers: TODO, DOING, NOW, LATER, WAITING
Excluded markers: DONE, CANCELLED (these won't trigger notifications)

## Usage

### For Users

1. Open Settings in the Logseq Android app
2. Navigate to the Task Notifications section
3. Toggle "Task Notifications" on
4. Optionally adjust the check interval (5-60 minutes)
5. Grant notification permissions when prompted

### For Developers

#### Enable notifications programmatically:

```typescript
import TaskNotification from 'frontend.mobile.capacitor.task-notification'

// Enable with default 15-minute interval
await TaskNotification.enableNotifications()

// Enable with custom interval
await TaskNotification.enableNotifications({ intervalMinutes: 30 })

// Check for due tasks immediately
await TaskNotification.checkNow()

// Disable notifications
await TaskNotification.disableNotifications()

// Check if enabled
const { enabled } = await TaskNotification.isEnabled()
```

## Implementation Details

### Permissions

The following permissions are required and added to AndroidManifest.xml:
- `POST_NOTIFICATIONS` - Show notifications (Android 13+)
- `SCHEDULE_EXACT_ALARM` - Schedule exact alarms for timely notifications
- `USE_EXACT_ALARM` - Use exact alarm timing

### WorkManager Configuration

- **Periodic Work**: Runs every 15 minutes (configurable) to check for due tasks
- **Constraints**: No battery constraints (notifications are important)
- **Unique Work**: Uses `ExistingPeriodicWorkPolicy.KEEP` to avoid duplicate workers

### File Discovery

The worker searches for tasks in:
1. `/Documents/Logseq/` directory
2. App's external files directory
3. All `.md` and `.org` files are parsed

### Notification Channel

- **Channel ID**: `logseq_task_notifications`
- **Channel Name**: "Task Reminders"
- **Importance**: HIGH (shows as heads-up notification)
- **Features**: Vibration enabled

## Testing

### Manual Testing

1. Create a test task with a near-future deadline:
   ```markdown
   - TODO Test notification
     DEADLINE: <2025-11-04 Mon 15:30>
   ```

2. Enable task notifications in settings

3. Use "Check for due tasks now" button to trigger immediate check

4. Wait for the scheduled time to see if notification appears

### Debug Logs

Enable verbose logging to see task detection:
```bash
adb logcat -s TaskNotificationWorker:D TaskParser:D
```

## Future Enhancements

Potential improvements:
- [ ] Advance notification (notify X minutes before due)
- [ ] Snooze functionality
- [ ] Custom notification sounds
- [ ] Quick actions in notification (mark as done, snooze)
- [ ] Notification grouping for multiple tasks
- [ ] Support for repeating tasks
- [ ] Integration with system calendar

## Comparison with Orgzly

Similar features:
- ✅ SCHEDULED/DEADLINE timestamp detection
- ✅ System notifications for due tasks
- ✅ Configurable check interval
- ✅ Manual check trigger

Differences:
- Orgzly supports more advanced agenda views
- Logseq uses WorkManager (more modern Android approach)
- Logseq integrates with its own UI components

## Troubleshooting

### Notifications not appearing

1. Check notification permissions: Settings > Apps > Logseq > Permissions > Notifications
2. Verify task format matches expected patterns
3. Check battery optimization settings (should allow background work)
4. Review logcat for error messages

### Tasks not detected

1. Ensure files are in correct directory
2. Verify timestamp format matches Org-mode spec
3. Check that tasks aren't marked as DONE/CANCELLED
4. Confirm file extensions are .md or .org

### Battery drain

1. Increase check interval in settings
2. Check WorkManager constraints
3. Review number of files being parsed
