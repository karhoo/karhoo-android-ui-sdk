package com.karhoo.uisdk.screen.booking.supplier

import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.supplier.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.supplier.SortMethod

interface SupplierListMVP {

    interface View {

        fun bindAvailability(availabilityProvider: AvailabilityProvider)

        fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?)

        fun destinationChanged(bookingStatus: BookingStatus)

        fun updateList(quoteList: List<Quote>)

        fun setSortMethod(sortMethod: SortMethod)

        fun togglePanelState()

        fun setChevronState(isExpanded: Boolean)

        fun prebook(isPrebook: Boolean)

        fun showList()

        fun hideList()

        fun showNoAvailability()

        fun hideNoAvailability()

    }

    interface Presenter {

        fun showMore()

        fun vehiclesShown(quoteId: String, isExpanded: Boolean)

        fun watchBookingStatus(): Observer<BookingStatus>

        fun watchVehicles(): Observer<List<Quote>>

        fun sortMethodChanged(sortMethod: SortMethod)

    }
}
