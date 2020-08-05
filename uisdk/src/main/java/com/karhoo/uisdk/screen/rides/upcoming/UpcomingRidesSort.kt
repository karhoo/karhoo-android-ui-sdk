package com.karhoo.uisdk.screen.rides.upcoming

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.util.extension.orZero

class UpcomingRidesSort : Comparator<TripInfo> {

    override fun compare(tripOne: TripInfo?, tripTwo: TripInfo?): Int {
        val tripOneTime = tripOne?.dateScheduled?.time.orZero()
        val tripTwoTime = tripTwo?.dateScheduled?.time.orZero()
        return tripOneTime.compareTo(tripTwoTime)
    }

}