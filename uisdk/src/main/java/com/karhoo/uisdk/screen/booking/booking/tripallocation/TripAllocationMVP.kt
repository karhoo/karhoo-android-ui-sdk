package com.karhoo.uisdk.screen.booking.booking.tripallocation

import com.karhoo.sdk.api.model.TripInfo

interface TripAllocationMVP {

    interface Presenter {
        fun cancelTrip()

        fun handleAllocationDelay()

        fun waitForAllocation(trip: TripInfo)

        fun unsubscribeFromUpdates()
    }

    interface View {
        fun showAllocationDelayAlert()

        fun displayBookingFailed(fleetName: String)

        fun displayTripCancelledSuccess()

        fun goToTrip(trip: TripInfo)

        fun displayWebTracking(followCode: String)

        fun showCallToCancelDialog(number: String, quote: String)
    }

    interface Actions {
        fun onBookingCancelledOrFinished()
    }
}
