package com.karhoo.uisdk.screen.rideplanning

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyInfo
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel

object RidePlanningStorage {
    var quote: Quote? = null
    var tripDetails: TripInfo? = null
    var outboundTripId: String? = null
    var journeyInfo: JourneyInfo? = null
    var passengerDetails: PassengerDetails? = null
    var loyaltyInfo: LoyaltyInfo? = null
    var bookingComments: String? = ""
    var bookingMetadata: HashMap<String, String>? = null

    lateinit var journeyDetailsStateViewModel: JourneyDetailsStateViewModel

    lateinit var bookingRequestStateViewModel: BookingRequestStateViewModel
}