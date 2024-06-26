package com.karhoo.uisdk.screen.booking.checkout.component.fragment

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails

interface CheckoutFragmentContract {
    interface LoadingButtonListener {
        fun onLoadingComplete()
        fun showLoading()
        fun enableButton(enable: Boolean)
        fun setState(bookButtonState: BookButtonState)
        fun checkState()
    }

    interface WebViewListener {
        fun showWebViewOnPress(url: String?)
    }

    interface PassengersListener {
        fun onPassengerSelected(passengerDetails: PassengerDetails?)
        fun onPassengerPageVisibilityChanged(visible: Boolean)
    }

    interface BookingListener {
        fun onTripBooked(tripInfo: TripInfo?)
        fun onBookingFailed(error: KarhooError?)
        fun startBookingProcess()
    }

    interface Presenter {
        fun savePassenger(passengerDetails: PassengerDetails?)
        fun getBookButtonState(
            isPassengerDetailsVisible: Boolean = false,
            arePassengerDetailsValid: Boolean,
            isTermsCheckBoxValid: Boolean = true
                              ): BookButtonState
        fun getValidMilisPeriod(validityTimestamp: Long): Long
    }
}
