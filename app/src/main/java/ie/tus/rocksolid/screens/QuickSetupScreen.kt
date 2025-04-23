package ie.tus.rocksolid.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSetupScreen(navController: NavController, authViewModel: AuthViewModel) {
    var questionIndex by remember { mutableStateOf(0) }
    val answers = remember { mutableStateListOf<String>() }

    val questions = listOf(
        "What is your climbing experience level?" to listOf("Beginner (V0-V2)", "Intermediate (V3-V5)", "Advanced (V6+)"),
        "What is your primary training goal?" to listOf("Build endurance", "Improve finger strength", "Increase power", "Core and body tension"),
        "How many days per week can you train?" to listOf("1 day", "2-3 days", "4+ days")
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

            val question = questions[questionIndex]

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(question.first, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                    Spacer(modifier = Modifier.height(16.dp))

                    question.second.forEachIndexed { index, answer ->
                        val buttonColor = if (index < buttonColors.size) buttonColors[index] else Color.Gray

                        Button(
                            onClick = {
                                answers.add(answer)
                                if (questionIndex < questions.size - 1) {
                                    questionIndex++
                                } else {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                                    val db = FirebaseFirestore.getInstance()

                                    val data = mapOf(
                                        "uid" to uid,
                                        "experienceLevel" to answers.getOrNull(0),
                                        "trainingGoals" to listOf(answers.getOrNull(1)),
                                        "trainingDaysPerWeek" to answers.getOrNull(2),
                                        "surveyType" to "Quick",
                                        "submittedAt" to Timestamp.now()
                                    )

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

                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = (questionIndex + 1) / questions.size.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}
