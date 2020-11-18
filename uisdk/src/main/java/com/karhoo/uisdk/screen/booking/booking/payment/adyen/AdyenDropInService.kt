package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.redirect.RedirectComponent
import org.json.JSONObject

class AdyenDropInService : DropInService(), AdyenDropInServiceMVP.Service {

    private val presenter = AdyenDropInServicePresenter(this)

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        clearTransactionId()
        presenter.getAdyenPayments(paymentComponentData, RedirectComponent.getReturnUrl(this))
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        val transactionId = this.getSharedPreferences(TRANSACTION_ID, MODE_PRIVATE)
                .getString(TRANSACTION_ID, "")
        presenter.getAdyenPaymentDetails(actionComponentData, transactionId)
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    override fun storeTransactionId(transactionId: String) {
        val sharedPref = this.getSharedPreferences(TRANSACTION_ID, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(TRANSACTION_ID, transactionId)
            commit()
        }
    }

    override fun clearTransactionId() {
        this.getSharedPreferences(TRANSACTION_ID, MODE_PRIVATE).edit().clear().commit()
    }

    override fun handleResult(callResult: CallResult) {
        asyncCallback(callResult)
    }

    companion object {
        const val TRANSACTION_ID = "transaction_id"
    }
}
