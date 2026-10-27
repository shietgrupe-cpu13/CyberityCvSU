package com.cyberity.cvsu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

@Composable
fun YearLevelField(value: Int?, onSelect: (Int) -> Unit, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) { if (!enabled) expanded = false }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Year Level", color = AppGray)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text(if (value == null) "Select Year Level" else YearLevels.label(value),
                    color = AppWhite, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ExpandMore, contentDescription = "Choose year level", tint = AppCyan)
            }
            DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
                YearLevels.options.forEach { year ->
                    DropdownMenuItem(text = { Text(YearLevels.label(year)) }, enabled = enabled, onClick = {
                        expanded = false
                        onSelect(year)
                    })
                }
            }
        }
    }
}

@Composable
fun StudentInformationScreen(onBusyChanged: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var profile by remember(uid) { mutableStateOf<UserProfile?>(null) }
    var firstName by rememberSaveable(uid) { mutableStateOf("") }
    var middleName by rememberSaveable(uid) { mutableStateOf("") }
    var lastName by rememberSaveable(uid) { mutableStateOf("") }
    var year by rememberSaveable(uid) { mutableStateOf<Int?>(null) }
    var loading by remember(uid) { mutableStateOf(true) }
    var saving by remember(uid) { mutableStateOf(false) }
    var message by remember(uid) { mutableStateOf<String?>(null) }
    var failed by remember(uid) { mutableStateOf(false) }
    var request by remember { mutableIntStateOf(0) }
    var nameLocked by remember(uid) { mutableStateOf(true) }
    var identityLoading by remember(uid) { mutableStateOf(true) }
    LaunchedEffect(uid, request) {
        loading = true
        identityLoading = true
        nameLocked = true
        failed = false
        message = null
        try {
            check(uid != null) { "Please sign in again." }
            val identity = StudentNameLock.load(uid).privacyAwait()
            val current = UserProfileRepository.loadConfirmed(uid).privacyAwait()
            check(current != null) { "Your student information is unavailable. Try again." }
            if (FirebaseAuth.getInstance().currentUser?.uid != uid) return@LaunchedEffect
            profile = current
            year = current.yearLevel
            firstName = current.firstName
            middleName = current.middleName
            lastName = current.lastName
            nameLocked = identity.locked
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            failed = true
            message = if (uid == null) "Please sign in again." else
                "Couldn't load your student information and name-lock status. Reconnect and try again."
        } finally {
            if (currentCoroutineContext().isActive) {
                loading = false
                identityLoading = false
            }
        }
    }
    DisposableEffect(Unit) { onDispose { onBusyChanged(false) } }
    SettingsNote("Your full name, student ID and year level appear on your completion certificate. Check your full name before starting Unit 1, Level 1: starting it locks your name. Your username is shown on the leaderboard.")
    if (loading) CircularProgressIndicator(color = AppCyan)
    profile?.let { student ->
        SettingsCard {
            Text("Username", color = AppGray)
            Text(student.displayName, color = AppWhite, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Student ID", color = AppGray)
            Text(student.studentId, color = AppWhite, fontWeight = FontWeight.SemiBold)
        }
        if (uid != null) UsernameSettings(uid, student.displayName, !saving && !loading,
            onBusy = { saving = it; onBusyChanged(it) }, onSaved = { request++ })
        if (nameLocked && !identityLoading && !failed) SettingsNote("Your full name is locked because Unit 1, Level 1 has already started. Your year level can still be updated. Contact the project administrator if your name needs correction.")
        if (student.firstName.isBlank() && student.fullName.isNotBlank())
            Text(if (nameLocked) "Full name: ${student.fullName}" else
                "Saved full name: ${student.fullName}. Enter your first and last names below to update it; middle name is optional.", color = AppGray)
        StudentNameFields(firstName, { firstName = it; message = null },
            middleName, { middleName = it; message = null }, lastName, { lastName = it; message = null },
            enabled = !saving && !loading && !identityLoading && !nameLocked)
        YearLevelField(year, { year = it; message = null }, enabled = !saving && !loading)
        Button(onClick = {
            val selected = year ?: return@Button
            val account = uid ?: return@Button
            saving = true
            onBusyChanged(true)
            message = null
            val save = if (nameLocked) UserProfileRepository.updateYearLevel(account, selected)
                else UserProfileRepository.updateStudentInformation(account, firstName, middleName, lastName, selected)
            save
                .addOnSuccessListener {
                    if (FirebaseAuth.getInstance().currentUser?.uid == account) {
                        profile = if (nameLocked) student.copy(yearLevel = selected)
                            else student.copy(yearLevel = selected, fullName = StudentIdRules.composeFullName(firstName, middleName, lastName),
                                firstName = firstName.trim(), middleName = middleName.trim(), lastName = lastName.trim())
                        ProfileCache.saveDetails(context, account, student.displayName, student.studentId, selected)
                        message = "Your student information has been saved."
                    }
                    saving = false; onBusyChanged(false)
                }
                .addOnFailureListener {
                    saving = false; onBusyChanged(false)
                    failed = true
                    nameLocked = true
                    message = "Couldn't save. Your name may have been locked on another device; reopen Student information and try again."
                }
        }, enabled = (nameLocked || StudentIdRules.validateNameParts(firstName, middleName, lastName) == null) && YearLevels.isValid(year) &&
                (year != student.yearLevel || (!nameLocked && (firstName.trim() != student.firstName ||
                    middleName.trim() != student.middleName || lastName.trim() != student.lastName))) && !saving && !loading && !identityLoading && !failed,
            modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Saving..." else "Save Student Information") }
    }
    message?.let { Text(it, color = AppCyan) }
    if (failed) TextButton(onClick = { request++ }) { Text("Try Again") }
}
