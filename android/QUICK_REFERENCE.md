# Task Notifications - Quick Reference

## 🚀 Quick Start

```bash
# Build the app
cd ~/logseq/android
./gradlew assembleDebug

# Test notifications
adb logcat -s TaskNotificationWorker:D TaskParser:D
```

## 📝 Task Format

```markdown
- TODO Task title
  DEADLINE: <2025-11-04 Mon 14:00>

- DOING Another task
  SCHEDULED: <2025-11-04 Mon>
```

## 💻 JavaScript API

```javascript
// Enable (15 min interval)
await window.TaskNotification.enableNotifications()

// Enable (custom interval)
await window.TaskNotification.enableNotifications({ intervalMinutes: 30 })

// Check now
await window.TaskNotification.checkNow()

// Disable
await window.TaskNotification.disableNotifications()

// Check status
const { enabled } = await window.TaskNotification.isEnabled()
```

## 📁 Key Files

**Android (Kotlin):**
- `notifications/Task.kt` - Data model
- `notifications/TaskParser.kt` - File parser
- `notifications/TaskNotificationWorker.kt` - Background worker
- `notifications/TaskNotificationScheduler.kt` - Scheduler
- `TaskNotificationPlugin.kt` - Capacitor plugin

**Frontend:**
- `mobile/capacitor/task-notification.ts` - TypeScript API
- `mobile/components/task_notifications.cljs` - Settings UI

## 🔧 Configuration

**Permissions (AndroidManifest.xml):**
- `POST_NOTIFICATIONS`
- `SCHEDULE_EXACT_ALARM`
- `USE_EXACT_ALARM`

**Dependency (build.gradle):**
```gradle
implementation 'androidx.work:work-runtime-ktx:2.8.1'
```

## 🎯 Supported Formats

**Task Markers:**
- TODO, DOING, NOW, LATER, WAITING ✅
- DONE, CANCELLED (ignored) ❌

**Date Formats:**
- `yyyy-MM-dd EEE HH:mm` → 2025-11-04 Mon 14:30
- `yyyy-MM-dd EEE` → 2025-11-04 Mon
- `yyyy-MM-dd HH:mm` → 2025-11-04 14:30
- `yyyy-MM-dd` → 2025-11-04

## 🐛 Debugging

```bash
# View logs
adb logcat -s TaskNotificationWorker:D TaskParser:D

# Check WorkManager status
adb shell dumpsys jobscheduler | grep logseq

# Test notification
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

## ⚙️ Settings UI Integration

```clojure
;; Add to settings
(ns frontend.components.settings
  (:require [frontend.mobile.components.task-notifications :as task-notif]))

;; In settings UI
(when (mobile-util/native-android?)
  [:div.settings-section
   [:h2 "Task Notifications"]
   (task-notif/task-notification-settings)])

;; Initialize on app start
(task-notif/init)
```

## 📊 Notification Behavior

| Time Until Due | Action |
|---------------|--------|
| > 0 minutes | No notification |
| ≤ 0 minutes | Show notification |

**Priority:** DEADLINE > SCHEDULED

## ✅ Testing Checklist

- [ ] App builds successfully
- [ ] Plugin registers in MainActivity
- [ ] Can enable via JavaScript
- [ ] Can disable via JavaScript  
- [ ] Worker executes periodically
- [ ] Notifications appear
- [ ] Tapping notification opens app
- [ ] Settings UI works

## 📚 Documentation

- `TASK_NOTIFICATIONS.md` - Full documentation
- `SETUP_TASK_NOTIFICATIONS.md` - Setup guide
- `TASK_NOTIFICATION_FEATURE_SUMMARY.md` - Implementation summary

## 🆘 Common Issues

**Notifications not showing?**
1. Check permissions in Android settings
2. Verify task format
3. Check logcat for errors
4. Disable battery optimization

**Tasks not detected?**
1. Verify file location
2. Check file extension (.md or .org)
3. Confirm timestamp format
4. Ensure task not marked DONE

**High battery usage?**
1. Increase check interval
2. Reduce number of files
3. Check WorkManager constraints

