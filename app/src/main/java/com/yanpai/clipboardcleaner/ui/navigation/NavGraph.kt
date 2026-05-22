package com.yanpai.clipboardcleaner.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yanpai.clipboardcleaner.ui.screens.HomeScreen
import com.yanpai.clipboardcleaner.ui.screens.NotebookScreen
import com.yanpai.clipboardcleaner.ui.screens.SettingsScreen
import com.yanpai.clipboardcleaner.viewmodel.HomeViewModel
import com.yanpai.clipboardcleaner.viewmodel.NotebookViewModel
import com.yanpai.clipboardcleaner.viewmodel.ThemeViewModel

object Routes {
    const val HOME = "home"
    const val NOTEBOOK = "notebook"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph(
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    themeViewModel: ThemeViewModel? = null
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(
            route = Routes.HOME,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            val activity = LocalContext.current as ComponentActivity
            val homeViewModel: HomeViewModel = viewModel(viewModelStoreOwner = activity)
            HomeScreen(
                viewModel = homeViewModel,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                onNavigateToNotebook = { navController.navigate(Routes.NOTEBOOK) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.NOTEBOOK,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            val activity = LocalContext.current as ComponentActivity
            val notebookViewModel: NotebookViewModel = viewModel(viewModelStoreOwner = activity)
            NotebookScreen(
                viewModel = notebookViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.SETTINGS,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            val activity = LocalContext.current as ComponentActivity
            val notebookViewModel: NotebookViewModel = viewModel(viewModelStoreOwner = activity)
            SettingsScreen(
                onBack = { navController.popBackStack() },
                themeViewModel = themeViewModel,
                notebookViewModel = notebookViewModel
            )
        }
    }
}
