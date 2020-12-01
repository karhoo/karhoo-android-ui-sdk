package com.karhoo.uisdk.screen.booking.booking.payment.adyen

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
                        val transactionId = result.getString(TRIP_ID)
                        view?.storeTransactionId(transactionId)
                        result.optJSONObject(PAYLOAD)?.let { payload ->
                            view?.handleResult(handlePaymentRequestResult(payload, transactionId))
                        } ?: view?.handleResult(handlePaymentRequestResult(result, transactionId))
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
            request.put(TRIP_ID, transactionId)
            request.put(PAYMENTS_PAYLOAD, actionComponentData)

            paymentsService.getAdyenPaymentDetails(request.toString()).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data.let {
                            view?.handleResult(handlePaymentRequestResult(it, transactionId))
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

    private fun handlePaymentRequestResult(response: JSONObject, transactionId: String?): CallResult {
        return try {
            if (response.has(ACTION)) {
                CallResult(CallResult.ResultType.ACTION, response.getString(ACTION))
            } else {
                transactionId?.let {
                    response.put(TRIP_ID, transactionId)
                    CallResult(CallResult.ResultType.FINISHED, response.toString())
                } ?: CallResult(CallResult.ResultType.ERROR, "Invalid transaction id")
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

        val additionalData = JSONObject()
        additionalData.put(ALLOW_3DS, ALLOW_3DS_TRUE)
        payload.put(ADDITIONAL_DATA, additionalData)

        val request = JSONObject()
        request.put(PAYMENTS_PAYLOAD, payload)
        request.put(RETURN_URL_SUFFIX, "")

        return request.toString()
    }

    companion object {
        const val ACTION = "action"
        const val ALLOW_3DS = "allow3DS2"
        const val ALLOW_3DS_TRUE = "true"
        const val ADDITIONAL_DATA = "additionalData"
        const val CHANNEL = "channel"
        const val PAYLOAD = "payload"
        const val PAYMENTS_PAYLOAD = "payments_payload"
        const val RETURN_URL = "returnUrl"
        const val RETURN_URL_SUFFIX = "return_url_suffix"
        const val TRIP_ID = AdyenDropInService.TRIP_ID
    }
}
