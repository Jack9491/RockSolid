package ie.tus.rocksolid.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.navigation.Screen
import ie.tus.rocksolid.viewmodel.AuthViewModel

@Composable
fun QuickSetupScreen(navController: NavController, authViewModel: AuthViewModel) {
    var questionIndex by remember { mutableStateOf(0) }
    val experienceAnswer = remember { mutableStateOf<String?>(null) }
    val trainingDaysAnswer = remember { mutableStateOf<String?>(null) }
    val selectedGoals = remember { mutableStateListOf<String>() }
    var showVGradeInfo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        selectedGoals.clear()
    }

    val questions = listOf(
        "What is your climbing experience level?" to listOf(
            "Beginner (V0–V2)",
            "Intermediate (V3–V5)",
            "Advanced (V6+)"
        ),
        "What is your primary training goal? (Select all that apply)" to listOf(
            "Improve overall endurance",
            "Build power and strength",
            "Increase finger strength",
            "Enhance core strength and body tension",
            "General fitness and health"
        ),
        "How many days per week can you train?" to listOf(
            "1 day",
            "2–3 days",
            "4+ days"
        )
    )

    val buttonColors = listOf(
        Color(0xFFEF5350), Color(0xFFE53935), Color(0xFFD32F2F), Color(0xFFC62828)
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Quick Setup", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(16.dp))

            if (questionIndex < questions.size) {
                val question = questions[questionIndex]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question.first,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            if (questionIndex == 0) {
                                IconButton(onClick = { showVGradeInfo = true }) {
                                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.DarkGray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        question.second.forEachIndexed { index, answer ->
                            val buttonColor = if (index < buttonColors.size) buttonColors[index] else Color.Gray

                            when (questionIndex) {
                                1 -> {
                                    val isSelected = selectedGoals.contains(answer)
                                    Button(
                                        onClick = {
                                            if (isSelected) {
                                                selectedGoals.remove(answer)
                                            } else {
                                                selectedGoals.add(answer)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .height(60.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color.Gray else buttonColor
                                        )
                                    ) {
                                        Text(answer, fontSize = 16.sp, color = Color.White)
                                    }
                                }

                                else -> {
                                    Button(
                                        onClick = {
                                            when (questionIndex) {
                                                0 -> experienceAnswer.value = answer
                                                2 -> trainingDaysAnswer.value = answer
                                            }
                                            questionIndex++
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .height(60.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                                    ) {
                                        Text(answer, fontSize = 16.sp, color = Color.White)
                                    }
                                }
                            }
                        }

                        if (questionIndex == 1) {
                            Button(
                                onClick = {
                                    questionIndex++
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Continue", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = (questionIndex + 1) / questions.size.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            if (showVGradeInfo) {
                AlertDialog(
                    onDismissRequest = { showVGradeInfo = false },
                    confirmButton = {
                        TextButton(onClick = { showVGradeInfo = false }) {
                            Text("OK", color = Color(0xFFD32F2F))
                        }
                    },
                    title = { Text("What is a V Grade?", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("If you don't know what a V Grade is, it's the scale that measures the difficulty of a bouldering climb — like levels in a game! The higher the number, the harder the climb.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("V0 → Beginner\nV3 → Intermediate\nV6+ → Advanced", fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Typically:\n• 0–6 months → Beginner (V0–V2)\n• 6–24 months → Intermediate (V3–V5)\n• 2+ years → Advanced (V6+)")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = Color.White
                )
            }

            // Final Submission
            if (questionIndex == questions.size) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Scaffold
                val db = FirebaseFirestore.getInstance()

                val safeGoals = if (selectedGoals.isEmpty()) listOf("General fitness and health") else selectedGoals

                val experienceLabel = when (experienceAnswer.value) {
                    "Beginner (V0–V2)" -> "Less than 6 months"
                    "Intermediate (V3–V5)" -> "6 months to 2 years"
                    "Advanced (V6+)" -> "2+ years"
                    else -> "Less than 6 months"
                }

                val levelDetail = when (experienceAnswer.value) {
                    "Beginner (V0–V2)" -> "Indoor: V0-V2 / Outdoor: V0-V1 (Beginner)"
                    "Intermediate (V3–V5)" -> "Indoor: V3-V5 / Outdoor: V2-V4 (Intermediate)"
                    "Advanced (V6+)" -> "Indoor: V6+ / Outdoor: V5+ (Advanced)"
                    else -> "Indoor: V0-V2 / Outdoor: V0-V1 (Beginner)"
                }

                val extractedLevel = levelDetail
                    .substringAfterLast("(")
                    .removeSuffix(")")
                    .trim()

                val data = mapOf(
                    "uid" to uid,
                    "experienceLevel" to experienceLabel,
                    "level" to levelDetail,
                    "trainingGoals" to safeGoals,
                    "trainingDaysPerWeek" to trainingDaysAnswer.value,
                    "preferredStyle" to "General",
                    "fitnessLevel" to "Average",
                    "injuries" to "No injuries",
                    "includeCrossTraining" to false,
                    "surveyType" to "Quick",
                    "submittedAt" to Timestamp.now()
                )

                LaunchedEffect(Unit) {
                    db.collection("SurveyAnswers").document(uid)
                        .set(data)
                        .addOnSuccessListener {
                            authViewModel.markSurveyComplete(
                                userId = uid,
                                onSuccess = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshHome", true)
                                    navController.navigate(Screen.HomeScreen.route) {
                                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                                    }
                                },
                                onError = {}
                            )
                        }
                }

                db.collection("Users").document(uid).update("level", extractedLevel)
                    .addOnSuccessListener {
                        Log.d("QuickSurvey", "User level updated to $extractedLevel")
                    }
                    .addOnFailureListener {
                        Log.e("QuickSurvey", "Failed to update level: ${it.message}")
                    }
            }
        }
    }
}
