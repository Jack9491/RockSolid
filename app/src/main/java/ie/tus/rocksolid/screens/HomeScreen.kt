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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
    val userName = "Jack" // Placeholder, replace with actual user data
    val climbingLevel = "Intermediate"
    val completedRoutes = 45 // Placeholder data
    val currentTraining = "Strength & Endurance"
    val currentProgress = "Week 1"


    Surface(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Profile Card
            ProfileCard(userName, climbingLevel, completedRoutes)
            Spacer(modifier = Modifier.height(20.dp))

            // Training Program Section
            TrainingProgramSection(currentTraining)

            // Progress Dashboard Section
            ProgressDashboard(currentTraining)
        }
    }
}

@Composable
fun ProfileCard(userName: String, level: String, completedRoutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile_pic), // Replace with actual user image
                contentDescription = "Profile Picture",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = userName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(text = "Level: $level", fontSize = 16.sp, color = Color.Gray)
                Text(text = "Routes Completed: $completedRoutes", fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun TrainingProgramSection(currentTraining: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                onClick = { /* Navigate to training details */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("View Training Plan", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ProgressDashboard(currentProgress: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column (modifier = Modifier.padding(16.dp)) {
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
                onClick = { /* Navigate to progress dashboard */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("View Progress Dashboard", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}