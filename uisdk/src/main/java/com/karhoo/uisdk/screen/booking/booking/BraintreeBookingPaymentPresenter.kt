package com.karhoo.uisdk.screen.booking.booking

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserManager
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.network.request.AddPaymentRequest
import com.karhoo.sdk.api.network.request.Payer
import com.karhoo.sdk.api.network.request.SDKInitRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.extension.isGuest

class BraintreeBookingPaymentPresenter(view: BookingPaymentMVP.View,
                                       private val userStore: UserStore = KarhooApi.userStore,
                                       private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<BookingPaymentMVP.View>(), BookingPaymentMVP.Presenter, UserManager.OnUserPaymentChangedListener {

    init {
        attachView(view)
        userStore.addSavedPaymentObserver(this)
    }

    override fun changeCard() {
        //currency is temporarily hardcoded to GBP as it isn't used by the backend to fix DROID-1536. Also hardcoded to GBP in the iOS code.
        val organisationId = KarhooUISDKConfigurationProvider.getGuestOrganisationId()?.let { it }
                ?: userStore.currentUser.organisations.first().id
        val sdkInitRequest = SDKInitRequest(organisationId = organisationId,
                                            currency = "GBP")
        paymentsService.initialisePaymentSDK(sdkInitRequest).execute { result ->
            when (result) {
                is Resource.Success -> handleChangeCardSuccess(result.data.token)
                is Resource.Failure -> view?.showError(R.string.something_went_wrong)
            }
        }
    }

    private fun handleChangeCardSuccess(braintreeSDKToken: String) {
        if (KarhooUISDKConfigurationProvider.handleBraintree()) {
            if (isGuest()) {
                userStore.savedPaymentInfo?.let {
                    updateCardDetails(it.lastFour, it.cardType.toString().toLowerCase().capitalize())
                }
                view?.handlePaymentDetailsUpdate(braintreeSDKToken)
            } else {
                passBackNonce(braintreeSDKToken)
            }
        } else {
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

    override fun updateCardDetails(description: String, typeLabel: String) {
        userStore.savedPaymentInfo = SavedPaymentInfo(description, CardType.fromString(typeLabel))
        view?.refresh()
    }
}
