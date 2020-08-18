package com.karhoo.uisdk.screen.booking.booking

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo

interface PaymentMVP {

    interface View {

        fun bindCardDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showError(@StringRes error: Int)

        fun showPaymentDialog(braintreeSDKToken: String)

        fun showPaymentUI(braintreeSDKToken: String)

        fun threeDSecureNonce(braintreeSDKToken: String, nonce: String, amount: String)

        fun refresh()
    }

    interface Presenter {

        fun getPaymentNonce(amount: String)

        fun sdkInit()

        fun passBackNonce(braintreeSDKNonce: String)

        fun updateCardDetails(nonce: String, description: String, typeLabel: String)

        fun initialiseGuestPayment(amount: String)

    }

    interface Actions {

        fun showErrorDialog(@StringRes stringId: Int)

    }
}