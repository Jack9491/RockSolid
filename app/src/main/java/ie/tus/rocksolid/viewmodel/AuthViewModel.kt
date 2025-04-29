package ie.tus.rocksolid.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

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
                    val userId = firebaseAuth.currentUser?.uid ?: "1000"
                    val userData = mapOf(
                        "email" to email,
                        "name" to name,
                        "uid" to userId,
                        "isFirstTime" to true
                    )

                    firestore.collection("Users").document(userId)
                        .set(userData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError("Failed to add user to Firestore: ${it.message}") }

                } else {
                    onError(task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Reset failed")
                }
            }
    }

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
