package com.karhoo.uisdk.trip

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.ServerRobot
import com.karhoo.uisdk.common.ServerRobot.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.common.ServerRobot.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.common.ServerRobot.Companion.TRIP_DER
import com.karhoo.uisdk.common.ServerRobot.Companion.TRIP_STATUS_CANCELLED_BY_USER
import com.karhoo.uisdk.common.ServerRobot.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.util.TestData
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class TripFlowTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<TripActivity> =
            ActivityTestRule(TripActivity::class.java, false, false)

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    /**
     * Given:   I am in a ride status DER
     * When:    I press the back button
     * Then:    I am returned to the Booking screen
     **/
    @Test
    fun userNavigatesFromTripDERToBookingScreen() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER,
                reverseGeo = REVERSE_GEO_SUCCESS)
        trip(this) {
            clickBackToolbarButton()
        }
        booking {
            sleep()
            result {
                checkBookingScreenIsShown()
            }
        }
    }

    /**
     * Given:   The state of the ride is DER
     * When:    The ride has been cancelled
     * Then:    I am taken back to the booking screen
     **/
    @Test
    fun userNavigatesFromTripToBookingScreenAfterCancellation() {
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER,
                reverseGeo = REVERSE_GEO_SUCCESS)
        serverRobot {
            cancelResponse(
                    code = HttpURLConnection.HTTP_CREATED,
                    response = TRIP_STATUS_CANCELLED_BY_USER,
                    trip = TestData.TRIP.tripId)
        }
        trip(this) {
            sleep()
            clickOnDriverDetails()
            clickOnCancelRide()
            clickConfirmCancellation()
            clickOKOnCancelledConfirmation()
        }
        booking {
            sleep()
        } result {
            checkBookingScreenIsShown()
        }
    }

    /**
     * Given:   A trip is in progress (POB)
     * When:    I press the back arrow
     * Then:    I am taken back to the booking screen
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun checkPOBDriverDetailsElements() {
        mockTripSuccessResponse(
                status = ServerRobot.TRIP_STATUS_POB,
                tracking = DRIVER_TRACKING,
                details = ServerRobot.TRIP_POB,
                reverseGeo = REVERSE_GEO_SUCCESS)
        trip(this) {
            sleep()
            clickBackToolbarButton()
        }
        booking {
            sleep()
        } result {
            checkBookingScreenIsShown()
        }
    }

    private fun mockTripSuccessResponse(status: Any, tracking: Any, details: Any, reverseGeo: Any) {
        serverRobot {
            successfulToken()
            bookingStatusResponse(code = HTTP_OK, response = status, trip = TestData.TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = tracking, trip = TestData.TRIP.tripId)
            bookingDetailsResponse(code = HTTP_OK, response = details, trip = TestData.TRIP.tripId)
            reverseGeocodeResponse(code = HTTP_OK, response = reverseGeo)
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(INTENT)
    }

    companion object {
        val INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TripActivity.Builder.EXTRA_TRIP, TestData.TRIP)
            })
        }
    }
}