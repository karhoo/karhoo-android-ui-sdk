package com.karhoo.uisdk.screen.booking.booking.quotes

import android.app.Application
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.state.BaseStateViewModel

class BookingQuotesViewModel(application: Application) :
        BaseStateViewModel<QuoteListStatus, BookingQuotesViewContract.BookingSupplierAction,
                BookingQuotesViewContract.BookingSupplierEvent>(application) {

    init {
        viewState = QuoteListStatus(null)
    }

    override fun process(viewEvent: BookingQuotesViewContract.BookingSupplierEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is BookingQuotesViewContract.BookingSupplierEvent.SupplierListVisibilityChanged ->
                setSupplierListVisibility(viewEvent.isVisible, viewEvent.panelState)
            is BookingQuotesViewContract.BookingSupplierEvent.SupplierListCollapsed         ->
                setSupplierListCollapsed()
            is BookingQuotesViewContract.BookingSupplierEvent.SupplierListExpanded          ->
                setSupplierListExpanded()
            is BookingQuotesViewContract.BookingSupplierEvent.SupplierItemClicked           ->
                showBookingRequest(viewEvent.quote)
            is BookingQuotesViewContract.BookingSupplierEvent.Availability                  -> setHideNoAvailability()
            is BookingQuotesViewContract.BookingSupplierEvent.Error                         ->
                setShowNoAvailability(viewEvent.snackbarConfig)
        }
    }

    private fun setShowNoAvailability(snackbarConfig: SnackbarConfig) {
        viewAction = BookingQuotesViewContract.BookingSupplierAction.ShowError(snackbarConfig)
    }

    private fun setHideNoAvailability() {
        viewAction = BookingQuotesViewContract.BookingSupplierAction.HideError
    }

    private fun setSupplierListCollapsed() {
        viewAction = BookingQuotesViewContract.BookingSupplierAction.UpdateViewForSupplierListCollapsed
    }

    private fun setSupplierListExpanded() {
        viewAction = BookingQuotesViewContract.BookingSupplierAction.UpdateViewForSupplierListExpanded
    }

    private fun setSupplierListVisibility(isVisible: Boolean, panelState: CollapsiblePanelView.PanelState) {
        viewAction = BookingQuotesViewContract.BookingSupplierAction
                .UpdateViewForSupplierListVisibilityChange(isVisible, panelState)
    }

    private fun showBookingRequest(selectedQuote: Quote) {
        viewState = QuoteListStatus(selectedQuote)
        viewAction = BookingQuotesViewContract.BookingSupplierAction.ShowBookingRequest(selectedQuote)
    }
}
