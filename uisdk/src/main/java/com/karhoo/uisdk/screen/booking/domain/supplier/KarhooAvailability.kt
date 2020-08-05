package com.karhoo.uisdk.screen.booking.domain.supplier

import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.QuoteId
import com.karhoo.sdk.api.model.QuoteListV2
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.sdk.api.model.QuotesSearch
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.observable.Observer
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.quotes.QuotesService
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.supplier.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.supplier.category.Category
import com.karhoo.uisdk.util.returnErrorStringOrLogoutIfRequired
import java.lang.ref.WeakReference

private const val MAX_ACCEPTABLE_QTA = 20

class KarhooAvailability(private val quotesService: QuotesService, private val analytics: Analytics?,
                         private val categoriesViewModel: CategoriesViewModel, private val liveFleetsViewModel: LiveFleetsViewModel,
                         private val bookingStatusStateViewModel: BookingStatusStateViewModel, lifecycleOwner: LifecycleOwner)
    : AvailabilityProvider {

    private var filteredList: MutableList<QuoteV2>? = liveFleetsViewModel.liveFleets.value?.toMutableList()
    private var categoryViewModels: MutableList<Category> = mutableListOf()
    private var availableVehicles: Map<String, List<QuoteV2>> = mutableMapOf()
    private var allCategory: Category? = null
    private var vehiclesObserver: Observer<Resource<QuoteListV2>>? = null
    private var vehiclesObservable: Observable<QuoteListV2>? = null
    private var currentFilter: String? = null
    private var errorView: WeakReference<ErrorView>? = null
    private var availabilityHandler: WeakReference<AvailabilityHandler>? = null

    private val observer = createObservable()

    init {
        bookingStatusStateViewModel.viewStates().observe(lifecycleOwner, observer)
    }

    override fun cleanup() {
        bookingStatusStateViewModel.viewStates().removeObserver(observer)
        errorView = null
        cancelVehicleCallback()
    }

    override fun bookingStatusObserver(): androidx.lifecycle.Observer<BookingStatus> {
        return observer
    }

    private fun setAvailableCategories(availableCategories: Map<String, Boolean>) {
        categoryViewModels.forEach { it.isAvailable = availableCategories.containsKey(it.categoryName) }
        categoriesViewModel.categories.value = categoryViewModels
    }

    private fun requestVehicleAvailability(bookingStatus: BookingStatus?) {
        bookingStatus?.pickup?.let { bookingStatusPickup ->
            bookingStatus.destination?.let { bookingStatusDestination ->
                vehiclesObserver = quotesCallback()
                vehiclesObserver?.let { observer ->
                    vehiclesObservable = quotesService
                            .quotesV2(QuotesSearch(
                                    origin = bookingStatusPickup,
                                    destination = bookingStatusDestination,
                                    dateScheduled = bookingStatus.date?.toDate()))
                            .observable().apply { subscribe(observer) }
                }
            }
        }
    }

    private fun cancelVehicleCallback() {
        vehiclesObserver?.let { vehiclesObservable?.apply { unsubscribe(it) } }
        availableVehicles = mutableMapOf()
        currentAvailableSuppliers()
        updateFleets(mutableListOf())
    }

    override fun filterVehicleListByCategory(name: String) {
        this.currentFilter = name
        filterVehicles()
        if (filteredList?.isEmpty() == false) {
            analytics?.vehicleSelected(currentFilter.orEmpty(), filteredList?.get(0)?.id)
        }
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

    private fun updateFleets(filteredList: MutableList<QuoteV2>?) {
        filteredList?.let {
            liveFleetsViewModel.liveFleets.value = filteredList
        }
    }

    override fun setAllCategory(category: String) {
        allCategory = Category(category, false)
    }

    private fun createObservable() = androidx.lifecycle.Observer<BookingStatus> { bookingStatus ->
        cancelVehicleCallback()
        if (bookingStatus != null && bookingStatus.destination == null
                && categoryViewModels.isNotEmpty()) {
            updateVehicles(QuoteListV2(categories = emptyMap(), id = QuoteId()))
        } else {
            requestVehicleAvailability(bookingStatus)
        }
    }

    override fun setErrorView(snackbar: ErrorView) {
        this.errorView = WeakReference(snackbar)
    }

    override fun setAvailabilityHandler(availabilityHandler: AvailabilityHandler) {
        this.availabilityHandler = WeakReference(availabilityHandler)
    }

    private fun quotesCallback() = object : Observer<Resource<QuoteListV2>> {
        override fun onValueChanged(value: Resource<QuoteListV2>) {
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
                errorView?.get()?.showSnackbar(SnackbarConfig(text = null, stringId = returnErrorStringOrLogoutIfRequired(error)))
            }
            else -> errorView?.get()?.showSnackbar(SnackbarConfig(text = null, stringId = returnErrorStringOrLogoutIfRequired(error)))
        }
    }

    private fun clearDestination() {
        bookingStatusStateViewModel.process(AddressBarViewContract.AddressBarEvent
                                                    .DestinationAddressEvent(null))
    }

    private fun updateVehicles(vehicles: QuoteListV2) {
        availabilityHandler?.get()?.hasAvailability = true
        currentCategories(currentCategories = vehicles.categories.keys.toList())
        availableVehicles = vehicles.categories
        currentAvailableSuppliers()
        filterVehicles()
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

    private fun currentAvailableSuppliers() {
        if (bookingStatusStateViewModel.currentState.date != null) {
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

    private fun isCategoryAvailable(activeCategories: HashMap<String, Boolean>, vehicleDetails:
    QuoteV2) {
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
}
