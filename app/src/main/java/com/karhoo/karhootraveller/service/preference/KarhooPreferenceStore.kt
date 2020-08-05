package com.karhoo.karhootraveller.service.preference

import android.content.Context
import android.content.SharedPreferences
import com.karhoo.sdk.api.model.TripInfo

class KarhooPreferenceStore private constructor(context: Context) : PreferenceStore {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(KEY_PREF_STORE, 0)

    override var loginTimeMillis: Long
        get() = SharedPreferencesHelper.getLong(sharedPreferences, KEY_LOGIN_TIME, 0L)
        set(value) {
            SharedPreferencesHelper.putLong(sharedPreferences, KEY_LOGIN_TIME, value)
        }

    override var lastTrip: TripInfo?
        get() = SharedPreferencesHelper.getParcelable(sharedPreferences, KEY_LAST_TRIP, TripInfo::class.java as Class<*>) as TripInfo?
        set(value) {
            SharedPreferencesHelper.putParcelable(sharedPreferences, KEY_LAST_TRIP, value)
        }

    companion object {

        private const val KEY_PREF_STORE = "PREF_STORE"
        private const val KEY_LAST_TRIP = "last::trip"
        private const val KEY_LOGIN_TIME = "login::time"

        private var INSTANCE: KarhooPreferenceStore? = null

        fun getInstance(context: Context): PreferenceStore {

            if (INSTANCE == null) {
                INSTANCE = KarhooPreferenceStore(context)
            }
            return INSTANCE as KarhooPreferenceStore
        }
    }

}
