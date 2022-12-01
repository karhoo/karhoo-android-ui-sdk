package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.googlepay.GooglePayConfiguration
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
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDK.analytics
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewPresenter.Companion.TRIP_ID
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentDropInContract
import com.karhoo.uisdk.screen.booking.checkout.payment.adyen.AdyenDropInServicePresenter.Companion.ADDITIONAL_DATA
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.toNormalizedLocale
import com.karhoo.uisdk.util.intToPriceNoSymbol
import org.json.JSONObject
import java.util.Currency
import java.util.Locale
import java.util.Date

class AdyenPaymentPresenter(
    private val userStore: UserStore = KarhooApi.userStore,
    private val paymentsService: PaymentsService = KarhooApi.paymentsService
) : BasePresenter<PaymentDropInContract.Actions>(), PaymentDropInContract.Presenter,
    UserManager.OnUserPaymentChangedListener {

    private var adyenKey: String = ""
    private var clientKey: String = ""
    private var quote: Quote? = null
    private var tripId: String = ""
    private var passengerDetails: PassengerDetails? = null

    override var view: PaymentDropInContract.Actions? = null
        set(value) {
            field = value

            attachView(view)

            paymentsService.getAdyenClientKey().execute { result ->
                when (result) {
                    is Resource.Success -> {
                        this.clientKey = result.data.clientKey
                        userStore.addSavedPaymentObserver(this)
                    }
                    is Resource.Failure -> {
                        view?.showError(R.string.kh_uisdk_something_went_wrong, result.error)
                    }
                }

            }
        }

    override fun getDropInConfig(context: Context, sdkToken: String): Any {
        val amount = Amount()
        amount.currency = quote?.price?.currencyCode ?: DEFAULT_CURRENCY
        amount.value = quote?.price?.highPrice.orZero()

        val environment = if (KarhooUISDKConfigurationProvider.configuration.environment() is
                    KarhooEnvironment.Production
        ) {
            Environment.LIVE
        } else {
            Environment.TEST
        }

        var version = 51
        userStore.paymentProvider?.provider?.version?.let {
            version = it.substring(1).toInt()
        }
        val key: String = if(version >= 68)
            clientKey
        else
            adyenKey

        val googlePayConfig = GooglePayConfiguration.Builder(context, key)
            .setAmount(amount)
            .setEnvironment(environment)
            .setShopperLocale(Locale.getDefault())
            .build()

        return DropInConfiguration.Builder(context, AdyenDropInService::class.java, key)
            .setAmount(amount)
            .setEnvironment(environment)
            .addGooglePayConfiguration(googlePayConfig)
            .setShopperLocale(Locale.getDefault())
            .addCardConfiguration(createCardConfig(context.applicationContext, key))
            .build()
    }

    override fun getPaymentNonce(quote: Quote?) {
        passBackThreeDSecureNonce(quote)
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (resultCode == AppCompatActivity.RESULT_OK && data == null) {
            view?.showPaymentFailureDialog()
        } else if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val dataString = DropIn.getDropInResultFromIntent(data)

            if (dataString != null) {
                val payload = JSONObject(dataString)

                when (payload.optString(RESULT_CODE, "")) {
                    AdyenPaymentView.AUTHORISED -> {
                        this.tripId = payload.optString(TRIP_ID, "")
                        updateCardDetails(paymentData = payload.optString(ADDITIONAL_DATA, ""))
                        analytics?.cardAuthorisationSuccess(quoteId = quote?.id)
                        return true
                    }
                    else -> {
                        val error = convertToKarhooError(payload)

                        val lastFourDigits: String =
                            JSONObject(payload.optString(ADDITIONAL_DATA, ""))
                                .optString(CARD_SUMMARY, "")

                        logPaymentFailureEvent(
                            payload.optString(REFUSAL_REASON, ""),
                            payload.optInt(REFUSAL_REASON_CODE, 0),
                            lastFourDigits,
                            quoteId = quote?.id
                        )

                        view?.showPaymentFailureDialog(error)
                    }
                }
            } else {
                view?.showPaymentFailureDialog()
            }
        }
        return false
    }

    override fun logPaymentFailureEvent(
        refusalReason: String,
        refusalReasonCode: Int,
        lastFourDigits: String?,
        quoteId: String?
    ) {
        when (refusalReasonCode) {
            11,// 3DS Not Authenticated
            12, // Not enough balance
            14, // Acquirer Fraud
            2 // Refused The transaction was refused.
            -> {
                KarhooUISDK.analytics?.paymentFailed(
                    errorMessage = refusalReason,
                    quoteId = quoteId,
                    lastFourDigits = lastFourDigits ?: userStore.savedPaymentInfo?.lastFour ?: "",
                    date = Date(),
                    amount = quote?.price?.highPrice ?: 0,
                    currency = quote?.price?.currencyCode ?: ""
                )
            }
            else -> {
                KarhooUISDKConfigurationProvider.configuration.paymentManager.paymentProviderView?.javaClass?.simpleName?.let {
                    KarhooUISDK.analytics?.cardAuthorisationFailure(
                        quoteId = quoteId,
                        errorMessage = refusalReason,
                        lastFourDigits = lastFourDigits ?: userStore.savedPaymentInfo?.lastFour ?: "",
                        paymentMethodUsed = it,
                        date = Date(),
                        amount = quote?.price?.highPrice ?: 0,
                        currency = quote?.price?.currencyCode ?: ""
                    )
                }
            }
        }


    }

    private fun convertToKarhooError(payload: JSONObject): KarhooError {
        val result = payload.optString(RESULT_CODE, "")
        val refusalReason = payload.optString(REFUSAL_REASON, "")
        val refusalReasonCode = payload.optString(REFUSAL_REASON_CODE, "")

        return KarhooError.fromCustomError(result,
            refusalReasonCode,
            if (AdyenPaymentErrorCode.getByRefusalCode(refusalReasonCode) == -1) refusalReason else AdyenPaymentErrorCode.getByRefusalCode(refusalReasonCode).toString() ,
        )
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

        var version = 51
        userStore.paymentProvider?.provider?.version?.let {
            version = it.substring(1).toInt()
        }

        if(version < 68) {
            paymentsService.getAdyenPublicKey().execute { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data.let {
                            adyenKey = it.publicKey
                            getPaymentMethods(locale)
                        }
                    }
                    is Resource.Failure -> {
                        logPaymentFailureEvent(
                            result.error.internalMessage,
                            0,
                            quoteId = quote?.id
                        )

                        view?.showError(
                            R.string.kh_uisdk_something_went_wrong,
                            result.error
                        )
                    }
                    //TODO Consider using returnErrorStringOrLogoutIfRequired
                }
            }
        }
        else
            getPaymentMethods(locale)
    }

    override fun setPassenger(passengerDetails: PassengerDetails?) {
        this.passengerDetails = passengerDetails
    }

    private fun createCardConfig(context: Context, publicKey: String): CardConfiguration {
        val environment = if (KarhooUISDKConfigurationProvider.configuration.environment() is
                    KarhooEnvironment.Production
        ) {
            Environment.LIVE
        } else {
            Environment.TEST
        }

        return CardConfiguration.Builder(context, publicKey)
            .setShopperLocale(Locale.getDefault())
            .setHolderNameRequired(true)
            .setEnvironment(environment)
            .setShowStorePaymentField(false)
            .build()
    }

    private fun getPaymentMethods(locale: Locale?) {
        val amount = AdyenAmount(
            quote?.price?.currencyCode ?: DEFAULT_CURRENCY,
            quote?.price?.highPrice.orZero()
        )
        if (KarhooUISDKConfigurationProvider.simulatePaymentProvider()) {
            view?.threeDSecureNonce(tripId, tripId)
        } else {
            let {
                val localizedString: String? = locale?.toNormalizedLocale()
                val request =
                    AdyenPaymentMethodsRequest(amount = amount, shopperLocale = localizedString)
                paymentsService.getAdyenPaymentMethods(request).execute { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data.let {
                                view?.showPaymentUI(this.adyenKey, it, this.quote)
                            }
                        }
                        is Resource.Failure -> {
                            logPaymentFailureEvent(
                                result.error.internalMessage,
                                0,
                                quoteId = quote?.id
                            )

                            view?.showError(
                                R.string.kh_uisdk_something_went_wrong,
                                result.error
                            )
                        }
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
                view?.showError(
                    R.string.kh_uisdk_something_went_wrong,
                    karhooError = KarhooError.FailedToCallMoneyService
                )
                //TODO Consider using returnErrorStringOrLogoutIfRequired
            }
        }
    }

    private fun quotePriceToAmount(quote: Quote?): String {
        val currency = Currency.getInstance(quote?.price?.currencyCode?.trim())
        return currency.intToPriceNoSymbol(quote?.price?.highPrice.orZero())
    }

    private fun updateCardDetails(paymentData: String?) {
        if (!paymentData.isNullOrEmpty()) {
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
