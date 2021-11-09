package com.karhoo.uisdk.screen.trip.bookingstatus

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observable.Companion.BASE_POLL_TIME
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.PollCall
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusPresenter.Companion.IN_PROGRESS_TRIP_INFO_UPDATE_PERIOD
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
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BookingStatusPresenterTest {

    private var tripsService: TripsService = mock()
    private var tripDetailsCall: PollCall<TripInfo> = mock()
    private var view: BookingStatusMVP.View = mock()
    private var observable: Observable<TripInfo> = mock()

    @InjectMocks
    private lateinit var presenter: BookingStatusPresenter

    private val observerTripInfoCaptor = argumentCaptor<Observer<Resource<TripInfo>>>()
    private val pollingTimeCaptor = argumentCaptor<Long>()

    @Before
    fun setUp() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)
        whenever(tripDetailsCall.observable()).thenReturn(observable)
        doNothing().whenever(observable).subscribe(observerTripInfoCaptor.capture(), anyLong())
    }

    /**
     * Given:   An invalid string as a trip id is asked to be monitored
     * When:    Trying to observe a trips status
     * Then:    The call should not be executed
     */
    @Test
    fun `when requesting a trip with a blank string nothing happens`() {
        presenter.monitorTrip("")
        verify(tripsService, never()).trackTrip(anyString())
    }

    /**
     * Given:   A valid string as a trip id is asked to be monitored
     * When:    Trying to observe a trips status
     * Then:    The call should be executed successfully
     */
    @Test
    fun `valid trip id makes a request to monitor trip`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.DRIVER_EN_ROUTE)))

        verify(view).updateStatus(any(), anyString())
    }

    /**
     * Given:   An valid string as a trip id is asked to be monitored
     * When:    Trying to observe a trips status
     * And:     An error occurs during execution
     * Then:    The user should be notified there was an issue
     */
    @Test
    fun `trip monitoring error gets called when requesting trip and theres an issue`() {
        whenever(tripsService.trackTrip(TRIP_ID)).thenReturn(tripDetailsCall)

        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Failure(KarhooError.Unexpected))

        verify(view).showTemporaryError(anyString(), any())
    }

    /**
     * Given:   A trip has been cancelled by the dispatch
     * When:    Trying to book a trip with a quote
     * Then:    A call to display the cancellation dialog should happen
     */
    @Test
    fun `trip cancelled by dispatch shows dialog`() {
        val tripDetails = tripDetailsWithState(TripStatus.CANCELLED_BY_DISPATCH)
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetails))

        verify(view).showCancellationDialog(tripDetails)
    }

    /**
     * Given:   A valid trip id
     * When:    Trying to observe a trips status
     * Then:    The observer should be added to the observable
     */
    @Test
    fun `observer is added to observable`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.REQUESTED)))

        verify(observable).subscribe(any(), pollingTimeCaptor.capture())

        assertEquals(BASE_POLL_TIME, pollingTimeCaptor.firstValue)
    }

    /**
     * Given:   A trip is in progress
     * When:    The driver has arrived
     * Then:    The observer should be unsubscribed
     * And:     And resubscibed with the new polling time
     */
    @Test
    fun `trip update polling times are changed when driver has arrived`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.ARRIVED)))

        verify(observable, times(2)).subscribe(any(), pollingTimeCaptor.capture())

        assertEquals(BASE_POLL_TIME, pollingTimeCaptor.firstValue)
        assertEquals(IN_PROGRESS_TRIP_INFO_UPDATE_PERIOD, pollingTimeCaptor.secondValue)
    }

    /**
     * When:   A trip has ended
     * Then:    The polling time for trip details should changed
     */
    @Test
    fun `observer is unsubscribed when the trip has ended`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.COMPLETED)))

        verify(observable).unsubscribe(any())
    }

    /**
     * Given:   A trip has been initialised
     * When:    The status of the trip is checked
     * Then:    A call should not be made to update the booking status
     */
    @Test
    fun `when trip is initialised a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.REQUESTED)))

        verify(view, never()).updateStatus(R.string.kh_uisdk_initialised, FLEET_NAME)
    }

    /**
     * Given:   A trip has been requested
     * When:    The status of the trip is checked
     * Then:    A call should not be made to update the booking status
     */
    @Test
    fun `when trip is requested a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.REQUESTED)))

        verify(view, never()).updateStatus(R.string.kh_uisdk_requested, FLEET_NAME)
    }

    /**
     * Given:   A trip has been confirmed
     * When:    The status of the trip is checked
     * Then:    A call should not be made to update the booking status
     */
    @Test
    fun `when trip is confirmed a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.CONFIRMED)))

        verify(view, never()).updateStatus(R.string.kh_uisdk_confirmed, FLEET_NAME)
    }

    /**
     * Given:   A trip is driver en route
     * When:    The status of the trip is checked
     * Then:    A call should be made to update the booking status
     */
    @Test
    fun `when trip is driver enRoute a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.DRIVER_EN_ROUTE)))

        verify(view).updateStatus(R.string.kh_uisdk_driver_en_route, FLEET_NAME)
    }

    /**
     * Given:   A trip has arrived
     * When:    The status of the trip is checked
     * Then:    A call should be made to update the booking status
     */
    @Test
    fun `when trip has arrived a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.ARRIVED)))

        verify(view).updateStatus(R.string.kh_uisdk_arrived, FLEET_NAME)
    }

    /**
     * Given:   A trip has passenger on board
     * When:    The status of the trip is checked
     * Then:    A call should be made to update the booking status
     */
    @Test
    fun `when trip is passenger on board a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.PASSENGER_ON_BOARD)))

        verify(view).updateStatus(R.string.kh_uisdk_pass_on_board, FLEET_NAME)
    }

    /**
     * Given:   A trip has completed
     * When:    The status of the trip is checked
     * Then:    A call should be made to update the booking status
     */
    @Test
    fun `when trip is completed a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.COMPLETED)))

        verify(view, never()).updateStatus(R.string.kh_uisdk_completed, FLEET_NAME)
    }

    /**
     * Given:   A trip has been cancelled by user
     * When:    The status of the trip is checked
     * Then:    A call should not be made to update the booking status
     */
    @Test
    fun `when trip is canceled by user a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.CANCELLED_BY_USER)))

        verify(view, never()).updateStatus(R.string.kh_uisdk_cancelled_by_user, FLEET_NAME)
    }

    /**
     * Given:   A trip has been cancelled by dispatch
     * When:    The status of the trip is checked
     * Then:    A call should not be made to update the booking status
     */
    @Test
    fun `when trip is cancelled by dispatch a call is made to update the booking status`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.CANCELLED_BY_DISPATCH)))

        verify(view, never()).updateStatus(R.string.kh_uisdk_cancelled_by_dispatch, FLEET_NAME)
    }

    /**
     * Given:   A trip has passenger on board
     * When:    The status of the trip is checked
     * Then:    A call should be made to disable the cancel button
     */
    @Test
    fun `when trip is passenger on board a call is made to disable cancel button`() {
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.PASSENGER_ON_BOARD)))

        verify(view).setCancelEnabled(false)
    }

    /**
     * Given:   A trip has completed
     * When:    The status of the trip is checked
     * Then:    A call should be made to disable the cancel button AND view should be notified trip completed
     */
    @Test
    fun `when trip is completed a call is made to disable cancel button AND notify the view`() {
        presenter.monitorTrip(TRIP_ID)
        val tripInfo = tripDetailsWithState(TripStatus.COMPLETED)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripInfo))

        verify(view).setCancelEnabled(false)
        verify(view).tripComplete(tripDetails = tripInfo)
    }

    /**
     * Given:   A trip has been cancelled by user
     * When:    The status of the trip is checked
     * Then:    A call should be made to update the booking status AND view should be notified trip canceled
     */
    @Test
    fun `when trip is cancelled by user a call is made to disable cancel button AND notify the view`() {
        presenter.monitorTrip(TRIP_ID)
        val tripInfo = tripDetailsWithState(TripStatus.CANCELLED_BY_USER)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripInfo))

        verify(view).setCancelEnabled(false)
        verify(view).tripCanceled(tripDetails = tripInfo)
    }

    /**
     * Given:   A trip has been cancelled by dispatch
     * When:    The status of the trip is checked
     * Then:    A call should be made to update the booking status AND view should be notified trip canceled
     */
    @Test
    fun `when trip is cancelled by dispatch a call is made to disable cancel button AND notify the view`() {
        presenter.monitorTrip(TRIP_ID)
        val tripInfo = tripDetailsWithState(TripStatus.CANCELLED_BY_DISPATCH)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripInfo))

        verify(view).setCancelEnabled(false)
        verify(view).tripCanceled(tripDetails = tripInfo)
    }

    /**
     * Given:   trip status is ARRIVED (& all states before, !tested due to clunkiness of parameterized tests)
     * When:    update trip status
     * Then:    cancelling is enabled
     */
    @Test
    fun `cancel enabled when trip has arrived`() {
        presenter.updateBookingStatus(tripDetailsWithState(TripStatus.ARRIVED))
        verify(view).setCancelEnabled(true)
    }

    /**
     * Given:   trip status is PASSENGER ON BOARD (& all states thereafter, !tested due to clunkiness of parameterized tests)
     * When:    update trip status
     * Then:    cancelling is disabled
     */
    @Test
    fun `cancel disabled when trip is passenger on board`() {
        presenter.updateBookingStatus(tripDetailsWithState(TripStatus.PASSENGER_ON_BOARD))
        verify(view).setCancelEnabled(false)
    }

    /**
     * Given:   A trip details has not been set
     * When:    Handling a trip update
     * Then:    If the trip details is null nothing should happen
     */
    @Test
    fun `inital null trip details doesn't crash when handling trip`() {
        presenter.monitorTrip(TRIP_ID)
        presenter.monitorTrip(TRIP_ID)
        observerTripInfoCaptor.firstValue.onValueChanged(Resource.Success(tripDetailsWithState(TripStatus.CANCELLED_BY_DISPATCH)))

        verify(view).showCancellationDialog(any())
    }

    companion object {

        private const val TRIP_ID = "1234"
        private const val FLEET_NAME = "Some Fleet Name"

        private fun tripDetailsWithState(state: TripStatus): TripInfo {
            return TripInfo(
                    tripId = TRIP_ID,
                    tripState = state,
                    fleetInfo = FleetInfo(name = FLEET_NAME))
        }
    }

}
