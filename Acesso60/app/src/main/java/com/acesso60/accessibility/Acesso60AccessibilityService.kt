package com.acesso60.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Acesso60AccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        activeService = this
        _connectionState.value = true
        Log.i(TAG, "connected instance=${System.identityHashCode(this)}")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "unbind instance=${System.identityHashCode(this)} active=${activeService === this}")
        if (activeService === this) {
            activeService = null
            _connectionState.value = false
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.i(TAG, "destroy instance=${System.identityHashCode(this)} active=${activeService === this}")
        if (activeService === this) {
            activeService = null
            _connectionState.value = false
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "Acesso60.A11y"
        @Volatile
        private var activeService: Acesso60AccessibilityService? = null

        private val _connectionState = MutableStateFlow(false)
        val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

        fun isConnected(): Boolean = activeService != null

        fun performBack(): Boolean = activeService?.performGlobalAction(GLOBAL_ACTION_BACK) == true
    }
}
