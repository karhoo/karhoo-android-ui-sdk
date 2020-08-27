package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import org.json.JSONObject

class AdyenDropInService : DropInService() {
    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        // make /payments call with the component data
        return CallResult(CallResult.ResultType.ACTION, "action JSON object")
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        // make /payments/details call with the component data
        return CallResult(CallResult.ResultType.FINISHED, "Success")
    }
}