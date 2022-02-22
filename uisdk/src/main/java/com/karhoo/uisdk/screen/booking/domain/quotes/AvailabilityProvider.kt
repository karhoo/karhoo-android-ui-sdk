package com.karhoo.uisdk.screen.booking.domain.quotes

import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails

interface AvailabilityProvider {

    fun filterVehicleListByCategory(name: String)

    fun setAllCategory(category: String)

    fun setAvailabilityHandler(availabilityHandler: AvailabilityHandler)

    fun cleanup()

    fun bookingStatusObserver(): androidx.lifecycle.Observer<JourneyDetails>

}

interface AvailabilityHandler {
    var hasAvailability: Boolean
    var hasNoResults: Boolean

    fun handleAvailabilityError(snackbarConfig: SnackbarConfig)
}
