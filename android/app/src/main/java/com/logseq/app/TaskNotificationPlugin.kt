package com.logseq.app

import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.logseq.app.notifications.TaskNotificationScheduler

@CapacitorPlugin(name = "TaskNotification")
class TaskNotificationPlugin : Plugin() {

    @PluginMethod
    fun enableNotifications(call: PluginCall) {
        val intervalMinutes = call.getLong("intervalMinutes") ?: 15L
        
        try {
            TaskNotificationScheduler.schedulePeriodic(context, intervalMinutes)
            call.resolve()
        } catch (e: Exception) {
            call.reject("Failed to enable notifications: ${e.message}")
        }
    }

    @PluginMethod
    fun disableNotifications(call: PluginCall) {
        try {
            TaskNotificationScheduler.cancel(context)
            call.resolve()
        } catch (e: Exception) {
            call.reject("Failed to disable notifications: ${e.message}")
        }
    }

    @PluginMethod
    fun checkNow(call: PluginCall) {
        try {
            TaskNotificationScheduler.checkNow(context)
            call.resolve()
        } catch (e: Exception) {
            call.reject("Failed to check tasks: ${e.message}")
        }
    }

    @PluginMethod
    fun isEnabled(call: PluginCall) {
        try {
            val isScheduled = TaskNotificationScheduler.isScheduled(context)
            val result = com.getcapacitor.JSObject()
            result.put("enabled", isScheduled)
            call.resolve(result)
        } catch (e: Exception) {
            call.reject("Failed to check status: ${e.message}")
        }
    }
}
