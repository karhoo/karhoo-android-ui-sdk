package com.karhoo.uisdk.screen.booking.checkout.payment

interface PaymentManager {
    var paymentProviderView: PaymentDropInContract.View?
    var shouldClearStoredPaymentMethod: Boolean
    var showSavedPaymentInfo: Boolean
}
