package com.karhoo.uisdk.screen.booking.checkout.tripallocation

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo

interface TripAllocationContract {

    interface Presenter {
        fun cancelTrip()

        fun handleAllocationDelay(trip: TripInfo)

        fun waitForAllocation(trip: TripInfo)

        fun unsubscribeFromUpdates()
    }

    interface View {
        fun showAllocationDelayAlert(trip: TripInfo)

        fun displayBookingFailed(fleetName: String)

        fun displayTripCancelledSuccess()

        fun goToTrip(trip: TripInfo)

        fun displayWebTracking(followCode: String)

        fun showCallToCancelDialog(number: String, quote: String, karhooError: KarhooError?)
    }

    interface Widget {
        fun onActivityResult(tripInfo: TripInfo)
    }

    interface Actions {
        fun onBookingCancelledOrFinished()
    }
}
