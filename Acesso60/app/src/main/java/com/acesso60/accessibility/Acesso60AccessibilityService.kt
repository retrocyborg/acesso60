package com.acesso60.accessibility

import android.accessibilityservice.AccessibilityButtonController
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import br.com.acesso60.core.screen.ScreenReadOutcome
import br.com.acesso60.core.screen.ScreenReader
import com.acesso60.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Acesso60AccessibilityService : AccessibilityService() {
    private var buttonCallback: AccessibilityButtonController.AccessibilityButtonCallback? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        activeService = this
        _connectionState.value = true
        Log.i(TAG, "connected instance=${System.identityHashCode(this)}")

        setupAccessibilityButton()
    }

    private fun setupAccessibilityButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val info = serviceInfo
            if (info != null) {
                info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
                serviceInfo = info
            }
            val controller = accessibilityButtonController
            val callback = object : AccessibilityButtonController.AccessibilityButtonCallback() {
                override fun onClicked(controller: AccessibilityButtonController) {
                    Log.i(TAG, "Accessibility button clicked, triggering assistant")
                    handleAccessibilityButtonClick()
                }
            }
            buttonCallback = callback
            controller.registerAccessibilityButtonCallback(callback)
        }
    }

    fun handleAccessibilityButtonClick() {
        onButtonClickedForTesting?.invoke()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_START_LISTENING, true)
        }
        startActivity(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "unbind instance=${System.identityHashCode(this)} active=${activeService === this}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buttonCallback?.let { accessibilityButtonController.unregisterAccessibilityButtonCallback(it) }
            buttonCallback = null
        }
        if (activeService === this) {
            activeService = null
            _connectionState.value = false
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.i(TAG, "destroy instance=${System.identityHashCode(this)} active=${activeService === this}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buttonCallback?.let { accessibilityButtonController.unregisterAccessibilityButtonCallback(it) }
            buttonCallback = null
        }
        if (activeService === this) {
            activeService = null
            _connectionState.value = false
        }
        super.onDestroy()
    }

    private fun captureScreen(): ScreenReadOutcome {
        val root = rootInActiveWindow ?: return ScreenReadOutcome.Unavailable
        return screenReader.read(
            packageName = root.packageName?.toString(),
            windowTitle = root.window?.title?.toString(),
            root = AccessibilityScreenNode(root),
        )
    }

    companion object {
        private val screenReader = ScreenReader()
        private const val TAG = "Acesso60.A11y"
        @Volatile
        private var activeService: Acesso60AccessibilityService? = null

        var onButtonClickedForTesting: (() -> Unit)? = null

        private val _connectionState = MutableStateFlow(false)
        val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

        fun isConnected(): Boolean = activeService != null

        fun performBack(): Boolean = activeService?.performGlobalAction(GLOBAL_ACTION_BACK) == true

        fun performHome(): Boolean = activeService?.performGlobalAction(GLOBAL_ACTION_HOME) == true

        fun readScreen(): ScreenReadOutcome = activeService?.captureScreen() ?: ScreenReadOutcome.Unavailable
    }
}
