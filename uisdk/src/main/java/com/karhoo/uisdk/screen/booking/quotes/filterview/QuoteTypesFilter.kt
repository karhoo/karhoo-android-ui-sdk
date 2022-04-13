package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class QuoteTypesFilter (selectedTypes: ArrayList<String>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }
        return (quote.quoteType.ordinal == 0 && selectedTypes.contains(FIXED_TAG))
                ||
                (quote.quoteType.ordinal == 1 && selectedTypes.contains(ESTIMATED_TAG))
    }

    companion object {
        const val FIXED_TAG = "fixed"
        const val ESTIMATED_TAG = "estimated"
    }
}
