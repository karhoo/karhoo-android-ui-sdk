package com.karhoo.uisdk.booking.checkout.bookingconfirmation

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.ScreenshotTest
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestSDKConfig
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
@LargeTest
class BookingConfirmationScreenshotTests :
    ScreenshotTest<MockBookingConfirmationBaseActivity>(MockBookingConfirmationBaseActivity::class.java) {

    /**
     * In order to generate locally screenshots, you need to use the -Precord argument
     * gradlew fulldebugexecuteScreenshotTests -Precord -Pandroid.testInstrumentationRunnerArguments.class=com.karhoo.uisdk.quotes.screenshot.QuotesScreenshotTests
     *
     * When you want to compare the screenshots after some UI changes, just run
     * gradlew fulldebugexecuteScreenshotTests -Pandroid.testInstrumentationRunnerArguments.class=com.karhoo.uisdk.quotes.screenshot.QuotesScreenshotTests
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


    /**
     * Given:   The booking confirmation opens up
     * When:    It has finished the transition on screen
     * Then:    The title is the correct one
     **/
    @Test
    fun bookingConfirmationTitleIsVisible() {
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

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
        val activity = startActivity(
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                MockBookingConfirmationBaseActivity::class.java
            )
        )

        bookingConfirmation {
            scrollUpBookingConfirmation()
            checkLoyaltyVisibility()
            shortSleep()
        }

        compareScreenshot(activity)
    }


}