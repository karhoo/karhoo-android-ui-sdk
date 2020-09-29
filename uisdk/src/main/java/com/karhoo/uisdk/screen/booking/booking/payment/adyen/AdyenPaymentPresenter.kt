package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropInConfiguration
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserManager
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.adyen.AdyenAmount
import com.karhoo.sdk.api.network.request.AdyenPaymentMethodsRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.booking.payment.BookingPaymentMVP
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.util.CurrencyUtils
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.extension.orZero
import org.json.JSONObject
import java.util.Currency
import java.util.Locale

class AdyenPaymentPresenter(view: BookingPaymentMVP.View,
                            private val userStore: UserStore = KarhooApi.userStore,
                            private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<BookingPaymentMVP.View>(), PaymentDropInMVP.Presenter, UserManager.OnUserPaymentChangedListener {

    private var adyenKey: String = ""
    var price: QuotePrice? = null

    private var sdkToken: String = ""
    private var nonce: String? = ""

    init {
        attachView(view)
    }

    override fun getDropInConfig(context: Context, sdkToken: String): Any {
        val cardConfiguration =
                CardConfiguration.Builder(context, sdkToken)
                        .setShopperLocale(Locale.getDefault())
                        .setHolderNameRequire(true)
                        .build()

        val dropInIntent = Intent(context, AdyenResultActivity::class.java).apply {
            putExtra(AdyenResultActivity.TYPE_KEY, AdyenComponentType.DROPIN.id)
            addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        }

        val amount = Amount()
        amount.currency = price?.currencyCode ?: DEFAULT_CURRENCY
        amount.value = price?.highPrice.orZero()

        val environment = if (KarhooUISDKConfigurationProvider.configuration.environment() ==
                KarhooEnvironment.Production()) Environment.EUROPE else Environment.TEST

        return DropInConfiguration.Builder(context, dropInIntent,
                                           AdyenDropInService::class.java)
                .setAmount(amount)
                .setEnvironment(environment)
                .setShopperLocale(Locale.getDefault())
                .addCardConfiguration(cardConfiguration)
                .build()
    }

    private fun getPaymentMethods() {
        val amount = AdyenAmount(price?.currencyCode ?: DEFAULT_CURRENCY, price?.highPrice.orZero())
        let {
            val request = AdyenPaymentMethodsRequest(amount = amount)
            paymentsService.getAdyenPaymentMethods(request).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data.let {
                            view?.showPaymentUI(adyenKey, it, price)
                        }
                    }
                    is Resource.Failure -> view?.showError(R.string.something_went_wrong)
                }
            }
        }
    }

    override fun getPaymentNonce(price: QuotePrice?) {
        nonce?.let {
            passBackThreeDSecureNonce(it, quotePriceToAmount(price))
        } ?: view?.showError(R.string.payment_issue_message)
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val dataString = data.getStringExtra(AdyenResultActivity.RESULT_KEY)
            val payload = JSONObject(dataString)
            when (payload.optString(AdyenPaymentView.RESULT_CODE, "")) {
                AdyenPaymentView.AUTHORISED -> {
                    val transactionId = payload.optString(AdyenPaymentView.MERCHANT_REFERENCE, "")
                    updateCardDetails(nonce = transactionId,
                                      paymentData = payload.optString
                                      (AdyenPaymentView.ADDITIONAL_DATA, null))
                    passBackNonce(transactionId)
                }
                //TODO Need to check if all other result codes should map to failure
                else -> view?.showPaymentFailureDialog()
            }
        } else {
            view?.showPaymentFailureDialog()
        }
    }

    override fun initialiseGuestPayment(price: QuotePrice?) {
        passBackThreeDSecureNonce(sdkToken, quotePriceToAmount(price))
    }

    override fun onSavedPaymentInfoChanged(userPaymentInfo: SavedPaymentInfo?) {
        view?.bindPaymentDetails(savedPaymentInfo = userPaymentInfo)
    }

    override fun passBackNonce(sdkNonce: String) {
        this.sdkToken = sdkNonce
    }

    private fun passBackThreeDSecureNonce(nonce: String, amount: String) {
        view?.threeDSecureNonce(sdkToken, nonce, amount)
    }

    private fun quotePriceToAmount(price: QuotePrice?): String {
        val currency = Currency.getInstance(price?.currencyCode?.trim())
        return CurrencyUtils.intToPriceNoSymbol(currency, price?.highPrice.orZero())
    }

    override fun sdkInit(price: QuotePrice?) {
        this.price = price
        paymentsService.getAdyenPublicKey().execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        adyenKey = it.publicKey
                        getPaymentMethods()
                    }
                }
                //TODO Change error message
                is Resource.Failure -> view?.showError(R.string.payment_issue_message)
            }
        }
    }

    override fun setSavedCardDetails() {
        view?.bindPaymentDetails(userStore.savedPaymentInfo)
    }

    override fun updateCardDetails(nonce: String, cardNumber: String?, typeLabel: String?,
                                   paymentData: String?) {
        this.nonce = nonce
        paymentData?.let {
            val additionalData = JSONObject(paymentData)
            val newCardNumber = additionalData.optString(CARD_SUMMARY, "")
            val type = additionalData.optString(PAYMENT_METHOD, "")
            val savedPaymentInfo = CardType.fromString(type)?.let {
                SavedPaymentInfo(newCardNumber, it)
            } ?: SavedPaymentInfo(newCardNumber, CardType.NOT_SET)
            view?.bindPaymentDetails(savedPaymentInfo)
        } ?: view?.refresh()
    }

    companion object {
        const val CARD_SUMMARY = "cardSummary"
        const val PAYMENT_METHOD = "paymentMethod"
    }
}
