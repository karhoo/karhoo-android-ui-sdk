package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.util.Log
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.redirect.RedirectComponent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import org.json.JSONObject

class AdyenDropInService : DropInService() {

    var trans: String? = ""

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        val requestString = createPaymentRequestString(paymentComponentData)
        KarhooApi.paymentsService.getAdyenPayments(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        val response = JSONObject(it)
                        //TODO Find a better way to store / pass through the transaction id
                        val transactionId = response.getString("transaction_id")
                        trans = transactionId
                        Log.d("Adyen", "trans: $trans")
                        val sharedPref = this.getSharedPreferences("transactionId", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("transactionId", transactionId)
                            commit()
                        }

                        Log.d("Adyen", "transactionId 1: $transactionId")
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

    private fun createPaymentRequestString(paymentComponentData: JSONObject): String {
        Log.d("Adyen", paymentComponentData.toString())
        Log.d("Adyen", "trans 2: $trans")
        val payload = JSONObject()
        payload.put("paymentMethod", paymentComponentData.getJSONObject("paymentMethod"))
        payload.put("amount", paymentComponentData.getJSONObject("amount"))
        val returnUrl = RedirectComponent.getReturnUrl(this)
        Log.d("Adyen", "returnUrl: $returnUrl")
        payload.put("returnUrl", RedirectComponent.getReturnUrl(this))
        payload.put("channel", "Android")

        val request = JSONObject()
        request.put("payments_payload", payload)
        request.put("return_url_suffix", "/paymentDetails")

        val requestString= request.toString().replace("\\\\", "").replace("\\", "")
        Log.d("Adyen", "requestString: $requestString")

        return requestString
    }

    // Handling for submitting additional payment details
    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        Log.d("Adyen", "makeDetailsCall")
        Log.d("Adyen", actionComponentData.toString())
        val transactionId = this.getSharedPreferences("transactionId", MODE_PRIVATE)
                .getString("transactionId", "")
        Log.d("Adyen", "transactionId: $transactionId")
        val request = JSONObject()
        request.put("transaction_id", transactionId)
        request.put("payments_payload", actionComponentData)

        val requestString = request.toString().replace("\\\\", "").replace("\\", "")
        Log.d("Adyen", requestString)
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
            val payload = response.getJSONObject("payload")
            if (payload.isNull("action")) {
                CallResult(CallResult.ResultType.FINISHED, response.toString())
            } else {
                val action = payload.getString("action")
                action.replace("\\\\", "").replace("\\", "")
                Log.d("Adyen", "action $action")
                CallResult(CallResult.ResultType.ACTION, action)
            }
        } catch (e: Exception) {
            CallResult(CallResult.ResultType.ERROR, e.toString())
        }
    }
}
