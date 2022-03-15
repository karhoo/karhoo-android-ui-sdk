package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.base.model.payments.Amount
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropInConfiguration
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserManager
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.adyen.AdyenAmount
import com.karhoo.sdk.api.network.request.AdyenPaymentMethodsRequest
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentDropInContract
import com.karhoo.uisdk.screen.booking.checkout.payment.adyen.AdyenDropInServicePresenter.Companion.TRIP_ID
import com.karhoo.uisdk.screen.booking.checkout.payment.adyen.AdyenPaymentView.Companion.ADDITIONAL_DATA
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.toNormalizedLocale
import com.karhoo.uisdk.util.intToPriceNoSymbol
import org.json.JSONObject
import java.util.Currency
import java.util.Locale

class AdyenPaymentPresenter(view: PaymentDropInContract.Actions,
                            private val userStore: UserStore = KarhooApi.userStore,
                            private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<PaymentDropInContract.Actions>(), PaymentDropInContract.Presenter, UserManager.OnUserPaymentChangedListener {

    private var adyenKey: String = ""
    var quote: Quote? = null
    private var tripId: String = ""
    private var passengerDetails: PassengerDetails? = null
    private var _context: Context? = null

    init {
        attachView(view)
        userStore.addSavedPaymentObserver(this)
    }

    override fun getDropInConfig(context: Context, sdkToken: String): Any {
        val amount = Amount()
        amount.currency = quote?.price?.currencyCode ?: DEFAULT_CURRENCY
        amount.value = quote?.price?.highPrice.orZero()

        val environment = if (KarhooUISDKConfigurationProvider.configuration.environment() ==
                KarhooEnvironment.Production()) Environment.EUROPE else Environment.TEST

        val dropInIntent = Intent(context, AdyenResultActivity::class.java).apply {
            putExtra(AdyenResultActivity.TYPE_KEY, AdyenComponentType.DROPIN.id)
            addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        }

        return DropInConfiguration.Builder(context, dropInIntent,
                                           AdyenDropInService::class.java)
                .setAmount(amount)
                .setEnvironment(environment)
                .setShopperLocale(Locale.getDefault())
                .addCardConfiguration(createCardConfig(context, sdkToken))
                .build()
    }

    override fun getPaymentNonce(quote: Quote?) {
        passBackThreeDSecureNonce(quote)
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data == null) {
            view?.showPaymentFailureDialog()
        } else if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val dataString = data.getStringExtra(AdyenResultActivity.RESULT_KEY) ?: ""
            val payload = JSONObject(dataString)
            when (payload.optString(RESULT_CODE, "")) {
                AdyenPaymentView.AUTHORISED -> {
                    this.tripId = payload.optString(TRIP_ID, "")
                    updateCardDetails(paymentData = payload.optString(ADDITIONAL_DATA, ""))
                }
                else -> {
                    val error = convertToKarhooError(payload)
                    view?.showPaymentFailureDialog(error)
                }
            }
        } else {
            view?.refresh()
        }
    }

    private fun convertToKarhooError(payload: JSONObject): KarhooError {
        val result = payload.optString(RESULT_CODE, "")
        val refusalReason = payload.optString(REFUSAL_REASON, "")
        val refusalReasonCode = payload.optString(REFUSAL_REASON_CODE, "")
        return KarhooError.fromCustomError(result, refusalReasonCode, refusalReason)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        passBackThreeDSecureNonce(quote)
    }

    override fun onSavedPaymentInfoChanged(userPaymentInfo: SavedPaymentInfo?) {
        view?.updatePaymentDetails(savedPaymentInfo = userPaymentInfo)
        view?.handlePaymentDetailsUpdate()
    }

    override fun sdkInit(quote: Quote?, locale: Locale?) {
        this.quote = quote
        paymentsService.getAdyenPublicKey().execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        adyenKey = it.publicKey
                        getPaymentMethods(locale)
                    }
                }
                is Resource.Failure -> view?.showError(R.string.kh_uisdk_something_went_wrong, result.error)
                //TODO Consider using returnErrorStringOrLogoutIfRequired
            }
        }
    }

    override fun setPassenger(passengerDetails: PassengerDetails?) {
        this.passengerDetails = passengerDetails
    }

    private fun createCardConfig(context: Context, publicKey: String): CardConfiguration {
        return CardConfiguration.Builder(context, publicKey)
                .setShopperLocale(Locale.getDefault())
                .setHolderNameRequire(true)
                .setShowStorePaymentField(false)
                .build()
    }

    private fun getPaymentMethods(locale: Locale?) {
        val amount = AdyenAmount(quote?.price?.currencyCode ?: DEFAULT_CURRENCY
                                 , quote?.price?.highPrice.orZero())
        if (KarhooUISDKConfigurationProvider.simulatePaymentProvider()) {
            view?.threeDSecureNonce(tripId, tripId)
        } else {
            let {
                val localizedString: String? = locale?.toNormalizedLocale()
                val request = AdyenPaymentMethodsRequest(amount = amount, shopperLocale = localizedString)
                paymentsService.getAdyenPaymentMethods(request).execute { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data.let {
                                view?.showPaymentUI(this.adyenKey, it, this.quote)
                            }
                        }
                        is Resource.Failure -> view?.showError(R.string.kh_uisdk_something_went_wrong, result.error)
                        //TODO Consider using returnErrorStringOrLogoutIfRequired
                    }
                }
            }
        }
    }

    private fun passBackThreeDSecureNonce(quote: Quote?) {
        val amount = quotePriceToAmount(quote)
        when {
            KarhooUISDKConfigurationProvider.simulatePaymentProvider() -> {
                view?.threeDSecureNonce(tripId, tripId)
            }
            tripId.isNotBlank() -> {
                view?.threeDSecureNonce(tripId, tripId, amount)
            }
            else -> {
                view?.showError(R.string.kh_uisdk_something_went_wrong, karhooError = KarhooError.FailedToCallMoneyService)
                //TODO Consider using returnErrorStringOrLogoutIfRequired
            }
        }
    }

    private fun quotePriceToAmount(quote: Quote?): String {
        val currency = Currency.getInstance(quote?.price?.currencyCode?.trim())
        return currency.intToPriceNoSymbol(quote?.price?.highPrice.orZero())
    }

    private fun updateCardDetails(paymentData: String?) {
        if (paymentData.isNullOrEmpty()) {
            view?.refresh()
        } else {
            val additionalData = JSONObject(paymentData)
            val newCardNumber = additionalData.optString(CARD_SUMMARY, "")
            val type = additionalData.optString(PAYMENT_METHOD, "")
            val savedPaymentInfo = CardType.fromString(type)?.let {
                SavedPaymentInfo(newCardNumber, it)
            } ?: SavedPaymentInfo(newCardNumber, CardType.NOT_SET)
            userStore.savedPaymentInfo = savedPaymentInfo
        }
    }

    companion object {
        const val CARD_SUMMARY = "cardSummary"
        const val PAYMENT_METHOD = "paymentMethod"
        const val RESULT_CODE = "resultCode"
        const val REFUSAL_REASON = "refusalReason"
        const val REFUSAL_REASON_CODE = "refusalReasonCode"
    }
}
