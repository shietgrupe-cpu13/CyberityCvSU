package com.cyberity.cvsu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudentNameFields(
    firstName: String, onFirstName: (String) -> Unit,
    middleName: String, onMiddleName: (String) -> Unit,
    lastName: String, onLastName: (String) -> Unit,
    enabled: Boolean = true
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(firstName, onFirstName, label = { Text("First name") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(middleName, onMiddleName, label = { Text("Middle name (optional)") },
            supportingText = { Text("Leave blank if you do not have a middle name.") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(lastName, onLastName, label = { Text("Last name") },
            singleLine = true, enabled = enabled, modifier = Modifier.fillMaxWidth())
        val error = StudentIdRules.validateNameParts(firstName, middleName, lastName)
        if (error != null && (firstName.isNotEmpty() || middleName.isNotEmpty() || lastName.isNotEmpty()))
            Text(error, color = AppDanger)
    }
}
