package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.fragment

import com.karhoo.sdk.api.network.request.PassengerDetails

class CheckoutPresenter : CheckoutFragmentContract.Presenter {
    private var passengerDetails: PassengerDetails? = null

    override fun savePassenger(passengerDetails: PassengerDetails?) {
        this.passengerDetails = passengerDetails
    }
}
