package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.redirect.RedirectComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AdyenDropInService : DropInService(), AdyenDropInServiceMVP.Service {

    private val presenter = AdyenDropInServicePresenter(this, this)

    override fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        presenter.clearTripId()

        CoroutineScope(Dispatchers.IO).launch {
            presenter.getAdyenPayments(
                paymentComponentJson,
                RedirectComponent.getReturnUrl(this@AdyenDropInService)
            )
        }
    }

    override fun onDetailsCallRequested(
        actionComponentData: ActionComponentData,
        actionComponentJson: JSONObject
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            presenter.getAdyenPaymentDetails(actionComponentJson, presenter.getCachedTripId())
        }
    }

    override fun handleResult(callResult: DropInServiceResult) {
        sendResult(callResult)
    }
}
