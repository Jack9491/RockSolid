package ie.tus.rocksolid.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import ie.tus.rocksolid.screens.*
import ie.tus.rocksolid.screens.tailoredsetup.TailoredSetupSection1
import ie.tus.rocksolid.screens.tailoredsetup.TailoredSetupSection2
import ie.tus.rocksolid.screens.tailoredsetup.TailoredSetupSection3
import ie.tus.rocksolid.screens.tailoredsetup.TailoredSetupSection4
import ie.tus.rocksolid.viewmodel.AuthViewModel
import ie.tus.rocksolid.viewmodel.HomeViewModel
import ie.tus.rocksolid.viewmodel.SurveyViewModel

@Composable
fun BuildNavigationGraph(
    homeViewModel: HomeViewModel = viewModel(),
    surveyViewModel: SurveyViewModel = viewModel()
) {
    val navController = rememberNavController()
    val firebaseAuth = FirebaseAuth.getInstance()
    val authViewModel = AuthViewModel(firebaseAuth)

    NavHost(navController = navController, startDestination = Screen.WelcomeScreen.route) {
        composable(Screen.WelcomeScreen.route) { WelcomeScreen(navController) }

        composable(Screen.RegisterScreen.route) {
            RegisterScreen(navController, authViewModel, onRegisterSuccess = {
                Log.d("Navigation", "Navigating to LoginScreen after successful registration")
                navController.navigate(Screen.LoginScreen.route) {
                }
            })
        }

        composable(Screen.LoginScreen.route) {
            LoginScreen(navController, authViewModel, onLoginSuccess = {
                Log.d("Navigation", "Navigating to HomeScreen after successful Login")
                navController.navigate(Screen.HomeScreen.route) }
            )
        }

        composable(Screen.HomeScreen.route) {
            HomeScreen(navController, authViewModel)
        }

        composable(Screen.SurveyIntroductionScreen.route) { SurveyIntroductionScreen(navController) }
        composable(Screen.TrainingProgramScreen.route) { TrainingProgramScreen(navController) }

        composable(
            route = "exercise/{day}/{weekStart}",
            arguments = listOf(
                navArgument("day") { type = NavType.StringType },
                navArgument("weekStart") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val day = backStackEntry.arguments?.getString("day") ?: return@composable
            val weekStart = backStackEntry.arguments?.getString("weekStart") ?: return@composable
            ExerciseScreen(day = day, weekStart = weekStart, navController = navController)
        }


        composable(Screen.ProgressDashboardScreen.route) { ProgressDashboardScreen(navController) }

        composable(Screen.QuickSetupScreen.route) { QuickSetupScreen(navController, authViewModel) }
        composable(Screen.TailoredSetupSection1.route) { TailoredSetupSection1(navController, surveyViewModel) }
        composable(Screen.TailoredSetupSection2.route) { TailoredSetupSection2(navController, surveyViewModel) }
        composable(Screen.TailoredSetupSection3.route) { TailoredSetupSection3(navController, surveyViewModel) }
        composable(Screen.TailoredSetupSection4.route) { TailoredSetupSection4(navController, surveyViewModel) }


        composable(Screen.SurveySummaryScreen.route) {
            SurveySummaryScreen(navController, authViewModel, surveyViewModel)
        }

        composable(Screen.AchievementScreen.route) { backStackEntry ->
            val index = backStackEntry.arguments?.getString("achievementIndex")?.toIntOrNull() ?: 0
            AchievementScreen(navController, index)
        }

        composable(Screen.UserDetailsScreen.route) {
            UserDetailsScreen(navController, authViewModel)
        }

        composable(Screen.NotificationScreen.route) {
            NotificationScreen(navController)
        }

    }
}
