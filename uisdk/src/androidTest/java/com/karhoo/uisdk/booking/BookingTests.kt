package com.karhoo.uisdk.booking

import android.content.Intent
import android.os.Bundle
import androidx.test.espresso.NoActivityResumedException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.address.address
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.ADDRESSES_IDENTICAL
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.DESTINATION_TRIP
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.GENERAL_ERROR
import com.karhoo.uisdk.util.TestData.Companion.LONG
import com.karhoo.uisdk.util.TestData.Companion.MEDIUM
import com.karhoo.uisdk.util.TestData.Companion.NO_AVAILABILITY
import com.karhoo.uisdk.util.TestData.Companion.ORIGIN_TRIP
import com.karhoo.uisdk.util.TestData.Companion.PAYMENTS_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.PLACE_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH_RESULT
import com.karhoo.uisdk.util.TestData.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS_ALTERNATIVE
import com.karhoo.uisdk.util.TestData.Companion.SHORT
import com.karhoo.uisdk.util.TestData.Companion.TIMEOUT
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER_NO_NUMBER_PLATE
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.util.TestData.Companion.VEHICLES_ASAP
import com.karhoo.uisdk.util.TestData.Companion.setUserInfo
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class BookingTests : Launch {

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
        setUserInfo(BRAINTREE)
    }

    @After
    fun teardown() {
        intent = null
        wireMockRule.resetAll()
    }

    /**
     * Given:   I am on the Booking screen
     * When:    An error occurs
     * Then:    The snackbar displays the correct message
     **/
    @Test
    @AllowFlaky(attempts = 3)
    fun checkSnackbarErrorText() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HttpURLConnection.HTTP_BAD_REQUEST, GENERAL_ERROR)
        }
        booking(this, null) {
            sleep(MEDIUM)
        } result {
            checkSnackbarWithText(R.string.K0001)
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I enter an address with no coverage
     * Then:    I am shown the snackbar about no coverage
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun snackbarShowsToUserWhenNoAvailability() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN, TIMEOUT)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS, TIMEOUT)
            quoteIdResponse(HttpURLConnection.HTTP_BAD_REQUEST, NO_AVAILABILITY)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            sleep()
        } result {
            checkErrorIsShown(R.string.no_availability)
            contactButtonSnackbarIsEnabled()
        }
    }

    @Test
    @AllowFlaky(attempts = 3)
    fun snackbarShowsToUserWhenNoAvailabilityAfterBackgrounding() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN, TIMEOUT)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_BAD_REQUEST, NO_AVAILABILITY)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            waitFor(MEDIUM)
            try {
                //Send app to background
                pressDeviceBackButton()
            } catch (ex: NoActivityResumedException) {
            }
        }
        booking(this, CLEAN_TRIP_INTENT) {
            waitFor(SHORT)
        } result {
            checkErrorIsShown(R.string.no_availability)
            contactButtonSnackbarIsEnabled()
        }
    }

    /**
     * Given:   I have 2 addresses entered
     * When:    I press the swap button
     * Then:    Addresses are swapped
     **/
    @Test
    fun addressesSwapSuccessfully() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            quoteIdResponse(HttpURLConnection.HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            pressSwapAddressesButton()
        } result {
            pickUpisNowDropoff(dropoffAddress = TestData.ADDRESS_ORIGIN)
            dropoffIsNowPickUp(pickupAddress = TestData.ADDRESS_DESTINATION)
        }
    }

    /**
     * Given:   I can see some quotes, ETA selected as default
     * When:    I check all the elements
     * Then:    I can see all elements expected: expand chevron, ETA and Price tabs, fleet logo holder,
    fleet name, Price, Fare type, car category, category tabs
     **/
    @Test
    fun fullQuoteListCheckETA() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep(TestData.LONG)
        } result {
            fullASAPQuotesListCheck()
        }
    }

    /**
     * Given:   I have received quotes
     * When:    I press to expand the quotes
     * Then:    The list is expanded
     **/
    @Test
    fun userExpandsQuoteList() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
        } result {
            quotesListNotExpanded(TestData.THIRD_FLEET)
        }
        booking {
            pressExpandListButton()
        } result {
            quotesListIsExpanded(TestData.THIRD_FLEET)
        }
    }

    /**
     * Given:   I have expanded the quotes list
     * When:    I press to minimise it
     * Then:    The list is minimised.
     **/
    @Test
    fun userMinimisesQuoteList() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
            pressExpandListButton()
        } result {
            TestData.FLEET_INFO_ALT.name?.let { quotesListIsExpanded(it) }
        }
        booking {
            pressExpandListButton()
        } result {
            TestData.FLEET_INFO_ALT.name?.let { quotesListNotExpanded(it) }
        }
    }

    /**
     * Given:   I have opened the prebook window
     * When:    I look at the window - top
     * Then:    I can see the info that the prebook time will be in local time
     **/
    @Test
    @AllowFlaky(attempts = 1)
    fun checkingForLocalTimeMessageOnPrebook() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            pressPrebookButton()
            sleep()
        } result {
            localTimeMessageIsDisplayed()
        }
    }

    /**
     * Given:   I have the prebook window opened
     * When:    I press "Cancel"
     * Then:    The prebook window is closed
     **/
    @Test
    fun closingPrebookWindow() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN, TIMEOUT)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            sleep()
            pressPrebookButton()
            sleep()
            pressCancelPrebookWindow()
        } result {
            prebookWindowNotVisible()
        }
    }

    /**
     * Given:   I have the prebook window opened
     * When:    I press "Cancel" after selecting a date (On time selector)
     * Then:    The prebook window is closed
     **/
    @Test
    fun closingPrebookWindowAfterDate() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN, TIMEOUT)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            sleep()
            pressPrebookButton()
            sleep()
            pressOKPrebookWindow()
            sleep()
            pressCancelPrebookWindow()
        } result {
            prebookWindowNotVisible()
        }
    }

    /**
     * Given:   I have the prebook window opened
     * When:    I press "OK" after selecting a date (On time selector)
     * Then:    The user is returned to the map screen
     **/
    @Test
    @AllowFlaky(attempts = 3)
    fun prebookWindowClosesAfterSelectingDateAndTime() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN, TIMEOUT)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            sleep()
            pressPrebookButton()
            sleep()
            pressOKPrebookWindow()
            sleep()
            pressOKPrebookWindow()
        } result {
            prebookWindowNotVisible()
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I have a successful reversegeo event
     * Then:    I can see the address in pickup field
     **/
    @Test
    fun checkReverseGeoAddressInPickUpField() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            sleep()
        } result {
            reverseGeoAddressVisiblePickUp(address = TestData.REVERSE_GEO_DISPLAY_ADDRESS)
        }
    }

    /**
     * Given:   I have the side menu opened
     * When:    I click on the map
     * Then:    The side menu is closed
     **/
    @Test
    @AllowFlaky(attempts = 1)
    fun closeSideMenuByClickingOnMap() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            pressMenuButton()
            sleep()
            pressOutOfSideMenu()
        } result {
            sideMenuIsNotVisible()
        }
    }

    /**
     * Given:   I have no pick up or destination address entered
     * When:    I look at the address fields
     * Then:    I cannot see the prebook button
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun noPrebookButtonVisibleWhenNoAddressEntered() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
        }
        booking(this, null) {
            sleep()
        } result {
            prebookButtonIsNotVisible()
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I enter identical addresses in pick up and drop off
     * Then:    I see an error informing me that pick up and drop off cannot be identical
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun pickUpAndDropOffAddressesCannotBeTheSame() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_BAD_REQUEST, ADDRESSES_IDENTICAL)
        }
        booking(this, IDENTICAL_ADDRESSES_TRIP_INTENT) {
            sleep(MEDIUM)
        } result {
            samePickUpAndDestinationErrorIsDisplayed()
        }
    }

    /**
     * Given:   I have entered a prebook time
     * When:    I press Locate me
     * Then:    The time should not be cleared
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun locateMeDoesNotClearPrebookTime() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this) {
            pressPrebookButton()
            pressOKPrebookWindow()
            sleep()
            pressOKPrebookWindow()
        } result {
            prebookLogoNotVisible()
        }
        serverRobot {
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS_ALTERNATIVE)
        }
        booking {
            clickOnLocateMeButton()
            sleep()
        } result {
            prebookLogoNotVisible()
        }
    }

    /**
     * Given:   I have entered a prebook time
     * When:    I press change the pickup address
     * Then:    The time should not be cleared
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun changeAddressDoesNotClearPrebookTime() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking(this) {
            pressPrebookButton()
            pressOKPrebookWindow()
            pressOKPrebookWindow()
        } result {
            prebookLogoNotVisible()
        }
        booking {
            waitFor(MEDIUM)
            clickPickUpAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS)
            clickBakerStreetResult()
        }
        booking {
            waitFor(SHORT)
        } result {
            selectedPickupAddressIsVisible(address = TestData.SELECTED_ADDRESS)
            prebookLogoNotVisible()
        }
    }

    /**
     * Given:   I have requested ASAP quotes
     * When:    I select one of the quotes
     * Then:    I can see the Book a Ride screen
     **/
    @Test
    fun bookARideScreenIsDisplayed() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            waitFor(SHORT)
            pressFirstQuote()
            waitFor(MEDIUM)
        } result {
            bookARideScreenIsVisible()
        }
    }

    /**
     * Given:   I can see the book a ride screen
     * When:    I press the close button
     * Then:    I am returned to the quotes list screen
     **/
    @Test
    fun closingBookARideScreen() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
            pressFirstQuote()
            sleep()
            pressCloseBookARideScreen()
        } result {
            fullASAPQuotesListCheck()
            bookARideScreenIsNotVisible()
        }
    }

    /**
     * Given:   I have selected an ASAP quote
     * When:    I check all the elements
     * Then:    I can see the following: fleet logo, fleet name, vehicle type, vehicle capacity,
     * ETA, Est. price, Payment widget (enabled as button), T&Cs (including clickable links),
     * Book button enabled.
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun ASAPBookARideScreenFullCheck() {
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_OK, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
            pressFirstQuote()
            sleep(MEDIUM)
        } result {
            fullCheckBookARideScreenASAP()
        }
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
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
            bookingWithNonceResponse(HTTP_OK, TRIP)
            bookingStatusResponse(code = HTTP_OK, response = TRIP_STATUS_DER, trip = TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = DRIVER_TRACKING, trip = TRIP.tripId)
            bookingDetailsResponse(code = HTTP_OK, response = TRIP_DER_NO_NUMBER_PLATE, trip = TRIP.tripId)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            waitFor(SHORT)
            pressFirstQuote()
            waitFor(MEDIUM)
            pressBookRideButton()
            waitFor(LONG)
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
