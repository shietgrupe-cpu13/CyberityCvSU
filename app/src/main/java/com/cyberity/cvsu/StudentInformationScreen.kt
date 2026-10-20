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

@Composable
fun YearLevelField(value: Int?, onSelect: (Int) -> Unit, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Year level", color = AppGray)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text(if (value == null) "Select year level" else YearLevels.label(value),
                    color = AppWhite, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ExpandMore, contentDescription = "Choose year level", tint = AppCyan)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                YearLevels.options.forEach { year ->
                    DropdownMenuItem(text = { Text(YearLevels.label(year)) }, onClick = {
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
    var fullName by rememberSaveable(uid) { mutableStateOf("") }
    var year by rememberSaveable(uid) { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }
    var request by remember { mutableIntStateOf(0) }
    LaunchedEffect(uid, request) {
        loading = true
        failed = false
        if (uid == null) { loading = false; failed = true; message = "Please sign in again." }
        else UserProfileRepository.load(uid, onResult = {
            profile = it
            year = it?.yearLevel
            fullName = it?.fullName.orEmpty()
            loading = false
            if (it == null) { failed = true; message = "Your student information is unavailable. Try again." }
        }, onError = { loading = false; failed = true; message = "Couldn't load your student information. Try again." })
    }
    DisposableEffect(Unit) { onDispose { onBusyChanged(false) } }
    SettingsNote("Your full name, student ID and year level appear on your completion certificate. Add or correct your full name here before downloading. Your username is shown on the leaderboard.")
    if (loading) CircularProgressIndicator(color = AppCyan)
    profile?.let { student ->
        SettingsCard {
            Text("Username", color = AppGray)
            Text(student.displayName, color = AppWhite, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Student ID", color = AppGray)
            Text(student.studentId, color = AppWhite, fontWeight = FontWeight.SemiBold)
        }
        OutlinedTextField(value = fullName, onValueChange = { fullName = it; message = null },
            label = { Text("Full name for certificate") }, singleLine = true,
            supportingText = { Text(StudentIdRules.validateFullName(fullName) ?: "This name appears on your certificate.") },
            isError = fullName.isNotEmpty() && StudentIdRules.validateFullName(fullName) != null,
            enabled = !saving && !loading, modifier = Modifier.fillMaxWidth())
        YearLevelField(year, { year = it; message = null }, enabled = !saving && !loading)
        Button(onClick = {
            val selected = year ?: return@Button
            val account = uid ?: return@Button
            saving = true
            onBusyChanged(true)
            message = null
            UserProfileRepository.updateStudentInformation(account, fullName, selected)
                .addOnSuccessListener {
                    if (FirebaseAuth.getInstance().currentUser?.uid == account) {
                        profile = student.copy(yearLevel = selected, fullName = fullName.trim())
                        ProfileCache.saveDetails(context, account, student.displayName, student.studentId, selected)
                        message = "Your student information has been saved."
                    }
                    saving = false; onBusyChanged(false)
                }
                .addOnFailureListener {
                    saving = false; onBusyChanged(false)
                    message = "Couldn't save your student information. Check your connection and try again."
                }
        }, enabled = StudentIdRules.validateFullName(fullName) == null && YearLevels.isValid(year) &&
                (year != student.yearLevel || fullName.trim() != student.fullName) && !saving && !loading,
            modifier = Modifier.fillMaxWidth()) { Text(if (saving) "Saving..." else "Save student information") }
    }
    message?.let { Text(it, color = AppCyan) }
    if (failed) TextButton(onClick = { request++ }) { Text("Try again") }
}
