package com.karhoo.uisdk.screen.booking.booking.cancellation

interface BookingCancellationMVP {
    interface View {
        fun cancelTrip()

        fun showCancellationFee()

        fun showCancellationError()
    }

    interface Presenter {
        fun handleCancellationRequest()

        fun getCancellationFee()
    }
}