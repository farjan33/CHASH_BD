package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBeneficiaryScreen(
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var accountNo by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Bank Account") }
    var selectedCountry by remember { mutableStateOf("Bangladesh") }

    // Validation trigger states
    var nameError by remember { mutableStateOf<String?>(null) }
    var accountNoError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val countries = listOf("Bangladesh", "United States", "United Kingdom", "Canada", "Germany", "India")
    val types = listOf("Bank Account", "Digital Wallet", "Mobile Banking")

    var countryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    fun validateForm(): Boolean {
        var isValid = true
        if (name.trim().length < 3) {
            nameError = "Full Name must be at least 3 characters."
            isValid = false
        } else {
            nameError = null
        }

        if (accountNo.trim().length < 5) {
            accountNoError = "Account details/IBAN must be at least 5 characters."
            isValid = false
        } else {
            accountNoError = null
        }
        return isValid
    }

    ReusablePageContainer(
        title = "Add Recipient",
        subtitle = "Connect a new global profile to your dynamic settlement ledger.",
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("back_to_beneficiaries_from_add")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = AccentBlue)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BaseCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Recipient Identification Credentials",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Beneficiary Type Dropdown Selection
                    Column {
                        Text(
                            text = "Beneficiary Type",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
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
                                    .testTag("beneficiary_type_dropdown"),
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
                                types.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(text = type, color = Color.White, fontSize = 14.sp) },
                                        onClick = {
                                            selectedType = type
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Full Name Input
                    Column {
                        Text(
                            text = "Beneficiary Full Name",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) nameError = null
                            },
                            placeholder = { Text("Enter recipient registration name...", color = Slate500) },
                            singleLine = true,
                            isError = nameError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("beneficiary_name_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = Slate800,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate800,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                errorContainerColor = Slate900,
                                errorBorderColor = DangerRed
                            )
                        )
                        nameError?.let {
                            Text(
                                text = it,
                                color = DangerRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // Destination Country Dropdown
                    Column {
                        Text(
                            text = "Destination Country / Region",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
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
                                    .testTag("beneficiary_country_dropdown"),
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
                                countries.forEach { country ->
                                    DropdownMenuItem(
                                        text = { Text(text = country, color = Color.White, fontSize = 14.sp) },
                                        onClick = {
                                            selectedCountry = country
                                            countryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Account Number / IBAN details input
                    Column {
                        Text(
                            text = when (selectedType) {
                                "Bank Account" -> "IBAN, Swift, or Account Number"
                                "Digital Wallet" -> "Digital Wallet Address (e.g. Email/ID)"
                                else -> "Mobile Number / Bank Gateway Route"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = accountNo,
                            onValueChange = {
                                accountNo = it
                                if (accountNoError != null) accountNoError = null
                            },
                            placeholder = {
                                Text(
                                    text = when (selectedType) {
                                        "Bank Account" -> "e.g. HSBC US1234567890"
                                        "Digital Wallet" -> "e.g. email@domain.com or hash"
                                        else -> "e.g. bKash / Nagad (+88017...)"
                                    },
                                    color = Slate500
                                )
                            },
                            singleLine = true,
                            isError = accountNoError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("beneficiary_account_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = Slate800,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate800,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                errorContainerColor = Slate900,
                                errorBorderColor = DangerRed
                            )
                        )
                        accountNoError?.let {
                            Text(
                                text = it,
                                color = DangerRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }

            // High-security verification notification banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Verification guidelines",
                    tint = AccentBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "PayNexa Secure Safe Directory",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Adding a beneficiary lets you initiate zero-fee, secure payments across any active currency vault instantly. Make sure all verification hashes match exact registration profiles to prevent network holds.",
                        fontSize = 11.sp,
                        color = Slate500,
                        lineHeight = 16.sp
                    )
                }
            }

            // Create submit action button
            Button(
                onClick = {
                    if (validateForm()) {
                        isLoading = true
                        walletViewModel.addBeneficiary(
                            name = name,
                            country = selectedCountry,
                            accountNumber = accountNo,
                            type = selectedType,
                            onComplete = { success ->
                                isLoading = false
                                if (success) {
                                    onNavigateBack()
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_beneficiary_form_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save Beneficiary")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Recipient", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
