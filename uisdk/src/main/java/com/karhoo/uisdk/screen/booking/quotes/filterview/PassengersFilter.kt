package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class PassengersFilter(number: Int): NumberedFilter(number) {

    override fun meetsCriteria(quote: Quote): Boolean {
        quote.vehicle.passengerCapacity?.let {
            return it >= currentNumber
        }?: kotlin.run {
            return 0 == currentNumber
        }
    }
}
