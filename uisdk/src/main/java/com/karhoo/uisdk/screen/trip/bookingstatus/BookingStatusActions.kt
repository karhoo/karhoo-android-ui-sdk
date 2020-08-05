package com.karhoo.uisdk.screen.trip.bookingstatus

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.base.listener.ErrorView

interface BookingStatusActions : ErrorView {

    fun goToCleanBooking()

    fun goToPrefilledBooking(trip: TripInfo)

    fun gotoRideDetails(trip: TripInfo)

    fun updateRideDetails(trip: TripInfo)

}
