package ie.tus.rocksolid.navigation

sealed class Screen(val route: String) {

    object WelcomeScreen: Screen("welcomeScreen")
    object LoginScreen: Screen("loginScreen")
    object RegisterScreen: Screen("registerScreen")
    object HomeScreen: Screen("homeScreen")
    object TrainingProgramScreen: Screen("trainingProgramScreen")
    object ExerciseScreen : Screen("exercise/{day}/{weekStart}") {
        fun passArgs(day: String, weekStart: String) = "exercise/$day/$weekStart"
    }
    object ProgressDashboardScreen: Screen("progressDashboardScreen")
    object SurveyIntroductionScreen : Screen("surveyIntroductionScreen")
    object QuickSetupScreen : Screen("quickSetupScreen")
    object TailoredSetupSection1 : Screen("tailoredSetupSection1")
    object TailoredSetupSection2 : Screen("tailoredSetupSection2")
    object TailoredSetupSection3 : Screen("tailoredSetupSection3")
    object TailoredSetupSection4 : Screen("tailoredSetupSection4")
    object SurveySummaryScreen : Screen("surveySummaryScreen")
    object AchievementScreen : Screen("achievementScreen/{achievementIndex}") {
        fun createRoute(index: Int) = "achievementScreen/$index"
    }
    object UserDetailsScreen : Screen("userDetailsScreen")
    object NotificationScreen : Screen("notificationScreen")


}

val screens = listOf(
    Screen.WelcomeScreen,
    Screen.LoginScreen,
    Screen.RegisterScreen,
    Screen.HomeScreen,
    Screen.TrainingProgramScreen,
    Screen.ExerciseScreen,
    Screen.ProgressDashboardScreen,
    Screen.SurveyIntroductionScreen,
    Screen.QuickSetupScreen,
    Screen.TailoredSetupSection1,
    Screen.TailoredSetupSection2,
    Screen.TailoredSetupSection3,
    Screen.TailoredSetupSection4,
    Screen.SurveySummaryScreen,
    Screen.UserDetailsScreen,
    Screen.NotificationScreen
)