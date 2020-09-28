package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import com.adyen.checkout.dropin.service.CallResult
import org.json.JSONObject

interface AdyenDropInServiceMVP {
    interface Service {

        fun clearTransactionId()

        fun handleResult(callResult: CallResult)

        fun storeTransactionId(transactionId: String)
    }

    interface Presenter {

        fun getAdyenPayments(paymentComponentData: JSONObject, returnUrl: String)

        fun getAdyenPaymentDetails(actionComponentData: JSONObject, transactionId: String?)
    }
}