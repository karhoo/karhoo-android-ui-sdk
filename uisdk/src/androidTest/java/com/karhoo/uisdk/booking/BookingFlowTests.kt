package com.karhoo.uisdk.booking

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.address.address
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.ServerRobot.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.common.ServerRobot.Companion.PLACE_DETAILS
import com.karhoo.uisdk.common.ServerRobot.Companion.PLACE_SEARCH_RESULT
import com.karhoo.uisdk.common.ServerRobot.Companion.REVERSE_GEO_SUCCESS
import com.karhoo.uisdk.common.ServerRobot.Companion.REVERSE_GEO_SUCCESS_ALTERNATIVE
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.rides.rides
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.MEDIUM
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_GENERAL_ADDRESS
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class BookingFlowTests : Launch {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @get:Rule
    val activityRule: ActivityTestRule<BookingActivity> =
            ActivityTestRule(BookingActivity::class.java, false, false)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    @get:Rule
    var wireMockRule = WireMockRule(options().port(UiSDKTestConfig.PORT_NUMBER), false)

    private val intent = Intent()

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    /**
     * Given:   I am on the booking screen
     * When:    I press the rides button
     * Then:    I am taken to the Rides screen - Upcoming section
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userNavigatesFromBookingToRides() {
        booking(this) {
            pressRidesButton()
        }
        rides {
            sleep()
        } result {
            checkRidesScreenIsShown()
        }
    }

    /**
     * Given:   I am on the rides screen
     * When:    I press Close
     * Then:    I am returned to the Booking screen
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun userNavigatesFromBookingToRidesToBooking() {
        booking(this) {
            pressRidesButton()
        }
        rides {
            clickBackButtonRidesScreen()
        }
        booking {
            result {
                checkBookingScreenIsShown()
            }
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I click on the destination field
     * Then:    I am taken to the address screen
     **/
    @Test
    fun userNavigatesFromBookingToAddressDestination() {
        booking(this) {
            clickDestinationAddressField()
        }
        address {
            result {
                checkAddressDestinationPageIsShown()
            }
        }
    }

    /**
     * Given:   I am on the Destination address screen
     * When:    I press "X" after having entered some text in the address field
     * Then:    The Destination address field is cleared
     **/
    @Test
    fun checkDestinationFieldIsClearedAfterEnteringText() {
        booking(this) {
            clickDestinationAddressField()
        }
        address {
            search(SEARCH_GENERAL_ADDRESS)
            sleep()
            clearAddressSearchList()
        } result {
            enterDestinationHintIsVisible()
        }
    }

    /**
     * Given:   No text is entered in the destination address search field
     * When:    I check the address field
     * Then:    The "X" to clear is disabled
     **/
    @Test
    fun clearButtonIsNotClickableIfNoTextEnteredInPickUpSearch() {
        booking(this) {
            clickDestinationAddressField()
        }
        address {
        } result {
            clearAddressButtonIsNotClickable()
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I go to the address screen and press back
     * Then:    I am returned to booking screen
     **/
    @Test
    fun userNavigatesFromBookingToAddressToBooking() {
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            pressBackButtonToolbar()
        }
        booking {
            result {
                checkBookingScreenIsShown()
            }
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I click on the pickup field
     * Then:    I am taken to the address screen
     **/
    @Test
    fun userNavigatesFromBookingToAddressPickup() {
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            result {
                checkAddressPickUpPageIsShown()
            }
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I click on the menu button
     * Then:    The side menu is visible, all buttons are visible
     **/
    @Test
    fun openSideMenuByClicking() {
        booking(this) {
            pressMenuButton()
        } result {
            checkSideMenuIsShown()
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I select the pick up address field
     * And:     I select set "pin location on map"
     * Then:    I am taken to the map screen
     **/
    @Test
    fun userNavigatesToMapFromAddressPickUp() {
        booking(this) {
            clickPickUpAddressField()
        }
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        address {
            clickSetPinOnMapButton()
        } result {
            fullCheckSetAddressOnMapPickup()
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I select the pick up address field
     * And:     I select set "pin location on map"
     * Then:    I am taken to the map screen
     **/
    @Test
    fun userNavigatesToMapFromAddressDestination() {
        booking(this) {
            clickDestinationAddressField()
        }
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        address {
            clickSetPinOnMapButton()
        } result {
            fullCheckSetAddressOnMapDestination()
        }
    }

    /**
     * Given:   The User is on the booking screen
     * When:    The user clicks on Destination
     * Then:    The user can see the text "Enter Destination" in the Toolbar on the address screen
     **/
    @Test
    fun userIsPromptedToEnterDestinationAddress() {
        booking(this) {
            clickDestinationAddressField()
        }
        address {
            sleep()
        } result {
            checkAddressDestinationPageIsShown()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   I have entered an address which is not my current location
     * When:    When I press the "locate me button"
     * Then:    The pick up address is overwritten with my current location
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun locateMeOverwritesEnteredAddress() {
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS)
            clickBakerStreetResult()
        }
        booking {
            sleep()
        } result {
            selectedPickupAddressIsVisible(address = TestData.SELECTED_ADDRESS)
        }
        booking {
            sleep(MEDIUM)
            clickOnLocateMeButton()
            sleep()
        } result {
            reverseGeoAddressVisiblePickUp(address = TestData.REVERSE_GEO_DISPLAY_ADDRESS)
        }
    }

    /**
     * Given:   I am on the pickup address screen
     * When:    I tap on the "Set current location" button
     * Then:    I am returned to map screen and reverse geolocation address is displayed correctly
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun setCurrentLocationButtonReverseGeolocationPickUp() {
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS_ALTERNATIVE)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        serverRobot {
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        address {
            clickGetCurrentLocation()
        }
        booking {
            sleep()
        } result {
            reverseGeoAddressVisiblePickUp(address = TestData.REVERSE_GEO_DISPLAY_ADDRESS)
        }
    }

    /**
     * Given:   I am on the dropOff address screen
     * When:    I tap on the "Set current location" button
     * Then:    I am returned to map screen and reverse geolocation address is displayed correctly
     **/
    @Test
    fun setCurrentLocationButtonReverseGeolocationDropOff() {
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS_ALTERNATIVE)
        }
        booking(this) {
            clickDestinationAddressField()
        }
        serverRobot {
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        address {
            clickGetCurrentLocation()
        }
        booking {
            sleep()
        } result {
            reverseGeoAddressVisibleDropOff(address = TestData.REVERSE_GEO_DISPLAY_ADDRESS)
        }
    }

    /**
     * Given:   I am on the booking screen
     * When:    I select pick up address
     * And:     Select an address from the list
     * Then:    I am returned to the booking screen and the address selected is populated in
     * the pick up field, the pickup pin is visible on the map
     **/
    @Test
    fun selectingPickUpAddressFlow() {
        serverRobot {
            successfulToken()
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
            reverseGeocodeResponse(HTTP_OK, REVERSE_GEO_SUCCESS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            clickBakerStreetResult()
        }
        booking {
            sleep()
        } result {
            selectedPickupAddressIsVisible(address = TestData.SELECTED_ADDRESS)
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(this.intent)
    }
}
