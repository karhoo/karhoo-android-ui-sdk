package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.uisdk.R

fun PickupType.toLocalisedString(context: Context): String {
    return when (this) {
        PickupType.CURBSIDE -> context.getString(R.string.pickup_type_curbside)
        PickupType.MEET_AND_GREET -> context.getString(R.string.pickup_type_meet_and_greet)
        PickupType.STANDBY -> context.getString(R.string.pickup_type_standby)
        PickupType.DEFAULT -> ""
        PickupType.NOT_SET -> ""
    }
}
