package ie.tus.rocksolid.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import ie.tus.rocksolid.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    var isRead: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (userId != null) {
            val snapshot = firestore.collection("Notification")
                .document(userId)
                .collection("Items")
                .orderBy("sent_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            notifications = snapshot.documents.map { doc ->
                val title = doc.getString("title") ?: "No Title"
                val message = doc.getString("message") ?: "No Message"
                val sentAt = doc.getTimestamp("sent_at") ?: Timestamp.now()
                val isRead = doc.getBoolean("is_read") ?: false

                NotificationItem(
                    id = doc.id,
                    title = title,
                    message = message,
                    time = formatTimestamp(sentAt),
                    isRead = isRead
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "No Notifications",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No new notifications", fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notification.isRead) Color(0xFFF5F5F5) else Color(0xFFFFF3E0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    if (!notification.isRead && userId != null) {
                                        firestore.collection("Notification")
                                            .document(userId)
                                            .collection("Items")
                                            .document(notification.id)
                                            .update("is_read", true)

                                        notification.isRead = true
                                    }
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = notification.title,
                                fontSize = 18.sp,
                                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = notification.message, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = notification.time,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
