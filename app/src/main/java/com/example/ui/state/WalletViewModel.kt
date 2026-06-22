package com.example.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- KOTLIN ENTITIES (Equivalent to Prisma / Database models) ---

data class Wallet(
    val id: String,
    val currencyCode: String,
    val displayName: String,
    val balance: Double,
    val flagEmoji: String,
    val symbol: String,
    val status: String, // "Active", "Locked"
    val routingNumber: String,
    val accountNumber: String,
    val bankName: String
)

data class WalletTransaction(
    val id: String,
    val walletId: String,
    val type: String, // "CREDIT" or "DEBIT"
    val amount: Double,
    val title: String,
    val category: String, // "Shopping", "Services", "Transfers", "Subscriptions", "Deposit"
    val timestamp: String,
    val referenceCode: String,
    val status: String = "Completed" // "Completed", "Pending", "Failed"
)

data class Beneficiary(
    val id: String,
    val name: String,
    val country: String,
    val accountNumber: String,
    val flagEmoji: String,
    val type: String = "Bank Account" // Bank Account, Digital Wallet, Mobile Banking
)

data class PayNexaNotification(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String,
    val isUrgent: Boolean
)

data class AddMoneyRequest(
    val id: String,
    val walletId: String,
    val walletName: String,
    val amount: Double,
    val currency: String,
    val method: String,
    val note: String,
    val status: String = "PENDING", // "PENDING", "COMPLETED", "FAILED"
    val timestamp: String,
    val referenceCode: String
)

data class TransferPreview(
    val walletId: String,
    val walletName: String,
    val walletCurrency: String,
    val walletSymbol: String,
    val walletBalance: Double,
    val beneficiaryId: String,
    val beneficiaryName: String,
    val beneficiaryCountry: String,
    val beneficiaryAccountNumber: String,
    val amount: Double,
    val exchangeRate: Double,
    val fee: Double,
    val receiveAmount: Double,
    val receiveCurrency: String,
    val receiveSymbol: String,
    val totalCharged: Double,
    val note: String
)

data class TransferRecord(
    val id: String,
    val walletId: String,
    val walletName: String,
    val beneficiaryId: String,
    val beneficiaryName: String,
    val amount: Double,
    val currency: String,
    val destCurrency: String,
    val exchangeRate: Double,
    val exchangeAmount: Double,
    val fee: Double,
    val totalCharged: Double,
    val note: String,
    val timestamp: String,
    val referenceCode: String,
    val status: String // "SUCCESS", "PENDING", "FAILED"
)

data class UserProfile(
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val country: String,
    val tier: String,
    val status: String // "Verified", "Pending Verification"
)

data class LoginActivity(
    val id: String,
    val timestamp: String,
    val ipAddress: String,
    val device: String,
    val location: String,
    val status: String // "Success", "Failed"
)

data class SecuritySession(
    val id: String,
    val deviceName: String,
    val ipAddress: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean
)

sealed class WalletUiState<out T> {
    object Idle : WalletUiState<Nothing>()
    object Loading : WalletUiState<Nothing>()
    data class Success<out T>(val data: T) : WalletUiState<T>()
    data class Error(val message: String) : WalletUiState<Nothing>()
}

// --- SECURE WALLET & ACCOUNTS CONTROLLER (API Simulation Backend & State Manager) ---

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    // Internal in-memory database simulation
    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _beneficiaries = MutableStateFlow<List<Beneficiary>>(emptyList())
    val beneficiaries: StateFlow<List<Beneficiary>> = _beneficiaries.asStateFlow()

    private val _notifications = MutableStateFlow<List<PayNexaNotification>>(emptyList())
    val notifications: StateFlow<List<PayNexaNotification>> = _notifications.asStateFlow()

    // Screen-level loading states representing TanStack Query UI reactions
    private val _walletsFetchState = MutableStateFlow<WalletUiState<List<Wallet>>>(WalletUiState.Idle)
    val walletsFetchState: StateFlow<WalletUiState<List<Wallet>>> = _walletsFetchState.asStateFlow()

    private val _selectedWalletState = MutableStateFlow<WalletUiState<Wallet>>(WalletUiState.Idle)
    val selectedWalletState: StateFlow<WalletUiState<Wallet>> = _selectedWalletState.asStateFlow()

    private val _beneficiariesFetchState = MutableStateFlow<WalletUiState<List<Beneficiary>>>(WalletUiState.Idle)
    val beneficiariesFetchState: StateFlow<WalletUiState<List<Beneficiary>>> = _beneficiariesFetchState.asStateFlow()

    private val _selectedBeneficiaryState = MutableStateFlow<WalletUiState<Beneficiary>>(WalletUiState.Idle)
    val selectedBeneficiaryState: StateFlow<WalletUiState<Beneficiary>> = _selectedBeneficiaryState.asStateFlow()

    private val _transactionsFetchState = MutableStateFlow<WalletUiState<List<WalletTransaction>>>(WalletUiState.Idle)
    val transactionsFetchState: StateFlow<WalletUiState<List<WalletTransaction>>> = _transactionsFetchState.asStateFlow()

    private val _selectedTransactionState = MutableStateFlow<WalletUiState<WalletTransaction>>(WalletUiState.Idle)
    val selectedTransactionState: StateFlow<WalletUiState<WalletTransaction>> = _selectedTransactionState.asStateFlow()

    private val _addMoneyRequests = MutableStateFlow<List<AddMoneyRequest>>(emptyList())
    val addMoneyRequests: StateFlow<List<AddMoneyRequest>> = _addMoneyRequests.asStateFlow()

    private val _addMoneyFetchState = MutableStateFlow<WalletUiState<List<AddMoneyRequest>>>(WalletUiState.Idle)
    val addMoneyFetchState: StateFlow<WalletUiState<List<AddMoneyRequest>>> = _addMoneyFetchState.asStateFlow()

    private val _addMoneySubmitState = MutableStateFlow<WalletUiState<AddMoneyRequest>>(WalletUiState.Idle)
    val addMoneySubmitState: StateFlow<WalletUiState<AddMoneyRequest>> = _addMoneySubmitState.asStateFlow()

    private val _transferPreviewState = MutableStateFlow<WalletUiState<TransferPreview>>(WalletUiState.Idle)
    val transferPreviewState: StateFlow<WalletUiState<TransferPreview>> = _transferPreviewState.asStateFlow()

    private val _transferSubmitState = MutableStateFlow<WalletUiState<TransferRecord>>(WalletUiState.Idle)
    val transferSubmitState: StateFlow<WalletUiState<TransferRecord>> = _transferSubmitState.asStateFlow()

    // --- SETTINGS & SECURITY STATE FLOWS ---
    private val _userProfileState = MutableStateFlow<WalletUiState<UserProfile>>(WalletUiState.Idle)
    val userProfileState: StateFlow<WalletUiState<UserProfile>> = _userProfileState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<WalletUiState<String>>(WalletUiState.Idle)
    val changePasswordState: StateFlow<WalletUiState<String>> = _changePasswordState.asStateFlow()

    private val _loginActivitiesState = MutableStateFlow<WalletUiState<List<LoginActivity>>>(WalletUiState.Idle)
    val loginActivitiesState: StateFlow<WalletUiState<List<LoginActivity>>> = _loginActivitiesState.asStateFlow()

    private val _userSessionsState = MutableStateFlow<WalletUiState<List<SecuritySession>>>(WalletUiState.Idle)
    val userSessionsState: StateFlow<WalletUiState<List<SecuritySession>>> = _userSessionsState.asStateFlow()

    // Backup persistence variables
    private var currentUserProfile = UserProfile(
        email = "Farjanamin1111@gmail.com",
        fullName = "Farjan Amin",
        phoneNumber = "+1 (555) 304-9492",
        country = "Bangladesh",
        tier = "PayNexa Premium Executive",
        status = "Verified"
    )

    private val currentLoginActivities = mutableListOf(
        LoginActivity("log-1", "2026-06-21 10:45", "192.168.1.1", "Android Emulator", "San Francisco, CA", "Success"),
        LoginActivity("log-2", "2026-06-20 18:32", "192.168.1.5", "Chrome Mobile on Pixel 8", "San Francisco, CA", "Success"),
        LoginActivity("log-3", "2026-06-19 14:15", "104.244.72.1", "Firefox Workspace MacOS", "San Jose, CA", "Success"),
        LoginActivity("log-4", "2026-06-18 09:12", "182.204.12.8", "Streaming Android Emulator", "Chicago, IL", "Success")
    )

    private val currentUserSessions = mutableListOf(
        SecuritySession("sess-1", "Streaming Android Emulator (This Device)", "192.168.1.1", "San Francisco, CA", "Active Now", true),
        SecuritySession("sess-2", "Chrome Workspace MacOS", "185.22.41.9", "San Jose, CA", "3 hours ago", false),
        SecuritySession("sess-3", "iPhone 15 Pro Max", "172.56.21.84", "New York, NY", "2 days ago", false)
    )

    init {
        seedInitialDatabase()
    }

    private fun seedInitialDatabase() {
        // Hydrate initial mock global accounts matching DashboardScreen defaults
        val defaultWallets = listOf(
            Wallet(
                id = "wallet-usd",
                currencyCode = "USD",
                displayName = "Primary Checking",
                balance = 7250.50,
                flagEmoji = "🇺🇸",
                symbol = "$",
                status = "Active",
                routingNumber = "RT-021000021",
                accountNumber = "AC-8849204910",
                bankName = "PayNexa Federal Reserve"
            ),
            Wallet(
                id = "wallet-eur",
                currencyCode = "EUR",
                displayName = "Business Savings",
                balance = 4250.00,
                flagEmoji = "🇪🇺",
                symbol = "€",
                status = "Active",
                routingNumber = "RT-051000104",
                accountNumber = "AC-3304192042",
                bankName = "PayNexa European Central Bank"
            ),
            Wallet(
                id = "wallet-gbp",
                currencyCode = "GBP",
                displayName = "Personal Wallet",
                balance = 2150.00,
                flagEmoji = "🇬🇧",
                symbol = "£",
                status = "Active",
                routingNumber = "RT-081005202",
                accountNumber = "AC-5593021922",
                bankName = "PayNexa Royal Vault"
            ),
            Wallet(
                id = "wallet-bdt",
                currencyCode = "BDT",
                displayName = "Local Transfers",
                balance = 196520.00,
                flagEmoji = "🇧🇩",
                symbol = "৳",
                status = "Active",
                routingNumber = "RT-120520930",
                accountNumber = "AC-9904201948",
                bankName = "PayNexa Dhaka Guild"
            )
        )

        val defaultTransactions = listOf(
            WalletTransaction(
                id = "txn-1",
                walletId = "wallet-usd",
                type = "DEBIT",
                amount = 280.50,
                title = "AWS SaaS Monthly Cloud Premium",
                category = "Subscriptions",
                timestamp = "2026-06-20 14:24",
                referenceCode = "TXN492051025",
                status = "Completed"
            ),
            WalletTransaction(
                id = "txn-2",
                walletId = "wallet-usd",
                type = "CREDIT",
                amount = 1500.00,
                title = "Freelance Consulting Remit Deposit",
                category = "Deposit",
                timestamp = "2026-06-19 11:30",
                referenceCode = "TXN220194812",
                status = "Completed"
            ),
            WalletTransaction(
                id = "txn-3",
                walletId = "wallet-usd",
                type = "DEBIT",
                amount = 85.00,
                title = "Whole Foods Market Grocery",
                category = "Shopping",
                timestamp = "2026-06-18 18:45",
                referenceCode = "TXN104928104",
                status = "Failed"
            ),
            WalletTransaction(
                id = "txn-4",
                walletId = "wallet-eur",
                type = "DEBIT",
                amount = 620.30,
                title = "DigitalOcean Node Cluster Setup",
                category = "Services",
                timestamp = "2026-06-21 09:15",
                referenceCode = "TXNEUR849102",
                status = "Completed"
            ),
            WalletTransaction(
                id = "txn-5",
                walletId = "wallet-gbp",
                type = "CREDIT",
                amount = 450.00,
                title = "Refund for Overpaid Royal Sub",
                category = "Transfers",
                timestamp = "2026-06-20 16:50",
                referenceCode = "TXNGBP049201",
                status = "Pending"
            ),
            WalletTransaction(
                id = "txn-6",
                walletId = "wallet-bdt",
                type = "DEBIT",
                amount = 12000.00,
                title = "Dhaka Central Electricity Remit",
                category = "Services",
                timestamp = "2026-06-17 10:15",
                referenceCode = "TXNBDT103952",
                status = "Completed"
            ),
            WalletTransaction(
                id = "txn-7",
                walletId = "wallet-eur",
                type = "CREDIT",
                amount = 750.00,
                title = "Inbound Stripe Payout Settlement",
                category = "Deposit",
                timestamp = "2026-06-16 11:40",
                referenceCode = "TXNEUR294029",
                status = "Completed"
            )
        )

        _wallets.value = defaultWallets
        _transactions.value = defaultTransactions

        val defaultBeneficiaries = listOf(
            Beneficiary(
                id = "b-1",
                name = "Muhammad Ali",
                country = "Bangladesh",
                accountNumber = "Dhaka Branch (•• 9876)",
                flagEmoji = "🇧🇩"
            ),
            Beneficiary(
                id = "b-2",
                name = "Sarah Johnson",
                country = "United States",
                accountNumber = "Wells Fargo NYC (•• 4321)",
                flagEmoji = "🇺🇸"
            ),
            Beneficiary(
                id = "b-3",
                name = "David Williams",
                country = "United Kingdom",
                accountNumber = "Barclays London (•• 5432)",
                flagEmoji = "🇬🇧"
            )
        )

        val defaultNotifications = listOf(
            PayNexaNotification(
                id = "n-1",
                title = "Login from new device detected",
                body = "Streaming Android Emulator active sign-in from San Francisco, CA.",
                timestamp = "May 24 • 10:15 AM",
                isUrgent = true
            ),
            PayNexaNotification(
                id = "n-2",
                title = "Password changed successfully",
                body = "Account credential was updated recently from dynamic IP range.",
                timestamp = "May 20 • 02:40 PM",
                isUrgent = false
            ),
            PayNexaNotification(
                id = "n-3",
                title = "Two-factor authentication (2FA) active",
                body = "Shield protected. Vault access requires instant secondary checks.",
                timestamp = "May 18 • 11:30 AM",
                isUrgent = false
            ),
            PayNexaNotification(
                id = "n-4",
                title = "Received $250.00 USD from Sarah",
                body = "Sarah Johnson instantly settled your freelance project payments.",
                timestamp = "May 15 • 09:12 AM",
                isUrgent = false
            )
        )

        _beneficiaries.value = defaultBeneficiaries
        _notifications.value = defaultNotifications

        val defaultRequests = listOf(
            AddMoneyRequest(
                id = "req-1",
                walletId = "wallet-usd",
                walletName = "Primary Checking",
                amount = 2500.00,
                currency = "USD",
                method = "FedWire Transfer",
                note = "Quarterly contract bonus payout",
                status = "COMPLETED",
                timestamp = "2026-06-15 09:30",
                referenceCode = "REF-DEP-004291"
            ),
            AddMoneyRequest(
                id = "req-2",
                walletId = "wallet-eur",
                walletName = "Business Savings",
                amount = 850.00,
                currency = "EUR",
                method = "Instant Debit Card",
                note = "Replenishing vault balance",
                status = "PENDING",
                timestamp = "2026-06-21 10:00",
                referenceCode = "REF-DEP-940182"
            )
        )
        _addMoneyRequests.value = defaultRequests
    }

    // --- API SERVICE ENDPOINTS (Simulating TanStack / Prisma Backend Pipeline) ---

    fun fetchWallets() {
        viewModelScope.launch {
            _walletsFetchState.value = WalletUiState.Loading
            delay(400) // Simulates lightning-fast, high-capacity server roundtrip
            _walletsFetchState.value = WalletUiState.Success(_wallets.value)
        }
    }

    fun fetchWalletDetails(walletId: String) {
        viewModelScope.launch {
            _selectedWalletState.value = WalletUiState.Loading
            delay(300)
            val wallet = _wallets.value.find { it.id == walletId }
            if (wallet != null) {
                _selectedWalletState.value = WalletUiState.Success(wallet)
            } else {
                _selectedWalletState.value = WalletUiState.Error("Wallet details not found on PayNexa Server")
            }
        }
    }

    // Opens / Creates a brand-new multi-currency wallet
    fun createWallet(currencyCode: String, nickname: String) {
        viewModelScope.launch {
            _walletsFetchState.value = WalletUiState.Loading
            delay(500)

            val flag = when (currencyCode) {
                "USD" -> "🇺🇸"
                "EUR" -> "🇪🇺"
                "GBP" -> "🇬🇧"
                "CAD" -> "🇨🇦"
                else -> "🎌"
            }
            val symbol = when (currencyCode) {
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "CAD" -> "C$"
                else -> "¥"
            }
            val bank = when (currencyCode) {
                "USD" -> "PayNexa Federal Reserve"
                "EUR" -> "PayNexa European Central Bank"
                "GBP" -> "PayNexa Royal Vault"
                else -> "PayNexa North Dominion"
            }

            val rNumber = "RT-" + (100000000..999999999).random().toString()
            val aNumber = "AC-" + (1000000000..9999999999).random().toString()

            val newWallet = Wallet(
                id = "wallet-${currencyCode.lowercase()}-${System.currentTimeMillis() % 100000}",
                currencyCode = currencyCode,
                displayName = if (nickname.isNotBlank()) nickname else "$currencyCode Account",
                balance = 0.0,
                flagEmoji = flag,
                symbol = symbol,
                status = "Active",
                routingNumber = rNumber,
                accountNumber = aNumber,
                bankName = bank
            )

            val currentList = _wallets.value.toMutableList()
            currentList.add(newWallet)
            _wallets.value = currentList

            _walletsFetchState.value = WalletUiState.Success(currentList)
        }
    }

    // Top-up or Deposit Money into specific Wallet
    fun addFundsToWallet(walletId: String, amount: Double) {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateString = formatter.format(Date())

        val currentList = _wallets.value.map { wallet ->
            if (wallet.id == walletId) {
                wallet.copy(balance = wallet.balance + amount)
            } else {
                wallet
            }
        }
        _wallets.value = currentList

        // Append to Transaction History Database table (equivalent to Prisma mutation)
        val newTxn = WalletTransaction(
            id = "txn-${System.currentTimeMillis()}",
            walletId = walletId,
            type = "CREDIT",
            amount = amount,
            title = "Manual Card Top-up Vault",
            category = "Deposit",
            timestamp = dateString,
            referenceCode = "TXN" + (100000000..999999999).random().toString()
        )

        val currentTxns = _transactions.value.toMutableList()
        currentTxns.add(0, newTxn)
        _transactions.value = currentTxns

        // Instantly refresh detail selection state to preserve synchronization
        val updatedWallet = currentList.find { it.id == walletId }
        if (updatedWallet != null) {
            _selectedWalletState.value = WalletUiState.Success(updatedWallet)
        }
    }

    // Get calculated balance aggregate across all vaults in primary currency (USD)
    fun getAggregateBalanceInUSD(): Double {
        return _wallets.value.sumOf { wallet ->
            val conversionRate = when (wallet.currencyCode) {
                "USD" -> 1.0
                "EUR" -> 1.08 // 1 EUR = 1.08 USD
                "GBP" -> 1.27 // 1 GBP = 1.27 USD
                "BDT" -> 0.0085 // 1 BDT = 0.0085 USD
                else -> 1.0
            }
            wallet.balance * conversionRate
        }
    }

    // --- BENEFICIARIES DATABASE CRUD API LOGS (TanStack/Prisma Backend Mimics) ---

    // GET /beneficiaries
    fun fetchBeneficiaries() {
        viewModelScope.launch {
            _beneficiariesFetchState.value = WalletUiState.Loading
            delay(300)
            _beneficiariesFetchState.value = WalletUiState.Success(_beneficiaries.value)
        }
    }

    // GET /beneficiaries/:id
    fun fetchBeneficiaryDetails(beneficiaryId: String) {
        viewModelScope.launch {
            _selectedBeneficiaryState.value = WalletUiState.Loading
            delay(300)
            val beneficiarObj = _beneficiaries.value.find { it.id == beneficiaryId }
            if (beneficiarObj != null) {
                _selectedBeneficiaryState.value = WalletUiState.Success(beneficiarObj)
            } else {
                _selectedBeneficiaryState.value = WalletUiState.Error("Beneficiary record not found.")
            }
        }
    }

    // POST /beneficiaries
    fun addBeneficiary(
        name: String,
        country: String,
        accountNumber: String,
        type: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _beneficiariesFetchState.value = WalletUiState.Loading
            delay(400)
            val flag = when (country.lowercase(Locale.getDefault())) {
                "bangladesh" -> "🇧🇩"
                "united states", "us", "usa" -> "🇺🇸"
                "united kingdom", "uk" -> "🇬🇧"
                "canada" -> "🇨🇦"
                "germany" -> "🇩🇪"
                "india" -> "🇮🇳"
                else -> "🌐"
            }
            val newBeneficiary = Beneficiary(
                id = "b-${System.currentTimeMillis() % 100000}",
                name = name,
                country = country,
                accountNumber = accountNumber,
                flagEmoji = flag,
                type = type
            )
            val newList = _beneficiaries.value.toMutableList()
            newList.add(newBeneficiary)
            _beneficiaries.value = newList
            _beneficiariesFetchState.value = WalletUiState.Success(newList)

            addLocalNotification(
                title = "Added recipient: $name",
                body = "A new ${type.lowercase(Locale.getDefault())} beneficiary was linked under $country."
            )
            onComplete(true)
        }
    }

    // PATCH /beneficiaries/:id
    fun updateBeneficiary(
        id: String,
        name: String,
        country: String,
        accountNumber: String,
        type: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _beneficiariesFetchState.value = WalletUiState.Loading
            delay(400)
            val flag = when (country.lowercase(Locale.getDefault())) {
                "bangladesh" -> "🇧🇩"
                "united states", "us", "usa" -> "🇺🇸"
                "united kingdom", "uk" -> "🇬🇧"
                "canada" -> "🇨🇦"
                "germany" -> "🇩🇪"
                "india" -> "🇮🇳"
                else -> "🌐"
            }
            val updatedList = _beneficiaries.value.map { b ->
                if (b.id == id) {
                    b.copy(
                        name = name,
                        country = country,
                        accountNumber = accountNumber,
                        flagEmoji = flag,
                        type = type
                     )
                } else {
                    b
                }
            }
            _beneficiaries.value = updatedList
            _beneficiariesFetchState.value = WalletUiState.Success(updatedList)

            val updatedObj = updatedList.find { it.id == id }
            if (updatedObj != null) {
                _selectedBeneficiaryState.value = WalletUiState.Success(updatedObj)
            }
            onComplete(true)
        }
    }

    // DELETE /beneficiaries/:id
    fun deleteBeneficiary(id: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _beneficiariesFetchState.value = WalletUiState.Loading
            delay(300)
            val deletedObj = _beneficiaries.value.find { it.id == id }
            val updatedList = _beneficiaries.value.filter { it.id != id }
            _beneficiaries.value = updatedList
            _beneficiariesFetchState.value = WalletUiState.Success(updatedList)

            deletedObj?.let {
                addLocalNotification(
                    title = "Removed recipient: ${it.name}",
                    body = "The recipient profile was retracted from your system directory."
                )
            }
            onComplete(true)
        }
    }

    private fun addLocalNotification(title: String, body: String) {
        val formatter = SimpleDateFormat("MMM dd • hh:mm a", Locale.getDefault())
        val dateString = formatter.format(Date())
        val newNotification = PayNexaNotification(
            id = "n-${System.currentTimeMillis()}",
            title = title,
            body = body,
            timestamp = dateString,
            isUrgent = false
        )
        val notificationsList = _notifications.value.toMutableList()
        notificationsList.add(0, newNotification)
        _notifications.value = notificationsList
    }

    // --- TRANSACTIONS DATABASE CRUD API LOGS (TanStack/Prisma Backend Mimics) ---

    // GET /transactions
    fun fetchTransactions() {
        viewModelScope.launch {
            _transactionsFetchState.value = WalletUiState.Loading
            delay(400)
            _transactionsFetchState.value = WalletUiState.Success(_transactions.value)
        }
    }

    // GET /transactions/:id
    fun fetchTransactionDetails(transactionId: String) {
        viewModelScope.launch {
            _selectedTransactionState.value = WalletUiState.Loading
            delay(300)
            val txn = _transactions.value.find { it.id == transactionId }
            if (txn != null) {
                _selectedTransactionState.value = WalletUiState.Success(txn)
            } else {
                _selectedTransactionState.value = WalletUiState.Error("Transaction record not found in system registers.")
            }
        }
    }

    // --- ADD MONEY RETRIEVAL / CREATION API (TanStack / Prisma Backend Mimics) ---

    // GET /add-money
    fun fetchAddMoneyRequests() {
        viewModelScope.launch {
            _addMoneyFetchState.value = WalletUiState.Loading
            delay(400)
            _addMoneyFetchState.value = WalletUiState.Success(_addMoneyRequests.value)
        }
    }

    // POST /add-money
    fun createAddMoneyRequest(
        walletId: String,
        amount: Double,
        currency: String,
        method: String,
        note: String
    ) {
        viewModelScope.launch {
            _addMoneySubmitState.value = WalletUiState.Loading
            delay(500)

            // Dynamic server-side business rules validation
            if (amount <= 0.0) {
                _addMoneySubmitState.value = WalletUiState.Error("Transaction amount must be strictly positive.")
                return@launch
            }

            val matchingWallet = _wallets.value.find { it.id == walletId }
            if (matchingWallet == null) {
                _addMoneySubmitState.value = WalletUiState.Error("Target wallet address was not found in database records.")
                return@launch
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateString = sdf.format(Date())
            val refCode = "REF-DEP-" + (100000..999999).random()

            val newRequest = AddMoneyRequest(
                id = "req-${System.currentTimeMillis()}",
                walletId = walletId,
                walletName = matchingWallet.displayName,
                amount = amount,
                currency = currency,
                method = method,
                note = note.ifBlank { "Assets deposit via $method" },
                status = "PENDING",
                timestamp = dateString,
                referenceCode = refCode
            )

            // Prepend new request row
            val updatedList = _addMoneyRequests.value.toMutableList()
            updatedList.add(0, newRequest)
            _addMoneyRequests.value = updatedList

            // Post a system notification alert
            addLocalNotification(
                title = "Added PENDING load request: $currency $amount",
                body = "A validation request has been logged. Action Reference: $refCode."
            )

            _addMoneySubmitState.value = WalletUiState.Success(newRequest)
            // Trigger fetch refresh to synchronize list UI
            fetchAddMoneyRequests()
        }
    }

    // Interactive helper: Approve PENDING request (dynamic wallet balance hydration)
    fun settleAddMoneyRequest(requestId: String) {
        viewModelScope.launch {
            val requestsList = _addMoneyRequests.value.toMutableList()
            val requestIndex = requestsList.indexOfFirst { it.id == requestId }
            if (requestIndex != -1) {
                val req = requestsList[requestIndex]
                if (req.status == "PENDING") {
                    // Update status
                    val approvedReq = req.copy(status = "COMPLETED")
                    requestsList[requestIndex] = approvedReq
                    _addMoneyRequests.value = requestsList

                    // Dynamically hydrate the actual wallet balance!
                    val walletsList = _wallets.value.toMutableList()
                    val walletIndex = walletsList.indexOfFirst { it.id == req.walletId }
                    if (walletIndex != -1) {
                        val originalWallet = walletsList[walletIndex]
                        walletsList[walletIndex] = originalWallet.copy(
                            balance = originalWallet.balance + req.amount
                        )
                        _wallets.value = walletsList
                        // Sync wallet fetches
                        fetchWallets()
                    }

                    // Dynamically append to main transactions ledger!
                    val transactionsList = _transactions.value.toMutableList()
                    val newTx = WalletTransaction(
                        id = "txn-${System.currentTimeMillis()}",
                        walletId = req.walletId,
                        type = "CREDIT",
                        amount = req.amount,
                        title = "Bank Deposit: ${req.note}",
                        category = "Deposit",
                        timestamp = req.timestamp,
                        referenceCode = req.referenceCode,
                        status = "Completed"
                    )
                    transactionsList.add(0, newTx)
                    _transactions.value = transactionsList
                    // Sync transaction lists
                    this@WalletViewModel.fetchTransactions()

                    // Trigger notification completion
                    addLocalNotification(
                        title = "Deposit Settled Successfully",
                        body = "Approved load request of ${req.currency} ${req.amount} has been fully cleared and added to your wallet."
                    )

                    // Refresh fetch logs to update view state
                    fetchAddMoneyRequests()
                }
            }
        }
    }

    fun clearAddMoneySubmitState() {
        _addMoneySubmitState.value = WalletUiState.Idle
    }

    // --- TRANSFERS CORE ENDPOINTS (TanStack / Prisma Backend Mimics) ---

    // POST /transfers/preview
    fun createTransferPreview(
        walletId: String,
        beneficiaryId: String,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            _transferPreviewState.value = WalletUiState.Loading
            delay(400) // simulated interbank route estimation delay

            // 1. Validation: Positive sum
            if (amount <= 0.0) {
                _transferPreviewState.value = WalletUiState.Error("Transfer amount must be strictly greater than zero.")
                return@launch
            }

            // 2. Resolve & Verify User Wallet Ownership
            val sourceWallet = _wallets.value.find { it.id == walletId }
            if (sourceWallet == null) {
                _transferPreviewState.value = WalletUiState.Error("Source wallet not found or does not belong to your account.")
                return@launch
            }

            // 3. Resolve & Verify User Beneficiary Ownership
            val beneficiary = _beneficiaries.value.find { it.id == beneficiaryId }
            if (beneficiary == null) {
                _transferPreviewState.value = WalletUiState.Error("Selected recipient is not registered under your beneficiary record catalog.")
                return@launch
            }

            // 4. Resolve Currencies and Exchange rates dynamically
            val srcCurrency = sourceWallet.currencyCode
            val destCurrency = when (beneficiary.country.lowercase(Locale.getDefault()).trim()) {
                "bangladesh" -> "BDT"
                "united states", "us", "usa" -> "USD"
                "united kingdom", "uk" -> "GBP"
                "canada" -> "CAD"
                "germany", "france", "italy", "spain", "europe" -> "EUR"
                else -> "USD"
            }

            val destSymbol = when (destCurrency) {
                "BDT" -> "৳"
                "USD" -> "$"
                "GBP" -> "£"
                "EUR" -> "€"
                "CAD" -> "C$"
                else -> "$"
            }

            val rateKey = "${srcCurrency}_to_${destCurrency}"
            val exchangeRate = when (rateKey) {
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
                else -> if (srcCurrency == destCurrency) 1.0 else 0.90
            }

            val conversionAmount = amount * exchangeRate
            val fee = (amount * 0.005).coerceAtLeast(1.50) // 0.5% with 1.50 min fee in source currency
            val totalCharged = amount + fee

            // Check balance status
            if (sourceWallet.balance < totalCharged) {
                _transferPreviewState.value = WalletUiState.Error(
                    "Insufficient funds. Total cost: ${sourceWallet.symbol}${String.format(Locale.US, "%.2f", totalCharged)} (Amount: ${sourceWallet.symbol}${String.format(Locale.US, "%.2f", amount)} + Est. Fee: ${sourceWallet.symbol}${String.format(Locale.US, "%.2f", fee)}). Wallet balance: ${sourceWallet.symbol}${String.format(Locale.US, "%.2f", sourceWallet.balance)}"
                )
                return@launch
            }

            val preview = TransferPreview(
                walletId = walletId,
                walletName = sourceWallet.displayName,
                walletCurrency = srcCurrency,
                walletSymbol = sourceWallet.symbol,
                walletBalance = sourceWallet.balance,
                beneficiaryId = beneficiaryId,
                beneficiaryName = beneficiary.name,
                beneficiaryCountry = beneficiary.country,
                beneficiaryAccountNumber = beneficiary.accountNumber,
                amount = amount,
                exchangeRate = exchangeRate,
                fee = fee,
                receiveAmount = conversionAmount,
                receiveCurrency = destCurrency,
                receiveSymbol = destSymbol,
                totalCharged = totalCharged,
                note = note.ifBlank { "Direct interbank settlement" }
            )

            _transferPreviewState.value = WalletUiState.Success(preview)
        }
    }

    // POST /transfers
    fun executeTransfer(
        walletId: String,
        beneficiaryId: String,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            _transferSubmitState.value = WalletUiState.Loading
            delay(600) // simulated secure clearing processing latency

            // Re-verify parameters matching backend API logic
            if (amount <= 0.0) {
                _transferSubmitState.value = WalletUiState.Error("Execution amount must be positive.")
                return@launch
            }

            val walletsList = _wallets.value.toMutableList()
            val walletIndex = walletsList.indexOfFirst { it.id == walletId }
            if (walletIndex == -1) {
                _transferSubmitState.value = WalletUiState.Error("Source wallet validation failed. Account non-existent.")
                return@launch
            }

            val sourceWallet = walletsList[walletIndex]
            val beneficiary = _beneficiaries.value.find { it.id == beneficiaryId }
            if (beneficiary == null) {
                _transferSubmitState.value = WalletUiState.Error("Beneficiary routing mismatch. Record invalid.")
                return@launch
            }

            val srcCurrency = sourceWallet.currencyCode
            val destCurrency = when (beneficiary.country.lowercase(Locale.getDefault()).trim()) {
                "bangladesh" -> "BDT"
                "united states", "us", "usa" -> "USD"
                "united kingdom", "uk" -> "GBP"
                "canada" -> "CAD"
                "germany", "france", "italy", "spain", "europe" -> "EUR"
                else -> "USD"
            }

            val rateKey = "${srcCurrency}_to_${destCurrency}"
            val exchangeRate = when (rateKey) {
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
                else -> if (srcCurrency == destCurrency) 1.0 else 0.90
            }

            val conversionAmount = amount * exchangeRate
            val fee = (amount * 0.005).coerceAtLeast(1.50)
            val totalCharged = amount + fee

            // Final real backend sufficient balance validation check
            if (sourceWallet.balance < totalCharged) {
                _transferSubmitState.value = WalletUiState.Error("Insufficient wallet balance for transfer Execution.")
                return@launch
            }

            // DEDUCT WALLET BALANCE CORRECTLY
            walletsList[walletIndex] = sourceWallet.copy(
                balance = sourceWallet.balance - totalCharged
            )
            _wallets.value = walletsList
            fetchWallets() // synchronizes list flows

            // CREATE TRANSFER RECORD
            val refCode = "REF-TRF-" + (100000..999999).random()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateString = sdf.format(Date())

            val record = TransferRecord(
                id = "trf-${System.currentTimeMillis()}",
                walletId = walletId,
                walletName = sourceWallet.displayName,
                beneficiaryId = beneficiaryId,
                beneficiaryName = beneficiary.name,
                amount = amount,
                currency = srcCurrency,
                destCurrency = destCurrency,
                exchangeRate = exchangeRate,
                exchangeAmount = conversionAmount,
                fee = fee,
                totalCharged = totalCharged,
                note = note.ifBlank { "Direct interbank settlement" },
                timestamp = dateString,
                referenceCode = refCode,
                status = "SUCCESS"
            )

            // CREATE TRANSACTION RECORD FOR THE MAIN HISTORICAL LEDGER (Debit category "Transfers")
            val transactionsList = _transactions.value.toMutableList()
            val walletTxOption = WalletTransaction(
                id = "txn-${System.currentTimeMillis()}",
                walletId = walletId,
                type = "DEBIT",
                amount = totalCharged,
                title = "To ${beneficiary.name} (${beneficiary.country})",
                category = "Transfers",
                timestamp = dateString,
                referenceCode = refCode,
                status = "Completed"
            )
            transactionsList.add(0, walletTxOption)
            _transactions.value = transactionsList
            fetchTransactions() // syncs overall history list

            // Generate System Push Notification Alert
            addLocalNotification(
                title = "Transfer Dispatched: $srcCurrency $amount",
                body = "Successfully processed and cleared $destCurrency $conversionAmount to $beneficiaryId (${beneficiary.name}). Reference ID: $refCode."
            )

            _transferSubmitState.value = WalletUiState.Success(record)
        }
    }

    fun clearTransferStates() {
        _transferPreviewState.value = WalletUiState.Idle
        _transferSubmitState.value = WalletUiState.Idle
    }

    // --- USER SETTINGS MODULE SERVICES (GET/PATCH /users/me & /settings/password) ---

    // GET /users/me
    fun fetchUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = WalletUiState.Loading
            delay(200)
            _userProfileState.value = WalletUiState.Success(currentUserProfile)
        }
    }

    // PATCH /users/me
    fun updateUserProfile(fullName: String, email: String, phoneNumber: String, country: String) {
        viewModelScope.launch {
            _userProfileState.value = WalletUiState.Loading
            delay(400)

            if (fullName.isBlank()) {
                _userProfileState.value = WalletUiState.Error("Profile full name cannot be blank.")
                return@launch
            }
            if (!email.contains("@") || email.length < 5) {
                _userProfileState.value = WalletUiState.Error("Please declare a valid email address.")
                return@launch
            }
            if (phoneNumber.isBlank() || country.isBlank()) {
                _userProfileState.value = WalletUiState.Error("Phone number and Country parameter are required fields.")
                return@launch
            }

            currentUserProfile = currentUserProfile.copy(
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                country = country
            )

            // Trigger security log notification alert
            addLocalNotification(
                title = "Profile Record Modification Profile",
                body = "System successfully updated your core contact metadata to $fullName ($country)."
            )

            _userProfileState.value = WalletUiState.Success(currentUserProfile)
        }
    }

    // PATCH /settings/password
    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = WalletUiState.Loading
            delay(500)

            if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                _changePasswordState.value = WalletUiState.Error("All secure fields must be declared.")
                return@launch
            }
            if (newPassword != confirmPassword) {
                _changePasswordState.value = WalletUiState.Error("Your new password and confirmation password do not match.")
                return@launch
            }
            if (newPassword.length < 6) {
                _changePasswordState.value = WalletUiState.Error("The secure password must contain at least 6 characters.")
                return@launch
            }
            if (oldPassword != "123456" && oldPassword.lowercase(Locale.getDefault()) != "secret") {
                _changePasswordState.value = WalletUiState.Error("Verify old secure password incorrect. (Default credential helper: '123456')")
                return@launch
            }

            addLocalNotification(
                title = "Password Security Altered",
                body = "Your user account credentials have been successfully updated. Existing active API credentials remain authorized."
            )

            _changePasswordState.value = WalletUiState.Success("Your account credentials were changed successfully.")
        }
    }

    fun clearChangePasswordState() {
        _changePasswordState.value = WalletUiState.Idle
    }

    // GET /security/login-activities
    fun fetchLoginActivities() {
        viewModelScope.launch {
            _loginActivitiesState.value = WalletUiState.Loading
            delay(200)
            _loginActivitiesState.value = WalletUiState.Success(currentLoginActivities)
        }
    }

    // GET /security/sessions
    fun fetchUserSessions() {
        viewModelScope.launch {
            _userSessionsState.value = WalletUiState.Loading
            delay(200)
            _userSessionsState.value = WalletUiState.Success(currentUserSessions)
        }
    }

    fun revokeSession(id: String) {
        viewModelScope.launch {
            val updated = currentUserSessions.filter { it.id != id }
            currentUserSessions.clear()
            currentUserSessions.addAll(updated)
            
            addLocalNotification(
                title = "Security Session Terminated",
                body = "Active key token session reference $id has been fully revoked and blacklisted."
            )
            
            fetchUserSessions()
        }
    }
}

