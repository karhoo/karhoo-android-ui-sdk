package com.karhoo.uisdk.screen.rides.past.card

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Fare
import com.karhoo.sdk.api.model.FareBreakdown
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.fare.FareService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class PastRideCardPresenterTest {

    private var view: PastRideCardMVP.View = mock()
    private var scheduledDateViewBinder: ScheduledDateViewBinder = mock()
    private var fareService: FareService = mock()
    private var fareCall: Call<Fare> = mock()
    private var fareCaptor = argumentCaptor<(Resource<Fare>) -> Unit>()

    private lateinit var presenter: PastRideCardPresenter

    @Before
    fun setUp() {
        Locale.setDefault(Locale.UK)
        presenter = PastRideCardPresenter(view, scheduledDateViewBinder, TRIP, fareService)
    }

    /**
     * Given:   a cancelled trip
     * When:    state is bound
     * Then:    display cancelled icon with 'cancelled' text
     */
    @Test
    fun `when state is bound then display cancelled icon with cancelled text`() {
        val cancelledTrip = TripInfo(
                tripState = TripStatus.CANCELLED_BY_USER)

        presenter = PastRideCardPresenter(view, scheduledDateViewBinder, cancelledTrip)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_cancelled, R.string.kh_uisdk_ride_state_cancelled, R.color.kh_uisdk_text)
    }

    /**
     * Given:   a completed trip
     * When:    state is bound
     * Then:    display completed icon with 'completed' text
     */
    @Test
    fun `when state is bound then display completed icon with completed text`() {
        val completedTrip = TripInfo(
                tripState = TripStatus.COMPLETED)

        presenter = PastRideCardPresenter(view, scheduledDateViewBinder, completedTrip)

        presenter.bindState()

        verify(view).displayState(R.drawable.uisdk_ic_trip_completed, R.string.kh_uisdk_ride_state_completed, R.color.kh_uisdk_text)
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

        presenter = PastRideCardPresenter(view, scheduledDateViewBinder, TRIP_WITH_NULL_FARE, fareService)

        presenter.bindPrice()
        fareCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify<PastRideCardMVP.View>(view).displayPricePending()
    }

    /**
     * Given:   an empty currency code value
     * When:    highPrice is bound
     * Then:    show pending highPrice
     */
    @Test
    fun `when currency symbol is empty then display price pending`() {
        whenever(fareService.fareDetails(anyString())).thenReturn(fareCall)
        doNothing().whenever(fareCall).execute(fareCaptor.capture())

        presenter = PastRideCardPresenter(view, scheduledDateViewBinder, TRIP_WITH_NULL_FARE, fareService)

        presenter.bindPrice()
        fareCaptor.firstValue.invoke(Resource.Success(FARE_NO_CURRENCY))

        verify(view).displayPricePending()
    }

    /**
     * Given:   a fare is available
     * When:    highPrice is bound
     * Then:    show pending highPrice
     */
    @Test
    fun `when fare is available then display price`() {
        whenever(fareService.fareDetails(anyString())).thenReturn(fareCall)
        doNothing().whenever(fareCall).execute(fareCaptor.capture())

        presenter = PastRideCardPresenter(view, scheduledDateViewBinder, TRIP, fareService)

        presenter.bindPrice()
        fareCaptor.firstValue.invoke(Resource.Success(FARE_COMPLETE))

        verify(view).displayPrice("£49.99")
    }

    /**
     * Given:   a past trip
     * When:    user selects details
     * Then:    go to details view
     */
    @Test
    fun `when select details then go to details`() {
        presenter.selectDetails()
        verify(view).goToDetails(TRIP)
    }

    /**
     * Given:   a view and a trip
     * When:    binding date
     * Then:    view binder handles the binding
     */
    @Test
    fun `scheduled date view binding handled by ScheduledDateViewBinder`() {
        presenter = PastRideCardPresenter(view, scheduledDateViewBinder, TRIP)

        presenter.bindDate()

        verify(scheduledDateViewBinder).bind(view, TRIP)
    }

    companion object {

        private val TRIP = TripInfo(
                tripId = "trip_id",
                tripState = TripStatus.COMPLETED)

        private val TRIP_WITH_NULL_FARE = TripInfo(
                tripId = "12345",
                tripState = TripStatus.COMPLETED)

        private val BREAKDOWN_NO_CURRENCY = FareBreakdown(
                total = 4999,
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
