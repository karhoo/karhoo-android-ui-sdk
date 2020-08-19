package com.karhoo.uisdk.screen.booking.booking

import androidx.annotation.StringRes

interface BookingPaymentMVP {

    interface View {

        fun showError(@StringRes error: Int)

    }

    interface Presenter {

        fun getPaymentProvider()
    }
}