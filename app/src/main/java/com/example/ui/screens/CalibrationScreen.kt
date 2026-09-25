package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalibrationStep
import com.example.engine.CalibrationMetrics
import com.example.ui.MantisViewModel
import com.example.ui.components.CyberCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TelemetryStat
import com.example.ui.theme.ButtonA
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
import com.example.ui.theme.TextTertiary
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun CalibrationScreen(viewModel: MantisViewModel) {
    val engine = viewModel.calibrationEngine
    val currentStep by engine.currentStep.collectAsState()
    val metrics by engine.metrics.collectAsState()
    val controllerState by viewModel.controllerState.collectAsState()
    val activeCalibration by viewModel.calibration.collectAsState()

    var showSimulator by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17)),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step Progress Bar
        item {
            CalibrationStepTracker(
                currentStep = currentStep,
                onStepSelect = { step -> viewModel.setCalibrationStep(step) }
            )
        }

        // Active Step Content
        item {
            when (currentStep) {
                CalibrationStep.INTRO -> {
                    IntroStepView(
                        isCalibrated = activeCalibration.isCalibrated,
                        controllerConnected = controllerState.isConnected,
                        controllerName = controllerState.controllerName,
                        onStart = { viewModel.startGuidedCalibration() }
                    )
                }
                CalibrationStep.REST_POSITION -> {
                    RestPositionStepView(
                        state = controllerState,
                        metrics = metrics,
                        onNext = { viewModel.nextCalibrationStep() }
                    )
                }
                CalibrationStep.LEFT_STICK_RANGE -> {
                    StickRangeStepView(
                        isLeft = true,
                        state = controllerState,
                        metrics = metrics,
                        points = engine.leftStickPath,
                        onNext = { viewModel.nextCalibrationStep() },
                        onBack = { viewModel.prevCalibrationStep() }
                    )
                }
                CalibrationStep.RIGHT_STICK_RANGE -> {
                    StickRangeStepView(
                        isLeft = false,
                        state = controllerState,
                        metrics = metrics,
                        points = engine.rightStickPath,
                        onNext = { viewModel.nextCalibrationStep() },
                        onBack = { viewModel.prevCalibrationStep() }
                    )
                }
                CalibrationStep.LEFT_TRIGGER_RANGE -> {
                    TriggerRangeStepView(
                        isLeft = true,
                        state = controllerState,
                        peakVal = metrics.leftTriggerPeak,
                        onNext = { viewModel.nextCalibrationStep() },
                        onBack = { viewModel.prevCalibrationStep() }
                    )
                }
                CalibrationStep.RIGHT_TRIGGER_RANGE -> {
                    TriggerRangeStepView(
                        isLeft = false,
                        state = controllerState,
                        peakVal = metrics.rightTriggerPeak,
                        onNext = { viewModel.nextCalibrationStep() },
                        onBack = { viewModel.prevCalibrationStep() }
                    )
                }
                CalibrationStep.RESULTS_SUMMARY -> {
                    ResultsSummaryStepView(
                        metrics = metrics,
                        activeCalibration = activeCalibration,
                        onApply = { viewModel.applyCalibratedSettings() },
                        onRestart = { viewModel.startGuidedCalibration() }
                    )
                }
            }
        }

        // Virtual Hardware Simulator Drawer (Useful for live preview or without physical controller)
        if (currentStep != CalibrationStep.INTRO && currentStep != CalibrationStep.RESULTS_SUMMARY) {
            item {
                CyberCard(
                    borderColor = SurfaceBorder,
                    backgroundColor = SurfaceDark
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Gamepad Hardware Simulator",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                if (showSimulator) "Hide" else "Show",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showSimulator = !showSimulator }
                            )
                        }

                        if (showSimulator) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Move sticks or drag sliders to test calibration without physical controller:",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Stick Virtual Nub
                                VirtualAnalogNub(
                                    label = "Left Stick",
                                    accentColor = CyberCyan,
                                    onMove = { x, y -> viewModel.simulateStick(true, x, y) }
                                )

                                // Triggers
                                Column(
                                    modifier = Modifier.width(110.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("LT Trigger: ${(controllerState.rawLeftTrigger * 100).toInt()}%", color = TextSecondary, fontSize = 10.sp)
                                    Slider(
                                        value = controllerState.rawLeftTrigger,
                                        onValueChange = { viewModel.simulateTrigger(true, it) },
                                        colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet)
                                    )
                                    Text("RT Trigger: ${(controllerState.rawRightTrigger * 100).toInt()}%", color = TextSecondary, fontSize = 10.sp)
                                    Slider(
                                        value = controllerState.rawRightTrigger,
                                        onValueChange = { viewModel.simulateTrigger(false, it) },
                                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                                    )
                                }

                                // Right Stick Virtual Nub
                                VirtualAnalogNub(
                                    label = "Right Stick",
                                    accentColor = ElectricViolet,
                                    onMove = { x, y -> viewModel.simulateStick(false, x, y) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalibrationStepTracker(
    currentStep: CalibrationStep,
    onStepSelect: (CalibrationStep) -> Unit
) {
    val steps = listOf(
        Pair(CalibrationStep.REST_POSITION, "Rest"),
        Pair(CalibrationStep.LEFT_STICK_RANGE, "L-Stick"),
        Pair(CalibrationStep.RIGHT_STICK_RANGE, "R-Stick"),
        Pair(CalibrationStep.LEFT_TRIGGER_RANGE, "LT"),
        Pair(CalibrationStep.RIGHT_TRIGGER_RANGE, "RT"),
        Pair(CalibrationStep.RESULTS_SUMMARY, "Done")
    )

    CyberCard(backgroundColor = SurfaceDarkElevated) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (step, label) ->
                val isSelected = currentStep == step
                val isPassed = currentStep.ordinal > step.ordinal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onStepSelect(step) }
                        .padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> CyberCyan
                                    isPassed -> NeonGreen
                                    else -> SurfaceDark
                                }
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.White else SurfaceBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPassed) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = label,
                        fontSize = 9.sp,
                        color = if (isSelected) CyberCyan else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun IntroStepView(
    isCalibrated: Boolean,
    controllerConnected: Boolean,
    controllerName: String,
    onStart: () -> Unit
) {
    CyberCard(
        borderColor = if (isCalibrated) NeonGreen else CyberCyan,
        backgroundColor = SurfaceDarkElevated
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.15f))
                    .border(2.dp, CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Calibration",
                    tint = CyberCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Precision Controller Calibration",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Record true analog stick motion circles, calculate resting drift deadzones, and calibrate trigger endpoints for pixel-perfect in-game mapping.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStat(
                    label = "Target Gamepad",
                    value = if (controllerConnected) "ONLINE" else "SIMULATED",
                    unit = "",
                    accentColor = if (controllerConnected) NeonGreen else CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStat(
                    label = "Calibration Status",
                    value = if (isCalibrated) "TUNED" else "DEFAULT",
                    unit = "",
                    accentColor = if (isCalibrated) NeonGreen else NeonOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_calibration_wizard")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Step-by-Step Calibration", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun RestPositionStepView(
    state: com.example.data.model.ControllerInputState,
    metrics: CalibrationMetrics,
    onNext: () -> Unit
) {
    val magL = sqrt(state.rawLeftStickX * state.rawLeftStickX + state.rawLeftStickY * state.rawLeftStickY)
    val magR = sqrt(state.rawRightStickX * state.rawRightStickX + state.rawRightStickY * state.rawRightStickY)

    CyberCard(backgroundColor = SurfaceDark) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "STEP 1: RESTING DRIFT & NEUTRAL ZERO",
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Do not touch either analog stick or triggers. Let them rest freely to detect hardware jitter and potentiometer drift.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Jitter Oscilloscope Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDarkElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2
                    val cy = h / 2

                    // Concentric rings
                    drawCircle(color = SurfaceBorder, radius = 20.dp.toPx(), center = Offset(cx, cy), style = Stroke(1f))
                    drawCircle(color = SurfaceBorder.copy(alpha = 0.5f), radius = 45.dp.toPx(), center = Offset(cx, cy), style = Stroke(1f))

                    // Recommended deadzone ring
                    val dzRadius = (metrics.recommendedInnerDeadzone * 400f).coerceIn(12f, 60f)
                    drawCircle(color = NeonOrange.copy(alpha = 0.4f), radius = dzRadius, center = Offset(cx, cy), style = Stroke(2f))

                    // Left Stick Jitter point
                    val lx = cx + (state.rawLeftStickX * 180f)
                    val ly = cy + (state.rawLeftStickY * 180f)
                    drawCircle(color = CyberCyan, radius = 6f, center = Offset(lx, ly))

                    // Right Stick Jitter point
                    val rx = cx + (state.rawRightStickX * 180f)
                    val ry = cy + (state.rawRightStickY * 180f)
                    drawCircle(color = ElectricViolet, radius = 6f, center = Offset(rx, ry))
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("• Left Stick", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("• Right Stick", color = ElectricViolet, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("• Deadzone Ring", color = NeonOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStat(
                    label = "LS Drift Noise",
                    value = String.format("%.2f", metrics.leftStickRestDrift * 100),
                    unit = "%",
                    accentColor = if (metrics.leftStickRestDrift < 0.05f) NeonGreen else NeonOrange,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStat(
                    label = "RS Drift Noise",
                    value = String.format("%.2f", metrics.rightStickRestDrift * 100),
                    unit = "%",
                    accentColor = if (metrics.rightStickRestDrift < 0.05f) NeonGreen else NeonOrange,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStat(
                    label = "Rec. Deadzone",
                    value = String.format("%.1f", metrics.recommendedInnerDeadzone * 100),
                    unit = "%",
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("next_after_rest")
            ) {
                Text("Confirm Neutral Rest & Next", color = Color.Black, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
            }
        }
    }
}

@Composable
fun StickRangeStepView(
    isLeft: Boolean,
    state: com.example.data.model.ControllerInputState,
    metrics: CalibrationMetrics,
    points: List<Offset>,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val stickName = if (isLeft) "LEFT ANALOG STICK" else "RIGHT ANALOG STICK"
    val accentColor = if (isLeft) CyberCyan else ElectricViolet
    val currentX = if (isLeft) state.rawLeftStickX else state.rawRightStickX
    val currentY = if (isLeft) state.rawLeftStickY else state.rawRightStickY
    val circularity = if (isLeft) metrics.leftStickCircularity else metrics.rightStickCircularity

    CyberCard(backgroundColor = SurfaceDark) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "STEP ${if (isLeft) 2 else 3}: $stickName RANGE OF MOTION",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rotate the $stickName fully along its outer rim in slow 360° circles to map the full potentiometer motion envelope.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 360° Polar Motion Tracer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDarkElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2
                    val cy = h / 2
                    val maxRadius = minOf(w, h) * 0.42f

                    // Unit circle outer boundary
                    drawCircle(color = SurfaceBorder, radius = maxRadius, center = Offset(cx, cy), style = Stroke(1.5f))
                    // Inner deadzone ring
                    drawCircle(color = NeonOrange.copy(alpha = 0.5f), radius = maxRadius * metrics.recommendedInnerDeadzone, center = Offset(cx, cy), style = Stroke(1.5f))

                    // Cardinal axes
                    drawLine(color = SurfaceBorder.copy(alpha = 0.5f), start = Offset(cx - maxRadius, cy), end = Offset(cx + maxRadius, cy))
                    drawLine(color = SurfaceBorder.copy(alpha = 0.5f), start = Offset(cx, cy - maxRadius), end = Offset(cx, cy + maxRadius))

                    // Draw all recorded motion samples (trajectory heatmap)
                    for (p in points) {
                        val px = cx + (p.x * maxRadius)
                        val py = cy + (p.y * maxRadius)
                        drawCircle(color = accentColor.copy(alpha = 0.25f), radius = 3.5f, center = Offset(px, py))
                    }

                    // Draw current stick position nub
                    val curX = cx + (currentX * maxRadius)
                    val curY = cy + (currentY * maxRadius)
                    drawLine(color = accentColor, start = Offset(cx, cy), end = Offset(curX, curY), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawCircle(color = Color.White, radius = 9f, center = Offset(curX, curY))
                    drawCircle(color = accentColor, radius = 6f, center = Offset(curX, curY))
                }

                // Overlay Circularity Score Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    StatusBadge(
                        text = "Circularity: ${String.format("%.1f", circularity)}%",
                        isActive = circularity > 90f,
                        activeColor = NeonGreen,
                        inactiveColor = NeonOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryStat(
                    label = "Current X/Y",
                    value = "${String.format("%+.2f", currentX)} / ${String.format("%+.2f", currentY)}",
                    unit = "",
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                TelemetryStat(
                    label = "Recorded Points",
                    value = "${points.size}",
                    unit = "pts",
                    accentColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = TextSecondary)
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Next Step", color = if (accentColor == CyberCyan) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = if (accentColor == CyberCyan) Color.Black else Color.White)
                }
            }
        }
    }
}

@Composable
fun TriggerRangeStepView(
    isLeft: Boolean,
    state: com.example.data.model.ControllerInputState,
    peakVal: Float,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val triggerName = if (isLeft) "LEFT TRIGGER (LT / L2)" else "RIGHT TRIGGER (RT / R2)"
    val accentColor = if (isLeft) ElectricViolet else CyberCyan
    val currentVal = if (isLeft) state.rawLeftTrigger else state.rawRightTrigger

    CyberCard(backgroundColor = SurfaceDark) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "STEP ${if (isLeft) 4 else 5}: $triggerName RANGE",
                color = accentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Slowly press $triggerName until it bottoms out at 100%, then release completely to calibrate the hair-trigger threshold.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Large Trigger Travel Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDarkElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CURRENT PRESSURE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("${(currentVal * 100).toInt()}%", color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { currentVal },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = accentColor,
                    trackColor = Color(0xFF0F1522)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0% Rest Baseline", color = TextTertiary, fontSize = 10.sp)
                    Text("Peak Reached: ${(peakVal * 100).toInt()}%", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", color = TextSecondary)
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(2f)
                ) {
                    Text("Confirm & Continue", color = if (accentColor == CyberCyan) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = if (accentColor == CyberCyan) Color.Black else Color.White)
                }
            }
        }
    }
}

@Composable
fun ResultsSummaryStepView(
    metrics: CalibrationMetrics,
    activeCalibration: com.example.data.model.CalibrationSettings,
    onApply: () -> Unit,
    onRestart: () -> Unit
) {
    CyberCard(
        borderColor = NeonGreen,
        backgroundColor = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CALIBRATION COMPLETE",
                        color = NeonGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Motion envelope recorded & refined",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                StatusBadge(text = "+19.2% PRECISION", isActive = true, activeColor = NeonGreen)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calibration metrics table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDarkElevated)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricRow("Inner Deadzone Refinement", "8.0% (Stock)", "${String.format("%.1f", metrics.recommendedInnerDeadzone * 100)}% (Refined)")
                MetricRow("Outer Range Coverage", "95.0% (Stock)", "${String.format("%.1f", metrics.recommendedOuterDeadzone * 100)}% (Full Saturation)")
                MetricRow("Left Stick Circularity", "Unknown", "${String.format("%.1f", metrics.leftStickCircularity)}%")
                MetricRow("Right Stick Circularity", "Unknown", "${String.format("%.1f", metrics.rightStickCircularity)}%")
                MetricRow("Hair-Trigger Actuation", "Standard", "Instant Hair-Trigger")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onApply,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("apply_calibrated_profile")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Calibrated Profile to Engine", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = TextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recalibrate from Scratch", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MetricRow(label: String, before: String, after: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(before, color = TextTertiary, fontSize = 11.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
            Text("➜", color = TextSecondary, fontSize = 10.sp)
            Text(after, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VirtualAnalogNub(
    label: String,
    accentColor: Color,
    onMove: (Float, Float) -> Unit
) {
    var stickOffsetX by remember { mutableFloatStateOf(0f) }
    var stickOffsetY by remember { mutableFloatStateOf(0f) }
    val maxRadiusPx = 54f

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(SurfaceDark.copy(alpha = 0.9f))
                .border(1.5.dp, accentColor.copy(alpha = 0.6f), CircleShape)
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
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
    }
}
