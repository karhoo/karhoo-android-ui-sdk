package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.util.Log
import com.adyen.checkout.dropin.service.CallResult
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.ANDROID
import org.json.JSONObject

class AdyenDropInServicePresenter(service: AdyenDropInServiceMVP.Service,
                                  private val paymentsService: PaymentsService = KarhooApi.paymentsService) :
        BasePresenter<AdyenDropInServiceMVP.Service>(), AdyenDropInServiceMVP.Presenter {

    init {
        attachView(service)
    }

    override fun getAdyenPayments(paymentComponentData: JSONObject, returnUrl: String) {
        view?.clearTransactionId()
        val requestString = createPaymentRequestString(paymentComponentData, returnUrl)
        paymentsService.getAdyenPayments(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let { result ->
                        //TODO Find a better way to store / pass through the transaction id
                        val transactionId = result.getString(TRANSACTION_ID)
                        view?.storeTransactionId(transactionId)
                        result.optJSONObject(PAYLOAD)?.let { payload ->
                            view?.handleResult(handlePaymentRequestResult(payload))
                        } ?: view?.handleResult(handlePaymentRequestResult(result))
                    }
                }
                is Resource.Failure -> {
                    view?.handleResult(CallResult(CallResult.ResultType.ERROR, result.error
                            .userFriendlyMessage))
                }
            }
        }
    }

    override fun getAdyenPaymentDetails(actionComponentData: JSONObject, transactionId: String?) {

        transactionId?.let {
            val request = JSONObject()
            request.put(TRANSACTION_ID, transactionId)
            request.put(PAYMENTS_PAYLOAD, actionComponentData)

            paymentsService.getAdyenPaymentDetails(request.toString()).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data.let {
                            view?.handleResult(handlePaymentRequestResult(it))
                        }
                    }
                    is Resource.Failure -> {
                        view?.handleResult(CallResult(CallResult.ResultType.ERROR, result.error
                                .userFriendlyMessage))
                    }
                }
            }
        } ?: view?.handleResult(CallResult(CallResult.ResultType.ERROR, "Invalid transactionId"))
    }

    private fun handlePaymentRequestResult(response: JSONObject): CallResult {
        return try {
            if (response.isNull(ACTION)) {
                CallResult(CallResult.ResultType.FINISHED, response.toString())
            } else {
                CallResult(CallResult.ResultType.ACTION, response.getString(ACTION))
            }
        } catch (e: Exception) {
            CallResult(CallResult.ResultType.ERROR, e.toString())
        }
    }

    private fun createPaymentRequestString(paymentComponentData: JSONObject, returnUrl: String):
            String {

        val payload = JSONObject()
        for (name in paymentComponentData.keys()) {
            val obj = paymentComponentData.get(name)
            if (obj !is String) {
                payload.put(name, obj)
            } else if (obj.isNotBlank()) {
                payload.put(name, obj)
            }
        }
        payload.put(RETURN_URL, returnUrl)
        payload.put(CHANNEL, ANDROID)

        val request = JSONObject()
        request.put(PAYMENTS_PAYLOAD, payload)
        request.put(RETURN_URL_SUFFIX, "")

        return request.toString()
    }

    companion object {
        const val ACTION = "action"
        const val CHANNEL = "channel"
        const val PAYLOAD = "payload"
        const val PAYMENTS_PAYLOAD = "payments_payload"
        const val RETURN_URL = "returnUrl"
        const val RETURN_URL_SUFFIX = "return_url_suffix"
        const val TRANSACTION_ID = "transaction_id"
    }
}
