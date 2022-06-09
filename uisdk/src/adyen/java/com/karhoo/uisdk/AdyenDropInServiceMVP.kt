package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import com.adyen.checkout.dropin.service.DropInServiceResult
import org.json.JSONObject

interface AdyenDropInServiceMVP {

    interface Service {
        fun handleResult(callResult: DropInServiceResult)
    }

    interface Presenter {

        fun getAdyenPayments(paymentComponentData: JSONObject, returnUrl: String)

        fun getAdyenPaymentDetails(actionComponentData: JSONObject, tripId: String?)

        fun clearTripId()

        fun getCachedTripId(): String?
    }

    interface Repository {
        var tripId: String?

        var supplyPartnerId: String?

        fun clearTripId()
    }
}
