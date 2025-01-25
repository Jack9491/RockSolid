package ie.tus.rocksolid.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import ie.tus.rocksolid.R

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val userName = "Jack" // Placeholder
    val climbingLevel = "Intermediate"
    val completedRoutes = 45 // Placeholder
    val currentTraining = "Strength & Endurance"
    val currentProgress = "Week 1"


    Surface(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Profile Card
                ProfileCard(userName, climbingLevel, completedRoutes)
                Spacer(modifier = Modifier.height(20.dp))

                // Training Program Section
                TrainingProgramSection(currentTraining, navController)

                // Progress Dashboard Section
                ProgressDashboard(currentProgress, navController)
            }

            // Logout Button
            Button(
                onClick = {
                    auth.signOut()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("welcomeScreen") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Logout", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ProfileCard(userName: String, level: String, completedRoutes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)) // Light blue background
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
                    .clip(RoundedCornerShape(50)) // Circular image
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = userName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_level), // Level icon
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
                        painter = painterResource(id = R.drawable.ic_routes), // Routes completed icon
                        contentDescription = "Routes Icon",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Routes Completed: $completedRoutes", fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TrainingProgramSection(currentTraining: String, navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center content
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

@Composable
fun ProgressDashboard(currentProgress: String, navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center content
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
