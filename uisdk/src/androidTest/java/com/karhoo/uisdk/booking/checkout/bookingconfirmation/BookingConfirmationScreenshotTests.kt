package com.karhoo.uisdk.booking.checkout.bookingconfirmation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.*
import com.karhoo.sdk.api.network.request.QuoteQTA
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.ScreenshotTest
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestSDKConfig
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
@LargeTest
class BookingConfirmationScreenshotTests :
    ScreenshotTest<MockBookingConfirmationBaseActivity>(MockBookingConfirmationBaseActivity::class.java) {

    /**
     * In order to generate locally screenshots, you need to use the -Precord argument
     * gradlew fulldebugexecuteScreenshotTests -Precord -Pandroid.testInstrumentationRunnerArguments.class=com.karhoo.uisdk.booking.checkout.bookingconfirmation.BookingConfirmationScreenshotTests
     *
     * When you want to compare the screenshots after some UI changes, just run
     * gradlew fulldebugexecuteScreenshotTests -Pandroid.testInstrumentationRunnerArguments.class=com.karhoouisdk.booking.checkout.bookingconfirmation.BookingConfirmationScreenshotTests
     *
     * In order to run all screenshot tests, remove the -P argument following the gradle task
     * gradlew fulldebugexecuteScreenshotTests
     */
    @get:Rule
    var wireMockRule = WireMockRule(
        WireMockConfiguration().port(UiSDKTestConfig.PORT_NUMBER).notifier(
            ConsoleNotifier(true)
        ).extensions(ResponseTemplateTransformer(true))
    )

    @Before
    fun setUp() {
        KarhooUISDK.setConfiguration(
            TestSDKConfig(
                context = InstrumentationRegistry.getInstrumentation()
                    .targetContext,
                authenticationMethod = AuthenticationMethod.Guest(
                    "identifier",
                    "referer",
                    "organisation_id"
                )
            )
        )

        preferences {
            setUserPreference(TestData.USER)
        }
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HttpURLConnection.HTTP_OK, TestData.BRAINTREE_PROVIDER)
            sdkInitResponse(HttpURLConnection.HTTP_OK, TestData.BRAINTREE_TOKEN)
            userProfileResponse(HttpURLConnection.HTTP_OK, TestData.USER_INFO)
        }
    }

    @After
    override fun tearDown() {
        super.tearDown()
        wireMockRule.resetAll()
    }

    private fun startBookingConfirmationActivityWithDefaultValues(): Activity {
        val bundle = Bundle()
        bundle.putParcelable(SCREENSHOT_TEST_JOURNEY_DETAILS, journeyDetails)
        bundle.putParcelable(SCREENSHOT_TEST_QUOTE, QUOTE)
        bundle.putBoolean(SCREENSHOT_TEST_LOYALTY_VISIBILITY, true)

        val intent = Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MockBookingConfirmationBaseActivity::class.java
        )

        intent.putExtras(bundle)

        return startActivity(
            intent
        )
    }

    private fun startBookingConfirmationActivityWithSecondaryValues(): Activity {
        val bundle = Bundle()
        bundle.putParcelable(SCREENSHOT_TEST_JOURNEY_DETAILS, journeyDetails)
        bundle.putParcelable(SCREENSHOT_TEST_QUOTE, QUOTE)
        bundle.putBoolean(SCREENSHOT_TEST_LOYALTY_VISIBILITY, false)

        val intent = Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MockBookingConfirmationBaseActivity::class.java
        )

        intent.putExtras(bundle)

        return startActivity(
            intent
        )
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The title is the correct one
     **/
    @Test
    fun bookingConfirmationTitleIsVisible() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            viewIsVisible(R.id.masterBottomSheetTitle)
            checkBookingConfirmationTitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The pickup address's first line is correct
     **/
    @Test
    fun bookingConfirmationPickupPrimaryAddressCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingPickupPrimaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The pickup address's second line is correct
     **/
    @Test
    fun bookingConfirmationPickupSecondaryAddressCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingPickupSecondaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The destination address's first line is correct
     **/
    @Test
    fun bookingConfirmationDestinationPrimaryAddressCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingDestinationPrimaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The destination address's second line is correct
     **/
    @Test
    fun bookingConfirmationDestinationSecondaryAddressCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingDestinationSecondaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The time is correct
     **/
    @Test
    fun bookingConfirmationTimeCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingTimeText()
            longSleep(10000)
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The date is correct
     **/
    @Test
    fun bookingConfirmationDateCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingDateText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The price is correct
     **/
    @Test
    fun bookingConfirmationPriceCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingPriceText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The etsimated fare type is correct
     **/
    @Test
    fun bookingConfirmationEstimatedFareTypeCorrect() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkBookingEstimatedFareTypeText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The loyalty is visible
     **/
    @Test
    fun bookingConfirmationEstimatedLoyaltyVisible() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkLoyaltyVisibility()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The loyalty is not visible
     **/
    @Test
    fun bookingConfirmationEstimatedLoyaltyNotVisible() {
        val activity = startBookingConfirmationActivityWithSecondaryValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkLoyaltyNotVisible()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The add to calendar should be visible
     **/
    @Test
    fun bookingConfirmationAddToCalendarVisible() {
        val activity = startBookingConfirmationActivityWithDefaultValues()

        bookingConfirmation {
            scrollUpBookingConfirmation()
            addToCalendarVisibility()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    companion object {
        val TRIP_POSITION_PICKUP = Position(
            latitude = 51.523766,
            longitude = -0.1375291
        )

        val TRIP_POSITION_DROPOFF = Position(
            latitude = 51.514432,
            longitude = -0.1585557
        )

        val TRIP_LOCATION_INFO_PICKUP = LocationInfo(
            address = Address(
                displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK",
                buildingNumber = "221B",
                streetName = "Baker St",
                city = "Marleybone",
                postalCode = "NW1 6XE",
                region = "London",
                countryCode = "UK"
            ),
            position = TRIP_POSITION_PICKUP,
            placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
            timezone = "Europe/London",
            poiType = Poi.NOT_SET
        )

        val TRIP_LOCATION_INFO_DROPOFF = LocationInfo(
            address =  Address(
                displayAddress = "368 Oxford St, London W1D 1LU, UK",
                buildingNumber = "368",
                streetName = "Oxford St",
                city = "London",
                postalCode = "W1D 1LU",
                countryCode = "UK"
            ),
            position = TRIP_POSITION_DROPOFF,
            placeId = "ChIJyWu2IisbdkgRHIRWuD0ANfM",
            timezone = "Europe/London",
            poiType = Poi.NOT_SET
        )
        val QUOTE_PRICE = QuotePrice(
            currencyCode = "GBP",
            highPrice = 577,
            lowPrice = 577
        )

        val QUOTE_FLEET = Fleet(
            id = "52123bd9-cc98-4b8d-a98a-122446d69e79",
            name = "iCabbi [Sandbox]",
            logoUrl = "https://cdn.karhoo.com/d/images/logos/52123bd9-cc98-4b8d-a98a-122446d69e79.png",
            description = "Some fleet description",
            phoneNumber = "+447904839920",
            termsConditionsUrl = "http://www.google.com"
        )

        val QUOTE_VEHICLE = QuoteVehicle(
            vehicleClass = "Electric",
            vehicleQta = QuoteQTA(highMinutes = 30, lowMinutes = 1),
            luggageCapacity = 2,
            vehicleType = "standard",
            vehicleTags = arrayListOf("taxi, hybrid"),
            passengerCapacity = 2
        )

        val QUOTE = Quote(
            id = "NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
            quoteType = QuoteType.ESTIMATED,
            quoteSource = QuoteSource.FLEET,
            price = QUOTE_PRICE,
            fleet = QUOTE_FLEET,
            pickupType = PickupType.CURBSIDE,
            vehicle = QUOTE_VEHICLE
        )

        fun getDate(dateScheduled: String): Date {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.parse(dateScheduled)
        }

        val SCHEDULED_DATE = getDate("2019-07-31T12:35:00Z")

        val journeyDetails = JourneyDetails(
            TRIP_LOCATION_INFO_PICKUP,
            TRIP_LOCATION_INFO_DROPOFF,
            DateTime(SCHEDULED_DATE)
        )

        const val SCREENSHOT_TEST_JOURNEY_DETAILS = "SCREENSHOT_TEST_JOURNEY_DETAILS"
        const val SCREENSHOT_TEST_QUOTE = "SCREENSHOT_TEST_QUOTE"
        const val SCREENSHOT_TEST_LOYALTY_VISIBILITY = "SCREENSHOT_TEST_LOYALTY_VISIBILITY"
    }
}