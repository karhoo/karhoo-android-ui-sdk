package com.karhoo.samples.uisdk.dropin.config

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.karhoo.samples.uisdk.dropin.BuildConfig
import com.karhoo.samples.uisdk.dropin.R
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration

class GuestConfig(private val context: Context) : KarhooUISDKConfiguration {

    override fun context(): Context {
        return context
    }

    override fun environment(): KarhooEnvironment {
        return KarhooEnvironment.Sandbox()
    }

    override fun analyticsProvider(): AnalyticProvider? {
        return null
    }

    override fun authenticationMethod(): AuthenticationMethod {
        return AuthenticationMethod.Guest(
            identifier = BuildConfig.BRAINTREE_GUEST_CHECKOUT_IDENTIFIER,
            referer = BuildConfig.GUEST_CHECKOUT_REFERER,
            organisationId = BuildConfig.BRAINTREE_GUEST_CHECKOUT_ORGANISATION_ID
        )
    }

    override fun logo(): Drawable? {
        return ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    }
}