package br.com.acesso60.core.logging

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Logger seguro com redação de metadados")
class SafeExecutionLoggerTest {

    @Test
    fun `chaves seguras saem em claro e demais sao redigidas sem revelar tamanho`() {
        val lines = mutableListOf<String>()
        val logger = RedactingConsoleLogger(sink = { lines += it })

        logger.log(
            SafeLogEvent(
                category = "screen", action = "read", status = "ok",
                metadata = mapOf("package" to "com.whatsapp", "screen_text" to "segredo 1234", "message" to "oi"),
            )
        )

        val line = lines.single()
        assertTrue(line.contains("package=com.whatsapp"))
        assertTrue(line.contains("screen_text=[REDACTED]"))
        assertTrue(line.contains("message=[REDACTED]"))
        assertFalse(line.contains("segredo"))
        assertFalse(line.contains("LENGTH"))
    }

    @Test
    fun `linha tem formato estavel e sem caracteres de controle`() {
        val lines = mutableListOf<String>()
        RedactingConsoleLogger(sink = { lines += it })
            .log(SafeLogEvent("exec", "run", "success"))
        assertEquals("Acesso60Log | category=exec | action=run | status=success | meta={}", lines.single())
    }
}
