package com.acesso60

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.acesso60.core.interpreter.CommandInterpreter
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.router.CommandRouter
import br.com.acesso60.core.validator.CommandValidator
import com.acesso60.execution.AndroidCommandExecutor
import com.acesso60.ui.Acesso60App
import com.acesso60.ui.RealAssistantController
import com.acesso60.voice.AndroidSpeechToTextEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Acesso60App(controllerFactory = { scope ->
                val validator = CommandValidator()
                val riskPolicy = RiskPolicy()
                RealAssistantController(
                    scope = scope,
                    speechToText = AndroidSpeechToTextEngine(this),
                    interpreter = CommandInterpreter(),
                    validator = validator,
                    riskPolicy = riskPolicy,
                    router = CommandRouter(validator, riskPolicy, AndroidCommandExecutor()),
                )
            })
        }
    }
}
