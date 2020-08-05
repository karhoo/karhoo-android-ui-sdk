package com.karhoo.uisdk.screen.booking.domain.address

import android.os.Parcelable
import com.karhoo.sdk.api.model.Position
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
data class JourneyInfo(val origin: Position?,
                       val destination: Position?,
                       val date: DateTime?) : Parcelable
