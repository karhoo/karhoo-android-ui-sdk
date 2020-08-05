package com.karhoo.uisdk.base

import com.karhoo.sdk.api.model.TripInfo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

interface ScheduledDateView {

    fun displayDate(date: DateTime)

    fun displayNoDateAvailable()

}

class ScheduledDateViewBinder {

    fun bind(view: ScheduledDateView, trip: TripInfo) {
        trip.dateScheduled?.let {
            if (it.time > 0L) {
                val timeZone = if (trip.origin?.timezone.isNullOrBlank()) {
                    DateTimeZone.UTC
                } else {
                    DateTimeZone.forID(trip.origin?.timezone)
                }
                val time = DateTime(it.time, timeZone)
                view.displayDate(time)
            } else {
                view.displayNoDateAvailable()
            }
        } ?: run {
            view.displayNoDateAvailable()
        }
    }
}