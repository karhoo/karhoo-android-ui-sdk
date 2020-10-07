package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Intent
import androidx.annotation.StringRes
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.model.QuotePrice

interface BookingPaymentMVP {

    interface View {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun bindDropInView()

        fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun showError(@StringRes error: Int)

        fun setPaymentView(view: PaymentDropInMVP.View?)
    }

    interface Presenter {

        fun createPaymentView(provider: Provider?, actions: PaymentDropInMVP.Actions)

        fun getPaymentProvider()

        fun setSavedCardDetails(savedPaymentInfo: SavedPaymentInfo?)
    }

    interface CardActions {

        fun showErrorDialog(@StringRes stringId: Int)

        fun handleChangeCard()
    }

    interface PaymentActions {

        fun handleChangeCard()

        fun showPaymentUI()

        fun showPaymentFailureDialog()

        fun handlePaymentDetailsUpdate()

        fun showPaymentDialog()

        fun threeDSecureNonce(threeDSNonce: String)
    }
}
