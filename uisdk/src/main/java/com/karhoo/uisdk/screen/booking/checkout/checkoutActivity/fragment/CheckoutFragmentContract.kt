package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.fragment

interface CheckoutFragmentContract {
    interface LoadingButtonListener {
        fun onLoadingComplete()
        fun showLoading()
    }

    interface TermsListener {
        fun showWebViewOnPress(url: String?)
    }
}
