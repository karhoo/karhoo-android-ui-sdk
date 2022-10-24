package com.karhoo.uisdk.util

import android.content.Context
import android.graphics.drawable.Drawable
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration
import com.karhoo.uisdk.screen.booking.checkout.payment.AdyenPaymentManager
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentManager

class TestSDKConfig(val context: Context, private val authenticationMethod: AuthenticationMethod =
        AuthenticationMethod.KarhooUser()) :
        KarhooUISDKConfiguration {

    override fun environment(): KarhooEnvironment {
        return KarhooEnvironment.Custom(host = "http://127.0.0.1:8089", authHost = "", guestHost = "")
    }

    override fun simulatePaymentProvider(): Boolean {
        return true
    }

    override fun context(): Context {
        return context
    }

    override fun logo(): Drawable? {
        return null
    }

    override fun authenticationMethod(): AuthenticationMethod {
        return authenticationMethod
    }

    override fun analyticsProvider(): AnalyticProvider? {
        return null
    }

    override var paymentManager: PaymentManager = AdyenPaymentManager()
    override suspend fun requireSDKAuthentication(callback: () -> Unit) {
        // do nothing
    }
}