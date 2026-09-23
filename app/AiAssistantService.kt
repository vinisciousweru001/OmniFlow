package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.CalendarEvent
import com.example.data.model.NoteItem
import com.example.data.model.SmartSuggestion
import com.example.data.model.SuggestionType
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class AiAssistantService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun isKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("YOUR_")
    }

    suspend fun generateDailyBriefing(
        pendingTasks: List<TaskItem>,
        todayEvents: List<CalendarEvent>
    ): String = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            return@withContext generateLocalBriefing(pendingTasks, todayEvents)
        }

        try {
            val taskListDesc = pendingTasks.take(8).joinToString("\n") {
                "- [${it.priority.name}] ${it.title} (Est: ${it.estimatedMinutes}m, Due: ${formatDate(it.dueDateEpochMs)})"
            }
            val eventListDesc = todayEvents.joinToString("\n") {
                "- ${it.title} (${formatTime(it.startEpochMs)} - ${formatTime(it.endEpochMs)})"
            }

            val prompt = """
                You are OmniFlow, an executive AI personal productivity assistant.
                Analyze the user's workload for today and produce a concise, inspiring executive daily briefing.
                Include:
                1. Immediate Top Priority (one clear focus)
                2. Optimal Time-Blocking Strategy around their meetings
                3. Proactive advice on managing deadlines and preventing burnout.
                
                Current Schedule:
                Meetings/Events:
                $eventListDesc
                
                Pending Tasks:
                $taskListDesc
                
                Keep your tone encouraging, punchy, and clear. Format with bold headers and bullet points. Max 150 words.
            """.trimIndent()

            val response = callGeminiRaw(prompt)
            if (response.isNotBlank()) response else generateLocalBriefing(pendingTasks, todayEvents)
        } catch (e: Exception) {
            Log.e("AiAssistantService", "Gemini API error, falling back to local reasoning", e)
            generateLocalBriefing(pendingTasks, todayEvents)
        }
    }

    suspend fun parseNaturalLanguageTask(input: String): ParsedTaskResult = withContext(Dispatchers.IO) {
        if (input.isBlank()) return@withContext ParsedTaskResult(title = "New Task")

        if (isKeyConfigured()) {
            try {
                val prompt = """
                    Parse this natural language task into structured JSON:
                    "$input"
                    
                    Respond strictly in valid JSON format with keys:
                    - "title": cleaned concise task title
                    - "priority": one of "CRITICAL", "HIGH", "MEDIUM", "LOW"
                    - "category": one of "WORK", "PERSONAL", "DEEP_WORK", "LEARNING", "ADMIN"
                    - "estimatedMinutes": integer minutes
                    - "dueInHours": integer offset from now (e.g. 4 for today, 24 for tomorrow, 72 for 3 days), or null
                    - "notes": extra details mentioned or empty
                """.trimIndent()

                val rawJson = callGeminiRaw(prompt)
                val cleanJson = extractJsonPayload(rawJson)
                if (cleanJson.isNotBlank()) {
                    val obj = JSONObject(cleanJson)
                    val title = obj.optString("title", input)
                    val priorityStr = obj.optString("priority", "MEDIUM")
                    val categoryStr = obj.optString("category", "WORK")
                    val minutes = obj.optInt("estimatedMinutes", 30)
                    val dueInHours = if (obj.has("dueInHours") && !obj.isNull("dueInHours")) obj.optLong("dueInHours") else null
                    val notes = obj.optString("notes", "")

                    val priority = try { TaskPriority.valueOf(priorityStr) } catch (_: Exception) { TaskPriority.MEDIUM }
                    val category = try { TaskCategory.valueOf(categoryStr) } catch (_: Exception) { TaskCategory.WORK }
                    val dueEpoch = dueInHours?.let { System.currentTimeMillis() + (it * 3600 * 1000L) }

                    return@withContext ParsedTaskResult(
                        title = title,
                        priority = priority,
                        category = category,
                        estimatedMinutes = minutes,
                        dueDateEpochMs = dueEpoch,
                        notes = notes
                    )
                }
            } catch (e: Exception) {
                Log.w("AiAssistantService", "NLP parsing fallback", e)
            }
        }

        // Local Smart Heuristic Parser
        var title = input.trim()
        var priority = TaskPriority.MEDIUM
        var category = TaskCategory.WORK
        var minutes = 30
        var dueEpoch: Long? = null

        val lower = input.lowercase()
        if (lower.contains("urgent") || lower.contains("asap") || lower.contains("critical")) {
            priority = TaskPriority.CRITICAL
        } else if (lower.contains("high priority") || lower.contains("important")) {
            priority = TaskPriority.HIGH
        } else if (lower.contains("low priority") || lower.contains("someday")) {
            priority = TaskPriority.LOW
        }

        if (lower.contains("today")) {
            dueEpoch = System.currentTimeMillis() + (4 * 3600 * 1000L)
        } else if (lower.contains("tomorrow")) {
            dueEpoch = System.currentTimeMillis() + (24 * 3600 * 1000L)
        } else if (lower.contains("friday") || lower.contains("end of week")) {
            dueEpoch = System.currentTimeMillis() + (48 * 3600 * 1000L)
        }

        if (lower.contains("personal") || lower.contains("buy") || lower.contains("grocer") || lower.contains("doctor")) {
            category = TaskCategory.PERSONAL
        } else if (lower.contains("study") || lower.contains("read") || lower.contains("learn")) {
            category = TaskCategory.LEARNING
        } else if (lower.contains("project") || lower.contains("deep work") || lower.contains("code")) {
            category = TaskCategory.DEEP_WORK
        }

        // Clean out buzzwords from title
        val cleanedTitle = title
            .replace(Regex("(?i)\\b(urgent|asap|critical|high priority|today|tomorrow|by friday)\\b"), "")
            .trim()

        ParsedTaskResult(
            title = if (cleanedTitle.isNotBlank()) cleanedTitle else title,
            priority = priority,
            category = category,
            estimatedMinutes = minutes,
            dueDateEpochMs = dueEpoch,
            notes = "Auto-parsed from quick capture"
        )
    }

    suspend fun extractTasksFromNote(note: NoteItem): List<ParsedTaskResult> = withContext(Dispatchers.IO) {
        if (note.content.isBlank()) return@withContext emptyList()

        if (isKeyConfigured()) {
            try {
                val prompt = """
                    Analyze this note titled "${note.title}":
                    "${note.content}"
                    
                    Extract all actionable tasks into a JSON array of objects with keys:
                    - "title": action-oriented task title
                    - "priority": "HIGH", "MEDIUM", or "LOW"
                    - "estimatedMinutes": number
                    - "category": "WORK" or "PERSONAL" or "DEEP_WORK"
                    Return only the JSON array.
                """.trimIndent()

                val raw = callGeminiRaw(prompt)
                val clean = extractJsonPayload(raw)
                if (clean.isNotBlank()) {
                    val arr = JSONArray(clean)
                    val result = mutableListOf<ParsedTaskResult>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        result.add(
                            ParsedTaskResult(
                                title = obj.optString("title", "Action item"),
                                priority = try { TaskPriority.valueOf(obj.optString("priority", "MEDIUM")) } catch (_: Exception) { TaskPriority.MEDIUM },
                                category = try { TaskCategory.valueOf(obj.optString("category", "WORK")) } catch (_: Exception) { TaskCategory.WORK },
                                estimatedMinutes = obj.optInt("estimatedMinutes", 30),
                                notes = "Extracted from note: ${note.title}"
                            )
                        )
                    }
                    if (result.isNotEmpty()) return@withContext result
                }
            } catch (e: Exception) {
                Log.w("AiAssistantService", "Extract tasks failed, using local extraction", e)
            }
        }

        // Local line-based extractor for bullet points, checkboxes or action words
        val lines = note.content.lines().map { it.trim() }.filter { it.isNotBlank() }
        val extracted = mutableListOf<ParsedTaskResult>()
        for (line in lines) {
            val isBullet = line.startsWith("-") || line.startsWith("*") || line.startsWith("•") || line.startsWith("[ ]")
            val isAction = line.lowercase().let { 
                it.startsWith("need to") || it.startsWith("todo") || it.startsWith("review") || 
                it.startsWith("send") || it.startsWith("prepare") || it.startsWith("schedule") ||
                it.startsWith("finish") || it.startsWith("complete") || it.startsWith("call")
            }

            if (isBullet || isAction) {
                val cleanLine = line.replace(Regex("^[-*•\\[\\]\\s]+"), "").trim()
                if (cleanLine.length > 3) {
                    val priority = if (cleanLine.lowercase().contains("urgent") || cleanLine.lowercase().contains("asap")) 
                        TaskPriority.HIGH else TaskPriority.MEDIUM
                    extracted.add(
                        ParsedTaskResult(
                            title = cleanLine,
                            priority = priority,
                            category = TaskCategory.WORK,
                            estimatedMinutes = 25,
                            notes = "Extracted from note: ${note.title}"
                        )
                    )
                }
            }
        }

        if (extracted.isEmpty() && note.content.length > 10) {
            extracted.add(
                ParsedTaskResult(
                    title = "Review and action: ${note.title}",
                    priority = TaskPriority.MEDIUM,
                    category = TaskCategory.WORK,
                    estimatedMinutes = 20,
                    notes = "Extracted from note: ${note.title}"
                )
            )
        }

        extracted
    }

    suspend fun chatWithAssistant(
        conversationHistory: List<Pair<String, String>>, // (role, text)
        userMessage: String,
        contextSummary: String
    ): String = withContext(Dispatchers.IO) {
        if (!isKeyConfigured()) {
            return@withContext localChatResponse(userMessage, contextSummary)
        }

        try {
            val systemContext = """
                You are OmniFlow AI, an intelligent personal productivity co-pilot running synchronized across mobile and desktop.
                Current Workspace State:
                $contextSummary
                
                Provide sharp, actionable, structured productivity guidance.
                Help the user triage priorities, schedule deep focus blocks, manage deadlines, and stay calm and focused.
            """.trimIndent()

            val fullPrompt = buildString {
                appendLine(systemContext)
                appendLine("\nConversation History:")
                conversationHistory.takeLast(6).forEach { (role, text) ->
                    appendLine("$role: $text")
                }
                appendLine("User: $userMessage")
                appendLine("Assistant:")
            }

            val response = callGeminiRaw(fullPrompt)
            if (response.isNotBlank()) response else localChatResponse(userMessage, contextSummary)
        } catch (e: Exception) {
            Log.e("AiAssistantService", "Chat error", e)
            localChatResponse(userMessage, contextSummary)
        }
    }

    private suspend fun callGeminiRaw(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val rootJson = JSONObject().apply {
            val contentsArr = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArr = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", partsArr)
                }
                put(contentObj)
            }
            put("contents", contentsArr)
        }

        val body = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.w("AiAssistantService", "API responded with code ${response.code}: $errBody")
                return ""
            }
            val resString = response.body?.string() ?: return ""
            val resObj = JSONObject(resString)
            val candidates = resObj.optJSONArray("candidates") ?: return ""
            if (candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
            return ""
        }
    }

    private fun extractJsonPayload(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.removePrefix("```json")
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```")
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.removeSuffix("```")
        }
        trimmed = trimmed.trim()
        val firstBrace = trimmed.indexOf('{')
        val firstBracket = trimmed.indexOf('[')

        if (firstBrace != -1 && (firstBracket == -1 || firstBrace < firstBracket)) {
            val lastBrace = trimmed.lastIndexOf('}')
            if (lastBrace > firstBrace) return trimmed.substring(firstBrace, lastBrace + 1)
        } else if (firstBracket != -1) {
            val lastBracket = trimmed.lastIndexOf(']')
            if (lastBracket > firstBracket) return trimmed.substring(firstBracket, lastBracket + 1)
        }
        return trimmed
    }

    private fun generateLocalBriefing(pendingTasks: List<TaskItem>, todayEvents: List<CalendarEvent>): String {
        val criticalTasks = pendingTasks.filter { it.priority == TaskPriority.CRITICAL }
        val highTasks = pendingTasks.filter { it.priority == TaskPriority.HIGH }
        val dueToday = pendingTasks.filter { it.isDueToday || it.isOverdue }

        return buildString {
            append("☀️ **Good Morning! Here is your OmniFlow Executive Plan:**\n\n")
            if (dueToday.isNotEmpty()) {
                append("• **Immediate Focus:** You have **${dueToday.size}** deadline-sensitive items today. Start with **${dueToday.first().title}**.\n")
            } else if (criticalTasks.isNotEmpty()) {
                append("• **Top Priority:** Tackle **${criticalTasks.first().title}** before other requests.\n")
            } else if (pendingTasks.isNotEmpty()) {
                append("• **Top Priority:** Kick off momentum with **${pendingTasks.first().title}** (${pendingTasks.first().estimatedMinutes} mins).\n")
            } else {
                append("• **Inbox Zero:** All scheduled tasks are complete! Great time for strategic review.\n")
            }

            if (todayEvents.isNotEmpty()) {
                append("• **Schedule:** You have **${todayEvents.size}** calendar commitments today. Earliest is at ${formatTime(todayEvents.first().startEpochMs)}.\n")
                append("• **Smart Window:** Schedule a 90-minute focus block between meetings for uninterrupted deep work.\n")
            } else {
                append("• **Schedule:** No calendar meetings today! Enjoy an uninterrupted deep-work flow.\n")
            }
            append("• **Device Sync:** Seamlessly synchronized across your mobile and desktop devices.")
        }
    }

    private fun localChatResponse(userMessage: String, context: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("focus") || lower.contains("next") || lower.contains("what should i do") -> {
                "🎯 **Top Recommendation:** Based on deadline proximity and priority, focus on your highest-ranked pending task right now. Turn on a 25-minute Pomodoro timer, mute notifications, and make progress before your next calendar event."
            }
            lower.contains("schedule") || lower.contains("plan") || lower.contains("afternoon") -> {
                "📅 **Optimal Time-Block Suggestion:**\n- 2:00 PM – 3:30 PM: Deep Focus Block on Critical Tasks\n- 3:30 PM – 3:45 PM: Bio break & stretch\n- 3:45 PM – 4:30 PM: Catch-up on notes and team communications\n- 4:30 PM – 5:00 PM: Daily wrap-up & cross-device sync verification."
            }
            lower.contains("overwhelm") || lower.contains("stressed") || lower.contains("too many") -> {
                "🌿 **Take a breath:** Let's simplify. 1) Pick only ONE critical task for the next hour. 2) Move lower-priority non-urgent tasks to tomorrow. 3) You're in control—OmniFlow has everything tracked safely across your devices."
            }
            else -> {
                "💡 **OmniFlow Assistant:** I'm monitoring your tasks, calendar commitments, and notes across all connected devices. Ask me to prioritize your day, suggest optimal time slots, or convert meeting notes into action items!"
            }
        }
    }

    private fun formatTime(epoch: Long): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epoch))
    }

    private fun formatDate(epoch: Long?): String {
        if (epoch == null) return "No deadline"
        return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epoch))
    }
}

data class ParsedTaskResult(
    val title: String,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val estimatedMinutes: Int = 30,
    val dueDateEpochMs: Long? = null,
    val notes: String = ""
)
