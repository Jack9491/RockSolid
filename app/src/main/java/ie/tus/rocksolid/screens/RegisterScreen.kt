package ie.tus.rocksolid.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import ie.tus.rocksolid.R

// RegisterScreen - Entry point for user registration with Firebase Authentication
@Composable
fun RegisterScreen(navController: NavHostController) {
    val context = LocalContext.current // Access current context for Toast messages
    val auth = remember { FirebaseAuth.getInstance() } // FirebaseAuth instance
    var isLoading by remember { mutableStateOf(false) } // Loading state for the register button

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Register screen content
        RegisterScreenContent(
            onRegisterSubmit = { email, password ->
                isLoading = true // Show loading state
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false // Stop loading
                        if (task.isSuccessful) {
                            // Registration successful
                            Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                            navController.navigate("loginScreen") // Navigate to LoginScreen
                        } else {
                            // Registration failed, display error
                            Toast.makeText(
                                context,
                                "Registration Failed: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            },
            isLoading = isLoading // Pass loading state to content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreenContent(
    onRegisterSubmit: (String, String) -> Unit,
    isLoading: Boolean
) {
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Name", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.Black
            )
        )

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email Address", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.Black
            )
        )

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.Black
            )
        )

        OutlinedTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Confirm Password", color = Color.Black) },
            textStyle = TextStyle(color = Color.Black),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.Black
            )
        )

        Button(
            onClick = {
                onRegisterSubmit(email.value, password.value)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Red
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Sign Up",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}
