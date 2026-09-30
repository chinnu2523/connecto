package com.example.connecto.ui.auth

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

@Composable
fun TwoFactorLoginDialog(
    loginIdentifier: String,
    maskedDestination: String?,
    twoFactorMethod: String = "email",
    devOtp: String? = null,
    onDismiss: () -> Unit,
    onSuccess: (AuthResult) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isLight = isAppInLightTheme()
    val gradientColors = getDynamicAccentGradientColors()
    val contentOnGradient = getContentColorOnAccentGradient()

    var otpCode by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // REAL-TIME VERIFY WHEN 6 DIGITS ENTERED
    LaunchedEffect(otpCode) {
        val cleanOtp = otpCode.trim()
        if (cleanOtp.length == 6 && !isVerifying) {
            isVerifying = true
            errorMessage = null
            coroutineScope.launch {
                val res = ConnectoApiClient.verifyLogin2Fa(
                    loginIdentifier = loginIdentifier,
                    otpCode = cleanOtp
                )
                isVerifying = false
                if (res.isSuccess) {
                    onSuccess(res.getOrThrow())
                } else {
                    errorMessage = res.exceptionOrNull()?.message ?: "Invalid or expired 2FA code."
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
                // Header
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
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = contentOnGradient,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Two-Factor Auth",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = "2FA Security Challenge",
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

                val destText = if (!maskedDestination.isNullOrBlank()) {
                    "sent to $maskedDestination"
                } else {
                    "sent to your registered email"
                }

                Text(
                    text = "A 6-digit verification code was $destText.\nLook for the email titled '[Connecto 2FA Sign-In]'.",
                    fontSize = 13.sp,
                    color = subtitleColor,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

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

                // 6-digit OTP Input
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
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = gradientColors.first())
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

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (otpCode.length < 6) {
                            errorMessage = "Please enter the 6-digit code."
                            return@Button
                        }
                        isVerifying = true
                        errorMessage = null
                        coroutineScope.launch {
                            val res = ConnectoApiClient.verifyLogin2Fa(
                                loginIdentifier = loginIdentifier,
                                otpCode = otpCode.trim()
                            )
                            isVerifying = false
                            if (res.isSuccess) {
                                onSuccess(res.getOrThrow())
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "2FA verification failed."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = !isVerifying && otpCode.length == 6
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(Brush.horizontalGradient(gradientColors), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = contentOnGradient, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        } else {
                            Text("VERIFY & LOG IN", color = contentOnGradient, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
