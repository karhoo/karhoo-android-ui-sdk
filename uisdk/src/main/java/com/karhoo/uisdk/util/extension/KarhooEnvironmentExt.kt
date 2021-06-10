package com.karhoo.uisdk.util.extension

import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.uisdk.R

fun KarhooEnvironment.guestTripTrackingUrl(): Int {
    return when (this) {
        is KarhooEnvironment.Sandbox
        -> R.string.kh_uisdk_guest_trip_tracking_sandbox
        is KarhooEnvironment.Production
        -> R.string.kh_uisdk_guest_trip_tracking_prod
        else ->
            R.string.kh_uisdk_guest_trip_tracking_custom
    }
}