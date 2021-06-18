package com.karhoo.uisdk.screen.booking.booking.quotes

import android.content.Context
import com.karhoo.sdk.api.model.ServiceCancellation

interface BookingQuotesMVP {

    interface View {
        fun setCapacity(luggage: Int, people: Int)
        fun setCancellationText(text: String)
        fun setCategoryText(text: String)
        fun showCancellationText(show: Boolean)
    }

    interface Presenter {
        fun checkCancellationSLAMinutes(context: Context, serviceCancellation: ServiceCancellation?, isPrebook: Boolean)
        fun capitalizeCategory(category: String)
    }
}
