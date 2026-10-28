package com.acesso60.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.acesso60.ui.preferences.SharedPreferencesStore
import com.acesso60.ui.preferences.TextScale
import com.acesso60.ui.screens.AssistanceScreen
import com.acesso60.ui.screens.ConfirmationScreen
import com.acesso60.ui.screens.ListeningScreen
import com.acesso60.ui.screens.ResultScreen
import com.acesso60.ui.screens.SettingsScreen
import com.acesso60.ui.screens.WorkingScreen
import com.acesso60.ui.state.AssistantScreenState
import com.acesso60.ui.state.AssistantUiState
import com.acesso60.ui.system.SystemStatusReader
import com.acesso60.ui.system.AndroidAccessibilityStatusMonitor
import com.acesso60.ui.theme.Acesso60Theme
import kotlinx.coroutines.CoroutineScope

/** Todas as ações que a interface dispara. Valores padrão vazios facilitam previews e testes. */
data class Acesso60Actions(
    val onListen: () -> Unit = {},
    val onRequestMicrophone: () -> Unit = {},
    val onCancelListening: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onCancelConfirmation: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onDismissResult: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onCloseSettings: () -> Unit = {},
    val onOpenAccessibilitySettings: () -> Unit = {},
    val onOpenAppSettings: () -> Unit = {},
    val onTextScaleSelected: (TextScale) -> Unit = {},
)

/**
 * Raiz da interface (stateful): liga ViewModel, permissões e intents do sistema.
 *
 * @param controllerFactory fornece o [AssistantController]. A Activity injeta a
 *   implementação real; o simulador continua disponível para previews e testes de UI.
 */
@Composable
fun Acesso60App(
    controllerFactory: (CoroutineScope) -> AssistantController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val viewModel: AssistantViewModel = viewModel(
        factory = remember {
            val appContext = context.applicationContext
            AssistantViewModel.Factory(
                controllerFactory = controllerFactory,
                preferences = SharedPreferencesStore(appContext),
                accessibilityMonitorFactory = { scope ->
                    AndroidAccessibilityStatusMonitor(appContext, scope)
                },
                statusReaderFactory = { readAccessibilityStatus ->
                    SystemStatusReader(appContext, readAccessibilityStatus)
                },
            )
        },
    )
    val presenter = viewModel.presenter
    val state by presenter.uiState.collectAsState()
    val textScale by viewModel.textScale.collectAsState()

    // Ao voltar das telas do sistema (permissão/acessibilidade), relê a situação.
    val lifecycleOwner = context as? LifecycleOwner
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshPermissions()
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.refreshPermissions()
        if (granted) presenter.onDismissResult() // sai da tela "falta permissão", se estiver nela
    }

    val actions = Acesso60Actions(
        onListen = viewModel::onListenRequested,
        onRequestMicrophone = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        onCancelListening = presenter::onCancelListening,
        onConfirm = presenter::onConfirm,
        onCancelConfirmation = presenter::onCancelConfirmation,
        onRetry = viewModel::onListenRequested,
        onDismissResult = presenter::onDismissResult,
        onOpenSettings = presenter::onOpenSettings,
        onCloseSettings = presenter::onCloseSettings,
        onOpenAccessibilitySettings = { context.startSafely(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
        onOpenAppSettings = {
            context.startSafely(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
            )
        },
        onTextScaleSelected = viewModel::onTextScaleSelected,
    )

    Acesso60Theme(textScale = textScale) {
        Acesso60Content(state = state, textScale = textScale, actions = actions, modifier = modifier)
    }
}

/** Raiz stateless: escolhe qual das telas mostrar. Usada pelo app, pelos previews e pelos testes. */
@Composable
fun Acesso60Content(
    state: AssistantUiState,
    textScale: TextScale,
    actions: Acesso60Actions,
    modifier: Modifier = Modifier,
) {
    val screen = state.screen

    // Botão "voltar" do sistema: sempre sai do estado atual de forma segura.
    BackHandler(enabled = state.settingsOpen || screen !is AssistantScreenState.Idle) {
        when {
            state.settingsOpen -> actions.onCloseSettings()
            screen is AssistantScreenState.Listening -> actions.onCancelListening()
            screen is AssistantScreenState.Confirming -> actions.onCancelConfirmation() // cancelar = não executa
            screen is AssistantScreenState.Result -> actions.onDismissResult()
            else -> Unit // Working: aguarda terminar
        }
    }

    if (state.settingsOpen) {
        SettingsScreen(
            permissions = state.permissions,
            textScale = textScale,
            onTextScaleSelected = actions.onTextScaleSelected,
            onRequestMicrophone = actions.onRequestMicrophone,
            onOpenAccessibilitySettings = actions.onOpenAccessibilitySettings,
            onOpenAppSettings = actions.onOpenAppSettings,
            onBack = actions.onCloseSettings,
            modifier = modifier,
        )
        return
    }

    when (screen) {
        AssistantScreenState.Idle -> AssistanceScreen(
            permissions = state.permissions,
            onListen = actions.onListen,
            onRequestMicrophone = actions.onRequestMicrophone,
            onOpenSettings = actions.onOpenSettings,
            modifier = modifier,
        )

        is AssistantScreenState.Listening -> ListeningScreen(
            transcript = screen.transcript,
            onCancel = actions.onCancelListening,
            modifier = modifier,
        )

        AssistantScreenState.Working -> WorkingScreen(modifier)

        is AssistantScreenState.Confirming -> ConfirmationScreen(
            actionDescription = screen.actionDescription,
            simulated = screen.simulated,
            onConfirm = actions.onConfirm,
            onCancel = actions.onCancelConfirmation,
            modifier = modifier,
        )

        is AssistantScreenState.Result -> ResultScreen(
            outcome = screen.outcome,
            onRetry = actions.onRetry,
            onDone = actions.onDismissResult,
            onOpenSettings = actions.onOpenSettings,
            onRequestMicrophone = actions.onRequestMicrophone,
            modifier = modifier,
        )
    }
}

private fun Context.startSafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Aparelho sem essa tela de ajustes: não mostramos erro técnico ao usuário.
    }
}
