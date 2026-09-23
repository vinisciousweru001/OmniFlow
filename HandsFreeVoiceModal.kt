package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
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
import com.example.ui.viewmodel.OmniFlowViewModel

@Composable
fun HandsFreeVoiceModal(
    viewModel: OmniFlowViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val soundLevel by viewModel.voiceSoundLevel.collectAsState()
    val transcription by viewModel.spokenTranscription.collectAsState()
    val responseText by viewModel.spokenResponseText.collectAsState()
    val statusMessage by viewModel.voiceStatusMessage.collectAsState()

    var manualCommandText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening()
        } else {
            viewModel.speakText("Microphone permission is required for voice control. You can also use voice presets below.")
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else if (isSpeaking) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Slate700, RoundedCornerShape(28.dp))
                .testTag("hands_free_voice_modal"),
            color = Slate900,
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Indigo500, Cyan400))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hands-Free Voice Hub",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Voice commands, briefing & audio actions",
                                color = Slate400,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_voice_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsing Voice Orb Visualizer
                Box(
                    modifier = Modifier
                        .size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer reactive aura
                    Box(
                        modifier = Modifier
                            .size((110 * (if (isListening) (1f + soundLevel * 0.4f) else pulseScale)).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isSpeaking) {
                                        listOf(EmeraldGreen.copy(alpha = 0.4f), Color.Transparent)
                                    } else if (isListening) {
                                        listOf(Cyan400.copy(alpha = 0.5f), Indigo500.copy(alpha = 0.2f), Color.Transparent)
                                    } else {
                                        listOf(Indigo500.copy(alpha = 0.25f), Color.Transparent)
                                    }
                                )
                            )
                    )

                    // Core Interactive Button
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isSpeaking) {
                                        listOf(EmeraldGreen, Cyan500)
                                    } else if (isListening) {
                                        listOf(Cyan400, Indigo600)
                                    } else {
                                        listOf(Indigo500, PurpleViolet)
                                    }
                                )
                            )
                            .clickable {
                                if (isListening) {
                                    viewModel.stopVoiceListening()
                                } else if (isSpeaking) {
                                    viewModel.stopSpeaking()
                                } else {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        viewModel.startVoiceListening()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("voice_orb_action_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isSpeaking -> Icons.AutoMirrored.Filled.VolumeUp
                                isListening -> Icons.Default.GraphicEq
                                else -> Icons.Default.Mic
                            },
                            contentDescription = "Voice Action",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status text
                Text(
                    text = when {
                        isSpeaking -> "Speaking response..."
                        isListening -> "Listening... Say a command"
                        else -> statusMessage
                    },
                    color = if (isSpeaking) EmeraldGreen else if (isListening) Cyan400 else Slate400,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Transcription Box
                if (transcription.isNotBlank() || isListening) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "HEARD FROM YOU",
                                color = Cyan400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = transcription.ifBlank { "..." },
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Spoken AI Response Card
                if (responseText.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ASSISTANT AUDIO RESPONSE",
                                    color = EmeraldGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Row {
                                    if (isSpeaking) {
                                        IconButton(
                                            onClick = { viewModel.stopSpeaking() },
                                            modifier = Modifier.size(28.dp).testTag("stop_speaking_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Stop,
                                                contentDescription = "Stop",
                                                tint = Slate200,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { viewModel.speakText(responseText) },
                                            modifier = Modifier.size(28.dp).testTag("replay_speaking_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                contentDescription = "Play",
                                                tint = Cyan400,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = responseText,
                                color = Slate200,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Preset Voice Commands Chips
                Text(
                    text = "QUICK HANDS-FREE VOICE COMMANDS",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoiceCommandChip(
                        title = "🎙️ Read my briefing",
                        onClick = { viewModel.executeVoiceCommand("Brief me on today") }
                    )
                    VoiceCommandChip(
                        title = "🎙️ Urgent tasks rundown",
                        onClick = { viewModel.executeVoiceCommand("What are my urgent tasks?") }
                    )
                    VoiceCommandChip(
                        title = "🎙️ What's my next meeting?",
                        onClick = { viewModel.executeVoiceCommand("What is my next meeting?") }
                    )
                    VoiceCommandChip(
                        title = "🎙️ Schedule 1hr deep focus",
                        onClick = { viewModel.executeVoiceCommand("Schedule focus block") }
                    )
                    VoiceCommandChip(
                        title = "🎙️ Sync all devices",
                        onClick = { viewModel.executeVoiceCommand("Sync now") }
                    )
                    VoiceCommandChip(
                        title = "📦 Download APK & Backup",
                        onClick = {
                            onDismiss()
                            viewModel.openDownloadDialog()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Fallback input bar
                OutlinedTextField(
                    value = manualCommandText,
                    onValueChange = { manualCommandText = it },
                    placeholder = { Text("Speak above or type voice command...", color = Slate400, fontSize = 13.sp) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (manualCommandText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.executeVoiceCommand(manualCommandText)
                                        manualCommandText = ""
                                    },
                                    modifier = Modifier.testTag("submit_manual_voice_command")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send Command",
                                        tint = Cyan400
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_command_text_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = Cyan400,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun VoiceCommandChip(
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Slate800,
        border = androidx.compose.foundation.BorderStroke(1.dp, Indigo500.copy(alpha = 0.5f)),
        modifier = Modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Slate200,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
