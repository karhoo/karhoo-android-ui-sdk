package com.karhoo.uisdk.screen.booking.booking.payment

import com.karhoo.sdk.api.KarhooApi

class PaymentFactory {
    companion object {
        fun createPresenter(provider: ProviderType, view: PaymentMVP.View): PaymentMVP.Presenter = when
            (provider) {
            ProviderType.ADYEN -> BraintreePaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
            ProviderType.BRAINTREE -> BraintreePaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
        }
    }
}

enum class ProviderType {
    ADYEN, BRAINTREE
}