package com.karhoo.uisdk.base.featureFlags

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karhoo.uisdk.BuildConfig
import com.karhoo.uisdk.base.FeatureFlagsModel
import java.lang.Exception
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class FeatureFlagsService(
    private val context: Context,
    private val featureFlagsStore: FeatureFlagsStore = KarhooFeatureFlagsStore(
        context
    )
) {

    fun update() {
        try {
            if (!isNetworkAvailable(context))
                return
        }catch (e: Exception){
            e.printStackTrace()
        }

        thread {
            val jsonUrl =
                "https://raw.githubusercontent.com/karhoo/karhoo-android-ui-sdk/master/feature_flag.json"

            try {
                val url = URL(jsonUrl)
                val connection = url.openConnection() as HttpsURLConnection
                val inputStream = connection.inputStream

                val data = inputStream.readBytes()
                val jsonString = String(data)

                val type = object : TypeToken<List<FeatureFlagsModel>>() {

                }.type
                val decoder = Gson().fromJson<List<FeatureFlagsModel>>(jsonString, type)

                handleFlagSets(decoder)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false

        var isOk = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        isOk = true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        isOk = true
                    }

                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        isOk = true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                isOk = true
            }
        }
        return isOk
    }

    fun handleFlagSets(sets: List<FeatureFlagsModel>) {
        val selectedSet = selectProperSet(BuildConfig.VERSION_NAME, sets) ?: return
        storeFeatureFlag(selectedSet)
    }

    private fun selectProperSet(
        version: String,
        sets: List<FeatureFlagsModel>
    ): FeatureFlagsModel? {
        val versionStringComparator = VersionStringComparator()
        val sortedSets = sets.sortedWith(FeatureFlagsVersionComparator()).reversed()

        for (set in sortedSets) {
            if (versionStringComparator.compare(set.version, version) <= 0) {
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
