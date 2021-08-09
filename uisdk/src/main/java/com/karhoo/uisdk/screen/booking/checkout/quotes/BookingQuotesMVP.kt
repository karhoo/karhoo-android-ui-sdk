package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Spannable
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities.Capability

interface BookingQuotesMVP {

    interface View {
        fun setCapacity(luggage: Int, people: Int)
        fun setCancellationText(text: String)
        fun setCategoryText(text: String)
        fun showCancellationText(show: Boolean)
        fun getDrawableResource(id: Int): Drawable?
        fun setCapabilities(capabilities: List<Capability>)
    }

    interface Presenter {
        fun checkCancellationSLAMinutes(context: Context, serviceCancellation: ServiceCancellation?, isPrebook: Boolean)
        fun capitalizeCategory(category: String)
        fun createTagsString(tags: List<String>, shortVersion: Boolean): Spannable
    }
}
