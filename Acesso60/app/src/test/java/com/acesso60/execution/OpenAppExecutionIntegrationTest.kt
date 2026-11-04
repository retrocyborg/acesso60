package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Test

/** Contratos do fluxo completo do wrapper para qualquer aplicativo registrado. */
class OpenAppExecutionIntegrationTest {
    private val applications = listOf(
        AppDefinition("whatsapp", "com.whatsapp", setOf("whatsapp")),
        AppDefinition("calculator", "com.android.calculator2", setOf("calculator")),
    )

    @Test
    fun `wrapper returns unavailable when a registered application is not installed`() {
        val launcher = FakeLauncher(LaunchResult.NotInstalled)

        applications.forEach { app ->
            assertEquals(
                ExecutionResult.Unavailable,
                wrapper(launcher).execute(Command.OpenApp(app.id)),
            )
            assertEquals(app.packageName, launcher.lastPackage)
        }
    }

    @Test
    fun `wrapper returns controlled failure for launcher errors`() {
        val launcher = FakeLauncher(LaunchResult.Failed("activity blocked"))

        applications.forEach { app ->
            assertEquals(
                ExecutionResult.Failure("Não foi possível abrir ${app.id}."),
                wrapper(launcher).execute(Command.OpenApp(app.id)),
            )
            assertEquals(app.packageName, launcher.lastPackage)
        }
    }

    private fun wrapper(launcher: FakeLauncher) = DefaultExecutionWrapper(
        listOf(
            OpenAppCommandExecutor(
                registry = StaticAppRegistry(applications),
                launcher = launcher,
            ),
        ),
    )

    private class FakeLauncher(private val result: LaunchResult) : AppLauncher {
        var lastPackage: String? = null

        override fun launch(packageName: String): LaunchResult {
            lastPackage = packageName
            return result
        }
    }
}
