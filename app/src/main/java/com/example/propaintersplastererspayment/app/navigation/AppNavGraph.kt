package com.example.propaintersplastererspayment.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.propaintersplastererspayment.feature.home.ui.HomeRoute
import com.example.propaintersplastererspayment.feature.job.ui.JobDetailScreen
import com.example.propaintersplastererspayment.feature.job.ui.JobFormRoute
import com.example.propaintersplastererspayment.feature.settings.ui.SettingsRoute

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        composable(AppDestinations.HOME_ROUTE) {
            HomeRoute(
                onOpenSettings = { navController.navigate(AppDestinations.SETTINGS_ROUTE) },
                onAddJob = { navController.navigate(AppDestinations.jobFormRoute(null)) },
                onOpenJob = { jobId -> navController.navigate(AppDestinations.jobDetailRoute(jobId)) }
            )
        }

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

        composable(
            route = AppDestinations.JOB_DETAIL_WITH_ARG,
            arguments = listOf(navArgument(AppDestinations.JOB_ID_ARG) { type = NavType.LongType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getLong(AppDestinations.JOB_ID_ARG) ?: return@composable
            JobDetailScreen(jobId = jobId)
        }

        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsRoute()
        }
    }
}

