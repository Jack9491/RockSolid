package ie.tus.rocksolid.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.rpc.context.AttributeContext.Auth
import ie.tus.rocksolid.screens.WelcomeScreen
import ie.tus.rocksolid.screens.RegisterScreen
import ie.tus.rocksolid.screens.LoginScreen
import ie.tus.rocksolid.screens.HomeScreen
import ie.tus.rocksolid.screens.ProgressDashboardScreen
import ie.tus.rocksolid.screens.TrainingProgramScreen
import ie.tus.rocksolid.viewmodel.AuthViewModel
import ie.tus.rocksolid.viewmodel.HomeViewModel


@Composable
fun BuildNavigationGraph(
    homeViewModel: HomeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val firebaseAuth = FirebaseAuth.getInstance()
    val authViewModel = AuthViewModel(firebaseAuth)

    NavHost(navController = navController, startDestination = Screen.WelcomeScreen.route) {
        composable(Screen.WelcomeScreen.route) { WelcomeScreen(navController) }
        composable(Screen.RegisterScreen.route) { RegisterScreen(navController) }
        composable(Screen.LoginScreen.route) { LoginScreen(navController, authViewModel, onLoginSuccess = {
            Log.d("TestingStuff", "OH NO")
            navController.navigate(Screen.HomeScreen.route)
        }) }
        composable(Screen.HomeScreen.route) { HomeScreen(navController) }
        composable(Screen.TrainingProgramScreen.route) { TrainingProgramScreen(navController) }
        composable(Screen.ProgressDashboardScreen.route) { ProgressDashboardScreen(navController) }
    }
}
