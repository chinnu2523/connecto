package com.connecto.store.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.connecto.store.ui.components.StoreButtonState
import com.connecto.store.ui.components.StoreFiveStateButton
import com.connecto.store.viewmodel.LoginUiState

enum class StoreAuthMode {
    SIGN_IN,
    CREATE_ACCOUNT
}

@Composable
fun StoreLoginScreen(
    loginState: LoginUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLoginCredentials: (username: String, pass: String) -> Unit,
    onRegisterCredentials: (username: String, pass: String, displayName: String, email: String) -> Unit,
    onLoginConnecto: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onBiometricLogin: () -> Unit,
    isBiometricAvailable: Boolean,
    onClearLoginError: () -> Unit
) {
    val theme = LocalStoreTheme.current
    val focusManager = LocalFocusManager.current

    var authMode by remember { mutableStateOf(StoreAuthMode.SIGN_IN) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showLegalDialog by remember { mutableStateOf<Int?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showReferDialog by remember { mutableStateOf(false) }

    val isLoading = loginState is LoginUiState.Loading
    val isError = loginState is LoginUiState.Error

    val canSubmit = when (authMode) {
        StoreAuthMode.SIGN_IN -> username.trim().isNotBlank() && password.trim().isNotBlank()
        StoreAuthMode.CREATE_ACCOUNT -> username.trim().length >= 3 && password.trim().length >= 8
    }

    val primaryButtonState = when {
        isLoading -> StoreButtonState.LOADING
        isError -> StoreButtonState.ERROR
        !canSubmit -> StoreButtonState.DISABLED
        else -> StoreButtonState.DEFAULT
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar: Clean Brand Header (Theme Toggle managed directly in Account Tab)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OurStoreLogoMark(modifier = Modifier.size(28.dp))
                Text(
                    text = "OUR STORE",
                    color = theme.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        // Center Scrollable Authentication Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 70.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Brand Presentation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OurStoreLogoMark(modifier = Modifier.size(72.dp))

                Text(
                    text = if (authMode == StoreAuthMode.SIGN_IN) "Welcome Back" else "Create Account",
                    color = theme.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Curated Autonomous Ecosystem & Verified Apps",
                    color = theme.textMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Mode Switcher Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.cardElevated)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Sign In Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (authMode == StoreAuthMode.SIGN_IN) theme.surface else Color.Transparent)
                        .then(
                            if (authMode == StoreAuthMode.SIGN_IN) {
                                Modifier.border(1.dp, theme.cardBorder, RoundedCornerShape(9.dp))
                            } else Modifier
                        )
                        .clickable {
                            if (authMode != StoreAuthMode.SIGN_IN) {
                                authMode = StoreAuthMode.SIGN_IN
                                onClearLoginError()
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SIGN IN",
                        color = if (authMode == StoreAuthMode.SIGN_IN) theme.textPrimary else theme.textDarkMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Create Account Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (authMode == StoreAuthMode.CREATE_ACCOUNT) theme.surface else Color.Transparent)
                        .then(
                            if (authMode == StoreAuthMode.CREATE_ACCOUNT) {
                                Modifier.border(1.dp, theme.cardBorder, RoundedCornerShape(9.dp))
                            } else Modifier
                        )
                        .clickable {
                            if (authMode != StoreAuthMode.CREATE_ACCOUNT) {
                                authMode = StoreAuthMode.CREATE_ACCOUNT
                                onClearLoginError()
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NEW ACCOUNT",
                        color = if (authMode == StoreAuthMode.CREATE_ACCOUNT) theme.textPrimary else theme.textDarkMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Error Alert Banner
            AnimatedVisibility(
                visible = isError,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (loginState is LoginUiState.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E1010))
                            .border(1.dp, Color(0xFF7F1D1D), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = loginState.message,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onClearLoginError() }
                            )
                        }
                    }
                }
            }

            // Input Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(theme.surface)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (isError) onClearLoginError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Username or Email", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = theme.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (username.isNotEmpty()) {
                                IconButton(onClick = { username = "" }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = theme.textDarkMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = if (authMode == StoreAuthMode.CREATE_ACCOUNT) ImeAction.Next else ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedBorderColor = theme.accentColor,
                            unfocusedBorderColor = theme.cardBorder,
                            focusedContainerColor = theme.cardElevated,
                            unfocusedContainerColor = theme.cardElevated,
                            focusedLabelColor = theme.accentColor,
                            unfocusedLabelColor = theme.textMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Display Name Field (Create Account mode only)
                    AnimatedVisibility(
                        visible = authMode == StoreAuthMode.CREATE_ACCOUNT,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Display Name", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Badge,
                                    contentDescription = null,
                                    tint = theme.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary,
                                focusedBorderColor = theme.accentColor,
                                unfocusedBorderColor = theme.cardBorder,
                                focusedContainerColor = theme.cardElevated,
                                unfocusedContainerColor = theme.cardElevated,
                                focusedLabelColor = theme.accentColor,
                                unfocusedLabelColor = theme.textMuted
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (isError) onClearLoginError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                if (authMode == StoreAuthMode.CREATE_ACCOUNT) "Password (min 8 chars)" else "Password",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = theme.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = theme.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (canSubmit && !isLoading) {
                                    if (authMode == StoreAuthMode.SIGN_IN) {
                                        onLoginCredentials(username, password)
                                    } else {
                                        onRegisterCredentials(username, password, displayName, "")
                                    }
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedBorderColor = theme.accentColor,
                            unfocusedBorderColor = theme.cardBorder,
                            focusedContainerColor = theme.cardElevated,
                            unfocusedContainerColor = theme.cardElevated,
                            focusedLabelColor = theme.accentColor,
                            unfocusedLabelColor = theme.textMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Primary 5-State Submit Button
                    StoreFiveStateButton(
                        text = if (authMode == StoreAuthMode.SIGN_IN) "SIGN IN TO OUR STORE" else "CREATE ACCOUNT & ENTER",
                        onClick = {
                            focusManager.clearFocus()
                            if (authMode == StoreAuthMode.SIGN_IN) {
                                onLoginCredentials(username, password)
                            } else {
                                onRegisterCredentials(username, password, displayName, "")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        state = primaryButtonState,
                        icon = if (authMode == StoreAuthMode.SIGN_IN) Icons.AutoMirrored.Rounded.Login else Icons.Rounded.PersonAdd,
                        backgroundColor = theme.accentColor,
                        contentColor = Color.White
                    )

                    // Subtle Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = theme.cardBorder,
                            thickness = 1.dp
                        )
                        Text(
                            text = "OR FAST SIGN-IN",
                            color = theme.textDarkMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = theme.cardBorder,
                            thickness = 1.dp
                        )
                    }

                    // 1-Tap Connecto SSO Button
                    StoreFiveStateButton(
                        text = "CONTINUE WITH CONNECTO SSO",
                        onClick = onLoginConnecto,
                        modifier = Modifier.fillMaxWidth(),
                        state = if (isLoading) StoreButtonState.LOADING else StoreButtonState.DEFAULT,
                        icon = Icons.Rounded.VerifiedUser,
                        backgroundColor = Color(0xFF4F46E5),
                        contentColor = Color.White
                    )

                    // Biometric Quick Sign-In Button (if device hardware available)
                    if (isBiometricAvailable) {
                        StoreFiveStateButton(
                            text = "FINGERPRINT QUICK UNLOCK",
                            onClick = onBiometricLogin,
                            modifier = Modifier.fillMaxWidth(),
                            state = StoreButtonState.DEFAULT,
                            icon = Icons.Rounded.Fingerprint,
                            backgroundColor = Color(0xFF059669),
                            contentColor = Color.White
                        )
                    }
                }
            }

            // Bottom Alternative: Explore as Guest
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onContinueAsGuest() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Explore Catalog as Guest",
                        color = theme.textMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = theme.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Legal & Support Links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Refer",
                    color = theme.textMuted.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showReferDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
                Text(
                    text = "•",
                    color = theme.textMuted.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Text(
                    text = "Help & Support",
                    color = theme.textMuted.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showHelpDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
                Text(
                    text = "•",
                    color = theme.textMuted.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Text(
                    text = "Feedback",
                    color = theme.textMuted.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showFeedbackDialog = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
                Text(
                    text = "•",
                    color = theme.textMuted.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Text(
                    text = "Terms of Use",
                    color = theme.textMuted.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showLegalDialog = 0 }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
                Text(
                    text = "•",
                    color = theme.textMuted.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                Text(
                    text = "Privacy Policy",
                    color = theme.textMuted.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showLegalDialog = 1 }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Help & Support Dialog
    if (showHelpDialog) {
        OurStoreHelpSupportDialog(
            onDismiss = { showHelpDialog = false }
        )
    }

    // Feedback & Suggestions Dialog
    if (showFeedbackDialog) {
        OurStoreFeedbackDialog(
            onDismiss = { showFeedbackDialog = false }
        )
    }

    // Refer Our Store Dialog
    if (showReferDialog) {
        OurStoreReferDialog(
            onDismiss = { showReferDialog = false }
        )
    }

    // Legal Terms & Privacy Dialog
    showLegalDialog?.let { initialTab ->
        OurStoreLegalDialog(
            initialTab = initialTab,
            onAgree = { showLegalDialog = null },
            onDismiss = { showLegalDialog = null }
        )
    }
}
