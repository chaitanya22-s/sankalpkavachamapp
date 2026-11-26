package com.sentinel.protection.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sentinel.protection.ui.navigation.Screen
import com.sentinel.protection.ui.theme.*

@Composable
fun ProfileScreen(onNavigateTo: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryGreen)
                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Sankalp Kavacham", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                    Text("Protected Device", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // Settings sections
            SectionHeader("Protection")
            ProfileMenuItem(icon = Icons.Default.Shield, label = "Privacy Centre", subtitle = "Permissions & data") {
                onNavigateTo(Screen.Privacy.route)
            }
            ProfileMenuItem(icon = Icons.Default.Block, label = "Block List", subtitle = "Blocked numbers") {
                // Block list screen
            }
            ProfileMenuItem(icon = Icons.Default.ReportProblem, label = "Emergency Cyber Help", subtitle = "1930 & cybercrime.gov.in") {
                onNavigateTo(Screen.CyberHelp.route)
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Features")
            ProfileMenuItem(icon = Icons.Default.FaceRetouchingNatural, label = "Honey Trap Protection", subtitle = "Detect suspicious patterns") {
                onNavigateTo(Screen.HoneyTrap.route)
            }
            ProfileMenuItem(icon = Icons.Default.Image, label = "Media Authenticity", subtitle = "Check photos and videos") {
                onNavigateTo(Screen.MediaChecker.route)
            }
            ProfileMenuItem(icon = Icons.Default.RecordVoiceOver, label = "Kavacham Amma", subtitle = "Telugu voice assistant") {
                onNavigateTo(Screen.Assistant.route)
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("Settings")
            ProfileMenuItem(icon = Icons.Default.Settings, label = "App Settings", subtitle = "Language, text size, notifications") {
                onNavigateTo(Screen.Settings.route)
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader("About")
            ProfileMenuItem(icon = Icons.Default.Info, label = "About Sankalp Kavacham", subtitle = "Version 1.0.0") {}
            ProfileMenuItem(icon = Icons.Default.PrivacyTip, label = "Privacy Policy", subtitle = "How we protect your data") {}

            Spacer(Modifier.height(16.dp))

            // App tagline
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛡️ సంకల్ప్ కవచం", fontWeight = FontWeight.Bold, color = PrimaryGreen, style = MaterialTheme.typography.headlineSmall)
                    Text("Truecaller Kante Better", color = PrimaryGreen, style = MaterialTheme.typography.bodyMedium)
                    Text("100% Free • No Ads • Made for India", color = DarkGreen, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = PrimaryGreen,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(10.dp)
    ) {
        ListItem(
            headlineContent = { Text(label, fontWeight = FontWeight.Medium) },
            supportingContent = { Text(subtitle, color = TextSecondary) },
            leadingContent = { Icon(icon, contentDescription = null, tint = PrimaryGreen) },
            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary) }
        )
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Language
            Text("Language / భాష", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            listOf("English", "Telugu (తెలుగు)").forEach { lang ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedLanguage = lang }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(lang, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Language switching will be fully applied in the next version.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Accessibility note
            Text("Accessibility", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "• Large button targets for easy tapping\n" +
                "• High contrast text\n" +
                "• Supports system font size scaling\n" +
                "• Voice guidance via Kavacham Amma",
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("About", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Sankalp Kavacham v1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text("Package: com.sentinel.protection", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text("Made for India 🇮🇳 – Telugu First", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen)
        }
    }
}

