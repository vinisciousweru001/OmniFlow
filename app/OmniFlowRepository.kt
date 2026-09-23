package com.example.data.repository

import com.example.data.local.CalendarDao
import com.example.data.local.NoteDao
import com.example.data.local.TaskDao
import com.example.data.model.CalendarEvent
import com.example.data.model.ConnectedDevice
import com.example.data.model.DeviceType
import com.example.data.model.EventType
import com.example.data.model.NoteItem
import com.example.data.model.SmartSuggestion
import com.example.data.model.Subtask
import com.example.data.model.SuggestionType
import com.example.data.model.SyncState
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

class OmniFlowRepository(
    private val taskDao: TaskDao,
    private val calendarDao: CalendarDao,
    private val noteDao: NoteDao
) {
    private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(getDefaultDevices())
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskItem>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<TaskItem>> = taskDao.getCompletedTasks()

    val allEvents: Flow<List<CalendarEvent>> = calendarDao.getAllEvents()
    val allNotes: Flow<List<NoteItem>> = noteDao.getAllNotes()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkAndSeedInitialData()
        }
    }

    private suspend fun checkAndSeedInitialData() {
        val existingTasks = taskDao.getAllTasks().first()
        if (existingTasks.isEmpty()) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            // Task 1: Overdue/Due Today High Priority
            cal.timeInMillis = now
            cal.set(Calendar.HOUR_OF_DAY, 17)
            cal.set(Calendar.MINUTE, 0)
            val today5pm = cal.timeInMillis

            cal.add(Calendar.DAY_OF_YEAR, 1)
            cal.set(Calendar.HOUR_OF_DAY, 12)
            val tomorrowNoon = cal.timeInMillis

            cal.add(Calendar.DAY_OF_YEAR, 2)
            val threeDaysLater = cal.timeInMillis

            val sampleTasks = listOf(
                TaskItem(
                    title = "Deliver Q3 Architecture & Cross-Platform Roadmap",
                    notes = "Include unified data schemas, cloud sync architecture, and mobile-desktop UX parity.",
                    priority = TaskPriority.CRITICAL,
                    category = TaskCategory.DEEP_WORK,
                    dueDateEpochMs = today5pm,
                    estimatedMinutes = 60,
                    isCompleted = false,
                    subtasksJson = serializeSubtasks(listOf(
                        Subtask(UUID.randomUUID().toString(), "Review cross-device sync latency targets", true),
                        Subtask(UUID.randomUUID().toString(), "Draft offline conflict resolution spec", false),
                        Subtask(UUID.randomUUID().toString(), "Send deck to team for async feedback", false)
                    )),
                    aiPriorityScore = 96,
                    aiSuggestedSlot = "Today 2:00 PM - 3:00 PM"
                ),
                TaskItem(
                    title = "Review Security & Auth Token Refresh Flow",
                    notes = "Ensure desktop and mobile share seamless session handoff with zero re-logins.",
                    priority = TaskPriority.HIGH,
                    category = TaskCategory.WORK,
                    dueDateEpochMs = tomorrowNoon,
                    estimatedMinutes = 45,
                    isCompleted = false,
                    subtasksJson = serializeSubtasks(listOf(
                        Subtask(UUID.randomUUID().toString(), "Verify biometric prompt on mobile", true),
                        Subtask(UUID.randomUUID().toString(), "Inspect token expiry on macOS app", false)
                    )),
                    aiPriorityScore = 88,
                    aiSuggestedSlot = "Tomorrow 10:30 AM"
                ),
                TaskItem(
                    title = "Prepare Weekly Executive Briefing",
                    notes = "Highlight major velocity milestones, blocker resolutions, and productivity metrics.",
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.ADMIN,
                    dueDateEpochMs = tomorrowNoon + (4 * 3600 * 1000L),
                    estimatedMinutes = 30,
                    isCompleted = false,
                    subtasksJson = serializeSubtasks(listOf(
                        Subtask(UUID.randomUUID().toString(), "Pull stats from calendar and completed tasks", false)
                    )),
                    aiPriorityScore = 72
                ),
                TaskItem(
                    title = "Research Deep Work & Attention Science Papers",
                    notes = "Investigate the impact of scheduled focus blocks vs scattered multitasking.",
                    priority = TaskPriority.LOW,
                    category = TaskCategory.LEARNING,
                    dueDateEpochMs = threeDaysLater,
                    estimatedMinutes = 40,
                    isCompleted = false,
                    aiPriorityScore = 45
                ),
                TaskItem(
                    title = "Morning Coffee & Inbox Triage",
                    notes = "Cleared all zero-inbox incoming notifications.",
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.PERSONAL,
                    dueDateEpochMs = now - (3 * 3600 * 1000L),
                    estimatedMinutes = 15,
                    isCompleted = true,
                    completedAtEpochMs = now - (2 * 3600 * 1000L),
                    aiPriorityScore = 50
                )
            )
            taskDao.insertTasks(sampleTasks)

            // Seed Calendar Events
            val calEvent = Calendar.getInstance()
            calEvent.timeInMillis = now
            calEvent.set(Calendar.HOUR_OF_DAY, 10)
            calEvent.set(Calendar.MINUTE, 0)
            val meetStart = calEvent.timeInMillis
            calEvent.set(Calendar.HOUR_OF_DAY, 11)
            val meetEnd = calEvent.timeInMillis

            calEvent.set(Calendar.HOUR_OF_DAY, 14)
            val focusStart = calEvent.timeInMillis
            calEvent.set(Calendar.HOUR_OF_DAY, 15)
            calEvent.set(Calendar.MINUTE, 30)
            val focusEnd = calEvent.timeInMillis

            val sampleEvents = listOf(
                CalendarEvent(
                    title = "Productivity Strategy & Sync Standup",
                    description = "Virtual meeting with engineering and design leads.",
                    startEpochMs = meetStart,
                    endEpochMs = meetEnd,
                    locationOrLink = "Google Meet: meet.google.com/omn-flow",
                    colorHex = "#6366F1",
                    eventType = EventType.MEETING
                ),
                CalendarEvent(
                    title = "Deep Focus: Roadmap Finalization",
                    description = "Protected deep-work block recommended by OmniFlow AI.",
                    startEpochMs = focusStart,
                    endEpochMs = focusEnd,
                    locationOrLink = "Desk / Do Not Disturb",
                    colorHex = "#8B5CF6",
                    eventType = EventType.FOCUS_BLOCK
                )
            )
            calendarDao.insertEvents(sampleEvents)

            // Seed Notes
            val sampleNotes = listOf(
                NoteItem(
                    title = "Cross-Platform Sync Design Principles",
                    content = """
                        Key guidelines for seamless productivity:
                        - Instant local mutations with optimistic UI
                        - Automated background sync across mobile, laptop, and desktop
                        - Clear prioritization: deadline proximity + task impact
                        - Smart suggestions for open calendar gaps
                        - Effortless capture: Convert raw ideas directly into tasks
                    """.trimIndent(),
                    tags = "Architecture, Product, Ideas",
                    isPinned = true,
                    colorHex = "#1E293B",
                    aiSummary = "Outlines instant local responsiveness, background multi-device synchronization, and automated task prioritization principles."
                ),
                NoteItem(
                    title = "Meeting Notes: Team Priority Review",
                    content = """
                        Action Items from morning sync:
                        - [ ] Finalize benchmark tests for local database speed
                        - [ ] Draft client documentation for desktop pairing
                        - [ ] Setup notifications for 1-hour deadline warnings
                    """.trimIndent(),
                    tags = "Meeting, ActionItems",
                    isPinned = false,
                    colorHex = "#1E293B",
                    aiSummary = "Contains 3 direct action items regarding benchmark tests, desktop pairing, and deadline notifications."
                )
            )
            noteDao.insertNotes(sampleNotes)
        }
    }

    private fun getDefaultDevices(): List<ConnectedDevice> {
        val now = System.currentTimeMillis()
        return listOf(
            ConnectedDevice(
                id = "device_android_primary",
                name = "Pixel 9 Pro (This Device)",
                type = DeviceType.MOBILE,
                osDetails = "Android 15 • OmniFlow Mobile",
                lastSyncTimeEpochMs = now,
                syncState = SyncState.ONLINE,
                isCurrentDevice = true,
                ipAddress = "192.168.1.105"
            ),
            ConnectedDevice(
                id = "device_macbook_pro",
                name = "MacBook Pro 16\" (M3 Max)",
                type = DeviceType.LAPTOP,
                osDetails = "macOS Sequoia • OmniFlow Desktop",
                lastSyncTimeEpochMs = now - (4 * 60 * 1000L),
                syncState = SyncState.ONLINE,
                isCurrentDevice = false,
                ipAddress = "192.168.1.142"
            ),
            ConnectedDevice(
                id = "device_pc_workstation",
                name = "Workstation PC",
                type = DeviceType.DESKTOP,
                osDetails = "Windows 11 • OmniFlow Web/Desktop",
                lastSyncTimeEpochMs = now - (12 * 60 * 1000L),
                syncState = SyncState.ONLINE,
                isCurrentDevice = false,
                ipAddress = "192.168.1.189"
            ),
            ConnectedDevice(
                id = "device_tablet_ipad",
                name = "iPad Pro 13\"",
                type = DeviceType.TABLET,
                osDetails = "iPadOS 18 • OmniFlow Tablet",
                lastSyncTimeEpochMs = now - (35 * 60 * 1000L),
                syncState = SyncState.ONLINE,
                isCurrentDevice = false,
                ipAddress = "192.168.1.177"
            )
        )
    }

    suspend fun triggerCrossDeviceSync() {
        _isSyncing.value = true

        // Simulate multi-phase sync handshake across all mobile & desktop clients
        _connectedDevices.value = _connectedDevices.value.map {
            it.copy(syncState = SyncState.SYNCING)
        }

        delay(900) // realistic network handshake delay

        val now = System.currentTimeMillis()
        _lastSyncTimestamp.value = now

        _connectedDevices.value = _connectedDevices.value.map {
            it.copy(
                syncState = SyncState.ONLINE,
                lastSyncTimeEpochMs = now
            )
        }

        _isSyncing.value = false
    }

    fun computeSmartSuggestions(
        tasks: List<TaskItem>,
        events: List<CalendarEvent>,
        notes: List<NoteItem>
    ): List<SmartSuggestion> {
        val suggestions = mutableListOf<SmartSuggestion>()
        val pending = tasks.filter { !it.isCompleted }

        // 1. Deadline-driven suggestion
        val overdueOrToday = pending.filter { it.isOverdue || it.isDueToday }
        if (overdueOrToday.isNotEmpty()) {
            val topUrgent = overdueOrToday.maxByOrNull { it.priority.level } ?: overdueOrToday.first()
            suggestions.add(
                SmartSuggestion(
                    id = "sug_deadline_${topUrgent.id}",
                    title = "Deadline Alert: ${topUrgent.title}",
                    subtitle = "Due today. OmniFlow recommends reserving your next 45 minutes to complete this.",
                    type = SuggestionType.DEADLINE_WARNING,
                    actionText = "Focus Now",
                    relatedTaskId = topUrgent.id,
                    urgencyScore = 95
                )
            )
        }

        // 2. Schedule focus gap suggestion
        if (events.isNotEmpty() && pending.isNotEmpty()) {
            val highTask = pending.firstOrNull { it.priority == TaskPriority.HIGH || it.priority == TaskPriority.CRITICAL }
            val taskTitle = highTask?.title ?: pending.first().title
            suggestions.add(
                SmartSuggestion(
                    id = "sug_gap_1",
                    title = "Optimal Deep Focus Window",
                    subtitle = "Found a 90-minute open calendar slot this afternoon. Auto-block it for: '$taskTitle'?",
                    type = SuggestionType.OPTIMAL_FOCUS_BLOCK,
                    actionText = "Auto-Schedule",
                    relatedTaskId = highTask?.id,
                    suggestedSlot = "2:00 PM – 3:30 PM",
                    urgencyScore = 88
                )
            )
        }

        // 3. Quick-win suggestion
        val quickTask = pending.firstOrNull { it.estimatedMinutes <= 20 }
        if (quickTask != null) {
            suggestions.add(
                SmartSuggestion(
                    id = "sug_quick_${quickTask.id}",
                    title = "Quick Win: ${quickTask.title}",
                    subtitle = "Takes only ${quickTask.estimatedMinutes} mins. Completing this now will boost your momentum score.",
                    type = SuggestionType.QUICK_WIN,
                    actionText = "Complete Task",
                    relatedTaskId = quickTask.id,
                    urgencyScore = 75
                )
            )
        }

        // 4. Notes to Action items suggestion
        val unextractedNote = notes.firstOrNull { it.content.contains("- [ ]") || it.content.contains("Action Items") }
        if (unextractedNote != null) {
            suggestions.add(
                SmartSuggestion(
                    id = "sug_note_${unextractedNote.id}",
                    title = "Extract Action Items from Note",
                    subtitle = "Note '${unextractedNote.title}' contains checklist items not yet tracked in your unified task list.",
                    type = SuggestionType.NOTE_TO_ACTION,
                    actionText = "Extract Tasks",
                    urgencyScore = 80
                )
            )
        }

        return suggestions
    }

    // Task CRUD
    suspend fun insertTask(task: TaskItem): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = taskDao.deleteTask(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(id, completed, completedAt)
    }

    // Calendar CRUD
    suspend fun insertEvent(event: CalendarEvent): Long = calendarDao.insertEvent(event)
    suspend fun updateEvent(event: CalendarEvent) = calendarDao.updateEvent(event)
    suspend fun deleteEvent(event: CalendarEvent) = calendarDao.deleteEvent(event)

    // Notes CRUD
    suspend fun insertNote(note: NoteItem): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteItem) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteItem) = noteDao.deleteNote(note)
    suspend fun togglePinNote(id: Long, isPinned: Boolean) = noteDao.setPinned(id, !isPinned)

    // Subtask parsing helpers
    fun parseSubtasks(json: String): List<Subtask> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<Subtask>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Subtask(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        isDone = obj.optBoolean("isDone", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeSubtasks(subtasks: List<Subtask>): String {
        val arr = JSONArray()
        for (st in subtasks) {
            val obj = JSONObject().apply {
                put("id", st.id)
                put("title", st.title)
                put("isDone", st.isDone)
            }
            arr.put(obj)
        }
        return arr.toString()
    }
}
