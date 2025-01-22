package com.example.deepwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.DeepWorkTheme
import com.example.deepwork.ui.navigation.Routes
import com.example.deepwork.ui.navigation.navigate
import com.example.deepwork.ui.session_management.create_session.CreateSessionScreen
import com.example.deepwork.ui.session_management.create_session.CreateSessionViewModel
import com.example.deepwork.ui.session_management.create_session.add_time_block.AddTimeBlockScreen
import com.example.deepwork.ui.session_management.create_session.add_time_block.AddTimeBlockViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeepWorkTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { padding ->
                    padding.calculateTopPadding()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.CREATE_SESSION,
                    ) {
                        composable(Routes.CREATE_SESSION) { backStackEntry ->
                            val viewModel = hiltViewModel<CreateSessionViewModel>(
                                viewModelStoreOwner = backStackEntry
                            )
                            CreateSessionScreen(
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState,
                                onNavigate = { navigateEvent -> navController.navigate(navigateEvent.route) },
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.CREATE_TIMEBLOCK) { navBackStackEntry ->
                            val viewModel = hiltViewModel<AddTimeBlockViewModel>(
                                viewModelStoreOwner = navBackStackEntry
                            )
                            AddTimeBlockScreen(
                                viewModel = viewModel,
                                snackbarHostState = snackbarHostState,
                                onNavigate = { navigateEvent -> navController.navigate(navigateEvent.route) },
                                onNavigateUp = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }
}
