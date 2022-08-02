package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class QuoteTypesFilter (selectedTypes: ArrayList<MultiSelectData>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }
        return (quote.quoteType.ordinal == 0 && selectedTypes.any { it.fixedTag?.contains(FIXED_TAG) == true })
                ||
                ((quote.quoteType.ordinal == 1 || quote.quoteType.ordinal == 2) && selectedTypes.any { it.fixedTag?.contains(ESTIMATED_TAG) == true || it.fixedTag?.contains(
                    METERED_TAG) == true })
    }

    companion object {
        const val FIXED_TAG = "fixed"
        const val ESTIMATED_TAG = "estimated"
        const val METERED_TAG = "metered"
    }
}
