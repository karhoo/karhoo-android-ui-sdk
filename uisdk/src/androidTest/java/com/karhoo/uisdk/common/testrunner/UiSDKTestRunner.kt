package com.karhoo.uisdk.common.testrunner

import android.app.Application
import android.content.Context
import com.karumi.shot.ShotTestRunner

class UiSDKTestRunner : ShotTestRunner() {

    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        val applicationClassName = UiSDKApplication::class.java.name
        return super.newApplication(cl, applicationClassName, context)
    }

}