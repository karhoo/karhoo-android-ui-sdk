package com.karhoo.uisdk.screen.booking.checkout.payment

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.network.request.PassengerDetails

interface BookingPaymentContract {

    interface PaymentHandler {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean

        fun bindDropInView()

        fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun showError(@StringRes error: Int, karhooError: KarhooError?)

        fun setPaymentView(view: PaymentDropInContract.View?)

        fun setPassengerDetails(passengerDetails: PassengerDetails?)

        fun retrieveLoyaltyStatus()

        fun getPaymentProvider()
    }

    interface Presenter {

        fun createPaymentView(actions: PaymentDropInContract.Actions)

        fun getPaymentProvider()
    }

    interface PaymentViewActions {

        fun showErrorDialog(@StringRes stringId: Int, karhooError: KarhooError?)

        fun handleChangeCard()
    }

    interface PaymentActions {

        fun handleChangeCard()

        fun showPaymentFailureDialog(stringId: Int?, error: KarhooError?)

        fun handlePaymentDetailsUpdate()

        fun threeDSecureNonce(threeDSNonce: String, tripId: String?)

        fun retrieveLoyaltyStatus()
    }
}
