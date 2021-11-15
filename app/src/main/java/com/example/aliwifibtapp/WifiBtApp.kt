package com.example.aliwifibtapp

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager

class WifiBtApp : Application() {
    private lateinit var wifiManager: WifiManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    companion object {

        var instance: WifiBtApp? = null

        @JvmName("getInstance1")
        fun getInstance(): WifiBtApp {
            return instance!!
        }
    }

    fun getWifiManager(): WifiManager {
        return wifiManager
    }
}