package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class VehicleEcoFilter(selectedTypes: ArrayList<MultiSelectData>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }

        selectedTypes.forEach { data ->
            if(quote.vehicle.vehicleTags.map { tag -> tag.lowercase() }.any { tag ->
                    tag == data.fixedTag
                })
                return true
        }
        return false
    }

    companion object {
        const val ELECTRIC = "electric"
        const val HYBRID = "hybrid"
    }
}
