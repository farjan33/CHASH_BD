package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.state.Wallet
import com.example.ui.state.WalletUiState
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.Slate500
import com.example.ui.theme.SuccessGreen

@Composable
fun AccountsScreen(
    walletViewModel: WalletViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var accountNickName by remember { mutableStateOf("") }

    // Fetch live wallets from remote simulation server
    LaunchedEffect(Unit) {
        walletViewModel.fetchWallets()
    }

    val walletsState by walletViewModel.walletsFetchState.collectAsState()

    ReusablePageContainer(
        title = "Accounts",
        subtitle = "Manage your global multi-currency accounts and wallets.",
        actionButton = {
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("open_account_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Account")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Account", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        when (val state = walletsState) {
            is WalletUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
            is WalletUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load wallets list: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            is WalletUiState.Success -> {
                val walletsList = state.data

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Dynamic Asset Balance Summary Distribution Card
                    item {
                        BaseCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Account Distribution",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Calculate real dynamic distribution weights in USD equivalent
                                val usdBalances = walletsList.map { wallet ->
                                    val conversionRate = when (wallet.currencyCode) {
                                        "USD" -> 1.0
                                        "EUR" -> 1.08
                                        "GBP" -> 1.27
                                        "BDT" -> 0.0085
                                        else -> 1.0
                                    }
                                    wallet to (wallet.balance * conversionRate)
                                }
                                val sumUsd = usdBalances.sumOf { it.second }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    if (sumUsd <= 0.0) {
                                        // Standard even default fallback distribution bar if all wallets are empty
                                        walletsList.forEachIndexed { idx, _ ->
                                            val color = when (idx % 4) {
                                                0 -> AccentBlue
                                                1 -> Color(0xFF10B981)
                                                2 -> Color(0xFFF59E0B)
                                                else -> Color(0xFFEF4444)
                                            }
                                            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(color))
                                        }
                                    } else {
                                        usdBalances.forEachIndexed { idx, pair ->
                                            val (wallet, usdVal) = pair
                                            val weight = (usdVal / sumUsd).toFloat().coerceAtLeast(0.02f)
                                            val color = when (wallet.currencyCode) {
                                                "USD" -> AccentBlue
                                                "EUR" -> Color(0xFF10B981)
                                                "GBP" -> Color(0xFFF59E0B)
                                                "BDT" -> Color(0xFFEF4444)
                                                else -> Color(0xFF8B5CF6)
                                            }
                                            Box(modifier = Modifier.weight(weight).fillMaxHeight().background(color))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Distribution Legends Section
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (walletsList.isEmpty()) {
                                        Text("No opened accounts to distribute.", color = Slate500, fontSize = 11.sp)
                                    } else {
                                        walletsList.take(4).forEach { wallet ->
                                            val conversionRate = when (wallet.currencyCode) {
                                                "USD" -> 1.0
                                                "EUR" -> 1.08
                                                "GBP" -> 1.27
                                                "BDT" -> 0.0085
                                                else -> 1.0
                                            }
                                            val usdVal = wallet.balance * conversionRate
                                            val percentage = if (sumUsd > 0.0) {
                                                (usdVal / sumUsd * 100).toInt()
                                            } else {
                                                0
                                            }
                                            val legendColor = when (wallet.currencyCode) {
                                                "USD" -> AccentBlue
                                                "EUR" -> Color(0xFF10B981)
                                                "GBP" -> Color(0xFFF59E0B)
                                                "BDT" -> Color(0xFFEF4444)
                                                else -> Color(0xFF8B5CF6)
                                            }
                                            DistributionIndicator(color = legendColor, currency = "${wallet.currencyCode} ($percentage%)")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Wallet Cards List
                    items(walletsList.size) { index ->
                        val wallet = walletsList[index]
                        BaseCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToDetail(wallet.id) }
                                    .padding(16.dp)
                                    .testTag("wallet_card_${wallet.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(wallet.flagEmoji, fontSize = 24.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = wallet.currencyCode, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = wallet.status,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                        }
                                        Text(text = wallet.displayName, fontSize = 12.sp, color = Slate500)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${wallet.symbol} ${String.format("%,.2f", wallet.balance)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForwardIos,
                                        contentDescription = "Details",
                                        tint = Slate500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> { /* Idle State */ }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Open Global Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select a currency and enter a nickname to instantly open a new global routing account.",
                        fontSize = 13.sp,
                        color = Slate500
                    )
                    
                    Text("Currency", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("USD", "EUR", "GBP", "CAD").forEach { code ->
                            val isSelected = selectedCurrency == code
                            OutlinedButton(
                                onClick = { selectedCurrency = code },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) AccentBlue.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (isSelected) AccentBlue else Slate500
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("currency_select_$code")
                            ) {
                                Text(code, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = accountNickName,
                        onValueChange = { accountNickName = it },
                        label = { Text("Account Nickname") },
                        placeholder = { Text("e.g. Travel pocket") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Slate500.copy(alpha = 0.4f),
                            focusedLabelColor = AccentBlue,
                            unfocusedLabelColor = Slate500,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nickname_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        walletViewModel.createWallet(selectedCurrency, accountNickName)
                        showCreateDialog = false
                        accountNickName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier.testTag("confirm_open_wallet")
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = Slate500)
                }
            }
        )
    }
}

@Composable
fun DistributionIndicator(color: Color, currency: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = currency, fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
    }
}
