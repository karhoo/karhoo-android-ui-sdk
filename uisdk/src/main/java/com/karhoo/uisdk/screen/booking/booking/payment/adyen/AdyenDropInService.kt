package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.redirect.RedirectComponent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import org.json.JSONObject

class AdyenDropInService : DropInService() {

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        clearTransactionId()
        val requestString = createPaymentRequestString(paymentComponentData)
        KarhooApi.paymentsService.getAdyenPayments(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let { result ->
                        val response = JSONObject(result)
                        //TODO Find a better way to store / pass through the transaction id
                        val transactionId = response.getString(TRANSACTION_ID)
                        val sharedPref = this.getSharedPreferences(TRANSACTION_ID, MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString(TRANSACTION_ID, transactionId)
                            commit()
                        }
                        response.optJSONObject(PAYLOAD)?.let { payload ->
                            asyncCallback(handlePaymentRequestResult(payload))
                        } ?: asyncCallback(handlePaymentRequestResult(response))
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
        val transactionId = this.getSharedPreferences(TRANSACTION_ID, MODE_PRIVATE)
                .getString(TRANSACTION_ID, "")
        val request = JSONObject()
        request.put(TRANSACTION_ID, transactionId)
        request.put(PAYMENTS_PAYLOAD, actionComponentData)

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
            if (response.isNull(ACTION)) {
                clearTransactionId()
                CallResult(CallResult.ResultType.FINISHED, response.toString())
            } else {
                CallResult(CallResult.ResultType.ACTION, response.getString(ACTION))
            }
        } catch (e: Exception) {
            clearTransactionId()
            CallResult(CallResult.ResultType.ERROR, e.toString())
        }
    }

    private fun createPaymentRequestString(paymentComponentData: JSONObject): String {
        val payload = JSONObject()
        payload.put(PAYMENT_METHOD, paymentComponentData.getJSONObject(PAYMENT_METHOD))
        payload.put(AMOUNT, paymentComponentData.getJSONObject(AMOUNT))
        payload.put(RETURN_URL, RedirectComponent.getReturnUrl(this))
        payload.put(CHANNEL, "Android")

        val request = JSONObject()
        request.put(PAYMENTS_PAYLOAD, payload)
        request.put(RETURN_URL_SUFFIX, "")

        return request.toString()
    }

    private fun clearTransactionId() {
        this.getSharedPreferences(TRANSACTION_ID, MODE_PRIVATE).edit().clear().commit()
    }

    companion object {
        const val ACTION = "action"
        const val AMOUNT = "amount"
        const val CHANNEL = "channel"
        const val PAYLOAD = "payload"
        const val PAYMENT_METHOD = "paymentMethod"
        const val PAYMENTS_PAYLOAD = "payments_payload"
        const val RETURN_URL = "returnUrl"
        const val RETURN_URL_SUFFIX = "return_url_suffix"
        const val TRANSACTION_ID = "transaction_id"
    }
}
