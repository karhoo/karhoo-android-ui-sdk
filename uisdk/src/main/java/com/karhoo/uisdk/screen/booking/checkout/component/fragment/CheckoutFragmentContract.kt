package com.karhoo.uisdk.screen.booking.checkout.component.fragment

import com.karhoo.sdk.api.network.request.PassengerDetails

interface CheckoutFragmentContract {
    interface LoadingButtonListener {
        fun onLoadingComplete()
        fun showLoading()
        fun enableButton(enable: Boolean)
    }

    interface WebViewListener {
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
