package com.sentinel.protection.ui.screens.honeytrap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sentinel.protection.domain.model.*
import com.sentinel.protection.ui.theme.*
import com.sentinel.protection.util.HoneyTrapDetector

@Composable
fun HoneyTrapScreen(onBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var analysis by remember { mutableStateOf<HoneyTrapAnalysis?>(null) }
    var showDismissed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Honey Trap Protection") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED))
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("What is Honey Trap?", fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Scammers use romantic messages to build trust, then request private photos or money for blackmail. This tool analyses messages for such patterns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Analyse a message or conversation", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                label = { Text("Paste message(s) here") },
                placeholder = { Text("Example: Hi handsome, I like you. Let's do a video call. Send me a photo.") },
                maxLines = 8
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val messages = inputText.split("\n").filter { it.isNotBlank() }
                        analysis = HoneyTrapDetector.analyzeConversation(messages)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.weight(1f),
                    enabled = inputText.isNotBlank()
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Analyse")
                }
                OutlinedButton(
                    onClick = { inputText = ""; analysis = null },
                    modifier = Modifier.weight(1f)
                ) { Text("Clear") }
            }

            Spacer(Modifier.height(16.dp))

            // Results
            analysis?.let { result ->
                HoneyTrapResultCard(
                    result = result,
                    onBlock = { /* Show dialog */ },
                    onReport = { /* Navigate to report */ },
                    onDismiss = { analysis = null }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Example messages for context
            Text("Example Honey Trap Pattern", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            val examples = listOf(
                "1. \"Hi handsome\"",
                "2. \"I like you a lot\"",
                "3. \"Let's do a video call\"",
                "4. \"Send me a private photo\"",
                "5. \"Send some money or I will share your photo\""
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    examples.forEach { ex ->
                        Text(ex, style = MaterialTheme.typography.bodySmall, color = DangerRed, modifier = Modifier.padding(vertical = 2.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "⚠️ Trap Lo Padakandi. Photo / Money Pampakandi.",
                        fontWeight = FontWeight.Bold,
                        color = DangerRed
                    )
                }
            }
        }
    }
}

@Composable
private fun HoneyTrapResultCard(
    result: HoneyTrapAnalysis,
    onBlock: () -> Unit,
    onReport: () -> Unit,
    onDismiss: () -> Unit
) {
    val (bg, text, titleText) = when (result.riskLevel) {
        RiskLevel.RED -> Triple(Color(0xFFFEE2E2), DangerRed, "🚨 HONEY TRAP RISK DETECTED")
        RiskLevel.ORANGE -> Triple(Color(0xFFFEF3C7), WarningOrange, "⚠️ Suspicious Patterns Detected")
        else -> Triple(Color(0xFFDCFCE7), PrimaryGreen, "✅ No Honey Trap Indicators")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titleText, fontWeight = FontWeight.Bold, color = text, fontSize = 16.sp)

            if (result.riskLevel == RiskLevel.RED) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Trap Lo Padakandi. Photo / Money Pampakandi.",
                    color = DangerRed,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Risk Score: ${result.riskScore}/100", color = text, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(result.explanation, style = MaterialTheme.typography.bodySmall, color = text)

            if (result.triggers.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Detected Triggers:", color = text, fontWeight = FontWeight.Medium)
                result.triggers.forEach { trigger ->
                    Text("• ${trigger.description}", style = MaterialTheme.typography.bodySmall, color = text)
                }
            }

            if (result.riskLevel != RiskLevel.GREEN) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onBlock,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Block")
                    }
                    OutlinedButton(
                        onClick = onReport,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningOrange),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Report")
                    }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Dismiss Warning", color = TextSecondary)
                }
            }
        }
    }
}

