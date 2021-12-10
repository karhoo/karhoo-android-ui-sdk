package com.karhoo.uisdk.screen.booking.checkout.payment

import android.view.View.GONE
import android.view.View.VISIBLE
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.checkout.payment.adyen.AdyenPaymentView
import com.karhoo.uisdk.screen.booking.checkout.payment.braintree.BraintreePaymentView
import java.util.Locale

class BookingPaymentPresenter(view: BookingPaymentContract.View,
                              private val userStore: UserStore = KarhooApi.userStore,
                              private val paymentsService: PaymentsService = KarhooApi.paymentsService)
    : BasePresenter<BookingPaymentContract.View>(), BookingPaymentContract.Presenter {

    init {
        attachView(view)
    }

    override fun createPaymentView(actions: PaymentDropInContract.Actions) {
        val paymentView = userStore.paymentProvider?.let {
            when (enumValueOf<ProviderType>(it.provider.id.uppercase(Locale.US))) {
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

    override fun getPaymentViewVisibility() {
        val visibility = if (ProviderType.ADYEN.name.equals(userStore.paymentProvider?.provider?.id,
                                                            ignoreCase = true))
            GONE
        else
            VISIBLE
        view?.setViewVisibility(visibility)
    }

    override fun getPaymentProvider() {
        if (userStore.paymentProvider == null) {
            paymentsService.getPaymentProvider().execute { result ->
                when (result) {
                    is Resource.Success -> {
                        view?.retrieveLoyaltyStatus()
                        view?.bindDropInView()
                    }
                    is Resource.Failure -> view?.showError(R.string.kh_uisdk_something_went_wrong, result.error)
                }
            }
        } else {
            view?.retrieveLoyaltyStatus()
            view?.bindDropInView()
        }
    }
}
