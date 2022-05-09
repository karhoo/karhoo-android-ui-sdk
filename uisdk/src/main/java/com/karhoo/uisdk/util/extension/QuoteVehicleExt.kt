package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.QuoteVehicle
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
