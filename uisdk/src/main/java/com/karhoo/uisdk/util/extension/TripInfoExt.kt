package com.karhoo.uisdk.util.extension

import com.karhoo.sdk.api.model.TripInfo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

fun TripInfo.convertToDateTime(): DateTime? {
    var time: DateTime? = null
    dateScheduled?.let {
        if (it.time > 0L) {
            val timeZone = if (origin?.timezone.isNullOrBlank()) {
                DateTimeZone.UTC
            } else {
                DateTimeZone.forID(origin?.timezone)
            }
            time = DateTime(it.time, timeZone)
        }
    }

    return time
}
