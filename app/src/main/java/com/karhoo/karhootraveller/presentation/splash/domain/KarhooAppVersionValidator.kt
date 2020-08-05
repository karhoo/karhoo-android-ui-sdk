package com.karhoo.karhootraveller.presentation.splash.domain

import com.google.gson.JsonObject
import com.karhoo.karhootraveller.service.preference.PreferenceStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.extension.orZero
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class KarhooAppVersionValidator(private val currentVersion: Int,
                                private val preferenceStore: PreferenceStore) : AppVersionValidator {

    private var isValid: Boolean = false
    private var isTokenValid: Boolean = false

    override fun isCurrentVersionValid(listener: AppVersionValidator.Listener) {
        if (isValid && isTokenValid) {
            listener.isAppValid(true)
            listener.isTokenValid(true)
        } else {
            getMinimumSupportedVersion(listener)
        }
    }

    override fun saveLoginTime(loginTimeMillis: Long) {
        preferenceStore.loginTimeMillis = loginTimeMillis
    }

    private fun getMinimumSupportedVersion(listener: AppVersionValidator.Listener?) {
        val versionService = Retrofit.Builder()
                .baseUrl("https://cdn.karhoo.net/s/app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VersionService::class.java)

        versionService.versions.enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                listener?.let {

                    isValid = currentVersion >= response.body()?.get("android")?.asInt.orZero()
                    it.isAppValid(isValid)

                    val forceLogoutAfterString = response.body()?.get("android_force_logout_after")?.asString
                    if (forceLogoutAfterString == null) {
                        it.isTokenValid(true)
                    } else {
                        val forceLogoutAfter = DateUtil.parseSimpleDate(forceLogoutAfterString)
                        it.isTokenValid(preferenceStore.loginTimeMillis > forceLogoutAfter.millis)
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                listener?.isAppValid(true)
                listener?.isTokenValid(true)
            }
        })
    }

    interface VersionService {
        @get:GET("version.json")
        val versions: Call<JsonObject>
    }

}
