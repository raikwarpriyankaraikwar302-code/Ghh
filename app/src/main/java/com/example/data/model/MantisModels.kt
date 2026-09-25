package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TouchGestureType {
    TAP,             // Standard touch tap
    SWIPE,           // Directional swipe (up, down, left, right, custom angle)
    HOLD,            // Continuous touch down hold
    ANALOG_STICK,    // 2D Movement stick displacement
    CAMERA_LOOK,     // 2D Camera / aiming panning
    SMART_AIM,       // Skill cast with directional joystick
    TURBO            // High frequency repeated rapid tap
}

enum class ControllerType {
    XBOX,
    PLAYSTATION,
    SWITCH_PRO,
    GENERIC
}

@Entity(tableName = "game_profiles")
data class GameProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val packageName: String,
    val genre: String = "Action",
    val isFavorite: Boolean = false,
    val targetFps: Int = 120,
    val pollingRateHz: Int = 500,
    val bannerColorHex: String = "#00E5FF",
    val lastPlayedTimestamp: Long = System.currentTimeMillis(),
    val totalPlayMinutes: Int = 0
)

@Entity(tableName = "key_mappings")
data class KeyMapping(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val buttonKey: String, // e.g. "BUTTON_A", "BUTTON_B", "RT", "LT", "LEFT_STICK", "RIGHT_STICK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT"
    val displayLabel: String,
    val touchXPercent: Float, // 0.0f .. 1.0f relative to screen width
    val touchYPercent: Float, // 0.0f .. 1.0f relative to screen height
    val radiusDp: Float = 34f,
    val gestureType: TouchGestureType = TouchGestureType.TAP,
    
    // Swipe gesture attributes
    val swipeAngleDeg: Float = 0f,      // 0° = East, 90° = South, 180° = West, 270° = North
    val swipeDistanceDp: Float = 60f,   // Length of the swipe stroke
    val swipeDurationMs: Long = 100L,   // Swipe speed
    
    // Hold gesture attributes
    val holdDurationMs: Long = 300L,
    
    // Analog sensitivity & deadzone customizations
    val sensitivityX: Float = 1.0f,
    val sensitivityY: Float = 1.0f,
    val deadzone: Float = 0.08f,
    val outerDeadzone: Float = 0.95f,
    val responseCurve: String = "Linear", // Linear, Exponential, S-Curve, Aggressive
    val invertAxisX: Boolean = false,
    val invertAxisY: Boolean = false,
    val antiDeadzone: Float = 0.02f,
    
    val turboFrequencyHz: Int = 12,
    val isEnabled: Boolean = true
)

data class CalibrationSettings(
    val innerDeadzone: Float = 0.08f,
    val outerDeadzone: Float = 0.95f,
    val leftStickCenterDrift: Float = 0.02f,
    val rightStickCenterDrift: Float = 0.02f,
    val leftStickMaxRange: Float = 1.0f,
    val rightStickMaxRange: Float = 1.0f,
    val leftTriggerDeadzone: Float = 0.05f,
    val leftTriggerMax: Float = 1.0f,
    val rightTriggerDeadzone: Float = 0.05f,
    val rightTriggerMax: Float = 1.0f,
    val responseCurve: String = "Linear", // Linear, Exponential, S-Curve, Aggressive
    val invertX: Boolean = false,
    val invertY: Boolean = false,
    val antiDeadzone: Float = 0.02f,
    val smoothingFactor: Float = 0.15f,
    val pollingRateHz: Int = 500,
    val isCalibrated: Boolean = false,
    val calibratedTimestamp: Long = 0L
)

data class ControllerInputState(
    val isConnected: Boolean = false,
    val controllerName: String = "No Gamepad Detected",
    val controllerType: ControllerType = ControllerType.GENERIC,
    val rawLeftStickX: Float = 0f,
    val rawLeftStickY: Float = 0f,
    val rawRightStickX: Float = 0f,
    val rawRightStickY: Float = 0f,
    val leftStickX: Float = 0f,
    val leftStickY: Float = 0f,
    val rightStickX: Float = 0f,
    val rightStickY: Float = 0f,
    val rawLeftTrigger: Float = 0f,
    val rawRightTrigger: Float = 0f,
    val leftTrigger: Float = 0f,
    val rightTrigger: Float = 0f,
    val pressedButtons: Set<String> = emptySet(),
    val dpadX: Float = 0f,
    val dpadY: Float = 0f,
    val latencyMs: Float = 1.6f,
    val packetsPerSec: Int = 498,
    val batteryPercent: Int = 90
)

data class ActiveTouchSimulation(
    val pointerId: Int,
    val xPercent: Float,
    val yPercent: Float,
    val sourceKey: String,
    val gestureType: TouchGestureType = TouchGestureType.TAP,
    val isPressed: Boolean,
    val swipeProgress: Float = 0f, // 0.0f .. 1.0f for swipes
    val holdProgress: Float = 0f,  // 0.0f .. 1.0f for holds
    val timestamp: Long = System.currentTimeMillis()
)

enum class CalibrationStep {
    INTRO,
    REST_POSITION,        // Release all controls to measure drift/neutral
    LEFT_STICK_RANGE,     // Rotate 360° to measure circularity & outer reach
    RIGHT_STICK_RANGE,    // Rotate 360° to measure circularity & outer reach
    LEFT_TRIGGER_RANGE,   // Press 0% -> 100% and release
    RIGHT_TRIGGER_RANGE,  // Press 0% -> 100% and release
    RESULTS_SUMMARY       // View metrics, deadzone graphs, and apply
}

data class TriggeredActionItem(
    val id: Long = System.nanoTime(),
    val actionName: String,
    val buttonKey: String,
    val gestureType: TouchGestureType,
    val timestamp: Long = System.currentTimeMillis(),
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.5f,
    val extraInfo: String = ""
)

enum class OverlayHudMode(val label: String) {
    GAMEPAD_SCHEMATIC("Gamepad"),
    GESTURE_FEED("Live Feed"),
    COMPACT_PILL("Compact")
}

data class OverlayHudConfig(
    val isVisible: Boolean = false,
    val mode: OverlayHudMode = OverlayHudMode.GAMEPAD_SCHEMATIC,
    val opacity: Float = 0.92f,
    val isMinimized: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)
