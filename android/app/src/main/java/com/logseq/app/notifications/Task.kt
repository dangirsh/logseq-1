package com.logseq.app.notifications

import java.util.Date

data class Task(
    val id: String,
    val title: String,
    val content: String,
    val scheduledDate: Date? = null,
    val deadlineDate: Date? = null,
    val marker: String? = null // TODO, DOING, etc.
) {
    fun getNotificationDate(): Date? {
        return deadlineDate ?: scheduledDate
    }
    
    fun isDue(): Boolean {
        val notificationDate = getNotificationDate() ?: return false
        return notificationDate.time <= System.currentTimeMillis()
    }
    
    fun getMinutesUntilDue(): Long {
        val notificationDate = getNotificationDate() ?: return Long.MAX_VALUE
        val diff = notificationDate.time - System.currentTimeMillis()
        return diff / (60 * 1000)
    }
}
