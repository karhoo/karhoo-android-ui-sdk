package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.karhoo.uisdk.BuildConfig
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import org.json.JSONObject
import java.util.Locale

class AdyenPaymentView : PaymentDropInMVP.View {

    var actions: PaymentDropInMVP.Actions? = null

    override fun handleThreeDSecure(context: Context, braintreeSDKToken: String, nonce: String, amount: String) {
        TODO("Not yet implemented")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //TODO
    }

    override fun showPaymentUI(paymentsString: String, context: Context) {
        val payments = JSONObject(paymentsString)
        val paymentMethods = PaymentMethodsApiResponse.SERIALIZER.deserialize(payments)

        //TODO Get the correct public key from new endpoint when ready
        val cardConfiguration =
                CardConfiguration.Builder(context, BuildConfig.ADYEN_PUBLIC_KEY)
                        .setShopperLocale(Locale.getDefault())
                        .build()

        val intent = Intent(context, ResultActivity::class.java).apply {
            putExtra(ResultActivity.TYPE_KEY, ComponentType.DROPIN.id)
        }

        val amount = Amount()
        // Optional. In this example, the Pay button will display 10 EUR.
        amount.currency = "GBP"
        amount.value = TEST_VALUE

        //TODO Set up for live envs
        val dropInConfiguration = DropInConfiguration.Builder(context, intent,
                                                              AdyenDropInService::class.java)
                // When you're ready to accept live payments, change the value to one of our live environments.
                .setEnvironment(Environment.TEST)
                // Optional. Use to set the language rendered in Drop-in, overriding the default device language setting. See list of Supported languages at https://github.com/Adyen/adyen-android/tree/master/card-ui-core/src/main/res
                // Make sure that you have set the locale in the payment method configuration object as well.
                .setShopperLocale(Locale.getDefault())
                .addCardConfiguration(cardConfiguration)
                .build()

        DropIn.startPayment(context, paymentMethods, dropInConfiguration)
    }

    companion object {
        const val TEST_VALUE = 1000
    }
}

enum class ComponentType(val id: String) {
    DROPIN("drop-in")
}
