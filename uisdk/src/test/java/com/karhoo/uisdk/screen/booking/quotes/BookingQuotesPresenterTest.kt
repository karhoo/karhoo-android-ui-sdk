package com.karhoo.uisdk.screen.booking.quotes

import android.content.Context
import android.content.res.Resources
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesMVP
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesPresenter
import com.karhoo.uisdk.screen.booking.quotes.mocks.BookingQuotesViewMock
import com.karhoo.uisdk.screen.rides.upcoming.card.UpcomingRideCardPresenterTest
import com.karhoo.uisdk.screen.rides.upcoming.card.UpcomingRideCardPresenterTest.Companion.TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT
import com.karhoo.uisdk.util.CANCELLATION_TIME_BEFORE_PICKUP
import com.karhoo.uisdk.util.ServiceCancellationExtTests
import com.karhoo.uisdk.util.ServiceCancellationExtTests.Companion.TEST_TWO
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BookingQuotesPresenterTest {
    private val view: BookingQuotesViewMock = BookingQuotesViewMock()
    private val presenter: BookingQuotesMVP.Presenter = BookingQuotesPresenter(view)
    private val testContext: Context = mock()
    private var resources: Resources = mock()
    private var isPrebook: Boolean = TripInfo().dateScheduled != null

    @Before
    fun setup() {
        view.cancellationMinutesText = null
        view.capacityPeople = null
        view.capacityLuggage = null
        view.catgText = null
        view.showCancellation = null

        whenever(testContext.resources).thenReturn(resources)
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, TEST_TWO, TEST_TWO)).thenReturn(String.format(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTES, TEST_TWO))
        whenever(testContext.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)).thenReturn(TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
        whenever(testContext.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_start)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_START)
        whenever(testContext.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_ending)).thenReturn(ServiceCancellationExtTests.TEST_CANCELLATION_TEXT_BEFORE_PICKUP_END)
    }

    @Test
    fun `When receiving a vehicle category, it's name is capitalized`() {
        presenter.capitalizeCategory(TEST_STRING)

        assertEquals(view.catgText, TEST_STRING[0].toUpperCase() + TEST_STRING.substring(1))
    }

    @Test
    fun `When checking the cancellation SLA minutes, if a null value is received, then the cancellation text is not visible`() {
        presenter.checkCancellationSLAMinutes(testContext, null, isPrebook)

        assertFalse(view.showCancellation!!)
    }

    @Test
    fun `When checking the cancellation SLA minutes for a before pickup agreement, if zero minutes are received, then the cancellation text is not visible`() {
        presenter.checkCancellationSLAMinutes(testContext, UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_ZERO_MINUTES.freeCancellation, isPrebook)

        assertFalse(view.showCancellation!!)
    }

    @Test
    fun `When checking the cancellation SLA minutes for a before pickup agreement, if a number of minutes are received, then the cancellation text is visible`() {
        presenter.checkCancellationSLAMinutes(testContext, UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_PICKUP.freeCancellation, isPrebook)

        assertTrue(view.showCancellation!!)
    }

    @Test
    fun `When checking the cancellation SLA minutes for a before pickup agreement, if a number of minutes are received, then the cancellation text has the correct value`() {
        presenter.checkCancellationSLAMinutes(testContext, UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_PICKUP.freeCancellation, isPrebook)

        assertEquals(view.cancellationMinutesText!!, TEST_CANCELLATION_TEXT)
    }

    @Test
    fun `When checking the cancellation SLA minutes for a before driver en route agreement, then the cancellation text is visible`() {
        presenter.checkCancellationSLAMinutes(testContext, UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_DRIVER_EN_ROUTE.freeCancellation, isPrebook)

        assertTrue(view.showCancellation!!)
    }

    @Test
    fun `When checking the cancellation SLA minutes for a before driver en route agreement, then the cancellation text has the correct value`() {
        presenter.checkCancellationSLAMinutes(testContext, UpcomingRideCardPresenterTest.CANCELLATION_AGREEMENT_BEFORE_DRIVER_EN_ROUTE.freeCancellation, isPrebook)

        assertEquals(view.cancellationMinutesText!!,TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
    }

    companion object {
        private const val TEST_STRING = "test"
        const val TEST_CANCELLATION_TEXT = "Free cancellation up to %d minutes before pickup"
        const val TEST_CANCELLATION_TEXT_ASAP = "Free cancellation up to %d minutes after booking"
    }
}