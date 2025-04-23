package ie.tus.rocksolid.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.R
import ie.tus.rocksolid.viewmodel.AuthViewModel
import kotlinx.coroutines.tasks.await



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val uid = authViewModel.getCurrentUserUid()

    // User Fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }
    var profilePicRes by remember { mutableStateOf(R.drawable.ic_profile_pic) }

    // Fetch Firestore User Data
    LaunchedEffect(uid) {
        if (uid != null) {
            try {
                val doc = firestore.collection("Users").document(uid).get().await()
                name = doc.getString("name") ?: ""
                email = doc.getString("email") ?: ""
                //password = doc.getString("password") ?: ""
                height = doc.get("height")?.toString() ?: ""
                weight = doc.get("weight")?.toString() ?: ""
                // Simulated profilePic logic – real implementation would load from URL or storage
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        } else {
            Toast.makeText(context, "User ID is null", Toast.LENGTH_SHORT).show()
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Your Details", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Picture
                Image(
                    painter = painterResource(id = profilePicRes),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable {
                            Toast
                                .makeText(context, "Profile pic click — implement picker!", Toast.LENGTH_SHORT)
                                .show()
                            // You could launch image picker or navigation here
                        }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Form Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val textFieldModifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)

                        val whiteFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = textFieldModifier,
                            colors = whiteFieldColors
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = textFieldModifier,
                            colors = whiteFieldColors
                        )

//                        OutlinedTextField(
//                            value = password,
//                            onValueChange = { password = it },
//                            label = { Text("Password") },
//                            visualTransformation = PasswordVisualTransformation(),
//                            modifier = textFieldModifier,
//                            colors = whiteFieldColors
//                        )

                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            modifier = textFieldModifier,
                            colors = whiteFieldColors
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            modifier = textFieldModifier,
                            colors = whiteFieldColors
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (uid != null) {
                                    val updatedData = mapOf(
                                        "name" to name,
                                        "email" to email,
                                        //"password" to password,
                                        "height" to height.toIntOrNull(),
                                        "weight" to weight.toIntOrNull()
                                    )

                                    firestore.collection("Users").document(uid)
                                        .update(updatedData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Details updated!", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Update failed!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Save", color = Color(0xFFD32F2F))
                        }

                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
