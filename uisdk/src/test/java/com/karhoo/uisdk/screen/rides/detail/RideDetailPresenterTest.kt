package com.karhoo.uisdk.screen.rides.detail

import android.content.Context
import android.content.res.Resources
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Fare
import com.karhoo.sdk.api.model.FareBreakdown
import com.karhoo.sdk.api.model.Price
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.fare.FareService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.Call
import com.karhoo.sdk.call.PollCall
import com.karhoo.uisdk.R
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.screen.booking.quotes.BookingQuotesPresenterTest
import com.karhoo.uisdk.screen.rides.detail.RideDetailPresenter.Companion.TRIP_INFO_UPDATE_PERIOD
import com.karhoo.uisdk.screen.rides.feedback.FeedbackCompletedTripsStore
import com.karhoo.uisdk.screen.rides.upcoming.card.UpcomingRideCardPresenterTest
import com.karhoo.uisdk.screen.rides.upcoming.card.UpcomingRideCardPresenterTest.Companion.TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.ServiceCancellationExtTests
import com.karhoo.uisdk.util.ServiceCancellationExtTests.Companion.TEST_TWO
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class RideDetailPresenterTest {

    private var context: Context = mock()
    private val view: RideDetailMVP.View = mock()
    private var tripInfo: TripInfo = mock()
    private val tripsService: TripsService = mock()
    private val tripDetailsCall: PollCall<TripInfo> = mock()
    private val scheduledDateViewBinder: ScheduledDateViewBinder = mock()
    private val feedbackCompletedTripsStore: FeedbackCompletedTripsStore = mock()
    private val observable: Observable<TripInfo> = mock()
    private var fareService: FareService = mock()
    private var fareCall: Call<Fare> = mock()

    private val TRIP_ID = "trip001"
    private val FOLLOW_CODE = "follow001"
    private val EMPTY_TRIP: TripInfo = TripInfo(tripId = TRIP_ID)

    private var presenter: RideDetailPresenter = RideDetailPresenter(
            view = view,
            trip = EMPTY_TRIP,
            tripsService = tripsService,
            scheduledDateBinder = scheduledDateViewBinder,
            feedbackCompletedTripsStore = feedbackCompletedTripsStore,
            fareService = fareService)

    private val observerTripInfoCaptor = argumentCaptor<Observer<Resource<TripInfo>>>()
    private val pollingTimeCaptor = argumentCaptor<Long>()
    private var fareCaptor = argumentCaptor<(Resource<Fare>) -> Unit>()
    private var resources: Resources = mock()

    @Before
    fun setUp() {
        UnitTestUISDKConfig.setKarhooAuthentication(context)
        Locale.setDefault(Locale.UK)
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)
        whenever(tripDetailsCall.observable()).thenReturn(observable)
        doNothing().whenever(observable).subscribe(observerTripInfoCaptor.capture(), anyLong())
        whenever(context.resources).thenReturn(resources)
        whenever(context.resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, TEST_TWO, TEST_TWO)).thenReturn(String.format(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTES, TEST_TWO))
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)).thenReturn(TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_start)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_START)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_ending)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_END)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)).thenReturn(UpcomingRideCardPresenterTest.TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_after_booking_ending)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_AFTER_BOOKING_END)
    }

    /**
     * Given:   a cancelled trip
     * When:    state is bound
     * Then:    display cancelled icon with 'cancelled' text
     */
    @Test
    fun `when state is bound then display cancelled icon with cancelled text`() {
        val cancelledTrip = TripInfo(tripState = TripStatus.CANCELLED_BY_USER)
        presenter = RideDetailPresenter(view, cancelledTrip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_cancelled, R.string.kh_uisdk_ride_state_cancelled, R.color.off_black)
    }

    /**
     * Given:   there are no drivers trip
     * When:    state is bound
     * Then:    display cancelled icon with 'cancelled' text
     */
    @Test
    fun `when state is bound and there are no drivers a cancelled icon should show`() {
        val noDriverTrip = TripInfo(tripState = TripStatus.NO_DRIVERS)
        presenter = RideDetailPresenter(view, noDriverTrip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_cancelled, R.string.kh_uisdk_ride_state_cancelled, R.color.off_black)
    }

    /**
     * Given:   a completed trip
     * When:    state is bound
     * Then:    display completed icon with 'completed' text
     */
    @Test
    fun `when state is bound then display completed icon with completed text`() {
        val completedTrip = TripInfo(tripState = TripStatus.COMPLETED)
        presenter = RideDetailPresenter(view, completedTrip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_completed, R.string.kh_uisdk_ride_state_completed, R.color.off_black)
    }

    /**
     * Given:   a null fare
     * When:    highPrice is bound
     * Then:    show pending highPrice
     */
    @Test
    fun `when fare is null then display price pending`() {
        whenever(fareService.fareDetails(anyString())).thenReturn(fareCall)
        doNothing().whenever(fareCall).execute(fareCaptor.capture())

        val tripWithNullFare = TripInfo(tripState = TripStatus.COMPLETED)
        presenter = RideDetailPresenter(
                view = view,
                trip = tripWithNullFare,
                tripsService = tripsService,
                scheduledDateBinder = scheduledDateViewBinder,
                feedbackCompletedTripsStore = feedbackCompletedTripsStore,
                fareService = fareService)

        presenter.bindPrice()
        fareCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(view, times(2)).displayPricePending()
    }

    /**
     * Given:   an empty currency code value
     * When:    highPrice is bound
     * Then:    show pending highPrice
     */
    @Test
    fun `when currency symbol is empty then display price pending`() {
        val tripWithNullFare = TripInfo(
                tripState = TripStatus.COMPLETED)

        whenever(fareService.fareDetails(anyString())).thenReturn(fareCall)
        doNothing().whenever(fareCall).execute(fareCaptor.capture())

        presenter = RideDetailPresenter(
                view = view,
                trip = tripWithNullFare,
                tripsService = tripsService,
                scheduledDateBinder = scheduledDateViewBinder,
                feedbackCompletedTripsStore = feedbackCompletedTripsStore,
                fareService = fareService)

        presenter.bindPrice()
        fareCaptor.firstValue.invoke(Resource.Success(FARE_NO_CURRENCY))

        verify(view, times(2)).displayPricePending()
    }

    /**
     * Given:   a fare is available
     * When:    highPrice is bound
     * Then:    show pending highPrice
     */
    @Test
    fun `when fare is available then display price`() {
        val tripWithFare = TripInfo(
                tripState = TripStatus.COMPLETED)

        whenever(fareService.fareDetails(anyString())).thenReturn(fareCall)
        doNothing().whenever(fareCall).execute(fareCaptor.capture())

        presenter = RideDetailPresenter(
                view = view,
                trip = tripWithFare,
                tripsService = tripsService,
                scheduledDateBinder = scheduledDateViewBinder,
                feedbackCompletedTripsStore = feedbackCompletedTripsStore,
                fareService = fareService)

        presenter.bindPrice()
        fareCaptor.firstValue.invoke(Resource.Success(FARE_COMPLETE))

        verify(view).displayPrice("£21.34")
    }

    /**
     * Given:   A valid trip with a pollable state
     * When:    presenter onResume
     * Then:    Observer should be added to the observable
     * And:    The correct endpoint is called
     */
    @Test
    fun `observer is added to observable`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                origin = TripLocationInfo(),
                dateScheduled = Date(0),
                destination = TripLocationInfo(),
                tripState = TripStatus.CONFIRMED,
                vehicle = Vehicle(),
                quote = Price(total = 123, currency = "GBP"))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)
        presenter.onResume()
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(trip))

        verify(observable).subscribe(any(), pollingTimeCaptor.capture())
        verify(tripsService).trackTrip(TRIP_ID)

        assertEquals(TRIP_INFO_UPDATE_PERIOD, pollingTimeCaptor.firstValue)
    }

    /**
     * Given:   A valid trip with a pollable state
     * When:    presenter onResume
     * And:     it is a guest booking
     * Then:    Observer should be added to the observable
     * And:    The correct endpoint is called
     */
    @Test
    fun `observer is added to observable for guest booking`() {
        UnitTestUISDKConfig.setGuestAuthentication(context)
        val trip = TripInfo(
                tripId = TRIP_ID,
                followCode = FOLLOW_CODE,
                origin = TripLocationInfo(),
                dateScheduled = Date(0),
                destination = TripLocationInfo(),
                tripState = TripStatus.CONFIRMED,
                vehicle = Vehicle(),
                quote = Price(total = 123, currency = "GBP"))
        whenever(tripsService.trackTrip(FOLLOW_CODE)).thenReturn(tripDetailsCall)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)
        presenter.onResume()
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(trip))

        verify(observable).subscribe(any(), pollingTimeCaptor.capture())
        verify(tripsService).trackTrip(FOLLOW_CODE)

        assertEquals(TRIP_INFO_UPDATE_PERIOD, pollingTimeCaptor.firstValue)
    }

    /**
     * Given:   A valid trip with a completed/non-live status
     * When:    presenter onResume
     * Then:    No Observable is added to the observable
     */
    @Test
    fun `no observable is added to observable`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.COMPLETED,
                origin = TripLocationInfo(),
                vehicle = Vehicle())

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)
        presenter.onResume()

        verify(observable, never()).subscribe(any(), any())
    }

    /**
     * When:    onPause is called
     * Then:    The All observers are disposed
     */
    @Test
    fun `observers are disposed on pause`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                origin = TripLocationInfo(),
                dateScheduled = Date(0),
                destination = TripLocationInfo(),
                tripState = TripStatus.CONFIRMED,
                vehicle = Vehicle(),
                quote = Price(total = 123, currency = "GBP"))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)
        presenter.onResume()
        presenter.onPause()
        verify(observable).unsubscribe(any())
    }

    /**
     * Given:   a trip with a completed/non-live status, origin place id and destination place id
     * When:    binding buttons
     * Then:    display rebook button
     */
    @Test
    fun `displays rebook button`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.COMPLETED,
                origin = TripLocationInfo(placeId = "ORIGIN01"),
                destination = TripLocationInfo(placeId = "DESTINATION01"))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).displayRebookButton()
    }

    /**
     * Given:   a trip with a completed/non-live status
     * When:    binding buttons
     * Then:    display report issue button
     */
    @Test
    fun `displays report issue button`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.COMPLETED,
                origin = TripLocationInfo(placeId = "ORIGIN01"),
                destination = TripLocationInfo(placeId = "DESTINATION01"))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).displayReportIssueButton()
    }

    /**
     * Given:   A trip with a completed/non-live status
     * When:    binding buttons
     * Then:    hide cancel and contact fleet buttons
     */
    @Test
    fun `hides cancel ride and contact fleet buttons`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.COMPLETED,
                origin = TripLocationInfo(placeId = "ORIGIN01"),
                destination = TripLocationInfo(placeId = "DESTINATION01"))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).hideContactOptions()
        verify(view).hideContactOptions()
    }

    /**
     * Given:   A trip with a live status
     * When:    binding buttons
     * Then:    hide rebook and report issue buttons
     */
    @Test
    fun `hides rebook ride and report issue buttons`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.DRIVER_EN_ROUTE)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).hideRebookButton()
        verify(view).hideReportIssueButton()
    }

    /**
     * Given:   A trip with a live status before POB
     * When:    binding buttons
     * Then:    show cancel ride
     */
    @Test
    fun `displays cancel ride button before POB`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.ARRIVED)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).displayContactOptions()
    }

    /**
     * Given:   A trip with a live status of POB
     * When:    binding buttons
     * Then:    hide cancel ride
     */
    @Test
    fun `shows contact options when POB`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.PASSENGER_ON_BOARD)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).displayContactOptions()
    }

    /**
     * Given:   A trip with a live status and a fleet number
     * When:    binding buttons
     * Then:    show contact fleet
     */
    @Test
    fun `displays contact fleet button`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.PASSENGER_ON_BOARD)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).displayContactOptions()
    }

    /**
     * Given:   A trip with null flight details
     * When:    binding flight details
     * Then:    The view should be told to hide the flight details UI
     */
    @Test
    fun `flight details hide when not available`() {
        presenter.bindFlightDetails()

        verify(view).hideFlightDetails()
        verify(view, never()).displayFlightDetails(anyString(), anyString())
    }

    /**
     * Given:   A trip with flight details
     * When:    binding flight details
     * Then:    The view should be told to display flight details
     */
    @Test
    fun `flight details display when available`() {
        val FLIGHT_NUM = "KH001"
        val FLIGHT_COMMENTS = "Terminal 2 pickup point"
        val trip = TripInfo(
                tripId = TRIP_ID,
                flightNumber = FLIGHT_NUM,
                comments = FLIGHT_COMMENTS)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindFlightDetails()
        verify(view).displayFlightDetails(FLIGHT_NUM, "")
    }

    /**
     * Given:   A trip with null comments
     * When:    binding comments
     * Then:    The view should be told to hide the comments UI
     */
    @Test
    fun `trip comments hide when not available`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                flightNumber = "KH002",
                comments = null)
        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindComments()

        verify(view).hideComments()
        verify(view, never()).displayComments(anyString())
    }

    /**
     * Given:   A trip with comments
     * When:    binding comments
     * Then:    The view should be told to display comments UI
     */
    @Test
    fun `comments display when available`() {
        val TRIP_COMMENTS = "Terminal 2 pickup point"
        val trip = TripInfo(
                tripId = TRIP_ID,
                flightNumber = "KH002",
                comments = TRIP_COMMENTS)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindComments()
        verify(view).displayComments(TRIP_COMMENTS)
    }

    /**
     * Given:   A trip with upcoming state
     * When:    Binding the highPrice
     * Then:    The view should be told to display base highPrice
     */
    @Test
    fun `binding upcoming trip tells view to show base price`() {
        val trip = TripInfo(
                tripState = TripStatus.CONFIRMED,
                quote = Price(total = 123, currency = "GBP", quoteType = QuoteType.METERED))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindPrice()

        verify(view).displayBasePrice("£1.23")
    }

    /**
     * Given:   A user presses the base fare icon
     * When:    The icon is displayed
     * Then:    The view should be asked to display the base fare dialog
     */
    @Test
    fun `pressing the basefare dialog tells the view to show the base fare`() {
        presenter.baseFarePressed()
        verify(view).displayBaseFareDialog()
    }

    /**
     * Given:   a view and a trip
     * When:    binding date
     * Then:    view binder handles the binding
     */
    @Test
    fun `scheduled date view binding handled by ScheduledDateViewBinder`() {
        val trip = TripInfo(
                tripId = TRIP_ID)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, feedbackCompletedTripsStore)

        presenter.bindDate()

        verify(scheduledDateViewBinder).bind(view, trip)
    }

    /**
     * Given:   The trip feedback has been submitted
     * When:    presenter onResume
     * Then:    showFeedbackSubmitted is called
     */
    @Test
    fun `show feedback submitted called when feedback already submitted`() {
        whenever(feedbackCompletedTripsStore.contains(TRIP_ID)).thenReturn(true)

        presenter.onResume()

        verify(view).showFeedbackSubmitted()
    }

    /**
     * Given:   The trip has a service cancellation of type before pickup with a tripStatus different
     *          than requested or confirmed
     * Then:    The cancellation text is not shown
     */
    @Test
    fun `When the trip has a service cancellation of type before pickup with a passenger on board trip state, the cancellation text is not shown`() {
        presenter.checkCancellationSLA(
                context,
                tripInfo,
                UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_PICKUP.freeCancellation)

        verify(view, never()).showCancellationText(any())
        verify(view, never()).setCancellationText(any())
    }

    /**
     * Given:   The trip has a service cancellation of type before pickup with a tripStatus equal to confirmed
     *
     * Then:    The cancellation text is shown
     * Then:    The cancellation text is the correct one
     */
    @Test
    fun `When the asap trip has a service cancellation of type before pickup, the asap cancellation text is shown`() {
        tripInfo = TripInfo(tripState = TripStatus.CONFIRMED)
        presenter.checkCancellationSLA(
                context,
                tripInfo,
                UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_PICKUP.freeCancellation)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(String.format(BookingQuotesPresenterTest.TEST_CANCELLATION_TEXT_ASAP, UpcomingRideCardPresenterTest.TEST_TWO_MINUTES))
    }

    /**
     * Given:   The trip has a service cancellation of type before pickup with a tripStatus equal to confirmed
     *
     * Then:    The cancellation text is shown
     * Then:    The cancellation text is the correct one
     */
    @Test
    fun `When the prebook trip has a service cancellation of type before pickup, the prebook cancellation text is shown`() {
        tripInfo = TripInfo(tripState = TripStatus.CONFIRMED,
                            dateBooked = "2021-06-17T09:39:24Z",
                            dateScheduled = DateUtil.parseDateString("2021-06-18T09:39:24Z"))
        presenter.checkCancellationSLA(
                context,
                tripInfo,
                UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_PICKUP.freeCancellation)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(String.format(BookingQuotesPresenterTest.TEST_CANCELLATION_TEXT_PREBOOK, UpcomingRideCardPresenterTest.TEST_TWO_MINUTES))
    }

    /**
     * Given:   The trip has a service cancellation of type before driver en route with a tripStatus equal to confirmed
     *
     * Then:    The cancellation text is shown
     * Then:    The cancellation text is the correct one
     */
    @Test
    fun `When the trip has a service cancellation of type before driver en route with a confirmed status, the cancellation text is shown`() {
        tripInfo = TripInfo(tripState = TripStatus.CONFIRMED)
        presenter.checkCancellationSLA(
                context,
                tripInfo,
                UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_DRIVER_EN_ROUTE.freeCancellation)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)

    }

    /**
     * Given:   The trip has a service cancellation of type before driver en route with a tripStatus equal to requested
     *
     * Then:    The cancellation text is shown
     * Then:    The cancellation text is the correct one
     */
    @Test
    fun `When the trip has a service cancellation of type before driver en route with a requested status, the cancellation text is shown`() {
        tripInfo = TripInfo(tripState = TripStatus.CONFIRMED)
        presenter.checkCancellationSLA(
                context,
                tripInfo,
                UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_DRIVER_EN_ROUTE.freeCancellation)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
    }

    /**
     * Given:   The trip has a service cancellation of type before driver en route with a tripStatus equal to requested
     *
     * Then:    The cancellation text is shown
     * Then:    The cancellation text is the correct one
     */
    @Test
    fun `When the asap trip has a service cancellation of type before pickup with a requested status, the asap cancellation text is shown`() {
        tripInfo = TripInfo(tripState = TripStatus.CONFIRMED)
        presenter.checkCancellationSLA(
                context,
                tripInfo,
                UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_PICKUP.freeCancellation)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(String.format(BookingQuotesPresenterTest.TEST_CANCELLATION_TEXT_ASAP, UpcomingRideCardPresenterTest.TEST_TWO_MINUTES))
    }

    /**
     * Given:   The trip has a service cancellation of type before driver en route with a tripStatus equal to requested
     *
     * Then:    The cancellation text is shown
     * Then:    The cancellation text is the correct one
     */
    @Test
    fun `When the prebook trip has a service cancellation of type before pickup with a requested status, the prebook cancellation text is shown`() {
        tripInfo = TripInfo(tripState = TripStatus.CONFIRMED,
                            dateBooked = "2021-06-17T09:39:24Z",
                            dateScheduled = DateUtil.parseDateString("2021-06-18T09:39:24Z"))
        presenter.checkCancellationSLA(
                context,
                tripInfo,
                UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_PICKUP.freeCancellation)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(String.format(BookingQuotesPresenterTest.TEST_CANCELLATION_TEXT_PREBOOK, UpcomingRideCardPresenterTest.TEST_TWO_MINUTES))
    }

    companion object {
        private val BREAKDOWN_NO_CURRENCY = FareBreakdown(
                total = 2134,
                currency = ""
        )

        private val FARE_NO_CURRENCY = Fare(
                state = "COMPLETE",
                breakdown = BREAKDOWN_NO_CURRENCY
        )

        private val FARE_COMPLETE = FARE_NO_CURRENCY.copy(
                breakdown = BREAKDOWN_NO_CURRENCY.copy(
                        currency = "GBP"))
    }

}
