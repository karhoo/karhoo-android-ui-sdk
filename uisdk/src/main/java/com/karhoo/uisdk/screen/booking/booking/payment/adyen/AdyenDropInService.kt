package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.util.Log
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import org.json.JSONObject

class AdyenDropInService : DropInService() {
    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        getPayment(paymentComponentData)
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    private fun getPayment(paymentComponentData: JSONObject) {
        val requestString = createPaymentRequestString(paymentComponentData)
        KarhooApi.paymentsService.getAdyenPayments(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        Log.d("Adyen", it)
                        val payments = JSONObject(it).getJSONObject("payload")
                        asyncCallback(handlePaymentRequestResult(payments))
                    }
                }
                is Resource.Failure -> {
                    asyncCallback(CallResult(CallResult.ResultType.ERROR, result.error
                            .userFriendlyMessage))
                }
            }
        }
    }

    private fun createPaymentRequestString(paymentComponentData: JSONObject): String {
        val payload = JSONObject()
        payload.put("paymentMethod", paymentComponentData.getJSONObject("paymentMethod"))
        payload.put("channel", "Android")
        payload.put("returnUrl", "http://karhoo.com")
        payload.put("amount", paymentComponentData.getJSONObject("amount"))

        val request = JSONObject()
        request.put("payments_payload", payload)
        request.put("return_url_suffix", "/payment")
        return request.toString().replace("\\", "")
    }

    // Handling for submitting additional payment details
    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        val payload = JSONObject()
        payload.put("paymentMethod", actionComponentData.getJSONObject("paymentMethod"))
        payload.put("channel", "Android")
        payload.put("returnUrl", "http://karhoo.com")

        val request = JSONObject()
        request.put("payments_payload", payload)
        request.put("return_url_suffix", "/payment")
        val requestString = request.toString().replace("\\", "")
        // See step 4 - Your server should make a /payments/details call containing the `actionComponentData`
        // Create the `CallResult` based on the /payments/details response
        KarhooApi.paymentsService.getAdyenPaymentDetails(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        Log.d("Adyen", it)
                        val response = JSONObject(it)
                        asyncCallback(handlePaymentRequestResult(response))
                    }
                }
                is Resource.Failure -> {
                    asyncCallback(CallResult(CallResult.ResultType.ERROR, result.error
                            .userFriendlyMessage))
                }
            }
        }
        //        return CallResult(CallResult.ResultType.FINISHED, "Authorised")
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    private fun handlePaymentRequestResult(response: JSONObject): CallResult {
        return try {
            if (response.isNull("action")) {
                CallResult(CallResult.ResultType.FINISHED, response.getString("resultCode"))
            } else {
                CallResult(CallResult.ResultType.ACTION, response.getString("action"))
            }
        } catch (e: Exception) {
            CallResult(CallResult.ResultType.ERROR, e.toString())
        }
    }
}
