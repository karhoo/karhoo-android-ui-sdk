package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class VehicleTypeFilter(selectedTypes: ArrayList<String>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }
        return quote.vehicle.vehicleType?.lowercase() in selectedTypes
    }

}
