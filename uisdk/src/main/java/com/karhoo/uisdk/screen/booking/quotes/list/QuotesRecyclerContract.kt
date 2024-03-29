package com.karhoo.uisdk.screen.booking.quotes.list

import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod

interface QuotesRecyclerContract {

    interface View {

        fun setSortMethod(sortMethod: SortMethod)

        fun updateList(quoteList: List<Quote>, refreshAll: Boolean = false)

        fun prebook(isPrebook: Boolean)

        fun setListVisibility(visible: Boolean)

        fun showNoFleetsError(show: Boolean, isPrebook: Boolean)

        fun showSameAddressesError(show: Boolean)

        fun showNoCoverageError(show: Boolean)

        fun showNoAddressesError(show: Boolean)

        fun showNoResultsAfterFilterError(show: Boolean)

        fun watchQuoteListStatus(lifecycleOwner: LifecycleOwner, bookingQuotesViewModel:
        BookingQuotesViewModel)

        fun setQuotesLoaderVisibility(visible: Int)

    }
}
