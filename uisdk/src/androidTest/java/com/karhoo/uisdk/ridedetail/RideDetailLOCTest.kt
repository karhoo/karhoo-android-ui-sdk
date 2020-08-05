package com.karhoo.uisdk.ridedetail

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.networkServiceRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.LONG
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RideDetailLOCTest : Launch {
    @get:Rule
    val activityRule: ActivityTestRule<RideDetailActivity> =
            ActivityTestRule(RideDetailActivity::class.java, false, false)

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER) // No-args constructor defaults to port 8080

    private var intent: Intent? = null

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
     * Given:   The user is on the ride details screen
     * When:    The wifi is disabled
     * Then:    The snackbar should show to enable wifi
     **/
    @Test
    fun snackbarShowsToUserWhenWifiIsDisabledRideDetails() {
        rideDetail(this, TRIP_INTENT) {
            networkServiceRobot {
                disableNetwork(activityRule.activity.applicationContext)
            }
            sleep(LONG)
        } result {
            checkSnackbarWithText(R.string.network_error)
        }
    }

    override fun launch(intent: Intent?) {
        if (intent != null) {
            activityRule.launchActivity(intent)
        } else {
            activityRule.launchActivity(this.intent)
        }
    }

    companion object {
        private const val TRIP_EXTRA = RideDetailActivity.Builder.EXTRA_TRIP

        val TRIP_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TRIP_EXTRA, TestData.TRIP)
            })
        }
    }
}