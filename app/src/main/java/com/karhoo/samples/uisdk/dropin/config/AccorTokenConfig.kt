package com.karhoo.samples.uisdk.dropin.config;

import android.content.Context;
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.karhoo.samples.uisdk.dropin.BuildConfig
import com.karhoo.samples.uisdk.dropin.R
import com.karhoo.sdk.analytics.AnalyticProvider
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfiguration

public class AccorTokenConfig(private val context: Context) : KarhooUISDKConfiguration {

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
        return AuthenticationMethod.TokenExchange(clientId = BuildConfig.ACCOR_CLIENT_ID,
                                                  scope = BuildConfig.ACCOR_CLIENT_SCOPE)
    }

    override fun logo(): Drawable? {
        return ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    }
}

