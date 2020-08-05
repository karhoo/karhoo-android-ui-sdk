package com.karhoo.uisdk.util

import android.content.Context
import com.karhoo.uisdk.R
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore

fun formattedTripId(context: Context, tripId: String?): String {

    tripId?.let { return context.getString(R.string.trip_id, it) }

    val prefs = KarhooPreferenceStore.getInstance(context.applicationContext)
    val lastTripId = prefs.lastTrip?.displayTripId
    lastTripId?.let { return context.getString(R.string.last_trip_id, it) }

    return context.getString(R.string.last_trip_id_not_set)
}