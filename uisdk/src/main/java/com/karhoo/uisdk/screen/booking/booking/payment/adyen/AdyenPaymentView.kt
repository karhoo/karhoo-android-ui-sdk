package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.util.extension.orZero
import org.json.JSONObject
import java.util.Locale

class AdyenPaymentView : PaymentDropInMVP.View {

    var presenter: PaymentDropInMVP.Presenter? = null
    var actions: PaymentDropInMVP.Actions? = null

    override fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount:
    String) {
        actions?.threeDSecureNonce(sdkToken)
    }

    override fun showPaymentDropInUI(context: Context, sdkToken: String, paymentData: String?, price: QuotePrice?) {
        val payments = JSONObject(paymentData)
        val paymentMethods = PaymentMethodsApiResponse.SERIALIZER.deserialize(payments)

        val dropInConfiguration: DropInConfiguration = presenter?.getDropInConfig(context, sdkToken)
                as DropInConfiguration

        DropIn.startPayment(context, paymentMethods, dropInConfiguration)
    }

    companion object {
        const val ADDITIONAL_DATA = "additionalData"
        const val AUTHORISED = "Authorised"
        const val MERCHANT_REFERENCE = "merchantReference"
        const val RESULT_CODE = "resultCode"
        const val REQ_CODE_ADYEN = DropIn.Companion.DROP_IN_REQUEST_CODE
    }
}

enum class ComponentType(val id: String) {
    DROPIN("drop-in")
}
