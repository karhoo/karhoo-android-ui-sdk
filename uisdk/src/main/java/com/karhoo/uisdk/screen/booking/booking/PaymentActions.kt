package com.karhoo.uisdk.screen.booking.booking

import androidx.annotation.StringRes

interface PaymentActions {

    fun showErrorDialog(@StringRes stringId: Int)

    fun showWebView(url: String?)

}