package com.karhoo.uisdk.screen.rides.past

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.SimpleErrorMessageView

interface PastRidesMVP {

    interface Presenter {

        fun getPastRides()

    }

    interface View : SimpleErrorMessageView {

        fun showEmptyState()

        fun showPastRides(pastRides: List<TripInfo>)

    }


}
