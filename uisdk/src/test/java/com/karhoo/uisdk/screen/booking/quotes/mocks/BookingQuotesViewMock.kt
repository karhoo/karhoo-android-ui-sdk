package com.karhoo.uisdk.screen.booking.quotes.mocks

import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesMVP

class BookingQuotesViewMock: BookingQuotesMVP.View {
    var cancellationMinutesText: String? = null
    var capacityLuggage: Int? = null
    var capacityPeople: Int? = null
    var catgText: String? = null
    var showCancellation: Boolean? = null

    override fun setCancellationText(text: String) {
        this.cancellationMinutesText = text
    }

    override fun setCapacity(luggage: Int, people: Int) {
        this.capacityLuggage = luggage
        this.capacityPeople = people
    }

    override fun setCategoryText(text: String) {
        this.catgText = text
    }

    override fun showCancellationText(show: Boolean) {
        this.showCancellation = show
    }
}