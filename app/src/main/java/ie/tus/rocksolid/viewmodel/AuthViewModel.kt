package ie.tus.rocksolid.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import java.lang.Error

class AuthViewModel(
    private val firebaseAuth: FirebaseAuth
) :  ViewModel(){

     fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
         firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                //isLoading = false
                if (task.isSuccessful) {
                    onSuccess()
                    //navController.navigate("homeScreen")
                } else {
                    onError("OOPY DAIY")
                }
            }
    }
}