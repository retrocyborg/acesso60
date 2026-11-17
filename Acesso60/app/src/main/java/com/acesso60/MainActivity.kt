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
import br.com.acesso60.core.logging.ExecutionLogger
import br.com.acesso60.core.logging.ExecutionLogEvent
import kotlinx.coroutines.runBlocking
import br.com.acesso60.core.executor.CommandHandler
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Acesso60App(controllerFactory = { scope ->
                val validator = CommandValidator()
                val riskPolicy = RiskPolicy()

                // New orchestration using CommandProcessor and handlers
                val processor = CommandProcessor(
                    handlers = listOf<CommandHandler>(
                        AndroidCommandHandler(),
                        HomeCommandHandler(),
                        VolumeCommandHandler(AndroidVolumeController(this)),
                        OpenAppCommandHandler(
                            registry = runBlocking { JsonAppDefinitionProvider.create(this@MainActivity) }
                                .let { provider -> StaticAppRegistry(provider) },
                            launcher = AndroidAppLauncher(this),
                        ),
                        // Handler para que o usuário adicione/atualize aliases via voz
                        AddAliasCommandHandler(runBlocking { JsonAppDefinitionProvider.create(this@MainActivity) })
                    ),
                    middlewares = listOf<ExecutionMiddleware>(
                        DebounceExecutionMiddleware(),
// ... existing imports ...

                        LoggingExecutionMiddleware(
                            logger = object : ExecutionLogger {
                                override fun log(event: ExecutionLogEvent) {
                                    Log.i("Acesso60.Execution", "command=${event.commandName} duration=${event.durationMs}ms result=${event.result}")
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
                )
            })
        }
    }
}
