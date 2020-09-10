package com.karhoo.karhootraveller.service.analytics

import android.content.Context
import android.util.Log
import com.karhoo.karhootraveller.BuildConfig
import com.karhoo.sdk.analytics.AnalyticProvider
import com.segment.analytics.Analytics
import com.segment.analytics.Properties

class SegmentProvider constructor(private val context: Context) : AnalyticProvider {
    var segmentKey: String

    init {

        segmentKey = if (BuildConfig.BUILD_TYPE == "prodQA" || BuildConfig.BUILD_TYPE
                == "release") BuildConfig.KARHOO_SEGMENT_API_KEY_PROD else BuildConfig.KARHOO_SEGMENT_API_KEY_SANDBOX
        if (segmentKey.isNullOrEmpty()) {
            Log.w("SEGMENT PROVIDER", "Segment API Key missing, please add the api key the")
        } else {
            val analytics = Analytics.Builder(context, segmentKey)
                    .trackApplicationLifecycleEvents()
                    .recordScreenViews()
                    .build()

            Analytics.setSingletonInstance(analytics)
        }
    }

    override fun trackEvent(event: String) {
        if (!segmentKey.isNullOrEmpty()) {
            Analytics.with(context).track(event)
        }
    }

    override fun trackEvent(event: String, payloadMap: Map<String, Any>) {
        val properties = Properties()

        for ((key, value) in payloadMap) {
            properties[key] = value
        }

        if (!segmentKey.isNullOrEmpty()) {
            Analytics.with(context).track(event, properties)
        }
    }
}
