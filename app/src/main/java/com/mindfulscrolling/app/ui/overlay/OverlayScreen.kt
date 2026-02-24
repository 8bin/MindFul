package com.mindfulscrolling.app.ui.overlay

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Overlay colors (independent of system theme since it draws over other apps)
private val OverlayBgStart = Color(0xFF0A1118)
private val OverlayBgEnd = Color(0xFF0F1923)
private val OverlayCardBg = Color(0xFF1A2D3D)
private val OverlayTeal = Color(0xFF26A69A)
private val OverlayCyan = Color(0xFF00E5FF)
private val OverlayRed = Color(0xFFFF6B6B)
private val OverlayAmber = Color(0xFFFFCA28)
private val OverlayTextWhite = Color(0xFFF0F4F8)
private val OverlayTextGray = Color(0xFF8BA4B8)

private val motivationalQuotes = listOf(
    "Every moment of self-control is a victory. 🏆",
    "Your time is your most valuable currency. 💎",
    "Choose presence over screen time. 🧘",
    "Small breaks lead to big clarity. ✨",
    "Your future self will thank you. 🌟",
    "Disconnect to reconnect with what matters. 💚",
    "Mindful scrolling starts with mindful stopping. 🛑",
    "You're stronger than the scroll. 💪"
)

@Composable
fun OverlayScreen(
    packageName: String,
    isBreakMode: Boolean = false,
    remainingTime: Long = 0,
    breakEndTime: Long = 0L,
    whitelistedApps: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onOverride: (String) -> Unit,
    onLaunchApp: (String) -> Unit = {}
) {
    var showChallenge by remember { mutableStateOf(false) }
    val quote = remember { motivationalQuotes.random() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        OverlayBgStart.copy(alpha = 0.97f),
                        OverlayBgEnd.copy(alpha = 0.98f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isBreakMode) {
            BreakContent(
                breakEndTime = breakEndTime,
                remainingTime = remainingTime,
                quote = quote,
                whitelistedApps = whitelistedApps,
                onDismiss = onDismiss,
                onLaunchApp = onLaunchApp
            )

            // Phone & Camera quick-access buttons at bottom corners
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Phone button
                val context = LocalContext.current
                val dialerPackage = remember {
                    val dialerIntent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                    context.packageManager.resolveActivity(dialerIntent, 0)?.activityInfo?.packageName
                        ?: "com.google.android.dialer"
                }
                val cameraPackage = remember {
                    val cameraIntent = android.provider.MediaStore.ACTION_IMAGE_CAPTURE
                    val intent = android.content.Intent(cameraIntent)
                    context.packageManager.resolveActivity(intent, 0)?.activityInfo?.packageName
                        ?: "com.android.camera"
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(OverlayTeal.copy(alpha = 0.25f))
                        .clickable { onLaunchApp(dialerPackage) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📞", fontSize = 24.sp)
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(OverlayTeal.copy(alpha = 0.25f))
                        .clickable { onLaunchApp(cameraPackage) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 24.sp)
                }
            }
        } else if (showChallenge) {
            ChallengeContent(
                onDismiss = { showChallenge = false },
                onSuccess = { onOverride("Emergency Override") }
            )
        } else {
            BlockedContent(
                packageName = packageName,
                quote = quote,
                onDismiss = onDismiss,
                onChallengeRequest = { showChallenge = true }
            )
        }
    }
}

// ─── Breathing phase enum ───────────────────────────────────────────────────
private enum class BreathPhase(val label: String, val durationMs: Int) {
    BREATHE_IN("Breathe In", 4000),
    HOLD("Hold", 2000),
    BREATHE_OUT("Breathe Out", 4000)
}

@Composable
fun BreakContent(
    breakEndTime: Long,
    remainingTime: Long,
    quote: String,
    whitelistedApps: List<String>,
    onDismiss: () -> Unit,
    onLaunchApp: (String) -> Unit
) {
    // ── Live countdown ticker ────────────────────────────────────────────
    val effectiveEndTime = if (breakEndTime > 0) breakEndTime
                           else System.currentTimeMillis() + remainingTime
    var remainingMs by remember { mutableStateOf(effectiveEndTime - System.currentTimeMillis()) }

    LaunchedEffect(effectiveEndTime) {
        while (true) {
            remainingMs = (effectiveEndTime - System.currentTimeMillis()).coerceAtLeast(0)
            delay(1000)
        }
    }

    val minutes = (remainingMs / 60000).toInt()
    val seconds = ((remainingMs % 60000) / 1000).toInt()

    // ── 3-phase breathing animation ─────────────────────────────────────
    var breathPhase by remember { mutableStateOf(BreathPhase.BREATHE_IN) }
    var phaseProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            for (phase in BreathPhase.entries) {
                breathPhase = phase
                val steps = phase.durationMs / 16  // ~60fps
                for (i in 0..steps) {
                    phaseProgress = i.toFloat() / steps
                    delay(16)
                }
            }
        }
    }

    // Scale: expand on BREATHE_IN, hold, contract on BREATHE_OUT
    val targetScale = when (breathPhase) {
        BreathPhase.BREATHE_IN -> 0.85f + (0.30f * phaseProgress)
        BreathPhase.HOLD -> 1.15f
        BreathPhase.BREATHE_OUT -> 1.15f - (0.30f * phaseProgress)
    }

    // Text fade: fade in at start of phase, fade out near end
    val textAlpha = when {
        phaseProgress < 0.15f -> phaseProgress / 0.15f  // fade in
        phaseProgress > 0.85f -> (1f - phaseProgress) / 0.15f  // fade out
        else -> 1f
    }

    // Phase-specific color
    val phaseColor = when (breathPhase) {
        BreathPhase.BREATHE_IN -> OverlayCyan
        BreathPhase.HOLD -> OverlayAmber
        BreathPhase.BREATHE_OUT -> OverlayTeal
    }

    // Glow ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Bubble background alpha synced with breathing
    val bubbleAlpha = when (breathPhase) {
        BreathPhase.BREATHE_IN -> 0.4f + (0.4f * phaseProgress)
        BreathPhase.HOLD -> 0.8f
        BreathPhase.BREATHE_OUT -> 0.8f - (0.4f * phaseProgress)
    }

    // Rotating tips with crossfade
    val tips = listOf(
        "🌿 Take a deep breath and relax",
        "💧 Grab a glass of water",
        "👀 Look at something 20 feet away",
        "🚶 Stretch your legs, walk around",
        "📝 Write down one thing you're grateful for",
        "🎵 Listen to your favorite song"
    )
    var currentTip by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            currentTip = (currentTip + 1) % tips.size
        }
    }

    // Essential apps info
    val context = LocalContext.current
    data class EssentialAppInfo(val packageName: String, val name: String, val icon: Drawable?)
    val essentialApps = remember(whitelistedApps) {
        whitelistedApps.mapNotNull { pkg ->
            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val name = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(appInfo)
                EssentialAppInfo(pkg, name, icon)
            } catch (_: Exception) { null }
        }.filter {
            // Filter out system internals — only show user-meaningful apps
            !it.packageName.startsWith("com.android.systemui") &&
            !it.packageName.startsWith("com.android.settings") &&
            it.packageName != "com.mindfulscrolling.app"
        }
    }

    var showEssentialApps by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Take a Breath",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = OverlayTeal
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Animated breathing circle with glow ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            // Outer glow ring (shimmer)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(glowScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                phaseColor.copy(alpha = glowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Middle pulsing ring
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(targetScale * 0.95f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                phaseColor.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Inner breathing circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(targetScale)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                phaseColor.copy(alpha = bubbleAlpha),
                                OverlayCyan.copy(alpha = bubbleAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = breathPhase.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = OverlayTextWhite.copy(alpha = textAlpha),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Live countdown timer
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OverlayCardBg)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Break ends in",
                    style = MaterialTheme.typography.bodySmall,
                    color = OverlayTextGray
                )
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = OverlayTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Essential Apps section
        if (essentialApps.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = OverlayCardBg.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEssentialApps = !showEssentialApps },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📱 Essential Apps",
                            style = MaterialTheme.typography.titleSmall,
                            color = OverlayTextWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (showEssentialApps) "▲" else "▼",
                            color = OverlayTextGray,
                            fontSize = 12.sp
                        )
                    }
                    
                    AnimatedVisibility(visible = showEssentialApps) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tap an app to open it",
                                style = MaterialTheme.typography.bodySmall,
                                color = OverlayTextGray.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(essentialApps, key = { it.packageName }) { app ->
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { onLaunchApp(app.packageName) }
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(OverlayCardBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val icon = app.icon
                                            if (icon != null) {
                                                Image(
                                                    bitmap = overlayDrawableToBitmap(icon).asImageBitmap(),
                                                    contentDescription = app.name,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            } else {
                                                Text("📱", fontSize = 18.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = app.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OverlayTextGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.widthIn(max = 60.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rotating tips with crossfade
        AnimatedContent(
            targetState = currentTip,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith
                fadeOut(animationSpec = tween(600))
            },
            label = "tipCrossfade"
        ) { tipIndex ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = OverlayCardBg.copy(alpha = 0.6f))
            ) {
                Text(
                    text = tips[tipIndex],
                    style = MaterialTheme.typography.bodyMedium,
                    color = OverlayTextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quote
        Text(
            text = quote,
            style = MaterialTheme.typography.bodySmall,
            color = OverlayTextGray.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun BlockedContent(
    packageName: String,
    quote: String,
    onDismiss: () -> Unit,
    onChallengeRequest: () -> Unit
) {
    // Resolve app name
    val context = LocalContext.current
    val appName = remember(packageName) {
        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }
    }
    
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stop icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(OverlayRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⏰",
                fontSize = 36.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Time's Up!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = OverlayRed
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your daily limit for $appName has been reached",
            style = MaterialTheme.typography.bodyLarge,
            color = OverlayTextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Quote card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OverlayCardBg)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OverlayTextGray,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = OverlayTeal),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                "Close App",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onChallengeRequest,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = OverlayAmber
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    listOf(OverlayAmber.copy(alpha = 0.5f), OverlayAmber.copy(alpha = 0.3f))
                )
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                "5 More Minutes ⚡",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ChallengeContent(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val num1 = remember { (10..50).random() }
    val num2 = remember { (10..50).random() }
    val answer = num1 + num2
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Brain icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(OverlayAmber.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🧠", fontSize = 28.sp)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Quick Challenge",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = OverlayAmber
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Solve this to unlock 5 more minutes",
            style = MaterialTheme.typography.bodyMedium,
            color = OverlayTextGray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Math problem card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OverlayCardBg)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$num1 + $num2 = ?",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = OverlayTextWhite
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) {
                            input = it
                            error = false
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your answer", color = OverlayTextGray) },
                    shape = RoundedCornerShape(12.dp),
                    isError = error,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OverlayTextWhite,
                        unfocusedTextColor = OverlayTextWhite,
                        focusedBorderColor = OverlayTeal,
                        unfocusedBorderColor = OverlayTextGray.copy(alpha = 0.3f),
                        errorBorderColor = OverlayRed,
                        cursorColor = OverlayTeal
                    )
                )
                
                if (error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Wrong answer, try again!",
                        color = OverlayRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OverlayTextGray)
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
            
            Button(
                onClick = {
                    if (input == answer.toString()) {
                        onSuccess()
                    } else {
                        error = true
                        input = ""
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OverlayAmber),
                enabled = input.isNotEmpty()
            ) {
                Text(
                    "Unlock",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

private fun overlayDrawableToBitmap(drawable: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(1),
        drawable.intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
