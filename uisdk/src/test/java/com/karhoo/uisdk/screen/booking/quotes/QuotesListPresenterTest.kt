package com.karhoo.uisdk.screen.booking.quotes

import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityHandler
import com.karhoo.uisdk.screen.booking.quotes.mocks.QuotesListViewMock
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QuotesListPresenterTest {
    private val view: QuotesListViewMock = QuotesListViewMock()
    private val presenter: QuotesListMVP.Presenter = QuotesListPresenter(view, null)

    @Before
    fun setup() {
        view.calledShowNowResults = false
    }

    @Test
    fun `When setting the hasNoResults to true, the view is called with the correct method and param`() {
        (presenter as AvailabilityHandler).hasNoResults = true
        Assert.assertTrue(view.calledShowNowResults)
    }

    @Test
    fun `When setting the hasNoResults to false, the view is called with the correct method and param`() {
        view.calledShowNowResults = true
        (presenter as AvailabilityHandler).hasNoResults = false
        Assert.assertFalse(view.calledShowNowResults)
    }
}