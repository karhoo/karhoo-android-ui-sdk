package com.karhoo.uisdk.screen.booking.booking.payment

import com.braintreepayments.api.dropin.DropInRequest
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
import com.karhoo.uisdk.util.CurrencyUtils
import com.karhoo.uisdk.util.extension.isGuest
import com.karhoo.uisdk.util.extension.orZero
import java.util.Currency

class BraintreePaymentPresenter(view: PaymentMVP.ViewActions,
                                private val userStore: UserStore = KarhooApi.userStore,
                                private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<PaymentMVP.ViewActions>(), PaymentMVP.Presenter, UserManager
.OnUserPaymentChangedListener {

    private var braintreeSDKToken: String = ""
    private var nonce: String = ""

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

    override fun sdkInit() {
        //currency is temporarily hardcoded to GBP as it isn't used by the backend to fix DROID-1536. Also hardcoded to GBP in the iOS code.
        val sdkInitRequest = getSDKInitRequest("GBP")
        paymentsService.initialisePaymentSDK(sdkInitRequest).execute { result ->
            when (result) {
                is Resource.Success -> handleChangeCardSuccess(result.data.token)
                is Resource.Failure -> view?.showError(R.string.something_went_wrong)
            }
        }
    }

    override fun getPaymentNonce(price: QuotePrice?) {
        val sdkInitRequest = getSDKInitRequest(price?.currencyCode.orEmpty())
        paymentsService.initialisePaymentSDK(sdkInitRequest).execute {
            result ->
            when (result) {
                is Resource.Success -> getNonce(result.data.token, quotePriceToAmount(price))
                is Resource.Failure -> view?.showError(R.string.something_went_wrong)
            }
        }
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
                is Resource.Success -> view?.threeDSecureNonce(braintreeSDKToken, result.data
                        .nonce, amount)
                is Resource.Failure -> view?.showPaymentDialog(braintreeSDKToken)
            }
        }
    }

    private fun quotePriceToAmount(price: QuotePrice?): String {
        val currency = Currency.getInstance(price?.currencyCode?.trim())
        return CurrencyUtils.intToPriceNoSymbol(currency, price?.highPrice.orZero())
    }

    private fun handleChangeCardSuccess(braintreeSDKToken: String) {
        this.braintreeSDKToken = braintreeSDKToken
        if (KarhooUISDKConfigurationProvider.handleBraintree()) {
            if (isGuest()) {
                userStore.savedPaymentInfo?.let {
                    updateCardDetails("", it.lastFour, it.cardType.toString().toLowerCase()
                            .capitalize())
                }
                view?.handlePaymentDetailsUpdate(braintreeSDKToken)
            } else {
                passBackNonce(braintreeSDKToken)
            }
        } else {
            val dropInRequest = DropInRequest().clientToken(braintreeSDKToken)
            val requestCode = if (isGuest()) REQ_CODE_BRAINTREE_GUEST else REQ_CODE_BRAINTREE
            view?.showPaymentUI(braintreeSDKToken)
        }
    }

    override fun passBackNonce(braintreeSDKNonce: String) {
        val user = userStore.currentUser
        val addPaymentRequest = AddPaymentRequest(payer = Payer(id = user.userId,
                                                                email = user.email,
                                                                firstName = user.firstName,
                                                                lastName = user.lastName),
                                                  organisationId = user.organisations.first().id,
                                                  nonce = braintreeSDKNonce)

        paymentsService.addPaymentMethod(addPaymentRequest).execute { result ->
            when (result) {
                is Resource.Success -> view?.bindCardDetails(userStore.savedPaymentInfo)
                is Resource.Failure -> view?.showError(R.string.booking_error)
            }
        }
    }

    override fun onSavedPaymentInfoChanged(userPaymentInfo: SavedPaymentInfo?) {
        view?.bindCardDetails(userPaymentInfo)
    }

    override fun updateCardDetails(nonce: String, description: String, typeLabel: String) {
        this.nonce = nonce
        userStore.savedPaymentInfo = SavedPaymentInfo(description, CardType.fromString(typeLabel))
        view?.refresh()
    }

    override fun initialiseGuestPayment(price: QuotePrice?) {
        view?.threeDSecureNonce(braintreeSDKToken, nonce, quotePriceToAmount(price))
    }

    companion object {
        private const val REQ_CODE_BRAINTREE = 301
        private const val REQ_CODE_BRAINTREE_GUEST = 302
    }
}
