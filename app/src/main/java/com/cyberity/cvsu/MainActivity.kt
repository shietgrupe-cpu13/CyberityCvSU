package com.cyberity.cvsu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberity.cvsu.ui.theme.MyFirstTryTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.vector.ImageVector

// Shared colors so every screen stays consistent
val AppBlue = Color(0xFF005CEB)
val AppNavy = Color(0xFF010E45)
val AppCard = Color(0xFF021A50)
val AppCyan = Color(0xFF6CB8EC)
val AppWhite = Color(0xFFF5F8FC)
val AppGray = Color(0xFF9AA9C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyFirstTryTheme {
                AppNavigator()
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val auth = remember { FirebaseAuth.getInstance() }

    var currentScreen by remember {
        mutableStateOf(
            if (auth.currentUser != null)
                "loggedIn"
            else
                "login"
        )
    }

    when (currentScreen) {
        "home" -> Greeting(
            name = "-CYBERITY-",
            onLoginClick = { currentScreen = "login" }
        )
        "login" -> LoginScreen(
            onRegisterClick = { currentScreen = "register" },
            onLoginSuccess = { currentScreen = "loggedIn" }
        )
        "register" -> RegisterScreen(
            onBackClick = { currentScreen = "login" },
            onRegisterSuccess = { currentScreen = "checkEmail" }
        )
        "checkEmail" -> CheckEmailScreen(
            onBackToLogin = { currentScreen = "login" }
        )
        "loggedIn" -> HomeScreen(
            onLogout = {
                currentScreen = "login"
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier, onLoginClick: () -> Unit) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                color = AppBlue,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
            ) {
                Text("Go to Login")
            }
        }
    }
}

// Reusable styled text field used by both Login and Register
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {

    var passwordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },

        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppCyan
            )
        },

        trailingIcon = {

            if (isPassword && value.isNotEmpty()) {

                val image =
                    if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisible)
                            "Hide password"
                        else
                            "Show password",
                        tint = AppGray
                    )
                }
            }
        },

        visualTransformation =
            if (isPassword && !passwordVisible)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,

        singleLine = true,

        shape = RoundedCornerShape(12.dp),

        colors = OutlinedTextFieldDefaults.colors(

            focusedBorderColor = AppBlue,
            unfocusedBorderColor = AppGray,

            focusedLabelColor = AppCyan,

            cursorColor = AppCyan,

            focusedTextColor = AppWhite,
            unfocusedTextColor = AppWhite
        ),

        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResetLoading by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
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
                    "Welcome!",
                    color = AppWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Log in to continue",
                    color = AppGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                AuthTextField(
                    email,
                    { email = it },
                    "CvSU Email",
                    Icons.Filled.Email
                )

                Spacer(modifier = Modifier.height(14.dp))

                AuthTextField(
                    password,
                    { password = it },
                    "Password",
                    Icons.Filled.Lock,
                    isPassword = true
                )

                TextButton(
                    onClick = {
                        val resetEmail = email.trim()
                        val normalizedEmail = resetEmail.lowercase()
                        errorMessage = ""
                        resetMessage = ""

                        when {
                            resetEmail.isBlank() -> {
                                errorMessage = "Please enter your CvSU email first"
                            }

                            !android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches() ||
                                    !normalizedEmail.endsWith("@cvsu.edu.ph") -> {
                                errorMessage = "Please use your @cvsu.edu.ph email"
                            }

                            else -> {
                                isResetLoading = true
                                auth.sendPasswordResetEmail(normalizedEmail)
                                    .addOnSuccessListener {
                                        isResetLoading = false
                                        resetMessage =
                                            "If this email is registered, password reset instructions will be sent shortly."
                                    }
                                    .addOnFailureListener { exception ->
                                        isResetLoading = false
                                        errorMessage = exception.localizedMessage
                                            ?: "Could not send password reset email"
                                    }
                            }
                        }
                    },
                    enabled = !isLoading && !isResetLoading
                ) {
                    Text(
                        text = if (isResetLoading) "Sending reset email..." else "Forgot password?",
                        color = AppCyan
                    )
                }

                if (resetMessage.isNotEmpty()) {
                    Text(resetMessage, color = AppCyan, fontSize = 13.sp)
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(errorMessage, color = Color.Red, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = AppCyan)
                } else {
                    Button(
                        onClick = {
                            val loginEmail = email.trim().lowercase()

                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches() ||
                                !loginEmail.endsWith("@cvsu.edu.ph")
                            ) {
                                errorMessage = "Please use your @cvsu.edu.ph email"
                            } else if (password.isBlank()) {
                                errorMessage = "Please enter your password"
                            } else {
                                isLoading = true
                                errorMessage = ""
                                auth.signInWithEmailAndPassword(loginEmail, password)
                                    .addOnSuccessListener {
                                        val user = auth.currentUser
                                        if (user != null && !user.isEmailVerified) {
                                            isLoading = false
                                            errorMessage =
                                                "Please verify your email before logging in. Check your inbox."
                                            auth.signOut()
                                        } else {
                                            isLoading = false
                                            onLoginSuccess()
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        isLoading = false
                                        errorMessage =
                                            exception.localizedMessage ?: "Login failed"
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Log In", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRegisterClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        "Don't have an account? Create one",
                        color = AppCyan
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {  BackHandler {
    onBackClick()
}
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
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
                    "Create Account",
                    color = AppWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Use your CvSU email to register",
                    color = AppGray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    username,
                    { username = it },
                    "Username",
                    Icons.Filled.Person
                )

                Spacer(modifier = Modifier.height(14.dp))

                AuthTextField(
                    email,
                    { email = it },
                    "Email (@cvsu.edu.ph)",
                    Icons.Filled.Email
                )

                Spacer(modifier = Modifier.height(14.dp))

                AuthTextField(
                    password,
                    { password = it },
                    "Password",
                    Icons.Filled.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                AuthTextField(
                    confirmPassword,
                    { confirmPassword = it },
                    "Confirm Password",
                    Icons.Filled.Lock,
                    isPassword = true
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(errorMessage, color = Color.Red, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = AppCyan)
                } else {
                    Button(
                        onClick = {
                            errorMessage = when {
                                !email.endsWith("@cvsu.edu.ph") ->
                                    "Please use your @cvsu.edu.ph email"

                                password != confirmPassword ->
                                    "Passwords do not match"

                                password.length < 6 ->
                                    "Password must be at least 6 characters"

                                username.isBlank() ->
                                    "Please enter a username"

                                else -> ""
                            }

                            if (errorMessage.isEmpty()) {
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        auth.currentUser?.sendEmailVerification()
                                            ?.addOnCompleteListener {
                                                isLoading = false
                                                onRegisterSuccess()
                                            }
                                    }
                                    .addOnFailureListener { exception ->
                                        isLoading = false
                                        errorMessage =
                                            exception.localizedMessage ?: "Registration failed"
                                    }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Create Account", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Back to Login", color = AppGray)
                }
            }
        }
    }
}

@Composable
fun CheckEmailScreen(onBackToLogin: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null,
                tint = AppCyan,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verify your email",
                color = AppWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We sent a verification link to your CvSU email. Please check your inbox and click the link before logging in.",
                color = AppGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackToLogin,
                colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
            ) {
                Text("Back to Login")
            }
        }
    }
}

