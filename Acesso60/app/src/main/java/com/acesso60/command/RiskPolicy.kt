package com.acesso60.command

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

class RiskPolicy {
    fun classify(command: Command): RiskLevel = when (command) {
        Command.Back,
        Command.Home,
        Command.ReadScreen,
        Command.VolumeUp,
        Command.VolumeDown,
        is Command.OpenApp -> RiskLevel.LOW
    }
}
