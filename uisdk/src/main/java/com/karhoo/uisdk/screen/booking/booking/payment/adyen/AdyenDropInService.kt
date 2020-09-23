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
                        val response = JSONObject(it)
                        //TODO Find a better way to store / pass through the transaction id
                        val transactionId = response.getString("transaction_id")
                        val sharedPref = this.getSharedPreferences("transactionId", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("transactionId", transactionId)
                            commit()
                        }

                        asyncCallback(handlePaymentRequestResult(response.getJSONObject("payload")))
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

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        val transactionId = this.getSharedPreferences("transactionId", MODE_PRIVATE)
                .getString("transactionId", "")
        val request = JSONObject()
        request.put("transaction_id", transactionId)
        request.put("payments_payload", actionComponentData)

        KarhooApi.paymentsService.getAdyenPaymentDetails(request.toString()).execute { result ->
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

    private fun createPaymentRequestString(paymentComponentData: JSONObject): String {
        val payload = JSONObject()
        payload.put("paymentMethod", paymentComponentData.getJSONObject("paymentMethod"))
        payload.put("amount", paymentComponentData.getJSONObject("amount"))
        payload.put("returnUrl", RedirectComponent.getReturnUrl(this))
        payload.put("channel", "Android")

        val request = JSONObject()
        request.put("payments_payload", payload)
        request.put("return_url_suffix", "")

        return request.toString()
    }
}
