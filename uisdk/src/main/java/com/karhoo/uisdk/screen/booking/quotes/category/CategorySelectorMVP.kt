package com.karhoo.uisdk.screen.booking.quotes.category

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider

interface CategorySelectorMVP {

    interface View {

        fun setCategories(categories: List<Category>)

        fun bindViewToData(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel, bookingStatusStateViewModel: BookingStatusStateViewModel)

        fun bindAvailability(availabilityProvider: AvailabilityProvider)

        fun hideCategories()

        fun showCategories()

    }

    interface Presenter {

        fun setVehicleCategory(vehicleType: String)

        fun subscribeToAvailableCategories(): Observer<List<Category>>

        fun subscribeToBookingStatus(): Observer<BookingInfo>

    }

}
