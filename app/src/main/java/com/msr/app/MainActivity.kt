package com.msr.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.random.Random

val DarkBg = Color(0xFF101216)
val SurfaceBg = Color(0xFF1A1D24)
val SurfaceVariantBg = Color(0xFF242834)
val NeonGreen = Color(0xFF00E676)
val LightLime = Color(0xFFAEEA00)
val NeonRed = Color(0xFFFF1744)
val BrightOrange = Color(0xFFFF9100)
val TextWhite = Color(0xFFF0F4F8)
val TextMuted = Color(0xFF94A3B8)
val AccentBlue = Color(0xFF29B6F6)
val GoldYellow = Color(0xFFFFD54F)

private const val PREFS_NAME = "msr_account_prefs"
private const val KEY_EMAIL = "ms_email"
private const val KEY_PASSWORD = "ms_password"
private const val KEY_LOGGED_IN = "ms_logged_in"

@Composable
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bounceScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

class PointsBridge(private val onData: (String, String, String) -> Unit) {
    @JavascriptInterface
    fun updatePoints(total: String, mobile: String, user: String) {
        onData(total, mobile, user)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    var accountEmail by remember { mutableStateOf(prefs.getString(KEY_EMAIL, "anicetaterante@gmail.com") ?: "anicetaterante@gmail.com") }
    var accountPassword by remember { mutableStateOf(prefs.getString(KEY_PASSWORD, "dadong04") ?: "dadong04") }
    var isLoggedIn by remember { mutableStateOf(prefs.getBoolean(KEY_LOGGED_IN, false)) }
    var isLoggingIn by remember { mutableStateOf(false) }

    var totalPoints by rememberSaveable { mutableStateOf("---") }
    var dailyPointsText by rememberSaveable { mutableStateOf("0 / 60 PTS") }
    var loginErrorMsg by rememberSaveable { mutableStateOf("") }
    var showInteractiveLogin by remember { mutableStateOf(false) }

    var searchLimit by rememberSaveable { mutableIntStateOf(35) }
    var selectedIntervalMs by rememberSaveable { mutableLongStateOf(9999L) }

    var isRunning by rememberSaveable { mutableStateOf(value = false) }
    var currentCount by rememberSaveable { mutableIntStateOf(0) }
    var currentTerm by rememberSaveable { mutableStateOf("") }
    var currentUrl by rememberSaveable { mutableStateOf("https://www.bing.com") }
    var timerStatusText by rememberSaveable { mutableStateOf("Ready to earn points") }
    var isCompleted by rememberSaveable { mutableStateOf(value = false) }

    var showSettingsDialog by rememberSaveable { mutableStateOf(value = false) }

    // Helper function to refresh points directly via Native HTTP
    fun refreshPointsNow() {
        coroutineScope.launch {
            val res = fetchRewardsPointsNative()
            if (res != null) {
                if (res.first.isNotEmpty()) totalPoints = res.first
                if (res.second.isNotEmpty()) dailyPointsText = res.second
            }
        }
    }

    // Auto-fetch points balance on startup or login
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            refreshPointsNow()
            currentUrl = "https://rewards.bing.com/dashboard"
        }
    }

    // Timeout safety for logging in
    LaunchedEffect(isLoggingIn) {
        if (isLoggingIn) {
            delay(18000)
            if (isLoggingIn) {
                loginErrorMsg = "Microsoft 2FA / Code required. Tap 'Web Login' below to complete sign in."
            }
        }
    }

    if (isRunning) {
        LaunchedEffect(Unit) {
            try {
                runNativeAutoSearch(
                    limit = searchLimit,
                    intervalMs = selectedIntervalMs,
                    onProgress = { current, term ->
                        currentCount = current
                        currentTerm = term
                        refreshPointsNow()
                    },
                    onStatus = { status ->
                        timerStatusText = status
                    },
                    onUrlChange = { url ->
                        currentUrl = url
                    },
                    onComplete = {
                        isRunning = false
                        isCompleted = true
                        refreshPointsNow()
                        timerStatusText = "All searches finished! Points earned."
                        currentUrl = "https://rewards.bing.com/dashboard"
                    }
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                isRunning = false
                timerStatusText = "Error: ${error.message ?: "Search failed"}"
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBg,
            surface = SurfaceBg,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                HeaderSection(
                    isLoggedIn = isLoggedIn,
                    isRunning = isRunning,
                    isCompleted = isCompleted
                )

                // Direct Native Microsoft Login & Points Dashboard Card
                NativeAccountPointsCard(
                    email = accountEmail,
                    password = accountPassword,
                    isLoggedIn = isLoggedIn,
                    isLoggingIn = isLoggingIn,
                    totalPoints = totalPoints,
                    dailyPointsText = dailyPointsText,
                    errorMsg = loginErrorMsg,
                    onLoginAttempt = { email, pass ->
                        accountEmail = email
                        accountPassword = pass
                        isLoggingIn = true
                        loginErrorMsg = ""
                        prefs.edit {
                            putString(KEY_EMAIL, email)
                            putString(KEY_PASSWORD, pass)
                        }
                        currentUrl = "https://login.live.com/"
                    },
                    onOpenInteractiveLogin = {
                        showInteractiveLogin = true
                    },
                    onRefreshPoints = {
                        refreshPointsNow()
                        currentUrl = "https://rewards.bing.com/dashboard"
                    },
                    onLogOut = {
                        isLoggedIn = false
                        isLoggingIn = false
                        totalPoints = "---"
                        dailyPointsText = "0 / 60 PTS"
                        prefs.edit { putBoolean(KEY_LOGGED_IN, false) }
                        CookieManager.getInstance().removeAllCookies(null)
                    }
                )

                // Warning Banner
                WarningBanner()

                // Live Progress & Analytics Card
                ProgressCard(
                    currentCount = currentCount,
                    limit = searchLimit,
                    currentTerm = currentTerm,
                    statusText = timerStatusText,
                    isRunning = isRunning,
                    isCompleted = isCompleted
                )

                // Primary Action Button (Start / Stop)
                if (!isRunning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .bounceClick {
                                isRunning = true
                                isCompleted = false
                                currentCount = 0
                                currentTerm = ""
                                if (currentUrl.isEmpty() || !currentUrl.startsWith("http")) {
                                    currentUrl = "https://www.bing.com"
                                }
                                timerStatusText = "Starting point automation..."
                            }
                            .background(
                                brush = Brush.horizontalGradient(listOf(NeonGreen, LightLime)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Start Point Automation", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$searchLimit Searches | Delay: ${getIntervalLabel(selectedIntervalMs)}",
                                    color = Color.Black.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .bounceClick {
                                isRunning = false
                                timerStatusText = "Stopped by user."
                            }
                            .background(
                                brush = Brush.horizontalGradient(listOf(NeonRed, BrightOrange)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Stop Automation ($currentCount/$searchLimit)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(timerStatusText, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Settings Trigger Button
                OutlinedButton(
                    onClick = { if (!isRunning) showSettingsDialog = true },
                    enabled = !isRunning,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { if (!isRunning) showSettingsDialog = true }
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = TextWhite)
                    Spacer(Modifier.width(8.dp))
                    Text("Automation Settings", color = TextWhite, fontWeight = FontWeight.SemiBold)
                }

                // Developer Credit Footer Card
                DeveloperFooterCard(
                    onOpenGithub = {
                        try {
                            uriHandler.openUri("https://github.com/ja3h1/MSR-Bing")
                        } catch (_: Exception) {}
                    }
                )

                // Background Web Engine
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .alpha(0.01f)
                ) {
                    BackgroundWebEngine(
                        url = currentUrl,
                        savedEmail = accountEmail,
                        savedPassword = accountPassword,
                        isLoggingIn = isLoggingIn,
                        onLoginSuccess = { userName ->
                            isLoggingIn = false
                            isLoggedIn = true
                            loginErrorMsg = ""
                            if (userName.isNotEmpty() && (accountEmail.isEmpty() || !accountEmail.contains("@"))) {
                                accountEmail = userName
                                prefs.edit { putString(KEY_EMAIL, userName) }
                            }
                            prefs.edit { putBoolean(KEY_LOGGED_IN, true) }
                            refreshPointsNow()
                            currentUrl = "https://rewards.bing.com/dashboard"
                        },
                        onLoginError = { error ->
                            isLoggingIn = false
                            loginErrorMsg = error
                        },
                        onPointsScraped = { points, mobilePts, userDisplayName ->
                            if (points.isNotEmpty() && points != "0") {
                                val num = points.toLongOrNull()
                                totalPoints = if (num != null) String.format(Locale.US, "%,d PTS", num) else "$points PTS"
                            }
                            if (mobilePts.isNotEmpty()) {
                                dailyPointsText = mobilePts
                            }
                            if (userDisplayName.isNotEmpty() && (accountEmail.isEmpty() || !accountEmail.contains("@"))) {
                                accountEmail = userDisplayName
                                prefs.edit { putString(KEY_EMAIL, userDisplayName) }
                            }
                        }
                    )
                }
            }

            // Interactive Web Login Window
            if (showInteractiveLogin) {
                AlertDialog(
                    onDismissRequest = { showInteractiveLogin = false },
                    containerColor = SurfaceBg,
                    title = {
                        Text("Interactive Microsoft Sign In", color = TextWhite, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sign in below (complete 2FA/Code if prompted). Then tap 'I Have Signed In - Verify'.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, SurfaceVariantBg, RoundedCornerShape(12.dp))
                            ) {
                                InteractiveLoginWebView(
                                    initialUrl = "https://login.live.com/",
                                    savedEmail = accountEmail,
                                    savedPassword = accountPassword,
                                    onLoginSuccess = { detectedName ->
                                        isLoggingIn = false
                                        isLoggedIn = true
                                        loginErrorMsg = ""
                                        if (detectedName.isNotEmpty()) {
                                            accountEmail = detectedName
                                            prefs.edit { putString(KEY_EMAIL, detectedName) }
                                        }
                                        prefs.edit { putBoolean(KEY_LOGGED_IN, true) }
                                        refreshPointsNow()
                                    }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isLoggingIn = false
                                isLoggedIn = true
                                prefs.edit { putBoolean(KEY_LOGGED_IN, true) }
                                showInteractiveLogin = false
                                refreshPointsNow()
                                currentUrl = "https://rewards.bing.com/dashboard"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.bounceClick {
                                isLoggingIn = false
                                isLoggedIn = true
                                prefs.edit { putBoolean(KEY_LOGGED_IN, true) }
                                showInteractiveLogin = false
                                refreshPointsNow()
                                currentUrl = "https://rewards.bing.com/dashboard"
                            }
                        ) {
                            Text("I Have Signed In - Verify", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showInteractiveLogin = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    }
                )
            }

            // Search Settings Modal Dialog
            if (showSettingsDialog) {
                var tempLimit by remember(showSettingsDialog) { mutableIntStateOf(searchLimit) }
                var tempInterval by remember(showSettingsDialog) { mutableLongStateOf(selectedIntervalMs) }

                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    containerColor = SurfaceBg,
                    title = {
                        Text("Automation Settings", color = TextWhite, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Search Count (Points Target)", color = TextMuted, fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                DropdownSelector(
                                    options = listOf(5, 10, 20, 25, 30, 35, 40, 45, 50),
                                    selectedValue = tempLimit,
                                    onSelect = { tempLimit = it }
                                )
                            }

                            Column {
                                Text("Interval Delay", color = TextMuted, fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                                DropdownSelectorInterval(
                                    selectedValue = tempInterval,
                                    onSelect = { tempInterval = it }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                searchLimit = tempLimit
                                selectedIntervalMs = tempInterval
                                showSettingsDialog = false
                            }
                        ) {
                            Text("Save Settings", color = NeonGreen, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text("Cancel", color = TextMuted)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DeveloperFooterCard(onOpenGithub: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { onOpenGithub() }
            .border(1.dp, SurfaceVariantBg, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Dev: Ja3h1", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("GitHub: github.com/ja3h1/MSR-Bing", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Direct Native HTTP Points & Mobile Search Progress Fetcher targeting Microsoft Rewards Dashboard & API Endpoints
suspend fun fetchRewardsPointsNative(): Pair<String, String>? = withContext(Dispatchers.IO) {
    try {
        val cookieManager = CookieManager.getInstance()
        cookieManager.flush()

        val bingCookies = cookieManager.getCookie("https://www.bing.com") ?: ""
        val liveCookies = cookieManager.getCookie("https://login.live.com") ?: ""
        val rewardsCookies = cookieManager.getCookie("https://rewards.bing.com") ?: ""
        val msCookies = cookieManager.getCookie("https://account.microsoft.com") ?: ""

        val rawCookiesList = listOf(bingCookies, liveCookies, rewardsCookies, msCookies)
            .filter { it.isNotBlank() }
            .flatMap { it.split(";") }
            .map { it.trim() }
            .distinct()

        val combinedCookies = rawCookiesList.joinToString("; ")

        if (combinedCookies.isEmpty()) return@withContext null

        val userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

        val endpoints = listOf(
            "https://rewards.bing.com/dashboard",
            "https://www.bing.com/rewardsapi/getuserinfo",
            "https://rewards.bing.com/api/getuserinfo",
            "https://www.bing.com/"
        )

        for (targetUrl in endpoints) {
            try {
                val url = URL(targetUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", userAgent)
                conn.setRequestProperty("Cookie", combinedCookies)
                conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/json,text/plain,*/*")
                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                conn.setRequestProperty("Referer", "https://rewards.bing.com/")
                conn.connectTimeout = 7000
                conn.readTimeout = 7000
                conn.connect()

                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }

                    val balanceMatch = Regex("\"(?:balance|availablePoints)\":\\s*(\\d+)").find(response)
                    val mobProgMatch = Regex("\"mobileSearch\":\\{[^}]*\"pointProgress\":\\s*(\\d+)").find(response)
                        ?: Regex("\"title\":\"Mobile search\"[^}]*\"pointProgress\":\\s*(\\d+)").find(response)
                    val mobMaxMatch = Regex("\"mobileSearch\":\\{[^}]*\"pointProgressMax\":\\s*(\\d+)").find(response)
                        ?: Regex("\"title\":\"Mobile search\"[^}]*\"pointProgressMax\":\\s*(\\d+)").find(response)

                    val htmlRcMatch = Regex("id=\"id_r[chp]\"[^>]*>([^<]+)<").find(response)

                    val totalNum = balanceMatch?.groupValues?.get(1) ?: htmlRcMatch?.groupValues?.get(1)?.replace("[^0-9,]".toRegex(), "")
                    val mobProg = mobProgMatch?.groupValues?.get(1)
                    val mobMax = mobMaxMatch?.groupValues?.get(1) ?: "60"

                    val totalText = if (!totalNum.isNullOrEmpty()) {
                        val num = totalNum.toLongOrNull()
                        if (num != null) String.format(Locale.US, "%,d PTS", num) else "$totalNum PTS"
                    } else ""

                    val mobText = if (mobProg != null) {
                        "$mobProg / $mobMax PTS"
                    } else ""

                    if (totalText.isNotEmpty() || mobText.isNotEmpty()) {
                        return@withContext Pair(totalText, mobText)
                    }
                }
            } catch (_: Exception) {}
        }
    } catch (_: Exception) {}
    return@withContext null
}

@Composable
fun NativeAccountPointsCard(
    email: String,
    password: String,
    isLoggedIn: Boolean,
    isLoggingIn: Boolean,
    totalPoints: String,
    dailyPointsText: String,
    errorMsg: String,
    onLoginAttempt: (String, String) -> Unit,
    onOpenInteractiveLogin: () -> Unit,
    onRefreshPoints: () -> Unit,
    onLogOut: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Microsoft Rewards Account", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                if (isLoggedIn) {
                    Surface(
                        color = NeonGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Connected", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (!isLoggedIn) {
                // Direct In-App Login Form
                var tempEmail by remember { mutableStateOf(email) }
                var tempPassword by remember { mutableStateOf(password) }
                var isPasswordVisible by remember { mutableStateOf(false) }

                Text(
                    text = "Log in with your Microsoft account below to authenticate and earn points.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = tempEmail,
                    onValueChange = { tempEmail = it },
                    label = { Text("Microsoft Email / Username") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SurfaceVariantBg,
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tempPassword,
                    onValueChange = { tempPassword = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SurfaceVariantBg,
                        focusedLabelColor = NeonGreen
                    ),
                    trailingIcon = {
                        TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Text(if (isPasswordVisible) "Hide" else "Show", color = TextMuted, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = BrightOrange, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .bounceClick {
                                if (tempEmail.isNotEmpty() && tempPassword.isNotEmpty() && !isLoggingIn) {
                                    onLoginAttempt(tempEmail.trim(), tempPassword)
                                }
                            }
                            .background(
                                color = if (!isLoggingIn && tempEmail.isNotEmpty() && tempPassword.isNotEmpty()) AccentBlue else SurfaceVariantBg,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoggingIn) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                                Text("Logging In...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Text("Direct Log In", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .bounceClick { onOpenInteractiveLogin() }
                            .background(SurfaceVariantBg, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Web Login", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                // Logged In Points Stats Dashboard
                Text("Account: ${email.ifEmpty { "Microsoft User" }}", color = TextMuted, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Points Card
                    Surface(
                        color = SurfaceVariantBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = GoldYellow, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Total Points", color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(totalPoints, color = GoldYellow, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Daily Mobile Search Points Progress
                    Surface(
                        color = SurfaceVariantBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Mobile Search", color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(dailyPointsText, color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRefreshPoints,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick { onRefreshPoints() }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refresh Points", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onLogOut,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .bounceClick { onLogOut() }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = NeonRed, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Log Out", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(isLoggedIn: Boolean, isRunning: Boolean, isCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("MSR Search Assistant", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Native Microsoft Rewards Point Earner", color = TextMuted, fontSize = 12.sp)
        }

        val (statusText, statusColor) = when {
            isRunning -> "ACTIVE" to NeonGreen
            isCompleted -> "COMPLETED" to LightLime
            isLoggedIn -> "LOGGED IN" to AccentBlue
            else -> "IDLE" to TextMuted
        }

        Surface(
            color = statusColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(50.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WarningBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1215)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = NeonRed, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Log in with your Microsoft account above to earn rewards search points natively.",
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ProgressCard(
    currentCount: Int,
    limit: Int,
    currentTerm: String,
    statusText: String,
    isRunning: Boolean,
    isCompleted: Boolean
) {
    val progress = if (limit > 0) currentCount.toFloat() / limit.toFloat() else 0f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Automation Progress", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    text = "$currentCount / $limit",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) LightLime else NeonGreen,
                trackColor = SurfaceVariantBg
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentTerm.isNotEmpty()) {
                    Surface(
                        color = SurfaceVariantBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(currentTerm, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Text(
                    text = statusText,
                    color = when {
                        isCompleted -> LightLime
                        isRunning -> NeonGreen
                        else -> TextMuted
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveLoginWebView(
    initialUrl: String,
    savedEmail: String,
    savedPassword: String,
    onLoginSuccess: (String) -> Unit
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?
                    ): Boolean {
                        return false
                    }

                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)
                        CookieManager.getInstance().flush()

                        if (pageUrl == null) return

                        // Auto Bypass Passkeys & Interrupts + Fill Email/Password
                        val fillScript = """
                            (function() {
                                function setReactValue(input, val) {
                                    var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                                    if (nativeSetter) {
                                        nativeSetter.call(input, val);
                                    } else {
                                        input.value = val;
                                    }
                                    input.dispatchEvent(new Event('input', { bubbles: true }));
                                    input.dispatchEvent(new Event('change', { bubbles: true }));
                                    input.dispatchEvent(new Event('blur', { bubbles: true }));
                                }

                                if (window.__msrLoginInterval) return;
                                window.__msrLoginInterval = setInterval(function() {
                                    try {
                                        var url = window.location.href;

                                        // Bypass Passkey & Security Interrupts
                                        if (url.indexOf('interrupt') !== -1 || url.indexOf('passkey') !== -1 || url.indexOf('oauth20_authorize') !== -1) {
                                            var skipBtn = document.getElementById('iCancel') ||
                                                          document.getElementById('iSkip') ||
                                                          document.getElementById('idNever') ||
                                                          document.querySelector('a[href*="user_cancel"]') ||
                                                          document.querySelector('button[aria-label*="Skip"]') ||
                                                          document.getElementById('idSIButton9');
                                            if (skipBtn && !skipBtn.dataset.msrDone) {
                                                skipBtn.dataset.msrDone = 'true';
                                                setTimeout(function() { skipBtn.click(); }, 300);
                                            }
                                        }

                                        // Email Step
                                        var emailInput = document.getElementById('i0116') || document.querySelector('input[type="email"]');
                                        if (emailInput && emailInput.offsetParent !== null && !emailInput.dataset.msrDone) {
                                            if (!emailInput.value && '$savedEmail' !== '') {
                                                setReactValue(emailInput, '$savedEmail');
                                            }
                                            var nextBtn = document.getElementById('idSIButton9') || document.querySelector('input[type="submit"]');
                                            if (nextBtn) {
                                                emailInput.dataset.msrDone = 'true';
                                                setTimeout(function() {
                                                    nextBtn.disabled = false;
                                                    nextBtn.click();
                                                }, 300);
                                            }
                                        }

                                        // Password Step
                                        var passInput = document.getElementById('i0118') || document.querySelector('input[type="password"]');
                                        if (passInput && passInput.offsetParent !== null && !passInput.dataset.msrDone) {
                                            if (!passInput.value && '$savedPassword' !== '') {
                                                setReactValue(passInput, '$savedPassword');
                                            }
                                            var signInBtn = document.getElementById('idSIButton9') || document.querySelector('input[type="submit"]');
                                            if (signInBtn) {
                                                passInput.dataset.msrDone = 'true';
                                                setTimeout(function() {
                                                    signInBtn.disabled = false;
                                                    signInBtn.click();
                                                }, 300);
                                            }
                                        }

                                        // KMSI Step
                                        var kmsiBtn = document.getElementById('acceptButton') || document.getElementById('idSIButton9');
                                        var kmsiTitle = document.getElementById('KmsiTitle') || document.querySelector('.kmsi-title');
                                        if ((kmsiTitle || kmsiBtn) && kmsiBtn && !kmsiBtn.dataset.msrDone) {
                                            kmsiBtn.dataset.msrDone = 'true';
                                            setTimeout(function() { kmsiBtn.click(); }, 300);
                                        }
                                    } catch(e) {}
                                }, 400);
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(fillScript, null)

                        val cookies = CookieManager.getInstance().getCookie(pageUrl) ?: ""
                        val bingCookies = CookieManager.getInstance().getCookie("https://www.bing.com") ?: ""

                        val isAuthenticated = pageUrl.contains("wlexpsignin") ||
                                              pageUrl.contains("idtokenv2") ||
                                              pageUrl.contains("rewards/upsell") ||
                                              pageUrl.contains("rewards.bing.com") ||
                                              (pageUrl.contains("bing.com") && !pageUrl.contains("login.live.com")) ||
                                              cookies.contains("RPSSecAuth") ||
                                              cookies.contains("MSPOK") ||
                                              bingCookies.contains("RPSSecAuth") ||
                                              bingCookies.contains("MSPOK") ||
                                              bingCookies.contains("MUID")

                        if (isAuthenticated) {
                            val nameScript = "(function() { var el = document.getElementById('id_n') || document.getElementById('id_a') || document.querySelector('.username'); return el ? el.innerText : ''; })();"
                            view?.evaluateJavascript(nameScript) { userName ->
                                val cleanedName = userName?.replace("\"", "")?.trim() ?: ""
                                onLoginSuccess(cleanedName)
                            }
                        }
                    }
                }
                loadUrl(initialUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun BackgroundWebEngine(
    url: String,
    savedEmail: String,
    savedPassword: String,
    isLoggingIn: Boolean,
    onLoginSuccess: (String) -> Unit,
    onLoginError: (String) -> Unit,
    onPointsScraped: (String, String, String) -> Unit
) {
    val bridge = remember {
        PointsBridge { total, mobile, user ->
            onPointsScraped(total, mobile, user)
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

                addJavascriptInterface(bridge, "MSRBridge")

                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?
                    ): Boolean {
                        return false
                    }

                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)
                        CookieManager.getInstance().flush()

                        if (pageUrl == null) return

                        // Detect 2FA / One-Time Code Screen
                        if (pageUrl.contains("GetOneTimeCode.srf") || pageUrl.contains("otc")) {
                            onLoginError("Microsoft 2FA / One-Time Code required. Tap 'Web Login' to complete.")
                        }

                        val cookies = CookieManager.getInstance().getCookie(pageUrl) ?: ""
                        val bingCookies = CookieManager.getInstance().getCookie("https://www.bing.com") ?: ""

                        // 1. Check Authentication Status via OAuth Redirect Signals & Cookies
                        val isAuthenticated = pageUrl.contains("wlexpsignin") ||
                                              pageUrl.contains("idtokenv2") ||
                                              pageUrl.contains("rewards/upsell") ||
                                              pageUrl.contains("rewards.bing.com") ||
                                              (pageUrl.contains("bing.com") && !pageUrl.contains("login.live.com")) ||
                                              cookies.contains("RPSSecAuth") ||
                                              cookies.contains("MSPOK") ||
                                              bingCookies.contains("RPSSecAuth") ||
                                              bingCookies.contains("MSPOK") ||
                                              bingCookies.contains("MUID")

                        if (isLoggingIn && isAuthenticated) {
                            val nameScript = "(function() { var el = document.getElementById('id_n') || document.getElementById('id_a') || document.querySelector('.username'); return el ? el.innerText : ''; })();"
                            view?.evaluateJavascript(nameScript) { userName ->
                                val cleanedName = userName?.replace("\"", "")?.trim() ?: ""
                                onLoginSuccess(cleanedName)
                            }
                        }

                        // 2. React-Compatible Direct Background Login Auto-Fill & Passkey Bypass Script
                        if (isLoggingIn && savedEmail.isNotEmpty()) {
                            val fillScript = """
                                (function() {
                                    function setReactValue(input, val) {
                                        var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
                                        if (nativeSetter) {
                                            nativeSetter.call(input, val);
                                        } else {
                                            input.value = val;
                                        }
                                        input.dispatchEvent(new Event('input', { bubbles: true }));
                                        input.dispatchEvent(new Event('change', { bubbles: true }));
                                        input.dispatchEvent(new Event('blur', { bubbles: true }));
                                    }

                                    if (window.__msrLoginInterval) return;
                                    window.__msrLoginInterval = setInterval(function() {
                                        try {
                                            var url = window.location.href;

                                            // Bypass Passkey & Security Interrupts
                                            if (url.indexOf('interrupt') !== -1 || url.indexOf('passkey') !== -1 || url.indexOf('oauth20_authorize') !== -1) {
                                                var skipBtn = document.getElementById('iCancel') ||
                                                              document.getElementById('iSkip') ||
                                                              document.getElementById('idNever') ||
                                                              document.querySelector('a[href*="user_cancel"]') ||
                                                              document.querySelector('button[aria-label*="Skip"]') ||
                                                              document.getElementById('idSIButton9');
                                                if (skipBtn && !skipBtn.dataset.msrDone) {
                                                    skipBtn.dataset.msrDone = 'true';
                                                    setTimeout(function() { skipBtn.click(); }, 300);
                                                }
                                            }

                                            // Email step
                                            var emailInput = document.getElementById('i0116') || document.querySelector('input[type="email"]');
                                            if (emailInput && emailInput.offsetParent !== null && !emailInput.dataset.msrDone) {
                                                if (!emailInput.value) {
                                                    setReactValue(emailInput, '$savedEmail');
                                                }
                                                var nextBtn = document.getElementById('idSIButton9') || document.querySelector('input[type="submit"]');
                                                if (nextBtn) {
                                                    emailInput.dataset.msrDone = 'true';
                                                    setTimeout(function() {
                                                        nextBtn.disabled = false;
                                                        nextBtn.click();
                                                    }, 300);
                                                }
                                            }

                                            // Password step
                                            var passInput = document.getElementById('i0118') || document.querySelector('input[type="password"]');
                                            if (passInput && passInput.offsetParent !== null && !passInput.dataset.msrDone) {
                                                if (!passInput.value && '$savedPassword' !== '') {
                                                    setReactValue(passInput, '$savedPassword');
                                                }
                                                var signInBtn = document.getElementById('idSIButton9') || document.querySelector('input[type="submit"]');
                                                if (signInBtn) {
                                                    passInput.dataset.msrDone = 'true';
                                                    setTimeout(function() {
                                                        signInBtn.disabled = false;
                                                        signInBtn.click();
                                                    }, 300);
                                                }
                                            }

                                            // KMSI (Stay signed in) step
                                            var kmsiBtn = document.getElementById('acceptButton') || document.getElementById('idSIButton9');
                                            var kmsiTitle = document.getElementById('KmsiTitle') || document.querySelector('.kmsi-title');
                                            if ((kmsiTitle || kmsiBtn) && kmsiBtn && !kmsiBtn.dataset.msrDone) {
                                                kmsiBtn.dataset.msrDone = 'true';
                                                setTimeout(function() { kmsiBtn.click(); }, 300);
                                            }
                                        } catch(e) {}
                                    }, 400);
                                })();
                            """.trimIndent()
                            view?.evaluateJavascript(fillScript, null)
                        }

                        // 3. Asynchronous Realtime Points & Mobile Search Progress Scraper
                        val bridgeScript = """
                            (function() {
                                var total = '';
                                var mobile = '';
                                var user = '';

                                var rc = document.getElementById('id_rc') ||
                                         document.getElementById('id_rh') ||
                                         document.getElementById('id_p') ||
                                         document.querySelector('.eRewardsPoints') ||
                                         document.querySelector('.pointsDetail') ||
                                         document.querySelector('#flyout_points');
                                if (rc && rc.innerText) {
                                    total = rc.innerText.replace(/[^0-9,]/g, '');
                                }

                                var userElem = document.getElementById('id_n') ||
                                               document.getElementById('id_a') ||
                                               document.querySelector('.username');
                                if (userElem && userElem.innerText) {
                                    user = userElem.innerText.trim();
                                }

                                try {
                                    var xhr = new XMLHttpRequest();
                                    xhr.open('GET', 'https://www.bing.com/rewardsapi/getuserinfo', true);
                                    xhr.onload = function() {
                                        try {
                                            var d = JSON.parse(xhr.responseText);
                                            if (d && d.userInfo) {
                                                if (d.userInfo.balance !== undefined) {
                                                    total = d.userInfo.balance.toString();
                                                }
                                                if (d.userInfo.counters && d.userInfo.counters.mobileSearch) {
                                                    var mob = d.userInfo.counters.mobileSearch;
                                                    mobile = mob.pointProgress + ' / ' + mob.pointProgressMax + ' PTS';
                                                } else if (d.userInfo.counters && d.userInfo.counters.pcSearch) {
                                                    var pc = d.userInfo.counters.pcSearch;
                                                    mobile = pc.pointProgress + ' / ' + pc.pointProgressMax + ' PTS';
                                                }
                                                if (d.userInfo.name) {
                                                    user = d.userInfo.name;
                                                }
                                            }
                                        } catch(e) {}
                                        if (window.MSRBridge) {
                                            window.MSRBridge.updatePoints(total, mobile, user);
                                        }
                                    };
                                    xhr.onerror = function() {
                                        if (window.MSRBridge) {
                                            window.MSRBridge.updatePoints(total, mobile, user);
                                        }
                                    };
                                    xhr.send();
                                } catch(e) {
                                    if (window.MSRBridge) {
                                        window.MSRBridge.updatePoints(total, mobile, user);
                                    }
                                }
                            })();
                        """.trimIndent()

                        view?.evaluateJavascript(bridgeScript, null)
                    }
                }
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun <T> DropdownSelector(options: List<T>, selectedValue: T, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(value = false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(selectedValue.toString(), color = TextWhite)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownSelectorInterval(selectedValue: Long, onSelect: (Long) -> Unit) {
    var expanded by remember { mutableStateOf(value = false) }
    val intervalMap = listOf(
        5000L to "5 seconds",
        10000L to "10 seconds",
        15000L to "15 seconds",
        30000L to "30 seconds",
        60000L to "60 seconds",
        9999L to "10~60s Random (Default)"
    )

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(getIntervalLabel(selectedValue), color = TextWhite)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            intervalMap.forEach { (ms, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(ms)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun getIntervalLabel(ms: Long): String = when (ms) {
    5000L -> "5s"
    10000L -> "10s"
    15000L -> "15s"
    30000L -> "30s"
    60000L -> "60s"
    else -> "10~60s Random"
}

suspend fun runNativeAutoSearch(
    limit: Int,
    intervalMs: Long,
    onProgress: (Int, String) -> Unit,
    onStatus: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    for (i in 1..limit) {
        currentCoroutineContext().ensureActive()

        val term = DataLists.getRandomTerm()
        val form = DataLists.getRandomForm()
        val encodedTerm = withContext(Dispatchers.IO) {
            URLEncoder.encode(term.lowercase(), StandardCharsets.UTF_8.name())
        }
        val searchUrl = "https://www.bing.com/search?q=$encodedTerm&FORM=$form&mkt=en-US&setmkt=en-US&isMobile=1"

        onProgress(i, term)
        onUrlChange(searchUrl)

        if (i < limit) {
            val delayDuration = if (intervalMs == 9999L) {
                Random.nextLong(10, 61) * 1000L
            } else {
                intervalMs
            }

            var remainingSeconds = delayDuration / 1000
            while (remainingSeconds > 0) {
                currentCoroutineContext().ensureActive()
                onStatus("Next search in ${remainingSeconds}s")
                delay(1000)
                remainingSeconds--
            }
        }
    }

    currentCoroutineContext().ensureActive()
    withContext(Dispatchers.Main) {
        onComplete()
    }
}
