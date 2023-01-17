package com.karhoo.uisdk.screen.booking.quotes.capacity

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_capacity.view.*

class CapacityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), CapacityMVP.View {

    init {
        inflate(context, R.layout.uisdk_view_capacity, this)
    }

    override fun setCapacity(luggage: Int?, people: Int?, otherCapabilities: Int?) {
        luggage?.let {
            luggageCapacityText.text =
                resources.getString(R.string.kh_uisdk_capacity, luggage.toString())
            luggageCapacityText.visibility = VISIBLE
            briefcaseIcon.visibility = VISIBLE
        }?: kotlin.run {
            luggageCapacityText.visibility = GONE
            briefcaseIcon.visibility = GONE
        }

        people?.let {
            peopleCapacityText.text = resources.getString(R.string.kh_uisdk_capacity, people.toString())
            peopleCapacityText.visibility = VISIBLE
            passengerIcon.visibility = VISIBLE
        }?: kotlin.run {
            peopleCapacityText.visibility = GONE
            passengerIcon.visibility = GONE
        }


//        val capabilitiesCount = otherCapabilities?.minus(DEFAULT_CAPABILITIES_NO)
//
//        if (capabilitiesCount != null && capabilitiesCount > 0) {
//            otherCapabilitiesText.text =
//                resources.getString(R.string.kh_uisdk_extra_capabilities, capabilitiesCount)
//            otherCapabilitiesText.visibility = VISIBLE
//        } else {
//            otherCapabilitiesText.visibility = GONE
//        }
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
