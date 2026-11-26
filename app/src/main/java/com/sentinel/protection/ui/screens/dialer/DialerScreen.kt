package com.sentinel.protection.ui.screens.dialer

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sentinel.protection.domain.model.CallRecord
import com.sentinel.protection.domain.model.CallType
import com.sentinel.protection.domain.model.NumberCheckResult
import com.sentinel.protection.domain.model.RiskLevel
import com.sentinel.protection.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DialerScreen(
    onNavigateTo: (String) -> Unit,
    onNumberCheck: (String) -> Unit,
    viewModel: DialerViewModel = hiltViewModel()
) {
    val dialedNumber by viewModel.dialedNumber.collectAsStateWithLifecycle()
    val numberResult by viewModel.numberCheckResult.collectAsStateWithLifecycle()
    val recentCalls by viewModel.recentCalls.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val callLogPermission = rememberPermissionState(Manifest.permission.READ_CALL_LOG)
    val contactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)
    val callPhonePermission = rememberPermissionState(Manifest.permission.CALL_PHONE)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Keypad", "Recents", "Contacts")

    LaunchedEffect(callLogPermission.status.isGranted) {
        viewModel.loadRecentCalls(callLogPermission.status.isGranted)
    }
    LaunchedEffect(contactsPermission.status.isGranted) {
        viewModel.loadContacts(contactsPermission.status.isGranted)
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardWhite,
            contentColor = PrimaryGreen
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        when (selectedTab) {
            0 -> KeypadTab(
                dialedNumber = dialedNumber,
                numberResult = numberResult,
                isLoading = isLoading,
                onDigit = viewModel::appendDigit,
                onBackspace = viewModel::backspace,
                onClear = viewModel::clearNumber,
                onCheckNumber = { viewModel.checkNumber() },
                onCall = {
                    if (dialedNumber.isNotBlank()) {
                        if (callPhonePermission.status.isGranted) {
                            try {
                                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$dialedNumber"))
                                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            } catch (e: Exception) { /* Fallback to ACTION_DIAL */ }
                        } else {
                            callPhonePermission.launchPermissionRequest()
                        }
                    }
                }
            )
            1 -> RecentsTab(
                calls = recentCalls,
                hasPermission = callLogPermission.status.isGranted,
                onRequestPermission = { callLogPermission.launchPermissionRequest() },
                onNumberCheck = onNumberCheck,
                onCall = { number ->
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: Exception) { }
                }
            )
            2 -> ContactsTab(
                contacts = contacts,
                hasPermission = contactsPermission.status.isGranted,
                onRequestPermission = { contactsPermission.launchPermissionRequest() },
                onCall = { number ->
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: Exception) { }
                }
            )
        }
    }
}

@Composable
private fun KeypadTab(
    dialedNumber: String,
    numberResult: NumberCheckResult?,
    isLoading: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCheckNumber: () -> Unit,
    onCall: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Number display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dialedNumber.ifEmpty { "Enter Number" },
                style = MaterialTheme.typography.displaySmall.copy(
                    color = if (dialedNumber.isEmpty()) TextSecondary else TextPrimary,
                    fontSize = 28.sp
                ),
                modifier = Modifier.weight(1f)
            )
            if (dialedNumber.isNotEmpty()) {
                IconButton(onClick = onBackspace, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Check result
        numberResult?.let { result ->
            NumberCheckResultCard(result)
            Spacer(Modifier.height(8.dp))
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryGreen)
            Spacer(Modifier.height(8.dp))
        }

        // Keypad grid
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    DialerKey(label = key, onClick = { onDigit(key) })
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Check number button
            if (dialedNumber.isNotEmpty()) {
                OutlinedButton(
                    onClick = onCheckNumber,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Check")
                }
            }

            // Call button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreen)
                    .clickable(onClick = onCall),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun NumberCheckResultCard(result: NumberCheckResult) {
    val (bgColor, textColor, label) = when (result.riskLevel) {
        RiskLevel.GREEN -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "✅ Low Risk")
        RiskLevel.ORANGE -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), "⚠️ Suspicious")
        RiskLevel.RED -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "🚨 High Risk")
        RiskLevel.UNKNOWN -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "❓ Unknown")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (!result.isValid) {
                Text("Invalid number format", color = DangerRed, fontWeight = FontWeight.Medium)
                return@Column
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(result.formattedNumber, style = MaterialTheme.typography.bodySmall, color = textColor)
            result.contactName?.let {
                Text("Contact: $it", style = MaterialTheme.typography.bodySmall, color = textColor)
            }
            if (result.reportCount > 0) {
                Text(
                    "Reported by ${result.reportCount} user(s) in this app",
                    style = MaterialTheme.typography.bodySmall, color = textColor
                )
            }
            Text(result.explanation, style = MaterialTheme.typography.labelSmall, color = textColor)
        }
    }
}

@Composable
private fun DialerKey(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(CardWhite)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

@Composable
fun RecentsTab(
    calls: List<CallRecord>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onNumberCheck: (String) -> Unit,
    onCall: (String) -> Unit
) {
    if (!hasPermission) {
        PermissionRequiredCard(
            permission = "Call Log",
            reason = "To show your recent calls, we need access to call history.",
            onRequest = onRequestPermission
        )
        return
    }

    if (calls.isEmpty()) {
        EmptyState(message = "No recent calls")
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(calls) { call ->
            CallRecordItem(call = call, onCall = { onCall(call.phoneNumber) }, onCheck = { onNumberCheck(call.phoneNumber) })
        }
    }
}

@Composable
private fun CallRecordItem(call: CallRecord, onCall: () -> Unit, onCheck: () -> Unit) {
    val typeIcon = when (call.type) {
        CallType.INCOMING -> Icons.Default.CallReceived
        CallType.OUTGOING -> Icons.Default.CallMade
        CallType.MISSED -> Icons.Default.CallMissed
        CallType.REJECTED -> Icons.Default.CallMissed
        CallType.BLOCKED -> Icons.Default.Block
    }
    val typeColor = when (call.type) {
        CallType.MISSED, CallType.REJECTED -> DangerRed
        CallType.BLOCKED -> TextSecondary
        else -> PrimaryGreen
    }

    ListItem(
        headlineContent = {
            Text(call.contactName ?: call.phoneNumber, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
            Text("${call.type.name.lowercase().replaceFirstChar { it.uppercase() }} • ${sdf.format(Date(call.timestamp))}")
        },
        leadingContent = {
            Icon(typeIcon, contentDescription = call.type.name, tint = typeColor)
        },
        trailingContent = {
            Row {
                IconButton(onClick = onCheck, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Search, contentDescription = "Check", tint = PrimaryGreen)
                }
                IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = PrimaryGreen)
                }
            }
        }
    )
    HorizontalDivider(color = Color(0xFFF1F5F9))
}

@Composable
fun ContactsTab(
    contacts: List<Pair<String, String>>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onCall: (String) -> Unit
) {
    if (!hasPermission) {
        PermissionRequiredCard("Contacts", "To show your contacts.", onRequestPermission)
        return
    }
    if (contacts.isEmpty()) {
        EmptyState(message = "No contacts found")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(contacts) { (name, number) ->
            ListItem(
                headlineContent = { Text(name, fontWeight = FontWeight.Medium) },
                supportingContent = { Text(number, color = TextSecondary) },
                leadingContent = {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(LightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.firstOrNull()?.uppercase() ?: "?",
                            color = PrimaryGreen, fontWeight = FontWeight.Bold
                        )
                    }
                },
                trailingContent = {
                    IconButton(onClick = { onCall(number) }) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = PrimaryGreen)
                    }
                }
            )
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }
    }
}

@Composable
fun NumberCheckScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    viewModel: DialerViewModel = hiltViewModel()
) {
    val result by viewModel.numberCheckResult.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(phoneNumber) {
        viewModel.setNumber(phoneNumber)
        if (phoneNumber.isNotBlank()) viewModel.checkNumber(phoneNumber)
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopAppBar(
            title = { Text("Number Check") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
        )
        Column(modifier = Modifier.padding(16.dp)) {
            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = PrimaryGreen)
            result?.let { NumberCheckResultCard(it) }
        }
    }
}

@Composable
fun CallsScreen(onNumberCheck: (String) -> Unit, viewModel: DialerViewModel = hiltViewModel()) {
    val calls by viewModel.recentCalls.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        TopAppBar(title = { Text("Call History") })
        RecentsTab(
            calls = calls,
            hasPermission = true,
            onRequestPermission = {},
            onNumberCheck = onNumberCheck,
            onCall = { number ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: Exception) { }
            }
        )
    }
}

@Composable
private fun PermissionRequiredCard(permission: String, reason: String, onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text("$permission Permission Required", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(reason, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequest, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
    }
}

