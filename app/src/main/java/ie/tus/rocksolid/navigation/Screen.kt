package ie.tus.rocksolid.navigation

sealed class Screen(val route: String) {

    object WelcomeScreen: Screen("welcomeScreen")
    object LoginScreen: Screen("loginScreen")
    object RegisterScreen: Screen("registerScreen")
    object HomeScreen: Screen("homeScreen")
    object TrainingProgramScreen: Screen("trainingProgramScreen")
    object ProgressDashboardScreen: Screen("progressDashboardScreen")

}

val screens = listOf(
    Screen.WelcomeScreen,
    Screen.LoginScreen,
    Screen.RegisterScreen,
    Screen.HomeScreen,
    Screen.TrainingProgramScreen,
    Screen.ProgressDashboardScreen

)