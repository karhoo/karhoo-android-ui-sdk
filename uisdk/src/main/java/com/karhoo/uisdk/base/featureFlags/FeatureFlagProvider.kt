package com.karhoo.uisdk.base.featureFlags

import com.karhoo.uisdk.base.FeatureFlags

interface FeatureFlagProvider {
    fun get(): FeatureFlags
}

class KarhooFeatureFlagProvider(private val store: FeatureFlagsStore = KarhooFeatureFlagsStore()) : FeatureFlagProvider {

    override fun get(): FeatureFlags {
        return store.get()?.flags ?: FeatureFlags()
    }
}