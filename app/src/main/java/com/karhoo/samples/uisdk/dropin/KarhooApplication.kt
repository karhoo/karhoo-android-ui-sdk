package com.karhoo.samples.uisdk.dropin

import android.app.Application
import com.karhoo.samples.uisdk.dropin.config.GuestConfig
import com.karhoo.uisdk.KarhooUISDK

class KarhooApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KarhooUISDK.apply {
            setConfiguration(
                GuestConfig(
                    applicationContext
                )
            )
        }
    }
}
