package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class VehicleClassFilter(selectedTypes: ArrayList<MultiSelectData>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }
        return selectedTypes.map { it.text.lowercase() }.find { tag ->
            quote.vehicle.vehicleTags.contains(tag)
        } != null
    }

    companion object {
        const val EXECUTIVE = "executive"
        const val LUXURY = "luxury"
    }
}
