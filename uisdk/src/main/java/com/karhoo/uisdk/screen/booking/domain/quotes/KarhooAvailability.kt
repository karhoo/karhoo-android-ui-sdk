package com.karhoo.uisdk.screen.booking.domain.quotes

import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteId
import com.karhoo.sdk.api.model.QuoteList
import com.karhoo.sdk.api.model.QuoteStatus
import com.karhoo.sdk.api.model.QuotesSearch
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.quotes.QuotesService
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.quotes.fragment.QuotesFragmentContract
import com.karhoo.uisdk.screen.booking.quotes.filterview.FilterChain
import com.karhoo.uisdk.util.ViewsConstants.VALIDITY_DEFAULT_INTERVAL
import com.karhoo.uisdk.util.ViewsConstants.VALIDITY_SECONDS_TO_MILLISECONDS_FACTOR
import com.karhoo.uisdk.util.extension.toNormalizedLocale
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

object KarhooAvailability : AvailabilityProvider {

    private lateinit var quotesService: QuotesService
    private lateinit var liveFleetsViewModel: LiveFleetsViewModel
    private lateinit var journeyDetailsStateViewModel: JourneyDetailsStateViewModel
    private lateinit var lifecycleOwner: LifecycleOwner
    private var locale: Locale? = null
    private var filteredList: MutableList<Quote>? = null
    private var lastDataRetrieved: QuoteList? = null
    private var restorePreviousData = false

    private var availableVehicles: Map<String, List<Quote>> = mutableMapOf()
    private var vehiclesObserver: Observer<Resource<QuoteList>>? = null
    private var vehiclesObservable: Observable<QuoteList>? = null
    private var currentFilter: String? = null
    private var availabilityHandler: WeakReference<AvailabilityHandler>? = null
    private var analytics: Analytics? = null
    private var filterChain: FilterChain? = null
    private var refreshDelay: Long = 0
    private var running: Boolean = false
    private var journeyDetails: JourneyDetails? = null

    private lateinit var observer: androidx.lifecycle.Observer<JourneyDetails>
    private var vehiclesJob: Job? = null
    var quoteListValidityListener: QuotesFragmentContract.QuoteValidityListener? = null
    var quoteListPoolingStatusListener: QuotesFragmentContract.QuotePoolingStatusListener? = null

    override var shouldRunInBackground: Boolean = false

    override fun setup(
        quotesService: QuotesService,
        liveFleetsViewModel: LiveFleetsViewModel,
        journeyDetailsStateViewModel: JourneyDetailsStateViewModel,
        lifecycleOwner: LifecycleOwner,
        locale: Locale?,
        shouldRestoreData: Boolean
    ) {
        this.quotesService = quotesService
        this.liveFleetsViewModel = liveFleetsViewModel
        this.journeyDetailsStateViewModel = journeyDetailsStateViewModel
        this.lifecycleOwner = lifecycleOwner
        this.locale = locale
        this.restorePreviousData = shouldRestoreData
        filteredList = KarhooAvailability.liveFleetsViewModel.liveFleets.value?.toMutableList()
        observer = createObservable()
        KarhooAvailability.journeyDetailsStateViewModel.viewStates()
            .observe(KarhooAvailability.lifecycleOwner, observer)
    }

    override fun getExistingFilterChain(): FilterChain? {
        return if(this.filterChain != null)
            this.filterChain
        else null
    }


    override fun cleanup() {
        filterChain = null
        lastDataRetrieved = null
    }

    override fun journeyDetailsObserver(): androidx.lifecycle.Observer<JourneyDetails> {
        return observer
    }

    @Suppress("NestedBlockDepth")
    private fun requestVehicleAvailability(journeyDetails: JourneyDetails?) {
        if (restorePreviousData && lastDataRetrieved != null) {
            restoreData()
            restorePreviousData = false
            return
        }

        running = true
        cancelVehicleCallback()
        journeyDetails?.pickup?.let { bookingStatusPickup ->
            journeyDetails.destination?.let { bookingStatusDestination ->
                vehiclesObserver = quotesCallback()
                vehiclesObserver?.let { observer ->
                    vehiclesObservable = quotesService
                        .quotes(
                            QuotesSearch(
                                origin = bookingStatusPickup,
                                destination = bookingStatusDestination,
                                dateScheduled = journeyDetails.date?.toDate()
                            ),
                            locale.toNormalizedLocale()
                        )
                        .observable().apply { subscribe(observer) }
                }
            }
        }
    }

    private fun cancelVehicleCallback() {
        vehiclesObserver?.let { vehiclesObservable?.apply { unsubscribe(it) } }
        vehiclesJob?.cancel()
        availableVehicles = mutableMapOf()
        currentAvailableQuotes()
        updateFleets(mutableListOf())
        this.journeyDetails = null
    }

    override fun filterVehicleListByFilterChain(filterChain: FilterChain): FilterChain {
        this.filterChain = filterChain
        filterVehicles()
        return this.filterChain!!
    }

    private fun filterVehicles() {
        filterChain?.let {
            getFilteredVehiclesForFilterChain(it)
        }
    }

    private fun getFilteredVehiclesForFilterChain(filterChain: FilterChain) {
        filteredList = mutableListOf()
        availableVehicles.values.forEach {
            filteredList?.addAll(filterChain.applyFilters(it))
        }
        updateFleets(filteredList)
    }

    override fun getNonFilteredVehicles(): List<Quote> {
        return availableVehicles.values.flatten()
    }

    private fun updateFleets(filteredList: MutableList<Quote>?) {
        filteredList?.let {
            liveFleetsViewModel.liveFleets.value = filteredList
        }
    }

    fun setAnalytics(analytics: Analytics?) {
        this.analytics = analytics
    }

    private fun createObservable() = androidx.lifecycle.Observer<JourneyDetails> { journeyDetails ->
        if (journeyDetails != null && journeyDetails.destination == null
        ) {
            cancelVehicleCallback()
            updateVehicles(QuoteList(categories = emptyMap(), id = QuoteId(), validity = 0))
        } else {
            requestVehicleAvailability(journeyDetails)
        }
        this.journeyDetails = journeyDetails
    }

    override fun setAvailabilityHandler(availabilityHandler: AvailabilityHandler) {
        this.availabilityHandler = WeakReference(availabilityHandler)
    }

    private fun quotesCallback() = object : Observer<Resource<QuoteList>> {
        override fun onValueChanged(value: Resource<QuoteList>) {
            when (value) {
                is Resource.Success -> {
                    quoteListPoolingStatusListener?.changedStatus(value.data.status)
                    handleVehiclePolling(value.data)
                    if (!shouldRunInBackground) {
                        updateVehicles(value.data)
                        shouldRunInBackground = false;
                    }
                }
                is Resource.Failure -> {
                    quoteListPoolingStatusListener?.changedStatus(QuoteStatus.COMPLETED)
                    handleAvailabilityError(value.error)
                }
            }
        }
    }

    private fun handleAvailabilityError(error: KarhooError) {
        when (error) {
            KarhooError.CouldNotGetAvailabilityNoneFound -> {
                availabilityHandler?.get()?.hasAvailability = false
            }
            KarhooError.OriginAndDestinationIdentical -> {
                availabilityHandler?.get()?.handleSameAddressesError()
            }
            else -> availabilityHandler?.get()?.handleAvailabilityError(
                SnackbarConfig(
                    text = null, messageResId =
                    returnErrorStringOrLogoutIfRequired(error), karhooError = error
                )
            )
        }

        cancelVehicleCallback()
    }

    private fun handleVehicleValidity(vehicles: QuoteList) {
        refreshDelay = when {
            vehicles.validity == -1 -> 0
            vehicles.validity >= VALIDITY_DEFAULT_INTERVAL -> vehicles.validity.times(
                VALIDITY_SECONDS_TO_MILLISECONDS_FACTOR
            )
            else -> VALIDITY_DEFAULT_INTERVAL.times(VALIDITY_SECONDS_TO_MILLISECONDS_FACTOR)
        }

        vehiclesJob = GlobalScope.launch {
            delay(refreshDelay)
            vehiclesObserver?.let { vehiclesObservable?.subscribe(it) }
        }
    }

    private fun handleVehiclePolling(vehicles: QuoteList) {
        if (vehicles.status == QuoteStatus.COMPLETED) {
            cancelVehicleCallback()
            handleVehicleValidity(vehicles)
        }
        lastDataRetrieved = vehicles

        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, vehicles.validity)
        quoteListValidityListener?.isValidUntil(calendar.time.time)
    }

    private fun updateVehicles(vehicles: QuoteList) {
        var hasQuotes = false
        vehicles.categories.forEach {
            if (it.value.isNotEmpty()) {
                hasQuotes = true
            }
        }

        if (vehicles.status == QuoteStatus.COMPLETED && !hasQuotes) {
            availabilityHandler?.get()?.hasNoResults = true
        } else {
            availabilityHandler?.get()?.hasNoResults = false
            availabilityHandler?.get()?.hasAvailability = true
            availableVehicles = vehicles.categories

            currentAvailableQuotes()
            filterVehicles()
        }

        if (vehicles.status == QuoteStatus.COMPLETED && filteredList?.isEmpty() == true){
            availabilityHandler?.get()?.handleNoResultsForFiltersError()
        }
    }

    private fun currentAvailableQuotes() {
        val activeCategories = HashMap<String, Boolean>()
        availableVehicles.forEach {
            it.value.forEach { activeCategories[it.id!!] = true }
        }
    }

    override fun pauseUpdates(fromBackButton: Boolean) {
        if(!fromBackButton){
            running = false
            vehiclesObserver?.let {
                vehiclesObservable?.unsubscribe(it)
            }
            vehiclesJob?.cancel()
        }
    }

    override fun resumeUpdates() {
        if (!running) {
            vehiclesJob = GlobalScope.launch {
                vehiclesObserver?.let { vehiclesObservable?.subscribe(it) }
            }

            running = true
        }
    }

    override fun restoreData() {
        if (shouldRunInBackground) {
            lastDataRetrieved?.let {
                updateVehicles(it)
                shouldRunInBackground = false
            }
        }
    }
}
