package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.ConnectedDevice
import com.example.data.model.DeviceType
import com.example.data.model.EventType
import com.example.data.model.NoteItem
import com.example.data.model.SmartSuggestion
import com.example.data.model.Subtask
import com.example.data.model.SyncState
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.BlueSky
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.PurpleViolet
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PriorityBadge(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (priority) {
        TaskPriority.CRITICAL -> Triple(CoralRed.copy(alpha = 0.18f), CoralRed, "Critical")
        TaskPriority.HIGH -> Triple(AmberOrange.copy(alpha = 0.18f), AmberOrange, "High")
        TaskPriority.MEDIUM -> Triple(BlueSky.copy(alpha = 0.18f), BlueSky, "Medium")
        TaskPriority.LOW -> Triple(Slate400.copy(alpha = 0.18f), Slate400, "Low")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategoryBadge(category: TaskCategory, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = category.label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SyncHeaderPill(
    isSyncing: Boolean,
    deviceCount: Int = 4,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onSyncClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSyncing) Indigo600.copy(alpha = 0.2f) else Slate800,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSyncing) Indigo400 else Slate700
        ),
        modifier = modifier.testTag("sync_header_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = Indigo400
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Syncing devices...",
                    fontSize = 12.sp,
                    color = Indigo400,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$deviceCount Devices Synced",
                    fontSize = 12.sp,
                    color = Slate200,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: TaskItem,
    subtasks: List<Subtask>,
    onToggleComplete: () -> Unit,
    onToggleSubtask: (String) -> Unit,
    onAddSubtask: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }
    var showAddSubtaskInput by remember { mutableStateOf(false) }

    val completedSubtasks = subtasks.count { it.isDone }
    val totalSubtasks = subtasks.size

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Interactive Checkmark
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.isCompleted) EmeraldGreen else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.5.dp,
                            if (task.isCompleted) EmeraldGreen else Slate600,
                            CircleShape
                        )
                        .clickable { onToggleComplete() }
                        .testTag("task_check_${task.id}")
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) Slate400 else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = if (expanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            maxLines = if (expanded) 10 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // AI Priority Score badge
                if (!task.isCompleted && task.aiPriorityScore > 75) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Indigo500.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Indigo400,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${task.aiPriorityScore}%",
                                fontSize = 10.sp,
                                color = Indigo400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand task",
                        tint = Slate400
                    )
                }
            }

            // Badges row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PriorityBadge(task.priority)
                CategoryBadge(task.category)

                // Deadline Badge
                if (task.dueDateEpochMs != null) {
                    val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(task.dueDateEpochMs))
                    val isOverdue = task.isOverdue
                    val isDueToday = task.isDueToday

                    val (chipBg, chipColor) = when {
                        isOverdue -> CoralRed.copy(alpha = 0.2f) to CoralRed
                        isDueToday -> AmberOrange.copy(alpha = 0.2f) to AmberOrange
                        else -> Slate700 to Slate200
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(chipBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = chipColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isOverdue) "Overdue ($dateStr)" else if (isDueToday) "Today" else dateStr,
                                color = chipColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Estimated time
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${task.estimatedMinutes}m",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Subtask mini indicator
                if (totalSubtasks > 0) {
                    Text(
                        text = "$completedSubtasks/$totalSubtasks",
                        fontSize = 11.sp,
                        color = if (completedSubtasks == totalSubtasks) EmeraldGreen else Slate400,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Subtask progress bar if has subtasks
            if (totalSubtasks > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { completedSubtasks.toFloat() / totalSubtasks.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (completedSubtasks == totalSubtasks) EmeraldGreen else Indigo500,
                    trackColor = Slate800,
                )
            }

            // AI suggested time slot notice
            if (task.aiSuggestedSlot != null && !task.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PurpleViolet.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PurpleViolet,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Suggested: ${task.aiSuggestedSlot}",
                        fontSize = 11.sp,
                        color = PurpleViolet,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Expanded Subtasks & Actions section
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (subtasks.isNotEmpty()) {
                        Text(
                            text = "Checklist Items",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        subtasks.forEach { st ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleSubtask(st.id) }
                                    .padding(vertical = 3.dp)
                            ) {
                                Checkbox(
                                    checked = st.isDone,
                                    onCheckedChange = { onToggleSubtask(st.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = EmeraldGreen,
                                        uncheckedColor = Slate600
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = st.title,
                                    fontSize = 13.sp,
                                    color = if (st.isDone) Slate400 else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (st.isDone) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralRed),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onEdit,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Details", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartSuggestionCard(
    suggestion: SmartSuggestion,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Indigo500.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("suggestion_card_${suggestion.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = suggestion.type.iconEmoji,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = suggestion.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Indigo400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "OmniFlow AI Suggestion",
                        fontSize = 11.sp,
                        color = Indigo400,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo500,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = suggestion.actionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: CalendarEvent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.startEpochMs))
    val endTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(event.endEpochMs))

    val eventColor = try {
        Color(android.graphics.Color.parseColor(event.colorHex))
    } catch (_: Exception) {
        Indigo500
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Color accent vertical strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(eventColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.eventType.label,
                            fontSize = 10.sp,
                            color = eventColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$startTime - $endTime",
                        fontSize = 12.sp,
                        color = Slate400
                    )
                }

                if (event.locationOrLink.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.locationOrLink,
                        fontSize = 11.sp,
                        color = Indigo400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete event",
                    tint = Slate400,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteItem,
    onTogglePin: () -> Unit,
    onExtractTasks: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}")
            .clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin note",
                        tint = if (note.isPinned) AmberOrange else Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                color = Slate200,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            if (note.aiSummary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Indigo400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = note.aiSummary,
                        fontSize = 11.sp,
                        color = Slate400,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tags
                if (note.tags.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        note.tags.split(",").take(2).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate800)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#${tag.trim()}",
                                    fontSize = 10.sp,
                                    color = Cyan400
                                )
                            }
                        }
                    }
                }

                // AI Extract Tasks Button
                Button(
                    onClick = onExtractTasks,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo500.copy(alpha = 0.2f),
                        contentColor = Indigo400
                    ),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI to Tasks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ConnectedDeviceItem(
    device: ConnectedDevice,
    modifier: Modifier = Modifier
) {
    val deviceIcon = when (device.type) {
        DeviceType.MOBILE -> Icons.Default.PhoneAndroid
        DeviceType.LAPTOP -> Icons.Default.Laptop
        DeviceType.DESKTOP -> Icons.Default.Devices
        DeviceType.TABLET -> Icons.Default.Tablet
    }

    val syncColor = when (device.syncState) {
        SyncState.ONLINE -> EmeraldGreen
        SyncState.SYNCING -> Indigo400
        SyncState.PENDING -> AmberOrange
        SyncState.OFFLINE -> Slate600
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate700)
            ) {
                Icon(
                    imageVector = deviceIcon,
                    contentDescription = null,
                    tint = if (device.isCurrentDevice) Cyan400 else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (device.isCurrentDevice) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Cyan400.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("This device", fontSize = 9.sp, color = Cyan400, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.osDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(syncColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = device.syncState.label,
                        fontSize = 11.sp,
                        color = syncColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                val syncTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(device.lastSyncTimeEpochMs))
                Text(
                    text = "Synced $syncTime",
                    fontSize = 10.sp,
                    color = Slate400
                )
            }
        }
    }
}
