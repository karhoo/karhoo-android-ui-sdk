package com.karhoo.uisdk.screen.booking.quotes.capacity

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.uisdk.R

class CapacityView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), CapacityMVP.View {

    private lateinit var peopleCapacityText: TextView
    private lateinit var luggageCapacityText: TextView
    private lateinit var briefcaseIcon: ImageView
    private lateinit var passengerIcon: ImageView
    private lateinit var capacitiesLayout: ConstraintLayout

    init {
        inflate(context, R.layout.uisdk_view_capacity, this)
        peopleCapacityText = findViewById(R.id.peopleCapacityText)
        luggageCapacityText = findViewById(R.id.luggageCapacityText)
        briefcaseIcon = findViewById(R.id.briefcaseIcon)
        passengerIcon = findViewById(R.id.passengerIcon)
        capacitiesLayout = findViewById(R.id.capacitiesLayout)
    }

    override fun setCapacity(luggage: Int?, people: Int?, otherCapabilities: Int?) {
        luggage?.let {
            luggageCapacityText.text =
                resources.getString(R.string.kh_uisdk_capacity, luggage.toString())
            luggageCapacityText.visibility = VISIBLE
            briefcaseIcon.visibility = VISIBLE
            luggageCapacityText.contentDescription = resources.getString(R.string.kh_uisdk_baggage_max, luggage)
        }?: kotlin.run {
            luggageCapacityText.visibility = GONE
            briefcaseIcon.visibility = GONE
        }

        people?.let {
            peopleCapacityText.text = resources.getString(R.string.kh_uisdk_capacity, people.toString())
            peopleCapacityText.visibility = VISIBLE
            passengerIcon.visibility = VISIBLE
            peopleCapacityText.contentDescription = resources.getString(R.string.kh_uisdk_passengers_max, people)
        }?: kotlin.run {
            peopleCapacityText.visibility = GONE
            passengerIcon.visibility = GONE
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
