package com.karhoo.uisdk.screen.booking.booking.payment

import androidx.annotation.StringRes
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.QuotePrice

interface PaymentMVP {

    interface View {

        fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?, quotePrice: QuotePrice? = null)

        fun handlePaymentDetailsUpdate(sdkNonce: String?)

        fun initialiseChangeCard(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun initialisePaymentFlow(price: QuotePrice?)

        fun showError(@StringRes error: Int)

        fun showPaymentDialog(braintreeSDKToken: String)

        fun showPaymentUI(sdkToken: String, paymentData: String? = null, price: QuotePrice? = null)

        fun threeDSecureNonce(sdkToken: String, nonce: String, amount: String)

        fun threeDSecureNonce(threeDSNonce: String)

        fun refresh()
    }

    interface Presenter {

        fun getPaymentNonce(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun setSavedCardDetails()

        fun sdkInit(price: QuotePrice?)

        fun passBackNonce(sdkNonce: String)

        fun updateCardDetails(nonce: String, description: String? = "", typeLabel: String? = "", paymentData: String? = "")

    }

    interface CardActions {

        fun showErrorDialog(@StringRes stringId: Int)

        fun handleChangeCard()
    }

    interface PaymentActions {

        fun handleChangeCard()

        fun showPaymentUI()

        fun showPaymentFailureDialog()

        fun handlePaymentDetailsUpdate(sdkNonce: String?)

        fun showPaymentDialog()

        fun threeDSecureNonce(threeDSNonce: String)
    }
}
