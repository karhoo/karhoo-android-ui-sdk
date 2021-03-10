package com.karhoo.uisdk.screen.rides.upcoming.card

import com.karhoo.sdk.api.model.Driver
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpcomingRideCardPresenterTest {

    private val TRIP_DETAILS = TripInfo(
            tripState = TripStatus.DRIVER_EN_ROUTE,
            tripId = TRIP_ID,
            fleetInfo = FLEET_INFO,
            origin = ORIGIN,
            destination = DESTINATION,
            vehicle = Vehicle(driver = Driver(phoneNumber = DRIVER_NUMBER)))

    private var view: UpcomingRideCardMVP.View = mock()
    private var scheduledDateViewBinder: ScheduledDateViewBinder = mock()
    private var analytics: Analytics = mock()

    private lateinit var presenter: UpcomingRideCardPresenter

    /**
     * When:    the user selects track
     * Then:    track trip with a 'Trip' that's equal to the 'TripInfo'
     * And:     analytics call made
     */
    @Test
    fun tracksTripWithSameDataAsTripInfoWhenTrack() {
        presenter = UpcomingRideCardPresenter(view, TRIP_DETAILS, scheduledDateViewBinder, analytics)
        presenter.track()
        verify(analytics).trackRide()
        verify(view).trackTrip(TRIP_DETAILS)
    }

    /**
     * Given:   an upcoming trip
     * When:    user selects details
     * Then:    go to details view
     */
    @Test
    fun whenSelectDetailsThenGoToDetails() {
        presenter = UpcomingRideCardPresenter(view, TRIP_DETAILS, scheduledDateViewBinder, analytics)
        presenter.selectDetails()
        verify(view).goToDetails(TRIP_DETAILS)
    }

    /**
     * Given:   an upcoming trip CONFIRMED or before
     * When:    presenter initialised
     * Then:    hide track driver button
     */
    @Test
    fun hideTrackDriverWhenConfirmedState() {
        presenter = UpcomingRideCardPresenter(view, TripInfo(tripState = TripStatus.CONFIRMED), scheduledDateViewBinder, analytics)
        verify(view).hideTrackDriverButton()
    }

    /**
     * Given:   an upcoming trip ALLOCATED or after
     * When:    presenter initialised
     * Then:    display track driver button
     */
    @Test
    fun displayTrackDriverWhenDriverEnRouteState() {
        presenter = UpcomingRideCardPresenter(view, TripInfo(tripState = TripStatus.DRIVER_EN_ROUTE), scheduledDateViewBinder, analytics)
        verify(view).displayTrackDriverButton()
    }

    /**
     * Given:   An upcoming trip ALLOCATED or after
     * When:    There is driver information
     * Then:    The view should set the text to contact fleet
     */
    @Test
    fun `when the contact fleet button is clicked the fleet number is called`() {
        presenter = UpcomingRideCardPresenter(view,
                                              TRIP_DETAILS,
                                              scheduledDateViewBinder,
                                              analytics)
        presenter.call()

        verify(view).callFleet(FLEET_PHONE_NUMBER)
    }

    /**
     * Given:   a view and a trip
     * When:    binding date
     * Then:    view binder handles the binding
     */
    @Test
    fun `scheduled date view binding handled by ScheduledDateViewBinder`() {
        val trip = TripInfo(tripId = TRIP_ID)
        presenter = UpcomingRideCardPresenter(view, trip, scheduledDateViewBinder, analytics)

        presenter.bindDate()

        verify(scheduledDateViewBinder).bind(view, trip)
    }

    companion object {

        private const val TRIP_ID = "someTripId"
        private const val FLEET_ID = "someFleetId"
        private const val FLEET_PHONE_NUMBER = "01234 56789"
        private val ORIGIN = TripLocationInfo(
                placeId = "originId",
                position = Position(1.0, 2.0))
        private val DESTINATION = TripLocationInfo(
                placeId = "destinationId",
                position = Position(0.5, 1.5))
        private const val CURRENCY = "GBP"
        private const val PRICE = 1243
        private val FLEET_INFO = FleetInfo(
                fleetId = FLEET_ID,
                phoneNumber = FLEET_PHONE_NUMBER)
        private val DRIVER_NUMBER = "98834 123934"

    }

}
