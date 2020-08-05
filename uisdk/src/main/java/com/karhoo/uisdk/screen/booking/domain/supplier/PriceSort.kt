package com.karhoo.uisdk.screen.booking.domain.supplier

import com.karhoo.sdk.api.model.QuoteV2
import java.util.Comparator

class PriceSort : Comparator<QuoteV2> {

    override fun compare(vehicleOne: QuoteV2, vehicleTwo: QuoteV2): Int {
        return if (vehicleOne.price.highPrice > 0 || vehicleTwo.price.highPrice > 0) {
            sortNormally(vehicleOne, vehicleTwo)
        } else sortMissingPrice(vehicleOne, vehicleTwo)
    }

    private fun sortNormally(vehicleOne: QuoteV2, vehicleTwo: QuoteV2): Int {
        return when {
            vehicleOne.price.highPrice <= 0 -> 1
            vehicleTwo.price.highPrice <= 0 -> -1
            vehicleOne.price.highPrice > vehicleTwo.price.highPrice -> 1
            vehicleOne.price.highPrice == vehicleTwo.price.highPrice -> sortMissingPrice(vehicleOne, vehicleTwo)
            else -> -1
        }
    }

    private fun sortBySupplierName(vehicleOne: QuoteV2, vehicleTwo: QuoteV2): Int {
        if (vehicleOne.fleet.name != vehicleTwo.fleet.name) {
            return vehicleOne.fleet.name.orEmpty().compareTo(vehicleTwo.fleet.name.orEmpty(), ignoreCase =
            true)
        }
        return sortByCategoryName(vehicleOne, vehicleTwo)
    }

    private fun sortMissingPrice(vehicleOne: QuoteV2, vehicleTwo: QuoteV2): Int {
        val vehicleOneQta: Int? = vehicleOne.vehicle.vehicleQta.highMinutes
        val vehicleTwoQta: Int? = vehicleTwo.vehicle.vehicleQta.highMinutes

        if (vehicleOneQta == null || vehicleTwoQta == null || vehicleOneQta == vehicleTwoQta) {
            return sortBySupplierName(vehicleOne, vehicleTwo)
        } else if (vehicleOneQta > vehicleTwoQta) {
            return 1
        }
        return -1
    }

    private fun sortByCategoryName(vehicleOne: QuoteV2, vehicleTwo: QuoteV2): Int {
        return vehicleOne.vehicle.vehicleClass.orEmpty().compareTo(vehicleTwo.vehicle
                                                                           .vehicleClass.orEmpty(),
                                                                   ignoreCase = true)
    }

}
