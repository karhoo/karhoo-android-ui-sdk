package com.karhoo.karhootraveller.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.gson.Gson
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.karhootraveller.common.preferences
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.login.login
import com.karhoo.karhootraveller.presentation.splash.SplashActivity
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.address.address
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.ServerRobot
import com.karhoo.uisdk.common.ServerRobot.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.common.ServerRobot.Companion.PLACE_DETAILS
import com.karhoo.uisdk.common.ServerRobot.Companion.PLACE_DETAILS_EXTRA
import com.karhoo.uisdk.common.ServerRobot.Companion.PLACE_SEARCH_RESULT
import com.karhoo.uisdk.common.ServerRobot.Companion.PLACE_SEARCH_RESULT_EXTRA
import com.karhoo.uisdk.common.ServerRobot.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.common.ServerRobot.Companion.USER_INFO
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.LONG
import com.karhoo.uisdk.util.TestData.Companion.MEDIUM
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS_EXTRA
import com.karhoo.uisdk.util.TestData.Companion.SELECTED_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.SELECTED_ADDRESS_EXTRA
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
class SplashTestsEndToEnd : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options().port(TravellerTestConfig.PORT_NUMBER), false)

    @get:Rule
    val activityRule: ActivityTestRule<SplashActivity> =
            ActivityTestRule(SplashActivity::class.java, false, false)

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

    private fun setUserInfo() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
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
     * Given:   I am not logged in
     * When:    I log in and go through the booking flow (search for addresses, select quote,
     * complete trip)
     * Then:    The trip completes as expected
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userLogsInAndCompletesATrip() {
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            userProfileResponse(HTTP_OK, USER_INFO)
            paymentsProviderResponse(HTTP_OK, ServerRobot.BRAINTREE_PROVIDER)
            paymentsNonceResponse(HTTP_OK, ServerRobot.PAYMENTS_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        splash(this) {
            sleep(MEDIUM)
            clickOnSignInButton()
        }
        login {
            validEmailAndPasswordLogin()
            userClicksOnSignInButton()
        }
        booking {
            sleep(MEDIUM)
        } result {
            checkBookingScreenIsShown()
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
            clickBakerStreetResult()
        }
        booking {
            sleep(MEDIUM)
        } result {
            selectedPickupAddressIsVisible(address = SELECTED_ADDRESS)
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        address {
            search(SEARCH_ADDRESS_EXTRA)
            sleep(LONG)
            clickOxfordStreetResult()
        }
        serverRobot {
            quoteIdResponse(HTTP_OK, ServerRobot.QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, ServerRobot.VEHICLES_V2_ASAP)
        }
        booking {
            sleep(MEDIUM)
        } result {
            selectedDestinationAddressIsVisible(address = SELECTED_ADDRESS_EXTRA)
        }
        serverRobot {
            bookingResponse(HTTP_OK, TestData.TRIP)
            bookingStatusResponse(code = HTTP_OK, response = ServerRobot.TRIP_STATUS_DER, trip = TestData.TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = ServerRobot.DRIVER_TRACKING, trip = TestData.TRIP.tripId)
            bookingDetailsResponse(code = HTTP_OK, response = ServerRobot.TRIP_DER_NO_NUMBER_PLATE, trip = TestData.TRIP.tripId)
        }
        booking {
            waitForTime(7000)
            pressFirstQuote()
            pressBookRideButton()
        } result {
            checkDriverDetails()
        }
    }

    companion object {

        private val origin = TestData.ORIGIN_TRIP
        private val destination = TestData.DESTINATION_TRIP
        private val initialTripDetails = TripInfo(origin = origin, destination = destination)
        private val identicalAddresses = TripInfo(origin = origin, destination = origin)

//        val CLEAN_TRIP_INTENT = Intent().apply {
//            putExtras(Bundle().apply {
//                putParcelable(BookingActivity.Builder.EXTRA_TRIP_DETAILS, TestData.TRIP)
//            })
//        }
//
//        val INITIAL_TRIP_INTENT = Intent().apply {
//            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, initialTripDetails)
//        }
//
//        val IDENTICAL_ADDRESSES_TRIP_INTENT = Intent().apply {
//            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, identicalAddresses)
//        }

    }
}

