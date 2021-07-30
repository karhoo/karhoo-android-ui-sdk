package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import android.content.Context
import android.os.Build
import com.adyen.checkout.dropin.service.CallResult
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.BuildConfig
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.ANDROID
import org.json.JSONObject

class AdyenDropInServicePresenter(context: Context,
                                  service: AdyenDropInServiceMVP.Service,
                                  private val paymentsService: PaymentsService = KarhooApi.paymentsService,
                                  private val repository: AdyenDropInServiceMVP.Repository =
                                          AdyenDropInServiceRepository(context = context)) :
        BasePresenter<AdyenDropInServiceMVP.Service>(), AdyenDropInServiceMVP.Presenter {
    init {
        attachView(service)
    }

    override fun getAdyenPayments(paymentComponentData: JSONObject, returnUrl: String) {
        clearTripId()
        val requestString = createPaymentRequestString(paymentComponentData, returnUrl)
        paymentsService.getAdyenPayments(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let { result ->
                        val tripId = result.getString(TRIP_ID)
                        storeTripId(tripId)
                        result.optJSONObject(PAYLOAD)?.let { payload ->
                            view?.handleResult(handlePaymentRequestResult(payload, tripId))
                        } ?: view?.handleResult(handlePaymentRequestResult(result, tripId))
                    }
                }
                is Resource.Failure -> {
                    view?.handleResult(CallResult(CallResult.ResultType.ERROR, result.error
                            .userFriendlyMessage))
                }
            }
        }
    }

    override fun getAdyenPaymentDetails(actionComponentData: JSONObject, tripId: String?) {

        tripId?.let {
            val request = JSONObject()
            request.put(TRIP_ID, tripId)
            request.put(PAYMENTS_PAYLOAD, actionComponentData)

            paymentsService.getAdyenPaymentDetails(request.toString()).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data.let {
                            view?.handleResult(handlePaymentRequestResult(it, tripId))
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

    override fun clearTripId() {
        repository.clearTripId()
    }

    override fun getCachedTripId(): String? {
        return repository.tripId
    }

    private fun storeTripId(tripId: String) {
        repository.tripId = tripId
    }

    private fun getSupplyPartnerId(): String? {
        return repository.supplyPartnerId
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

        val browserInfo = JSONObject()
        browserInfo.put(USER_AGENT, getUserAgent())
        browserInfo.put(ACCEPT_HEADER, getAcceptHeader())
        payload.put(BROWSER_INFO, browserInfo)

        val request = JSONObject()
        request.put(PAYMENTS_PAYLOAD, payload)
        request.put(SUPPLY_PARTNER_ID, getSupplyPartnerId())

        return request.toString()
    }

    private fun getUserAgent(): String {
        return System.getProperty("http.agent")?.let { agent ->
            "$agent, UISDK  ${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
        } ?: run {
            "${Build.DEVICE} ${Build.MODEL} ${Build.PRODUCT}, UISDK  ${BuildConfig.VERSION_NAME} " +
                    "(${BuildConfig.VERSION_CODE})"
        }
    }

    private fun getAcceptHeader(): String {
        return ACCEPT_HEADER_VALUE
    }

    companion object {
        const val ACTION = "action"
        const val ALLOW_3DS = "allow3DS2"
        const val ALLOW_3DS_TRUE = "true"
        const val ADDITIONAL_DATA = "additionalData"
        const val BROWSER_INFO = "browserInfo"
        const val USER_AGENT = "userAgent"
        const val ACCEPT_HEADER = "acceptHeader"
        const val CHANNEL = "channel"
        const val PAYLOAD = "payload"
        const val PAYMENTS_PAYLOAD = "payments_payload"
        const val SUPPLY_PARTNER_ID = "supply_partner_id"
        const val RETURN_URL = "returnUrl"
        const val TRIP_ID = "trip_id"
        const val ACCEPT_HEADER_VALUE = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
    }
}
