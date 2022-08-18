package com.karhoo.samples.uisdk.dropin

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.karhoo.samples.uisdk.dropin.config.KarhooConfig
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.api.KarhooApi.setConfiguration
import com.karhoo.uisdk.KarhooUISDK

open class KarhooApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        instance = this
        KarhooUISDK.apply {
            setConfiguration(KarhooConfig(applicationContext))
        }

        initChannel("analyticsChannel", "analyticsChannel")
    }

    private fun initChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        lateinit  var appContext: Context
        lateinit var instance: KarhooApplication
    }
}
