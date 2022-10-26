package com.karhoo.samples.uisdk.dropin.config

import android.content.Context
import android.graphics.drawable.Drawable
import com.karhoo.samples.uisdk.dropin.BuildConfig
import com.karhoo.samples.uisdk.dropin.KarhooAnalyticsProviderWithNotifications
import com.karhoo.samples.uisdk.dropin.R
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentManager

class KarhooConfig(val context: Context, private val authMethod: AuthenticationMethod = AuthenticationMethod.KarhooUser()) :
        KarhooUISDKConfiguration {
    override lateinit var paymentManager: PaymentManager
    var sdkAuthenticationRequired: ((callback: () -> Unit) -> Unit)? = null

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

    override fun simulatePaymentProvider(): Boolean = false

    override fun context(): Context {
        return context
    }

    override fun authenticationMethod(): AuthenticationMethod {
        return authMethod
    }

    override fun analyticsProvider(): AnalyticProvider? {
        return KarhooAnalyticsProviderWithNotifications()
    }

    override suspend fun requireSDKAuthentication(callback: () -> Unit) {
        sdkAuthenticationRequired?.invoke(callback)
    }
}
