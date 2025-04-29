package ie.tus.rocksolid.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun ExerciseScreen(day: String, weekStart: String, navController: NavHostController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var exercises by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val exerciseStatus = remember { mutableStateMapOf<Int, String>() }
    val setsDone = remember { mutableStateMapOf<Int, Int>() }
    val repsDone = remember { mutableStateMapOf<Int, Int>() }
    val partialConfirmed = remember { mutableStateMapOf<Int, Boolean>() }

    var alreadyCompleted by remember { mutableStateOf<Boolean?>(null) }
    var showTutorialDialog by remember { mutableStateOf<String?>(null) }
    var showStartTrainingDialog by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val progressCheck = db.collection("Progress")
            .whereEqualTo("uid", uid)
            .whereEqualTo("weekStart", weekStart)
            .whereEqualTo("day", day)
            .get()
            .await()

        alreadyCompleted = !progressCheck.isEmpty

        if (!alreadyCompleted!!) {
            val docId = "${uid}_$weekStart"
            val doc = db.collection("TrainingPrograms").document(docId).get().await()
            val allDays = doc.get("days") as? Map<String, List<Map<String, Any>>>
            val exerciseList = allDays?.get(day) ?: emptyList()

            val updatedExercises = mutableListOf<Map<String, Any>>()
            for (ex in exerciseList) {
                val name = ex["name"].toString()
                val snapshot = db.collection("Exercises")
                    .whereEqualTo("name", name)
                    .get()
                    .await()
                val tutorial = snapshot.documents.firstOrNull()?.getString("tutorial") ?: ""
                updatedExercises.add(ex + mapOf("tutorial" to tutorial))
            }

            exercises = updatedExercises
        }
    }

    val canSubmit = exercises.indices.all { i ->
        val status = exerciseStatus[i]
        when (status) {
            "completed", "incomplete" -> true
            "partial" -> partialConfirmed[i] == true
            else -> false
        }
    }

    when (alreadyCompleted) {
        null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }

        true -> Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("You've already completed this workout!", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }

        false -> {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Training: ${day.replaceFirstChar { it.uppercase() }}", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))

                if (showStartTrainingDialog) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000))
                            .clickable(enabled = false) {} // absorbs outside clicks
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Coach image (whistle version)
                            Image(
                                painter = painterResource(id = R.drawable.coach_whistle),
                                contentDescription = "Coach Rocky Whistle",
                                modifier = Modifier.size(180.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Ready to train? 3...2...1.. Go Go Go",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { showStartTrainingDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                    ) {
                                        Text("Start Training", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }


                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                ) {
                    exercises.forEachIndexed { index, ex ->
                        val name = ex["name"].toString()
                        val desc = ex["description"]?.toString().orEmpty()
                        val sets = ex["sets"]?.toString().orEmpty()
                        val reps = ex["reps"]?.toString().orEmpty()
                        val type = ex["type"]?.toString().orEmpty()
                        val tutorial = ex["tutorial"]?.toString().orEmpty()

                        val status = remember { mutableStateOf(exerciseStatus[index] ?: "") }
                        exerciseStatus[index] = status.value
                        setsDone.putIfAbsent(index, 0)
                        repsDone.putIfAbsent(index, 0)
                        partialConfirmed.putIfAbsent(index, false)

                        ExerciseCard(
                            index = index,
                            name = name,
                            description = desc,
                            sets = sets,
                            reps = reps,
                            type = type,
                            status = status,
                            setsDone = remember { mutableStateOf(setsDone[index] ?: 0) },
                            repsDone = remember { mutableStateOf(repsDone[index] ?: 0) },
                            setsDoneMap = setsDone,
                            repsDoneMap = repsDone,
                            partialConfirmed = partialConfirmed,
                            tutorial = tutorial,
                            onTutorialClick = { showTutorialDialog = tutorial }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        scope.launch {
                            val totalExercises = exercises.size
                            val completedCount = exerciseStatus.count { it.value == "completed" }
                            val partialCount = exerciseStatus.count { it.value == "partial" }
                            val incompleteCount = exerciseStatus.count { it.value == "incomplete" }
                            val sessionCompleted = exerciseStatus.values.all { it in listOf("completed", "partial", "incomplete") }

                            val result = exercises.mapIndexed { i, ex ->
                                val status = exerciseStatus[i] ?: "not_selected"
                                val setsOriginal = ex["sets"]?.toString()?.toIntOrNull() ?: 0
                                val repsOriginal = ex["reps"]?.toString()?.toIntOrNull() ?: 0
                                val type = ex["type"]?.toString().orEmpty()

                                val (setsFinal, repsFinal) = when {
                                    type == "warmup" || type == "cooldown" ->
                                        if (status == "completed") 1 to 1 else 0 to 0
                                    status == "completed" -> setsOriginal to repsOriginal
                                    status == "partial" -> (setsDone[i] ?: 0) to (repsDone[i] ?: 0)
                                    else -> 0 to 0
                                }

                                mapOf(
                                    "name" to ex["name"],
                                    "status" to status,
                                    "setsDone" to setsFinal,
                                    "repsDone" to repsFinal
                                )
                            }

                            val progress = mapOf(
                                "uid" to uid,
                                "weekStart" to weekStart,
                                "day" to day,
                                "timestamp" to Timestamp.now(),
                                "sessionCompleted" to sessionCompleted,
                                "completedExercises" to completedCount,
                                "partialExercises" to partialCount,
                                "incompleteExercises" to incompleteCount,
                                "totalExercises" to totalExercises,
                                "exercises" to result
                            )

                            val docId = "${uid}_${weekStart}_$day"
                            db.collection("Progress").document(docId).set(progress).addOnSuccessListener {
                                Toast.makeText(context, "Workout saved!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }.addOnFailureListener {
                                Toast.makeText(context, "Error saving workout", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canSubmit) Color(0xFFD32F2F) else Color.Gray
                    )
                ) {
                    Text("Finish Workout", color = Color.White)
                }
            }

            showTutorialDialog?.let { tutorialText ->
                AlertDialog(
                    onDismissRequest = { showTutorialDialog = null },
                    title = { Text("Exercise Tutorial", fontWeight = FontWeight.Bold) },
                    text = { Text(tutorialText, fontSize = 14.sp) },
                    confirmButton = {
                        Button(onClick = { showTutorialDialog = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    index: Int,
    name: String,
    description: String,
    sets: String,
    reps: String,
    type: String,
    status: MutableState<String>,
    setsDone: MutableState<Int>,
    repsDone: MutableState<Int>,
    setsDoneMap: MutableMap<Int, Int>,
    repsDoneMap: MutableMap<Int, Int>,
    partialConfirmed: MutableMap<Int, Boolean>,
    tutorial: String,
    onTutorialClick: () -> Unit
) {
    val isWarmupOrCooldown = type == "warmup" || type == "cooldown"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                if (tutorial.isNotBlank()) {
                    IconButton(onClick = onTutorialClick) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                }
            }

            if (sets.isNotEmpty() || reps.isNotEmpty()) {
                Text("Sets: $sets   Reps: $reps", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 13.sp, color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val buttons = if (isWarmupOrCooldown) {
                listOf("completed" to "Completed", "incomplete" to "Not Done")
            } else {
                listOf("completed" to "Completed", "partial" to "Partial", "incomplete" to "Not Done")
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                buttons.forEach { (value, label) ->
                    val isSelected = status.value == value
                    Button(
                        onClick = {
                            status.value = value
                            if (value != "partial") partialConfirmed[index] = true
                        },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFFD32F2F) else Color.LightGray
                        )
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            if (status.value == "partial" && !isWarmupOrCooldown) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("How much did you complete?", fontSize = 13.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    WheelPicker("Sets", 0..(sets.toIntOrNull() ?: 5), setsDone)
                    WheelPicker("Reps", 0..(reps.toIntOrNull() ?: 10), repsDone)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        setsDoneMap[index] = setsDone.value
                        repsDoneMap[index] = repsDone.value
                        partialConfirmed[index] = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Confirm Partial", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WheelPicker(label: String, range: IntRange, selected: MutableState<Int>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        LazyColumn(
            modifier = Modifier.height(80.dp).width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(range.count()) { i ->
                val value = range.first + i
                Text(
                    text = value.toString(),
                    fontSize = 18.sp,
                    fontWeight = if (value == selected.value) FontWeight.Bold else FontWeight.Normal,
                    color = if (value == selected.value) Color.Black else Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected.value = value }
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}