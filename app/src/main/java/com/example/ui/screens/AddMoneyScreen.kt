package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.state.AddMoneyRequest
import com.example.ui.state.Wallet
import com.example.ui.state.WalletUiState
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyScreen(
    walletViewModel: WalletViewModel,
    onNavigateTo: (String) -> Unit
) {
    // State collection
    val walletsState by walletViewModel.walletsFetchState.collectAsState()
    val requestsState by walletViewModel.addMoneyFetchState.collectAsState()
    val submitState by walletViewModel.addMoneySubmitState.collectAsState()

    // Tab state: 0 = Form, 1 = History
    var selectedTab by remember { mutableStateOf(0) }

    // Form inputs
    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Instant Debit Card") }
    var noteText by remember { mutableStateOf("") }

    // Client-side validation states
    var amountError by remember { mutableStateOf<String?>(null) }
    var walletError by remember { mutableStateOf<String?>(null) }

    // Dropdown expanding parameters
    var isWalletExpanded by remember { mutableStateOf(false) }
    var isMethodExpanded by remember { mutableStateOf(false) }

    val methodOptions = listOf(
        "Instant Debit Card",
        "FedWire Transfer",
        "Bank ACH Transfer",
        "International Swift Remit"
    )

    // Trigger initial fetching logs on mount
    LaunchedEffect(Unit) {
        walletViewModel.fetchWallets()
        walletViewModel.fetchAddMoneyRequests()
        walletViewModel.clearAddMoneySubmitState()
    }

    // Set default wallet option once loaded
    LaunchedEffect(walletsState) {
        val state = walletsState
        if (state is WalletUiState.Success && state.data.isNotEmpty() && selectedWallet == null) {
            selectedWallet = state.data.first()
        }
    }

    // React to successful submission of request
    LaunchedEffect(submitState) {
        if (submitState is WalletUiState.Success) {
            // Reset input values on successful submission
            amountText = ""
            noteText = ""
            amountError = null
            walletError = null
            // Redirect to request list tab
            selectedTab = 1
            // Clear submission state
            walletViewModel.clearAddMoneySubmitState()
        }
    }

    ReusablePageContainer(
        title = "Capital Hydration Core",
        subtitle = "Initiate and track asset settlement requests into your PayNexa multi-currency vault."
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- NAVIGATION TABS CONTROLLER (Slate styled) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Slate900, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Load Assets Form", "Settlement Ledger").forEachIndexed { index, label ->
                    val isTabSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTabSelected) Slate800 else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp)
                            .testTag("add_money_tab_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isTabSelected) AccentBlue else Slate500,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Render sub-screens depending on active tab selected
            if (selectedTab == 0) {
                // --- TAB 0: ASSET INPUT LOADING FORM ---
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    text = "Deposit Manifest Parameters",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                // Wallet Selection Field
                                Column {
                                    Text(
                                        text = "Target Vault Wallet",
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
                                                        .testTag("trigger_wallet_picker"),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = selectedWallet?.flagEmoji ?: "🌍", fontSize = 16.sp)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(
                                                            text = selectedWallet?.let { "${it.displayName} (${it.currencyCode} - Balance: ${it.symbol}${it.balance})" } ?: "Select a target vault...",
                                                            color = if (selectedWallet != null) Color.White else Slate500,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "dropdown arrow", tint = Slate500)
                                                }

                                                DropdownMenu(
                                                    expanded = isWalletExpanded,
                                                    onDismissRequest = { isWalletExpanded = false },
                                                    modifier = Modifier.background(Slate800)
                                                ) {
                                                    walletList.forEach { w ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text("${w.flagEmoji} ${w.displayName} (${w.currencyCode} - Balance: ${w.symbol}${w.balance})", color = Color.White, fontSize = 13.sp)
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

                                // Settlement Channel (Method) Selection Field
                                Column {
                                    Text(
                                        text = "Clearing Channel Protocol",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                                                .background(Slate900)
                                                .clickable { isMethodExpanded = true }
                                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                                .testTag("trigger_method_picker"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = when(selectedMethod) {
                                                        "Instant Debit Card" -> Icons.Default.CreditCard
                                                        else -> Icons.Default.AccountBalance
                                                    },
                                                    contentDescription = "Method icon",
                                                    tint = AccentBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = selectedMethod,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "dropdown arrow", tint = Slate500)
                                        }

                                        DropdownMenu(
                                            expanded = isMethodExpanded,
                                            onDismissRequest = { isMethodExpanded = false },
                                            modifier = Modifier.background(Slate800)
                                        ) {
                                            methodOptions.forEach { m ->
                                                DropdownMenuItem(
                                                    text = { Text(m, color = Color.White, fontSize = 13.sp) },
                                                    onClick = {
                                                        selectedMethod = m
                                                        isMethodExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Amount inputs featuring real-time error checking feedback loops
                                Column {
                                    Text(
                                        text = "Funding Amount (${selectedWallet?.currencyCode ?: "USD"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
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
                                            .testTag("add_money_amount_input"),
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
                                    listOf("100", "500", "2500", "10000").forEach { quickAmount ->
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

                                // Informational Notes Parameter
                                Column {
                                    Text(
                                        text = "Audit ledger note (Optional context)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate500
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = noteText,
                                        onValueChange = { noteText = it },
                                        placeholder = { Text("E.g., Self deposit via credit linkage", color = Slate500) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_money_note_input"),
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
                                val isSubmitting = submitState is WalletUiState.Loading
                                Button(
                                    onClick = {
                                        // Robust validation checks
                                        var hasErrors = false
                                        val amountVal = amountText.toDoubleOrNull()
                                        if (selectedWallet == null) {
                                            walletError = "Please select a wallet address first."
                                            hasErrors = true
                                        }
                                        if (amountVal == null || amountVal <= 0.0) {
                                            amountError = "Please enter a strictly positive deposit sum."
                                            hasErrors = true
                                        }
                                        
                                        if (!hasErrors && selectedWallet != null && amountVal != null) {
                                            // Trigger API service creation
                                            walletViewModel.createAddMoneyRequest(
                                                walletId = selectedWallet!!.id,
                                                amount = amountVal,
                                                currency = selectedWallet!!.currencyCode,
                                                method = selectedMethod,
                                                note = noteText
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isSubmitting,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("add_money_submit_btn")
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text(
                                            text = "Log Pending Load Request",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
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
                            }
                        }
                    }
                }
            } else {
                // --- TAB 1: RETRIEVED ADD-MONEY REQUEST LIST ---
                when (val state = requestsState) {
                    is WalletUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    }
                    is WalletUiState.Error -> {
                        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", tint = DangerRed, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = state.message, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                    is WalletUiState.Success -> {
                        val requests = state.data
                        if (requests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = "Receipt Log Empty", tint = Slate500, modifier = Modifier.size(56.dp))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = "No assets loading requests logged", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Toggle the 'Load Assets Form' to request transfer approvals on-demand.",
                                        color = Slate500,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(requests) { req ->
                                    BaseCard {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = req.walletName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = Color.White
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Slate800)
                                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = req.currency,
                                                                fontSize = 9.sp,
                                                                color = AccentBlue,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = req.referenceCode,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Slate500
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        text = "${req.amount} ${req.currency}",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 15.sp,
                                                        color = if (req.status == "COMPLETED") SuccessGreen else Color.White
                                                    )
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    
                                                    // Status badges mapping
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(
                                                                when (req.status) {
                                                                    "COMPLETED" -> SuccessGreen.copy(alpha = 0.12f)
                                                                    "PENDING" -> WarningOrange.copy(alpha = 0.12f)
                                                                    else -> DangerRed.copy(alpha = 0.12f)
                                                                }
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = req.status,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (req.status) {
                                                                "COMPLETED" -> SuccessGreen
                                                                "PENDING" -> WarningOrange
                                                                else -> DangerRed
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Divider(color = Slate800)

                                            // Description notes and metadata line
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Route: ${req.method}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate500
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = req.note,
                                                        fontSize = 12.sp,
                                                        color = Color.LightGray
                                                    )
                                                }
                                                Text(
                                                    text = req.timestamp,
                                                    fontSize = 10.sp,
                                                    color = Slate500,
                                                    textAlign = TextAlign.End
                                                )
                                            }

                                            // DYNAMIC INTERACTIVE TRIGGER: Approve Deposit Settlement instanstly on-device
                                            if (req.status == "PENDING") {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Button(
                                                    onClick = {
                                                        walletViewModel.settleAddMoneyRequest(req.id)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(34.dp)
                                                        .testTag("settle_transfer_btn_${req.id}")
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Settle", tint = Color.Black, modifier = Modifier.size(12.dp))
                                                        Text(
                                                            text = "Instantly Settle Transfer Gateways (Add to Balance)",
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 11.sp,
                                                            color = Color.Black
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
