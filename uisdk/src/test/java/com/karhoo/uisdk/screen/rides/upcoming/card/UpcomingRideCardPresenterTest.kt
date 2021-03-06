package com.karhoo.uisdk.screen.rides.upcoming.card

import android.content.Context
import android.content.res.Resources
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.ServiceAgreements
import com.karhoo.sdk.api.model.Driver
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.screen.booking.quotes.BookingQuotesPresenterTest
import com.karhoo.uisdk.util.CANCELLATION_BEFORE_DRIVER_EN_ROUTE
import com.karhoo.uisdk.util.CANCELLATION_TIME_BEFORE_PICKUP
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.ServiceCancellationExtTests
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UpcomingRideCardPresenterTest {
    private var view: UpcomingRideCardMVP.View = mock()
    private var scheduledDateViewBinder: ScheduledDateViewBinder = mock()
    private var analytics: Analytics = mock()
    private var context: Context = mock()
    private lateinit var presenter: UpcomingRideCardPresenter
    private var resources: Resources = mock()

    /**
     * When:    the user selects track
     * Then:    track trip with a 'Trip' that's equal to the 'TripInfo'
     * And:     analytics call made
     */
    @Before
    fun setup() {
        whenever(context.resources).thenReturn(resources)
        whenever(context.resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, TEST_TWO_MINUTES, TEST_TWO_MINUTES)).thenReturn(String.format(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTES, TEST_TWO_MINUTES))
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)).thenReturn(TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_start)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_START)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_ending)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_END)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)).thenReturn(TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_after_booking_ending)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_AFTER_BOOKING_END)
    }

    @Test
    fun tracksTripWithSameDataAsTripInfoWhenTrack() {
        presenter = UpcomingRideCardPresenter(view, TRIP_DETAILS, scheduledDateViewBinder, analytics, context)
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
        presenter = UpcomingRideCardPresenter(view, TRIP_DETAILS, scheduledDateViewBinder, analytics, context)
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
        presenter = UpcomingRideCardPresenter(view, TripInfo(tripState = TripStatus.CONFIRMED), scheduledDateViewBinder, analytics, context)
        verify(view).hideTrackDriverButton()
    }

    /**
     * Given:   an upcoming trip ALLOCATED or after
     * When:    presenter initialised
     * Then:    display track driver button
     */
    @Test
    fun displayTrackDriverWhenDriverEnRouteState() {
        presenter = UpcomingRideCardPresenter(view, TripInfo(tripState = TripStatus.DRIVER_EN_ROUTE), scheduledDateViewBinder, analytics, context)
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
                analytics,
                context)
        presenter.call()

        verify(view).callFleet(FLEET_PHONE_NUMBER)
    }

    /**
     * Given:   An upcoming asap trip
     * When:    There is a free cancellation SLA before pickup
     * Then:    The view should show correctly the cancellation SLA textview
     * Then:    The view should set the correct text in the cancellation SLA textview
     */
    @Test
    fun `When we have a free cancellation SLA with before pickup, the asap cancellation text is visible`() {
        presenter = UpcomingRideCardPresenter(view,
                TRIP_DETAILS_SLA_BEFORE_PICKUP_ASAP,
                scheduledDateViewBinder,
                analytics,
                context)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(String.format(BookingQuotesPresenterTest.TEST_CANCELLATION_TEXT_ASAP, TEST_TWO_MINUTES))
    }

    /**
     * Given:   An upcoming prebook trip
     * When:    There is a free cancellation SLA before pickup
     * Then:    The view should show correctly the cancellation SLA textview
     * Then:    The view should set the correct text in the cancellation SLA textview
     */
    @Test
    fun `When we have a free cancellation SLA with before pickup, the prebook cancellation text is visible`() {
        presenter = UpcomingRideCardPresenter(view,
                TRIP_DETAILS_SLA_BEFORE_PICKUP_PREBOOK,
                scheduledDateViewBinder,
                analytics,
                context)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(String.format(BookingQuotesPresenterTest.TEST_CANCELLATION_TEXT_PREBOOK, TEST_TWO_MINUTES))
    }

    /**
     * Given:   An upcoming trip
     * When:    There is a free cancellation SLA before driver en route
     * Then:    The view should show correctly the cancellation SLA textview
     */
    @Test
    fun `When we have a free cancellation SLA with before driver en route duration, the cancellation text is shown`() {
        presenter = UpcomingRideCardPresenter(view,
                TRIP_DETAILS_SLA_BEFORE_DRIVER_EN_ROUTE,
                scheduledDateViewBinder,
                analytics,
                context)

        verify(view).showCancellationText(true)
        verify(view).setCancellationText(TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
    }

    /**
     * Given:   An upcoming trip
     * When:    There is a free cancellation SLA with 0 minutes
     * Then:    The view should not show the cancellation SLA textview
     */
    @Test
    fun `When we have tripe with free cancellation but with 0 minutes, the cancellation text is not shown`() {
        presenter = UpcomingRideCardPresenter(view,
                TRIP_DETAILS_SLA_ZERO_MINUTES,
                scheduledDateViewBinder,
                analytics,
                context)

        verify(view).showCancellationText(false)
    }

    /**
     * Given:   An upcoming trip
     * When:    There is no cancellation SLA
     * Then:    The view should not show the cancellation SLA textview
     */
    @Test
    fun `When we have tripe without free cancellation, the cancellation text is not shown`() {
        presenter = UpcomingRideCardPresenter(view,
                TRIP_DETAILS_WITHOUT_SLA,
                scheduledDateViewBinder,
                analytics,
                context)

        verify(view).showCancellationText(false)
    }

    /**
     * Given:   a view and a trip
     * When:    binding date
     * Then:    view binder handles the binding
     */
    @Test
    fun `scheduled date view binding handled by ScheduledDateViewBinder`() {
        val trip = TripInfo(tripId = TRIP_ID)
        presenter = UpcomingRideCardPresenter(view, trip, scheduledDateViewBinder, analytics, context)

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
        const val TEST_TWO_MINUTES = 2
        private val TRIP_DETAILS = TripInfo(
                tripState = TripStatus.DRIVER_EN_ROUTE,
                tripId = TRIP_ID,
                fleetInfo = FLEET_INFO,
                origin = ORIGIN,
                destination = DESTINATION,
                vehicle = Vehicle(driver = Driver(phoneNumber = DRIVER_NUMBER)))
        val CANCELLATION_AGREEMENT_BEFORE_DRIVER_EN_ROUTE = ServiceAgreements(ServiceCancellation(CANCELLATION_BEFORE_DRIVER_EN_ROUTE, TEST_TWO_MINUTES))
        val CANCELLATION_AGREEMENT_ZERO_MINUTES = ServiceAgreements(ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, 0))
        val CANCELLATION_AGREEMENT_BEFORE_PICKUP = ServiceAgreements(ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_TWO_MINUTES))
        private val TRIP_DETAILS_SLA_BEFORE_DRIVER_EN_ROUTE = TRIP_DETAILS.copy(
                tripState = TripStatus.CONFIRMED,
                serviceAgreements = CANCELLATION_AGREEMENT_BEFORE_DRIVER_EN_ROUTE
        )
        private val TRIP_DETAILS_SLA_BEFORE_PICKUP_ASAP = TRIP_DETAILS.copy(
                tripState = TripStatus.CONFIRMED,
                serviceAgreements = CANCELLATION_AGREEMENT_BEFORE_PICKUP
        )
        private val TRIP_DETAILS_SLA_BEFORE_PICKUP_PREBOOK = TRIP_DETAILS.copy(
                tripState = TripStatus.CONFIRMED,
                dateBooked = DateUtil.parseDateString("2021-06-18T09:39:24Z"),
                dateScheduled = DateUtil.parseDateString("2021-06-20T09:39:24Z"),
                serviceAgreements = CANCELLATION_AGREEMENT_BEFORE_PICKUP
        )
        private val TRIP_DETAILS_SLA_ZERO_MINUTES = TRIP_DETAILS.copy(
                tripState = TripStatus.CONFIRMED,
                serviceAgreements = CANCELLATION_AGREEMENT_ZERO_MINUTES
        )
        private val TRIP_DETAILS_WITHOUT_SLA = TRIP_DETAILS.copy(
                tripState = TripStatus.CONFIRMED
        )
        const val TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT = "Free cancellation until driver is en route"
    }

}
