package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.state.*
import com.example.ui.theme.*

@Composable
fun SecurityScreen(
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    var isTwoFactorEnabled by remember { mutableStateOf(true) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var pinText by remember { mutableStateOf("") }

    // Change Password Form States
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordSuccess by remember { mutableStateOf<String?>(null) }

    // Collect Viewmodel States
    val changePasswordState by walletViewModel.changePasswordState.collectAsState()
    val sessionsState by walletViewModel.userSessionsState.collectAsState()
    val loginActivitiesState by walletViewModel.loginActivitiesState.collectAsState()

    // Dispatch fetches on start
    LaunchedEffect(Unit) {
        walletViewModel.fetchUserSessions()
        walletViewModel.fetchLoginActivities()
        walletViewModel.clearChangePasswordState()
    }

    // Handle Password Change response
    LaunchedEffect(changePasswordState) {
        when (val state = changePasswordState) {
            is WalletUiState.Success -> {
                passwordSuccess = state.data
                passwordError = null
                oldPassword = ""
                newPassword = ""
                confirmPassword = ""
            }
            is WalletUiState.Error -> {
                passwordError = state.message
                passwordSuccess = null
            }
            else -> {}
        }
    }

    ReusablePageContainer(
        title = "Vault Access Security Suites",
        subtitle = "Administer your secure encryption PINs, active credentials, and active physical tokens.",
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("security_back_btn")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White)
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Master Security Vault Active Shield Card
            item {
                BaseCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Shield protected", tint = SuccessGreen, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Vault Shield Active", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Biometrics and PIN secondary checks enabled.", fontSize = 11.sp, color = Slate500)
                        }
                    }
                }
            }

            // Interactive Controls Category Card
            item {
                BaseCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Core Protection Controls",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Divider(color = Slate800)

                        PreferenceSwitchRow(
                            title = "Two-Factor Authentication (2FA)",
                            subtitle = "Require code from email/SMS during login",
                            checked = isTwoFactorEnabled,
                            onCheckedChange = { isTwoFactorEnabled = it },
                            modifier = Modifier.testTag("switch_2fa")
                        )

                        PreferenceArrowRow(
                            title = "Change App PIN",
                            subtitle = "Currently set to 4-digit master PIN",
                            onClick = { showChangePinDialog = true }
                        )
                    }
                }
            }

            // Secure Change Password Card (Requested Profile Feature)
            item {
                BaseCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Change Account Password",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Divider(color = Slate800)

                        Text(
                            text = "Modify your system account password securely. Strong passwords include capitals, numbers, and symbols.",
                            fontSize = 11.sp,
                            color = Slate500
                        )

                        // Password update validation alerts
                        passwordError?.let {
                            Text(text = "❌ $it", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        passwordSuccess?.let {
                            Text(text = "✅ $it", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Current password
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Current Password", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                            OutlinedTextField(
                                value = oldPassword,
                                onValueChange = { oldPassword = it; passwordError = null },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("old_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = Slate800,
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // New password
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("New Password", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it; passwordError = null },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = Slate800,
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        // Confirm password
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Confirm New Password", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; passwordError = null },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("confirm_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = Slate800,
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit action
                        Button(
                            onClick = {
                                if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                                    passwordError = "Complete all password credentials fields."
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    passwordError = "Passwords do not match."
                                    return@Button
                                }
                                if (newPassword.length < 6) {
                                    passwordError = "Password must remain at least 6 characters."
                                    return@Button
                                }
                                walletViewModel.changePassword(oldPassword, newPassword, confirmPassword)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = changePasswordState !is WalletUiState.Loading,
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("change_password_btn")
                        ) {
                            if (changePasswordState is WalletUiState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Update Security Password", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Active Sessions list (Real-time dynamic cancellation)
            item {
                BaseCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Authorized Devices & Active Sessions",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Divider(color = Slate800)

                        when (val state = sessionsState) {
                            is WalletUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(24.dp))
                                }
                            }
                            is WalletUiState.Success -> {
                                state.data.forEach { session ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Smartphone, contentDescription = "Device info", tint = Slate500, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(session.deviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                                if (session.isCurrent) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(SuccessGreen.copy(alpha = 0.12f))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Current", color = SuccessGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            Text("${session.location} • IP: ${session.ipAddress} • Last Active: ${session.lastActive}", fontSize = 10.sp, color = Slate500)
                                        }

                                        if (!session.isCurrent) {
                                            TextButton(
                                                onClick = { walletViewModel.revokeSession(session.id) },
                                                colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                                            ) {
                                                Text("Revoke", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            else -> {
                                Text("No active security authorization session found.", color = Slate500, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Recent Login Activities list (Audit history sequence logs)
            item {
                BaseCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Login Authentication History Audit Logs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Divider(color = Slate800)

                        when (val state = loginActivitiesState) {
                            is WalletUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(24.dp))
                                }
                            }
                            is WalletUiState.Success -> {
                                state.data.forEach { activity ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (activity.status == "Success") SuccessGreen.copy(alpha = 0.12f)
                                                    else DangerRed.copy(alpha = 0.12f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (activity.status == "Success") Icons.Default.LockOpen else Icons.Default.Lock,
                                                contentDescription = "Lock",
                                                tint = if (activity.status == "Success") SuccessGreen else DangerRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(activity.device, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            Text("${activity.timestamp} • IP: ${activity.ipAddress} • ${activity.location}", fontSize = 10.sp, color = Slate500)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (activity.status == "Success") SuccessGreen.copy(alpha = 0.12f)
                                                    else DangerRed.copy(alpha = 0.12f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = activity.status.uppercase(),
                                                color = if (activity.status == "Success") SuccessGreen else DangerRed,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                            else -> {
                                Text("No authentication records discovered.", color = Slate500, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showChangePinDialog) {
        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Vault PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a new 4-digit master PIN to access PayNexa vault features.", fontSize = 13.sp, color = Slate500)
                    OutlinedTextField(
                        value = pinText,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinText = it },
                        label = { Text("New PIN") },
                        placeholder = { Text("••••") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_pin_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinText.length == 4) {
                            showChangePinDialog = false
                            pinText = ""
                        }
                    },
                    enabled = pinText.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Change", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("Cancel", color = Slate500)
                }
            }
        )
    }
}
