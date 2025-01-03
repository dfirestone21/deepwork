package com.example.deepwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeepWorkTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Routes.CREATE_SESSION
                ) {
                    composable(Routes.CREATE_SESSION) { backStackEntry ->
                        val viewModel = hiltViewModel<CreateSessionViewModel>(
                            viewModelStoreOwner = backStackEntry
                        )
                        CreateSessionScreen(
                            viewModel = viewModel,
                            onNavigate = navController::navigate,
                            onNavigateUp = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
