package com.karhoo.uisdk.screen.booking.domain.supplier

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.uisdk.util.extension.orZero
import java.util.Comparator

class QtaSort : Comparator<QuoteV2> {

    override fun compare(vehicleOne: QuoteV2, vehicleTwo: QuoteV2) =
            if ((vehicleOne.vehicle.vehicleQta != null || vehicleTwo.vehicle.vehicleQta != null)
                    && vehicleOne.vehicle.vehicleQta.highMinutes != vehicleTwo.vehicle.vehicleQta
                    .highMinutes) {
                (vehicleOne.vehicle.vehicleQta.highMinutes) - (vehicleTwo.vehicle.vehicleQta.highMinutes)
            } else {
                sortByPrice(vehicleOne, vehicleTwo)
            }

    private fun sortByPrice(vehicleOne: QuoteV2, vehicleTwo: QuoteV2) =
            vehicleOne.price.highPrice - vehicleTwo.price.highPrice

}
