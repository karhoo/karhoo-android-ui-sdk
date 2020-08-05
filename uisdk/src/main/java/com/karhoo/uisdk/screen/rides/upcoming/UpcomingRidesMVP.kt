package com.karhoo.uisdk.screen.rides.upcoming

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.SimpleErrorMessageView

interface UpcomingRidesMVP {

    interface Presenter {

        fun getUpcomingRides()

    }

    interface View : SimpleErrorMessageView {

        fun showEmptyState()

        fun showUpcomingRides(upcomingRides: List<TripInfo>)

    }

}
