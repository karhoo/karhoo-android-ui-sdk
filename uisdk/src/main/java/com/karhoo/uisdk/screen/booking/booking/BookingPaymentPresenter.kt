package com.karhoo.uisdk.screen.booking.booking

import android.util.Log
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter

class BookingPaymentPresenter(view: BookingPaymentMVP.View,
                              private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<BookingPaymentMVP.View>(), BookingPaymentMVP.Presenter {

    init {
        attachView(view)
        getPaymentProvider()
    }

    override fun getPaymentProvider() {
        paymentsService.getPaymentProvider().execute { result ->
            when (result) {
                is Resource.Success -> handleGetPaymentProviderSuccess(result.data.provider.id)
                is Resource.Failure -> view?.showError(R.string.something_went_wrong)
            }
        }
    }
    
    private fun handleGetPaymentProviderSuccess(provider: String) {
        Log.d("PD36", "Payment provider: $provider")
    }
}
