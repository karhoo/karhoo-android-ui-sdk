package com.karhoo.uisdk.booking

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.NoActivityResumedException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.gson.Gson
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.address.address
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.ServerRobot
import com.karhoo.uisdk.common.ServerRobot.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.common.ServerRobot.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.common.ServerRobot.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.common.ServerRobot.Companion.PAYMENTS_TOKEN
import com.karhoo.uisdk.common.ServerRobot.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.common.ServerRobot.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.common.ServerRobot.Companion.TRIP_DER_NO_NUMBER_PLATE
import com.karhoo.uisdk.common.ServerRobot.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.common.ServerRobot.Companion.VEHICLES_V2_ASAP
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.DESTINATION_TRIP
import com.karhoo.uisdk.util.TestData.Companion.MEDIUM
import com.karhoo.uisdk.util.TestData.Companion.ORIGIN_TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class AdyenBookingTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<BookingActivity> =
            ActivityTestRule(BookingActivity::class.java, false, false)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    private var intent: Intent? = null

    @Before
    fun setUp() {
        setUserInfo()
    }

    @After
    fun teardown() {
        intent = null
        wireMockRule.resetAll()
    }

    private fun setUserInfo() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val sharedPreferences = context.getSharedPreferences("user", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        sharedPreferences.edit()
                .putString("first_name", "John")
                .putString("last_name", "Smith")
                .putString("email", "test@test.test")
                .putString("mobile_number", "123")
                .putString("user_id", "1234")
                .putString("organisations", Gson().toJson(
                        listOf(Organisation(id = "organisation_id",
                                            name = "B2C DefaultOrgForKarhooAppUsers",
                                            roles = emptyList()))))
                .putString("locale", "en-GB")
                .putString("payment_provider_id", "Braintree")
                .apply()
        editor.commit()
    }

    /**
     * Given:   I have selected an ASAP quote
     * When:    I book a successful trip
     * And:     A driver is allocated
     * Then:    I can see the following driver details.
     **/
    @Test
    fun asapBookARideSuccess() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_OK, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_V2_ASAP)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
            bookingResponse(HTTP_OK, TRIP)
            bookingStatusResponse(code = HTTP_OK, response = TRIP_STATUS_DER, trip = TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = DRIVER_TRACKING, trip = TRIP.tripId)
            bookingDetailsResponse(code = HTTP_OK, response = TRIP_DER_NO_NUMBER_PLATE, trip = TRIP.tripId)
        }
        booking(this, BookingTests.INITIAL_TRIP_INTENT) {
            sleep()
            pressFirstQuote()
            sleep(MEDIUM)
            pressBookRideButton()
            waitForTime(5000)
            sleep()
        } result {
            checkDriverDetails()
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

        private val origin = ORIGIN_TRIP
        private val destination = DESTINATION_TRIP
        private val initialTripDetails = TripInfo(origin = origin, destination = destination)
        private val identicalAddresses = TripInfo(origin = origin, destination = origin)

        val CLEAN_TRIP_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(BookingActivity.Builder.EXTRA_TRIP_DETAILS, TRIP)
            })
        }

        val INITIAL_TRIP_INTENT = Intent().apply {
            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, initialTripDetails)
        }

        val IDENTICAL_ADDRESSES_TRIP_INTENT = Intent().apply {
            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, identicalAddresses)
        }

    }
}
