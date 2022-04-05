package com.karhoo.uisdk.screen.booking.domain.quotes

import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterChain

interface AvailabilityProvider {

    fun filterVehicleListByCategory(name: String)

    fun filterVehicleListByFilterChain(filterChain: FilterChain)

    fun getNonFilteredVehicles(): List<Quote>

    fun setAllCategory(category: String)

    fun setAvailabilityHandler(availabilityHandler: AvailabilityHandler)

    fun cleanup()

    fun journeyDetailsObserver(): androidx.lifecycle.Observer<JourneyDetails>

}

interface AvailabilityHandler {
    var hasAvailability: Boolean
    var hasNoResults: Boolean

    fun handleAvailabilityError(snackbarConfig: SnackbarConfig)
    fun handleSameAddressesError()
}
