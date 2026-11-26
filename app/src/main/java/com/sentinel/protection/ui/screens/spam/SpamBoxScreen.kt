package com.sentinel.protection.ui.screens.spam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sentinel.protection.data.local.entity.SmsClassificationEntity
import com.sentinel.protection.domain.model.RiskLevel
import com.sentinel.protection.ui.navigation.Screen
import com.sentinel.protection.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpamBoxScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: SpamViewModel = hiltViewModel()
) {
    val spamMessages by viewModel.spamMessages.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopAppBar(
            title = { Text("Spam Box") },
            actions = {
                IconButton(onClick = { onNavigateTo(Screen.MessageScan.route) }) {
                    Icon(Icons.Default.Search, "Scan message", tint = PrimaryGreen)
                }
            }
        )

        if (spamMessages.isEmpty()) {
            EmptySpamBox()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(spamMessages) { message ->
                    SpamMessageItem(message = message)
                }
            }
        }
    }
}

@Composable
private fun EmptySpamBox() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MarkEmailRead,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("Spam Box is Clean!", style = MaterialTheme.typography.headlineMedium, color = PrimaryGreen)
        Spacer(Modifier.height(8.dp))
        Text(
            "Detected spam messages will appear here.\nTap Search to manually scan a message.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun SpamMessageItem(message: SmsClassificationEntity) {
    val riskColor = when (message.riskLevel) {
        "RED" -> DangerRed
        "ORANGE" -> WarningOrange
        else -> PrimaryGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    message.sender,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Surface(
                    color = riskColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        message.riskLevel,
                        color = riskColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            message.spamReason?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Spacer(Modifier.height(4.dp))
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            Text(
                sdf.format(Date(message.classifiedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun MessageScanScreen(
    onBack: () -> Unit,
    viewModel: SpamViewModel = hiltViewModel()
) {
    val scanInput by viewModel.scanInput.collectAsStateWithLifecycle()
    val spamResult by viewModel.spamAnalysisResult.collectAsStateWithLifecycle()
    val scamResult by viewModel.scamDetectionResult.collectAsStateWithLifecycle()
    val honeyTrapResult by viewModel.honeyTrapResult.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopAppBar(
            title = { Text("Scan a Message") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = scanInput,
                onValueChange = viewModel::setScanInput,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                label = { Text("Paste message here") },
                placeholder = { Text("Example: Congratulations! You won Rs.5000 prize. Click here to claim.") },
                maxLines = 6
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.analyzeMessage(body = scanInput) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    enabled = scanInput.isNotBlank()
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Analyze")
                }
                OutlinedButton(
                    onClick = viewModel::clearScanResults,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Results
            spamResult?.let { result ->
                AnalysisResultCard(
                    title = "Spam Analysis",
                    isRisk = result.isSpam,
                    summary = when (result.riskLevel) {
                        "RED" -> "🚨 High spam risk detected"
                        "ORANGE" -> "⚠️ Suspicious content detected"
                        else -> "✅ No obvious spam indicators"
                    },
                    details = result.reasons + result.suspiciousLinks.map { "Link: $it" }
                )
                Spacer(Modifier.height(8.dp))
            }

            scamResult?.let { result ->
                if (result.hasScamIndicators) {
                    AnalysisResultCard(
                        title = "Scam Keywords",
                        isRisk = true,
                        summary = result.warningMessage,
                        details = result.matchedKeywords.map { "Keyword: $it" }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            honeyTrapResult?.let { result ->
                if (result.riskLevel != RiskLevel.GREEN) {
                    AnalysisResultCard(
                        title = "Honey Trap Check",
                        isRisk = true,
                        summary = result.explanation,
                        details = result.triggers.map { "${it.type.name}: ${it.description}" }
                    )
                }
            }

            if (spamResult == null && scamResult == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "How it works",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PrimaryGreen
                        )
                        Spacer(Modifier.height(8.dp))
                        listOf(
                            "Checks for fraud keywords in English and Telugu",
                            "Detects suspicious links and URL shorteners",
                            "Analyses sender patterns",
                            "Checks for honey trap indicators",
                            "All analysis is done on-device"
                        ).forEach {
                            Text("• $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisResultCard(
    title: String,
    isRisk: Boolean,
    summary: String,
    details: List<String>
) {
    val bgColor = if (isRisk) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
    val textColor = if (isRisk) DangerRed else PrimaryGreen

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = textColor, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = textColor)
            details.forEach { detail ->
                Text("• $detail", style = MaterialTheme.typography.bodySmall, color = textColor, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

