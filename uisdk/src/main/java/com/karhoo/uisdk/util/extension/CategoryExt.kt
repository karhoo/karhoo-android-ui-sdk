package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.quotes.category.Category

fun Category.toLocalizedString(context: Context): String {
    return when (this.categoryName) {
        "MPV" -> context.getString(R.string.kh_uisdk_mpv)
        "Saloon" -> context.getString(R.string.kh_uisdk_saloon)
        "Exec" -> context.getString(R.string.kh_uisdk_exec)
        "Taxi" -> context.getString(R.string.kh_uisdk_taxi)
        "Moto" -> context.getString(R.string.kh_uisdk_moto)
        "Electric" -> context.getString(R.string.kh_uisdk_electric)
        else -> categoryName
    }
}
