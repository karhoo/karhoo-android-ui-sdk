package com.karhoo.uisdk.analytics

import android.location.Location
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.screen.rides.feedback.FeedbackAnswer
import java.util.Date

@Suppress("TooManyFunctions")
interface Analytics {

    fun appOpened()

    fun appClosed()

    fun appBackground(trip: TripInfo?)

    fun userLoggedIn(userInfo: UserInfo)

    fun userLoggedOut()

    fun registrationStarted()

    fun registrationComplete()

    fun userLocated(location: Location)

    fun bookingWithCallbackOpened()

    fun bookingRequested(tripDetails: TripInfo, outboundTripId: String?)

    fun tripStateChanged(tripState: TripInfo?)

    fun etaDisplayed(eta: Int, tripId: String)

    fun detaDisplayed(deta: Int, tripId: String)

    fun userCancelTrip(trip: TripInfo?)

    fun amountAddressesShown(amount: Int)

    fun pickupAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationPressed()

    fun reverseGeo()

    fun reverseGeoResponse(locationDetails: LocationInfo)

    fun currentLocationPressed()

    fun prebookSet(date: Date, timezone: String)

    fun vehicleSelected(vehicle: String, quoteId: String?)

    fun fleetsShown(quoteListId: String?, amountShown: Int)

    fun moreShown(currentVehicles: List<QuoteV2>?, isExpanded: Boolean)

    fun fleetsSorted(quoteListId: String?, sortType: String)

    fun prebookOpened()

    fun locationServiceRejected()

    fun cardAddedSuccessfully()

    fun cardAddingFailed()

    fun termsReviewed()

    fun userCalledDriver(trip: TripInfo?)

    fun userCalledFleet(trip: TripInfo?)

    fun userEnteredTextSearch(search: String?)

    fun bookReturnRide()

    fun trackRide()

    fun userPositionChanged(trip: TripStatus, location: Location)

    fun submitRating(tripId: String, rating: Float)

    fun submitAdditionalFeedback(tripId: String, answers: List<FeedbackAnswer>)

    fun userProfileEditPressed()

    fun userProfileDiscardPressed()

    fun userProfileSavePressed()

    fun userProfileUpdateSuccess(userInfo: UserInfo)

    fun userProfileUpdateFailed()
}
