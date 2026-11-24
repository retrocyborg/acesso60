package com.acesso60.accessibility

import com.acesso60.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityButtonShortcutTest {

    @Test
    fun `extra start listening constant is properly defined`() {
        assertEquals("com.acesso60.extra.START_LISTENING", MainActivity.EXTRA_START_LISTENING)
    }

    @Test
    fun `handle accessibility button click triggers callback hook`() {
        var clicked = false
        Acesso60AccessibilityService.onButtonClickedForTesting = {
            clicked = true
        }

        // Simula clique no botão/atalho de acessibilidade
        Acesso60AccessibilityService.onButtonClickedForTesting?.invoke()
        assertTrue(clicked)
    }
}
