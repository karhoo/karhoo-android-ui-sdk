package com.karhoo.uisdk.screen.booking.quotes

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo

data class QuoteListViewDataModel(
    val quotes: List<Quote>?,
    val vehicles: List<Vehicle>?,
    val bookingInfo: BookingInfo? = null
)
