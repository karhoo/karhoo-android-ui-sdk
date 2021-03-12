package com.karhoo.uisdk.screen.booking.quotes

import android.content.Context
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesMVP
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesPresenter
import com.karhoo.uisdk.screen.booking.quotes.mocks.BookingQuotesViewMock
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

    @Before
    fun setup() {
        view.cancellationMinutesText = null
        view.capacityPeople = null
        view.capacityLuggage = null
        view.catgText = null
        view.showCancellation = null

        whenever(testContext.getString(R.string.uisdk_quote_cancellation_minutes)).thenReturn(TEST_CANCELLATION_TEXT)
    }

    @Test
    fun `When receiving a vehicle category, it's name is capitalized`() {
        presenter.capitalizeCategory(TEST_STRING)

        assertEquals(view.catgText, TEST_STRING[0].toUpperCase() + TEST_STRING.substring(1))
    }

    @Test
    fun `When checking the cancellation SLA minutes, if a null value is received, then the cancellation text is not visible`() {
        presenter.checkCancellationSLAMinutes(null, testContext)

        assertFalse(view.showCancellation!!)
    }

    @Test
    fun `When checking the cancellation SLA minutes, if zero minutes are received, then the cancellation text is not visible`() {
        presenter.checkCancellationSLAMinutes(TEST_ZERO_MINUTES, testContext)

        assertFalse(view.showCancellation!!)
    }

    @Test
    fun `When checking the cancellation SLA minutes, if a number of minutes are received, then the cancellation text is visible`() {
        presenter.checkCancellationSLAMinutes(TEST_TEN_MINUTES, testContext)

        assertTrue(view.showCancellation!!)
    }

    @Test
    fun `When checking the cancellation SLA minutes, if a number of minutes are received, then the cancellation text has the correct value`() {
        presenter.checkCancellationSLAMinutes(TEST_TEN_MINUTES, testContext)

        assertEquals(view.cancellationMinutesText!!, String.format(TEST_CANCELLATION_TEXT, TEST_TEN_MINUTES))
    }

    companion object {
        private const val TEST_STRING = "test"
        private const val TEST_ZERO_MINUTES = 0
        private const val TEST_TEN_MINUTES = 10
        private const val TEST_CANCELLATION_TEXT = "Free cancellation up to %d mins before pickup"
    }
}