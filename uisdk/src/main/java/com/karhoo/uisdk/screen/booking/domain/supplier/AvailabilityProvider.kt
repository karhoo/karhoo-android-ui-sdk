package com.karhoo.uisdk.screen.booking.domain.supplier

import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus

interface AvailabilityProvider {

    fun filterVehicleListByCategory(name: String)

    fun setAllCategory(category: String)

    fun setAvailabilityHandler(availabilityHandler: AvailabilityHandler)

    fun cleanup()

    fun bookingStatusObserver(): androidx.lifecycle.Observer<BookingStatus>

}

interface AvailabilityHandler {
    var hasAvailability: Boolean

    fun handleAvailabilityError(snackbarConfig: SnackbarConfig)
}
