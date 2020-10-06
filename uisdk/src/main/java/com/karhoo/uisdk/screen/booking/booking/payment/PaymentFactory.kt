package com.karhoo.uisdk.screen.booking.booking.payment

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.Provider
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenPaymentPresenter
import com.karhoo.uisdk.screen.booking.booking.payment.adyen.AdyenPaymentView
import com.karhoo.uisdk.screen.booking.booking.payment.braintree.BraintreePaymentPresenter
import com.karhoo.uisdk.screen.booking.booking.payment.braintree.BraintreePaymentView

@Suppress("UtilityClassWithPublicConstructor")
class PaymentFactory {

    //TODO Implemente changes for guest checkout
    companion object {
        /*fun createPresenter(provider: Provider?, view: BookingPaymentMVP.View): PaymentDropInMVP.Presenter? {
            return provider?.let {
                when (enumValueOf<ProviderType>(it.id.toUpperCase())) {
                    ProviderType.ADYEN -> AdyenPaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
                    ProviderType.BRAINTREE -> BraintreePaymentPresenter(view, KarhooApi.userStore, KarhooApi.paymentsService)
                }
            }
        }*/

        fun createPaymentView(provider: Provider?, actions: PaymentDropInMVP.Actions):
                PaymentDropInMVP.View? {
            return provider?.let {
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
        }
    }
}

enum class ProviderType {
    ADYEN, BRAINTREE
}
