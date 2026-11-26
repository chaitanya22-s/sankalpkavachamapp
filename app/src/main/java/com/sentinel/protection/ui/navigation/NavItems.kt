package com.sentinel.protection.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val contentDescription: String
)

val bottomNavItems = listOf(
    BottomNavItem("Dialer", Screen.Dialer.route, Icons.Default.Dialpad, "Open dialer"),
    BottomNavItem("Calls", Screen.Calls.route, Icons.Default.History, "Call history"),
    BottomNavItem("Spam Box", Screen.SpamBox.route, Icons.Default.Shield, "Spam messages"),
    BottomNavItem("Amma", Screen.Assistant.route, Icons.Default.RecordVoiceOver, "Voice assistant"),
    BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person, "Profile and settings")
)

