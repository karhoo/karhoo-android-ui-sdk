package com.karhoo.uisdk.screen.booking.quotes.list

import android.view.View
import androidx.lifecycle.Observer
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.quotes.category.Category

class QuotesRecyclerPresenter constructor(view: QuotesRecyclerMVP.View)
    : BasePresenter<QuotesRecyclerMVP.View>(), QuotesRecyclerMVP.Presenter {

    init {
        attachView(view)
    }

    override fun watchCategories() = Observer<List<Category>> { categories ->
        if (categories != null) {
            val filteredCats = categories.filter { it.isAvailable }
            setLoaderVisibility(filteredCats)
        }
    }

    private fun setLoaderVisibility(filteredCats: List<Category>) {
        if (filteredCats.isEmpty()) {
            view?.setQuotesLoaderVisibility(View.VISIBLE)
        } else {
            view?.setQuotesLoaderVisibility(View.GONE)
        }
    }
}
