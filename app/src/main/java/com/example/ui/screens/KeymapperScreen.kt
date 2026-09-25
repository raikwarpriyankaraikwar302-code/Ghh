package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.data.model.KeyMapping
import com.example.data.model.TouchGestureType
import com.example.ui.MantisViewModel
import com.example.ui.components.GamepadKeyChip
import com.example.ui.components.StatusBadge
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
import com.example.ui.theme.TextTertiary
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeymapperScreen(
    viewModel: MantisViewModel,
    onNavigateToTestArena: (GameProfile) -> Unit
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val mappings by viewModel.mappings.collectAsState()
    val controllerState by viewModel.controllerState.collectAsState()

    var activeEditingMapping by remember { mutableStateOf<KeyMapping?>(null) }
    var showAddKeyDialog by remember { mutableStateOf(false) }
    var selectedHudStyle by remember { mutableStateOf("FPS Arena") }
    var showProfileDropdown by remember { mutableStateOf(false) }

    val hudStyles = listOf("FPS Arena", "Battle Royale", "Racing", "Clean Grid")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A10))
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile selector
            Box {
                Button(
                    onClick = { showProfileDropdown = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkElevated),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = selectedProfile?.title ?: "Select Game",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = showProfileDropdown,
                    onDismissRequest = { showProfileDropdown = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    profiles.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile.title, color = TextPrimary) },
                            onClick = {
                                viewModel.selectProfile(profile)
                                showProfileDropdown = false
                            }
                        )
                    }
                }
            }

            // Right Action Controls
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { showAddKeyDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_key_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Key", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Mapping", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                IconButton(onClick = { viewModel.resetLayoutToDefault() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Layout", tint = TextSecondary)
                }

                selectedProfile?.let { profile ->
                    IconButton(
                        onClick = { onNavigateToTestArena(profile) },
                        modifier = Modifier.testTag("test_arena_shortcut")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Test in Arena", tint = NeonGreen)
                    }
                }
            }
        }

        // Mapping Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .testTag("keymapper_canvas")
        ) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            // Background simulation layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Cyber grid lines
                val step = 48.dp.toPx()
                var x = 0f
                while (x < w) {
                    drawLine(
                        color = Color(0xFF131D2D).copy(alpha = 0.5f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f
                    )
                    x += step
                }
                var y = 0f
                while (y < h) {
                    drawLine(
                        color = Color(0xFF131D2D).copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                    y += step
                }

                // In-Game HUD overlay backdrop
                when (selectedHudStyle) {
                    "FPS Arena" -> {
                        val cx = w / 2
                        val cy = h / 2
                        drawCircle(color = CyberCyan.copy(alpha = 0.4f), radius = 16.dp.toPx(), center = Offset(cx, cy), style = Stroke(2f))
                        drawLine(color = CyberCyan.copy(alpha = 0.6f), start = Offset(cx - 24.dp.toPx(), cy), end = Offset(cx + 24.dp.toPx(), cy), strokeWidth = 2f)
                        drawLine(color = CyberCyan.copy(alpha = 0.6f), start = Offset(cx, cy - 24.dp.toPx()), end = Offset(cx, cy + 24.dp.toPx()), strokeWidth = 2f)

                        drawRect(color = Color(0x3310B981), topLeft = Offset(32f, h - 80f), size = androidx.compose.ui.geometry.Size(200f, 16f))
                        drawRect(color = NeonGreen.copy(alpha = 0.7f), topLeft = Offset(32f, h - 80f), size = androidx.compose.ui.geometry.Size(160f, 16f))
                    }
                    "Racing" -> {
                        drawCircle(
                            color = NeonOrange.copy(alpha = 0.3f),
                            radius = 60.dp.toPx(),
                            center = Offset(w / 2, h - 80.dp.toPx()),
                            style = Stroke(4f)
                        )
                    }
                    else -> {}
                }

                // Draw swipe arrows for SWIPE mappings
                mappings.forEach { mapping ->
                    if (mapping.gestureType == TouchGestureType.SWIPE && mapping.isEnabled) {
                        val startX = mapping.touchXPercent * w
                        val startY = mapping.touchYPercent * h
                        val rad = Math.toRadians(mapping.swipeAngleDeg.toDouble())
                        val lengthPx = mapping.swipeDistanceDp * density.density
                        val endX = startX + (cos(rad) * lengthPx).toFloat()
                        val endY = startY + (sin(rad) * lengthPx).toFloat()

                        // Direction line
                        drawLine(
                            color = NeonOrange.copy(alpha = 0.85f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Arrow head
                        val arrowAngle = Math.toRadians(30.0)
                        val arrowLen = 10.dp.toPx()
                        val leftX = endX - (cos(rad - arrowAngle) * arrowLen).toFloat()
                        val leftY = endY - (sin(rad - arrowAngle) * arrowLen).toFloat()
                        val rightX = endX - (cos(rad + arrowAngle) * arrowLen).toFloat()
                        val rightY = endY - (sin(rad + arrowAngle) * arrowLen).toFloat()

                        drawLine(color = NeonOrange, start = Offset(endX, endY), end = Offset(leftX, leftY), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                        drawLine(color = NeonOrange, start = Offset(endX, endY), end = Offset(rightX, rightY), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                    }
                }
            }

            // Draggable Touch Nodes
            mappings.forEach { mapping ->
                val isPressed = when (mapping.buttonKey) {
                    "LEFT_STICK" -> (controllerState.leftStickX != 0f || controllerState.leftStickY != 0f)
                    "RIGHT_STICK" -> (controllerState.rightStickX != 0f || controllerState.rightStickY != 0f)
                    "LT" -> (controllerState.leftTrigger > 0.2f || controllerState.pressedButtons.contains("LT"))
                    "RT" -> (controllerState.rightTrigger > 0.2f || controllerState.pressedButtons.contains("RT"))
                    "DPAD_UP" -> (controllerState.pressedButtons.contains("DPAD_UP") || controllerState.dpadY < -0.5f)
                    "DPAD_DOWN" -> (controllerState.pressedButtons.contains("DPAD_DOWN") || controllerState.dpadY > 0.5f)
                    "DPAD_LEFT" -> (controllerState.pressedButtons.contains("DPAD_LEFT") || controllerState.dpadX < -0.5f)
                    "DPAD_RIGHT" -> (controllerState.pressedButtons.contains("DPAD_RIGHT") || controllerState.dpadX > 0.5f)
                    else -> controllerState.pressedButtons.contains(mapping.buttonKey)
                }

                val nodeRadiusPx = with(density) { mapping.radiusDp.dp.toPx() }
                val nodeDiameterDp = (mapping.radiusDp * 2).dp

                val posX = (mapping.touchXPercent * canvasWidthPx) - nodeRadiusPx
                val posY = (mapping.touchYPercent * canvasHeightPx) - nodeRadiusPx

                val nodeBorderColor = when {
                    isPressed -> Color.White
                    mapping.gestureType == TouchGestureType.SWIPE -> NeonOrange
                    mapping.gestureType == TouchGestureType.HOLD -> ElectricViolet
                    mapping.buttonKey.contains("STICK") -> CyberCyan
                    else -> NeonGreen
                }

                val nodeBgColor = when {
                    isPressed -> nodeBorderColor.copy(alpha = 0.85f)
                    mapping.gestureType == TouchGestureType.SWIPE -> NeonOrange.copy(alpha = 0.25f)
                    mapping.gestureType == TouchGestureType.HOLD -> ElectricViolet.copy(alpha = 0.25f)
                    mapping.buttonKey.contains("STICK") -> CyberCyan.copy(alpha = 0.25f)
                    else -> SurfaceDarkElevated.copy(alpha = 0.8f)
                }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(posX.roundToInt(), posY.roundToInt())
                        }
                        .size(nodeDiameterDp)
                        .clip(CircleShape)
                        .background(nodeBgColor)
                        .border(
                            width = if (isPressed) 3.dp else 1.5.dp,
                            color = nodeBorderColor,
                            shape = CircleShape
                        )
                        .pointerInput(mapping.id) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newXPercent = ((posX + nodeRadiusPx + dragAmount.x) / canvasWidthPx).coerceIn(0.04f, 0.96f)
                                    val newYPercent = ((posY + nodeRadiusPx + dragAmount.y) / canvasHeightPx).coerceIn(0.04f, 0.96f)
                                    viewModel.updateMappingPosition(mapping.id, newXPercent, newYPercent)
                                }
                            )
                        }
                        .testTag("mapping_node_${mapping.buttonKey}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        GamepadKeyChip(
                            buttonKey = mapping.buttonKey,
                            isPressed = isPressed,
                            size = if (mapping.radiusDp > 45f) 30.dp else 22.dp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mapping.displayLabel,
                            color = if (isPressed) Color.White else TextPrimary,
                            fontSize = if (mapping.radiusDp > 45f) 11.sp else 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        // Gesture badge
                        if (mapping.gestureType != TouchGestureType.TAP) {
                            Text(
                                text = mapping.gestureType.name.take(4),
                                color = if (isPressed) Color.Black else CyberCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Tapping gear icon overlay to edit details
                    IconButton(
                        onClick = { activeEditingMapping = mapping },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Edit Key Details",
                            tint = CyberCyan.copy(alpha = 0.9f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // HUD Style Switcher floating at bottom center
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark.copy(alpha = 0.9f))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                hudStyles.forEach { style ->
                    val isSel = selectedHudStyle == style
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) CyberCyan else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { selectedHudStyle = style }
                    ) {
                        Text(
                            text = style,
                            color = if (isSel) Color.Black else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }

    // Modal to configure selected button mapping with Advanced Customization Options
    activeEditingMapping?.let { mapping ->
        AdvancedMappingCustomizationDialog(
            mapping = mapping,
            onDismiss = { activeEditingMapping = null },
            onSave = { updatedMapping ->
                viewModel.saveCustomMapping(updatedMapping)
                activeEditingMapping = null
            },
            onDelete = {
                viewModel.deleteMapping(mapping.id)
                activeEditingMapping = null
            }
        )
    }

    // Dialog to add new button key
    if (showAddKeyDialog) {
        AddNewKeyDialog(
            onDismiss = { showAddKeyDialog = false },
            onAdd = { key, label, gestureType, angle, distance ->
                viewModel.addNewMapping(
                    buttonKey = key,
                    label = label,
                    xPercent = 0.5f,
                    yPercent = 0.5f,
                    gestureType = gestureType,
                    swipeAngleDeg = angle,
                    swipeDistanceDp = distance
                )
                showAddKeyDialog = false
            }
        )
    }
}

@Composable
fun AdvancedMappingCustomizationDialog(
    mapping: KeyMapping,
    onDismiss: () -> Unit,
    onSave: (KeyMapping) -> Unit,
    onDelete: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var buttonKey by remember { mutableStateOf(mapping.buttonKey) }
    var label by remember { mutableStateOf(mapping.displayLabel) }
    var radiusDp by remember { mutableFloatStateOf(mapping.radiusDp) }
    var gestureType by remember { mutableStateOf(mapping.gestureType) }

    // Swipe customization
    var swipeAngleDeg by remember { mutableFloatStateOf(mapping.swipeAngleDeg) }
    var swipeDistanceDp by remember { mutableFloatStateOf(mapping.swipeDistanceDp) }
    var swipeDurationMs by remember { mutableLongStateOf(mapping.swipeDurationMs) }

    // Hold customization
    var holdDurationMs by remember { mutableLongStateOf(mapping.holdDurationMs) }

    // Analog Sensitivity & Deadzone customization
    var sensX by remember { mutableFloatStateOf(mapping.sensitivityX) }
    var sensY by remember { mutableFloatStateOf(mapping.sensitivityY) }
    var deadzone by remember { mutableFloatStateOf(mapping.deadzone) }
    var outerDeadzone by remember { mutableFloatStateOf(mapping.outerDeadzone) }
    var antiDeadzone by remember { mutableFloatStateOf(mapping.antiDeadzone) }
    var responseCurve by remember { mutableStateOf(mapping.responseCurve) }
    var invertAxisX by remember { mutableStateOf(mapping.invertAxisX) }
    var invertAxisY by remember { mutableStateOf(mapping.invertAxisY) }

    val controllerButtons = listOf(
        "BUTTON_A", "BUTTON_B", "BUTTON_X", "BUTTON_Y",
        "RT", "LT", "RB", "LB",
        "LEFT_STICK", "RIGHT_STICK",
        "BUTTON_THUMBL", "BUTTON_THUMBR",
        "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT",
        "START", "SELECT"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Column {
                Text(
                    text = "Mapping Customization",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bound: $buttonKey ➔ $label",
                    color = CyberCyan,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                // Category Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceDarkElevated,
                    contentColor = CyberCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Binding", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Gesture", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Analog/Deadzones", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable tab content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // TAB 0: Key Binding & Label
                    if (selectedTab == 0) {
                        item {
                            OutlinedTextField(
                                value = label,
                                onValueChange = { label = it },
                                label = { Text("Display Label") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            Text("Select Target Gamepad Control:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Action buttons
                            Text("Buttons & Bumpers:", color = TextTertiary, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("BUTTON_A", "BUTTON_B", "BUTTON_X", "BUTTON_Y", "LB", "RB").forEach { k ->
                                    val isSel = buttonKey == k
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) CyberCyan else SurfaceDarkElevated)
                                            .clickable { buttonKey = k }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(k.removePrefix("BUTTON_"), color = if (isSel) Color.Black else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Analog Sticks & Triggers
                            Text("Analog Axes & Triggers:", color = TextTertiary, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("LEFT_STICK", "RIGHT_STICK", "LT", "RT").forEach { k ->
                                    val isSel = buttonKey == k
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) ElectricViolet else SurfaceDarkElevated)
                                            .clickable {
                                                buttonKey = k
                                                if (k == "LEFT_STICK") gestureType = TouchGestureType.ANALOG_STICK
                                                if (k == "RIGHT_STICK") gestureType = TouchGestureType.CAMERA_LOOK
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(k, color = if (isSel) Color.White else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // D-Pad
                            Text("D-Pad Directions:", color = TextTertiary, fontSize = 10.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT").forEach { k ->
                                    val isSel = buttonKey == k
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) NeonGreen else SurfaceDarkElevated)
                                            .clickable { buttonKey = k }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(k.removePrefix("DPAD_"), color = if (isSel) Color.Black else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Text("Touch Node Radius: ${radiusDp.toInt()} dp", color = TextSecondary, fontSize = 11.sp)
                            Slider(
                                value = radiusDp,
                                onValueChange = { radiusDp = it },
                                valueRange = 24f..80f,
                                colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                            )
                        }
                    }

                    // TAB 1: Touch Gesture (Tap, Swipe, Hold, Turbo)
                    if (selectedTab == 1) {
                        item {
                            Text("Select Touch Gesture Type:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    Pair(TouchGestureType.TAP, "Tap - Standard instantaneous touch"),
                                    Pair(TouchGestureType.SWIPE, "Swipe - Directional flick/drag gesture"),
                                    Pair(TouchGestureType.HOLD, "Hold - Continuous down press"),
                                    Pair(TouchGestureType.ANALOG_STICK, "Analog Stick - 2D movement follow"),
                                    Pair(TouchGestureType.CAMERA_LOOK, "Camera Look - Aiming panning"),
                                    Pair(TouchGestureType.TURBO, "Turbo - High-speed auto-repeat")
                                ).forEach { (type, desc) ->
                                    val isSel = gestureType == type
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) CyberCyan.copy(alpha = 0.2f) else SurfaceDarkElevated)
                                            .border(1.dp, if (isSel) CyberCyan else SurfaceBorder, RoundedCornerShape(8.dp))
                                            .clickable { gestureType = type }
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(type.name, color = if (isSel) CyberCyan else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(desc, color = TextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Specific Swipe Controls
                        if (gestureType == TouchGestureType.SWIPE) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceDarkElevated)
                                        .border(1.dp, NeonOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("SWIPE CUSTOMIZATION", color = NeonOrange, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Direction Angle: ${swipeAngleDeg.toInt()}°", color = TextPrimary, fontSize = 11.sp)
                                    Slider(
                                        value = swipeAngleDeg,
                                        onValueChange = { swipeAngleDeg = it },
                                        valueRange = 0f..360f,
                                        colors = SliderDefaults.colors(thumbColor = NeonOrange, activeTrackColor = NeonOrange)
                                    )

                                    // Quick Angle Presets
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(Pair("Right (0°)", 0f), Pair("Down (90°)", 90f), Pair("Left (180°)", 180f), Pair("Up (270°)", 270f)).forEach { (name, deg) ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (swipeAngleDeg == deg) NeonOrange else SurfaceDark)
                                                    .clickable { swipeAngleDeg = deg }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(name, color = if (swipeAngleDeg == deg) Color.Black else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Swipe Distance: ${swipeDistanceDp.toInt()} dp", color = TextPrimary, fontSize = 11.sp)
                                    Slider(
                                        value = swipeDistanceDp,
                                        onValueChange = { swipeDistanceDp = it },
                                        valueRange = 30f..140f,
                                        colors = SliderDefaults.colors(thumbColor = NeonOrange, activeTrackColor = NeonOrange)
                                    )
                                }
                            }
                        }

                        // Specific Hold Controls
                        if (gestureType == TouchGestureType.HOLD) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceDarkElevated)
                                        .border(1.dp, ElectricViolet.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text("HOLD CUSTOMIZATION", color = ElectricViolet, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Hold Duration: ${holdDurationMs} ms", color = TextPrimary, fontSize = 11.sp)
                                    Slider(
                                        value = holdDurationMs.toFloat(),
                                        onValueChange = { holdDurationMs = it.toLong() },
                                        valueRange = 100f..1000f,
                                        colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet)
                                    )
                                }
                            }
                        }
                    }

                    // TAB 2: Analog Axes, Sensitivity & Deadzones
                    if (selectedTab == 2) {
                        item {
                            Text("SENSITIVITY MULTIPLIERS", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text("X-Axis Sensitivity: ${String.format("%.1f", sensX)}x", color = TextPrimary, fontSize = 11.sp)
                            Slider(
                                value = sensX,
                                onValueChange = { sensX = it },
                                valueRange = 0.2f..3.0f,
                                colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                            )

                            Text("Y-Axis Sensitivity: ${String.format("%.1f", sensY)}x", color = TextPrimary, fontSize = 11.sp)
                            Slider(
                                value = sensY,
                                onValueChange = { sensY = it },
                                valueRange = 0.2f..3.0f,
                                colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                            )
                        }

                        item {
                            Text("DEADZONES & THRESHOLDS", color = ElectricViolet, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Inner Deadzone: ${(deadzone * 100).toInt()}%", color = TextPrimary, fontSize = 11.sp)
                            Slider(
                                value = deadzone,
                                onValueChange = { deadzone = it },
                                valueRange = 0.01f..0.30f,
                                colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet)
                            )

                            Text("Outer Deadzone: ${(outerDeadzone * 100).toInt()}%", color = TextPrimary, fontSize = 11.sp)
                            Slider(
                                value = outerDeadzone,
                                onValueChange = { outerDeadzone = it },
                                valueRange = 0.70f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = ElectricViolet, activeTrackColor = ElectricViolet)
                            )

                            Text("Anti-Deadzone Boost: ${(antiDeadzone * 100).toInt()}%", color = TextPrimary, fontSize = 11.sp)
                            Slider(
                                value = antiDeadzone,
                                onValueChange = { antiDeadzone = it },
                                valueRange = 0f..0.20f,
                                colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                            )
                        }

                        item {
                            Text("Axis Inversion:", color = TextSecondary, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invert X-Axis", color = TextPrimary, fontSize = 12.sp)
                                Switch(
                                    checked = invertAxisX,
                                    onCheckedChange = { invertAxisX = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = SurfaceDarkElevated)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Invert Y-Axis", color = TextPrimary, fontSize = 12.sp)
                                Switch(
                                    checked = invertAxisY,
                                    onCheckedChange = { invertAxisY = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = SurfaceDarkElevated)
                                )
                            }
                        }

                        item {
                            Text("Response Curve Profile:", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Linear", "Exponential", "S-Curve", "Aggressive").forEach { curve ->
                                    val isSel = responseCurve == curve
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) CyberCyan else SurfaceDarkElevated)
                                            .clickable { responseCurve = curve }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(curve, color = if (isSel) Color.Black else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = mapping.copy(
                        buttonKey = buttonKey,
                        displayLabel = label,
                        radiusDp = radiusDp,
                        gestureType = gestureType,
                        swipeAngleDeg = swipeAngleDeg,
                        swipeDistanceDp = swipeDistanceDp,
                        swipeDurationMs = swipeDurationMs,
                        holdDurationMs = holdDurationMs,
                        sensitivityX = sensX,
                        sensitivityY = sensY,
                        deadzone = deadzone,
                        outerDeadzone = outerDeadzone,
                        antiDeadzone = antiDeadzone,
                        responseCurve = responseCurve,
                        invertAxisX = invertAxisX,
                        invertAxisY = invertAxisY
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Save Configuration", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = NeonRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    )
}

@Composable
fun AddNewKeyDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, TouchGestureType, Float, Float) -> Unit
) {
    var selectedKey by remember { mutableStateOf("BUTTON_A") }
    var label by remember { mutableStateOf("Action") }
    var gestureType by remember { mutableStateOf(TouchGestureType.TAP) }
    var swipeAngle by remember { mutableFloatStateOf(0f) }
    var swipeDist by remember { mutableFloatStateOf(60f) }

    val keys = listOf(
        "BUTTON_A", "BUTTON_B", "BUTTON_X", "BUTTON_Y",
        "RT", "LT", "RB", "LB",
        "LEFT_STICK", "RIGHT_STICK",
        "BUTTON_THUMBL", "BUTTON_THUMBR",
        "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Add Touch Mapping", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Button Label (e.g. Fire, Jump, Ability)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Touch Gesture Type:", color = TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(TouchGestureType.TAP, TouchGestureType.SWIPE, TouchGestureType.HOLD).forEach { t ->
                        val isSel = gestureType == t
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) CyberCyan else SurfaceDarkElevated)
                                .clickable { gestureType = t }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(t.name, color = if (isSel) Color.Black else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Gamepad Key:", color = TextSecondary, fontSize = 11.sp)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    keys.chunked(4).forEach { rowKeys ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            rowKeys.forEach { k ->
                                val isSel = selectedKey == k
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) CyberCyan else SurfaceDarkElevated)
                                        .clickable { selectedKey = k }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = k.removePrefix("BUTTON_"),
                                        color = if (isSel) Color.Black else TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(selectedKey, label, gestureType, swipeAngle, swipeDist) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Add to Canvas", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
