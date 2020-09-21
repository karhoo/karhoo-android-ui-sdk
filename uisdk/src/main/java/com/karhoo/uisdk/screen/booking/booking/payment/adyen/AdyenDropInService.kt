package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.util.Log
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.redirect.RedirectComponent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import org.json.JSONObject

class AdyenDropInService : DropInService() {
    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        val requestString = createPaymentRequestString(paymentComponentData)
        KarhooApi.paymentsService.getAdyenPayments(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        val jsonObject = JSONObject(it)
                        asyncCallback(handlePaymentRequestResult(jsonObject))
                    }
                }
                is Resource.Failure -> {
                    asyncCallback(CallResult(CallResult.ResultType.ERROR, result.error
                            .userFriendlyMessage))
                }
            }
        }
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    private fun createPaymentRequestString(paymentComponentData: JSONObject): String {
        Log.d("Adyen", paymentComponentData.toString())
        val request = JSONObject()
        val payload = JSONObject()
        payload.put("paymentMethod", paymentComponentData.getJSONObject("paymentMethod"))
        payload.put("amount", paymentComponentData.getJSONObject("amount"))
        payload.put("returnUrl", RedirectComponent.getReturnUrl(this))
        payload.put("channel", "Android")
        request.put("payments_payload", payload)
        request.put("return_url_suffix", "/paymentDetails")
        return request.toString().replace("\\", "")
    }

    // Handling for submitting additional payment details
    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        Log.d("Adyen", "makeDetailsCall")
        val request = JSONObject()
        request.put("payload", actionComponentData)
        request.put("transaction_id", actionComponentData)

        val requestString = request.toString().replace("\\", "")
        // See step 4 - Your server should make a /payments/details call containing the `actionComponentData`
        // Create the `CallResult` based on the /payments/details response
        KarhooApi.paymentsService.getAdyenPaymentDetails(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
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
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    private fun handlePaymentRequestResult(response: JSONObject): CallResult {
        return try {
            if (response.isNull("action")) {
                CallResult(CallResult.ResultType.FINISHED, response.toString())
            } else {
                CallResult(CallResult.ResultType.ACTION, response.getString("action"))
            }
        } catch (e: Exception) {
            CallResult(CallResult.ResultType.ERROR, e.toString())
        }
    }
}
