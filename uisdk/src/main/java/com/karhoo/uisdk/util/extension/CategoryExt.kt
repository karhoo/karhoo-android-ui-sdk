package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.quotes.category.Category

fun Category.toLocalizedString(context: Context): String {
    return when (this.categoryName.uppercase()) {
        "MPV" -> context.getString(R.string.kh_uisdk_mpv)
        "SALOON" -> context.getString(R.string.kh_uisdk_saloon)
        "EXEC" -> context.getString(R.string.kh_uisdk_exec)
        "TAXI" -> context.getString(R.string.kh_uisdk_taxi)
        "MOTO" -> context.getString(R.string.kh_uisdk_moto)
        "ELECTRIC" -> context.getString(R.string.kh_uisdk_electric)
        else -> categoryName
    }
}
