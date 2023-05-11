package com.karhoo.uisdk.util

import com.karhoo.sdk.api.KarhooEnvironment
import com.karhoo.sdk.api.model.FeatureFlag
import com.karhoo.sdk.api.network.request.FeatureFlagsRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.config.ConfigService
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider

object FeatureFlagsProvider {
    private const val FEATURE_FLAGS_PROD_URL = "https://raw.githubusercontent.com/karhoo/karhoo-android-ui-sdk/master/feature_flag.json"
    private const val FEATURE_FLAGS_DEV_URL = "https://raw.githubusercontent.com/karhoo/karhoo-android-sdk/develop/feature_flag.json"
    private var featureFlags: List<FeatureFlag>? = null
    private lateinit var configService: ConfigService
    const val ADYEN_AVAILABLE = "adyenAvailable"
    const val NEW_RIDE_PLANNING = "newRidePlanningScreen"
    const val FORBIDDEN_PAYMENT_MANAGER = "AdyenPaymentManager"
    const val LOYALTY_ENABLED = "loyaltyEnabled"
    const val LOYALTY_CAN_EARN = "loyaltyCanEarn"
    const val LOYALTY_CAN_BURN = "loyaltyCanBurn"


    fun setup(configService: ConfigService) {
        this.configService = configService
        retrieveFeatureFlags()
    }

    fun retrieveFeatureFlags(callback: ((List<FeatureFlag>?) -> Unit)? = null) {
        if (featureFlags == null) {
            val url = if(KarhooUISDKConfigurationProvider.configuration.environment() is KarhooEnvironment.Production) {
                FEATURE_FLAGS_PROD_URL
            } else {
                FEATURE_FLAGS_PROD_URL
            }

            configService.featureFlags(FeatureFlagsRequest(url)).execute {
                when (it) {
                    is Resource.Success -> {
                        val originalList = ArrayList<FeatureFlag>(it.data)
                        originalList.forEach { flag ->
                            flag.flags = flag.flags.toMutableMap().apply {
                                put(LOYALTY_ENABLED, true)
                                put(LOYALTY_CAN_EARN, true)
                                put(LOYALTY_CAN_BURN, true)
                            }
                        }
                        featureFlags = originalList

                        callback?.invoke(featureFlags)
                    }
                    is Resource.Failure -> {
                        callback?.invoke(null)
                        //will retry at the next quote list loading
                    }
                }
            }
        }
    }

    fun getFeatureFlags(): List<FeatureFlag>? {
        return featureFlags
    }
}
