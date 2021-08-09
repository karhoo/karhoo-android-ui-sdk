package com.karhoo.uisdk.screen.booking.quotes.capacity

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_capacity.view.*

class CapacityView @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), CapacityMVP.View {

    init {
        inflate(context, R.layout.uisdk_view_capacity, this)
    }

    override fun setCapacity(luggage: Int, people: Int, otherCapabilities: Int?) {
        luggageCapacityText.text = resources.getString(R.string.kh_uisdk_capacity, luggage.toString())
        peopleCapacityText.text = resources.getString(R.string.kh_uisdk_capacity, people.toString())

        otherCapabilities?.let {
            otherCapabilitiesText.text = resources.getString(R.string.kh_uisdk_extra_capabilities, it - DEFAULT_CAPABILITIES_NO)
            otherCapabilitiesText.visibility = VISIBLE
        } ?: run {
            otherCapabilitiesText.visibility = GONE
        }
    }

    override fun showCapacities(show: Boolean) {
        if (show) {
            capacitiesLayout.visibility = VISIBLE
        } else {
            capacitiesLayout.visibility = GONE
        }
    }

    companion object {
        private const val DEFAULT_CAPABILITIES_NO = 2
    }

}
