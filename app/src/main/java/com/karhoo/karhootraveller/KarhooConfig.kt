package com.karhoo.karhootraveller

import android.content.Context
import android.graphics.drawable.Drawable
import com.karhoo.karhootraveller.service.analytics.SegmentProvider
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration

class KarhooConfig(val context: Context, private val authMethod: AuthenticationMethod = AuthenticationMethod.KarhooUser()) :
        KarhooUISDKConfiguration {

    override fun logo(): Drawable? {
        return context.getDrawable(R.drawable.karhoo_wordmark)
    }

    override fun environment(): KarhooEnvironment {
        return if (BuildConfig.BUILD_TYPE == "debug") {
            KarhooEnvironment.Custom(host = BuildConfig.STAGING_HOST,
                                     authHost = BuildConfig.STAGING_AUTH_HOST,
                                     guestHost = BuildConfig.STAGING_GUEST_HOST)
        } else if (BuildConfig.BUILD_TYPE == "prodQA" || BuildConfig.BUILD_TYPE == "release") {
            KarhooEnvironment.Production()
        } else {
            KarhooEnvironment.Sandbox()
        }
    }

    override fun handleBraintree(): Boolean = false

    override fun context(): Context {
        return context
    }

    override fun authenticationMethod(): AuthenticationMethod {
        return authMethod
    }

    override fun analyticsProvider(): AnalyticProvider? {
        return SegmentProvider(context)
    }
}
