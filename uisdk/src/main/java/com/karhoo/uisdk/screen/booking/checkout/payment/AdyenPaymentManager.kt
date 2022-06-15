package com.karhoo.uisdk.screen.booking.checkout.payment

class AdyenPaymentManager: PaymentManager {
    override var paymentProviderView: PaymentDropInContract.View? = null
    override var shouldClearStoredPaymentMethod: Boolean = true
    override var showSavedPaymentInfo: Boolean = true
}
