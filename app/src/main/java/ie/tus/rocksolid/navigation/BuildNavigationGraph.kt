package ie.tus.rocksolid.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import ie.tus.rocksolid.screens.WelcomeScreen
import ie.tus.rocksolid.screens.RegisterScreen
import ie.tus.rocksolid.screens.LoginScreen
import ie.tus.rocksolid.screens.HomeScreen
import ie.tus.rocksolid.screens.TrainingProgramScreen
import ie.tus.rocksolid.viewmodel.HomeViewModel


@Composable
fun BuildNavigationGraph(
    homeViewModel: HomeViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.WelcomeScreen.route) {
        composable(Screen.WelcomeScreen.route) { WelcomeScreen(navController) }
        composable(Screen.RegisterScreen.route) { RegisterScreen(navController) }
        composable(Screen.LoginScreen.route) { LoginScreen(navController) }
        composable(Screen.HomeScreen.route) { HomeScreen(navController) }
        composable(Screen.TrainingProgramScreen.route) { TrainingProgramScreen(navController) }
        composable(Screen.TrainingProgramScreen.route) { TrainingProgramScreen(navController) }
    }
}
