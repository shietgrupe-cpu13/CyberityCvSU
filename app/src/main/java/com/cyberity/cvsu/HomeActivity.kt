package com.cyberity.cvsu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton


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
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = AppNavy,
        bottomBar = {
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
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> LearnTab()
                1 -> LeaderboardTab()
                2 -> ProfileTab(
                    onLogout = onLogout
                )
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

// ---------------------------------------------------------------------------
// Learn tab: topic header + stats + progress + expandable module list
// ---------------------------------------------------------------------------

@Composable
fun LearnTab() {
    val topics = remember {
        listOf(
            "Intro to Cybersecurity",
            "Network Security",
            "Web Application Security",
            "Cryptography Basics"
        )
    }
    var currentTopic by remember { mutableStateOf(topics.first()) }
    var topicMenuExpanded by remember { mutableStateOf(false) }

    val modules = remember { sampleModules() }
    // Tracks which module ids are expanded. "network" starts open to mirror the sample data.
    val expandedState = remember { mutableStateMapOf("network" to true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppCard)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { topicMenuExpanded = true }
                ) {
                    Text(
                        text = currentTopic,
                        color = AppWhite,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppGray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                DropdownMenu(
                    expanded = topicMenuExpanded,
                    onDismissRequest = { topicMenuExpanded = false }
                ) {
                    topics.forEach { topic ->
                        DropdownMenuItem(
                            text = { Text(topic) },
                            onClick = {
                                currentTopic = topic
                                topicMenuExpanded = false
                                // TODO: reload modules for the newly selected topic
                            }
                        )
                    }
                }
            }

            IconButton(onClick = { /* TODO: open notifications */ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = AppWhite)
            }
            IconButton(onClick = { /* TODO: open overflow menu */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = AppWhite)
            }
        }

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatChip(icon = Icons.Filled.Favorite, value = "5", tint = Color(0xFFFF4D6D))
            Spacer(modifier = Modifier.width(8.dp))
            StatChip(icon = Icons.Filled.Star, value = "0", tint = AppCyan)
            Spacer(modifier = Modifier.width(8.dp))
            StatChip(icon = Icons.Filled.CheckCircle, value = "45", tint = AppBlue)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { /* TODO: navigate to your paywall / upgrade screen */ },
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("TRY PRO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Topic progress bar
        LinearProgressIndicator(
            progress = { 0.35f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(6.dp),
            color = AppBlue,
            trackColor = AppCard,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Module + lesson list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(modules) { module ->
                ModuleHeaderRow(
                    module = module,
                    expanded = expandedState[module.id] == true,
                    onToggle = {
                        expandedState[module.id] = expandedState[module.id] != true
                    }
                )
                if (expandedState[module.id] == true) {
                    module.items.forEach { lesson ->
                        LessonCard(
                            lesson = lesson,
                            onLearnClick = {
                                // TODO: navigate to the actual lesson/CTF challenge screen
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

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

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    val user = auth.currentUser

    val email = user?.email ?: "No email"

    val username = email.substringBefore("@")

    val initial = username.firstOrNull()?.uppercase() ?: "?"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Profile",
            color = AppWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(110.dp)
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
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = email,
            color = AppGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppCard
            ),
            shape = RoundedCornerShape(18.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        tint = AppCyan
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {

                        Text(
                            "CvSU Email",
                            color = AppWhite,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            email,
                            color = AppGray,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        showLogoutDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Sign Out")
                }

            }

        }
    }
    if (showLogoutDialog) {

        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },

            title = {
                Text("Sign Out")
            },

            text = {
                Text("Are you sure you want to sign out?")
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()
                        onLogout()
                    }
                ) {
                    Text(
                        "Sign Out",
                        color = Color.Red
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}