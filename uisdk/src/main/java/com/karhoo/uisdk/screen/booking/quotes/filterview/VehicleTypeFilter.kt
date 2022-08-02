package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class VehicleTypeFilter(selectedTypes: ArrayList<MultiSelectData>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }
        return quote.vehicle.vehicleType?.lowercase() in selectedTypes.map { it.text.lowercase() }
    }

    companion object {
        const val STANDARD = "standard"
        const val BERLINE = "berline"
        const val VAN = "van"
        const val MOTO = "moto"
        const val BIKE = "bike"
    }
}
