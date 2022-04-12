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
import com.karhoo.uisdk.screen.booking.quotes.QuotesListMVP
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.quotes.category.Category
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
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

private const val MAX_ACCEPTABLE_QTA = 20

class KarhooAvailability(
    private val quotesService: QuotesService,
    private val categoriesViewModel: CategoriesViewModel,
    private val liveFleetsViewModel: LiveFleetsViewModel,
    private val journeyDetailsStateViewModel: JourneyDetailsStateViewModel,
    lifecycleOwner: LifecycleOwner,
    private val locale: Locale? = null
) : AvailabilityProvider {

    private var filteredList: MutableList<Quote>? =
        liveFleetsViewModel.liveFleets.value?.toMutableList()
    private var categoryViewModels: MutableList<Category> = mutableListOf()
    private var availableVehicles: Map<String, List<Quote>> = mutableMapOf()
    private var allCategory: Category? = null
    private var vehiclesObserver: Observer<Resource<QuoteList>>? = null
    private var vehiclesObservable: Observable<QuoteList>? = null
    private var currentFilter: String? = null
    private var availabilityHandler: WeakReference<AvailabilityHandler>? = null
    private var analytics: Analytics? = null
    private var lastTimestampDate: Long = 0
    private var refreshDelay: Long = 0
    private var running: Boolean = false
    private var journeyDetails: JourneyDetails? = null

    private val observer = createObservable()
    private var vehiclesJob: Job? = null
    var quoteListValidityListener: QuotesListMVP.QuoteValidityListener? = null

    init {
        journeyDetailsStateViewModel.viewStates().observe(lifecycleOwner, observer)
    }


    override fun cleanup() {
        cancelVehicleCallback()
        journeyDetailsStateViewModel.viewStates().removeObserver(observer)
    }

    override fun journeyDetailsObserver(): androidx.lifecycle.Observer<JourneyDetails> {
        return observer
    }

    private fun setAvailableCategories(availableCategories: Map<String, Boolean>) {
        categoryViewModels.forEach {
            it.isAvailable = availableCategories.containsKey(it.categoryName)
        }
        categoriesViewModel.categories.value = categoryViewModels
    }

    @Suppress("NestedBlockDepth")
    private fun requestVehicleAvailability(journeyDetails: JourneyDetails?) {
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

    override fun filterVehicleListByCategory(name: String) {
        this.currentFilter = name
        filterVehicles()
    }

    private fun filterVehicles() {
        if (currentFilter?.isEmpty() == true) {
            return
        }
        currentFilter?.let {
            getFilteredVehiclesForCategory(it)
        }
    }

    private fun getFilteredVehiclesForCategory(currentFilter: String) {
        if (currentFilter == allCategory?.categoryName) {
            filteredList = mutableListOf()
            availableVehicles.values.forEach { filteredList?.addAll(it) }
        } else {
            filteredList = availableVehicles[this.currentFilter.orEmpty()]?.toMutableList()
        }
        updateFleets(filteredList)
    }

    private fun updateFleets(filteredList: MutableList<Quote>?) {
        filteredList?.let {
            liveFleetsViewModel.liveFleets.value = filteredList
        }
    }

    override fun setAllCategory(category: String) {
        allCategory = Category(category, false)
    }

    fun setAnalytics(analytics: Analytics?) {
        this.analytics = analytics
    }

    private fun createObservable() = androidx.lifecycle.Observer<JourneyDetails> { journeyDetails ->
        cancelVehicleCallback()
        if (journeyDetails != null && journeyDetails.destination == null
            && categoryViewModels.isNotEmpty()
        ) {
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
                is Resource.Success -> updateVehicles(value.data)
                is Resource.Failure -> handleAvailabilityError(value.error)
            }
        }
    }

    private fun handleAvailabilityError(error: KarhooError) {
        when (error) {
            KarhooError.CouldNotGetAvailabilityNoneFound -> {
                cancelVehicleCallback()
                availabilityHandler?.get()?.hasAvailability = false
            }
            KarhooError.OriginAndDestinationIdentical -> {
                clearDestination()
                availabilityHandler?.get()?.handleAvailabilityError(SnackbarConfig(text = null, messageResId =
                returnErrorStringOrLogoutIfRequired(error), karhooError = error))
            }
            else -> availabilityHandler?.get()?.handleAvailabilityError(
                SnackbarConfig(
                    text = null, messageResId =
                    returnErrorStringOrLogoutIfRequired(error), karhooError = error
                )
            )
        }
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

        lastTimestampDate = Date().time

        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, vehicles.validity)
        quoteListValidityListener?.isValidUntil(calendar.time.time)
    }

    private fun updateVehicles(vehicles: QuoteList) {
        handleVehiclePolling(vehicles)

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
            currentCategories(currentCategories = vehicles.categories.keys.toList())
            availableVehicles = vehicles.categories
            currentAvailableQuotes()
            filterVehicles()
        }
    }

    private fun currentCategories(currentCategories: List<String>) {
        if (currentCategories.isEmpty()) {
            categoryViewModels.clear()
        } else {
            categoryViewModels = mutableListOf()
            currentCategories.forEach { categoryViewModels.add(Category(it, false)) }
            allCategory?.let { categoryViewModels.add(it) }
        }
    }

    private fun currentAvailableQuotes() {
        if (journeyDetailsStateViewModel.currentState.date != null) {
            setAvailableCategories(handlePrebookCategories())
        } else {
            setAvailableCategories(handleAsapCategories())
        }
    }

    private fun handleAsapCategories(): Map<String, Boolean> {
        val activeCategories = HashMap<String, Boolean>()
        availableVehicles.forEach {
            it.value.filter { it.vehicle.vehicleQta.highMinutes <= MAX_ACCEPTABLE_QTA }
                .forEach { isCategoryAvailable(activeCategories, it) }
        }

        return activeCategories
    }

    private fun handlePrebookCategories(): MutableMap<String, Boolean> {
        val activeCategories = HashMap<String, Boolean>()
        availableVehicles.forEach {
            it.value.forEach { isCategoryAvailable(activeCategories, it) }
        }
        return activeCategories
    }

    private fun isCategoryAvailable(
        activeCategories: HashMap<String, Boolean>, vehicleDetails:
        Quote
    ) {
        vehicleDetails.vehicle.vehicleClass?.let {
            if (!activeCategories.containsKey(it)) {
                activeCategories[it] = true
            }
        }
        allCategory?.let {
            if (availableVehicles.isNotEmpty() && !activeCategories.containsKey(it.categoryName)) {
                activeCategories[it.categoryName] = true
            }
        }
    }

    override fun pauseUpdates() {
        running = false
        vehiclesJob?.cancel()
    }

    override fun resumeUpdates() {
        if (!running) {
            if (TimeUnit.MILLISECONDS.toSeconds((Date().time - lastTimestampDate)) < MINIMUM_REFRESH_DURATION_LEFT) {
                requestVehicleAvailability(journeyDetails)
            } else {
                vehiclesJob = GlobalScope.launch {
                    delay(refreshDelay)
                    vehiclesObserver?.let { vehiclesObservable?.subscribe(it) }
                }
            }
        }
    }

    companion object {
        private const val MINIMUM_REFRESH_DURATION_LEFT = 120
    }
}
