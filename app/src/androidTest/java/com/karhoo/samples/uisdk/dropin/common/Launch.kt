package com.karhoo.samples.uisdk.dropin.common

import android.content.Intent

interface Launch {

    fun launch(intent: Intent? = null)

}