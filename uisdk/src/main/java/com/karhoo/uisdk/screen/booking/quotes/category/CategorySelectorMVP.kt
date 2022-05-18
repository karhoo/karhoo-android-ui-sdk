package com.karhoo.uisdk.screen.booking.quotes.category

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider

interface CategorySelectorMVP {

    interface View {

        fun setCategories(categories: List<Category>)

        fun bindViewToData(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel, journeyDetailsStateViewModel: JourneyDetailsStateViewModel)

        fun bindAvailability(availabilityProvider: AvailabilityProvider)

        fun hideCategories()

        fun showCategories()

    }

    interface Presenter {

        fun setVehicleCategory(vehicleType: String)

        fun subscribeToAvailableCategories(): Observer<List<Category>>

        fun subscribeToJourneyDetails(): Observer<JourneyDetails>

    }

}
