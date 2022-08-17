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
        const val STANDARD = "STANDARD"
        const val BUS = "BUS"
        const val MPV = "MPV"
        const val MOTO = "MOTO"
    }
}
