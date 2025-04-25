package ie.tus.rocksolid.screens

import android.content.Context
import android.media.MediaPlayer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ie.tus.rocksolid.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// Achievement data class

data class Achievement(
    val imageRes: Int,
    val badgeRes: Int,
    val message: String,
    val currentSessions: Int,
    val nextGoal: Int
)

@Composable
fun AchievementScreen(navController: NavController, achievementIndex: Int) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    var totalSessions by remember { mutableIntStateOf(0) }
    var showBadge by remember { mutableStateOf(false) }
    var currentMilestoneIndex by remember { mutableIntStateOf(-1) }
    var selectedAchievement by remember { mutableStateOf<Pair<Achievement, Boolean>?>(null) }

    val animatedSessions = remember { mutableIntStateOf(0) }
    val animatedNextGoal = remember { mutableIntStateOf(1) }

    // Fetch sessions from Firestore
    LaunchedEffect(Unit) {
        val progressDocs = db.collection("Progress")
            .whereEqualTo("uid", uid)
            .get()
            .await()

        totalSessions = progressDocs.size()

        val milestoneIndex = when {
            totalSessions >= 50 -> 2
            totalSessions >= 10 -> 1
            totalSessions >= 1 -> 0
            else -> -1
        }

        if (milestoneIndex != -1) {
            currentMilestoneIndex = milestoneIndex
            if (totalSessions == listOf(1, 10, 50)[milestoneIndex]) {
                showBadge = true
                playAchievementSound(context)
            }
        }

        // Animate progress bar forward
        val currentGoal = listOf(1, 10, 50).getOrNull(milestoneIndex.coerceAtLeast(0)) ?: 1
        val nextGoal = when {
            totalSessions < 1 -> 1
            totalSessions in 1..9 -> 10
            totalSessions in 10..49 -> 50
            else -> 50
        }

        animatedSessions.intValue = totalSessions
        animatedNextGoal.intValue = currentGoal

        if (totalSessions >= currentGoal && totalSessions < nextGoal) {
            delay(1500L)
            animatedNextGoal.intValue = nextGoal
        }
    }

    val achievements = listOf(
        Achievement(R.drawable.achievement1, R.drawable.badge1, "First Session Completed!", totalSessions, 1),
        Achievement(R.drawable.achievement2, R.drawable.badge2, "10th Session Completed!", totalSessions, 10),
        Achievement(R.drawable.achievement3, R.drawable.badge3, "50th Session Completed!", totalSessions, 50)
    )

    val validIndex = currentMilestoneIndex.coerceIn(0, achievements.lastIndex)
    val achievement = achievements.getOrNull(validIndex)
    val progress = animatedSessions.intValue.toFloat() / animatedNextGoal.intValue.toFloat()

    if (showBadge && currentMilestoneIndex != -1 && totalSessions >= achievements[currentMilestoneIndex].nextGoal) {
        BadgeDialog(achievements[currentMilestoneIndex].message) {
            showBadge = false
        }
    }

    if (achievement != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            ) {
                Text("Back", color = Color.White)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(
                            id = if (totalSessions == 0) R.drawable.achievement0 else achievement.imageRes
                        ),
                        contentDescription = "Achievement Image",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(250.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = Color(0xFFD32F2F),
                        trackColor = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${animatedSessions.intValue} of ${animatedNextGoal.intValue} sessions completed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(
                            id = if (totalSessions == 0) R.drawable.badge0 else achievement.badgeRes
                        ),
                        contentDescription = "Badge Board",
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(150.dp)
                    )
                }
            }

            Text(
                text = "Your Other Achievements",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                achievements.forEachIndexed { index, item ->
                    val isUnlocked = totalSessions >= item.nextGoal
                    val badgeRes = when (index) {
                        0 -> if (isUnlocked) R.drawable.single_unlocked_badge_1 else R.drawable.single_locked_badge_1
                        1 -> if (isUnlocked) R.drawable.single_unlocked_badge_2 else R.drawable.single_locked_badge_2
                        2 -> if (isUnlocked) R.drawable.single_unlocked_badge_3 else R.drawable.single_locked_badge_3
                        else -> R.drawable.single_locked_badge_1
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedAchievement = item to isUnlocked },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) Color(0xFFFFF176) else Color.LightGray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = badgeRes),
                                contentDescription = "Badge $index",
                                modifier = Modifier
                                    .size(140.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = item.message,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            selectedAchievement?.let { (ach, unlocked) ->
                val badgeRes = when (achievements.indexOf(ach)) {
                    0 -> if (unlocked) R.drawable.single_unlocked_badge_1 else R.drawable.single_locked_badge_1
                    1 -> if (unlocked) R.drawable.single_unlocked_badge_2 else R.drawable.single_locked_badge_2
                    2 -> if (unlocked) R.drawable.single_unlocked_badge_3 else R.drawable.single_locked_badge_3
                    else -> R.drawable.single_locked_badge_1
                }

                val dialogTitle = if (unlocked) "Unlocked Achievement" else "Locked Achievement"
                val badgeTitle = ach.message
                val dialogMessage = if (unlocked) {
                    when (achievements.indexOf(ach)) {
                        0 -> "You earned this badge by completing your first training session!"
                        1 -> "You earned this badge by completing 10 training sessions!"
                        2 -> "You earned this badge by completing 50 training sessions!"
                        else -> "Achievement unlocked."
                    }
                } else {
                    "Complete ${ach.nextGoal} sessions to earn this badge."
                }

                AchievementDetailDialog(
                    imageRes = badgeRes,
                    title = dialogTitle,
                    badgeName = badgeTitle,
                    message = dialogMessage,
                    onDismiss = { selectedAchievement = null }
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
                .background(Color(0xFFFDD835)),
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
                        .background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83C\uDFC6",
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

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(text = "Ok")
                }
            }
        }
    }
}

@Composable
fun AchievementDetailDialog(
    imageRes: Int,
    title: String,
    badgeName: String,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Badge Image",
                    modifier = Modifier
                        .size(140.dp)
                        .padding(bottom = 12.dp)
                )

                Text(
                    text = badgeName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

fun playAchievementSound(context: Context) {
    val mediaPlayer = MediaPlayer.create(context, R.raw.achievement_sound)
    mediaPlayer.start()
    mediaPlayer.setOnCompletionListener {
        it.release()
    }
}
