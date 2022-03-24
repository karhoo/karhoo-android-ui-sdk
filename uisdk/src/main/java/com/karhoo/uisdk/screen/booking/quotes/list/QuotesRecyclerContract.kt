package com.karhoo.uisdk.screen.booking.quotes.list

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.quotes.category.Category

interface QuotesRecyclerContract {

    interface View {

        fun setSortMethod(sortMethod: SortMethod)

        fun updateList(quoteList: List<Quote>)

        fun prebook(isPrebook: Boolean)

        fun setListVisibility(visible: Boolean)

        fun showNoFleetsError(show: Boolean)

        fun showSameAddressesError(show: Boolean)

        fun showNoCoverageError(show: Boolean)

        fun showNoAddressesError(show: Boolean)

        fun watchQuoteListStatus(lifecycleOwner: LifecycleOwner, bookingQuotesViewModel:
        BookingQuotesViewModel)

        fun watchCategories(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel)

        fun setQuotesLoaderVisibility(visible: Int)

    }

    interface Presenter {

        fun watchCategories(): Observer<List<Category>>

    }
}
