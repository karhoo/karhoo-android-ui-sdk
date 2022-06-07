package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class VehicleExtrasFilter(selectedTypes: ArrayList<MultiSelectData>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }

        selectedTypes.forEach { data ->
            if(quote.vehicle.vehicleTags.map { tag -> tag.lowercase() }.any { tag ->
                    data.fixedTag?.let {
                        tag == it
                    }?: kotlin.run {
                        tag == data.text
                    }
            })
                return true
        }
        return false
    }

    companion object {
        const val CHILD_SEAT = "child-seat"
        const val TAXI = "taxi"
        const val WHEELCHAIR = "wheelchair"
    }
}
