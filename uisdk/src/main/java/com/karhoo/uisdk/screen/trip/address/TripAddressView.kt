package com.karhoo.uisdk.screen.trip.address

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R

class TripAddressView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

        private lateinit var pickupLabel: TextView
        private lateinit var dropOffLabel: TextView

    init {
        inflate(context, R.layout.uisdk_view_trip_address, this)
        pickupLabel = findViewById(R.id.pickupLabel)
        dropOffLabel = findViewById(R.id.dropOffLabel)
    }

    fun bindTripPickupAndDropoff(tripDetails: TripInfo?) {
        pickupLabel.text = tripDetails?.origin?.displayAddress
        dropOffLabel.text = tripDetails?.destination?.displayAddress
    }

}
