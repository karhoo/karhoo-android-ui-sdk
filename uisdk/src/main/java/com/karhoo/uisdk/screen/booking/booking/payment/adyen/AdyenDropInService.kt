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
        val requestString = createPaymentRequestString(paymentComponentData.getJSONObject("paymentMethod"))
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

    private fun createPaymentRequestString(paymentMethod: JSONObject): String {
        val amount = JSONObject()
        amount.put("currency", "GBP")
        amount.put("value", TEST_VALUE)

        val payload = JSONObject()
        payload.put("paymentMethod", paymentMethod)
        payload.put("channel", "Android")
        payload.put("returnUrl", "http://karhoo.com")
        payload.put("amount", amount)

        val request = JSONObject()
        request.put("payments_payload", payload)
        request.put("return_url_suffix", "/payment")
        return request.toString().replace("\\", "")
    }

    // Handling for submitting additional payment details
    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        // See step 4 - Your server should make a /payments/details call containing the `actionComponentData`
        // Create the `CallResult` based on the /payments/details response
        return CallResult(CallResult.ResultType.FINISHED, "Authorised")
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

    companion object {
        const val TEST_VALUE = 1000
    }
}
