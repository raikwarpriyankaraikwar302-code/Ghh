package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActiveTouchSimulation
import com.example.data.model.ControllerInputState
import com.example.data.model.OverlayHudConfig
import com.example.data.model.OverlayHudMode
import com.example.data.model.TouchGestureType
import com.example.data.model.TriggeredActionItem
import com.example.ui.theme.ButtonA
import com.example.ui.theme.ButtonB
import com.example.ui.theme.ButtonX
import com.example.ui.theme.ButtonY
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun OverlayHudComponent(
    hudConfig: OverlayHudConfig,
    controllerState: ControllerInputState,
    activeTouches: List<ActiveTouchSimulation>,
    recentTriggers: List<TriggeredActionItem>,
    onModeChange: (OverlayHudMode) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onMinimizeToggle: (Boolean) -> Unit,
    onClose: () -> Unit,
    onTriggerTest: (String, String, TouchGestureType) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hudConfig.isVisible) return

    var offsetX by remember { mutableFloatStateOf(hudConfig.offsetX) }
    var offsetY by remember { mutableFloatStateOf(hudConfig.offsetY) }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(-100f, 600f)
                        offsetY = (offsetY + dragAmount.y).coerceIn(-50f, 1600f)
                    }
                }
                .testTag("overlay_hud_container")
        ) {
            if (hudConfig.isMinimized) {
                // Minimized floating bubble
                MinimizedHudBubble(
                    controllerState = controllerState,
                    latestTrigger = recentTriggers.firstOrNull(),
                    onExpand = { onMinimizeToggle(false) }
                )
            } else {
                // Expanded floating HUD Window
                ExpandedHudWindow(
                    hudConfig = hudConfig,
                    controllerState = controllerState,
                    activeTouches = activeTouches,
                    recentTriggers = recentTriggers,
                    onModeChange = onModeChange,
                    onOpacityChange = onOpacityChange,
                    onMinimize = { onMinimizeToggle(true) },
                    onClose = onClose,
                    onTriggerTest = onTriggerTest,
                    onClearHistory = onClearHistory
                )
            }
        }
    }
}

@Composable
private fun MinimizedHudBubble(
    controllerState: ControllerInputState,
    latestTrigger: TriggeredActionItem?,
    onExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xE60D121F))
            .border(1.5.dp, CyberCyan, RoundedCornerShape(28.dp))
            .clickable { onExpand() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("overlay_hud_minimized_bubble"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (controllerState.isConnected) NeonGreen else Color(0xFF64748B))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Expand HUD",
            tint = CyberCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "HUD",
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
        )
        if (latestTrigger != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "• ${latestTrigger.actionName}",
                color = ElectricViolet,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
        if (controllerState.pressedButtons.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberCyan.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${controllerState.pressedButtons.size} Active",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExpandedHudWindow(
    hudConfig: OverlayHudConfig,
    controllerState: ControllerInputState,
    activeTouches: List<ActiveTouchSimulation>,
    recentTriggers: List<TriggeredActionItem>,
    onModeChange: (OverlayHudMode) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onTriggerTest: (String, String, TouchGestureType) -> Unit,
    onClearHistory: () -> Unit
) {
    val alpha = hudConfig.opacity
    val containerBg = Color(0xFF090D16).copy(alpha = alpha)

    Surface(
        modifier = Modifier
            .width(360.dp)
            .shadow(24.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(containerBg)
            .border(1.5.dp, Brush.linearGradient(listOf(CyberCyan, ElectricViolet.copy(alpha = 0.6f))), RoundedCornerShape(20.dp))
            .testTag("overlay_hud_expanded_window"),
        color = containerBg,
        tonalElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag HUD",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (controllerState.isConnected) NeonGreen else Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OVERLAY HUD",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.1f", controllerState.latencyMs)}ms",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Opacity Cycler Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDarkElevated)
                            .clickable {
                                val next = when (hudConfig.opacity) {
                                    in 0.85f..1.0f -> 0.60f
                                    in 0.55f..0.84f -> 0.35f
                                    else -> 0.95f
                                }
                                onOpacityChange(next)
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(hudConfig.opacity * 100).toInt()}%",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("overlay_hud_minimize_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Minimize HUD",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("overlay_hud_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close HUD",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDarkElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OverlayHudMode.values().forEach { mode ->
                    val isSelected = hudConfig.mode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { onModeChange(mode) }
                            .padding(vertical = 6.dp)
                            .testTag("overlay_hud_tab_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSelected) CyberCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body depending on HUD mode
            when (hudConfig.mode) {
                OverlayHudMode.GAMEPAD_SCHEMATIC -> {
                    GamepadSchematicView(
                        controllerState = controllerState,
                        recentTriggers = recentTriggers
                    )
                }
                OverlayHudMode.GESTURE_FEED -> {
                    GestureFeedView(
                        recentTriggers = recentTriggers,
                        onClearHistory = onClearHistory
                    )
                }
                OverlayHudMode.COMPACT_PILL -> {
                    CompactPillView(
                        controllerState = controllerState,
                        recentTriggers = recentTriggers
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Interactive Trigger Tester Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceDarkElevated)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TEST ACTIONS:",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TestTriggerChip(
                        label = "A (Tap)",
                        color = ButtonA,
                        testTag = "overlay_hud_test_action_a",
                        onClick = { onTriggerTest("Jump", "BUTTON_A", TouchGestureType.TAP) }
                    )
                    TestTriggerChip(
                        label = "B (Swipe)",
                        color = ButtonB,
                        testTag = "overlay_hud_test_action_b",
                        onClick = { onTriggerTest("Dodge Roll", "BUTTON_B", TouchGestureType.SWIPE) }
                    )
                    TestTriggerChip(
                        label = "LT (Hold)",
                        color = ElectricViolet,
                        testTag = "overlay_hud_test_action_lt",
                        onClick = { onTriggerTest("ADS Aim", "LT", TouchGestureType.HOLD) }
                    )
                    TestTriggerChip(
                        label = "RT (Fire)",
                        color = CyberCyan,
                        testTag = "overlay_hud_test_action_rt",
                        onClick = { onTriggerTest("Shoot", "RT", TouchGestureType.TAP) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GamepadSchematicView(
    controllerState: ControllerInputState,
    recentTriggers: List<TriggeredActionItem>
) {
    val latest = recentTriggers.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D121F))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        // Shoulder Triggers LT / RT & Bumpers LB / RB
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Trigger & Bumper
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LT",
                        color = if (controllerState.leftTrigger > 0.15f) ElectricViolet else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = controllerState.leftTrigger.coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(ElectricViolet)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                HudButtonIndicator(
                    label = "LB",
                    isPressed = controllerState.pressedButtons.contains("LB"),
                    color = ElectricViolet,
                    modifier = Modifier.width(60.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Trigger & Bumper
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = controllerState.rightTrigger.coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(CyberCyan)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RT",
                        color = if (controllerState.rightTrigger > 0.15f) CyberCyan else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                HudButtonIndicator(
                    label = "RB",
                    isPressed = controllerState.pressedButtons.contains("RB"),
                    color = CyberCyan,
                    modifier = Modifier.width(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Controller Face: D-Pad, Left Stick, Right Stick, Face Buttons (A, B, X, Y)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Stick & D-Pad
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MiniStickVisualizer(
                    x = controllerState.leftStickX,
                    y = controllerState.leftStickY,
                    isPressed = controllerState.pressedButtons.contains("BUTTON_THUMBL"),
                    label = "LS",
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                MiniDpadVisualizer(controllerState = controllerState)
            }

            // Center Actions / Active Gesture Callout
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudButtonIndicator(
                        label = "SEL",
                        isPressed = controllerState.pressedButtons.contains("SELECT"),
                        color = TextSecondary,
                        modifier = Modifier.size(24.dp, 16.dp)
                    )
                    HudButtonIndicator(
                        label = "STR",
                        isPressed = controllerState.pressedButtons.contains("START"),
                        color = TextSecondary,
                        modifier = Modifier.size(24.dp, 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active gesture callout card
                if (latest != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDarkElevated)
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TRIGGERED GESTURE",
                                color = CyberCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = latest.actionName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "[${latest.buttonKey}] • ${latest.gestureType.name}",
                                color = ElectricViolet,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDarkElevated.copy(alpha = 0.5f))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awaiting input...",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Right Stick & Diamond Face Buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Face Buttons (Y on top, X left, B right, A bottom)
                MiniFaceButtonsDiamond(controllerState = controllerState)
                Spacer(modifier = Modifier.height(6.dp))
                MiniStickVisualizer(
                    x = controllerState.rightStickX,
                    y = controllerState.rightStickY,
                    isPressed = controllerState.pressedButtons.contains("BUTTON_THUMBR"),
                    label = "RS",
                    color = ElectricViolet
                )
            }
        }
    }
}

@Composable
private fun GestureFeedView(
    recentTriggers: List<TriggeredActionItem>,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D121F))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REAL-TIME TRIGGER STREAM",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "CLEAR",
                color = CyberCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onClearHistory() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (recentTriggers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No gestures triggered yet.\nPress controller buttons or use test bar.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(recentTriggers, key = { it.id }) { item ->
                    TriggerFeedItemCard(item)
                }
            }
        }
    }
}

@Composable
private fun TriggerFeedItemCard(item: TriggeredActionItem) {
    val (icon, color) = when (item.gestureType) {
        TouchGestureType.TAP -> Pair(Icons.Default.TouchApp, CyberCyan)
        TouchGestureType.SWIPE -> Pair(Icons.Default.TrendingFlat, NeonOrange)
        TouchGestureType.HOLD -> Pair(Icons.Default.HourglassEmpty, ElectricViolet)
        TouchGestureType.ANALOG_STICK -> Pair(Icons.Default.SportsEsports, NeonGreen)
        TouchGestureType.CAMERA_LOOK -> Pair(Icons.Default.CenterFocusStrong, NeonYellow)
        TouchGestureType.TURBO -> Pair(Icons.Default.Speed, NeonOrange)
        TouchGestureType.SMART_AIM -> Pair(Icons.Default.PlayArrow, CyberCyan)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDarkElevated)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = item.actionName,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (item.extraInfo.isNotBlank()) {
                    Text(
                        text = item.extraInfo,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = item.buttonKey,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun CompactPillView(
    controllerState: ControllerInputState,
    recentTriggers: List<TriggeredActionItem>
) {
    val latest = recentTriggers.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0D121F))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACTIVE BUTTONS:",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (controllerState.pressedButtons.isEmpty()) {
                    Text(
                        text = "None",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        controllerState.pressedButtons.take(4).forEach { btn ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCyan)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = btn.take(4),
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "${controllerState.packetsPerSec} Hz",
                color = NeonGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (latest != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDarkElevated)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LAST GESTURE: ",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${latest.actionName} [${latest.buttonKey}]",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MiniStickVisualizer(
    x: Float,
    y: Float,
    isPressed: Boolean,
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFF151D2E))
            .border(1.5.dp, if (isPressed) Color.White else color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Crosshair
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1f
            )

            // Dynamic Stick Puck
            val maxTravel = radius * 0.7f
            val puckX = center.x + (x * maxTravel).coerceIn(-maxTravel, maxTravel)
            val puckY = center.y + (y * maxTravel).coerceIn(-maxTravel, maxTravel)

            drawCircle(
                color = if (isPressed) Color.White else color,
                radius = 7.dp.toPx(),
                center = Offset(puckX, puckY)
            )
        }

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun MiniDpadVisualizer(controllerState: ControllerInputState) {
    val isUp = controllerState.pressedButtons.contains("DPAD_UP") || controllerState.dpadY < -0.5f
    val isDown = controllerState.pressedButtons.contains("DPAD_DOWN") || controllerState.dpadY > 0.5f
    val isLeft = controllerState.pressedButtons.contains("DPAD_LEFT") || controllerState.dpadX < -0.5f
    val isRight = controllerState.pressedButtons.contains("DPAD_RIGHT") || controllerState.dpadX > 0.5f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(50.dp)
    ) {
        HudDpadButton(isPressed = isUp, label = "▲")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HudDpadButton(isPressed = isLeft, label = "◀")
            HudDpadButton(isPressed = isRight, label = "▶")
        }
        HudDpadButton(isPressed = isDown, label = "▼")
    }
}

@Composable
private fun HudDpadButton(isPressed: Boolean, label: String) {
    val bg = if (isPressed) CyberCyan else Color(0xFF1E293B)
    val text = if (isPressed) Color.Black else TextSecondary
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = text, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MiniFaceButtonsDiamond(controllerState: ControllerInputState) {
    val isA = controllerState.pressedButtons.contains("BUTTON_A")
    val isB = controllerState.pressedButtons.contains("BUTTON_B")
    val isX = controllerState.pressedButtons.contains("BUTTON_X")
    val isY = controllerState.pressedButtons.contains("BUTTON_Y")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(54.dp)
    ) {
        HudFaceButton(label = "Y", isPressed = isY, color = ButtonY)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HudFaceButton(label = "X", isPressed = isX, color = ButtonX)
            HudFaceButton(label = "B", isPressed = isB, color = ButtonB)
        }
        HudFaceButton(label = "A", isPressed = isA, color = ButtonA)
    }
}

@Composable
private fun HudFaceButton(
    label: String,
    isPressed: Boolean,
    color: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.25f else 1.0f,
        animationSpec = spring(),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .size(17.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (isPressed) color else color.copy(alpha = 0.25f))
            .border(1.dp, if (isPressed) Color.White else color.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color.Black else TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun HudButtonIndicator(
    label: String,
    isPressed: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPressed) color else color.copy(alpha = 0.25f))
            .border(1.dp, if (isPressed) Color.White else color.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color.Black else TextPrimary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun TestTriggerChip(
    label: String,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
