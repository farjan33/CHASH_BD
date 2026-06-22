package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BaseCard
import com.example.ui.components.ReusablePageContainer
import com.example.ui.state.WalletTransaction
import com.example.ui.state.WalletUiState
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionsScreen(
    walletViewModel: WalletViewModel,
    onNavigateTo: (String) -> Unit
) {
    // Collect fetched transaction records
    val fetchState by walletViewModel.transactionsFetchState.collectAsState()

    // Query filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") } // "All", "DEBIT", "CREDIT"
    var selectedStatus by remember { mutableStateOf("All") } // "All", "Completed", "Pending", "Failed"
    var selectedCategory by remember { mutableStateOf("All") } // "All", "Shopping", "Services", "Transfers", "Subscriptions", "Deposit"

    // Pagination properties
    var currentPage by remember { mutableStateOf(1) }
    val itemsPerPage = 5 // Premium pagination limit

    // Refresh transactions list on view attachment
    LaunchedEffect(Unit) {
        walletViewModel.fetchTransactions()
    }

    ReusablePageContainer(
        title = "Ledger Audit Records",
        subtitle = "Verifiable blockchain routing histories and real-time transaction validations."
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- ADVANCED SEARCH & INTEGRATED FILTER BLOCK ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it 
                    currentPage = 1 // Reset pagination on filter change
                },
                placeholder = { Text("Search by description title, hash reference or location...", color = Slate500) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Slate500) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("tx_list_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Slate800,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Responsive filter chips block in a FlowRow to avoid clipping on smaller screens
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Type Filter Menu Dropdown Trigger
                Box {
                    var typeMenuExpanded by remember { mutableStateOf(false) }
                    FilterConfigBox(
                        label = "Type: $selectedType",
                        onClick = { typeMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false },
                        modifier = Modifier.background(Slate800)
                    ) {
                        listOf("All", "DEBIT", "CREDIT").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    selectedType = type
                                    currentPage = 1
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Status Filter Menu Dropdown Trigger
                Box {
                    var statusMenuExpanded by remember { mutableStateOf(false) }
                    FilterConfigBox(
                        label = "Status: $selectedStatus",
                        onClick = { statusMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false },
                        modifier = Modifier.background(Slate800)
                    ) {
                        listOf("All", "Completed", "Pending", "Failed").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    selectedStatus = status
                                    currentPage = 1
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Category Filter Menu Dropdown Trigger
                Box {
                    var categoryMenuExpanded by remember { mutableStateOf(false) }
                    FilterConfigBox(
                        label = "Category: $selectedCategory",
                        onClick = { categoryMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false },
                        modifier = Modifier.background(Slate800)
                    ) {
                        listOf("All", "Shopping", "Services", "Transfers", "Subscriptions", "Deposit").forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = Color.White, fontSize = 13.sp) },
                                onClick = {
                                    selectedCategory = category
                                    currentPage = 1
                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Reset Action trigger
                if (searchQuery.isNotEmpty() || selectedType != "All" || selectedStatus != "All" || selectedCategory != "All") {
                    Text(
                        text = "Clear Filters",
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clickable {
                                searchQuery = ""
                                selectedType = "All"
                                selectedStatus = "All"
                                selectedCategory = "All"
                                currentPage = 1
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Database network status queries
            when (val state = fetchState) {
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
                    // Filter list before pagination
                    val totalList = state.data
                    val filteredList = totalList.filter { tx ->
                        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) || tx.referenceCode.contains(searchQuery, ignoreCase = true)
                        val matchesType = selectedType == "All" || tx.type == selectedType
                        val matchesStatus = selectedStatus == "All" || tx.status == selectedStatus
                        val matchesCategory = selectedCategory == "All" || tx.category == selectedCategory
                        matchesSearch && matchesType && matchesStatus && matchesCategory
                    }

                    // Perform professional pagination indices slicing
                    val totalPages = maxOf(1, kotlin.math.ceil(filteredList.size.toDouble() / itemsPerPage).toInt())
                    // Limit current page upper bounds
                    if (currentPage > totalPages) {
                        currentPage = totalPages
                    }
                    val startIndex = (currentPage - 1) * itemsPerPage
                    val endIndex = minOf(startIndex + itemsPerPage, filteredList.size)
                    val paginatedList = if (filteredList.isEmpty()) emptyList() else filteredList.subList(startIndex, endIndex)

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Empty",
                                    tint = Slate500,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No matching transactions recorded",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clear or adjust filter parameters to inspect alternative entries.",
                                    color = Slate500,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    } else {
                        // Display production-style list structure with test tags
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(paginatedList) { tx ->
                                BaseCard {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateTo("transactions/${tx.id}") }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (tx.type == "CREDIT") SuccessGreen.copy(alpha = 0.12f)
                                                        else DangerRed.copy(alpha = 0.12f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (tx.type == "CREDIT") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                    contentDescription = tx.type,
                                                    tint = if (tx.type == "CREDIT") SuccessGreen else DangerRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = tx.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = tx.timestamp,
                                                        fontSize = 11.sp,
                                                        color = Slate500
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Slate800)
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = tx.category,
                                                            fontSize = 9.sp,
                                                            color = Slate500,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = (if (tx.type == "CREDIT") "+ " else "- ") + tx.amount,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = if (tx.type == "CREDIT") SuccessGreen else Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        when (tx.status) {
                                                            "Completed" -> SuccessGreen.copy(alpha = 0.15f)
                                                            "Pending" -> WarningOrange.copy(alpha = 0.15f)
                                                            else -> DangerRed.copy(alpha = 0.15f)
                                                        }
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tx.status,
                                                    fontSize = 9.sp,
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
                            }
                        }

                        // --- PRODUCTION PAGINATION CONTROLLERS FOOTER ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Showing $startIndex-$endIndex of ${filteredList.size}",
                                color = Slate500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { if (currentPage > 1) currentPage-- },
                                    enabled = currentPage > 1,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Slate800,
                                        disabledContainerColor = Slate800.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("tx_prev_page_btn")
                                ) {
                                    Text("Previous", fontSize = 12.sp, color = if (currentPage > 1) Color.White else Slate500)
                                }

                                Button(
                                    onClick = { if (currentPage < totalPages) currentPage++ },
                                    enabled = currentPage < totalPages,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Slate800,
                                        disabledContainerColor = Slate800.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("tx_next_page_btn")
                                ) {
                                    Text("Next", fontSize = 12.sp, color = if (currentPage < totalPages) Color.White else Slate500)
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

@Composable
fun FilterConfigBox(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate800)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "dropdown arrow", tint = Slate500, modifier = Modifier.size(16.dp))
        }
    }
}
