package com.karhoo.uisdk.screen.booking.checkout.payment

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.network.request.PassengerDetails

interface BookingPaymentContract {

    interface View {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun bindDropInView()

        fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun showError(@StringRes error: Int, karhooError: KarhooError?)

        fun setPaymentView(view: PaymentDropInContract.View?)

        fun setViewVisibility(visibility: Int)

        fun updatePaymentViewVisbility(visibility: Int)

        fun setPassengerDetails(passengerDetails: PassengerDetails?)

        fun hasValidPaymentType(): Boolean

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

        fun handleViewVisibility(visibility: Int)
    }

    interface PaymentActions {

        fun handleChangeCard()

        fun showPaymentFailureDialog(stringId: Int?, error: KarhooError?)

        fun handlePaymentDetailsUpdate()

        fun showPaymentDialog(error: KarhooError? = null)

        fun threeDSecureNonce(threeDSNonce: String, tripId: String?)

        fun retrieveLoyaltyStatus()
    }
}
