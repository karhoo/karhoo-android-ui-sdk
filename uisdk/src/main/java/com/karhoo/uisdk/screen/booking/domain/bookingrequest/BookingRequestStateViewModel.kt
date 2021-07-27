package com.karhoo.uisdk.screen.booking.domain.bookingrequest

import android.app.Application
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.state.BaseStateViewModel
import com.karhoo.uisdk.screen.booking.booking.bookingcheckout.views.BookingCheckoutViewContract

class BookingRequestStateViewModel(application: Application) : BaseStateViewModel<BookingRequestStatus,
        BookingCheckoutViewContract.BookingRequestAction, BookingCheckoutViewContract.BookingRequestEvent>
                                                               (application) {
    init {
        viewState = BookingRequestStatus(null)
    }

    // update the state by using a set of predefined contracts. Some of the event can trigger an
    // action to be performed (e.g. output of the widget)
    override fun process(viewEvent: BookingCheckoutViewContract.BookingRequestEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is BookingCheckoutViewContract.BookingRequestEvent.TermsAndConditionsRequested ->
                showTermsAndConditions(viewEvent.url)
            is BookingCheckoutViewContract.BookingRequestEvent.BookingSuccess ->
                updateBookingRequestStatus(viewEvent.tripInfo)
            is BookingCheckoutViewContract.BookingRequestEvent.BookingError ->
                handleBookingError(viewEvent.stringId, viewEvent.karhooError)
        }
    }

    private fun handleBookingError(@StringRes stringId: Int, karhooError: KarhooError?) {
        viewAction = BookingCheckoutViewContract.BookingRequestAction.HandleBookingError(stringId,
                                                                                        karhooError)
    }

    private fun showTermsAndConditions(url: String) {
        viewAction = BookingCheckoutViewContract.BookingRequestAction.ShowTermsAndConditions(url)
    }

    private fun updateBookingRequestStatus(tripInfo: TripInfo) {
        viewAction = BookingCheckoutViewContract.BookingRequestAction.WaitForTripAllocation
        viewState = BookingRequestStatus(tripInfo)
    }
}
