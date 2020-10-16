package com.karhoo.uisdk.booking

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.booking.braintree.BraintreeBookingTests
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.networkServiceRobot
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_DESTINATION
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_ORIGIN
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
class BookingLOCTest : Launch {
    @get:Rule
    val activityRule: ActivityTestRule<BookingActivity> =
            ActivityTestRule(BookingActivity::class.java, false, false)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    private var intent: Intent? = null

    @After
    fun teardown() {
        intent = null
        wireMockRule.resetAll()
    }

    fun turnWifiOn() {
        networkServiceRobot {
            enableNetwork(activityRule.activity.applicationContext)
        }
    }

    /**
     * Given:   The user is on the booking screen
     * When:    The wifi is disabled
     * Then:    The snackbar should show to enable wifi
     **/
    @Test
    fun snackbarShowsToUserWhenWifiIsDisabledBooking() {
        serverRobot {
            successfulToken()
            sdkInitResponse(HttpURLConnection.HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HttpURLConnection.HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, BraintreeBookingTests.CLEAN_TRIP_INTENT) {
            networkServiceRobot {
                disableNetwork(activityRule.activity.applicationContext)
            }
            longSleep()
        } result {
            checkSnackbarWithText(R.string.network_error)
        }
    }

    override fun launch(intent: Intent?) {
        intent?.let {
            activityRule.launchActivity(it)
        } ?: run {
            activityRule.launchActivity(this.intent)
        }
    }

    companion object {

        private val origin = TripLocationInfo(displayAddress = ADDRESS_ORIGIN)
        private val destination = TripLocationInfo(displayAddress = ADDRESS_DESTINATION)
        private val initialTripDetails = TripInfo(origin = origin, destination = destination)

        val CLEAN_TRIP_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(BookingActivity.Builder.EXTRA_TRIP_DETAILS, TestData.TRIP)
            })
        }

        val INITAL_TRIP_INTENT = Intent().apply {
            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, initialTripDetails)
        }

    }

}