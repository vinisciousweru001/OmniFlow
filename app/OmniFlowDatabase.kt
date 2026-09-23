package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.CalendarEvent
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem

@Database(
    entities = [TaskItem::class, CalendarEvent::class, NoteItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OmniFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun calendarDao(): CalendarDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: OmniFlowDatabase? = null

        fun getDatabase(context: Context): OmniFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmniFlowDatabase::class.java,
                    "omniflow_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
