package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.QuotePrice

interface PaymentMVP {

    interface View {

        fun bindCardDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun initialisePaymentFlow(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun showError(@StringRes error: Int)

        fun showPaymentDialog(braintreeSDKToken: String)

        fun showPaymentUI(braintreeSDKToken: String)

        fun threeDSecureNonce(braintreeSDKToken: String, nonce: String, amount: String)

        fun refresh()
    }

    interface Presenter {

        fun getPaymentNonce(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun sdkInit()

        fun passBackNonce(braintreeSDKNonce: String)

        fun updateCardDetails(nonce: String, description: String, typeLabel: String)

    }

    interface ViewActions {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentUI(braintreeSDKToken: String, context: Context)

    }

    interface DropInActions {

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun passBackNonce(braintreeSDKNonce: String)

        fun showPaymentUI(braintreeSDKToken: String)

        fun refresh()

        fun updateCardDetails(nonce: String, description: String, typeLabel: String)
    }

    interface CardActions {

        fun showErrorDialog(@StringRes stringId: Int)

    }

    interface PaymentActions {

        fun showPaymentUI()

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun showPaymentDialog()

        fun threeDSecureNonce(braintreeSDKToken: String, nonce: String, amount: String)
    }
}