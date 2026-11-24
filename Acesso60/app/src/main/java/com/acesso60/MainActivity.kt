package com.acesso60

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.acesso60.core.interpreter.CommandInterpreter
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.router.CommandRouter
import br.com.acesso60.core.validator.CommandValidator
import com.acesso60.execution.AndroidAppLauncher
import com.acesso60.execution.AndroidCommandHandler
import com.acesso60.execution.AndroidVolumeController
import com.acesso60.execution.AppDefinition
import com.acesso60.execution.DebounceExecutionMiddleware
import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.processing.CommandProcessor
import com.acesso60.execution.HomeCommandHandler
import com.acesso60.execution.StaticAppRegistry
import com.acesso60.execution.OpenAppCommandHandler
import com.acesso60.execution.JsonAppDefinitionProvider
import com.acesso60.execution.VolumeCommandHandler
import com.acesso60.execution.AddAliasCommandHandler
import com.acesso60.ui.Acesso60App
import com.acesso60.ui.RealAssistantController
import com.acesso60.execution.LoggingExecutionMiddleware
import br.com.acesso60.core.middleware.ExecutionMiddleware
import com.acesso60.voice.AndroidSpeechToTextEngine
import com.acesso60.voice.AndroidTextSpeaker
import com.acesso60.messaging.AndroidMessageLauncher
import com.acesso60.execution.SendMessageCommandHandler
import com.acesso60.execution.ReadScreenCommandHandler
import br.com.acesso60.core.messaging.VoiceMessageCoordinator
import br.com.acesso60.core.logging.RedactingConsoleLogger
import br.com.acesso60.core.logging.SafeLogEvent
import br.com.acesso60.core.logging.ExecutionLogger
import br.com.acesso60.core.logging.ExecutionLogEvent
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import br.com.acesso60.core.executor.CommandHandler

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_START_LISTENING = "com.acesso60.extra.START_LISTENING"
    }

    private val startListeningTrigger = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent?.getBooleanExtra(EXTRA_START_LISTENING, false) == true) {
            startListeningTrigger.value = true
        }
        setContent {
            Acesso60App(
                controllerFactory = { scope ->
                val validator = CommandValidator()
                val riskPolicy = RiskPolicy()
                // Uma única instância compartilhada pelo registro de apps e pelo handler de alias.
                val appDefinitions = runBlocking { JsonAppDefinitionProvider.create(this@MainActivity) }
                val safeLogger = RedactingConsoleLogger(sink = { line -> Log.i("Acesso60.Execution", line) })

                // New orchestration using CommandProcessor and handlers
                val processor = CommandProcessor(
                    handlers = listOf<CommandHandler>(
                        AndroidCommandHandler(),
                        HomeCommandHandler(),
                        VolumeCommandHandler(AndroidVolumeController(this)),
                        OpenAppCommandHandler(
                            registry = StaticAppRegistry(appDefinitions),
                            launcher = AndroidAppLauncher(this),
                        ),
                        // Handler para que o usuário adicione/atualize aliases via voz
                        AddAliasCommandHandler(appDefinitions),
                        // Mensagem por voz: abre o app escolhido com o texto pronto (após confirmação)
                        SendMessageCommandHandler(AndroidMessageLauncher(applicationContext)),
                        // Leitura de tela filtrada pela ScreenPrivacyPolicy
                        ReadScreenCommandHandler(),
                    ),
                    middlewares = listOf<ExecutionMiddleware>(
                        DebounceExecutionMiddleware(),
                        LoggingExecutionMiddleware(
                            logger = object : ExecutionLogger {
                                override fun log(event: ExecutionLogEvent) {
                                    // Nunca registrar o conteúdo do resultado (pode conter texto da tela).
                                    safeLogger.log(
                                        SafeLogEvent(
                                            category = "execution",
                                            action = "command",
                                            status = "finished",
                                            metadata = mapOf(
                                                "command" to event.commandName,
                                                "duration_ms" to event.durationMs.toString(),
                                                "result" to (event.result::class.simpleName ?: "Unknown"),
                                            ),
                                        )
                                    )
                                }
                            },
                        ),
                    ),
                )
                // Adapter to satisfy CommandExecutor expected by CommandRouter
                val executionWrapper = object : CommandExecutor {
                    override fun execute(command: Command) = processor.process(command)
                }

                RealAssistantController(
                    scope = scope,
                    speechToText = AndroidSpeechToTextEngine(this),
                    interpreter = CommandInterpreter(),
                    validator = validator,
                    riskPolicy = riskPolicy,
                    router = CommandRouter(
                        validator,
                        riskPolicy,
                        executionWrapper,
                    ),
                    messageCoordinator = VoiceMessageCoordinator(),
                    speaker = AndroidTextSpeaker(applicationContext),
                )
            },
            startListeningTrigger = startListeningTrigger.asStateFlow(),
            onListeningTriggerConsumed = { startListeningTrigger.value = false },
        )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_START_LISTENING, false)) {
            startListeningTrigger.value = true
        }
    }
}
