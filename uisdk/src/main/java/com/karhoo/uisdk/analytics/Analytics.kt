package com.karhoo.uisdk.analytics

import android.location.Location
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripInfo
import java.util.Date

@Suppress("TooManyFunctions")
interface Analytics {

    fun userLocated(location: Location)

    fun bookingRequested(tripDetails: TripInfo, outboundTripId: String?)

    fun tripStateChanged(tripState: TripInfo?)

    fun userCancelTrip(trip: TripInfo?)

    fun pickupAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationPressed()

    fun prebookSet(date: Date, timezone: String)

    fun fleetsShown(quoteListId: String?, amountShown: Int)

    fun prebookOpened()

    fun userCalledDriver(trip: TripInfo?)

    fun userCalledFleet(trip: TripInfo?)

    fun trackRide()
}
