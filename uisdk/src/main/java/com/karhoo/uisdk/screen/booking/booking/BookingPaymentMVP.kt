package com.karhoo.uisdk.screen.booking.booking

import androidx.annotation.StringRes
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo

interface BookingPaymentMVP {

    interface View {

        fun bindCardDetails(savedPaymentInfo: SavedPaymentInfo?)

        fun showPaymentUI(braintreeSDKToken: String)

        fun showError(@StringRes error: Int)

        fun passBackBraintreeSDKNonce(braintreeSDKNonce: String)

        fun refresh()

    }

    interface Presenter {

        fun changeCard()

        fun passBackBraintreeSDKNonce(braintreeSDKNonce: String)

    }

    interface Actions {

        fun showPaymentUI(braintreeSDKToken: String)

        fun showErrorDialog(@StringRes stringId: Int)

    }
}