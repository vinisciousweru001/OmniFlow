package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.EventType
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority

class Converters {
    @TypeConverter
    fun fromPriority(priority: TaskPriority?): String = priority?.name ?: TaskPriority.MEDIUM.name

    @TypeConverter
    fun toPriority(value: String?): TaskPriority = try {
        if (value != null) TaskPriority.valueOf(value) else TaskPriority.MEDIUM
    } catch (_: Exception) {
        TaskPriority.MEDIUM
    }

    @TypeConverter
    fun fromCategory(category: TaskCategory?): String = category?.name ?: TaskCategory.WORK.name

    @TypeConverter
    fun toCategory(value: String?): TaskCategory = try {
        if (value != null) TaskCategory.valueOf(value) else TaskCategory.WORK
    } catch (_: Exception) {
        TaskCategory.WORK
    }

    @TypeConverter
    fun fromEventType(eventType: EventType?): String = eventType?.name ?: EventType.MEETING.name

    @TypeConverter
    fun toEventType(value: String?): EventType = try {
        if (value != null) EventType.valueOf(value) else EventType.MEETING
    } catch (_: Exception) {
        EventType.MEETING
    }
}
