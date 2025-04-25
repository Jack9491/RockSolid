package ie.tus.rocksolid.modelScripts

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BeginnerExerciseTutorialInjector {

    private val tutorialMap = mapOf(
        "Band Finger Extension" to "Loop a resistance band around your fingers with your hand in a fist. Spread your fingers against the resistance of the band. Hold briefly, then return. Equipment: Resistance band. Alternative: Use a hair tie or rubber band.",
        "Reverse Wrist Curls" to "Sit and rest your forearm on your thigh, palm facing down, holding a light dumbbell. Curl the wrist upward, then lower slowly. Equipment: Dumbbell. Alternative: Use a can or water bottle.",
        "Pronators" to "Hold a hammer or similar object with one hand, elbow at 90°. Rotate your wrist inward against the weight. Equipment: Hammer or dumbbell. Alternative: Use a resistance band instead.",
        "Reverse Arm Curl" to "Stand or sit with arms at your sides holding dumbbells, palms facing down. Curl up, focusing on forearms. Equipment: Dumbbells. Alternative: Resistance band or water bottles.",
        "DB Internal Rotation" to "Lie on your side with a light dumbbell in the top hand. Bend elbow 90°, rotate arm inward. Equipment: Dumbbell. Alternative: Resistance band anchored at side.",
        "DB External Rotation" to "Lie on your side with a dumbbell in the top hand. Bend elbow 90°, rotate arm upward. Equipment: Dumbbell. Alternative: Resistance band anchored in front.",
        "Sling Trainer “Ts”" to "Lean back holding a sling trainer, arms straight. Pull arms out to a 'T' shape while keeping body straight. Equipment: Sling trainer. Alternative: Resistance band pull-aparts.",
        "Sling Trainer “Ys”" to "Lean back holding sling trainer, arms straight. Pull arms into a 'Y' shape above head. Equipment: Sling trainer. Alternative: Light dumbbell shoulder raises.",
        "Shrugs" to "Hold dumbbells at your sides, lift shoulders toward ears, squeeze, then lower. Equipment: Dumbbells. Alternative: Backpack with weight.",
        "Scapular Push-Up" to "In plank position, keep arms straight and pinch shoulder blades together, then push away. Equipment: None. Alternative: Do on knees if needed.",
        "Scapular Pull-Up" to "Hang from a bar and pull shoulders down without bending elbows. Equipment: Pull-up bar. Alternative: Band-assisted scapular activation.",
        "Shoulder Press" to "Press dumbbells overhead while keeping core tight. Equipment: Dumbbells. Alternative: Resistance bands anchored under feet.",
        "Bench Press/Push-Up" to "Do bench press with weights or perform push-ups on the floor. Equipment: Barbell/bench or bodyweight. Alternative: Knee push-ups.",
        "Tricep Pushdown" to "Push a resistance band or cable down, keeping elbows tucked. Equipment: Cable machine or resistance band. Alternative: Tricep dips.",
        "Feet-Up Crunches" to "Lie down, feet on bench, knees 90°. Cross arms and crunch upward. Equipment: Bench or chair. Alternative: Crunches with feet on floor.",
        "Hanging Knee Lifts" to "Hang from bar and raise knees towards chest. Equipment: Pull-up bar. Alternative: Lying knee tucks.",
        "Mountain Climber Plank" to "In plank, alternate knees to chest rapidly. Equipment: None. Alternative: Slower pace or elevate hands.",
        "1-Arm Elbow & Side Plank" to "Do a side plank on one elbow, stack feet or place one in front. Hold position. Equipment: None. Alternative: Do with knees down.",
        "Superman" to "Lie face down and lift arms and legs off ground simultaneously. Equipment: None. Alternative: Lift arms and legs alternately.",
        "Reverse Plank" to "Sit, hands behind hips, lift hips to make straight line from head to heels. Hold. Equipment: None. Alternative: Bent knees for easier version.",
        "Sumo Deadlift" to "Stand wide, hold weight, and lift while keeping back straight. Equipment: Barbell/dumbbell. Alternative: Bodyweight sumo squats.",
        "Steep Wall Traversing" to "Climb sideways along a steep wall, keeping low and controlled. Equipment: Climbing wall. Alternative: Traverse a lower angle wall.",
        "Limit Bouldering" to "Try hard problems near your limit, focus on max effort. Equipment: Bouldering gym. Alternative: Home wall with hard moves.",
        "Wide Pinch w/ Wrist Extension" to "Pinch wide block or weights with wrist extended. Hold. Equipment: Pinch blocks/weights. Alternative: DIY pinch block with books.",
        "Weighted Pull-Ups" to "Perform pull-ups with extra weight using belt or vest. Equipment: Pull-up bar, weight. Alternative: Slow tempo bodyweight pull-ups.",
        "Lat Pulldown" to "Pull bar to chest on a machine, elbows down and in. Equipment: Lat pulldown machine. Alternative: Band pulldowns.",
        "One-Arm Traversing" to "Traverse wall using mostly one arm at a time. Equipment: Climbing wall. Alternative: Weighted normal traversing.",
        "One-Arm Lunging" to "Climb or simulate lunging with one arm dynamically. Equipment: Wall or hangboard. Alternative: Do with both hands, focus on control.",
        "Big-Move Boulder Problems" to "Select problems with long, explosive moves. Focus on commitment. Equipment: Bouldering wall. Alternative: Dynamic moves on home board.",
        "Short-Duration Fingerboard Repeaters" to "Hang for short bursts with rest between. Equipment: Fingerboard. Alternative: Door frame edges (be careful).",
        "Bouldering 4x4s" to "Climb 4 problems back to back, rest, repeat 4 rounds. Equipment: Bouldering gym. Alternative: Repeat home wall circuits.",
        "Pull-Up Intervals" to "Do timed sets of pull-ups with short rests. Equipment: Pull-up bar. Alternative: Rows or band pull-downs.",
        "Big-Holds, Big-Move 4x4s" to "Do 4x4s on big holds with dynamic movement. Equipment: Bouldering wall. Alternative: Jug circuits on home wall.",
        "Route Intervals" to "Climb routes with timed rests to build endurance. Equipment: Climbing gym. Alternative: Traverse circuits if no rope wall.",
        "ARC Traversing" to "Traverse continuously at very easy level for 20+ minutes. Equipment: Wall with long traverse. Alternative: Foot-on campus laddering.",
        "Submaximal Route Intervals" to "Climb moderate routes for 3 minutes, rest, repeat. Equipment: Climbing routes. Alternative: Circuit climbing.",
        "Climb All Day" to "Pick a variety of routes or boulders and try to climb for several hours with pacing. Equipment: Gym or outdoor area. Alternative: Extended home session with breaks.",
        "Steady State Aerobic Training" to "Traverse or climb at easy level for 20 minutes straight. Equipment: Wall. Alternative: Stair climbing or step-ups if no wall."
    )

    fun inject() {
        val db = FirebaseFirestore.getInstance()

        tutorialMap.forEach { (exerciseName, tutorialText) ->
            db.collection("Exercises")
                .whereEqualTo("name", exerciseName)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.w("TUTORIAL_INJECT", "No match found for exercise: $exerciseName")
                    } else {
                        documents.forEach { doc ->
                            db.collection("Exercises")
                                .document(doc.id)
                                .update("tutorial", tutorialText)
                                .addOnSuccessListener {
                                    Log.d("TUTORIAL_INJECT", "Tutorial added to: $exerciseName")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("TUTORIAL_INJECT", "Failed to update $exerciseName", e)
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TUTORIAL_INJECT", "Error fetching exercise: $exerciseName", e)
                }
        }
    }
}
