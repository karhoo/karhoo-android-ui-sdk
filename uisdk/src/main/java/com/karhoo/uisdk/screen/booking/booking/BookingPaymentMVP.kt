package com.karhoo.uisdk.screen.booking.booking

import android.content.Intent
import androidx.annotation.StringRes
import com.braintreepayments.api.models.PaymentMethodNonce
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo

interface BookingPaymentMVP {

    interface View {

        fun handlePaymentDetailsUpdate(braintreeSDKNonce: String?)

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showPaymentUI(braintreeSDKToken: String)

        fun showError(@StringRes error: Int)

        fun initialisePaymentFlow(amount: String)

        fun initialiseGuestPayment(amount: String)

    }

    interface Presenter {

        fun getPaymentProvider()
    }
}