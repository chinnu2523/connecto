package com.connecto.store.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.sin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.connecto.store.ui.components.StoreDragToRefresh
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
import com.connecto.store.ui.components.StoreButtonState
import com.connecto.store.ui.components.StoreFiveStateButton

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
    val accentColor: Color = Color(0xFF2563EB),
    val accentColorLight: Color = Color(0xFF3B82F6)
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
    accentColor = Color(0xFF2563EB),
    accentColorLight = Color(0xFF3B82F6)
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
    accentColor = Color(0xFF2563EB),
    accentColorLight = Color(0xFF1D4ED8)
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
        animationSpec = tween(
            durationMillis = 160,
            easing = if (isPressed) EaseInCubic else EaseOutCubic
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
private val ElectricBlueUpdate = Color(0xFF2563EB)
private val ElectricBlueUpdateLight = Color(0xFF3B82F6)
private val ElectricBlueUpdateBg = Color(0xFF0F172A)
private val DangerRed = Color(0xFFEF4444)
private val DangerRedBg = Color(0xFF2E1010)
private val DangerRedBorder = Color(0xFF7F1D1D)

// ─── Navigation Tabs ───────────────────────────────────────────────────────────
enum class StoreNavSection(
    val label: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OVERVIEW("Store", Icons.Rounded.Storefront, Icons.Outlined.Storefront),
    UPDATES("Updates", Icons.Rounded.ArrowCircleUp, Icons.Outlined.ArrowCircleUp),
    LOGIN("Account", Icons.Rounded.Person, Icons.Outlined.Person);

    val icon: androidx.compose.ui.graphics.vector.ImageVector get() = activeIcon
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
    var catalogScrollToTopTrigger by remember { mutableStateOf(0) }
    var accountScrollToTopTrigger by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedPackageName by remember { mutableStateOf<String?>(null) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    // Respond to deep-link parameters from Android system notifications
    LaunchedEffect(initialPackageName, initialSectionName, currentUser) {
        if (initialPackageName != null) {
            val role = currentUser?.role ?: StoreRole.GUEST
            if ((initialPackageName == "com.omni.assistant" || initialPackageName == "jarvis") && role != StoreRole.ADMIN) {
                selectedPackageName = null
            } else {
                selectedPackageName = initialPackageName
            }
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

    // RBAC Security Guard: Ensure non-admin users cannot remain inside JARVIS AI app details
    LaunchedEffect(currentUser, selectedPackageName) {
        val role = currentUser?.role ?: StoreRole.GUEST
        if ((selectedPackageName == "com.omni.assistant" || selectedPackageName == "jarvis") && role != StoreRole.ADMIN) {
            selectedPackageName = null
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
                    StoreLoginScreen(
                        loginState = loginState,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onLoginCredentials = { u, p -> vm.loginWithCredentials(u, p) },
                        onRegisterCredentials = { u, p, d, e -> vm.registerWithCredentials(u, p, d, e) },
                        onLoginConnecto = { vm.loginWithConnecto(context) },
                        onContinueAsGuest = { vm.continueAsGuest() },
                        onBiometricLogin = {
                            val activity = context as? androidx.fragment.app.FragmentActivity
                            if (activity != null) {
                                com.connecto.store.auth.StoreBiometricHelper.promptBiometric(
                                    activity = activity,
                                    title = "Unlock Our Store",
                                    subtitle = "Touch sensor to sign in",
                                    onSuccess = { vm.continueAsGuest() },
                                    onError = { /* ignored */ },
                                    onNegativeButton = {}
                                )
                            }
                        },
                        isBiometricAvailable = com.connecto.store.auth.StoreBiometricHelper.isBiometricAvailable(context),
                        onClearLoginError = { vm.clearLoginError() }
                    )
                } else {
                    AnimatedContent(
                        targetState = selectedPackageName,
                        transitionSpec = {
                            if (targetState != null) {
                                // Opening App Detail: fluid ease-in/ease-out curves slide up + scale in + fade in
                                (slideInVertically(
                                    initialOffsetY = { it / 5 },
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = EaseOutCubic
                                    )
                                ) + fadeIn(animationSpec = tween(260, easing = EaseOutCubic)) +
                                scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = EaseOutCubic
                                    )
                                )) togetherWith (
                                    fadeOut(animationSpec = tween(180, easing = EaseInCubic)) +
                                    scaleOut(targetScale = 0.96f, animationSpec = tween(180, easing = EaseInCubic))
                                )
                            } else {
                                // Returning to Catalog: fluid ease-in/ease-out curves slide down + scale in + fade in
                                (fadeIn(animationSpec = tween(260, easing = EaseOutCubic)) +
                                scaleIn(
                                    initialScale = 0.96f,
                                    animationSpec = tween(
                                        durationMillis = 280,
                                        easing = EaseOutCubic
                                    )
                                )) togetherWith (
                                    slideOutVertically(
                                        targetOffsetY = { it / 5 },
                                        animationSpec = tween(
                                            durationMillis = 240,
                                            easing = EaseInCubic
                                        )
                                    ) + fadeOut(animationSpec = tween(200, easing = EaseInCubic)) +
                                    scaleOut(targetScale = 0.94f, animationSpec = tween(200, easing = EaseInCubic))
                                )
                            }
                        },
                        label = "appDetailModalTransition"
                    ) { targetPkg ->
                        val activeDetailApp = targetPkg?.let { pkg ->
                            s.apps.find { it.app.packageName == pkg }
                        }
                        if (activeDetailApp != null) {
                            // Dedicated Full-Screen Application Detail & Preview View
                            ClassicAppDetailView(
                                appState = activeDetailApp,
                                onBack = { selectedPackageName = null },
                                onAction = { vm.handleAppAction(context, activeDetailApp) },
                                onUninstall = { vm.uninstallApp(context, activeDetailApp) },
                                onCancelDownload = { vm.cancelDownload(activeDetailApp.app.packageName) },
                                onToggleBookmark = { vm.toggleBookmark(activeDetailApp.app.id) },
                                onRateApp = { stars -> vm.rateApp(activeDetailApp.app.id, stars) }
                            )
                        } else {
                            // Catalog Browsing View with Header, Filter & Footer Navigation
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Executive Top Header (No Top Profile, Refresh, or Security buttons)
                                ClassicTopHeader(
                                    searchQuery = searchQuery,
                                    onSearchChange = { searchQuery = it },
                                    showSearch = selectedSection == StoreNavSection.OVERVIEW,
                                    isRefreshing = s.isRefreshing
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
                                                (selectedCategory == "Saved" && appState.isBookmarked) ||
                                                appState.app.category.equals(selectedCategory, ignoreCase = true)

                                            val matchesSection = when (selectedSection) {
                                                StoreNavSection.OVERVIEW -> true
                                                StoreNavSection.UPDATES -> appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE || appState.isDownloading || appState.isInstalling
                                                StoreNavSection.LOGIN -> true
                                            }

                                            matchesSearch && matchesCategory && matchesSection
                                        }
                                    }

                                    AnimatedContent(
                                        targetState = selectedSection,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(220, easing = EaseOutCubic)) + slideInHorizontally(
                                                initialOffsetX = { if (targetState.ordinal > initialState.ordinal) it / 6 else -it / 6 },
                                                animationSpec = tween(240, easing = EaseOutCubic)
                                            )) togetherWith (
                                                fadeOut(animationSpec = tween(180, easing = EaseInCubic)) + slideOutHorizontally(
                                                    targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -it / 6 else it / 6 },
                                                    animationSpec = tween(200, easing = EaseInCubic)
                                                )
                                            )
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
                                                onRegisterCredentials = { u, p, d, e -> vm.registerWithCredentials(u, p, d, e) },
                                                onLoginConnecto = { vm.loginWithConnecto(context) },
                                                onLogout = { vm.logout() },
                                                onClearLoginError = { vm.clearLoginError() },
                                                scrollToTopTrigger = accountScrollToTopTrigger,
                                                isRefreshing = s.isRefreshing,
                                                onRefresh = {
                                                    vm.syncSsoProfileIfAvailable()
                                                    vm.loadCatalog(silently = true)
                                                }
                                            )
                                        } else {
                                            ClassicCatalogBody(
                                                state = s,
                                                filteredApps = filteredApps,
                                                selectedCategory = selectedCategory,
                                                onSelectCategory = { selectedCategory = it },
                                                onAppClick = { selectedPackageName = it.app.packageName },
                                                onAppAction = { appState -> vm.handleAppAction(context, appState) },
                                                onCancelDownload = { appState -> vm.cancelDownload(appState.app.packageName) },
                                                onToggleBookmark = { appState -> vm.toggleBookmark(appState.app.id) },
                                                onUpdateAll = { vm.updateAll(context) },
                                                onRefresh = { vm.loadCatalog(silently = true) },
                                                isUpdatesTab = currentSection == StoreNavSection.UPDATES,
                                                scrollToTopTrigger = catalogScrollToTopTrigger
                                            )
                                        }
                                    }
                                }

                                // Interactive Modern Footer Navigation Bar
                                ClassicBottomNavBar(
                                    selected = selectedSection,
                                    onSelect = { section ->
                                        if (section == selectedSection) {
                                            if (section == StoreNavSection.OVERVIEW || section == StoreNavSection.UPDATES) {
                                                catalogScrollToTopTrigger++
                                            } else if (section == StoreNavSection.LOGIN) {
                                                accountScrollToTopTrigger++
                                            }
                                        }
                                        selectedSection = section
                                    },
                                    updateCount = s.apps.count {
                                        it.installState == StoreApp.InstallState.UPDATE_AVAILABLE
                                    },
                                    currentUser = currentUser
                                )
                            }
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
    showSearch: Boolean = true,
    isRefreshing: Boolean = false
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
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Master Architectural Obsidian Store Insignia
                OurStoreLogoMark(modifier = Modifier.size(40.dp))

                Text(
                    text = "OUR STORE",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp
                )
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

        // Sleek 2.5dp Edge Sync Shimmer when updating in the background
        if (isRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = theme.accentColor,
                trackColor = Color.Transparent
            )
        }
    }
}

// ─── Modern Architectural Bottom Navigation Bar ──────────────────────────────
@Composable
private fun ClassicBottomNavBar(
    selected: StoreNavSection,
    onSelect: (StoreNavSection) -> Unit,
    updateCount: Int,
    currentUser: StoreUser? = null
) {
    val theme = LocalStoreTheme.current
    val sections = remember { StoreNavSection.values() }
    val selectedIndex = sections.indexOf(selected).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(68.dp)
            .background(theme.navBarBackground)
    ) {
        val totalWidth = maxWidth
        val tabCount = sections.size
        val tabWidth = totalWidth / tabCount

        // Center X for active tab sliding indicator with ease-in/ease-out curve
        val targetCenterX = tabWidth * selectedIndex + tabWidth / 2
        val animatedCenterX by animateDpAsState(
            targetValue = targetCenterX,
            animationSpec = tween(
                durationMillis = 280,
                easing = EaseInOutCubic
            ),
            label = "storeActiveTabCenterX"
        )

        // Top hairline divider
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).align(Alignment.TopCenter)) {
            drawLine(
                color = theme.dividerColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Active Tab Sliding Dock Pill
        val pillWidth = (tabWidth - 16.dp).coerceAtMost(96.dp)
        Box(
            modifier = Modifier
                .offset(x = animatedCenterX - pillWidth / 2, y = 10.dp)
                .width(pillWidth)
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (theme.isDark) Color(0xFF16161A) else Color(0xFFF1F5F9)
                )
                .border(
                    width = 0.8.dp,
                    color = if (theme.isDark) theme.cardBorder.copy(alpha = 0.9f) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        // Active Tab Top Illuminated Micro-Pip
        val pipWidth = 26.dp
        Box(
            modifier = Modifier
                .offset(x = animatedCenterX - pipWidth / 2, y = 0.dp)
                .width(pipWidth)
                .height(2.5.dp)
                .clip(RoundedCornerShape(bottomStart = 1.5.dp, bottomEnd = 1.5.dp))
                .background(theme.accentColorLight)
        )

        // Tab items row
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            sections.forEach { section ->
                val isSelected = selected == section
                val badgeCount = if (section == StoreNavSection.UPDATES) updateCount else 0

                ClassicBottomNavTabItem(
                    section = section,
                    isSelected = isSelected,
                    badgeCount = badgeCount,
                    currentUser = currentUser,
                    onClick = { onSelect(section) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun ClassicBottomNavTabItem(
    section: StoreNavSection,
    isSelected: Boolean,
    badgeCount: Int,
    currentUser: StoreUser? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalStoreTheme.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = if (isPressed) 0.92f else if (isSelected) 1.04f else 1.0f
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = 180,
            easing = if (isPressed) EaseInCubic else EaseOutCubic
        ),
        label = "navTabScale"
    )

    val iconOffsetY by animateDpAsState(
        targetValue = if (isSelected) (-2.dp) else 0.dp,
        animationSpec = tween(
            durationMillis = 220,
            easing = EaseInOutCubic
        ),
        label = "navIconOffsetY"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) (if (theme.isDark) Color.White else theme.accentColor) else theme.textDarkMuted,
        animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
        label = "navTabFg"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                try {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                } catch (_: Exception) {}
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.offset(y = iconOffsetY)
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                if (section == StoreNavSection.LOGIN && currentUser != null && !currentUser.username.equals("guest", ignoreCase = true)) {
                    UserAvatarImage(
                        avatarUrl = currentUser.avatarUrl,
                        displayName = currentUser.displayName,
                        modifier = Modifier
                            .size(23.dp)
                            .clip(CircleShape)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) theme.accentColorLight else theme.textDarkMuted.copy(alpha = 0.45f),
                                shape = CircleShape
                            ),
                        fallback = {
                            Box(
                                modifier = Modifier
                                    .size(23.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) theme.accentColorLight.copy(alpha = 0.25f) else theme.cardBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.displayName.take(1).uppercase(),
                                    color = if (isSelected) theme.accentColorLight else theme.textSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                } else {
                    val iconVector = if (isSelected) section.activeIcon else section.inactiveIcon
                    Icon(
                        imageVector = iconVector,
                        contentDescription = section.label,
                        tint = animatedContentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 8.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(ElectricBlueUpdateLight)
                            .border(1.5.dp, if (theme.isDark) Color(0xFF000000) else Color(0xFFFFFFFF), CircleShape)
                            .padding(horizontal = 4.5.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "$badgeCount",
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
            }

            Text(
                text = section.label,
                color = animatedContentColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = if (isSelected) 11.5.sp else 11.sp,
                letterSpacing = 0.2.sp,
                maxLines = 1
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
    onCancelDownload: (AppUiState) -> Unit,
    onToggleBookmark: (AppUiState) -> Unit,
    onUpdateAll: () -> Unit,
    onRefresh: () -> Unit = {},
    isUpdatesTab: Boolean,
    scrollToTopTrigger: Int = 0
) {
    val theme = LocalStoreTheme.current
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            try {
                if (listState.firstVisibleItemIndex > 15) {
                    listState.scrollToItem(15)
                }
                listState.animateScrollToItem(0)
            } catch (_: Exception) {}
        }
    }

    val categories = remember(state.apps) {
        listOf("All", "Saved") + state.apps.map { it.app.category }.filter { it != "Saved" && it != "All" }.distinct()
    }
    val updateCount = state.apps.count { it.installState == StoreApp.InstallState.UPDATE_AVAILABLE }

    StoreDragToRefresh(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        canScrollBackward = {
            listState.canScrollBackward ||
            listState.firstVisibleItemIndex > 0 ||
            listState.firstVisibleItemScrollOffset > 0
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (updateCount > 0) ElectricBlueUpdateLight else EmeraldStatus)
                        )
                        Text(
                            text = if (updateCount > 0) "$updateCount update${if (updateCount > 1) "s" else ""} ready" else "All apps up to date",
                            color = if (updateCount > 0) ElectricBlueUpdateLight else EmeraldStatus,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
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
                    onActionClick = { onAppAction(appState) },
                    onCancelDownload = { onCancelDownload(appState) },
                    onToggleBookmark = { onToggleBookmark(appState) }
                )
            }
        }

        // Professional Footer Sign-Off
        item {
            ClassicFooterInfo()
        }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (category == "Saved") {
                        Icon(
                            imageVector = Icons.Rounded.Bookmark,
                            contentDescription = null,
                            tint = if (isSelected) theme.background else theme.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
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
}

// ─── Ecosystem Spotlight Hero Card (Linear / x.ai Modern Aesthetic) ───────────
@Composable
private fun ClassicEcosystemSpotlightCard(
    featuredApp: AppUiState?,
    onOpenDetail: () -> Unit
) {
    val theme = LocalStoreTheme.current
    val isDark = theme.isDark

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .tactilePress(pressedScale = 0.98f) { onOpenDetail() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = theme.borderHighlight
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Top row: Tag & Live Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8))
                        )
                        Text(
                            text = "ECOSYSTEM SPOTLIGHT",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Text(
                        text = "CURATED RELEASE",
                        color = theme.textMuted,
                        fontSize = 10.sp,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Headline & Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Next-Gen Decentralized Private Suite",
                        color = theme.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Military-grade end-to-end encrypted messaging, studio HD voice, and autonomous 1-tap package distribution.",
                        color = theme.textMuted,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                }

                // Telemetry Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Sub-5ms IPC",
                        "End-to-End Encrypted",
                        "Pure OLED #000"
                    ).forEach { pill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(theme.cardBorder.copy(alpha = 0.5f))
                                .border(1.dp, theme.borderHighlight.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = pill,
                                color = theme.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── App Store Style Curated App Item (Clean Minimalist Front Row) ─────────────
@Composable
private fun ClassicAppCard(
    appState: AppUiState,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    onCancelDownload: () -> Unit = {},
    onToggleBookmark: () -> Unit = {}
) {
    val theme = LocalStoreTheme.current
    val app = appState.app

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .tactilePress(pressedScale = 0.98f) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Authentic Application Logo / Rounded Squircle Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.cardElevated)
                    .border(1.dp, theme.borderHighlight, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppIconImage(appId = app.id, appName = app.name)
            }

            // App Name & Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = app.name,
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp,
                    letterSpacing = (-0.3).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = app.tagline,
                    color = theme.textMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = app.sizeFormatted,
                        color = theme.textDarkMuted,
                        fontSize = 11.sp,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (appState.userRating > 0) {
                        Text(
                            text = "• ${appState.userRating}.0 ★",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Bookmark Heart / Ribbon Action
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (appState.isBookmarked) theme.cardElevated else Color.Transparent)
                    .border(1.dp, if (appState.isBookmarked) theme.accentColor else Color.Transparent, CircleShape)
                    .tactilePress(pressedScale = 0.85f) { onToggleBookmark() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (appState.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (appState.isBookmarked) theme.accentColor else theme.textDarkMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Single App Store Style Tactile Action Button
            val (actionLabel, actionBg, actionFg) = when {
                appState.error != null -> Triple("RETRY", DangerRed, Color.White)
                appState.isInstalling -> Triple("INSTALLING…", ElectricBlueUpdate.copy(alpha = 0.15f), ElectricBlueUpdateLight)
                appState.isDownloading -> Triple("${appState.downloadProgress}% ✕", theme.cardBorder, theme.textPrimary)
                appState.installState == StoreApp.InstallState.NOT_INSTALLED -> Triple("GET", theme.cardElevated, theme.textPrimary)
                appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> Triple("UPDATE", ElectricBlueUpdate, Color.White)
                else -> Triple("OPEN", theme.cardElevated, theme.textPrimary)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(actionBg)
                    .border(
                        1.dp,
                        when {
                            appState.error != null -> DangerRedBorder
                            appState.isInstalling -> ElectricBlueUpdate.copy(alpha = 0.60f)
                            appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> ElectricBlueUpdate
                            else -> theme.borderHighlight
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .tactilePress(
                        pressedScale = 0.90f,
                        onClick = when {
                            appState.isDownloading -> onCancelDownload
                            appState.isInstalling -> null
                            else -> onActionClick
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                if (appState.isInstalling) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(11.dp),
                            color = ElectricBlueUpdateLight,
                            strokeWidth = 1.8.dp
                        )
                        Text(
                            text = "INSTALLING…",
                            color = ElectricBlueUpdateLight,
                            fontSize = 11.sp,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Text(
                        text = actionLabel,
                        color = actionFg,
                        fontSize = 11.5.sp,
                        style = TextStyle(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
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
    onUninstall: () -> Unit,
    onCancelDownload: () -> Unit = {},
    onToggleBookmark: () -> Unit = {},
    onRateApp: (Int) -> Unit = {}
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
        // Top Back Navigation Bar with Bookmark Action
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

                Text(
                    text = app.name,
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.3).sp
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(theme.cardElevated)
                    .border(1.dp, if (appState.isBookmarked) theme.accentColor else theme.cardBorder, CircleShape)
                    .tactilePress(pressedScale = 0.88f) { onToggleBookmark() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (appState.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (appState.isBookmarked) theme.accentColor else theme.textPrimary,
                    modifier = Modifier.size(19.dp)
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
                                style = TextStyle(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold
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
                            if (appState.isInstalling) "Please wait…" else "${appState.downloadProgress}%",
                            color = if (appState.isInstalling) ElectricBlueUpdateLight else theme.textMuted,
                            fontSize = 12.sp,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (appState.isInstalling) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = ElectricBlueUpdateLight,
                            trackColor = theme.cardElevated
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { appState.downloadProgress / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = theme.textPrimary,
                            trackColor = theme.cardElevated
                        )
                    }
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
                                appState.isInstalling -> ElectricBlueUpdate.copy(alpha = 0.20f)
                                appState.isDownloading -> theme.cardBorder
                                appState.installState == StoreApp.InstallState.NOT_INSTALLED -> theme.textPrimary
                                appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> ElectricBlueUpdate
                                else -> theme.textPrimary
                            }
                        )
                        .then(
                            if (appState.isInstalling) {
                                Modifier.border(1.dp, ElectricBlueUpdate.copy(alpha = 0.70f), RoundedCornerShape(12.dp))
                            } else {
                                Modifier
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
                            appState.isInstalling -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = ElectricBlueUpdateLight,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "INSTALLING…",
                                    color = ElectricBlueUpdateLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            appState.isDownloading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = theme.textPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "DOWNLOADING (${appState.downloadProgress}%)…",
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
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "UPDATE (v${app.versionName})",
                                    color = Color.White,
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

                // Dedicated 0ms Optimistic Cancel Button when Downloading
                if (appState.isDownloading) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.cardElevated)
                            .border(1.dp, DangerRedBorder, RoundedCornerShape(12.dp))
                            .tactilePress(pressedScale = 0.90f) { onCancelDownload() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                            Text("CANCEL", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.Black)
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
            AppPreviewCarousel(appId = app.id, appName = app.name)

            // Interactive Community & User Rating (0ms Optimistic local updates)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surface)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RATINGS & REVIEWS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = theme.textMuted
                    )
                    if (appState.userRating > 0) {
                        Text(
                            text = "Rated: ${appState.userRating}/5",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                            color = Color(0xFFF59E0B)
                        )
                    } else {
                        Text(
                            text = "Tap a star to rate",
                            fontSize = 11.sp,
                            color = theme.textDarkMuted
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { starIndex ->
                        val isFilled = starIndex <= appState.userRating
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(CircleShape)
                                .tactilePress(pressedScale = 0.82f) { onRateApp(starIndex) }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "$starIndex Stars",
                                tint = if (isFilled) Color(0xFFF59E0B) else theme.textDarkMuted,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }

            // Description Section
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

            // What's New Section
            if (app.changelog.isNotEmpty()) {
                ClassicChangelogBox(app.changelog)
            }

            // Technical Details Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(theme.surface)
                    .border(1.dp, theme.cardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TechSpecRow("Version", "v${app.versionName}")
                    TechSpecRow("Download Size", app.sizeFormatted)
                    TechSpecRow("Category", app.category)
                    TechSpecRow("Compatibility", "Android 8.0+")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Application Previews Carousel & Mockups ─────────────────────────────────
@Composable
private fun AppPreviewCarousel(appId: String, appName: String = "") {
    val previews = remember(appId) { getAppPreviews(appId) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        itemsIndexed(previews) { index, preview ->
            PhoneMockupCard(
                preview = preview,
                onClick = { selectedIndex = index }
            )
        }
    }

    selectedIndex?.let { index ->
        ScreenshotLightboxDialog(
            previews = previews,
            initialIndex = index,
            appName = appName,
            onDismiss = { selectedIndex = null }
        )
    }
}

data class AppScreenshotPreview(
    val assetPath: String,
    val caption: String,
    val tag: String
)

private fun getAppPreviews(appId: String): List<AppScreenshotPreview> = when (appId.lowercase()) {
    "ourstore" -> listOf(
        AppScreenshotPreview("previews/ourstore_1.png", "Curated Ecosystem Catalog", "STOREFRONT"),
        AppScreenshotPreview("previews/ourstore_2.png", "Autonomous 1-Tap Updates", "UPDATES"),
        AppScreenshotPreview("previews/ourstore_3.png", "Profile & Security Vault", "ACCOUNT"),
        AppScreenshotPreview("previews/ourstore_4.png", "Native In-App Installations", "DETAILS")
    )
    "connecto" -> listOf(
        AppScreenshotPreview("previews/connecto_1.png", "Private End-to-End Chat", "MESSAGES"),
        AppScreenshotPreview("previews/connecto_2.png", "Channels & Communities", "DISCOVER"),
        AppScreenshotPreview("previews/connecto_3.png", "Guest Sandbox Profile", "GUEST MODE"),
        AppScreenshotPreview("previews/connecto_4.png", "Offline Direct Mesh Hub", "LOCAL MESH")
    )
    "pdfstudio" -> listOf(
        AppScreenshotPreview("previews/pdfstudio_1.png", "Same-Font In-Place Editor", "EDITOR"),
        AppScreenshotPreview("previews/pdfstudio_2.png", "Photo & Image to PDF", "CONVERTER"),
        AppScreenshotPreview("previews/pdfstudio_3.png", "Lossless Multi-Document Merge", "MERGER")
    )
    "jarvis" -> listOf(
        AppScreenshotPreview("previews/jarvis_1.png", "Neural Conversational Assistant", "INTELLIGENCE"),
        AppScreenshotPreview("previews/jarvis_2.png", "Zero-Leak On-Device Memory Vault", "MEMORY VAULT"),
        AppScreenshotPreview("previews/jarvis_3.png", "Multi-Cloud AI Engine & Personality", "SETTINGS")
    )
    else -> listOf(
        AppScreenshotPreview("previews/ourstore_1.png", "Curated Ecosystem Experience", "ECOSYSTEM")
    )
}

@Composable
private fun rememberAssetBitmap(assetPath: String): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = null, key1 = assetPath) {
        value = withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { inputStream ->
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2 // Downsampled to 540x1200 for lightning-fast memory & silky 120Hz scrolling
                        inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeStream(inputStream, null, options)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }
    }.value
}

@Composable
private fun PhoneMockupCard(
    preview: AppScreenshotPreview,
    onClick: () -> Unit = {}
) {
    val theme = LocalStoreTheme.current
    val bitmap = rememberAssetBitmap(preview.assetPath)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "previewCardScale"
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(410.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(24.dp))
            .background(theme.surface)
            .border(1.5.dp, theme.borderHighlight, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = preview.caption,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )


            // Subtle Simulated Camera Notch
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .width(40.dp)
                    .height(9.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
            )

            // Fullscreen expand indicator badge on top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fullscreen,
                    contentDescription = "Expand screenshot",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Bottom Tag & Caption
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2563EB))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = preview.tag,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = preview.caption,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }
        } else {
            // Elegant skeleton shimmer preview placeholder
            StoreSkeletonBox(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }
}

// ─── Interactive Fullscreen Screenshot Slider & Lightbox ──────────────────────
@Composable
private fun ScreenshotLightboxDialog(
    previews: List<AppScreenshotPreview>,
    initialIndex: Int,
    appName: String = "",
    onDismiss: () -> Unit
) {
    if (previews.isEmpty()) return

    val validInitialIndex = initialIndex.coerceIn(0, previews.size - 1)
    val pagerState = rememberPagerState(initialPage = validInitialIndex) { previews.size }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        BackHandler { onDismiss() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF508080A))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val currentPreview = previews.getOrNull(pagerState.currentPage)
                        if (currentPreview != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2563EB))
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentPreview.tag,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Text(
                            text = if (appName.isNotBlank()) "$appName • ${pagerState.currentPage + 1} of ${previews.size}" else "${pagerState.currentPage + 1} of ${previews.size}",
                            color = Color(0xFFE5E7EB),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close preview",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Center Swipable Screen Pager
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 18.dp,
                        contentPadding = PaddingValues(horizontal = 28.dp)
                    ) { page ->
                        val preview = previews[page]
                        val bitmap = rememberAssetBitmap(preview.assetPath)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Phone frame container
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(9f / 19.5f)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(Color(0xFF101014))
                                    .border(2.dp, Color(0xFF2E2E36), RoundedCornerShape(32.dp))
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = preview.caption,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(32.dp)),
                                        contentScale = ContentScale.Crop
                                    )


                                    // Camera notch simulation
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 12.dp)
                                            .width(46.dp)
                                            .height(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.8f))
                                    )

                                    // Bottom caption inside frame
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = preview.caption,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.5.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                } else {
                                    StoreSkeletonBox(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Floating Left Arrow (Previous)
                    if (pagerState.currentPage > 0) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 10.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Previous screenshot",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Floating Right Arrow (Next)
                    if (pagerState.currentPage < previews.size - 1) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 10.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Next screenshot",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Bottom Pagination Bar & Instruction Hint
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Dot indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        previews.forEachIndexed { idx, _ ->
                            val isSelected = pagerState.currentPage == idx
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "dotWidth"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF2563EB) else Color.White.copy(alpha = 0.25f)
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(idx)
                                        }
                                    }
                            )
                        }
                    }

                    // Tactile hint text
                    Text(
                        text = "Slide or tap arrows to view next screenshot",
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
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
            style = TextStyle(fontFeatureSettings = "tnum"),
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
        appState.installState == StoreApp.InstallState.UPDATE_AVAILABLE -> Triple(ElectricBlueUpdateBg, ElectricBlueUpdateLight, "UPDATE AVAILABLE")
        else -> Triple(EmeraldStatusBg, EmeraldStatus, "UP TO DATE")
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
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }

            if (appState.installedVersionName != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DEVICE", color = theme.textDarkMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "v${appState.installedVersionName}",
                        color = theme.textMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(fontFeatureSettings = "tnum")
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
            Image(
                painter = painterResource(R.drawable.ic_app_connecto),
                contentDescription = appName,
                modifier = modifier.clip(RoundedCornerShape(13.dp)),
                contentScale = ContentScale.Crop
            )
        }
        "jarvis" -> {
            Image(
                painter = painterResource(R.drawable.ic_app_jarvis),
                contentDescription = appName,
                modifier = modifier.clip(RoundedCornerShape(13.dp)),
                contentScale = ContentScale.Crop
            )
        }
        "pdfstudio" -> {
            Image(
                painter = painterResource(R.drawable.ic_app_pdfstudio),
                contentDescription = appName,
                modifier = modifier.clip(RoundedCornerShape(13.dp)),
                contentScale = ContentScale.Crop
            )
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
        Text("Verified Cloudflare Anycast Delivery • targetSdk 37", color = theme.textDarkMuted, fontSize = 9.5.sp, fontWeight = FontWeight.Normal)
    }
}

// ─── Loading & Error Views ────────────────────────────────────────────────────
@Composable
fun StoreSkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val theme = LocalStoreTheme.current
    val infiniteTransition = rememberInfiniteTransition(label = "storePulseTransition")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "storePulseAlpha"
    )
    val baseColor = if (theme.isDark) Color(0xFF202026) else Color(0xFFE2E8F0)

    Box(
        modifier = modifier
            .clip(shape)
            .background(baseColor.copy(alpha = alpha))
    )
}

@Composable
private fun ClassicLoadingState() {
    val theme = LocalStoreTheme.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shimmer Status Subheader
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StoreSkeletonBox(
                modifier = Modifier.width(140.dp).height(14.dp),
                shape = RoundedCornerShape(4.dp)
            )
            StoreSkeletonBox(
                modifier = Modifier.width(90.dp).height(20.dp),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // 1. Hero Spotlight Skeleton Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(theme.surface)
                .border(1.dp, theme.cardBorder, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StoreSkeletonBox(
                        modifier = Modifier.width(110.dp).height(18.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                    StoreSkeletonBox(
                        modifier = Modifier.width(60.dp).height(18.dp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
                StoreSkeletonBox(
                    modifier = Modifier.width(230.dp).height(24.dp),
                    shape = RoundedCornerShape(6.dp)
                )
                StoreSkeletonBox(
                    modifier = Modifier.fillMaxWidth().height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                StoreSkeletonBox(
                    modifier = Modifier.width(180.dp).height(14.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StoreSkeletonBox(
                            modifier = Modifier.width(60.dp).height(14.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        StoreSkeletonBox(
                            modifier = Modifier.width(60.dp).height(14.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                    StoreSkeletonBox(
                        modifier = Modifier.width(120.dp).height(38.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 2. Filter Chips Row Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val pillWidths = listOf(54.dp, 84.dp, 108.dp, 80.dp)
            pillWidths.forEach { w ->
                StoreSkeletonBox(
                    modifier = Modifier.width(w).height(32.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // 3. App Feed Skeleton Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(theme.surface)
                        .border(1.dp, theme.cardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        StoreSkeletonBox(
                            modifier = Modifier.size(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StoreSkeletonBox(
                                modifier = Modifier.width(130.dp).height(16.dp),
                                shape = RoundedCornerShape(4.dp)
                            )
                            StoreSkeletonBox(
                                modifier = Modifier.fillMaxWidth().height(12.dp),
                                shape = RoundedCornerShape(4.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StoreSkeletonBox(
                                    modifier = Modifier.width(55.dp).height(10.dp),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                StoreSkeletonBox(
                                    modifier = Modifier.width(55.dp).height(10.dp),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        }
                        StoreSkeletonBox(
                            modifier = Modifier.width(76.dp).height(34.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
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
                tint = if (isDarkTheme) Color(0xFF38BDF8) else theme.textPrimary,
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

// ─── Our Store Centralized Settings Section ──────────────────────────────────
@Composable
fun StoreSettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = LocalStoreTheme.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = theme.textMuted,
            modifier = Modifier.padding(start = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(theme.surface)
                .border(1.dp, theme.cardBorder, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

// ─── Our Store Dual-Segmented Theme Pill Button ────────────────────────────────────
@Composable
fun StoreThemeToggleRow(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    theme: StoreThemeColors,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val iconRotation by animateFloatAsState(
        targetValue = if (isDarkTheme) 360f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "storeThemeRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            val iconTint = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFFF59E0B)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .graphicsLayer { rotationZ = iconRotation }
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = if (theme.isDark) 0.14f else 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Display Theme",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = if (isDarkTheme) "Pure OLED Black (#000000)" else "Studio Crisp White",
                    color = theme.textMuted,
                    fontSize = 11.5.sp
                )
            }
        }

        // Dual Segmented Theme Button Pill [ 🌙 Black ] [ ☀️ White ]
        Row(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(theme.cardElevated)
                .border(1.dp, theme.cardBorder, RoundedCornerShape(10.dp))
                .padding(2.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Black Theme Segment Button
            val blackInteraction = remember { MutableInteractionSource() }
            val isBlackPressed by blackInteraction.collectIsPressedAsState()
            val blackScale by animateFloatAsState(
                targetValue = if (isBlackPressed) 0.94f else 1.0f,
                animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "storeBlackBtnScale"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = blackScale
                        scaleY = blackScale
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkTheme) (if (theme.isDark) Color(0xFF000000) else Color(0xFF1E293B)) else Color.Transparent)
                    .then(
                        if (isDarkTheme) Modifier.border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.50f), RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clickable(
                        interactionSource = blackInteraction,
                        indication = null
                    ) {
                        if (!isDarkTheme) {
                            try { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                            onToggleTheme()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DarkMode,
                        contentDescription = "Black Theme",
                        tint = if (isDarkTheme) Color(0xFF38BDF8) else theme.textDarkMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Black",
                        fontSize = 11.5.sp,
                        fontWeight = if (isDarkTheme) FontWeight.Bold else FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else theme.textDarkMuted
                    )
                }
            }

            // White Theme Segment Button
            val whiteInteraction = remember { MutableInteractionSource() }
            val isWhitePressed by whiteInteraction.collectIsPressedAsState()
            val whiteScale by animateFloatAsState(
                targetValue = if (isWhitePressed) 0.94f else 1.0f,
                animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "storeWhiteBtnScale"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = whiteScale
                        scaleY = whiteScale
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isDarkTheme) Color(0xFFFFFFFF) else Color.Transparent)
                    .then(
                        if (!isDarkTheme) Modifier.border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.60f), RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clickable(
                        interactionSource = whiteInteraction,
                        indication = null
                    ) {
                        if (isDarkTheme) {
                            try { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) } catch (_: Exception) {}
                            onToggleTheme()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LightMode,
                        contentDescription = "White Theme",
                        tint = if (!isDarkTheme) Color(0xFFF59E0B) else theme.textDarkMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "White",
                        fontSize = 11.5.sp,
                        fontWeight = if (!isDarkTheme) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isDarkTheme) Color.Black else theme.textDarkMuted
                    )
                }
            }
        }
    }
}

// ─── Store Navigation Setting Row ─────────────────────────────────────────────
@Composable
fun StoreSettingsNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    theme: StoreThemeColors,
    trailingBadge: String? = null,
    trailingBadgeColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val rowScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "settingsRowScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = rowScale
                scaleY = rowScale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        try {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        } catch (_: Exception) {}
                        onClick()
                    }
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = if (theme.isDark) 0.14f else 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = theme.textPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = theme.textMuted,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingBadge != null) {
            val badgeColor = trailingBadgeColor ?: theme.accentColor
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = if (theme.isDark) 0.15f else 0.10f))
                    .border(0.6.dp, badgeColor.copy(alpha = if (theme.isDark) 0.35f else 0.25f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 2.5.dp)
            ) {
                Text(
                    text = trailingBadge,
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = theme.textDarkMuted,
                modifier = Modifier.size(18.dp)
            )
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
    onRegisterCredentials: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onLoginConnecto: () -> Unit,
    onLogout: () -> Unit,
    onClearLoginError: () -> Unit,
    scrollToTopTrigger: Int = 0,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val theme = LocalStoreTheme.current
    var showLegalDialog by remember { mutableStateOf<Int?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showReferDialog by remember { mutableStateOf(false) }
    val accountScrollState = rememberScrollState()

    val isGuest = currentUser == null || currentUser.username.equals("guest", ignoreCase = true)

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            try {
                accountScrollState.animateScrollTo(0)
            } catch (_: Exception) {}
        }
    }

    StoreDragToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        refreshText = "Refreshing profile & session...",
        canScrollBackward = {
            accountScrollState.canScrollBackward || accountScrollState.value > 0
        },
        modifier = Modifier.fillMaxSize()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(accountScrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Our Store In-App Update Card (ONLY shown when update available)
        if (storeSelfUpdate != null && storeSelfUpdate.installState == StoreApp.InstallState.UPDATE_AVAILABLE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(theme.surface)
                    .border(1.dp, ElectricBlueUpdate.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricBlueUpdateLight)
                        )
                        Text(
                            text = "Update available (v${storeSelfUpdate.app.versionName})",
                            color = ElectricBlueUpdateLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
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
                                    text = if (storeSelfUpdate.isInstalling) "Please wait…" else "${storeSelfUpdate.downloadProgress}%",
                                    color = if (storeSelfUpdate.isInstalling) ElectricBlueUpdateLight else theme.textMuted,
                                    fontSize = 11.5.sp,
                                    style = TextStyle(fontFeatureSettings = "tnum"),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (storeSelfUpdate.isInstalling) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                    color = ElectricBlueUpdateLight,
                                    trackColor = theme.cardElevated
                                )
                            } else {
                                LinearProgressIndicator(
                                    progress = { storeSelfUpdate.downloadProgress / 100f },
                                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                    color = theme.textPrimary,
                                    trackColor = theme.cardElevated
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ElectricBlueUpdate)
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
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "UPDATE OUR STORE NOW",
                                    color = Color.White,
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

        // ================= 1. USER IDENTITY HERO CARD (Connecto Profile Style) =================
        val avatarCardShape = RoundedCornerShape(20.dp)
        val role = currentUser?.role ?: StoreRole.MEMBER
        val isAdmin = role == StoreRole.ADMIN
        val isConnectoSso = currentUser?.isConnectoSso == true
        val statusRingColor = when {
            isAdmin -> Color(0xFF10B981) // Emerald Green
            isConnectoSso -> Color(0xFF8B5CF6) // Violet / Indigo
            !isGuest -> Color(0xFF2563EB) // Electric Blue
            else -> Color(0xFFF59E0B) // Amber
        }

        val roleBadgeText = when {
            isAdmin -> "ADMINISTRATOR"
            isConnectoSso -> "CONNECTO SSO"
            !isGuest -> "OFFICIAL VERIFIED"
            else -> "GUEST EXPLORER"
        }

        val roleBadgeDesc = when {
            isAdmin -> "Full administrative privileges, verified release signing & telemetry diagnostics."
            isConnectoSso -> "Authenticated via Connecto Single Sign-On ecosystem with unified identity."
            !isGuest -> "Verified account with autonomous background updates and repository access."
            else -> "Browsing in Guest Mode. Sign in with Connecto or credentials to unlock full sync."
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(avatarCardShape)
                .background(theme.surface)
                .border(1.dp, theme.cardBorder, avatarCardShape)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Large Avatar with concentric status ring & verification badge
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isGuest) {
                        UserAvatarImage(
                            avatarUrl = currentUser.avatarUrl,
                            displayName = currentUser.displayName,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, statusRingColor, CircleShape),
                            fallback = {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(statusRingColor.copy(alpha = 0.15f))
                                        .border(2.5.dp, statusRingColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isAdmin) Icons.Rounded.AdminPanelSettings else Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = statusRingColor,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(statusRingColor.copy(alpha = 0.15f))
                                .border(2.5.dp, statusRingColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Explore,
                                contentDescription = null,
                                tint = statusRingColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Bottom-End Verification Badge Pip
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(theme.surface)
                            .border(1.5.dp, statusRingColor, CircleShape)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Rounded.Shield else if (!isGuest) Icons.Rounded.CheckCircle else Icons.Rounded.Public,
                            contentDescription = null,
                            tint = statusRingColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Display Name & Handle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (!isGuest) currentUser.displayName else "Guest Explorer",
                        color = theme.textPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (!isGuest) "@${currentUser.username}" else "@guest.store",
                        color = theme.textMuted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                // Role / Status Badge Capsule (Matching Connecto Profile Style)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusRingColor.copy(alpha = 0.12f))
                        .border(0.8.dp, statusRingColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusRingColor)
                        )
                        Text(
                            text = roleBadgeText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusRingColor,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                Text(
                    text = roleBadgeDesc,
                    color = theme.textDarkMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // 3-Column Telemetry / Metric Strip
                HorizontalDivider(
                    color = theme.dividerColor,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ACCOUNT",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textDarkMuted,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = if (isAdmin) "Admin" else if (isConnectoSso) "SSO" else if (!isGuest) "Verified" else "Guest",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(theme.dividerColor)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "INTEGRITY",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textDarkMuted,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "SHA-256",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF3B82F6)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(theme.dividerColor)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ENGINE",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textDarkMuted,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "IPC Session",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        // ================= 2. APPEARANCE & DISPLAY SECTION =================
        StoreSettingsSection(title = "Appearance & Display") {
            StoreThemeToggleRow(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                theme = theme
            )
        }

        // ================= 3. SECURITY & SYSTEM ENGINE SECTION =================
        StoreSettingsSection(title = "Security & System Engine") {
            StoreSettingsNavRow(
                icon = Icons.Rounded.Security,
                iconTint = Color(0xFF10B981),
                title = "PackageInstaller IPC",
                subtitle = "Direct Android OS package session manager",
                theme = theme,
                trailingBadge = "ACTIVE",
                trailingBadgeColor = Color(0xFF10B981)
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.VerifiedUser,
                iconTint = Color(0xFF3B82F6),
                title = "Cryptographic Hash Vault",
                subtitle = "End-to-end SHA-256 package verification",
                theme = theme,
                trailingBadge = "SHA-256",
                trailingBadgeColor = Color(0xFF3B82F6)
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.Sync,
                iconTint = Color(0xFF8B5CF6),
                title = "Autonomous Background Sync",
                subtitle = "Cloudflare Edge delta updates & version polling",
                theme = theme,
                trailingBadge = "ENABLED",
                trailingBadgeColor = Color(0xFF8B5CF6)
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.Shield,
                iconTint = Color(0xFF06B6D4),
                title = "Hardware Keystore Vault",
                subtitle = "Tamper-proof local-only key storage & cryptographic trust",
                theme = theme,
                trailingBadge = "LOCAL",
                trailingBadgeColor = Color(0xFF06B6D4)
            )
        }

        // ================= 4. SUPPORT & COMMUNITY SECTION =================
        StoreSettingsSection(title = "Support & Community") {
            StoreSettingsNavRow(
                icon = Icons.Rounded.Share,
                iconTint = Color(0xFF8B5CF6),
                title = "Refer Our Store",
                subtitle = "Share download link & invite friends to open apps",
                theme = theme,
                onClick = { showReferDialog = true }
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.RateReview,
                iconTint = Color(0xFFF59E0B),
                title = "Send Feedback & Suggestions",
                subtitle = "Report bugs, propose features & rate experience",
                theme = theme,
                onClick = { showFeedbackDialog = true }
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.HeadsetMic,
                iconTint = Color(0xFF2563EB),
                title = "Help & Troubleshooting",
                subtitle = "FAQs, installation troubleshooting & diagnostic logs",
                theme = theme,
                onClick = { showHelpDialog = true }
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.Description,
                iconTint = Color(0xFF3B82F6),
                title = "Terms of Use",
                subtitle = "Ecosystem guidelines, distribution & license terms",
                theme = theme,
                onClick = { showLegalDialog = 0 }
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.Policy,
                iconTint = Color(0xFF10B981),
                title = "Privacy Policy",
                subtitle = "Zero telemetry, zero tracking & local data ownership",
                theme = theme,
                onClick = { showLegalDialog = 1 }
            )
        }

        // ================= 5. ABOUT OUR STORE SECTION =================
        StoreSettingsSection(title = "About Our Store") {
            StoreSettingsNavRow(
                icon = Icons.Rounded.Storefront,
                iconTint = theme.accentColor,
                title = "Our Store for Android",
                subtitle = "v${com.connecto.store.BuildConfig.VERSION_NAME} (Build ${com.connecto.store.BuildConfig.VERSION_CODE})",
                theme = theme,
                trailingBadge = "LATEST",
                trailingBadgeColor = theme.accentColor
            )
            HorizontalDivider(color = theme.dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(start = 66.dp))
            StoreSettingsNavRow(
                icon = Icons.Rounded.Cloud,
                iconTint = Color(0xFF06B6D4),
                title = "Ecosystem Network Endpoint",
                subtitle = "connecto.fun / Cloudflare Edge D1",
                theme = theme,
                trailingBadge = "ONLINE",
                trailingBadgeColor = Color(0xFF10B981)
            )
        }

        // ================= 6. ACCOUNT & SESSION SECTION =================
        if (!isGuest) {
            StoreSettingsSection(title = "Account & Session") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
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
                }
            }
        } else {
            StoreSettingsSection(title = "Authentication & Access") {
                var authTab by remember { mutableStateOf(0) }
                var inputUser by remember { mutableStateOf("") }
                var inputDisplay by remember { mutableStateOf("") }
                var inputPass by remember { mutableStateOf("") }
                var passVisibility by remember { mutableStateOf(false) }

                val isAuthenticating = loginState is LoginUiState.Loading
                val hasAuthError = loginState is LoginUiState.Error

                val canSubmitForm = when (authTab) {
                    0 -> inputUser.trim().isNotBlank() && inputPass.trim().isNotBlank()
                    else -> inputUser.trim().length >= 3 && inputPass.trim().length >= 8
                }

                val formBtnState = when {
                    isAuthenticating -> StoreButtonState.LOADING
                    hasAuthError -> StoreButtonState.ERROR
                    !canSubmitForm -> StoreButtonState.DISABLED
                    else -> StoreButtonState.DEFAULT
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SSO 1-Tap Quick Action
                    StoreFiveStateButton(
                        text = "CONTINUE WITH CONNECTO SSO",
                        onClick = onLoginConnecto,
                        modifier = Modifier.fillMaxWidth(),
                        state = if (isAuthenticating) StoreButtonState.LOADING else StoreButtonState.DEFAULT,
                        icon = Icons.Rounded.VerifiedUser,
                        backgroundColor = Color(0xFF4F46E5),
                        contentColor = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = theme.dividerColor, thickness = 0.8.dp)
                        Text(
                            text = "OR SIGN IN WITH CREDENTIALS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textDarkMuted,
                            letterSpacing = 0.6.sp
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = theme.dividerColor, thickness = 0.8.dp)
                    }

                    // Segmented Tabs with Sliding Indicator Pill
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.cardElevated)
                            .border(1.dp, theme.cardBorder, RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        val segmentWidth = maxWidth / 2
                        val animatedThumbOffset by animateDpAsState(
                            targetValue = if (authTab == 0) 0.dp else segmentWidth,
                            animationSpec = spring(stiffness = 450f, dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "authTabThumb"
                        )

                        // Animated sliding thumb
                        Box(
                            modifier = Modifier
                                .offset(x = animatedThumbOffset)
                                .width(segmentWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(theme.surface)
                                .border(1.dp, theme.cardBorder, RoundedCornerShape(9.dp))
                        )

                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        authTab = 0
                                        onClearLoginError()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SIGN IN",
                                    color = if (authTab == 0) theme.textPrimary else theme.textDarkMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (authTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        authTab = 1
                                        onClearLoginError()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CREATE ACCOUNT",
                                    color = if (authTab == 1) theme.textPrimary else theme.textDarkMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (authTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }

                    // Error Alert Banner
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
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = loginState.message,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
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

                    // Input 1: Username
                    OutlinedTextField(
                        value = inputUser,
                        onValueChange = {
                            inputUser = it
                            if (hasAuthError) onClearLoginError()
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
                        singleLine = true,
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

                    // Input 2: Display Name (Create Account only)
                    if (authTab == 1) {
                        OutlinedTextField(
                            value = inputDisplay,
                            onValueChange = { inputDisplay = it },
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

                    // Input 3: Password
                    OutlinedTextField(
                        value = inputPass,
                        onValueChange = {
                            inputPass = it
                            if (hasAuthError) onClearLoginError()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (authTab == 1) "Password (min 8 chars)" else "Password", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = theme.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passVisibility = !passVisibility }) {
                                Icon(
                                    imageVector = if (passVisibility) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = theme.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (passVisibility) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
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
                        text = if (authTab == 0) "SIGN IN TO STORE" else "CREATE ACCOUNT",
                        onClick = {
                            if (authTab == 0) {
                                onLoginCredentials(inputUser, inputPass)
                            } else {
                                onRegisterCredentials(inputUser, inputPass, inputDisplay, "")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        state = formBtnState,
                        icon = if (authTab == 0) Icons.AutoMirrored.Rounded.Login else Icons.Rounded.PersonAdd,
                        backgroundColor = theme.accentColor,
                        contentColor = Color.White
                    )
                }
            }

            // Legal Footer Note for Guest View
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Refer",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showReferDialog = true }
                    )
                    Text(text = "•", color = theme.textDarkMuted, fontSize = 12.sp)
                    Text(
                        text = "Help & Support",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showHelpDialog = true }
                    )
                    Text(text = "•", color = theme.textDarkMuted, fontSize = 12.sp)
                    Text(
                        text = "Feedback",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showFeedbackDialog = true }
                    )
                    Text(text = "•", color = theme.textDarkMuted, fontSize = 12.sp)
                    Text(
                        text = "Terms of Use",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showLegalDialog = 0 }
                    )
                    Text(text = "•", color = theme.textDarkMuted, fontSize = 12.sp)
                    Text(
                        text = "Privacy Policy",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showLegalDialog = 1 }
                    )
                }
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

    if (showHelpDialog) {
        OurStoreHelpSupportDialog(
            onDismiss = { showHelpDialog = false }
        )
    }

    if (showFeedbackDialog) {
        OurStoreFeedbackDialog(
            onDismiss = { showFeedbackDialog = false }
        )
    }

    if (showReferDialog) {
        OurStoreReferDialog(
            onDismiss = { showReferDialog = false }
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
