package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.redirect.RedirectComponent
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenResultActivity.Companion.RESULT_KEY
import com.karhoo.uisdk.util.extension.orZero
import org.json.JSONObject
import java.util.Locale

class AdyenPaymentView : PaymentDropInMVP.View {

    var actions: PaymentDropInMVP.Actions? = null

    override fun handleThreeDSecure(context: Context, sdkToken: String, nonce: String, amount:
    String) {
        actions?.threeDSecureNonce(sdkToken)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("Adyen", "$requestCode $resultCode")
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val dataString = data.getStringExtra(RESULT_KEY)
            val jsonResponse = JSONObject(dataString)
            val transactionId = jsonResponse.optString("transaction_id", "")
            val payload = jsonResponse.getJSONObject("payload")
            when (payload.optString("resultCode", "")) {
                "Authorised" -> {
                    actions?.updateCardDetails(nonce = transactionId,
                                               paymentResponseData = jsonResponse.optString("payload", ""))
                    actions?.passBackNonce(transactionId)
                }
                else -> actions?.showPaymentFailureDialog()
            }
        } else {
            actions?.showPaymentFailureDialog()
        }
    }

    override fun showPaymentDropInUI(sdkToken: String, paymentsString: String?, price: QuotePrice?, context: Context) {
        //TODO Move config build logic to Presenter
        val payments = JSONObject(paymentsString)
        val paymentMethods = PaymentMethodsApiResponse.SERIALIZER.deserialize(payments)

        val cardConfiguration =
                CardConfiguration.Builder(context, sdkToken)
                        .setShopperLocale(Locale.getDefault())
                        .setHolderNameRequire(true)
                        .build()

        val dropInIntent = Intent(context, AdyenResultActivity::class.java).apply {
            putExtra(AdyenResultActivity.TYPE_KEY, ComponentType.DROPIN.id)
            addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        }

        val amount = Amount()
        amount.currency = price?.currencyCode ?: "GBP"
        amount.value = price?.highPrice.orZero()

        val environment = if (KarhooUISDKConfigurationProvider.configuration.environment() ==
                KarhooEnvironment.Production()) Environment.EUROPE else Environment.TEST

        //TODO Set up for live envs
        val dropInConfiguration = DropInConfiguration.Builder(context, dropInIntent,
                                                              AdyenDropInService::class.java)
                // When you're ready to accept live payments, change the value to one of our live environments.
                .setAmount(amount)
                .setEnvironment(environment)
                // Optional. Use to set the language rendered in Drop-in, overriding the default device language setting. See list of Supported languages at https://github.com/Adyen/adyen-android/tree/master/card-ui-core/src/main/res
                // Make sure that you have set the locale in the payment method configuration object as well.
                .setShopperLocale(Locale.getDefault())
                .addCardConfiguration(cardConfiguration)
                .build()

        DropIn.startPayment(context, paymentMethods, dropInConfiguration)

        val returnUrl = RedirectComponent.getReturnUrl(context)
        Log.d("Adyen", "returnUrl $returnUrl")
    }

    companion object {
        const val REQ_CODE_ADYEN = DropIn.Companion.DROP_IN_REQUEST_CODE
    }
}

enum class ComponentType(val id: String) {
    DROPIN("drop-in")
}
