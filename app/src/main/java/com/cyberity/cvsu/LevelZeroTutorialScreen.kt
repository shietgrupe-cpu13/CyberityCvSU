package com.cyberity.cvsu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LevelZeroTutorialScreen(
    onCompleteTutorial: () -> Unit,
    onExit: () -> Unit
) {
    var selectedChoice by remember { mutableStateOf<Int?>(null) }
    var submitted by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }

    val choices = listOf(
        "A) Approve the login request and ignore the alert.",
        "B) Revoke session immediately, force password reset, and flag IP.",
        "C) Forward the security alert to a social media group."
    )
    val correctChoice = 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Filled.Close, contentDescription = "Exit", tint = AppGray)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LEVEL 0: SECURITY TRIAGE SIMULATOR", color = AppCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Your First Hands-on Challenge", color = AppWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mission Briefing Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = AppCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Simulation Briefing", color = AppWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Welcome to CYBERITY! In this training lab, you will analyze a suspicious login incident. Review the telemetry evidence below and select the correct cybersecurity response.",
                    color = AppGray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telemetry Evidence Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("SECURITY TELEMETRY", color = AppCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Event: Admin Login Attempt\n• Source IP: 195.201.19.82 (External / Data Center)\n• User-Agent: Python-Requests/2.28\n• Risk Score: 94 / 100 (High Threat)", color = AppWhite, fontSize = 14.sp, lineHeight = 22.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hint Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { showHint = true },
                colors = ButtonDefaults.buttonColors(containerColor = AppCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppCyan.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = AppCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Free Tutorial Hint", color = AppCyan, fontSize = 13.sp)
            }
        }

        if (showHint) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Hint: Automated scripts from external IP ranges attempting admin access indicate a credential stuffing attack. Immediate session revocation is required.",
                    color = AppCyan,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Choices
        Text("SELECT YOUR INCIDENT RESPONSE", color = AppGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        choices.forEachIndexed { index, text ->
            val isSelected = selectedChoice == index
            val borderColor = when {
                submitted && index == correctChoice -> AppSuccess
                submitted && isSelected -> AppDanger
                isSelected -> AppCyan
                else -> AppBorder
            }
            val backgroundColor = if (isSelected) AppBlue.copy(alpha = 0.3f) else AppCard

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable(enabled = !submitted) { selectedChoice = index },
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (isSelected) AppCyan else AppNavy, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(modifier = Modifier.size(10.dp).background(AppNavy, CircleShape))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(text, color = AppWhite, fontSize = 14.sp, modifier = Modifier.weight(1f), lineHeight = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (submitted) {
            val isCorrect = selectedChoice == correctChoice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) AppSuccess.copy(alpha = 0.15f) else AppDanger.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isCorrect) AppSuccess else AppDanger)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isCorrect) "✓ Correct Triage Decision!" else "✗ Incorrect Response",
                        color = if (isCorrect) AppSuccess else AppDanger,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isCorrect)
                            "Great job! You successfully identified the threat and revoked compromised credentials. You are now ready to tackle real CYBERITY challenges."
                        else
                            "Not quite. Approving unverified automated logins leads to a security breach. Revoking credentials was the correct choice.",
                        color = AppWhite,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onCompleteTutorial,
                colors = ButtonDefaults.buttonColors(containerColor = AppSuccess),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "COMPLETE TUTORIAL & START LEVEL 1",
                    color = AppNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        } else {
            Button(
                onClick = { if (selectedChoice != null) submitted = true },
                enabled = selectedChoice != null,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "SUBMIT RESPONSE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
