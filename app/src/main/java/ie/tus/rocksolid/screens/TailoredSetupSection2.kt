package ie.tus.rocksolid.screens.tailoredsetup

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
import ie.tus.rocksolid.viewmodel.SurveyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailoredSetupSection2(navController: NavController, surveyViewModel: SurveyViewModel) {
    var questionIndex by remember { mutableStateOf(0) }

    val questions = listOf(
        "What are your primary climbing goals? (Select all that apply)" to listOf(
            "Improve overall endurance",
            "Build power and strength",
            "Increase finger strength",
            "Enhance core strength and body tension",
            "Injury prevention and recovery",
            "General fitness and health"
        ),
        "Which style of climbing do you focus on most?" to listOf(
            "Bouldering",
            "Sport Climbing",
            "Trad Climbing",
            "General (a mix of all)"
        )
    )

    val buttonColors = listOf(
        Color(0xFFEF5350),  // Soft Red
        Color(0xFFE53935),  // Medium Red
        Color(0xFFD32F2F),  // Deep Red
        Color(0xFFC62828)   // Dark Red
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
            Text(
                text = "Tailored Setup - Section 2",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = questions[questionIndex].first,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    questions[questionIndex].second.forEachIndexed { index, answer ->
                        val buttonColor = if (index < buttonColors.size) buttonColors[index] else Color(0xFFC62828)
                        Button(
                            onClick = {
                                surveyViewModel.saveResponse(questionIndex + 3, answer)  // Offset for section 2
                                if (questionIndex < questions.size - 1) {
                                    questionIndex++
                                } else {
                                    navController.navigate("tailoredSetupSection3")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
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

