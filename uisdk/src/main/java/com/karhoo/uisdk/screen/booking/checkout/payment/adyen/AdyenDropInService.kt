package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.redirect.RedirectComponent
import org.json.JSONObject

class AdyenDropInService : DropInService(), AdyenDropInServiceMVP.Service {

    private val presenter = AdyenDropInServicePresenter(this, this)

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        presenter.clearTripId()
        presenter.getAdyenPayments(paymentComponentData, RedirectComponent.getReturnUrl(this))
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        presenter.getAdyenPaymentDetails(actionComponentData, presenter.getCachedTripId())
        return CallResult(CallResult.ResultType.WAIT, "")
    }

    override fun handleResult(callResult: CallResult) {
        asyncCallback(callResult)
    }
}
