package com.karhoo.samples.uisdk.dropin.common.testrunner

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class TravellerTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        val applicationClassName = UiSDKApplication::class.java.name
        return super.newApplication(cl, applicationClassName, context)
    }

}