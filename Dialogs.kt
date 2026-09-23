package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CalendarEvent
import com.example.data.model.EventType
import com.example.data.model.NoteItem
import com.example.data.model.Subtask
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Calendar
import java.util.UUID

@Composable
fun TaskEditorDialog(
    task: TaskItem?,
    existingSubtasks: List<Subtask>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        notes: String,
        priority: TaskPriority,
        category: TaskCategory,
        dueDateEpochMs: Long?,
        estimatedMinutes: Int,
        subtasks: List<Subtask>
    ) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var notes by remember { mutableStateOf(task?.notes ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var category by remember { mutableStateOf(task?.category ?: TaskCategory.WORK) }
    var estimatedMinutes by remember { mutableIntStateOf(task?.estimatedMinutes ?: 30) }

    var dueDateEpochMs by remember { mutableStateOf(task?.dueDateEpochMs) }
    val subtasksList = remember { mutableStateListOf<Subtask>().apply { addAll(existingSubtasks) } }
    var newSubtaskInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (task == null) "New Productivity Task" else "Edit Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *", color = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_dialog_title")
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Details & Context", color = Slate400) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Priority Level", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    TaskPriority.values().forEach { p ->
                        val isSelected = priority == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { priority = p },
                            label = { Text(p.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo600,
                                selectedLabelColor = Color.White,
                                containerColor = Slate800,
                                labelColor = Slate200
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Indigo400 else Slate700
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    TaskCategory.values().forEach { c ->
                        val isSelected = category == c
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = c },
                            label = { Text(c.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo600,
                                selectedLabelColor = Color.White,
                                containerColor = Slate800,
                                labelColor = Slate200
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Indigo400 else Slate700
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Deadline Presets", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    val cal = Calendar.getInstance()
                    // Today 5 PM
                    cal.set(Calendar.HOUR_OF_DAY, 17)
                    cal.set(Calendar.MINUTE, 0)
                    val t5 = cal.timeInMillis

                    // Tomorrow 12 PM
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 12)
                    val tmNoon = cal.timeInMillis

                    // End of Week
                    cal.add(Calendar.DAY_OF_YEAR, 3)
                    val eow = cal.timeInMillis

                    FilterChip(
                        selected = dueDateEpochMs == t5,
                        onClick = { dueDateEpochMs = t5 },
                        label = { Text("Today 5 PM", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = dueDateEpochMs == tmNoon,
                        onClick = { dueDateEpochMs = tmNoon },
                        label = { Text("Tomorrow Noon", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = dueDateEpochMs == eow,
                        onClick = { dueDateEpochMs = eow },
                        label = { Text("End of Week", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = dueDateEpochMs == null,
                        onClick = { dueDateEpochMs = null },
                        label = { Text("No Deadline", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Estimated Duration: ${estimatedMinutes}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(15, 30, 45, 60, 90).forEach { mins ->
                        val isSelected = estimatedMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { estimatedMinutes = mins },
                            label = { Text("${mins}m", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo600,
                                selectedLabelColor = Color.White,
                                containerColor = Slate800,
                                labelColor = Slate200
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Checklist / Subtasks (${subtasksList.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))

                subtasksList.forEachIndexed { index, st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text("• ${st.title}", fontSize = 12.sp, color = Slate200, modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { subtasksList.removeAt(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newSubtaskInput,
                        onValueChange = { newSubtaskInput = it },
                        placeholder = { Text("Add subtask...", fontSize = 12.sp, color = Slate400) },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Indigo400,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (newSubtaskInput.isNotBlank()) {
                                subtasksList.add(Subtask(UUID.randomUUID().toString(), newSubtaskInput.trim(), false))
                                newSubtaskInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Cyan400)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel", color = Slate400)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    title,
                                    notes,
                                    priority,
                                    category,
                                    dueDateEpochMs,
                                    estimatedMinutes,
                                    subtasksList.toList()
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        modifier = Modifier.testTag("task_dialog_save")
                    ) {
                        Text("Save & Sync", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EventEditorDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        startEpoch: Long,
        endEpoch: Long,
        location: String,
        eventType: EventType
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf(EventType.MEETING) }

    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 14)
    cal.set(Calendar.MINUTE, 0)
    val startEpoch = cal.timeInMillis
    val endEpoch = startEpoch + (60 * 60 * 1000L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Schedule Event or Block",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *", color = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_dialog_title")
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location or Virtual Link", color = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Event Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate400)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    EventType.values().forEach { et ->
                        val isSelected = eventType == et
                        FilterChip(
                            selected = isSelected,
                            onClick = { eventType = et },
                            label = { Text(et.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo600,
                                selectedLabelColor = Color.White,
                                containerColor = Slate800,
                                labelColor = Slate200
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel", color = Slate400)
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, description, startEpoch, endEpoch, location, eventType)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        modifier = Modifier.testTag("event_dialog_save")
                    ) {
                        Text("Add Event", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NoteEditorDialog(
    note: NoteItem?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, tags: String, isPinned: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var tags by remember { mutableStateOf(note?.tags ?: "Ideas, Product") }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (note == null) "New Scratchpad Note" else "Edit Note",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row {
                        IconButton(onClick = { isPinned = !isPinned }) {
                            Icon(
                                imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin",
                                tint = if (isPinned) AmberOrange else Slate400
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title", color = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_dialog_title")
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content (supports bullet points e.g. - [ ] task)", color = Slate400) },
                    minLines = 5,
                    maxLines = 10,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_dialog_content")
                )

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated e.g. Ideas, Sprint, Meeting)", color = Slate400) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo400,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Cancel", color = Slate400)
                    }
                    Button(
                        onClick = {
                            if (content.isNotBlank() || title.isNotBlank()) {
                                onSave(title, content, tags, isPinned)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo500),
                        modifier = Modifier.testTag("note_dialog_save")
                    ) {
                        Text("Save Note", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
