package com.karhoo.karhootraveller

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.provider.Settings
import com.karhoo.karhootraveller.service.analytics.KarhooAnalytics
import com.karhoo.karhootraveller.service.preference.KarhooPreferenceStore
import com.karhoo.karhootraveller.util.KHMenuHandler
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.notification.KarhooNotificationContract
import com.karhoo.uisdk.notification.rides.past.RideNotificationView
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import java.util.UUID

open class KarhooApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        KarhooUISDK.apply {
            setConfiguration(KarhooConfig(applicationContext))
            karhooNotification = KarhooNotificationContract(RideNotificationView())
            karhooNotification?.initWith(instance)
        }

        registerActivityLifecycleCallbacks(
                AnalyticActivityLifecycleCallbacks(
                        Settings.Secure.getString(this.contentResolver,
                                                  Settings.Secure.ANDROID_ID)))
        AnalyticsManager.initialise()

        KarhooUISDK.analytics = KarhooAnalytics.INSTANCE
        KarhooUISDK.Routing.apply {
            rides = RidesActivity::class.java
            rideDetail = RideDetailActivity::class.java
        }
        KarhooUISDK.menuHandler = KHMenuHandler()
    }

    override fun onTerminate() {
        super.onTerminate()
        KarhooAnalytics.INSTANCE.appClosed()
    }

    private inner class AnalyticActivityLifecycleCallbacks internal constructor(private val devId: String) : ActivityLifecycleCallbacks {

        private var numStarted = 0
        private var sessionId = ""

        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
            // Do nothing
        }

        override fun onActivityDestroyed(activity: Activity) {
            // Do nothing
        }

        override fun onActivityPaused(activity: Activity) {
            // Do nothing
        }

        override fun onActivityResumed(activity: Activity) {
            // Do nothing
        }

        override fun onActivitySaveInstanceState(activity: Activity,
                                                 outState: Bundle?) {
            // Do nothing
        }

        override fun onActivityStarted(activity: Activity) {
            if (sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString()
            }
            if (numStarted == 0) {
                // app opened
                AnalyticsManager.deviceId = devId
                AnalyticsManager.sessionId = sessionId
                sendAppOpenedEventWithGdprCompliance()
            }
            numStarted++
        }

        override fun onActivityStopped(activity: Activity) {
            numStarted--
            if (numStarted == 0) {
                // app went to background
                KarhooAnalytics.INSTANCE.appBackground(KarhooPreferenceStore.getInstance(instance).lastTrip)
                sessionId = ""
            }
        }

        private fun sendAppOpenedEventWithGdprCompliance() {
            if (KarhooApi.userStore.isCurrentUserValid) {
                KarhooAnalytics.INSTANCE.appOpened()
            }
        }
    }

    companion object {
        lateinit var instance: KarhooApplication
            private set
    }

}
