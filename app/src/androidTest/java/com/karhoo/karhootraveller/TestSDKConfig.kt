package com.karhoo.karhootraveller

import android.content.Context
import android.graphics.drawable.Drawable
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration

class TestSDKConfig(private val context: Context, private val authenticationMethod:
AuthenticationMethod = AuthenticationMethod.KarhooUser()) : KarhooUISDKConfiguration {
    override fun logo(): Drawable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun environment(): KarhooEnvironment {
        return KarhooEnvironment.Custom(host = "http://127.0.0.1:8089", authHost = "", guestHost = "")
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