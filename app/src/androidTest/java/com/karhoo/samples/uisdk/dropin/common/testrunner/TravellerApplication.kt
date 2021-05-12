package com.karhoo.samples.uisdk.dropin.common.testrunner

import com.karhoo.samples.uisdk.dropin.KarhooApplication
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.common.testrunner.EmptyMenuHandler
import com.karhoo.uisdk.util.TestSDKConfig

class UiSDKApplication : KarhooApplication() {

    override fun onCreate() {
        super.onCreate()
        KarhooUISDK.setConfiguration(configuration = TestSDKConfig(this.applicationContext))
        KarhooUISDK.menuHandler = EmptyMenuHandler()
    }

}
