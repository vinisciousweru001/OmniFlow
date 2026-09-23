package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String = "Ideas", // Comma-separated tags
    val isPinned: Boolean = false,
    val colorHex: String = "#1E293B",
    val linkedDateEpochMs: Long? = null,
    val aiSummary: String? = null,
    val extractedTasksCount: Int = 0,
    val syncedDevicesCount: Int = 4,
    val lastModifiedEpochMs: Long = System.currentTimeMillis()
)
