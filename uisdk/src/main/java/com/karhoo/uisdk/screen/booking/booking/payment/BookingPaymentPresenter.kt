package com.karhoo.uisdk.screen.booking.booking.payment

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter

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

    override fun setSavedCardDetails() {
        view?.bindPaymentDetails(userStore.savedPaymentInfo)
    }
}