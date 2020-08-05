package com.karhoo.uisdk.screen.booking.domain.supplier

import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus

interface AvailabilityProvider {

    fun filterVehicleListByCategory(name: String)

    fun setAllCategory(category: String)

    fun setErrorView(snackbar: ErrorView)

    fun setAvailabilityHandler(availabilityHandler: AvailabilityHandler)

    fun cleanup()

    fun bookingStatusObserver(): androidx.lifecycle.Observer<BookingStatus>

}

interface AvailabilityHandler {
    var hasAvailability: Boolean
}