package com.karhoo.samples.uisdk.dropin

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.karhoo.sdk.analytics.AnalyticProvider
import kotlin.random.Random

class KarhooAnalyticsProviderWithNotifications: AnalyticProvider {
    override fun trackEvent(event: String) {
        trackEvent(event, emptyMap())
    }

    override fun trackEvent(event: String, payloadMap: Map<String, Any>) {
        val sharedPrefs = KarhooApplication.appContext.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("notifications_enabled", false)) {
            val notification =
                NotificationCompat.Builder(KarhooApplication.appContext, "analyticsChannel")
            notification.setContentTitle(event)
            notification.setContentText(payloadMap.toString())
            notification.setSmallIcon(R.drawable.uisdk_karhoo_wordmark)
            notification.setAutoCancel(true)
            notification.setPriority(NotificationCompat.PRIORITY_MAX)
            notification.setStyle(NotificationCompat.BigTextStyle().bigText(payloadMap.toString()))
            val managerCompat = NotificationManagerCompat.from(KarhooApplication.appContext)
            managerCompat.notify(Random.nextInt(), notification.build())
        }
    }
}
