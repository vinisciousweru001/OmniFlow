package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.Cyan400
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Indigo500
import com.example.ui.theme.PurpleViolet
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

private data class ManualSection(
    val title: String,
    val summary: String,
    val details: List<String>
)

private val manualSections = listOf(
    ManualSection(
        title = "1. Start with the Today hub",
        summary = "Use Today as the creator’s first check-in.",
        details = listOf(
            "Review the AI briefing for deadlines, meetings, and focus recommendations.",
            "Open urgent tasks from the priority area and edit them without leaving the hub.",
            "Use the quick actions to jump to Tasks, Calendar, or Sync & AI."
        )
    ),
    ManualSection(
        title = "2. Manage the three core records",
        summary = "Tasks, calendar events, and notes are stored locally first.",
        details = listOf(
            "Tasks support priorities, categories, due dates, estimated effort, notes, and subtasks.",
            "Calendar events support a title, description, time range, location, and event type.",
            "Notes support searchable content, tags, and pinning for important context.",
            "Use the search and filters on each screen before creating duplicate records."
        )
    ),
    ManualSection(
        title = "3. Use the AI co-pilot",
        summary = "Ask for planning help from Sync & AI.",
        details = listOf(
            "Quick prompts are starting points; the free-form chat accepts questions about the current workload.",
            "The AI uses the local task, calendar, and note context supplied by the app.",
            "If Gemini is not configured, the app continues working for local productivity flows but AI responses are unavailable.",
            "Keep sensitive content out of prompts unless the configured AI service and its data handling have been reviewed."
        )
    ),
    ManualSection(
        title = "4. Voice and hands-free mode",
        summary = "Use voice when typing is inconvenient.",
        details = listOf(
            "Grant microphone access only when hands-free mode is needed.",
            "The global microphone button opens the voice hub from the main screens.",
            "You can type a command in the voice hub when speech recognition is unavailable.",
            "Stop speech playback before switching away if you need the device to stay quiet."
        )
    ),
    ManualSection(
        title = "5. Sync and export",
        summary = "Keep device copies aligned and maintain a recoverable backup.",
        details = listOf(
            "Sync & AI shows the connected device list and the last sync timestamp.",
            "Use Sync Now after making important changes on another device.",
            "Use Download APK & Export to create offline JSON and ICS backups.",
            "Treat exported files as private data and store them in a protected location."
        )
    ),
    ManualSection(
        title = "6. Release checklist",
        summary = "Complete these checks before sharing a build.",
        details = listOf(
            "Build the normal release without the creator flag so the manual entry point is absent.",
            "Configure the Gemini key through the secrets flow; never commit a real key to the repository.",
            "Test task, event, and note creation with AI disabled to confirm offline-first behavior.",
            "Test microphone denial, empty states, export, and a fresh install before distribution.",
            "Confirm the package signing configuration and remove any development-only settings before publishing."
        )
    )
)

@Composable
fun CreatorManualScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!BuildConfig.CREATOR_MODE) {
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("creator_manual_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Creator Manual",
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Private OmniFlow reference",
                            color = Cyan400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("creator_manual_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Sync & AI"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Slate200
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Indigo500.copy(alpha = 0.18f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Indigo400.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Cyan400
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Creator build enabled",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This manual is visible only when the app is compiled with creator mode.",
                                color = Slate200,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "OmniFlow is an offline-first productivity co-pilot. Use this guide to validate the core flows, configure creator builds, and prepare a safe release.",
                    color = Slate200,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            items(manualSections, key = { it.title }) { section ->
                ManualSectionCard(section)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Creator access rule",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Creator mode is off by default. Enable it only for the creator build, then ship the normal build to users. This keeps the manual out of the ordinary navigation and avoids treating a hidden gesture as security.",
                            color = Slate200,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}

@Composable
private fun ManualSectionCard(section: ManualSection) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = section.summary,
                color = PurpleViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            section.details.forEach { detail ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = detail,
                        color = Slate200,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}