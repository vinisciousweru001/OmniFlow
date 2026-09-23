package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.CalendarEvent
import com.example.data.model.ConnectedDevice
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DownloadExportManager {

    private const val TAG = "DownloadExportManager"

    /**
     * Packages and prepares the app's APK file for direct download/export and sharing.
     */
    fun exportAndDownloadApk(context: Context): Pair<Boolean, String> {
        return try {
            val appInfo = context.applicationInfo
            val sourceApkFile = File(appInfo.sourceDir)

            if (!sourceApkFile.exists()) {
                return Pair(false, "APK file not found at source location: ${sourceApkFile.absolutePath}")
            }

            // Copy to export cache with friendly name
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val targetApk = File(exportDir, "OmniFlow-v1.0.apk")

            FileInputStream(sourceApkFile).use { input ->
                FileOutputStream(targetApk).use { output ->
                    input.copyTo(output)
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetApk
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "OmniFlow Android APK Package (v1.0)")
                putExtra(Intent.EXTRA_TEXT, "Here is the compiled OmniFlow Android Application APK package ready to install.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Download or Save OmniFlow APK").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Pair(true, "OmniFlow APK prepared (${targetApk.length() / (1024 * 1024)} MB). Saving/sharing dialog opened!")
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting APK", e)
            Pair(false, "Could not export APK directly: ${e.localizedMessage}")
        }
    }

    /**
     * Generates a complete JSON backup of all tasks, events, and notes, and opens download/share chooser.
     */
    fun exportAndDownloadDataJson(
        context: Context,
        tasks: List<TaskItem>,
        events: List<CalendarEvent>,
        notes: List<NoteItem>,
        devices: List<ConnectedDevice>
    ): Pair<Boolean, String> {
        return try {
            val root = JSONObject().apply {
                put("app", "OmniFlow")
                put("version", "1.0")
                put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                put("totalTasks", tasks.size)
                put("totalEvents", events.size)
                put("totalNotes", notes.size)

                // Tasks array
                val tasksArray = JSONArray()
                tasks.forEach { task ->
                    val obj = JSONObject().apply {
                        put("id", task.id)
                        put("title", task.title)
                        put("notes", task.notes)
                        put("priority", task.priority.name)
                        put("category", task.category.name)
                        put("dueDateEpochMs", task.dueDateEpochMs ?: 0L)
                        put("estimatedMinutes", task.estimatedMinutes)
                        put("isCompleted", task.isCompleted)
                        put("completedAtEpochMs", task.completedAtEpochMs ?: 0L)
                        put("subtasksJson", task.subtasksJson)
                    }
                    tasksArray.put(obj)
                }
                put("tasks", tasksArray)

                // Events array
                val eventsArray = JSONArray()
                events.forEach { ev ->
                    val obj = JSONObject().apply {
                        put("id", ev.id)
                        put("title", ev.title)
                        put("description", ev.description)
                        put("startEpochMs", ev.startEpochMs)
                        put("endEpochMs", ev.endEpochMs)
                        put("locationOrLink", ev.locationOrLink)
                        put("eventType", ev.eventType.name)
                    }
                    eventsArray.put(obj)
                }
                put("events", eventsArray)

                // Notes array
                val notesArray = JSONArray()
                notes.forEach { note ->
                    val obj = JSONObject().apply {
                        put("id", note.id)
                        put("title", note.title)
                        put("content", note.content)
                        put("tags", note.tags)
                        put("isPinned", note.isPinned)
                        put("lastModifiedEpochMs", note.lastModifiedEpochMs)
                    }
                    notesArray.put(obj)
                }
                put("notes", notesArray)

                // Devices
                val devArray = JSONArray()
                devices.forEach { dev ->
                    val obj = JSONObject().apply {
                        put("id", dev.id)
                        put("name", dev.name)
                        put("type", dev.type.name)
                        put("syncState", dev.syncState.name)
                        put("ipAddress", dev.ipAddress)
                    }
                    devArray.put(obj)
                }
                put("pairedDevices", devArray)
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val jsonFile = File(exportDir, "OmniFlow_Backup_$timestamp.json")

            jsonFile.writeText(root.toString(2))

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                jsonFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "OmniFlow Productivity Data Backup ($timestamp)")
                putExtra(Intent.EXTRA_TEXT, "OmniFlow Data Backup exported with ${tasks.size} tasks, ${events.size} calendar events, and ${notes.size} notes.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Download or Save OmniFlow Backup (JSON)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Pair(true, "Data backup generated! Save to Downloads, Drive, or Files.")
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data JSON", e)
            Pair(false, "Failed to export data: ${e.localizedMessage}")
        }
    }

    /**
     * Generates standard iCalendar (.ics) format file for calendar events.
     */
    fun exportAndDownloadIcsCalendar(
        context: Context,
        events: List<CalendarEvent>
    ): Pair<Boolean, String> {
        return try {
            val icsDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
            val sb = java.lang.StringBuilder()
            sb.appendLine("BEGIN:VCALENDAR")
            sb.appendLine("VERSION:2.0")
            sb.appendLine("PRODID:-//OmniFlow AI Productivity//OmniFlow Assistant 1.0//EN")
            sb.appendLine("CALSCALE:GREGORIAN")
            sb.appendLine("METHOD:PUBLISH")

            events.forEach { ev ->
                sb.appendLine("BEGIN:VEVENT")
                sb.appendLine("UID:omniflow-ev-${ev.id}@omniflow.app")
                sb.appendLine("DTSTAMP:${icsDateFormat.format(Date())}")
                sb.appendLine("DTSTART:${icsDateFormat.format(Date(ev.startEpochMs))}")
                sb.appendLine("DTEND:${icsDateFormat.format(Date(ev.endEpochMs))}")
                sb.appendLine("SUMMARY:${ev.title.replace("\n", " ")}")
                if (ev.description.isNotBlank()) {
                    sb.appendLine("DESCRIPTION:${ev.description.replace("\n", "\\n")}")
                }
                if (ev.locationOrLink.isNotBlank()) {
                    sb.appendLine("LOCATION:${ev.locationOrLink}")
                }
                sb.appendLine("STATUS:CONFIRMED")
                sb.appendLine("END:VEVENT")
            }
            sb.appendLine("END:VCALENDAR")

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val icsFile = File(exportDir, "OmniFlow_Schedule_$timestamp.ics")

            icsFile.writeText(sb.toString())

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                icsFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "OmniFlow Calendar Schedule ($timestamp)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Download Calendar (.ics)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

            Pair(true, "iCalendar (.ics) ready with ${events.size} events!")
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting iCalendar", e)
            Pair(false, "Failed to export calendar: ${e.localizedMessage}")
        }
    }
}
