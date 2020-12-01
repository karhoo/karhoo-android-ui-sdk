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
        val tripId = this.getSharedPreferences(TRIP_ID, MODE_PRIVATE)
                .getString(TRIP_ID, "")
        presenter.getAdyenPaymentDetails(actionComponentData, tripId)
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    override fun storeTripId(tripId: String) {
        val sharedPref = this.getSharedPreferences(TRIP_ID, MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(TRIP_ID, tripId)
            commit()
        }
    }

    override fun clearTransactionId() {
        this.getSharedPreferences(TRIP_ID, MODE_PRIVATE).edit().clear().commit()
    }

    override fun handleResult(callResult: CallResult) {
        asyncCallback(callResult)
    }

    companion object {
        const val TRIP_ID = "trip_id"
    }
}
