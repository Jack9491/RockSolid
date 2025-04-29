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

@Composable
fun TailoredSetupSection1(
    navController: NavController,
    surveyViewModel: SurveyViewModel
) {
    var questionIndex by remember { mutableStateOf(0) }

    val questions = listOf(
        "How long have you been climbing?" to listOf("Less than 6 months", "6 months to 2 years", "2+ years", "5+ years (Consistent Training)"),
        "What climbing grade do you comfortably climb?" to listOf(
            "Indoor: V0-V2 / Outdoor: V0-V1 (Beginner)",
            "Indoor: V3-V5 / Outdoor: V2-V4 (Intermediate)",
            "Indoor: V6+ / Outdoor: V5+ (Advanced)"
        ),
        "How often do you climb per week?" to listOf("1x per week", "2-3x per week", "4+ times per week")
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
                text = "Tailored Setup - Section 1",
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
                        Button(
                            onClick = {
                                surveyViewModel.saveResponse(questionIndex, answer)
                                if (questionIndex < questions.size - 1) {
                                    questionIndex++
                                } else {
                                    navController.navigate("tailoredSetupSection2")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(60.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColors[index])
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
