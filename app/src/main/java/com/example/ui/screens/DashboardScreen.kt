package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.components.StatCard
import com.example.ui.state.WalletViewModel
import com.example.ui.state.Wallet
import com.example.ui.state.WalletTransaction
import com.example.ui.state.Beneficiary
import com.example.ui.state.PayNexaNotification
import com.example.ui.theme.*

// --- SECURE WALLET DASHBOARD MODULE ---

@Composable
fun DashboardScreen(
    walletViewModel: WalletViewModel,
    onNavigateTo: (String) -> Unit
) {
    // --- DASHBOARD DATA HOOKS (Collecting real backend-ready active flows) ---
    val walletsList by walletViewModel.wallets.collectAsState()
    val transactionsList by walletViewModel.transactions.collectAsState()
    val beneficiariesList by walletViewModel.beneficiaries.collectAsState()
    val notificationsList by walletViewModel.notifications.collectAsState()

    // Trigger instant active sync
    LaunchedEffect(Unit) {
        walletViewModel.fetchWallets()
    }

    ReusablePageContainer(
        title = "Dashboard",
        subtitle = "Global Payments, Simplified."
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // WIDGET 1: Total Balance Card (Calculated dynamically across vaults with USD conversion values)
            item {
                val aggrUSD = walletViewModel.getAggregateBalanceInUSD()
                BaseCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryBlue.copy(alpha = 0.05f))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Combined Balance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$ ${String.format("%,.2f", aggrUSD)} USD",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "≈ ${String.format("%,.2f", aggrUSD * 117.5)} BDT",
                                    fontSize = 12.sp,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet Balance",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Divider(
                            color = Slate500.copy(alpha = 0.1f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Current Market Trend",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Trending Up",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+12.4% this week",
                                    fontSize = 11.sp,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // WIDGET 2: Quick Actions Card Grid (Send, Deposit, Transact, Beneficiaries)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Quick Actions Portal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardQuickAction(
                            title = "Send Money",
                            icon = Icons.Default.Send,
                            color = DangerRed,
                            onClick = { onNavigateTo("send_money") },
                            modifier = Modifier.weight(1f)
                        )
                        DashboardQuickAction(
                            title = "Add Money",
                            icon = Icons.Default.Add,
                            color = SuccessGreen,
                            onClick = { onNavigateTo("add_money") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardQuickAction(
                            title = "Ledger History",
                            icon = Icons.Default.SwapHoriz,
                            color = AccentBlue,
                            onClick = { onNavigateTo("transactions") },
                            modifier = Modifier.weight(1f)
                        )
                        DashboardQuickAction(
                            title = "Recipients",
                            icon = Icons.Default.People,
                            color = WarningOrange,
                            onClick = { onNavigateTo("beneficiaries") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // WIDGET 3: Balance by Currency Card (Vault Overview Segment)
            item {
                BaseCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Currency Vaults",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TextButton(onClick = { onNavigateTo("accounts") }) {
                                Text("View All", color = AccentBlue, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (walletsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active vaults found. Please open an account.",
                                    color = Slate500,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            walletsList.take(3).forEach { wallet ->
                                SimpleCurrencyBalanceRow(
                                    flag = wallet.flagEmoji,
                                    code = wallet.currencyCode,
                                    name = wallet.displayName,
                                    amount = "${wallet.symbol} ${String.format("%,.2f", wallet.balance)}",
                                    onClick = { onNavigateTo("accounts/${wallet.id}") }
                                )
                            }
                        }
                    }
                }
            }

            // WIDGET 4: Recent Transactions Card Ledger Preview
            item {
                BaseCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Transactions Ledger",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TextButton(onClick = { onNavigateTo("transactions") }) {
                                Text("View All", color = AccentBlue, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (transactionsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "Empty",
                                        tint = Slate500,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No transacted ledger logs available",
                                        color = Slate500,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            transactionsList.take(3).forEach { transaction ->
                                DashboardTransactionRow(transaction)
                            }
                        }
                    }
                }
            }

            // WIDGET 5: Beneficiaries Preview Card (Horizontal Recipient Quick Pay Roll)
            item {
                BaseCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stored Beneficiary Recipients",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TextButton(onClick = { onNavigateTo("beneficiaries") }) {
                                Text("Manage", color = AccentBlue, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (beneficiariesList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No stored recipients found.",
                                    color = Slate500,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                items(beneficiariesList) { beneficiary ->
                                    DashboardBeneficiaryItem(
                                        beneficiary = beneficiary,
                                        onClick = { onNavigateTo("send_money") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // WIDGET 6: Recent Activity / Notifications Preview Card
            item {
                BaseCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "System alerts & Notifications",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            TextButton(onClick = { onNavigateTo("notifications") }) {
                                Text("View All", color = AccentBlue, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (notificationsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active alerts in your Inbox.",
                                    color = Slate500,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            notificationsList.take(2).forEach { alert ->
                                DashboardNotificationItem(alert)
                            }
                        }
                    }
                }
            }

            // WIDGET 7: Spending Ring Chart Section (Wallet budget utilization split)
            item {
                BaseCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Vault Settlement Distribution",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(160.dp)
                        ) {
                            Canvas(modifier = Modifier.size(150.dp)) {
                                val strokeWidth = 35f
                                drawArc(
                                    color = AccentBlue,
                                    startAngle = -90f,
                                    sweepAngle = 125f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                drawArc(
                                    color = PrimaryBlue,
                                    startAngle = 35f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                drawArc(
                                    color = SuccessGreen,
                                    startAngle = 125f,
                                    sweepAngle = 65f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                                drawArc(
                                    color = WarningOrange,
                                    startAngle = 190f,
                                    sweepAngle = 80f,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Settlement Weight",
                                    fontSize = 10.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$ ${String.format("%,.2f", walletViewModel.getAggregateBalanceInUSD())}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "USD equivalent",
                                    fontSize = 10.sp,
                                    color = Slate500,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpendingLegendRow(color = AccentBlue, label = "USD checkings", percentage = "34.7%", amount = "Active")
                            SpendingLegendRow(color = PrimaryBlue, label = "EUR business index", percentage = "25.3%", amount = "Active")
                            SpendingLegendRow(color = SuccessGreen, label = "GBP personal portfolio", percentage = "18.4%", amount = "Active")
                            SpendingLegendRow(color = WarningOrange, label = "BDT guild currency", percentage = "21.6%", amount = "Active")
                        }
                    }
                }
            }
        }
    }
}

// --- REUSABLE SUB-WIDGET COMPONENTS ---

@Composable
fun DashboardQuickAction(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .height(56.dp)
            .testTag("action_btn_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Slate800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DashboardTransactionRow(transaction: WalletTransaction) {
    val isCredit = transaction.type == "CREDIT"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCredit) SuccessGreen.copy(alpha = 0.1f) else DangerRed.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (transaction.category) {
                    "Subscriptions" -> Icons.Default.Subscriptions
                    "Shopping" -> Icons.Default.ShoppingBag
                    "Services" -> Icons.Default.SettingsSuggest
                    "Transfers" -> Icons.Default.CompareArrows
                    "Deposit" -> Icons.Default.ArrowOutward
                    else -> Icons.Default.ReceiptLong
                }
                Icon(
                    imageVector = icon,
                    contentDescription = transaction.category,
                    tint = if (isCredit) SuccessGreen else DangerRed,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = transaction.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.category} • ${transaction.timestamp}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = "${if (isCredit) "+" else "-"} $ ${String.format("%,.2f", transaction.amount)}",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = if (isCredit) SuccessGreen else Color.White
            )
            Text(
                text = transaction.referenceCode,
                fontSize = 9.sp,
                color = Slate500
            )
        }
    }
}

@Composable
fun DashboardBeneficiaryItem(
    beneficiary: Beneficiary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Slate800)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = beneficiary.name,
                    tint = Slate500,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Slate900),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = beneficiary.flagEmoji,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = beneficiary.name.substringBefore(" "),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = beneficiary.country,
            fontSize = 9.sp,
            color = Slate500,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DashboardNotificationItem(alert: PayNexaNotification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (alert.isUrgent) DangerRed.copy(alpha = 0.1f) else AccentBlue.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (alert.isUrgent) DangerRed else AccentBlue,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = alert.body,
                fontSize = 11.sp,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = alert.timestamp,
                fontSize = 9.sp,
                color = Slate500,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun SimpleCurrencyBalanceRow(
    flag: String,
    code: String,
    name: String,
    amount: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = flag, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text(text = name, fontSize = 11.sp, color = Slate500)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Detail arrow", tint = Slate500, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SpendingLegendRow(color: Color, label: String, percentage: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "($percentage)", fontSize = 11.sp, color = Slate500)
        }
        Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
