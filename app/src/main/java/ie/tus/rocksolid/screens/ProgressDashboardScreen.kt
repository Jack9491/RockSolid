package ie.tus.rocksolid.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressDashboardScreen(navController: NavHostController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    var totalSessions by remember { mutableIntStateOf(0) }
    var totalExercises by remember { mutableIntStateOf(0) }
    var thisWeekExercises by remember { mutableIntStateOf(0) }

    var hardestGrade by remember { mutableStateOf("V5") }
    var personalBest by remember { mutableStateOf("Max Fingerboard Hang: +20kg for 7s") }
    var goalGrade by remember { mutableStateOf("V6") }
    var goalProgressSessions by remember { mutableIntStateOf(0) }
    val goalTargetSessions = 20

    var showEditDialog by remember { mutableStateOf(false) }
    var editField by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        val progressDocs = db.collection("Progress").whereEqualTo("uid", uid).get().await()
        totalSessions = progressDocs.size()
        totalExercises = progressDocs.documents.sumOf {
            val exercises = it.get("exercises") as? List<Map<String, Any>> ?: emptyList()
            exercises.count { ex -> (ex["status"] == "completed" || ex["status"] == "partial") }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        val mondayDate = sdf.format(cal.time)

        thisWeekExercises = progressDocs.documents.filter {
            it.getString("weekStart") == mondayDate
        }.sumOf {
            val exercises = it.get("exercises") as? List<Map<String, Any>> ?: emptyList()
            exercises.count { ex -> (ex["status"] == "completed" || ex["status"] == "partial") }
        }

        val userStatsRef = db.collection("UserStats").document(uid)
        val userStats = userStatsRef.get().await()

        if (userStats.exists()) {
            hardestGrade = userStats.getString("hardestGrade") ?: "V5"
            personalBest = userStats.getString("personalBest") ?: "Max Fingerboard Hang: +20kg for 7s"
            goalGrade = userStats.getString("goalGrade") ?: "V6"

            val storedProgress = (userStats.getLong("goalProgressSessions") ?: 0).toInt()
            goalProgressSessions = maxOf(storedProgress, totalSessions)

            if (totalSessions > storedProgress) {
                userStatsRef.update(
                    mapOf(
                        "goalProgressSessions" to totalSessions
                    )
                )
            }
        } else {
            val survey = db.collection("SurveyAnswers").document(uid).get().await()
            val level = survey.getString("level") ?: ""

            val estimatedHardestGrade = when {
                level.contains("Beginner", true) -> "V2"
                level.contains("Intermediate", true) -> "V5"
                level.contains("Advanced", true) -> "V6"
                else -> "V2"
            }

            userStatsRef.set(
                mapOf(
                    "uid" to uid,
                    "hardestGrade" to estimatedHardestGrade,
                    "personalBest" to "No data yet",
                    "goalGrade" to "V${estimatedHardestGrade.drop(1).toInt() + 1}",
                    "goalProgressSessions" to totalSessions,
                    "goalTargetSessions" to goalTargetSessions
                )
            )

            hardestGrade = estimatedHardestGrade
            personalBest = "No data yet"
            goalGrade = "V${estimatedHardestGrade.drop(1).toInt() + 1}"
            goalProgressSessions = totalSessions
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5F5))
    ) {
        item {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Back", color = Color.White)
            }
        }

        item {
            StatsCard(hardestGrade, totalExercises, totalSessions) {
                editField = "hardestGrade"
                showEditDialog = true
            }
        }

        item {
            SectionHeader("Progress Charts")
            ChartPlaceholder()
        }

        item {
            SectionHeader("Personal Bests")
            PersonalBestCard(personalBest) {
                editField = "personalBest"
                showEditDialog = true
            }
        }

        item {
            SectionHeader("Weekly Summary")
            WeeklySummaryCard(thisWeekExercises)
        }

        item {
            SectionHeader("Goal Progress")
            GoalProgressCard(goalGrade, goalProgressSessions, goalTargetSessions) {
                editField = "goalGrade"
                showEditDialog = true
            }
        }
    }

    if (showEditDialog) {
        EditDialog(
            field = editField,
            onDismiss = { showEditDialog = false },
            onSave = { newValue ->
                showEditDialog = false
                val updates = mutableMapOf<String, Any>()

                when (editField) {
                    "hardestGrade" -> {
                        hardestGrade = newValue
                        updates["hardestGrade"] = newValue
                    }
                    "personalBest" -> {
                        personalBest = newValue
                        updates["personalBest"] = newValue
                    }
                    "goalGrade" -> {
                        goalGrade = newValue
                        updates["goalGrade"] = newValue
                    }
                }

                updates["uid"] = uid
                updates["goalProgressSessions"] = goalProgressSessions
                updates["goalTargetSessions"] = goalTargetSessions

                db.collection("UserStats").document(uid).set(updates)
            }
        )
    }
}

@Composable
fun StatsCard(hardestGrade: String, totalExercises: Int, totalSessions: Int, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Climbing Stats", fontSize = 24.sp, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.clickable { onEdit() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Exercises: $totalExercises", color = Color.White)
            Text("Hardest Grade: $hardestGrade", color = Color.White)
            Text("Sessions Completed: $totalSessions", color = Color.White)
        }
    }
}

@Composable
fun PersonalBestCard(personalBest: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Personal Bests", fontSize = 20.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.clickable { onEdit() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(personalBest)
        }
    }
}

@Composable
fun WeeklySummaryCard(thisWeekExercises: Int) {
    val totalMinutes = thisWeekExercises * 15
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Exercises Done: $thisWeekExercises")
            Text("Total Training Time: ${totalMinutes} mins (${hours}h ${minutes}m)")
        }
    }
}

@Composable
fun GoalProgressCard(goalGrade: String, progressSessions: Int, targetSessions: Int, onEdit: () -> Unit) {
    val progress = progressSessions.toFloat() / targetSessions
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Goal: Climb $goalGrade", fontSize = 20.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.clickable { onEdit() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Progress: ${(progress * 100).toInt()}%")
        }
    }
}


/*
Part of the code in this screen (specifically related to setting up and displaying charts)
is adapted from the MPAndroidChart library by PhilJay (https://github.com/PhilJay/MPAndroidChart).

I have modified and integrated the code into my own composables to suit the needs of my project.

This code is used under the terms of the Apache License, Version 2.0.

Copyright [2025] [PhilJay]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

@Composable
fun ChartPlaceholder() {
    LocalContext.current
    var showFullScreen by remember { mutableStateOf(false) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val db = FirebaseFirestore.getInstance()
    var sessionsOverTime by remember { mutableStateOf<List<Date>>(emptyList()) }

    LaunchedEffect(uid) {
        val progressDocs = db.collection("Progress")
            .whereEqualTo("uid", uid)
            .get()
            .await()

        val sessionDates = progressDocs.documents.mapNotNull { doc ->
            doc.getTimestamp("timestamp")?.toDate()
        }.sorted()

        sessionsOverTime = sessionDates
    }

    if (showFullScreen) {
        FullScreenChartSessions(sessionsOverTime) { showFullScreen = false }
    }

    if (sessionsOverTime.size <= 1) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Not enough data to display chart yet.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clickable { showFullScreen = true }
                .padding(8.dp),
            factory = { context ->
                com.github.mikephil.charting.charts.LineChart(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setTouchEnabled(true)
                    setPinchZoom(true)
                    description.isEnabled = false
                    setDrawGridBackground(false)
                }
            },
            update = { chart ->
                val entries = mutableListOf<com.github.mikephil.charting.data.Entry>()

                sessionsOverTime.forEachIndexed { index, date ->
                    entries.add(com.github.mikephil.charting.data.Entry(index.toFloat(), 1f))
                }

                val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Sessions Over Time").apply {
                    color = android.graphics.Color.parseColor("#D32F2F")
                    valueTextColor = android.graphics.Color.BLACK
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawFilled(true)
                    mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
                    fillColor = android.graphics.Color.parseColor("#D32F2F")
                    fillAlpha = 100
                }

                val lineData = com.github.mikephil.charting.data.LineData(dataSet)
                chart.data = lineData
                chart.invalidate()
            }
        )
    }
}

@Composable
fun FullScreenChartSessions(sessionsOverTime: List<Date>, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = { context ->
                    com.github.mikephil.charting.charts.LineChart(context).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setTouchEnabled(true)
                        setPinchZoom(true)
                        description.isEnabled = false
                        setDrawGridBackground(false)
                    }
                },
                update = { chart ->
                    val entries = mutableListOf<com.github.mikephil.charting.data.Entry>()

                    sessionsOverTime.forEachIndexed { index, date ->
                        entries.add(com.github.mikephil.charting.data.Entry(index.toFloat(), 1f))
                    }

                    val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Sessions Over Time").apply {
                        color = android.graphics.Color.parseColor("#D32F2F")
                        valueTextColor = android.graphics.Color.BLACK
                        lineWidth = 3f
                        setDrawCircles(false)
                        setDrawFilled(true)
                        mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
                        fillColor = android.graphics.Color.parseColor("#D32F2F")
                        fillAlpha = 120
                    }

                    val lineData = com.github.mikephil.charting.data.LineData(dataSet)
                    chart.data = lineData
                    chart.invalidate()
                }
            )
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 20.sp, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun EditDialog(field: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        },
        title = { Text("Edit $field") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Enter new value") }
            )
        }
    )
}
