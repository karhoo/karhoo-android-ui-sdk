package com.karhoo.uisdk.screen.booking.booking.supplier

import android.app.Application
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.state.BaseStateViewModel

class BookingSupplierViewModel(application: Application) :
        BaseStateViewModel<QuoteListStatus, BookingSupplierViewContract.BookingSupplierAction,
                BookingSupplierViewContract.BookingSupplierEvent>(application) {

    init {
        viewState = QuoteListStatus(null)
    }

    override fun process(viewEvent: BookingSupplierViewContract.BookingSupplierEvent) {
        super.process(viewEvent)
        when (viewEvent) {
            is BookingSupplierViewContract.BookingSupplierEvent.SupplierListVisibilityChanged ->
                setSupplierListVisibility(viewEvent.isVisible, viewEvent.panelState)
            is BookingSupplierViewContract.BookingSupplierEvent.SupplierListCollapsed ->
                setSupplierListCollapsed()
            is BookingSupplierViewContract.BookingSupplierEvent.SupplierListExpanded ->
                setSupplierListExpanded()
            is BookingSupplierViewContract.BookingSupplierEvent.SupplierItemClicked ->
                showBookingRequest(viewEvent.quote)
            is BookingSupplierViewContract.BookingSupplierEvent.Availability -> setHideNoAvailability()
            is BookingSupplierViewContract.BookingSupplierEvent.Error ->
                setShowNoAvailability(viewEvent.snackbarConfig)
        }
    }

    private fun setShowNoAvailability(snackbarConfig: SnackbarConfig) {
        viewAction = BookingSupplierViewContract.BookingSupplierAction.ShowError(snackbarConfig)
    }

    private fun setHideNoAvailability() {
        viewAction = BookingSupplierViewContract.BookingSupplierAction.HideError
    }

    private fun setSupplierListCollapsed() {
        viewAction = BookingSupplierViewContract.BookingSupplierAction.UpdateViewForSupplierListCollapsed
    }

    private fun setSupplierListExpanded() {
        viewAction = BookingSupplierViewContract.BookingSupplierAction.UpdateViewForSupplierListExpanded
    }

    private fun setSupplierListVisibility(isVisible: Boolean, panelState: CollapsiblePanelView.PanelState) {
        viewAction = BookingSupplierViewContract.BookingSupplierAction
                .UpdateViewForSupplierListVisibilityChange(isVisible, panelState)
    }

    private fun showBookingRequest(selectedQuote: Quote) {
        viewState = QuoteListStatus(selectedQuote)
        viewAction = BookingSupplierViewContract.BookingSupplierAction.ShowBookingRequest(selectedQuote)
    }
}
