package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.example.engine.ControllerInputManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MantisOverlayService : Service() {

    companion object {
        private val _isOverlayActive = MutableStateFlow(false)
        val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, MantisOverlayService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MantisOverlayService::class.java)
            context.stopService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var statusTextView: TextView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        createFloatingWidget()
        _isOverlayActive.value = true
        observeInputTriggers()
    }

    private fun createFloatingWidget() {
        val wm = windowManager ?: return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 200
        }

        val frame = FrameLayout(this).apply {
            setPadding(28, 18, 28, 18)
            setBackgroundColor(0xEE090D16.toInt())
            val tv = TextView(this@MantisOverlayService).apply {
                text = "⚡ MANTIS HUD [Active]"
                setTextColor(0xFF00E5FF.toInt())
                textSize = 12f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            addView(tv)
            statusTextView = tv
        }

        try {
            wm.addView(frame, params)
            floatingView = frame
        } catch (_: Exception) {}
    }

    private fun observeInputTriggers() {
        val inputManager = ControllerInputManager.instance ?: return
        serviceScope.launch {
            inputManager.inputState.collectLatest { state ->
                val latestTrigger = inputManager.recentTriggers.value.firstOrNull()
                val textBuilder = StringBuilder("⚡ MANTIS HUD")
                if (state.pressedButtons.isNotEmpty()) {
                    textBuilder.append(" [")
                    textBuilder.append(state.pressedButtons.joinToString(","))
                    textBuilder.append("]")
                }
                if (latestTrigger != null) {
                    textBuilder.append(" • ")
                    textBuilder.append(latestTrigger.actionName)
                    textBuilder.append(" (")
                    textBuilder.append(latestTrigger.gestureType.name)
                    textBuilder.append(")")
                }
                statusTextView?.text = textBuilder.toString()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
        floatingView = null
        statusTextView = null
        _isOverlayActive.value = false
    }
}
