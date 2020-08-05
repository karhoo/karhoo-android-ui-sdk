package com.karhoo.uisdk.screen.rides.detail

import android.content.Context
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.model.Fare
import com.karhoo.sdk.api.model.FareBreakdown
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.Price
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.request.TripCancellation
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.fare.FareService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.Call
import com.karhoo.sdk.call.PollCall
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.UnitTestUISDKConfig
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.screen.rides.detail.RideDetailPresenter.Companion.TRIP_INFO_UPDATE_PERIOD
import com.karhoo.uisdk.screen.rides.feedback.FeedbackCompletedTripsStore
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
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class RideDetailPresenterTest {

    private var context: Context = mock()
    private val view: RideDetailMVP.View = mock()
    private val tripsService: TripsService = mock()
    private val tripDetailsCall: PollCall<TripInfo> = mock()
    private val scheduledDateViewBinder: ScheduledDateViewBinder = mock()
    private val analytics: Analytics = mock()
    private val feedbackCompletedTripsStore: FeedbackCompletedTripsStore = mock()
    private val cancelTripCall: Call<Void> = mock()
    private val observable: Observable<TripInfo> = mock()
    private var fareService: FareService = mock()
    private var fareCall: Call<Fare> = mock()

    private val TRIP_ID = "trip001"
    private val FOLLOW_CODE = "follow001"
    private val EMPTY_TRIP: TripInfo = TripInfo(
            tripId = TRIP_ID)

    private var presenter: RideDetailPresenter = RideDetailPresenter(
            view = view,
            trip = EMPTY_TRIP,
            tripsService = tripsService,
            scheduledDateBinder = scheduledDateViewBinder,
            analytics = analytics,
            feedbackCompletedTripsStore = feedbackCompletedTripsStore,
            fareService = fareService)

    private val cancelTripLambdaCaptor = argumentCaptor<(Resource<Void>) -> Unit>()
    private val observerTripInfoCaptor = argumentCaptor<Observer<Resource<TripInfo>>>()
    private val pollingTimeCaptor = argumentCaptor<Long>()
    private var fareCaptor = argumentCaptor<(Resource<Fare>) -> Unit>()

    @Before
    fun setUp() {
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.KarhooUser()))
        Locale.setDefault(Locale.UK)
        doNothing().whenever(cancelTripCall).execute(cancelTripLambdaCaptor.capture())
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)
        whenever(tripDetailsCall.observable()).thenReturn(observable)
        doNothing().whenever(observable).subscribe(observerTripInfoCaptor.capture(), anyLong())
    }

    /**
     * Given:   a cancelled trip
     * When:    state is bound
     * Then:    display cancelled icon with 'cancelled' text
     */
    @Test
    fun `when state is bound then display cancelled icon with cancelled text`() {
        val cancelledTrip = TripInfo(tripState = TripStatus.CANCELLED_BY_USER)
        presenter = RideDetailPresenter(view, cancelledTrip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_cancelled, R.string.ride_state_cancelled, R.color.off_black)
    }

    /**
     * Given:   there are no drivers trip
     * When:    state is bound
     * Then:    display cancelled icon with 'cancelled' text
     */
    @Test
    fun `when state is bound and there are no drivers a cancelled icon should show`() {
        val noDriverTrip = TripInfo(tripState = TripStatus.NO_DRIVERS)
        presenter = RideDetailPresenter(view, noDriverTrip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_cancelled, R.string.ride_state_cancelled, R.color.off_black)
    }

    /**
     * Given:   a completed trip
     * When:    state is bound
     * Then:    display completed icon with 'completed' text
     */
    @Test
    fun `when state is bound then display completed icon with completed text`() {
        val completedTrip = TripInfo(tripState = TripStatus.COMPLETED)
        presenter = RideDetailPresenter(view, completedTrip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_completed, R.string.ride_state_completed, R.color.off_black)
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
                analytics = analytics,
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
                analytics = analytics,
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
                analytics = analytics,
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)
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
        KarhooUISDKConfigurationProvider.setConfig(configuration = UnitTestUISDKConfig(context =
                                                                                       context,
                                                                                       authenticationMethod = AuthenticationMethod.Guest("identifier", "referer", "guestOrganisationId")))
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).hideCancelRideButton()
        verify(view).hideContactFleetButton()
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).displayCancelRideButton()
    }

    /**
     * Given:   A trip with a live status of POB
     * When:    binding buttons
     * Then:    hide cancel ride
     */
    @Test
    fun `hides cancel ride button when POB`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.PASSENGER_ON_BOARD)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).hideCancelRideButton()
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.bindButtons()
        verify(view).displayContactFleetButton()
    }

    /**
     * Given:   A trip with a fleet phone number
     * When:    contact fleet
     * Then:    make call to fleet phone number
     */
    @Test
    fun `makes call to fleet phone number`() {
        val PHONE_NUMBER = "+441234567891"
        val trip = TripInfo(
                tripId = TRIP_ID,
                tripState = TripStatus.DRIVER_EN_ROUTE,
                fleetInfo = FleetInfo(phoneNumber = PHONE_NUMBER))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

        presenter.contactFleet()
        verify(view).makeCall(PHONE_NUMBER)
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel has started
     * Then:    An event that the user pressed cancel should be sent
     */
    @Test
    fun `user presses cancel trip fires event`() {
        val trip = TripInfo(
                tripId = TRIP_ID,
                origin = TripLocationInfo(),
                destination = TripLocationInfo(),
                tripState = TripStatus.CONFIRMED,
                vehicle = Vehicle())

        whenever(tripsService.cancel(TripCancellation(TRIP_ID))).thenReturn(cancelTripCall)

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)
        presenter.cancelTrip()

        cancelTripLambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(analytics).userCancelTrip(trip)
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel has started
     * Then:    the loading dialog is shown
     */
    @Test
    fun `user presses cancel shows loading dialog`() {
        whenever(tripsService.cancel(TripCancellation(TRIP_ID))).thenReturn(cancelTripCall)

        presenter.cancelTrip()
        cancelTripLambdaCaptor.firstValue.invoke(mock())

        verify(view).displayLoadingDialog()
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel is unsuccessful
     * Then:    The view should be told to display the call dialog
     */
    @Test
    fun `user cancels trip unsuccessfully`() {
        val FLEET_NAME = "Keaney's Cars"
        val FLEET_NUMBER = "+353 1234 567891"
        val trip = TripInfo(
                tripId = TRIP_ID,
                fleetInfo = FleetInfo(
                        name = FLEET_NAME,
                        phoneNumber = FLEET_NUMBER))

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)
        whenever(tripsService.cancel(TripCancellation(TRIP_ID))).thenReturn(cancelTripCall)

        presenter.cancelTrip()
        cancelTripLambdaCaptor.firstValue.invoke(Resource.Failure(mock()))

        verify(view).displayCallToCancelDialog(FLEET_NUMBER, FLEET_NAME)
    }

    /**
     * Given:   fleet info is null
     * When:    attempting to cancel trip, sdk callback onServiceError
     * Then:    show error
     */
    @Test
    fun `show error when attempting to cancel on service error trip and fleet info is null`() {
        whenever(tripsService.cancel(TripCancellation(TRIP_ID))).thenReturn(cancelTripCall)

        presenter.cancelTrip()
        cancelTripLambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).hideLoadingDialog()
        verify(view).displayError(anyInt())
    }

    /**
     * Given:   A user wishes to cancel a trip
     * When:    The cancel is successful by karhoo
     * Then:    The view should be told to display the complete dialog
     */
    @Test
    fun `user cancels trip successfully by karhoo`() {
        whenever(tripsService.cancel(TripCancellation(TRIP_ID))).thenReturn(cancelTripCall)

        presenter.cancelTrip()
        cancelTripLambdaCaptor.firstValue.invoke(Resource.Success(mock()))

        verify(view).hideLoadingDialog()
        verify(view).displayTripCancelledDialog()
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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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
        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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

        presenter = RideDetailPresenter(view, trip, tripsService, scheduledDateViewBinder, analytics, feedbackCompletedTripsStore)

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

    private fun createInstanceOfVoid(): Void? {
        return try {
            val constructor = Void::class.java.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        } catch (e: Exception) {
            null
        }
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
