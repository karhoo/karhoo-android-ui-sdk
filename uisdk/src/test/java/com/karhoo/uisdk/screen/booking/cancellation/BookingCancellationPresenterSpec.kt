package com.karhoo.uisdk.screen.booking.cancellation

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.BookingFee
import com.karhoo.sdk.api.model.BookingFeePrice
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.trips.KarhooTripsService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.screen.booking.booking.cancellation.BookingCancellationMVP
import com.karhoo.uisdk.screen.booking.booking.cancellation.BookingCancellationPresenter
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BookingCancellationPresenterSpec {
    private val tripId = "1234"
    private val bookingFee = BookingFeePrice(currency = "GBP", value = 5200)
    private var tripService: KarhooTripsService = mock()
    private var view: BookingCancellationMVP.View = mock()
    private val bookingFeeCall: Call<BookingFee> = mock()
    private val bookingFeeCaptor = argumentCaptor<(Resource<BookingFee>) -> Unit>()
    private val cancellationCall: Call<Void> = mock()
    private val cancellationCaptor = argumentCaptor<(Resource<Void>) -> Unit>()

    private lateinit var presenter: BookingCancellationPresenter

    @Before
    fun setUp() {
        whenever(tripService.cancellationFee(any())).thenReturn(bookingFeeCall)
        doNothing().whenever(bookingFeeCall).execute(bookingFeeCaptor.capture())

        whenever(tripService.cancel(any())).thenReturn(cancellationCall)
        doNothing().whenever(cancellationCall).execute(cancellationCaptor.capture())

        presenter = BookingCancellationPresenter(view = view, tripsService = tripService)
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    The call fails
     * Then:    Then view is updated with the error
     */
    @Test
    fun `a cancellation fee request failure correctly updates the view`() {
        presenter.getCancellationFee(tripId)

        bookingFeeCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).showCancellationFeeError()
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    There is a null cancellation fee
     * Then:    Then the information is returned to the user
     */
    @Test
    fun `a null cancellation fee response correctly updates the view`() {
        presenter.getCancellationFee(tripId)

        bookingFeeCaptor.firstValue.invoke(Resource.Success(BookingFee(fee = null)))

        verify(view).showCancellationFee("")
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    There there is no cancellation fee
     * Then:    Then the information is returned to the user
     */
    @Test
    fun `a no cancellation fee response correctly updates the view`() {
        presenter.getCancellationFee(tripId)

        bookingFeeCaptor.firstValue.invoke(Resource.Success(BookingFee()))

        verify(view).showCancellationFee("")
    }

    /**
     * Given:   The cancellation fee is requested
     * When:    There is a valid cancellation fee
     * Then:    Then the information is returned to the user
     */
    @Test
    fun `a valid cancellation fee response correctly updates the view`() {
        presenter.getCancellationFee(tripId)

        bookingFeeCaptor.firstValue.invoke(Resource.Success(BookingFee(fee = bookingFee)))

        verify(view).showCancellationFee("£52.00")
    }

    /**
     * Given:   A cancellation is requested
     * When:    The call fails
     * Then:    Then view is updated with the error
     */
    @Test
    fun `a cancellation request failure correctly updates the view`() {
        presenter.handleCancellationRequest(tripId)

        cancellationCaptor.firstValue.invoke(Resource.Failure(KarhooError.GeneralRequestError))

        verify(view).showCancellationError()
    }

    /**
     * Given:   A cancellation is requested
     * When:    The cancellation succeeds
     * Then:    Then the information is returned to the user
     */
    @Test
    fun `a successful cancellation fee response correctly updates the view`() {
        presenter.handleCancellationRequest(tripId)

        cancellationCaptor.firstValue.invoke(Resource.Success(data = mock()))

        verify(view).showCancellationSuccess()
    }
}