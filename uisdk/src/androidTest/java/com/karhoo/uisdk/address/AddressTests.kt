package com.karhoo.uisdk.address

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.uisdk.base.address.AddressType
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.address.AddressActivity
import com.karhoo.uisdk.util.TestData.Companion.GENERAL_ERROR
import com.karhoo.uisdk.util.TestData.Companion.NO_ADDRESS_FOUND
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH_AIRPORT
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_AIRPORT_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_GENERAL_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_INCORRECT_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.USER
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND

@RunWith(AndroidJUnit4::class)
class AddressTests : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(UiSDKTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<AddressActivity> =
            ActivityTestRule(AddressActivity::class.java, false, false)

    private val intent = Intent().apply {
        putExtra("address::type", AddressType.PICKUP)
    }

    @Before
    fun setUp() {
        preferences {
            setUserPreference(USER)
        }
    }

    @After
    fun tearDown() {
        wireMockRule.resetAll()
    }

    /**
     * Given:   The User is on the address search screen
     * When:    The user has not entered any text.
     * Then:    The user can see the text "Enter Destination/Pickup" in the Toolbar
     **/
    @Test
    fun userIsPromptedToEnterPickupAddress() {
        address(this) {
            shortSleep()
        } result {
            checkAddressPickUpPageIsShown()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
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
            addressListResponse(HTTP_CREATED, PLACE_SEARCH)
        }
        address(this) {
            search(SEARCH_GENERAL_ADDRESS)
            shortSleep()
        } result {
            areAddressesAvailable()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   A user has entered an address that does not exist
     * When:    The search is completed
     * Then:    No results are shown
     **/
    @Test
    @AllowFlaky(attempts = 5)
    fun searchingForAnInvalidAddressReturnsNoResult() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_NOT_FOUND, NO_ADDRESS_FOUND)
        }
        address(this) {
            search(SEARCH_INCORRECT_ADDRESS)
            shortSleep()
        } result {
            noAddressesFound()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   A user is on the address screen
     * When:    User presses back button
     * Then:    User leaves address screen
     **/
    @Test
    fun userLeavesAddressScreenWhenPressingBackButtonFromToolbar() {
        address(this) {
            pressBackButtonToolbar()
        }
    }

    /**
     * Given:   A user enters an address
     * When:    The user clears the address
     * Then:    There is no address search results visible.
     **/
    @Test
    fun noAddressSearchResultsAfterClearingAddressSearch() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_CREATED, PLACE_SEARCH)
        }

        address(this) {
            search(SEARCH_GENERAL_ADDRESS)
            shortSleep()
            clearAddressSearchList()
        } result {
            noAddressesFound()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   The user has entered an address
     * When:    The user clears the address
     * Then:    User can successfully enter a new address.
     **/
    @Test
    fun userSuccessfullyEntersNewAddressAfterClearingPreviousSearch() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_CREATED, PLACE_SEARCH)
        }

        address(this) {
            search(SEARCH_GENERAL_ADDRESS)
            shortSleep()
            clearAddressSearchList()
        } result {
            noAddressesFound()
        }

        address {
            search(SEARCH_GENERAL_ADDRESS)
            shortSleep()
        } result {
            areAddressesAvailable()
        }


    }

    /**
     * Given:   The user has entered a valid address
     * When:    The list of addresses has loaded
     * Then:    The "Powered by Google Logo is visible"
     **/
    @Test
    fun poweredByGoogleVisibleOnValidAddressSearchScreen() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_CREATED, PLACE_SEARCH)
        }
        address(this) {
            search(SEARCH_GENERAL_ADDRESS)
            shortSleep()
        } result {
            poweredByGoogleFound()
        }

    }

    /**
     * Given:   The user has entered a invalid address
     * When:    No results are shown
     * Then:    The "Powered by Google Logo is visible"
     **/
    @Test
    fun poweredByGoogleVisibleOnInvalidAddressSearch() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_NOT_FOUND, NO_ADDRESS_FOUND)
        }

        address(this) {
            search(SEARCH_INCORRECT_ADDRESS)
            shortSleep()
        } result {
            poweredByGoogleFound()
        }
    }

    /**
     * Given:   The user has no recent addresses
     * When:    The user is on the address search list
     * Then:    The "Powered by Google Logo is visible"
     **/
    @Test
    fun poweredByGoogleVisibleOnEmptyRecentAddressSearch() {
        address(this) {
        } result {
            poweredByGoogleFound()
        }
    }

    /**
     * Given:   The user has no recent addresses
     * When:    The user is on the address search screen
     * Then:    The user can see "No recent results"
     **/
    @Test
    fun noRecentAddressesDisplayedIfUserHasNoRecent() {
        address(this) {
        } result {
            noRecentFound()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   I am on the address screen
     * When:    An error occurs
     * Then:    The snackbar displays the correct message
     **/
    @Test
    fun checkSnackbarErrorText() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_INTERNAL_ERROR, GENERAL_ERROR)
        }
        address(this) {
            search(SEARCH_GENERAL_ADDRESS)
        } result {
            checkSnackbarWithText("General request error. [K0001]")
        }
    }

    /** Given:   The user has recent addresses
     * When:    The user is on the address search screen
     * Then:    The user can see recent addresses
     **/
    @Test
    fun recentAddressesAreAvailableWhenAddressScreenIsLaunched() {
        address {
            setDefaultRecents()
            launch()
        } result {
            areRecentAddressesAvailable()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   I am on the Pickup address screen
     * When:    I press "X" after having entered some text in the address field
     * Then:    The Pickup address field is cleared
     **/
    @Test
    fun checkPickupFieldIsClearedAfterEnteringText() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_INTERNAL_ERROR, GENERAL_ERROR)
        }
        address(this) {
            search(SEARCH_GENERAL_ADDRESS)
            shortSleep()
            clearAddressSearchList()
        } result {
            enterPickupHintIsVisible()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   No text is entered in the pickup address search field
     * When:    I check the address field
     * Then:    The "X" to clear is disabled
     **/
    @Test
    fun clearButtonIsNotClickableIfNoTextEnteredInPickUpSearch() {
        address(this) {
        } result {
            clearAddressButtonIsNotClickable()
            setLocationOnMapButtonIsEnabled()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   I am on the Address Screen
     * When:    I look a the second option
     * Then:    Get current location button should be visible
     */
    @Test
    fun checkGetCurrentLocationField() {
        address(this) {
        } result {
            checkGetLocationIsVisible()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   I am on the Address Screen
     * When:    I look at the first option
     * Then:    Set pin on map field should be visible
     */
    @Test
    fun checkSetPinOnMapField() {
        address(this) {
        } result {
            checkSetPinOnMapIsVisible()
            getCurrentLocationIsEnabled()
        }
    }

    /**
     * Given:   I search for an address that has an Airport POI associated with it
     * When:    I look at the address search result
     * Then:    I can see the plane symbol next to the address
     **/
    @Test
    fun airportSymbolCheckAddressSearch() {
        serverRobot {
            successfulToken()
            addressListResponse(HTTP_CREATED, PLACE_SEARCH_AIRPORT)
        }
        address(this) {
            search(SEARCH_AIRPORT_ADDRESS)
        } result {
            airportSymbolIsVisible()
        }
    }

    /**
     * Given:   I have recently searched and selected an Airport POI address
     * When:    I look at the recent address list
     * Then:    I can see the plane symbol next to the address
     **/
    @Test
    fun airportSymbolCheckRecentAddress() {
        address {
            setAirportRecent()
            launch()
        } result {
            airportSymbolIsVisible()
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(this.intent)
    }
}