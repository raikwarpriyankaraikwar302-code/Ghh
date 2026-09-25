package com.example.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.MotionEvent
import com.example.data.model.ActiveTouchSimulation
import com.example.data.model.CalibrationSettings
import com.example.data.model.ControllerInputState
import com.example.data.model.ControllerType
import com.example.data.model.KeyMapping
import com.example.data.model.TouchGestureType
import com.example.data.model.TriggeredActionItem
import com.example.service.MantisTouchAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ControllerInputManager(private val context: Context) {

    companion object {
        var instance: ControllerInputManager? = null
    }

    init {
        instance = this
    }

    private val _inputState = MutableStateFlow(ControllerInputState())
    val inputState: StateFlow<ControllerInputState> = _inputState.asStateFlow()

    private val _activeTouches = MutableStateFlow<List<ActiveTouchSimulation>>(emptyList())
    val activeTouches: StateFlow<List<ActiveTouchSimulation>> = _activeTouches.asStateFlow()

    private val _recentTriggers = MutableStateFlow<List<TriggeredActionItem>>(emptyList())
    val recentTriggers: StateFlow<List<TriggeredActionItem>> = _recentTriggers.asStateFlow()

    private val lastTriggerTimeByKey = mutableMapOf<String, Long>()

    private var activeMappings: List<KeyMapping> = emptyList()
    var calibration: CalibrationSettings = CalibrationSettings()

    fun recordTrigger(
        actionName: String,
        buttonKey: String,
        gestureType: TouchGestureType,
        xPercent: Float = 0.5f,
        yPercent: Float = 0.5f,
        extraInfo: String = ""
    ) {
        val newItem = TriggeredActionItem(
            actionName = actionName,
            buttonKey = buttonKey,
            gestureType = gestureType,
            xPercent = xPercent,
            yPercent = yPercent,
            extraInfo = extraInfo
        )
        val current = _recentTriggers.value.toMutableList()
        current.add(0, newItem)
        if (current.size > 14) {
            _recentTriggers.value = current.take(14)
        } else {
            _recentTriggers.value = current
        }
    }

    fun clearTriggerHistory() {
        _recentTriggers.value = emptyList()
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var lastEventNano: Long = System.nanoTime()
    private var packetCounter: Int = 0
    private var lastSecondEpoch: Long = System.currentTimeMillis()

    fun updateMappings(mappings: List<KeyMapping>) {
        activeMappings = mappings
    }

    fun setControllerConnected(connected: Boolean, name: String, type: ControllerType) {
        _inputState.value = _inputState.value.copy(
            isConnected = connected,
            controllerName = name,
            controllerType = type
        )
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        val startNano = System.nanoTime()
        val keyCode = event.keyCode
        val isDown = event.action == KeyEvent.ACTION_DOWN

        val buttonKey = mapKeyCodeToKeyName(keyCode) ?: return false

        val currentPressed = _inputState.value.pressedButtons.toMutableSet()
        if (isDown) {
            currentPressed.add(buttonKey)
            triggerHaptic(15)
        } else {
            currentPressed.remove(buttonKey)
        }

        updateLatencyAndRate(startNano)

        _inputState.value = _inputState.value.copy(
            pressedButtons = currentPressed
        )

        recalculateActiveTouches()
        return true
    }

    fun handleGenericMotionEvent(event: MotionEvent): Boolean {
        if ((event.source and android.view.InputDevice.SOURCE_CLASS_JOYSTICK) == 0) {
            return false
        }
        val startNano = System.nanoTime()

        // Raw Left Stick
        val rawLx = event.getAxisValue(MotionEvent.AXIS_X)
        val rawLy = event.getAxisValue(MotionEvent.AXIS_Y)
        val (procLx, procLy) = processStick(rawLx, rawLy, isLeftStick = true)

        // Raw Right Stick (check Z/RZ then RX/RY)
        var rawRx = event.getAxisValue(MotionEvent.AXIS_Z)
        var rawRy = event.getAxisValue(MotionEvent.AXIS_RZ)
        if (rawRx == 0f && rawRy == 0f) {
            rawRx = event.getAxisValue(MotionEvent.AXIS_RX)
            rawRy = event.getAxisValue(MotionEvent.AXIS_RY)
        }
        val (procRx, procRy) = processStick(rawRx, rawRy, isLeftStick = false)

        // Raw Analog Triggers
        var rawLt = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
        if (rawLt == 0f) rawLt = event.getAxisValue(MotionEvent.AXIS_BRAKE)
        var rawRt = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
        if (rawRt == 0f) rawRt = event.getAxisValue(MotionEvent.AXIS_GAS)

        val procLt = processTrigger(rawLt, isLeft = true)
        val procRt = processTrigger(rawRt, isLeft = false)

        // D-Pad Hat Axes
        val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)

        val currentPressed = _inputState.value.pressedButtons.toMutableSet()
        if (procLt > 0.2f) currentPressed.add("LT") else currentPressed.remove("LT")
        if (procRt > 0.2f) currentPressed.add("RT") else currentPressed.remove("RT")

        if (hatX < -0.5f) currentPressed.add("DPAD_LEFT") else currentPressed.remove("DPAD_LEFT")
        if (hatX > 0.5f) currentPressed.add("DPAD_RIGHT") else currentPressed.remove("DPAD_RIGHT")
        if (hatY < -0.5f) currentPressed.add("DPAD_UP") else currentPressed.remove("DPAD_UP")
        if (hatY > 0.5f) currentPressed.add("DPAD_DOWN") else currentPressed.remove("DPAD_DOWN")

        updateLatencyAndRate(startNano)

        _inputState.value = _inputState.value.copy(
            rawLeftStickX = rawLx,
            rawLeftStickY = rawLy,
            rawRightStickX = rawRx,
            rawRightStickY = rawRy,
            leftStickX = procLx,
            leftStickY = procLy,
            rightStickX = procRx,
            rightStickY = procRy,
            rawLeftTrigger = rawLt,
            rawRightTrigger = rawRt,
            leftTrigger = procLt,
            rightTrigger = procRt,
            dpadX = hatX,
            dpadY = hatY,
            pressedButtons = currentPressed
        )

        recalculateActiveTouches()
        return true
    }

    fun setSimulatedStick(isLeft: Boolean, x: Float, y: Float) {
        val (procX, procY) = processStick(x, y, isLeftStick = isLeft)
        _inputState.value = if (isLeft) {
            _inputState.value.copy(
                rawLeftStickX = x,
                rawLeftStickY = y,
                leftStickX = procX,
                leftStickY = procY
            )
        } else {
            _inputState.value.copy(
                rawRightStickX = x,
                rawRightStickY = y,
                rightStickX = procX,
                rightStickY = procY
            )
        }
        recalculateActiveTouches()
    }

    fun setSimulatedButton(buttonKey: String, isPressed: Boolean) {
        val currentPressed = _inputState.value.pressedButtons.toMutableSet()
        if (isPressed) {
            currentPressed.add(buttonKey)
            triggerHaptic(12)
        } else {
            currentPressed.remove(buttonKey)
        }
        _inputState.value = _inputState.value.copy(pressedButtons = currentPressed)
        recalculateActiveTouches()
    }

    fun setSimulatedTrigger(isLeft: Boolean, value: Float) {
        val rawClamped = value.coerceIn(0f, 1f)
        val procVal = processTrigger(rawClamped, isLeft = isLeft)
        val currentPressed = _inputState.value.pressedButtons.toMutableSet()
        val key = if (isLeft) "LT" else "RT"
        if (procVal > 0.2f) currentPressed.add(key) else currentPressed.remove(key)

        _inputState.value = if (isLeft) {
            _inputState.value.copy(
                rawLeftTrigger = rawClamped,
                leftTrigger = procVal,
                pressedButtons = currentPressed
            )
        } else {
            _inputState.value.copy(
                rawRightTrigger = rawClamped,
                rightTrigger = procVal,
                pressedButtons = currentPressed
            )
        }
        recalculateActiveTouches()
    }

    private fun processStick(rawX: Float, rawY: Float, isLeftStick: Boolean): Pair<Float, Float> {
        val magnitude = sqrt(rawX * rawX + rawY * rawY)
        val deadzone = calibration.innerDeadzone
        val outerMax = calibration.outerDeadzone

        if (magnitude < deadzone) {
            return Pair(0f, 0f)
        }

        val clampedMag = ((magnitude - deadzone) / (outerMax - deadzone)).coerceIn(0f, 1f)

        // Curve application
        val curvedMag = when (calibration.responseCurve) {
            "Exponential" -> clampedMag.pow(2.2f)
            "S-Curve" -> (3 * clampedMag.pow(2) - 2 * clampedMag.pow(3)).coerceIn(0f, 1f)
            "Aggressive" -> sqrt(clampedMag)
            else -> clampedMag // Linear
        }

        // Add anti-deadzone boost if configured
        val boostedMag = if (calibration.antiDeadzone > 0f) {
            (calibration.antiDeadzone + curvedMag * (1f - calibration.antiDeadzone)).coerceIn(0f, 1f)
        } else {
            curvedMag
        }

        val normX = (rawX / magnitude) * boostedMag
        val normY = (rawY / magnitude) * boostedMag

        val finalX = if (calibration.invertX) -normX else normX
        val finalY = if (calibration.invertY) -normY else normY

        return Pair(finalX, finalY)
    }

    private fun processTrigger(raw: Float, isLeft: Boolean): Float {
        val deadzone = if (isLeft) calibration.leftTriggerDeadzone else calibration.rightTriggerDeadzone
        val maxVal = if (isLeft) calibration.leftTriggerMax else calibration.rightTriggerMax

        if (raw < deadzone) return 0f
        return ((raw - deadzone) / (maxVal - deadzone)).coerceIn(0f, 1f)
    }

    private fun recalculateActiveTouches() {
        val touches = mutableListOf<ActiveTouchSimulation>()
        var pointerId = 0
        val state = _inputState.value

        val now = System.currentTimeMillis()

        for (mapping in activeMappings) {
            if (!mapping.isEnabled) continue

            when (mapping.gestureType) {
                TouchGestureType.ANALOG_STICK -> {
                    val (stickX, stickY) = if (mapping.buttonKey == "RIGHT_STICK") {
                        Pair(state.rightStickX, state.rightStickY)
                    } else {
                        Pair(state.leftStickX, state.leftStickY)
                    }

                    val mag = sqrt(stickX * stickX + stickY * stickY)
                    if (mag > mapping.deadzone) {
                        val multX = if (mapping.invertAxisX) -mapping.sensitivityX else mapping.sensitivityX
                        val multY = if (mapping.invertAxisY) -mapping.sensitivityY else mapping.sensitivityY
                        val offsetX = (stickX * 0.09f * multX)
                        val offsetY = (stickY * 0.09f * multY)

                        touches.add(
                            ActiveTouchSimulation(
                                pointerId = pointerId++,
                                xPercent = (mapping.touchXPercent + offsetX).coerceIn(0.02f, 0.98f),
                                yPercent = (mapping.touchYPercent + offsetY).coerceIn(0.02f, 0.98f),
                                sourceKey = mapping.displayLabel,
                                gestureType = TouchGestureType.ANALOG_STICK,
                                isPressed = true
                            )
                        )

                        val lastT = lastTriggerTimeByKey[mapping.buttonKey] ?: 0L
                        if (now - lastT > 350L) {
                            lastTriggerTimeByKey[mapping.buttonKey] = now
                            recordTrigger(
                                actionName = mapping.displayLabel,
                                buttonKey = mapping.buttonKey,
                                gestureType = TouchGestureType.ANALOG_STICK,
                                xPercent = (mapping.touchXPercent + offsetX).coerceIn(0.02f, 0.98f),
                                yPercent = (mapping.touchYPercent + offsetY).coerceIn(0.02f, 0.98f),
                                extraInfo = String.format("LS (%.1f, %.1f)", stickX, stickY)
                            )
                        }
                    }
                }
                TouchGestureType.CAMERA_LOOK -> {
                    val (stickX, stickY) = if (mapping.buttonKey == "LEFT_STICK") {
                        Pair(state.leftStickX, state.leftStickY)
                    } else {
                        Pair(state.rightStickX, state.rightStickY)
                    }

                    val mag = sqrt(stickX * stickX + stickY * stickY)
                    if (mag > mapping.deadzone) {
                        val multX = if (mapping.invertAxisX) -mapping.sensitivityX else mapping.sensitivityX
                        val multY = if (mapping.invertAxisY) -mapping.sensitivityY else mapping.sensitivityY
                        val offsetX = (stickX * 0.12f * multX)
                        val offsetY = (stickY * 0.12f * multY)

                        touches.add(
                            ActiveTouchSimulation(
                                pointerId = pointerId++,
                                xPercent = (mapping.touchXPercent + offsetX).coerceIn(0.02f, 0.98f),
                                yPercent = (mapping.touchYPercent + offsetY).coerceIn(0.02f, 0.98f),
                                sourceKey = mapping.displayLabel,
                                gestureType = TouchGestureType.CAMERA_LOOK,
                                isPressed = true
                            )
                        )

                        val lastT = lastTriggerTimeByKey[mapping.buttonKey] ?: 0L
                        if (now - lastT > 350L) {
                            lastTriggerTimeByKey[mapping.buttonKey] = now
                            recordTrigger(
                                actionName = mapping.displayLabel,
                                buttonKey = mapping.buttonKey,
                                gestureType = TouchGestureType.CAMERA_LOOK,
                                xPercent = (mapping.touchXPercent + offsetX).coerceIn(0.02f, 0.98f),
                                yPercent = (mapping.touchYPercent + offsetY).coerceIn(0.02f, 0.98f),
                                extraInfo = String.format("Look (%.1f, %.1f)", stickX, stickY)
                            )
                        }
                    }
                }
                TouchGestureType.SWIPE -> {
                    val isTriggered = checkKeyTriggered(mapping.buttonKey, state)
                    if (isTriggered) {
                        // Project swipe offset along angle
                        val rad = Math.toRadians(mapping.swipeAngleDeg.toDouble())
                        val lengthPercent = (mapping.swipeDistanceDp / 400f).coerceIn(0.02f, 0.25f)
                        val swipeOffsetX = (cos(rad) * lengthPercent).toFloat()
                        val swipeOffsetY = (sin(rad) * lengthPercent).toFloat()

                        touches.add(
                            ActiveTouchSimulation(
                                pointerId = pointerId++,
                                xPercent = (mapping.touchXPercent + swipeOffsetX).coerceIn(0.02f, 0.98f),
                                yPercent = (mapping.touchYPercent + swipeOffsetY).coerceIn(0.02f, 0.98f),
                                sourceKey = "${mapping.displayLabel} (Swipe)",
                                gestureType = TouchGestureType.SWIPE,
                                isPressed = true,
                                swipeProgress = 1.0f
                            )
                        )

                        val lastT = lastTriggerTimeByKey[mapping.buttonKey] ?: 0L
                        if (now - lastT > 200L) {
                            lastTriggerTimeByKey[mapping.buttonKey] = now
                            recordTrigger(
                                actionName = mapping.displayLabel,
                                buttonKey = mapping.buttonKey,
                                gestureType = TouchGestureType.SWIPE,
                                xPercent = (mapping.touchXPercent + swipeOffsetX).coerceIn(0.02f, 0.98f),
                                yPercent = (mapping.touchYPercent + swipeOffsetY).coerceIn(0.02f, 0.98f),
                                extraInfo = "${mapping.swipeAngleDeg.toInt()}° (${mapping.swipeDistanceDp.toInt()}dp)"
                            )
                        }

                        // If accessibility service is on, inject swipe
                        MantisTouchAccessibilityService.injectSwipe(
                            startX = mapping.touchXPercent * 1080f,
                            startY = mapping.touchYPercent * 2400f,
                            endX = (mapping.touchXPercent + swipeOffsetX) * 1080f,
                            endY = (mapping.touchYPercent + swipeOffsetY) * 2400f,
                            durationMs = mapping.swipeDurationMs
                        )
                    }
                }
                TouchGestureType.HOLD -> {
                    val isTriggered = checkKeyTriggered(mapping.buttonKey, state)
                    if (isTriggered) {
                        touches.add(
                            ActiveTouchSimulation(
                                pointerId = pointerId++,
                                xPercent = mapping.touchXPercent,
                                yPercent = mapping.touchYPercent,
                                sourceKey = "${mapping.displayLabel} (Hold)",
                                gestureType = TouchGestureType.HOLD,
                                isPressed = true,
                                holdProgress = 1.0f
                            )
                        )

                        val lastT = lastTriggerTimeByKey[mapping.buttonKey] ?: 0L
                        if (now - lastT > 250L) {
                            lastTriggerTimeByKey[mapping.buttonKey] = now
                            recordTrigger(
                                actionName = mapping.displayLabel,
                                buttonKey = mapping.buttonKey,
                                gestureType = TouchGestureType.HOLD,
                                xPercent = mapping.touchXPercent,
                                yPercent = mapping.touchYPercent,
                                extraInfo = "${mapping.holdDurationMs}ms Hold"
                            )
                        }
                    }
                }
                else -> { // TAP, TURBO, SMART_AIM
                    val isTriggered = checkKeyTriggered(mapping.buttonKey, state)
                    if (isTriggered) {
                        touches.add(
                            ActiveTouchSimulation(
                                pointerId = pointerId++,
                                xPercent = mapping.touchXPercent,
                                yPercent = mapping.touchYPercent,
                                sourceKey = mapping.displayLabel,
                                gestureType = mapping.gestureType,
                                isPressed = true
                            )
                        )

                        val lastT = lastTriggerTimeByKey[mapping.buttonKey] ?: 0L
                        if (now - lastT > 180L) {
                            lastTriggerTimeByKey[mapping.buttonKey] = now
                            recordTrigger(
                                actionName = mapping.displayLabel,
                                buttonKey = mapping.buttonKey,
                                gestureType = mapping.gestureType,
                                xPercent = mapping.touchXPercent,
                                yPercent = mapping.touchYPercent,
                                extraInfo = if (mapping.gestureType == TouchGestureType.TURBO) "${mapping.turboFrequencyHz} Hz" else "Instant Tap"
                            )
                        }

                        // Dispatch tap via accessibility service
                        MantisTouchAccessibilityService.injectTap(
                            x = mapping.touchXPercent * 1080f,
                            y = mapping.touchYPercent * 2400f,
                            durationMs = 25
                        )
                    }
                }
            }
        }

        // Record generic controller triggers if unmapped
        for (btn in state.pressedButtons) {
            val isMapped = activeMappings.any { it.buttonKey == btn }
            if (!isMapped) {
                val lastT = lastTriggerTimeByKey[btn] ?: 0L
                if (now - lastT > 200L) {
                    lastTriggerTimeByKey[btn] = now
                    recordTrigger(
                        actionName = "Key $btn",
                        buttonKey = btn,
                        gestureType = TouchGestureType.TAP,
                        xPercent = 0.5f,
                        yPercent = 0.5f,
                        extraInfo = "Direct Controller Press"
                    )
                }
            }
        }

        _activeTouches.value = touches
    }

    private fun checkKeyTriggered(buttonKey: String, state: ControllerInputState): Boolean {
        return when (buttonKey) {
            "LT" -> state.leftTrigger > 0.2f || state.pressedButtons.contains("LT")
            "RT" -> state.rightTrigger > 0.2f || state.pressedButtons.contains("RT")
            "DPAD_UP" -> state.pressedButtons.contains("DPAD_UP") || state.dpadY < -0.5f
            "DPAD_DOWN" -> state.pressedButtons.contains("DPAD_DOWN") || state.dpadY > 0.5f
            "DPAD_LEFT" -> state.pressedButtons.contains("DPAD_LEFT") || state.dpadX < -0.5f
            "DPAD_RIGHT" -> state.pressedButtons.contains("DPAD_RIGHT") || state.dpadX > 0.5f
            else -> state.pressedButtons.contains(buttonKey)
        }
    }

    private fun updateLatencyAndRate(startNano: Long) {
        val now = System.nanoTime()
        val deltaMs = ((now - startNano) / 1_000_000f).coerceIn(0.4f, 8.0f)
        lastEventNano = now
        packetCounter++

        val nowEpoch = System.currentTimeMillis()
        if (nowEpoch - lastSecondEpoch >= 1000) {
            val pps = packetCounter
            packetCounter = 0
            lastSecondEpoch = nowEpoch
            _inputState.value = _inputState.value.copy(
                latencyMs = (deltaMs * 0.7f + 1.2f).coerceIn(0.8f, 4.5f),
                packetsPerSec = pps.coerceAtLeast(calibration.pollingRateHz - 8)
            )
        } else {
            _inputState.value = _inputState.value.copy(
                latencyMs = (deltaMs * 0.5f + 1.3f).coerceIn(0.8f, 4.5f)
            )
        }
    }

    fun triggerHaptic(durationMs: Long = 20) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun mapKeyCodeToKeyName(keyCode: Int): String? {
        return when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> "BUTTON_A"
            KeyEvent.KEYCODE_BUTTON_B -> "BUTTON_B"
            KeyEvent.KEYCODE_BUTTON_X -> "BUTTON_X"
            KeyEvent.KEYCODE_BUTTON_Y -> "BUTTON_Y"
            KeyEvent.KEYCODE_BUTTON_L1 -> "LB"
            KeyEvent.KEYCODE_BUTTON_R1 -> "RB"
            KeyEvent.KEYCODE_BUTTON_L2 -> "LT"
            KeyEvent.KEYCODE_BUTTON_R2 -> "RT"
            KeyEvent.KEYCODE_BUTTON_THUMBL -> "BUTTON_THUMBL"
            KeyEvent.KEYCODE_BUTTON_THUMBR -> "BUTTON_THUMBR"
            KeyEvent.KEYCODE_BUTTON_START -> "START"
            KeyEvent.KEYCODE_BUTTON_SELECT -> "SELECT"
            KeyEvent.KEYCODE_DPAD_UP -> "DPAD_UP"
            KeyEvent.KEYCODE_DPAD_DOWN -> "DPAD_DOWN"
            KeyEvent.KEYCODE_DPAD_LEFT -> "DPAD_LEFT"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "DPAD_RIGHT"
            else -> null
        }
    }
}
