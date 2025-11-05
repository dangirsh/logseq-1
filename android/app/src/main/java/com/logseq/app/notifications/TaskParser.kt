package com.logseq.app.notifications

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object TaskParser {
    private const val TAG = "TaskParser"
    
    // Org-mode style timestamp patterns
    private val SCHEDULED_PATTERN = Pattern.compile(
        "SCHEDULED:\\s*<([^>]+)>",
        Pattern.CASE_INSENSITIVE
    )
    
    private val DEADLINE_PATTERN = Pattern.compile(
        "DEADLINE:\\s*<([^>]+)>",
        Pattern.CASE_INSENSITIVE
    )
    
    private val TODO_MARKER_PATTERN = Pattern.compile(
        "^\\s*(?:[-*]|\\d+\\.)?\\s+(TODO|DOING|NOW|LATER|WAITING|DONE|CANCELLED)\\s+",
        Pattern.CASE_INSENSITIVE
    )
    
    // Common date formats used in Logseq/Org-mode
    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd EEE HH:mm", Locale.US),
        SimpleDateFormat("yyyy-MM-dd EEE", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )
    
    /**
     * Parse tasks from Logseq markdown/org content
     */
    fun parseTasks(content: String, fileId: String = "unknown"): List<Task> {
        val tasks = mutableListOf<Task>()
        val lines = content.split("\n")
        
        var currentBlock = StringBuilder()
        var blockStartIndex = 0
        
        for ((index, line) in lines.withIndex()) {
            // Check if this is a new block (starts with -, *, or number)
            if (line.trimStart().matches(Regex("^[-*]|^\\d+\\."))) {
                // Process previous block if any
                if (currentBlock.isNotEmpty()) {
                    parseBlock(currentBlock.toString(), "$fileId:$blockStartIndex")?.let {
                        tasks.add(it)
                    }
                }
                // Start new block
                currentBlock = StringBuilder(line)
                blockStartIndex = index
            } else if (currentBlock.isNotEmpty()) {
                // Continue current block
                currentBlock.append("\n").append(line)
            }
        }
        
        // Process last block
        if (currentBlock.isNotEmpty()) {
            parseBlock(currentBlock.toString(), "$fileId:$blockStartIndex")?.let {
                tasks.add(it)
            }
        }
        
        return tasks.filter { it.marker != "DONE" && it.marker != "CANCELLED" }
    }
    
    private fun parseBlock(block: String, blockId: String): Task? {
        // Extract TODO marker
        val markerMatcher = TODO_MARKER_PATTERN.matcher(block)
        val marker = if (markerMatcher.find()) {
            markerMatcher.group(1)
        } else {
            null
        }
        
        // Extract SCHEDULED date
        val scheduledMatcher = SCHEDULED_PATTERN.matcher(block)
        val scheduledDate = if (scheduledMatcher.find()) {
            parseDate(scheduledMatcher.group(1))
        } else {
            null
        }
        
        // Extract DEADLINE date
        val deadlineMatcher = DEADLINE_PATTERN.matcher(block)
        val deadlineDate = if (deadlineMatcher.find()) {
            parseDate(deadlineMatcher.group(1))
        } else {
            null
        }
        
        // Only create task if it has a marker or a date
        if (marker == null && scheduledDate == null && deadlineDate == null) {
            return null
        }
        
        // Extract title (first line, remove markers and timestamps)
        val title = extractTitle(block)
        
        return Task(
            id = blockId,
            title = title,
            content = block,
            scheduledDate = scheduledDate,
            deadlineDate = deadlineDate,
            marker = marker
        )
    }
    
    private fun extractTitle(block: String): String {
        var title = block.lines().firstOrNull() ?: ""
        
        // Remove bullet/number prefix
        title = title.replace(Regex("^\\s*[-*]\\s*|^\\s*\\d+\\.\\s*"), "")
        
        // Remove TODO marker
        title = title.replace(TODO_MARKER_PATTERN.toRegex(), "")
        
        // Remove SCHEDULED/DEADLINE lines from title
        title = title.replace(Regex("SCHEDULED:\\s*<[^>]+>"), "")
        title = title.replace(Regex("DEADLINE:\\s*<[^>]+>"), "")
        
        return title.trim()
    }
    
    private fun parseDate(dateStr: String): Date? {
        for (format in dateFormats) {
            try {
                return format.parse(dateStr)
            } catch (e: Exception) {
                // Try next format
            }
        }
        
        Log.w(TAG, "Failed to parse date: $dateStr")
        return null
    }
    
    /**
     * Get all tasks that should trigger notifications soon
     */
    fun getUpcomingTasks(content: String, fileId: String, advanceMinutes: Int = 0): List<Task> {
        val allTasks = parseTasks(content, fileId)
        val now = System.currentTimeMillis()
        val threshold = now + (advanceMinutes * 60 * 1000)
        
        return allTasks.filter { task ->
            val notificationDate = task.getNotificationDate()
            notificationDate != null && notificationDate.time <= threshold
        }
    }
}
