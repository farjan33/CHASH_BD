package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    walletViewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val profileState by walletViewModel.userProfileState.collectAsState()

    // Form states
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    // Validation error triggers
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var countryError by remember { mutableStateOf<String?>(null) }

    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Fetch user profile on startup
    LaunchedEffect(Unit) {
        walletViewModel.fetchUserProfile()
    }

    // Bind form variables when successfully loaded
    LaunchedEffect(profileState) {
        val state = profileState
        if (state is WalletUiState.Success) {
            fullName = state.data.fullName
            email = state.data.email
            phoneNumber = state.data.phoneNumber
            country = state.data.country
        }
    }

    ReusablePageContainer(
        title = "Personal Portfolio Profile",
        subtitle = "Verify and customize your core contact parameters and localized settlement parameters.",
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("profile_back_btn")
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back", tint = Color.White)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = profileState) {
                is WalletUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                    }
                }
                is WalletUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = "Error", tint = DangerRed, modifier = Modifier.size(48.dp))
                        Text(text = "Failed to load user credentials: ${state.message}", color = Color.White, fontSize = 14.sp)
                        Button(
                            onClick = { walletViewModel.fetchUserProfile() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Retry Sync")
                        }
                    }
                }
                else -> {
                    // Success or Idle view
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        // Success Toast indicator
                        saveSuccessMessage?.let {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreen.copy(alpha = 0.12f))
                                        .padding(14.dp)
                                       
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Saved", tint = SuccessGreen)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(it, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Avatar card with user status badge
                        item {
                            BaseCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(AccentBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = "User avatar", tint = AccentBlue, modifier = Modifier.size(28.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (state is WalletUiState.Success) state.data.fullName else "User Portfolio",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(SuccessGreen.copy(alpha = 0.12f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (state is WalletUiState.Success) state.data.status else "Verified",
                                                    color = SuccessGreen,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (state is WalletUiState.Success) state.data.tier else "Premium Portfolio Member",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlue
                                        )
                                    }
                                }
                            }
                        }

                        // Input fields card
                        item {
                            BaseCard {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("Portfolio Profile Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Divider(color = Slate800)

                                    // Full Name Field
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Full Legal Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                        OutlinedTextField(
                                            value = fullName,
                                            onValueChange = { fullName = it; nameError = null },
                                            isError = nameError != null,
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = Slate800,
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate800,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        nameError?.let { Text(it, color = DangerRed, fontSize = 11.sp) }
                                    }

                                    // Email Address Field
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Contact Email Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { email = it; emailError = null },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                            isError = emailError != null,
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("profile_email_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = Slate800,
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate800,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        emailError?.let { Text(it, color = DangerRed, fontSize = 11.sp) }
                                    }

                                    // Phone Number Field
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Authorized Phone Number", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                        OutlinedTextField(
                                            value = phoneNumber,
                                            onValueChange = { phoneNumber = it; phoneError = null },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            isError = phoneError != null,
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("profile_phone_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = Slate800,
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate800,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        phoneError?.let { Text(it, color = DangerRed, fontSize = 11.sp) }
                                    }

                                    // Country/Region Field
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Domicile Country/Region", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                        OutlinedTextField(
                                            value = country,
                                            onValueChange = { country = it; countryError = null },
                                            isError = countryError != null,
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("profile_country_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = Slate800,
                                                focusedContainerColor = Slate900,
                                                unfocusedContainerColor = Slate800,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        countryError?.let { Text(it, color = DangerRed, fontSize = 11.sp) }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Dynamic Submit trigger calling PATCH /users/me
                                    Button(
                                        onClick = {
                                            var isInvalid = false
                                            if (fullName.isBlank()) {
                                                nameError = "Full Legal Name is a mandatory field."
                                                isInvalid = true
                                            }
                                            if (!email.contains("@") || email.length < 5) {
                                                emailError = "Please write a standard valid email address."
                                                isInvalid = true
                                            }
                                            if (phoneNumber.isBlank()) {
                                                phoneError = "An authorized phone number identifier is mandatory."
                                                isInvalid = true
                                            }
                                            if (country.isBlank()) {
                                                countryError = "Country domiciliation must be configured."
                                                isInvalid = true
                                            }

                                            if (!isInvalid) {
                                                walletViewModel.updateUserProfile(
                                                    fullName = fullName,
                                                    email = email,
                                                    phoneNumber = phoneNumber,
                                                    country = country
                                                )
                                                saveSuccessMessage = "Portfolio profile updated cleared and executed."
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("save_profile_btn")
                                    ) {
                                        Text("Update Profile Parameters", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
