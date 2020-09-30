package com.karhoo.uisdk.booking

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.gson.Gson
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.ServerRobot.Companion.ADYEN_PUBLIC_KEY
import com.karhoo.uisdk.common.ServerRobot.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.common.ServerRobot.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.common.ServerRobot.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.common.ServerRobot.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.common.ServerRobot.Companion.TRIP_DER_NO_NUMBER_PLATE
import com.karhoo.uisdk.common.ServerRobot.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.common.ServerRobot.Companion.VEHICLES_V2_ASAP
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData.Companion.DESTINATION_TRIP
import com.karhoo.uisdk.util.TestData.Companion.MEDIUM
import com.karhoo.uisdk.util.TestData.Companion.ORIGIN_TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.net.HttpURLConnection.HTTP_OK
import java.nio.charset.StandardCharsets

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
                .putString("payment_provider_id", "Adyen")
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
        val context = InstrumentationRegistry.getInstrumentation().context
        val adyenPaymentMethodsResponse = readAsset(context, "adyen_payment_methods.json")
                /*context.assets.open("adyen_payment_methods.json").bufferedReader()
                .use(BufferedReader::readText)*/

        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_OK, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_V2_ASAP)
            adyenPublicKeyResponse(HTTP_OK, ADYEN_PUBLIC_KEY)
            adyenPaymentMethodsResponse(HTTP_OK, adyenPaymentMethodsResponse)
            bookingResponse(HTTP_OK, TRIP)
            bookingStatusResponse(code = HTTP_OK, response = TRIP_STATUS_DER, trip = TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = DRIVER_TRACKING, trip = TRIP.tripId)
            bookingDetailsResponse(code = HTTP_OK, response = TRIP_DER_NO_NUMBER_PLATE, trip = TRIP.tripId)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
            pressFirstQuote()
            sleep(MEDIUM)
            pressAddCardButton()
//            pressBookRideButton()
            waitForTime(5000)
            sleep()
        } result {
            checkDriverDetails()
        }
    }

    fun readAsset(context: Context, fileName: String): String =
            context.assets
                    .open(fileName)
                    .bufferedReader()
                    .use(BufferedReader::readText)

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

        val INITIAL_TRIP_INTENT = Intent().apply {
            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, initialTripDetails)
        }

    }
}
