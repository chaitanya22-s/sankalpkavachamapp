package com.sentinel.protection.ui.screens.assistant

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sentinel.protection.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AssistantScreen(viewModel: AssistantViewModel = hiltViewModel()) {
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val isSpeechAvailable by viewModel.isSpeechAvailable.collectAsStateWithLifecycle()

    val micPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val listState = rememberLazyListState()
    var textInput by remember { mutableStateOf("") }

    LaunchedEffect(conversation.size) {
        if (conversation.isNotEmpty()) listState.animateScrollToItem(conversation.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGreen)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Column {
                Text("కవచం అమ్మ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Kavacham Amma – Cyber Safety Assistant", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }

        // Conversation
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conversation) { message ->
                ChatBubble(message = message)
            }
            if (isProcessing) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistantAvatar()
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryGreen, strokeWidth = 2.dp)
                    }
                }
            }
        }

        // Input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardWhite)
                .padding(12.dp)
        ) {
            if (!isSpeechAvailable) {
                Text(
                    "⚠️ Speech recognition not available on this device. Use text input below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningOrange,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your question…", fontSize = 14.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp)
                )
                // Send text button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.processUserInput(textInput)
                            textInput = ""
                        }
                    },
                    enabled = textInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryGreen)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mic button
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MicButton(
                    isListening = isListening,
                    enabled = isSpeechAvailable && micPermission.status.isGranted,
                    onStart = {
                        if (micPermission.status.isGranted) {
                            viewModel.startListening()
                        } else {
                            micPermission.launchPermissionRequest()
                        }
                    },
                    onStop = viewModel::stopListening
                )
            }

            if (!micPermission.status.isGranted) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Microphone permission needed for voice input",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Quick prompts
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("OTP share cheyyali?", "1930 ki call", "Spam check").forEach { prompt ->
                    SuggestionChip(
                        onClick = { viewModel.processUserInput(prompt) },
                        label = { Text(prompt, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ConversationMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            AssistantAvatar()
            Spacer(Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    )
                )
                .background(if (message.isUser) PrimaryGreen else CardWhite)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) Color.White else TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun AssistantAvatar() {
    Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(LightGreen),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun MicButton(isListening: Boolean, enabled: Boolean, onStart: () -> Unit, onStop: () -> Unit) {
    val bgColor = when {
        isListening -> DangerRed
        !enabled -> TextDisabled
        else -> PrimaryGreen
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (enabled) Modifier.padding(0.dp) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = { if (isListening) onStop() else onStart() },
            modifier = Modifier.fillMaxSize(),
            enabled = enabled
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop listening" else "Start listening",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

