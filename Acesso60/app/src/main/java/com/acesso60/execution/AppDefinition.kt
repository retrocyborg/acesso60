package com.acesso60.execution

import kotlinx.serialization.Serializable

/** Descrição controlada de um aplicativo que pode ser aberto por voz. */
@Serializable
data class AppDefinition(
    val id: String,
    val packageName: String,
    val aliases: Set<String>,
) {
    /** Convenience secondary constructor used in tests */
    constructor(id: String, aliases: List<String>) : this(
        id = id,
        packageName = id,
        aliases = aliases.toSet()
    )
}

