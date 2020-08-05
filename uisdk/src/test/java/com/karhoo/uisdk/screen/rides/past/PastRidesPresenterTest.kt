package com.karhoo.uisdk.screen.rides.past

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
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.junit.MockitoJUnitRunner
import java.util.Arrays
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class PastRidesPresenterTest {

    private var view: PastRidesMVP.View = mock()
    private var tripsService: TripsService = mock()
    private var tripDetailsCall: Call<List<TripInfo>> = mock()

    private lateinit var presenter: PastRidesPresenter

    private val lambdaCaptor = argumentCaptor<(Resource<List<TripInfo>>) -> Unit>()

    @Before
    fun setUp() {
        doNothing().whenever(tripDetailsCall).execute(lambdaCaptor.capture())
        presenter = PastRidesPresenter(view, tripsService)
    }

    /**
     * Given:   past rides for a user
     * When:    Requesting past rides
     * Then:    The view should display all past rides sorted by date scheduled
     */
    @Test
    fun `valid past rides displayed sorted by date scheduled`() {
        whenever(tripsService.search(TRIP_HISTORY_REQUEST)).thenReturn(tripDetailsCall)

        presenter.getPastRides()
        lambdaCaptor.firstValue.invoke(Resource.Success(RIDES_PAST))

        verify(view).showPastRides(RIDES_PAST_DATE_SORTED)
    }

    /**
     * Given:   No past rides for a user
     * When:    Requesting past rides
     * Then:    The view should display empty state
     */
    @Test
    fun `empty state shown when no past rides`() {
        whenever(tripsService.search(TRIP_HISTORY_REQUEST)).thenReturn(tripDetailsCall)

        presenter.getPastRides()
        lambdaCaptor.firstValue.invoke(Resource.Success(RIDES_NONE_PAST))

        verify(view).showEmptyState()
    }

    /**
     * Given:   The user requests past rides
     * When:    onServiceError from the sdk callback
     * Then:    The view should show an error\
     */
    @Test
    fun `displays error when error`() {
        whenever(tripsService.search(TRIP_HISTORY_REQUEST)).thenReturn(tripDetailsCall)

        presenter.getPastRides()
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.Unexpected))

        verify(view).showError(anyInt())
    }

    companion object {

        private val RIDES_PAST = Arrays.asList(
                TripInfo(tripState = TripStatus.CANCELLED_BY_USER, dateScheduled = Date(5)),
                TripInfo(tripState = TripStatus.CANCELLED_BY_DISPATCH, dateScheduled = Date(6)),
                TripInfo(tripState = TripStatus.NO_DRIVERS, dateScheduled = Date(4)),
                TripInfo(tripState = TripStatus.COMPLETED, dateScheduled = Date(1)),
                TripInfo(tripState = TripStatus.CANCELLED_BY_KARHOO, dateScheduled = Date(7)),
                TripInfo(tripState = TripStatus.INCOMPLETE, dateScheduled = Date(8))
                                              )

        private val RIDES_PAST_DATE_SORTED = Arrays.asList(
                TripInfo(tripState = TripStatus.INCOMPLETE, dateScheduled = Date(8)),
                TripInfo(tripState = TripStatus.CANCELLED_BY_KARHOO, dateScheduled = Date(7)),
                TripInfo(tripState = TripStatus.CANCELLED_BY_DISPATCH, dateScheduled = Date(6)),
                TripInfo(tripState = TripStatus.CANCELLED_BY_USER, dateScheduled = Date(5)),
                TripInfo(tripState = TripStatus.NO_DRIVERS, dateScheduled = Date(4)),
                TripInfo(tripState = TripStatus.COMPLETED, dateScheduled = Date(1))
                                                          )

        private val RIDES_NONE_PAST = Arrays.asList(
                TripInfo(tripState = TripStatus.REQUESTED),
                TripInfo(tripState = TripStatus.CONFIRMED),
                TripInfo(tripState = TripStatus.DRIVER_EN_ROUTE),
                TripInfo(tripState = TripStatus.ARRIVED),
                TripInfo(tripState = TripStatus.PASSENGER_ON_BOARD)
                                                   )

        private const val TRIP_TYPE = "BOTH"

        private val TRIP_HISTORY_REQUEST = TripSearch(
                tripState = VALID_PAST_STATES.toList(),
                tripType = TRIP_TYPE)
    }

}
