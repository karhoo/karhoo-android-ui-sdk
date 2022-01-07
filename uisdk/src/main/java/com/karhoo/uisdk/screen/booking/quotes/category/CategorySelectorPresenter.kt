package com.karhoo.uisdk.screen.booking.quotes.category

import androidx.lifecycle.Observer
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider

internal class CategorySelectorPresenter(view: CategorySelectorMVP.View)
    : BasePresenter<CategorySelectorMVP.View>(), CategorySelectorMVP.Presenter {

    private var currentAvailability: List<Category> = mutableListOf()
    private var hasDestination: Boolean = false
    var availabilityProvider: AvailabilityProvider? = null

    init {
        attachView(view)
        subscribeToAvailableCategories()
        subscribeToBookingStatus()
    }

    override fun setVehicleCategory(vehicleType: String) {
        availabilityProvider?.filterVehicleListByCategory(vehicleType)
    }

    override fun subscribeToAvailableCategories() = Observer<List<Category>> { categories ->
        if (shouldUpdateVehicleClassAvailability(currentAvailability, categories.orEmpty())) {
            currentAvailability = categories.orEmpty()
            setCategoriesAvailable()
        }
    }

    override fun subscribeToBookingStatus() = Observer<BookingInfo> { bookingStatus ->
        hasDestination = bookingStatus?.destination != null
        setCategoriesAvailable()
    }

    private fun setCategoriesAvailable() {
        view?.setCategories(currentAvailability)
        if (hasDestination && currentAvailability.isNotEmpty()) {
            view?.showCategories()
        } else {
            view?.hideCategories()
        }
    }

    private fun shouldUpdateVehicleClassAvailability(currentAvailability: List<Category>?,
                                                     newAvailability: List<Category>): Boolean {
        return when (currentAvailability) {
            null -> true
            else -> availabilityIsDifferent(currentAvailability, newAvailability)
        }
    }

    private fun availabilityIsDifferent(currentAvailability: List<Category>, newAvailability: List<Category>): Boolean {
        if (newAvailability.size != currentAvailability.size) {
            return true
        }

        for (i in currentAvailability.indices) {
            val newVehicleClass = newAvailability[i]
            val currentVehicleClass = currentAvailability[i]
            if (newVehicleClass != currentVehicleClass) {
                return true
            }
        }
        return false
    }
}
