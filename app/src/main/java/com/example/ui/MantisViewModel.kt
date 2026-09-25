package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MantisDatabase
import com.example.data.model.CalibrationSettings
import com.example.data.model.CalibrationStep
import com.example.data.model.GameProfile
import com.example.data.model.KeyMapping
import com.example.data.model.OverlayHudConfig
import com.example.data.model.OverlayHudMode
import com.example.data.model.TouchGestureType
import com.example.data.model.TriggeredActionItem
import com.example.data.repository.MantisRepository
import com.example.engine.ControllerCalibrationEngine
import com.example.engine.ControllerInputManager
import com.example.engine.GamepadDetector
import com.example.service.MantisOverlayService
import com.example.service.MantisTouchAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MantisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MantisDatabase.getInstance(application)
    private val repository = MantisRepository(database.mantisDao())

    val gamepadDetector = GamepadDetector(application)
    val inputManager = ControllerInputManager(application)
    val calibrationEngine = ControllerCalibrationEngine()

    val allProfiles: StateFlow<List<GameProfile>> = repository.allProfiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedProfile = MutableStateFlow<GameProfile?>(null)
    val selectedProfile: StateFlow<GameProfile?> = _selectedProfile.asStateFlow()

    private val _mappings = MutableStateFlow<List<KeyMapping>>(emptyList())
    val mappings: StateFlow<List<KeyMapping>> = _mappings.asStateFlow()

    val controllerState = inputManager.inputState
    val activeTouches = inputManager.activeTouches

    private val _calibration = MutableStateFlow(CalibrationSettings())
    val calibration: StateFlow<CalibrationSettings> = _calibration.asStateFlow()

    val isAccessibilityActive = MantisTouchAccessibilityService.isServiceRunning
    val isOverlayActive = MantisOverlayService.isOverlayActive

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val recentTriggers = inputManager.recentTriggers

    private val _hudConfig = MutableStateFlow(OverlayHudConfig(isVisible = true))
    val hudConfig: StateFlow<OverlayHudConfig> = _hudConfig.asStateFlow()

    fun toggleHudVisibility(visible: Boolean? = null) {
        val next = visible ?: !_hudConfig.value.isVisible
        _hudConfig.value = _hudConfig.value.copy(isVisible = next)
        showNotice(if (next) "Overlay HUD Enabled" else "Overlay HUD Hidden")
    }

    fun setHudMode(mode: OverlayHudMode) {
        _hudConfig.value = _hudConfig.value.copy(mode = mode)
    }

    fun setHudOpacity(opacity: Float) {
        _hudConfig.value = _hudConfig.value.copy(opacity = opacity.coerceIn(0.25f, 1.0f))
    }

    fun setHudMinimized(minimized: Boolean) {
        _hudConfig.value = _hudConfig.value.copy(isMinimized = minimized)
    }

    fun setHudOffset(offsetX: Float, offsetY: Float) {
        _hudConfig.value = _hudConfig.value.copy(offsetX = offsetX, offsetY = offsetY)
    }

    fun clearTriggerHistory() {
        inputManager.clearTriggerHistory()
    }

    fun triggerManualAction(actionName: String, buttonKey: String, gestureType: TouchGestureType) {
        inputManager.recordTrigger(
            actionName = actionName,
            buttonKey = buttonKey,
            gestureType = gestureType,
            extraInfo = "Manual Test"
        )
        inputManager.setSimulatedButton(buttonKey, true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(120)
            inputManager.setSimulatedButton(buttonKey, false)
        }
    }

    init {
        gamepadDetector.startListening()

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        viewModelScope.launch {
            allProfiles.collectLatest { list ->
                if (_selectedProfile.value == null && list.isNotEmpty()) {
                    _selectedProfile.value = list.first()
                }
            }
        }

        viewModelScope.launch {
            _selectedProfile.collectLatest { profile ->
                if (profile != null) {
                    repository.getMappingsForProfile(profile.id).collectLatest { mappingList ->
                        _mappings.value = mappingList
                        inputManager.updateMappings(mappingList)
                    }
                }
            }
        }

        viewModelScope.launch {
            gamepadDetector.connectedGamepad.collectLatest { gamepad ->
                if (gamepad != null) {
                    inputManager.setControllerConnected(true, gamepad.name, gamepad.type)
                } else {
                    inputManager.setControllerConnected(false, "No Gamepad Detected", com.example.data.model.ControllerType.GENERIC)
                }
            }
        }

        // Live stream controller input state into calibration engine
        viewModelScope.launch {
            controllerState.collectLatest { state ->
                calibrationEngine.recordSample(state)
            }
        }
    }

    fun selectProfile(profile: GameProfile) {
        _selectedProfile.value = profile
    }

    fun addGameProfile(title: String, packageName: String, genre: String) {
        viewModelScope.launch {
            val colors = listOf("#00E5FF", "#A855F7", "#F97316", "#10B981", "#EF4444", "#3B82F6")
            val chosenColor = colors.random()
            val newProfile = GameProfile(
                title = title.ifBlank { "Custom Game" },
                packageName = packageName.ifBlank { "com.custom.game" },
                genre = genre,
                bannerColorHex = chosenColor
            )
            val id = repository.saveProfile(newProfile)
            _selectedProfile.value = newProfile.copy(id = id)
            showNotice("Profile '$title' created with default $genre keymap!")
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            repository.deleteProfile(id)
            val currentList = allProfiles.value.filter { it.id != id }
            _selectedProfile.value = currentList.firstOrNull()
            showNotice("Profile deleted")
        }
    }

    fun updateMappingPosition(mappingId: Long, xPercent: Float, yPercent: Float) {
        viewModelScope.launch {
            val current = _mappings.value.find { it.id == mappingId } ?: return@launch
            val updated = current.copy(
                touchXPercent = xPercent.coerceIn(0.02f, 0.98f),
                touchYPercent = yPercent.coerceIn(0.02f, 0.98f)
            )
            repository.saveMapping(updated)
        }
    }

    fun saveCustomMapping(mapping: KeyMapping) {
        viewModelScope.launch {
            repository.saveMapping(mapping)
            showNotice("Mapping updated: ${mapping.displayLabel}")
        }
    }

    fun updateMappingDetails(
        mappingId: Long,
        buttonKey: String,
        label: String,
        radiusDp: Float,
        gestureType: TouchGestureType,
        swipeAngleDeg: Float,
        swipeDistanceDp: Float,
        swipeDurationMs: Long,
        holdDurationMs: Long,
        sensX: Float,
        sensY: Float,
        deadzone: Float,
        outerDeadzone: Float,
        responseCurve: String,
        invertX: Boolean,
        invertY: Boolean,
        antiDeadzone: Float
    ) {
        viewModelScope.launch {
            val current = _mappings.value.find { it.id == mappingId } ?: return@launch
            val updated = current.copy(
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
                responseCurve = responseCurve,
                invertAxisX = invertX,
                invertAxisY = invertY,
                antiDeadzone = antiDeadzone
            )
            repository.saveMapping(updated)
            showNotice("Configured '${label}' (${gestureType.name})")
        }
    }

    fun addNewMapping(
        buttonKey: String,
        label: String,
        xPercent: Float = 0.5f,
        yPercent: Float = 0.5f,
        gestureType: TouchGestureType = TouchGestureType.TAP,
        swipeAngleDeg: Float = 0f,
        swipeDistanceDp: Float = 60f
    ) {
        val profile = _selectedProfile.value ?: return
        viewModelScope.launch {
            val newMapping = KeyMapping(
                profileId = profile.id,
                buttonKey = buttonKey,
                displayLabel = label,
                touchXPercent = xPercent,
                touchYPercent = yPercent,
                radiusDp = if (buttonKey.contains("STICK")) 60f else 36f,
                gestureType = gestureType,
                swipeAngleDeg = swipeAngleDeg,
                swipeDistanceDp = swipeDistanceDp
            )
            repository.saveMapping(newMapping)
            showNotice("Added '$label' ($buttonKey)")
        }
    }

    fun deleteMapping(mappingId: Long) {
        viewModelScope.launch {
            repository.deleteMapping(mappingId)
            showNotice("Button removed from canvas")
        }
    }

    fun resetLayoutToDefault() {
        val profile = _selectedProfile.value ?: return
        viewModelScope.launch {
            repository.resetMappingsToDefault(profile.id, profile.genre)
            showNotice("Layout reset to default for ${profile.genre}")
        }
    }

    // --- Guided Controller Calibration Tool Controls ---

    fun startGuidedCalibration() {
        calibrationEngine.startCalibration()
        showNotice("Calibration Started: Release all sticks & triggers")
    }

    fun nextCalibrationStep() {
        calibrationEngine.nextStep()
    }

    fun prevCalibrationStep() {
        calibrationEngine.prevStep()
    }

    fun setCalibrationStep(step: CalibrationStep) {
        calibrationEngine.setStep(step)
    }

    fun applyCalibratedSettings() {
        val refined = calibrationEngine.buildRefinedCalibrationSettings()
        _calibration.value = refined
        inputManager.calibration = refined

        // Refine existing analog mappings with new calibrated deadzones
        viewModelScope.launch {
            val updatedMappings = _mappings.value.map { m ->
                if (m.gestureType == TouchGestureType.ANALOG_STICK || m.gestureType == TouchGestureType.CAMERA_LOOK) {
                    m.copy(
                        deadzone = refined.innerDeadzone,
                        outerDeadzone = refined.outerDeadzone,
                        antiDeadzone = refined.antiDeadzone
                    )
                } else {
                    m
                }
            }
            for (m in updatedMappings) {
                repository.saveMapping(m)
            }
        }

        inputManager.triggerHaptic(80)
        showNotice("✅ Calibrated Profile Applied! Inner Deadzone: ${(refined.innerDeadzone * 100).toInt()}% • Range: ${(refined.outerDeadzone * 100).toInt()}%")
    }

    fun updateCalibration(settings: CalibrationSettings) {
        _calibration.value = settings
        inputManager.calibration = settings
    }

    fun simulateStick(isLeft: Boolean, x: Float, y: Float) {
        inputManager.setSimulatedStick(isLeft, x, y)
    }

    fun simulateButton(key: String, isPressed: Boolean) {
        inputManager.setSimulatedButton(key, isPressed)
    }

    fun simulateTrigger(isLeft: Boolean, value: Float) {
        inputManager.setSimulatedTrigger(isLeft, value)
    }

    fun triggerVibration() {
        inputManager.triggerHaptic(50)
        showNotice("Testing Rumble Motors...")
    }

    fun toggleOverlay(context: Context) {
        if (!hasOverlayPermission(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            showNotice("Please grant Overlay Permission in Android Settings")
            return
        }

        if (isOverlayActive.value) {
            MantisOverlayService.stop(context)
            showNotice("Mantis Overlay stopped")
        } else {
            MantisOverlayService.start(context)
            showNotice("Mantis Overlay launched on screen")
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        showNotice("Enable 'Mantis Low-Latency Touch Injector' in Accessibility")
    }

    fun clearNotice() {
        _statusMessage.value = null
    }

    private fun showNotice(msg: String) {
        _statusMessage.value = msg
    }

    override fun onCleared() {
        super.onCleared()
        gamepadDetector.stopListening()
    }
}
