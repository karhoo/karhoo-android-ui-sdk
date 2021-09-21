package com.karhoo.uisdk.util.extension

import android.content.Context
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.R

fun Vehicle.classToLocalisedString() = if (vehicleClass.isNullOrEmpty()) {
    "Vehicle: "
} else {
    "${vehicleClass.toCharArray().first().toUpperCase()}${vehicleClass.subSequence(1, vehicleClass.length).toString().toLowerCase()}: "
}
