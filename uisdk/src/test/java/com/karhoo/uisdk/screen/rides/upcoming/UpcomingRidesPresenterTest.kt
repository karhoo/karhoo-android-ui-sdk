package com.karhoo.uisdk.screen.rides.upcoming

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.request.TripSearch
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.TripsService
import com.karhoo.sdk.call.Call
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.Arrays
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class UpcomingRidesPresenterTest {

    private var view: UpcomingRidesMVP.View = mock()
    private var tripsService: TripsService = mock()
    private var tripDetailsCall: Call<List<TripInfo>> = mock()

    private lateinit var presenter: UpcomingRidesPresenter

    private val lambdaCaptor = argumentCaptor<(Resource<List<TripInfo>>) -> Unit>()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        doNothing().whenever(tripDetailsCall).execute(lambdaCaptor.capture())
        presenter = UpcomingRidesPresenter(view, tripsService)
    }

    /**
     * Given:   upcoming rides for a user
     * When:    Requesting upcoming rides
     * Then:    The view should display all upcoming rides sorted by trip scheduled time
     */
    @Test
    fun `valid upcoming rides displayed in correct order`() {
        whenever(tripsService.search(TRIP_HISTORY_REQUEST)).thenReturn(tripDetailsCall)

        presenter.getUpcomingRides()
        lambdaCaptor.firstValue.invoke(Resource.Success(RIDES_UPCOMING_UNSORTED))

        verify(view).showUpcomingRides(RIDES_UPCOMING_SORTED)
    }

    /**
     * Given:   No upcoming rides for a user
     * When:    Trying to track upcoming rides
     * Then:    The view should display empty state
     */
    @Test
    fun `empty state shown when no upcoming rides`() {
        whenever(tripsService.search(TRIP_HISTORY_REQUEST)).thenReturn(tripDetailsCall)

        presenter.getUpcomingRides()
        lambdaCaptor.firstValue.invoke(Resource.Success(RIDES_NONE_UPCOMING))

        verify(view).showEmptyState()
    }

    /**
     * Given:   The user requests upcoming rides
     * When:    onServiceError from the sdk callback
     * Then:    The view should show an error
     */
    @Test
    fun `displays error when error`() {
        whenever(tripsService.search(TRIP_HISTORY_REQUEST)).thenReturn(tripDetailsCall)

        presenter.getUpcomingRides()
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.Unexpected))

        verify(view).showError(anyInt())
    }

    companion object {

        private val RIDES_NONE_UPCOMING = Arrays.asList(
                TripInfo(tripState = TripStatus.CANCELLED_BY_USER),
                TripInfo(tripState = TripStatus.CANCELLED_BY_DISPATCH),
                TripInfo(tripState = TripStatus.COMPLETED)
                                                       )

        private val RIDES_UPCOMING_UNSORTED = Arrays.asList(
                TripInfo(
                        tripId = "2",
                        tripState = TripStatus.REQUESTED,
                        dateScheduled = Date(50L)),
                TripInfo(
                        tripId = "3",
                        tripState = TripStatus.CONFIRMED,
                        dateScheduled = Date(20L)),
                TripInfo(
                        tripId = "5",
                        tripState = TripStatus.DRIVER_EN_ROUTE,
                        dateScheduled = Date(90L)),
                TripInfo(
                        tripId = "7",
                        tripState = TripStatus.ARRIVED,
                        dateScheduled = Date(110L)),
                TripInfo(
                        tripId = "8",
                        tripState = TripStatus.PASSENGER_ON_BOARD,
                        dateScheduled = Date(10L))
                                                           )

        private val RIDES_UPCOMING_SORTED = Arrays.asList(
                TripInfo(
                        tripId = "8",
                        tripState = TripStatus.PASSENGER_ON_BOARD,
                        dateScheduled = Date(10L)),
                TripInfo(
                        tripId = "3",
                        tripState = TripStatus.CONFIRMED,
                        dateScheduled = Date(20L)),
                TripInfo(
                        tripId = "2",
                        tripState = TripStatus.REQUESTED,
                        dateScheduled = Date(50L)),
                TripInfo(
                        tripId = "5",
                        tripState = TripStatus.DRIVER_EN_ROUTE,
                        dateScheduled = Date(90L)),
                TripInfo(
                        tripId = "7",
                        tripState = TripStatus.ARRIVED,
                        dateScheduled = Date(110L))
                                                         )

        private val VALID_UPCOMING_STATES = Arrays.asList(
                TripStatus.REQUESTED,
                TripStatus.CONFIRMED,
                TripStatus.DRIVER_EN_ROUTE,
                TripStatus.ARRIVED,
                TripStatus.PASSENGER_ON_BOARD)

        private const val TRIP_TYPE = "BOTH"

        private val TRIP_HISTORY_REQUEST = TripSearch(
                tripState = VALID_UPCOMING_STATES,
                tripType = TRIP_TYPE)
    }

}
