package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import android.util.Log
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import org.json.JSONObject
import java.lang.Exception
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
        Log.d("Adyen", "showPaymentUI")

        val payments = JSONObject(paymentsString)
        val paymentMethods = PaymentMethodsApiResponse.SERIALIZER.deserialize(payments)

        //TODO remove try/catch when this is working
        try {
            //TODO Get the corretc public key
            val cardConfiguration =
                    CardConfiguration.Builder(context, "<publicKey>")
                            .setShopperLocale(Locale.getDefault())
                            .build()

            val amount = Amount()
            // Optional. In this example, the Pay button will display 10 EUR.
            amount.currency = "EUR"
            amount.value = 1000

            //TODO Set up the resultHandlerIntent correctly
            val resultHandlerIntent = Intent()
            val dropInConfiguration = DropInConfiguration.Builder(context, resultHandlerIntent,
                                                                  AdyenDropInService::class.java)
                    // Optional. Use if you want to display the amount and currency on the Pay button.
                    .setAmount(amount)
                    // When you're ready to accept live payments, change the value to one of our live environments.
                    .setEnvironment(Environment.TEST)
                    // Optional. Use to set the language rendered in Drop-in, overriding the default device language setting. See list of Supported languages at https://github.com/Adyen/adyen-android/tree/master/card-ui-core/src/main/res
                    // Make sure that you have set the locale in the payment method configuration object as well.
                    .setShopperLocale(Locale.getDefault())
                    .addCardConfiguration(cardConfiguration)
                    .build()

            DropIn.startPayment(context, paymentMethods, dropInConfiguration, resultHandlerIntent)
        } catch (ex: Exception) {
            actions?.refresh()
        }
    }
}