package com.sentinel.protection.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Dialer : Screen("dialer")
    object Calls : Screen("calls")
    object SpamBox : Screen("spam_box")
    object Assistant : Screen("assistant")
    object Profile : Screen("profile")
    object NumberCheck : Screen("number_check/{number}") {
        fun createRoute(number: String) = "number_check/$number"
    }
    object MessageScan : Screen("message_scan")
    object HoneyTrap : Screen("honey_trap")
    object MediaChecker : Screen("media_checker")
    object Privacy : Screen("privacy")
    object BlockList : Screen("block_list")
    object Settings : Screen("settings")
    object CyberHelp : Screen("cyber_help")
}

