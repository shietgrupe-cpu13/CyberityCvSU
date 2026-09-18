package com.cyberity.cvsu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

// ===========================================================================
// COMPLETE YOUR PROFILE
// ===========================================================================
// Shown once after login to any account without a profile — mainly accounts
// created before student IDs existed, including developer test accounts.

@Composable
fun CompleteProfileScreen(
    onProfileSaved: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val user = auth.currentUser
    val debugBuild = remember { context.isDebugBuild() }

    var studentId by rememberSaveable { mutableStateOf("") }
    // The username chosen at registration, if this account has one. When it
    // exists the name isn't asked again — it stays the same as registered.
    val registeredName = remember { user?.displayName?.takeIf { it.isNotBlank() } }
    var displayName by rememberSaveable {
        mutableStateOf(
            registeredName
                ?: user?.email?.substringBefore("@").orEmpty().take(StudentIdRules.DISPLAY_NAME_MAX)
        )
    }
    var errorMessage by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    fun submit() {
        val uid = user?.uid ?: return onSignOut()
        val idError = StudentIdRules.validateStudentId(studentId, debugBuild)
        val nameError = if (registeredName != null) null else StudentIdRules.validateDisplayName(displayName)
        errorMessage = idError ?: nameError ?: ""
        if (errorMessage.isNotEmpty()) return

        val normalizedId = StudentIdRules.normalize(studentId)
        val profile = UserProfile(
            studentId = normalizedId,
            displayName = displayName.trim(),
            // DEV- IDs mark test accounts, so they can be hidden from the leaderboard.
            isTester = StudentIdRules.isDevId(normalizedId)
        )

        isSaving = true
        UserProfileRepository.save(uid, user?.email, profile) { result ->
            isSaving = false
            when (result) {
                ProfileSaveResult.Saved -> {
                    if (registeredName == null) {
                        user?.updateProfile(userProfileChangeRequest { this.displayName = profile.displayName })
                    }
                    ProfileCache.markComplete(context, uid)
                    onProfileSaved()
                }
                ProfileSaveResult.IdTaken ->
                    errorMessage = "This student ID is already linked to another account."
                is ProfileSaveResult.Failed ->
                    errorMessage = result.message
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Complete your profile",
                    color = AppWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "We need a few details before you continue. You only have to do this once.",
                    color = AppGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    studentId,
                    { input ->
                        // Real IDs are 9 digits; debug builds may also type DEV-### test IDs.
                        studentId = if (debugBuild && input.any { !it.isDigit() }) {
                            input.take(8)
                        } else {
                            input.filter { it.isDigit() }.take(9)
                        }
                    },
                    "Student ID (e.g. 202310502)",
                    Icons.Filled.Badge,
                    keyboardType = if (debugBuild) KeyboardType.Text else KeyboardType.Number
                )

                if (debugBuild) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Debug build: test accounts can use DEV-001, DEV-002, …",
                        color = AppCyan.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                AuthTextField(
                    displayName,
                    { displayName = it },
                    "Username",
                    Icons.Filled.Person,
                    enabled = registeredName == null
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    if (registeredName != null) "The username you registered with."
                    else "Shown to other students, e.g. on the leaderboard.",
                    color = AppGray,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorMessage, color = Color.Red, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isSaving) {
                    CircularProgressIndicator(color = AppCyan)
                } else {
                    Button(
                        onClick = { submit() },
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Save and continue", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onSignOut, enabled = !isSaving) {
                    Text("Not you? Sign out", color = AppGray)
                }
            }
        }
    }
}

/** Shown while the profile is being checked, or when that check couldn't reach the server. */
@Composable
fun ProfileCheckScreen(
    failed: Boolean,
    errorDetail: String?,
    onRetry: () -> Unit,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!failed) {
            CircularProgressIndicator(color = AppCyan)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Couldn't load your profile",
                    color = AppWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Check your internet connection and try again.",
                    color = AppGray,
                    fontSize = 14.sp
                )
                // The real reason, shown only in debug builds to help development.
                if (errorDetail != null && LocalContext.current.isDebugBuild()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(errorDetail, color = Color.Red, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                ) {
                    Text("Try again")
                }
                TextButton(onClick = onSignOut) {
                    Text("Sign out", color = AppGray)
                }
            }
        }
    }
}
