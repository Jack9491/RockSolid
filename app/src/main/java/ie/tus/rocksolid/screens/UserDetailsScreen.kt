package ie.tus.rocksolid.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import ie.tus.rocksolid.R
import ie.tus.rocksolid.viewmodel.AuthViewModel
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.AlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val uid = authViewModel.getCurrentUserUid()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var profilePicUrl by remember { mutableStateOf<String?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    LaunchedEffect(uid) {
        if (uid != null) {
            try {
                val doc = firestore.collection("Users").document(uid).get().await()
                name = doc.getString("name") ?: ""
                email = doc.getString("email") ?: ""
                height = doc.get("height")?.toString() ?: ""
                weight = doc.get("weight")?.toString() ?: ""
                profilePicUrl = doc.getString("profilePictureUrl")
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
                Text("Edit Your Details", fontSize = 22.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(16.dp))

                Image(
                    painter = rememberAsyncImagePainter(model = selectedImageUri ?: profilePicUrl ?: R.drawable.ic_profile_placeholder),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { showImageDialog = true }
                )

                if (showImageDialog) {
                    AlertDialog(
                        onDismissRequest = { showImageDialog = false },
                        confirmButton = {},
                        title = { Text("Choose Image Source") },
                        text = {
                            Column {
                                Text(
                                    "Take Photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showImageDialog = false
                                            ImagePicker.with(context as androidx.activity.ComponentActivity)
                                                .cameraOnly()
                                                .cropSquare()
                                                .createIntent { launcher.launch(it) }
                                        }
                                        .padding(12.dp)
                                )
                                Text(
                                    "Choose from Gallery",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showImageDialog = false
                                            ImagePicker.with(context as androidx.activity.ComponentActivity)
                                                .galleryOnly()
                                                .cropSquare()
                                                .createIntent { launcher.launch(it) }
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val fieldModifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)

                        val whiteFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color.White
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = fieldModifier,
                            colors = whiteFieldColors
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = fieldModifier,
                            colors = whiteFieldColors
                        )

                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            modifier = fieldModifier,
                            colors = whiteFieldColors
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            modifier = fieldModifier,
                            colors = whiteFieldColors
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (uid != null) {
                                    val updatedData = mapOf(
                                        "name" to name,
                                        "email" to email,
                                        "height" to height.toIntOrNull(),
                                        "weight" to weight.toIntOrNull()
                                    ).toMutableMap()

                                    if (selectedImageUri != null) {
                                        val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                        if (inputStream != null) {
                                            val storageRef = FirebaseStorage.getInstance().reference
                                            val imageRef = storageRef.child("profilePictures/${uid}.jpg")

                                            imageRef.putStream(inputStream)
                                                .addOnSuccessListener {
                                                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                                                        updatedData["profilePictureUrl"] = uri.toString()
                                                        firestore.collection("Users").document(uid)
                                                            .update(updatedData as Map<String, Any>)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(context, "Details updated!", Toast.LENGTH_SHORT).show()
                                                                navController.popBackStack()
                                                            }
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            Toast.makeText(context, "Failed to open image stream", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        firestore.collection("Users").document(uid)
                                            .update(updatedData as Map<String, Any>)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Details updated!", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            }
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
