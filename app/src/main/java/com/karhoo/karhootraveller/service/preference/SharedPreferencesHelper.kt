package com.karhoo.karhootraveller.service.preference

import android.content.SharedPreferences
import com.google.gson.Gson
import com.karhoo.sdk.api.model.TripInfo

internal object SharedPreferencesHelper {

    fun getString(sharedPreferences: SharedPreferences, key: String, valDefault: String?): String? {
        return sharedPreferences.getString(key, valDefault)
    }

    fun putString(sharedPreferences: SharedPreferences, key: String, value: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun putLong(sharedPreferences: SharedPreferences, key: String, value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(sharedPreferences: SharedPreferences, key: String, valDefault: Long): Long {
        return sharedPreferences.getLong(key, valDefault)
    }

    fun getParcelable(sharedPreferences: SharedPreferences, key: String, classType: Class<*>): Any? {
        return try {
            val json = sharedPreferences.getString(key, "")
            Gson().fromJson(json, classType)
        } catch (e: Exception) {
            TripInfo()
        }
    }

    fun putParcelable(sharedPreferences: SharedPreferences, key: String, value: Any?) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(value)
        editor.putString(key, json)
        editor.apply()
    }

}
