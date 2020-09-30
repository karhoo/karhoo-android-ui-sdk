package com.karhoo.uisdk.screen.booking.domain.supplier

import com.karhoo.sdk.api.model.Quote
import java.util.Comparator

class QtaSort : Comparator<Quote> {

    override fun compare(vehicleOne: Quote, vehicleTwo: Quote) =
            if ((vehicleOne.vehicle.vehicleQta != null || vehicleTwo.vehicle.vehicleQta != null)
                    && vehicleOne.vehicle.vehicleQta.highMinutes != vehicleTwo.vehicle.vehicleQta
                    .highMinutes) {
                (vehicleOne.vehicle.vehicleQta.highMinutes) - (vehicleTwo.vehicle.vehicleQta.highMinutes)
            } else {
                sortByPrice(vehicleOne, vehicleTwo)
            }

    private fun sortByPrice(vehicleOne: Quote, vehicleTwo: Quote) =
            vehicleOne.price.highPrice - vehicleTwo.price.highPrice

}
