package com.karhoo.uisdk.base.featureFlags

import android.content.SharedPreferences
import com.karhoo.uisdk.base.FeatureFlagsModel

interface FeatureFlagsStore {
    fun save(model: FeatureFlagsModel)
    fun get(): FeatureFlagsModel?
}

class KarhooFeatureFlagsStore : FeatureFlagsStore {

    private val storeKey = "KarhooUISDKFeatureFlags"
    private val userDefaults: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())

    override fun save(model: FeatureFlagsModel) {
        val encodedModel = Json.encodeToString(model)
        userDefaults.edit().putString(storeKey, encodedModel).apply()
    }

    override fun get(): FeatureFlagsModel? {
        val encodedModel = userDefaults.getString(storeKey, null) ?: return null
        return Json.decodeFromString(encodedModel)
    }
}