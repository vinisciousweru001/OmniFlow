package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.OmniFlowDatabase
import com.example.data.model.CalendarEvent
import com.example.data.model.ConnectedDevice
import com.example.data.model.EventType
import com.example.data.model.NoteItem
import com.example.data.model.SmartSuggestion
import com.example.data.model.Subtask
import com.example.data.model.SuggestionType
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.remote.AiAssistantService
import com.example.data.repository.OmniFlowRepository
import com.example.util.DownloadExportManager
import com.example.util.HandsFreeVoiceManager
import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class NavigationTab(val label: String) {
    TODAY("Today"),
    TASKS("Tasks"),
    CALENDAR("Calendar"),
    NOTES("Notes"),
    SYNC_AI("Sync & AI")
}

enum class TaskFilter(val label: String) {
    ALL("All"),
    URGENT("Urgent & Due"),
    WORK("Work"),
    PERSONAL("Personal"),
    COMPLETED("Done")
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class OmniFlowViewModel(application: Application) : AndroidViewModel(application) {
    private val database = OmniFlowDatabase.getDatabase(application)
    val repository = OmniFlowRepository(database.taskDao(), database.calendarDao(), database.noteDao())
    val aiService = AiAssistantService()

    private val _currentTab = MutableStateFlow(NavigationTab.TODAY)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery: StateFlow<String> = _taskSearchQuery.asStateFlow()

    private val _noteSearchQuery = MutableStateFlow("")
    val noteSearchQuery: StateFlow<String> = _noteSearchQuery.asStateFlow()

    private val _dailyBriefing = MutableStateFlow<String?>(null)
    val dailyBriefing: StateFlow<String?> = _dailyBriefing.asStateFlow()

    private val _isBriefingLoading = MutableStateFlow(false)
    val isBriefingLoading: StateFlow<Boolean> = _isBriefingLoading.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "assistant",
                text = "👋 Hello! I am OmniFlow, your personal productivity co-pilot. I analyze your tasks, calendar commitments, and notes across all paired devices to ensure you hit deadlines without stress. How can I help optimize your schedule today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatResponding = MutableStateFlow(false)
    val isChatResponding: StateFlow<Boolean> = _isChatResponding.asStateFlow()

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    // Hands-Free Voice Assistant
    val voiceManager = HandsFreeVoiceManager(application)
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val isListening: StateFlow<Boolean> = voiceManager.isListening
    val voiceSoundLevel: StateFlow<Float> = voiceManager.soundLevel
    val voiceStatusMessage: StateFlow<String> = voiceManager.statusMessage
    val spokenTranscription: StateFlow<String> = voiceManager.transcription
    val spokenResponseText: StateFlow<String> = voiceManager.spokenResponseText

    private val _isHandsFreeModalOpen = MutableStateFlow(false)
    val isHandsFreeModalOpen: StateFlow<Boolean> = _isHandsFreeModalOpen.asStateFlow()

    private val _showDownloadDialog = MutableStateFlow(false)
    val showDownloadDialog: StateFlow<Boolean> = _showDownloadDialog.asStateFlow()

    // Data streams
    val allTasks: StateFlow<List<TaskItem>> = repository.allTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val pendingTasks: StateFlow<List<TaskItem>> = repository.pendingTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allEvents: StateFlow<List<CalendarEvent>> = repository.allEvents.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allNotes: StateFlow<List<NoteItem>> = repository.allNotes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val connectedDevices: StateFlow<List<ConnectedDevice>> = repository.connectedDevices
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val lastSyncTimestamp: StateFlow<Long> = repository.lastSyncTimestamp

    // Dynamic Filtered Tasks
    val filteredTasks: StateFlow<List<TaskItem>> = combine(
        allTasks,
        _taskFilter,
        _taskSearchQuery
    ) { tasks, filter, query ->
        var result = tasks
        if (query.isNotBlank()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
            }
        }
        when (filter) {
            TaskFilter.ALL -> result.filter { !it.isCompleted }
            TaskFilter.URGENT -> result.filter { !it.isCompleted && (it.priority == TaskPriority.CRITICAL || it.priority == TaskPriority.HIGH || it.isDueToday || it.isOverdue) }
            TaskFilter.WORK -> result.filter { !it.isCompleted && (it.category == TaskCategory.WORK || it.category == TaskCategory.DEEP_WORK) }
            TaskFilter.PERSONAL -> result.filter { !it.isCompleted && (it.category == TaskCategory.PERSONAL || it.category == TaskCategory.ADMIN) }
            TaskFilter.COMPLETED -> result.filter { it.isCompleted }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Filtered Notes
    val filteredNotes: StateFlow<List<NoteItem>> = combine(
        allNotes,
        _noteSearchQuery
    ) { notes, query ->
        if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic Smart Suggestions
    val smartSuggestions: StateFlow<List<SmartSuggestion>> = combine(
        allTasks,
        allEvents,
        allNotes
    ) { tasks, events, notes ->
        repository.computeSmartSuggestions(tasks, events, notes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load initial daily briefing
        refreshDailyBriefing()
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setTaskFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun setTaskSearchQuery(query: String) {
        _taskSearchQuery.value = query
    }

    fun setNoteSearchQuery(query: String) {
        _noteSearchQuery.value = query
    }

    fun refreshDailyBriefing() {
        viewModelScope.launch {
            _isBriefingLoading.value = true
            val tasks = pendingTasks.value
            val events = allEvents.value
            val briefing = aiService.generateDailyBriefing(tasks, events)
            _dailyBriefing.value = briefing
            _isBriefingLoading.value = false
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.triggerCrossDeviceSync()
            _snackBarMessage.emit("✓ All devices synchronized (Mobile & Desktop)")
        }
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            val newCompleted = !task.isCompleted
            repository.setTaskCompleted(task.id, newCompleted)
            val msg = if (newCompleted) "Completed: ${task.title} 🎉" else "Reopened: ${task.title}"
            _snackBarMessage.emit(msg)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _snackBarMessage.emit("Deleted: ${task.title}")
        }
    }

    fun saveTask(
        id: Long = 0,
        title: String,
        notes: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueDateEpochMs: Long?,
        estimatedMinutes: Int,
        subtasks: List<Subtask> = emptyList()
    ) {
        viewModelScope.launch {
            val subtasksJson = repository.serializeSubtasks(subtasks)
            val score = calculatePriorityScore(priority, dueDateEpochMs, estimatedMinutes)
            val task = TaskItem(
                id = id,
                title = title.trim(),
                notes = notes.trim(),
                priority = priority,
                category = category,
                dueDateEpochMs = dueDateEpochMs,
                estimatedMinutes = estimatedMinutes,
                subtasksJson = subtasksJson,
                aiPriorityScore = score,
                lastModifiedEpochMs = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertTask(task)
                _snackBarMessage.emit("Task created & synced across devices")
            } else {
                repository.updateTask(task)
                _snackBarMessage.emit("Task updated")
            }
        }
    }

    fun quickAddNaturalLanguageTask(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            val parsed = aiService.parseNaturalLanguageTask(input)
            val score = calculatePriorityScore(parsed.priority, parsed.dueDateEpochMs, parsed.estimatedMinutes)
            val task = TaskItem(
                title = parsed.title,
                notes = parsed.notes,
                priority = parsed.priority,
                category = parsed.category,
                dueDateEpochMs = parsed.dueDateEpochMs,
                estimatedMinutes = parsed.estimatedMinutes,
                aiPriorityScore = score,
                lastModifiedEpochMs = System.currentTimeMillis()
            )
            repository.insertTask(task)
            _snackBarMessage.emit("AI parsed task: '${parsed.title}' [${parsed.priority.label}]")
        }
    }

    fun toggleSubtask(task: TaskItem, subtaskId: String) {
        viewModelScope.launch {
            val current = repository.parseSubtasks(task.subtasksJson)
            val updated = current.map {
                if (it.id == subtaskId) it.copy(isDone = !it.isDone) else it
            }
            repository.updateTask(task.copy(subtasksJson = repository.serializeSubtasks(updated)))
        }
    }

    fun addSubtask(task: TaskItem, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val current = repository.parseSubtasks(task.subtasksJson).toMutableList()
            current.add(Subtask(UUID.randomUUID().toString(), title.trim(), false))
            repository.updateTask(task.copy(subtasksJson = repository.serializeSubtasks(current)))
        }
    }

    // Calendar Actions
    fun saveEvent(
        id: Long = 0,
        title: String,
        description: String,
        startEpoch: Long,
        endEpoch: Long,
        location: String,
        eventType: EventType,
        linkedTaskId: Long? = null
    ) {
        viewModelScope.launch {
            val event = CalendarEvent(
                id = id,
                title = title.trim(),
                description = description.trim(),
                startEpochMs = startEpoch,
                endEpochMs = endEpoch,
                locationOrLink = location.trim(),
                colorHex = eventType.defaultColor,
                eventType = eventType,
                linkedTaskId = linkedTaskId,
                lastModifiedEpochMs = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertEvent(event)
                _snackBarMessage.emit("Event scheduled on unified calendar")
            } else {
                repository.updateEvent(event)
                _snackBarMessage.emit("Event updated")
            }
        }
    }

    fun deleteEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            _snackBarMessage.emit("Removed event: ${event.title}")
        }
    }

    fun autoScheduleFocusBlock(task: TaskItem, startEpoch: Long, durationMinutes: Int) {
        viewModelScope.launch {
            val endEpoch = startEpoch + (durationMinutes * 60 * 1000L)
            val event = CalendarEvent(
                title = "🎯 Focus: ${task.title}",
                description = "AI-reserved focus block to complete task deadline.",
                startEpochMs = startEpoch,
                endEpochMs = endEpoch,
                locationOrLink = "OmniFlow Focus Mode",
                colorHex = "#8B5CF6",
                eventType = EventType.FOCUS_BLOCK,
                linkedTaskId = task.id
            )
            repository.insertEvent(event)
            val slotFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(startEpoch))
            repository.updateTask(task.copy(aiSuggestedSlot = "Scheduled at $slotFormat"))
            _snackBarMessage.emit("Scheduled focus block for '${task.title}' at $slotFormat")
        }
    }

    // Notes Actions
    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        tags: String,
        isPinned: Boolean = false
    ) {
        viewModelScope.launch {
            val note = NoteItem(
                id = id,
                title = if (title.isBlank()) "Untitled Note" else title.trim(),
                content = content.trim(),
                tags = tags.trim(),
                isPinned = isPinned,
                lastModifiedEpochMs = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertNote(note)
                _snackBarMessage.emit("Note saved and synced across devices")
            } else {
                repository.updateNote(note)
                _snackBarMessage.emit("Note updated")
            }
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            repository.deleteNote(note)
            _snackBarMessage.emit("Deleted note: ${note.title}")
        }
    }

    fun togglePinNote(note: NoteItem) {
        viewModelScope.launch {
            repository.togglePinNote(note.id, note.isPinned)
        }
    }

    fun extractTasksFromNote(note: NoteItem) {
        viewModelScope.launch {
            val extracted = aiService.extractTasksFromNote(note)
            if (extracted.isEmpty()) {
                _snackBarMessage.emit("No action items found in note.")
                return@launch
            }
            var count = 0
            for (p in extracted) {
                val score = calculatePriorityScore(p.priority, p.dueDateEpochMs, p.estimatedMinutes)
                val task = TaskItem(
                    title = p.title,
                    notes = p.notes,
                    priority = p.priority,
                    category = p.category,
                    estimatedMinutes = p.estimatedMinutes,
                    dueDateEpochMs = p.dueDateEpochMs,
                    aiPriorityScore = score
                )
                repository.insertTask(task)
                count++
            }
            repository.updateNote(note.copy(extractedTasksCount = note.extractedTasksCount + count))
            _snackBarMessage.emit("Generated $count actionable tasks from '${note.title}'!")
        }
    }

    // AI Chat Assistant
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        val userMsg = ChatMessage(sender = "user", text = userText.trim())
        currentList.add(userMsg)
        _chatMessages.value = currentList
        _isChatResponding.value = true

        viewModelScope.launch {
            val history = currentList.map { it.sender to it.text }
            val contextSummary = buildWorkspaceSummary()
            val reply = aiService.chatWithAssistant(history, userText, contextSummary)

            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(ChatMessage(sender = "assistant", text = reply))
            _chatMessages.value = updatedList
            _isChatResponding.value = false
        }
    }

    fun handleSuggestionAction(suggestion: SmartSuggestion) {
        viewModelScope.launch {
            when (suggestion.type) {
                SuggestionType.DEADLINE_WARNING, SuggestionType.OPTIMAL_FOCUS_BLOCK -> {
                    val targetId = suggestion.relatedTaskId
                    val task = if (targetId != null) allTasks.value.find { it.id == targetId } else null
                    if (task != null) {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 14)
                        cal.set(Calendar.MINUTE, 0)
                        autoScheduleFocusBlock(task, cal.timeInMillis, 60)
                    } else {
                        _snackBarMessage.emit("Focus block prepared!")
                    }
                }
                SuggestionType.QUICK_WIN -> {
                    suggestion.relatedTaskId?.let {
                        repository.setTaskCompleted(it, true)
                        _snackBarMessage.emit("Quick win completed! Keep up the momentum! 🔥")
                    }
                }
                SuggestionType.NOTE_TO_ACTION -> {
                    val note = allNotes.value.firstOrNull()
                    if (note != null) {
                        extractTasksFromNote(note)
                    }
                }
                SuggestionType.RESCHEDULE_LOW_PRIORITY -> {
                    _snackBarMessage.emit("Optimized priorities for maximum flow")
                }
            }
        }
    }

    // Hands-Free Voice Assistant Controls
    fun openHandsFreeModal() {
        _isHandsFreeModalOpen.value = true
        startVoiceListening()
    }

    fun closeHandsFreeModal() {
        _isHandsFreeModalOpen.value = false
        stopVoiceListening()
        stopSpeaking()
    }

    fun toggleHandsFreeModal() {
        if (_isHandsFreeModalOpen.value) {
            closeHandsFreeModal()
        } else {
            openHandsFreeModal()
        }
    }

    fun startVoiceListening() {
        voiceManager.startListening { spoken ->
            executeVoiceCommand(spoken)
        }
    }

    fun stopVoiceListening() {
        voiceManager.stopListening()
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    fun readDailyBriefingAloud() {
        viewModelScope.launch {
            val current = _dailyBriefing.value
            if (current != null) {
                voiceManager.speak(current)
            } else {
                _isBriefingLoading.value = true
                val generated = aiService.generateDailyBriefing(
                    pendingTasks = pendingTasks.value,
                    todayEvents = allEvents.value
                )
                _dailyBriefing.value = generated
                _isBriefingLoading.value = false
                voiceManager.speak(generated)
            }
        }
    }

    fun readUrgentTasksAloud() {
        val pending = pendingTasks.value
        if (pending.isEmpty()) {
            voiceManager.speak("You have no pending tasks. Your workspace is completely clear!")
            return
        }
        val top = pending.take(3)
        val sb = StringBuilder()
        sb.append("You have ${pending.size} pending tasks. ")
        top.forEachIndexed { i, t ->
            sb.append("Priority ${i + 1}: ${t.title}. ")
        }
        sb.append("Say 'Schedule focus' to block time for your top priority.")
        voiceManager.speak(sb.toString())
    }

    fun readUpcomingScheduleAloud() {
        val now = System.currentTimeMillis()
        val upcoming = allEvents.value.filter { it.endEpochMs >= now }.sortedBy { it.startEpochMs }
        if (upcoming.isEmpty()) {
            voiceManager.speak("You have no more calendar events today. Excellent window for deep work.")
        } else {
            val first = upcoming.first()
            val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(first.startEpochMs))
            voiceManager.speak("Your next event is ${first.title} at $timeStr. You have ${upcoming.size} scheduled commitments.")
        }
    }

    fun executeVoiceCommand(command: String) {
        if (command.isBlank()) return
        val lower = command.trim().lowercase(Locale.ROOT)
        viewModelScope.launch {
            when {
                lower.contains("briefing") || lower.contains("brief me") || lower.contains("agenda") || lower.contains("my day") -> {
                    readDailyBriefingAloud()
                }
                lower.contains("task") && (lower.contains("what") || lower.contains("list") || lower.contains("read") || lower.contains("urgent") || lower.contains("pending")) -> {
                    readUrgentTasksAloud()
                }
                lower.startsWith("add task") || lower.startsWith("new task") || lower.startsWith("create task") || lower.startsWith("remind me to") -> {
                    val raw = command.trim()
                        .replaceFirst("(?i)^(add task|new task|create task|remind me to)\\s*".toRegex(), "")
                    if (raw.isNotBlank()) {
                        quickAddNaturalLanguageTask(raw)
                        voiceManager.speak("Added task: $raw to your priority backlog.")
                    } else {
                        voiceManager.speak("What task would you like me to add?")
                    }
                }
                lower.startsWith("complete task") || (lower.startsWith("mark") && (lower.contains("done") || lower.contains("complete"))) || lower.startsWith("finish") -> {
                    val targetText = lower
                        .replace("complete task", "")
                        .replace("mark", "")
                        .replace("as done", "")
                        .replace("as complete", "")
                        .replace("done", "")
                        .replace("task", "")
                        .trim()
                    val match = pendingTasks.value.find { it.title.lowercase(Locale.ROOT).contains(targetText) }
                    if (match != null) {
                        toggleTaskCompletion(match)
                        voiceManager.speak("Marked ${match.title} as completed. Excellent work!")
                    } else {
                        voiceManager.speak("I couldn't locate an open task matching that description.")
                    }
                }
                lower.contains("next meeting") || lower.contains("schedule") || lower.contains("calendar") || lower.contains("events") -> {
                    readUpcomingScheduleAloud()
                }
                lower.contains("focus") || lower.contains("deep work") -> {
                    val topTask = pendingTasks.value.firstOrNull()
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.HOUR_OF_DAY, 1)
                    cal.set(Calendar.MINUTE, 0)
                    if (topTask != null) {
                        autoScheduleFocusBlock(topTask, cal.timeInMillis, 60)
                        voiceManager.speak("Scheduled a 60-minute deep focus block for ${topTask.title}.")
                    } else {
                        saveEvent(
                            title = "Deep Focus Sprint",
                            description = "Distraction-free flow session",
                            startEpoch = cal.timeInMillis,
                            endEpoch = cal.timeInMillis + 3600000L,
                            location = "Focus Zone",
                            eventType = EventType.FOCUS_BLOCK
                        )
                        voiceManager.speak("Scheduled a 60-minute Deep Focus block on your calendar.")
                    }
                }
                lower.contains("sync") -> {
                    triggerSync()
                    voiceManager.speak("Sync complete. All 4 paired mobile and desktop devices are aligned.")
                }
                lower.contains("download") || lower.contains("export") || lower.contains("apk") -> {
                    _showDownloadDialog.value = true
                    voiceManager.speak("Opening the Download and Export center.")
                }
                else -> {
                    // Conversational assistant fallback with Gemini
                    val currentList = _chatMessages.value.toMutableList()
                    currentList.add(ChatMessage(sender = "user", text = command))
                    _chatMessages.value = currentList
                    _isChatResponding.value = true

                    val history = currentList.map { it.sender to it.text }
                    val contextSummary = buildWorkspaceSummary()
                    val reply = aiService.chatWithAssistant(history, command, contextSummary)

                    val updatedList = _chatMessages.value.toMutableList()
                    updatedList.add(ChatMessage(sender = "assistant", text = reply))
                    _chatMessages.value = updatedList
                    _isChatResponding.value = false

                    voiceManager.speak(reply)
                }
            }
        }
    }

    // Download & Export Controls
    fun openDownloadDialog() {
        _showDownloadDialog.value = true
    }

    fun closeDownloadDialog() {
        _showDownloadDialog.value = false
    }

    fun downloadApk(context: Context) {
        val (success, msg) = DownloadExportManager.exportAndDownloadApk(context)
        viewModelScope.launch {
            _snackBarMessage.emit(msg)
        }
    }

    fun downloadBackupJson(context: Context) {
        val (success, msg) = DownloadExportManager.exportAndDownloadDataJson(
            context = context,
            tasks = allTasks.value,
            events = allEvents.value,
            notes = allNotes.value,
            devices = connectedDevices.value
        )
        viewModelScope.launch {
            _snackBarMessage.emit(msg)
        }
    }

    fun downloadCalendarIcs(context: Context) {
        val (success, msg) = DownloadExportManager.exportAndDownloadIcsCalendar(
            context = context,
            events = allEvents.value
        )
        viewModelScope.launch {
            _snackBarMessage.emit(msg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }

    private fun calculatePriorityScore(priority: TaskPriority, dueEpoch: Long?, estimatedMinutes: Int): Int {
        var score = when (priority) {
            TaskPriority.CRITICAL -> 90
            TaskPriority.HIGH -> 75
            TaskPriority.MEDIUM -> 50
            TaskPriority.LOW -> 25
        }
        if (dueEpoch != null) {
            val diffHours = (dueEpoch - System.currentTimeMillis()) / (3600 * 1000L)
            if (diffHours < 0) {
                score += 15 // Overdue
            } else if (diffHours < 24) {
                score += 10 // Due within 24h
            } else if (diffHours < 48) {
                score += 5
            }
        }
        return score.coerceIn(1, 99)
    }

    private fun buildWorkspaceSummary(): String {
        val pending = pendingTasks.value
        val events = allEvents.value
        val devices = connectedDevices.value
        return "Pending Tasks: ${pending.size} (Top: ${pending.firstOrNull()?.title ?: "None"}). Today Events: ${events.size}. Paired Devices: ${devices.size} all online."
    }
}
