package ie.tus.rocksolid.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import ie.tus.rocksolid.R

data class Achievement(
    val imageRes: Int,
    val badgeRes: Int,
    val message: String,
    val currentSessions: Int,
    val nextGoal: Int
)

@Composable
fun AchievementScreen(navController: NavController, achievementIndex: Int) {
    val achievements = listOf(
        Achievement(R.drawable.achievement1, R.drawable.badge1, "First Session Completed!", 1, 10),
        Achievement(R.drawable.achievement2, R.drawable.badge2, "10th Session Completed!", 10, 50),
        Achievement(R.drawable.achievement3, R.drawable.badge3, "50th Session Completed!", 50, 100)
    )

    val validIndex = achievementIndex.coerceIn(0, achievements.lastIndex)
    var showBadge by remember { mutableStateOf(true) }

    val achievement = achievements[validIndex]
    val sessionsLeft = achievement.nextGoal - achievement.currentSessions
    val progress = achievement.currentSessions.toFloat() / achievement.nextGoal.toFloat()

    if (showBadge) {
        BadgeDialog(achievement.message) { showBadge = false }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {

            // Back Button
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Back", color = Color.White)
                }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Mountain Image
                Image(
                    painter = painterResource(id = achievement.imageRes),
                    contentDescription = "Achievement ${validIndex + 1}",
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(350.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar with Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                ) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = Color(0xFFD32F2F),
                        trackColor = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${achievement.currentSessions} of ${achievement.nextGoal} sessions completed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Badge Image
                Image(
                    painter = painterResource(id = achievement.badgeRes),
                    contentDescription = "Badge for Achievement ${validIndex + 1}",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(350.dp)
                )
            }
        }
    }
}


@Composable
fun BadgeDialog(achievementText: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFDD835)), // Gold color for badge
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC107)), // Bright golden circle
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Achievement Unlocked!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = achievementText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))) {
                    Text(text = "Ok")
                }
            }
        }
    }
}
