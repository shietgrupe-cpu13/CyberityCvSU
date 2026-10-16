package com.cyberity.cvsu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** A separate, ungraded introduction on the Unit 2 learning path. */
@Composable
fun UnitTwoWalkthroughScreen(onExit: () -> Unit, onComplete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val examples = listOf(
        "Inspect the evidence" to "Open the simulation. Read the case file and inspect login records or account settings before drawing a conclusion.",
        "Investigate and repair" to "Use the lab tools to record a finding, generate credentials, configure sign-in protection, or revoke access. Save and verify your work.",
        "Record your proof" to "After the lab verifies your work, it displays one CASE proof code. Submit that code in the task panel, then continue the same case."
    )
    Column(Modifier.fillMaxSize().background(AppNavy).statusBarsPadding()
        .navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onExit) { Text("Back to Unit 2", color = AppCyan) }
        Text("UNIT 2 · LEVEL 0", color = AppCyan, fontSize = 12.sp)
        Text("How the account security labs work", color = AppWhite, fontSize = 21.sp)
        Text("Before Level 1, see how the connected labs work. This walkthrough has no score, timer, or heart cost. You can skip it and start the lessons.", color = AppGray, fontSize = 14.sp, lineHeight = 22.sp)
        Text("One case, three connected tasks", color = AppWhite, fontSize = 17.sp)
        Text("Meet Maya, a student who needs help with her account; Jules, who investigates portal alerts; and Elena, who owns the registrar mailbox. Ask about their situation, inspect the evidence, then repair the fictional account. Your findings carry forward through three tasks. The lab opens from the task panel, just like Unit 1.", color = AppGray, fontSize = 14.sp, lineHeight = 22.sp)
        Text("Example: a student account audit", color = AppCyan, fontSize = 14.sp)
        Text("Task 1: Replace a predictable portal password.\nTask 2: Remove reuse from its linked shopping account.\nTask 3: Repair the portal password policy.", color = AppWhite, fontSize = 14.sp, lineHeight = 24.sp)
        Text("$step of 3 workflow steps reviewed", color = AppCyan, fontSize = 14.sp)
        examples.take(step).forEach { (card, reason) ->
            Text("$card\n$reason", color = AppWhite, fontSize = 14.sp, lineHeight = 22.sp)
        }
        if (step < 3) {
            Text("Let's walk through lab step ${step + 1}.", color = AppGray, fontSize = 14.sp)
            Button(onClick = { step++ }) { Text("Show step ${step + 1}") }
        } else {
            Text("The proof code becomes available only after the lab verifies your practical work. Guessing a code before doing the work does not unlock submission.", color = AppGray, fontSize = 14.sp, lineHeight = 22.sp)
            Text("Use optional hints when needed. Close the simulation to return to the task panel; your current work stays in that lab session. Each completed task adds to the case journal. At the end, read what happened, why it matters, and what to do in real life.", color = AppWhite, fontSize = 14.sp, lineHeight = 22.sp)
            Button(onClick = onComplete) { Text("Finish walkthrough · unlock Level 1") }
        }
        TextButton(onClick = onComplete) { Text("Skip walkthrough · unlock Level 1", color = AppCyan) }
    }
}
