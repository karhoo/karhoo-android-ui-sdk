package com.karhoo.uisdk.screen.rideplanning

import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity

class RidePlanningCoordinator(var activity: Activity) {
    fun startCheckoutActivity(data: Intent?, journeyDetails: JourneyDetails) {
        BookingStorage.quote = data?.getParcelableExtra(QuotesActivity.QUOTES_SELECTED_QUOTE_KEY)

        BookingStorage.quote?.let { quote ->
            val builder = CheckoutActivity.Builder()
                .quote(quote)
                .outboundTripId(BookingStorage.outboundTripId)
                .bookingMetadata(BookingStorage.bookingMetadata)
                .journeyDetails(journeyDetails)

            BookingStorage.passengerDetails?.let {
                builder.passengerDetails(it)
            }

            BookingStorage.bookingComments?.let {
                builder.comments(it)
            }

            BookingStorage.loyaltyInfo?.let {
                builder.loyaltyInfo(it)
            }

            val validityTimeStamp = data?.getLongExtra(
                QuotesActivity.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP, 0
            )

            validityTimeStamp?.let {
                builder.validityDeadlineTimestamp(
                    validityTimeStamp
                )
            }

            activity.startActivityForResult(builder.build(activity), REQ_CODE_BOOKING_REQUEST_ACTIVITY)
        }
    }

    fun startQuoteListActivity(
        restorePreviousData: Boolean,
        validityTimestamp: Long? = null,
        restoredJourneyData: JourneyDetails?
    ) {
        val builder = QuotesActivity.Builder().restorePreviousData(restorePreviousData).bookingInfo(restoredJourneyData)
        validityTimestamp?.let {
            builder.validityTimestamp(validityTimestamp)
        }

        activity.startActivityForResult(builder.build(activity), QuotesActivity.QUOTES_INFO_REQUEST_NUMBER)
    }

    fun startRideDetailActivity(tripInfo: TripInfo) {
        ContextCompat.startActivity(activity, RideDetailActivity.Builder.newBuilder().trip(tripInfo).build(activity), null)
    }

    fun startActivityForResult(intent: Intent, code: Int) {
        activity.startActivityForResult(intent, code)
    }

    companion object {
        const val REQ_CODE_BOOKING_REQUEST_ACTIVITY = 304
    }

}
