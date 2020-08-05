package com.karhoo.karhootraveller.util.playservices

import android.content.Context

import com.google.android.gms.common.GoogleApiAvailability

class KarhooPlayServicesUtil(private val context: Context?) : PlayServicesUtil {

    override fun playServicesUpToDate(): Int {
        return if (context != null) {
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            googleApiAvailability.isGooglePlayServicesAvailable(context)
        } else {
            0
        }
    }
}
