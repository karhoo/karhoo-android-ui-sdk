package com.karhoo.uisdk.screen.booking.quotes.extendedcapabilities

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.base.BaseRecyclerView

class CapabilityAdapter(private val context: Context) : BaseRecyclerAdapter<Capability, CapabilityItem>() {
    override fun onCreateItemView(parent: ViewGroup, viewType: Int): CapabilityItem {
        return CapabilityItem(context)
    }

    override fun onBindViewHolder(holder: BaseRecyclerView<CapabilityItem>, position: Int) {
        val capability = items[position]

        val iconId = getCapabilityIcon(capability.type)
        val text = getCapabilityText(capability.type, capability.count)

        if (iconId != null) {
            holder.view.bind(iconId, text)
        }
    }

    private fun getCapabilityText(type: String, count: Int?): String {
        return when (type) {
            GPS_TRACKING -> context.getString(R.string.kh_uisdk_gps_tracking)
            FLIGHT_TRACKING -> context.getString(R.string.kh_uisdk_flight_tracking)
            TRAIN_TRACKING -> context.getString(R.string.kh_uisdk_train_tracking)
            PASSENGERS_MAX -> String.format(context.getString(R.string.kh_uisdk_passengers_max), count)
            BAGGAGE_MAX -> String.format(context.getString(R.string.kh_uisdk_baggage_max), count)
            else -> ""
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getCapabilityIcon(type: String): Int? {
        return when (type) {
            GPS_TRACKING -> R.drawable.kh_uisdk_ic_capabilities_gps
            FLIGHT_TRACKING -> R.drawable.kh_uisdk_ic_capabilities_flight_tracking
            TRAIN_TRACKING -> R.drawable.kh_uisdk_ic_capabilities_train_tracking
            PASSENGERS_MAX -> R.drawable.kh_uisdk_ic_capabilities_passengers
            BAGGAGE_MAX -> R.drawable.kh_uisdk_ic_capabilities_baggage
            else -> null
        }

        return null
    }

    companion object {
         const val GPS_TRACKING = "gps_tracking"
         const val FLIGHT_TRACKING = "flight_tracking"
         const val TRAIN_TRACKING = "train_tracking"
        const val PASSENGERS_MAX = "passengers_max"
        const val BAGGAGE_MAX = "baggage_max"
    }
}
