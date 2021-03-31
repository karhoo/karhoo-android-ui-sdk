package com.karhoo.samples.uisdk.dropin

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.samples.uisdk.dropin.common.preferences
import com.karhoo.uisdk.address.address
import com.karhoo.uisdk.booking.booking
import com.karhoo.samples.uisdk.dropin.common.Launch
import com.karhoo.samples.uisdk.dropin.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.ridedetail.rideDetail
import com.karhoo.uisdk.trip.trip
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.PAYMENTS_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.PLACE_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.PLACE_DETAILS_EXTRA
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH_RESULT
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH_RESULT_EXTRA
import com.karhoo.uisdk.util.TestData.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS_EXTRA
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP_ARRIVED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_COMPLETED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_POB
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_ARRIVED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_COMPLETED
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_POB
import com.karhoo.uisdk.util.TestData.Companion.USER_INFO
import com.karhoo.uisdk.util.TestData.Companion.VEHICLES_ASAP
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class EndToEndBraintreeUserTest : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options().port(UiSDKTestConfig.PORT_NUMBER), false)

    @get:Rule
    val activityRule: ActivityTestRule<MainActivity> =
            ActivityTestRule(MainActivity::class.java, false, false)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

    @Before
    fun clearUser() {
        preferences {
            clearUserPreference()
        }
    }

    @After
    fun tearDown() {
        preferences {
            clearUserPreference()
        }
    }

    /**
     * Given:   I am on the splash screen
     * When:    I use the App
     * Then:    I can go through from login to ride completed with no issues
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun endToEndTestWithBraintreeUser() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
            userProfileResponse(HTTP_OK, USER_INFO)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        main(this) {
            mediumSleep()
            userClicksOnBookATripGuest()
        }
        booking {
            mediumSleep()
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking {
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            mediumSleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        serverRobot {
            quoteIdResponse(HTTP_OK, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
            bookingWithNonceResponse(HTTP_OK, TRIP)
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_DER,
                tracking = DRIVER_TRACKING,
                details = TRIP_DER,
                reverseGeo = REVERSE_GEO_SUCCESS
                               )
        address {
            search(SEARCH_ADDRESS_EXTRA)
            mediumSleep()
            clickOxfordStreetResult()
        }
        booking {
            mediumSleep()
            pressFirstQuote()
            mediumSleep()
            pressBookRideButton()
            shortSleep()
        }
        trip {
            mediumSleep()
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_ARRIVED,
                tracking = DRIVER_TRACKING,
                details = TRIP_ARRIVED,
                reverseGeo = REVERSE_GEO_SUCCESS
                               )
        trip {
            mediumSleep()
        }
        mockTripSuccessResponse(
                status = TRIP_STATUS_POB,
                tracking = DRIVER_TRACKING,
                details = TRIP_POB,
                reverseGeo = REVERSE_GEO_SUCCESS
                               )
        trip {
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
}