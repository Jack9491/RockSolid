package ie.tus.rocksolid.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    // Login function with callbacks for success and error
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "An unknown error occurred.")
                }
            }
    }

    // Register function with Firestore integration to store user details
    fun register(
        email: String,
        password: String,
        name: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TestingScreen",  "Entered is sucsefful")
                    val userId = firebaseAuth.currentUser?.uid ?: "1000"
                    val userData = mapOf(
                        "email" to email,
                        "name" to name,
                        "uid" to userId,
                        "isFirstTime" to true
                    )
                    //customerService.addCustomer(userData)

                    onSuccess()


//                    firestore.collection("Users").document(userId)
//                        .set(userData)
//                        .addOnSuccessListener {
//                            onSuccess()
//                        }
//                        .addOnFailureListener {
//                            onError("Failed to add user to Firestore: ${it.message}")
//                        }
                } else {
                    Log.d("TestingScreen",  "Lol")
                    onError(task.exception?.message ?: "Registration failed")
                }
            }
    }

    // Get the current user's UID (if logged in)
    fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun markSurveyComplete(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Users").document(userId)
            .update("isFirstTime", false)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onError(error.message ?: "Failed to update Firestore") }
    }

}
