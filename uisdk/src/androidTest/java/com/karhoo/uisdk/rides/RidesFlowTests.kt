package com.karhoo.uisdk.rides

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class RidesFlowTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<RidesActivity> =
            ActivityTestRule(RidesActivity::class.java, false, false)

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options().port(UiSDKTestConfig.PORT_NUMBER), false)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        val intent = Intent()
        activityRule.launchActivity(intent)
    }

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    /**
     * Given:   I am on the Upcoming Rides screen
     * When:    I press the Book ride button
     * Then:    I am taken to the booking screen
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userNavigatesFromUpcomingRidesToBooking() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        rides {
            clickUpcomingBookingsTabButton()
            mediumSleep()
            clickBookRideButton()
        }
        booking {
            result {
                checkBookingScreenIsShown()
            }
        }
    }

    /**
     * Given:   I am on the Past Rides screen
     * When:    I press the Book ride button
     * Then:    I am taken to the booking screen
     **/
    @Test
    fun userNavigatesFromPastRidesToBooking() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        rides {
            clickPastBookingsTabButton()
            shortSleep()
            clickBookRideButton()
        }
        booking {
            result {
                checkBookingScreenIsShown()
            }
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

}