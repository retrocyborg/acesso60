package com.acesso60

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.acesso60.ui.Acesso60App
import com.acesso60.ui.simulation.SimulatedAssistantController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // PONTO DE INTEGRAÇÃO: trocar o controlador simulado pela implementação real
            // (Núcleo + Android) quando o contrato AssistantController estiver acordado.
            Acesso60App(controllerFactory = { scope -> SimulatedAssistantController(scope) })
        }
    }
}
