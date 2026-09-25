package com.example

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MantisApp
import com.example.ui.MantisViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MantisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MantisApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.gamepadDetector.refreshControllers()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Direct intercept of controller buttons for ultra low latency
        if (viewModel.inputManager.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        // Direct intercept of controller analog sticks and triggers
        if (viewModel.inputManager.handleGenericMotionEvent(event)) {
            return true
        }
        return super.dispatchGenericMotionEvent(event)
    }
}
