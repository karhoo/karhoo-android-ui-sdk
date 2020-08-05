package com.karhoo.uisdk.base.listener

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.provider.Settings

class NetworkReceiver(private val actions: Actions) : BroadcastReceiver() {

    val intentFilter: IntentFilter
        get() = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    val settingsIntent: Intent
        get() = Intent(Settings.ACTION_SETTINGS)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (!action.isNullOrEmpty() && action == ConnectivityManager.CONNECTIVITY_ACTION) {
            actions.connectionChanged(hasConnection(context))
        }
    }

    @SuppressLint("MissingPermission")
    fun hasConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    interface Actions {

        fun connectionChanged(isConnected: Boolean)

    }
}
