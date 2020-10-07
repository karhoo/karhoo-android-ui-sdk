package com.karhoo.uisdk.rides

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.networkServiceRobot
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.util.TestData.Companion.LONG
import com.karhoo.uisdk.util.TestData.Companion.TRIP_HISTORY_EMPTY
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
class RidesLOCTest : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<RidesActivity> =
            ActivityTestRule(RidesActivity::class.java, false, false)

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
     * Given:   The user is on the rides screen
     * When:    The wifi is disabled
     * Then:    The snackbar should show to enable wifi
     **/
    @Test
    fun snackbarShowsToUserWhenWifiIsDisabledRides() {
        serverRobot {
            successfulToken()
            pastRidesResponse(HttpURLConnection.HTTP_OK, TRIP_HISTORY_EMPTY)
        }
        rides(this) {
            networkServiceRobot {
                disableNetwork(activityRule.activity.applicationContext)
            }
            sleep(LONG)
        } result {
            sleep()
            checkErrorIsShown(R.string.network_error)
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }
}