package ie.tus.rocksolid.modelScripts

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object IntermediateAdvancedTutorialInjector {

    private val tutorialMap = mapOf(
        "\"Minimum Edge\" Hangs" to "Perform the exercise '\"Minimum Edge\" Hangs' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Aerobic Circuit Training" to "Perform the exercise 'Aerobic Circuit Training' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Antagonist Strength Band Extensions" to "Perform the exercise 'Antagonist Strength Band Extensions' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Archer Push-Ups" to "Perform the exercise 'Archer Push-Ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Barbell Deadlift" to "Perform the exercise 'Barbell Deadlift' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Barbell Squat" to "Perform the exercise 'Barbell Squat' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Boulder Campusing (Monkey Biz)" to "Perform the exercise 'Boulder Campusing (Monkey Biz)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Bulgarian Split Squats" to "Perform the exercise 'Bulgarian Split Squats' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "CB Double Dynos (Large Rungs)" to "Perform the exercise 'CB Double Dynos (Large Rungs)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "CB Laddering (no skips, smallish rungs)" to "Perform the exercise 'CB Laddering (no skips, smallish rungs)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "CB “Laddering” (larger rungs w/ skips)" to "Perform the exercise 'CB “Laddering” (larger rungs w/ skips)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Campus Board \"Switch Hands\"" to "Perform the exercise 'Campus Board \"Switch Hands\"' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Campus Board Double Dynos (small rungs)" to "Perform the exercise 'Campus Board Double Dynos (small rungs)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Campus Ladder Laps on Big Holds" to "Perform the exercise 'Campus Ladder Laps on Big Holds' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Clap Pull-Ups" to "Perform the exercise 'Clap Pull-Ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Dips" to "Perform the exercise 'Dips' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Dumbbell Snatch" to "Perform the exercise 'Dumbbell Snatch' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Dynamic Dead Hangs" to "Perform the exercise 'Dynamic Dead Hangs' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Explosive Plyometric Push-Ups" to "Perform the exercise 'Explosive Plyometric Push-Ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Explosive Step-Ups" to "Perform the exercise 'Explosive Step-Ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Fingerboard Moving Hangs" to "Perform the exercise 'Fingerboard Moving Hangs' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Frenchies" to "Perform the exercise 'Frenchies' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Front Lever" to "Perform the exercise 'Front Lever' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Front Lever Raises" to "Perform the exercise 'Front Lever Raises' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Gym Rope Climbing" to "Perform the exercise 'Gym Rope Climbing' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Gym Rope Interval" to "Perform the exercise 'Gym Rope Interval' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "HIT System Pinch Hold Double Dynos" to "Perform the exercise 'HIT System Pinch Hold Double Dynos' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "HIT System—Max-Strength Protocol" to "Perform the exercise 'HIT System—Max-Strength Protocol' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "HIT System—S/P-Endurance Protocol" to "Perform the exercise 'HIT System—S/P-Endurance Protocol' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Hanging Windshield Wipers" to "Perform the exercise 'Hanging Windshield Wipers' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Heavy Finger Rolls" to "Perform the exercise 'Heavy Finger Rolls' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Hypergravity Bouldering" to "Perform the exercise 'Hypergravity Bouldering' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Interval Training" to "Perform the exercise 'Interval Training' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "L-Sit Holds" to "Perform the exercise 'L-Sit Holds' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Long-Duration Fingerboard Repeaters" to "Perform the exercise 'Long-Duration Fingerboard Repeaters' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Max-Weight Hangs \"10-Second\"" to "Perform the exercise 'Max-Weight Hangs \"10-Second\"' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Max-Weight Hangs \"7-53\" Protocol" to "Perform the exercise 'Max-Weight Hangs \"7-53\" Protocol' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "One-Arm Lock-Offs" to "Perform the exercise 'One-Arm Lock-Offs' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "One-Arm Pull-ups" to "Perform the exercise 'One-Arm Pull-ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Pinch Ball Hangs" to "Perform the exercise 'Pinch Ball Hangs' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Power Pull-Up (Chest-Bump)" to "Perform the exercise 'Power Pull-Up (Chest-Bump)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Power Pull-Up Intervals (Chest-Bump)" to "Perform the exercise 'Power Pull-Up Intervals (Chest-Bump)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Pronator Rotations" to "Perform the exercise 'Pronator Rotations' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Reverse Mountain Climber Plank" to "Perform the exercise 'Reverse Mountain Climber Plank' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Reverse Wrist Curls" to "Perform the exercise 'Reverse Wrist Curls' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Roof Lever-Ups" to "Perform the exercise 'Roof Lever-Ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Scapular Pull-Up" to "Perform the exercise 'Scapular Pull-Up' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Shoulder Stability Press" to "Perform the exercise 'Shoulder Stability Press' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Side Hip Raises" to "Perform the exercise 'Side Hip Raises' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Sling Trainer Pec Flys" to "Perform the exercise 'Sling Trainer Pec Flys' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Small-Rung Ladder Laps (No Skips)" to "Perform the exercise 'Small-Rung Ladder Laps (No Skips)' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Square Dance" to "Perform the exercise 'Square Dance' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Steep Wall Cut & Catch" to "Perform the exercise 'Steep Wall Cut & Catch' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "System Wall \"Isolation\"" to "Perform the exercise 'System Wall \"Isolation\"' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "System Wall Repeaters" to "Perform the exercise 'System Wall Repeaters' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Threshold Intervals" to "Perform the exercise 'Threshold Intervals' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "TRX “Marine Core”" to "Perform the exercise 'TRX “Marine Core”' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Typewriters" to "Perform the exercise 'Typewriters' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Uneven-Grip Pull-Ups" to "Perform the exercise 'Uneven-Grip Pull-Ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Weighted Muscle-Ups" to "Perform the exercise 'Weighted Muscle-Ups' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Windshield Wipers" to "Perform the exercise 'Windshield Wipers' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Wrist Extension Isometric" to "Perform the exercise 'Wrist Extension Isometric' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level.",
        "Wide Pinch w/ Wrist Ext." to "Perform the exercise 'Wide Pinch w/ Wrist Ext.' with controlled form focusing on proper technique. Equipment: See standard setup. Alternative: Modify based on equipment availability or fitness level."
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
                            if (!doc.contains("tutorial")) {
                                db.collection("Exercises")
                                    .document(doc.id)
                                    .update("tutorial", tutorialText)
                                    .addOnSuccessListener {
                                        Log.d("TUTORIAL_INJECT", "Tutorial added to: $exerciseName")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("TUTORIAL_INJECT", "Failed to update $exerciseName", e)
                                    }
                            } else {
                                Log.d("TUTORIAL_INJECT", "Tutorial already exists for: $exerciseName")
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
