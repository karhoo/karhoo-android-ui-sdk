package com.karhoo.uisdk.screen.booking.domain.quotes

import com.karhoo.sdk.api.model.Quote
import java.util.Comparator

class PriceSort : Comparator<Quote> {

    override fun compare(vehicleOne: Quote, vehicleTwo: Quote): Int {
        return if (vehicleOne.price.highPrice > 0 || vehicleTwo.price.highPrice > 0) {
            sortNormally(vehicleOne, vehicleTwo)
        } else sortMissingPrice(vehicleOne, vehicleTwo)
    }

    private fun sortNormally(vehicleOne: Quote, vehicleTwo: Quote): Int {
        return when {
            vehicleOne.price.highPrice <= 0 -> 1
            vehicleTwo.price.highPrice <= 0 -> -1
            vehicleOne.price.highPrice > vehicleTwo.price.highPrice -> 1
            vehicleOne.price.highPrice == vehicleTwo.price.highPrice -> sortMissingPrice(vehicleOne, vehicleTwo)
            else -> -1
        }
    }

    private fun sortByQuoteName(vehicleOne: Quote, vehicleTwo: Quote): Int {
        if (vehicleOne.fleet.name != vehicleTwo.fleet.name) {
            return vehicleOne.fleet.name.orEmpty().compareTo(vehicleTwo.fleet.name.orEmpty(), ignoreCase =
            true)
        }
        return sortByCategoryName(vehicleOne, vehicleTwo)
    }

    private fun sortMissingPrice(vehicleOne: Quote, vehicleTwo: Quote): Int {
        val vehicleOneQta: Int? = vehicleOne.vehicle.vehicleQta.highMinutes
        val vehicleTwoQta: Int? = vehicleTwo.vehicle.vehicleQta.highMinutes

        if (vehicleOneQta == null || vehicleTwoQta == null || vehicleOneQta == vehicleTwoQta) {
            return sortByQuoteName(vehicleOne, vehicleTwo)
        } else if (vehicleOneQta > vehicleTwoQta) {
            return 1
        }
        return -1
    }

    private fun sortByCategoryName(vehicleOne: Quote, vehicleTwo: Quote): Int {
        return vehicleOne.vehicle.vehicleClass.orEmpty().compareTo(vehicleTwo.vehicle
                                                                           .vehicleClass.orEmpty(),
                                                                   ignoreCase = true)
    }

}
