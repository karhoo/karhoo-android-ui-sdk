package com.karhoo.uisdk.base.featureFlags

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karhoo.uisdk.base.FeatureFlagsModel

interface FeatureFlagsStore {
    fun save(model: FeatureFlagsModel)
    fun get(): FeatureFlagsModel?
}

class KarhooFeatureFlagsStore(val context: Context) : FeatureFlagsStore {
    private val storeKey = "KarhooUISDKFeatureFlags"
    private val userDefaults: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()
    override fun save(model: FeatureFlagsModel) {
        val encodedModel = gson.toJson(model)
        userDefaults.edit().putString(storeKey, encodedModel).apply()
    }
    override fun get(): FeatureFlagsModel? {
        val encodedModel = userDefaults.getString(storeKey, null) ?: return null
        return gson.fromJson(encodedModel, object : TypeToken<FeatureFlagsModel>() {}.type)
    }
}