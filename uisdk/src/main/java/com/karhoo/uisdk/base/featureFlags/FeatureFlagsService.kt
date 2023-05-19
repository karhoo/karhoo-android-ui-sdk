package com.karhoo.uisdk.base.featureFlags

import com.karhoo.uisdk.base.FeatureFlagsModel
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class FeatureFlagsService(private val currentSdkVersion: String, private val featureFlagsStore: FeatureFlagsStore = KarhooFeatureFlagsStore()) {

    fun update() {
        val jsonUrl = "https://raw.githubusercontent.com/karhoo/karhoo-ios-ui-sdk/master/KarhooUISDK/FeatureFlags/feature_flag.json"

        val url = URL(jsonUrl)
        val connection = url.openConnection() as HttpsURLConnection
        val inputStream = connection.inputStream

        val data = inputStream.readBytes()
        val jsonString = String(data)

        val decoder = Json.decodeFromString<List<FeatureFlagsModel>>(jsonString)

        handleFlagSets(decoder)
    }

    private fun handleFlagSets(sets: List<FeatureFlagsModel>) {
        val selectedSet = selectProperSet(currentSdkVersion, sets) ?: return
        storeFeatureFlag(selectedSet)
    }

    private fun selectProperSet(version: String, sets: List<FeatureFlagsModel>): FeatureFlagsModel? {
        val sortedSets = sets.sortedByDescending { it.version }
        for (set in sortedSets) {
            if (set.version.toDoubleOrNull() != null && set.version.toDoubleOrNull() <= currentSdkVersion.toDoubleOrNull()) {
                return set
            }
        }
        return null
    }

    private fun storeFeatureFlag(model: FeatureFlagsModel) {
        featureFlagsStore.save(model)
    }
}