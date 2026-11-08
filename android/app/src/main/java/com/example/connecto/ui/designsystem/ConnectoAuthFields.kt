package com.example.connecto.ui.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.ui.auth.FieldValidationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

// =========================================================================
// 1. SIX-BOX OTP CODE FIELD WITH CASCADING MINT WAVE ANIMATION
// =========================================================================

/**
 * Six-box OTP Code Input Field.
 * - Displays 6 individual squircle boxes.
 * - Boxes fill one by one as the user types digits.
 * - When all 6 digits are filled, the boxes turn Mint (#10B981) in a sequential wave animation from box 1 to 6.
 * - Underlying numeric BasicTextField handles focus, backspace, and pasting.
 */
@Composable
fun ConnectoOtpCodeField(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    onComplete: ((String) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Cascading wave animatables for each of the 6 boxes (0f = initial, 1f = mint wave peak)
    val waveAnimatables = remember { List(6) { Animatable(0f) } }

    val mintColor = Color(0xFF10B981)
    val mintText = Color(0xFF34D399)

    // Trigger sequential wave animation when all 6 digits are entered
    LaunchedEffect(otpCode) {
        if (otpCode.length == 6) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            waveAnimatables.forEachIndexed { index, anim ->
                launch {
                    delay((index * 60).toLong())
                    anim.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = 0.65f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            }
            onComplete?.invoke(otpCode)
        } else {
            waveAnimatables.forEach { anim ->
                launch { anim.snapTo(0f) }
            }
        }
    }

    // Idle blinking cursor for active unfilled box
    val infiniteTransition = rememberInfiniteTransition(label = "otpCursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
            },
        contentAlignment = Alignment.Center
    ) {
        // Invisible input field capturing keystrokes, backspace, paste
        BasicTextField(
            value = TextFieldValue(
                text = otpCode,
                selection = TextRange(otpCode.length)
            ),
            onValueChange = { tfv ->
                val digits = tfv.text.filter { it.isDigit() }.take(6)
                if (digits != otpCode) {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                    onOtpChange(digits)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (otpCode.length == 6) {
                        focusManager.clearFocus()
                    }
                }
            ),
            singleLine = true,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .background(Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp)
        )

        // Row of 6 individual boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 6) {
                val digit = otpCode.getOrNull(i)
                val isFilled = digit != null
                val isCurrent = isFocused && (otpCode.length == i || (otpCode.length == 6 && i == 5))
                val waveVal = waveAnimatables[i].value

                // Dynamic wave colors and styling
                val baseBorder = when {
                    isError -> Color(0xFFEF4444)
                    isCurrent -> Color(0xFF38BDF8)
                    isFilled -> Color.White.copy(alpha = 0.70f)
                    else -> Color.White.copy(alpha = 0.16f)
                }
                val boxBorderColor = lerp(baseBorder, mintColor, waveVal)

                val baseBg = when {
                    isError -> Color(0xFFEF4444).copy(alpha = 0.08f)
                    isCurrent -> Color(0xFF38BDF8).copy(alpha = 0.07f)
                    isFilled -> Color.White.copy(alpha = 0.05f)
                    else -> Color(0xFF131722).copy(alpha = 0.85f)
                }
                val boxBgColor = lerp(baseBg, mintColor.copy(alpha = 0.18f), waveVal)

                val baseTextColor = when {
                    isError -> Color(0xFFEF4444)
                    else -> Color.White
                }
                val digitColor = lerp(baseTextColor, mintText, waveVal)

                // Micro bounce scale during wave
                val waveBounceScale = 1.0f + (0.12f * sin(waveVal * PI.toFloat())).toFloat()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .padding(horizontal = 3.dp)
                        .scale(waveBounceScale)
                        .shadow(
                            elevation = if (waveVal > 0.1f) 8.dp else if (isCurrent) 4.dp else 0.dp,
                            shape = RoundedCornerShape(13.dp),
                            spotColor = if (waveVal > 0.1f) mintColor.copy(alpha = 0.6f * waveVal) else Color(0xFF38BDF8).copy(alpha = 0.3f),
                            ambientColor = Color.Black.copy(alpha = 0.5f)
                        )
                        .clip(RoundedCornerShape(13.dp))
                        .background(boxBgColor)
                        .border(
                            width = if (waveVal > 0f || isCurrent || isError) 1.8.dp else 1.2.dp,
                            color = boxBorderColor,
                            shape = RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFilled) {
                        Text(
                            text = digit.toString(),
                            color = digitColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    } else if (isCurrent) {
                        // Pulsing cursor bar inside current unfilled box
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = cursorAlpha))
                        )
                    } else {
                        // Subtle hollow placeholder dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. PASSWORD STRENGTH METER (FOUR-STEP METER FILLS AND RECOLOURS)
// =========================================================================

data class PasswordStrength(
    val score: Int, // 0 to 4
    val label: String,
    val color: Color,
    val hint: String
)

/**
 * Computes granular password security strength score from 0 (empty) to 4 (strong).
 */
fun evaluatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) {
        return PasswordStrength(0, "Empty", Color.Transparent, "Enter at least 6 characters")
    }

    var score = 0
    val hasMinLength = password.length >= 6
    val hasGoodLength = password.length >= 8
    val hasGreatLength = password.length >= 10
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    if (hasMinLength) score++
    if (hasGoodLength && (hasDigit || hasUppercase)) score++
    if ((hasUppercase && hasLowercase && hasDigit) || (hasGreatLength && hasDigit)) score++
    if (hasGreatLength && hasUppercase && hasLowercase && hasDigit && hasSpecial) {
        score = 4
    } else if (password.length >= 12 && score >= 3) {
        score = 4
    }

    score = score.coerceIn(1, 4)

    return when (score) {
        1 -> PasswordStrength(
            score = 1,
            label = "Weak",
            color = Color(0xFFEF4444), // Coral Red
            hint = "Needs numbers or mixed case"
        )
        2 -> PasswordStrength(
            score = 2,
            label = "Fair",
            color = Color(0xFFF59E0B), // Warm Amber
            hint = "Add special symbols (!@#)"
        )
        3 -> PasswordStrength(
            score = 3,
            label = "Good",
            color = Color(0xFF38BDF8), // Electric Sky Blue
            hint = "Good password security"
        )
        4 -> PasswordStrength(
            score = 4,
            label = "Strong",
            color = Color(0xFF10B981), // Mint / Emerald
            hint = "Maximum cryptographic strength ✓"
        )
        else -> PasswordStrength(0, "", Color.Transparent, "")
    }
}

/**
 * Four-Step Password Strength Meter.
 * - Displays 4 discrete horizontal segment bars.
 * - Fills and smoothly recolours across Red -> Amber -> Blue -> Mint as the password grows in strength.
 * - Features animated fill widths and animated color transitions.
 */
@Composable
fun ConnectoPasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier,
    showHint: Boolean = true
) {
    val strength = remember(password) { evaluatePasswordStrength(password) }

    // Smooth color morphing for the active segments
    val animatedColor by animateColorAsState(
        targetValue = if (strength.score > 0) strength.color else Color.Transparent,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "meterColor"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // 4 discrete horizontal segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (step in 1..4) {
                val isFilled = step <= strength.score
                val fillRatio by animateFloatAsState(
                    targetValue = if (isFilled) 1.0f else 0.0f,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "barFill_$step"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                ) {
                    if (fillRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fillRatio)
                                .height(4.5.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(animatedColor)
                        )
                    }
                }
            }
        }

        // Strength label and micro-hint
        if (showHint && password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(animatedColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Strength: ${strength.label}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = animatedColor
                    )
                }

                Text(
                    text = strength.hint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

// =========================================================================
// 3. EMAIL GHOST COMPLETION FIELD (GRAY GHOST TEXT -> TAP TO FILL WHITE)
// =========================================================================

private val POPULAR_EMAIL_DOMAINS = listOf(
    "@gmail.com",
    "@connecto.fun",
    "@outlook.com",
    "@icloud.com",
    "@yahoo.com"
)

/**
 * Email Input Field with Interactive Ghost Completion.
 * - When typing an email prefix, a gray ghost completion (@gmail.com / domain) appears immediately adjacent to the text.
 * - Also provides an interactive tap target chip.
 * - When tapped: the completion fills into the field in solid white with a gentle tactile pulse!
 */
@Composable
fun ConnectoEmailGhostField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Email Address",
    icon: ImageVector = Icons.Outlined.Mail,
    validationStatus: FieldValidationStatus = FieldValidationStatus.IDLE
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Determine the active ghost suggestion
    val ghostCompletion = remember(value) {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            null
        } else if (!trimmed.contains("@")) {
            // User typed username: suggest @gmail.com
            GhostSuggestion(
                fullEmail = "$trimmed@gmail.com",
                displaySuffix = "@gmail.com"
            )
        } else {
            val prefix = trimmed.substringBefore("@")
            val typedDomain = trimmed.substringAfter("@")
            if (prefix.isNotEmpty()) {
                val match = POPULAR_EMAIL_DOMAINS.firstOrNull { domain ->
                    val domainWithoutAt = domain.removePrefix("@")
                    domainWithoutAt.startsWith(typedDomain, ignoreCase = true) && domainWithoutAt != typedDomain
                }
                if (match != null) {
                    val full = prefix + match
                    val suffix = match.removePrefix("@").removePrefix(typedDomain)
                    GhostSuggestion(
                        fullEmail = full,
                        displaySuffix = suffix
                    )
                } else null
            } else null
        }
    }

    // Flash white animation when ghost completion is tapped
    val fillWhiteAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val applyGhostCompletion: () -> Unit = {
        ghostCompletion?.let { suggestion ->
            try {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            } catch (_: Exception) {}
            onValueChange(suggestion.fullEmail)
            scope.launch {
                fillWhiteAnim.snapTo(1f)
                fillWhiteAnim.animateTo(0f, animationSpec = tween(500, easing = FastOutSlowInEasing))
            }
        }
    }

    val dynamicBorderColor = when (validationStatus) {
        FieldValidationStatus.VALID_AVAILABLE -> Color(0xFF10B981)
        FieldValidationStatus.TAKEN, FieldValidationStatus.INVALID_FORMAT -> Color(0xFFEF4444)
        FieldValidationStatus.CHECKING -> Color(0xFF38BDF8)
        FieldValidationStatus.IDLE -> if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = if (isFocused) 1.6.dp else 1.dp,
                    color = dynamicBorderColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Email Icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when (validationStatus) {
                        FieldValidationStatus.VALID_AVAILABLE -> Color(0xFF10B981)
                        FieldValidationStatus.TAKEN, FieldValidationStatus.INVALID_FORMAT -> Color(0xFFEF4444)
                        else -> if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Input + Ghost Overlay Area
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        interactionSource = interactionSource,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = if (fillWhiteAnim.value > 0f) {
                                lerp(Color.White, Color(0xFF38BDF8), fillWhiteAnim.value)
                            } else {
                                Color.White
                            },
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    if (value.isEmpty()) {
                                        Text(
                                            text = placeholder,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                                if (ghostCompletion != null && isFocused) {
                                    Text(
                                        text = ghostCompletion.displaySuffix,
                                        color = if (fillWhiteAnim.value > 0f) Color.White else Color(0xFF94A3B8).copy(alpha = 0.55f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            applyGhostCompletion()
                                        }
                                    )
                                }
                            }
                        }
                    )
                }

                // Interactive Tap-to-Fill Chip in Trailing Area
                if (ghostCompletion != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                            .clickable {
                                applyGhostCompletion()
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "tap ",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = ghostCompletion.displaySuffix.take(12),
                                color = if (fillWhiteAnim.value > 0f) Color.White else Color(0xFF38BDF8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Validation Status Indicators
                when (validationStatus) {
                    FieldValidationStatus.CHECKING -> {
                        CircularProgressIndicator(
                            color = Color(0xFF38BDF8),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    FieldValidationStatus.VALID_AVAILABLE -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    FieldValidationStatus.TAKEN, FieldValidationStatus.INVALID_FORMAT -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    FieldValidationStatus.IDLE -> Unit
                }
            }
        }
    }
}

private data class GhostSuggestion(
    val fullEmail: String,
    val displaySuffix: String
)
