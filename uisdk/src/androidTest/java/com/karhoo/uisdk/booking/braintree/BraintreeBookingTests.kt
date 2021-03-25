package com.karhoo.uisdk.booking.braintree

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
import com.karhoo.uisdk.booking.booking
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.BRAINTREE
import com.karhoo.uisdk.util.TestData.Companion.ADDRESSES_IDENTICAL
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_DESTINATION
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_ORIGIN
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.DESTINATION_TRIP
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.FLEET_INFO_ALT
import com.karhoo.uisdk.util.TestData.Companion.GENERAL_ERROR
import com.karhoo.uisdk.util.TestData.Companion.NO_AVAILABILITY
import com.karhoo.uisdk.util.TestData.Companion.ORIGIN_TRIP
import com.karhoo.uisdk.util.TestData.Companion.PAYMENTS_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.PLACE_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH_RESULT
import com.karhoo.uisdk.util.TestData.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_DISPLAY_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.util.TestData.Companion.REVERSE_GEO_SUCCESS_ALTERNATIVE
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.SELECTED_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.LAST_FLEET
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
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class BraintreeBookingTests : Launch {

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
        serverRobot {
            successfulToken()
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
        }
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
            reverseGeocodeResponse(HTTP_BAD_REQUEST, GENERAL_ERROR)
        }
        booking(this, null) {
            mediumSleep()
        } result {
            // R.string.kh_uisdk_K0001
            checkSnackbarWithText("General request error. [K0001]")
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS, TIMEOUT)
            quoteIdResponse(HTTP_BAD_REQUEST, NO_AVAILABILITY)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            shortSleep()
        } result {
            checkErrorIsShown(R.string.kh_uisdk_no_availability)
            contactButtonSnackbarIsEnabled()
        }
    }

    @Test
    @AllowFlaky(attempts = 3)
    fun snackbarShowsToUserWhenNoAvailabilityAfterBackgrounding() {
        serverRobot {
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_BAD_REQUEST, NO_AVAILABILITY)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            mediumSleep()
            try {
                //Send app to background
                pressDeviceBackButton()
            } catch (ex: NoActivityResumedException) {
            }
        }
        booking(this, CLEAN_TRIP_INTENT) {
            shortSleep()
        } result {
            checkErrorIsShown(R.string.kh_uisdk_no_availability)
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
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            pressSwapAddressesButton()
        } result {
            pickUpisNowDropoff(dropoffAddress = ADDRESS_ORIGIN)
            dropoffIsNowPickUp(pickupAddress = ADDRESS_DESTINATION)
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            mediumSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
        } result {
            quotesListNotExpanded(LAST_FLEET)
        }
        booking {
            pressExpandListButton()
        } result {
            quotesListIsExpanded(LAST_FLEET)
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HttpURLConnection.HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
            pressExpandListButton()
        } result {
            quotesListIsExpanded(LAST_FLEET)
        }
        booking {
            pressExpandListButton()
        } result {
            quotesListNotExpanded(LAST_FLEET)
        }
    }

    /**
     * Given:   I have opened the prebook window
     * When:    I look at the window - top
     * Then:    I can see the info that the prebook time will be in local time
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun checkingForLocalTimeMessageOnPrebook() {
        serverRobot {
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            pressPrebookButton()
            shortSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            shortSleep()
            pressPrebookButton()
            shortSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            shortSleep()
            pressPrebookButton()
            shortSleep()
            pressOKPrebookWindow()
            shortSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            shortSleep()
            pressPrebookButton()
            shortSleep()
            pressOKPrebookWindow()
            shortSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, null) {
            shortSleep()
        } result {
            reverseGeoAddressVisiblePickUp(address = REVERSE_GEO_DISPLAY_ADDRESS)
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this, CLEAN_TRIP_INTENT) {
            pressMenuButton()
            shortSleep()
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
        booking(this, null) {
            shortSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_BAD_REQUEST, ADDRESSES_IDENTICAL)
        }
        booking(this, IDENTICAL_ADDRESSES_TRIP_INTENT) {
            mediumSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this) {
            pressPrebookButton()
            pressOKPrebookWindow()
            shortSleep()
            pressOKPrebookWindow()
        } result {
            prebookLogoNotVisible()
        }
        serverRobot {
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS_ALTERNATIVE)
        }
        booking {
            clickOnLocateMeButton()
            shortSleep()
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
            mediumSleep()
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            clickBakerStreetResult()
        }
        booking {
            shortSleep()
        } result {
            selectedPickupAddressIsVisible(address = SELECTED_ADDRESS)
            prebookLogoNotVisible()
        }
    }

    /**
     * Given:   I have requested ASAP quotes
     * When:    I select one of the quotes
     * Then:    I can see the Book a Ride screen
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun bookARideScreenIsDisplayed() {
        serverRobot {
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
            pressFirstQuote()
            mediumSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
            pressFirstQuote()
            shortSleep()
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
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_OK, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
            pressFirstQuote()
            mediumSleep()
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
            shortSleep()
            pressFirstQuote()
            mediumSleep()
            pressBookRideButton()
            longSleep()
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
