package ie.tus.rocksolid.modelScripts

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

object TrainingPlanGenerator {

    private val warmUp = mapOf(
        "name" to "Warm-up: Mobility Drills",
        "type" to "warmup"
    )

    private val coolDown = mapOf(
        "name" to "Cool-down: Stretching",
        "type" to "cooldown"
    )

    suspend fun generatePlanForCurrentWeek(onComplete: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.e("TRAINING_GEN", "User not logged in.")
            onComplete(false)
            return
        }

        val weekStart = getWeekStartDate(Date())
        Log.d("TRAINING_GEN", "Generating plan for UID=$uid, weekStart=$weekStart")

        try {
            val surveySnapshot = db.collection("SurveyAnswers").document(uid).get().await()
            if (!surveySnapshot.exists()) {
                Log.e("TRAINING_GEN", "No survey data found for user $uid")
                onComplete(false)
                return
            }

            val level = surveySnapshot.getString("experienceLevel") ?: "Beginner"
            val rawGoals = surveySnapshot.get("trainingGoals") as? List<String> ?: emptyList()
            val goals = if (rawGoals.isEmpty()) listOf("General fitness and health") else rawGoals

            Log.d("TRAINING_GEN", "Survey found. Level=$level, Goals=$goals")

            // Map survey values to tags
            val levelTag = when {
                level.contains("Beginner", ignoreCase = true) -> "Beginner"
                level.contains("Intermediate", ignoreCase = true) -> "Intermediate"
                level.contains("Advanced", ignoreCase = true) -> "Advanced"
                else -> "Beginner"
            }


            val goalTags = goals.flatMap {
                when (it.lowercase()) {
                    "build power and strength" -> listOf("Strength", "Power")
                    "enhance core strength and body tension" -> listOf("Core", "Stability")
                    "increase finger strength" -> listOf("Finger", "Grip")
                    "improve overall endurance" -> listOf("Endurance", "Aerobic")
                    "general fitness and health" -> listOf("Endurance", "Stability", "Core")
                    else -> emptyList()
                }
            }

            Log.d("TRAINING_GEN", "Mapped level=$levelTag, goalTags=$goalTags")

            // Filter matching exercises

            val exerciseSnapshots = db.collection("Exercises").get().await()
            val matchingExercises = exerciseSnapshots.documents.filter { doc ->
                val difficulty = doc.getString("difficulty") ?: ""
                val category = doc.getString("category") ?: ""

                difficulty.contains(levelTag, ignoreCase = true) &&
                        goalTags.any { tag -> category.contains(tag, ignoreCase = true) }
            }.map { doc ->
                mapOf(
                    "name" to (doc.getString("name") ?: "Unnamed Exercise"),
                    "type" to "workout",
                    "description" to (doc.getString("description") ?: ""),
                    "reps" to (doc.getString("reps") ?: ""),
                    "sets" to (doc.getString("sets") ?: "")
                )
            }

            Log.d("TRAINING_GEN", "Found ${matchingExercises.size} matching exercises.")

            if (matchingExercises.size < 3) {
                Log.e("TRAINING_GEN", "Not enough matching exercises. Need at least 3.")
                onComplete(false)
                return
            }

            // 🧠 Create weekly plan
            val days = mutableMapOf<String, List<Map<String, Any>>>()
            val dayKeys = listOf("monday", "tuesday", "wednesday", "thursday", "friday")

            for (day in dayKeys) {
                val selected = matchingExercises.shuffled().take(3)
                days[day] = listOf(warmUp) + selected + listOf(coolDown)
            }

            val plan = hashMapOf(
                "uid" to uid,
                "weekStart" to weekStart,
                "generatedAt" to Timestamp.now(),
                "days" to days
            )

            val docId = "${uid}_$weekStart"
            db.collection("TrainingPrograms").document(docId).set(plan).await()
            Log.d("TRAINING_GEN", "Plan written to Firestore.")

            onComplete(true)

        } catch (e: Exception) {
            Log.e("TRAINING_GEN", "Exception while generating plan", e)
            onComplete(false)
        }
    }

    private fun getWeekStartDate(date: Date): String {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }
}
