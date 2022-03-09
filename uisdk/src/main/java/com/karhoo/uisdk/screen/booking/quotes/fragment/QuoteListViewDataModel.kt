package com.karhoo.uisdk.screen.booking.quotes.fragment

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo

data class QuoteListViewDataModel(
    var quotes: List<Quote>?,
    var vehicles: List<Vehicle>?,
    var bookingInfo: BookingInfo? = null
)
