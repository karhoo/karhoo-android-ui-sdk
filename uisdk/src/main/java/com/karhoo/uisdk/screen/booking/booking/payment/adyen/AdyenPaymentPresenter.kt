package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.util.Log
import com.karhoo.sdk.api.KarhooApi
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
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentMVP
import com.karhoo.uisdk.util.CurrencyUtils
import com.karhoo.uisdk.util.extension.orZero
import org.json.JSONObject
import java.util.Currency

class AdyenPaymentPresenter(view: PaymentMVP.View,
                            private val userStore: UserStore = KarhooApi.userStore,
                            private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<PaymentMVP.View>(), PaymentMVP.Presenter, UserManager.OnUserPaymentChangedListener {

    private var adyenKey: String = ""
    var price: QuotePrice? = null

    private var sdkToken: String = ""
    private var nonce: String? = ""

    init {
        attachView(view)
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

    private fun getPaymentMethods() {
        val amount = AdyenAmount(price?.currencyCode ?: "GBP", price?.highPrice.orZero())
        let {
            val request = AdyenPaymentMethodsRequest(amount = amount)
            paymentsService.getAdyenPaymentMethods(request).execute { result ->
                when (result) {
                    is Resource.Success -> {
                        result.data.let {
                            view?.showPaymentUI(adyenKey, it, price)
                        }
                    }
                    //TODO Change error message
                    is Resource.Failure -> view?.showError(R.string.payment_issue_message)
                }
            }
        }
    }

    override fun getPaymentNonce(price: QuotePrice?) {
        nonce?.let {
            passBackThreeDSecureNonce(it, quotePriceToAmount(price))
        } ?: view?.showError(R.string.payment_issue_message)
    }

    override fun passBackNonce(sdkNonce: String) {
        this.sdkToken = sdkNonce
    }

    override fun onSavedPaymentInfoChanged(userPaymentInfo: SavedPaymentInfo?) {
        view?.bindPaymentDetails(savedPaymentInfo = userPaymentInfo)
    }

    override fun updateCardDetails(nonce: String, cardNumber: String?, typeLabel: String?,
                                   paymentData: String?) {
        this.nonce = nonce
        paymentData?.let {
            val additionalData = JSONObject(paymentData)
            val cardNumber = additionalData.optString("cardSummary", "")
            val type = additionalData.optString("paymentMethod", "")
            //TODO Use CardType fromString
            val savedPaymentInfo = CardType.fromString(type)?.let {
                SavedPaymentInfo(cardNumber, it)
            } ?: SavedPaymentInfo(cardNumber, CardType.NOT_SET)
            view?.bindPaymentDetails(savedPaymentInfo)
        } ?: view?.refresh()
    }

    override fun initialiseGuestPayment(price: QuotePrice?) {
        passBackThreeDSecureNonce(sdkToken, quotePriceToAmount(price))
    }

    override fun setSavedCardDetails() {
        view?.bindPaymentDetails(userStore.savedPaymentInfo)
    }

    private fun passBackThreeDSecureNonce(nonce: String, amount: String) {
        if (KarhooUISDKConfigurationProvider.handleBraintree()) {
            view?.threeDSecureNonce(sdkToken)
        } else {
            view?.threeDSecureNonce(sdkToken, nonce, amount)
        }
    }

    private fun quotePriceToAmount(price: QuotePrice?): String {
        val currency = Currency.getInstance(price?.currencyCode?.trim())
        return CurrencyUtils.intToPriceNoSymbol(currency, price?.highPrice.orZero())
    }

}
