package com.karhoo.uisdk.screen.booking.domain.address

import android.os.Parcelable
import com.karhoo.sdk.api.model.LocationInfo
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class JourneyDetails(var pickup: LocationInfo?,
                          var destination: LocationInfo?,
                          var date: DateTime?) : Parcelable
