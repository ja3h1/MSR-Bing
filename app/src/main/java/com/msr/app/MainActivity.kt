package com.msr.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.random.Random

val DarkBg = Color(0xFF121214)
val SurfaceBg = Color(0xFF1E1E24)
val NeonGreen = Color(0xFF00B09B)
val LightLime = Color(0xFF96C93D)
val NeonRed = Color(0xFFFF416C)
val BrightOrange = Color(0xFFFF4B2B)
val TextWhite = Color(0xFFE1E1E6)

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

    var searchLimit by remember { mutableIntStateOf(35) }
    var selectedIntervalMs by remember { mutableLongStateOf(9999L) }
    var openInBrowser by remember { mutableStateOf(true) }

    var isRunning by remember { mutableStateOf(false) }
    var currentProgressText by remember { mutableStateOf("(0/$searchLimit)") }
    var timerStatusText by remember { mutableStateOf("Ready to start") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = DarkBg,
            surface = SurfaceBg
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Warning Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF321214)),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NeonRed)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = ""Warning": Take at your own Risk",
                            color = NeonRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Start / Stop Action Buttons
                if (!isRunning) {
                    Button(
                        onClick = {
                            isRunning = true
                            searchJob = coroutineScope.launch(Dispatchers.IO) {
                                runAutoSearch(
                                    context = context,
                                    limit = searchLimit,
                                    intervalMs = selectedIntervalMs,
                                    openInBrowser = openInBrowser,
                                    onProgress = { current, max ->
                                        currentProgressText = "($current/$max)"
                                    },
                                    onStatus = { status ->
                                        timerStatusText = status
                                    },
                                    onComplete = {
                                        isRunning = false
                                        timerStatusText = "Finished all searches!"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rewards.bing.com/pointsbreakdown"))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                brush = Brush.horizontalGradient(listOf(NeonGreen, LightLime)),
                                shape = RoundedCornerShape(50.dp)
                            )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                Spacer(Modifier.width(6.dp))
                                Text("Start", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "Count: $searchLimit | Interval: ${getIntervalLabel(selectedIntervalMs)}",
                                color = Color.Black.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            searchJob?.cancel()
                            isRunning = false
                            timerStatusText = "Stopped by user."
                        },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                brush = Brush.horizontalGradient(listOf(NeonRed, BrightOrange)),
                                shape = RoundedCornerShape(50.dp)
                            )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Stop $currentProgressText",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = timerStatusText,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Settings Trigger Button
                Button(
                    onClick = { if (!isRunning) showSettingsDialog = true },
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = DarkBg)
                    Spacer(Modifier.width(6.dp))
                    Text("Settings", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            }

            // Settings Modal
            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    containerColor = SurfaceBg,
                    title = {
                        Text("Settings", color = TextWhite, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("Search Count:", color = TextWhite, fontSize = 13.sp)
                            DropdownSelector(
                                options = listOf(5, 10, 20, 25, 30, 35, 40, 45, 50),
                                selectedValue = searchLimit,
                                onSelect = { searchLimit = it }
                            )

                            Text("Interval Delay:", color = TextWhite, fontSize = 13.sp)
                            DropdownSelectorInterval(
                                selectedValue = selectedIntervalMs,
                                onSelect = { selectedIntervalMs = it }
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open in External Browser:", color = TextWhite, fontSize = 13.sp)
                                Switch(
                                    checked = openInBrowser,
                                    onCheckedChange = { openInBrowser = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text("Save", color = NeonGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun <T> DropdownSelector(options: List<T>, selectedValue: T, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text("$selectedValue", color = TextWhite)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("$option") },
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
    var expanded by remember { mutableStateOf(false) }
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
            shape = RoundedCornerShape(50.dp)
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

suspend fun runAutoSearch(
    context: Context,
    limit: Int,
    intervalMs: Long,
    openInBrowser: Boolean,
    onProgress: (Int, Int) -> Unit,
    onStatus: (String) -> Unit,
    onComplete: () -> Unit
) {
    for (i in 1..limit) {
        val term = DataLists.getRandomTerm()
        val form = DataLists.getRandomForm()
        val encodedTerm = URLEncoder.encode(term.lowercase(), "UTF-8")
        val searchUrl = "https://www.bing.com/search?q=$encodedTerm&FORM=$form&mkt=en-US&setmkt=en-US&isMobile=1"

        onProgress(i, limit)

        if (openInBrowser) {
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val conn = URL(searchUrl).openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                    )
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    conn.connect()
                    conn.responseCode
                    conn.disconnect()
                } catch (_: Exception) {}
            }
        }

        if (i < limit) {
            val delayDuration = if (intervalMs == 9999L) {
                Random.nextLong(10_000, 60_000)
            } else {
                intervalMs
            }

            var remaining = delayDuration
            while (remaining > 0) {
                onStatus("Next search in ${remaining / 1000}s...")
                delay(1000)
                remaining -= 1000
            }
        }
    }
    withContext(Dispatchers.Main) {
        onComplete()
    }
}
