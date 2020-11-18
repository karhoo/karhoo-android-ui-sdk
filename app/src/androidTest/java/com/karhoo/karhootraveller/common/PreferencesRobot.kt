package com.karhoo.karhootraveller.common

import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.UserLogin
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.util.TestData.Companion.ADYEN_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.PAYMENTS_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.PAYMENTS_TOKEN_NO_CARD_REGISTERED
import com.karhoo.uisdk.util.TestData.Companion.USER_INFO
import com.karhoo.uisdk.util.TestData.Companion.USER_INFO_ADYEN
import java.net.HttpURLConnection.HTTP_OK

fun preferences(func: PreferencesRobot.() -> Unit) = PreferencesRobot().apply { func() }

class PreferencesRobot {

    fun setUserPreference(userInfo: UserInfo) {
        serverRobot {
            successfulToken()
            userProfileResponse(HTTP_OK, USER_INFO)
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
        }
        loginUser(userInfo)
    }

    fun setUserPreferenceNoCard(userInfo: UserInfo) {
        serverRobot {
            successfulToken()
            userProfileResponse(HTTP_OK, USER_INFO)
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN_NO_CARD_REGISTERED)
        }
        loginUser(userInfo)
    }

    fun setUserPreferenceAdyen(userInfo: UserInfo) {
        serverRobot {
            successfulToken()
            userProfileResponse(HTTP_OK, USER_INFO_ADYEN)
            paymentsProviderResponse(HTTP_OK, ADYEN_PROVIDER)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
        }
        loginUser(userInfo)
    }

    private fun loginUser(userInfo: UserInfo) {
        KarhooApi.userService.logout()
        KarhooApi.userService.loginUser(UserLogin(
                email = userInfo.email,
                password = "testpassword")).execute {
            when (it) {
                is Resource.Success -> print("User updated successfully " + it.data)
                is Resource.Failure -> print("User update failed")
            }
        }
    }

    fun clearUserPreference() {
        serverRobot {
            deleteRefreshToken()
        }
        KarhooApi.userService.logout()
    }
}