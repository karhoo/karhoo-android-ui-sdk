package com.karhoo.uisdk.common

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import com.karhoo.uisdk.util.extension.toNormalizedLocale

interface Launch {

    fun launch(intent: Intent? = null)

}

fun Launch.getLocale(): String = InstrumentationRegistry.getInstrumentation()
        .targetContext.resources.configuration.locale.toNormalizedLocale()