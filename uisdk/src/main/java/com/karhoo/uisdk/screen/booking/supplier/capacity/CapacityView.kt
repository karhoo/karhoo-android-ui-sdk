package com.karhoo.uisdk.screen.booking.supplier.capacity

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_capacity.view.luggageCapacityText
import kotlinx.android.synthetic.main.uisdk_view_capacity.view.peopleCapacityText

class CapacityView @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), CapacityMVP.View {

    init {
        inflate(context, R.layout.uisdk_view_capacity, this)
    }

    override fun setCapacity(luggage: Int, people: Int) {
        luggageCapacityText.text = resources.getString(R.string.capacity, luggage.toString())
        peopleCapacityText.text = resources.getString(R.string.capacity, people.toString())
    }

}