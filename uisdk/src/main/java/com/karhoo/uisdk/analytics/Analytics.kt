package com.karhoo.uisdk.analytics

import android.location.Location
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.screen.rides.feedback.FeedbackAnswer
import java.util.Date

@Suppress("TooManyFunctions")
interface Analytics {

    @Deprecated("ToBeDeleted")
    fun appOpened()
    @Deprecated("ToBeDeleted")
    fun appClosed()
    @Deprecated("ToBeDeleted")
    fun appBackground(trip: TripInfo?)
    @Deprecated("ToBeDeleted")
    fun userLoggedIn(userInfo: UserInfo)
    @Deprecated("ToBeDeleted")
    fun userLoggedOut()
    @Deprecated("ToBeDeleted")
    fun registrationStarted()
    @Deprecated("ToBeDeleted")
    fun registrationComplete()

    fun userLocated(location: Location)
    @Deprecated("ToBeDeleted")
    fun bookingWithCallbackOpened()

    fun bookingRequested(tripDetails: TripInfo, outboundTripId: String?)

    fun tripStateChanged(tripState: TripInfo?)

    fun userCancelTrip(trip: TripInfo?)
    @Deprecated("ToBeDeleted")
    fun amountAddressesShown(amount: Int)

    fun pickupAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationPressed()
    @Deprecated("ToBeDeleted")
    fun reverseGeo()
    @Deprecated("ToBeDeleted")
    fun currentLocationPressed()

    fun prebookSet(date: Date, timezone: String)
    @Deprecated("ToBeDeleted")
    fun vehicleSelected(vehicle: String, quoteId: String?)

    fun fleetsShown(quoteListId: String?, amountShown: Int)
    @Deprecated("ToBeDeleted")
    fun moreShown(currentVehicles: List<Quote>?, isExpanded: Boolean)
    @Deprecated("ToBeDeleted")
    fun fleetsSorted(quoteListId: String?, sortType: String)

    fun prebookOpened()
    @Deprecated("ToBeDeleted")
    fun locationServiceRejected()
    @Deprecated("ToBeDeleted")
    fun cardAddedSuccessfully()
    @Deprecated("ToBeDeleted")
    fun cardAddingFailed()
    @Deprecated("ToBeDeleted")
    fun termsReviewed()

    fun userCalledDriver(trip: TripInfo?)

    fun userCalledFleet(trip: TripInfo?)
    @Deprecated("ToBeDeleted")
    fun userEnteredTextSearch(search: String?)

    fun trackRide()
    @Deprecated("ToBeDeleted")
    fun userPositionChanged(trip: TripStatus, location: Location)
    @Deprecated("ToBeDeleted")
    fun submitRating(tripId: String, rating: Float)
    @Deprecated("ToBeDeleted")
    fun submitAdditionalFeedback(tripId: String, answers: List<FeedbackAnswer>)
    @Deprecated("ToBeDeleted")
    fun userProfileEditPressed()
    @Deprecated("ToBeDeleted")
    fun userProfileDiscardPressed()
    @Deprecated("ToBeDeleted")
    fun userProfileSavePressed()
    @Deprecated("ToBeDeleted")
    fun userProfileUpdateSuccess(userInfo: UserInfo)
    @Deprecated("ToBeDeleted")
    fun userProfileUpdateFailed()
}
