package com.acesso60.execution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

class AndroidAppLauncher(
    private val context: Context,
) : AppLauncher {
    override fun launch(packageName: String): LaunchResult {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: return LaunchResult.NotInstalled

        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            LaunchResult.Started
        } catch (_: ActivityNotFoundException) {
            LaunchResult.Failed("Atividade de inicialização não encontrada.")
        } catch (_: SecurityException) {
            LaunchResult.Failed("Permissão insuficiente para abrir o aplicativo.")
        }
    }
}
