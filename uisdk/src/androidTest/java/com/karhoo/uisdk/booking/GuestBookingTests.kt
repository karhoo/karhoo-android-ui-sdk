package com.karhoo.uisdk.booking

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.address.address
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.getLocale
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.common.testrunner.UiSDKTestConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.ADYEN_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_PROVIDER
import com.karhoo.uisdk.util.TestData.Companion.BRAINTREE_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.DESTINATION_TRIP
import com.karhoo.uisdk.util.TestData.Companion.DRIVER_TRACKING
import com.karhoo.uisdk.util.TestData.Companion.ORIGIN_TRIP
import com.karhoo.uisdk.util.TestData.Companion.PAYMENTS_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.PLACE_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.PLACE_DETAILS_EXTRA
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH_RESULT
import com.karhoo.uisdk.util.TestData.Companion.PLACE_SEARCH_RESULT_EXTRA
import com.karhoo.uisdk.util.TestData.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.TRIP_DER_NO_NUMBER_PLATE
import com.karhoo.uisdk.util.TestData.Companion.TRIP_STATUS_DER
import com.karhoo.uisdk.util.TestData.Companion.VEHICLES_ASAP
import com.karhoo.uisdk.util.TestData.Companion.VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS
import com.karhoo.uisdk.util.TestData.Companion.VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS_BEFORE_DRIVER_EN_ROUTE
import com.karhoo.uisdk.util.TestData.Companion.VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS_ZERO_MINUTES
import com.karhoo.uisdk.util.TestSDKConfig
import com.karhoo.uisdk.util.extension.toNormalizedLocale
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Ignore
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
                .targetContext, authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "organisation_id")))
    }

    @After
    fun teardown() {
        KarhooUISDK.setConfiguration(TestSDKConfig(context = InstrumentationRegistry.getInstrumentation()
                .targetContext, authenticationMethod = AuthenticationMethod.KarhooUser()))
        KarhooApi.userStore.savedPaymentInfo = null
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
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
        } result {
            fullASAPQuotesListCheckGuest()
        }
    }

    /**
     * Given:   Received some quotes with a cancellation service agreement
     * When:    Checking the quotes
     * Then:    The user can see the free cancellation text
     **/
    @Test
    fun fullQuoteListCheckCancellationTextForGuestUser() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
        } result {
            freeCancellationTextVisible()
        }
    }

    /**
     * Given:   Received some quotes with a cancellation service agreement before driver en route
     * When:    Checking the quotes
     * Then:    The user can see the free cancellation text
     **/
    @Test
    fun fullQuoteListCheckCancellationBeforeDriverEnRouteTextForGuestUser() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS_BEFORE_DRIVER_EN_ROUTE)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
        } result {
            freeCancellationTextVisible()
        }
    }

    /**
     * Given:   Received some quotes with a cancellation service agreement having 0 minutes
     * When:    Checking the quotes
     * Then:    The user can not see the free cancellation text
     **/
    @Test
    fun checkNotVisibleCancellationTextForGuestUserAndZeroMinutesSLA() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS_ZERO_MINUTES)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
        } result {
            freeCancellationTextNotVisible()
        }
    }

    /**
     * Given:   Received some quotes without a cancellation service agreement
     * When:    Checking the quotes
     * Then:    The user can not see the free cancellation text
     **/
    @Test
    fun checkVisibleCancellationTextForGuestUser() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
        } result {
            freeCancellationTextVisible()
        }
    }

    /**
     * Given:   I have selected a quote on guest checkout mode that has a cancellation SLA
     * And:     I am on the guest details page
     * Then:    The cancellation text is shown on the details page
     **/
    @Test
    fun cancellationTextVisibleInTheGuestDetailsPage() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, ADYEN_PROVIDER)
            reverseGeocodeResponse(HTTP_OK, TestData.REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP, locale = getLocale())
            quotesResponse(HTTP_OK, VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            mediumSleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS_EXTRA)
            shortSleep()
            clickOxfordStreetResult()
        }
        booking {
            shortSleep()
            pressFirstQuote()
            shortSleep()
        } result {
            checkCancellationTextInDetailsPageIsShown()
        }
    }

    /**
     * Given:   I have selected a quote on guest checkout mode that has a cancellation SLA
     *          with 0 minutes
     * And:     I am on the guest details page
     * Then:    The cancellation text is NOT shown on the details page
     **/
//    @Test
    fun cancellationTextWithZeroMinutesNotVisibleInTheGuestDetailsPage() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP_WITH_CANCELLATION_AGREEMENTS_ZERO_MINUTES)
        }
        booking(this, INITIAL_TRIP_INTENT) {
            shortSleep()
            pressFirstQuote()
            shortSleep()
        } result {
            checkCancellationTextInDetailsPageIsNotShown()
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
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
        }
        booking(this) {
            shortSleep()
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
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            shortSleep()
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
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            reverseGeocodeResponse(HTTP_OK, TestData.REVERSE_GEO_SUCCESS)
        }
        booking(this) {
            clickDestinationAddressField()
        }
        address {
            shortSleep()
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
    //    @AllowFlaky(attempts = 10)
    fun searchAddressesTest() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            mediumSleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS_EXTRA)
            shortSleep()
            clickOxfordStreetResult()
        }
        booking {
            shortSleep()
        } result {
            bothSelectedAddressesAreVisible()
        }
    }

    /**
     * Given:   I am on the guest checkout mode
     * When:    I search for pick up and destination addresses
     * And:     I select them
     * Then:    The booking screen populates the quotes as expected
     **/
    @Test
    fun searchAddressesAndGetQuotesTest() {
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS)
            shortSleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        serverRobot {
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
        }
        address {
            search(TestData.SEARCH_ADDRESS_EXTRA)
            shortSleep()
            clickOxfordStreetResult()
        }
        booking {
            shortSleep()
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
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
        }
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
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS)
            shortSleep()
            clickBakerStreetResult()
        }
        booking {
            shortSleep()
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
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            reverseGeocodeResponse(HTTP_OK, TestData.REVERSE_GEO_SUCCESS)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP, locale = getLocale())
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            mediumSleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS_EXTRA)
            shortSleep()
            clickOxfordStreetResult()
        }
        booking {
            shortSleep()
            pressFirstQuote()
            shortSleep()
        } result {
            checkGuestDetailsPageIsShown()
        }
        booking {
            pressDeviceBackButton()
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
    fun addCardGuestDetailsPageFullCheck() {
        KarhooApi.userStore.savedPaymentInfo = SavedPaymentInfo(TestData.CARD_ENDING, CardType.VISA)
        serverRobot {
            paymentsProviderResponse(HTTP_OK, BRAINTREE_PROVIDER)
            quoteIdResponse(HTTP_CREATED, QUOTE_LIST_ID_ASAP, locale = getLocale())
            reverseGeocodeResponse(HTTP_OK, TestData.REVERSE_GEO_SUCCESS)
            quotesResponse(HTTP_OK, VEHICLES_ASAP)
            sdkInitResponse(HTTP_OK, BRAINTREE_TOKEN)
            addCardResponse(HTTP_OK, PAYMENTS_TOKEN)
            paymentsNonceResponse(HTTP_OK, PAYMENTS_TOKEN)
            bookingWithNonceResponse(HTTP_OK, TRIP)
            bookingStatusResponse(code = HTTP_OK, response = TRIP_STATUS_DER, trip = TRIP.tripId)
            driverTrackingResponse(code = HTTP_OK, response = DRIVER_TRACKING, trip = TRIP.tripId)
            guestBookingDetailsResponse(code = HTTP_OK, response = TRIP_DER_NO_NUMBER_PLATE, trip = TRIP.tripId)
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT)
            addressDetails(HTTP_OK, PLACE_DETAILS)
        }
        booking(this) {
            clickPickUpAddressField()
        }
        address {
            search(SEARCH_ADDRESS)
            mediumSleep()
            clickBakerStreetResult()
        }
        serverRobot {
            addressListResponse(HTTP_OK, PLACE_SEARCH_RESULT_EXTRA)
            addressDetails(HTTP_OK, PLACE_DETAILS_EXTRA)
        }
        booking {
            clickDestinationAddressField()
        }
        address {
            search(TestData.SEARCH_ADDRESS_EXTRA)
            shortSleep()
            clickOxfordStreetResult()
        }
        booking {
            shortSleep()
            pressFirstQuote()
            shortSleep()
            fillCorrectInfoGuestDetails()
            shortSleep()
        } result {
            fullCheckFilledGuestDetailsPage()
            guestBookingCheckCardDetails()
        }
    }

    companion object {

        private val origin = ORIGIN_TRIP
        private val destination = DESTINATION_TRIP
        private val initialTripDetails = TripInfo(origin = origin, destination = destination)

        val INITIAL_TRIP_INTENT = Intent().apply {
            putExtra(BookingActivity.Builder.EXTRA_TRIP_DETAILS, initialTripDetails)
        }
    }
}