package com.karhoo.uisdk.common

import android.content.Context
import android.net.wifi.WifiManager

fun networkServiceRobot(func: NetworkServiceRobot.() -> Unit) = NetworkServiceRobot().apply { func() }

class NetworkServiceRobot {

    fun enableNetwork(context: Context) {
        setWifiStatus(context, true)
    }

    fun disableNetwork(context: Context) {
        setWifiStatus(context, false)
    }

    private fun setWifiStatus(context: Context, wifiStatus: Boolean) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = wifiStatus
    }

}