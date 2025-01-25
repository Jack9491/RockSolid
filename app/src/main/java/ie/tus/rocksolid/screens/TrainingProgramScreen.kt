package ie.tus.rocksolid.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.Calendar

@Composable
fun TrainingProgramScreen(navController: NavHostController) {
    val today = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }
    var selectedDay by remember { mutableStateOf(today) }
    val workoutCompletion = remember { mutableStateMapOf<String, String>() }
    val notes = remember { mutableStateListOf<String>() }
    val currentExerciseIndex = remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5F5)) // Light gray background for the screen
    ) {
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

        // Calendar Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            CustomCalendar(today, selectedDay) { day ->
                selectedDay = day
                currentExerciseIndex.value = 0
            }
        }

        // Training Details Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            TrainingDetails(
                selectedDay = selectedDay,
                today = today,
                workoutCompletion = workoutCompletion,
                notes = notes,
                currentExerciseIndex = currentExerciseIndex
            )
        }

        // Notes Section
        NotesSection(notes)
    }
}

@Composable
fun NotesSection(notes: List<String>) {
    if (notes.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Notes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F), // Red header for the notes
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                notes.forEach { note ->
                    Text(
                        text = "- $note",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomCalendar(today: Int, selectedDay: Int, onDaySelected: (Int) -> Unit) {
    val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "January ${Calendar.getInstance().get(Calendar.YEAR)}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(daysInMonth) { day ->
                val dayNumber = day + 1
                val isToday = dayNumber == today
                val isSelected = dayNumber == selectedDay
                val isLocked = dayNumber > today

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when {
                                isSelected -> Color(0xFFD32F2F)
                                isToday -> Color.Black
                                isLocked -> Color.LightGray
                                else -> Color.White
                            }
                        )
                        .clickable(enabled = !isLocked) { onDaySelected(dayNumber) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber.toString(),
                        color = when {
                            isSelected || isToday -> Color.White
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

@Composable
fun TrainingDetails(
    selectedDay: Int,
    today: Int,
    workoutCompletion: MutableMap<String, String>,
    notes: MutableList<String>,
    currentExerciseIndex: MutableState<Int>
) {
    var showReasonDialog by remember { mutableStateOf<Pair<Int, String>?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        val workoutDetails = if (selectedDay == today) dummyWorkoutDetails else getClimbingWorkoutsForDay(selectedDay)
        items(workoutDetails.size) { index ->
            val workout = workoutDetails[index]
            val status = workoutCompletion["$selectedDay-$index"] ?: ""

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (index == currentExerciseIndex.value) Color(0xFFFFCDD2) else Color.White
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = workout,
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = if (index == currentExerciseIndex.value) FontWeight.Bold else FontWeight.Normal
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = status == "completed",
                            onCheckedChange = if (selectedDay == today) {
                                { checked ->
                                    if (checked) {
                                        workoutCompletion["$selectedDay-$index"] = "completed"
                                        currentExerciseIndex.value = index + 1
                                    }
                                }
                            } else null
                        )
                        Text(
                            text = "Completed",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 16.dp)
                        )

                        Checkbox(
                            checked = status == "incomplete",
                            onCheckedChange = if (selectedDay == today) {
                                { checked ->
                                    if (checked) {
                                        workoutCompletion["$selectedDay-$index"] = "incomplete"
                                        showReasonDialog = index to workout
                                    }
                                }
                            } else null
                        )
                        Text(
                            text = "Not Completed",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }

    // Show Reason Dialog
    showReasonDialog?.let { (index, workout) ->
        ReasonDialog(
            workout = workout,
            onDismiss = { showReasonDialog = null },
            onConfirm = { reason ->
                notes.add("$workout - Reason: $reason")
            }
        )
    }
}


@Composable
fun ReasonDialog(
    workout: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                onConfirm(reason)
                onDismiss()
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        },
        title = { Text("Why was the workout not completed?") },
        text = {
            Column {
                Text("Provide a reason below:")
                TextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

fun getClimbingWorkoutsForDay(day: Int): List<String> {
    return when (day % 5) {
        0 -> listOf(
            "Warm-up: Mobility Drills - 10 min",
            "Exercise 1: Fingerboard Training - Max Hangs (7x10s)",
            "Exercise 2: Campus Board Ladders - 5 sets",
            "Exercise 3: Weighted Pull-ups - 3x8 reps",
            "Cool-down: Stretching - 10 min"
        )
        1 -> listOf(
            "Warm-up: General Stretching - 8 min",
            "Exercise 1: Pinch Block Training - 3x10s holds",
            "Exercise 2: Boulder Circuit - 6 problems at V2-V4",
            "Exercise 3: Dead Hangs - 4x12s",
            "Cool-down: Foam Rolling - 10 min"
        )
        2 -> listOf(
            "Warm-up: Dynamic Stretching - 5 min",
            "Exercise 1: ARC Training - 30 min at low intensity",
            "Exercise 2: Finger Rolls - 3x12 reps",
            "Exercise 3: Core Conditioning (Plank + Leg Raises) - 3x1 min",
            "Cool-down: Relaxation Stretch - 8 min"
        )
        3 -> listOf(
            "Warm-up: Jump Rope - 5 min",
            "Exercise 1: Campus Board: Double Dynos - 5 sets",
            "Exercise 2: Boulder Projecting - 1 hour",
            "Exercise 3: Core Stability - 3x45s side planks",
            "Cool-down: Easy Climbing - 15 min"
        )
        else -> listOf(
            "Warm-up: Easy Traversing - 10 min",
            "Exercise 1: Repeaters on Fingerboard - 6x10s/5s",
            "Exercise 2: One-Arm Lock-offs - 3x each arm",
            "Exercise 3: Mobility Training (Shoulders + Hips) - 10 min",
            "Cool-down: Light Stretching - 10 min"
        )
    }
}

val dummyWorkoutDetails = listOf(
    "Warm-up: Mobility Drills - 10 min",
    "Exercise 1: Fingerboard Training - Max Hangs (7x10s)",
    "Exercise 2: Campus Board Ladders - 5 sets",
    "Exercise 3: Weighted Pull-ups - 3x8 reps",
    "Cool-down: Stretching - 10 min"
)
