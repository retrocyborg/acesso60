package com.acesso60.execution

/** Descrição controlada de um aplicativo que pode ser aberto por voz. */
data class AppDefinition(
    val id: String,
    val packageName: String,
    val aliases: Set<String>,
)
