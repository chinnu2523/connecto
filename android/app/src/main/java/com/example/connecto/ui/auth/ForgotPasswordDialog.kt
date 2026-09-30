package com.example.connecto.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.connecto.network.AuthResult
import com.example.connecto.network.ConnectoApiClient
import com.example.connecto.ui.theme.getContentColorOnAccentGradient
import com.example.connecto.ui.theme.getDynamicAccentGradientColors
import com.example.connecto.ui.theme.isAppInLightTheme
import kotlinx.coroutines.launch

private enum class ForgotPasswordStep {
    IDENTIFIER_AND_METHOD,
    OTP_VERIFICATION,
    NEW_PASSWORD
}

@Composable
fun ForgotPasswordDialog(
    initialIdentifier: String = "",
    onDismiss: () -> Unit,
    onSuccess: (AuthResult) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isLight = isAppInLightTheme()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    var currentStep by remember { mutableStateOf(ForgotPasswordStep.IDENTIFIER_AND_METHOD) }
    var identifier by remember { mutableStateOf(initialIdentifier) }
    var selectedMethod by remember { mutableStateOf("email") }

    var otpCode by remember { mutableStateOf("") }
    var maskedDestination by remember { mutableStateOf<String?>(null) }
    var isOtpVerified by remember { mutableStateOf(false) }

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // REAL-TIME OTP AUTO-VERIFICATION TRIGGER
    LaunchedEffect(otpCode) {
        val cleanOtp = otpCode.trim()
        if (cleanOtp.length == 6 && !isOtpVerified && !isVerifyingOtp && currentStep == ForgotPasswordStep.OTP_VERIFICATION) {
            isVerifyingOtp = true
            errorMessage = null
            coroutineScope.launch {
                val res = ConnectoApiClient.verifyForgotPasswordOtp(
                    identifier = identifier.trim(),
                    otpCode = cleanOtp
                )
                isVerifyingOtp = false
                if (res.isSuccess && res.getOrNull()?.verified == true) {
                    isOtpVerified = true
                    statusMessage = "✓ Verification code verified! Please enter your new password."
                    currentStep = ForgotPasswordStep.NEW_PASSWORD
                } else {
                    errorMessage = res.exceptionOrNull()?.message ?: "Invalid or expired code. Please check and try again."
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        val dialogBg = if (isLight) Color(0xFFFFFFFF) else Color(0xFF000000)
        val cardBorder = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E1E24)
        val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
        val subtitleColor = if (isLight) Color(0xFF64748B) else Color(0xFF94A3B8)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(dialogBg)
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = contentOnGradient,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Reset Password",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = when (currentStep) {
                                    ForgotPasswordStep.IDENTIFIER_AND_METHOD -> "Step 1 of 3: Choose method"
                                    ForgotPasswordStep.OTP_VERIFICATION -> "Step 2 of 3: Real-time OTP"
                                    ForgotPasswordStep.NEW_PASSWORD -> "Step 3 of 3: New password"
                                },
                                fontSize = 12.sp,
                                color = subtitleColor
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = subtitleColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Error Banner
                if (errorMessage != null) {
                    val errorBg = if (isLight) Color(0xFFFEE2E2) else Color(0xFF7F1D1D).copy(alpha = 0.35f)
                    val errorText = if (isLight) Color(0xFFDC2626) else Color(0xFFFCA5A5)
                    val errorBorder = if (isLight) Color(0xFFFCA5A5) else Color(0xFFEF4444).copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(errorBg)
                            .border(1.dp, errorBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = errorText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Success/Status Banner
                if (statusMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // STEP 1: IDENTIFIER AND METHOD
                if (currentStep == ForgotPasswordStep.IDENTIFIER_AND_METHOD) {
                    Text(
                        text = "Enter your username or registered email address to receive a 6-digit recovery code.",
                        fontSize = 13.sp,
                        color = subtitleColor,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            errorMessage = null
                        },
                        placeholder = { Text("Username or Email", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = subtitleColor)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gradientColors.first(),
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Recovery Information Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Email OTP Recovery",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "A one-time verification code will be sent to your inbox",
                                    fontSize = 11.sp,
                                    color = subtitleColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (identifier.isBlank()) {
                                errorMessage = "Please enter your username or email address."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.requestForgotPasswordOtp(
                                    identifier = identifier.trim(),
                                    method = "email"
                                )
                                isLoading = false
                                if (res.isSuccess) {
                                    val data = res.getOrNull()
                                    maskedDestination = data?.maskedDestination
                                    statusMessage = data?.message ?: "Verification code dispatched to your email!"
                                    currentStep = ForgotPasswordStep.OTP_VERIFICATION
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Failed to send verification code."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !isLoading && identifier.isNotBlank()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Brush.horizontalGradient(gradientColors), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = contentOnGradient, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Text("SEND RECOVERY CODE", color = contentOnGradient, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // STEP 2: REAL-TIME OTP VERIFICATION
                if (currentStep == ForgotPasswordStep.OTP_VERIFICATION) {
                    Text(
                        text = "A 6-digit recovery code has been sent to ${maskedDestination ?: "your registered email"}.\nLook for the email titled '[Connecto Password Reset]'.",
                        fontSize = 13.sp,
                        color = subtitleColor,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = {
                            if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                otpCode = it
                                errorMessage = null
                            }
                        },
                        placeholder = { Text("000000", fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Security, contentDescription = null, tint = gradientColors.first())
                        },
                        trailingIcon = {
                            if (isVerifyingOtp) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = gradientColors.first())
                            } else if (isOtpVerified) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            letterSpacing = 6.sp,
                            color = textColor
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gradientColors.first(),
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Didn't receive code?",
                            fontSize = 12.sp,
                            color = subtitleColor
                        )

                        Text(
                            text = "Resend Code",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = gradientColors.first(),
                            modifier = Modifier
                                .clickable {
                                    otpCode = ""
                                    errorMessage = null
                                    coroutineScope.launch {
                                        val res = ConnectoApiClient.requestForgotPasswordOtp(identifier.trim(), selectedMethod)
                                        if (res.isSuccess) {
                                            statusMessage = "New code sent!"
                                        } else {
                                            errorMessage = res.exceptionOrNull()?.message ?: "Failed to resend."
                                        }
                                    }
                                }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (otpCode.length < 6) {
                                errorMessage = "Please enter the 6-digit verification code."
                                return@Button
                            }
                            isVerifyingOtp = true
                            errorMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.verifyForgotPasswordOtp(
                                    identifier = identifier.trim(),
                                    otpCode = otpCode.trim()
                                )
                                isVerifyingOtp = false
                                if (res.isSuccess && res.getOrNull()?.verified == true) {
                                    isOtpVerified = true
                                    currentStep = ForgotPasswordStep.NEW_PASSWORD
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Invalid code."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !isVerifyingOtp && otpCode.length == 6
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Brush.horizontalGradient(gradientColors), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVerifyingOtp) {
                                CircularProgressIndicator(color = contentOnGradient, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Text("VERIFY CODE", color = contentOnGradient, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // STEP 3: NEW PASSWORD CREATION
                if (currentStep == ForgotPasswordStep.NEW_PASSWORD) {
                    Text(
                        text = "Enter a new secure password for your account.",
                        fontSize = 13.sp,
                        color = subtitleColor,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        placeholder = { Text("New Password (min 4 chars)", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = subtitleColor)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    tint = subtitleColor
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gradientColors.first(),
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        placeholder = { Text("Confirm New Password", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = subtitleColor)
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gradientColors.first(),
                            unfocusedBorderColor = cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (newPassword.length < 4) {
                                errorMessage = "Password must be at least 4 characters long."
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                errorMessage = "Passwords do not match."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.resetPasswordWithOtp(
                                    identifier = identifier.trim(),
                                    otpCode = otpCode.trim(),
                                    newPassword = newPassword
                                )
                                isLoading = false
                                if (res.isSuccess) {
                                    val authResult = res.getOrThrow()
                                    onSuccess(authResult)
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Failed to reset password."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !isLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(Brush.horizontalGradient(gradientColors), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = contentOnGradient, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                            } else {
                                Text("RESET PASSWORD & SIGN IN", color = contentOnGradient, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
