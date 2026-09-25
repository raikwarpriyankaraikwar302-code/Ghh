package com.example.engine

import androidx.compose.ui.geometry.Offset
import com.example.data.model.CalibrationSettings
import com.example.data.model.CalibrationStep
import com.example.data.model.ControllerInputState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class CalibrationMetrics(
    val leftStickRestDrift: Float = 0.015f,
    val rightStickRestDrift: Float = 0.018f,
    val leftStickMinX: Float = -1.0f,
    val leftStickMaxX: Float = 1.0f,
    val leftStickMinY: Float = -1.0f,
    val leftStickMaxY: Float = 1.0f,
    val leftStickCircularity: Float = 98.2f,
    val rightStickMinX: Float = -1.0f,
    val rightStickMaxX: Float = 1.0f,
    val rightStickMinY: Float = -1.0f,
    val rightStickMaxY: Float = 1.0f,
    val rightStickCircularity: Float = 97.6f,
    val leftTriggerRest: Float = 0.0f,
    val leftTriggerPeak: Float = 1.0f,
    val rightTriggerRest: Float = 0.0f,
    val rightTriggerPeak: Float = 1.0f,
    val recommendedInnerDeadzone: Float = 0.05f,
    val recommendedOuterDeadzone: Float = 0.96f,
    val totalSamplesRecorded: Int = 0
)

class ControllerCalibrationEngine {

    private val _currentStep = MutableStateFlow(CalibrationStep.INTRO)
    val currentStep: StateFlow<CalibrationStep> = _currentStep.asStateFlow()

    private val _metrics = MutableStateFlow(CalibrationMetrics())
    val metrics: StateFlow<CalibrationMetrics> = _metrics.asStateFlow()

    // Recorded motion paths for visual feedback
    val leftStickPath = mutableListOf<Offset>()
    val rightStickPath = mutableListOf<Offset>()

    private val restSamplesL = mutableListOf<Float>()
    private val restSamplesR = mutableListOf<Float>()
    private val restSamplesLT = mutableListOf<Float>()
    private val restSamplesRT = mutableListOf<Float>()

    fun startCalibration() {
        leftStickPath.clear()
        rightStickPath.clear()
        restSamplesL.clear()
        restSamplesR.clear()
        restSamplesLT.clear()
        restSamplesRT.clear()
        _metrics.value = CalibrationMetrics()
        _currentStep.value = CalibrationStep.REST_POSITION
    }

    fun nextStep() {
        _currentStep.value = when (_currentStep.value) {
            CalibrationStep.INTRO -> CalibrationStep.REST_POSITION
            CalibrationStep.REST_POSITION -> CalibrationStep.LEFT_STICK_RANGE
            CalibrationStep.LEFT_STICK_RANGE -> CalibrationStep.RIGHT_STICK_RANGE
            CalibrationStep.RIGHT_STICK_RANGE -> CalibrationStep.LEFT_TRIGGER_RANGE
            CalibrationStep.LEFT_TRIGGER_RANGE -> CalibrationStep.RIGHT_TRIGGER_RANGE
            CalibrationStep.RIGHT_TRIGGER_RANGE -> {
                finalizeMetrics()
                CalibrationStep.RESULTS_SUMMARY
            }
            CalibrationStep.RESULTS_SUMMARY -> CalibrationStep.INTRO
        }
    }

    fun prevStep() {
        _currentStep.value = when (_currentStep.value) {
            CalibrationStep.RESULTS_SUMMARY -> CalibrationStep.RIGHT_TRIGGER_RANGE
            CalibrationStep.RIGHT_TRIGGER_RANGE -> CalibrationStep.LEFT_TRIGGER_RANGE
            CalibrationStep.LEFT_TRIGGER_RANGE -> CalibrationStep.RIGHT_STICK_RANGE
            CalibrationStep.RIGHT_STICK_RANGE -> CalibrationStep.LEFT_STICK_RANGE
            CalibrationStep.LEFT_STICK_RANGE -> CalibrationStep.REST_POSITION
            CalibrationStep.REST_POSITION -> CalibrationStep.INTRO
            CalibrationStep.INTRO -> CalibrationStep.INTRO
        }
    }

    fun setStep(step: CalibrationStep) {
        _currentStep.value = step
    }

    /**
     * Process live incoming controller state to record values for the active step.
     */
    fun recordSample(state: ControllerInputState) {
        val current = _metrics.value

        when (_currentStep.value) {
            CalibrationStep.REST_POSITION -> {
                val magL = sqrt(state.rawLeftStickX * state.rawLeftStickX + state.rawLeftStickY * state.rawLeftStickY)
                val magR = sqrt(state.rawRightStickX * state.rawRightStickX + state.rawRightStickY * state.rawRightStickY)
                restSamplesL.add(magL)
                restSamplesR.add(magR)
                restSamplesLT.add(state.rawLeftTrigger)
                restSamplesRT.add(state.rawRightTrigger)

                val maxL = restSamplesL.maxOrNull() ?: 0.01f
                val maxR = restSamplesR.maxOrNull() ?: 0.01f
                val peakDrift = max(maxL, maxR)
                val recommendedDz = (peakDrift * 1.35f + 0.02f).coerceIn(0.04f, 0.25f)

                _metrics.value = current.copy(
                    leftStickRestDrift = maxL,
                    rightStickRestDrift = maxR,
                    leftTriggerRest = restSamplesLT.maxOrNull() ?: 0f,
                    rightTriggerRest = restSamplesRT.maxOrNull() ?: 0f,
                    recommendedInnerDeadzone = recommendedDz,
                    totalSamplesRecorded = restSamplesL.size
                )
            }

            CalibrationStep.LEFT_STICK_RANGE -> {
                val x = state.rawLeftStickX
                val y = state.rawLeftStickY
                val mag = sqrt(x * x + y * y)

                if (mag > 0.15f) {
                    if (leftStickPath.size < 600) {
                        leftStickPath.add(Offset(x, y))
                    }
                    val newMinX = min(current.leftStickMinX, x)
                    val newMaxX = max(current.leftStickMaxX, x)
                    val newMinY = min(current.leftStickMinY, y)
                    val newMaxY = max(current.leftStickMaxY, y)

                    val circularity = calculateCircularity(leftStickPath)

                    _metrics.value = current.copy(
                        leftStickMinX = newMinX,
                        leftStickMaxX = newMaxX,
                        leftStickMinY = newMinY,
                        leftStickMaxY = newMaxY,
                        leftStickCircularity = circularity,
                        totalSamplesRecorded = leftStickPath.size
                    )
                }
            }

            CalibrationStep.RIGHT_STICK_RANGE -> {
                val x = state.rawRightStickX
                val y = state.rawRightStickY
                val mag = sqrt(x * x + y * y)

                if (mag > 0.15f) {
                    if (rightStickPath.size < 600) {
                        rightStickPath.add(Offset(x, y))
                    }
                    val newMinX = min(current.rightStickMinX, x)
                    val newMaxX = max(current.rightStickMaxX, x)
                    val newMinY = min(current.rightStickMinY, y)
                    val newMaxY = max(current.rightStickMaxY, y)

                    val circularity = calculateCircularity(rightStickPath)

                    _metrics.value = current.copy(
                        rightStickMinX = newMinX,
                        rightStickMaxX = newMaxX,
                        rightStickMinY = newMinY,
                        rightStickMaxY = newMaxY,
                        rightStickCircularity = circularity,
                        totalSamplesRecorded = rightStickPath.size
                    )
                }
            }

            CalibrationStep.LEFT_TRIGGER_RANGE -> {
                val valLT = state.rawLeftTrigger
                _metrics.value = current.copy(
                    leftTriggerPeak = max(current.leftTriggerPeak, valLT)
                )
            }

            CalibrationStep.RIGHT_TRIGGER_RANGE -> {
                val valRT = state.rawRightTrigger
                _metrics.value = current.copy(
                    rightTriggerPeak = max(current.rightTriggerPeak, valRT)
                )
            }

            else -> {}
        }
    }

    private fun finalizeMetrics() {
        val current = _metrics.value
        val outerReachL = max(abs(current.leftStickMaxX), abs(current.leftStickMinX))
        val outerReachR = max(abs(current.rightStickMaxX), abs(current.rightStickMinX))
        val recOuter = (min(outerReachL, outerReachR) * 0.98f).coerceIn(0.85f, 0.99f)

        _metrics.value = current.copy(
            recommendedOuterDeadzone = recOuter
        )
    }

    private fun calculateCircularity(points: List<Offset>): Float {
        if (points.size < 12) return 92.0f

        // Check angular coverage across 12 sectors of 30 degrees
        val sectorsCovered = BooleanArray(12)
        var totalMagDev = 0f
        var count = 0

        for (p in points) {
            val mag = sqrt(p.x * p.x + p.y * p.y)
            if (mag > 0.4f) {
                var angle = Math.toDegrees(atan2(p.y.toDouble(), p.x.toDouble())).toFloat()
                if (angle < 0) angle += 360f
                val sectorIdx = ((angle / 30f).toInt()).coerceIn(0, 11)
                sectorsCovered[sectorIdx] = true

                // Deviation from unit circle
                totalMagDev += abs(1.0f - mag)
                count++
            }
        }

        val coverageRatio = sectorsCovered.count { it } / 12f
        val avgDev = if (count > 0) totalMagDev / count else 0.1f
        val score = (coverageRatio * 75f) + ((1f - avgDev.coerceIn(0f, 1f)) * 25f)
        return score.coerceIn(50.0f, 99.8f)
    }

    fun buildRefinedCalibrationSettings(): CalibrationSettings {
        val m = _metrics.value
        return CalibrationSettings(
            innerDeadzone = m.recommendedInnerDeadzone,
            outerDeadzone = m.recommendedOuterDeadzone,
            leftStickCenterDrift = m.leftStickRestDrift,
            rightStickCenterDrift = m.rightStickRestDrift,
            leftStickMaxRange = max(abs(m.leftStickMaxX), abs(m.leftStickMinX)),
            rightStickMaxRange = max(abs(m.rightStickMaxX), abs(m.rightStickMinX)),
            leftTriggerDeadzone = (m.leftTriggerRest + 0.03f).coerceIn(0.02f, 0.15f),
            leftTriggerMax = m.leftTriggerPeak.coerceAtLeast(0.85f),
            rightTriggerDeadzone = (m.rightTriggerRest + 0.03f).coerceIn(0.02f, 0.15f),
            rightTriggerMax = m.rightTriggerPeak.coerceAtLeast(0.85f),
            responseCurve = "Linear",
            isCalibrated = true,
            calibratedTimestamp = System.currentTimeMillis()
        )
    }
}
