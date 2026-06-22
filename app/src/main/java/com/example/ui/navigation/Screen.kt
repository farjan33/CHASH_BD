package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Accounts : Screen("accounts", "Accounts", Icons.Default.AccountBalance)
    object AddMoney : Screen("add_money", "Add Money", Icons.Default.AddCircle)
    object SendMoney : Screen("send_money", "Send Money", Icons.Default.Send)
    object Beneficiaries : Screen("beneficiaries", "Beneficiaries", Icons.Default.People)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.SwapHoriz)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Security : Screen("settings/security", "Security", Icons.Default.Security)
    object ProfileSettings : Screen("settings/profile", "Profile", Icons.Default.Person)
    object Notifications : Screen("notifications", "Notifications", Icons.Default.Notifications)

    // Auth Modules Routes
    object Login : Screen("login", "Login", Icons.Default.Security)
    object Register : Screen("register", "Register", Icons.Default.Security)
    object ForgotPassword : Screen("forgot_password", "Forgot Password", Icons.Default.Security)

    // Wallet Detail Routing Route
    object AccountDetails : Screen("accounts/{walletId}", "Account Details", Icons.Default.AccountBalance) {
        fun createRoute(walletId: String): String = "accounts/$walletId"
    }

    object AddBeneficiary : Screen("beneficiaries/new", "Add Beneficiary", Icons.Default.People)

    object BeneficiaryDetails : Screen("beneficiaries/{beneficiaryId}", "Beneficiary Details", Icons.Default.People) {
        fun createRoute(beneficiaryId: String): String = "beneficiaries/$beneficiaryId"
    }

    object TransactionDetails : Screen("transactions/{transactionId}", "Transaction Details", Icons.Default.SwapHoriz) {
        fun createRoute(transactionId: String): String = "transactions/$transactionId"
    }

    companion object {
        val sidebarItems: List<Screen>
            get() = listOf(
                Dashboard,
                Accounts,
                SendMoney,
                AddMoney,
                Transactions,
                Beneficiaries,
                Notifications,
                Settings,
                Security
            )
    }
}
