package com.karhoo.uisdk.screen.booking.booking.payment

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.screen.booking.booking.payment.braintree.BraintreePaymentPresenter
import com.karhoo.uisdk.screen.booking.booking.payment.braintree.BraintreePaymentView

class PaymentFactory {
    companion object {
        fun createPresenter(provider: ProviderType, view: PaymentMVP.View): PaymentMVP.Presenter = when
            (provider) {
            ProviderType.ADYEN -> BraintreePaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
            ProviderType.BRAINTREE -> BraintreePaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
        }

        fun createPaymentView(provider: ProviderType, actions: PaymentDropInMVP.Actions): PaymentDropInMVP.View = when
            (provider) {
            ProviderType.ADYEN -> {
                val view = BraintreePaymentView()
                view.actions = actions
                view
            }
            ProviderType.BRAINTREE -> {
                val view = BraintreePaymentView()
                view.actions = actions
                view
            }
        }
    }
}

enum class ProviderType {
    ADYEN, BRAINTREE
}