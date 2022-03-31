package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.uisdk.base.BasePresenter

class FilterDialogPresenter(view: FilterDialogContract.View) :
    BasePresenter<FilterDialogContract.View>(), FilterDialogContract.Presenter {

    lateinit var filterChain: FilterChain
    private var filterDelegate: FilterDelegate? = null

    init {
        attachView(view)
    }

    override fun createFilterChain(filterChain: FilterChain) {
        this.filterChain = filterChain
    }

    override fun createFilters() {
        view?.createFilters(filterChain)
    }

    override fun callFilterChanged() {
        filterDelegate?.onUserChangedFilter()?.let {
            view?.setNumberOfResultsAfterFilter(it)
        }
    }

    override fun resetFilters() {
        for(filter in filterChain.filters)
            filter.clearFilter()
    }

    interface FilterDelegate {
        fun onUserChangedFilter(): Int
    }

    fun setFilterDelegate(filterDelegate: FilterDelegate) {
        this.filterDelegate = filterDelegate
    }
}
