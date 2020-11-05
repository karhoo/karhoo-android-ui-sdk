package com.karhoo.uisdk.screen.trip.bookingstatus

import androidx.annotation.StringRes
import com.karhoo.sdk.api.model.TripInfo

interface BookingStatusMVP {

    interface View {

        fun monitorTrip(tripId: String)

        fun showCancellationDialog(tripDetails: TripInfo)

        fun setCancelEnabled(enabled: Boolean)

        fun updateStatus(@StringRes status: Int, quote: String)

        fun showTemporaryError(error: String)

        fun tripComplete(tripDetails: TripInfo)

        fun tripCanceled(tripDetails: TripInfo)
    }

    interface Presenter {

        fun monitorTrip(tripIdentifier: String)

        fun updateBookingStatus(tripDetails: TripInfo)

        fun addTripInfoObserver(tripInfoListener: OnTripInfoChangedListener?)

        interface OnTripInfoChangedListener {

            fun onTripInfoChanged(tripInfo: TripInfo?)

        }
    }
}
