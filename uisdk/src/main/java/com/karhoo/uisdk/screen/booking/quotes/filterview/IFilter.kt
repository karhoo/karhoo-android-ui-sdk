package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

interface IFilter {
    fun meetsCriteria(quote: Quote): Boolean {
        return true
    }

    fun clearFilter() {}
}
