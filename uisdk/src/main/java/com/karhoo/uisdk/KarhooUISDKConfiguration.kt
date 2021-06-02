package com.karhoo.uisdk

import android.graphics.drawable.Drawable
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.api.KarhooSDKConfiguration
import com.karhoo.sdk.api.model.AuthenticationMethod

interface KarhooUISDKConfiguration : KarhooSDKConfiguration, PaymentProviderConfig {

    fun logo(): Drawable?

    fun bookingMetadata(): HashMap<String, String>? = null

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
