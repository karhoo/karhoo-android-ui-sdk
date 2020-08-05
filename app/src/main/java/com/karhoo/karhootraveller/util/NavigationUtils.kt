package com.karhoo.karhootraveller.util

import android.content.Intent
import com.karhoo.karhootraveller.KarhooApplication
import com.karhoo.karhootraveller.presentation.splash.SplashActivity
import com.karhoo.karhootraveller.service.analytics.KarhooAnalytics
import com.karhoo.karhootraveller.service.preference.KarhooPreferenceStore
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.screen.address.recents.SharedPreferencesLocationStore
import com.karhoo.uisdk.screen.rides.feedback.FeedbackCompletedTripsStore

fun logoutAndResetApp(isAutomaticLogout: Boolean = false) {
    val context = KarhooApplication.instance
    KarhooApi.userService.logout()
    KarhooAnalytics.INSTANCE.userLoggedOut()
    KarhooPreferenceStore.getInstance(context).apply {
        lastTrip = null
        loginTimeMillis = 0L
    }
    SharedPreferencesLocationStore(context).clear()
    val intent = Intent(context, SplashActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(SplashActivity.EXTRA_AUTOMATIC_LOGOUT, isAutomaticLogout)
    }
    FeedbackCompletedTripsStore(context).clear()
    context.startActivity(intent)
}
