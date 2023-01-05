package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.uisdk.R

fun QuoteType.toLocalisedString(context: Context): String {
    return when (this) {
        QuoteType.FIXED -> context.getString(R.string.kh_uisdk_fixed_fare)
        QuoteType.ESTIMATED -> context.getString(R.string.kh_uisdk_estimated_fare)
        QuoteType.METERED -> context.getString(R.string.kh_uisdk_metered) + " " + context.getString(R.string.kh_uisdk_price)
    }
}

fun QuoteType.toLocalisedInfoString(context: Context): String {
    return when (this) {
        QuoteType.ESTIMATED -> context.getString(R.string.kh_uisdk_price_info_text_estimated)
        QuoteType.FIXED -> context.getString(R.string.kh_uisdk_price_info_text_fixed)
        QuoteType.METERED -> context.getString(R.string.kh_uisdk_price_info_text_metered)
    }
}
