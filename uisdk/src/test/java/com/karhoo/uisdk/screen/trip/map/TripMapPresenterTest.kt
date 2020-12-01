package com.karhoo.uisdk.screen.trip.map

import android.location.Location
import com.karhoo.sdk.api.model.Direction
import com.karhoo.sdk.api.model.DriverTrackingInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.drivertracking.DriverTrackingService
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.PollCall
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.domain.userlocation.LocationProvider
import com.karhoo.uisdk.screen.booking.domain.userlocation.PositionListener
import com.karhoo.uisdk.screen.trip.map.TripMapPresenter.Companion.TRIP_INFO_UPDATE_PERIOD
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TripMapPresenterTest {

    private var view: TripMapMVP.View = mock()
    private var analytics: Analytics = mock()
    private var locationProvider: LocationProvider = mock()
    private var driverTrackingService: DriverTrackingService = mock()
    private var tripsService: TripsService = mock()
    private var driverTrackingCall: PollCall<DriverTrackingInfo> = mock()
    private var observableDriverTracking: Observable<DriverTrackingInfo> = mock()
    private var observableTripInfo: Observable<TripInfo> = mock()
    private var tripDetailsCall: PollCall<TripInfo> = mock()

    private val observerDriverTrackingCaptor = argumentCaptor<Observer<Resource<DriverTrackingInfo>>>()
    private val observerTripInfoCaptor = argumentCaptor<Observer<Resource<TripInfo>>>()
    private val pollingTimeCaptor = argumentCaptor<Long>()

    @InjectMocks
    private lateinit var presenter: TripMapPresenter

    @Before
    fun setUp() {
        whenever(driverTrackingService.trackDriver(TRIP_ID)).thenReturn(driverTrackingCall)
        whenever(driverTrackingCall.observable()).thenReturn(observableDriverTracking)
        doNothing().whenever(observableDriverTracking).subscribe(observerDriverTrackingCaptor.capture(), any())

        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)
        //default null state, this should be behaviour should be overridden for tests where state matters
        whenever(tripDetailsCall.observable()).thenReturn(observableTripInfo)
        doNothing().whenever(observableTripInfo).subscribe(observerTripInfoCaptor.capture(), any())

    }

    /**
     * Given:   An invalid string as a trip id is asked to track driver position
     * When:    Trying to track a driver position
     * Then:    The call should not be executed
     */
    @Test
    fun `when tracking driver for a trip with a blank string nothing happens`() {
        presenter.trackDriverPosition("")
        verify(driverTrackingService, never()).trackDriver(anyString())
    }

    /**
     * Given:   A valid trip id
     * When:    Trying to track driver
     * Then:    The view should be updated of changes to position
     */
    @Test
    fun `view receives driver position updates`() {
        presenter.trackDriverPosition(TRIP_ID)
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))
        verify(view).animateDriverPositionToLatLng(anyInt(), eq(LAT), eq(LNG))
    }

    /**
     * Given:   A valid trip id, origin set, trip is DER
     * When:    Trying to track driver
     * Then:    The view should be zoomed to include origin and driver position
     */
    @Test
    fun `zooms view to include origin and driver position when DER`() {
        presenter.setOrigin(ORIGIN)
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.DRIVER_EN_ROUTE)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        verify(view).zoomMapToIncludeLatLngs(anyInt(), eq(DRIVER_POSITION.position), eq(ORIGIN))
    }

    /**
     * Given:   A valid trip id, origin set, trip is approaching
     * When:    Trying to track driver
     * Then:    The view should be zoomed to include origin and driver position
     */
    @Test
    fun `zooms view to include origin and driver position when approaching`() {
        presenter.setOrigin(ORIGIN)
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.DRIVER_EN_ROUTE)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        verify(view).zoomMapToIncludeLatLngs(anyInt(), eq(DRIVER_POSITION.position), eq(ORIGIN))
    }

    /**
     * Given:   A valid trip id, origin set, trip is arrived
     * When:    Trying to track driver
     * Then:    The view should be zoomed to include origin and driver position
     */
    @Test
    fun `zooms view to include origin and driver position when arrived`() {
        presenter.setOrigin(ORIGIN)
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.ARRIVED)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        verify(view).zoomMapToIncludeLatLngs(anyInt(), eq(DRIVER_POSITION.position), eq(ORIGIN))
    }

    /**
     * Given:   A valid trip id, destination set, trip is POB
     * When:    Trying to track driver
     * Then:    The view should be zoomed to include destination and driver position
     */
    @Test
    fun `zooms view to include destination and driver position when POB`() {
        presenter.setDestination(DESTINATION)
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        verify(view).zoomMapToIncludeLatLngs(anyInt(), any(), any())
    }

    /**
     * Given:   A valid trip id, origin set, trip is allocated
     * When:    Trying to track driver
     * Then:    The view should be zoomed to include destination and driver position
     */
    @Test
    fun `zooms view to include origin and driver position when allocated`() {
        presenter.setOrigin(ORIGIN)
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.DRIVER_EN_ROUTE)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        verify(view).zoomMapToIncludeLatLngs(anyInt(), eq(DRIVER_POSITION.position), eq(ORIGIN))
    }

    /**
     * Given:   A valid trip id
     * When:    Trying to track driver
     * Then:    An observer should be added to the CompositeDisposableContainer
     */
    @Test
    fun `observables for the service are added to composite disposable container`() {
        presenter.trackDriverPosition(TRIP_ID)
        verify(observableTripInfo, times(1)).subscribe(any(), pollingTimeCaptor.capture())
        verify(observableDriverTracking, times(1)).subscribe(any(), any())

        assertEquals(TRIP_INFO_UPDATE_PERIOD, pollingTimeCaptor.firstValue)
    }

    /**
     * Given:   A valid trip id, origin and destination
     * When:    Map is ready
     * Then:    Add markers to the map and zoom to fit both
     */
    @Test
    fun `markers added and zoomed`() {
        presenter.setOrigin(ORIGIN)
        presenter.setDestination(DESTINATION)

        presenter.mapIsReady()

        verify(view).addPinToMap(ORIGIN, true, R.string.address_pick_up)
        verify(view).addPinToMap(DESTINATION, false, R.string.address_drop_off)
        verify(view).zoomMapToIncludeLatLngs(anyInt(), eq(ORIGIN), eq(DESTINATION))
    }

    /**
     * Given:   A valid trip id, destination set, trip is POB, map is dragged
     * When:    Trying to track driver
     * Then:    The view should not zoom
     */
    @Test
    fun `map does not zoom when map dragged`() {
        presenter.mapDragged()
        presenter.setDestination(DESTINATION)
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))

        verify(view, never()).zoomMapToIncludeLatLngs(anyInt(), any(), any())
    }

    /**
     * Given:   A valid trip id, destination set, trip is POB, map is dragged, tracking driver
     * When:    locateMe is selected
     * Then:    The view should zoom immediately
     */
    @Test
    fun `map immediately zooms after locate me`() {
        presenter.mapDragged()
        presenter.setDestination(DESTINATION)
        presenter.trackDriverPosition(TRIP_ID)

        presenter.locateMe()
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))
        observerDriverTrackingCaptor.firstValue.onValueChanged(Resource.Success(DRIVER_POSITION))

        //NOTE: should only be called times(1)!
        verify(view).zoomMapToIncludeLatLngs(anyInt(), eq(DRIVER_POSITION.position), eq(DESTINATION))
    }

    /**
     * When:    onDestroy is called
     * Then:    The CompositeDisposableContainer is disposed
     */
    @Test
    fun `observer is unsubscribed on destroy`() {
        presenter.trackDriverPosition(TRIP_ID)
        presenter.onStop()
        verify(observableDriverTracking).unsubscribe(any())
        verify(observableTripInfo).unsubscribe(any())
    }

    /**
     * When:    onDestroy is called
     * Then:    The CompositeDisposableContainer is disposed
     */
    @Test
    fun `composite disposable container is disposed on destroy`() {
        presenter.trackDriverPosition(TRIP_ID)
        presenter.onDestroy()
        verify(observableDriverTracking).unsubscribe(any())
        verify(observableTripInfo).unsubscribe(any())
    }

    /**
     * Given:   tripState is passenger onboard
     * When:    Tracking driver
     * Then:    The user location is not visible
     */
    @Test
    fun `user location visible is set false`() {
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))

        verify(view).userLocationVisible = false
    }

    /**
     * Given:   tripState is other than passenger on board
     * When:    Tracking driver
     * Then:    The user location is visible
     */
    @Test
    fun `user location visible is set true`() {
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.DRIVER_EN_ROUTE)))

        verify(view).userLocationVisible = true
    }

    @Test
    fun `start getting location updates onResume`() {
        presenter.onResume()
        verify(locationProvider).listenForLocations(any(), eq(null))
    }

    @Test
    fun `stop getting location updates onPause`() {
        presenter.onPause()
        verify(locationProvider).stopListeningForLocations()
    }

    /**
     * Given:   tracking trip and tripState not null
     * When:    user location update
     * Then:    analytics call made for user position changed
     */
    @Test
    fun `analytics call for user position changed made when location update`() {
        val location = Location("").apply {
            latitude = 1.0
            longitude = 2.0
        }

        presenter.onResume()
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))
        argumentCaptor<PositionListener>().apply {
            verify(locationProvider).listenForLocations(capture(), eq(null))
            val callback = firstValue
            callback.onPositionUpdated(location)
        }

        verify(analytics).userPositionChanged(TripStatus.PASSENGER_ON_BOARD, location)
    }

    /**
     * Given:   Tracking a trip
     * When:    The trip has not ended
     * Then:    Then the observers remain subscribed
     */
    @Test
    fun `observers remain subscribed when trip has not ended`() {
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState(TripStatus.PASSENGER_ON_BOARD)))

        verify(observableTripInfo, never()).unsubscribe(any())
        verify(observableDriverTracking, never()).unsubscribe(any())
    }

    /**
     * Given:   Tracking a trip
     * When:    The trip has ended
     * Then:    Then the observers are unsubscribed
     */
    @Test
    fun `observers are unsubscribed when trip has ended`() {
        presenter.trackDriverPosition(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripWithState
                                                                          (TripStatus.COMPLETED)))

        verify(observableTripInfo).unsubscribe(any())
        verify(observableDriverTracking).unsubscribe(any())
    }

    companion object {

        private val TRIP_ID = "1234"
        private val LAT = 0.534
        private val LNG = 1.432
        private val KPH = 10
        private val HEADING = 180
        private val DRIVER_POSITION = DriverTrackingInfo(position = Position(LAT, LNG), direction
        = Direction(kph = KPH, heading = HEADING))
        private val ORIGIN = Position(0.111, 0.222)
        private val DESTINATION = Position(0.333, 0.444)

        private fun tripWithState(state: TripStatus?): TripInfo {
            return TripInfo(
                    tripId = TRIP_ID,
                    tripState = state)
        }
    }

}
