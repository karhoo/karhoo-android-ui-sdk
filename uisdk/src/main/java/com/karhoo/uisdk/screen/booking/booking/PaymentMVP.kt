package com.karhoo.uisdk.screen.booking.booking

import android.content.Intent
import androidx.annotation.StringRes

interface PaymentMVP {

    interface View {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showError(@StringRes error: Int)

    }

    interface Presenter {

        fun changeCard()

        fun passBackNonce(braintreeSDKNonce: String)

        fun updateCardDetails(description: String, typeLabel: String)

    }

    interface Actions {

        fun showPaymentUI(braintreeSDKToken: String)

        fun showErrorDialog(@StringRes stringId: Int)

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)
    }
}