package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.CANCELLATION_BEFORE_DRIVER_EN_ROUTE
import com.karhoo.uisdk.util.CANCELLATION_TIME_BEFORE_PICKUP
import com.karhoo.uisdk.util.TimeUtil

fun ServiceCancellation.getCancellationText(context: Context, isPrebook: Boolean): String? {
    var cancellationText: String?

    when (this.type) {
        CANCELLATION_TIME_BEFORE_PICKUP -> {
            if (minutes == 0) {
                cancellationText = null
            } else {
                val hours: Int = TimeUtil.roundMinutesInHours(minutes)
                val leftOverMinutes: Int = TimeUtil.getLeftOverMinutesFromHours(minutes)

                if (isPrebook) {
                    cancellationText = context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_start) + " "
                    cancellationText += TimeUtil.getHourAndMinutesFormattedText(context, leftOverMinutes, hours)
                    cancellationText += context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_ending)
                } else {
                    cancellationText = context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_start) + " "
                    cancellationText += TimeUtil.getHourAndMinutesFormattedText(context, leftOverMinutes, hours)
                    cancellationText += context.getString(R.string.kh_uisdk_quote_cancellation_after_booking_ending)
                }
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

            else -> {}
        }
    } else if (this.type == CANCELLATION_BEFORE_DRIVER_EN_ROUTE) {
        when (tripStatus) {
            TripStatus.REQUESTED,
            TripStatus.CONFIRMED -> {
                return true
            }

            else -> {}
        }
    }

    return false
}
