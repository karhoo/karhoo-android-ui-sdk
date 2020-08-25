package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import android.util.Log
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP

class AdyenPaymentView : PaymentDropInMVP.View {

    var actions: PaymentDropInMVP.Actions? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //TODO
    }

    override fun showPaymentUI(braintreeSDKToken: String, context: Context) {
        Log.d("Adyen", "showPaymentUI")
        //TODO
    }
}