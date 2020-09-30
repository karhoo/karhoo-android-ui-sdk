package com.karhoo.uisdk.screen.booking.booking.supplier

import android.app.Application
import com.karhoo.sdk.api.model.Quote
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
                setSupplierListVisibility(viewEvent.isVisible)
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

    private fun setSupplierListVisibility(isVisible: Boolean) {
        viewAction = BookingSupplierViewContract.BookingSupplierAction.UpdateViewForSupplierListVisibilityChange(isVisible)
    }

    private fun showBookingRequest(selectedQuote: Quote) {
        viewState = QuoteListStatus(selectedQuote)
        viewAction = BookingSupplierViewContract.BookingSupplierAction.ShowBookingRequest(selectedQuote)
    }
}
