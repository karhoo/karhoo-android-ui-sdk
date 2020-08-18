package com.karhoo.uisdk.screen.booking.booking

import android.content.Intent
import androidx.annotation.StringRes
import com.braintreepayments.api.models.PaymentMethodNonce
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo

interface BookingPaymentMVP {

    interface View {

        fun bindCardDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentUI(braintreeSDKToken: String)

        fun showError(@StringRes error: Int)

        fun passBackBraintreeSDKNonce(braintreeSDKNonce: String)

        fun refresh()

    }

    interface Presenter {

        fun getPaymentProvider()

        fun changeCard()

        fun passBackBraintreeSDKNonce(braintreeSDKNonce: String)

        fun updateCardDetails(description: String, typeLabel: String)

    }

    interface Actions {

        fun showPaymentUI(braintreeSDKToken: String)

        fun showErrorDialog(@StringRes stringId: Int)

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)
    }
}