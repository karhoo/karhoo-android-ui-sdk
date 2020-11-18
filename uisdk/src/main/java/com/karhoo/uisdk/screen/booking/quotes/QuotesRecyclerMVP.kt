package com.karhoo.uisdk.screen.booking.quotes

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.quotes.category.Category

interface QuotesRecyclerMVP {

    interface View {

        fun setSortMethod(sortMethod: SortMethod)

        fun updateList(quoteList: List<Quote>)

        fun prebook(isPrebook: Boolean)

        fun setListVisibility(visible: Boolean)

        fun watchQuoteListStatus(lifecycleOwner: LifecycleOwner, bookingQuotesViewModel:
        BookingQuotesViewModel)

        fun watchCategories(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel)

        fun setQuotesLoaderVisibility(visible: Int)

    }

    interface Presenter {

        fun watchCategories(): Observer<List<Category>>

    }
}
