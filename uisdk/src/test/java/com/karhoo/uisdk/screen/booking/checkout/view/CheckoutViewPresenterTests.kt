package com.karhoo.uisdk.screen.booking.checkout.view

import android.content.Context
import com.braintreepayments.api.models.PaymentMethodNonce
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.*
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.request.TripBooking
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.R
import com.karhoo.uisdk.UnitTestUISDKConfig.Companion.setGuestAuthentication
import com.karhoo.uisdk.UnitTestUISDKConfig.Companion.setKarhooAuthentication
import com.karhoo.uisdk.UnitTestUISDKConfig.Companion.setTokenAuthentication
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.BookButtonState
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewPresenter
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewPresenter.Companion.TRIP_ID
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.CapabilityAdapter
import com.karhoo.uisdk.service.preference.PreferenceStore
import com.karhoo.uisdk.util.ADYEN
import com.karhoo.uisdk.util.BRAINTREE
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class CheckoutViewPresenterTests {

    private val vehicleAttributes: QuoteVehicle = QuoteVehicle(passengerCapacity = 2,
            luggageCapacity = 2)
    private val trip: TripInfo = TripInfo(
            tripId = "tripId1234",
            origin = TripLocationInfo(placeId = "placeId1234"),
            destination = TripLocationInfo(placeId = "placeId4321"))
    private val price: QuotePrice = QuotePrice(highPrice = 10, currencyCode = "GBP")
    private val fleet: Fleet = Fleet(null, null, null, null, null, null, null)
    private val userDetails: UserInfo = UserInfo(firstName = "David",
            lastName = "Smith",
            email = "test.test@test.test",
            phoneNumber = "+441234 56789",
            userId = "123",
            locale = "en-GB",
            organisations = listOf(Organisation(id = "organisation_id", name = "Organisation", roles = listOf("PERMISSION_ONE", "PERMISSION_TWO"))))
    private val passengerDetails: PassengerDetails = PassengerDetails(firstName = "David",
            lastName = "Smith",
            email = "test.test@test.test",
            phoneNumber = "+441234 56789",
            locale = "en-GB")
    private val bookingComment = "Booking Comments"
    private val flightInfo = "AA123"

    private val analytics: Analytics = mock()
    private val braintreePaymentNonce: PaymentMethodNonce = mock()
    private val context: Context = mock()
    private val flightDetails: FlightDetails = mock()
    private val journeyDetailsStateViewModel: JourneyDetailsStateViewModel = mock()
    private var bookingRequestStateViewModel: BookingRequestStateViewModel = mock()
    private val locationDetails: LocationInfo = LocationInfo()
    private val preferenceStore: PreferenceStore = mock()
    private val quote: Quote = mock()
    private val quotePrice: QuotePrice = mock()
    private val savedPaymentInfo: SavedPaymentInfo = mock()
    private val tripsService: TripsService = mock()
    private val userStore: UserStore = mock()
    private var view: CheckoutViewContract.View = mock()

    private val sdkInitCall: Call<BraintreeSDKToken> = mock()
    private val sdkInitCaptor = argumentCaptor<(Resource<BraintreeSDKToken>) -> Unit>()
    private val getNonceCall: Call<PaymentsNonce> = mock()
    private val getNonceCaptor = argumentCaptor<(Resource<PaymentsNonce>) -> Unit>()
    private val tripBookingCaptor = argumentCaptor<TripBooking>()
    private val tripCall: Call<TripInfo> = mock()
    private val tripCaptor = argumentCaptor<(Resource<TripInfo>) -> Unit>()

    private lateinit var checkoutPresenter: CheckoutViewPresenter

    @Before
    fun setUp() {
        setAuthenticatedUser()

        whenever(quote.price).thenReturn(quotePrice)
        doNothing().whenever(sdkInitCall).execute(sdkInitCaptor.capture())
        doNothing().whenever(getNonceCall).execute(getNonceCaptor.capture())
        doNothing().whenever(tripCall).execute(tripCaptor.capture())

        checkoutPresenter = CheckoutViewPresenter(view, analytics, preferenceStore, tripsService,
                userStore)
    }

    /**
     * Given:   A user see the booking screen
     * When:    They are a logged in user
     * Then:    The correct input fields are displayed
     */
    @Test
    fun `user without saved card sees the correct input fields`() {
        setAuthenticatedUser()

        verify(view, never()).fillInPassengerDetails(any())
    }

    /**
     * Given:   A user see the booking screen
     * When:    They are a logged in user
     * Then:    The correct payment is acknowledged
     */
    @Test
    fun `user payment returns valid`() {
        whenever(userStore.savedPaymentInfo).thenReturn(savedPaymentInfo)

        setAuthenticatedUser()

        assertTrue(checkoutPresenter.isPaymentSet())
    }

    /**
     * Given:   A user has pressed a quote
     * When:    There is no destination
     * Then:    The correct error message should be shown
     */
    @Test
    fun `null destination doesn't show request screen`() {
        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(locationDetails, null, null))

        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.showBookingRequest(quote, null, null, null)

        verify(view, never()).bindPriceAndEta(quote, "")
        verify(view).onError(null)
        verify(bookingRequestStateViewModel).process(CheckoutViewContract
                .Event
                .BookingError(R.string
                        .kh_uisdk_destination_book_error, null))
    }

    /**
     * Given:   A user has pressed a quote
     * When:    There is no origin
     * Then:    The correct error message should be shown
     */
    @Test
    fun `null origin doesn't show request screen`() {
        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(null, locationDetails, null))

        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.showBookingRequest(quote, null, null, null)

        verify(view, never()).bindPriceAndEta(quote, "")
        verify(view).onError(null)
        verify(bookingRequestStateViewModel).process(CheckoutViewContract
                .Event
                .BookingError(R.string
                        .kh_uisdk_origin_book_error, null))
    }

    /**
     * Given:   A user has pressed a quote
     * When:    The quote is valid and higher than zero
     * Then:    The view should be bound with the correct info
     */
    @Test
    fun `selected quote updates view with correct info for quote with non zero highest price`() {
        whenever(userStore.savedPaymentInfo).thenReturn(savedPaymentInfo)
        whenever(quote.price.highPrice).thenReturn(150)
        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.price).thenReturn(price)
        whenever(quote.fleet).thenReturn(fleet)

        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(locationDetails, locationDetails, null))

        checkoutPresenter.showBookingRequest(quote, null, null, null)

        verify(view).showUpdatedPaymentDetails(savedPaymentInfo)
        verify(userStore).savedPaymentInfo
        verify(view).setCapacityAndCapabilities(arrayListOf(Capability(CapabilityAdapter.PASSENGERS_MAX, 2), Capability(CapabilityAdapter.BAGGAGE_MAX, 2)), vehicleAttributes)
        verify(view).bindPriceAndEta(quote, "")
    }

    /**
     * Given:   The passenger details view is visible
     * When:    The user clicks back button
     * Then:    The view should be dismissed and the click consumed
     */
    @Test
    fun `back pressed consumed if passenger details visible`() {
        whenever(view.isPassengerDetailsViewVisible()).thenReturn(true)

        val returnValue = checkoutPresenter.consumeBackPressed()

        verify(view).showPassengerDetailsLayout(false)
        assertTrue(returnValue)
    }

    /**
     * Given:   The passenger details view is not visible
     * When:    The user clicks back button
     * Then:    The clicked should not be consumed and return false
     */
    @Test
    fun `back pressed not consumed if passenger details not visible`() {
        whenever(view.isPassengerDetailsViewVisible()).thenReturn(false)

        val returnValue = checkoutPresenter.consumeBackPressed()

        verify(view, never()).showPassengerDetailsLayout(false)
        assertFalse(returnValue)
    }

    /**
     * Given:   The checkout is visible
     * When:    The passenger details are valid but payment is not
     * Then:    The button should be next state
     */
    @Test
    fun `passenger details visible and valid but payment is not`() {
        val returnValue = checkoutPresenter.getBookingButtonState(
            arePassengerDetailsValid = true,
                                                                 )

        assertEquals(returnValue, BookButtonState.NEXT)
    }

    /**
     * Given:   The checkout is visible
     * When:    The passenger details are invalid but payment is valid
     * Then:    The button should be in next state
     */
    @Test
    fun `passenger details are invalid but payment is valid`() {
        val returnValue = checkoutPresenter.getBookingButtonState(
            arePassengerDetailsValid = false,
                                                                 )

        assertEquals(returnValue, BookButtonState.NEXT)
    }

    /**
     * Given:   The checkout is visible
     * When:    The passenger details and payment is valid
     * Then:    The button should be in book state
     */
    @Test
    fun `passenger details and payment valid`() {
        val returnValue = checkoutPresenter.getBookingButtonState(
            arePassengerDetailsValid = true,
                                                                 )

        assertEquals(returnValue, BookButtonState.NEXT)
    }

    /**
     * Given:   The checkout is visible and CheckBox Terms&Conditions Required
     * When:    The passenger details and payment is valid but checkbox not checked
     * Then:    The button should be in next state
     */
    @Test
    fun `passenger details and payment valid but checkbox not checked`() {
        val returnValue = checkoutPresenter.getBookingButtonState(
            arePassengerDetailsValid = true,
            isTermsCheckBoxValid = false
        )

        assertEquals(returnValue, BookButtonState.NEXT)
    }

    /**
     * Given:   The checkout is visible and CheckBox Terms&Conditions Required
     * When:    The passenger details and payment is valid but checkbox is checked
     * Then:    The button should be in book state
     */
    @Test
    fun `passenger details and payment valid but checkbox is checked`() {
        val returnValue = checkoutPresenter.getBookingButtonState(
            arePassengerDetailsValid = true,
            isTermsCheckBoxValid = true
        )

        assertEquals(returnValue, BookButtonState.NEXT)
    }

    /**
     * Given:   A user has pressed a quote
     * When:    The quote is valid but there is no price
     * Then:    The view should be bound with the correct info
     */
    @Test
    fun `selected quote updates view with correct info for quote with zero highest price`() {
        whenever(quote.price.highPrice).thenReturn(0)
        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.fleet).thenReturn(fleet)

        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(locationDetails, locationDetails, null))

        checkoutPresenter.showBookingRequest(quote, null, null, null)

        verify(view).setCapacityAndCapabilities(arrayListOf(Capability(CapabilityAdapter.PASSENGERS_MAX, 2), Capability(CapabilityAdapter.BAGGAGE_MAX, 2)), vehicleAttributes)
        verify(view).bindEta(quote, "")
    }

    /**
     * Given:   A user has pressed a quote
     * When:    The quote is valid and is for a prebooking
     * Then:    The view should be bound with the correct info
     */
    @Test
    fun `selected quote updates view with the correct info for prebooking`() {
        val scheduledDate = DateTime.now()
        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.price).thenReturn(price)
        whenever(quote.fleet).thenReturn(fleet)

        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(locationDetails, locationDetails, scheduledDate))

        checkoutPresenter.showBookingRequest(quote, null, null, null)

        verify(view).setCapacityAndCapabilities(arrayListOf(Capability(CapabilityAdapter.PASSENGERS_MAX, 2), Capability(CapabilityAdapter.BAGGAGE_MAX, 2)), vehicleAttributes)
        verify(view).bindPrebook(quote, "", scheduledDate)
    }

    /**
     * Given:   A user tries to make a booking
     * When:    And the origin is an airport
     * And:     There are flight details
     * Then:    The payment flow is triggered
     */
    @Test
    fun `guest booking for airport origin with flight details triggers payment`() {
        setGuestUser()

        val origin = LocationInfo(poiType = Poi.ENRICHED, details = PoiDetails(type = PoiType.AIRPORT))

        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(origin, locationDetails, null))

        whenever(braintreePaymentNonce.nonce).thenReturn("")
        whenever(braintreePaymentNonce.description).thenReturn("desc")
        whenever(braintreePaymentNonce.typeLabel).thenReturn("VISA")
        whenever(quote.price.currencyCode).thenReturn("GBP")
        whenever(quote.price.highPrice).thenReturn(10)
        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.price).thenReturn(price)
        whenever(quote.fleet).thenReturn(fleet)
        whenever(quote.id).thenReturn(IDENTIFIER)

        //        requestPresenter.updateCardDetails(braintreePaymentNonce.nonce)
        checkoutPresenter.showBookingRequest(quote, null, "tripId", null)

        checkoutPresenter.makeBooking()

        verify(analytics).bookingRequested(anyString())
        verify(view).initialiseGuestPayment(any())
    }

    /**
     * Given:   A user has selected a pickup address with an Airport POI,
     * And:     The flight number is provided
     * When:    The details page is displayed
     * Then:    The Flight Number field is visible and pre-populated with the flight number
     **/
    @Test
    fun `display and populate flight number field when pickup address has airport POI`() {
        val origin = LocationInfo(poiType = Poi.ENRICHED, details = PoiDetails(type =
        PoiType.AIRPORT))
        whenever(flightDetails.flightNumber).thenReturn("flight number")
        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.price).thenReturn(price)
        whenever(quote.fleet).thenReturn(fleet)

        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(origin, locationDetails, null))

        checkoutPresenter.showBookingRequest(quote, null, "tripId", null)

        verify(view).displayFlightDetailsField(origin.details.type)
    }

    /**
     * Given:   A user has selected a pickup address with an Airport POI,
     * When:    The details page is displayed
     * Then:    The Flight Number field is visible and pre-populated with the flight number
     **/
    @Test
    fun `display flight number field when pickup address has airport POI`() {
        val origin = LocationInfo(poiType = Poi.ENRICHED, details = PoiDetails(type =
        PoiType.AIRPORT))
        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(origin, locationDetails, null))

        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.price).thenReturn(price)
        whenever(quote.fleet).thenReturn(fleet)

        checkoutPresenter.showBookingRequest(quote, null, "tripId", null)

        verify(view).displayFlightDetailsField(origin.details.type)
        verify(view, never()).populateFlightDetailsField(anyString())
    }

    /**
     * Given:   A guest user tries to make a booking
     * When:    And the destination is an airport
     * And:     There are flight details
     * Then:    The payment flow is triggered
     */
    @Test
    fun `guest booking for airport destination with flight details triggers payment`() {
        setGuestUser()

        val origin = LocationInfo(poiType = Poi.NOT_SET)
        val destination = LocationInfo(poiType = Poi.ENRICHED, details = PoiDetails(type = PoiType.AIRPORT))

        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(origin, destination, null))

        whenever(braintreePaymentNonce.nonce).thenReturn("")
        whenever(braintreePaymentNonce.description).thenReturn("desc")
        whenever(braintreePaymentNonce.typeLabel).thenReturn("VISA")
        whenever(quote.price).thenReturn(price)
        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.fleet).thenReturn(fleet)
        whenever(quote.id).thenReturn(IDENTIFIER)
        //        requestPresenter.updateCardDetails(braintreePaymentNonce.nonce)
        checkoutPresenter.showBookingRequest(quote, null, "tripId", null)

        checkoutPresenter.makeBooking()

        verify(analytics).bookingRequested(anyString())
        verify(view).initialiseGuestPayment(quote)
    }

    /**
     * Given:   A guest user tries to make a booking
     * When:    And the origin and destination are not airports
     * And:     There are no flight details
     * Then:    The user is taken to the flight details screen
     */
    @Test
    fun `guest booking started for non-airport origin and destination triggers payment`() {
        setGuestUser()

        val observer = checkoutPresenter.watchJourneyDetails(journeyDetailsStateViewModel)
        observer.onChanged(JourneyDetails(locationDetails, locationDetails, null))

        whenever(braintreePaymentNonce.nonce).thenReturn("")
        whenever(braintreePaymentNonce.description).thenReturn("desc")
        whenever(braintreePaymentNonce.typeLabel).thenReturn("VISA")
        whenever(quote.price).thenReturn(price)
        whenever(quote.vehicle).thenReturn(vehicleAttributes)
        whenever(quote.fleet).thenReturn(fleet)
        whenever(quote.id).thenReturn(IDENTIFIER)

        //        requestPresenter.updateCardDetails(braintreePaymentNonce.nonce)
        checkoutPresenter.showBookingRequest(quote, null, outboundTripId = "tripId")

        checkoutPresenter.makeBooking()

        verify(analytics).bookingRequested(anyString())
        verify(view).initialiseGuestPayment(quote)
    }

    /**
     * Given:   Three D Secure nonce is passed back for booking trip
     * When:    Book trip fails
     * Then:    A booking error is shown
     */
    @Test
    fun `book trip failure shows error`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)

        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.passBackPaymentIdentifiers(IDENTIFIER, null, passengerDetails,
                bookingComment, flightInfo)

        tripCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).onError(any())
        verify(bookingRequestStateViewModel).process(CheckoutViewContract
                .Event
                .BookingError(R.string.kh_uisdk_K0001, KarhooError.GeneralRequestError))
    }

    /**
     * Given:   Three D Secure nonce is passed back for booking trip
     * When:    Book trip fails
     * And:     The error is due to InvalidPayloadRequest
     * Then:    A booking details error is shown
     */
    @Test
    fun `guest book trip invalid payload failure shows booking details error`() {

        whenever(tripsService.book(any())).thenReturn(tripCall)

        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.passBackPaymentIdentifiers(IDENTIFIER, null, passengerDetails =
        passengerDetails, comments = bookingComment, flightInfo)

        tripCaptor.firstValue.invoke(Resource.Failure(KarhooError.InvalidRequestPayload))

        verify(view).onError(any())
        verify(bookingRequestStateViewModel).process(CheckoutViewContract
                .Event
                .BookingError(R.string
                        .kh_uisdk_booking_details_error, KarhooError.InvalidRequestPayload))
    }

    /**
     * Given:   Three D Secure nonce is passed back for booking trip
     * When:    Book trip failure with CouldNotBookPaymentPreAuthFailed
     * Then:    View shows payment dialog
     */
    @Test
    fun `book trip CouldNotBookPaymentPreAuthFailed failure shows payment dialog`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)

        checkoutPresenter.passBackPaymentIdentifiers(IDENTIFIER, null, passengerDetails, bookingComment, flightInfo)

        tripCaptor.firstValue.invoke(Resource.Failure(KarhooError.CouldNotBookPaymentPreAuthFailed))

        verify(view).showPaymentFailureDialog(null, KarhooError.CouldNotBookPaymentPreAuthFailed)
    }

    /**
     * Given: Three D Secure nonce is passed back for booking trip
     * When: Book trip failure with CouldNotPaymentPreAuthFailed and payment dialog appears
     * Then: onPaymentFailureDialogPositive is called
     */
    @Test
    fun `Select Positive option on Payment Failure Dialog`() {
        checkoutPresenter.onPaymentFailureDialogPositive()

        verify(view).showLoading(false)
        verify(view).initialiseChangeCard(null)
    }

    /**
     * Given: Three D Secure nonce is passed back for booking trip
     * When: Book trip failure with CouldNotPaymentPreAuthFailed and payment dialog appears
     * Then: onPaymentFailureDialogCancelled is called
     */
    @Test
    fun `Select Negative option on Payment Failure Dialog`() {
        checkoutPresenter.onPaymentFailureDialogCancelled()

        verify(view).showLoading(false)
    }

    /**
     * Given:   The payment identifier (nonce or trip id) is passed back for booking trip
     * When:    It is an Adyen payment
     * And:     The trip is is sent through in the meta field
     */
    @Test
    fun `Adyen booking request has trip id in meta`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)

        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.passBackPaymentIdentifiers(IDENTIFIER, IDENTIFIER, passengerDetails,
                bookingComment, flightInfo)

        tripCaptor.firstValue.invoke(Resource.Success(trip))

        verify(tripsService).book(tripBookingCaptor.capture())
        val tripBooking: TripBooking = tripBookingCaptor.firstValue
        assertNotNull(tripBooking.meta)
        assertEquals(IDENTIFIER, tripBooking.meta?.get(TRIP_ID))
        assertEquals(IDENTIFIER, tripBooking.nonce)
    }

    /**
     * Given:   Meta dictionary is passed into the Booking Request component
     * When:    Booking a trip
     * Then:    The meta is sent through in the Booking API meta field
     */
    @Test
    fun `Booking meta data injected in the Booking Request contains meta`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)
        val map = hashMapOf<String, String>()
        map[BOOKING__META_MAP_KEY] = BOOKING__META_MAP_VALUE

        checkoutPresenter.showBookingRequest(quote, outboundTripId = null, bookingMetadata = map, journeyDetails = null)
        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.passBackPaymentIdentifiers(IDENTIFIER, IDENTIFIER, passengerDetails,
                bookingComment, flightInfo)

        tripCaptor.firstValue.invoke(Resource.Success(trip))

        verify(tripsService).book(tripBookingCaptor.capture())
        val tripBooking: TripBooking = tripBookingCaptor.firstValue
        assertNotNull(tripBooking.meta)
        assertEquals(IDENTIFIER, tripBooking.meta?.get(TRIP_ID))
        assertEquals(IDENTIFIER, tripBooking.nonce)
        assertEquals(BOOKING__META_MAP_VALUE, tripBooking.meta?.get(BOOKING__META_MAP_KEY))
    }

    /**
     * Given:   The payment identifier (nonce or trip id) is passed back for booking trip
     * When:    It is a Braintree payment
     * The:     The nonce is sent through on the request
     * And:     The trip id is not sent through in the meta field
     */
    @Test
    fun `Braintree booking request has null meta`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)
        whenever(userStore.paymentProvider).thenReturn(PaymentProvider(Provider(BRAINTREE)))

        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.passBackPaymentIdentifiers(IDENTIFIER, null, passengerDetails, bookingComment, flightInfo)

        tripCaptor.firstValue.invoke(Resource.Success(trip))

        verify(tripsService).book(tripBookingCaptor.capture())
        val tripBooking: TripBooking = tripBookingCaptor.firstValue
        assertNull(tripBooking.meta)
        assertEquals(IDENTIFIER, tripBooking.nonce)
    }

    /**
     * Given:   The payment identifier (nonce or trip id) is passed back for booking trip
     * When:    Book trip success
     * Then:    Store last trip in prefs
     * And:     Hide request booking
     * And:     View shows trip booked successfully
     */
    @Test
    fun `book trip success`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)

        checkoutPresenter.watchBookingRequest(bookingRequestStateViewModel)

        checkoutPresenter.passBackPaymentIdentifiers(IDENTIFIER, null, passengerDetails, bookingComment, flightInfo)

        tripCaptor.firstValue.invoke(Resource.Success(trip))

        verify(preferenceStore).lastTrip = trip
        verify(view).onTripBookedSuccessfully(trip)
        verify(bookingRequestStateViewModel).process(CheckoutViewContract
                .Event.BookingSuccess(trip))
    }

    /**
     * Given:   The user leaves the booking screen
     * When:    The user is a guest
     * Then:    The user data is removed
     */
    @Test
    fun `clear data removes user data for guest users`() {
        setGuestUser()

        checkoutPresenter.clearData()

        verify(userStore).removeCurrentUser()
    }

    /**
     * Given:   Authenticated with TOKEN Exchange
     * When:    We clear the data
     * Then:    The payment method data is cleared
     */
    @Test
    fun `Clear data removes the saved payment info for a TOKEN Exchange authenticated user`() {
        setTokenUser()
        whenever(userStore.paymentProvider).thenReturn(PaymentProvider(Provider(id = ADYEN)))
        checkoutPresenter.clearData()

        verify(userStore, atLeastOnce()).clearSavedPaymentInfo()
    }

    /**
     * Given:   Authenticated with Guest Account
     * When:    We clear the data
     * Then:    The payment method data is cleared
     */
    @Test
    fun `Clear data removes the saved payment info for a Guest user`() {
        setGuestUser()
        checkoutPresenter.clearData()

        verify(userStore, atLeastOnce()).clearSavedPaymentInfo()
    }

    /**
     * Given:   Authenticated with Authenticated User
     * When:    We clear the data
     * Then:    The payment method data is NOT cleared
     */
    @Test
    fun `Clear data removes the saved payment info for a Authenticated user`() {
        setAuthenticatedUser()
        checkoutPresenter.clearData()

        verify(userStore, never()).clearSavedPaymentInfo()
    }

    /**
     * Given:   Authenticated with TOKEN Exchange
     * When:    We clear the data as a result of an unrecoverable error
     * Then:    The payment method data is cleared
     */
    @Test
    fun `Clear data removes the saved payment info for a TOKEN Exchange authenticated user in case of an unrecoverable err`() {
        setTokenUser()
        whenever(userStore.paymentProvider).thenReturn(PaymentProvider(Provider(id = ADYEN), null))
        checkoutPresenter.handleError(0, KarhooError.InternalSDKError)

        verify(userStore, atLeastOnce()).clearSavedPaymentInfo()
    }

    /**
     * Given:   Authenticated with Guest Account
     * When:    We clear the data
     * Then:    The payment method data is cleared
     */
    @Test
    fun `Clear data removes the saved payment info for a Guest user in case of an unrecoverable err`() {
        setGuestUser()
        checkoutPresenter.handleError(0, KarhooError.InternalSDKError)

        verify(userStore, atLeastOnce()).clearSavedPaymentInfo()
    }

    /**
     * Given:   The user leaves the booking screen
     * When:    The user is not a guest
     * Then:    The user data is removed
     */
    @Test
    fun `clear data does not remove user data for logged in users`() {
        setAuthenticatedUser()

        checkoutPresenter.clearData()

        verify(userStore, never()).removeCurrentUser()
    }


    /**
     * Given:   The user leaves the booking screen
     * When:    The user is a Braintree user
     * Then:    The saved payment info is not removed
     */
    @Test
    fun `clear data does not remove saved payment info for Braintree users`() {
        setAuthenticatedUser()
        userStore.paymentProvider = PaymentProvider(Provider(id = BRAINTREE))

        checkoutPresenter.clearData()

        verify(userStore, never()).removeCurrentUser()
    }

    /**
     * Given:   The user leaves the booking screen
     * When:    The user is an Adyen user
     * Then:    The saved payment info is removed
     */
    @Test
    fun `clear data removes saved payment info for Adyen users`() {
        setAuthenticatedUser()
        userStore.paymentProvider = PaymentProvider(Provider(id = ADYEN), null)

        checkoutPresenter.clearData()

        verify(userStore, never()).removeCurrentUser()
    }

    private fun setGuestUser() {
        whenever(userStore.currentUser).thenReturn(UserInfo())
        setGuestAuthentication(context)
    }

    private fun setTokenUser() {
        whenever(userStore.currentUser).thenReturn(userDetails)
        setTokenAuthentication(context)
    }

    private fun setAuthenticatedUser() {
        whenever(userStore.currentUser).thenReturn(userDetails)
        setKarhooAuthentication(context)
    }

    companion object {
        private const val IDENTIFIER = "3dsNonceOrTripId"
        private const val BOOKING__META_MAP_KEY = "map_key"
        private const val BOOKING__META_MAP_VALUE = "map_value"
    }
}
