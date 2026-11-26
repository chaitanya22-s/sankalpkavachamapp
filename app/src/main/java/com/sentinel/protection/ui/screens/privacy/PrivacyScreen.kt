package com.sentinel.protection.ui.screens.privacy

import android.Manifest
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.sentinel.protection.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Privacy Centre") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Privacy principles card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Your Privacy Matters", style = MaterialTheme.typography.headlineMedium, color = DarkGreen)
                    }
                    Spacer(Modifier.height(12.dp))
                    listOf(
                        "✅ No advertisements",
                        "✅ No paid premium features",
                        "✅ Your data is never sold",
                        "✅ Contacts not uploaded without permission",
                        "✅ On-device processing wherever possible",
                        "✅ Clear permission management",
                        "✅ You can delete all local data anytime"
                    ).forEach { principle ->
                        Text(principle, style = MaterialTheme.typography.bodyMedium, color = DarkGreen, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Permission Dashboard
            Text("Permission Dashboard", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                "These permissions are requested only when you use the relevant features.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))

            data class PermInfo(val name: String, val icon: ImageVector, val perm: String, val purpose: String)

            val permInfoList = listOf(
                PermInfo("Contacts", Icons.Default.Contacts, Manifest.permission.READ_CONTACTS, "Number lookup in your phonebook"),
                PermInfo("Phone & Calls", Icons.Default.Phone, Manifest.permission.READ_PHONE_STATE, "Call screening and protection"),
                PermInfo("Call Log", Icons.Default.History, Manifest.permission.READ_CALL_LOG, "Show recent calls"),
                PermInfo("SMS", Icons.Default.Message, Manifest.permission.READ_SMS, "SMS spam detection"),
                PermInfo("Microphone", Icons.Default.Mic, Manifest.permission.RECORD_AUDIO, "Voice assistant"),
                PermInfo("Camera", Icons.Default.CameraAlt, Manifest.permission.CAMERA, "Media analysis")
            )

            permInfoList.forEach { info ->
                val permState = rememberPermissionState(info.perm)
                PermissionRow(
                    name = info.name,
                    icon = info.icon,
                    purpose = info.purpose,
                    isGranted = permState.status.isGranted,
                    onGrant = { permState.launchPermissionRequest() }
                )
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Data management
            Text("Data Management", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    ListItem(
                        headlineContent = { Text("All processing is on-device") },
                        supportingContent = { Text("SMS analysis, number lookups, and media checks run locally") },
                        leadingContent = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryGreen) }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    ListItem(
                        headlineContent = { Text("No cloud data collection") },
                        supportingContent = { Text("Reports you submit stay on this device") },
                        leadingContent = { Icon(Icons.Default.CloudOff, contentDescription = null, tint = PrimaryGreen) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    name: String,
    icon: ImageVector,
    purpose: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isGranted) PrimaryGreen else WarningOrange, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                Text(purpose, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (isGranted) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = PrimaryGreen)
            } else {
                TextButton(onClick = onGrant) {
                    Text("Grant", color = WarningOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CyberHelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Emergency Cyber Help") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
        )

        Column(modifier = Modifier.padding(16.dp)) {

            // Emergency Call Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Emergency, contentDescription = null, tint = DangerRed, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Indian Cyber Crime Helpline", style = MaterialTheme.typography.headlineMedium, color = DangerRed)
                    Text("1930", style = MaterialTheme.typography.displayLarge, color = DangerRed, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:1930"))
                                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (e: Exception) { }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Call 1930 Now", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Online Portal Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("File Online Complaint", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF1E40AF))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Visit the official Indian Cybercrime portal to file a formal complaint. " +
                        "You will need to complete the official process on the government website.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1E40AF)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in/"))
                                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (e: Exception) { }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open cybercrime.gov.in")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // What to do steps
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("What to do if you're a fraud victim", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "1. Do NOT send any more money or share more information",
                        "2. Take screenshots of all conversations as evidence",
                        "3. Call 1930 immediately (available 24/7)",
                        "4. File online complaint at cybercrime.gov.in",
                        "5. Block the number on your phone",
                        "6. Contact your bank immediately if money was transferred"
                    ).forEach { step ->
                        Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "⚠️ Disclaimer: Sankalp Kavacham is a helper tool. Filing an official complaint requires completing the process on 1930 or the official portal. We cannot file complaints on your behalf.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

