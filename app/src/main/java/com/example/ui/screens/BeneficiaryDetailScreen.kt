package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.state.Beneficiary
import com.example.ui.state.WalletUiState
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryDetailScreen(
    beneficiaryId: String?,
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit
) {
    if (beneficiaryId == null) {
        onNavigateBack()
        return
    }

    val selectedBeneficiaryState by walletViewModel.selectedBeneficiaryState.collectAsState()

    // Flag toggles
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Edit form properties
    var name by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Bank Account") }
    var selectedCountry by remember { mutableStateOf("Bangladesh") }

    // Validation alerts
    var nameError by remember { mutableStateOf<String?>(null) }
    var accountNoError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val countries = listOf("Bangladesh", "United States", "United Kingdom", "Canada", "Germany", "India")
    val types = listOf("Bank Account", "Digital Wallet", "Mobile Banking")

    var countryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(beneficiaryId) {
        walletViewModel.fetchBeneficiaryDetails(beneficiaryId)
    }

    // Sync form values once data succeeds
    LaunchedEffect(selectedBeneficiaryState) {
        val state = selectedBeneficiaryState
        if (state is WalletUiState.Success) {
            val b = state.data
            name = b.name
            accountNo = b.accountNumber
            selectedType = b.type
            selectedCountry = b.country
        }
    }

    fun validateForm(): Boolean {
        var isValid = true
        if (name.trim().length < 3) {
            nameError = "Full Name must be at least 3 characters."
            isValid = false
        } else {
            nameError = null
        }

        if (accountNo.trim().length < 5) {
            accountNoError = "Account/IBAN must be at least 5 characters."
            isValid = false
        } else {
            accountNoError = null
        }
        return isValid
    }

    ReusablePageContainer(
        title = "Recipient Details",
        subtitle = "Verifiable routing assets for international settlement transfers.",
        navigationIcon = {
            IconButton(
                onClick = {
                    if (isEditing) {
                        // Cancel edit state first
                        isEditing = false
                        // Reset properties
                        val s = selectedBeneficiaryState
                        if (s is WalletUiState.Success) {
                            name = s.data.name
                            accountNo = s.data.accountNumber
                            selectedType = s.data.type
                            selectedCountry = s.data.country
                        }
                    } else {
                        onNavigateBack()
                    }
                },
                modifier = Modifier.testTag("back_to_beneficiaries_from_details")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = AccentBlue)
            }
        }
    ) {
        when (val state = selectedBeneficiaryState) {
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
                val beneficiary = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Core Hero Profile Header Card
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
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Slate900),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = beneficiary.name,
                                        tint = Slate500,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Slate800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = beneficiary.flagEmoji,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = beneficiary.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = beneficiary.type,
                                    fontSize = 11.sp,
                                    color = AccentBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Interactive details form/view layout box
                    if (!isEditing) {
                        // --- VIEW MODE DETAILS ---
                        BaseCard {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Account Country", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                    Text(beneficiary.country, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = Slate800)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Routing Details/IBAN", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                    Text(beneficiary.accountNumber, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = Slate800)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Gateway Protocol", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                    Text(if (beneficiary.type == "Bank Account") "Swift Network Transfer" else "Instant Ledger Settling", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = Slate800)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Directory Identifier", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                                    Text(beneficiary.id, fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Immediate send money shortcut action button
                        Button(
                            onClick = { onNavigateTo("send_money") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_money_to_recipient_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Ledger Money", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // Edit / Delete management controls row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { isEditing = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("edit_beneficiary_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Recipient", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Info", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("delete_beneficiary_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Recipient", tint = DangerRed)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retract Profile", color = DangerRed, fontWeight = FontWeight.Bold)
                            }
                        }

                    } else {
                        // --- EDIT MODE INPUTS ---
                        BaseCard {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Update Verification Fields",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                // Full Name
                                Column {
                                    Text("Beneficiary Full Name", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate500, modifier = Modifier.padding(bottom = 6.dp))
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = {
                                            name = it
                                            if (nameError != null) nameError = null
                                        },
                                        isError = nameError != null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("edit_beneficiary_name_input"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = Slate800,
                                            focusedContainerColor = Slate900,
                                            unfocusedContainerColor = Slate800,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    nameError?.let {
                                        Text(text = it, color = DangerRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }

                                // Type Dropdown Selection
                                Column {
                                    Text("Beneficiary Type", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate500, modifier = Modifier.padding(bottom = 6.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = selectedType,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = "Expand type dropdown",
                                                    tint = Slate500,
                                                    modifier = Modifier.clickable { typeExpanded = !typeExpanded }
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("edit_beneficiary_type"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = Slate800,
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate800,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        DropdownMenu(
                                            expanded = typeExpanded,
                                            onDismissRequest = { typeExpanded = false },
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .background(Slate800)
                                        ) {
                                            types.forEach { t ->
                                                DropdownMenuItem(
                                                    text = { Text(text = t, color = Color.White, fontSize = 13.sp) },
                                                    onClick = {
                                                        selectedType = t
                                                        typeExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Country Selection
                                Column {
                                    Text("Destination Country", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate500, modifier = Modifier.padding(bottom = 6.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = selectedCountry,
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = "Expand country dropdown",
                                                    tint = Slate500,
                                                    modifier = Modifier.clickable { countryExpanded = !countryExpanded }
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("edit_beneficiary_country"),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = Slate800,
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate800,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        DropdownMenu(
                                            expanded = countryExpanded,
                                            onDismissRequest = { countryExpanded = false },
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .background(Slate800)
                                        ) {
                                            countries.forEach { c ->
                                                DropdownMenuItem(
                                                    text = { Text(text = c, color = Color.White, fontSize = 13.sp) },
                                                    onClick = {
                                                        selectedCountry = c
                                                        countryExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Account Number
                                Column {
                                    Text("Account Routing / IBAN", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Slate500, modifier = Modifier.padding(bottom = 6.dp))
                                    OutlinedTextField(
                                        value = accountNo,
                                        onValueChange = {
                                            accountNo = it
                                            if (accountNoError != null) accountNoError = null
                                        },
                                        isError = accountNoError != null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("edit_beneficiary_account"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = Slate800,
                                            focusedContainerColor = Slate900,
                                            unfocusedContainerColor = Slate800,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    accountNoError?.let {
                                        Text(text = it, color = DangerRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        }

                        // Submit changes button row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { isEditing = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    if (validateForm()) {
                                        isSaving = true
                                        walletViewModel.updateBeneficiary(
                                            id = beneficiary.id,
                                            name = name,
                                            country = selectedCountry,
                                            accountNumber = accountNo,
                                            type = selectedType,
                                            onComplete = { success ->
                                                 isSaving = false
                                                 if (success) {
                                                     isEditing = false
                                                 }
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("save_beneficiary_edit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Save Changes", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    // Retract Profile Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Retract Recipient Profile", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    text = "Are you absolutely sure you want to retract $name from your global directory? Future instant payments to this routing info will lock down until verified again.",
                    color = Slate500,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        walletViewModel.deleteBeneficiary(
                            id = beneficiaryId,
                            onComplete = { success ->
                                if (success) {
                                    onNavigateBack()
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.testTag("confirm_retract_beneficiary_btn")
                ) {
                    Text("Retract Directory", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Slate500)
                }
            },
            containerColor = Slate900
        )
    }
}
