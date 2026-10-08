package com.example.connecto.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.connecto.network.ConnectoApiClient
import kotlinx.coroutines.launch

/**
 * Top warning banner displayed when an existing user is in the 7-day grace period.
 */
@Composable
fun EmailGracePeriodBanner(
    daysLeft: Int?,
    onVerifyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayDays = daysLeft ?: 7
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFEAB308).copy(alpha = 0.22f),
                        Color(0xFFF97316).copy(alpha = 0.28f)
                    )
                )
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Verify Email Address ($displayDays day${if (displayDays == 1) "" else "s"} left)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEF08A)
                    )
                    Text(
                        text = "Old accounts require email verification within 7 days.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF59E0B))
                    .clickable { onVerifyClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Verify ⚡",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

/**
 * Fullscreen non-dismissible modal when account is temporarily blocked
 * due to expiration of 7-day grace period, OR opened manually via banner.
 */
@Composable
fun EmailVerificationModal(
    isBlocked: Boolean,
    daysLeft: Int?,
    onDismiss: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var otpCode by rememberSaveable { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var otpSent by rememberSaveable { mutableStateOf(false) }
    var statusMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!isBlocked) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isBlocked,
            dismissOnClickOutside = !isBlocked,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (isBlocked) Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)))
                            else Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF3B82F6)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBlocked) Icons.Filled.Lock else Icons.Filled.MarkEmailRead,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isBlocked) "Account Temporarily Blocked" else "Verify Your Email",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isBlocked)
                        "Your 7-day grace period has expired. Please verify your registered email address with an OTP code to unlock your account."
                    else
                        "You have ${daysLeft ?: 7} day${if ((daysLeft ?: 7) == 1) "" else "s"} remaining to verify your registered email address. Verify now to prevent your account from being temporarily blocked.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Request OTP Button or Trigger
                if (!otpSent) {
                    Button(
                        onClick = {
                            isSendingOtp = true
                            isError = false
                            statusMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.requestEmailVerificationOtp()
                                isSendingOtp = false
                                if (res.isSuccess) {
                                    otpSent = true
                                    statusMessage = res.getOrThrow()
                                } else {
                                    isError = true
                                    statusMessage = res.exceptionOrNull()?.message ?: "Failed to send code"
                                }
                            }
                        },
                        enabled = !isSendingOtp,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isSendingOtp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sending Code...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Text("Send 6-Digit Code ✉️", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    // OTP Input Field
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it },
                        label = { Text("6-Digit OTP Code") },
                        placeholder = { Text("123456") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (otpCode.trim().length != 6) {
                                isError = true
                                statusMessage = "Please enter the complete 6-digit code"
                                return@Button
                            }
                            isVerifying = true
                            isError = false
                            statusMessage = null
                            coroutineScope.launch {
                                val res = ConnectoApiClient.confirmEmailVerification(otpCode.trim())
                                isVerifying = false
                                if (res.isSuccess && res.getOrThrow()) {
                                    isError = false
                                    statusMessage = "Email verified successfully! ✓"
                                    onDismiss()
                                } else {
                                    isError = true
                                    statusMessage = res.exceptionOrNull()?.message ?: "Invalid verification code"
                                }
                            }
                        },
                        enabled = !isVerifying && otpCode.trim().length == 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying...", fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Text("Confirm & Unlock ➔", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resend link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Didn't receive code? Resend",
                            fontSize = 12.sp,
                            color = Color(0xFF818CF8),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    coroutineScope.launch {
                                        val res = ConnectoApiClient.requestEmailVerificationOtp()
                                        if (res.isSuccess) {
                                            isError = false
                                            statusMessage = "New verification code sent!"
                                        } else {
                                            isError = true
                                            statusMessage = res.exceptionOrNull()?.message ?: "Failed to resend"
                                        }
                                    }
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // Status / Error message display
                statusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isError) Color(0xFFEF4444) else Color(0xFF10B981),
                        textAlign = TextAlign.Center
                    )
                }

                // If not blocked, allow dismissing
                if (!isBlocked) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "I'll do this later",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}
