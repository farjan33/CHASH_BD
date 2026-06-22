package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.state.Wallet
import com.example.ui.state.WalletTransaction
import com.example.ui.state.WalletUiState
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailScreen(
    walletId: String?,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Trigger api round-trip fetch on start
    LaunchedEffect(walletId) {
        if (walletId != null) {
            walletViewModel.fetchWalletDetails(walletId)
        }
    }

    val selectedState by walletViewModel.selectedWalletState.collectAsState()
    val allTransactions by walletViewModel.transactions.collectAsState()

    // Deposit simulator fields
    var depositAmountText by remember { mutableStateOf("") }
    var showDepositDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp)
    ) {
        when (val state = selectedState) {
            is WalletUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
            is WalletUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Wallet error status",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = state.message, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Back to Accounts", fontWeight = FontWeight.Bold)
                    }
                }
            }
            is WalletUiState.Success -> {
                val wallet = state.data
                val filteredTransactions = allTransactions.filter { it.walletId == wallet.id }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header navigation bar
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.testTag("wallet_details_back_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Return back", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = wallet.displayName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${wallet.currencyCode} Vault Portal",
                                        fontSize = 12.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            // Dynamic active badge status pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = wallet.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }

                    // Card style Hero Balance Summary component
                    item {
                        BaseCard {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PrimaryBlue.copy(alpha = 0.05f))
                                    .padding(24.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = wallet.flagEmoji, fontSize = 28.sp)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Available Vault Balance",
                                        fontSize = 11.sp,
                                        color = Slate500,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Formatted Balance Display
                                    Text(
                                        text = "${wallet.symbol} ${String.format("%,.2f", wallet.balance)}",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "Verified on Central Remittance Network",
                                        fontSize = 11.sp,
                                        color = AccentBlue,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Account operations
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = { showDepositDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .testTag("wallet_deposit_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.AddCard, contentDescription = "Deposit funds")
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Deposit Funds", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Vault Routing & Global Settling credentials
                    item {
                        BaseCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Global Routing Credentials",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    text = wallet.bankName,
                                    fontSize = 11.sp,
                                    color = Slate500,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                                )

                                Divider(color = Slate500.copy(alpha = 0.15f))

                                credentialRow(
                                    label = "ACH ROUTING NUMBER",
                                    value = wallet.routingNumber,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(wallet.routingNumber))
                                        Toast.makeText(context, "Routing Number copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    testTagValue = "copy_routing_num"
                                )

                                credentialRow(
                                    label = "BANK ACCOUNT NUMBER",
                                    value = wallet.accountNumber,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(wallet.accountNumber))
                                        Toast.makeText(context, "Account Number copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    testTagValue = "copy_account_num"
                                )
                            }
                        }
                    }

                    // Transaction Table Section / Preview
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaction History Ledger",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentBlue.copy(alpha = 0.1f))
                                    .padding(vertical = 4.dp, horizontal = 10.dp)
                            ) {
                                Text(
                                    text = "${filteredTransactions.size} Transacted",
                                    color = AccentBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (filteredTransactions.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Slate500.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inbox,
                                        contentDescription = "Empty History",
                                        tint = Slate500,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No recorded ledger logs found",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Fund this account or execute payments to record transactions securely.",
                                    color = Slate500,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
                                )
                            }
                        }
                    } else {
                        items(items = filteredTransactions, key = { it.id }) { transaction ->
                            TransactionRowItem(transaction)
                        }
                    }
                }
            }
            else -> { /* Idle */ }
        }
    }

    // Interactive simulated top-up modal popup
    if (showDepositDialog) {
        val successState = selectedState as? WalletUiState.Success
        if (successState != null) {
            val wallet = successState.data
            AlertDialog(
                onDismissRequest = {
                    showDepositDialog = false
                    depositAmountText = ""
                },
                title = { Text("Simulate Deposit") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Perform an instant deposit mock simulation to top-up your ${wallet.displayName} wallet.",
                            fontSize = 13.sp,
                            color = Slate500
                        )

                        OutlinedTextField(
                            value = depositAmountText,
                            onValueChange = { depositAmountText = it },
                            label = { Text("Transfer Amount (${wallet.symbol})") },
                            placeholder = { Text("e.g. 500.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
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
                                .testTag("deposit_sum_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val doubleAmount = depositAmountText.toDoubleOrNull()
                            if (doubleAmount != null && doubleAmount > 0.0) {
                                walletViewModel.addFundsToWallet(wallet.id, doubleAmount)
                                Toast.makeText(context, "${wallet.symbol} ${String.format("%.2f", doubleAmount)} deposited successfully!", Toast.LENGTH_LONG).show()
                                showDepositDialog = false
                                depositAmountText = ""
                            } else {
                                Toast.makeText(context, "Please enter a valid deposit quantity", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.testTag("submit_deposit_confirmation")
                    ) {
                        Text("Confirm Deposit", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDepositDialog = false
                        depositAmountText = ""
                    }) {
                        Text("Cancel", color = Slate500)
                    }
                }
            )
        }
    }
}

@Composable
fun credentialRow(
    label: String,
    value: String,
    onCopy: () -> Unit,
    testTagValue: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Slate500, letterSpacing = 1.sp)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(top = 4.dp))
        }

        IconButton(
            onClick = onCopy,
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                .testTag(testTagValue)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy credential value",
                tint = AccentBlue,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun TransactionRowItem(transaction: WalletTransaction) {
    val isCredit = transaction.type == "CREDIT"
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circle category helper
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCredit) SuccessGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
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
                        tint = if (isCredit) SuccessGreen else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = transaction.category,
                            fontSize = 11.sp,
                            color = Slate500,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Slate500)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = transaction.timestamp,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
            }

            // Ledger quantity badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"} ${String.format("%.2f", transaction.amount)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
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
}
