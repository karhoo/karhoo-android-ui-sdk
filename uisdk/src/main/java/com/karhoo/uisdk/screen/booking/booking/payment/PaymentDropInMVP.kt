package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.QuotePrice

interface PaymentDropInMVP {

    interface View {

        fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount: String)

        fun initialiseChangeCard(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun initialisePaymentFlow(price: QuotePrice?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, price: QuotePrice?)
    }

    interface Presenter {

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun getPaymentNonce(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun sdkInit(price: QuotePrice?)

        fun passBackNonce(sdkNonce: String)

        fun updateCardDetails(nonce: String, description: String? = "", typeLabel: String? = "", paymentData: String? = "")

        fun getDropInConfig(context: Context, sdkToken: String): Any
    }

    interface Actions {

        fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun handlePaymentDetailsUpdate(sdkNonce: String?)

        fun initialiseChangeCard(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun initialisePaymentFlow(price: QuotePrice?)

        fun showError(@StringRes error: Int)

        fun showPaymentDialog(braintreeSDKToken: String)

        fun showPaymentUI(sdkToken: String, paymentData: String? = null, price: QuotePrice? = null)

        fun threeDSecureNonce(sdkToken: String, nonce: String, amount: String)

        fun refresh()

        fun showPaymentFailureDialog()

        fun threeDSecureNonce(threeDSNonce: String)
    }
}
