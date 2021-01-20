package com.karhoo.uisdk.screen.trip.deta

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Direction
import com.karhoo.sdk.api.model.DriverTrackingInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripState
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.drivertracking.DriverTrackingService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.PollCall
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.time.ZoneId

@RunWith(MockitoJUnitRunner::class)
class DetaPresenterTest {

    private var view: DetaMVP.View = mock()
    private var driverTrackingService: DriverTrackingService = mock()
    private var tripsService: TripsService = mock()
    private var driverTrackingCall: PollCall<DriverTrackingInfo> = mock()
    private var observableDriverTracking: Observable<DriverTrackingInfo> = mock()
    private var tripStateCall: PollCall<TripState> = mock()
    private var observableTripState: Observable<TripState> = mock()

    private var observerDriverCaptor = argumentCaptor<Observer<Resource<DriverTrackingInfo>>>()
    private var observerTripStateCaptor = argumentCaptor<Observer<Resource<TripState>>>()

    private var error: Throwable? = null

    @InjectMocks
    private lateinit var presenter: DetaPresenter

    @Before
    fun setUp() {
        whenever(driverTrackingService.trackDriver(any())).thenReturn(driverTrackingCall)
        whenever(driverTrackingCall.observable()).thenReturn(observableDriverTracking)
        doNothing().whenever(observableDriverTracking).subscribe(observerDriverCaptor.capture(), anyLong())

        whenever(tripsService.status(any())).thenReturn(tripStateCall)
        whenever(tripStateCall.observable()).thenReturn(observableTripState)
        doNothing().whenever(observableTripState).subscribe(observerTripStateCaptor.capture(), anyLong())

        error = Throwable("error")
    }

    /**
     * Given:   A null trip id
     * When:    Trying to monitor deta
     * Then:    The call should not be executed
     */
    @Test
    fun `when tracking driver for a trip with a empty string nothing happens`() {
        presenter.monitorDeta("", LONDON_TIMEZONE)
        verify(tripsService, never()).trackTrip(anyString())
        verify(driverTrackingService, never()).trackDriver(anyString())
    }

    /**
     * Given:   A valid trip id
     * When:    Trying to monitor deta
     * Then:    Two observers should be added to the observable
     */
    @Test
    fun `observers for both services are added to observables`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        verify(observableTripState).subscribe(any(), anyLong())
        verify(observableDriverTracking).subscribe(any(), anyLong())
    }

    /**
     * Given:   A valid trip id
     * When:    trip state is DER
     * Then:    display deta
     */
    @Test
    fun `show origin deta when trip state is POB`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        observerTripStateCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))
        observerDriverCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        if (isDST) {
            verify(view).showDeta(DESTINATION_ETA, LONDON_BST_OFFSET)
        } else {
            verify(view).showDeta(DESTINATION_ETA, 0)
        }
    }

    /**
     * Given:   a valid trip id
     * When:    trip state is DER, deta returned is less than 1 minute
     * Then:    display deta as 1 minute
     */
    @Test
    fun `show minimum 1 min deta when trip state is POB`() {
        val driverPosition = DriverTrackingInfo(
                position = Position(1.0, 2.0),
                destinationEta = -1,
                direction = Direction(1, 2))

        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        observerTripStateCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))
        observerDriverCaptor.firstValue.onValueChanged(Resource.Success(driverPosition))

        if (isDST) {
            verify(view).showDeta(1, LONDON_BST_OFFSET)
        } else {
            verify(view).showDeta(1, 0)
        }
    }

    /**
     * Given:   A valid trip id
     * When:    trip state is ARRIVED
     * Then:    hide deta
     */
    @Test
    fun `hide deta when trip state is before POB`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        observerTripStateCaptor.firstValue
                .onValueChanged(Resource.Success(tripWithState(TripStatus.ARRIVED)))

        verify(view).hideDeta()
    }

    /**
     * Given:   A valid trip id
     * When:    trip state is other than POB
     * Then:    hide deta
     */
    @Test
    fun `hide deta when trip state other than DER`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        observerTripStateCaptor.firstValue
                .onValueChanged(Resource.Success(tripWithState(TripStatus.ARRIVED)))

        verify(view).hideDeta()
    }

    /**
     * Given:   A valid tripId
     * When:    Driver position returns, trip state is null
     * Then:    DETA is hidden
     */
    @Test
    fun `hide deta when driver position returns and trip state is null`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        observerTripStateCaptor.firstValue
                .onValueChanged(Resource.Success(tripWithState(TripStatus.COMPLETED)))
        verify(view).hideDeta()
    }

    /**
     * When:    onDestroy is called
     * Then:    The CompositeDisposableContainer is disposed
     */
    @Test
    fun `observers are unsubscribed on destroy`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        presenter.onDestroy()
        verify(observableDriverTracking).unsubscribe(any())
        verify(observableTripState).unsubscribe(any())
    }

    /**
     * When:    onDestroy is called
     * Then:    The CompositeDisposableContainer is disposed
     */
    @Test
    fun `observer is unsubscribed on destroy`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        presenter.onStop()
        verify(observableDriverTracking).unsubscribe(any())
        verify(observableTripState).unsubscribe(any())
    }

    /**
     * Given:   trip details returns error
     * When:    monitoring deta
     * Then:    hide deta
     */
    @Test
    fun `hide deta in view when driver position error`() {
        presenter.monitorDeta(TRIP_ID, LONDON_TIMEZONE)
        observerDriverCaptor.firstValue.onValueChanged(Resource.Failure(karhooError))

        verify(view, times(1)).hideDeta()
    }

    companion object {

        private val karhooError = KarhooError.Unexpected

        private const val TRIP_ID = "1234"
        private const val DESTINATION_ETA = 43

        private val DRIVER_POSITION = DriverTrackingInfo(
                position = Position(0.5, 0.4),
                destinationEta = DESTINATION_ETA,
                direction = Direction(1, 2))

        private fun tripWithState(state: TripStatus) = TripState(state)

        private const val LONDON_TIMEZONE = "Europe/London"
        private const val LONDON_BST_OFFSET = 3600000

        private val isDST = ZoneId.of("Europe/London")
                .rules
                .isDaylightSavings(Instant.now())
    }

}
