package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.theme.*
import com.example.ui.state.*
import com.example.ui.navigation.Screen

@Composable
fun SettingsScreen(
    walletViewModel: WalletViewModel,
    onNavigateTo: (String) -> Unit
) {
    val profileState by walletViewModel.userProfileState.collectAsState()
    var emailNotificationEnabled by remember { mutableStateOf(true) }
    var pushNotificationEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        walletViewModel.fetchUserProfile()
    }

    ReusablePageContainer(
        title = "Settings Ledger Profile",
        subtitle = "Manage your global portfolio tier, contact metadata, preferences, and security suites."
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Avatar Header Card
            item {
                BaseCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateTo(Screen.ProfileSettings.route) }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Avatar", tint = AccentBlue, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (profileState is WalletUiState.Success) (profileState as WalletUiState.Success<UserProfile>).data.fullName else "Farjan Amin",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Verified", color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = if (profileState is WalletUiState.Success) (profileState as WalletUiState.Success<UserProfile>).data.email else "Farjanamin1111@gmail.com",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = "Edit Profile", tint = Slate500, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Membership Badge / Tier
            item {
                BaseCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Tiers", tint = WarningOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tiers & Account Limit", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "You are currently on PayNexa Premium Executive tier. You enjoy higher transaction speeds & unlimited interbank asset clearing channels.",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            }

            // Settings navigation groups
            item {
                BaseCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Standard Configuration Layout",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Divider(color = Slate800)

                        PreferenceArrowRow(
                            title = "Edit Personal Profile Details",
                            subtitle = "Modify full legal name, register address contacts and domicile countries",
                            onClick = { onNavigateTo(Screen.ProfileSettings.route) }
                        )

                        PreferenceArrowRow(
                            title = "Vault Access Security & Audit Info",
                            subtitle = "Manage passwords, PIN, active security logs and authorization tokens",
                            onClick = { onNavigateTo(Screen.Security.route) }
                        )
                    }
                }
            }

            // Multi-channel Communication Alerts Preferences
            item {
                BaseCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Communication Alerts & Prefs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Divider(color = Slate800)

                        PreferenceSwitchRow(
                            title = "Email Cleared Bulletins",
                            subtitle = "Receive direct statements and interbank remittance dispatches via inbox",
                            checked = emailNotificationEnabled,
                            onCheckedChange = { emailNotificationEnabled = it },
                            modifier = Modifier.testTag("switch_email_notify")
                        )

                        PreferenceSwitchRow(
                            title = "Push Clearing Notifications",
                            subtitle = "Immediate alerts for incoming wire settlements directly to mobile device",
                            checked = pushNotificationEnabled,
                            onCheckedChange = { pushNotificationEnabled = it },
                            modifier = Modifier.testTag("switch_push_notify")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = Slate500)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue, checkedTrackColor = AccentBlue.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun PreferenceArrowRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = Slate500)
        }
        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = "Edit preference", tint = Slate500, modifier = Modifier.size(14.dp))
    }
}
