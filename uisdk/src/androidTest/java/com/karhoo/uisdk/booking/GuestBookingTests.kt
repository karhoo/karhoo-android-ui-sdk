package com.karhoo.uisdk.booking

import android.content.Intent
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.address.address
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.ServerRobot
import com.karhoo.uisdk.common.ServerRobot.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.common.ServerRobot.Companion.VEHICLES_V2_ASAP
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.DESTINATION_TRIP
import com.karhoo.uisdk.util.TestData.Companion.ORIGIN_TRIP
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestSDKConfig
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class GuestBookingTests : Launch {

    @get:Rule
    val activityRule: ActivityTestRule<BookingActivity> =
            ActivityTestRule(BookingActivity::class.java, false, false)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    @get:Rule
    var wireMockRule = WireMockRule(WireMockConfiguration.options().port(UiSDKTestConfig.PORT_NUMBER), false)

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private var intent: Intent? = null

    @Before
    fun setup() {
        KarhooUISDK.setConfiguration(TestSDKConfig(context = InstrumentationRegistry.getInstrumentation()
                .targetContext, authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "organisationId")))
    }

    @After
    fun teardown() {
        KarhooUISDK.setConfiguration(TestSDKConfig(context = InstrumentationRegistry.getInstrumentation()
                .targetContext, authenticationMethod = AuthenticationMethod.KarhooUser()))
        intent = null
        wireMockRule.resetAll()
    }

    override fun launch(intent: Intent?) {
        intent?.let {
            activityRule.launchActivity(it)
        } ?: run {
            activityRule.launchActivity(this.intent)
        }
    }

    /**
     * Given:   I can see some quotes, ETA selected as default
     * When:    I check all the elements
     * Then:    I can see all elements expected: expand chevron, ETA and Price tabs, fleet logo holder,
    fleet name, Price, Fare type, car category, category tabs
     **/
    @Test
    fun fullQuoteListCheckGuest() {
        serverRobot {
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_V2_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
        } result {
            fullASAPQuotesListCheckGuest()
        }
    }

    /**
     * Given:   I land on the booking screen as a guest user
     * When:    I check all the elements on the screen
     * Then:    I can see: Burger menu button enabled, "Add pick up" and "Add destination" in
    address fields, NO rides button, NO locate me button, NO prebook icon.
     **/
    @Test
    fun emptyBookingScreenGuestCheckout() {
        booking(this) {
            sleep()
        } result {
            guestCheckoutEmptyFullCheck()
        }
    }

    /**
     * Given:   I am on the guest checkout booking screen
     * When:    I select the pickup field
     * Then:    I am taken to the address page
     * And:     I can check the following: Enter pickup in toolbar, powered by google logo is
    visible, set pin on map and use current location buttons are not visible.
     **/
    @Test
    fun addressScreenCheckFromPickupGuestCheckout() {
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            sleep()
        } result {
            checkAddressScreenFromPickupGuestCheckout()
        }
    }

    /**
     * Given:   I am on the guest checkout booking screen
     * When:    I select the destination field
     * Then:    I am taken to the address page
     * And:     I can check the following: Enter destination in toolbar, powered by google logo is
    visible, set pin on map and use current location buttons are not visible.
     **/
    @Test
    fun addressScreenCheckFromDestinationGuestCheckout() {
        booking(this) {
            clickDestinationAddressField()
        }
        address {
            sleep()
        } result {
            checkAddressScreenFromDestinationGuestCheckout()
        }
    }

    /**
     * Given:   I am on the guest checkout mode
     * When:    I search for pick up and destination addresses
     * And:     I select them
     * Then:    I can see both addresses populated in the correct fields on the booking screen
     **/
    @Test
    fun searchAddressesTest() {
        serverRobot {
            addressListResponse(HTTP_OK, ServerRobot.PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, ServerRobot.PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            sleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, ServerRobot.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, ServerRobot.PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS_EXTRA)
            sleep()
            clickOxfordStreetResult()
        }
        booking {
            sleep()
        } result {
            bothSelectedAddressesAreVisible()
        }
    }

    /**
     * Given:   I am on the guest checkout mode
     * When:    I search for pick up and destination addresses
     * And:     I select them
     * Then:    The booking screen populates the quotes as expecte
     **/
    @Test
    fun searchAddressesAndGetQuotesTest() {
        serverRobot {
            addressListResponse(HTTP_OK, ServerRobot.PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, ServerRobot.PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS)
            sleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, ServerRobot.PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, ServerRobot.PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        serverRobot {
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_V2_ASAP)
        }
        address {
            search(TestData.SEARCH_ADDRESS_EXTRA)
            sleep()
            clickOxfordStreetResult()
        }
        booking {
            sleep()
        } result {
            fullASAPQuotesListCheckGuest()
        }
    }

    /**
     * Given:   I am on the guest checkout booking screen
     * When:    I select the menu button
     * Then:    I can see the following options in the menu: Feedback, Help, About
     **/
    @Test
    fun checkMenuItemsGuestCheckout() {
        booking(this) {
            pressMenuButton()
        } result {
            checkSideMenuGuestCheckoutIsShown()
        }
    }

    /**
     * Given:   I am on the guest checkout booking screen
     * When:    I select the pick up address field
     * And:     I select an address
     * Then:    I am returned to the booking screen and can see the following: Address in pickup
     * field, pickup pin not present, Add destination field visible, prebook button enabled
     **/
    @Test
    fun flowGuestCheckoutBookingToPickUpAddressToBooking() {
        serverRobot {
            addressListResponse(HTTP_OK, ServerRobot.PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, ServerRobot.PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS)
            sleep()
            clickBakerStreetResult()
        }
        booking {
            sleep()
        } result {
            flowBookingPickupBookingCheck()
            // TODO check pickupPinIcon
        }
    }

    /**
     * Given:   I have selected a quote on guest checkout mode
     * And:     I am on the guest details page
     * When:    I press the close button
     * Then:    I am returned to the quote screen
     **/
    @Test
    fun closingTheGuestDetailsPage() {
        serverRobot {
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_V2_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
            pressFirstQuote()
            sleep()
        } result {
            checkGuestDetailsPageIsShown()
        }
        booking {
            pressCloseGuestDetailsPage()
        } result {
            fullASAPQuotesListCheckGuest()
        }
    }

    /**
     * Given:   I am on the guest booking details screen
     * When:    I check all the elements before entering any details
     * Then:    I can see: Fleet logo and name, Vehicle capacity details, Close button enabled,
     * ETA, Price amount and type, All fields are visible: First name, Last name, Email, phone
     * number country code, phone number, additional comments, Add card button is enabled, Terms and
     * conditions text is visible, Book ride button is disabled.
     **/
    @Test
    fun emptyGuestDetailsPageFullCheck() {
        serverRobot {
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_V2_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            sleep()
            pressFirstQuote()
            sleep()
        } result {
            fullCheckEmptyGuestDetailsPage()
        }
    }

    //    /**
    //     * Given:   I am on the guest booking details screen
    //     * When:    I check all the elements after entering any details
    //     * Then:    I can see correctly filled: Fleet logo and name, Vehicle capacity details, Close
    //     * button enabled,ETA, Price amount and type, All fields are visible: First name, Last name,
    //     * Email, phone number country code, phone number, additional comments, card number is
    //     * visible, card edit button is enabled, Terms and conditions text is visible, Book ride button
    //     * is enabled.
    //     **/
    //    @Test
    //    // TODO implement adding a card
    //    fun filledGuestDetailsPageFullCheck() {
    //        serverRobot {
    //            availabilitiesResponse(HTTP_CREATED, AVAILABILITIES)
    //            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
    //            quotesResponse(HTTP_OK, QUOTE_TRIP_ASAP)
    //            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
    //        }
    //        booking(this, INITIAL_TRIP_INTENT) {
    //            sleep()
    //            pressFirstQuote()
    //            sleep()
    //            fillCorrectInfoGuestDetails()
    //            enterCardDetails()
    //            sleep()
    //        } result {
    //            fullCheckFilledGuestDetailsPage()
    //        }
    //    }

    companion object {

        private val origin = ORIGIN_TRIP
        private val destination = DESTINATION_TRIP
        private val initialTripDetails = TripInfo(origin = origin, destination = destination)

        val CLEAN_TRIP_INTENT = Intent().apply {
            putExtras(Bundle().apply {
                putParcelable(BookingActivity.Builder.EXTRA_TRIP_DETAILS, TRIP)
            })
        }

        val INITIAL_TRIP_INTENT = Intent().apply {
            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, initialTripDetails)
        }
    }
}