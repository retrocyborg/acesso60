package com.acesso60.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.acesso60.ui.preferences.PreferencesStore
import com.acesso60.ui.preferences.TextScale
import com.acesso60.ui.state.AccessibilityStatus
import com.acesso60.ui.system.AccessibilityStatusMonitor
import com.acesso60.ui.system.SystemStatusReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantViewModel(
    controllerFactory: (CoroutineScope) -> AssistantController,
    private val preferences: PreferencesStore,
    accessibilityMonitorFactory: (CoroutineScope) -> AccessibilityStatusMonitor,
    statusReaderFactory: (() -> AccessibilityStatus) -> SystemStatusReader,
) : ViewModel() {

    private val controller = controllerFactory(viewModelScope)
    private val accessibilityMonitor = accessibilityMonitorFactory(viewModelScope)
    private val statusReader = statusReaderFactory { accessibilityMonitor.status.value }

    val presenter = AssistantPresenter(controller, statusReader::read)

    private val _textScale = MutableStateFlow(preferences.loadTextScale())
    val textScale: StateFlow<TextScale> = _textScale.asStateFlow()

    init {
        viewModelScope.launch {
            controller.events.collect { presenter.onEvent(it) }
        }
        viewModelScope.launch {
            accessibilityMonitor.status.collect { presenter.refreshPermissions() }
        }
    }

    fun refreshPermissions() {
        accessibilityMonitor.refresh()
        presenter.refreshPermissions()
    }

    fun onListenRequested() {
        if (accessibilityMonitor.status.value == AccessibilityStatus.CONNECTION_FAILED) {
            presenter.onDismissResult()
            accessibilityMonitor.refresh()
            return
        }
        presenter.onListenRequested()
    }

    fun onTextScaleSelected(scale: TextScale) {
        _textScale.value = scale
        preferences.saveTextScale(scale)
    }

    override fun onCleared() {
        controller.dispose()
        super.onCleared()
    }

    class Factory(
        private val controllerFactory: (CoroutineScope) -> AssistantController,
        private val preferences: PreferencesStore,
        private val accessibilityMonitorFactory: (CoroutineScope) -> AccessibilityStatusMonitor,
        private val statusReaderFactory: (() -> AccessibilityStatus) -> SystemStatusReader,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AssistantViewModel(
                controllerFactory,
                preferences,
                accessibilityMonitorFactory,
                statusReaderFactory,
            ) as T
    }
}
