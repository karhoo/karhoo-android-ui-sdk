package com.karhoo.uisdk.trip

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.networkServiceRobot
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_DER
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
class TripLOCTest : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<TripActivity> =
            ActivityTestRule(TripActivity::class.java, false, false)

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options()
                                            .port(UiSDKTestConfig.PORT_NUMBER)
                                            .notifier(ConsoleNotifier(true)))

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

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
     * Given:   The user is on a live trip screen
     * When:    The wifi is disabled
     * Then:    The snackbar should show to enable wifi
     **/
    @Test
    fun snackbarShowsToUserWhenWifiIsDisabled() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER)
        trip(this) {
            networkServiceRobot {
                disableNetwork(activityRule.activity.applicationContext)
            }
            mediumSleep()
        } result {
            checkSnackbarWithText(R.string.kh_uisdk_network_error)
        }
    }

    private fun mockTripSuccessResponse(status: Any, tracking: Any, details: TripInfo) {
        serverRobot {
            successfulToken()
            bookingStatusResponse(code = HttpURLConnection.HTTP_OK, response = status, trip = TestData.TRIP.tripId)
            driverTrackingResponse(code = HttpURLConnection.HTTP_OK, response = tracking, trip = TestData.TRIP.tripId)
            bookingDetailsResponse(code = HttpURLConnection.HTTP_OK, response = details, trip = TestData.TRIP.tripId)
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(INTENT)
    }

    companion object {
        private val INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TripActivity.Builder.EXTRA_TRIP, TestData.TRIP)
            })
        }
    }
}