package com.karhoo.uisdk.screen.booking.booking.cancellation

interface BookingCancellationMVP {
    interface View {
        fun cancelTrip(tripId: String)

        fun showCancellationFee(formattedPrice: String, tripId: String)

        fun showCancellationFeeError()

        fun showCancellationError()

        fun showCancellationSuccess()
    }

    interface Presenter {
        fun getCancellationFee(tripId: String)

        fun handleCancellationRequest(tripId: String)
    }
}