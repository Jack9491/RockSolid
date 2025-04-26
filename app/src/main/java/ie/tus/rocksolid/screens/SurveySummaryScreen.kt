package ie.tus.rocksolid.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.viewmodel.AuthViewModel
import ie.tus.rocksolid.viewmodel.SurveyViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveySummaryScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    surveyViewModel: SurveyViewModel
) {
    val responses = surveyViewModel.responses.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Survey Summary") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD32F2F))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Thank you for completing the survey!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Here’s a summary of your responses:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            responses.forEach { (index, response) ->
                Text(
                    text = "${index + 1}. Question $index",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "   ➔ $response",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val uid = authViewModel.getCurrentUserUid() ?: return@Button
                    val db = FirebaseFirestore.getInstance()

                    val fullLevel = responses[1] ?: ""
                    val extractedLevel = fullLevel
                        .substringAfterLast("(")
                        .removeSuffix(")")
                        .trim()

                    val data = mapOf(
                        "uid" to uid,
                        "experienceLevel" to (responses[0] ?: ""),
                        "level" to fullLevel,
                        "climbingFrequency" to (responses[2] ?: ""),
                        "trainingGoals" to (responses[3]?.split(", ") ?: listOf()),
                        "preferredStyle" to (responses[4] ?: ""),
                        "fitnessLevel" to (responses[5] ?: ""),
                        "injuries" to (responses[6] ?: ""),
                        "trainingDaysPerWeek" to (responses[7] ?: ""),
                        "includeCrossTraining" to ((responses[8] ?: "") == "Yes"),
                        "surveyType" to "Tailored",
                        "submittedAt" to Timestamp.now()
                    )

                    db.collection("SurveyAnswers").document(uid)
                        .set(data)
                        .addOnSuccessListener {
                            Log.d("Survey", "Survey answers saved for user: $uid")

                            // Step 1: Update user's level field
                            db.collection("Users").document(uid)
                                .update("level", extractedLevel)
                                .addOnSuccessListener {
                                    Log.d("Survey", "User level updated to $extractedLevel")
                                }
                                .addOnFailureListener {
                                    Log.e("Survey", "Failed to update user level: ${it.message}")
                                }

                            // Step 2: Mark isFirstTime = false in Users table
                            authViewModel.markSurveyComplete(
                                userId = uid,
                                onSuccess = {
                                    Log.d("Survey", "isFirstTime updated to false")

                                    // Step 3: Navigate back to home
                                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshHome", true)
                                    navController.navigate("homeScreen") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                onError = { error ->
                                    Log.e("Survey", "Failed to update isFirstTime: $error")
                                }
                            )
                        }
                        .addOnFailureListener {
                            Log.e("Survey", "Error writing survey: ${it.message}")
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Finish and Go to Home", color = Color.White)
            }
        }
    }
}
