package com.karhoo.uisdk.screen.booking.comment

import com.karhoo.uisdk.screen.booking.booking.comment.BookingOptionalInfoMVP
import com.karhoo.uisdk.screen.booking.booking.comment.BookingOptionalInfoPresenter
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BookingOptionalInfoPresenterTest {

    private var view: BookingOptionalInfoMVP.View = mock()

    private lateinit var presenter: BookingOptionalInfoPresenter

    @Before
    fun setUp() {
        presenter = BookingOptionalInfoPresenter(view)
    }

    /**
     * Given:   A user views the booking comments
     * When:    The edit mode is updated
     * Then:    The view is correctly updated
     */
    @Test
    fun `view is correctly set when edit mode is updated`() {
        presenter.isEditingMode = false

        verify(view).bindEditMode(false)
    }

    /**
     * Given:   A user views the booking comments
     * When:    The details are prefilled
     * Then:    The view is correctly updated
     * And:     The correct details can be retrieved
     */
    @Test
    fun `view correctly updated with prefilled booking comments`() {
        presenter.prefillForBookingOptionalInfo(COMMENT)

        verify(view).bindBookingOptionalInfo(COMMENT)
        verify(view).bindEditMode(true)
        assertEquals(COMMENT, presenter.comments)
    }

    /**
     * Given:   A user views the booking comments
     * When:    There are new details
     * Then:    The details are correctly updated
     */
    @Test
    fun `booking comment can be correctly updated`() {
        val comment = "Another comment"

        presenter.prefillForBookingOptionalInfo(COMMENT)
        presenter.updateOptionalInfo(comment)

        assertEquals(comment, presenter.comments)
    }

    companion object {
        private const val COMMENT = "Some comment"
    }
}