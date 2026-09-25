package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameProfile
import com.example.ui.MantisViewModel
import com.example.ui.components.GamepadKeyChip
import com.example.ui.components.StatusBadge
import com.example.ui.components.TelemetryStat
import com.example.ui.theme.ButtonA
import com.example.ui.theme.ButtonB
import com.example.ui.theme.ButtonX
import com.example.ui.theme.ButtonY
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class BulletTrace(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: Color,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun TestArenaScreen(
    viewModel: MantisViewModel,
    profile: GameProfile?,
    onBack: () -> Unit
) {
    val controllerState by viewModel.controllerState.collectAsState()
    val activeTouches by viewModel.activeTouches.collectAsState()
    val mappings by viewModel.mappings.collectAsState()

    // Interactive 3D Arena state
    var playerPosX by remember { mutableFloatStateOf(0.5f) }
    var playerPosY by remember { mutableFloatStateOf(0.5f) }
    var cameraLookX by remember { mutableFloatStateOf(0.5f) }
    var cameraLookY by remember { mutableFloatStateOf(0.5f) }
    var isFiring by remember { mutableStateOf(false) }
    var ammoCount by remember { mutableIntStateOf(30) }
    var score by remember { mutableIntStateOf(1450) }
    val bulletTraces = remember { mutableStateListOf<BulletTrace>() }
    val latencyHistory = remember { mutableStateListOf(1.8f, 1.6f, 1.9f, 2.1f, 1.7f, 1.5f, 2.0f, 1.6f) }

    // Game loop for controller response
    LaunchedEffect(controllerState) {
        // Move player based on Left Stick
        if (controllerState.leftStickX != 0f || controllerState.leftStickY != 0f) {
            playerPosX = (playerPosX + controllerState.leftStickX * 0.02f).coerceIn(0.15f, 0.85f)
            playerPosY = (playerPosY + controllerState.leftStickY * 0.02f).coerceIn(0.15f, 0.85f)
        }

        // Aim camera based on Right Stick
        if (controllerState.rightStickX != 0f || controllerState.rightStickY != 0f) {
            cameraLookX = (cameraLookX + controllerState.rightStickX * 0.03f).coerceIn(0.1f, 0.9f)
            cameraLookY = (cameraLookY + controllerState.rightStickY * 0.03f).coerceIn(0.1f, 0.9f)
        }

        // Trigger fire
        val firePressed = controllerState.pressedButtons.contains("RT") || controllerState.rightTrigger > 0.3f
        if (firePressed && !isFiring && ammoCount > 0) {
            isFiring = true
            ammoCount--
            score += 50
            viewModel.triggerVibration()
        } else if (!firePressed) {
            isFiring = false
        }

        // Reload on X
        if (controllerState.pressedButtons.contains("BUTTON_X")) {
            ammoCount = 30
        }

        // Keep latency graph running
        if (latencyHistory.size > 20) latencyHistory.removeAt(0)
        latencyHistory.add(controllerState.latencyMs)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF06090F))
            .testTag("test_arena_root")
    ) {
        val arenaWidthPx = constraints.maxWidth.toFloat()
        val arenaHeightPx = constraints.maxHeight.toFloat()

        // 3D Perspective Virtual Arena Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cyber Horizon gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF04060A), Color(0xFF0D1424), Color(0xFF131D33)),
                    startY = 0f,
                    endY = h * 0.5f
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w, h * 0.5f)
            )

            // Neon Ground Grid
            val horizonY = h * 0.42f
            drawLine(color = CyberCyan.copy(alpha = 0.8f), start = Offset(0f, horizonY), end = Offset(w, horizonY), strokeWidth = 2f)

            // Perspective grid lines
            val vanishingPointX = w * cameraLookX
            val vanishingPointY = horizonY
            for (i in -8..8) {
                val startX = w / 2 + (i * (w / 10f))
                drawLine(
                    color = CyberCyan.copy(alpha = 0.25f),
                    start = Offset(vanishingPointX, vanishingPointY),
                    end = Offset(startX, h),
                    strokeWidth = 1.5f
                )
            }

            // Horizontal depth grid lines
            var depthY = horizonY + 20f
            var step = 8f
            while (depthY < h) {
                drawLine(
                    color = CyberCyan.copy(alpha = 0.18f),
                    start = Offset(0f, depthY),
                    end = Offset(w, depthY),
                    strokeWidth = 1f
                )
                depthY += step
                step *= 1.35f
            }

            // Floating target drones in arena
            val drone1X = w * 0.35f
            val drone1Y = horizonY - 40f
            drawCircle(color = NeonRed.copy(alpha = 0.8f), radius = 14f, center = Offset(drone1X, drone1Y))
            drawCircle(color = Color.White, radius = 5f, center = Offset(drone1X, drone1Y))

            val drone2X = w * 0.68f
            val drone2Y = horizonY - 70f
            drawCircle(color = NeonOrange.copy(alpha = 0.8f), radius = 18f, center = Offset(drone2X, drone2Y))
            drawCircle(color = Color.White, radius = 6f, center = Offset(drone2X, drone2Y))

            // Player character position in arena
            val px = w * playerPosX
            val py = horizonY + (h - horizonY) * (playerPosY * 0.8f)
            // Player shadow & marker
            drawOval(
                color = ElectricViolet.copy(alpha = 0.4f),
                topLeft = Offset(px - 28f, py + 18f),
                size = Size(56f, 16f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(CyberCyan, ElectricViolet), center = Offset(px, py), radius = 24f),
                radius = 18f,
                center = Offset(px, py)
            )

            // Dynamic Aiming Reticle / Crosshair
            val aimX = w * cameraLookX
            val aimY = horizonY * (0.8f + cameraLookY * 0.4f)
            drawCircle(color = CyberCyan.copy(alpha = 0.8f), radius = 20f, center = Offset(aimX, aimY), style = Stroke(2f))
            drawLine(color = CyberCyan, start = Offset(aimX - 30f, aimY), end = Offset(aimX - 8f, aimY), strokeWidth = 2f)
            drawLine(color = CyberCyan, start = Offset(aimX + 8f, aimY), end = Offset(aimX + 30f, aimY), strokeWidth = 2f)
            drawLine(color = CyberCyan, start = Offset(aimX, aimY - 30f), end = Offset(aimX, aimY - 8f), strokeWidth = 2f)
            drawLine(color = CyberCyan, start = Offset(aimX, aimY + 8f), end = Offset(aimX, aimY + 30f), strokeWidth = 2f)

            // Bullet tracers when firing
            if (isFiring) {
                drawLine(
                    color = Color.White,
                    start = Offset(px, py - 10f),
                    end = Offset(aimX, aimY),
                    strokeWidth = 4f
                )
                drawLine(
                    color = CyberCyan,
                    start = Offset(px, py - 10f),
                    end = Offset(aimX, aimY),
                    strokeWidth = 2f
                )
                drawCircle(color = Color(0xFFFDE047), radius = 16f, center = Offset(aimX, aimY))
            }

            // Real-time Injected Touch Visualizer Rings
            activeTouches.forEach { touch ->
                val tx = touch.xPercent * w
                val ty = touch.yPercent * h
                val ringColor = when (touch.gestureType) {
                    com.example.data.model.TouchGestureType.SWIPE -> NeonOrange
                    com.example.data.model.TouchGestureType.HOLD -> ElectricViolet
                    else -> CyberCyan
                }
                drawCircle(
                    color = ringColor.copy(alpha = 0.45f),
                    radius = 38f,
                    center = Offset(tx, ty)
                )
                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = Offset(tx, ty)
                )
            }
        }

        // Top Navigation & Stats Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = profile?.title ?: "Live Test Arena",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Low Latency Simulation Sandbox",
                            color = CyberCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = "${String.format("%.1f", controllerState.latencyMs)} ms",
                        isActive = true,
                        activeColor = NeonGreen
                    )
                    StatusBadge(
                        text = "Score: $score",
                        isActive = true,
                        activeColor = ElectricViolet
                    )
                }
            }

            // HUD Overlay info: Ammo, Controller prompt
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ammo Counter
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDarkElevated.copy(alpha = 0.85f))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AMMO: ", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$ammoCount / 30", color = if (ammoCount > 5) CyberCyan else NeonRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // In-Game Touch Coordinates readout
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDarkElevated.copy(alpha = 0.85f))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Touches: ", color = TextSecondary, fontSize = 11.sp)
                    Text("${activeTouches.size}", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // On-Screen Virtual Testing Controls (allows testing immediately on emulator or without gamepad)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Virtual Left Thumbstick (Move)
                VirtualAnalogStick(
                    label = "MOVE (L-Stick)",
                    accentColor = CyberCyan,
                    onMove = { x, y ->
                        viewModel.simulateStick(isLeft = true, x = x, y = y)
                    }
                )

                // Quick Action Buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VirtualActionButton("RT", "FIRE", CyberCyan) { pressed ->
                            viewModel.simulateButton("RT", pressed)
                        }
                        VirtualActionButton("LT", "ADS", ElectricViolet) { pressed ->
                            viewModel.simulateButton("LT", pressed)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VirtualActionButton("X", "RELOAD", ButtonX) { pressed ->
                            viewModel.simulateButton("BUTTON_X", pressed)
                        }
                        VirtualActionButton("Y", "SWAP", ButtonY) { pressed ->
                            viewModel.simulateButton("BUTTON_Y", pressed)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VirtualActionButton("B", "CROUCH", ButtonB) { pressed ->
                            viewModel.simulateButton("BUTTON_B", pressed)
                        }
                        VirtualActionButton("A", "JUMP", ButtonA) { pressed ->
                            viewModel.simulateButton("BUTTON_A", pressed)
                        }
                    }
                }

                // Virtual Right Thumbstick (Aim)
                VirtualAnalogStick(
                    label = "AIM (R-Stick)",
                    accentColor = ElectricViolet,
                    onMove = { x, y ->
                        viewModel.simulateStick(isLeft = false, x = x, y = y)
                    }
                )
            }
        }
    }
}

@Composable
fun VirtualAnalogStick(
    label: String,
    accentColor: Color,
    onMove: (Float, Float) -> Unit
) {
    var stickOffsetX by remember { mutableFloatStateOf(0f) }
    var stickOffsetY by remember { mutableFloatStateOf(0f) }
    val maxRadiusPx = 70f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(SurfaceDark.copy(alpha = 0.85f))
                .border(2.dp, accentColor.copy(alpha = 0.5f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            stickOffsetX = 0f
                            stickOffsetY = 0f
                            onMove(0f, 0f)
                        },
                        onDragCancel = {
                            stickOffsetX = 0f
                            stickOffsetY = 0f
                            onMove(0f, 0f)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            stickOffsetX = (stickOffsetX + dragAmount.x).coerceIn(-maxRadiusPx, maxRadiusPx)
                            stickOffsetY = (stickOffsetY + dragAmount.y).coerceIn(-maxRadiusPx, maxRadiusPx)
                            onMove(stickOffsetX / maxRadiusPx, stickOffsetY / maxRadiusPx)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(stickOffsetX.roundToInt(), stickOffsetY.roundToInt()) }
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun VirtualActionButton(
    keyLabel: String,
    actionName: String,
    accentColor: Color,
    onPressChanged: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPressed) accentColor else SurfaceDark.copy(alpha = 0.85f))
            .border(1.5.dp, if (isPressed) Color.White else accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isPressed = true
                        onPressChanged(true)
                    },
                    onDragEnd = {
                        isPressed = false
                        onPressChanged(false)
                    },
                    onDragCancel = {
                        isPressed = false
                        onPressChanged(false)
                    },
                    onDrag = { _, _ -> }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = keyLabel,
                color = if (isPressed) Color.Black else TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
            Text(
                text = actionName,
                color = if (isPressed) Color.Black else TextSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
