package com.karhoo.uisdk.screen.rides.upcoming.card

import androidx.annotation.StringRes
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.ScheduledDateView

interface UpcomingRideCardMVP {

    interface Presenter {
        fun call()

        fun track()

        fun selectDetails()

        fun bindDate()
    }

    interface View : ScheduledDateView {
        fun callDriver(number: String)

        fun callText(@StringRes contactText: Int)

        fun trackTrip(trip: TripInfo)

        fun goToDetails(trip: TripInfo)

        fun displayTrackDriverButton()

        fun hideTrackDriverButton()
    }

}
