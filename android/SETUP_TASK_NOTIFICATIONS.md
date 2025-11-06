# Task Notifications Setup Guide

Quick guide to get task notifications working in your Logseq Android build.

## Prerequisites

- Android Studio or Gradle build environment
- Kotlin support enabled
- Capacitor-based Logseq Android app

## Installation Steps

### 1. Add Dependencies

The WorkManager dependency has already been added to `app/build.gradle`:

```gradle
implementation 'androidx.work:work-runtime-ktx:2.8.1'
```

### 2. Verify Permissions

Check that `AndroidManifest.xml` includes:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

### 3. Register the Plugin

Verify `MainActivity.java` registers the plugin:

```java
registerPlugin(TaskNotificationPlugin.class);
```

### 4. Build the App

```bash
cd android
./gradlew assembleDebug
```

Or build through Android Studio.

### 5. Test the Feature

1. Install the app on a device or emulator
2. Create a test task:
   ```
   - TODO Test task
     DEADLINE: <2025-11-04 Mon 14:00>
   ```
3. Enable notifications via settings (when UI is integrated)
4. Or test programmatically via console:
   ```javascript
   await window.TaskNotification.enableNotifications()
   await window.TaskNotification.checkNow()
   ```

## Files Added

### Android (Kotlin)
- `app/src/main/java/com/logseq/app/notifications/Task.kt`
- `app/src/main/java/com/logseq/app/notifications/TaskParser.kt`
- `app/src/main/java/com/logseq/app/notifications/TaskNotificationWorker.kt`
- `app/src/main/java/com/logseq/app/notifications/TaskNotificationScheduler.kt`
- `app/src/main/java/com/logseq/app/TaskNotificationPlugin.kt`
- `app/src/main/res/drawable/ic_notification.xml`

### Frontend (TypeScript/ClojureScript)
- `src/main/frontend/mobile/capacitor/task-notification.ts`
- `src/main/frontend/mobile/components/task_notifications.cljs`

### Documentation
- `android/TASK_NOTIFICATIONS.md`
- `android/SETUP_TASK_NOTIFICATIONS.md`

## Integrating Settings UI

To add the settings UI to your app:

1. Import the component in your settings page:
   ```clojure
   (ns frontend.components.settings
     (:require [frontend.mobile.components.task-notifications :as task-notif]))
   ```

2. Add to your settings UI:
   ```clojure
   (when (mobile-util/native-android?)
     [:div.settings-section
      [:h2 "Task Notifications"]
      (task-notif/task-notification-settings)])
   ```

3. Initialize on app startup:
   ```clojure
   (task-notif/init)
   ```

## Testing Checklist

- [ ] App builds without errors
- [ ] Plugin registered successfully
- [ ] Can enable notifications via JavaScript
- [ ] Can disable notifications via JavaScript
- [ ] Worker runs on schedule
- [ ] Notifications appear for due tasks
- [ ] Notification icon displays correctly
- [ ] Tapping notification opens app
- [ ] Settings UI toggles work correctly

## Troubleshooting Build Issues

### Kotlin not configured
If you see Kotlin errors, ensure `kotlin-android` plugin is applied in `build.gradle`.

### Capacitor API not found
Make sure Capacitor dependencies are properly imported. The plugin extends `com.getcapacitor.Plugin`.

### WorkManager not found
Sync Gradle and ensure the dependency was added correctly.

### R.drawable.ic_notification not found
Verify `ic_notification.xml` is in `res/drawable/` directory.

## Next Steps

After successful build:
1. Test with real tasks in your Logseq graph
2. Adjust notification timing and formatting as needed
3. Consider adding advanced features (see TASK_NOTIFICATIONS.md)
4. Submit feedback or contribute improvements

## Support

For issues or questions:
- Check `adb logcat` for error messages
- Review TASK_NOTIFICATIONS.md for detailed documentation
- Open an issue on the Logseq repository
