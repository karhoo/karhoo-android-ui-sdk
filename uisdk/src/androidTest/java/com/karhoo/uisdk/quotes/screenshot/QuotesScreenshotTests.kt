package com.karhoo.uisdk.quotes.screenshot

import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
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

import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import org.junit.After

@RunWith(AndroidJUnit4::class)
@LargeTest
class QuotesScreenshotTests : ScreenshotTest<QuotesActivity>(QuotesActivity::class.java) {

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
    var wireMockRule = WireMockRule(WireMockConfiguration().port(UiSDKTestConfig.PORT_NUMBER).notifier(ConsoleNotifier(true)).extensions(ResponseTemplateTransformer(true)))

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
            setUserPreference(USER)
        }
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HttpURLConnection.HTTP_OK, TestData.BRAINTREE_PROVIDER)
            sdkInitResponse(HttpURLConnection.HTTP_OK, TestData.BRAINTREE_TOKEN)
            userProfileResponse(HttpURLConnection.HTTP_OK, TestData.USER_INFO)
            paymentsNonceResponse(HttpURLConnection.HTTP_OK, TestData.PAYMENTS_TOKEN)
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS)
            reverseGeocodeResponse(HttpURLConnection.HTTP_OK, TestData.REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_OK, TestData.QUOTE_LIST_ID_ASAP)
            quotesResponse(HttpURLConnection.HTTP_OK, TestData.VEHICLES_ASAP)
        }
    }

    @After
    override fun tearDown() {
        super.tearDown()
        wireMockRule.resetAll()
    }


    /**
     * Given:   A user has entered a search term
     * When:    The term is valid
     * Then:    There should be a result for the term on the list
     **/
    @Test
    fun searchingForAnAddressReturnsResultsSuccessfully() {
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

    /**
     * Given:   A user has entered a search term
     * When:    The results are loaded
     * Then:    The filter by button is visible
     **/

    @Test
    fun filterByButtonIsVisibleWhenLoadingAndAfterResults() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()

        }

        quotes {
            mediumSleep(2000)

            checkFilterButtonIsVisible()

            mediumSleep(3000)

            checkFilterButtonIsVisible()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   Quotes results have loaded
     * When:    Clicking on the filter by button
     * Then:    The bottom sheet opens up
     **/

    @Test
    fun bottomSheetShouldOpenWhenClickingOnFilterBy() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickFilterButton()

            mediumSleep()

            checkBottomSheetFilterTitle()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   Quotes results have loaded
     * When:    Clicking on the filter by button
     * Then:    The bottom sheet opens up and contains the 4 radio buttons
     **/

    @Test
    fun bottomSheetContainsTheFourRadioButtons() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickFilterButton()

            mediumSleep()

            scrollUpFilterScreen()

            checkFixedPriceTextVisible()
            checkEstimatedPriceTextVisible()
            checkFreeWaitingTimeTextVisible()
            checkFreeCancellationTextVisible()
        }

        compareScreenshot(activity)
    }


    /**
     * Given:   User clicks on filter by button
     * When:    The user swipes up
     * Then:    The bottom sheet closes
     **/

    @Test
    fun bottomSheetGetsClosedWhenSwipingUp() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickFilterButton()

            mediumSleep()

            scrollDownFilterScreen()

            mediumSleep()

            checkBottomSheetFilterTitleIsNotVisible()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   Results have been loaded in the quotes list
     * When:    The user presses on the sort by button
     * Then:    The bottom sheet opens
     **/

    @Test
    fun bottomSheetOpensWhenPressingOnSortBy() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickSortByButton()

            mediumSleep()

            checkBottomSheetSortByTitle()
        }

        compareScreenshot(activity)
    }


    /**
     * Given:   Results have been loaded in the quotes list
     * When:    The user presses on the sort by button and then on the X button
     * Then:    The bottom sheet closes
     **/

    @Test
    fun bottomSheetClosesAfterPressingX() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickSortByButton()

            mediumSleep()

            clickXButton()

            mediumSleep()

            checkBottomSheetSortByTitleIsNotVisible()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   Results have been loaded in the quotes list
     * When:    The user presses on the sort by button
     * Then:    The bottom sheet contains the radio buttons and PRICE button should be selected by default
     **/

    @Test
    fun bottomSheetContainsSortByCheckboxes() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickSortByButton()

            mediumSleep()

            checkPriceTextVisible()

            checkDriverArrivalTextVisible()

            checkPriceCheckboxIsSelected()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   Results have been loaded in the quotes list
     * When:    The user presses on the sort by button and then on the Driver Arrival checkbox
     * Then:    The driver arrival checbox is selected
     **/

    @Test
    fun bottomSheetSortByDrivalArrivalIsSelected() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickSortByButton()

            mediumSleep()

            clickOnDriverArrivalCheckbox()

            checkDrivelArrivalCheckboxIsSelected()
        }

        compareScreenshot(activity)
    }

    /**
     * Given:   Results have been loaded in the quotes list
     * When:    The user opens up the sort bottom sheet and then presses the Driver Arrival checkbox
     * Then:    The quotes values should be ordered by ETA
     **/

    @Test
    fun quotesListGetsSortedByDriverArrival() {
        val activity = startActivity()

        quotes {
            clickPickUpAddressField()

            search(TestData.SEARCH_ADDRESS)

            clickBakerStreetResult()

            clickDestinationAddressField()
        }

        serverRobot {
            addressListResponse(HttpURLConnection.HTTP_OK, TestData.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HttpURLConnection.HTTP_OK, TestData.PLACE_DETAILS_EXTRA)
        }

        quotes {
            search(TestData.SEARCH_ADDRESS_EXTRA)

            clickOxfordStreetResult()
        }

        quotes {
            mediumSleep(2000)

            clickSortByButton()

            mediumSleep()

            clickOnDriverArrivalCheckbox()

            mediumSleep()

            clickSortBySaveButton()

            mediumSleep()

            checkFirstItemOfQuoteListSortedByDriverArrival()
        }

        compareScreenshot(activity)
    }



}