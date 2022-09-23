package com.karhoo.samples.uisdk.dropin.config

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.karhoo.samples.uisdk.dropin.BuildConfig
import com.karhoo.samples.uisdk.dropin.KarhooAnalyticsProviderWithNotifications
import com.karhoo.samples.uisdk.dropin.R
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration
import android.util.Log
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentManager

class AdyenTokenExchangeConfig(private val context: Context) : KarhooUISDKConfiguration {
    override lateinit var paymentManager: PaymentManager
    var externalAuthenticationNeeded: ((callback: () -> Unit) -> Unit)? = null

    override fun context(): Context {
        return context
    }

    override fun environment(): KarhooEnvironment {
        return KarhooEnvironment.Custom(
            host = BuildConfig.STAGING_HOST,
            authHost = BuildConfig.STAGING_AUTH_HOST,
            guestHost = BuildConfig.STAGING_GUEST_HOST
        )
    }

    override fun analyticsProvider(): AnalyticProvider? {
        return KarhooAnalyticsProviderWithNotifications()
    }

    override fun authenticationMethod(): AuthenticationMethod {
        return AuthenticationMethod.TokenExchange(
            clientId = BuildConfig.ADYEN_CLIENT_ID,
            scope = BuildConfig.ADYEN_CLIENT_SCOPE
        )
    }

    override fun logo(): Drawable? {
        return ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    }

    override fun isExplicitTermsAndConditionsConsentRequired(): Boolean {
        return true
    }

    override suspend fun requireSDKAuthentication(callback: () -> Unit) {
        Log.e("matei","requireSDKAuthentication")
        externalAuthenticationNeeded?.invoke(callback)
    }
}
