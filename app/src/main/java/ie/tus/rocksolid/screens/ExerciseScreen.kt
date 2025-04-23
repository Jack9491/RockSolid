package ie.tus.rocksolid.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun ExerciseScreen(day: String, weekStart: String, navController: NavHostController) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var exercises by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val exerciseStatus = remember { mutableStateMapOf<Int, String>() }
    val notes = remember { mutableStateMapOf<Int, String>() }

    LaunchedEffect(Unit) {
        val docId = "${uid}_$weekStart"
        val doc = db.collection("TrainingPrograms").document(docId).get().await()
        val allDays = doc.get("days") as? Map<String, List<Map<String, Any>>>
        exercises = allDays?.get(day) ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Training: ${day.replaceFirstChar { it.uppercase() }}", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        exercises.forEachIndexed { index, ex ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(ex["name"].toString(), fontSize = 18.sp)

                    Row {
                        RadioButton(
                            selected = exerciseStatus[index] == "completed",
                            onClick = { exerciseStatus[index] = "completed" }
                        )
                        Text("Completed")

                        Spacer(modifier = Modifier.width(8.dp))

                        RadioButton(
                            selected = exerciseStatus[index] == "partial",
                            onClick = { exerciseStatus[index] = "partial" }
                        )
                        Text("Partially Completed")

                        Spacer(modifier = Modifier.width(8.dp))

                        RadioButton(
                            selected = exerciseStatus[index] == "incomplete",
                            onClick = { exerciseStatus[index] = "incomplete" }
                        )
                        Text("Incomplete")
                    }

                    if (exerciseStatus[index] == "incomplete" || exerciseStatus[index] == "partial") {
                        OutlinedTextField(
                            value = notes[index] ?: "",
                            onValueChange = { notes[index] = it },
                            label = { Text("Reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                val result = exercises.mapIndexed { i, ex ->
                    mapOf(
                        "name" to ex["name"],
                        "status" to (exerciseStatus[i] ?: "not_selected"),
                        "note" to (notes[i] ?: "")
                    )
                }

                val progress = mapOf(
                    "uid" to uid,
                    "weekStart" to weekStart,
                    "day" to day,
                    "exercises" to result,
                    "timestamp" to Timestamp.now()
                )

                db.collection("Progress").add(progress).addOnSuccessListener {
                    Toast.makeText(context, "Workout saved!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }.addOnFailureListener {
                    Toast.makeText(context, "Error saving workout", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text("Finish Workout")
        }
    }
}
