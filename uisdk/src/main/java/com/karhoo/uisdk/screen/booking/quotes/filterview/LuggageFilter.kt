package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class LuggageFilter(number: Int): NumberedFilter(number) {

    override fun meetsCriteria(quote: Quote): Boolean {
        return quote.vehicle.luggageCapacity >= currentNumber
    }
}
