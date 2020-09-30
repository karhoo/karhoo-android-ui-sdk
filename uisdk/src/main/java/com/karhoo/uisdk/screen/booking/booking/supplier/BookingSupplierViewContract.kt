package com.karhoo.uisdk.screen.booking.booking.supplier

import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.supplier.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.supplier.LiveFleetsViewModel
import com.karhoo.uisdk.screen.booking.supplier.category.CategoriesViewModel

interface BookingSupplierViewContract {

    interface BookingSupplierWidget {
        fun setSupplierListVisibility()
        fun bindAvailability(availabilityProvider: AvailabilityProvider)
        fun bindViewToData(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel: BookingStatusStateViewModel,
                           categoriesViewModel: CategoriesViewModel, vehicles:
                           LiveFleetsViewModel, bookingSupplierViewModel: BookingSupplierViewModel)
    }

    sealed class BookingSupplierEvent {
        data class SupplierListVisibilityChanged(val isVisible: Boolean) : BookingSupplierEvent()
        data class SupplierItemClicked(val quote: Quote) : BookingSupplierEvent()
        object Availability : BookingSupplierEvent()
        data class Error(val snackbarConfig: SnackbarConfig) : BookingSupplierEvent()
    }

    sealed class BookingSupplierAction {
        object HideError : BookingSupplierAction()
        data class ShowError(val snackbarConfig: SnackbarConfig) : BookingSupplierAction()
        data class UpdateViewForSupplierListVisibilityChange(val isVisible: Boolean) : BookingSupplierAction()
        data class ShowBookingRequest(val quote: Quote) : BookingSupplierAction()
    }
}
