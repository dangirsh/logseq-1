package com.logseq.app.notifications

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object TaskNotificationScheduler {
    private const val TAG = "TaskNotificationScheduler"
    private const val WORK_NAME = "task_notification_check"
    
    /**
     * Schedule periodic task notifications check
     * @param context Application context
     * @param intervalMinutes How often to check for due tasks (default: 15 minutes)
     */
    fun schedulePeriodic(context: Context, intervalMinutes: Long = 15) {
        Log.d(TAG, "Scheduling periodic task notifications every $intervalMinutes minutes")
        
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<TaskNotificationWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        Log.d(TAG, "Periodic task notifications scheduled successfully")
    }
    
    /**
     * Run an immediate one-time check for due tasks
     */
    fun checkNow(context: Context) {
        Log.d(TAG, "Running immediate task notification check")
        
        val workRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .addTag("task_notification_check_now")
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    /**
     * Cancel all scheduled task notifications
     */
    fun cancel(context: Context) {
        Log.d(TAG, "Cancelling task notifications")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
    
    /**
     * Check if notifications are currently scheduled
     */
    fun isScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()
        
        return workInfos.any { 
            it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING 
        }
    }
}
