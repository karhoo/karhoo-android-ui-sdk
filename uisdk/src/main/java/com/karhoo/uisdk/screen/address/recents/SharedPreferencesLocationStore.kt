package com.karhoo.uisdk.screen.address.recents

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karhoo.sdk.api.model.LocationInfo

class SharedPreferencesLocationStore(context: Context) : LocationStore {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun save(locations: List<LocationInfo>): Boolean {
        val locationsAsJson = Gson().toJson(locations)
        return sharedPreferences.edit()
                .putString(KEY, locationsAsJson)
                .commit()
    }

    override fun retrieve(): List<LocationInfo> {
        val locationsAsJson = sharedPreferences.getString(KEY, JSON_EMPTY_ARRAY)
        val type = object : TypeToken<List<LocationInfo>>() {

        }.type

        return Gson().fromJson(locationsAsJson, type)
    }

    override fun clear() {
        save(emptyList())
    }

    companion object {
        private const val KEY = "SharedPreferencesLocationStore.LocationsKey"
        private const val JSON_EMPTY_ARRAY = "[]"
    }

}
