package com.karhoo.uisdk.common

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.UserLogin
import com.karhoo.uisdk.util.TestData.Companion.PAYMENTS_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.USER_INFO

fun preferences(func: PreferencesRobot.() -> Unit) = PreferencesRobot().apply { func() }

class PreferencesRobot {

    fun setStringPreference(key: String, value: String) {
        PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().targetContext)
                .edit()
                .putString(key, value)
                .commit()
    }

    fun setUserPreference(userInfo: UserInfo) {
        serverRobot {
            userProfileResponse(200, USER_INFO)
            paymentsNonceResponse(200, PAYMENTS_TOKEN)
            successfulToken()
        }

        KarhooApi.userService.loginUser(UserLogin(
                email = userInfo.email,
                password = "testpassword")).execute { }
    }

    fun clear() {
        PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getInstrumentation().targetContext)
                .edit()
                .clear()
                .commit()
    }

}