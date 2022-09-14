package com.karhoo.farechoice.service.analytics

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.karhoo.samples.uisdk.dropin.KarhooApplication
import com.karhoo.sdk.analytics.AnalyticsManager
import com.karhoo.sdk.analytics.Event
import com.karhoo.sdk.analytics.Payloader
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.util.DateUtil
import java.util.Date
import java.util.TimeZone

@Suppress("TooManyFunctions")
class KarhooAnalytics private constructor() : Analytics {

    override fun userLocated(location: Location) {
        AnalyticsManager.usersLatLng = Position(location.latitude,
            location.longitude)
    }

    override fun bookingFailure(
        quoteId: String?,
        correlationId: String?,
        errorMessage: String,
        lastFourDigits: String,
        paymentMethodUsed: String,
        date: Date,
        amount: Int,
        currency: String
    ) {
        val builder = Payloader.Builder.builder.bookingFailure(
            correlationId = correlationId,
            quoteId = quoteId,
            errorMessage = errorMessage,
            paymentMethodUsed = paymentMethodUsed,
            lastFourDigits = lastFourDigits,
            date = date,
            amount = amount,
            currency = currency
        ).build()

        AnalyticsManager.fireEvent(Event.BOOKING_FAILURE, builder)
    }

    override fun bookingRequested(quoteId: String) {
        AnalyticsManager.fireEvent(Event.BOOKING_REQUESTED,
            Payloader.Builder.builder
                .bookingRequested(quoteId = quoteId)
                .build())
    }

    override fun bookingScreenOpened() {
        // Event to be added soon
    }

    override fun checkoutOpened(quote: Quote) {
        // Event to be added soon
        AnalyticsManager.fireEvent(Event.CHECKOUT_SCREEN,
            Payloader.Builder.builder
                .checkoutOpened(quoteId = quote.id)
                .build())
    }

    override fun contactDriverClicked(page: String, tripInfo: TripInfo) {
        // Event to be added soon
    }

    override fun contactFleetClicked(page: String, tripInfo: TripInfo) {
        // Event to be added soon
    }

    override fun tripStateChanged(tripState: TripInfo?) {
        AnalyticsManager.fireEvent(Event.TRIP_STATE_CHANGED,
            Payloader.Builder.builder
                .addTripState(tripState?.tripState.toString())
                .tripId(tripState?.tripId)
                .build())
    }

    override fun upcomingTripsOpened() {
        // Event to be added soon
    }

    override fun userCancelTrip(trip: TripInfo?) {
        AnalyticsManager.fireEvent(Event.USER_CANCEL_TRIP, Payloader.Builder
            .builder
            .tripId(trip?.tripId)
            .build())
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

    @SuppressLint("NewApi")
    private fun getNetworkNameFromCapabilities(connectivityManager: ConnectivityManager?): String {
        connectivityManager?.run {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                return when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        WIFI_TYPE
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        MOBILE_TYPE
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                        VPN_TYPE
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        ETHERNET_TYPE
                    }
                    else -> UNKNOWN_TYPE // Returns connection type
                }
            }
        }
        return UNKNOWN_TYPE
    }

    @Suppress("DEPRECATION")
    private fun getNetworkNameFromActiveNetworkInfo(connectivityManager: ConnectivityManager?): String {
        connectivityManager?.run {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        WIFI_TYPE
                    }
                    ConnectivityManager.TYPE_MOBILE -> {
                        MOBILE_TYPE
                    }
                    ConnectivityManager.TYPE_VPN -> {
                        VPN_TYPE
                    }
                    ConnectivityManager.TYPE_ETHERNET -> {
                        ETHERNET_TYPE
                    }
                    else -> UNKNOWN_TYPE
                }
            }
        }
        return UNKNOWN_TYPE
    }

    @Suppress("DEPRECATION")
    fun getConnectionType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getNetworkNameFromCapabilities(connectivityManager)
        } else {
            getNetworkNameFromActiveNetworkInfo(connectivityManager)
        }
    }


    override fun destinationPressed() {
        AnalyticsManager.fireEvent(Event.DESTINATION_PRESSED)
    }

    override fun prebookSet(date: Date, timeZone: String) {
        AnalyticsManager.fireEvent(Event.PREBOOK_SET,
            Payloader.Builder.builder
                .prebookSet(DateUtil.getDateAndTimeFormat(KarhooApplication.instance, date, TimeZone.getTimeZone(timeZone)))
                .build())
    }

    override fun paymentFailed(
        errorMessage: String,
        quoteId: String?,
        lastFourDigits: String,
        date: Date,
        amount: Int,
        currency: String
    ) {
        val builder = Payloader.Builder.builder.cardAuthorisationFailure(
            errorMessage = errorMessage,
            quoteId = quoteId,
            lastFourDigits = lastFourDigits,
            paymentMethodUsed = "",
            date = date,
            amount = amount,
            currency = currency
        ).build()

        AnalyticsManager.fireEvent(Event.CARD_AUTHORISATION_FAILURE,builder)
    }

    override fun cardAuthorisationSuccess(quoteId: String?) {
        AnalyticsManager.fireEvent(
            Event.CARD_AUTHORISATION_SUCCESS,
            Payloader.Builder.builder
                .cardAuthorisationSuccess(quoteId)
                .build()
        )
    }

    override fun quoteListOpened(journeyDetails: JourneyDetails) {
        // Will be implemented later
    }

    override fun fleetsShown(quoteListId: String?, amountShown: Int) {
        AnalyticsManager.fireEvent(Event.FLEET_LIST_SHOWN,
            Payloader.Builder.builder
                .fleetsShown(amountShown, quoteListId)
                .build())
    }

    override fun loyaltyPreAuthFailure(
        quoteId: String?,
        correlationId: String?,
        loyaltyMode: String,
        errorSlug: String?,
        errorMessage: String?
    ) {
        val builder = Payloader.Builder.builder.loyaltyPreAuthFailure(
            errorSlug = errorSlug,
            errorMessage =  errorMessage,
            quoteId = quoteId,
            correlationId = correlationId,
            loyaltyMode = loyaltyMode
        ).build()

        AnalyticsManager.fireEvent(Event.LOYALTY_PREAUTH_FAILURE, builder)
    }

    override fun pastTripsOpened() {
        // Event to be added soon
    }

    override fun bookingSuccess(tripId: String, quoteId: String?, correlationId: String?) {
        val builder = Payloader.Builder.builder.bookingSuccess(
            tripId = tripId,
            correlationId = correlationId,
            quoteId = quoteId,
        ).build()

        AnalyticsManager.fireEvent(Event.BOOKING_SUCCESS, builder)
    }

    override fun cardAuthorisationFailure(
        quoteId: String?,
        errorMessage: String,
        lastFourDigits: String,
        paymentMethodUsed: String,
        date: Date,
        amount: Int,
        currency: String
    ) {
        val builder = Payloader.Builder.builder.cardAuthorisationFailure(
            quoteId = quoteId,
            errorMessage = errorMessage,
            paymentMethodUsed = paymentMethodUsed,
            lastFourDigits = lastFourDigits,
            date = date,
            amount = amount,
            currency = currency
        ).build()

        AnalyticsManager.fireEvent(Event.CARD_AUTHORISATION_FAILURE,builder)
    }

    override fun loyaltyPreAuthSuccess(
        quoteId: String?,
        correlationId: String?,
        loyaltyMode: String
    ) {
        val builder = Payloader.Builder.builder.loyaltyPreAuthSuccess(
            quoteId = quoteId,
            correlationId = correlationId,
            loyaltyMode = loyaltyMode
        ).build()

        AnalyticsManager.fireEvent(Event.LOYALTY_PREAUTH_SUCCESS, builder)
    }

    override fun loyaltyStatusRequested(
        quoteId: String?,
        correlationId: String?,
        loyaltyName: String?,
        loyaltyMode: String,
        loyaltyStatus: LoyaltyStatus?,
        errorSlug: String?,
        errorMessage: String?
    ) {
        val builder = Payloader.Builder.builder.loyaltyStatusRequested(
            errorSlug = errorSlug,
            errorMessage = errorMessage,
            quoteId = quoteId,
            loyaltyMode = loyaltyMode,
            correlationId = correlationId,
            loyaltyStatus = loyaltyStatus,
            loyaltyName = loyaltyName
        ).build()

        AnalyticsManager.fireEvent(Event.LOYALTY_STATUS_REQUESTED, builder)
    }

    override fun tripPrebookConfirmation(tripInfo: TripInfo) {
        val builder = Payloader.Builder.builder.tripId(tripInfo.tripId).build()

        AnalyticsManager.fireEvent(Event.PREBOOK_CONFIRMATION, builder)
    }

    override fun paymentSucceed() {
        // Event to be added soon
    }

    override fun prebookOpened() {
        AnalyticsManager.fireEvent(Event.PREBOOK_OPENED)
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

    override fun trackRide() {
        AnalyticsManager.fireEvent(Event.TRACK_RIDE)
    }

    override fun trackTripClicked(tripInfo: TripInfo) {
        // Event to be added soon
    }

    override fun trackTripOpened(tripInfo: TripInfo, isGuest: Boolean) {
        // Event to be added soon
    }

    private object Holder {
        val INSTANCE = KarhooAnalytics()
    }

    companion object {
        const val MOBILE_TYPE = "MOBILE"
        const val WIFI_TYPE = "WIFI"
        const val ETHERNET_TYPE = "ETHERNET"
        const val VPN_TYPE = "VPN"
        const val UNKNOWN_TYPE = "UNKNOWN"
        val INSTANCE: KarhooAnalytics by lazy { Holder.INSTANCE }
    }

}
