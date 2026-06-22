package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.state.WalletUiState
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.*

@Composable
fun TransactionDetailScreen(
    transactionId: String?,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    if (transactionId == null) {
        onNavigateBack()
        return
    }

    val selectedTxState by walletViewModel.selectedTransactionState.collectAsState()

    // Trigger details fetching on mount or route transition
    LaunchedEffect(transactionId) {
        walletViewModel.fetchTransactionDetails(transactionId)
    }

    ReusablePageContainer(
        title = "Transaction Audit",
        subtitle = "Detailed blockchain receipt and settlement validation records.",
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("back_to_tx_list_btn")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = AccentBlue)
            }
        }
    ) {
        when (val state = selectedTxState) {
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
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", tint = DangerRed, modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = state.message, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            is WalletUiState.Success -> {
                val tx = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // --- PROFILE HERO HEADER CARD ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.type == "CREDIT") SuccessGreen.copy(alpha = 0.12f)
                                        else DangerRed.copy(alpha = 0.12f)
                                    ),
                                contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     imageVector = if (tx.type == "CREDIT") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                     contentDescription = "Transaction Type Hero Icon",
                                     tint = if (tx.type == "CREDIT") SuccessGreen else DangerRed,
                                     modifier = Modifier.size(36.dp)
                                 )
                             }
                             Spacer(modifier = Modifier.height(16.dp))
                             Text(
                                 text = if (tx.type == "CREDIT") "Inbound Deposit Received" else "Outbound Settlement Dispatched",
                                 fontSize = 12.sp,
                                 fontWeight = FontWeight.Bold,
                                 color = Slate500,
                                 letterSpacing = 1.sp
                             )
                             Spacer(modifier = Modifier.height(6.dp))
                             Text(
                                 text = (if (tx.type == "CREDIT") "+ " else "- ") + tx.amount,
                                 fontSize = 26.sp,
                                 fontWeight = FontWeight.Black,
                                 color = if (tx.type == "CREDIT") SuccessGreen else Color.White
                             )
                             Spacer(modifier = Modifier.height(10.dp))
                             Box(
                                 modifier = Modifier
                                     .clip(RoundedCornerShape(6.dp))
                                     .background(
                                         when (tx.status) {
                                             "Completed" -> SuccessGreen.copy(alpha = 0.15f)
                                             "Pending" -> WarningOrange.copy(alpha = 0.15f)
                                             else -> DangerRed.copy(alpha = 0.15f)
                                         }
                                     )
                                     .padding(horizontal = 12.dp, vertical = 4.dp)
                             ) {
                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                     Box(
                                         modifier = Modifier
                                             .size(6.dp)
                                             .clip(CircleShape)
                                             .background(
                                                 when (tx.status) {
                                                     "Completed" -> SuccessGreen
                                                     "Pending" -> WarningOrange
                                                     else -> DangerRed
                                                 }
                                             )
                                     )
                                     Spacer(modifier = Modifier.width(6.dp))
                                     Text(
                                         text = tx.status.uppercase(),
                                         fontSize = 10.sp,
                                         fontWeight = FontWeight.Bold,
                                         color = when (tx.status) {
                                             "Completed" -> SuccessGreen
                                             "Pending" -> WarningOrange
                                             else -> DangerRed
                                         }
                                     )
                                 }
                             }
                        }
                    }

                    // --- DETAILED LEDGER TABLE METADATA ---
                    BaseCard {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Transaction Parameters",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // Title Description
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Description / Profile", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                Text(tx.title, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("tx_detail_title"))
                            }
                            Divider(color = Slate800)

                            // Reference Hash Code
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Transaction Reference", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                Text(tx.referenceCode, fontSize = 13.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = Slate800)

                            // Category Parameters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Asset Classification", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                Text(tx.category, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = Slate800)

                            // Settlement Date / Timestamp
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Validation Timestamp", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                Text(tx.timestamp, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Divider(color = Slate800)

                            // Protocol Networks Fees
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Network Fuel/Gas Fee", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Zero fees", tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Free Settlement", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // --- SECURITY/COMPLIANCE BANNER ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryBlue.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Validated safe ledger",
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cryptographic Root Proof",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "This transaction has been permanently recorded onto the PayNexa hybrid ledger. All currency exchange limits, compliance parameters, and security signatures were authenticated successfully.",
                                fontSize = 11.sp,
                                color = Slate500,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // --- ACTION SHORTCUTS ---
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("back_to_audit_list_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Return to Ledger Directory", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            else -> {}
        }
    }
}
