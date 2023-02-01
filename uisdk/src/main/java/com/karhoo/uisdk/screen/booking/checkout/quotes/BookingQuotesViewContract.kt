package com.karhoo.uisdk.screen.booking.checkout.quotes

import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig

interface BookingQuotesViewContract {
    sealed class BookingQuotesEvent {
        data class QuotesListVisibilityChanged(val isVisible: Boolean, val panelState: CollapsiblePanelView.PanelState) : BookingQuotesEvent()
        object QuotesListCollapsed : BookingQuotesEvent()
        object QuotesListExpanded : BookingQuotesEvent()
        data class QuotesItemClicked(val quote: Quote) : BookingQuotesEvent()
        object Availability : BookingQuotesEvent()
        data class Error(val snackbarConfig: SnackbarConfig) : BookingQuotesEvent()
        data class QuoteListValidity(val timestamp: Long) : BookingQuotesEvent()
    }

    sealed class BookingQuotesAction {
        object HideError : BookingQuotesAction()
        data class ShowError(val snackbarConfig: SnackbarConfig) : BookingQuotesAction()
        data class UpdateViewForQuotesListVisibilityChange(val isVisible: Boolean, val panelState: CollapsiblePanelView.PanelState) : BookingQuotesAction()
        object UpdateViewForQuotesListCollapsed : BookingQuotesAction()
        object UpdateViewForQuotesListExpanded : BookingQuotesAction()
        data class ShowBookingRequest(val quote: Quote) : BookingQuotesAction()
        data class SetValidityDeadlineTimestamp(val timestamp: Long) : BookingQuotesAction()
    }
}
