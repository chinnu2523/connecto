package com.example.connecto.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.connecto.ui.theme.pressScaleEffect
import org.json.JSONArray

object ConnectoSearchPreferences {
    private const val PREFS_NAME = "connecto_search_prefs"
    private const val KEY_RECENT = "recent_queries"

    fun getRecentSearches(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_RECENT, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optString(i, "").trim()
                if (item.length >= 2 && !list.any { it.equals(item, ignoreCase = true) }) {
                    try {
                        if (!com.example.connecto.security.SecretVaultManager.isSecretCodeMatch(context, item)) {
                            list.add(item)
                        }
                    } catch (_: Exception) {
                        list.add(item)
                    }
                }
            }
            list.take(5)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveRecentSearch(context: Context, query: String) {
        val clean = query.trim()
        if (clean.length < 2) return
        try {
            if (com.example.connecto.security.SecretVaultManager.isSecretCodeMatch(context, clean)) {
                return
            }
        } catch (_: Exception) {}
        val current = getRecentSearches(context).toMutableList()
        current.removeAll { it.equals(clean, ignoreCase = true) }
        current.add(0, clean)
        val limited = current.take(5)
        val arr = JSONArray()
        limited.forEach { arr.put(it) }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENT, arr.toString())
            .apply()
    }

    fun clearRecentSearches(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_RECENT)
            .apply()
    }
}

/**
 * 60fps Micro-Animated Search Bar with active focus transition, sliding cancel button,
 * active glow border, 16px inline spinner, and pop-in clear icon with haptics.
 */
@Composable
fun ConnectoAnimatedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search messages, groups, or friends...",
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    isLoading: Boolean = false,
    onCancel: () -> Unit,
    onExplicitSearch: ((String) -> Unit)? = null,
    focusRequester: FocusRequester = remember { FocusRequester() },
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isQueryActive = query.isNotEmpty()
    val isSearchActive = isFocused || isQueryActive

    // Border transitions smoothly from subtle rgba(255,255,255,0.08) to active crisp white
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0x66FFFFFF) else (if (isDark) Color(0x14FFFFFF) else Color(0x1A000000)),
        animationSpec = tween(220, easing = EaseOutCubic),
        label = "searchBorderColor"
    )

    // Icon tint transitions smoothly to active white
    val iconTint by animateColorAsState(
        targetValue = if (isFocused) (if (isDark) Color.White else Color(0xFF0F172A)) else (if (isDark) Color(0xFF8E929F) else Color(0xFF64748B)),
        animationSpec = tween(220, easing = EaseOutCubic),
        label = "searchIconTint"
    )

    // Clear (X) button pop-in spring animation (scale: 0.5 -> 1.0, 140ms spring)
    val clearScale by animateFloatAsState(
        targetValue = if (isQueryActive) 1f else 0.5f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.6f),
        label = "searchClearScale"
    )
    val clearAlpha by animateFloatAsState(
        targetValue = if (isQueryActive) 1f else 0f,
        animationSpec = tween(140),
        label = "searchClearAlpha"
    )

    // Sliding Cancel Button (translateX: 40px -> 0px, opacity: 0 -> 1) using spring physics (stiffness: 380, damping: 30)
    val cancelTranslateX by animateFloatAsState(
        targetValue = if (isSearchActive) 0f else 40f,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.75f),
        label = "cancelTranslateX"
    )
    val cancelAlpha by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = spring(stiffness = 380f, dampingRatio = 0.75f),
        label = "cancelAlpha"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Search Pill
        Box(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0xFF12141A) else Color(0xFFF1F5F9))
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon: Seamlessly swaps between 16px inline spinner and magnifying glass
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF818CF8)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Text Input Field
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = if (isDark) Color(0xFF686C78) else Color(0xFF94A3B8),
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { onFocusChanged(it.isFocused) },
                        textStyle = TextStyle(
                            color = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(Color(0xFF818CF8)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                val q = query.trim()
                                if (q.length >= 2) {
                                    onExplicitSearch?.invoke(q)
                                }
                            }
                        )
                    )
                }

                // Pop-in Clear (X) Button with Haptic Feedback
                if (isQueryActive || clearAlpha > 0.05f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = clearScale
                                scaleY = clearScale
                                alpha = clearAlpha
                            }
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E2230) else Color(0xFFCBD5E1))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onQueryChange("")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = if (isDark) Color(0xFF8E929F) else Color(0xFF475569),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // Sliding Cancel Button (Spring Physics, 12px left spacing, vertically centered)
        if (isSearchActive || cancelAlpha > 0.05f) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = cancelTranslateX
                        alpha = cancelAlpha
                    }
                    .padding(start = 12.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onCancel()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF818CF8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Filter Chips and Recent Searches Pill Row displayed when the search bar is focused.
 */
@Composable
fun RecentSearchesAndFilterChips(
    recentSearches: List<String>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    onSelectRecentSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val filterTabs = listOf("All", "People", "Messages", "Channels")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterTabs.forEach { tab ->
                val isSelected = selectedFilter == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A))
                            else if (isDark) Color(0xFF161822)
                            else Color(0xFFE2E8F0)
                        )
                        .border(
                            1.dp,
                            if (isSelected) (if (isDark) Color.White else Color(0xFF0F172A))
                            else (if (isDark) Color(0x14FFFFFF) else Color(0x1A000000)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectFilter(tab)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) (if (isDark) Color.Black else Color.White)
                                else (if (isDark) Color(0xFF94A3B8) else Color(0xFF475569))
                    )
                }
            }
        }

        // Recent Searches Pill Row (if available)
        if (recentSearches.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RECENT SEARCHES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                    )
                }
                Text(
                    text = "Clear",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF818CF8),
                    modifier = Modifier.clickable { onClearRecentSearches() }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recentSearches.forEach { queryItem ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0xFF1E2230) else Color(0xFFE2E8F0))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectRecentSearch(queryItem)
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = queryItem,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders text with exact substring matches highlighted in bold white and a subtle tinted background (rgba(99, 102, 241, 0.28)).
 */
@Composable
fun SubstringHighlightedText(
    text: String,
    query: String,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    modifier: Modifier = Modifier
) {
    val cleanQ = query.trim().lowercase()
    val annotated = remember(text, cleanQ) {
        if (cleanQ.isEmpty() || !text.lowercase().contains(cleanQ)) {
            AnnotatedString(text)
        } else {
            buildAnnotatedString {
                val lowerText = text.lowercase()
                var startIndex = 0
                while (startIndex < text.length) {
                    val matchIndex = lowerText.indexOf(cleanQ, startIndex)
                    if (matchIndex == -1) {
                        append(text.substring(startIndex))
                        break
                    }
                    if (matchIndex > startIndex) {
                        append(text.substring(startIndex, matchIndex))
                    }
                    val matchEnd = matchIndex + cleanQ.length
                    pushStyle(
                        SpanStyle(
                            background = Color(0x33FFFFFF),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    append(text.substring(matchIndex, matchEnd))
                    pop()
                    startIndex = matchEnd
                }
            }
        }
    }

    Text(
        text = annotated,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
    )
}

/**
 * Centered empty state graphic when zero results match the query.
 */
@Composable
fun SearchEmptyState(
    query: String,
    isDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFF161822))
                .border(1.dp, Color(0x26FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (isDark) Color.White else Color(0xFF0F172A),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No matches for \"$query\"",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF0F172A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Check the spelling or try searching for another contact, message, or channel keyword.",
            fontSize = 12.5.sp,
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Animated In-Chat and Channel Search Bar Header:
 * Replaces or overlays the top header with an inline search bar, live match counter (e.g. 1 / 8),
 * ↑ and ↓ navigation buttons, and a ✕ close button.
 */
@Composable
fun InlineChatSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search in chat...",
    currentMatchIndex: Int,
    totalMatches: Int,
    onPreviousMatch: () -> Unit,
    onNextMatch: () -> Unit,
    onClose: () -> Unit,
    isDark: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(if (isDark) Color(0xFF0E1016) else Color(0xFFF8FAFC))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Capsule
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF161822) else Color(0xFFE2E8F0))
                .border(1.dp, if (isDark) Color(0x26FFFFFF) else Color(0x33000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isDark) Color.White else Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            color = if (isDark) Color.White else Color(0xFF0F172A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(Color(0xFF818CF8)),
                        singleLine = true
                    )
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Match Counter (e.g. 1 / 8)
        val matchText = if (totalMatches > 0) "${currentMatchIndex + 1} / $totalMatches" else "0 / 0"
        Text(
            text = matchText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (totalMatches > 0) Color(0xFF818CF8) else (if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)),
            style = TextStyle(fontFeatureSettings = "tnum")
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Previous Match (↑)
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPreviousMatch()
            },
            enabled = totalMatches > 0,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous Match",
                tint = if (totalMatches > 0) (if (isDark) Color.White else Color(0xFF0F172A)) else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Next Match (↓)
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onNextMatch()
            },
            enabled = totalMatches > 0,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next Match",
                tint = if (totalMatches > 0) (if (isDark) Color.White else Color(0xFF0F172A)) else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }

        // Close Search Button (✕)
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Search",
                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
