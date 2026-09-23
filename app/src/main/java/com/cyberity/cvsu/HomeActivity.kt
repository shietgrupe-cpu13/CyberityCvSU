package com.cyberity.cvsu


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector


// ---------------------------------------------------------------------------
// Data model for the Learn tab's module/lesson list
// ---------------------------------------------------------------------------

enum class LessonType { LESSON, PRACTICE }

data class LessonItem(
    val id: String,
    val type: LessonType,
    val title: String,
    val xp: Int,
    val locked: Boolean
)

data class ModuleData(
    val id: String,
    val title: String,
    val completed: Boolean,
    val items: List<LessonItem>
)

// TODO: replace with real data from Firestore/your backend once ready.
private fun sampleModules(): List<ModuleData> = listOf(
    ModuleData(
        id = "basics",
        title = "Security Basics",
        completed = true,
        items = emptyList()
    ),
    ModuleData(
        id = "network",
        title = "Network Fundamentals",
        completed = false,
        items = listOf(
            LessonItem("l1", LessonType.LESSON, "How Networks Communicate", 10, locked = false),
            LessonItem("p1", LessonType.PRACTICE, "Capture the Packet: CTF Challenge", 5, locked = true),
            LessonItem("l2", LessonType.LESSON, "Ports & Protocols", 10, locked = true),
            LessonItem("p2", LessonType.PRACTICE, "Scan the Target: CTF Challenge", 5, locked = true)
        )
    )
)

// ---------------------------------------------------------------------------
// HomeScreen: hosts the bottom nav and switches between the 3 tabs
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(
    isTutorialMode: Boolean = false,
    onFinishTutorial: () -> Unit = {},
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var levelRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTutorialMode) {
        if (isTutorialMode) {
            TutorialManager.startTutorial()
        } else {
            TutorialManager.finishTutorial()
        }
    }

    val currentStep = TutorialManager.currentStep
    val isTutorial = isTutorialMode && TutorialManager.isTutorialActive

    Scaffold(
        containerColor = AppNavy,
        bottomBar = {
            if (!levelRunning) {
                NavigationBar(containerColor = AppCard) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Learn") },
                        label = { Text("Learn") },
                        colors = navItemColors()
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Filled.Star, contentDescription = "Leaderboard") },
                        label = { Text("Leaderboard") },
                        colors = navItemColors()
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        colors = navItemColors()
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> LearnScreen(onLevelRunningChanged = { levelRunning = it })
                1 -> LeaderboardTab()
                2 -> ProfileTab(onLogout = onLogout)
            }

            if (isTutorial) {
                when (currentStep) {
                    TutorialStep.STEP1_HOME -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Start Your Journey",
                            description = "This is where you can access your cybersecurity lessons and challenges. Tap the highlighted level node to continue.",
                            stepNumber = 1,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP2_LEVEL_PREVIEW
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP2_LEVEL_PREVIEW -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Your First Level",
                            description = "Start here to learn the fundamentals of cybersecurity. Tap START LEVEL to continue.",
                            stepNumber = 2,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP3_SCENARIO
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP3_SCENARIO -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Read Carefully",
                            description = "CYBERITY presents realistic cybersecurity situations. Read the scenario carefully before choosing your answer.",
                            stepNumber = 3,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP4_CHOICE
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP4_CHOICE -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Choose Your Answer",
                            description = "Select the option you think is the safest response. Tap an answer to continue.",
                            stepNumber = 4,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP5_HINT
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP5_HINT -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Need Help?",
                            description = "You can use a hint when you're unsure. Tap Hint to see how assistance works.",
                            stepNumber = 5,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP6_SUBMIT
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP6_SUBMIT -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Submit Your Answer",
                            description = "Once you've chosen your answer, tap Submit to check your response.",
                            stepNumber = 6,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP7_FEEDBACK
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP7_FEEDBACK -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Learn From Your Result",
                            description = "CYBERITY gives you feedback after each challenge so you can understand why an answer is correct or incorrect.",
                            stepNumber = 7,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP8_PROGRESS
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP8_PROGRESS -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "Track Your Progress",
                            description = "You can monitor your completed levels, streak, and performance here.",
                            stepNumber = 8,
                            onTargetTapped = {
                                TutorialManager.currentStep = TutorialStep.STEP9_FINISH
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                    TutorialStep.STEP9_FINISH -> {
                        TutorialSpotlightOverlay(
                            targetBounds = TutorialManager.targetBounds,
                            title = "You're Ready!",
                            description = "You've learned how to navigate CYBERITY. Now you're ready to start your cybersecurity training.",
                            stepNumber = 9,
                            onTargetTapped = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            },
                            onSkipTutorial = {
                                TutorialManager.finishTutorial()
                                onFinishTutorial()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppCyan,
    selectedTextColor = AppCyan,
    unselectedIconColor = AppGray,
    unselectedTextColor = AppGray,
    indicatorColor = AppBlue.copy(alpha = 0.25f)
)

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    tint: Color
) {
    Surface(
        color = AppCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(value, color = AppWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ModuleHeaderRow(
    module: ModuleData,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = if (module.completed) Color(0xFF00C853) else AppCyan,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (module.completed) Icons.Filled.CheckCircle else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = AppNavy
            )
        }

        Text(
            text = module.title,
            color = AppWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        )

        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = AppGray
        )
    }
}

@Composable
private fun LessonCard(
    lesson: LessonItem,
    onLearnClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (lesson.type == LessonType.LESSON) Icons.Filled.Info else Icons.Filled.Edit,
                    contentDescription = null,
                    tint = if (lesson.type == LessonType.LESSON) AppCyan else AppBlue
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = if (lesson.type == LessonType.LESSON) "Lesson" else "Practice",
                        color = AppGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = lesson.title,
                        color = AppWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (lesson.locked) {
                    Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = AppGray)
                }
            }

            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = "XP +${lesson.xp}",
                    color = AppGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (!lesson.locked) {
                Button(
                    onClick = onLearnClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .height(46.dp)
                ) {
                    Text("LEARN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Placeholder tabs — build these out once you have real data
// ---------------------------------------------------------------------------

@Composable
fun LeaderboardTab() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = AppCyan,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("Leaderboard", color = AppWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Rankings coming soon", color = AppGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ProfileTab(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    var showSettings by remember { mutableStateOf(false) }
    val user = auth.currentUser
    val email = user?.email ?: "No email"

    // Registered username and student ID from users/{uid}; until they load,
    // fall back to the name on the Firebase account, then the email.
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        UserProfileRepository.load(uid, onResult = { profile = it }, onError = {})
    }

    val username = profile?.displayName
        ?: user?.displayName?.takeIf { it.isNotBlank() }
        ?: email.substringBefore("@")

    val initial = username.firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Profile Dashboard",
                color = AppWhite,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(AppCard, RoundedCornerShape(12.dp))
                    .clickable { showSettings = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = AppCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(110.dp)
                .background(AppBlue.copy(alpha = 0.2f), CircleShape)
                .padding(8.dp)
                .background(AppBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = AppWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = username,
            color = AppWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "CvSU Student",
            color = AppGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Personal Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppCard),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ProfileInfoRow(icon = Icons.Filled.Email, label = "CvSU Email", value = email)

                profile?.studentId?.let { id ->
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = AppNavy.copy(alpha = 0.5f)
                    )
                    ProfileInfoRow(icon = Icons.Filled.Badge, label = "Student ID", value = id)
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            onDismiss = { showSettings = false },
            onSignOut = {
                showSettings = false
                auth.signOut()
                onLogout()
            }
        )
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = AppGray, fontSize = 12.sp)
            Text(value, color = AppWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}
