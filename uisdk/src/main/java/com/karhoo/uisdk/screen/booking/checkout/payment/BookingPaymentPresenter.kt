package com.karhoo.uisdk.screen.booking.checkout.payment

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter

class BookingPaymentPresenter(
    view: BookingPaymentContract.PaymentHandler,
    private val userStore: UserStore = KarhooApi.userStore,
    private val paymentsService: PaymentsService = KarhooApi.paymentsService
) : BasePresenter<BookingPaymentContract.PaymentHandler>(), BookingPaymentContract.Presenter {

    init {
        attachView(view)
    }

    override fun createPaymentView(actions: PaymentDropInContract.Actions) {
        val view = KarhooUISDKConfigurationProvider.configuration.paymentManager.paymentProviderView
        view?.actions = actions
        this@BookingPaymentPresenter.view?.setPaymentView(view = view)
    }

    override fun getPaymentProvider() {
        if (userStore.paymentProvider == null) {
            paymentsService.getPaymentProvider().execute { result ->
                when (result) {
                    is Resource.Success -> {
                        view?.retrieveLoyaltyStatus()
                        view?.bindDropInView()
                    }
                    is Resource.Failure -> view?.showError(
                        R.string.kh_uisdk_something_went_wrong,
                        result.error
                    )
                }
            }
        } else {
            view?.retrieveLoyaltyStatus()
            view?.bindDropInView()
        }
    }
}
