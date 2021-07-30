package com.karhoo.uisdk.screen.booking.domain.bookingrequest

import android.app.Application
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.state.BaseStateViewModel
import com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views.CheckoutViewContract

class BookingRequestStateViewModel(application: Application) : BaseStateViewModel<BookingRequestStatus,
        CheckoutViewContract.Action, CheckoutViewContract.Event>
                                                               (application) {
    init {
        viewState = BookingRequestStatus(null)
    }

    // update the state by using a set of predefined contracts. Some of the event can trigger an
    // action to be performed (e.g. output of the widget)
    override fun process(viewEvent: CheckoutViewContract.Event) {
        super.process(viewEvent)
        when (viewEvent) {
            is CheckoutViewContract.Event.TermsAndConditionsRequested ->
                showTermsAndConditions(viewEvent.url)
            is CheckoutViewContract.Event.BookingSuccess ->
                updateBookingRequestStatus(viewEvent.tripInfo)
            is CheckoutViewContract.Event.BookingError ->
                handleBookingError(viewEvent.stringId, viewEvent.karhooError)
        }
    }

    private fun handleBookingError(@StringRes stringId: Int, karhooError: KarhooError?) {
        viewAction = CheckoutViewContract.Action.HandleBookingError(stringId,
                                                                                        karhooError)
    }

    private fun showTermsAndConditions(url: String) {
        viewAction = CheckoutViewContract.Action.ShowTermsAndConditions(url)
    }

    private fun updateBookingRequestStatus(tripInfo: TripInfo) {
        viewAction = CheckoutViewContract.Action.WaitForTripAllocation
        viewState = BookingRequestStatus(tripInfo)
    }
}
