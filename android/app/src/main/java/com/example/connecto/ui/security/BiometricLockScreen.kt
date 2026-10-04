package com.example.connecto.ui.security

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.security.BiometricAuthManager
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import kotlinx.coroutines.launch

/**
 * Traverses context wrappers to find the parent FragmentActivity.
 */
private fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun BiometricLockScreen(
    username: String,
    onUnlockSuccess: () -> Unit,
    onPasswordUnlock: () -> Unit = onUnlockSuccess,
    onSwitchAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()
    val coroutineScope = rememberCoroutineScope()

    var authStatusMessage by remember {
        mutableStateOf("Scan fingerprint, use device PIN, or enter account password")
    }
    var isAuthFailed by remember { mutableStateOf(false) }
    var isAuthSuccess by remember { mutableStateOf(false) }

    // Account Password Unlock Fallback state
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVerifying by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val userInitial = if (username.isNotBlank()) username.first().toString().uppercase() else "C"

    // Pulse animation for biometric icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Android Native Device Screen Lock (PIN, Pattern, Password) Activity Launcher
    val deviceCredentialLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isAuthSuccess = true
            isAuthFailed = false
            authStatusMessage = "Device screen lock verified! 🔓"
            onUnlockSuccess()
        } else {
            isAuthFailed = true
            authStatusMessage = "Screen lock verification canceled or failed."
        }
    }

    fun triggerScreenLockPrompt() {
        try {
            // API 30+: BiometricPrompt with DEVICE_CREDENTIAL handles screen lock natively
            // (KeyguardManager.createConfirmDeviceCredentialIntent is deprecated since API 29 and returns null on API 30+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && activity != null) {
                val executor = ContextCompat.getMainExecutor(activity)
                val callback = object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                        isAuthSuccess = true
                        isAuthFailed = false
                        authStatusMessage = "Device screen lock verified! 🔓"
                        onUnlockSuccess()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                            errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            isAuthFailed = true
                            authStatusMessage = "$errString. Unlock with Connecto password below."
                        }
                    }
                    override fun onAuthenticationFailed() {
                        isAuthFailed = true
                        authStatusMessage = "Screen lock not recognized. Please try again."
                    }
                }
                val prompt = androidx.biometric.BiometricPrompt(activity, executor, callback)
                val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Connecto")
                    .setSubtitle("Enter device PIN, pattern, or password")
                    .setAllowedAuthenticators(BiometricAuthManager.ALLOWED_AUTHENTICATORS_API30)
                    .build()
                authStatusMessage = "Confirming device screen lock..."
                isAuthFailed = false
                prompt.authenticate(promptInfo)
                return
            }

            // API 24-29: Use KeyguardManager intent (deprecated but functional)
            val intent = BiometricAuthManager.createConfirmDeviceCredentialIntent(
                context = context,
                title = "Unlock Connecto",
                description = "Enter your device PIN, pattern, or password to access Connecto"
            )
            if (intent != null) {
                authStatusMessage = "Confirming device screen lock..."
                isAuthFailed = false
                deviceCredentialLauncher.launch(intent)
            } else {
                authStatusMessage = "Device screen lock not available. Unlock with account password."
                showPasswordDialog = true
            }
        } catch (t: Throwable) {
            authStatusMessage = "Could not open screen lock: ${t.localizedMessage}"
            showPasswordDialog = true
        }
    }

    fun triggerBiometricPrompt() {
        val isBioEnrolled = BiometricAuthManager.isBiometricEnrolled(context)
        val isSecure = BiometricAuthManager.isDeviceSecure(context)

        if (activity == null) {
            if (isSecure) triggerScreenLockPrompt() else showPasswordDialog = true
            return
        }

        if (!isBioEnrolled && !isSecure) {
            authStatusMessage = "No biometrics on device. Tap below to unlock with Connecto password."
            showPasswordDialog = true
            return
        }

        // On older Android without enrolled biometrics, directly trigger screen lock or password
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R && !isBioEnrolled) {
            if (isSecure) triggerScreenLockPrompt() else showPasswordDialog = true
            return
        }

        try {
            authStatusMessage = "Waiting for biometric scan or screen lock..."
            isAuthFailed = false

            BiometricAuthManager.authenticate(
                activity = activity,
                title = "Unlock Connecto",
                subtitle = "Verify identity for @$username",
                description = "Scan fingerprint, face, or enter device screen lock to unlock Connecto",
                onSuccess = {
                    isAuthSuccess = true
                    isAuthFailed = false
                    authStatusMessage = "Identity verified successfully! 🔓"
                    onUnlockSuccess()
                },
                onError = { errorCode, errString ->
                    if (errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        // Negative button tapped -> open password dialog or screen lock
                        if (isSecure) triggerScreenLockPrompt() else showPasswordDialog = true
                    } else if (errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED) {
                        isAuthFailed = true
                        authStatusMessage = "$errString. You can also unlock with your password below."
                    }
                },
                onFailed = {
                    isAuthFailed = true
                    authStatusMessage = "Biometric not recognized. Please try again or unlock with password."
                },
                onFallbackToDeviceCredential = {
                    if (isSecure) triggerScreenLockPrompt() else showPasswordDialog = true
                }
            )
        } catch (t: Throwable) {
            isAuthFailed = true
            authStatusMessage = "Biometric unavailable. Unlock using screen lock or password below."
            if (isSecure) triggerScreenLockPrompt() else showPasswordDialog = true
        }
    }

    // Automatically trigger authentication on launch
    LaunchedEffect(Unit) {
        try {
            val isBioEnrolled = BiometricAuthManager.isBiometricEnrolled(context)
            val isSecure = BiometricAuthManager.isDeviceSecure(context)
            if (isBioEnrolled) {
                triggerBiometricPrompt()
            } else if (isSecure) {
                triggerScreenLockPrompt()
            } else {
                authStatusMessage = "Device screen lock not set. Enter Connecto password to unlock."
                showPasswordDialog = true
            }
        } catch (t: Throwable) {
            authStatusMessage = "Tap below to unlock with Connecto password"
            isAuthFailed = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // User Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitial,
                    color = contentOnGradient,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unlock Connecto",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "@$username",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Interactive Biometric Fingerprint Scanner
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .scale(if (isAuthSuccess) 1.0f else pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isAuthSuccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else if (isAuthFailed) Color(0xFFEF4444).copy(alpha = 0.20f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            if (isAuthSuccess) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
                            else if (isAuthFailed) listOf(Color(0xFFEF4444), Color(0xFFEF4444))
                            else gradientColors
                        ),
                        shape = CircleShape
                    )
                    .clickable {
                        val isBioEnrolled = BiometricAuthManager.isBiometricEnrolled(context)
                        val isSecure = BiometricAuthManager.isDeviceSecure(context)
                        if (isBioEnrolled) {
                            triggerBiometricPrompt()
                        } else if (isSecure) {
                            triggerScreenLockPrompt()
                        } else {
                            showPasswordDialog = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAuthSuccess) Icons.Default.LockOpen else Icons.Default.Fingerprint,
                    contentDescription = "Trigger Biometric Unlock",
                    tint = if (isAuthSuccess) MaterialTheme.colorScheme.primary else if (isAuthFailed) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Live Security Status Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = authStatusMessage,
                    fontSize = 13.sp,
                    color = if (isAuthSuccess) MaterialTheme.colorScheme.primary else if (isAuthFailed) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Action Button 1: SCAN BIOMETRICS / RETRY
            Button(
                onClick = { triggerBiometricPrompt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan Biometrics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button 2: UNLOCK WITH SCREEN LOCK (PIN / Pattern / Password)
            OutlinedButton(
                onClick = { triggerScreenLockPrompt() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Unlock with Device Screen Lock",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button 3: UNLOCK WITH ACCOUNT PASSWORD
            OutlinedButton(
                onClick = { showPasswordDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Enter Account Password",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Switch Account / Sign Out
            TextButton(
                onClick = onSwitchAccount
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Switch Account / Log Out",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Connecto Password Unlock Dialog
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isPasswordVerifying) {
                    showPasswordDialog = false
                    passwordInput = ""
                    passwordError = null
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Account Password", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter the password for @$username to unlock:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    if (!passwordError.isNullOrBlank()) {
                        Text(
                            text = passwordError!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Switch Account link — always accessible inside the dialog, fixing
                    // the z-order tap issue on devices without biometrics/screen lock.
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isPasswordVerifying) { onSwitchAccount() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Switch Account / Log Out",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordInput.isNotBlank() && !isPasswordVerifying) {
                            isPasswordVerifying = true
                            passwordError = null
                            coroutineScope.launch {
                                try {
                                    val res = ConnectoApiClient.login(username, passwordInput)
                                    if (res.isSuccess) {
                                        val auth = res.getOrNull()
                                        if (auth?.token != null) {
                                            try {
                                                val sp = context.getSharedPreferences("connecto_session_prefs", Context.MODE_PRIVATE)
                                                sp.edit()
                                                    .putString("token", auth.token)
                                                    .putString("user_id", auth.id)
                                                    .putString("username", auth.username)
                                                    .apply()
                                            } catch (_: Exception) {}
                                        }
                                        showPasswordDialog = false
                                        isAuthSuccess = true
                                        onPasswordUnlock()
                                    } else {
                                        passwordError = res.exceptionOrNull()?.message ?: "Incorrect password"
                                    }
                                } catch (e: Exception) {
                                    passwordError = "Verification error: ${e.message}"
                                } finally {
                                    isPasswordVerifying = false
                                }
                            }
                        }
                    },
                    enabled = passwordInput.isNotBlank() && !isPasswordVerifying,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isPasswordVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying...")
                    } else {
                        Text("Unlock", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        passwordInput = ""
                        passwordError = null
                    },
                    enabled = !isPasswordVerifying
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
