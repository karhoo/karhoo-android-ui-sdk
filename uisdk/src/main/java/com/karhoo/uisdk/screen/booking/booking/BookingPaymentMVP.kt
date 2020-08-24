package com.karhoo.uisdk.screen.booking.booking

import android.content.Intent
import androidx.annotation.StringRes

interface BookingPaymentMVP {

    interface View {

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

        fun showError(@StringRes error: Int)

        fun handleGetPaymentProviderSuccess(provider: String)

    }

    interface Presenter {

        fun getPaymentProvider()

        fun handleGetPaymentProviderSuccess(provider: String)
    }
}