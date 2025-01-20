package ie.tus.rocksolid.navigation

sealed class Screen(val route: String) {

    object WelcomeScreen: Screen("welcomeScreen")
    object LoginScreen: Screen("loginScreen")
    object RegisterScreen: Screen("registerScreen")
    object HomeScreen: Screen("homeScreen")

}

val screens = listOf(
    Screen.WelcomeScreen,
    Screen.LoginScreen,
    Screen.RegisterScreen,
    Screen.HomeScreen
)