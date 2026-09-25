package com.example.engine

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import com.example.data.model.ControllerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DetectedGamepad(
    val id: Int,
    val name: String,
    val descriptor: String,
    val type: ControllerType,
    val vendorId: Int,
    val productId: Int,
    val hasVibrator: Boolean
)

class GamepadDetector(private val context: Context) {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager
    private val _connectedGamepad = MutableStateFlow<DetectedGamepad?>(null)
    val connectedGamepad: StateFlow<DetectedGamepad?> = _connectedGamepad.asStateFlow()

    private val deviceListener = object : InputManager.InputDeviceListener {
        override fun onInputDeviceAdded(deviceId: Int) {
            refreshControllers()
        }

        override fun onInputDeviceRemoved(deviceId: Int) {
            refreshControllers()
        }

        override fun onInputDeviceChanged(deviceId: Int) {
            refreshControllers()
        }
    }

    fun startListening() {
        inputManager?.registerInputDeviceListener(deviceListener, null)
        refreshControllers()
    }

    fun stopListening() {
        inputManager?.unregisterInputDeviceListener(deviceListener)
    }

    fun refreshControllers() {
        val deviceIds = InputDevice.getDeviceIds()
        var foundGamepad: DetectedGamepad? = null

        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources

            val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK

            if (isGamepad && !device.isVirtual) {
                val name = device.name.orEmpty()
                val type = classifyController(name, device.vendorId, device.productId)
                foundGamepad = DetectedGamepad(
                    id = id,
                    name = if (name.isBlank()) "Bluetooth Controller" else name,
                    descriptor = device.descriptor.orEmpty(),
                    type = type,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    hasVibrator = device.vibrator?.hasVibrator() ?: false
                )
                break
            }
        }

        _connectedGamepad.value = foundGamepad
    }

    private fun classifyController(name: String, vendorId: Int, productId: Int): ControllerType {
        val lower = name.lowercase()
        return when {
            lower.contains("xbox") || vendorId == 0x045e -> ControllerType.XBOX
            lower.contains("wireless controller") || lower.contains("dualsense") || lower.contains("dualshock") || vendorId == 0x054c -> ControllerType.PLAYSTATION
            lower.contains("switch") || lower.contains("pro controller") || vendorId == 0x057e -> ControllerType.SWITCH_PRO
            else -> ControllerType.GENERIC
        }
    }
}
