package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.state.AuthViewModel
import com.example.ui.state.AuthState
import com.example.ui.state.WalletViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PayNexaApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayNexaApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val walletViewModel: WalletViewModel = viewModel()

    val scope = rememberCoroutineScope()

    when (val state = authState) {
        is AuthState.Authenticated -> {
            val userSession = state.session
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            // Current navigation stack inspection
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

            val configuration = LocalConfiguration.current
            val isTablet = configuration.screenWidthDp >= 600

            // List of bottom-nav major items (Compact view)
            val bottomNavItems = listOf(
                Screen.Dashboard,
                Screen.Accounts,
                Screen.SendMoney,
                Screen.Transactions
            )

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = !isTablet,
                drawerContent = {
                    ModalDrawerSheet {
                        Spacer(modifier = Modifier.height(16.dp))
                        // Drawer logo / branding
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("PN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("PayNexa", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Sidebar and Drawer navigation pages list
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp)
                        ) {
                            Screen.sidebarItems.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                NavigationDrawerItem(
                                    icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title, fontWeight = FontWeight.Bold) },
                                    selected = isSelected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier
                                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                                        .testTag("drawer_item_${screen.route}"),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = AccentBlue.copy(alpha = 0.12f),
                                        selectedIconColor = AccentBlue,
                                        selectedTextColor = AccentBlue,
                                        unselectedTextColor = Slate500,
                                        unselectedIconColor = Slate500
                                    )
                                )
                            }
                        }

                        // Logout button at the very bottom of the drawer sheet
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        NavigationDrawerItem(
                            icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Out") },
                            label = { Text("Log Out Securely", fontWeight = FontWeight.Black) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                authViewModel.logout()
                            },
                            modifier = Modifier
                                .padding(NavigationDrawerItemDefaults.ItemPadding)
                                .testTag("drawer_logout_btn"),
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedTextColor = MaterialTheme.colorScheme.error,
                                unselectedIconColor = MaterialTheme.colorScheme.error
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // SIDEBAR LAYOUT (For Tablets / Wide Landscapes)
                    if (isTablet) {
                        Column(
                            modifier = Modifier
                                .width(260.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(top = 24.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Brand Banner Section
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("PN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("PayNexa", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Sidebar Navigation Items
                                Screen.sidebarItems.forEach { screen ->
                                    val isSelected = currentRoute == screen.route
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentBlue.copy(alpha = 0.12f) else Color.Transparent)
                                            .clickable {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("sidebar_item_${screen.route}"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            tint = if (isSelected) AccentBlue else Slate500,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = screen.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Dynamic user profile box layout + logout visual action
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(AccentBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = "Security verified status", tint = AccentBlue, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(userSession.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Verified Secure Tier", color = Slate500, fontSize = 11.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { authViewModel.logout() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(40.dp).testTag("sidebar_logout_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log out CTA", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Logout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Divider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    // MAIN INTERFACE WRAPPER (Dynamic view depending on Tablet vs Mobile screen width sizes)
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "PayNexa",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = PrimaryBlue
                                    )
                                },
                                navigationIcon = {
                                    if (!isTablet) {
                                        IconButton(
                                            onClick = { scope.launch { drawerState.open() } },
                                            modifier = Modifier.testTag("open_drawer_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Navigation Menu", tint = PrimaryBlue)
                                        }
                                    }
                                },
                                actions = {
                                    // Quick Notifications Bell Shortcut trigger
                                    IconButton(
                                        onClick = { navController.navigate(Screen.Notifications.route) },
                                        modifier = Modifier.testTag("shortcut_notify_bell")
                                    ) {
                                        Box {
                                            Icon(imageVector = Screen.Notifications.icon, contentDescription = "Notifications bell icon", tint = PrimaryBlue)
                                            // Live unread alert badge dot
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Red)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = PrimaryBlue
                                )
                            )
                        },
                        bottomBar = {
                            if (!isTablet) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.testTag("bottom_nav_bar")
                                ) {
                                    bottomNavItems.forEach { screen ->
                                        val isSelected = currentRoute == screen.route
                                        NavigationBarItem(
                                            icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                                            label = { Text(screen.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            selected = isSelected,
                                            onClick = {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = AccentBlue,
                                                selectedTextColor = AccentBlue,
                                                indicatorColor = AccentBlue.copy(alpha = 0.1f),
                                                unselectedIconColor = Slate500,
                                                unselectedTextColor = Slate500
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(innerPadding)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Dashboard.route,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(Screen.Dashboard.route) {
                                    DashboardScreen(
                                        walletViewModel = walletViewModel,
                                        onNavigateTo = { route -> navController.navigate(route) }
                                    )
                                }
                                composable(Screen.Accounts.route) {
                                    AccountsScreen(
                                        walletViewModel = walletViewModel,
                                        onNavigateToDetail = { walletId ->
                                            navController.navigate(Screen.AccountDetails.createRoute(walletId))
                                        }
                                    )
                                }
                                composable(Screen.AccountDetails.route) { backStackEntry ->
                                    val walletId = backStackEntry.arguments?.getString("walletId")
                                    WalletDetailScreen(
                                        walletId = walletId,
                                        walletViewModel = walletViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable(Screen.AddMoney.route) {
                                    AddMoneyScreen(
                                        walletViewModel = walletViewModel,
                                        onNavigateTo = { route -> navController.navigate(route) }
                                    )
                                }
                                composable(Screen.SendMoney.route) {
                                    SendMoneyScreen(
                                        walletViewModel = walletViewModel,
                                        onNavigateTo = { route -> navController.navigate(route) }
                                    )
                                }
                                composable(Screen.AddBeneficiary.route) {
                                     AddBeneficiaryScreen(
                                         walletViewModel = walletViewModel,
                                         onNavigateBack = { navController.popBackStack() }
                                     )
                                 }
                                 composable(Screen.BeneficiaryDetails.route) { backStackEntry ->
                                     val beneficiaryId = backStackEntry.arguments?.getString("beneficiaryId")
                                     BeneficiaryDetailScreen(
                                         beneficiaryId = beneficiaryId,
                                         walletViewModel = walletViewModel,
                                         onNavigateBack = { navController.popBackStack() },
                                         onNavigateTo = { route -> navController.navigate(route) }
                                     )
                                 }
                                 composable(Screen.Beneficiaries.route) {
                                    BeneficiariesScreen(
                                         walletViewModel = walletViewModel,
                                         onNavigateTo = { route -> navController.navigate(route) }
                                     )
                                }
                                composable(Screen.Transactions.route) {
                                    TransactionsScreen(
                                         walletViewModel = walletViewModel,
                                         onNavigateTo = { route -> navController.navigate(route) }
                                     )
                                 }
                                 composable(Screen.TransactionDetails.route) { backStackEntry ->
                                     val transactionId = backStackEntry.arguments?.getString("transactionId")
                                     TransactionDetailScreen(
                                         transactionId = transactionId,
                                         walletViewModel = walletViewModel,
                                         onNavigateBack = { navController.popBackStack() }
                                     )
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(
                                        walletViewModel = walletViewModel,
                                        onNavigateTo = { route -> navController.navigate(route) }
                                    )
                                }
                                composable(Screen.Security.route) {
                                    SecurityScreen(
                                        walletViewModel = walletViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable(Screen.ProfileSettings.route) {
                                    ProfileSettingsScreen(
                                        walletViewModel = walletViewModel,
                                        onNavigateBack = { navController.popBackStack() }
                                    )
                                }
                                composable(Screen.Notifications.route) {
                                    NotificationsScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {
            // UNAUTHENTICATED ROUTING SEQUENCE flow (Protected dashboard)
            val authNavController = rememberNavController()
            val errorMessage = if (state is AuthState.Error) state.message else null
            val isLoading = state is AuthState.Loading

            NavHost(
                navController = authNavController,
                startDestination = Screen.Login.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = { email, pass -> authViewModel.login(email, pass) },
                        onNavigateToRegister = { authNavController.navigate(Screen.Register.route) },
                        onNavigateToForgotPassword = { authNavController.navigate(Screen.ForgotPassword.route) },
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onClearError = { authViewModel.clearError() }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        onRegisterSuccess = { name, email, pass -> authViewModel.register(name, email, pass) },
                        onNavigateToLogin = { authNavController.navigate(Screen.Login.route) },
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onClearError = { authViewModel.clearError() }
                    )
                }
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onResetRequest = { email -> authViewModel.resetPassword(email) },
                        onNavigateToLogin = { authNavController.navigate(Screen.Login.route) },
                        errorMessage = errorMessage,
                        onClearError = { authViewModel.clearError() }
                    )
                }
            }
        }
    }
}
