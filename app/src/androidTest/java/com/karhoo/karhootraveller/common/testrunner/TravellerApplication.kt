package com.karhoo.karhootraveller.common.testrunner

import com.karhoo.karhootraveller.KarhooApplication
import com.karhoo.karhootraveller.util.KHMenuHandler
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.util.TestSDKConfig

class UiSDKApplication : KarhooApplication() {

    override fun onCreate() {
        super.onCreate()
        KarhooUISDK.setConfiguration(configuration = TestSDKConfig(this.applicationContext))
        KarhooUISDK.menuHandler = KHMenuHandler()
    }

}
