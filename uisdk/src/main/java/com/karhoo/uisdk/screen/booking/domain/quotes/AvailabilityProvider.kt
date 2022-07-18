package com.karhoo.uisdk.screen.booking.domain.quotes

import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterChain
import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.service.quotes.QuotesService
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import java.util.*

interface AvailabilityProvider {
    var shouldRunInBackground: Boolean

    fun filterVehicleListByFilterChain(filterChain: FilterChain): FilterChain

    fun getNonFilteredVehicles(): List<Quote>

    fun setAvailabilityHandler(availabilityHandler: AvailabilityHandler)

    fun cleanup()

    fun pauseUpdates(fromBackButton: Boolean = false)

    fun resumeUpdates()

    fun restoreData()

    fun journeyDetailsObserver(): androidx.lifecycle.Observer<JourneyDetails>

    fun setup(quotesService: QuotesService,
              liveFleetsViewModel: LiveFleetsViewModel,
              journeyDetailsStateViewModel: JourneyDetailsStateViewModel,
              lifecycleOwner: LifecycleOwner,
              locale: Locale? = null,
              shouldRestoreData: Boolean = false)

    fun getExistingFilterChain(): FilterChain?
}

interface AvailabilityHandler {
    var hasAvailability: Boolean
    var hasNoResults: Boolean

    fun handleAvailabilityError(snackbarConfig: SnackbarConfig)
    fun handleSameAddressesError()
    fun handleNoResultsForFiltersError()
}
