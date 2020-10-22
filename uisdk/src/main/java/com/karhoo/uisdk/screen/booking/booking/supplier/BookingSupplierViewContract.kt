package com.karhoo.uisdk.screen.booking.booking.supplier

import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel

interface BookingSupplierViewContract {

    interface BookingSupplierWidget {
        fun initAvailability(lifecycleOwner: LifecycleOwner)
        fun setSupplierListVisibility()
        fun bindViewToData(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel: BookingStatusStateViewModel, bookingSupplierViewModel: BookingSupplierViewModel)
        fun cleanup()
    }

    sealed class BookingSupplierEvent {
        data class SupplierListVisibilityChanged(val isVisible: Boolean, val panelState: CollapsiblePanelView.PanelState) : BookingSupplierEvent()
        object SupplierListCollapsed : BookingSupplierEvent()
        object SupplierListExpanded : BookingSupplierEvent()
        data class SupplierItemClicked(val quote: Quote) : BookingSupplierEvent()
        object Availability : BookingSupplierEvent()
        data class Error(val snackbarConfig: SnackbarConfig) : BookingSupplierEvent()
    }

    sealed class BookingSupplierAction {
        object HideError : BookingSupplierAction()
        data class ShowError(val snackbarConfig: SnackbarConfig) : BookingSupplierAction()
        data class UpdateViewForSupplierListVisibilityChange(val isVisible: Boolean, val panelState: CollapsiblePanelView.PanelState) : BookingSupplierAction()
        object UpdateViewForSupplierListCollapsed : BookingSupplierAction()
        object UpdateViewForSupplierListExpanded : BookingSupplierAction()
        data class ShowBookingRequest(val quote: Quote) : BookingSupplierAction()
    }
}
