package com.cyberity.cvsu

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern, styled Settings Dialog matching Cyberity CvSU app theme.
 * Includes level font scaling (Small / Medium / Large), Multi-Factor Authentication (MFA),
 * preference toggles, and Sign Out action.
 */
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onSignOut: () -> Unit = {},
    onReplayTutorial: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_settings", Context.MODE_PRIVATE) }

    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications", true)) }
    var fontSizeChoice by remember { mutableStateOf(prefs.getString("font_size", "medium") ?: "medium") }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    fun saveBool(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppCard,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = AppCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Settings",
                        color = AppWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = AppGray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp)
            ) {
                // Section 1: Level Font Size
                Text(
                    text = "LEVEL FONT SIZE",
                    color = AppCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Text scaling for lessons, labs and quizzes",
                    color = AppGray,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf("small" to "Small", "medium" to "Medium", "large" to "Large")
                    options.forEach { (key, label) ->
                        val isSelected = fontSizeChoice.equals(key, ignoreCase = true) ||
                                (key == "medium" && fontSizeChoice.equals("normal", ignoreCase = true))

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    fontSizeChoice = key
                                    saveString("font_size", key)
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AppBlue else AppNavy,
                            border = if (isSelected) BorderStroke(1.dp, AppCyan) else BorderStroke(1.dp, AppNavy)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = if (isSelected) AppWhite else AppGray,
                                    fontSize = when (key) {
                                        "small" -> 12.sp
                                        "medium" -> 14.sp
                                        else -> 16.sp
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = AppNavy, thickness = 1.dp)
                Spacer(Modifier.height(16.dp))

                // Section 2: Multi-Factor Authentication
                Text(
                    text = "SECURITY",
                    color = AppCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(10.dp))

                TotpMfaCard()

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = AppNavy, thickness = 1.dp)
                Spacer(Modifier.height(16.dp))

                // Section 3: Preferences
                Text(
                    text = "PREFERENCES",
                    color = AppCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Notifications", color = AppWhite, fontSize = 14.sp)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            saveBool("notifications", it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppWhite,
                            checkedTrackColor = AppBlue,
                            uncheckedThumbColor = AppGray,
                            uncheckedTrackColor = AppNavy
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Replay App Tutorial Button
                Button(
                    onClick = {
                        onDismiss()
                        onReplayTutorial()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppBlue.copy(alpha = 0.25f),
                        contentColor = AppCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AppBlue)
                ) {
                    Text("Replay Guided Tutorial", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = AppNavy, thickness = 1.dp)
                Spacer(Modifier.height(20.dp))

                // Section 4: Sign Out
                Button(
                    onClick = { showSignOutConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.12f),
                        contentColor = Color(0xFFFF5C7A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.25f))
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        },
        confirmButton = {}
    )

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            containerColor = AppCard,
            title = { Text("Sign Out", color = AppWhite, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out?", color = AppGray, fontSize = 14.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        onDismiss()
                        onSignOut()
                    }
                ) {
                    Text("Sign Out", color = Color(0xFFFF5C7A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", color = AppGray)
                }
            }
        )
    }
}
