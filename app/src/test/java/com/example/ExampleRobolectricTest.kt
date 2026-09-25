package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CalibrationSettings
import com.example.data.model.ControllerInputState
import com.example.data.model.KeyMapping
import com.example.data.model.TouchGestureType
import com.example.engine.ControllerCalibrationEngine
import com.example.engine.ControllerInputManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mantis Gamepad", appName)
    }

    @Test
    fun `controller input manager initial state`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputManager = ControllerInputManager(context)

        val state = inputManager.inputState.value
        assertFalse(state.isConnected)
        assertEquals(0f, state.leftStickX, 0.001f)
        assertEquals(0f, state.rightStickX, 0.001f)
        assertTrue(state.pressedButtons.isEmpty())
    }

    @Test
    fun `controller input tap simulation updates state and touches`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputManager = ControllerInputManager(context)

        val testMapping = listOf(
            KeyMapping(
                profileId = 1,
                buttonKey = "BUTTON_A",
                displayLabel = "Jump",
                touchXPercent = 0.85f,
                touchYPercent = 0.85f,
                gestureType = TouchGestureType.TAP
            )
        )
        inputManager.updateMappings(testMapping)

        // Simulate pressing button A
        inputManager.setSimulatedButton("BUTTON_A", true)

        val state = inputManager.inputState.value
        assertTrue(state.pressedButtons.contains("BUTTON_A"))

        val activeTouches = inputManager.activeTouches.value
        assertEquals(1, activeTouches.size)
        assertEquals("Jump", activeTouches[0].sourceKey)
        assertEquals(0.85f, activeTouches[0].xPercent, 0.001f)
        assertEquals(0.85f, activeTouches[0].yPercent, 0.001f)

        // Release button A
        inputManager.setSimulatedButton("BUTTON_A", false)
        assertTrue(inputManager.activeTouches.value.isEmpty())
    }

    @Test
    fun `swipe touch input mapping produces directional swipe offset`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputManager = ControllerInputManager(context)

        val swipeMapping = listOf(
            KeyMapping(
                profileId = 1,
                buttonKey = "BUTTON_B",
                displayLabel = "Dodge Roll",
                touchXPercent = 0.50f,
                touchYPercent = 0.50f,
                gestureType = TouchGestureType.SWIPE,
                swipeAngleDeg = 0f, // East / Right
                swipeDistanceDp = 80f
            )
        )
        inputManager.updateMappings(swipeMapping)

        inputManager.setSimulatedButton("BUTTON_B", true)
        val activeTouches = inputManager.activeTouches.value
        assertEquals(1, activeTouches.size)
        assertEquals(TouchGestureType.SWIPE, activeTouches[0].gestureType)
        // With angle 0°, X offset should increase to the right
        assertTrue(activeTouches[0].xPercent > 0.50f)
        assertEquals(0.50f, activeTouches[0].yPercent, 0.001f)
    }

    @Test
    fun `hold touch input mapping preserves hold gesture state`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputManager = ControllerInputManager(context)

        val holdMapping = listOf(
            KeyMapping(
                profileId = 1,
                buttonKey = "LT",
                displayLabel = "ADS Aim",
                touchXPercent = 0.80f,
                touchYPercent = 0.40f,
                gestureType = TouchGestureType.HOLD,
                holdDurationMs = 400L
            )
        )
        inputManager.updateMappings(holdMapping)

        inputManager.setSimulatedTrigger(isLeft = true, value = 0.85f)
        val activeTouches = inputManager.activeTouches.value
        assertEquals(1, activeTouches.size)
        assertEquals(TouchGestureType.HOLD, activeTouches[0].gestureType)
        assertEquals(1.0f, activeTouches[0].holdProgress, 0.001f)
    }

    @Test
    fun `analog axis sensitivity multiplier scales touch displacement`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputManager = ControllerInputManager(context)

        // Mapping with 2.0x look sensitivity
        val lookMapping = listOf(
            KeyMapping(
                profileId = 1,
                buttonKey = "RIGHT_STICK",
                displayLabel = "Camera Look",
                touchXPercent = 0.70f,
                touchYPercent = 0.50f,
                gestureType = TouchGestureType.CAMERA_LOOK,
                sensitivityX = 2.0f,
                sensitivityY = 2.0f,
                deadzone = 0.05f
            )
        )
        inputManager.updateMappings(lookMapping)

        inputManager.setSimulatedStick(isLeft = false, x = 0.6f, y = 0.0f)
        val activeTouches = inputManager.activeTouches.value
        assertEquals(1, activeTouches.size)
        // Check that touch displacement is scaled
        assertTrue(activeTouches[0].xPercent > 0.70f)
    }

    @Test
    fun `deadzone calibration suppresses small stick noise`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val inputManager = ControllerInputManager(context)
        inputManager.calibration = CalibrationSettings(innerDeadzone = 0.10f)

        // Tiny stick noise below deadzone
        inputManager.setSimulatedStick(isLeft = true, x = 0.05f, y = 0.05f)
        assertEquals(0f, inputManager.inputState.value.leftStickX, 0.001f)
        assertEquals(0f, inputManager.inputState.value.leftStickY, 0.001f)

        // Large stick displacement above deadzone
        inputManager.setSimulatedStick(isLeft = true, x = 0.8f, y = 0.0f)
        assertTrue(inputManager.inputState.value.leftStickX > 0.5f)
    }

    @Test
    fun `controller calibration wizard records drift and generates refined settings`() {
        val calEngine = ControllerCalibrationEngine()
        calEngine.startCalibration()

        // Rest step
        calEngine.recordSample(
            ControllerInputState(
                rawLeftStickX = 0.03f,
                rawLeftStickY = 0.02f,
                rawRightStickX = 0.04f,
                rawRightStickY = 0.01f,
                rawLeftTrigger = 0.01f,
                rawRightTrigger = 0.01f
            )
        )

        val metrics = calEngine.metrics.value
        assertTrue(metrics.leftStickRestDrift > 0.02f)
        assertTrue(metrics.recommendedInnerDeadzone > 0.04f)

        // Advance and record stick range
        calEngine.nextStep() // To LEFT_STICK_RANGE
        calEngine.recordSample(
            ControllerInputState(
                rawLeftStickX = 0.98f,
                rawLeftStickY = 0.0f
            )
        )

        val refined = calEngine.buildRefinedCalibrationSettings()
        assertTrue(refined.isCalibrated)
        assertTrue(refined.innerDeadzone > 0.03f)
        assertTrue(refined.outerDeadzone > 0.85f)
    }
}
