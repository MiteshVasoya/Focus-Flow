package com.miteshvasoya.focusflow

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.addListener
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miteshvasoya.focusflow.data.TaskEntity
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splash.setOnExitAnimationListener { provider ->
                val v = provider.view
                ObjectAnimator.ofPropertyValuesHolder(
                    v,
                    PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.2f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.2f)
                ).apply {
                    duration = 520
                    interpolator = OvershootInterpolator(1.6f)
                    addListener(onEnd = { provider.remove() })
                    start()
                }
            }
        }

        setContent {
            MaterialTheme(colorScheme = dynamicColorOrDefault()) {
                Surface(Modifier.fillMaxSize()) {
                    val factory = remember { TaskVMFactory(this) }
                    val vm: TaskViewModel = viewModel(factory = factory)
                    FocusFlowApp(vm)
                }
            }
        }
    }
}

@Composable
fun dynamicColorOrDefault(): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        darkColorScheme(
            primary = Color(0xFF6EE7F5),
            secondary = Color(0xFFA78BFA),
            tertiary = Color(0xFFF472B6),
            background = Color(0xFF0B1020),
            surface = Color(0xFF11162A),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color(0xFFE6E8EF),
            onSurface = Color(0xFFE6E8EF)
        )
    }
}

@Composable
fun FocusFlowApp(vm: TaskViewModel) {
    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        // Confetti controller at the root
        val confetti = rememberConfettiController()
        ConfettiOverlay(controller = confetti)

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header()
            Spacer(Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                FocusTimer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    onCelebrate = { confetti.burst() }
                )
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                TaskList(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    vm = vm,
                    onTaskCompleted = { confetti.burst() }
                )
            }
        }
    }
}

@Composable
fun Header() {
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val timeOfDay = when (hour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..21 -> "Evening"
        else -> "Night"
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Focus Flow", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Good $timeOfDay! Let’s make progress with joy.", style = MaterialTheme.typography.bodyMedium)
        }
        PulsingDot()
    }
}

@Composable
fun PulsingDot() {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val scale by infinite.animateFloat(
        1f, 1.25f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale"
    )
    Box(
        Modifier
            .size(16.dp)
            .scale(scale)
            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
    )
}

@Preview
@Composable
fun AnimatedGradientBackground() {
    val infinite = rememberInfiniteTransition(label = "bg")
    val t by infinite.animateFloat(
        0f, 360f, animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "angle"
    )
    val colors = listOf(
        Color(0xFF0B1020),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        Color(0xFF0B1020)
    )
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val center = Offset(size.width / 2, size.height / 2)
                val brush = Brush.sweepGradient(colors = colors, center = center)
                rotate(t, center) { drawCircle(brush = brush, radius = size.maxDimension) }
            }
    )
}

/* ---------- Focus timer with animated ring and celebratory confetti ---------- */

@SuppressLint("AutoboxingStateCreation")
@Composable
fun FocusTimer(modifier: Modifier = Modifier, onCelebrate: () -> Unit) {
    var totalSeconds by remember { mutableIntStateOf(25 * 60) }
    var remaining by remember { mutableIntStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var bouncing by remember { mutableStateOf(false) }

    // Tick loop
    LaunchedEffect(isRunning) {
        while (isRunning && remaining > 0) {
            delay(1.seconds)
            remaining--
        }
        if (isRunning && remaining <= 0) {
            isRunning = false
            bouncing = true
            onCelebrate()
            delay(700)
            bouncing = false
            remaining = totalSeconds
        }
    }

    val progress by animateFloatAsState(
        targetValue = 1f - (remaining.toFloat() / totalSeconds.coerceAtLeast(1)),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val bounceScale by animateFloatAsState(
        targetValue = if (bouncing) 1.08f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.35f),
        label = "bounce"
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ProgressRing(
                size = 240.dp,
                progress = progress,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                glowColor = MaterialTheme.colorScheme.primary,
                scale = bounceScale
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val minutes = remaining / 60
                val seconds = remaining % 60
                Text(
                    String.format("%02d:%02d", minutes, seconds),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Crossfade(targetState = isRunning, label = "stateText") { running ->
                    Text(
                        if (running) "In session" else if (remaining == 0) "Completed" else "Ready",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        DurationChips(
            selected = totalSeconds,
            onSelect = {
                totalSeconds = it
                remaining = it
                isRunning = false
            },
            enabled = !isRunning
        )

        Spacer(Modifier.height(8.dp))

        ControlButtons(
            isRunning = isRunning,
            canReset = remaining < totalSeconds,
            onStartPause = { isRunning = !isRunning },
            onReset = {
                isRunning = false
                remaining = totalSeconds
            }
        )
    }
}

@Composable
fun ProgressRing(
    size: Dp,
    progress: Float,
    trackColor: Color,
    glowColor: Color,
    scale: Float = 1f
) {
    val stroke = 16.dp
    val cap = StrokeCap.Round
    val animatedGlow by rememberInfiniteTransition(label = "glow").animateFloat(
        0.6f, 1.0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAnim"
    )
    Canvas(Modifier.size(size).scale(scale)) {
        val px = stroke.toPx()
        // Track
        drawArc(
            color = trackColor,
            startAngle = -90f, sweepAngle = 360f,
            useCenter = false, style = Stroke(width = px, cap = cap),
            size = Size(size.toPx(), size.toPx()),
            topLeft = Offset.Zero
        )
        // Progress
        val sweep = (progress.coerceIn(0f, 1f)) * 360f
        val brush = Brush.sweepGradient(
            listOf(
                glowColor.copy(alpha = 0.3f),
                glowColor,
                glowColor.copy(alpha = 0.3f)
            )
        )
        drawArc(
            brush = brush,
            startAngle = -90f, sweepAngle = sweep,
            useCenter = false, style = Stroke(width = px, cap = cap),
            size = Size(size.toPx(), size.toPx()),
            topLeft = Offset.Zero
        )
        // Halo at tip
        if (sweep > 0f) {
            val r = size.toPx() / 2
            val angleRad = Math.toRadians((sweep - 90f).toDouble())
            val cx = size.toPx() / 2 + (r - px / 2) * cos(angleRad).toFloat()
            val cy = size.toPx() / 2 + (r - px / 2) * sin(angleRad).toFloat()
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(glowColor.copy(alpha = 0.35f * animatedGlow), Color.Transparent)
                ),
                radius = px * 1.6f,
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
fun DurationChips(selected: Int, onSelect: (Int) -> Unit, enabled: Boolean) {
    val presets = listOf(15 * 60, 25 * 60, 45 * 60)
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        presets.forEach { seconds ->
            val isSelected = selected == seconds
            val wobble by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
                label = "wobble"
            )
            FilterChip(
                selected = isSelected,
                onClick = { if (enabled) onSelect(seconds) },
                label = {
                    Text(
                        when (seconds) {
                            900 -> "15m"
                            1500 -> "25m"
                            else -> "45m"
                        }
                    )
                },
                enabled = enabled,
                modifier = Modifier.scale(wobble)
            )
        }
    }
}

@Composable
fun ControlButtons(
    isRunning: Boolean,
    canReset: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val label = if (isRunning) "Pause" else "Start"
        ElevatedButton(
            onClick = onStartPause,
            modifier = Modifier.padding(6.dp),
        ) { Text(label) }
        Spacer(Modifier.width(8.dp))
        OutlinedButton(
            onClick = onReset,
            enabled = canReset,
            modifier = Modifier.padding(6.dp)
        ) { Text("Reset") }
    }
}

/* ---------- Confetti overlay (playful) ---------- */

class ConfettiController {
    var trigger by mutableIntStateOf(0)
        private set
    fun burst() { trigger++ }
}

@Preview
@Composable
fun rememberConfettiController(): ConfettiController = remember { ConfettiController() }

@Composable
fun ConfettiOverlay(controller: ConfettiController) {
    var emission by remember { mutableIntStateOf(0) }
    LaunchedEffect(controller.trigger) {
        emission++
        // auto-clear after some time
        delay(1400)
        emission--
    }
    if (emission <= 0) return

    val particles = remember { generateParticles(80) }
    val anim = remember { Animatable(0f) }
    LaunchedEffect(emission) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(1200, easing = LinearOutSlowInEasing))
    }
    Canvas(Modifier.fillMaxSize()) {
        val t = anim.value
        particles.forEach { p ->
            val progress = t
            val x = size.width * 0.5f + p.vx * progress * size.width * 0.4f
            val y = size.height * 0.35f + (p.vy * progress + 0.8f * progress * progress) * size.height * 0.6f
            val rot = p.rotation + progress * p.rotationSpeed
            val alpha = 1f - progress
            withTransform({
                translate(x, y)
                rotate(rot)
            }) {
                drawRoundRect(
                    color = p.color.copy(alpha = alpha),
                    size = Size(p.size, p.size * 0.6f),
                    cornerRadius = CornerRadius(p.size * 0.2f, p.size * 0.2f)
                )
            }
        }
    }
}

data class Particle(
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

fun generateParticles(count: Int): List<Particle> {
    val rnd = Random(System.currentTimeMillis())
    val palette = listOf(
        Color(0xFF6EE7F5), Color(0xFFA78BFA), Color(0xFFF472B6),
        Color(0xFFFFE066), Color(0xFF7EE081)
    )
    return List(count) {
        Particle(
            vx = rnd.nextFloat() * 2f - 1f,            // left/right
            vy = -rnd.nextFloat() * 1.2f - 0.2f,       // upwards initial
            color = palette[rnd.nextInt(palette.size)],
            size = rnd.nextFloat() * 16f + 8f,
            rotation = rnd.nextFloat() * 360f,
            rotationSpeed = (rnd.nextFloat() * 360f + 180f) * if (rnd.nextBoolean()) 1 else -1
        )
    }
}

/* ---------- Task list with Room persistence and playful row animations ---------- */

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TaskList(modifier: Modifier = Modifier, vm: TaskViewModel, onTaskCompleted: () -> Unit) {
    val tasks by vm.tasks.collectAsState()

    var input by remember { mutableStateOf("") }
    val addEnabled = input.isNotBlank()

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Add a quick task") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(
                onClick = {
                    vm.add(input)
                    input = ""
                    onTaskCompleted() // little celebration for adding momentum
                },
                enabled = addEnabled
            ) { Text("Add") }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tasks, key = { it.id }) { task ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.7f)
                    ) + fadeIn(),
                    exit = fadeOut(tween(200))
                ) {
                    TaskRow(
                        task = task,
                        onToggle = {
                            vm.toggle(task)
                            if (!task.done) onTaskCompleted()
                        },
                        onDelete = { vm.remove(task) }
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { vm.clearCompleted() }) { Text("Clear completed") }
        }
    }
}

@Composable
fun TaskRow(task: TaskEntity, onToggle: () -> Unit, onDelete: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (task.done) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(400),
        label = "rowBg"
    )
    val strike = if (task.done) TextDecoration.LineThrough else TextDecoration.None
    val alpha by animateFloatAsState(if (task.done) 0.6f else 1f, label = "rowAlpha")
    val pop by animateFloatAsState(
        targetValue = if (task.done) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label = "rowScale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier.fillMaxWidth().scale(pop)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    textDecoration = strike,
                    modifier = Modifier.graphicsLayer { this.alpha = alpha }
                )
            }
            AnimatedVisibility(visible = task.done) {
                Row {
                    AssistChip(onClick = onDelete, label = { Text("Remove") })
                }
            }
        }
    }
}
