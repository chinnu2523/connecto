package com.connecto.store.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.connecto.store.R
import com.connecto.store.installer.ApkInstaller
import com.connecto.store.model.StoreApp
import com.connecto.store.model.StoreRole
import com.connecto.store.model.StoreUser
import com.connecto.store.viewmodel.AppUiState
import com.connecto.store.viewmodel.LoginUiState
import com.connecto.store.viewmodel.StoreUiState
import com.connecto.store.viewmodel.StoreViewModel

// ─── Pitch-Black Luxury OLED & Clean White Theme Palette ─────────────────────
data class StoreThemeColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val cardElevated: Color,
    val cardBorder: Color,
    val borderHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDarkMuted: Color,
    val navBarBackground: Color,
    val dividerColor: Color,
    val searchBarBg: Color,
    val accentColor: Color = Color(0xFF6C5CE7),
    val accentColorLight: Color = Color(0xFFA29BFE)
)

val DarkStoreTheme = StoreThemeColors(
    isDark = true,
    background = Color(0xFF000000),
    surface = Color(0xFF0E0E10),
    cardElevated = Color(0xFF161619),
    cardBorder = Color(0xFF222226),
    borderHighlight = Color(0xFF333338),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFE5E7EB),
    textMuted = Color(0xFF9CA3AF),
    textDarkMuted = Color(0xFF6B7280),
    navBarBackground = Color(0xFF000000),
    dividerColor = Color(0xFF222226),
    searchBarBg = Color(0xFF161619),
    accentColor = Color(0xFF6C5CE7),
    accentColorLight = Color(0xFFA29BFE)
)

val LightStoreTheme = StoreThemeColors(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    cardElevated = Color(0xFFF1F5F9),
    cardBorder = Color(0xFFE2E8F0),
    borderHighlight = Color(0xFFCBD5E1),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF334155),
    textMuted = Color(0xFF64748B),
    textDarkMuted = Color(0xFF94A3B8),
    navBarBackground = Color(0xFFFFFFFF),
    dividerColor = Color(0xFFE2E8F0),
    searchBarBg = Color(0xFFF1F5F9),
    accentColor = Color(0xFF6C5CE7),
    accentColorLight = Color(0xFF5645CA)
)

val LocalStoreTheme = staticCompositionLocalOf { DarkStoreTheme }

// ─── Tactile Spring Press Interaction Modifier ────────────────────────────────
@Composable
fun Modifier.tactilePress(
    pressedScale: Float = 0.94f,
    onClick: (() -> Unit)? = null
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tactilePressScale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else {
                Modifier
            }
        )
}

// ─── Theme Persistence Manager ───────────────────────────────────────────────
object StoreThemeManager {
    private const val PREFS_NAME = "our_store_theme_prefs"
    private const val KEY_IS_DARK = "key_is_dark_theme"

    fun isDarkTheme(context: android.content.Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_DARK, true)
    }

    fun setDarkTheme(context: android.content.Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DARK, isDark).apply()
    }
}

// ─── Pitch-Black Luxury OLED Palette ──────────────────────────────────────────
private val TruePitchBlack = Color(0xFF000000)
private val DarkGraphiteSurface = Color(0xFF0E0E10)
private val DarkCardElevated = Color(0xFF161619)
private val DarkCardBorder = Color(0xFF222226)
private val DarkBorderHighlight = Color(0xFF333338)
private val TextPureWhite = Color(0xFFFFFFFF)
private val TextSilver = Color(0xFFE5E7EB)
private val TextMutedSilver = Color(0xFF9CA3AF)
private val TextDarkMuted = Color(0xFF6B7280)
private val EmeraldStatus = Color(0xFF10B981)
private val EmeraldStatusBg = Color(0xFF06281E)
private val AmberUpdate = Color(0xFFF59E0B)
private val AmberUpdateBg = Color(0xFF2B1D04)
private val DangerRed = Color(0xFFEF4444)
private val DangerRedBg = Color(0xFF2E1010)
private val DangerRedBorder = Color(0xFF7F1D1D)

// ─── Navigation Tabs ───────────────────────────────────────────────────────────
enum class StoreNavSection(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    OVERVIEW("Store", Icons.Rounded.Storefront),
    UPDATES("Updates", Icons.Rounded.SystemUpdate),
    LOGIN("Account", Icons.Rounded.AccountCircle)
}

// ─── Main Application Composable ──────────────────────────────────────────────
@Composable
fun StoreApp(
    vm: StoreViewModel = viewModel(),
    initialPackageName: String? = null,
    initialSectionName: String? = null,
    onTargetHandled: () -> Unit = {}
) {
    val state by vm.uiState.collectAsState()
    val currentUser by vm.currentUser.collectAsState()
    val loginState by vm.loginState.collectAsState()
    val pendingLegalConsentUser by vm.pendingLegalConsentUser.collectAsState()
    val pendingAuthApp by vm.pendingAuthApp.collectAsState()
    val authError by vm.authError.collectAsState()
    val isVerifyingAuth by vm.isVerifyingAuth.collectAsState()
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf(StoreNavSection.OVERVIEW) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPackageName by remember { mutableStateOf<String?>(null) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    // Respond to deep-link parameters from Android system notifications
    LaunchedEffect(initialPackageName, initialSectionName) {
        if (initialPackageName != null) {
            selectedPackageName = initialPackageName
            onTargetHandled()
        } else if (initialSectionName != null) {
            when (initialSectionName.uppercase()) {
                "UPDATES" -> selectedSection = StoreNavSection.UPDATES
                "LOGIN", "ACCOUNT" -> selectedSection = StoreNavSection.LOGIN
                else -> selectedSection = StoreNavSection.OVERVIEW
            }
            onTargetHandled()
        }
    }

    LaunchedEffect(Unit) {
        vm.refreshInstallStates()
    }

    // Intercept back key when inside App Details to return to the catalog
    BackHandler(enabled = selectedPackageName != null) {
        selectedPackageName = null
    }

    var isDarkTheme by remember { mutableStateOf(StoreThemeManager.isDarkTheme(context)) }
    val currentTheme = if (isDarkTheme) DarkStoreTheme else LightStoreTheme
    val onToggleTheme: () -> Unit = {
        val next = !isDarkTheme
        isDarkTheme = next
        StoreThemeManager.setDarkTheme(context, next)
    }

    CompositionLocalProvider(LocalStoreTheme provides currentTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = currentTheme.background
        ) {
        when (val s = state) {
            is StoreUiState.Loading -> ClassicLoadingState()
            is StoreUiState.Error   -> ClassicErrorState(s.message, onRetry = { vm.loadCatalog() })
            is StoreUiState.Success -> {
                if (currentUser == null) {
                    // Strictly unauthenticated gate: show NOTHING except the Login with Connecto screen
                    ConnectoLoginGateScreen(
                        loginState = loginState,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onLoginConnecto = { vm.loginWithConnecto(context) },
                        onClearLoginError = { vm.clearLoginError() }
                    )
                } else {
                    val detailApp = selectedPackageName?.let { pkg ->
                        s.apps.find { it.app.packageName == pkg }
                    }

                    if (detailApp != null) {
                        // Dedicated Full-Screen Application Detail & Preview View
                        ClassicAppDetailView(
                            appState = detailApp,
                            onBack = { selectedPackageName = null },
                            onAction = { vm.handleAppAction(context, detailApp) },
                            onUninstall = { vm.uninstallApp(context, detailApp) }
                        )
                    } else {
                        // Catalog Browsing View with Header, Filter & Footer Navigation
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Executive Top Header
                            ClassicTopHeader(
                                searchQuery = searchQuery,
                                onSearchChange = { searchQuery = it },
                                onRefresh = { vm.loadCatalog() },
                                onOpenSecurityDialog = { showSecurityDialog = true },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = onToggleTheme,
                                showSearch = selectedSection == StoreNavSection.OVERVIEW
                            )

                            // Content Stage with Navigation Section Transition
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                val filteredApps = remember(s.apps, searchQuery, selectedCategory, selectedSection) {
                                    s.apps.filter { appState ->
                                        val matchesSearch = searchQuery.isBlank() ||
                                            appState.app.name.contains(searchQuery, ignoreCase = true) ||
                                            appState.app.tagline.contains(searchQuery, ignoreCase = true) ||
                                            appState.app.packageName.contains(searchQuery, ignoreCase = true)

                                        val matchesCategory = (selectedSection != StoreNavSection.OVERVIEW) || selectedCategory == "All" ||
                                            appState.app.category.equals(selectedCategory, ignoreCase = true)

                                        val matchesSection = when (selectedSection) {
                                            StoreNavSection.OVERVIEW -> true
                                            StoreNavSection.UPDATES -> appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE
                                            StoreNavSection.LOGIN -> true
                                        }

                                        matchesSearch && matchesCategory && matchesSection
                                    }
                                }

                                AnimatedContent(
                                    targetState = selectedSection,
                                    transitionSpec = {
                                        (fadeIn(animationSpec = tween(220)) +
                                            slideInHorizontally(
                                                initialOffsetX = { fullWidth -> if (targetState.ordinal > initialState.ordinal) fullWidth / 4 else -fullWidth / 4 },
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            )) togetherWith
                                        (fadeOut(animationSpec = tween(180)) +
                                            slideOutHorizontally(
                                                targetOffsetX = { fullWidth -> if (targetState.ordinal > initialState.ordinal) -fullWidth / 4 else fullWidth / 4 },
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                            ))
                                    },
                                    label = "navSectionTransition"
                                ) { currentSection ->
                                    if (currentSection == StoreNavSection.LOGIN) {
                                        ClassicLoginAccountView(
                                            currentUser = currentUser,
                                            loginState = loginState,
                                            storeSelfUpdate = s.storeSelfUpdate,
                                            isDarkTheme = isDarkTheme,
                                            onToggleTheme = onToggleTheme,
                                            onUpdateOurStore = { s.storeSelfUpdate?.let { vm.downloadAndInstall(context, it) } },
                                            onLoginCredentials = { u, p -> vm.loginWithCredentials(u, p) },
                                            onLoginConnecto = { vm.loginWithConnecto(context) },
                                            onLogout = { vm.logout() },
                                            onClearLoginError = { vm.clearLoginError() }
                                        )
                                    } else {
                                        ClassicCatalogBody(
                                            state = s,
                                            filteredApps = filteredApps,
                                            selectedCategory = selectedCategory,
                                            onSelectCategory = { selectedCategory = it },
                                            onAppClick = { selectedPackageName = it.app.packageName },
                                            onAppAction = { appState -> vm.handleAppAction(context, appState) },
                                            onUpdateAll = { vm.updateAll(context) },
                                            isUpdatesTab = currentSection == StoreNavSection.UPDATES
                                        )
                                    }
                                }
                            }

                            // Interactive Modern Footer Navigation Bar
                            ClassicBottomNavBar(
                                selected = selectedSection,
                                onSelect = { selectedSection = it },
                                updateCount = s.apps.count {
                                    it.installState == StoreApp.InstallState.UPDATE_AVAILABLE
                                }
                            )
                        }
                    }

                    // Native Play Store Install Permission Prompt
                    s.permissionPromptApp?.let { pendingApp ->
                        ClassicPermissionDialog(
                            appName = pendingApp.app.name,
                            onGrant = { vm.grantPermissionClicked(context) },
                            onDismiss = { vm.dismissPermissionPrompt() }
                        )
                    }

                    // Mandatory Terms of Use & Privacy Policy Agreement on Connecto Login
                    pendingLegalConsentUser?.let { pendingUser ->
                        OurStoreLegalDialog(
                            initialTab = 0,
                            onAgree = { vm.recordUserLegalConsent(pendingUser) },
                            onDismiss = { vm.dismissLegalConsent() }
                        )
                    }

                    // Biometric & Password Download Authorization Sheet
                    pendingAuthApp?.let { pendingApp ->
                        StoreAppDownloadAuthSheet(
                            appState = pendingApp,
                            currentUser = currentUser,
                            authError = authError,
                            isVerifying = isVerifyingAuth,
                            onDismiss = { vm.dismissDownloadAuth() },
                            onBiometricSuccess = { vm.completeBiometricAuthAndDownload(context, pendingApp) },
                            onPasswordSubmit = { pass, enableBio -> vm.verifyPasswordAndDownload(context, pendingApp, pass, enableBio) }
                        )
                    }

                    // Google Play Protect & Safety Status Dialog
                    if (showSecurityDialog) {
                        PlayProtectSecurityDialog(
                            onDismiss = { showSecurityDialog = false },
                            onOpenSettings = { ApkInstaller.openPlayProtectSettings(context) }
                        )
                    }
                }
            }
        }
    }
    }
}

// ─── Classic Executive Top Header ─────────────────────────────────────────────
@Composable
private fun ClassicTopHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenSecurityDialog: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    showSearch: Boolean = true
) {
    val theme = LocalStoreTheme.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.surface)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Master Architectural Obsidian Store Insignia
                OurStoreLogoMark(modifier = Modifier.size(40.dp))

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "OUR STORE",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(theme.cardElevated)
                                .border(1.dp, theme.cardBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ENTERPRISE HUB",
                                color = theme.textMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }
                    Text(
                        text = "Native In-App Package Engine • Instant Deployment",
                        color = theme.textDarkMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Theme Toggle Button with Spring Physics
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.cardElevated)
                        .border(1.dp, theme.cardBorder, CircleShape)
                        .tactilePress(pressedScale = 0.88f) { onToggleTheme() },
                    contentAlignment = Alignment.Center
                ) {
                    val rotation by animateFloatAsState(
                        targetValue = if (isDarkTheme) 0f else 180f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                        label = "themeIconRotation"
                    )
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = "Toggle Appearance Theme",
                        tint = if (isDarkTheme) AmberUpdate else theme.textPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = rotation }
                    )
                }

                // Play Protect & Safety Status Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.cardElevated)
                        .border(1.dp, EmeraldStatus.copy(alpha = 0.45f), CircleShape)
                        .tactilePress(pressedScale = 0.88f) { onOpenSecurityDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = "Play Protect & Safety Status",
                        tint = EmeraldStatus,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Refresh Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(theme.cardElevated)
                        .border(1.dp, theme.cardBorder, CircleShape)
                        .tactilePress(pressedScale = 0.88f) { onRefresh() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh Catalog",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (showSearch) {
            // Monochromatic Executive Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                placeholder = {
                    Text(
                        "Search ecosystem applications…",
                        color = theme.textDarkMuted,
                        fontSize = 12.5.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = theme.textDarkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = theme.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = theme.searchBarBg,
                    unfocusedContainerColor = theme.searchBarBg,
                    focusedBorderColor = theme.borderHighlight,
                    unfocusedBorderColor = theme.cardBorder,
                    focusedTextColor = theme.textPrimary,
                    unfocusedTextColor = theme.textSecondary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }
    }
}

// ─── Footer Bottom Navigation Bar with Spring Animations ──────────────────────
@Composable
private fun ClassicBottomNavBar(
    selected: StoreNavSection,
    onSelect: (StoreNavSection) -> Unit,
    updateCount: Int
) {
    val theme = LocalStoreTheme.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = theme.navBarBackground
    ) {
        Column {
            HorizontalDivider(color = theme.dividerColor, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreNavSection.values().forEach { section ->
                    val isSelected = selected == section
                    val badgeCount = if (section == StoreNavSection.UPDATES) updateCount else 0

                    ClassicBottomNavTabItem(
                        section = section,
                        isSelected = isSelected,
                        badgeCount = badgeCount,
                        onClick = { onSelect(section) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassicBottomNavTabItem(
    section: StoreNavSection,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    val theme = LocalStoreTheme.current

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tabScale"
    )

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) (if (theme.isDark) theme.cardElevated else Color(0xFFF1F5F9)) else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "tabBg"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) theme.accentColor else theme.textDarkMuted,
        animationSpec = tween(durationMillis = 200),
        label = "tabFg"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(animatedContainerColor)
            .border(
                1.dp,
                if (isSelected) theme.borderHighlight else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .tactilePress(pressedScale = 0.92f) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = section.label,
                    tint = animatedContentColor,
                    modifier = Modifier.size(24.dp)
                )

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 6.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(AmberUpdate)
                            .padding(horizontal = 4.5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "$badgeCount",
                            color = Color.Black,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Text(
                text = section.label,
                color = animatedContentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

// ─── Catalog Body ─────────────────────────────────────────────────────────────
@Composable
private fun ClassicCatalogBody(
    state: StoreUiState.Success,
    filteredApps: List<AppUiState>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAppClick: (AppUiState) -> Unit,
    onAppAction: (AppUiState) -> Unit,
    onUpdateAll: () -> Unit,
    isUpdatesTab: Boolean
) {
    val theme = LocalStoreTheme.current
    val categories = remember(state.apps) {
        listOf("All") + state.apps.map { it.app.category }.distinct()
    }
    val updateCount = state.apps.count { it.installState == StoreApp.InstallState.UPDATE_AVAILABLE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isUpdatesTab) {
            // Category Filter Pills
            item {
                ClassicCategoryPills(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = onSelectCategory
                )
            }
        } else {
            // Streamlined Background Auto-Update Status Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (updateCount > 0) AmberUpdate else EmeraldStatus)
                            )
                            Text(
                                text = if (updateCount > 0) "$updateCount UPDATE${if (updateCount > 1) "S" else ""} AVAILABLE" else "ALL APPS UP TO DATE",
                                color = if (updateCount > 0) AmberUpdate else EmeraldStatus,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Text(
                            text = "Auto-Update: Autonomous Background Engine Active",
                            color = theme.textDarkMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (updateCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.textPrimary)
                                .tactilePress(pressedScale = 0.92f) { onUpdateAll() }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DownloadDone,
                                    contentDescription = null,
                                    tint = theme.background,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "UPDATE ALL",
                                    color = theme.background,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Meta Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUpdatesTab) "PENDING SYSTEM UPDATES" else "VERIFIED ECOSYSTEM APPLICATIONS",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${filteredApps.size} apps available",
                    color = theme.textDarkMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // App Card Buttons
        if (filteredApps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldStatusBg)
                                .border(1.dp, EmeraldStatus.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = EmeraldStatus,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (isUpdatesTab) "All installed applications are up to date" else "No packages found",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = if (isUpdatesTab) "The background engine monitors and syncs new releases continuously." else "Try searching with a different keyword or category.",
                            color = theme.textMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            itemsIndexed(filteredApps, key = { _, item -> item.app.packageName }) { _, appState ->
                ClassicAppCard(
                    appState = appState,
                    onClick = { onAppClick(appState) },
                    onActionClick = { onAppAction(appState) }
                )
            }
        }

        // Professional Footer Sign-Off
        item {
            ClassicFooterInfo()
        }
    }
}

// ─── Category Pills ───────────────────────────────────────────────────────────
@Composable
private fun ClassicCategoryPills(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val theme = LocalStoreTheme.current

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(categories) { category ->
            val isSelected = selected == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) theme.textPrimary else theme.surface)
                    .border(
                        1.dp,
                        if (isSelected) theme.textPrimary else theme.cardBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .tactilePress(pressedScale = 0.94f) { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = category,
                    color = if (isSelected) theme.background else theme.textMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ─── Classic App Card (Acts as Interactive Button) ────────────────────────────
@Composable
private fun ClassicAppCard(
    appState: AppUiState,
    onClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val theme = LocalStoreTheme.current
    val app = appState.app

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .tactilePress(pressedScale = 0.98f) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: App Logo + Info + Size
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Authentic Application Logo/Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(theme.cardElevated)
                        .border(1.dp, theme.borderHighlight, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AppIconImage(appId = app.id, appName = app.name)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = app.name,
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                letterSpacing = (-0.3).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(theme.cardElevated)
                                    .border(1.dp, theme.cardBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = app.badge.uppercase(),
                                    color = theme.textMuted,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Prominent High-Visibility Size Pill on Top of Card Header
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.cardElevated)
                                .border(1.dp, theme.borderHighlight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = app.sizeFormatted,
                                color = theme.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }

                    Text(
                        text = app.tagline,
                        color = theme.textMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = app.packageName,
                        color = theme.textDarkMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Specs Row: Version & Install State
            ClassicVersionSpecRow(appState = appState)

            // Bottom Interactive Tap Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.cardElevated)
                    .border(1.dp, theme.borderHighlight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val (statusColor, statusLabel) = when {
                        appState.isInstalling -> Pair(theme.textPrimary, "Installing into OS…")
                        appState.isDownloading -> Pair(theme.textPrimary, "Downloading ${appState.downloadProgress}%…")
                        appState.installState == StoreApp.InstallState.NOT_INSTALLED -> Pair(theme.textMuted, "Tap to Install & View Preview")
                        appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> Pair(AmberUpdate, "Update Available • Tap to View")
                        else -> Pair(EmeraldStatus, "Installed • Tap to Manage / Uninstall")
                    }
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val actionLabel = when {
                        appState.isInstalling -> "INSTALLING…"
                        appState.isDownloading -> "${appState.downloadProgress}%"
                        appState.installState == StoreApp.InstallState.NOT_INSTALLED -> "GET"
                        appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> "UPDATE"
                        else -> "OPEN"
                    }
                    val actionBg = when {
                        appState.isDownloading || appState.isInstalling -> theme.cardBorder
                        appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> AmberUpdate
                        else -> theme.textPrimary
                    }
                    val actionFg = when {
                        appState.isDownloading || appState.isInstalling -> theme.textPrimary
                        appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> Color.Black
                        else -> theme.background
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(actionBg)
                            .tactilePress(pressedScale = 0.90f) { onActionClick() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = actionLabel,
                            color = actionFg,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Details",
                        tint = theme.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─── Dedicated Full-Screen Application Detail & Preview View ───────────────────
@Composable
private fun ClassicAppDetailView(
    appState: AppUiState,
    onBack: () -> Unit,
    onAction: () -> Unit,
    onUninstall: () -> Unit
) {
    val theme = LocalStoreTheme.current
    val app = appState.app
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarsPadding()
    ) {
        // Top Back Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(theme.cardElevated)
                        .border(1.dp, theme.cardBorder, CircleShape)
                        .tactilePress(pressedScale = 0.88f) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = app.name,
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Ecosystem Application",
                        color = theme.textDarkMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.cardElevated)
                    .border(1.dp, theme.borderHighlight, RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = app.category.uppercase(),
                    color = theme.textMuted,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }

        HorizontalDivider(color = theme.cardBorder, thickness = 1.dp)

        // Scrollable App Detail Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Identity Hero
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.cardElevated)
                        .border(1.5.dp, theme.borderHighlight, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AppIconImage(appId = app.id, appName = app.name)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.name,
                            color = theme.textPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            letterSpacing = (-0.5).sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.cardElevated)
                                .border(1.dp, theme.borderHighlight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = app.sizeFormatted,
                                color = theme.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = app.tagline,
                        color = theme.textMuted,
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = app.packageName,
                        color = theme.textDarkMuted,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Specs Row (Version, Device Version, Size Pill, Status)
            ClassicVersionSpecRow(appState = appState)

            // Progress Bar if Downloading or Installing with Shimmer Sweep
            AnimatedVisibility(visible = appState.isDownloading || appState.isInstalling) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val status = if (appState.isInstalling) "Installing directly into Android OS…" else "Downloading package…"
                        Text(status, color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (appState.isInstalling) "Please wait" else "${appState.downloadProgress}%",
                            color = theme.textMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (appState.isInstalling) 1f else appState.downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = theme.textPrimary,
                        trackColor = theme.cardElevated
                    )
                }
            }

            // Action Row: [ INSTALL / UPDATE / OPEN ] and [ UNINSTALL ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isInstalled = (appState.installState == StoreApp.InstallState.UP_TO_DATE ||
                    (appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE && appState.installedVersionName != null)) &&
                    appState.app.packageName != "com.connecto.store"

                // Primary Action Button (Install / Update / Open)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                appState.isDownloading || appState.isInstalling -> theme.cardBorder
                                appState.installState == StoreApp.InstallState.NOT_INSTALLED -> theme.textPrimary
                                appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> AmberUpdate
                                else -> theme.textPrimary
                            }
                        )
                        .tactilePress(
                            pressedScale = 0.95f,
                            onClick = if (!appState.isDownloading && !appState.isInstalling) onAction else null
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when {
                            appState.isDownloading || appState.isInstalling -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = theme.textPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = if (appState.isInstalling) "INSTALLING…" else "DOWNLOADING…",
                                    color = theme.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            appState.installState == StoreApp.InstallState.NOT_INSTALLED -> {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = theme.background,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "INSTALL (${app.sizeFormatted})",
                                    color = theme.background,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "UPDATE (v${app.versionName})",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Launch,
                                    contentDescription = null,
                                    tint = theme.background,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "OPEN APP",
                                    color = theme.background,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Secondary Action Button (UNINSTALL) - only visible if app is installed!
                if (isInstalled && !appState.isDownloading && !appState.isInstalling) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DangerRedBg)
                            .border(1.dp, DangerRedBorder, RoundedCornerShape(12.dp))
                            .tactilePress(pressedScale = 0.94f) { onUninstall() }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Uninstall",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "UNINSTALL",
                                color = DangerRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Visual Application Previews Section (Interactive Horizontal Carousel)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "APPLICATION PREVIEWS",
                        color = theme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "3 interactive frames",
                        color = theme.textDarkMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                AppPreviewCarousel(appId = app.id)
            }

            // Description Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ABOUT THIS APPLICATION",
                    color = theme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = app.description,
                        color = theme.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // What's New Section
            if (app.changelog.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "WHAT'S NEW IN v${app.versionName}",
                        color = theme.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    ClassicChangelogBox(app.changelog)
                }
            }

            // Technical Details Box
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TECHNICAL SPECIFICATIONS",
                    color = theme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TechSpecRow("Package ID", app.packageName)
                        TechSpecRow("Release Version", "v${app.versionName} (build ${app.versionCode})")
                        TechSpecRow("Binary Size", app.sizeFormatted)
                        TechSpecRow("Architecture", "arm64-v8a • universal")
                        TechSpecRow("Target System", "Android 16+ (API 37 Ready)")
                        TechSpecRow("Minimum System", "Android 8.0 (API 26)+")
                        TechSpecRow("Signature Schemes", "APK V1 + V2 + V3 + V4 Certified")
                        TechSpecRow("Installer Engine", "PackageInstaller Session IPC")
                        TechSpecRow("Distribution Edge", "Cloudflare Anycast Global CDN")
                        TechSpecRow("Security Integrity", "SHA-256 Validated • Clean")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Application Previews Carousel & Mockups ─────────────────────────────────
@Composable
private fun AppPreviewCarousel(appId: String) {
    val previews = remember(appId) { getAppPreviews(appId) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(previews) { preview ->
            PhoneMockupCard(preview)
        }
    }
}

data class AppPreviewData(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val headline: String,
    val highlights: List<String>,
    val accentColor: Color
)

private fun getAppPreviews(appId: String): List<AppPreviewData> = when (appId.lowercase()) {
    "ourstore" -> listOf(
        AppPreviewData(
            title = "ECOSYSTEM HUB",
            subtitle = "Native In-App 1-Tap Installs",
            icon = Icons.Rounded.Storefront,
            headline = "Autonomous Store Architecture",
            highlights = listOf(
                "Direct PackageInstaller session IPC",
                "Instant installation without browser redirect",
                "Prominent package size indicators"
            ),
            accentColor = Color(0xFFFFFFFF)
        ),
        AppPreviewData(
            title = "AUTO-UPDATE",
            subtitle = "Silent Background Sync",
            icon = Icons.Rounded.Autorenew,
            headline = "Zero-Interruption Engine",
            highlights = listOf(
                "Continuous release monitoring",
                "1-Tap 'Update All' batch engine",
                "Autonomous background execution"
            ),
            accentColor = EmeraldStatus
        ),
        AppPreviewData(
            title = "SECURITY VAULT",
            subtitle = "Cryptographic Verification",
            icon = Icons.Rounded.Security,
            headline = "Pure Obsidian OLED",
            highlights = listOf(
                "SHA-256 signature verification",
                "Native system uninstallation support",
                "Zero telemetry, tracking, or ads"
            ),
            accentColor = Color(0xFF60A5FA)
        )
    )
    "connecto" -> listOf(
        AppPreviewData(
            title = "OFFLINE MESH",
            subtitle = "Direct WiFi Local Chat",
            icon = Icons.AutoMirrored.Rounded.Chat,
            headline = "Zero Internet Required",
            highlights = listOf(
                "Local subnet & hotspot mesh network",
                "Instant peer auto-discovery",
                "End-to-end encrypted messaging"
            ),
            accentColor = Color(0xFF3B82F6)
        ),
        AppPreviewData(
            title = "P2P FILE SHARING",
            subtitle = "Gigabit Local Transfers",
            icon = Icons.AutoMirrored.Rounded.Send,
            headline = "Blazing Transfer Speeds",
            highlights = listOf(
                "Direct socket streaming architecture",
                "Lossless photo & video transmission",
                "Zero file size restriction"
            ),
            accentColor = EmeraldStatus
        ),
        AppPreviewData(
            title = "HARDWARE VAULT",
            subtitle = "Biometric Security",
            icon = Icons.Rounded.Fingerprint,
            headline = "Hardware KeyStore Enclave",
            highlights = listOf(
                "Biometric fingerprint authentication",
                "Anti-screenshot FLAG_SECURE shielding",
                "Secure multi-profile partition"
            ),
            accentColor = AmberUpdate
        )
    )
    "jarvis" -> listOf(
        AppPreviewData(
            title = "VOICE INTELLIGENCE",
            subtitle = "Neural Speech Interaction",
            icon = Icons.Rounded.Mic,
            headline = "Conversational Voice AI",
            highlights = listOf(
                "Continuous speech recognition",
                "Real-time voice activity detection",
                "Low-latency natural audio synthesis"
            ),
            accentColor = Color(0xFF8B5CF6)
        ),
        AppPreviewData(
            title = "AUTONOMOUS SHELL",
            subtitle = "System Execution Engine",
            icon = Icons.Rounded.Terminal,
            headline = "Real-Time Terminal Control",
            highlights = listOf(
                "Direct Android shell command processing",
                "Subprocess monitoring & telemetry",
                "Self-correcting execution pipelines"
            ),
            accentColor = EmeraldStatus
        ),
        AppPreviewData(
            title = "WORKFLOW AGENT",
            subtitle = "Smart Task Automation",
            icon = Icons.Rounded.SmartToy,
            headline = "Multi-Step Orchestration",
            highlights = listOf(
                "Background automation & timers",
                "Context-aware memory retention",
                "Tool-calling neural integration"
            ),
            accentColor = Color(0xFFEC4899)
        )
    )
    "pdfstudio" -> listOf(
        AppPreviewData(
            title = "IN-PLACE EDITOR",
            subtitle = "Matching Font Replacement",
            icon = Icons.Rounded.Edit,
            headline = "Same-Font Text Replacement",
            highlights = listOf(
                "Find & replace names in matching fonts",
                "Tap-to-edit text anywhere on page",
                "Preserves document layout & geometry"
            ),
            accentColor = DangerRed
        ),
        AppPreviewData(
            title = "IMAGE TO PDF",
            subtitle = "Instant Multi-Format Compiler",
            icon = Icons.Rounded.PhotoCamera,
            headline = "Universal Photo Converter",
            highlights = listOf(
                "Convert JPG, PNG, WEBP, BMP to PDF",
                "Auto-page rotation & margin scaling",
                "High-resolution lossless compression"
            ),
            accentColor = AmberUpdate
        ),
        AppPreviewData(
            title = "PDF MERGER",
            subtitle = "Multi-Document Assembly",
            icon = Icons.Rounded.PictureAsPdf,
            headline = "Lossless File Reordering",
            highlights = listOf(
                "Merge unlimited documents into one",
                "Visual page reordering & rotation",
                "Interactive pen drawing & highlight tools"
            ),
            accentColor = Color(0xFF3B82F6)
        )
    )
    else -> listOf(
        AppPreviewData(
            title = "VERIFIED APP",
            subtitle = "Official Package",
            icon = Icons.Rounded.Apps,
            headline = "Official Ecosystem Client",
            highlights = listOf(
                "Optimized for Android targetSdk 37",
                "High-speed binary distribution",
                "Zero adware or tracking"
            ),
            accentColor = TextPureWhite
        )
    )
}

@Composable
private fun PhoneMockupCard(preview: AppPreviewData) {
    val theme = LocalStoreTheme.current

    Box(
        modifier = Modifier
            .width(230.dp)
            .height(380.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(theme.surface)
            .border(1.5.dp, theme.borderHighlight, RoundedCornerShape(26.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mockup Status Bar with Dynamic Island / Punch Hole Notch
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "9:41",
                        color = theme.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Dynamic Camera Notch
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(if (theme.isDark) Color(0xFF161619) else Color(0xFFCBD5E1))
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(preview.accentColor)
                        )
                        Text(
                            text = "5G",
                            color = theme.textDarkMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Mockup Visual Core
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accent Icon Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(preview.accentColor.copy(alpha = 0.15f))
                        .border(1.dp, preview.accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = preview.icon,
                        contentDescription = null,
                        tint = preview.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = preview.title,
                        color = preview.accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = preview.headline,
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 19.sp
                    )
                }

                // Feature Highlights Inside Screen Mockup
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    preview.highlights.forEach { highlight ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(theme.textMuted)
                            )
                            Text(
                                text = highlight,
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Mockup Bottom Device Pill Bar & Caption
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.cardElevated)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = preview.subtitle,
                        color = theme.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Home Bar Pill
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(theme.textDarkMuted.copy(alpha = 0.5f))
                )
            }
        }
    }
}

// ─── Technical Specification Row Helper ───────────────────────────────────────
@Composable
private fun TechSpecRow(label: String, value: String) {
    val theme = LocalStoreTheme.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = theme.textDarkMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = theme.textPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ─── Version Spec Row ─────────────────────────────────────────────────────────
@Composable
private fun ClassicVersionSpecRow(appState: AppUiState) {
    val theme = LocalStoreTheme.current
    val (statusBg, statusFg, statusText) = when {
        appState.isInstalling -> Triple(theme.cardElevated, theme.textPrimary, "INSTALLING")
        appState.installState == StoreApp.InstallState.NOT_INSTALLED -> Triple(theme.cardElevated, theme.textMuted, "NOT INSTALLED")
        appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> Triple(AmberUpdateBg, AmberUpdate, "UPDATE AVAILABLE")
        else -> Triple(EmeraldStatusBg, EmeraldStatus, "UP TO DATE ✓")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.cardElevated)
            .border(1.dp, theme.cardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("CATALOG", color = theme.textDarkMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    "v${appState.app.versionName}",
                    color = theme.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (appState.installedVersionName != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DEVICE", color = theme.textDarkMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "v${appState.installedVersionName}",
                        color = theme.textMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusFg,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ─── Changelog Box ───────────────────────────────────────────────────────────
@Composable
private fun ClassicChangelogBox(changelog: List<String>) {
    val theme = LocalStoreTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.surface)
            .border(1.dp, theme.cardBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "CHANGELOG",
                color = theme.textDarkMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            changelog.take(4).forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("•", color = theme.accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(item, color = theme.textSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}

// ─── App Icon Image Helper ───────────────────────────────────────────────────
@Composable
internal fun AppIconImage(appId: String, appName: String, modifier: Modifier = Modifier.fillMaxSize()) {
    when (appId.lowercase()) {
        "ourstore" -> {
            OurStoreLogoMark(modifier = modifier)
        }
        "connecto" -> {
            Box(
                modifier = modifier.clip(RoundedCornerShape(13.dp)).background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Chat,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        "jarvis" -> {
            Box(
                modifier = modifier.clip(RoundedCornerShape(13.dp)).background(Color(0xFF8B5CF6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.SmartToy,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        "pdfstudio" -> {
            Box(
                modifier = modifier.clip(RoundedCornerShape(13.dp)).background(Color(0xFFEF4444)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PictureAsPdf,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        else -> {
            Box(
                modifier = modifier.clip(RoundedCornerShape(13.dp)).background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Apps,
                    contentDescription = appName,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ─── Footer Info ─────────────────────────────────────────────────────────────
@Composable
private fun ClassicFooterInfo() {
    val theme = LocalStoreTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("OUR STORE • NATIVE PACKAGE HUB", color = theme.textDarkMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("Verified Cloudflare Anycast Delivery • targetSdk 37", color = theme.textDarkMuted, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
    }
}

// ─── Loading & Error Views ────────────────────────────────────────────────────
@Composable
private fun ClassicLoadingState() {
    val theme = LocalStoreTheme.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(color = theme.textPrimary, strokeWidth = 3.dp)
            Text("SYNCHRONIZING REPOSITORY…", color = theme.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun ClassicErrorState(msg: String, onRetry: () -> Unit) {
    val theme = LocalStoreTheme.current
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("REPOSITORY SYNCHRONIZATION FAILED", color = DangerRed, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
            Text(msg, color = theme.textMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(theme.textPrimary)
                    .tactilePress(pressedScale = 0.92f) { onRetry() }
                    .padding(horizontal = 20.dp, vertical = 9.dp)
            ) {
                Text("RETRY", color = theme.background, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ─── Master Architectural Our Store Insignia Logo ───────────────────────────
@Composable
fun OurStoreLogoMark(modifier: Modifier = Modifier) {
    val theme = LocalStoreTheme.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(theme.cardElevated)
            .border(1.dp, theme.borderHighlight, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(7.dp)) {
            val w = size.width
            val h = size.height
            val primaryColor = if (theme.isDark) Color.White else Color(0xFF0F172A)
            val secondaryColor = if (theme.isDark) Color(0xFFE5E7EB) else Color(0xFF334155)
            val mutedColor = if (theme.isDark) Color(0xFF9CA3AF) else Color(0xFF64748B)

            // 1. Classical Pediment (Triangle Roof)
            val pathRoof = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                lineTo(w * 0.16f, h * 0.34f)
                lineTo(w * 0.84f, h * 0.34f)
                close()
            }
            drawPath(pathRoof, color = primaryColor)

            // 2. Entablature / Architrave Beam
            drawRect(
                color = secondaryColor,
                topLeft = Offset(w * 0.14f, h * 0.37f),
                size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.055f)
            )

            // 3. Classical 4 Pillars
            val pillarWidth = w * 0.09f
            val pillarHeight = h * 0.32f
            val pillarY = h * 0.45f

            drawRect(color = primaryColor, topLeft = Offset(w * 0.18f, pillarY), size = androidx.compose.ui.geometry.Size(pillarWidth, pillarHeight))
            drawRect(color = primaryColor, topLeft = Offset(w * 0.37f, pillarY), size = androidx.compose.ui.geometry.Size(pillarWidth, pillarHeight))
            drawRect(color = primaryColor, topLeft = Offset(w * 0.54f, pillarY), size = androidx.compose.ui.geometry.Size(pillarWidth, pillarHeight))
            drawRect(color = primaryColor, topLeft = Offset(w * 0.73f, pillarY), size = androidx.compose.ui.geometry.Size(pillarWidth, pillarHeight))

            // 4. Classical Base Plinth (2-step foundation)
            drawRect(color = secondaryColor, topLeft = Offset(w * 0.14f, h * 0.79f), size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.055f))
            drawRect(color = mutedColor, topLeft = Offset(w * 0.10f, h * 0.86f), size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.055f))

            // 5. Center Diamond Sparkle
            val starPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.5f, h * 0.49f)
                lineTo(w * 0.53f, h * 0.59f)
                lineTo(w * 0.63f, h * 0.61f)
                lineTo(w * 0.53f, h * 0.63f)
                lineTo(w * 0.5f, h * 0.73f)
                lineTo(w * 0.47f, h * 0.63f)
                lineTo(w * 0.37f, h * 0.61f)
                lineTo(w * 0.47f, h * 0.59f)
                close()
            }
            drawPath(starPath, color = primaryColor)
        }
    }
}

// ─── Native Play Store Install Permission Prompt ──────────────────────────────
@Composable
private fun ClassicPermissionDialog(
    appName: String,
    onGrant: () -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalStoreTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        titleContentColor = theme.textPrimary,
        textContentColor = theme.textMuted,
        title = {
            Text("PACKAGE INSTALLATION PERMISSION", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "To install and update $appName directly through Our Store, Android requires authorization to install packages from this source.",
                    color = theme.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Text(
                    "Tap 'Grant Permission', toggle ON 'Allow from this source', and the installation will proceed immediately.",
                    fontSize = 12.sp,
                    color = theme.textMuted,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.textPrimary)
                    .tactilePress(pressedScale = 0.92f) { onGrant() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("GRANT PERMISSION", color = theme.background, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = theme.textMuted, fontSize = 12.sp)
            }
        }
    )
}

// ─── Play Protect & Security Status Dialog ────────────────────────────────────
@Composable
private fun PlayProtectSecurityDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val theme = LocalStoreTheme.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        titleContentColor = theme.textPrimary,
        textContentColor = theme.textMuted,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = EmeraldStatus,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "PLAY PROTECT & SAFETY",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldStatusBg)
                        .border(1.dp, EmeraldStatus.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldStatus,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "100% CLEAN • CRYPTOGRAPHICALLY VERIFIED",
                            color = EmeraldStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Text(
                    text = "Why does Google Play Protect show warnings?",
                    color = theme.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Our Store is an independent direct-distribution hub. Because it is distributed privately and not through Google's commercial Play Store, Play Protect checks the private signing certificate and flags it as an 'Unrecognized Developer'.",
                    fontSize = 12.sp,
                    color = theme.textSecondary,
                    lineHeight = 16.sp
                )

                Text(
                    text = "How to proceed if Play Protect prompts you:",
                    color = theme.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "1. When prompted during install, tap 'More details'.\n2. Tap 'Install anyway'.\n3. Tap 'Manage Play Protect' below to configure device scanning preferences.",
                    fontSize = 12.sp,
                    color = theme.textSecondary,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(theme.textPrimary)
                    .tactilePress(pressedScale = 0.92f) {
                        onDismiss()
                        onOpenSettings()
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("MANAGE PLAY PROTECT", color = theme.background, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("GOT IT", color = theme.textMuted, fontSize = 12.sp)
            }
        }
    )
}

// ─── Dedicated Connecto SSO Login Gate Screen ─────────────────────────────────
@Composable
fun ConnectoLoginGateScreen(
    loginState: LoginUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onLoginConnecto: () -> Unit,
    onClearLoginError: () -> Unit
) {
    val context = LocalContext.current
    val theme = LocalStoreTheme.current
    val isLoading = loginState is LoginUiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Top right theme toggle button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(38.dp)
                .clip(CircleShape)
                .background(theme.cardElevated)
                .border(1.dp, theme.cardBorder, CircleShape)
                .tactilePress(pressedScale = 0.88f) { onToggleTheme() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                contentDescription = "Toggle Appearance Theme",
                tint = if (isDarkTheme) AmberUpdate else theme.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Elegant Master Insignia
            OurStoreLogoMark(modifier = Modifier.size(76.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "OUR STORE",
                    color = theme.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )

                Text(
                    text = "Autonomous App Ecosystem Hub",
                    color = theme.textMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Sign in with your Connecto account to access curated applications, tools, and background updates.",
                    color = theme.textDarkMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Error alert if SSO failed
            if (loginState is LoginUiState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DangerRedBg)
                        .border(1.dp, DangerRedBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
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
                            tint = theme.textMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onClearLoginError() }
                        )
                    }
                }
            }

            // Prominent Connecto SSO Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surface)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLoading) Color(0xFF4A3E9E) else Color(0xFF6C5CE7))
                            .tactilePress(
                                pressedScale = 0.95f,
                                onClick = if (!isLoading) onLoginConnecto else null
                            )
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Login,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "LOGIN WITH CONNECTO",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Quick launch Connecto if user needs to open it
                    if (loginState is LoginUiState.Error && loginState.message.contains("Connecto is installed, but no account is currently signed in")) {
                        Text(
                            text = "Launch Connecto App →",
                            color = Color(0xFFA29BFE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage("com.connecto.app")
                                    if (launchIntent != null) context.startActivity(launchIntent)
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Classic Login & Account Screen ───────────────────────────────────────────
@Composable
fun ClassicLoginAccountView(
    currentUser: StoreUser?,
    loginState: LoginUiState,
    storeSelfUpdate: AppUiState? = null,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onUpdateOurStore: () -> Unit = {},
    onLoginCredentials: (String, String) -> Unit,
    onLoginConnecto: () -> Unit,
    onLogout: () -> Unit,
    onClearLoginError: () -> Unit
) {
    val theme = LocalStoreTheme.current
    var showLegalDialog by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Dynamic Our Store In-App Update Card (ONLY shown when update available)
        if (storeSelfUpdate != null && storeSelfUpdate.installState == StoreApp.InstallState.UPDATE_AVAILABLE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surface)
                    .border(1.dp, AmberUpdate.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AmberUpdate)
                            )
                            Text(
                                text = "OUR STORE UPDATE READY",
                                color = AmberUpdate,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "v${storeSelfUpdate.app.versionName}",
                            color = theme.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "A new version of Our Store is available with system stability, installer enhancements, and background optimizations.",
                        color = theme.textMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    if (storeSelfUpdate.isDownloading || storeSelfUpdate.isInstalling) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (storeSelfUpdate.isInstalling) "Installing directly into OS…" else "Downloading package…",
                                    color = theme.textPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (storeSelfUpdate.isInstalling) "Please wait" else "${storeSelfUpdate.downloadProgress}%",
                                    color = theme.textMuted,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            LinearProgressIndicator(
                                progress = { if (storeSelfUpdate.isInstalling) 1f else storeSelfUpdate.downloadProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = theme.textPrimary,
                                trackColor = theme.cardElevated
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberUpdate)
                                .tactilePress(pressedScale = 0.94f) { onUpdateOurStore() }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "UPDATE OUR STORE NOW",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (currentUser != null) {
            // Logged In Account Dashboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.surface)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val ringColor = if (currentUser.role == StoreRole.ADMIN) Color(0xFF10B981) else Color(0xFF6C5CE7)
                    Box(
                        modifier = Modifier.size(88.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarImage(
                            avatarUrl = currentUser.avatarUrl,
                            displayName = currentUser.displayName,
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, ringColor, CircleShape),
                            fallback = {
                                Box(
                                    modifier = Modifier
                                        .size(82.dp)
                                        .clip(CircleShape)
                                        .background(ringColor.copy(alpha = 0.15f))
                                        .border(2.5.dp, ringColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (currentUser.role == StoreRole.ADMIN) Icons.Rounded.AdminPanelSettings else Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = ringColor,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                        )

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(theme.cardElevated)
                                .border(1.5.dp, theme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentUser.role == StoreRole.ADMIN) Icons.Rounded.Security else Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = ringColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentUser.displayName,
                            color = theme.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "@${currentUser.username}",
                            color = theme.textMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Role Badge Pill
                    val badgeBg = if (currentUser.role == StoreRole.ADMIN) EmeraldStatusBg else Color(0xFF261D45)
                    val badgeBorder = if (currentUser.role == StoreRole.ADMIN) EmeraldStatus.copy(alpha = 0.4f) else Color(0xFF6C5CE7).copy(alpha = 0.5f)
                    val badgeFg = if (currentUser.role == StoreRole.ADMIN) EmeraldStatus else Color(0xFFA29BFE)
                    val roleLabel = if (currentUser.role == StoreRole.ADMIN) "SHIELD • SYSTEM ADMINISTRATOR" else "VERIFIED MEMBER"

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, RoundedCornerShape(100.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = roleLabel,
                            color = badgeFg,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }

                    if (currentUser.isConnectoSso) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFFA29BFE),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Authenticated via Connecto SSO",
                                color = Color(0xFFA29BFE),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ─── Theme Preference & Mode Switcher Card ─────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(theme.cardElevated)
                            .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Palette,
                                        contentDescription = null,
                                        tint = theme.accentColorLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "VISUAL THEME & APPEARANCE",
                                        color = theme.textPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Text(
                                    text = if (isDarkTheme) "OLED NOIR" else "CLEAN WHITE",
                                    color = if (isDarkTheme) AmberUpdate else theme.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Dark Mode Option Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDarkTheme) Color(0xFF000000) else theme.surface)
                                        .border(
                                            width = if (isDarkTheme) 2.dp else 1.dp,
                                            color = if (isDarkTheme) AmberUpdate else theme.cardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .tactilePress(pressedScale = 0.94f) {
                                            if (!isDarkTheme) onToggleTheme()
                                        }
                                        .padding(vertical = 12.dp, horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DarkMode,
                                            contentDescription = null,
                                            tint = if (isDarkTheme) AmberUpdate else theme.textDarkMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "OLED Noir",
                                            color = if (isDarkTheme) Color.White else theme.textSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isDarkTheme) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }

                                // Light Mode Option Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (!isDarkTheme) Color(0xFFF8FAFC) else theme.cardElevated)
                                        .border(
                                            width = if (!isDarkTheme) 2.dp else 1.dp,
                                            color = if (!isDarkTheme) theme.accentColor else theme.cardBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .tactilePress(pressedScale = 0.94f) {
                                            if (isDarkTheme) onToggleTheme()
                                        }
                                        .padding(vertical = 12.dp, horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.LightMode,
                                            contentDescription = null,
                                            tint = if (!isDarkTheme) theme.accentColor else theme.textDarkMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Clean White",
                                            color = if (!isDarkTheme) Color(0xFF0F172A) else theme.textMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (!isDarkTheme) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Logout Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DangerRedBg)
                            .border(1.dp, DangerRedBorder, RoundedCornerShape(12.dp))
                            .tactilePress(pressedScale = 0.95f) { onLogout() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "SIGN OUT / SWITCH ACCOUNT",
                                color = DangerRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // App Version & System Specs Status Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.cardElevated)
                            .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Our Store Client",
                                    color = theme.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "v${com.connecto.store.BuildConfig.VERSION_NAME} (Build ${com.connecto.store.BuildConfig.VERSION_CODE})",
                                    color = theme.textDarkMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldStatusBg)
                                    .border(1.dp, EmeraldStatus.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "INSTALLED & ACTIVE",
                                    color = EmeraldStatus,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Logged Out Connecto SSO View
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Welcome to Our Store",
                    color = theme.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Sign in to access your curated applications and personalized tools.",
                    color = theme.textMuted,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
            }

            // Error banner if any
            if (loginState is LoginUiState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DangerRedBg)
                        .border(1.dp, DangerRedBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
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
                            tint = theme.textMuted,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onClearLoginError() }
                        )
                    }
                }
            }

            // Connecto SSO Card
            val isLoading = loginState is LoginUiState.Loading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surface)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6C5CE7).copy(alpha = 0.15f))
                            .border(1.5.dp, Color(0xFF6C5CE7).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = null,
                            tint = Color(0xFFA29BFE),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Connecto Single Sign-On",
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Sign in directly with your Connecto profile on this device.",
                        color = theme.textMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLoading) Color(0xFF4A3E9E) else Color(0xFF6C5CE7))
                            .tactilePress(
                                pressedScale = 0.95f,
                                onClick = if (!isLoading) onLoginConnecto else null
                            )
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Login,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "LOGIN WITH CONNECTO",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Legal Footer for Logged-Out View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "By signing in or installing apps from Our Store, you agree to our",
                    color = theme.textDarkMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms of Use",
                        color = Color(0xFF6C5CE7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showLegalDialog = 0 }
                    )
                    Text(
                        text = "•",
                        color = theme.textDarkMuted,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Privacy Policy",
                        color = Color(0xFF6C5CE7),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showLegalDialog = 1 }
                    )
                }
            }
        }
    }

    if (showLegalDialog != null) {
        OurStoreLegalDialog(
            initialTab = showLegalDialog!!,
            onDismiss = { showLegalDialog = null }
        )
    }
}

// ─── Asynchronous Profile Avatar Loader ───────────────────────────────────────
private val avatarBitmapCache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap>()

@Composable
fun UserAvatarImage(
    avatarUrl: String,
    displayName: String,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit
) {
    val fullUrl = remember(avatarUrl, displayName) {
        val cleanName = displayName.trim().lowercase().removePrefix("@")
        when {
            avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://") -> avatarUrl
            avatarUrl.startsWith("/") -> "https://connecto.fun$avatarUrl"
            avatarUrl.isNotBlank() && !avatarUrl.startsWith("file:") && !avatarUrl.startsWith("/data/") -> "https://connecto.fun/$avatarUrl"
            cleanName == "chinnu14754x" || cleanName == "chinnu" -> "https://connecto.fun/uploads/avatars/chinnu_avatar_verified.webp"
            else -> ""
        }
    }

    var bitmap by remember(fullUrl) { mutableStateOf(avatarBitmapCache[fullUrl]) }

    LaunchedEffect(fullUrl) {
        if (fullUrl.isBlank()) {
            bitmap = null
            return@LaunchedEffect
        }
        if (avatarBitmapCache.containsKey(fullUrl)) {
            bitmap = avatarBitmapCache[fullUrl]
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val url = java.net.URL(fullUrl)
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.instanceFollowRedirects = true
                conn.setRequestProperty("User-Agent", "OurStore-Android/1.6.4")
                conn.connect()
                if (conn.responseCode in 200..299) {
                    val bytes = conn.inputStream.use { it.readBytes() }
                    val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        val imgBmp = bmp.asImageBitmap()
                        avatarBitmapCache[fullUrl] = imgBmp
                        withContext(Dispatchers.Main) {
                            bitmap = imgBmp
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = "$displayName's profile picture",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        fallback()
    }
}
