package ie.tus.rocksolid.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import ie.tus.rocksolid.R
import ie.tus.rocksolid.viewmodel.AuthViewModel
import ie.tus.rocksolid.utils.ReminderManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val loginError = remember { mutableStateOf("") }

    val showResetDialog = remember { mutableStateOf(false) }
    val resetEmail = remember { mutableStateOf("") }
    val resetMessage = remember { mutableStateOf("") }

    val context = LocalContext.current // Needed for reminder scheduling

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAEAEA))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFD32F2F),
                focusedLabelColor = Color(0xFFD32F2F),
                cursorColor = Color(0xFFD32F2F)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFFD32F2F),
                focusedLabelColor = Color(0xFFD32F2F),
                cursorColor = Color(0xFFD32F2F)
            )
        )




        if (loginError.value.isNotBlank()) {
            Text(text = loginError.value, color = Color.Red, modifier = Modifier.padding(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                isLoading.value = true
                loginError.value = ""
                authViewModel.login(
                    email = email.value,
                    password = password.value,
                    onSuccess = {
                        // Schedule daily workout reminder
                        ReminderManager.scheduleDailyReminder(context)
                        onLoginSuccess()
                    },
                    onError = { error ->
                        isLoading.value = false
                        loginError.value = error
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading.value,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }

        TextButton(
            onClick = { showResetDialog.value = true },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Forgot Password?", color = Color.Gray)
        }

        if (showResetDialog.value) {
            AlertDialog(
                onDismissRequest = { showResetDialog.value = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (resetEmail.value.isBlank()) {
                                resetMessage.value = "Email cannot be empty"
                                return@TextButton
                            }
                            authViewModel.sendPasswordReset(
                                resetEmail.value,
                                onSuccess = {
                                    resetMessage.value = "Reset email sent successfully!"
                                },
                                onError = {
                                    resetMessage.value = "Failed: $it"
                                }
                            )
                        }
                    ) {
                        Text("Send Reset Email")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog.value = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Reset Password") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = resetEmail.value,
                            onValueChange = { resetEmail.value = it },
                            label = { Text("Enter your email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (resetMessage.value.isNotBlank()) {
                            Text(
                                text = resetMessage.value,
                                color = if (resetMessage.value.startsWith("Reset")) Color.Green else Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}
