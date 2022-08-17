package com.karhoo.uisdk.screen.booking.domain.quotes

import com.karhoo.sdk.api.model.VehicleMapping
import com.karhoo.sdk.api.model.VehicleMappings
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.quotes.QuotesService
import com.squareup.picasso.Picasso

object VehicleMappingsProvider {
    private var vehicleMappings: VehicleMappings? = null
    private lateinit var quotesService: QuotesService

    fun setup(quotesService: QuotesService) {
        this.quotesService = quotesService
        retrieveVehicleMappings()
    }

    fun retrieveVehicleMappings(callback: ((VehicleMappings?) -> Unit)? = null) {
        if (vehicleMappings == null) {
            quotesService.getVehicleMappings().execute {
                when (it) {
                    is Resource.Success -> {
                        vehicleMappings = it.data

                        vehicleMappings?.mappings?.let { mappings ->
                            for (mapping: VehicleMapping in mappings) {
                                Picasso.get().load(mapping.vehicleImagePNG).noFade().fetch()
                            }
                        }

                        callback?.invoke(vehicleMappings)
                    }
                    is Resource.Failure -> {
                        callback?.invoke(null)
                        //will retry at the next quote list loading
                    }
                }
            }
        }
    }

    fun getVehicleMappings(): VehicleMappings? {
        return vehicleMappings
    }
}
