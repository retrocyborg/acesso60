package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAppCommandExecutorTest {
    private val app = AppDefinition("whatsapp", "com.whatsapp", setOf("whatsapp"))

    @Test
    fun `opens resolved application`() {
        val launcher = FakeLauncher(LaunchResult.Started)

        val result = executor(launcher).execute(Command.OpenApp("whatsapp"))

        assertEquals(ExecutionResult.Success("Solicitei a abertura de whatsapp."), result)
        assertEquals("com.whatsapp", launcher.lastPackage)
    }

    @Test
    fun `unknown application is unavailable without launching`() {
        val launcher = FakeLauncher(LaunchResult.Started)

        assertEquals(
            ExecutionResult.Unavailable,
            executor(launcher).execute(Command.OpenApp("telegram")),
        )
        assertEquals(null, launcher.lastPackage)
    }

    @Test
    fun `not installed application is unavailable`() {
        val launcher = FakeLauncher(LaunchResult.NotInstalled)

        assertEquals(
            ExecutionResult.Unavailable,
            executor(launcher).execute(Command.OpenApp("whatsapp")),
        )
    }

    @Test
    fun `launcher failure is controlled`() {
        val launcher = FakeLauncher(LaunchResult.Failed("blocked"))

        assertTrue(executor(launcher).execute(Command.OpenApp("whatsapp")) is ExecutionResult.Failure)
    }

    private fun executor(launcher: FakeLauncher) = OpenAppCommandExecutor(
        registry = StaticAppRegistry(listOf(app)),
        launcher = launcher,
    )

    private class FakeLauncher(private val result: LaunchResult) : AppLauncher {
        var lastPackage: String? = null

        override fun launch(packageName: String): LaunchResult {
            lastPackage = packageName
            return result
        }
    }
}
