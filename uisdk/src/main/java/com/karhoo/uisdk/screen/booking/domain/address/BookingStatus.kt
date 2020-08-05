package com.karhoo.uisdk.screen.booking.domain.address

import com.karhoo.sdk.api.model.LocationInfo
import org.joda.time.DateTime

data class BookingStatus(var pickup: LocationInfo?,
                         var destination: LocationInfo?,
                         var date: DateTime?)