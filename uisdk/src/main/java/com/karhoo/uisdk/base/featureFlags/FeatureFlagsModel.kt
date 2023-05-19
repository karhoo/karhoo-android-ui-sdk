package com.karhoo.uisdk.base

data class FeatureFlags(
    var adyenAvailable: Boolean?,
    var newRidePlaningScreen: Boolean?,
) {
    val loyaltyEnabled: Boolean
        get() = true

    val loyaltyCanEarn: Boolean
        get() = loyaltyEnabled && true

    val loyaltyCanBurn: Boolean
        get() = loyaltyEnabled && true
}

data class FeatureFlagsModel(
    var version: String,
    var flags: FeatureFlags
)
