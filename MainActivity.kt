package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CalendarEvent
import com.example.data.model.NoteItem
import com.example.data.model.TaskItem
import com.example.ui.components.DownloadApkAndDataDialog
import com.example.ui.components.EventEditorDialog
import com.example.ui.components.HandsFreeVoiceModal
import com.example.ui.components.NoteEditorDialog
import com.example.ui.components.TaskEditorDialog
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CreatorManualScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.SyncAssistantScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.Cyan400
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.OmniFlowViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OmniFlowApp()
            }
        }
    }
}

@Composable
fun OmniFlowApp(
    viewModel: OmniFlowViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val isHandsFreeModalOpen by viewModel.isHandsFreeModalOpen.collectAsState()
    val showDownloadDialog by viewModel.showDownloadDialog.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog States
    var showCreatorManual by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }

    var showEventDialog by remember { mutableStateOf(false) }

    var showNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<NoteItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackBarMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Slate900,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                // Tab 1: Today
                NavigationBarItem(
                    selected = currentTab == NavigationTab.TODAY,
                    onClick = { viewModel.selectTab(NavigationTab.TODAY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.TODAY) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Today Hub"
                        )
                    },
                    label = { Text("Today", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Cyan400,
                        indicatorColor = Indigo500,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_tab_today")
                )

                // Tab 2: Tasks
                NavigationBarItem(
                    selected = currentTab == NavigationTab.TASKS,
                    onClick = { viewModel.selectTab(NavigationTab.TASKS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingTasks.isNotEmpty()) {
                                    Badge(
                                        containerColor = Indigo400,
                                        contentColor = Color.White
                                    ) {
                                        Text("${pendingTasks.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == NavigationTab.TASKS) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                                contentDescription = "Tasks"
                            )
                        }
                    },
                    label = { Text("Tasks", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Cyan400,
                        indicatorColor = Indigo500,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_tab_tasks")
                )

                // Tab 3: Calendar
                NavigationBarItem(
                    selected = currentTab == NavigationTab.CALENDAR,
                    onClick = { viewModel.selectTab(NavigationTab.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Calendar"
                        )
                    },
                    label = { Text("Calendar", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Cyan400,
                        indicatorColor = Indigo500,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_tab_calendar")
                )

                // Tab 4: Notes
                NavigationBarItem(
                    selected = currentTab == NavigationTab.NOTES,
                    onClick = { viewModel.selectTab(NavigationTab.NOTES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.NOTES) Icons.Filled.Description else Icons.Outlined.Description,
                            contentDescription = "Notes"
                        )
                    },
                    label = { Text("Notes", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Cyan400,
                        indicatorColor = Indigo500,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_tab_notes")
                )

                // Tab 5: Sync & AI
                NavigationBarItem(
                    selected = currentTab == NavigationTab.SYNC_AI,
                    onClick = { viewModel.selectTab(NavigationTab.SYNC_AI) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == NavigationTab.SYNC_AI) Icons.Filled.CloudSync else Icons.Outlined.CloudSync,
                            contentDescription = "Sync & AI"
                        )
                    },
                    label = { Text("Sync & AI", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Cyan400,
                        indicatorColor = Indigo500,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400
                    ),
                    modifier = Modifier.testTag("nav_tab_sync")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showCreatorManual && BuildConfig.CREATOR_MODE) {
                CreatorManualScreen(
                    onBack = { showCreatorManual = false }
                )
            } else {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { tab ->
                    when (tab) {
                    NavigationTab.TODAY -> TodayScreen(
                        viewModel = viewModel,
                        onNavigateToTasks = { viewModel.selectTab(NavigationTab.TASKS) },
                        onNavigateToCalendar = { viewModel.selectTab(NavigationTab.CALENDAR) },
                        onNavigateToSync = { viewModel.selectTab(NavigationTab.SYNC_AI) },
                        onOpenTaskDialog = { task ->
                            editingTask = task
                            showTaskDialog = true
                        }
                    )
                    NavigationTab.TASKS -> TasksScreen(
                        viewModel = viewModel,
                        onOpenTaskDialog = { task ->
                            editingTask = task
                            showTaskDialog = true
                        }
                    )
                    NavigationTab.CALENDAR -> CalendarScreen(
                        viewModel = viewModel,
                        onOpenEventDialog = { showEventDialog = true }
                    )
                    NavigationTab.NOTES -> NotesScreen(
                        viewModel = viewModel,
                        onOpenNoteDialog = { note ->
                            editingNote = note
                            showNoteDialog = true
                        }
                    )
                        NavigationTab.SYNC_AI -> SyncAssistantScreen(
                            viewModel = viewModel,
                            onOpenCreatorManual = { showCreatorManual = true }
                        )
                    }
                }

                // Global Floating Hands-Free Voice Action Button
                FloatingActionButton(
                    onClick = { viewModel.openHandsFreeModal() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .testTag("global_hands_free_fab"),
                    shape = CircleShape,
                    containerColor = if (isSpeaking) EmeraldGreen else if (isListening) Cyan400 else Indigo500,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = when {
                            isSpeaking -> Icons.AutoMirrored.Filled.VolumeUp
                            isListening -> Icons.Default.GraphicEq
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Hands-Free Voice Hub",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    // Hands-Free Voice Assistant Modal
    if (isHandsFreeModalOpen) {
        HandsFreeVoiceModal(
            viewModel = viewModel,
            onDismiss = { viewModel.closeHandsFreeModal() }
        )
    }

    // Download & Export Hub Dialog
    if (showDownloadDialog) {
        DownloadApkAndDataDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeDownloadDialog() }
        )
    }

    // Task Dialog
    if (showTaskDialog) {
        val existingSubtasks = editingTask?.let { viewModel.repository.parseSubtasks(it.subtasksJson) } ?: emptyList()
        TaskEditorDialog(
            task = editingTask,
            existingSubtasks = existingSubtasks,
            onDismiss = {
                showTaskDialog = false
                editingTask = null
            },
            onSave = { title, notes, priority, category, dueDate, estMinutes, subtasks ->
                viewModel.saveTask(
                    id = editingTask?.id ?: 0L,
                    title = title,
                    notes = notes,
                    priority = priority,
                    category = category,
                    dueDateEpochMs = dueDate,
                    estimatedMinutes = estMinutes,
                    subtasks = subtasks
                )
            }
        )
    }

    // Event Dialog
    if (showEventDialog) {
        EventEditorDialog(
            onDismiss = { showEventDialog = false },
            onSave = { title, desc, start, end, loc, type ->
                viewModel.saveEvent(
                    title = title,
                    description = desc,
                    startEpoch = start,
                    endEpoch = end,
                    location = loc,
                    eventType = type
                )
            }
        )
    }

    // Note Dialog
    if (showNoteDialog) {
        NoteEditorDialog(
            note = editingNote,
            onDismiss = {
                showNoteDialog = false
                editingNote = null
            },
            onSave = { title, content, tags, isPinned ->
                viewModel.saveNote(
                    id = editingNote?.id ?: 0L,
                    title = title,
                    content = content,
                    tags = tags,
                    isPinned = isPinned
                )
            }
        )
    }
}

/**
 * Maintained for backward compatibility with testing suites
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
