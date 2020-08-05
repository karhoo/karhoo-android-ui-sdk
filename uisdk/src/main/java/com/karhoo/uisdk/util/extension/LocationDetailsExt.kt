package com.karhoo.uisdk.util.extension

import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripLocationInfo

fun LocationInfo.toTripLocationDetails() = TripLocationInfo(
        displayAddress = this.displayAddress,
        position = this.position,
        placeId = this.placeId,
        poiType = this.poiType,
        timezone = this.timezone
                                                           )