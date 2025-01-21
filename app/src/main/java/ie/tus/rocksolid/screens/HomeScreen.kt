package ie.tus.rocksolid.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Surface(
        modifier = Modifier.fillMaxSize().background(Color(0xFFEAEAEA)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome, ${user?.email ?: "User"}!",
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("loginScreen")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                )
            ) {
                Text("Logout", fontSize = 16.sp)
            }
        }
    }
}
