package ie.tus.rocksolid.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.R
import ie.tus.rocksolid.navigation.Screen
import ie.tus.rocksolid.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import coil.compose.rememberAsyncImagePainter


@Composable
fun HomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var isFirstTimeUser by remember { mutableStateOf(false) }
    var checkCompleted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var showCoachOverlay by remember { mutableStateOf(true) }
    var coachStep by remember { mutableStateOf(0) }
    val refreshKey = remember { mutableStateOf(System.currentTimeMillis()) }
    val currentBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = currentBackStackEntry?.savedStateHandle

    // state variables for real data
    var trainingFocus by remember { mutableStateOf("Loading...") }
    var trainingWeekLabel by remember { mutableStateOf("Loading...") }
    var hasUnreadNotification by remember { mutableStateOf(false) }

    LaunchedEffect(savedStateHandle?.get<Boolean>("refreshHome")) {
        savedStateHandle?.remove<Boolean>("refreshHome")
        refreshKey.value = System.currentTimeMillis()
    }

    val coachTips = listOf(
        "Hi I am Coach Rocky. I am your coach and general assistant to help you navigate through the app. Let's get started!",
        "This is the User Card. Here you can see a quick view of your stats, and if you click it you can edit your details.",
        "This is the Survey Screen. Here you will answer a survey to make the app customisable to your needs.",
        "This is the Training Plans. Here you will view your daily training plan and complete exercises.",
        "This is the Progress Dashboard. Here you can view all of your progress statistics and see how close your next goal is.",
        "This is the Achievements Section. Here you can see your current achievements and your earned badges."
    )

    var userName by remember { mutableStateOf("--") }
    var userLevel by remember { mutableStateOf("--") }
    var completedSessions by remember { mutableStateOf(0) }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(refreshKey.value) {
        val userId = authViewModel.getCurrentUserUid()
        var achievementCheckInProgress = false

        if (userId != null) {
            val notificationRef = firestore.collection("Notification")
                .document(userId)
                .collection("Items")

            firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val firstTime = snapshot.getBoolean("isFirstTime") ?: true
                        isFirstTimeUser = firstTime
                        userName = snapshot.getString("name") ?: "--"
                        userLevel = snapshot.getString("level") ?: "--"
                        profilePictureUrl = snapshot.getString("profilePictureUrl")

                        if (!firstTime) {
                            showCoachOverlay = false
                        }

                        firestore.collection("Progress")
                            .whereEqualTo("uid", userId)
                            .get()
                            .addOnSuccessListener { progressSnapshot ->
                                val completed = progressSnapshot.size()
                                completedSessions = completed
                                checkCompleted = true

                                // Check if an achievement should trigger a notification
                                val unlockedAchievement = when (completed) {
                                    1 -> "First Session Completed"
                                    10 -> "10 Sessions Completed"
                                    50 -> "50 Sessions Completed"
                                    else -> null
                                }

                                if (unlockedAchievement != null && !achievementCheckInProgress) {
                                    achievementCheckInProgress = true // Lock it

                                    val query = notificationRef
                                        .whereEqualTo("achievement_id", unlockedAchievement)

                                    query.get().addOnSuccessListener { existing ->
                                        if (existing.isEmpty) {
                                            val notification = mapOf(
                                                "title" to "New Achievement Unlocked",
                                                "message" to "Well done! You've unlocked \"$unlockedAchievement\". Keep going to earn more!",
                                                "sent_at" to com.google.firebase.Timestamp.now(),
                                                "is_read" to false,
                                                "achievement_id" to unlockedAchievement
                                            )
                                            notificationRef.add(notification)
                                        }
                                        achievementCheckInProgress = false // Optional unlock if needed
                                    }.addOnFailureListener {
                                        achievementCheckInProgress = false // Unlock on error too
                                    }
                                }



                                // Check if there are unread notifications
                                notificationRef
                                    .whereEqualTo("is_read", false)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        if (!snapshot.isEmpty) {
                                            hasUnreadNotification = true
                                            showNotificationDialog = true
                                        }
                                    }
                            }
                            .addOnFailureListener {
                                Log.e("HOME", "Failed to fetch sessions")
                                checkCompleted = true
                            }
                    } else {
                        checkCompleted = true
                    }
                }
                .addOnFailureListener {
                    Log.e("HOME", "Failed to fetch user")
                    checkCompleted = true
                }

            // Load training focus and week
            val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val weekStart = sdf.format(cal.time)
            val docId = "${userId}_$weekStart"

            firestore.collection("TrainingPrograms").document(docId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val storedWeekStart = doc.getString("weekStart") ?: weekStart
                        trainingWeekLabel = try {
                            val parsedDate = sdf.parse(storedWeekStart)
                            val weekFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                            "Week of ${weekFormat.format(parsedDate!!)}"
                        } catch (e: Exception) {
                            "This Week"
                        }

                        val daysMap = doc.get("days") as? Map<*, *> ?: emptyMap<String, Any>()
                        val allExercises = daysMap.values
                            .filterIsInstance<List<*>>()
                            .flatten()
                            .mapNotNull { item ->
                                (item as? Map<*, *>)?.get("name") as? String
                            }

                        trainingFocus = when {
                            allExercises.any { it.contains("strength", ignoreCase = true) } -> "Strength & Endurance"
                            allExercises.any { it.contains("core", ignoreCase = true) } -> "Core Training"
                            allExercises.any { it.contains("endurance", ignoreCase = true) } -> "Endurance Focus"
                            allExercises.any { it.contains("finger", ignoreCase = true) } -> "Grip & Fingers"
                            else -> "Custom Focus"
                        }

                    } else {
                        trainingWeekLabel = "No Plan"
                        trainingFocus = "Not Available"
                    }
                }
                .addOnFailureListener {
                    trainingWeekLabel = "Error"
                    trainingFocus = "Error"
                }
        } else {
            checkCompleted = true
        }
    }


    val scrollTargets = listOf(0, 0, 250, 620, 1000, 1400)

    LaunchedEffect(coachStep) {
        if (coachStep in scrollTargets.indices) {
            coroutineScope.launch {
                scrollState.animateScrollTo(scrollTargets[coachStep])
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val dim = showCoachOverlay && coachStep != 1
            ProfileCard(
                userName = userName,
                level = userLevel,
                completedSessions = completedSessions,
                onClick = { navController.navigate(Screen.UserDetailsScreen.route) },
                dim = dim,
                navController = navController,
                profilePictureUrl = profilePictureUrl,
                showNotificationDot = hasUnreadNotification
            )

            Spacer(modifier = Modifier.height(20.dp))
            SurveySection(navController, dim = showCoachOverlay && coachStep != 2)

            TrainingProgramSection(
                currentTraining = trainingFocus,
                navController = navController,
                dim = showCoachOverlay && coachStep != 3
            )

            ProgressDashboard(
                currentProgress = trainingWeekLabel,
                navController = navController,
                dim = showCoachOverlay && coachStep != 4
            )

            AchievementsSection(navController, dim = showCoachOverlay && coachStep != 5)

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("welcomeScreen") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Logout", color = Color.White, fontSize = 16.sp)
            }
        }

        if (showCoachOverlay && isFirstTimeUser) {
            val alignment = listOf(
                Alignment.Center,           // Step 0
                Alignment.CenterEnd,        // Step 1
                Alignment.BottomStart,      // Step 2
                Alignment.TopStart,         // Step 3
                Alignment.TopCenter,        // Step 4
                Alignment.TopEnd            // Step 5
            )

            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = alignment.getOrElse(coachStep) { Alignment.Center }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val coachImage = when (coachStep) {
                        0 -> R.drawable.coach_happy
                        1, 2 -> R.drawable.coach_point_up
                        3, 4, 5 -> R.drawable.coach_point_down
                        else -> R.drawable.coach_happy
                    }

                    Image(
                        painter = painterResource(id = coachImage),
                        contentDescription = "Coach Rocky",
                        modifier = Modifier.size(140.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = coachTips[coachStep],
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = {
                                    showCoachOverlay = false
                                    navController.navigate("surveyIntroductionScreen") {
                                        popUpTo("homeScreen") { inclusive = true }
                                    }
                                }) {
                                    Text("Skip")
                                }
                                Button(
                                    onClick = {
                                        if (coachStep == coachTips.lastIndex) {
                                            showCoachOverlay = false
                                            navController.navigate("surveyIntroductionScreen") {
                                                popUpTo("homeScreen") { inclusive = true }
                                            }
                                        } else {
                                            coachStep++
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) {
                                    Text(
                                        if (coachStep == coachTips.lastIndex) "Start Training" else "Next Tip",
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showNotificationDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000)) // semi-transparent dark backdrop
                    .clickable(enabled = false) {} // absorb clicks
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Coach Rocky image on top, fully visible
                    Image(
                        painter = painterResource(id = R.drawable.coach_notification),
                        contentDescription = "Coach Rocky",
                        modifier = Modifier.size(180.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "New Notification",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "You have a new notification. Go to the Notifications screen to check it out!",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TextButton(onClick = { showNotificationDialog = false }) {
                                    Text("Dismiss")
                                }
                                Button(
                                    onClick = {
                                        showNotificationDialog = false
                                        navController.navigate(Screen.NotificationScreen.route)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) {
                                    Text("Go Now", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileCard(userName: String, level: String, completedSessions: Int, onClick: () -> Unit, dim: Boolean = false, navController: NavHostController, profilePictureUrl: String? = null, showNotificationDot: Boolean = false) {
    val alpha = if (dim) 0.3f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() }
            .shadow(8.dp, shape = RoundedCornerShape(12.dp))
            .alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Image(
                    painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.ic_profile_placeholder),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(50))
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Text Column
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            text = userName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_level),
                                contentDescription = "Level Icon",
                                modifier = Modifier.size(18.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Level: ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = level, fontSize = 16.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_routes),
                                contentDescription = "Routes Icon",
                                modifier = Modifier.size(18.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Sessions Completed: ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = "$completedSessions", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            // Notification Icon
            IconButton(
                onClick = { navController.navigate(Screen.NotificationScreen.route) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            if (showNotificationDot) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, shape = RoundedCornerShape(6.dp))
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                )
            }
        }
    }
}


@Composable
fun SurveySection(navController: NavHostController, dim: Boolean = false) {
    val alpha = if (dim) 0.3f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_survey),
                contentDescription = "Survey",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )
            Text(text = "Survey", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("surveyIntroductionScreen") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Survey", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun TrainingProgramSection(currentTraining: String, navController: NavHostController, dim: Boolean = false) {
    val alpha = if (dim) 0.3f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_training_plan),
                contentDescription = "Training Plan",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )
            Text(text = "Training Program", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Current Focus: $currentTraining", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("trainingProgramScreen") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("View Training Plan", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ProgressDashboard(currentProgress: String, navController: NavHostController, dim: Boolean = false) {
    val alpha = if (dim) 0.3f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_progress_dashboard),
                contentDescription = "Progress Dashboard",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 24.dp),
                contentScale = ContentScale.Fit
            )
            Text(text = "Progress Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Progress For: $currentProgress", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("ProgressDashboardScreen") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("View Progress Dashboard", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun AchievementsSection(navController: NavHostController, dim: Boolean = false) {
    val alpha = if (dim) 0.3f else 1f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🏆", fontSize = 100.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text(text = "Achievements", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate(Screen.AchievementScreen.createRoute(0)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("View Achievements", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}