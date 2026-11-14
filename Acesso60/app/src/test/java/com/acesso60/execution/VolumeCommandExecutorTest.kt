package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeCommandExecutorTest {

    @Test
    fun `volume up returns success when controller succeeds`() {
        val controller = FakeVolumeController(raiseReturns = true, lowerReturns = true)
        val executor = VolumeCommandExecutor(controller)

        assertEquals(
            ExecutionResult.Success("Aumentei o volume."),
            executor.execute(Command.VolumeUp),
        )
        assertEquals(1, controller.raiseCalls)
    }

    @Test
    fun `volume down returns success when controller succeeds`() {
        val controller = FakeVolumeController(raiseReturns = true, lowerReturns = true)
        val executor = VolumeCommandExecutor(controller)

        assertEquals(
            ExecutionResult.Success("Diminui o volume."),
            executor.execute(Command.VolumeDown),
        )
        assertEquals(1, controller.lowerCalls)
    }

    @Test
    fun `volume commands return unavailable when controller returns false`() {
        val controller = FakeVolumeController(raiseReturns = false, lowerReturns = false)
        val executor = VolumeCommandExecutor(controller)

        assertEquals(ExecutionResult.Unavailable, executor.execute(Command.VolumeUp))
        assertEquals(ExecutionResult.Unavailable, executor.execute(Command.VolumeDown))
    }

    @Test
    fun `unsupported command is rejected`() {
        val controller = FakeVolumeController(raiseReturns = true, lowerReturns = true)
        val executor = VolumeCommandExecutor(controller)

        assertEquals(ExecutionResult.Unavailable, executor.execute(Command.Back))
    }

    private class FakeVolumeController(
        private val raiseReturns: Boolean,
        private val lowerReturns: Boolean,
    ) : VolumeController {
        var raiseCalls = 0
        var lowerCalls = 0

        override fun raiseVolume(): Boolean {
            raiseCalls++
            return raiseReturns
        }

        override fun lowerVolume(): Boolean {
            lowerCalls++
            return lowerReturns
        }
    }
}
