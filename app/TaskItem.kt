package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority(val label: String, val level: Int) {
    CRITICAL("Critical", 4),
    HIGH("High", 3),
    MEDIUM("Medium", 2),
    LOW("Low", 1)
}

enum class TaskCategory(val label: String) {
    WORK("Work"),
    PERSONAL("Personal"),
    DEEP_WORK("Focus Project"),
    LEARNING("Study"),
    ADMIN("Admin & Errands")
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val dueDateEpochMs: Long? = null,
    val estimatedMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val completedAtEpochMs: Long? = null,
    val subtasksJson: String = "[]", // Serialized JSON list of subtask items
    val aiPriorityScore: Int = 50,    // 0-100 score calculated by AI/Heuristic
    val aiSuggestedSlot: String? = null,
    val syncedDevicesCount: Int = 4,   // How many devices have this synced
    val lastModifiedEpochMs: Long = System.currentTimeMillis()
) {
    val isOverdue: Boolean
        get() {
            if (isCompleted || dueDateEpochMs == null) return false
            return System.currentTimeMillis() > dueDateEpochMs
        }

    val isDueToday: Boolean
        get() {
            if (isCompleted || dueDateEpochMs == null) return false
            val now = System.currentTimeMillis()
            val diff = dueDateEpochMs - now
            return diff in 0..(24 * 3600 * 1000L)
        }
}

data class Subtask(
    val id: String,
    val title: String,
    val isDone: Boolean = false
)
