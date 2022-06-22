package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentDropInContract
import org.json.JSONObject
import java.util.Locale

class AdyenPaymentView : PaymentDropInContract.View {

    var presenter: PaymentDropInContract.Presenter? = AdyenPaymentPresenter()
    override var actions: PaymentDropInContract.Actions? = null
        set(value) {
            presenter?.view = value
            field = value
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter?.handleActivityResult(requestCode, resultCode, data)
    }

    override fun initialiseChangeCard(quote: Quote?, locale: Locale?) {
        presenter?.sdkInit(quote, locale)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        presenter?.initialiseGuestPayment(quote)
    }

    override fun initialisePaymentFlow(quote: Quote?) {
        presenter?.getPaymentNonce(quote)
    }

    override fun handleThreeDSecure(
        context: Context, sdkToken: String, nonce: String, amount:
        String
    ) {
        actions?.threeDSecureNonce(sdkToken, sdkToken)
    }

    override fun showPaymentDropInUI(
        context: Context,
        sdkToken: String,
        paymentData: String?,
        quote: Quote?
    ) {
        val payments = JSONObject(paymentData)
        val paymentMethods = PaymentMethodsApiResponse.SERIALIZER.deserialize(payments)

        try {
            val dropInConfiguration: DropInConfiguration = presenter?.getDropInConfig(context, sdkToken) as DropInConfiguration

            cacheSupplyPartnerId(context, quote)

            DropIn.startPayment(context as Activity, paymentMethods, dropInConfiguration)
        } catch (e: Exception) {
            actions?.showError(
                R.string.kh_uisdk_something_went_wrong,
                karhooError = KarhooError.FailedToCallMoneyService
            )
        }
    }

    private fun cacheSupplyPartnerId(context: Context, quote: Quote?) {
        val repository = AdyenDropInServiceRepository(context)
        repository.supplyPartnerId = quote?.fleet?.id ?: ""
    }

    override fun setPassenger(passengerDetails: PassengerDetails?) {
        presenter?.setPassenger(passengerDetails)
    }

    companion object {
        const val ADDITIONAL_DATA = "additionalData"
        const val AUTHORISED = "Authorised"
        const val MERCHANT_REFERENCE = "merchantReference"
    }
}

