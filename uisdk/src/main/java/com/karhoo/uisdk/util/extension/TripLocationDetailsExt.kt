package com.karhoo.uisdk.util.extension

import com.karhoo.sdk.api.model.Address
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripLocationInfo

fun TripLocationInfo.toSimpleLocationInfo() =
        LocationInfo(
                address = Address(displayAddress = displayAddress),
                position = position,
                placeId = placeId,
                poiType = poiType,
                timezone = timezone)
