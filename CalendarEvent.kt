package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventType(val label: String, val defaultColor: String) {
    MEETING("Meeting", "#6366F1"),
    FOCUS_BLOCK("Deep Focus", "#8B5CF6"),
    DEADLINE("Deadline", "#EF4444"),
    TASK_TIMEBLOCK("Task Block", "#0EA5E9"),
    PERSONAL("Personal", "#10B981")
}

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startEpochMs: Long,
    val endEpochMs: Long,
    val locationOrLink: String = "",
    val colorHex: String = "#6366F1",
    val eventType: EventType = EventType.MEETING,
    val linkedTaskId: Long? = null,
    val isAllDay: Boolean = false,
    val syncedDevicesCount: Int = 4,
    val lastModifiedEpochMs: Long = System.currentTimeMillis()
)
