package com.karhoo.uisdk.screen.booking.bookingrequest

import android.content.Context
import com.braintreepayments.api.models.PaymentMethodNonce
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.BraintreeSDKToken
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.FlightDetails
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.PaymentsNonce
import com.karhoo.sdk.api.model.Poi
import com.karhoo.sdk.api.model.PoiDetails
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.model.VehicleAttributes
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.payments.PaymentsService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.booking.bookingrequest.BookingRequestViewContract
import com.karhoo.uisdk.screen.booking.booking.bookingrequest.BookingRequestMVP
import com.karhoo.uisdk.screen.booking.booking.bookingrequest.BookingRequestPresenter
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.service.preference.PreferenceStore
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class BookingRequestPresenterTest {

    private val vehicleAttributes: VehicleAttributes = VehicleAttributes(2, 2)
    private val trip: TripInfo = TripInfo(
            tripId = "tripId1234",
            origin = TripLocationInfo(placeId = "placeId1234"),
            destination = TripLocationInfo(placeId = "placeId4321"))
    private val price: QuotePrice = QuotePrice(highPrice = 10, currencyCode = "GBP")
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

    private val analytics: Analytics = mock()
    private val braintreePaymentNonce: PaymentMethodNonce = mock()
    private val context: Context = mock()
    private val flightDetails: FlightDetails = mock()
    private val bookingStatusStateViewModel: BookingStatusStateViewModel = mock()
    private var bookingRequestStateViewModel: BookingRequestStateViewModel = mock()
    private val locationDetails: LocationInfo = LocationInfo()
    private val preferenceStore: PreferenceStore = mock()
    private val quote: Quote = mock()
    private val quotePrice: QuotePrice = mock()
    private val savedPaymentInfo: SavedPaymentInfo = mock()
    private val tripsService: TripsService = mock()
    private val paymentsService: PaymentsService = mock()
    private val userStore: UserStore = mock()
    private var view: BookingRequestMVP.View = mock()

    private val sdkInitCall: Call<BraintreeSDKToken> = mock()
    private val sdkInitCaptor = argumentCaptor<(Resource<BraintreeSDKToken>) -> Unit>()
    private val getNonceCall: Call<PaymentsNonce> = mock()
    private val getNonceCaptor = argumentCaptor<(Resource<PaymentsNonce>) -> Unit>()
    private val tripCall: Call<TripInfo> = mock()
    private val tripCaptor = argumentCaptor<(Resource<TripInfo>) -> Unit>()

    private lateinit var requestPresenter: BookingRequestPresenter

    @Before
    fun setUp() {
        setAuthenticatedUser()

        whenever(userStore.currentUser).thenReturn(userDetails)
        whenever(quote.price).thenReturn(quotePrice)
        doNothing().whenever(sdkInitCall).execute(sdkInitCaptor.capture())
        doNothing().whenever(getNonceCall).execute(getNonceCaptor.capture())
        doNothing().whenever(tripCall).execute(tripCaptor.capture())

        requestPresenter = BookingRequestPresenter(view, analytics, paymentsService,
                                                   preferenceStore, tripsService, userStore)
    }

    /**
     * Given:   A user see the booking screen
     * When:    They are a logged in user
     * Then:    The correct input fields are displayed
     * And:     The booking button is enabled
     */
    @Test
    fun `logged in user sees the correct input fields and booking button is enabled`() {
        setAuthenticatedUser()

        requestPresenter.setBookingFields(false)

        verify(view).showAuthenticatedUserBookingFields()
        verify(view).enableBooking()
    }

    /**
     * Given:   A user see the booking screen
     * When:    They are a guest user
     * And:     The input fields are not all valid
     * Then:    The correct input fields are displayed
     * And:     The booking button is disabled
     */
    @Test
    fun `guest user sees the correct input fields and booking button is disabled`() {
        setGuestUser()

        requestPresenter.setBookingFields(false)

        verify(view).showGuestBookingFields()
        verify(view).disableBooking()
    }

    /**
     * Given:   A user see the booking screen
     * When:    They are a guest user
     * And:     The input fields are all valid
     * Then:    The correct input fields are displayed
     * And:     The booking button is enabled
     */
    @Test
    fun `guest user sees the correct input fields and booking button is enabled`() {
        setGuestUser()

        whenever(userStore.savedPaymentInfo).thenReturn(savedPaymentInfo)

        requestPresenter.setBookingFields(true)

        verify(view).showGuestBookingFields()
        verify(view).enableBooking()
    }

    /**
     * Given:   A user has pressed a quote
     * When:    There is no destination
     * Then:    The correct error message should be shown
     */
    @Test
    fun `null destination doesn't show request screen`() {
        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(locationDetails, null, null))

        requestPresenter.watchBookingRequest(bookingRequestStateViewModel)

        requestPresenter.showBookingRequest(quote)

        verify(view, never()).bindPriceAndEta(quote, "")
        verify(view).onError()
        verify(bookingRequestStateViewModel).process(BookingRequestViewContract
                                                             .BookingRequestEvent
                                                             .BookingError(R.string
                                                                                   .destination_book_error))
    }

    /**
     * Given:   A user has pressed a quote
     * When:    There is no origin
     * Then:    The correct error message should be shown
     */
    @Test
    fun `null origin doesn't show request screen`() {
        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(null, locationDetails, null))

        requestPresenter.watchBookingRequest(bookingRequestStateViewModel)

        requestPresenter.showBookingRequest(quote)

        verify(view, never()).bindPriceAndEta(quote, "")
        verify(view).onError()
        verify(bookingRequestStateViewModel).process(BookingRequestViewContract
                                                             .BookingRequestEvent
                                                             .BookingError(R.string.origin_book_error))
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
        whenever(quote.vehicleAttributes).thenReturn(vehicleAttributes)

        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(locationDetails, locationDetails, null))

        requestPresenter.showBookingRequest(quote)

        verify(view).showUpdatedPaymentDetails(savedPaymentInfo, quotePrice)
        verify(userStore).savedPaymentInfo
        verify(view).setCapacity(vehicleAttributes)
        verify(view).animateIn()
        verify(view).bindPriceAndEta(quote, "")
    }

    /**
     * Given:   A user has pressed a quote
     * When:    The quote is valid but there is no price
     * Then:    The view should be bound with the correct info
     */
    @Test
    fun `selected quote updates view with correct info for quote with zero highest price`() {
        whenever(quote.price.highPrice).thenReturn(0)
        whenever(quote.vehicleAttributes).thenReturn(vehicleAttributes)

        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(locationDetails, locationDetails, null))

        requestPresenter.showBookingRequest(quote)

        verify(view).setCapacity(vehicleAttributes)
        verify(view).animateIn()
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
        whenever(quote.vehicleAttributes).thenReturn(vehicleAttributes)

        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(locationDetails, locationDetails, scheduledDate))

        requestPresenter.showBookingRequest(quote)

        verify(view).setCapacity(vehicleAttributes)
        verify(view).animateIn()
        verify(view).bindPrebook(quote, "", scheduledDate)
    }

    /**
     * Given:   A user sets the booking enablement
     * When:    The user has a payment card
     * And:     The passenger details are invalid
     * Then:    The view is updated to disable booking
     */
    @Test
    fun `disable booking when passenger details are invalid and there are card details`() {
        setGuestUser()

        whenever(userStore.savedPaymentInfo).thenReturn(savedPaymentInfo)

        requestPresenter.setBookingEnablement(false)

        verify(view).disableBooking()
    }

    /**
     * Given:   A user sets the booking enablement
     * When:    The user does not have a payment card
     * And:     The passenger details are invalid
     * Then:    The view is updated to disable booking
     */
    @Test
    fun `disable booking when passenger details are valid and there are no card details`() {
        setGuestUser()

        requestPresenter.setBookingEnablement(false)

        verify(view).disableBooking()
    }

    /**
     * Given:   A user sets the booking enablement
     * When:    The user has a payment card
     * And:     The passenger details are invalid
     * Then:    The view is updated to disable booking
     */
    @Test
    fun `enable booking when passenger details are valid and there are card details`() {
        setGuestUser()

        whenever(userStore.savedPaymentInfo).thenReturn(savedPaymentInfo)

        requestPresenter.setBookingEnablement(true)

        verify(view).enableBooking()
    }

    /**
     * Given:   A user adds a payment card
     * When:    The Braintree result does not have payment nonce details
     * Then:    The view is not updated
     */
    @Test
    fun `no updates made to view if there is no payment nonce info`() {
        requestPresenter.updateCardDetails(null)

        verify(userStore, never()).savedPaymentInfo
        verify(view, never()).showUpdatedPaymentDetails(any(), any())
        verify(view).disableBooking()
    }

    /**
     * Given:   A user adds a payment card
     * When:    The Braintree result has payment nonce details
     * Then:    The card info is stored and the view is not updated
     */
    @Test
    fun `card info stored and correct updates made to view if there is payment nonce info`() {
        val desc = "ending in 00"

        whenever(braintreePaymentNonce.nonce).thenReturn(PAYMENTS_NONCE)
        whenever(braintreePaymentNonce.description).thenReturn(desc)
        whenever(braintreePaymentNonce.typeLabel).thenReturn(CardType.VISA.toString())
        whenever(userStore.savedPaymentInfo).thenReturn(savedPaymentInfo)

        requestPresenter.updateCardDetails(braintreePaymentNonce.nonce)

        verify(view).enableBooking()
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

        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(origin, locationDetails, null))

        whenever(braintreePaymentNonce.nonce).thenReturn("")
        whenever(braintreePaymentNonce.description).thenReturn("desc")
        whenever(braintreePaymentNonce.typeLabel).thenReturn("VISA")
        whenever(quote.price.currencyCode).thenReturn("GBP")
        whenever(quote.price.highPrice).thenReturn(10)

        requestPresenter.updateCardDetails(braintreePaymentNonce.nonce)
        requestPresenter.showBookingRequest(quote, "tripId")

        requestPresenter.makeBooking()

        verify(analytics).bookingRequested(any(), anyString())
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
        val origin = LocationInfo(poiType = Poi.ENRICHED, details = PoiDetails(type = PoiType.AIRPORT))
        whenever(flightDetails.flightNumber).thenReturn("flight number")
        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(origin, locationDetails, null))

        requestPresenter.showBookingRequest(quote, "tripId")

        verify(view).displayFlightDetailsField(origin.details.type)
    }

    /**
     * Given:   A user has selected a pickup address with an Airport POI,
     * When:    The details page is displayed
     * Then:    The Flight Number field is visible and pre-populated with the flight number
     **/
    @Test
    fun `display flight number field when pickup address has airport POI`() {
        val origin = LocationInfo(poiType = Poi.ENRICHED, details = PoiDetails(type = PoiType.AIRPORT))
        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(origin, locationDetails, null))

        requestPresenter.showBookingRequest(quote, "tripId")

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

        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(origin, destination, null))

        whenever(braintreePaymentNonce.nonce).thenReturn("")
        whenever(braintreePaymentNonce.description).thenReturn("desc")
        whenever(braintreePaymentNonce.typeLabel).thenReturn("VISA")
        whenever(quote.price).thenReturn(price)

        requestPresenter.updateCardDetails(braintreePaymentNonce.nonce)
        requestPresenter.showBookingRequest(quote, "tripId")

        requestPresenter.makeBooking()

        verify(analytics).bookingRequested(any(), anyString())
        verify(view).initialiseGuestPayment(price)
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

        val observer = requestPresenter.watchBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(locationDetails, locationDetails, null))

        whenever(braintreePaymentNonce.nonce).thenReturn("")
        whenever(braintreePaymentNonce.description).thenReturn("desc")
        whenever(braintreePaymentNonce.typeLabel).thenReturn("VISA")
        whenever(quote.price).thenReturn(price)

        requestPresenter.updateCardDetails(braintreePaymentNonce.nonce)
        requestPresenter.showBookingRequest(quote, outboundTripId = "tripId")

        requestPresenter.makeBooking()

        verify(analytics).bookingRequested(any(), anyString())
        verify(view).initialiseGuestPayment(price)
    }

    /**
     * Given:   Three D Secure nonce is passed back for booking trip
     * When:    Book trip fails
     * Then:    A booking error is shown
     */
    @Test
    fun `book trip failure shows error`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)

        requestPresenter.watchBookingRequest(bookingRequestStateViewModel)

        requestPresenter.passBackThreeDSecuredNonce(THREE_D_SECURE_NONCE, passengerDetails, bookingComment)

        tripCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).onError()
        verify(bookingRequestStateViewModel).process(BookingRequestViewContract
                                                             .BookingRequestEvent
                                                             .BookingError(R.string.K0001))
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

        requestPresenter.watchBookingRequest(bookingRequestStateViewModel)

        requestPresenter.passBackThreeDSecuredNonce(THREE_D_SECURE_NONCE, passengerDetails, bookingComment)

        tripCaptor.firstValue.invoke(Resource.Failure(KarhooError.InvalidRequestPayload))

        verify(view).onError()
        verify(bookingRequestStateViewModel).process(BookingRequestViewContract
                                                             .BookingRequestEvent
                                                             .BookingError(R.string.booking_details_error))
    }

    /**
     * Given:   Three D Secure nonce is passed back for booking trip
     * When:    Book trip failure with CouldNotBookPaymentPreAuthFailed
     * Then:    View shows payment dialog
     */
    @Test
    fun `book trip CouldNotBookPaymentPreAuthFailed failure shows payment dialog`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)

        requestPresenter.passBackThreeDSecuredNonce(THREE_D_SECURE_NONCE, passengerDetails, bookingComment)

        tripCaptor.firstValue.invoke(Resource.Failure(KarhooError.CouldNotBookPaymentPreAuthFailed))

        verify(view).showPaymentFailureDialog()
    }

    /**
     * Given: Three D Secure nonce is passed back for booking trip
     * When: Book trip failure with CouldNotPaymentPreAuthFailed and payment dialog appears
     * Then: onPaymentFailureDialogPositive is called
     */
    @Test
    fun `Select Positive option on Payment Failure Dialog`() {
        requestPresenter.onPaymentFailureDialogPositive()

        verify(view).initialiseChangeCard(null)
    }

    /**
     * Given: Three D Secure nonce is passed back for booking trip
     * When: Book trip failure with CouldNotPaymentPreAuthFailed and payment dialog appears
     * Then: onPaymentFailureDialogCancelled is called
     */
    @Test
    fun `Select Negative option on Payment Failure Dialog`() {
        requestPresenter.onPaymentFailureDialogCancelled()

        verify(view).animateOut()
    }

    /**
     * Given:   Three D Secure nonce is passed back for booking trip
     * When:    Book trip success
     * Then:    Store last trip in prefs
     * And:     Hide request booking
     * And:     View shows trip booked successfully
     */
    @Test
    fun `book trip success`() {
        whenever(tripsService.book(any())).thenReturn(tripCall)

        requestPresenter.watchBookingRequest(bookingRequestStateViewModel)

        requestPresenter.passBackThreeDSecuredNonce(THREE_D_SECURE_NONCE, passengerDetails, bookingComment)

        tripCaptor.firstValue.invoke(Resource.Success(trip))

        verify(preferenceStore).lastTrip = trip
        verify(view).animateOut()
        verify(view).onTripBookedSuccessfully(trip)
        verify(bookingRequestStateViewModel).process(BookingRequestViewContract
                                                             .BookingRequestEvent.BookingSuccess(trip))
    }

    /**
     * Given:   The user leaves the booking screen
     * When:    The user is a guest
     * Then:    The user data is removed
     */
    @Test
    fun `clear data removes user data for guest users`() {
        setGuestUser()

        requestPresenter.clearData()

        verify(userStore).removeCurrentUser()
    }

    /**
     * Given:   The user leaves the booking screen
     * When:    The user is not a guest
     * Then:    The user data is removed
     */
    @Test
    fun `clear data does not remove user data for logged in users`() {
        setAuthenticatedUser()

        requestPresenter.clearData()

        verify(userStore, never()).removeCurrentUser()
    }

    private fun setGuestUser() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId")))
    }

    private fun setAuthenticatedUser() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.KarhooUser()))
    }

    companion object {
        private const val THREE_D_SECURE_NONCE = "threeDSecureNonce"
        private const val PAYMENTS_NONCE = "paymentsNonce"
    }
}
