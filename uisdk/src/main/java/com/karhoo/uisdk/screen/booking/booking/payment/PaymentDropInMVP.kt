package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.Quote

interface PaymentDropInMVP {

    interface View {

        fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount: String)

        fun initialiseChangeCard(quote: Quote?)

        fun initialiseGuestPayment(quote: Quote?)

        fun initialisePaymentFlow(quote: Quote?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, quote: Quote?)
    }

    interface Presenter {

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun getPaymentNonce(quote: Quote?)

        fun initialiseGuestPayment(quote: Quote?)

        fun sdkInit(quote: Quote?)

        fun getDropInConfig(context: Context, sdkToken: String): Any
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
