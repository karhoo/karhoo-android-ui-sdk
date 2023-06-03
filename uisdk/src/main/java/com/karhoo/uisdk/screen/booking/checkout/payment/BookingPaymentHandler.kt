package com.karhoo.uisdk.screen.booking.checkout.payment

import android.content.Context
import android.content.Intent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.featureFlags.KarhooFeatureFlagProvider

class BookingPaymentHandler @JvmOverloads constructor(
    private val userStore: UserStore = KarhooApi.userStore,
    private val paymentsService: PaymentsService = KarhooApi.paymentsService,
    val context: Context,
) : BookingPaymentContract.PaymentHandler,
    PaymentDropInContract.Actions {

    var paymentActions: BookingPaymentContract.PaymentActions? = null
    var cardActions: BookingPaymentContract.PaymentViewActions? = null
    private var dropInView: PaymentDropInContract.View? = null

    private var hasValidPayment = false

    override fun getPaymentProvider() {
        if (userStore.paymentProvider == null) {
            paymentsService.getPaymentProvider().execute { result ->
                when (result) {
                    is Resource.Success -> {
                        retrieveLoyaltyStatus()
                        bindDropInView()
                    }
                    is Resource.Failure -> showError(
                        R.string.kh_uisdk_something_went_wrong,
                        result.error
                    )
                }
            }
        } else {
            retrieveLoyaltyStatus()
            bindDropInView()
        }
    }

    override fun setPassengerDetails(passengerDetails: PassengerDetails?) {
        dropInView?.setPassenger(passengerDetails)
    }

    override fun bindDropInView() {
        val view = KarhooUISDKConfigurationProvider.configuration.paymentManager.paymentProviderView
        view?.actions = this
        setPaymentView(view = view)
        bindPaymentDetails(KarhooApi.userStore.savedPaymentInfo)
    }

    override fun setPaymentView(view: PaymentDropInContract.View?) {
        dropInView = view
    }

    fun changeCard() {
        cardActions?.handleChangeCard()
    }

    override fun initialisePaymentFlow(quote: Quote?) {
        dropInView?.initialisePaymentFlow(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        dropInView?.initialiseGuestPayment(quote)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return dropInView?.onActivityResult(requestCode, resultCode, data) == true
    }

    override fun showError(error: Int, karhooError: KarhooError?) {
        paymentActions?.showPaymentFailureDialog(null, karhooError)
    }

    override fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        paymentActions?.handlePaymentDetailsUpdate()
    }

    override fun showPaymentUI(sdkToken: String, paymentData: String?, quote: Quote?) {
        dropInView?.showPaymentDropInUI(
            context = context, sdkToken = sdkToken, paymentData =
            paymentData, quote = quote
        )
    }

    override fun showPaymentFailureDialog(error: KarhooError?) {
        paymentActions?.showPaymentFailureDialog(null, error)
    }

    override fun showLoadingButton(loading: Boolean) {
        paymentActions?.showLoadingButton(loading)
    }

    override fun updatePaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        bindPaymentDetails(savedPaymentInfo)
    }

    override fun handlePaymentDetailsUpdate() {
        paymentActions?.handlePaymentDetailsUpdate()
    }

    override fun initialiseChangeCard(quote: Quote?) {
        dropInView?.initialiseChangeCard(quote, context?.resources?.configuration?.locale)
    }

    override fun threeDSecureNonce(threeDSNonce: String, tripId: String?) {
        paymentActions?.threeDSecureNonce(threeDSNonce, tripId)
    }

    override fun threeDSecureNonce(sdkToken: String, nonce: String, amount: String) {
        dropInView?.handleThreeDSecure(context, sdkToken, nonce, amount)
    }

    override fun retrieveLoyaltyStatus() {
        val loyaltyEnabled = KarhooFeatureFlagProvider(context).get().loyaltyEnabled
        if(loyaltyEnabled && !userStore.paymentProvider?.loyalty?.id.isNullOrEmpty())
            paymentActions?.retrieveLoyaltyStatus()
    }
}
