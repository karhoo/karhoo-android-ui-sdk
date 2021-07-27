package com.karhoo.uisdk.screen.booking.booking.bookingcheckout.fragment

interface BookingCheckoutFragmentContract {
    interface LoadingButtonListener {
        fun onLoadingComplete()
        fun showLoading()
    }
}
