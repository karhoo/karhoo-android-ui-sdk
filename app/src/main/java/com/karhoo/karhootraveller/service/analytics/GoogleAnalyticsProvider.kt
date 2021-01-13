package com.karhoo.karhootraveller.service.analytics

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.karhoo.sdk.analytics.AnalyticProvider

class GoogleAnalyticsProvider : AnalyticProvider {

    fun Map<String, Any?>.toBundle(): Bundle = bundleOf(*this.toList().toTypedArray())

    override fun trackEvent(event: String) {
        Firebase.analytics.logEvent(event, null)
    }

    override fun trackEvent(event: String, payloadMap: Map<String, Any>) {
        val bundle = payloadMap.toBundle()

        Firebase.analytics.logEvent(event, bundle)
    }
}
