package com.karhoo.uisdk.screen.booking.booking.tripallocation

import com.karhoo.sdk.api.model.TripInfo

interface TripAllocationMVP {

    interface Presenter {

        fun cancelTrip()

        fun waitForAllocation(trip: TripInfo)

    }

    interface View {

        fun displayBookingFailed(fleetName: String)

        fun displayTripCancelledSuccess()

        fun goToTrip(trip: TripInfo)

        fun displayWebTracking(followCode: String)

        fun showCallToCancelDialog(number: String, supplier: String)

    }

    interface Actions {
        fun onBookingCancelledOrFinished()
    }
}