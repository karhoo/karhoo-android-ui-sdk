package com.karhoo.uisdk.screen.booking.booking.checkout.fragment

interface CheckoutFragmentContract {
    interface LoadingButtonListener {
        fun onLoadingComplete()
        fun showLoading()
    }
}
