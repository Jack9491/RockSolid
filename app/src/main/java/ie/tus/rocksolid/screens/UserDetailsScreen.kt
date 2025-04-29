package ie.tus.rocksolid.screens

import android.net.Uri
import android.util.Base64
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.R
import ie.tus.rocksolid.viewmodel.AuthViewModel
import kotlinx.coroutines.tasks.await
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

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
    var isSaving by remember { mutableStateOf(false) }

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
            Box(modifier = Modifier.fillMaxSize()) {
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
                                        isSaving = true
                                        val updatedData = mutableMapOf<String, Any>(
                                            "name" to name,
                                            "email" to email
                                        )

                                        height.toIntOrNull()?.let { updatedData["height"] = it }
                                        weight.toIntOrNull()?.let { updatedData["weight"] = it }

                                        if (selectedImageUri != null) {
                                            uploadToImgbb(
                                                context = context,
                                                imageUri = selectedImageUri!!,
                                                apiKey = "82ea6bf65cf3387f9e0747bb9ea612ce",
                                                onSuccess = { imageUrl ->
                                                    updatedData["profilePictureUrl"] = imageUrl
                                                    firestore.collection("Users").document(uid)
                                                        .update(updatedData)
                                                        .addOnSuccessListener {
                                                            isSaving = false
                                                            Toast.makeText(context, "Details updated!", Toast.LENGTH_SHORT).show()
                                                            navController.popBackStack()
                                                        }
                                                        .addOnFailureListener {
                                                            isSaving = false
                                                            Toast.makeText(context, "Update failed!", Toast.LENGTH_SHORT).show()
                                                        }
                                                },
                                                onError = { errorMsg ->
                                                    isSaving = false
                                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            firestore.collection("Users").document(uid)
                                                .update(updatedData)
                                                .addOnSuccessListener {
                                                    isSaving = false
                                                    Toast.makeText(context, "Details updated!", Toast.LENGTH_SHORT).show()
                                                    navController.popBackStack()
                                                }
                                                .addOnFailureListener {
                                                    isSaving = false
                                                    Toast.makeText(context, "Update failed!", Toast.LENGTH_SHORT).show()
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

                if (isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
}

// Helper: Convert Uri to Base64
fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Helper: Upload Base64 image to imgbb
fun uploadToImgbb(
    context: android.content.Context,
    imageUri: Uri,
    apiKey: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val base64Image = uriToBase64(context, imageUri)
    if (base64Image == null) {
        onError("Failed to encode image")
        return
    }

    val client = OkHttpClient()
    val requestBody = FormBody.Builder()
        .add("key", apiKey)
        .add("image", base64Image)
        .build()

    val request = Request.Builder()
        .url("https://api.imgbb.com/1/upload")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onError("Upload failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (!response.isSuccessful) {
                onError("Upload failed: ${response.message}")
                return
            }
            val json = JSONObject(response.body?.string() ?: "")
            val url = json.getJSONObject("data").getString("url")
            onSuccess(url)
        }
    })
}
