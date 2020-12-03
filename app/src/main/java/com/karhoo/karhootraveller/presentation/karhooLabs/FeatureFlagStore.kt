package com.karhoo.karhootraveller.presentation.karhooLabs

import android.content.Context
import android.content.SharedPreferences

class FeatureFlagStore constructor(context: Context) {

    fun getBoolean(key: String, valDefault: Boolean):
            Boolean {
        return sharedPreferences.getBoolean(key, valDefault)
    }

    fun putBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(KEY_PREF_STORE, 0)

    companion object {

        private const val KEY_PREF_STORE = "PREF_STORE"
        const val KEY_GUEST_CHECKOUT = "guestCheckout"

        private var INSTANCE: FeatureFlagStore? = null

        fun getInstance(context: Context): FeatureFlagStore {

            if (INSTANCE == null) {
                INSTANCE = FeatureFlagStore(context)
            }
            return INSTANCE as FeatureFlagStore
        }
    }
}
