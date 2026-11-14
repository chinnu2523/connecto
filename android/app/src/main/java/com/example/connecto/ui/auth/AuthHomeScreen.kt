package com.example.connecto.ui.auth

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Checkbox
import com.example.connecto.ui.designsystem.ConnectoEmailGhostField
import com.example.connecto.ui.designsystem.ConnectoOtpCodeField
import com.example.connecto.ui.designsystem.ConnectoPasswordStrengthMeter
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.connecto.R
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.security.BiometricAuthManager
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.isAppInLightTheme
import com.example.connecto.ui.designsystem.ConnectoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun sanitizeAuthError(raw: String?): String {
    if (raw.isNullOrBlank()) return "An unexpected issue occurred. Please try again."
    val lower = raw.lowercase()
    return when {
        lower.contains("failed to connect") || lower.contains("connectexception") || lower.contains("network") || lower.contains("timeout") || lower.contains("unable to resolve host") || lower.contains("connection refused") ->
            "Unable to reach the server. Please check your internet connection."
        lower.contains("invalid username") || lower.contains("invalid password") || lower.contains("incorrect password") || lower.contains("credentials") || lower.contains("401") || lower.contains("unauthorized") || lower.contains("invalid login") ->
            "Incorrect username/email or password. Please verify your credentials."
        lower.contains("already exists") || lower.contains("already registered") || lower.contains("taken") ->
            "This account or username is already taken. Please sign in or choose another."
        lower.contains("otp") && (lower.contains("invalid") || lower.contains("expired") || lower.contains("incorrect") || lower.contains("mismatch")) ->
            "Invalid or expired verification code. Please request a new OTP code."
        lower.contains("rate limit") || lower.contains("too many") || lower.contains("429") ->
            "Too many attempts. Please wait a moment before trying again."
        lower.contains("age") || lower.contains("18") ->
            "You must be 18 years or older to register on Connecto."
        lower.contains("terms") ->
            "Please agree to the Terms of Use and Privacy Policy to continue."
        lower.contains("500") || lower.contains("server error") || lower.contains("internal") || lower.contains("json") || lower.contains("exception") || lower.contains("sqlite") ->
            "Service temporarily unavailable. Please try again shortly."
        raw.length > 80 ->
            "Unable to process request. Please check your details and try again."
        else -> raw
    }
}

enum class FieldValidationStatus {
    IDLE,
    CHECKING,
    VALID_AVAILABLE,
    TAKEN,
    INVALID_FORMAT
}

@Composable
fun AuthHomeScreen(
    onSignUpClick: (email: String, username: String, displayName: String, password: String) -> Unit = { _, _, _, _ -> },
    onSignInClick: (emailOrUsername: String, password: String) -> Unit = { _, _ -> },
    onAuthSuccess: (username: String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val isLight = isAppInLightTheme()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    var isSignUp by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Sign Up Form states
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Real-time verification states for Sign Up
    var usernameValidation by remember { mutableStateOf(FieldValidationStatus.IDLE) }
    var usernameMessage by remember { mutableStateOf("") }

    var emailValidation by remember { mutableStateOf(FieldValidationStatus.IDLE) }
    var emailMessage by remember { mutableStateOf("") }

    // Sign Up OTP verification states
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var isEmailOtpSent by remember { mutableStateOf(false) }
    var isEmailOtpVerified by remember { mutableStateOf(false) }
    var emailOtpCode by remember { mutableStateOf("") }
    var otpStatusMessage by remember { mutableStateOf<String?>(null) }
    var otpStatusIsError by remember { mutableStateOf(false) }

    // Date of birth and Age verification states (18+)
    var dateOfBirth by remember { mutableStateOf("") }
    var calculatedAge by remember { mutableStateOf<Int?>(null) }
    var dobErrorMessage by remember { mutableStateOf<String?>(null) }

    // Terms and Conditions agreement state
    var agreedToTerms by remember { mutableStateOf(false) }

    // Sign In Form states
    var loginUsernameOrEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isBiometricAvailable = remember { BiometricAuthManager.isBiometricEnrolled(context) }
    val isFaceId = remember { BiometricAuthManager.isFaceIdPrimary(context) }

    val lastLoginUser = remember {
        context.getSharedPreferences("connecto_user_profile_prefs", Context.MODE_PRIVATE)
            .getString("last_logged_in_username", "") ?: ""
    }

    LaunchedEffect(Unit) {
        if (loginUsernameOrEmail.isBlank() && lastLoginUser.isNotBlank()) {
            loginUsernameOrEmail = lastLoginUser
        }
    }

    // Helper function to calculate age and validate 18+
    fun validateAndCalculateAge(dobStr: String) {
        val trimmed = dobStr.trim()
        if (trimmed.length != 10 || !trimmed.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            calculatedAge = null
            dobErrorMessage = if (trimmed.isNotEmpty()) "Format: YYYY-MM-DD" else null
            return
        }
        try {
            val parts = trimmed.split("-")
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val today = java.util.Calendar.getInstance()
            var a = today.get(java.util.Calendar.YEAR) - y
            val curMonth = today.get(java.util.Calendar.MONTH) + 1
            val curDay = today.get(java.util.Calendar.DAY_OF_MONTH)
            if (curMonth < m || (curMonth == m && curDay < d)) {
                a--
            }
            calculatedAge = a
            if (a < 18) {
                dobErrorMessage = "You must be at least 18 years old to join Connecto (Age: $a)"
            } else {
                dobErrorMessage = null
            }
        } catch (_: Exception) {
            calculatedAge = null
            dobErrorMessage = "Invalid date format (YYYY-MM-DD)"
        }
    }

    // Forgot Password & 2FA states
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var show2FaLoginDialog by remember { mutableStateOf(false) }
    var pending2FaLogin by remember { mutableStateOf("") }
    var pending2FaMaskedDest by remember { mutableStateOf<String?>(null) }
    var pending2FaMethod by remember { mutableStateOf("email") }
    var showLegalTermsDialog by remember { mutableStateOf(false) }
    var legalTermsInitialTab by remember { mutableIntStateOf(0) }
    var forceShowValidationErrors by remember { mutableStateOf(false) }

    val emailPattern = Regex("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")

    // Live Debounced Username Uniqueness & Format Check
    LaunchedEffect(username, isSignUp) {
        if (!isSignUp) {
            usernameValidation = FieldValidationStatus.IDLE
            usernameMessage = ""
            return@LaunchedEffect
        }
        val clean = username.trim()
        if (clean.isEmpty()) {
            usernameValidation = FieldValidationStatus.IDLE
            usernameMessage = ""
            return@LaunchedEffect
        }
        if (clean.length < 3) {
            usernameValidation = FieldValidationStatus.INVALID_FORMAT
            usernameMessage = "Username must be at least 3 characters"
            return@LaunchedEffect
        }
        if (clean.length > 32) {
            usernameValidation = FieldValidationStatus.INVALID_FORMAT
            usernameMessage = "Username cannot exceed 32 characters"
            return@LaunchedEffect
        }
        if (!clean.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            usernameValidation = FieldValidationStatus.INVALID_FORMAT
            usernameMessage = "Only letters, numbers, and underscores allowed (no spaces)"
            return@LaunchedEffect
        }

        usernameValidation = FieldValidationStatus.CHECKING
        usernameMessage = "Checking database availability..."
        delay(320)

        val res = ConnectoApiClient.checkUsernameAvailability(clean)
        if (res.isSuccess) {
            val result = res.getOrThrow()
            if (result.available) {
                usernameValidation = FieldValidationStatus.VALID_AVAILABLE
                usernameMessage = "Username is available ✓"
            } else {
                usernameValidation = FieldValidationStatus.TAKEN
                usernameMessage = result.message.ifEmpty { "Username is already taken. Choose another." }
            }
        } else {
            // Fallback optimistic check
            usernameValidation = FieldValidationStatus.VALID_AVAILABLE
            usernameMessage = "Username available"
        }
    }

    // Live Debounced Email Validation & Uniqueness Check
    LaunchedEffect(email, isSignUp) {
        if (!isSignUp) {
            emailValidation = FieldValidationStatus.IDLE
            emailMessage = ""
            return@LaunchedEffect
        }
        val clean = email.trim()
        if (clean.isEmpty()) {
            emailValidation = FieldValidationStatus.IDLE
            emailMessage = ""
            return@LaunchedEffect
        }
        if (!clean.matches(emailPattern) || clean.length < 5) {
            emailValidation = FieldValidationStatus.INVALID_FORMAT
            emailMessage = if (!clean.contains("@")) {
                "Missing @ symbol (e.g. name@example.com)"
            } else if (!clean.substringAfter("@").contains(".")) {
                "Incomplete domain (e.g. name@example.com)"
            } else {
                "Please enter a valid email address (e.g. name@example.com)"
            }
            return@LaunchedEffect
        }

        emailValidation = FieldValidationStatus.CHECKING
        emailMessage = "Verifying email address..."
        delay(350)

        val res = ConnectoApiClient.checkEmailAvailability(clean)
        if (res.isSuccess) {
            val result = res.getOrThrow()
            if (result.available) {
                emailValidation = FieldValidationStatus.VALID_AVAILABLE
                emailMessage = "Email verified & available ✓"
            } else {
                emailValidation = FieldValidationStatus.TAKEN
                emailMessage = result.message.ifEmpty { "An account with this email already exists." }
            }
        } else {
            // Offline/Network fallback: format is valid
            emailValidation = FieldValidationStatus.VALID_AVAILABLE
            emailMessage = "Email format verified ✓"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Subtle ambient glow behind logo
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = if (isLight) 0.12f else 0.18f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ===== LOGO =====
            ConnectoLogo()

            Spacer(modifier = Modifier.height(20.dp))

            // ===== APP NAME =====
            Text(
                text = "CONNECTO-FUN",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ===== TAGLINE PILL =====
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GAMERS & FRIENDS COMMUNITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== FORM CONTAINER =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                    .padding(8.dp)
            ) {
                Column {
                    // Modern Fluid Tab Switcher Pill with Spring Physics Indicator
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(4.dp)
                    ) {
                        val pillWidth = maxWidth / 2
                        val indicatorOffset by animateDpAsState(
                            targetValue = if (isSignUp) 0.dp else pillWidth,
                            animationSpec = spring(
                                dampingRatio = 0.82f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "authTabPillOffset"
                        )

                        // Sliding Active Pill Background
                        Box(
                            modifier = Modifier
                                .offset(x = indicatorOffset)
                                .width(pillWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(gradientColors))
                        )

                        // Tab Buttons
                        Row(modifier = Modifier.fillMaxSize()) {
                            // SIGN UP TAB
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isSignUp) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isSignUp = true
                                            errorMessage = null
                                            forceShowValidationErrors = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PersonAdd,
                                        contentDescription = null,
                                        tint = if (isSignUp) contentOnGradient else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SIGN UP",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSignUp) contentOnGradient else MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }

                            // SIGN IN TAB
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (isSignUp) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isSignUp = false
                                            errorMessage = null
                                            forceShowValidationErrors = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Login,
                                        contentDescription = null,
                                        tint = if (!isSignUp) contentOnGradient else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SIGN IN",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isSignUp) contentOnGradient else MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contextual Security & Ease-of-use Micro-Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isSignUp) "Fast 30-sec Setup" else "Instant & Secure",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = gradientColors.first(),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "End-to-End Encrypted",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = gradientColors.first()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ===== IN-PLACE ERROR BANNER (UI FIX #01 - Clean In-Place Errors) =====
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(180)) + fadeOut()
            ) {
                if (errorMessage != null) {
                    val errorBg = if (isLight) Color(0xFFFEE2E2) else Color(0xFF7F1D1D).copy(alpha = 0.35f)
                    val errorText = if (isLight) Color(0xFFDC2626) else Color(0xFFFCA5A5)
                    val errorBorder = if (isLight) Color(0xFFFCA5A5) else Color(0xFFEF4444).copy(alpha = 0.5f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(errorBg)
                            .border(1.dp, errorBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = errorText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = sanitizeAuthError(errorMessage),
                            color = errorText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 17.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ===== INPUT FIELDS WITH HORIZONTAL TRANSITION =====
            AnimatedContent(
                targetState = isSignUp,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(260)) + fadeIn(tween(260)))
                            .togetherWith(slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(220)) + fadeOut(tween(220)))
                    } else {
                        (slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(260)) + fadeIn(tween(260)))
                            .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(220)) + fadeOut(tween(220)))
                    }
                },
                label = "authFormAnimatedContent"
            ) { signUpMode ->
                if (signUpMode) {
                    Column {
                        // Email Field with Interactive Ghost Completion (tap to fill in white)
                        ConnectoEmailGhostField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                                // Reset OTP verification if email changes
                                if (isEmailOtpVerified) {
                                    isEmailOtpVerified = false
                                    isEmailOtpSent = false
                                    otpStatusMessage = null
                                }
                            },
                            placeholder = "Email Address",
                            icon = Icons.Outlined.Email,
                            validationStatus = emailValidation,
                            errorMessage = emailMessage,
                            forceShowError = forceShowValidationErrors
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Email OTP Verification Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isEmailOtpVerified) "Email Verified ✓" else "Email Verification required",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEmailOtpVerified) ConnectoTheme.colors.success else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (!isEmailOtpVerified) {
                                Button(
                                    onClick = {
                                        val clean = email.trim()
                                        if (clean.isBlank() || !clean.matches(emailPattern)) {
                                            errorMessage = "Please enter a valid email address first."
                                            return@Button
                                        }
                                        if (emailValidation == FieldValidationStatus.TAKEN) {
                                            errorMessage = "This email is already registered."
                                            return@Button
                                        }
                                        isSendingOtp = true
                                        otpStatusMessage = null
                                        coroutineScope.launch {
                                            val res = ConnectoApiClient.requestSignupOtp(clean)
                                            isSendingOtp = false
                                            if (res.isSuccess) {
                                                isEmailOtpSent = true
                                                otpStatusIsError = false
                                                otpStatusMessage = "OTP sent to $clean. Check your inbox!"
                                            } else {
                                                otpStatusIsError = true
                                                otpStatusMessage = sanitizeAuthError(res.exceptionOrNull()?.message ?: "Failed to send OTP code")
                                            }
                                        }
                                    },
                                    enabled = !isSendingOtp && email.isNotBlank() && emailValidation != FieldValidationStatus.TAKEN,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    if (isSendingOtp) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    } else {
                                        Text(
                                            text = if (isEmailOtpSent) "Resend OTP" else "Send OTP",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Six-Box OTP Code Field with Mint Wave Animation
                        if (isEmailOtpSent && !isEmailOtpVerified) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Enter 6-digit verification code:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isVerifyingOtp) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 1.5.dp,
                                                color = Color(0xFF10B981)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Verifying...", fontSize = 11.sp, color = Color(0xFF10B981))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                ConnectoOtpCodeField(
                                    otpCode = emailOtpCode,
                                    onOtpChange = {
                                        emailOtpCode = it
                                        otpStatusMessage = null
                                    },
                                    isError = otpStatusIsError,
                                    enabled = !isVerifyingOtp,
                                    onComplete = { code ->
                                        // Auto-verify on completion!
                                        isVerifyingOtp = true
                                        otpStatusMessage = null
                                        coroutineScope.launch {
                                            val res = ConnectoApiClient.verifySignupOtp(email.trim(), code)
                                            isVerifyingOtp = false
                                            if (res.isSuccess && res.getOrNull() == true) {
                                                isEmailOtpVerified = true
                                                otpStatusIsError = false
                                                otpStatusMessage = "Email verified successfully ✓"
                                            } else {
                                                otpStatusIsError = true
                                                otpStatusMessage = sanitizeAuthError(res.exceptionOrNull()?.message ?: "Invalid OTP verification code")
                                            }
                                        }
                                    }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            val code = emailOtpCode.trim()
                                            if (code.length != 6) {
                                                otpStatusIsError = true
                                                otpStatusMessage = "Please enter the 6-digit code."
                                                return@Button
                                            }
                                            isVerifyingOtp = true
                                            otpStatusMessage = null
                                            coroutineScope.launch {
                                                val res = ConnectoApiClient.verifySignupOtp(email.trim(), code)
                                                isVerifyingOtp = false
                                                if (res.isSuccess && res.getOrNull() == true) {
                                                    isEmailOtpVerified = true
                                                    otpStatusIsError = false
                                                    otpStatusMessage = "Email verified successfully ✓"
                                                } else {
                                                    otpStatusIsError = true
                                                    otpStatusMessage = sanitizeAuthError(res.exceptionOrNull()?.message ?: "Invalid OTP verification code")
                                                }
                                            }
                                        },
                                        enabled = !isVerifyingOtp && emailOtpCode.length == 6,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ConnectoTheme.colors.success,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        if (isVerifyingOtp) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = Color.White
                                            )
                                        } else {
                                            Text("Verify Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (!otpStatusMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = otpStatusMessage ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (otpStatusIsError) Color(0xFFEF4444) else ConnectoTheme.colors.success,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Username Field
                        AuthTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            placeholder = "Username (3-32 chars)",
                            icon = Icons.Outlined.Person,
                            validationStatus = usernameValidation,
                            errorMessage = usernameMessage,
                            forceShowError = forceShowValidationErrors
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Display Name Field
                        AuthTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                errorMessage = null
                            },
                            placeholder = "Display Name (Optional)",
                            icon = Icons.Outlined.Shield
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date of Birth Field (Age Verification 18+)
                        AuthTextField(
                            value = dateOfBirth,
                            onValueChange = { input ->
                                // Auto format as YYYY-MM-DD
                                val digits = input.filter { it.isDigit() }
                                val formatted = when {
                                    digits.length <= 4 -> digits
                                    digits.length <= 6 -> "${digits.substring(0, 4)}-${digits.substring(4)}"
                                    else -> "${digits.substring(0, 4)}-${digits.substring(4, 6.coerceAtMost(digits.length))}-${digits.substring(6, 8.coerceAtMost(digits.length))}"
                                }
                                dateOfBirth = formatted
                                validateAndCalculateAge(formatted)
                                errorMessage = null
                            },
                            placeholder = "Date of Birth (YYYY-MM-DD)",
                            icon = Icons.Outlined.DateRange,
                            keyboardType = KeyboardType.Number,
                            validationStatus = if (dobErrorMessage != null) FieldValidationStatus.INVALID_FORMAT else if (calculatedAge != null && (calculatedAge ?: 0) >= 18) FieldValidationStatus.VALID_AVAILABLE else FieldValidationStatus.IDLE,
                            errorMessage = dobErrorMessage,
                            forceShowError = forceShowValidationErrors
                        )

                        // Date of Birth / Age Verification feedback (Verified status)
                        if (calculatedAge != null && (calculatedAge ?: 0) >= 18) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Age: $calculatedAge years old (18+ Verified ✓)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ConnectoTheme.colors.success
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        AuthTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            placeholder = "Password (Min 4 chars)",
                            icon = Icons.Outlined.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            validationStatus = if (password.isNotEmpty() && password.length < 4) FieldValidationStatus.INVALID_FORMAT else if (password.length >= 4) FieldValidationStatus.VALID_AVAILABLE else FieldValidationStatus.IDLE,
                            errorMessage = if (password.isNotEmpty() && password.length < 4) "Password must be at least 4 characters" else null,
                            forceShowError = forceShowValidationErrors
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Four-Step Password Strength Meter (fills and recolours as password grows)
                        ConnectoPasswordStrengthMeter(
                            password = password,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Terms of Use & Privacy Policy Checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = agreedToTerms,
                                onCheckedChange = { agreedToTerms = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "I confirm I am 18+ years of age and agree to the",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "Terms of Use",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = gradientColors.first(),
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier
                                            .clickable {
                                                legalTermsInitialTab = 0
                                                showLegalTermsDialog = true
                                            }
                                            .padding(vertical = 2.dp)
                                    )
                                    Text(
                                        text = " and ",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
                                    )
                                    Text(
                                        text = "Privacy Policy",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = gradientColors.first(),
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier
                                            .clickable {
                                                legalTermsInitialTab = 1
                                                showLegalTermsDialog = true
                                            }
                                            .padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SIGN IN FIELDS
                    Column {
                        // Quick Biometric Sign In (if enrolled on this device)
                        if (isBiometricAvailable) {
                            OutlinedButton(
                                onClick = {
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        BiometricAuthManager.authenticate(
                                            activity = activity,
                                            title = if (isFaceId) "Sign In with Face ID" else "Sign In with Fingerprint",
                                            subtitle = if (loginUsernameOrEmail.isNotBlank()) "Authenticate as $loginUsernameOrEmail" else "Biometric verification",
                                            isFaceId = isFaceId,
                                            onSuccess = { _ ->
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val targetUser = loginUsernameOrEmail.ifBlank { lastLoginUser }
                                                if (targetUser.isNotBlank()) {
                                                    onAuthSuccess(targetUser)
                                                } else {
                                                    errorMessage = "Please enter your username to complete biometric login."
                                                }
                                            },
                                            onError = { _, errString ->
                                                errorMessage = sanitizeAuthError(errString.toString())
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Fingerprint,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isFaceId) "Instant Face ID Sign In" else "Instant Biometric Sign In",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                                Text(
                                    text = "OR SIGN IN WITH PASSWORD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    letterSpacing = 0.5.sp
                                )
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        AuthTextField(
                            value = loginUsernameOrEmail,
                            onValueChange = {
                                loginUsernameOrEmail = it
                                errorMessage = null
                            },
                            placeholder = "Username or Email",
                            icon = Icons.Outlined.Person,
                            keyboardType = KeyboardType.Email,
                            validationStatus = if (loginUsernameOrEmail.isNotBlank() && loginUsernameOrEmail.trim().length < 3) FieldValidationStatus.INVALID_FORMAT else FieldValidationStatus.IDLE,
                            errorMessage = if (loginUsernameOrEmail.isNotBlank() && loginUsernameOrEmail.trim().length < 3) "Enter a valid username or email" else null,
                            forceShowError = forceShowValidationErrors
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        AuthTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                errorMessage = null
                            },
                            placeholder = "Password",
                            icon = Icons.Outlined.Lock,
                            keyboardType = KeyboardType.Password,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            validationStatus = if (loginPassword.isNotEmpty() && loginPassword.length < 4) FieldValidationStatus.INVALID_FORMAT else FieldValidationStatus.IDLE,
                            errorMessage = if (loginPassword.isNotEmpty() && loginPassword.length < 4) "Password must be at least 4 characters" else null,
                            forceShowError = forceShowValidationErrors
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = gradientColors.first(),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Keystore 256-bit AES",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "Forgot Password?",
                                color = gradientColors.first(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        showForgotPasswordDialog = true
                                    }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Action Button Validation State
            val isFormSubmittable = if (isSignUp) {
                email.isNotBlank() &&
                username.isNotBlank() &&
                password.length >= 4 &&
                isEmailOtpVerified &&
                (calculatedAge != null && (calculatedAge ?: 0) >= 18) &&
                agreedToTerms &&
                usernameValidation != FieldValidationStatus.TAKEN &&
                usernameValidation != FieldValidationStatus.INVALID_FORMAT &&
                emailValidation != FieldValidationStatus.TAKEN &&
                emailValidation != FieldValidationStatus.INVALID_FORMAT
            } else {
                loginUsernameOrEmail.isNotBlank() && loginPassword.isNotBlank()
            }

            // ===== MAIN ACTION BUTTON =====
            Button(
                onClick = {
                    if (isLoading) return@Button
                    errorMessage = null
                    forceShowValidationErrors = true

                    if (isSignUp) {
                        val cleanEmail = email.trim()
                        val cleanUsername = username.trim()
                        val cleanDisplayName = displayName.trim()

                        if (cleanEmail.isBlank() || !cleanEmail.matches(emailPattern)) {
                            errorMessage = "Please enter a valid email address (e.g. name@example.com)."
                            return@Button
                        }
                        if (!isEmailOtpVerified) {
                            errorMessage = "Please verify your email address with the OTP code first."
                            return@Button
                        }
                        if (cleanUsername.length < 3) {
                            errorMessage = "Username must be at least 3 characters."
                            return@Button
                        }
                        if (cleanUsername.contains(" ")) {
                            errorMessage = "Username cannot contain spaces."
                            return@Button
                        }
                        if (!cleanUsername.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                            errorMessage = "Username may only contain letters, numbers, and underscores."
                            return@Button
                        }
                        if (usernameValidation == FieldValidationStatus.TAKEN) {
                            errorMessage = "Username is already taken. Please choose another."
                            return@Button
                        }
                        if (emailValidation == FieldValidationStatus.TAKEN) {
                            errorMessage = "Email is already registered. Please sign in."
                            return@Button
                        }
                        if (dateOfBirth.isBlank()) {
                            errorMessage = "Please enter your date of birth (YYYY-MM-DD)."
                            return@Button
                        }
                        if (calculatedAge == null || (calculatedAge ?: 0) < 18) {
                            errorMessage = "You must be at least 18 years old to join Connecto."
                            return@Button
                        }
                        if (!agreedToTerms) {
                            errorMessage = "Please agree to the Terms and Conditions to create an account."
                            return@Button
                        }
                        if (password.length < 4) {
                            errorMessage = "Password must be at least 4 characters."
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            val result = ConnectoApiClient.signup(
                                email = cleanEmail,
                                username = cleanUsername,
                                displayName = cleanDisplayName.ifEmpty { cleanUsername },
                                password = password,
                                dateOfBirth = dateOfBirth.trim(),
                                age = calculatedAge,
                                agreedToTerms = true,
                                otpCode = emailOtpCode.trim().ifEmpty { null }
                            )
                            isLoading = false
                            if (result.isSuccess) {
                                val user = result.getOrNull()
                                val passedName = user?.username ?: cleanUsername
                                onSignUpClick(cleanEmail, cleanUsername, cleanDisplayName, password)
                                onAuthSuccess(passedName)
                            } else {
                                val err = result.exceptionOrNull()?.message ?: "Sign up failed. Please try again."
                                errorMessage = sanitizeAuthError(err)
                            }
                        }
                    } else {
                        val cleanLogin = loginUsernameOrEmail.trim().removePrefix("@")
                        val cleanPassword = loginPassword.trim()
                        if (cleanLogin.isBlank()) {
                            errorMessage = "Please enter your username or email."
                            return@Button
                        }
                        if (cleanPassword.isBlank()) {
                            errorMessage = "Please enter your password."
                            return@Button
                        }

                        isLoading = true
                        coroutineScope.launch {
                            val result = ConnectoApiClient.login(
                                loginIdentifier = cleanLogin,
                                password = cleanPassword
                            )
                            isLoading = false
                            if (result.isSuccess) {
                                val user = result.getOrNull()
                                if (user != null && user.requires2Fa) {
                                    pending2FaLogin = cleanLogin
                                    pending2FaMaskedDest = user.maskedDestination
                                    pending2FaMethod = user.twoFactorMethod ?: "email"
                                    show2FaLoginDialog = true
                                } else {
                                    val passedName = user?.username ?: cleanLogin
                                    onSignInClick(cleanLogin, loginPassword)
                                    onAuthSuccess(passedName)
                                }
                            } else {
                                val err = result.exceptionOrNull()?.message ?: "Invalid username/email or password."
                                errorMessage = sanitizeAuthError(err)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                enabled = !isLoading && isFormSubmittable
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isFormSubmittable) Brush.horizontalGradient(gradientColors)
                                    else Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = contentOnGradient,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isFormSubmittable) contentOnGradient else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isSignUp) "CREATE ACCOUNT & CHAT" else "SIGN IN & CHAT",
                                color = if (isFormSubmittable) contentOnGradient else Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ===== FEATURE CHIPS =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureChip(icon = Icons.AutoMirrored.Outlined.Chat, label = "WebSocket Chat")
                FeatureChip(icon = Icons.Outlined.Security, label = "Encrypted Security")
                FeatureChip(icon = Icons.Outlined.Call, label = "WebRTC Voice")
            }
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            initialIdentifier = loginUsernameOrEmail.trim(),
            onDismiss = { showForgotPasswordDialog = false },
            onSuccess = { authResult ->
                showForgotPasswordDialog = false
                onAuthSuccess(authResult.username)
            }
        )
    }

    if (show2FaLoginDialog) {
        TwoFactorLoginDialog(
            loginIdentifier = pending2FaLogin,
            maskedDestination = pending2FaMaskedDest,
            twoFactorMethod = pending2FaMethod,
            onDismiss = { show2FaLoginDialog = false },
            onSuccess = { authResult ->
                show2FaLoginDialog = false
                onAuthSuccess(authResult.username)
            }
        )
    }

    if (showLegalTermsDialog) {
        LegalTermsDialog(
            initialTab = legalTermsInitialTab,
            onDismiss = { showLegalTermsDialog = false },
            onAccept = {
                agreedToTerms = true
                showLegalTermsDialog = false
            }
        )
    }
}

// ===== VALIDATION STATUS BADGE =====

@Composable
private fun ValidationStatusBadge(
    status: FieldValidationStatus,
    message: String
) {
    AnimatedVisibility(
        visible = status != FieldValidationStatus.IDLE && message.isNotBlank(),
        enter = fadeIn(tween(180)) + expandVertically(tween(180)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
    ) {
        val (bgColor, borderColor, contentColor, icon) = when (status) {
            FieldValidationStatus.VALID_AVAILABLE -> Quadruple(
                ConnectoTheme.colors.success.copy(alpha = 0.15f),
                ConnectoTheme.colors.success.copy(alpha = 0.6f),
                ConnectoTheme.colors.success,
                Icons.Default.CheckCircle
            )
            FieldValidationStatus.TAKEN, FieldValidationStatus.INVALID_FORMAT -> Quadruple(
                Color(0xFF7F1D1D).copy(alpha = 0.25f),
                Color(0xFFEF4444).copy(alpha = 0.6f),
                Color(0xFFF87171),
                Icons.Default.Close
            )
            FieldValidationStatus.CHECKING -> Quadruple(
                Color(0xFF0C4A6E).copy(alpha = 0.25f),
                Color(0xFF38BDF8).copy(alpha = 0.5f),
                Color(0xFF38BDF8),
                null
            )
            FieldValidationStatus.IDLE -> Quadruple(
                Color.Transparent,
                Color.Transparent,
                Color.Transparent,
                null
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, start = 4.dp, end = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (status == FieldValidationStatus.CHECKING) {
                    CircularProgressIndicator(
                        color = contentColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message,
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ===== REUSABLE COMPONENTS =====

@Composable
private fun ConnectoLogo() {
    Box(
        modifier = Modifier
            .size(92.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                    )
                ),
                shape = CircleShape
            )
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.connecto_app_icon),
            contentDescription = "Connecto Logo",
            modifier = Modifier.fillMaxSize().clip(CircleShape)
        )
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    validationStatus: FieldValidationStatus = FieldValidationStatus.IDLE,
    errorMessage: String? = null,
    forceShowError: Boolean = false,
    onBlur: (() -> Unit)? = null
) {
    var hasEverBeenFocused by remember { mutableStateOf(false) }
    var hasLeftField by remember { mutableStateOf(false) }

    val isErrorState = validationStatus == FieldValidationStatus.TAKEN || validationStatus == FieldValidationStatus.INVALID_FORMAT
    val shouldShowError = (hasLeftField || forceShowError) && isErrorState

    val dynamicBorderColor = when {
        shouldShowError -> Color(0xFFEF4444)
        validationStatus == FieldValidationStatus.VALID_AVAILABLE -> ConnectoTheme.colors.success
        validationStatus == FieldValidationStatus.CHECKING -> Color(0xFF38BDF8)
        else -> MaterialTheme.colorScheme.outline
    }

    val dynamicFocusedBorderColor = when {
        shouldShowError -> Color(0xFFEF4444)
        validationStatus == FieldValidationStatus.VALID_AVAILABLE -> ConnectoTheme.colors.success
        validationStatus == FieldValidationStatus.CHECKING -> Color(0xFF38BDF8)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        hasEverBeenFocused = true
                    } else if (hasEverBeenFocused) {
                        hasLeftField = true
                        onBlur?.invoke()
                    }
                },
            placeholder = {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = when {
                        shouldShowError -> Color(0xFFEF4444)
                        validationStatus == FieldValidationStatus.VALID_AVAILABLE -> ConnectoTheme.colors.success
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onTogglePassword?.invoke() }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (validationStatus == FieldValidationStatus.CHECKING) {
                {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (validationStatus == FieldValidationStatus.VALID_AVAILABLE) {
                {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = ConnectoTheme.colors.success,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (shouldShowError) {
                {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Error",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = dynamicFocusedBorderColor,
                unfocusedBorderColor = dynamicBorderColor,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        AnimatedVisibility(
            visible = shouldShowError && !errorMessage.isNullOrBlank(),
            enter = fadeIn(tween(180)) + expandVertically(tween(180)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage ?: "",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun FeatureChip(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
