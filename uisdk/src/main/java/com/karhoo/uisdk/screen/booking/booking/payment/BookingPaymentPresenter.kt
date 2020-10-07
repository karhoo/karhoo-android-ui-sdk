package com.karhoo.uisdk.screen.booking.booking.payment

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenPaymentView
import com.karhoo.uisdk.screen.booking.booking.payment.braintree.BraintreePaymentView

class BookingPaymentPresenter(view: BookingPaymentMVP.View,
                              private val userStore: UserStore = KarhooApi.userStore,
                              private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<BookingPaymentMVP.View>(), BookingPaymentMVP.Presenter {

    init {
        attachView(view)
    }

    override fun getPaymentProvider() {
        paymentsService.getPaymentProvider().execute { result ->
            when (result) {
                is Resource.Success -> view?.bindDropInView()
                is Resource.Failure -> view?.showError(R.string.something_went_wrong)
            }
        }
    }

    override fun setSavedCardDetails(savedPaymentInfo: SavedPaymentInfo?) {
        savedPaymentInfo?.let {
            userStore.savedPaymentInfo = it
        }
    }

    override fun createPaymentView(provider: Provider?, actions: PaymentDropInMVP.Actions) {
        val paymentView = provider?.let {
            when (enumValueOf<ProviderType>(it.id.toUpperCase())) {
                ProviderType.ADYEN -> {
                    val view = AdyenPaymentView(actions)
                    view.actions = actions
                    view
                }
                ProviderType.BRAINTREE -> {
                    val view = BraintreePaymentView(actions)
                    view.actions = actions
                    view
                }
            }
        }
        view?.setPaymentView(view = paymentView)
    }
}

enum class ProviderType {
    ADYEN, BRAINTREE
}