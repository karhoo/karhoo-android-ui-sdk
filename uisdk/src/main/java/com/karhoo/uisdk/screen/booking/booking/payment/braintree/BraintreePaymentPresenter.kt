package com.karhoo.uisdk.screen.booking.booking.payment.braintree

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserManager
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.network.request.AddPaymentRequest
import com.karhoo.sdk.api.network.request.NonceRequest
import com.karhoo.sdk.api.network.request.Payer
import com.karhoo.sdk.api.network.request.SDKInitRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentDropInMVP
import com.karhoo.uisdk.util.CurrencyUtils
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.extension.isGuest
import com.karhoo.uisdk.util.extension.orZero
import java.util.Currency

class BraintreePaymentPresenter(view: PaymentDropInMVP.Actions,
                                private val userStore: UserStore = KarhooApi.userStore,
                                private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<PaymentDropInMVP.Actions>(), PaymentDropInMVP.Presenter, UserManager.OnUserPaymentChangedListener {

    private var braintreeSDKToken: String? = null
    private var nonce: String? = null

    init {
        attachView(view)
        userStore.addSavedPaymentObserver(this)
    }

    private fun getSDKInitRequest(currencyCode: String): SDKInitRequest {
        val organisationId = KarhooUISDKConfigurationProvider.getGuestOrganisationId()?.let { it }
                ?: userStore.currentUser.organisations.first().id
        return SDKInitRequest(organisationId = organisationId,
                              currency = currencyCode)
    }

    override fun getDropInConfig(context: Context, sdkToken: String): Any {
        return DropInRequest().clientToken(sdkToken)
    }

    private fun getNonce(braintreeSDKToken: String, amount: String) {
        this.braintreeSDKToken = braintreeSDKToken
        val user = userStore.currentUser
        val nonceRequest = NonceRequest(payer = Payer(id = user.userId,
                                                      email = user.email,
                                                      firstName = user.firstName,
                                                      lastName = user.lastName),
                                        organisationId = user.organisations.first().id
                                       )
        paymentsService.getNonce(nonceRequest).execute { result ->
            when (result) {
                is Resource.Success -> passBackThreeDSecureNonce(result.data.nonce, amount)
                is Resource.Failure -> view?.showPaymentDialog(braintreeSDKToken, result.error)
            }
        }
    }

    override fun getPaymentNonce(price: QuotePrice?) {
        val sdkInitRequest = getSDKInitRequest(price?.currencyCode.orEmpty())
        paymentsService.initialisePaymentSDK(sdkInitRequest).execute { result ->
            when (result) {
                is Resource.Success -> getNonce(result.data.token, quotePriceToAmount(price))
                is Resource.Failure -> view?.showError(R.string.something_went_wrong, result.error)
                //TODO Consider using returnErrorStringOrLogoutIfRequired
            }
        }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                BraintreePaymentView.REQ_CODE_BRAINTREE -> {
                    val braintreeResult = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    setNonce(braintreeResult?.paymentMethodNonce?.nonce.orEmpty())
                }
                BraintreePaymentView.REQ_CODE_BRAINTREE_GUEST -> {
                    val braintreeResult = data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    braintreeResult?.paymentMethodNonce?.let {
                        this.nonce = it.nonce
                        updateCardDetails(cardNumber = it.description,
                                          cardTypeLabel = it.typeLabel)
                    }
                }
            }
        } else if (requestCode == BraintreePaymentView.REQ_CODE_BRAINTREE || requestCode == BraintreePaymentView.REQ_CODE_BRAINTREE_GUEST) {
            view?.refresh()
        }
    }

    private fun handleChangeCardSuccess(braintreeSDKToken: String) {
        this.braintreeSDKToken = braintreeSDKToken
        if (KarhooUISDKConfigurationProvider.simulatePaymentProvider()) {
            if (isGuest()) {
                userStore.savedPaymentInfo?.let {
                    updateCardDetails(it.lastFour, it.cardType.toString().toLowerCase().capitalize())
                }
            } else {
                setNonce(braintreeSDKToken)
            }
        } else {
            view?.showPaymentUI(braintreeSDKToken)
        }
    }

    override fun initialiseGuestPayment(price: QuotePrice?) {
        if (KarhooUISDKConfigurationProvider.simulatePaymentProvider()) {
            view?.threeDSecureNonce(braintreeSDKToken.orEmpty())
        } else {
            view?.threeDSecureNonce(braintreeSDKToken.orEmpty(), nonce.orEmpty(), quotePriceToAmount(price))
        }
    }

    override fun onSavedPaymentInfoChanged(userPaymentInfo: SavedPaymentInfo?) {
        view?.updatePaymentDetails(savedPaymentInfo = userPaymentInfo)
        view?.handlePaymentDetailsUpdate()
    }

    private fun setNonce(braintreeSDKNonce: String) {
        val user = userStore.currentUser
        val addPaymentRequest = AddPaymentRequest(payer = Payer(id = user.userId,
                                                                email = user.email,
                                                                firstName = user.firstName,
                                                                lastName = user.lastName),
                                                  organisationId = user.organisations.first().id,
                                                  nonce = braintreeSDKNonce)

        paymentsService.addPaymentMethod(addPaymentRequest).execute { result ->
            when (result) {
                is Resource.Success -> {
                    //TODO Check if this is already handled by the onSavedPayment method
                    view?.updatePaymentDetails(userStore.savedPaymentInfo)
                    view?.handlePaymentDetailsUpdate()
                }
                is Resource.Failure -> view?.showError(R.string.something_went_wrong, result.error)
                //TODO Consider using returnErrorStringOrLogoutIfRequired
            }
        }
    }

    private fun passBackThreeDSecureNonce(nonce: String, amount: String) {
        if (KarhooUISDKConfigurationProvider.simulatePaymentProvider()) {
            view?.threeDSecureNonce(braintreeSDKToken.orEmpty())
        } else {
            view?.threeDSecureNonce(braintreeSDKToken.orEmpty(), nonce, amount)
        }
    }

    private fun quotePriceToAmount(price: QuotePrice?): String {
        val currency = Currency.getInstance(price?.currencyCode?.trim())
        return CurrencyUtils.intToPriceNoSymbol(currency, price?.highPrice.orZero())
    }

    override fun sdkInit(price: QuotePrice?) {
        //currency is temporarily hardcoded to DEFAULT_CURRENCY as it isn't used by the backend to fix DROID-1536. Also hardcoded to DEFAULT_CURRENCY in the iOS code.
        val sdkInitRequest = getSDKInitRequest(DEFAULT_CURRENCY)
        paymentsService.initialisePaymentSDK(sdkInitRequest).execute { result ->
            when (result) {
                is Resource.Success -> handleChangeCardSuccess(result.data.token)
                is Resource.Failure -> view?.showError(R.string.something_went_wrong, result.error)
                //TODO Consider using returnErrorStringOrLogoutIfRequired
            }
        }
    }

    fun updateCardDetails(cardNumber: String?, cardTypeLabel: String?) {
        if (cardNumber != null && cardTypeLabel != null) {
            val userInfo = SavedPaymentInfo(cardNumber, CardType.fromString(cardTypeLabel))
            userStore.savedPaymentInfo = userInfo
        }
        view?.refresh()
    }
}
