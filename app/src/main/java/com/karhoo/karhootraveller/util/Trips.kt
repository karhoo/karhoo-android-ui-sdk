package com.karhoo.karhootraveller.util

import android.content.Context
import com.karhoo.karhootraveller.KarhooApplication
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.service.preference.KarhooPreferenceStore

fun formattedTripId(context: Context, tripId: String?): String {

    tripId?.let { return context.getString(R.string.trip_id, it) }

    val prefs = KarhooPreferenceStore.getInstance(KarhooApplication.instance)
    val lastTripId = prefs.lastTrip?.displayTripId
    lastTripId?.let { return context.getString(R.string.last_trip_id, it) }

    return context.getString(R.string.last_trip_id_not_set)
}