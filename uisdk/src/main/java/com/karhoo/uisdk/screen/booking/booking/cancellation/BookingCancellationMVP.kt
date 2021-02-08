package com.karhoo.uisdk.screen.booking.booking.cancellation

interface BookingCancellationMVP {
    interface View {
        fun cancelTrip()

        fun showCancellationFee(formattedPrice: String)

        fun showCancellationFeeError()

        fun showCancellationError()

        fun showCancellationSuccess()
    }

    interface Presenter {
        fun getCancellationFee(tripId: String)

        fun handleCancellationRequest(tripId: String)
    }
}