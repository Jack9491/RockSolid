package ie.tus.rocksolid.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
    val responses = surveyViewModel.responses.value  // Get the current responses map

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
                    val userId = authViewModel.getCurrentUserUid() ?: ""
                    authViewModel.markSurveyComplete(
                        userId = userId,
                        onSuccess = {
                            Log.d("SurveySummary", "Survey marked as complete. Navigating to home.")
                            navController.navigate("homeScreen") {
                                popUpTo("homeScreen") { inclusive = true }
                            }
                        },
                        onError = { error ->
                            Log.d("SurveySummary", "Error updating Firestore: $error")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Finish and Go to Home", color = Color.White)
            }
        }
    }
}
