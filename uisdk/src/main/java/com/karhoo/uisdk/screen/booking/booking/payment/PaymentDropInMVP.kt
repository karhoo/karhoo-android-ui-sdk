package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import android.content.Intent
import com.karhoo.sdk.api.model.QuotePrice

interface PaymentDropInMVP {

    interface View {

        fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount: String)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, price: QuotePrice?)

    }

    interface Actions {

        fun handlePaymentDetailsUpdate(sdkNonce: String?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun passBackNonce(sdkNonce: String)

        fun showPaymentUI(sdkToken: String, paymentData: String?, price: QuotePrice?)

        fun showPaymentFailureDialog()

        fun threeDSecureNonce(threeDSNonce: String)

        fun refresh()

        fun updateCardDetails(nonce: String, cardNumber: String? = "", cardTypeLabel: String? = "", paymentResponseData: String? = "")
    }
}
