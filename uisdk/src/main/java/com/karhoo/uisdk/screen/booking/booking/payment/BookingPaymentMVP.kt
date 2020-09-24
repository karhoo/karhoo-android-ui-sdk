package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.QuotePrice

interface BookingPaymentMVP {

    interface View {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?, quotePrice: QuotePrice? = null)

        fun handlePaymentDetailsUpdate(sdkNonce: String?)

        fun initialiseChangeCard(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

        fun initialisePaymentFlow(price: QuotePrice?)

        fun showError(@StringRes error: Int)

        fun showPaymentDialog(braintreeSDKToken: String)

        fun showPaymentFailureDialog()

        fun showPaymentUI(sdkToken: String, paymentData: String? = null, price: QuotePrice? = null)

        fun threeDSecureNonce(sdkToken: String, nonce: String, amount: String)

        fun threeDSecureNonce(threeDSNonce: String)

        fun refresh()
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
