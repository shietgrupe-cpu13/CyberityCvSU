package com.cyberity.cvsu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.imePadding
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.DisposableEffect
import com.cyberity.cvsu.ui.theme.CyberityThemeState
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.auth.userProfileChangeRequest
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberity.cvsu.ui.theme.MyFirstTryTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthMultiFactorException
import com.google.firebase.auth.MultiFactorResolver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.IconButton
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedButton
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.Surface
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Debug force option for onboarding tutorial testing
const val DEBUG_FORCE_ONBOARDING = false

// Shared colors so every screen stays consistent.
// "Terminal Teal" palette: each color has a Dark Mode and a Light Mode value.
// CyberityThemeState decides which one is returned.
private val isDarkMode: Boolean get() = CyberityThemeState.isDark

val AppBlue: Color get() = if (isDarkMode) Color(0xFF14B8A6) else Color(0xFF0F766E)   // primary (teal): buttons, unit banner
val AppNavy: Color get() = if (isDarkMode) Color(0xFF0A1016) else Color(0xFFD8E3E2)   // screen background
val AppCard: Color get() = if (isDarkMode) Color(0xFF17222E) else Color(0xFFF1F6F5)   // cards, header, nav bar
val AppCyan: Color get() = if (isDarkMode) Color(0xFF5EEAD4) else Color(0xFF0B6560)   // brand, links, highlights
val AppWhite: Color get() = if (isDarkMode) Color(0xFFE8EEF3) else Color(0xFF0A1A1E)  // main text
val AppGray: Color get() = if (isDarkMode) Color(0xFF8FA0B2) else Color(0xFF455A61)   // secondary text
val AppBorder: Color get() = if (isDarkMode) Color(0xFF2A3B4D) else Color(0xFFB1C6C4) // outlines, dividers

// Text and icons placed on a solid AppBlue (teal) surface.
// Dark Mode teal is bright, so its text is dark; Light Mode teal is deep, so its text is white.
val AppOnBlue: Color get() = if (isDarkMode) Color(0xFF04211E) else Color(0xFFFFFFFF)
// Small labels and progress bars on a solid AppBlue surface
val AppOnBlueAccent: Color get() = if (isDarkMode) Color(0xFF0B3B36) else Color(0xFFCCF5EE)

// Feedback colors
val AppSuccess: Color get() = if (isDarkMode) Color(0xFF3FD68A) else Color(0xFF146A34)
val AppDanger: Color get() = if (isDarkMode) Color(0xFFF2606C) else Color(0xFFBF2A47)

// Game colors
val AppXp: Color get() = if (isDarkMode) Color(0xFFF2B84B) else Color(0xFF8C5C00)     // XP and bonus rewards
val AppStreak: Color get() = if (isDarkMode) Color(0xFFFF9A52) else Color(0xFFAD4E0A) // day streak
val AppHeart: Color get() = if (isDarkMode) Color(0xFFFF6B81) else Color(0xFFC8304C)  // hearts / lives
val AppReward: Color get() = AppXp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Only local theme restoration belongs on the splash path; network work
        // is shown in the existing profile screen after the first app frame.
        CyberityThemeState.load(this)
        setTheme(if (CyberityThemeState.isDark) R.style.Theme_Cyberity_Starting else R.style.Theme_Cyberity_Starting_Light)
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        CyberityThemeState.rememberSplashTheme(this)
        splash.setOnExitAnimationListener { provider ->
            // ViewPropertyAnimator respects the device's animation scale.
            // Content is already drawn underneath; this never delays routing.
            provider.view.animate()
                .alpha(0f)
                .setDuration(180L)
                .withEndAction { provider.remove() }
                .start()
        }

        enableEdgeToEdge()
        LearningReminders.createChannel(this)
        LearningReminders.schedule(this)
        setContent {
            // Keep status bar and navigation bar icons readable in both modes.
            val dark = CyberityThemeState.isDark
            DisposableEffect(dark) {
                val barStyle = if (dark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose { }
            }

            MyFirstTryTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = AppNavy) {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current

    var mfaResolver by remember { mutableStateOf<MultiFactorResolver?>(null) }
    var currentScreen by remember {
        mutableStateOf(
            if (auth.currentUser?.isEmailVerified == true)
                "profileCheck"
            else
                "entry"
        )
    }

    val signOut = {
        AccountSessions.end(context)
        auth.signOut()
        currentScreen = "entry"
    }

    // Observe external sign-out and check revocation while authenticated content is open.
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { updated ->
            if (updated.currentUser == null && currentScreen in listOf("loggedIn", "profileCheck", "completeProfile")) {
                currentScreen = "entry"
            }
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    LaunchedEffect(currentScreen) {
        if (currentScreen in listOf("profileCheck", "loggedIn", "completeProfile")) {
            while (true) {
                val uid = auth.currentUser?.uid ?: break
                if (PrivacyPreferencesRepository.activeDeletionUid != uid) AccountSessions.touch(context) { error ->
                    if (auth.currentUser?.uid == uid && AccountSessions.isRevoked(error)) signOut()
                }
                delay(60_000L)
            }
        }
    }

    when (currentScreen) {
        "entry" -> WelcomeEntryScreen(
            onNewUserSelected = { currentScreen = "register" },
            onExistingUserSelected = { currentScreen = "login" }
        )
        "home" -> Greeting(
            name = "-CYBERITY-",
            onLoginClick = { currentScreen = "login" }
        )
        "login" -> LoginScreen(
            onBackClick = { currentScreen = "entry" },
            onRegisterClick = { currentScreen = "register" },
            onLoginSuccess = { currentScreen = "profileCheck" },
            onMfaRequired = { resolver ->
                mfaResolver = resolver
                currentScreen = "mfa"
            }
        )
        "mfa" -> TotpSignInScreen(
            resolver = mfaResolver,
            onSuccess = {
                currentScreen = "profileCheck"
            },
            onCancel = {
                signOut()
            }
        )
        "register" -> RegisterScreen(
            onBackClick = { currentScreen = "entry" },
            onLoginClick = { currentScreen = "login" },
            onRegisterSuccess = { currentScreen = "checkEmail" }
        )
        "checkEmail" -> CheckEmailScreen(
            onBackToLogin = { currentScreen = "login" }
        )
        "profileCheck" -> {
            var failed by remember { mutableStateOf(false) }
            var errorDetail by remember { mutableStateOf<String?>(null) }
            var attempt by remember { mutableIntStateOf(0) }
            var slow by remember { mutableStateOf(false) }

            LaunchedEffect(attempt) {
                failed = false
                slow = false
                errorDetail = null
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    currentScreen = "entry"
                    return@LaunchedEffect
                }
                try {
                    val deleting = withTimeout(30_000L) { PrivacyPreferencesRepository.readDeletionState(uid) }
                    if (deleting) {
                        currentScreen = "privacyRecovery"
                        return@LaunchedEffect
                    }
                    // A deadline bounds the wait; it never delays a ready result.
                    val profile = withTimeout(30_000L) {
                        suspendCancellableCoroutine<UserProfile?> { continuation ->
                            UserProfileRepository.load(
                                uid,
                                onResult = { profile ->
                                    if (continuation.isActive) continuation.resume(profile)
                                },
                                onError = { message ->
                                    if (continuation.isActive) continuation.resumeWithException(IllegalStateException(message))
                                }
                            )
                        }
                    }
                    if (profile?.isComplete == true) {
                        ProfileCache.markComplete(context, uid)
                        currentScreen = "loggedIn"
                    } else {
                        currentScreen = "completeProfile"
                    }
                } catch (_: TimeoutCancellationException) {
                    errorDetail = "Profile validation timed out."
                    failed = true
                } catch (cancelled: CancellationException) {
                    // Leaving this screen cancels its callbacks and timers.
                    throw cancelled
                } catch (error: Exception) {
                    errorDetail = error.message
                    failed = true
                }
            }

            LaunchedEffect(attempt, failed) {
                slow = false
                if (!failed) {
                    // Feedback timer only: validation can complete at any time.
                    delay(8_000L)
                    slow = true
                }
            }

            ProfileCheckScreen(
                failed = failed,
                errorDetail = errorDetail,
                onRetry = { attempt++ },
                onSignOut = signOut,
                slow = slow
            )
        }
        "privacyRecovery" -> SettingsDialog(onDismiss = signOut, onSignOut = signOut, startInPrivacy = true)
        "completeProfile" -> CompleteProfileScreen(
            onProfileSaved = { currentScreen = "profileCheck" },
            onSignOut = signOut
        )
        "loggedIn" -> HomeScreen(
            onLogout = {
                currentScreen = "entry"
            }
        )
    }
}

@Composable
fun WelcomeEntryScreen(
    onNewUserSelected: () -> Unit,
    onExistingUserSelected: () -> Unit
) {
    val logo = R.drawable.cyberity_logo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Top part scrolls on small phones; the buttons below always stay in reach.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Brand
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = logo),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Cyber")
                        withStyle(SpanStyle(color = AppCyan)) { append("ity") }
                    },
                    color = AppWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Headline
            Text(
                text = "Learn cybersecurity\nby doing it.",
                color = AppWhite,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Hands-on labs and challenges built for CvSU students.",
                color = AppGray,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // What students get
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                WelcomeFeature(
                    icon = Icons.Outlined.Computer,
                    title = "Interactive simulations",
                    subtitle = "Investigate real-looking inboxes and consoles"
                )
                WelcomeFeature(
                    icon = Icons.Outlined.Flag,
                    title = "Capture-the-flag challenges",
                    subtitle = "Find the clue, capture the flag"
                )
                WelcomeFeature(
                    icon = XpBolt,
                    title = "Earn XP and keep your streak",
                    subtitle = "Climb the leaderboard with your class"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Actions (same destinations as before)
        Button(
            onClick = onNewUserSelected,
            colors = ButtonDefaults.buttonColors(containerColor = AppBlue, contentColor = AppOnBlue),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(text = "Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(
            onClick = onExistingUserSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "I already have an account",
                color = AppCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** One row on the Welcome screen describing what Cyberity offers. */
@Composable
private fun WelcomeFeature(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCard, RoundedCornerShape(16.dp))
            .border(1.dp, AppBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AppBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppCyan, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, color = AppWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = AppGray, fontSize = 13.sp, lineHeight = 17.sp)
        }
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
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    /** The Login/Register look: card-filled, softer border, cyan focus. Off keeps the original style. */
    filled: Boolean = false
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

        enabled = enabled,

        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),

        shape = RoundedCornerShape(if (filled) 14.dp else 12.dp),

        colors = OutlinedTextFieldDefaults.colors(

            focusedBorderColor = if (filled) AppCyan else AppBlue,
            unfocusedBorderColor = if (filled) AppBorder else AppGray,
            focusedContainerColor = if (filled) AppCard else Color.Transparent,
            unfocusedContainerColor = if (filled) AppCard else Color.Transparent,

            focusedLabelColor = AppCyan,

            cursorColor = AppCyan,

            focusedTextColor = AppWhite,
            unfocusedTextColor = AppWhite,
            disabledTextColor = AppWhite.copy(alpha = 0.7f),
            disabledBorderColor = AppGray.copy(alpha = 0.4f),
            disabledLabelColor = AppGray,
            disabledLeadingIconColor = AppCyan.copy(alpha = 0.6f)
        ),

        modifier = Modifier.fillMaxWidth()
    )
}

// ---------------------------------------------------------------------------
// Login / Register building blocks
// ---------------------------------------------------------------------------

/** The shield and "Cyberity" wordmark, as on the Welcome screen. */
@Composable
internal fun AuthBrand(logoSize: Int = 36, fontSize: Int = 22) {
    val logo = R.drawable.cyberity_logo
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = logo),
            contentDescription = null,
            modifier = Modifier.size(logoSize.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = buildAnnotatedString {
                append("Cyber")
                withStyle(SpanStyle(color = AppCyan)) { append("ity") }
            },
            color = AppWhite,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** An error (red) or notice (teal) shown just above the main button. */
@Composable
private fun AuthBanner(text: String, isError: Boolean) {
    val accent = if (isError) AppDanger else AppCyan
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isError) Icons.Filled.Warning else Icons.Filled.Email,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = accent, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

/** Small grey hint under a field. */
@Composable
private fun AuthHint(text: String) {
    Text(
        text,
        color = AppGray,
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp)
    )
}

/** Small cyan label that opens a group of fields. */
@Composable
private fun AuthSectionLabel(text: String) {
    Text(
        text,
        color = AppCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

/** "Question? Action": grey question, cyan bold action. */
@Composable
private fun AuthFooterLink(question: String, action: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(question, color = AppGray, fontSize = 14.sp)
        TextButton(onClick = onClick) {
            Text(action, color = AppCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Login/Register page: [top] (the form) and [bottom] (banners, button, footer)
 * in one scroll. Its height is the screen WITHOUT the keyboard, so the button
 * sits at the bottom normally, and when the keyboard opens it stays put under
 * the keyboard (reachable by scrolling) instead of riding up above it.
 */
@Composable
private fun AuthPage(
    top: @Composable ColumnScope.() -> Unit,
    bottom: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppNavy)
            .systemBarsPadding()
    ) {
        val fullHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = fullHeight)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(content = top)
                Column(content = bottom)
            }
        }
    }
}

/** The main action button, matching the Welcome screen's. */
@Composable
private fun AuthPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = AppBlue, contentColor = AppOnBlue),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoginScreen(
    /** System back: return to the Welcome screen instead of closing the app. */
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onMfaRequired: (MultiFactorResolver) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var resetMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResetLoading by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current
    val signInGuard = remember { AuthAttemptGuard.get(context) }
    val resetGuard = remember { AuthAttemptGuard.get(context, passwordReset = true) }
    val retrySeconds = rememberRetrySeconds(signInGuard)
    val resetRetrySeconds = rememberRetrySeconds(resetGuard)
    BackHandler { if (!isLoading && !isResetLoading) onBackClick() }

    AuthPage(
        top = {
            Spacer(modifier = Modifier.height(28.dp))
            AuthBrand()
            Spacer(modifier = Modifier.height(52.dp))

            Text(
                "Welcome back",
                color = AppWhite,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Log in to continue your training.",
                color = AppGray,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                email,
                { email = it },
                "CvSU email",
                Icons.Filled.Email,
                enabled = !isLoading && !isResetLoading,
                filled = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            AuthTextField(
                password,
                { password = it },
                "Password",
                Icons.Filled.Lock,
                isPassword = true,
                enabled = !isLoading && !isResetLoading,
                filled = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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
                                if (!resetGuard.tryStart()) {
                                    errorMessage = "Please wait before requesting another reset email."
                                    return@TextButton
                                }
                                isResetLoading = true
                                auth.sendPasswordResetEmail(normalizedEmail)
                                    .addOnSuccessListener {
                                        resetGuard.finished()
                                        isResetLoading = false
                                        resetMessage =
                                            "If this email is registered, password reset instructions will be sent shortly."
                                    }
                                    .addOnFailureListener { exception ->
                                        isResetLoading = false
                                        when (exception) {
                                            is com.google.firebase.FirebaseTooManyRequestsException -> {
                                                resetGuard.serverThrottled()
                                                errorMessage = "Too many requests. Please wait before requesting another reset email."
                                            }
                                            is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                                resetGuard.finished()
                                                resetMessage = "If this email is registered, password reset instructions will be sent shortly."
                                            }
                                            else -> {
                                                resetGuard.finished()
                                                errorMessage = "Could not send reset instructions. Check your connection and try again later."
                                            }
                                        }
                                    }
                            }
                        }
                    },
                    enabled = !isLoading && !isResetLoading && resetRetrySeconds == 0L
                ) {
                    Text(
                        text = when {
                            isResetLoading -> "Sending reset email..."
                            resetRetrySeconds > 0 -> "Reset available in ${resetRetrySeconds}s"
                            else -> "Forgot password?"
                        },
                        color = AppCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        },
        bottom = {
            if (retrySeconds > 1 && !isLoading) {
                AuthBanner("Please wait ${retrySeconds}s before trying to sign in again.", isError = false)
                Spacer(modifier = Modifier.height(14.dp))
            }
            if (resetMessage.isNotEmpty()) {
                AuthBanner(resetMessage, isError = false)
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (errorMessage.isNotEmpty()) {
                AuthBanner(errorMessage, isError = true)
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppCyan)
                }
            } else {
                AuthPrimaryButton("Log in", enabled = !isResetLoading && retrySeconds == 0L) {
                    val loginEmail = email.trim().lowercase()

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches() ||
                        !loginEmail.endsWith("@cvsu.edu.ph")
                    ) {
                        errorMessage = "Please use your @cvsu.edu.ph email"
                    } else if (password.isBlank()) {
                        errorMessage = "Please enter your password"
                    } else if (!signInGuard.tryStart()) {
                        errorMessage = "Please wait before trying to sign in again."
                    } else {
                        isLoading = true
                        errorMessage = ""
                        auth.signInWithEmailAndPassword(loginEmail, password)
                            .addOnSuccessListener {
                                signInGuard.authenticated()
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
                                if (exception is FirebaseAuthMultiFactorException) {
                                    signInGuard.finished()
                                    onMfaRequired(exception.resolver)
                                } else {
                                    errorMessage = signInGuard.handleFailure(exception)
                                }
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            AuthFooterLink("New to Cyberity?", "Create an account") {
                if (!isLoading && !isResetLoading) onRegisterClick()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    )
}

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    /** "Already have an account? Log in" — straight to Login, unlike back (Welcome). */
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {  BackHandler {
    onBackClick()
}
    var username by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = remember { FirebaseAuth.getInstance() }
    val context = LocalContext.current
    val debugBuild = remember { context.isDebugBuild() }

    AuthPage(
        top = {
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppWhite
                    )
                }
                AuthBrand(logoSize = 30, fontSize = 19)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Create your account",
                color = AppWhite,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Use your @cvsu.edu.ph email.",
                color = AppGray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(22.dp))
            AuthSectionLabel("YOUR PROFILE")
            Spacer(modifier = Modifier.height(8.dp))

            AuthTextField(
                username,
                { username = it },
                "Username",
                Icons.Filled.Person,
                filled = true
            )
            AuthHint("Shown on the leaderboard.")

            Spacer(modifier = Modifier.height(10.dp))

            AuthTextField(
                studentId,
                { input ->
                    studentId = if (debugBuild && input.any { !it.isDigit() }) {
                        input.take(8)
                    } else {
                        input.filter { it.isDigit() }.take(9)
                    }
                },
                "Student ID",
                Icons.Filled.Badge,
                keyboardType = if (debugBuild) KeyboardType.Text else KeyboardType.Number,
                filled = true
            )
            AuthHint("9 digits, e.g. 202310502")

            Spacer(modifier = Modifier.height(20.dp))
            AuthSectionLabel("SIGN-IN DETAILS")
            Spacer(modifier = Modifier.height(8.dp))

            AuthTextField(
                email,
                { email = it },
                "CvSU email",
                Icons.Filled.Email,
                filled = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            AuthTextField(
                password,
                { password = it },
                "Password",
                Icons.Filled.Lock,
                isPassword = true,
                filled = true
            )
            AuthHint("At least 6 characters.")

            Spacer(modifier = Modifier.height(10.dp))

            AuthTextField(
                confirmPassword,
                { confirmPassword = it },
                "Confirm password",
                Icons.Filled.Lock,
                isPassword = true,
                filled = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        },
        bottom = {
            if (errorMessage.isNotEmpty()) {
                AuthBanner(errorMessage, isError = true)
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppCyan)
                }
            } else {
                AuthPrimaryButton("Create account") {
                    val registerEmail = email.trim().lowercase()
                    errorMessage =
                        StudentIdRules.validateDisplayName(username)?.let { "Username: $it" }
                            ?: StudentIdRules.validateStudentId(studentId, debugBuild)
                                    ?: when {
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmail).matches() ||
                                        !registerEmail.endsWith("@cvsu.edu.ph") ->
                                    "Please use your @cvsu.edu.ph email"

                                password != confirmPassword ->
                                    "Passwords do not match"

                                password.length < 6 ->
                                    "Password must be at least 6 characters"

                                else -> ""
                            }

                    if (errorMessage.isEmpty()) {
                        isLoading = true
                        val normalizedId = StudentIdRules.normalize(studentId)
                        val profile = UserProfile(
                            studentId = normalizedId,
                            displayName = username.trim(),
                            isTester = StudentIdRules.isDevId(normalizedId)
                        )

                        fun finish() {
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener {
                                    isLoading = false
                                    onRegisterSuccess()
                                }
                        }

                        auth.createUserWithEmailAndPassword(registerEmail, password)
                            .addOnSuccessListener { result ->
                                val newUser = result.user ?: return@addOnSuccessListener finish()

                                // Also keep the name on the Firebase account itself.
                                newUser.updateProfile(
                                    userProfileChangeRequest { displayName = profile.displayName }
                                )

                                UserProfileRepository.save(newUser.uid, registerEmail, profile) { saved ->
                                    when (saved) {
                                        ProfileSaveResult.Saved -> {
                                            ProfileCache.markComplete(context, newUser.uid)
                                            finish()
                                        }

                                        // ID belongs to someone else: undo the account so the
                                        // student can try again with the same email.
                                        ProfileSaveResult.IdTaken -> newUser.delete()
                                            .addOnCompleteListener {
                                                isLoading = false
                                                errorMessage =
                                                    "This student ID is already linked to another account."
                                            }

                                        // Couldn't save the profile (e.g. offline). The account
                                        // still works; "Complete your profile" asks again at login.
                                        is ProfileSaveResult.Failed -> finish()
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                errorMessage =
                                    exception.localizedMessage ?: "Registration failed"
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            AuthFooterLink("Already have an account?", "Log in", onLoginClick)
            Spacer(modifier = Modifier.height(16.dp))
        }
    )
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
