package com.karhoo.uisdk.quotes.screenshot

import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.common.ScreenshotTest
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.USER
import com.karhoo.uisdk.util.TestSDKConfig
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
@LargeTest
class QuotesScreenshotTests : ScreenshotTest<QuotesActivity>(QuotesActivity::class.java) {

    /**
     * In order to generate locally screenshots, you need to use the -Precord argument
     * gradlew fulldebugexecuteScreenshotTests -Precord -Pandroid.testInstrumentationRunnerArguments.class=com.karhoo.uisdk.address.screenshot.QuotesScreenshotTests
     *
     * When you want to compare the screenshots after some UI changes, just run
     * gradlew fulldebugexecuteScreenshotTests -Pandroid.testInstrumentationRunnerArguments.class=com.karhoo.uisdk.address.screenshot.QuotesScreenshotTests
     *
     * In order to run all screenshot tests, remove the -P argument following the gradle task
     * gradlew fulldebugexecuteScreenshotTests
     */
    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    @Before
    fun setUp() {
        KarhooUISDK.setConfiguration(
            TestSDKConfig(context = InstrumentationRegistry.getInstrumentation()
                .targetContext, authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "organisation_id"))
        )

        preferences {
            setUserPreference(USER)
        }
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HttpURLConnection.HTTP_OK, TestData.BRAINTREE_PROVIDER)
            sdkInitResponse(HttpURLConnection.HTTP_OK, TestData.BRAINTREE_TOKEN)
        }
    }

    /**
     * Given:   A user has entered a search term
     * When:    The term is valid
     * Then:    There should be a result for the term on the list
     **/
    @Test
    fun searchingForAnAddressReturnsResultsSuccessfully() {
        serverRobot {
            successfulToken()
            userProfileResponse(HttpURLConnection.HTTP_OK, TestData.USER_INFO)
            paymentsProviderResponse(HttpURLConnection.HTTP_OK, TestData.BRAINTREE_PROVIDER)
            paymentsNonceResponse(HttpURLConnection.HTTP_OK, TestData.PAYMENTS_TOKEN)
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS)
            reverseGeocodeResponse(HttpURLConnection.HTTP_OK, TestData.REVERSE_GEO_SUCCESS)
        }

        val activity = startActivity()

        Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.isAssignableFrom(TextView::class.java),
                ViewMatchers.withParent(ViewMatchers.isAssignableFrom(Toolbar::class.java))
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.withText("0 results")))

        compareScreenshot(activity)
    }
}