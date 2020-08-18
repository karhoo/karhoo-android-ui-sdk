package com.karhoo.uisdk.screen.booking.booking

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.model.QuotePrice

interface BookingPaymentMVP {

    interface View {

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentUI(braintreeSDKToken: String)

        fun showError(@StringRes error: Int)

        fun initialisePaymentFlow(price: QuotePrice?)

        fun initialiseGuestPayment(price: QuotePrice?)

    }

    interface Presenter {

        fun getPaymentProvider()
    }
}