package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalibrationSettings
import com.example.ui.MantisViewModel
import com.example.ui.components.CyberCard
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
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun TesterScreen(viewModel: MantisViewModel) {
    val controllerState by viewModel.controllerState.collectAsState()
    val calibration by viewModel.calibration.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17)),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gamepad Status Header
        item {
            CyberCard(
                borderColor = if (controllerState.isConnected) NeonGreen else CyberCyan.copy(alpha = 0.5f),
                backgroundColor = SurfaceDarkElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = controllerState.controllerName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (controllerState.isConnected) "Hardware active • Polling @ ${controllerState.packetsPerSec} Hz" else "Connect gamepad via Bluetooth or USB-OTG",
                            color = if (controllerState.isConnected) NeonGreen else TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.triggerVibration() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("rumble_test_button")
                    ) {
                        Icon(Icons.Default.Vibration, contentDescription = "Test Rumble", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rumble", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Live Joystick Vector Gauges
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StickVisualizerCard(
                    title = "LEFT STICK (LS)",
                    stickX = controllerState.leftStickX,
                    stickY = controllerState.leftStickY,
                    innerDeadzone = calibration.innerDeadzone,
                    outerDeadzone = calibration.outerDeadzone,
                    isThumbClicked = controllerState.pressedButtons.contains("BUTTON_THUMBL"),
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                )

                StickVisualizerCard(
                    title = "RIGHT STICK (RS)",
                    stickX = controllerState.rightStickX,
                    stickY = controllerState.rightStickY,
                    innerDeadzone = calibration.innerDeadzone,
                    outerDeadzone = calibration.outerDeadzone,
                    isThumbClicked = controllerState.pressedButtons.contains("BUTTON_THUMBR"),
                    accentColor = ElectricViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Analog Triggers Progress Gauges
        item {
            CyberCard(backgroundColor = SurfaceDark) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANALOG TRIGGER PRESSURE",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Trigger (LT)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("LT / L2", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${(controllerState.leftTrigger * 100).toInt()}%", color = ElectricViolet, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { controllerState.leftTrigger },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = ElectricViolet,
                                trackColor = SurfaceDarkElevated
                            )
                        }

                        // Right Trigger (RT)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("RT / R2", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${(controllerState.rightTrigger * 100).toInt()}%", color = CyberCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { controllerState.rightTrigger },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = CyberCyan,
                                trackColor = SurfaceDarkElevated
                            )
                        }
                    }
                }
            }
        }

        // Gamepad Buttons Matrix Tester
        item {
            CyberCard(backgroundColor = SurfaceDark) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BUTTON MATRIX REAL-TIME MONITOR",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bumpers & Triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GamepadKeyChip("LT", isPressed = controllerState.pressedButtons.contains("LT") || controllerState.leftTrigger > 0.2f, size = 38.dp)
                        GamepadKeyChip("LB", isPressed = controllerState.pressedButtons.contains("LB"), size = 38.dp)
                        GamepadKeyChip("RB", isPressed = controllerState.pressedButtons.contains("RB"), size = 38.dp)
                        GamepadKeyChip("RT", isPressed = controllerState.pressedButtons.contains("RT") || controllerState.rightTrigger > 0.2f, size = 38.dp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // D-Pad and Face Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // D-Pad
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            GamepadKeyChip("DPAD_UP", isPressed = controllerState.pressedButtons.contains("DPAD_UP"), size = 32.dp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                GamepadKeyChip("DPAD_LEFT", isPressed = controllerState.pressedButtons.contains("DPAD_LEFT"), size = 32.dp)
                                Box(modifier = Modifier.size(32.dp))
                                GamepadKeyChip("DPAD_RIGHT", isPressed = controllerState.pressedButtons.contains("DPAD_RIGHT"), size = 32.dp)
                            }
                            GamepadKeyChip("DPAD_DOWN", isPressed = controllerState.pressedButtons.contains("DPAD_DOWN"), size = 32.dp)
                        }

                        // Center buttons (Select, Start, Sticks)
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GamepadKeyChip("SELECT", isPressed = controllerState.pressedButtons.contains("SELECT"), size = 30.dp)
                                GamepadKeyChip("START", isPressed = controllerState.pressedButtons.contains("START"), size = 30.dp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                GamepadKeyChip("BUTTON_THUMBL", isPressed = controllerState.pressedButtons.contains("BUTTON_THUMBL"), size = 30.dp)
                                GamepadKeyChip("BUTTON_THUMBR", isPressed = controllerState.pressedButtons.contains("BUTTON_THUMBR"), size = 30.dp)
                            }
                        }

                        // Face buttons (X, Y, A, B)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            GamepadKeyChip("BUTTON_Y", isPressed = controllerState.pressedButtons.contains("BUTTON_Y"), size = 32.dp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                GamepadKeyChip("BUTTON_X", isPressed = controllerState.pressedButtons.contains("BUTTON_X"), size = 32.dp)
                                Box(modifier = Modifier.size(32.dp))
                                GamepadKeyChip("BUTTON_B", isPressed = controllerState.pressedButtons.contains("BUTTON_B"), size = 32.dp)
                            }
                            GamepadKeyChip("BUTTON_A", isPressed = controllerState.pressedButtons.contains("BUTTON_A"), size = 32.dp)
                        }
                    }
                }
            }
        }

        // Deadzone & Response Curve Calibration
        item {
            CyberCard(backgroundColor = SurfaceDark) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STICK DEADZONE & RESPONSE CURVE",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Inner Deadzone: ${(calibration.innerDeadzone * 100).toInt()}%", color = TextPrimary, fontSize = 13.sp)
                    Slider(
                        value = calibration.innerDeadzone,
                        onValueChange = { viewModel.updateCalibration(calibration.copy(innerDeadzone = it)) },
                        valueRange = 0.01f..0.30f,
                        colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Outer Deadzone: ${(calibration.outerDeadzone * 100).toInt()}%", color = TextPrimary, fontSize = 13.sp)
                    Slider(
                        value = calibration.outerDeadzone,
                        onValueChange = { viewModel.updateCalibration(calibration.copy(outerDeadzone = it)) },
                        valueRange = 0.70f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Response Curve Profile:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Linear", "Exponential", "S-Curve", "Aggressive").forEach { curve ->
                            val isSel = calibration.responseCurve == curve
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) CyberCyan else SurfaceDarkElevated)
                                    .border(1.dp, if (isSel) Color.White else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = curve,
                                    color = if (isSel) Color.Black else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Invert Y-Axis (Flight / FPS Aim)", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = calibration.invertY,
                            onCheckedChange = { viewModel.updateCalibration(calibration.copy(invertY = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = SurfaceDarkElevated)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Anti-Deadzone Boost (Overcomes in-game stick lag)", color = TextPrimary, fontSize = 13.sp)
                        Text(
                            text = "${(calibration.antiDeadzone * 100).toInt()}%",
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StickVisualizerCard(
    title: String,
    stickX: Float,
    stickY: Float,
    innerDeadzone: Float,
    outerDeadzone: Float,
    isThumbClicked: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier,
        backgroundColor = SurfaceDark
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Canvas drawing the vector circle & deadzones
            Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2, h / 2)
                    val maxRadius = (w / 2) - 8f

                    // Outer boundary
                    drawCircle(color = SurfaceBorder, radius = maxRadius, center = center, style = Stroke(1.5f))

                    // Outer deadzone ring
                    drawCircle(color = SurfaceDarkElevated, radius = maxRadius * outerDeadzone, center = center, style = Stroke(1f))

                    // Inner deadzone ring
                    drawCircle(color = NeonOrange.copy(alpha = 0.5f), radius = maxRadius * innerDeadzone, center = center, style = Stroke(1.5f))

                    // Crosshair guides
                    drawLine(color = SurfaceBorder.copy(alpha = 0.6f), start = Offset(center.x - maxRadius, center.y), end = Offset(center.x + maxRadius, center.y))
                    drawLine(color = SurfaceBorder.copy(alpha = 0.6f), start = Offset(center.x, center.y - maxRadius), end = Offset(center.x, center.y + maxRadius))

                    // Stick vector pointer
                    val pointX = center.x + (stickX * maxRadius)
                    val pointY = center.y + (stickY * maxRadius)

                    // Connecting line
                    drawLine(
                        color = accentColor.copy(alpha = 0.8f),
                        start = center,
                        end = Offset(pointX, pointY),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // Stick nub
                    drawCircle(
                        color = if (isThumbClicked) Color.White else accentColor,
                        radius = if (isThumbClicked) 10f else 8f,
                        center = Offset(pointX, pointY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "X: ${String.format("%+.2f", stickX)}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Y: ${String.format("%+.2f", stickY)}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
