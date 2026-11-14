package com.acesso60.execution

/** Contrato isolado de controle de volume para permitir testes unitários na JVM. */
interface VolumeController {
    fun raiseVolume(): Boolean
    fun lowerVolume(): Boolean
}
