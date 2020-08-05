package com.karhoo.karhootraveller.presentation.karhooLabs

import android.content.Context

class FeatureFlag constructor(val context: Context) {

    fun enabled(key: String): Boolean {
        return FeatureFlagStore.getInstance(context).getBoolean(key, true)
    }

    fun updateFlag(key: String, value: Boolean) {
        return FeatureFlagStore.getInstance(context).putBoolean(key, value)
    }
}
