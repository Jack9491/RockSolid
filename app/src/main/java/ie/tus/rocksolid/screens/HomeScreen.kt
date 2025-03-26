package ie.tus.rocksolid.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.delay
import ie.tus.rocksolid.viewmodel.AuthViewModel

@Composable
fun HomeScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var isFirstTimeUser by remember { mutableStateOf(false) }
    var checkCompleted by remember { mutableStateOf(false) }

    // Check Firestore to determine if the user is a first-time user
    LaunchedEffect(Unit) {
        val userId = authViewModel.getCurrentUserUid()
        if (userId != null) {
            firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    isFirstTimeUser = document.getBoolean("isFirstTime") ?: true
                    checkCompleted = true
                    Log.d("HomeScreen", "isFirstTimeUser: $isFirstTimeUser")
                }
                .addOnFailureListener {
                    checkCompleted = true
                    Log.d("HomeScreen", "Firestore error: ${it.message}")
                }
        } else {
            checkCompleted = true
            Log.d("HomeScreen", "User ID is null. Cannot check Firestore.")
        }
    }

    // Navigate to the survey after a delay if it's the user's first time
    LaunchedEffect(checkCompleted, isFirstTimeUser) {
        if (checkCompleted && isFirstTimeUser) {
            delay(30000)  // 30-second delay
            try {
                navController.navigate("surveyIntroductionScreen") {
                    popUpTo("homeScreen") { inclusive = false }
                }
                Log.d("HomeScreen", "Navigating to Survey Introduction Screen")
            } catch (e: Exception) {
                Log.e("HomeScreen", "Navigation failed: ${e.message}")
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileCard("Jack", "Intermediate", 45)
            Spacer(modifier = Modifier.height(20.dp))
            SurveySection(navController)
            TrainingProgramSection("Strength & Endurance", navController)
            ProgressDashboard("Week 1", navController)
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

            Button(
                onClick = { navController.navigate(Screen.AchievementScreen.createRoute(0)) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Achievements", fontSize = 16.sp, color = Color.White)
            }

        }
    }
}

// ProfileCard Composable
@Composable
fun ProfileCard(userName: String, level: String, completedSessions: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile_pic),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = userName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_level),
                        contentDescription = "Level Icon",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Level: $level", fontSize = 16.sp, color = Color.White)
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
                    Text(text = "Sessions Completed: $completedSessions", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

// User Survey Section
@Composable
fun SurveySection(navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

// Training Program Section
@Composable
fun TrainingProgramSection(currentTraining: String, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

// Progress Dashboard Section
@Composable
fun ProgressDashboard(currentProgress: String, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Text(text = "Current Focus: $currentProgress", fontSize = 16.sp, color = Color.Gray)
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
