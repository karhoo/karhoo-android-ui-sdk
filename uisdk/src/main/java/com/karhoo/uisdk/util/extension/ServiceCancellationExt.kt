package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.CANCELLATION_BEFORE_DRIVER_EN_ROUTE
import com.karhoo.uisdk.util.CANCELLATION_TIME_BEFORE_PICKUP
import com.karhoo.uisdk.util.DateUtil

fun ServiceCancellation.getCancellationText(context: Context): String? {
    var cancellationText: String?

    when (this.type) {
        CANCELLATION_TIME_BEFORE_PICKUP -> {
            if (minutes == 0) {
                cancellationText = null
            } else {
                val hours: Int = DateUtil.roundMinutesInHours(minutes)
                val leftOverMinutes: Int = DateUtil.getLeftOverMinutesFromHours(minutes)

                cancellationText = context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_start) + " "

                if (hours > 0) cancellationText += context.resources.getQuantityString(R.plurals.kh_uisdk_quote_cancellation_before_pickup_hours, hours, hours) + " "
                if (hours > 0 && leftOverMinutes > 0) cancellationText += context.getString(R.string.kh_uisdk_quote_cancellation_and_keyword) + " "
                if (leftOverMinutes > 0) cancellationText += context.resources.getQuantityString(R.plurals.kh_uisdk_quote_cancellation_before_pickup_minutes, leftOverMinutes, leftOverMinutes) + " "

                cancellationText += context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_ending)
            }
        }
        CANCELLATION_BEFORE_DRIVER_EN_ROUTE -> {
            cancellationText = context.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)
        }
        else -> {
            cancellationText = null
        }
    }

    return cancellationText
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
