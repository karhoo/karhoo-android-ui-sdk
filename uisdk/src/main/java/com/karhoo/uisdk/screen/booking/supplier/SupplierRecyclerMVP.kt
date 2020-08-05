package com.karhoo.uisdk.screen.booking.supplier

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.uisdk.screen.booking.booking.supplier.BookingSupplierViewModel
import com.karhoo.uisdk.screen.booking.domain.supplier.SortMethod
import com.karhoo.uisdk.screen.booking.supplier.category.CategoriesViewModel
import com.karhoo.uisdk.screen.booking.supplier.category.Category

interface SupplierRecyclerMVP {

    interface View {

        fun setSortMethod(sortMethod: SortMethod)

        fun updateList(quoteList: List<QuoteV2>)

        fun prebook(isPrebook: Boolean)

        fun setListVisibility(visible: Boolean)

        fun watchQuoteListStatus(lifecycleOwner: LifecycleOwner, bookingSupplierViewModel:
        BookingSupplierViewModel)

        fun watchCategories(lifecycleOwner: LifecycleOwner, categoriesViewModel: CategoriesViewModel)

        fun setQuotesLoaderVisibility(visible: Int)

    }

    interface Presenter {

        fun watchCategories(): Observer<List<Category>>

    }
}