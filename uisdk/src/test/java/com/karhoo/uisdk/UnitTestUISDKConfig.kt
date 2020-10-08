package com.karhoo.uisdk

import android.content.Context
import android.graphics.drawable.Drawable
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod

class UnitTestUISDKConfig(private val context: Context, private val authenticationMethod:
AuthenticationMethod =
        AuthenticationMethod.KarhooUser(), private val handleBraintree: Boolean = true) :
        KarhooUISDKConfiguration {

    override fun logo(): Drawable? {
        return null
    }

    override fun simulatePaymentProvider(): Boolean {
        return handleBraintree
    }

    override fun environment(): KarhooEnvironment {
        return KarhooEnvironment.Sandbox()
    }

    override fun context(): Context {
        return context
    }

    override fun authenticationMethod(): AuthenticationMethod {
        return authenticationMethod
    }

    override fun analyticsProvider(): AnalyticProvider? {
        return null
    }
}