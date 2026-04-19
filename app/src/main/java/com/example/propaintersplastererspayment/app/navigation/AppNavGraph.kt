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
import com.example.propaintersplastererspayment.feature.invoice.ui.InvoiceCreateRoute
import com.example.propaintersplastererspayment.feature.invoice.ui.InvoiceRoute
import com.example.propaintersplastererspayment.feature.selection.ui.MainSelectionScreen
import com.example.propaintersplastererspayment.feature.paint.ui.AddEditPaintScreen
import com.example.propaintersplastererspayment.feature.paint.ui.BrandDetailScreen
import com.example.propaintersplastererspayment.feature.paint.ui.PaintBrandListScreen
import com.example.propaintersplastererspayment.feature.paint.vm.PaintViewModel
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
                navController.navigate(AppDestinations.SELECTION_ROUTE) {
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
                    navController.navigate(AppDestinations.SELECTION_ROUTE) {
                        popUpTo(AppDestinations.INITIAL_SETUP_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // ── Main Selection ───────────────────────────────────────────────
        composable(AppDestinations.SELECTION_ROUTE) {
            MainSelectionScreen(
                onNavigateToInvoice = { navController.navigate(AppDestinations.invoiceCreateRoute()) },
                onNavigateToJobs = { navController.navigate(AppDestinations.HOME_ROUTE) },
                onNavigateToPaint = { navController.navigate(AppDestinations.PAINT_ROUTE) }
            )
        }

        // ── Paint Library ───────────────────────────────────────────────
        composable(AppDestinations.PAINT_ROUTE) {
            val paintViewModel: PaintViewModel = viewModel(
                factory = PaintViewModel.provideFactory(application.container.paintRepository)
            )
            PaintBrandListScreen(
                onBrandClick = { brandId -> navController.navigate(AppDestinations.paintBrandDetailRoute(brandId)) },
                onBack = { navController.popBackStack() },
                viewModel = paintViewModel
            )
        }

        composable(
            route = AppDestinations.PAINT_BRAND_DETAIL_WITH_ARG,
            arguments = listOf(navArgument(AppDestinations.BRAND_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getLong(AppDestinations.BRAND_ID_ARG) ?: return@composable
            val paintViewModel: PaintViewModel = viewModel(
                factory = PaintViewModel.provideFactory(application.container.paintRepository)
            )
            BrandDetailScreen(
                brandId = brandId,
                onAddPaint = { navController.navigate(AppDestinations.paintItemFormRoute(brandId, null)) },
                onEditPaint = { paintId -> navController.navigate(AppDestinations.paintItemFormRoute(brandId, paintId)) },
                onBack = { navController.popBackStack() },
                viewModel = paintViewModel
            )
        }

        composable(
            route = AppDestinations.PAINT_ITEM_FORM_WITH_ARG,
            arguments = listOf(
                navArgument(AppDestinations.BRAND_ID_ARG) { type = NavType.LongType; defaultValue = -1L },
                navArgument(AppDestinations.PAINT_ID_ARG) { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getLong(AppDestinations.BRAND_ID_ARG)?.takeIf { it > 0 } ?: return@composable
            val paintId = backStackEntry.arguments?.getLong(AppDestinations.PAINT_ID_ARG)?.takeIf { it > 0 }
            val paintViewModel: PaintViewModel = viewModel(
                factory = PaintViewModel.provideFactory(application.container.paintRepository)
            )
            AddEditPaintScreen(
                brandId = brandId,
                paintId = paintId,
                onBack = { navController.popBackStack() },
                viewModel = paintViewModel
            )
        }

        // ── Quick Invoice Creation ───────────────────────────────────────
        composable(AppDestinations.INVOICE_CREATE_ROUTE) {
            InvoiceCreateRoute(
                onBack = { navController.popBackStack() },
                onInvoiceCreated = { jobId ->
                    navController.navigate(AppDestinations.invoiceRoute(jobId, isQuickInvoice = true)) {
                        popUpTo(AppDestinations.INVOICE_CREATE_ROUTE) { inclusive = true }
                    }
                },
                onOpenQuickInvoice = { jobId ->
                    navController.navigate(AppDestinations.invoiceRoute(jobId, isQuickInvoice = true))
                }
            )
        }

        // ── Invoice Details ──────────────────────────────────────────────
        composable(
            route = AppDestinations.INVOICE_WITH_ARG,
            arguments = listOf(
                navArgument(AppDestinations.JOB_ID_ARG) { type = NavType.LongType },
                navArgument(AppDestinations.IS_QUICK_INVOICE_ARG) {
                    type = NavType.StringType
                    defaultValue = "false"
                }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong(AppDestinations.JOB_ID_ARG)
                ?: return@composable
            val isQuickInvoice = backStackEntry.arguments?.getString(AppDestinations.IS_QUICK_INVOICE_ARG)?.toBoolean() ?: false
            InvoiceRoute(
                jobId = jobId,
                isQuickInvoice = isQuickInvoice,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(AppDestinations.HOME_ROUTE) {
            HomeRoute(
                onOpenSettings = { navController.navigate(AppDestinations.SETTINGS_ROUTE) },
                onAddJob = { navController.navigate(AppDestinations.jobFormRoute(null)) },
                onOpenJob = { jobId -> navController.navigate(AppDestinations.jobDetailRoute(jobId)) },
                onOpenClients = { navController.navigate(AppDestinations.CLIENTS_ROUTE) },
                onBack = { navController.popBackStack() }
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
            val newClientIdState = backStackEntry.savedStateHandle.getStateFlow("newClientId", -1L)
            val newClientId by newClientIdState.collectAsState()
            JobFormRoute(
                jobId = if (jobIdArg <= 0L) null else jobIdArg,
                newClientId = newClientId.takeIf { it > 0L },
                onConsumeNewClientId = { backStackEntry.savedStateHandle["newClientId"] = -1L },
                onAddNewClient = { navController.navigate(AppDestinations.clientFormRoute(null)) },
                onBack = { navController.popBackStack() },
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
            JobDetailScreen(
                jobId = jobId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Settings (edit existing settings) ─────────────────────────────
        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsRoute(onBack = { navController.popBackStack() })
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
                onDone = { savedClientId ->
                    savedClientId?.let { id ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("newClientId", id)
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}
