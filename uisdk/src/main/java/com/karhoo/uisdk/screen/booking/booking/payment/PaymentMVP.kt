package com.karhoo.uisdk.screen.booking.booking.payment

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

        fun bindCardDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun getPaymentNonce(price: QuotePrice?)

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun sdkInit()

        fun showError(@StringRes error: Int)

        fun showPaymentUI(braintreeSDKToken: String)

        fun showPaymentDialog(braintreeSDKToken: String)

        fun threeDSecureNonce(braintreeSDKToken: String, nonce: String, amount: String)

        fun refresh()

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