package com.karhoo.uisdk.trip

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.ridedetail.rideDetail
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.CANCEL_WITHOUT_BOOKING_FEE
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP_COMPLETED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_POB
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_CANCELLED_BY_USER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_COMPLETED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_POB
import com.karhoo.uisdk.util.TestData.Companion.USER_INFO
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class TripFlowTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<TripActivity> =
            ActivityTestRule(TripActivity::class.java, false, false)

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
        }
    }

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
        serverRobot {
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER,
                reverseGeo = REVERSE_GEO_SUCCESS)
        trip(this) {
            clickBackToolbarButton()
        }
        booking {
            shortSleep()
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
    @AllowFlaky(attempts = 5)
    fun userNavigatesFromTripToBookingScreenAfterCancellation() {
        serverRobot {
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER,
                reverseGeo = REVERSE_GEO_SUCCESS)
        serverRobot {
            cancelFeeResponse(code = HTTP_OK,
                              response = CANCEL_WITHOUT_BOOKING_FEE,
                              trip = TRIP.tripId)
            cancelResponse(
                    code = HTTP_CREATED,
                    response = TRIP_STATUS_CANCELLED_BY_USER,
                    trip = TRIP.tripId)
        }
        trip(this) {
            shortSleep()
            clickOnDriverDetails()
            clickOnCancelRide()
            clickConfirmCancellation()
            clickOKOnCancelledConfirmation()
        }
        booking {
            shortSleep()
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
        serverRobot {
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_POB,
                tracking = DRIVER_TRACKING,
                details = TRIP_POB,
                reverseGeo = REVERSE_GEO_SUCCESS)
        trip(this) {
            mediumSleep()
            clickBackToolbarButton()
        }
        booking {
            shortSleep()
        } result {
            checkBookingScreenIsShown()
        }
    }

    /**
     * Given:   I have a trip in progress
     * When:    The trip completes
     * Then:    I am taken to the past ride details screen
     * And:     All completed ride checks are verified
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userIsTakenToCompletedRideScreen() {
        serverRobot {
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_POB,
                tracking = DRIVER_TRACKING,
                details = TRIP_POB,
                reverseGeo = REVERSE_GEO_SUCCESS)
        trip(this) {
            mediumSleep()
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_COMPLETED,
                tracking = Any(),
                details = TRIP_COMPLETED,
                reverseGeo = Any()
                               )
        trip {
            mediumSleep()
        }
        rideDetail {
            mediumSleep()
        } result {
            completedRideFullCheckFromTrip()
        }
    }

    private fun mockTripSuccessResponse(status: Any, tracking: Any, details: Any, reverseGeo: Any) {
        serverRobot {
            successfulToken()
            bookingStatusResponse(code = HTTP_OK, response = status, trip = TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = tracking, trip = TRIP.tripId)
            bookingDetailsResponse(code = HTTP_OK, response = details, trip = TRIP.tripId)
            reverseGeocodeResponse(code = HTTP_OK, response = reverseGeo)
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(INTENT)
    }

    companion object {
        val INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(TripActivity.Builder.EXTRA_TRIP, TRIP)
            })
        }
    }
}