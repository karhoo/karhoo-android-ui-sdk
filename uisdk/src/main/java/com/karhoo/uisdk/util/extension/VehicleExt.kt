package com.karhoo.uisdk.util.extension

import com.karhoo.sdk.api.model.Vehicle

fun Vehicle.classToLocalisedString() = if (vehicleClass.isNullOrEmpty()) {
    "Vehicle: "
} else {
    "${vehicleClass.toCharArray().first().toUpperCase()}${vehicleClass.subSequence(1, vehicleClass.length).toString().toLowerCase()}: "
}
