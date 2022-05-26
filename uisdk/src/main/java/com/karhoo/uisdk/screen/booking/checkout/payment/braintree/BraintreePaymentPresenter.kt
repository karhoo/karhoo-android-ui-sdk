package com.karhoo.uisdk.screen.booking.checkout.payment.braintree

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
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.AddPaymentRequest
import com.karhoo.sdk.api.network.request.NonceRequest
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.request.Payer
import com.karhoo.sdk.api.network.request.SDKInitRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentDropInContract
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.extension.isGuest
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.intToPriceNoSymbol
import java.util.Currency
import java.util.Locale

class BraintreePaymentPresenter(
    private val userStore: UserStore = KarhooApi.userStore,
    private val paymentsService: PaymentsService = KarhooApi.paymentsService
) : BasePresenter<PaymentDropInContract.Actions>(), PaymentDropInContract.Presenter,
    UserManager.OnUserPaymentChangedListener {

    private var braintreeSDKToken: String? = null
    private var nonce: String? = null
    private var passengerDetails: PassengerDetails? = null
    private var quote: Quote? = null
    override var view: PaymentDropInContract.Actions? = null
        set(value) {
            field = value

            attachView(value)
            userStore.addSavedPaymentObserver(this)
        }

    private fun getSDKInitRequest(currencyCode: String): SDKInitRequest {
        val organisationId = KarhooUISDKConfigurationProvider.getGuestOrganisationId()
            ?: userStore.currentUser.organisations.first().id
        return SDKInitRequest(
            organisationId = organisationId,
            currency = currencyCode
        )
    }

    override fun getDropInConfig(context: Context, sdkToken: String): Any {
        return DropInRequest().clientToken(sdkToken)
    }

    private fun getNonce(braintreeSDKToken: String, amount: String) {
        this.braintreeSDKToken = braintreeSDKToken
        val user = userStore.currentUser
        val nonceRequest = NonceRequest(
            payer = Payer(
                id = user.userId,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName
            ),
            organisationId = user.organisations.first().id
        )
        paymentsService.getNonce(nonceRequest).execute { result ->
            when (result) {
                is Resource.Success -> passBackThreeDSecureNonce(result.data.nonce, amount)
                is Resource.Failure -> view?.showPaymentFailureDialog(result.error)
            }
        }
    }

    override fun getPaymentNonce(quote: Quote?) {
        val sdkInitRequest = getSDKInitRequest(quote?.price?.currencyCode.orEmpty())
        paymentsService.initialisePaymentSDK(sdkInitRequest).execute { result ->
            when (result) {
                is Resource.Success -> getNonce(result.data.token, quotePriceToAmount(quote))
                is Resource.Failure -> view?.showError(R.string.kh_uisdk_something_went_wrong, result.error)
                //TODO Consider using returnErrorStringOrLogoutIfRequired
            }
        }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                BraintreePaymentView.REQ_CODE_BRAINTREE -> {
                    val braintreeResult =
                        data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    setNonce(braintreeResult?.paymentMethodNonce?.nonce.orEmpty())
                }
                BraintreePaymentView.REQ_CODE_BRAINTREE_GUEST -> {
                    val braintreeResult =
                        data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                    braintreeResult?.paymentMethodNonce?.let {
                        this.nonce = it.nonce
                        updateCardDetails(
                            cardNumber = it.description,
                            cardTypeLabel = it.typeLabel
                        )
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
                    updateCardDetails(
                        it.lastFour,
                        it.cardType.toString().toLowerCase(Locale.getDefault()).capitalize()
                    )
                }
            } else {
                setNonce(braintreeSDKToken)
            }
        } else {
            view?.showPaymentUI(braintreeSDKToken)
        }
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        if (KarhooUISDKConfigurationProvider.simulatePaymentProvider()) {
            view?.threeDSecureNonce(braintreeSDKToken.orEmpty())
        } else {
            view?.threeDSecureNonce(
                braintreeSDKToken.orEmpty(),
                nonce.orEmpty(),
                quotePriceToAmount(quote)
            )
        }
    }

    override fun onSavedPaymentInfoChanged(userPaymentInfo: SavedPaymentInfo?) {
        view?.updatePaymentDetails(savedPaymentInfo = userPaymentInfo)
        view?.handlePaymentDetailsUpdate()
    }

    private fun setNonce(braintreeSDKNonce: String) {
        val user = userStore.currentUser
        val addPaymentRequest = AddPaymentRequest(
            payer =
            Payer(
                id = user.userId,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName
            ),
            organisationId = user.organisations.first().id,
            nonce = braintreeSDKNonce
        )
        paymentsService.addPaymentMethod(addPaymentRequest).execute { result ->
            when (result) {
                is Resource.Success -> {
                    //TODO Check if this is already handled by the onSavedPayment method
                    view?.updatePaymentDetails(userStore.savedPaymentInfo)
                    view?.handlePaymentDetailsUpdate()
                }
                is Resource.Failure -> view?.showError(R.string.kh_uisdk_something_went_wrong, result.error)
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

    private fun quotePriceToAmount(quote: Quote?): String {
        val currency = Currency.getInstance(quote?.price?.currencyCode?.trim())
        return currency.intToPriceNoSymbol(quote?.price?.highPrice.orZero())
    }

    override fun sdkInit(quote: Quote?, locale: Locale?) {
        val currency = quote?.price?.currencyCode ?: DEFAULT_CURRENCY
        val sdkInitRequest = getSDKInitRequest(currency)
        paymentsService.initialisePaymentSDK(sdkInitRequest).execute { result ->
            when (result) {
                is Resource.Success -> handleChangeCardSuccess(result.data.token)
                is Resource.Failure -> view?.showError(R.string.kh_uisdk_something_went_wrong, result.error)
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

    override fun setPassenger(passengerDetails: PassengerDetails?) {
        this.passengerDetails = passengerDetails
    }
}
