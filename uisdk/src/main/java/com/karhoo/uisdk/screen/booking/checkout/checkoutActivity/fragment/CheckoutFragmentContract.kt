package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.fragment

import com.karhoo.sdk.api.network.request.PassengerDetails

interface CheckoutFragmentContract {
    interface LoadingButtonListener {
        fun onLoadingComplete()
        fun showLoading()
    }

    interface TermsListener {
        fun showWebViewOnPress(url: String?)
    }

    interface PassengersListener {
        fun onPassengerSelected(passengerDetails: PassengerDetails?)
        fun onPassengerPageVisibilityChanged(visible: Boolean)
    }

    interface Presenter {
        fun savePassenger(passengerDetails: PassengerDetails?)
    }
}
