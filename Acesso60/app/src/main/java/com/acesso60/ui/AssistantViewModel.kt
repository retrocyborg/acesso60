package com.acesso60.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.acesso60.ui.preferences.PreferencesStore
import com.acesso60.ui.preferences.TextScale
import com.acesso60.ui.state.PermissionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantViewModel(
    controllerFactory: (CoroutineScope) -> AssistantController,
    private val preferences: PreferencesStore,
    readPermissions: () -> PermissionStatus,
) : ViewModel() {

    private val controller = controllerFactory(viewModelScope)

    val presenter = AssistantPresenter(controller, readPermissions)

    private val _textScale = MutableStateFlow(preferences.loadTextScale())
    val textScale: StateFlow<TextScale> = _textScale.asStateFlow()

    init {
        viewModelScope.launch {
            controller.events.collect { presenter.onEvent(it) }
        }
    }

    fun onTextScaleSelected(scale: TextScale) {
        _textScale.value = scale
        preferences.saveTextScale(scale)
    }

    class Factory(
        private val controllerFactory: (CoroutineScope) -> AssistantController,
        private val preferences: PreferencesStore,
        private val readPermissions: () -> PermissionStatus,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AssistantViewModel(controllerFactory, preferences, readPermissions) as T
    }
}
