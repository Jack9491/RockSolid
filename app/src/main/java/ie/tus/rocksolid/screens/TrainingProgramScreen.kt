package ie.tus.rocksolid.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.modelScripts.TrainingPlanGenerator
import ie.tus.rocksolid.utils.getWeekStartDate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrainingProgramScreen(navController: NavHostController) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val selectedDay = remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    val weekStart = getWeekStartDate(calendar.time)
    val trainingPlanState = remember { mutableStateOf<Map<String, List<Map<String, Any>>>?>(null) }
    val loading = remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Load plan from Firestore
    fun loadPlan() {
        if (userId == null) return
        loading.value = true
        val db = FirebaseFirestore.getInstance()
        val docId = "${userId}_$weekStart"
        db.collection("TrainingPrograms").document(docId).get()
            .addOnSuccessListener { doc ->
                val plan = doc.get("days") as? Map<String, List<Map<String, Any>>>
                trainingPlanState.value = plan
                loading.value = false
                Log.d("TRAINING_GEN", "Plan loaded successfully.")
            }
            .addOnFailureListener {
                loading.value = false
                Log.e("TRAINING_GEN", "Failed to load training plan", it)
            }
    }

    LaunchedEffect(Unit) {
        loadPlan()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Back", color = Color.White)
        }

        // Header
        Text(
            text = "Training Program",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomCalendar(
            today = calendar,
            selectedDay = selectedDay.value,
            onDaySelected = { selectedDay.value = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            loading.value -> CircularProgressIndicator()

            trainingPlanState.value == null -> {
                Text("No training plan found for this week.", fontSize = 16.sp)
                Button(
                    onClick = {
                        scope.launch {
                            Log.d("TRAINING_GEN", "Generating training plan...")
                            TrainingPlanGenerator.generatePlanForCurrentWeek { success ->
                                Log.d("TRAINING_GEN", "Generation success: $success")
                                if (success) {
                                    Toast.makeText(context, "Training plan generated!", Toast.LENGTH_SHORT).show()
                                    loadPlan()
                                } else {
                                    Toast.makeText(context, "Failed to generate training plan.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Generate Training Plan")
                }
            }

            else -> {
                val dayKey = getDayKey(selectedDay.value)
                val exercises = trainingPlanState.value?.get(dayKey).orEmpty()

                if (exercises.isEmpty()) {
                    Text("No exercises planned for this day.")
                } else {
                    Column {
                        Text("Exercises for $dayKey", fontSize = 20.sp, modifier = Modifier.padding(vertical = 8.dp))
                        exercises.forEach { exercise ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(text = exercise["name"].toString(), fontSize = 18.sp)
                                    exercise["description"]?.let {
                                        Text(text = it.toString(), fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // start training button
                        Button(
                            onClick = {
                                navController.navigate(
                                    ie.tus.rocksolid.navigation.Screen.ExerciseScreen.passArgs(dayKey, weekStart)
                                )
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Start Training")
                        }
                    }
                }
            }
        }
    }
}

fun getDayKey(dayOfMonth: Int): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "monday"
        Calendar.TUESDAY -> "tuesday"
        Calendar.WEDNESDAY -> "wednesday"
        Calendar.THURSDAY -> "thursday"
        Calendar.FRIDAY -> "friday"
        Calendar.SATURDAY -> "saturday"
        Calendar.SUNDAY -> "sunday"
        else -> "unknown"
    }
}

@Composable
fun CustomCalendar(today: Calendar, selectedDay: Int, onDaySelected: (Int) -> Unit) {
    val cal = Calendar.getInstance()
    val currentMonth = cal.get(Calendar.MONTH)
    val currentYear = cal.get(Calendar.YEAR)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} $currentYear",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(daysInMonth) { day ->
                val dayNumber = day + 1
                val isToday = today.get(Calendar.DAY_OF_MONTH) == dayNumber &&
                        today.get(Calendar.MONTH) == currentMonth &&
                        today.get(Calendar.YEAR) == currentYear
                val isSelected = dayNumber == selectedDay
                val isLocked = dayNumber > today.get(Calendar.DAY_OF_MONTH) &&
                        today.get(Calendar.MONTH) == currentMonth &&
                        today.get(Calendar.YEAR) == currentYear

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when {
                                isSelected -> Color(0xFFD32F2F)
                                isToday -> Color.White
                                isLocked -> Color.LightGray
                                else -> Color.White
                            }
                        )
                        .border(
                            width = if (isToday) 2.dp else 0.dp,
                            color = if (isToday) Color.Black else Color.Transparent
                        )
                        .clickable(enabled = !isLocked) {
                            onDaySelected(dayNumber)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber.toString(),
                        color = when {
                            isSelected -> Color.White
                            isToday -> Color.Black
                            isLocked -> Color.Gray
                            else -> Color.Black
                        },
                        fontSize = 16.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
