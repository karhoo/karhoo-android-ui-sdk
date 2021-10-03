package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.R

fun Vehicle.categoryToLocalisedString(context: Context): String {

    if (vehicleClass.isNullOrEmpty()) {
        R.string.kh_uisdk_vehicle_label
    } else {
        return when (this.vehicleClass.uppercase()) {
            "MPV" -> context.getString(R.string.kh_uisdk_mpv)
            "SALOON" -> context.getString(R.string.kh_uisdk_saloon)
            "EXECUTIVE" -> context.getString(R.string.kh_uisdk_exec)
            "TAXI" -> context.getString(R.string.kh_uisdk_taxi)
            "MOTO" -> context.getString(R.string.kh_uisdk_moto)
            "ELECTRIC" -> context.getString(R.string.kh_uisdk_electric)
            else -> vehicleClass
        }
    }

    return vehicleClass
}
