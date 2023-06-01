package com.karhoo.uisdk.base.featureFlags

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karhoo.uisdk.base.FeatureFlagsModel
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class FeatureFlagsService(context: Context, private val currentSdkVersion: String, private val featureFlagsStore: FeatureFlagsStore = KarhooFeatureFlagsStore(context)) {

    fun update() {
        thread {
            val jsonUrl = "https://raw.githubusercontent.com/karhoo/karhoo-android-ui-sdk/master/feature_flag.json"

            val url = URL(jsonUrl)
            val connection = url.openConnection() as HttpsURLConnection
            val inputStream = connection.inputStream

            val data = inputStream.readBytes()
            val jsonString = String(data)

            val type = object : TypeToken<List<FeatureFlagsModel>>() {

            }.type
            val decoder = Gson().fromJson<List<FeatureFlagsModel>>(jsonString, type)

            handleFlagSets(decoder)
        }
    }

    fun handleFlagSets(sets: List<FeatureFlagsModel>) {
        val selectedSet = selectProperSet(currentSdkVersion, sets) ?: return
        storeFeatureFlag(selectedSet)
    }

    private fun selectProperSet(version: String, sets: List<FeatureFlagsModel>): FeatureFlagsModel? {
        val versionStringComparator = VersionStringComparator()
        val sortedSets = sets.sortedWith(FeatureFlagsVersionComparator()).reversed()

        for (set in sortedSets) {
            if ( versionStringComparator.compare(set.version, currentSdkVersion) <= 0) {
                return set
            }


//            if (set.version.toDoubleOrNull() != null && currentSdkVersion.toDoubleOrNull() != null && set.version.toDoubleOrNull()!! <= currentSdkVersion.toDoubleOrNull()!!) {
//                return set
//            }
        }
        return null
    }

    private fun storeFeatureFlag(model: FeatureFlagsModel) {
        featureFlagsStore.save(model)
    }
}