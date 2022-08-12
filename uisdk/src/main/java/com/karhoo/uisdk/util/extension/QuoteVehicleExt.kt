package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.VehicleMapping
import com.karhoo.sdk.api.model.VehicleMappings
import com.karhoo.uisdk.R

fun QuoteVehicle.typeToLocalisedString(context: Context): String? {
    return when (this.vehicleType?.uppercase()) {
        "STANDARD" -> context.getString(R.string.kh_uisdk_vehicle_standard)
        "MPV" -> context.getString(R.string.kh_uisdk_vehicle_mpv)
        "BUS" -> context.getString(R.string.kh_uisdk_vehicle_bus)
        "MOTO" -> context.getString(R.string.kh_uisdk_vehicle_moto)
        else -> vehicleType
    }
}

fun QuoteVehicle.getCorrespondingLogoMapping(vehicleMappings: VehicleMappings): VehicleMapping? {
    /**
     * Try and find the first precise mapping between the vehicle tags of the vehicle
     * and the standard mappings
     */
    var specificMapping = vehicleMappings.mappings?.firstOrNull {
        it.vehicleType.equals(this.vehicleType) &&
                (it.vehicleTags?.size == this.vehicleTags.size
                        && it.vehicleTags?.toSet() == this.vehicleTags.toSet())
    }

    /**
     * Fallback if exact match hasn't been found
     * Try and find the first matching mapping for an individual vehicle tag
     */
    if (specificMapping == null) {
        specificMapping = vehicleMappings.mappings?.firstOrNull { vehicleMapping ->
            this.vehicleTags.forEach { tag ->
                if(vehicleMapping.vehicleTags?.size == 1 &&
                            vehicleMapping.vehicleTags?.contains(tag) == true &&
                            vehicleMapping.vehicleType.equals(this.vehicleType)) {
                    return@firstOrNull true
                }
            }

            return@firstOrNull false
        }
    }

    if (specificMapping != null) {
        return specificMapping
    }

    /**
     * Default mapping if no tags are present on the vehicle
     * The matching will be done by the vehicle type
     */
    val defaultMapping = vehicleMappings.mappings?.firstOrNull {
        it.vehicleType.equals(this.vehicleType) && it.vehicleTags.isNullOrEmpty()
    }

    if (defaultMapping != null) {
        return defaultMapping
    }

    /**
     * Generic fallback to a default standard mapping
     */
    val fallbackMapping = vehicleMappings.mappings?.firstOrNull {
        it.vehicleType.equals("*")
    }

    return fallbackMapping
}
