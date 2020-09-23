package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.redirect.RedirectComponent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import org.json.JSONObject

class AdyenDropInService : DropInService() {

    var transactionId: String? = null

    override fun onCreate() {
        Log.d("Adyen", "onCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d("Adyen", "onDestroy")
        super.onDestroy()
    }

    override fun onHandleWork(intent: Intent) {
        Log.d("Adyen", "onHandleWork")
        super.onHandleWork(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Adyen", "onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("Adyen", "onBind")
        transactionId = intent.getStringExtra("transactionId")
        Log.d("Adyen", "transactionId bind $transactionId")
        Log.d("Adyen", "$transactionId")
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d("Adyen", "transactionId unbind $transactionId")
        intent.putExtra("transactionId", transactionId)
        Log.d("Adyen", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        clearTransactionId()
        val requestString = createPaymentRequestString(paymentComponentData)
        KarhooApi.paymentsService.getAdyenPayments(requestString).execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let { result ->
                        val response = JSONObject(result)
                        //TODO Find a better way to store / pass through the transaction id
                        transactionId = response.getString("transaction_id")
                        Log.d("Adyen", "transactionId set $transactionId")
                        val transactionId = response.getString("transaction_id")
                        val sharedPref = this.getSharedPreferences("transactionId", MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("transactionId", transactionId)
                            commit()
                        }
                        response.optJSONObject("payload")?.let { payload ->
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
        transactionId = this.getSharedPreferences("transactionId", MODE_PRIVATE)
                .getString("transactionId", "")
        Log.d("Adyen", "transactionId 4 $transactionId")
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
                clearTransactionId()
                CallResult(CallResult.ResultType.FINISHED, response.toString())
            } else {
                CallResult(CallResult.ResultType.ACTION, response.getString("action"))
            }
        } catch (e: Exception) {
            clearTransactionId()
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

    private fun clearTransactionId() {
        this.getSharedPreferences("transactionId", MODE_PRIVATE).edit().clear().commit()
    }
}
