package com.karhoo.uisdk.screen.booking.checkout.payment

class BraintreePaymentManager: PaymentManager {
    override var paymentProviderView: PaymentDropInContract.View? = null

    override var shouldClearStoredPaymentMethod: Boolean = false

    override var showSavedPaymentInfo: Boolean = false
}
