package com.acesso60.execution

sealed interface LaunchResult {
    data object Started : LaunchResult
    data object NotInstalled : LaunchResult
    data class Failed(val reason: String) : LaunchResult
}

interface AppLauncher {
    fun launch(packageName: String): LaunchResult
}
