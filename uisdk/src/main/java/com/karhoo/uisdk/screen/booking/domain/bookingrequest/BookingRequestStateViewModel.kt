package com.karhoo.uisdk.screen.booking.domain.bookingrequest

import android.app.Application
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.state.BaseStateViewModel
import com.karhoo.uisdk.screen.booking.booking.bookingrequest.BookingRequestViewContract

class BookingRequestStateViewModel(application: Application) : BaseStateViewModel<BookingRequestStatus,
        BookingRequestViewContract.BookingRequestAction, BookingRequestViewContract.BookingRequestEvent>
                                                               (application) {
    init {
        viewState = BookingRequestStatus(null)
    }

    // update the state by using a set of predefined contracts. Some of the event can trigger an
    // action to be performed (e.g. output of the widget)
    override fun process(viewEvent: BookingRequestViewContract.BookingRequestEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is BookingRequestViewContract.BookingRequestEvent.TermsAndConditionsRequested ->
                showTermsAndConditions(viewEvent.url)
            is BookingRequestViewContract.BookingRequestEvent.BookingSuccess ->
                updateBookingRequestStatus(viewEvent.tripInfo)
            is BookingRequestViewContract.BookingRequestEvent.BookingError ->
                handleBookingError(viewEvent.stringId, viewEvent.karhooError)
        }
    }

    private fun handleBookingError(@StringRes stringId: Int, karhooError: KarhooError?) {
        viewAction = BookingRequestViewContract.BookingRequestAction.HandleBookingError(stringId,
                                                                                        karhooError)
    }

    private fun showTermsAndConditions(url: String) {
        viewAction = BookingRequestViewContract.BookingRequestAction.ShowTermsAndConditions(url)
    }

    private fun updateBookingRequestStatus(tripInfo: TripInfo) {
        viewAction = BookingRequestViewContract.BookingRequestAction.WaitForTripAllocation
        viewState = BookingRequestStatus(tripInfo)
    }
}
