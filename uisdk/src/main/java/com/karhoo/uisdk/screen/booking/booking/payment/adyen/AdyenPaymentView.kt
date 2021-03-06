package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import org.json.JSONObject

class AdyenPaymentView constructor(actions: PaymentDropInMVP.Actions) : PaymentDropInMVP.View {

    var presenter: PaymentDropInMVP.Presenter? = AdyenPaymentPresenter(actions)
    var actions: PaymentDropInMVP.Actions? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.handleActivityResult(requestCode, resultCode, data)
    }

    override fun initialiseChangeCard(quote: Quote?) {
        presenter?.sdkInit(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        presenter?.initialiseGuestPayment(quote)
    }

    override fun initialisePaymentFlow(quote: Quote?) {
        presenter?.getPaymentNonce(quote)
    }

    override fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount:
    String) {
        actions?.threeDSecureNonce(sdkToken, sdkToken)
    }

    override fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, quote: Quote?) {
        val payments = JSONObject(paymentData)
        val paymentMethods = PaymentMethodsApiResponse.SERIALIZER.deserialize(payments)

        val dropInConfiguration: DropInConfiguration = presenter?.getDropInConfig(context, sdkToken)
                as DropInConfiguration

        cacheSupplyPartnerId(context, quote)

        DropIn.startPayment(context, paymentMethods, dropInConfiguration)
    }

    private fun cacheSupplyPartnerId(context: Context, quote: Quote?) {
        val repository = AdyenDropInServiceRepository(context)
        repository.supplyPartnerId = quote?.fleet?.id ?: ""
    }

    companion object {
        const val ADDITIONAL_DATA = "additionalData"
        const val AUTHORISED = "Authorised"
        const val MERCHANT_REFERENCE = "merchantReference"
        const val REQ_CODE_ADYEN = DropIn.Companion.DROP_IN_REQUEST_CODE
    }
}

