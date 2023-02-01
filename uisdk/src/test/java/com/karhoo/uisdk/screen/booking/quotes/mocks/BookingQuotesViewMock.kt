package com.karhoo.uisdk.screen.booking.quotes.mocks

import android.graphics.drawable.Drawable
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingVehicleDetailsMVP

class BookingQuotesViewMock: BookingVehicleDetailsMVP.View {
    var cancellationMinutesText: String? = null
    var capacityLuggage: Int? = null
    var capacityPeople: Int? = null
    var catgText: String? = null
    var showCancellation: Boolean? = null
    var getDrawableResourceCalled = false

    override fun setCancellationText(text: String) {
        this.cancellationMinutesText = text
    }

    override fun setCapacity(luggage: Int?, people: Int?, capabilitiesCount: Int) {
        this.capacityLuggage = luggage
        this.capacityPeople = people
    }

    override fun showCancellationText(show: Boolean) {
        this.showCancellation = show
    }

    override fun getDrawableResource(id: Int): Drawable? {
        this.getDrawableResourceCalled = true
        return null
    }
}
