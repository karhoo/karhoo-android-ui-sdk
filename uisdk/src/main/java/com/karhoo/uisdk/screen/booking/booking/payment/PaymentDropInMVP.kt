package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import android.content.Intent

interface PaymentDropInMVP {

    interface View {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentUI(braintreeSDKToken: String, context: Context)

    }

    interface Actions {

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun passBackNonce(braintreeSDKNonce: String)

        fun showPaymentUI(braintreeSDKToken: String)

        fun refresh()

        fun updateCardDetails(nonce: String, description: String, typeLabel: String)
    }
}