package com.sentinel.protection.ui.screens.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sentinel.protection.domain.model.ProtectionStats
import com.sentinel.protection.ui.navigation.Screen
import com.sentinel.protection.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val permissions by viewModel.permissionsStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.refreshStats() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
    ) {
        // Header
        DashboardHeader()

        // Main Shield Card
        ShieldStatusCard(stats = stats, permissionsGranted = permissions.values.count { it })

        Spacer(Modifier.height(16.dp))

        // Quick Actions Grid
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        QuickActionsGrid(
            onNavigate = onNavigate,
            onCyberHelp = {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: Exception) { /* Handle */ }
            }
        )

        Spacer(Modifier.height(16.dp))

        // Comparison Card
        ComparisonCard()

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DashboardHeader() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Namaskaram 🙏"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, DarkGreen)
                )
            )
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Column {
            Text(
                text = "సంకల్ప్ కవచం",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
            Text(
                text = "Sankalp Kavacham",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = greeting,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Truecaller Kante Better – 100% Free, No Ads",
                color = Color(0xFFBBF7D0),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ShieldStatusCard(stats: ProtectionStats, permissionsGranted: Int) {
    val totalPermissions = 6
    val isActive = permissionsGranted >= 2
    val statusColor = if (isActive) PrimaryGreen else WarningOrange
    val statusText = when {
        permissionsGranted >= 4 -> "Protection Active"
        permissionsGranted >= 2 -> "Partial Protection"
        else -> "Setup Required"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Shield icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isActive) LightGreen else Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Protection Shield",
                        tint = statusColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = statusColor
                    )
                    Text(
                        text = "$permissionsGranted/$totalPermissions permissions enabled",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Calls\nScreened", value = "${stats.callsScreened}", icon = Icons.Default.Phone)
                StatItem(label = "Messages\nChecked", value = "${stats.messagesChecked}", icon = Icons.Default.Message)
                StatItem(label = "Threats\nDetected", value = "${stats.threatsDetected}", icon = Icons.Default.Warning, valueColor = if (stats.threatsDetected > 0) DangerRed else PrimaryGreen)
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = PrimaryGreen
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = valueColor)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val route: String?,
    val action: (() -> Unit)? = null,
    val color: Color = PrimaryGreen
)

@Composable
private fun QuickActionsGrid(
    onNavigate: (String) -> Unit,
    onCyberHelp: () -> Unit
) {
    val actions = listOf(
        QuickAction("Open Dialer", Icons.Default.Dialpad, Screen.Dialer.route),
        QuickAction("Check Number", Icons.Default.Search, Screen.NumberCheck.createRoute("")),
        QuickAction("Scan Message", Icons.Default.Message, Screen.MessageScan.route),
        QuickAction("Check Media", Icons.Default.Image, Screen.MediaChecker.route),
        QuickAction("Kavacham Amma", Icons.Default.RecordVoiceOver, Screen.Assistant.route, color = Color(0xFF7C3AED)),
        QuickAction("Spam Box", Icons.Default.Shield, Screen.SpamBox.route),
        QuickAction("Honey Trap", Icons.Default.VisibilityOff, Screen.HoneyTrap.route, color = WarningOrange),
        QuickAction("Cyber Help\n1930", Icons.Default.Emergency, null, action = onCyberHelp, color = DangerRed)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(actions.size) { idx ->
            val action = actions[idx]
            QuickActionItem(action = action, onClick = {
                action.action?.invoke() ?: action.route?.let { onNavigate(it) }
            })
        }
    }
}

@Composable
private fun QuickActionItem(action: QuickAction, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(action.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, contentDescription = action.label, tint = action.color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            maxLines = 2,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Why Sankalp Kavacham?",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                // Our features
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "✅ Sankalp Kavacham",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    listOf(
                        "No advertisements",
                        "100% free",
                        "Telugu-first",
                        "Honey trap detection",
                        "Media authenticity check",
                        "On-device processing",
                        "1930 quick report"
                    ).forEach { feature ->
                        Text(text = "• $feature", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Note: Feature comparisons are based on publicly known information. We do not make unverified claims.",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

