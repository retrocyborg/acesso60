package com.acesso60.ui.system

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.acesso60.accessibility.Acesso60AccessibilityService
import com.acesso60.ui.state.AccessibilityStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface AccessibilityStatusMonitor {
    val status: StateFlow<AccessibilityStatus>
    fun refresh()
}

class AndroidAccessibilityStatusMonitor(
    context: Context,
    private val scope: CoroutineScope,
    private val connectionTimeoutMillis: Long = 10_000L,
) : AccessibilityStatusMonitor {
    private val appContext = context.applicationContext
    private val expected = ComponentName(appContext, Acesso60AccessibilityService::class.java)
    private val _status = MutableStateFlow(AccessibilityStatus.DISABLED)
    override val status: StateFlow<AccessibilityStatus> = _status.asStateFlow()
    private var timeoutJob: Job? = null

    init {
        scope.launch {
            Acesso60AccessibilityService.connectionState.collect { updateFromSystem() }
        }
        updateFromSystem()
    }

    override fun refresh() {
        updateFromSystem(restartTimeout = true)
    }

    private fun updateFromSystem(restartTimeout: Boolean = false) {
        Log.i(TAG, "refresh configured=${isConfigured()} connected=${Acesso60AccessibilityService.connectionState.value}")
        when {
            !isConfigured() -> setStatus(AccessibilityStatus.DISABLED)
            Acesso60AccessibilityService.connectionState.value -> setStatus(AccessibilityStatus.CONNECTED)
            _status.value == AccessibilityStatus.CONNECTION_FAILED && !restartTimeout -> Unit
            else -> {
                _status.value = AccessibilityStatus.CONNECTING
                if (restartTimeout || timeoutJob?.isActive != true) scheduleTimeout()
            }
        }
    }

    private fun scheduleTimeout() {
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(connectionTimeoutMillis)
            if (isConfigured() && !Acesso60AccessibilityService.connectionState.value) {
                _status.value = AccessibilityStatus.CONNECTION_FAILED
            }
        }
    }

    private fun setStatus(newStatus: AccessibilityStatus) {
        timeoutJob?.cancel()
        timeoutJob = null
        _status.value = newStatus
        Log.i(TAG, "status=$newStatus")
    }

    private fun isConfigured(): Boolean {
        val enabled = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
    }

    private companion object {
        const val TAG = "Acesso60.A11y"
    }
}
