package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.R

fun Vehicle.categoryToLocalisedString(context: Context): String {

    if (vehicleClass.isNullOrEmpty()) {
        R.string.kh_uisdk_vehicle_label
    } else {
        return when (this.vehicleClass) {
            "MPV" -> context.getString(R.string.kh_uisdk_mpv)
            "Saloon" -> context.getString(R.string.kh_uisdk_saloon)
            "Exec" -> context.getString(R.string.kh_uisdk_exec)
            "Taxi" -> context.getString(R.string.kh_uisdk_taxi)
            "Moto" -> context.getString(R.string.kh_uisdk_moto)
            "Electric" -> context.getString(R.string.kh_uisdk_electric)
            else -> vehicleClass
        }
    }

    return vehicleClass
}
