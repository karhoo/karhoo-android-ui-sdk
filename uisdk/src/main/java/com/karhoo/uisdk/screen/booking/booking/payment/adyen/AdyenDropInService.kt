package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.util.Log
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.google.gson.Gson
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.adyen.AdyenAmount
import com.karhoo.sdk.api.model.adyen.AdyenPaymentsRequestPayload
import com.karhoo.sdk.api.model.adyen.AdyenStoredPaymentMethod
import com.karhoo.sdk.api.network.request.AdyenPaymentsRequest
import com.karhoo.sdk.api.network.response.Resource
import org.json.JSONObject

class AdyenDropInService : DropInService() {
    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        getPayment(paymentComponentData)
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    private fun getPayment(paymentComponentData: JSONObject) {
        val paymentMethod = paymentComponentData.getJSONObject("paymentMethod").toString()
        val storedPaymentMethod = Gson().fromJson(paymentMethod, AdyenStoredPaymentMethod::class.java)
        val payload = AdyenPaymentsRequestPayload(amount = AdyenAmount(),
                                                  merchantAccount = "",
                                                  storedPaymentMethod = storedPaymentMethod,
                                                  reference = "",
                                                  returnUrl = "")
        val request = AdyenPaymentsRequest(paymentsPayload = payload)

        KarhooApi.paymentsService.getAdyenPayments(request).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        val paymentsString = Gson().toJson(it)
                        Log.d("Adyen", paymentsString)
                        val payments = JSONObject(paymentsString)
                        asyncCallback(handlePaymentRequestResult(payments))
                    }
                }
                is Resource.Failure -> {
                    Log.d("Adyen", result.error.userFriendlyMessage)
                    asyncCallback(CallResult(CallResult.ResultType.ERROR, result.error
                            .userFriendlyMessage))
                }
            }
        }
    }

    // Handling for submitting additional payment details
    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        // See step 4 - Your server should make a /payments/details call containing the `actionComponentData`
        // Create the `CallResult` based on the /payments/details response
        return CallResult(CallResult.ResultType.FINISHED, "Authorised")
    }

    /*override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        return handlePaymentRequestResult(checkoutApiService.initPayment(paymentComponentData, ComponentType.DROPIN.id))
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        return handlePaymentRequestResult(checkoutApiService.submitAdditionalDetails(actionComponentData))
    }*/

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