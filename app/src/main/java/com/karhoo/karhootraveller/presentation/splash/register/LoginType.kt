package com.karhoo.karhootraveller.presentation.splash.register

enum class LoginType constructor(val value: String) {
    HEADER("Please select a login type"),
    ADYEN_GUEST("Adyen guest"),
    BRAINTREE_GUEST("Braintree guest"),
    ADYEN_TOKEN("Adyen token"),
    BRAINTREE_TOKEN("Braintree token"),
    USERNAME_PASSWORD("Username/password")
}