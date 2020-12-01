package com.karhoo.uisdk.screen.trip.eta

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
import com.karhoo.uisdk.analytics.Analytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
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

@RunWith(MockitoJUnitRunner::class)
class EtaPresenterTest {

    private var view: EtaMVP.View = mock()
    private var driverTrackingService: DriverTrackingService = mock()
    private var tripsService: TripsService = mock()
    private var driverTrackingCall: PollCall<DriverTrackingInfo> = mock()
    private var observableDriverTracking: Observable<DriverTrackingInfo> = mock()
    private var tripStatusCall: PollCall<TripState> = mock()
    private var observableTripState: Observable<TripState> = mock()
    private var analytics: Analytics = mock()

    private var error: Throwable? = null

    private val observerDriverTrackingCaptor = argumentCaptor<Observer<Resource<DriverTrackingInfo>>>()
    private val observerTripStateCaptor = argumentCaptor<Observer<Resource<TripState>>>()

    @InjectMocks
    private lateinit var presenter: EtaPresenter

    @Before
    fun setUp() {
        whenever(driverTrackingService.trackDriver(TRIP_ID)).thenReturn(driverTrackingCall)
        whenever(driverTrackingCall.observable()).thenReturn(observableDriverTracking)
        doNothing().whenever(observableDriverTracking).subscribe(observerDriverTrackingCaptor.capture(), anyLong())

        whenever(tripsService.status(TRIP_ID)).thenReturn(tripStatusCall)
        whenever(tripStatusCall.observable()).thenReturn(observableTripState)
        doNothing().whenever(observableTripState).subscribe(observerTripStateCaptor.capture(), anyLong())

        error = Throwable("error")
    }

    /**
     * Given:   A null trip id
     * When:    Trying to monitor eta
     * Then:    The call should not be executed
     */
    @Test
    fun `when tracking driver for a trip with a empty string nothing happens`() {
        presenter.monitorEta("")
        verify(tripsService, never()).trackTrip(anyString())
        verify(driverTrackingService, never()).trackDriver(anyString())
    }

    /**
     * Given:   A valid trip id
     * When:    Trying to monitor eta
     * Then:    Two observables should be added to the CompositeDisposableContainer
     */
    @Test
    fun `observers for both services are subscribed to the observable`() {
        presenter.monitorEta(TRIP_ID)
        verify(observableDriverTracking).subscribe(any(), anyLong())
        verify(observableTripState).subscribe(any(), anyLong())
    }

    /**
     * Given:   A valid trip id
     * When:    trip state is DER
     * Then:    display origin eta
     */
    @Test
    fun `show origin eta when trip state is DER`() {
        presenter.monitorEta(TRIP_ID)
        observerTripStateCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.DRIVER_EN_ROUTE)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        verify(view).showEta(ORIGIN_ETA)
    }

    /**
     * Given:   A valid trip id
     * When:    trip state is DER
     * Then:    send eta displayed analytics event
     */
    @Test
    fun `send analytics event when trip state is DER`() {
        presenter.monitorEta(TRIP_ID)
        observerTripStateCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.DRIVER_EN_ROUTE)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        verify(analytics).etaDisplayed(ORIGIN_ETA, TRIP_ID)
    }

    /**
     * Given:   A valid trip id
     * When:    trip state is POB
     * Then:    hide eta
     */
    @Test
    fun `hide eta when trip state is POB`() {
        presenter.monitorEta(TRIP_ID)
        observerTripStateCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))

        verify(view).hideEta()
    }

    /**
     * Given:   A valid trip id
     * When:    trip state is other than DER
     * Then:    hide eta
     */
    @Test
    fun `hide eta when trip state other than DER`() {
        presenter.monitorEta(TRIP_ID)
        observerTripStateCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.ARRIVED)))

        verify(view).hideEta()
    }

    /**
     * Given:   A valid tripId
     * When:    Driver position returns, trip state is null
     * Then:    ETA is hidden
     */
    @Test
    fun `hide eta when driver position returns and trip state is null`() {
        presenter.monitorEta(TRIP_ID)
        observerTripStateCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.ARRIVED)))

        verify(view, atLeastOnce()).hideEta()
    }

    /**
     * When:    onStop is called
     * Then:    The observable removes the observer
     */
    @Test
    fun `observers are unsubscribed on stop`() {
        presenter.monitorEta(TRIP_ID)
        presenter.onStop()
        verify(observableTripState).unsubscribe(any())
        verify(observableDriverTracking).unsubscribe(any())
    }

    /**
     * When:    onDestroy is called
     * Then:    The CompositeDisposableContainer is disposed
     */
    @Test
    fun `observers are unsubscribed on destroy`() {
        presenter.monitorEta(TRIP_ID)
        presenter.onDestroy()
        verify(observableTripState).unsubscribe(any())
        verify(observableDriverTracking).unsubscribe(any())
    }

    /**
     * Given:   trip details returns error
     * When:    monitoring eta
     * Then:    hide eta
     */
    @Test
    fun `hide eta in view when driver position error`() {
        presenter.monitorEta(TRIP_ID)
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Failure(karhooError))

        verify(view, times(1)).hideEta()
    }

    companion object {

        private val karhooError = KarhooError.Unexpected

        private val TRIP_ID = "1234"
        private val ORIGIN_ETA = 23
        private val DESTINATION_ETA = 43
        private val DRIVER_POSITION = DriverTrackingInfo(
                position = Position(0.5, 0.4),
                originEta = ORIGIN_ETA,
                destinationEta = DESTINATION_ETA,
                direction = Direction(kph = 10, heading = 180))
        private val TRIP_NULL_STATE = TripState(TripStatus.COMPLETED)

        private fun tripWithState(state: TripStatus) = TripState(state)
    }

}
