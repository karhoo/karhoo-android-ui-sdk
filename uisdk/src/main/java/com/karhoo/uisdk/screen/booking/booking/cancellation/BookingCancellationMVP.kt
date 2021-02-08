package com.karhoo.uisdk.screen.booking.booking.cancellation

interface BookingCancellationMVP {
    interface View {
        fun cancelTrip()

        fun showCancellationFee(formattedPrice: String)

        fun showCancellationError()
    }

    interface Presenter {
        fun handleCancellationRequest()

        fun getCancellationFee(tripId: String)
    }
}