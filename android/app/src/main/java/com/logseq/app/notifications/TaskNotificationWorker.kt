package com.logseq.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.logseq.app.MainActivity
import com.logseq.app.R
import java.io.File

class TaskNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val TAG = "TaskNotificationWorker"
        const val CHANNEL_ID = "logseq_task_notifications"
        const val CHANNEL_NAME = "Task Reminders"
        private const val NOTIFICATION_ID_BASE = 10000
    }

    override fun doWork(): Result {
        Log.d(TAG, "TaskNotificationWorker started")
        
        try {
            createNotificationChannel()
            checkAndNotifyTasks()
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in TaskNotificationWorker", e)
            return Result.failure()
        }
    }

    private fun checkAndNotifyTasks() {
        // Get the Logseq data directory
        val graphDir = getLogseqGraphDirectory() ?: run {
            Log.w(TAG, "Could not find Logseq graph directory")
            return
        }
        
        Log.d(TAG, "Checking tasks in: ${graphDir.absolutePath}")
        
        val allTasks = mutableListOf<Task>()
        
        // Search for markdown and org files
        graphDir.walkTopDown().forEach { file ->
            if (file.isFile && (file.extension == "md" || file.extension == "org")) {
                try {
                    val content = file.readText()
                    val tasks = TaskParser.parseTasks(content, file.name)
                    allTasks.addAll(tasks)
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading file: ${file.name}", e)
                }
            }
        }
        
        Log.d(TAG, "Found ${allTasks.size} total tasks")
        
        // Filter tasks that are due or overdue
        val dueTasks = allTasks.filter { task ->
            val notificationDate = task.getNotificationDate()
            notificationDate != null && task.isDue()
        }
        
        Log.d(TAG, "Found ${dueTasks.size} due tasks")
        
        // Show notifications for due tasks
        dueTasks.forEachIndexed { index, task ->
            showNotification(task, NOTIFICATION_ID_BASE + index)
        }
    }

    private fun getLogseqGraphDirectory(): File? {
        // Try common Logseq storage locations
        val externalStorageDir = applicationContext.getExternalFilesDir(null)
        
        // Check for Logseq in Documents
        val documentsDir = File(externalStorageDir?.parentFile?.parentFile, "Documents/Logseq")
        if (documentsDir.exists() && documentsDir.isDirectory) {
            return documentsDir
        }
        
        // Check for Logseq in app's external files dir
        val appLogseqDir = File(externalStorageDir, "Logseq")
        if (appLogseqDir.exists() && appLogseqDir.isDirectory) {
            return appLogseqDir
        }
        
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for scheduled and deadline tasks"
                enableVibration(true)
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(task: Task, notificationId: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // You could add extras here to open the specific task
            putExtra("task_id", task.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notificationType = when {
            task.deadlineDate != null -> "DEADLINE"
            task.scheduledDate != null -> "SCHEDULED"
            else -> "TASK"
        }
        
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$notificationType: ${task.title}")
            .setContentText(getNotificationText(task))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getNotificationText(task)))
        
        try {
            NotificationManagerCompat.from(applicationContext).notify(notificationId, builder.build())
            Log.d(TAG, "Notification shown for task: ${task.title}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for showing notification", e)
        }
    }

    private fun getNotificationText(task: Task): String {
        val parts = mutableListOf<String>()
        
        if (task.marker != null) {
            parts.add("[${task.marker}]")
        }
        
        task.deadlineDate?.let {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
            parts.add("Deadline: ${format.format(it)}")
        }
        
        task.scheduledDate?.let {
            if (task.deadlineDate == null) {
                val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
                parts.add("Scheduled: ${format.format(it)}")
            }
        }
        
        return parts.joinToString(" • ")
    }
}
