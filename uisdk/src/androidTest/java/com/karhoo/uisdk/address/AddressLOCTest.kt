package com.karhoo.uisdk.address

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.networkServiceRobot
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.address.AddressActivity
import com.karhoo.uisdk.util.TestData
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddressLOCTest : Launch {
    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<AddressActivity> =
            ActivityTestRule(AddressActivity::class.java, false, false)

    private val intent = Intent().apply {
        putExtra("address::type", AddressType.PICKUP)
    }

    @Before
    fun setUp() {
        preferences {
            setUserPreference(TestData.USER)
        }
    }

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    fun turnWifiOn() {
        networkServiceRobot {
            enableNetwork(activityRule.activity.applicationContext)
        }
    }

    /**
     * Given:   The user is on the address screen
     * When:    The wifi is disabled
     * Then:    The snackbar should show to enable wifi
     **/
    @Test
    fun snackbarShowsToUserWhenWifiIsDisabledAddress() {
        address(this) {
            networkServiceRobot {
                disableNetwork(activityRule.activity.applicationContext)
            }
            longSleep()
        } result {
            checkSnackbarWithText(R.string.kh_uisdk_network_error)
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(this.intent)
    }
}
