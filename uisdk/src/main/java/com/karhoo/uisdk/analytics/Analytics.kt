package com.karhoo.uisdk.analytics

import android.location.Location
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.LoyaltyProgramme
import com.karhoo.sdk.api.model.LoyaltyStatus
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import java.util.Date

@Suppress("TooManyFunctions")
interface Analytics {

    fun userLocated(location: Location)

    fun bookingRequested(quoteId: String?)

    fun bookingSuccess(tripId: String, quoteId: String?, correlationId: String?)

    @Suppress("LongParameterList")
    fun bookingFailure(
        errorMessage: String,
        quoteId: String?,
        correlationId: String?,
        lastFourDigits: String,
        date: Date,
        amount: Int,
        currency: String,
        paymentMethodUsed: String,
    )

    fun tripStateChanged(tripState: TripInfo?)

    fun userCancelTrip(trip: TripInfo?)

    fun pickupAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationAddressSelected(locationDetails: LocationInfo, positionInAutocompleteList: Int)

    fun destinationPressed()

    fun prebookSet(date: Date, timezone: String)

    fun fleetsShown(quoteListId: String?, amountShown: Int)

    fun quoteListOpened(journeyDetails: JourneyDetails)

    fun prebookOpened()

    fun userCalledDriver(trip: TripInfo?)

    fun userCalledFleet(trip: TripInfo?)

    fun trackRide()

    fun bookingScreenOpened()

    fun checkoutOpened(quote: Quote)

    fun paymentSucceed()

    fun paymentFailed(
        errorMessage: String,
        quoteId: String?,
        lastFourDigits: String,
        date: Date,
        amount: Int,
        currency: String
    )

    fun cardAuthorisationFailure(
        errorMessage: String,
        quoteId: String?,
        lastFourDigits: String,
        date: Date,
        amount: Int,
        currency: String,
        paymentMethodUsed: String
    )

    fun tripPrebookConfirmation(tripInfo: TripInfo)

    fun trackTripOpened(tripInfo: TripInfo, isGuest: Boolean)

    fun pastTripsOpened()

    fun upcomingTripsOpened()

    fun trackTripClicked(tripInfo: TripInfo)

    fun contactFleetClicked(page: String, tripInfo: TripInfo)

    fun contactDriverClicked(page: String, tripInfo: TripInfo)

    fun loyaltyPreAuthFailure(
        slug: String?,
        errorMessage: String?,
        quoteId: String?,
        correlationId: String?,
        loyaltyMode: String,
        balance: Int?
    )

    fun loyaltyPreAuthSuccess(
        quoteId: String?,
        correlationId: String?,
        loyaltyMode: String,
        balance: Int?
    )
    @Suppress("LongParameterList")
    fun loyaltyStatusRequested(
        slug: String? = null,
        errorMessage: String?,
        quoteId: String?,
        correlationId: String?,
        loyaltyMode: String,
        balance: Int? = null,
        loyaltyStatus: LoyaltyStatus? = null,
        loyaltyProgramme: LoyaltyProgramme?,
    )

    fun cardAuthorisationSuccess(
        quoteId: String?
    )
}
