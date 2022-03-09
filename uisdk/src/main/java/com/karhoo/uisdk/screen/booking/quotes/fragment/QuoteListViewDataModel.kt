package com.karhoo.uisdk.screen.booking.quotes.fragment

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails

data class QuoteListViewDataModel(
    var quotes: List<Quote>?,
    var vehicles: List<Vehicle>?,
    var journeyDetails: JourneyDetails? = null
)
