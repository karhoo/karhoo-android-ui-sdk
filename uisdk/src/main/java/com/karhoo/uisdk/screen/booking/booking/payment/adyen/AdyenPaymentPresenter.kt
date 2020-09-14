package com.karhoo.uisdk.screen.booking.booking.payment.adyen

import android.util.Log
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserManager
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentMVP
import com.karhoo.uisdk.util.CurrencyUtils
import com.karhoo.uisdk.util.extension.orZero
import java.util.Currency

class AdyenPaymentPresenter(view: PaymentMVP.View,
                            private val userStore: UserStore = KarhooApi.userStore,
                            private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<PaymentMVP.View>(), PaymentMVP.Presenter, UserManager.OnUserPaymentChangedListener {

    var actions: PaymentDropInMVP.Actions? = null
    private var adyenKey: String = ""
    var price: QuotePrice? = null

    private var sdkToken: String = ""
    private var nonce: String = ""

    init {
        attachView(view)
        userStore.addSavedPaymentObserver(this)
        /*paymentsService.getAdyenPaymentMethods().execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        Log.d("Adyen", it)
                    }
                }
                is Resource.Failure -> Log.d("Adyen", "${result.error.userFriendlyMessage}")
            }
        }
        paymentsService.getAdyenPublicKey().execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        Log.d("Adyen", "Public key $it")
                        adyenKey = it.publicKey
                    }
                }
                is Resource.Failure -> Log.d("Adyen", "${result.error.userFriendlyMessage}")
            }
        }*/
    }

    override fun sdkInit(price: QuotePrice?) {
        this.price = price
        paymentsService.getAdyenPublicKey().execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        Log.d("Adyen", "Public key $it")
                        adyenKey = it.publicKey
                        getPaymentMethods()

                    }
                }
                is Resource.Failure -> Log.d("Adyen", "${result.error.userFriendlyMessage}")
            }
        }
    }

    private fun getPaymentMethods() {
        paymentsService.getAdyenPaymentMethods().execute { result ->
            when (result) {
                is Resource.Success -> {
                    result.data.let {
                        view?.showPaymentUI(adyenKey, it, price)
                    }
                }
                is Resource.Failure -> actions?.showPaymentFailureDialog()
            }
        }
    }

    override fun getPaymentNonce(price: QuotePrice?) {
        //TODO
    }

    private fun getNonce(braintreeSDKToken: String, amount: String) {
        //TODO
    }

    private fun handleChangeCardSuccess(braintreeSDKToken: String) {
        //TODO
    }

    override fun passBackNonce(braintreeSDKNonce: String) {
        //TODO
    }

    override fun onSavedPaymentInfoChanged(userPaymentInfo: SavedPaymentInfo?) {
        view?.bindPaymentDetails(userPaymentInfo)
    }

    override fun updateCardDetails(nonce: String, description: String, typeLabel: String) {
        this.nonce = nonce
        userStore.savedPaymentInfo = SavedPaymentInfo(description, CardType.fromString(typeLabel))
        view?.refresh()
    }

    override fun initialiseGuestPayment(price: QuotePrice?) {
        passBackThreeDSecureNonce(sdkToken, quotePriceToAmount(price))
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
