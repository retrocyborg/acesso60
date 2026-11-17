package com.acesso60


import android.app.Application



import timber.log.Timber


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Plant Timber tree for debug builds; in production you might plant a release tree.
        Timber.plant(Timber.DebugTree())
    }
}
