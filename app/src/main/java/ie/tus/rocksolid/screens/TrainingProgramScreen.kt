package ie.tus.rocksolid.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.modelScripts.BeginnerExerciseTutorialInjector
import ie.tus.rocksolid.modelScripts.IntermediateAdvancedTutorialInjector
import ie.tus.rocksolid.modelScripts.TrainingPlanGenerator
import ie.tus.rocksolid.utils.getWeekStartDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun TrainingProgramScreen(navController: NavHostController) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val selectedDay = remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    val weekStart = getWeekStartDate(calendar.time)
    val trainingPlanState = remember { mutableStateOf<Map<String, List<Map<String, Any>>>?>(null) }
    val loading = remember { mutableStateOf(true) }
    val progressMap = remember { mutableStateMapOf<String, Boolean>() }
    val showDialog = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    fun loadPlan() {
        if (userId == null) return
        loading.value = true
        val docId = "${userId}_$weekStart"
        db.collection("TrainingPrograms").document(docId).get()
            .addOnSuccessListener { doc ->
                val plan = doc.get("days") as? Map<String, List<Map<String, Any>>>
                trainingPlanState.value = plan
                loading.value = false
            }
            .addOnFailureListener {
                loading.value = false
            }
    }

    LaunchedEffect(Unit) {
        loadPlan()
        if (userId != null) {
            val progressSnapshot = db.collection("Progress")
                .whereEqualTo("uid", userId)
                .whereEqualTo("weekStart", weekStart)
                .get()
                .await()

            progressSnapshot.documents.forEach { doc ->
                val day = doc.getString("day") ?: return@forEach
                progressMap[day] = true
            }
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Back Button
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Back", color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Training Program",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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
                                        TrainingPlanGenerator.generatePlanForCurrentWeek { success ->
                                            if (success) {
                                                Toast.makeText(context, "Training plan generated!", Toast.LENGTH_SHORT).show()
                                                loadPlan()
                                            } else {
                                                Toast.makeText(context, "Failed to generate training plan.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Generate Training Plan", color = Color.White)
                            }
                        }

                        else -> {
                            val dayKey = getDayKey(selectedDay.value)
                            val exercises = trainingPlanState.value?.get(dayKey)
                            val todayDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                            val isToday = selectedDay.value == todayDayOfMonth
                            val isCompleted = progressMap[dayKey] == true
                            val isInPast = selectedDay.value < todayDayOfMonth
                            val isInFuture = selectedDay.value > todayDayOfMonth

                            Text("Exercises for ${dayKey.replaceFirstChar { it.uppercase() }}", fontSize = 20.sp, modifier = Modifier.padding(vertical = 8.dp))

                            when {
                                exercises == null -> {
                                    Text("No training data available for this day", color = Color.Gray, fontSize = 14.sp)
                                }

                                exercises.isEmpty() -> {
                                    Text(" No exercises planned for this day. Rest up Champ!")
                                }

                                else -> {
                                    exercises.forEach { exercise ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                                            elevation = CardDefaults.cardElevation(4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = exercise["name"].toString(),
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                exercise["description"]?.let {
                                                    Text(
                                                        text = it.toString(),
                                                        fontSize = 14.sp,
                                                        color = Color(0xFFFFCDD2) // soft light red
                                                    )
                                                }
                                            }
                                        }
                                    }


                                    Spacer(modifier = Modifier.height(24.dp))

                                    if (isToday && !isCompleted) {
                                        Button(
                                            onClick = {
                                                navController.navigate(
                                                    ie.tus.rocksolid.navigation.Screen.ExerciseScreen.passArgs(dayKey, weekStart)
                                                )
                                            },
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(top = 16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                        ) {
                                            Text("Start Training", color = Color.White)
                                        }
                                    }

                                }
                            }

//                            Button(
//                                onClick = {
//                                    IntermediateAdvancedTutorialInjector.inject()
//                                    Toast.makeText(context, "Intermediate & Advanced Tutorials injected!", Toast.LENGTH_SHORT).show()
//                                },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)), // Blue
//                                modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
//                            ) {
//                                Text("Inject Intermediate & Advanced Tutorials", color = Color.White)
//                            }


                            LaunchedEffect(selectedDay.value, isToday, isCompleted, isInPast, isInFuture) {
                                if (exercises != null) {
                                    when {
                                        isToday && isCompleted -> {
                                            showDialog.value = "You're an ANIMAL! You already completed today's training! Come back tomorrow."
                                        }
                                        isInPast -> {
                                            showDialog.value = "You cannot train a day in the past... Unless you have a time machine!"
                                        }
                                        isInFuture -> {
                                            showDialog.value = "You can’t start future workouts. FOCUS ON THE PRESENT!"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Popup
            showDialog.value?.let { dialogText ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val emoji = when {
                            dialogText.contains("already completed") -> "✅"
                            dialogText.contains("past") -> "❌"
                            dialogText.contains("future") -> "⏳"
                            else -> "ℹ️"
                        }

                        Text(emoji, fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))

                        Text(
                            "Training Access",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Text(
                            text = dialogText,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        Button(
                            onClick = { showDialog.value = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("OK", color = Color.White, fontSize = 16.sp)
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

    val firstDayOfMonth = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val dayOfWeekOffset = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0

    val totalCells = daysInMonth + dayOfWeekOffset
    val weeks = (totalCells + 6) / 7
    val todayDay = today.get(Calendar.DAY_OF_MONTH)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} $currentYear",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Day headers (Mon to Sun)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                Text(it, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid background
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5)) // soft light background
                .padding(8.dp)
        ) {
            Column {
                repeat(weeks) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (dayIndex in 0..6) {
                            val cellIndex = week * 7 + dayIndex
                            val dayNumber = cellIndex - dayOfWeekOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val isToday = dayNumber == todayDay
                                val isSelected = dayNumber == selectedDay
                                val isLocked = dayNumber > todayDay &&
                                        cal.get(Calendar.MONTH) == currentMonth &&
                                        cal.get(Calendar.YEAR) == currentYear

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
                                            width = if (isToday) 2.dp else 1.dp,
                                            color = if (isToday) Color.Black else Color.LightGray
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
                            } else {
                                Spacer(modifier = Modifier.size(40.dp)) // empty cells
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
