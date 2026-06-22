package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
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

@Composable
fun NotificationsScreen() {
    val notificationList = remember {
        mutableStateListOf(
            NotificationData("Login from new device detected", "Streaming Android Emulator active sign-in from San Francisco, CA.", "May 24 • 10:15 AM", true),
            NotificationData("Password changed successfully", "Account credential was updated recently from dynamic IP range.", "May 20 • 02:40 PM", false),
            NotificationData("Two-factor authentication (2FA) active", "Shield protected. Vault access requires instant secondary checks.", "May 18 • 11:30 AM", false),
            NotificationData("Received $250.00 USD from Sarah", "Sarah Johnson instantly settled your freelance project payments.", "May 15 • 09:12 AM", false)
        )
    }

    ReusablePageContainer(
        title = "Notifications",
        subtitle = "Check logs and instant account activity alerts.",
        actionButton = {
            TextButton(
                onClick = {
                    notificationList.clear()
                },
                modifier = Modifier.testTag("clear_notifications_btn")
            ) {
                Text("Clear All", color = AccentBlue, fontWeight = FontWeight.Bold)
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (notificationList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "None", tint = Slate500, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active alerts.", color = Slate500, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                items(notificationList) { alert ->
                    BaseCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (alert.isUrgent) DangerRed.copy(alpha = 0.1f) else AccentBlue.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Alert info",
                                    tint = if (alert.isUrgent) DangerRed else AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = alert.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = alert.body, fontSize = 11.sp, color = Slate500)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = alert.time, fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NotificationData(
    val title: String,
    val body: String,
    val time: String,
    val isUrgent: Boolean
)
