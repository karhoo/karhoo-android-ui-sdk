package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import android.content.Intent
import com.karhoo.sdk.api.model.QuotePrice

interface PaymentDropInMVP {

    interface View {

        fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount: String)

        fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, price: QuotePrice?)
    }

    interface Presenter {

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun getPaymentNonce(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun setSavedCardDetails()

        fun sdkInit(price: QuotePrice?)

        fun passBackNonce(sdkNonce: String)

        fun updateCardDetails(nonce: String, description: String? = "", typeLabel: String? = "", paymentData: String? = "")

        fun getDropInConfig(context: Context, sdkToken: String): Any
    }

    interface Actions {

        fun showPaymentFailureDialog()

        fun threeDSecureNonce(threeDSNonce: String)
    }
}
