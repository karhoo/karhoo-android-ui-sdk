package com.karhoo.uisdk.screen.booking.quotes

import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod

interface QuotesListMVP {

    interface View {

        fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?)

        fun destinationChanged(bookingStatus: BookingStatus)

        fun updateList(quoteList: List<Quote>)

        fun setSortMethod(sortMethod: SortMethod)

        fun togglePanelState()

        fun setChevronState(isExpanded: Boolean)

        fun prebook(isPrebook: Boolean)

        fun showList()

        fun hideList(): Boolean

        fun showNoAvailability()

        fun showNoResultsText(show: Boolean)

        fun hideNoAvailability()

        fun showSnackbarError(snackbarConfig: SnackbarConfig)

    }

    interface Presenter {

        fun showMore()

        fun vehiclesShown(quoteId: String, isExpanded: Boolean)

        fun watchBookingStatus(): Observer<BookingStatus>

        fun watchVehicles(): Observer<List<Quote>>

        fun sortMethodChanged(sortMethod: SortMethod)

    }

    interface Actions : ErrorView

    interface QuoteValidityListener {
        fun isValidUntil(timestamp: Long)
    }
}
