package com.example.propaintersplastererspayment.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.propaintersplastererspayment.ProPaintersApplication
import com.example.propaintersplastererspayment.feature.client.ui.AddEditClientRoute
import com.example.propaintersplastererspayment.feature.client.ui.ClientListRoute
import com.example.propaintersplastererspayment.feature.home.ui.HomeRoute
import com.example.propaintersplastererspayment.feature.job.ui.JobDetailScreen
import com.example.propaintersplastererspayment.feature.job.ui.JobFormRoute
import com.example.propaintersplastererspayment.feature.settings.ui.SettingsRoute
import com.example.propaintersplastererspayment.feature.setup.ui.InitialSetupRoute

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as ProPaintersApplication

    val appStartupViewModel: AppStartupViewModel = viewModel(
        factory = AppStartupViewModel.provideFactory(application.container.settingsRepository)
    )
    val setupState by appStartupViewModel.setupState.collectAsState()

    // Navigate from the splash screen once the setup state is resolved.
    LaunchedEffect(setupState) {
        when (setupState) {
            AppStartupViewModel.SetupState.NeedsSetup -> {
                navController.navigate(AppDestinations.INITIAL_SETUP_ROUTE) {
                    popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            }
            AppStartupViewModel.SetupState.SetupComplete -> {
                navController.navigate(AppDestinations.HOME_ROUTE) {
                    popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> { /* Loading – stay on splash */ }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH_ROUTE,
        modifier = modifier
    ) {
        // ── Splash (brief loading screen while we check settings) ──────────
        composable(AppDestinations.SPLASH_ROUTE) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // ── Initial Business Setup (first-run only) ────────────────────────
        composable(AppDestinations.INITIAL_SETUP_ROUTE) {
            InitialSetupRoute(
                onSetupComplete = {
                    navController.navigate(AppDestinations.HOME_ROUTE) {
                        popUpTo(AppDestinations.INITIAL_SETUP_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(AppDestinations.HOME_ROUTE) {
            HomeRoute(
                onOpenSettings = { navController.navigate(AppDestinations.SETTINGS_ROUTE) },
                onAddJob = { navController.navigate(AppDestinations.jobFormRoute(null)) },
                onOpenJob = { jobId -> navController.navigate(AppDestinations.jobDetailRoute(jobId)) },
                onOpenClients = { navController.navigate(AppDestinations.CLIENTS_ROUTE) }
            )
        }

        // ── Job form (add/edit) ────────────────────────────────────────────
        composable(
            route = AppDestinations.JOB_FORM_WITH_ARG,
            arguments = listOf(
                navArgument(AppDestinations.JOB_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val jobIdArg = backStackEntry.arguments?.getLong(AppDestinations.JOB_ID_ARG) ?: -1L
            JobFormRoute(
                jobId = if (jobIdArg <= 0L) null else jobIdArg,
                onDone = { navController.popBackStack() }
            )
        }

        // ── Job detail ────────────────────────────────────────────────────
        composable(
            route = AppDestinations.JOB_DETAIL_WITH_ARG,
            arguments = listOf(navArgument(AppDestinations.JOB_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong(AppDestinations.JOB_ID_ARG)
                ?: return@composable
            JobDetailScreen(jobId = jobId)
        }

        // ── Settings (edit existing settings) ─────────────────────────────
        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsRoute()
        }

        // ── Client list ───────────────────────────────────────────────────
        composable(AppDestinations.CLIENTS_ROUTE) {
            ClientListRoute(
                onAddClient = { navController.navigate(AppDestinations.clientFormRoute(null)) },
                onEditClient = { clientId -> navController.navigate(AppDestinations.clientFormRoute(clientId)) },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Add / edit client ─────────────────────────────────────────────
        composable(
            route = AppDestinations.CLIENT_FORM_WITH_ARG,
            arguments = listOf(
                navArgument(AppDestinations.CLIENT_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val clientIdArg = backStackEntry.arguments?.getLong(AppDestinations.CLIENT_ID_ARG) ?: -1L
            AddEditClientRoute(
                clientId = if (clientIdArg <= 0L) null else clientIdArg,
                onDone = { navController.popBackStack() }
            )
        }
    }
}
