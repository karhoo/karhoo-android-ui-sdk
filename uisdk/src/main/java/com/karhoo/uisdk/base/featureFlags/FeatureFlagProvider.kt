package com.karhoo.uisdk.base.featureFlags

import android.content.Context
import com.karhoo.uisdk.base.FeatureFlags

interface FeatureFlagProvider {
    fun get(): FeatureFlags
}

class KarhooFeatureFlagProvider(context: Context, private val store: FeatureFlagsStore = KarhooFeatureFlagsStore(context)) : FeatureFlagProvider {

    override fun get(): FeatureFlags {
        return store.get()?.flags ?: FeatureFlags()
    }
}