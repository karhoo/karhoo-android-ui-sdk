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

        //TODO Get the correct public key from new endpoint when ready
        val cardConfiguration =
                CardConfiguration.Builder(context, "10001|A26AD4BAD8ED5B316C23052C46441D54715497EA812E0B4E76F916C47FA9521093DF2EB92C8CA266D7E884EDD3DD76B19D3CA46E1A4E96521B4E6C226908664145F3D332A63126CAD212B200F027372D059471D07901FDB440A6D36E451228FBEA985A637679A77E605603AFA05E14C50AE51E8194C6C735FCBEF144354E6BF87B8A7E04E4B58F49C7B1EB606905C09B4410D075F3519FB1D89F52CC11B74C1FADE60EC4EB717CE1A9C2DFFD577DF2E16330CCC39C9AB177C7F767491718B72B5E89CF4B90511FEAD15864BE31BD9DDD32FE56FD58514EDC69CAB482D75DBF4221E3F01CF93064A2059F37D4BDBBD73B6C6551F37DFEA0B60DBD781409791BEB")
                        .setShopperLocale(Locale.getDefault())
                        .build()

        val intent = Intent(context, ResultActivity::class.java).apply {
            putExtra(ResultActivity.TYPE_KEY, ComponentType.DROPIN.id)
        }

        val amount = Amount()
        // Optional. In this example, the Pay button will display 10 EUR.
        amount.currency = "GBP"
        amount.value = 1000

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
}

enum class ComponentType(val id: String) {
    DROPIN("drop-in"), IDEAL("ideal"), CARD("scheme")
}