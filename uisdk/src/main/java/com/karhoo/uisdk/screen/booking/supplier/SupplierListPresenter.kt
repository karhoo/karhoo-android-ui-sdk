package com.karhoo.uisdk.screen.booking.supplier

import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.supplier.AvailabilityHandler
import com.karhoo.uisdk.screen.booking.domain.supplier.SortMethod

internal class SupplierListPresenter(view: SupplierListMVP.View, private val analytics: Analytics?)
    : BasePresenter<SupplierListMVP.View>(),
      SupplierListMVP.Presenter, AvailabilityHandler {

    private var currentVehicles: List<Quote> = mutableListOf()
    private var isExpanded: Boolean = false
    private var isPrebook: Boolean = false
    private var hasDestination: Boolean = false

    override var hasAvailability: Boolean = false
        set(value) {
            field = value
            shouldShowSupplierList()
        }

    init {
        attachView(view)
        this.currentVehicles = mutableListOf()
    }

    override fun handleAvailabilityError(snackbarConfig: SnackbarConfig) {
        view?.showSnackbarError(snackbarConfig)
    }

    override fun showMore() {
        analytics?.moreShown(currentVehicles, !isExpanded)
        isExpanded = !isExpanded
        view?.togglePanelState()
        view?.setChevronState(isExpanded)
    }

    override fun sortMethodChanged(sortMethod: SortMethod) {
        if (currentVehicles.isNotEmpty()) {
            analytics?.fleetsSorted(
                    currentVehicles[0].id,
                    sortMethod.name)
        }
        view?.setSortMethod(sortMethod)
        updateList()
    }

    private fun updateList() {
        if (isPrebook) {
            view?.updateList(currentVehicles)
        } else {
            val nearVehicles = currentVehicles.filter {
                it.vehicle.vehicleQta.highMinutes <= MAX_ACCEPTABLE_QTA
            }
            view?.updateList(nearVehicles)
        }
    }

    override fun vehiclesShown(quoteId: String, isExpanded: Boolean) {
        analytics?.fleetsShown(quoteId, if (isExpanded) 4 else 2)
    }

    override fun watchBookingStatus() = Observer<BookingStatus> { currentStatus ->
        currentStatus?.let {
            isPrebook = it.date != null
            hasDestination = currentStatus.destination != null

            view?.apply {
                prebook(isPrebook)
                setListVisibility(currentStatus.pickup, currentStatus.destination)
                destinationChanged(it)
            }

            if (!hasDestination) {
                shouldShowSupplierList()
            }
            updateList()
        }
    }

    override fun watchVehicles() = Observer<List<Quote>> { vehicleList ->
        vehicleList?.let {
            currentVehicles = it
            updateList()
        }
    }

    private fun shouldShowSupplierList() {
        when {
            !hasDestination -> view?.apply {
                if (isExpanded) {
                    showMore()
                }
                hideList()
            }
            hasAvailability -> view?.apply {
                showList()
            }
            else -> view?.apply {
                hideList()
                showNoAvailability()
            }
        }
    }

    companion object {
        private const val MAX_ACCEPTABLE_QTA = 20
    }

}
