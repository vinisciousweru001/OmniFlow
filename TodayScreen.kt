package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalendarEvent
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.ui.components.EventCard
import com.example.ui.components.SmartSuggestionCard
import com.example.ui.components.SyncHeaderPill
import com.example.ui.components.TaskCard
import com.example.ui.theme.AmberOrange
import com.example.ui.theme.CoralRed
import com.example.ui.theme.Cyan400
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Indigo600
import com.example.ui.theme.PurpleViolet
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.OmniFlowViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: OmniFlowViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSync: () -> Unit,
    onOpenTaskDialog: (TaskItem?) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val smartSuggestions by viewModel.smartSuggestions.collectAsState()
    val briefing by viewModel.dailyBriefing.collectAsState()
    val isBriefingLoading by viewModel.isBriefingLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    val todayDateFormatted = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    val criticalTasks = pendingTasks.filter { it.priority == TaskPriority.CRITICAL || it.priority == TaskPriority.HIGH || it.isDueToday || it.isOverdue }
    val regularTasks = pendingTasks.filter { it !in criticalTasks }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = todayDateFormatted.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        color = Cyan400,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Unified Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.openDownloadDialog() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("header_download_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Download APK & Data",
                            tint = Cyan400,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    SyncHeaderPill(
                        isSyncing = isSyncing,
                        deviceCount = 4,
                        onSyncClick = { viewModel.triggerSync() }
                    )
                }
            }
        }

        // AI Daily Briefing Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(listOf(Indigo500, Cyan400, PurpleViolet))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_daily_briefing_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Indigo600.copy(alpha = 0.3f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Cyan400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Executive Briefing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (isSpeaking) {
                                        viewModel.stopSpeaking()
                                    } else {
                                        viewModel.readDailyBriefingAloud()
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("listen_briefing_audio_button")
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isSpeaking) "Stop audio" else "Listen hands-free",
                                    tint = if (isSpeaking) EmeraldGreen else Cyan400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.refreshDailyBriefing() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                if (isBriefingLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Cyan400
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh briefing",
                                        tint = Slate400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = briefing ?: "Analyzing your cross-device workload, upcoming meetings, and critical deadlines...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate200,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.openHandsFreeModal() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan400),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("briefing_voice_assistant_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToSync,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Indigo600,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AI Co-Pilot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Metrics Bar
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Metric 1: Pending tasks
                MetricCard(
                    title = "Pending Tasks",
                    count = "${pendingTasks.size}",
                    highlight = if (criticalTasks.isNotEmpty()) "${criticalTasks.size} urgent" else "On track",
                    highlightColor = if (criticalTasks.isNotEmpty()) CoralRed else EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTasks
                )
                // Metric 2: Meetings today
                MetricCard(
                    title = "Commitments",
                    count = "${allEvents.size}",
                    highlight = "Synced calendar",
                    highlightColor = Cyan400,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCalendar
                )
                // Metric 3: Connected devices
                MetricCard(
                    title = "Synced Devices",
                    count = "4/4",
                    highlight = "Mobile & PC",
                    highlightColor = EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSync
                )
            }
        }

        // Proactive AI Smart Suggestions
        if (smartSuggestions.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Smart Suggestions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            items(smartSuggestions) { suggestion ->
                SmartSuggestionCard(
                    suggestion = suggestion,
                    onActionClick = { viewModel.handleSuggestionAction(suggestion) }
                )
            }
        }

        // Today's Priority Action Items
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Top Priority Action Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "View All (${pendingTasks.size})",
                    fontSize = 13.sp,
                    color = Indigo400,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onNavigateToTasks() }
                        .padding(4.dp)
                )
            }
        }

        if (criticalTasks.isEmpty() && regularTasks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All clear! No pending tasks.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Add a task or dictate to OmniFlow AI.", fontSize = 12.sp, color = Slate400)
                    }
                }
            }
        } else {
            val displayTasks = (criticalTasks + regularTasks).take(4)
            items(displayTasks, key = { it.id }) { task ->
                val subtasks = viewModel.repository.parseSubtasks(task.subtasksJson)
                TaskCard(
                    task = task,
                    subtasks = subtasks,
                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                    onToggleSubtask = { subtaskId -> viewModel.toggleSubtask(task, subtaskId) },
                    onAddSubtask = { title -> viewModel.addSubtask(task, title) },
                    onDelete = { viewModel.deleteTask(task) },
                    onEdit = { onOpenTaskDialog(task) }
                )
            }
        }

        // Today's Calendar Schedule
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Today's Schedule & Focus Blocks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Calendar",
                    fontSize = 13.sp,
                    color = Indigo400,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onNavigateToCalendar() }
                        .padding(4.dp)
                )
            }
        }

        if (allEvents.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Indigo400,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Free schedule today", fontWeight = FontWeight.SemiBold)
                        Text("Great opportunity for focused deep work.", fontSize = 12.sp, color = Slate400)
                    }
                }
            }
        } else {
            items(allEvents.take(3), key = { it.id }) { event ->
                EventCard(
                    event = event,
                    onDelete = { viewModel.deleteEvent(event) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    highlight: String,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = Slate400,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = highlight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor,
                maxLines = 1
            )
        }
    }
}
