package com.karhoo.uisdk.featureFlags

import com.karhoo.uisdk.base.FeatureFlagsModel
import com.karhoo.uisdk.base.featureFlags.FeatureFlagsStore

class MockFeatureFlagsStore : FeatureFlagsStore {

    var savedModel: FeatureFlagsModel? = null

    override fun save(model: FeatureFlagsModel) {
        savedModel = model
    }

    override fun get(): FeatureFlagsModel? {
        return savedModel
    }
}
