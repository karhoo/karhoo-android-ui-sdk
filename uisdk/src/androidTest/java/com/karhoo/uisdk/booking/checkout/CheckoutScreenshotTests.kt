package com.karhoo.uisdk.booking.checkout

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
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.booking.checkout.bookingconfirmation.BookingConfirmationScreenshotTests
import com.karhoo.uisdk.common.ScreenshotTest
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.bookingconfirmation.MockBookingConfirmationBaseActivity
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.QUOTE_WITH_CANCELLATION_AGREEMENT
import com.karhoo.uisdk.util.TestSDKConfig
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
@LargeTest
internal class CheckoutScreenshotTests :
    ScreenshotTest<MockBookingConfirmationBaseActivity>(MockBookingConfirmationBaseActivity::class.java) {

    /**
     * In order to generate locally screenshots, you need to use the -Precord argument
     * gradlew fulldebugexecuteScreenshotTests -Pandroid.testInstrumentationRunnerArguments.class=com.karhoo.uisdk.booking.checkout.CheckoutScreenshotTests
     *
     * When you want to compare the screenshots after some UI changes, just run
     * gradlew fulldebugexecuteScreenshotTests -Pandroid.testInstrumentationRunnerArguments.class=com.karhoo.uisdk.booking.checkout.CheckoutScreenshotTests
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
            paymentsProviderResponse(HttpURLConnection.HTTP_OK, TestData.ADYEN_PROVIDER)
            sdkInitResponse(HttpURLConnection.HTTP_OK, TestData.ADYEN_PUBLIC_KEY)
            userProfileResponse(HttpURLConnection.HTTP_OK, TestData.USER_INFO)
        }
    }

    @After
    override fun tearDown() {
        super.tearDown()
        wireMockRule.resetAll()
    }

    /**
     * Given:   The checkout opens up
     * When:    The address component is visible
     * Then:    The pickup address's first line is correct
     **/
    @Test
    fun checkoutPickupPrimaryAddressCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkPickupPrimaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The address component is visible
     * Then:    The pickup address's second line is correct
     **/
    @Test
    fun checkoutPickupSecondaryAddressCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkPickupSecondaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The address component is visible
     * Then:    The destination address's first line is correct
     **/
    @Test
    fun checkoutDestinationPrimaryAddressCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkDestinationPrimaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The address component is visible
     * Then:    The destination address's first line is correct
     **/
    @Test
    fun checkoutDestinationSecondaryAddressCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkDestinationSecondaryAddress()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The address component is visible
     * Then:    The TIME is correct
     **/
    @Test
    fun checkoutTimeCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkTimeText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The address component is visible
     * Then:    The date is correct
     **/
    @Test
    fun checkoutDateCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkDateText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The estimated price is visible
     * Then:    The price is correct
     **/
    @Test
    fun checkoutEstimatedPriceCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkPriceText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The estimated price is visible
     * Then:    The estimated price text is correct
     **/
    @Test
    fun checkoutEstimatedPriceTextCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkEstimatedFareTypeText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The vehicle title is correct
     **/
    @Test
    fun checkoutVehicleDetailsTitleCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkVehicleWidgetTitleText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The fleet name is correct
     **/
    @Test
    fun checkoutVehicleFleetNameCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkVehicleWidgetFleetText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The passenger capacity count is correct
     **/
    @Test
    fun checkVehicleWidgetFleetText() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkVehicleWidgetFleetText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The passenger capacity count is correct
     **/
    @Test
    fun checkVehiclePassengerCountText() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkVehicleWidgetPassengerCountText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The passenger luggage count is correct
     **/
    @Test
    fun checkVehicleLuggageCountText() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkVehicleWidgetLuggageCountText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The vehicle cancellation text is correct
     **/
    @Test
    fun checkVehicleCancellationText() {
        val activity = startCheckoutActivity(QUOTE_WITH_CANCELLATION_AGREEMENT)

        checkoutRobot {
            checkVehicleCancellationText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The passenger button title is visible
     **/
    @Test
    fun checkPassengerButtonTitleVisible() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkPassengerViewTitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The passenger button subtitle is visible
     **/
    @Test
    fun checkPassengerButtonSubtitleVisible() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkPassengerViewSubtitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    Pressing the passenger button
     * Then:    The passenger details screen is shown
     **/
    @Test
    fun clickPassengerButtonShowsCorrectScreen() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            clickPassengerButton()
            checkPassengerFirstNameVisible()

            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    Pressing the passenger button
     * Then:    The passenger name is filled
     **/
    @Test
    fun passengerNameIsCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            clickPassengerButton()
            checkPassengerFirstNameCorrect()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    Pressing the passenger button
     * Then:    The passenger last name is filled
     **/
    @Test
    fun passengerLastNameIsCorrect() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            clickPassengerButton()
            checkPassengerLastNameCorrect()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    Pressing the passenger button
     * Then:    The passenger phone is filled
     **/
    @Test
    fun passengerPhoneCorrect() {
        val activity = startCheckoutActivity(passengerDetails = TEST_PASSENGER_FULL)

        checkoutRobot {
            clickPassengerButton()
            checkPassengerPhoneCorrect()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    Pressing the passenger button
     * Then:    The passenger screen is closed and the passenger details are saved
     **/
    @Test
    fun passengerSaveWorksIfFieldsAreValid() {
        val activity = startCheckoutActivity(passengerDetails = TEST_PASSENGER_FULL)

        checkoutRobot {
            clickPassengerButton()
            checkPassengerPhoneCorrect()
            clickSavePassengerButton()
            checkPassengerViewGone()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    Pressing the passenger button
     * When:    Passenger details are filled
     * Then:    The passenger title is correct
     **/
    @Test
    fun passengerSaveDetailsWorks() {
        val activity = startCheckoutActivity(passengerDetails = TEST_PASSENGER_FULL)

        checkoutRobot {
            clickPassengerButton()
            clickSavePassengerButton()
            checkPassengerViewGone()
            checkPassengerNameVisibleOnCheckout()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    Pressing the passenger button
     * When:    Passenger details are filled
     * Then:    The passenger phone number is correct
     **/
    @Test
    fun passengerSavePhoneNumberWorks() {
        val activity = startCheckoutActivity(passengerDetails = TEST_PASSENGER_FULL)

        checkoutRobot {
            clickPassengerButton()
            clickSavePassengerButton()
            checkPassengerViewGone()
            checkPassengerPhoneNumberVisibleOnCheckout()
            shortSleep()
        }

        compareScreenshot(activity)
    }


    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The comment button title is visible
     **/
    @Test
    fun checkCommentButtonTitleVisible() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkCommentViewTitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The vehicle widget is visible
     * Then:    The comment button subtitle is visible
     **/
    @Test
    fun checkCommentButtonSubtitleVisible() {
        val activity = startCheckoutActivity()

        checkoutRobot {
            checkCommentViewSubtitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The journey details contain an airport POI
     * Then:    The FlightTracking widget title is visible
     **/
    @Test
    fun checkFlightTrackingWidgetTitleVisible() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_FLIGHT_TRACKING,
            journeyDetails = JOURNEY_DETAILS_AIRPORT
        )

        checkoutRobot {
            checkAirportViewTitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The journey details contain an airport POI
     * Then:    The FlightTracking widget subtitle is visible
     **/
    @Test
    fun checkFlightTrackingWidgetSubtitleVisible() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_FLIGHT_TRACKING,
            journeyDetails = JOURNEY_DETAILS_AIRPORT
        )

        checkoutRobot {
            checkAirportViewSubtitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The user presses on the airport widget
     * Then:    The airport number popup shows up
     **/
    @Test
    fun checkFlightTrackingWidgetOpensUp() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_FLIGHT_TRACKING,
            journeyDetails = JOURNEY_DETAILS_AIRPORT
        )

        checkoutRobot {
            checkAirportViewSubtitle()
            clickAirportWidget()
            checkAirportWidgetTitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The user presses on the airport widget and the user fills in data
     * Then:    The FlightTracking widget can be dismissed
     **/
    @Test
    fun fillFlightAllowSave() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_FLIGHT_TRACKING,
            journeyDetails = JOURNEY_DETAILS_AIRPORT
        )

        checkoutRobot {
            checkAirportViewSubtitle()
            clickAirportWidget()
            checkAirportWidgetTitle()
            fillFlightInfo()
            saveFlightInfo()
            checkAirportViewSubtitleFilled()
            shortSleep()
        }

        compareScreenshot(activity)
    }


    /**
     * Given:   The checkout opens up
     * When:    The journey details contain a train station POI
     * Then:    The train widget is visible
     **/
    @Test
    fun checkTrainWidgetTitleVisible() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_TRAIN_TRACKING,
            journeyDetails = JOURNEY_DETAILS_TRAIN
        )

        checkoutRobot {
            checkTrainTrackingViewTitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The user presses on the train tracking widget
     * Then:    The train tracking widget popup shows up
     **/
    @Test
    fun checkTrainTrackingWidgetOpensUp() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_TRAIN_TRACKING,
            journeyDetails = JOURNEY_DETAILS_TRAIN
        )

        checkoutRobot {
            checkTrainTrackingViewTitle()
            clickTrainTrackingWidget()
            checkTrainTrackingWidgetTitle()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The user presses on the price details question mark
     * Then:    The price details bottom sheet is shown
     **/
    @Test
    fun checkPriceDetailsBottomSheet() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_TRAIN_TRACKING,
            journeyDetails = JOURNEY_DETAILS_TRAIN
        )

        checkoutRobot {
            clickOnPriceDetailsIcon()
            checkPriceDetailsText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The user scrolls down
     * Then:    Then he sees the legal notice
     **/
    @Test
    fun legalNoticeIsVisible() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_TRAIN_TRACKING,
            journeyDetails = JOURNEY_DETAILS_TRAIN
        )

        checkoutRobot {
            scrollUpCheckout()
            shortSleep()
            checkLegalNoticeText()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The user scrolls down
     * Then:    Then he sees the terms and conditions
     **/
    @Test
    fun termsAndConditionsVisible() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_TRAIN_TRACKING,
            journeyDetails = JOURNEY_DETAILS_TRAIN
        )

        checkoutRobot {
            scrollUpCheckout()
            shortSleep()
            checkTermsText()
            shortSleep()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   The checkout opens up
     * When:    The user presses on the train tracking widget and fills in the data
     * Then:    The train tracking number can be saved
     **/
    @Test
    fun fillTrainTrackingDetails() {
        val activity = startCheckoutActivity(
            quote = QUOTE_WITH_TRAIN_TRACKING,
            journeyDetails = JOURNEY_DETAILS_TRAIN
        )

        checkoutRobot {
            shortSleep()
            checkTrainTrackingViewTitle()
            clickTrainTrackingWidget()
            fillTrainTrackingInfo()
            saveTrainTrackingInfo()
            checkTrainTrackingWidgetSubtitleFilled()
        }

        compareScreenshot(activity)
    }

    private fun startCheckoutActivity(
        quote: Quote? = BookingConfirmationScreenshotTests.QUOTE,
        passengerDetails: PassengerDetails? = TEST_PASSENGER_MINIMAL,
        journeyDetails: JourneyDetails? = BookingConfirmationScreenshotTests.journeyDetails
    ): Activity {
        val bundle = Bundle()
        bundle.putParcelable(
            CheckoutActivity.BOOKING_CHECKOUT_JOURNEY_DETAILS_KEY,
            journeyDetails
        )
        bundle.putParcelable(
            CheckoutActivity.BOOKING_CHECKOUT_QUOTE_KEY,
            quote
        )
        bundle.putParcelable(
            CheckoutActivity.BOOKING_CHECKOUT_PASSENGER_KEY,
            passengerDetails
        )
        bundle.putBoolean(
            BookingConfirmationScreenshotTests.SCREENSHOT_TEST_LOYALTY_VISIBILITY,
            true
        )

        val intent = Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            CheckoutActivity::class.java
        )

        intent.putExtras(bundle)

        return startActivity(
            intent
        )
    }

    companion object {
        private val TEST_PASSENGER_MINIMAL = PassengerDetails(
            firstName = "John",
            lastName = "Wick",
            locale = "US"
        )

        private val TEST_PASSENGER_FULL = PassengerDetails(
            firstName = "John",
            lastName = "Wick",
            locale = "US",
            email = "john.wick@test.com",
            phoneNumber = "12064512559 "
        )

        private val JOURNEY_DETAILS_AIRPORT =
            BookingConfirmationScreenshotTests.journeyDetails.copy(
                date = DateTime(BookingConfirmationScreenshotTests.SCHEDULED_DATE),
                pickup = BookingConfirmationScreenshotTests.journeyDetails.pickup?.copy(
                    details = PoiDetails(
                        "OTOPENI",
                        "TERMINAL 9",
                        PoiType.AIRPORT
                    )
                )
            )

        private val JOURNEY_DETAILS_TRAIN = BookingConfirmationScreenshotTests.journeyDetails.copy(
            date = DateTime(BookingConfirmationScreenshotTests.SCHEDULED_DATE),
            pickup = BookingConfirmationScreenshotTests.journeyDetails.pickup?.copy(
                details = PoiDetails(
                    "GARE DU NORD",
                    "TERMINAL 9",
                    PoiType.TRAIN_STATION
                )
            )
        )

        val QUOTE_WITH_FLIGHT_TRACKING = Quote(
            id = "NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
            quoteType = QuoteType.ESTIMATED,
            quoteSource = QuoteSource.FLEET,
            price = BookingConfirmationScreenshotTests.QUOTE_PRICE,
            fleet = BookingConfirmationScreenshotTests.QUOTE_FLEET.copy(
                capabilities = arrayListOf("flight_tracking")
            ),
            pickupType = PickupType.CURBSIDE,
            vehicle = BookingConfirmationScreenshotTests.QUOTE_VEHICLE
        )

        val QUOTE_WITH_TRAIN_TRACKING = Quote(
            id = "NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
            quoteType = QuoteType.ESTIMATED,
            quoteSource = QuoteSource.FLEET,
            price = BookingConfirmationScreenshotTests.QUOTE_PRICE,
            fleet = BookingConfirmationScreenshotTests.QUOTE_FLEET.copy(
                capabilities = arrayListOf("train_tracking")
            ),
            pickupType = PickupType.CURBSIDE,
            vehicle = BookingConfirmationScreenshotTests.QUOTE_VEHICLE
        )
    }
}