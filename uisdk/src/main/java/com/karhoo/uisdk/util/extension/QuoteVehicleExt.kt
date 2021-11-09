package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.uisdk.R

fun QuoteVehicle.categoryToLocalisedString(context: Context): String? {
    return when (this.vehicleClass?.uppercase()) {
        "MPV" -> context.getString(R.string.kh_uisdk_mpv)
        "SALOON" -> context.getString(R.string.kh_uisdk_saloon)
        "EXEC" -> context.getString(R.string.kh_uisdk_exec)
        "TAXI" -> context.getString(R.string.kh_uisdk_taxi)
        "MOTO" -> context.getString(R.string.kh_uisdk_moto)
        "ELECTRIC" -> context.getString(R.string.kh_uisdk_electric)
        else -> vehicleClass
    }
}
