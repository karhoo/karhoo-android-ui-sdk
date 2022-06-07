package com.karhoo.uisdk.screen.booking.checkout.payment

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.PassengerDetails
import java.util.Locale

interface PaymentDropInContract {

    interface View {
        var actions: Actions?

        fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount: String)

        fun initialiseChangeCard(quote: Quote?, locale: Locale?)

        fun initialiseGuestPayment(quote: Quote?)

        fun initialisePaymentFlow(quote: Quote?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun setPassenger(passengerDetails: PassengerDetails?)

        fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, quote: Quote?)
    }

    interface Presenter {
        var view: Actions?

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun getPaymentNonce(quote: Quote?)

        fun initialiseGuestPayment(quote: Quote?)

        fun setPassenger(passengerDetails: PassengerDetails?)

        fun sdkInit(quote: Quote?, locale: Locale? = null)

        fun getDropInConfig(context: Context, sdkToken: String): Any

        fun logPaymentErrorEvent(refusalReason: String, lastFourDigits: String? = null)
    }

    interface Actions {

        fun updatePaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun handlePaymentDetailsUpdate()

        fun updatePaymentViewVisbility(visibility: Int)

        fun initialiseChangeCard(quote: Quote?)

        fun initialiseGuestPayment(quote: Quote?)

        fun initialisePaymentFlow(quote: Quote?)

        fun showError(@StringRes error: Int, karhooError: KarhooError?)

        fun showPaymentDialog(karhooError: KarhooError?)

        fun showPaymentUI(sdkToken: String, paymentData: String? = null, quote: Quote? = null)

        fun threeDSecureNonce(sdkToken: String, nonce: String, amount: String)

        fun refresh()

        fun showPaymentFailureDialog(error: KarhooError? = null)

        fun threeDSecureNonce(threeDSNonce: String, tripId: String? = null)
    }
}
