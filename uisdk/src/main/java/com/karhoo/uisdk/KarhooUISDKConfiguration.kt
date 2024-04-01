package com.karhoo.uisdk

import android.graphics.drawable.Drawable
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.api.KarhooSDKConfiguration
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentManager
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterCategory

interface KarhooUISDKConfiguration : KarhooSDKConfiguration, PaymentProviderConfig {
    var paymentManager: PaymentManager

    fun logo(): Drawable?

    fun bookingMetadata(): HashMap<String, String>? = null

    fun isExplicitTermsAndConditionsConsentRequired(): Boolean = false

    fun useAddToCalendarFeature(): Boolean = true

    fun forceDarkMode(): Boolean = false

    fun disablePrebookRides(): Boolean = false

    fun excludedFilterCategories(): List<FilterCategory> = emptyList()

    fun disableCallDriverOrFleetFeature(): Boolean = false
}

internal object KarhooUISDKConfigurationProvider {

    lateinit var configuration: KarhooUISDKConfiguration

    fun setConfig(configuration: KarhooUISDKConfiguration) {
        this.configuration = configuration
        AnalyticsManager.setGuestMode(isGuest())
    }

    fun isGuest(): Boolean {
        return configuration.authenticationMethod() is AuthenticationMethod.Guest
    }

    fun getGuestOrganisationId(): String? {
        return if (isGuest()) (configuration.authenticationMethod() as
                AuthenticationMethod.Guest).organisationId else null
    }

    fun simulatePaymentProvider(): Boolean {
        return configuration.simulatePaymentProvider()
    }

    fun isConfigurationInitialized(): Boolean {
        return this::configuration.isInitialized
    }
}
