package ie.tus.rocksolid.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.R
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressDashboardScreen(navController: NavHostController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    var totalSessions by remember { mutableIntStateOf(0) }
    var totalExercises by remember { mutableIntStateOf(0) }
    var thisWeekExercises by remember { mutableIntStateOf(0) }

    // Load from Firestore
    LaunchedEffect(uid) {
        val progressDocs = db.collection("Progress")
            .whereEqualTo("uid", uid)
            .get()
            .await()

        totalSessions = progressDocs.size()
        totalExercises = progressDocs.documents.sumOf {
            val exercises = it.get("exercises") as? List<Map<String, Any>> ?: emptyList()
            exercises.count { ex -> (ex["status"] == "completed" || ex["status"] == "partial") }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val mondayDate = sdf.format(cal.time)

        thisWeekExercises = progressDocs.documents.filter {
            it.getString("weekStart") == mondayDate
        }.sumOf {
            val exercises = it.get("exercises") as? List<Map<String, Any>> ?: emptyList()
            exercises.count { ex -> (ex["status"] == "completed" || ex["status"] == "partial") }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5F5))
    ) {
        item {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Back", color = Color.White)
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Climbing Stats",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_routes),
                            contentDescription = "Routes Icon",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Total Exercises: $totalExercises", fontSize = 16.sp, color = Color.White)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_level),
                            contentDescription = "Level Icon",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hardest Grade: V5", fontSize = 16.sp, color = Color.White) // Placeholder
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_completed),
                            contentDescription = "Completed Icon",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sessions Completed: $totalSessions", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Progress Charts")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Routes Climbed Over Time", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("[Chart Placeholder]", color = Color.Gray)
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Personal Bests") //can get this from survey
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Hardest Route: V5", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Max Fingerboard Hang: +20kg for 7s", fontSize = 16.sp)
                }
            }
        }

        item {
            SectionHeader(title = "Weekly Summary")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Routes Climbed: $thisWeekExercises", fontSize = 16.sp)
                    Text("Total Training Time: 8 hours", fontSize = 16.sp) // Placeholder
                }
            }
        }

        item {
            SectionHeader(title = "Goal Progress")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Goal: Climb V6", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.7f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Progress: 70%", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
