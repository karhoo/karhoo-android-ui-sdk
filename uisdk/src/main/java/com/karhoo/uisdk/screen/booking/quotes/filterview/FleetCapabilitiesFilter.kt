package com.karhoo.uisdk.screen.booking.quotes.filterview

import com.karhoo.sdk.api.model.Quote

class FleetCapabilitiesFilter(selectedTypes: ArrayList<MultiSelectData>) : MultiSelectFilter(selectedTypes) {

    override fun meetsCriteria(quote: Quote): Boolean {
        if (selectedTypes.size == 0) {
            return true
        }

        return quote.fleet.capabilities.map { tag -> tag.lowercase() }.containsAll(selectedTypes.map { tag -> tag.fixedTag })
    }

    companion object {
        const val DRIVER_DETAILS = "driver_details"
        const val FLIGHT_TRACKING = "flight_tracking"
        const val GPS_TRACKING = "gps_tracking"
        const val TRAIN_TRACKING = "train_tracking"
        const val VEHICLE_DETAILS = "vehicle_details"
    }
}
