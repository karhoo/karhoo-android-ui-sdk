package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import com.karhoo.sdk.api.KarhooApi

class PaymentFactory {
    companion object {
        /*fun createPresenter(provider: ProviderType, view: PaymentMVP.View): PaymentMVP.Presenter = when
            (provider) {
            ProviderType.ADYEN -> BraintreePaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
            ProviderType.BRAINTREE -> BraintreePaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
        }*/

        fun createPaymentView(provider: ProviderType, context: Context): PaymentMVP.ViewActions = when
            (provider) {
            ProviderType.ADYEN -> BraintreePaymentView(context)
            ProviderType.BRAINTREE -> BraintreePaymentView(context)
        }
    }
}

enum class ProviderType {
    ADYEN, BRAINTREE
}