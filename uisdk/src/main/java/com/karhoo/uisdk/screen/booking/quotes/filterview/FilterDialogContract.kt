package com.karhoo.uisdk.screen.booking.quotes.filterview

interface FilterDialogContract {
    interface View {
        fun createFilters(filterChain: FilterChain)

        fun setNumberOfResultsAfterFilter(size: Int)
    }

    interface Presenter {
        fun createFilterChain(filterChain: FilterChain)

        fun createFilters()

        fun callFilterChanged()

        fun resetFilters()

        fun applyFilters()
    }
}
