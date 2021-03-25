package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.CANCELLATION_BEFORE_DRIVER_EN_ROUTE
import com.karhoo.uisdk.util.CANCELLATION_TIME_BEFORE_PICKUP

fun ServiceCancellation.getCancellationText(context: Context): String? {
    var text: String?

    when (this.type) {
        CANCELLATION_TIME_BEFORE_PICKUP -> {
            text = String.format(context.getString(R.string.kh_uisdk_quote_cancellation_minutes), minutes)

            if (minutes == 0) {
                text = null
            }
        }
        CANCELLATION_BEFORE_DRIVER_EN_ROUTE -> {
            text = context.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)
        }
        else -> {
            text = null
        }
    }

    return text
}

fun ServiceCancellation.hasValidCancellationDependingOnTripStatus(tripStatus: TripStatus): Boolean {
    if (this.type == CANCELLATION_TIME_BEFORE_PICKUP) {
        when (tripStatus) {
            TripStatus.REQUESTED,
            TripStatus.CONFIRMED,
            TripStatus.DRIVER_EN_ROUTE,
            TripStatus.ARRIVED -> {
                return true
            }
        }
    } else if (this.type == CANCELLATION_BEFORE_DRIVER_EN_ROUTE) {
        when (tripStatus) {
            TripStatus.REQUESTED,
            TripStatus.CONFIRMED -> {
                return true
            }
        }
    }

    return false
}
