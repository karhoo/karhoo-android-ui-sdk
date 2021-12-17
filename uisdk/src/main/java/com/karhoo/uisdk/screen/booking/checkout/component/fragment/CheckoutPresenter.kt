package com.karhoo.uisdk.screen.booking.checkout.component.fragment

import androidx.annotation.VisibleForTesting
import com.karhoo.sdk.api.network.request.PassengerDetails
import java.util.Date

class CheckoutPresenter : CheckoutFragmentContract.Presenter {

    @VisibleForTesting
    internal var passengerDetails: PassengerDetails? = null

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

    override fun getValidMilisSPeriod(validityTimestamp: Long): Long {
        return validityTimestamp - Date().time
    }
}
