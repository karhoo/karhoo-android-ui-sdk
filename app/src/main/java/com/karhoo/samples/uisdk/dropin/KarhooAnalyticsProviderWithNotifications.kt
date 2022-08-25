package com.karhoo.samples.uisdk.dropin

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.karhoo.samples.uisdk.dropin.KarhooApplication
import com.karhoo.sdk.analytics.AnalyticProvider
import kotlin.random.Random

class KarhooAnalyticsProviderWithNotifications: AnalyticProvider {
    override fun trackEvent(event: String) {
        trackEvent(event, emptyMap())
    }

    override fun trackEvent(event: String, payloadMap: Map<String, Any>) {
        val notification = NotificationCompat.Builder(KarhooApplication.appContext, "analyticsChannel")
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