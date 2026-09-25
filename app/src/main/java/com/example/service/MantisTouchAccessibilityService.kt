package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MantisTouchAccessibilityService : AccessibilityService() {

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        @Volatile
        private var instance: MantisTouchAccessibilityService? = null

        fun injectTap(x: Float, y: Float, durationMs: Long = 25) {
            val s = instance ?: return
            val path = Path().apply {
                moveTo(x, y)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            s.dispatchGesture(gesture, null, null)
        }

        fun injectSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 80) {
            val s = instance ?: return
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            s.dispatchGesture(gesture, null, null)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceRunning.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility event processing if needed
    }

    override fun onInterrupt() {
        _isServiceRunning.value = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        _isServiceRunning.value = false
        return super.onUnbind(intent)
    }
}
