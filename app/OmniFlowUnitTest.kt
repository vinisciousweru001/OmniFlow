package com.example

import com.example.data.local.CalendarDao
import com.example.data.local.NoteDao
import com.example.data.local.TaskDao
import com.example.data.model.CalendarEvent
import com.example.data.model.EventType
import com.example.data.model.NoteItem
import com.example.data.model.Subtask
import com.example.data.model.SuggestionType
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.repository.OmniFlowRepository
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OmniFlowUnitTest {

    private fun createDummyRepository(): OmniFlowRepository {
        val fakeTaskDao = object : TaskDao {
            override fun getAllTasks() = flowOf(emptyList<TaskItem>())
            override fun getPendingTasks() = flowOf(emptyList<TaskItem>())
            override fun getCompletedTasks() = flowOf(emptyList<TaskItem>())
            override suspend fun getTaskById(id: Long): TaskItem? = null
            override suspend fun insertTask(task: TaskItem): Long = 1L
            override suspend fun insertTasks(tasks: List<TaskItem>) {}
            override suspend fun updateTask(task: TaskItem) {}
            override suspend fun deleteTask(task: TaskItem) {}
            override suspend fun deleteTaskById(id: Long) {}
            override suspend fun setTaskCompleted(id: Long, completed: Boolean, completedAt: Long?) {}
        }

        val fakeCalendarDao = object : CalendarDao {
            override fun getAllEvents() = flowOf(emptyList<CalendarEvent>())
            override fun getEventsBetween(start: Long, end: Long) = flowOf(emptyList<CalendarEvent>())
            override suspend fun getEventById(id: Long): CalendarEvent? = null
            override suspend fun insertEvent(event: CalendarEvent): Long = 1L
            override suspend fun insertEvents(events: List<CalendarEvent>) {}
            override suspend fun updateEvent(event: CalendarEvent) {}
            override suspend fun deleteEvent(event: CalendarEvent) {}
            override suspend fun deleteEventById(id: Long) {}
        }

        val fakeNoteDao = object : NoteDao {
            override fun getAllNotes() = flowOf(emptyList<NoteItem>())
            override suspend fun getNoteById(id: Long): NoteItem? = null
            override suspend fun insertNote(note: NoteItem): Long = 1L
            override suspend fun insertNotes(notes: List<NoteItem>) {}
            override suspend fun updateNote(note: NoteItem) {}
            override suspend fun deleteNote(note: NoteItem) {}
            override suspend fun deleteNoteById(id: Long) {}
            override suspend fun setPinned(id: Long, isPinned: Boolean) {}
        }

        return OmniFlowRepository(fakeTaskDao, fakeCalendarDao, fakeNoteDao)
    }

    @Test
    fun testSubtaskSerializationAndDeserialization() {
        val dummyRepo = createDummyRepository()
        val subtasks = listOf(
            Subtask("st_1", "Draft architecture design", true),
            Subtask("st_2", "Review with team", false)
        )

        val json = dummyRepo.serializeSubtasks(subtasks)
        val deserialized = dummyRepo.parseSubtasks(json)

        assertEquals(2, deserialized.size)
        assertEquals("st_1", deserialized[0].id)
        assertEquals("Draft architecture design", deserialized[0].title)
        assertTrue(deserialized[0].isDone)

        assertEquals("st_2", deserialized[1].id)
        assertEquals("Review with team", deserialized[1].title)
        assertFalse(deserialized[1].isDone)
    }

    @Test
    fun testSmartSuggestionGeneration() {
        val dummyRepo = createDummyRepository()

        val tasks = listOf(
            TaskItem(
                id = 10,
                title = "Critical Client Deck",
                priority = TaskPriority.CRITICAL,
                dueDateEpochMs = System.currentTimeMillis() - 1000, // Overdue
                estimatedMinutes = 45
            ),
            TaskItem(
                id = 11,
                title = "Send Quick Confirmation Email",
                priority = TaskPriority.LOW,
                estimatedMinutes = 10
            )
        )

        val events = listOf(
            CalendarEvent(
                id = 1,
                title = "Sprint Standup",
                startEpochMs = System.currentTimeMillis(),
                endEpochMs = System.currentTimeMillis() + 3600000,
                eventType = EventType.MEETING
            )
        )

        val notes = listOf(
            NoteItem(
                id = 1,
                title = "Sprint Plan",
                content = "- [ ] Implement offline cache"
            )
        )

        val suggestions = dummyRepo.computeSmartSuggestions(tasks, events, notes)
        assertTrue(suggestions.isNotEmpty())
        assertTrue(suggestions.any { it.type == SuggestionType.DEADLINE_WARNING })
        assertTrue(suggestions.any { it.type == SuggestionType.QUICK_WIN })
        assertTrue(suggestions.any { it.type == SuggestionType.NOTE_TO_ACTION })
    }
}
