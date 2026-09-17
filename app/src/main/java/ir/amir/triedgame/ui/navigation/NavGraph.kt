package ir.amir.triedgame.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.amir.triedgame.ads.AdManager
import ir.amir.triedgame.billing.BillingManager
import ir.amir.triedgame.ui.GameViewModel
import ir.amir.triedgame.ui.screens.AboutScreen
import ir.amir.triedgame.ui.screens.AccountScreen
import ir.amir.triedgame.ui.screens.ChargeAccountScreen
import ir.amir.triedgame.ui.screens.LifeScreen
import ir.amir.triedgame.ui.screens.TradingScreen

private sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Trading : Tab("trading", "ترید", Icons.Filled.ShowChart)
    object Charge : Tab("charge", "شارژ حساب", Icons.Filled.AccountBalanceWallet)
    object Account : Tab("account", "حساب کاربری", Icons.Filled.Person)
    object Life : Tab("life", "زندگی من", Icons.Filled.SportsEsports)
    object About : Tab("about", "درباره ما", Icons.Filled.Info)
}

private val tabs = listOf(Tab.Trading, Tab.Charge, Tab.Account, Tab.Life, Tab.About)

@Composable
fun MainNavGraph(
    viewModel: GameViewModel,
    adManager: AdManager?,
    billingManager: BillingManager?
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Trading.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Tab.Trading.route) { TradingScreen(viewModel) }
            composable(Tab.Charge.route) { ChargeAccountScreen(viewModel, adManager, billingManager) }
            composable(Tab.Account.route) { AccountScreen(viewModel) }
            composable(Tab.Life.route) { LifeScreen(viewModel) }
            composable(Tab.About.route) { AboutScreen() }
        }
    }
}
