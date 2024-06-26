package com.karhoo.samples.uisdk.dropin.config;

import android.content.Context;
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.karhoo.samples.uisdk.dropin.BuildConfig
import com.karhoo.samples.uisdk.dropin.KarhooAnalyticsProviderWithNotifications
import com.karhoo.samples.uisdk.dropin.R
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentManager

class LoyaltyTokenBraintreeConfig(private val context: Context) : KarhooUISDKConfiguration {
    override lateinit var paymentManager: PaymentManager
    var sdkAuthenticationRequired: ((callback: () -> Unit) -> Unit)? = null
    var forceDarkMode = false

    override fun context(): Context {
        return context
    }

    override fun environment(): KarhooEnvironment {
        return KarhooEnvironment.Sandbox()
    }

    override fun analyticsProvider(): AnalyticProvider? {
        return KarhooAnalyticsProviderWithNotifications()
    }

    override fun authenticationMethod(): AuthenticationMethod {
        return AuthenticationMethod.TokenExchange(clientId = BuildConfig.LOYALTY_BRAINTREE_CLIENT_ID,
            scope = BuildConfig.LOYALTY_CLIENT_SCOPE)
    }

    override fun logo(): Drawable? {
        return ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    }

    override fun useAddToCalendarFeature(): Boolean {
        return true
    }

    override fun isExplicitTermsAndConditionsConsentRequired(): Boolean {
        return true
    }

    override suspend fun requireSDKAuthentication(callback: () -> Unit) {
        sdkAuthenticationRequired?.invoke(callback)
    }

    override fun forceDarkMode(): Boolean {
        return forceDarkMode
    }
}
