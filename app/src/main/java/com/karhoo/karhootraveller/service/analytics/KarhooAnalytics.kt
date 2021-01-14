package com.karhoo.karhootraveller.service.analytics

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.ConnectivityManager
import android.os.BatteryManager
import com.karhoo.karhootraveller.KarhooApplication
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.analytics.Event
import com.karhoo.sdk.analytics.Payloader
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.rides.feedback.FeedbackAnswer
import com.karhoo.uisdk.util.DateUtil
import java.util.Date
import java.util.TimeZone

@Suppress("TooManyFunctions")
class KarhooAnalytics private constructor() : Analytics {

    override fun submitAdditionalFeedback(tripId: String, answers: List<FeedbackAnswer>) {

        val additionalFeedback = arrayListOf<MutableMap<String, Any>>()
        answers.forEach {
            val feedbackItem = mutableMapOf<String, Any>()
            feedbackItem["id"] = it.fieldId
            feedbackItem["rating"] = it.rating
            feedbackItem["comments"] = it.additionalComments
            additionalFeedback.add(feedbackItem)
        }

        AnalyticsManager.fireEvent(Event.ADDITIONAL_FEEDBACK_SUBMITTED,
                                   Payloader.Builder.builder
                                           .tripId(tripId)
                                           .additionalFeedback(additionalFeedback)
                                           .addSource("MOBILE")
                                           .build())
    }

    override fun submitRating(tripId: String, rating: Float) {
        AnalyticsManager.fireEvent(Event.TRIP_RATING_SUBMITTED,
                                   Payloader.Builder.builder
                                           .addTripRating(tripId, rating)
                                           .addSource("MOBILE")
                                           .build())
    }

    override fun bookingWithCallbackOpened() {
        AnalyticsManager.fireEvent(Event.BOOKING_WITH_CALLBACK)
    }

    override fun appOpened() {
        AnalyticsManager.fireEvent(Event.APP_OPENED)
    }

    override fun appClosed() {
        AnalyticsManager.fireEvent(Event.APP_CLOSED)
    }

    override fun appBackground(trip: TripInfo?) {
        val payloader = Payloader.Builder.builder
        trip?.let {
            if (trip.tripState != TripStatus.COMPLETED || trip.tripState != TripStatus.CANCELLED_BY_USER
                    || trip.tripState != TripStatus.CANCELLED_BY_DISPATCH) {
                payloader.tripId(it.tripId)
            }
        }
        AnalyticsManager.fireEvent(Event.APP_BACKGROUND, payloader.build())
    }

    override fun userLoggedIn(userInfo: UserInfo) {
        AnalyticsManager.fireEvent(Event.LOGGED_IN,
                                   Payloader.Builder.builder.addUserDetails(userInfo).build())
    }

    override fun userLoggedOut() {
        AnalyticsManager.fireEvent(Event.LOGGED_OUT)
    }

    override fun registrationStarted() {
        AnalyticsManager.fireEvent(Event.USER_REGISTER_STARTED)
    }

    override fun registrationComplete() {
        AnalyticsManager.fireEvent(Event.USER_REGISTER_COMPLETE)
    }

    override fun userLocated(location: Location) {
        AnalyticsManager.usersLatLng = Position(location.latitude,
                                                location.longitude)
    }

    override fun bookingRequested(tripDetails: TripInfo, outboundTripId: String?) {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = KarhooApplication.instance.registerReceiver(null, ifilter)
        val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level / scale.toFloat()

        val connectivityManager = KarhooApplication.instance
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetworkInfo.typeName

        AnalyticsManager.fireEvent(Event.BOOKING_REQUESTED,
                                   Payloader.Builder.builder
                                           .addBookingRequest(batteryPct, network, tripDetails,
                                                              outboundTripId)
                                           .build())
    }

    override fun tripStateChanged(tripState: TripInfo?) {
        AnalyticsManager.fireEvent(Event.TRIP_STATE_CHANGED,
                                   Payloader.Builder.builder
                                           .addTripState(tripState?.tripState.toString())
                                           .tripId(tripState?.tripId)
                                           .build())
    }

    override fun userCancelTrip(trip: TripInfo?) {
        AnalyticsManager.fireEvent(Event.USER_CANCEL_TRIP, Payloader.Builder
                .builder
                .tripId(trip?.tripId)
                .build())
    }

    override fun amountAddressesShown(amount: Int) {
        AnalyticsManager.fireEvent(Event.AMOUNT_ADDRESSES,
                                   Payloader.Builder.builder.displayedAddresses(amount).build())
    }

    override fun pickupAddressSelected(locationInfo: LocationInfo, positionInAutocompleteList: Int) {
        AnalyticsManager.fireEvent(Event.PICKUP_SELECTED,
                                   Payloader.Builder.builder
                                           .addressSelected(locationInfo.displayAddress, positionInAutocompleteList)
                                           .build())
    }

    override fun destinationAddressSelected(locationInfo: LocationInfo, positionInAutocompleteList: Int) {
        AnalyticsManager.fireEvent(Event.DESTINATION_SELECTED,
                                   Payloader.Builder.builder
                                           .addressSelected(locationInfo.displayAddress, positionInAutocompleteList)
                                           .build())
    }

    override fun destinationPressed() {
        AnalyticsManager.fireEvent(Event.DESTINATION_PRESSED)
    }

    override fun reverseGeo() {
        AnalyticsManager.fireEvent(Event.REVERSE_GEO)
    }

    override fun reverseGeoResponse(locationInfo: LocationInfo) {
        AnalyticsManager.fireEvent(Event.REVERSE_GEO_RESPONSE,
                                   Payloader.Builder.builder.reverseGeoResponse(locationInfo).build())
    }

    override fun currentLocationPressed() {
        AnalyticsManager.fireEvent(Event.CURRENT_LOCATION_PRESSED)
    }

    override fun prebookSet(date: Date, timeZone: String) {
        AnalyticsManager.fireEvent(Event.PREBOOK_SET,
                                   Payloader.Builder.builder
                                           .prebookSet(DateUtil.getDateAndTimeFormat(KarhooApplication.instance, date, TimeZone.getTimeZone(timeZone)))
                                           .build())
    }

    override fun vehicleSelected(vehicle: String, quoteId: String?) {
        AnalyticsManager.fireEvent(Event.VEHICLE_SELECTED,
                                   Payloader.Builder.builder
                                           .vehicleSelected(vehicle, quoteId)
                                           .build())
    }

    override fun fleetsShown(quoteListId: String?, amountShown: Int) {
        AnalyticsManager.fireEvent(Event.FLEET_LIST_SHOWN,
                                   Payloader.Builder.builder
                                           .fleetsShown(amountShown, quoteListId)
                                           .build())
    }

    override fun moreShown(currentVehicles: List<Quote>?, isExpanded: Boolean) {
        var quoteListId: String? = null
        if (currentVehicles != null && currentVehicles.isNotEmpty()) {
            quoteListId = currentVehicles[0].id
        }
        AnalyticsManager.fireEvent(Event.MORE_SUPPLIERS,
                                   Payloader.Builder.builder
                                           .fleetsShown(if (!isExpanded) {
                                               4
                                           } else {
                                               2
                                           }, quoteListId)
                                           .build())
    }

    override fun fleetsSorted(quoteListId: String?, sortType: String) {
        AnalyticsManager.fireEvent(Event.FLEET_SORTED,
                                   Payloader.Builder.builder
                                           .fleetsSorted(sortType, quoteListId)
                                           .build())
    }

    override fun prebookOpened() {
        AnalyticsManager.fireEvent(Event.PREBOOK_OPENED)
    }

    override fun locationServiceRejected() {
        AnalyticsManager.fireEvent(Event.REJECT_LOCATION_SERVICES)
    }

    override fun cardAddedSuccessfully() {
        AnalyticsManager.fireEvent(Event.CARD_REGISTERED_SUCCESSFULLY)
    }

    override fun cardAddingFailed() {
        AnalyticsManager.fireEvent(Event.CARD_REGISTERED_FAILED)
    }

    override fun termsReviewed() {
        AnalyticsManager.fireEvent(Event.TERMS_REVIEWED)
    }

    override fun userCalledDriver(trip: TripInfo?) {
        AnalyticsManager.fireEvent(Event.USER_CALLED_DRIVER, Payloader.Builder
                .builder
                .tripId(trip?.tripId)
                .build())
    }

    override fun userCalledFleet(trip: TripInfo?) {
        AnalyticsManager.fireEvent(Event.USER_CALLED_FLEET, Payloader.Builder
                .builder
                .tripId(trip?.tripId)
                .build())
    }

    override fun userEnteredTextSearch(search: String?) {
        AnalyticsManager.fireEvent(Event.USER_ENTERED_ADDRESS, Payloader.Builder.builder
                .userEnteredAddressSearch(search.orEmpty())
                .build())
    }

    override fun bookReturnRide() {
        AnalyticsManager.fireEvent(Event.BOOK_RETURN_RIDE)
    }

    override fun trackRide() {
        AnalyticsManager.fireEvent(Event.TRACK_RIDE)
    }

    override fun userPositionChanged(tripState: TripStatus, location: Location) {
        AnalyticsManager.fireEvent(Event.USER_POSITION_CHANGED, Payloader.Builder.builder
                .addTripState(tripState.name)
                .build())

    }

    override fun userProfileEditPressed() {
        AnalyticsManager.fireEvent(Event.USER_PROFILE_EDIT_PRESSED)
    }

    override fun userProfileDiscardPressed() {
        AnalyticsManager.fireEvent(Event.USER_PROFILE_DISCARD_PRESSED)
    }

    override fun userProfileSavePressed() {
        AnalyticsManager.fireEvent(Event.USER_PROFILE_SAVE_PRESSED)

    }

    override fun userProfileUpdateSuccess(userInfo: UserInfo) {
        AnalyticsManager.fireEvent(Event.USER_PROFILE_UPDATE_SUCCESS, Payloader.Builder.builder
                .addUserDetails(userInfo)
                .build())
    }

    override fun userProfileUpdateFailed() {
        AnalyticsManager.fireEvent(Event.USER_PROFILE_UPDATE_FAILED)
    }

    private object Holder {
        val INSTANCE = KarhooAnalytics()
    }

    companion object {
        val INSTANCE: KarhooAnalytics by lazy { Holder.INSTANCE }
    }

}
