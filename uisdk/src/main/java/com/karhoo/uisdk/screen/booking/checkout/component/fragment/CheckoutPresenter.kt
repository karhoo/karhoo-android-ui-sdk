package com.karhoo.uisdk.screen.booking.checkout.component.fragment

import com.karhoo.sdk.api.network.request.PassengerDetails

class CheckoutPresenter : CheckoutFragmentContract.Presenter {
    private var passengerDetails: PassengerDetails? = null

    override fun savePassenger(passengerDetails: PassengerDetails?) {
        this.passengerDetails = passengerDetails
    }

    override fun getBookButtonState(
        isPassengerDetailsVisible: Boolean,
        arePassengerDetailsValid: Boolean,
        isPaymentValid: Boolean
                                   ): BookButtonState {
        return if (isPassengerDetailsVisible) {
            BookButtonState.SAVE
        } else if (arePassengerDetailsValid && isPaymentValid) {
            BookButtonState.BOOK
        } else {
            BookButtonState.NEXT
        }
    }
}
