package com.test.coinapp

import android.app.Application
import com.test.coinapp.assembly.AppInjector

class CoinApp : Application() {
    val appInjector: AppInjector by lazy {
        return@lazy AppInjector(this)
    }

    override fun onCreate() {
        super.onCreate()
    }

}