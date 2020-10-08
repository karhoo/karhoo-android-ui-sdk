package com.karhoo.uisdk

interface PaymentProviderConfig {
    fun simulatePaymentProvider(): Boolean = false
}
