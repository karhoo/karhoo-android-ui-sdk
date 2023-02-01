package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.Spannable
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.util.VehicleTags

interface BookingVehicleDetailsMVP {

    interface View {
        fun setCapacity(luggage: Int?, people: Int?, capabilitiesCount: Int)
        fun setCancellationText(text: String)
        fun showCancellationText(show: Boolean)
        fun getDrawableResource(id: Int): Drawable?
    }

    interface Presenter {
        fun checkCancellationSLAMinutes(context: Context, serviceCancellation: ServiceCancellation?, isPrebook: Boolean)
        fun createTagsString(tags: List<VehicleTags>, resources: Resources, shortVersion: Boolean): Spannable
    }
}
