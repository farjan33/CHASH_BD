package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.state.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    walletViewModel: WalletViewModel,
    onNavigateTo: (String) -> Unit
) {
    // Collect server-side UI states
    val walletsState by walletViewModel.walletsFetchState.collectAsState()
    val beneficiariesState by walletViewModel.beneficiariesFetchState.collectAsState()
    val previewState by walletViewModel.transferPreviewState.collectAsState()
    val submitState by walletViewModel.transferSubmitState.collectAsState()

    // Form control parameters
    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedBeneficiary by remember { mutableStateOf<Beneficiary?>(null) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    // Client-side Zod-like validation errors
    var walletError by remember { mutableStateOf<String?>(null) }
    var beneficiaryError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    // Dropdown expanding state
    var isWalletExpanded by remember { mutableStateOf(false) }
    var isBeneficiaryExpanded by remember { mutableStateOf(false) }

    // Mount lifecycle synchronization
    LaunchedEffect(Unit) {
        walletViewModel.fetchWallets()
        walletViewModel.fetchBeneficiaries()
        walletViewModel.clearTransferStates()
    }

    // Default select initial wallet & beneficiary when fetched successfully
    LaunchedEffect(walletsState) {
        val state = walletsState
        if (state is WalletUiState.Success && state.data.isNotEmpty() && selectedWallet == null) {
            selectedWallet = state.data.first()
        }
    }
    LaunchedEffect(beneficiariesState) {
        val state = beneficiariesState
        if (state is WalletUiState.Success && state.data.isNotEmpty() && selectedBeneficiary == null) {
            selectedBeneficiary = state.data.first()
        }
    }

    ReusablePageContainer(
        title = "Interbank Asset Remit",
        subtitle = "Securely dispatch settlement payouts across connected international channels and digital gateways."
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (submitState is WalletUiState.Success) {
                // --- SUCCESS TICKET VIEW (Clearing complete) ---
                val receipt = (submitState as WalletUiState.Success<TransferRecord>).data
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success tick",
                            tint = SuccessGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Text(
                        text = "Assets Fully Settled and Cleared",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Your electronic wire instructions have been executed successfully via the PayNexa Clearing Protocols.",
                        fontSize = 12.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    // Ticket Card info table
                    BaseCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Settlement ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text(receipt.referenceCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Divider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Source Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text(receipt.walletName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Divider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Inbound Beneficiary", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text(receipt.beneficiaryName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Divider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remitted Sum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text("${receipt.currency} ${String.format(Locale.US, "%.2f", receipt.amount)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Divider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Conversion Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text("1 ${receipt.currency} = ${receipt.exchangeRate} ${receipt.destCurrency}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                            }
                            Divider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Exchanged Inflow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text("${receipt.destCurrency} ${String.format(Locale.US, "%.2f", receipt.exchangeAmount)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                            }
                            Divider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Processing Fee", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text("${receipt.currency} ${String.format(Locale.US, "%.2f", receipt.fee)}", fontSize = 11.sp, color = Slate500)
                            }
                            Divider(color = Slate800)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Debited", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                Text("${receipt.currency} ${String.format(Locale.US, "%.2f", receipt.totalCharged)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            walletViewModel.clearTransferStates()
                            amountText = ""
                            noteText = ""
                            walletError = null
                            beneficiaryError = null
                            amountError = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_another_btn")
                    ) {
                        Text("Initiate New Remittance", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else {
                // --- PRIMARY REMITTANCE FORM VIEW ---
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        BaseCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Remittance Configuration Parameters",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                // 1. Source account picker
                                Column {
                                    Text(
                                        text = "Debit Source Account",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    when (val state = walletsState) {
                                        is WalletUiState.Loading -> {
                                            Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.CenterStart) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AccentBlue)
                                            }
                                        }
                                        is WalletUiState.Error -> {
                                            Text(text = "Error gathering accounts: ${state.message}", color = DangerRed, fontSize = 12.sp)
                                        }
                                        is WalletUiState.Success -> {
                                            val walletList = state.data
                                            Box {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(1.dp, if (walletError != null) DangerRed else Slate800, RoundedCornerShape(8.dp))
                                                        .background(Slate900)
                                                        .clickable { isWalletExpanded = true }
                                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                                        .testTag("transfer_wallet_toggle"),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = selectedWallet?.flagEmoji ?: "🇺🇸", fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = selectedWallet?.let { "${it.displayName} (${it.currencyCode} - Balance: ${it.symbol}${it.balance})" } ?: "Select a source vault...",
                                                            color = if (selectedWallet != null) Color.White else Slate500,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = Slate500)
                                                }

                                                DropdownMenu(
                                                    expanded = isWalletExpanded,
                                                    onDismissRequest = { isWalletExpanded = false },
                                                    modifier = Modifier.background(Slate800)
                                                ) {
                                                    walletList.forEach { w ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text("${w.flagEmoji} ${w.displayName} (${w.currencyCode} - Balance: ${w.symbol}${w.balance})", color = Color.White, fontSize = 12.sp)
                                                            },
                                                            onClick = {
                                                                selectedWallet = w
                                                                walletError = null
                                                                isWalletExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                    walletError?.let {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = it, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // 2. Beneficiary recipient selector
                                Column {
                                    Text(
                                        text = "Destination Beneficiary Recipient",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    when (val state = beneficiariesState) {
                                        is WalletUiState.Loading -> {
                                            Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.CenterStart) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = AccentBlue)
                                            }
                                        }
                                        is WalletUiState.Error -> {
                                            Text(text = "Error gathering beneficiaries: ${state.message}", color = DangerRed, fontSize = 12.sp)
                                        }
                                        is WalletUiState.Success -> {
                                            val baseList = state.data
                                            Box {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(1.dp, if (beneficiaryError != null) DangerRed else Slate800, RoundedCornerShape(8.dp))
                                                        .background(Slate900)
                                                        .clickable { isBeneficiaryExpanded = true }
                                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                                        .testTag("transfer_beneficiary_toggle"),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = selectedBeneficiary?.flagEmoji ?: "🇧🇩", fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = selectedBeneficiary?.let { "${it.name} (${it.country} - ${it.type})" } ?: "Select a beneficiary recipient...",
                                                            color = if (selectedBeneficiary != null) Color.White else Slate500,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = Slate500)
                                                }

                                                DropdownMenu(
                                                    expanded = isBeneficiaryExpanded,
                                                    onDismissRequest = { isBeneficiaryExpanded = false },
                                                    modifier = Modifier.background(Slate800)
                                                ) {
                                                    baseList.forEach { b ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text("${b.flagEmoji} ${b.name} (${b.country})", color = Color.White, fontSize = 12.sp)
                                                            },
                                                            onClick = {
                                                                selectedBeneficiary = b
                                                                beneficiaryError = null
                                                                isBeneficiaryExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        else -> {}
                                    }
                                    beneficiaryError?.let {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = it, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // 3. Exchange Rate Informational Panel
                                if (selectedWallet != null && selectedBeneficiary != null) {
                                    val src = selectedWallet!!.currencyCode
                                    val dest = when (selectedBeneficiary!!.country.lowercase(Locale.getDefault()).trim()) {
                                        "bangladesh" -> "BDT"
                                        "united states", "us", "usa" -> "USD"
                                        "united kingdom", "uk" -> "GBP"
                                        "canada" -> "CAD"
                                        "germany", "france", "italy", "spain", "europe" -> "EUR"
                                        else -> "USD"
                                    }
                                    val labelKey = "${src}_to_${dest}"
                                    val rate = when (labelKey) {
                                        "USD_to_BDT" -> 118.50
                                        "USD_to_EUR" -> 0.92
                                        "USD_to_GBP" -> 0.79
                                        "EUR_to_USD" -> 1.09
                                        "EUR_to_BDT" -> 126.80
                                        "EUR_to_GBP" -> 0.85
                                        "GBP_to_USD" -> 1.27
                                        "GBP_to_EUR" -> 1.17
                                        "GBP_to_BDT" -> 151.20
                                        "BDT_to_USD" -> 0.0085
                                        "BDT_to_EUR" -> 0.0078
                                        "BDT_to_GBP" -> 0.0066
                                        else -> if (src == dest) 1.0 else 0.90
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AccentBlue.copy(alpha = 0.08f))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Exchange Rate",
                                                tint = AccentBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Dynamic Live Rate: 1 $src = $rate $dest",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue
                                            )
                                        }
                                    }
                                }

                                // 4. Amount input
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            text = "Transfer Amount",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate500
                                        )
                                        selectedWallet?.let {
                                            Text(
                                                text = "Available balance: ${it.symbol}${it.balance}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate500
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = amountText,
                                        onValueChange = { input ->
                                            if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                                amountText = input
                                                amountError = null
                                            }
                                        },
                                        leadingIcon = {
                                            Text(
                                                text = selectedWallet?.symbol ?: "$",
                                                fontWeight = FontWeight.Black,
                                                color = AccentBlue,
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            Text(
                                                text = selectedWallet?.currencyCode ?: "USD",
                                                fontWeight = FontWeight.Bold,
                                                color = Slate500,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                        },
                                        placeholder = { Text("0.00", color = Slate500) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = amountError != null,
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("transfer_amount_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = Slate800,
                                            focusedContainerColor = Slate900,
                                            unfocusedContainerColor = Slate800,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    amountError?.let {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = it, color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Quick Increment Shortcut Buttons (Zod validation helper)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("50", "100", "500", "1000").forEach { quickAmount ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Slate800)
                                                .clickable {
                                                    amountText = quickAmount
                                                    amountError = null
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+$quickAmount",
                                                color = AccentBlue,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // 5. Note
                                Column {
                                    Text(
                                        text = "Client Settlement Reference Memo (Optional)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        placeholder = { Text("E.g., Business contractor fee payout", color = Slate500) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("transfer_note_input"),
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

                                Spacer(modifier = Modifier.height(6.dp))

                                // Submit Click Trigger calling /transfers/preview
                                val isPreviewing = previewState is WalletUiState.Loading
                                Button(
                                    onClick = {
                                        var invalid = false
                                        val amtVal = amountText.toDoubleOrNull()
                                        if (selectedWallet == null) {
                                            walletError = "Please select a funding source wallet."
                                            invalid = true
                                        }
                                        if (selectedBeneficiary == null) {
                                            beneficiaryError = "Please select a registered beneficiary recipient."
                                            invalid = true
                                        }
                                        if (amtVal == null || amtVal <= 0.0) {
                                            amountError = "Please enter a strictly positive transfer value."
                                            invalid = true
                                        } else if (selectedWallet != null && amtVal > selectedWallet!!.balance) {
                                            amountError = "Selected amount exceeds available wallet balance of ${selectedWallet!!.symbol}${selectedWallet!!.balance}"
                                            invalid = true
                                        }

                                        if (!invalid && selectedWallet != null && selectedBeneficiary != null && amtVal != null) {
                                            walletViewModel.createTransferPreview(
                                                walletId = selectedWallet!!.id,
                                                beneficiaryId = selectedBeneficiary!!.id,
                                                amount = amtVal,
                                                note = noteText
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isPreviewing,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("review_transfer_btn")
                                ) {
                                    if (isPreviewing) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text("Preview Interbank Remittance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                if (previewState is WalletUiState.Error) {
                                    Text(
                                        text = (previewState as WalletUiState.Error).message,
                                        color = DangerRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- TAB 2: DETAILED SECURE REVIEW PANEL (In-line BottomSheet style overlay) ---
                if (previewState is WalletUiState.Success) {
                    val preview = (previewState as WalletUiState.Success<TransferPreview>).data
                    val isExecuting = submitState is WalletUiState.Loading

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { if (!isExecuting) walletViewModel.clearTransferStates() }
                    ) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .border(1.dp, Slate800, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            color = Slate900
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                                    .clickable(enabled = false) {}, // prevent click-through dismissal
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Secure Transfer Review Protocol",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    IconButton(
                                        onClick = { if (!isExecuting) walletViewModel.clearTransferStates() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "close", tint = Slate500)
                                    }
                                }

                                Text(
                                    text = "Verify the international clearing and settlement path details listed below before execution.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )

                                Divider(color = Slate800)

                                // Route path details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Source Logo
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Slate800),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(preview.walletSymbol, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Funding Vault", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                        Text(preview.walletName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Balance: ${preview.walletSymbol}${preview.walletBalance}", fontSize = 11.sp, color = Slate500)
                                    }

                                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "transfer route", tint = Slate500, modifier = Modifier.size(24.dp))

                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Recipient Beneficiary", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                        Text(preview.beneficiaryName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(preview.beneficiaryCountry, fontSize = 11.sp, color = Slate500)
                                    }
                                }

                                Divider(color = Slate800)

                                // Calculations parameters table
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Funding Remittance", fontSize = 12.sp, color = Slate500)
                                        Text("${preview.walletSymbol}${String.format(Locale.US, "%.2f", preview.amount)} ${preview.walletCurrency}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Clearing & Exchange fee (0.5%)", fontSize = 12.sp, color = Slate500)
                                        Text("${preview.walletSymbol}${String.format(Locale.US, "%.2f", preview.fee)} ${preview.walletCurrency}", fontSize = 12.sp, color = Color.White)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Wallet Charge", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Bold)
                                        Text("${preview.walletSymbol}${String.format(Locale.US, "%.2f", preview.totalCharged)} ${preview.walletCurrency}", fontSize = 13.sp, color = AccentBlue, fontWeight = FontWeight.Black)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Divider(color = Slate800)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Dynamic Exchange rate", fontSize = 12.sp, color = Slate500)
                                        Text("1 ${preview.walletCurrency} = ${preview.exchangeRate} ${preview.receiveCurrency}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Inflow Settlement Target", fontSize = 12.sp, color = Slate500)
                                        Text("${preview.receiveSymbol}${String.format(Locale.US, "%.2f", preview.receiveAmount)} ${preview.receiveCurrency}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = SuccessGreen)
                                    }

                                    if (preview.note.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Memo Audit: \"${preview.note}\"", fontSize = 11.sp, color = Slate500)
                                    }
                                }

                                if (submitState is WalletUiState.Error) {
                                    Text(
                                        text = (submitState as WalletUiState.Error).message,
                                        color = DangerRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { walletViewModel.clearTransferStates() },
                                        border = BorderStroke(1.dp, Slate800),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("dismiss_preview_btn")
                                    ) {
                                        Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    Button(
                                        onClick = {
                                            walletViewModel.executeTransfer(
                                                walletId = preview.walletId,
                                                beneficiaryId = preview.beneficiaryId,
                                                amount = preview.amount,
                                                note = preview.note
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = !isExecuting,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("confirm_execute_btn")
                                    ) {
                                        if (isExecuting) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                        } else {
                                            Text("Confirm & Wire", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
