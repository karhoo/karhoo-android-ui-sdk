package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.app.Application
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.state.BaseStateViewModel

class BookingQuotesViewModel(application: Application) :
        BaseStateViewModel<QuoteListStatus, BookingQuotesViewContract.BookingQuotesAction,
                BookingQuotesViewContract.BookingQuotesEvent>(application) {

    init {
        viewState = QuoteListStatus(null)
    }

    override fun process(viewEvent: BookingQuotesViewContract.BookingQuotesEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is BookingQuotesViewContract.BookingQuotesEvent.QuotesListVisibilityChanged ->
                setQuotesListVisibility(viewEvent.isVisible, viewEvent.panelState)
            is BookingQuotesViewContract.BookingQuotesEvent.QuotesListCollapsed         ->
                setQuotesListCollapsed()
            is BookingQuotesViewContract.BookingQuotesEvent.QuotesListExpanded          ->
                setQuotesListExpanded()
            is BookingQuotesViewContract.BookingQuotesEvent.QuotesItemClicked           ->
                showBookingRequest(viewEvent.quote)
            is BookingQuotesViewContract.BookingQuotesEvent.Availability                -> setHideNoAvailability()
            is BookingQuotesViewContract.BookingQuotesEvent.Error                       ->
                setShowNoAvailability(viewEvent.snackbarConfig)
        }
    }

    private fun setShowNoAvailability(snackbarConfig: SnackbarConfig) {
        viewAction = BookingQuotesViewContract.BookingQuotesAction.ShowError(snackbarConfig)
    }

    private fun setHideNoAvailability() {
        viewAction = BookingQuotesViewContract.BookingQuotesAction.HideError
    }

    private fun setQuotesListCollapsed() {
        viewAction = BookingQuotesViewContract.BookingQuotesAction.UpdateViewForQuotesListCollapsed
    }

    private fun setQuotesListExpanded() {
        viewAction = BookingQuotesViewContract.BookingQuotesAction.UpdateViewForQuotesListExpanded
    }

    private fun setQuotesListVisibility(isVisible: Boolean, panelState: CollapsiblePanelView.PanelState) {
        viewAction = BookingQuotesViewContract.BookingQuotesAction
                .UpdateViewForQuotesListVisibilityChange(isVisible, panelState)
    }

    private fun showBookingRequest(selectedQuote: Quote) {
        viewState = QuoteListStatus(selectedQuote)
        viewAction = BookingQuotesViewContract.BookingQuotesAction.ShowBookingRequest(selectedQuote)
    }
}
